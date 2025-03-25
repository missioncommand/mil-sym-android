package armyc2.c5isr.renderer;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.util.LruCache;

import com.caverock.androidsvg.SVG;

import java.util.HashMap;
import java.util.Map;

import armyc2.c5isr.renderer.utilities.Color;
import armyc2.c5isr.renderer.utilities.DrawRules;
import armyc2.c5isr.renderer.utilities.ErrorLogger;
import armyc2.c5isr.renderer.utilities.ImageInfo;
import armyc2.c5isr.renderer.utilities.MSInfo;
import armyc2.c5isr.renderer.utilities.MSLookup;
import armyc2.c5isr.renderer.utilities.MilStdAttributes;
import armyc2.c5isr.renderer.utilities.Modifiers;
import armyc2.c5isr.renderer.utilities.RectUtilities;
import armyc2.c5isr.renderer.utilities.RendererSettings;
import armyc2.c5isr.renderer.utilities.RendererUtilities;
import armyc2.c5isr.renderer.utilities.SVGInfo;
import armyc2.c5isr.renderer.utilities.SVGLookup;
import armyc2.c5isr.renderer.utilities.SettingsChangedEvent;
import armyc2.c5isr.renderer.utilities.SettingsChangedEventListener;
import armyc2.c5isr.renderer.utilities.SymbolDimensionInfo;
import armyc2.c5isr.renderer.utilities.SymbolID;
import armyc2.c5isr.renderer.utilities.SymbolUtilities;

public class SinglePointRenderer implements SettingsChangedEventListener
{

    private final String TAG = "SinglePointRenderer";
    private static SinglePointRenderer _instance = null;

    private final Object _SinglePointCacheMutex = new Object();
    private final Object _UnitCacheMutex = new Object();

    private Paint _modifierFont = new Paint();
    private Paint _modifierOutlineFont = new Paint();
    private float _modifierDescent = 2;
    private float _modifierFontHeight = 10;
    private int _deviceDPI = 72;

    //private LruCache<String, ImageInfo> _unitCache = new LruCache<String, ImageInfo>(15);
    //private LruCache<String, ImageInfo> _tgCache = new LruCache<String, ImageInfo>(7);
    private LruCache<String, ImageInfo> _unitCache = new LruCache<String, ImageInfo>(1024);
    private LruCache<String, ImageInfo> _tgCache = new LruCache<String, ImageInfo>(1024);
    private final int maxMemory = (int) (Runtime.getRuntime().maxMemory());// / 1024);
    private int cacheSize = 5;//RendererSettings.getInstance().getCacheSize() / 2;
    private int maxCachedEntrySize = cacheSize / 5;
    private boolean cacheEnabled = RendererSettings.getInstance().getCacheEnabled();

    private SinglePointRenderer()
    {
        RendererSettings.getInstance().addEventListener(this);
        
        //get modifier font values.
        onSettingsChanged(new SettingsChangedEvent(SettingsChangedEvent.EventType_FontChanged));
        //set cache
        onSettingsChanged(new SettingsChangedEvent(SettingsChangedEvent.EventType_CacheSizeChanged));

    }

    public static synchronized SinglePointRenderer getInstance()
    {
        if (_instance == null)
        {
            _instance = new SinglePointRenderer();
        }

        return _instance;
    }

    /**
     *
     * @param symbolID
     * @param modifiers
     * @return
     */
    public ImageInfo RenderUnit(String symbolID, Map<String,String> modifiers, Map<String,String> attributes)
    {
        Color lineColor = SymbolUtilities.getLineColorOfAffiliation(symbolID);
        Color fillColor = SymbolUtilities.getFillColorOfAffiliation(symbolID);
        Color iconColor = null;

        int alpha = -1;


        //SVG values
        String frameID = null;
        String iconID = null;
        String mod1ID = null;
        String mod2ID = null;
        SVGInfo siFrame = null;
        SVGInfo siIcon = null;
        SVGInfo siMod1 = null;
        SVGInfo siMod2 = null;
        SVG mySVG = null;
        int top = 0;
        int left = 0;
        int width = 0;
        int height = 0;
        String svgStart = null;
        String strSVG = null;
        String strSVGFrame = null;


        Rect symbolBounds = null;
        Rect fullBounds = null;
        Bitmap fullBMP = null;

        boolean hasDisplayModifiers = false;
        boolean hasTextModifiers = false;

        int pixelSize = -1;
        boolean keepUnitRatio = true;
        boolean icon = false;
        boolean noFrame = false;

        int ver = SymbolID.getVersion(symbolID);

        // <editor-fold defaultstate="collapsed" desc="Parse Attributes">
        try
        {

            if (attributes.containsKey(MilStdAttributes.PixelSize))
            {
                pixelSize = Integer.parseInt(attributes.get(MilStdAttributes.PixelSize));
            }
            else
            {
                pixelSize = RendererSettings.getInstance().getDefaultPixelSize();
            }

            if (attributes.containsKey(MilStdAttributes.KeepUnitRatio))
            {
                keepUnitRatio = Boolean.parseBoolean(attributes.get(MilStdAttributes.KeepUnitRatio));
            }

            if (attributes.containsKey(MilStdAttributes.DrawAsIcon))
            {
                icon = Boolean.parseBoolean(attributes.get(MilStdAttributes.DrawAsIcon));
            }

            if (icon)//icon won't show modifiers or display icons
            {
                //TODO: symbolID modifications as necessary
                keepUnitRatio = false;
                hasDisplayModifiers = false;
                hasTextModifiers = false;
                //symbolID = symbolID.substring(0, 10) + "-----";
            }
            else
            {
                hasDisplayModifiers = ModifierRenderer.hasDisplayModifiers(symbolID, modifiers);
                hasTextModifiers = ModifierRenderer.hasTextModifiers(symbolID, modifiers);
            }

            if (attributes.containsKey(MilStdAttributes.LineColor))
            {
                lineColor = new Color(attributes.get(MilStdAttributes.LineColor));
            }
            if (attributes.containsKey(MilStdAttributes.FillColor))
            {
                fillColor = new Color(attributes.get(MilStdAttributes.FillColor));
            }
            if (attributes.containsKey(MilStdAttributes.IconColor))
            {
                iconColor = new Color(attributes.get(MilStdAttributes.IconColor));
            }//*/
            if (attributes.containsKey(MilStdAttributes.Alpha))
            {
                alpha = Integer.parseInt(attributes.get(MilStdAttributes.Alpha));
            }

        }
        catch (Exception excModifiers)
        {
            ErrorLogger.LogException("MilStdIconRenderer", "RenderUnit", excModifiers);
        }
        // </editor-fold>

        try
        {

            ImageInfo ii = null;
            String key = makeCacheKey(symbolID, lineColor.toInt(), fillColor.toInt(), String.valueOf(iconColor),pixelSize, keepUnitRatio, false);

            //see if it's in the cache
            if(_unitCache != null) {
                ii = _unitCache.get(key);
                //safety check in case bitmaps are getting recycled while still in the LRU cache
                if (ii != null && ii.getImage() != null && ii.getImage().isRecycled()) {
                    synchronized (_UnitCacheMutex) {
                        _unitCache.remove(key);
                        ii = null;
                    }
                }
            }
            //if not, generate symbol
            if (ii == null)//*/
            {
                int version = SymbolID.getVersion(symbolID);
                //Get SVG pieces of symbol
                frameID = SVGLookup.getFrameID(symbolID);
                iconID = SVGLookup.getMainIconID(symbolID);
                mod1ID = SVGLookup.getMod1ID(symbolID);
                mod2ID = SVGLookup.getMod2ID(symbolID);
                siFrame = SVGLookup.getInstance().getSVGLInfo(frameID, version);
                siIcon = SVGLookup.getInstance().getSVGLInfo(iconID, version);

                if(siFrame == null)
                {
                    frameID = SVGLookup.getFrameID(SymbolUtilities.reconcileSymbolID(symbolID));
                    siFrame = SVGLookup.getInstance().getSVGLInfo(frameID, version);
                    if(siFrame == null)//still no match, get unknown frame
                    {
                        frameID = SVGLookup.getFrameID(SymbolID.setSymbolSet(symbolID,SymbolID.SymbolSet_Unknown));
                        siFrame = SVGLookup.getInstance().getSVGLInfo(frameID, version);
                    }
                }

                if(siIcon == null)
                {
                        if(iconID.substring(2,8).equals("000000")==false && MSLookup.getInstance().getMSLInfo(symbolID) == null)
                            siIcon = SVGLookup.getInstance().getSVGLInfo("98100000", version);//inverted question mark
                        else if(SymbolID.getSymbolSet(symbolID) == SymbolID.SymbolSet_Unknown)
                            siIcon = SVGLookup.getInstance().getSVGLInfo("00000000", version);//question mark
                }

                siMod1 = SVGLookup.getInstance().getSVGLInfo(mod1ID, version);
                siMod2 = SVGLookup.getInstance().getSVGLInfo(mod2ID, version);
                top = Math.round(siFrame.getBbox().top);
                left = Math.round(siFrame.getBbox().left);
                width = Math.round(siFrame.getBbox().width());
                height = Math.round(siFrame.getBbox().height());
                if(siFrame.getBbox().bottom > 400)
                    svgStart = "<svg xmlns:svg=\"http://www.w3.org/2000/svg\" version=\"1.1\" viewBox=\"0 0 612 792\">";
                else
                    svgStart = "<svg xmlns:svg=\"http://www.w3.org/2000/svg\" version=\"1.1\" viewBox=\"0 0 400 400\">";

                //update line and fill color of frame SVG
                if(lineColor != null || fillColor != null)
                    strSVGFrame = RendererUtilities.setSVGFrameColors(symbolID,siFrame.getSVG(),lineColor,fillColor);
                else
                    strSVGFrame = siFrame.getSVG();

                if(frameID.equals("octagon"))//for the 1 unit symbol that doesn't have a frame: 30 + 15000
                {
                    noFrame = true;
                    strSVGFrame = strSVGFrame.replaceFirst("<g id=\"octagon\">", "<g id=\"octagon\" display=\"none\">");
                }


                //get SVG dimensions and target dimensions
                symbolBounds = RectUtilities.makeRect(left,top,width,height);
                Rect rect = new Rect(symbolBounds);
                float ratio = -1;

                if (pixelSize > 0 && keepUnitRatio == true)
                {
                    float heightRatio = SymbolUtilities.getUnitRatioHeight(symbolID);
                    float widthRatio = SymbolUtilities.getUnitRatioWidth(symbolID);

                    if(noFrame == true)//using octagon with display="none" as frame for a 1x1 shape
                    {
                        heightRatio = 1.0f;
                        widthRatio = 1.0f;
                    }

                    if (heightRatio > widthRatio)
                    {
                        pixelSize = (int) ((pixelSize / 1.5f) * heightRatio);
                    }
                    else
                    {
                        pixelSize = (int) ((pixelSize / 1.5f) * widthRatio);
                    }
                }
                if (pixelSize > 0)
                {
                    float p = pixelSize;
                    float h = rect.height();
                    float w = rect.width();

                    ratio = Math.min((p / h), (p / w));

                    symbolBounds = RectUtilities.makeRect(0f, 0f, w * ratio, h * ratio);
                }

                //center of octagon is the center of all unit symbols
                Point centerOctagon = new Point(306, 396);
                centerOctagon.offset(-left,-top);//offset for the symbol bounds x,y
                //scale center point by same ratio as the symbol
                centerOctagon = new Point((int)(centerOctagon.x * ratio), (int)(centerOctagon.y * ratio));

                //set centerpoint of the image
                Point centerPoint = centerOctagon;
                Point centerCache = new Point(centerOctagon.x, centerOctagon.y);

                //y offset to get centerpoint so we set back to zero when done.
                symbolBounds.top = 0;

                //Create destination BMP
                Bitmap bmp = Bitmap.createBitmap(symbolBounds.width(), symbolBounds.height(), Config.ARGB_8888);
                Canvas canvas = new Canvas(bmp);

                //draw unit from SVG
                StringBuilder sb = new StringBuilder();
                sb.append(svgStart);

                if(strSVGFrame != null)
                    sb.append(strSVGFrame);

                String color = "";
                String strokeFill = "";
                if(iconColor != null)
                {
                    //make sure string is properly formatted.
                    color = RendererUtilities.colorToHexString(iconColor,false);
                    if(color != null && color != "#000000" && color != "")
                        strokeFill = " stroke=\"" + color + "\" fill=\"" + color + "\" ";
                    else
                        color = null;
                }
                String unit = "<g" + strokeFill + ">";
                if (siIcon != null)
                    unit += (siIcon.getSVG());
                if (siMod1 != null)
                    unit += (siMod1.getSVG());
                if (siMod2 != null)
                    unit += (siMod2.getSVG());
                if(iconColor != null && color != null && color != "")
                    unit = unit.replaceAll("#000000",color);
                sb.append(unit + "</g>");

                sb.append("</svg>");

                strSVG = sb.toString();

                mySVG = SVG.getFromString(strSVG);
                mySVG.setDocumentViewBox(left,top,width,height);
                mySVG.renderToCanvas(canvas);


                //adjust centerpoint for HQStaff if present
                if (SymbolUtilities.isHQ(symbolID))
                {
                    PointF point1 = new PointF();
                    PointF point2 = new PointF();
                    int affiliation = SymbolID.getAffiliation(symbolID);
                    int ss = SymbolID.getStandardIdentity(symbolID);
                    if (affiliation == SymbolID.StandardIdentity_Affiliation_Friend
                            || affiliation == SymbolID.StandardIdentity_Affiliation_AssumedFriend
                            || affiliation == SymbolID.StandardIdentity_Affiliation_Neutral
                            || ss == 15 || ss == 16)//exercise joker or faker
                    {
                        point1.x = (symbolBounds.left);
                        point1.y = symbolBounds.top + (symbolBounds.height());
                        point2.x = point1.x;
                        point2.y = point1.y + symbolBounds.height();
                    }
                    else
                    {
                        point1.x = (symbolBounds.left + 1);
                        point1.y = symbolBounds.top + (symbolBounds.height() / 2);
                        point2.x = point1.x;
                        point2.y = point1.y + symbolBounds.height();
                    }
                    centerPoint = new Point((int) point2.x, (int) point2.y);
                }

                ii = new ImageInfo(bmp, centerPoint, symbolBounds);

                if(cacheEnabled && icon == false && bmp.getAllocationByteCount() <= maxCachedEntrySize)
                {
                    synchronized (_UnitCacheMutex)
                    {
                        if(_unitCache != null && _unitCache.get(key) == null)
                            _unitCache.put(key, new ImageInfo(bmp, new Point(centerCache), new Rect(symbolBounds)));
                    }
                }

                /*if(icon == false && pixelSize <= 100)
                {
                    _unitCache.put(key, new ImageInfo(bmp, new Point(centerCache), new Rect(symbolBounds)));
                }//*/
            }

            ImageInfo iiNew = null;
            SymbolDimensionInfo sdiTemp = null;
            ////////////////////////////////////////////////////////////////////
            //process display modifiers
            if (hasDisplayModifiers)
            {
                sdiTemp = ModifierRenderer.processUnitDisplayModifiers( ii, symbolID, modifiers, hasTextModifiers, attributes);
                iiNew = (sdiTemp instanceof ImageInfo ? (ImageInfo)sdiTemp : null);
                sdiTemp = null;
            }

            if (iiNew != null)
            {
                ii = iiNew;
            }
            iiNew = null;

            //process text modifiers
            if (hasTextModifiers)
            {
                int ss = SymbolID.getSymbolSet(symbolID);
                switch(ss)
                {
                    case SymbolID.SymbolSet_LandUnit:
                    case SymbolID.SymbolSet_LandCivilianUnit_Organization:
                        if(ver >= SymbolID.Version_2525E)
                            sdiTemp = ModifierRenderer.processLandUnitTextModifiersE(ii, symbolID, modifiers, attributes);
                        else
                            sdiTemp = ModifierRenderer.processLandUnitTextModifiers(ii, symbolID, modifiers, attributes);
                        break;
                    case SymbolID.SymbolSet_LandEquipment:
                    case SymbolID.SymbolSet_SignalsIntelligence_Land:
                        if(ver >= SymbolID.Version_2525E)
                            sdiTemp = ModifierRenderer.processLandEquipmentTextModifiersE(ii, symbolID, modifiers, attributes);
                        else
                            sdiTemp = ModifierRenderer.processLandEquipmentTextModifiers(ii, symbolID, modifiers, attributes);
                        break;
                    case SymbolID.SymbolSet_LandInstallation:
                        if(ver >= SymbolID.Version_2525E)
                            sdiTemp = ModifierRenderer.processLandInstallationTextModifiersE(ii, symbolID, modifiers, attributes);
                        else
                            sdiTemp = ModifierRenderer.processLandInstallationTextModifiers(ii, symbolID, modifiers, attributes);
                        break;
                    case SymbolID.SymbolSet_DismountedIndividuals:
                        sdiTemp = ModifierRenderer.processDismountedIndividualsTextModifiers(ii, symbolID, modifiers, attributes);
                        break;
                    case SymbolID.SymbolSet_Space:
                    case SymbolID.SymbolSet_SpaceMissile:
                    case SymbolID.SymbolSet_Air:
                    case SymbolID.SymbolSet_AirMissile:
                    case SymbolID.SymbolSet_SignalsIntelligence_Air:
                        if(ver >= SymbolID.Version_2525E)
                            sdiTemp = ModifierRenderer.processAirSpaceUnitTextModifiersE(ii, symbolID, modifiers, attributes);
                        else
                            sdiTemp = ModifierRenderer.processAirSpaceUnitTextModifiers(ii, symbolID, modifiers, attributes);
                        break;
                    case SymbolID.SymbolSet_SignalsIntelligence_Space:
                        if(ver < SymbolID.Version_2525E)
                            sdiTemp = ModifierRenderer.processAirSpaceUnitTextModifiers(ii, symbolID, modifiers, attributes);
                        else//SIGINT in 2525E+ uses modifer places based on frame shape
                        {
                            char frameShape = SymbolID.getFrameShape(symbolID);
                            if(frameShape == SymbolID.FrameShape_Space || frameShape == SymbolID.FrameShape_Air)
                                sdiTemp = ModifierRenderer.processAirSpaceUnitTextModifiersE(ii, symbolID, modifiers, attributes);
                            else if(frameShape == SymbolID.FrameShape_LandEquipment_SeaSurface)//sea surface, but can't tell which so default land equip
                                sdiTemp = ModifierRenderer.processLandEquipmentTextModifiersE(ii, symbolID, modifiers, attributes);
                            else if(frameShape == SymbolID.FrameShape_SeaSubsurface)
                                sdiTemp = ModifierRenderer.processSeaSubSurfaceTextModifiersE(ii, symbolID, modifiers, attributes);
                            else//default land equipment
                                sdiTemp = ModifierRenderer.processLandEquipmentTextModifiersE(ii, symbolID, modifiers, attributes);
                        }
                        break;
                    case SymbolID.SymbolSet_SeaSurface:
                    case SymbolID.SymbolSet_SignalsIntelligence_SeaSurface:
                        if(ver >= SymbolID.Version_2525E)
                            sdiTemp = ModifierRenderer.processSeaSurfaceTextModifiersE(ii, symbolID, modifiers, attributes);
                        else
                            sdiTemp = ModifierRenderer.processSeaSurfaceTextModifiers(ii, symbolID, modifiers, attributes);
                        break;
                    case SymbolID.SymbolSet_SeaSubsurface:
                    case SymbolID.SymbolSet_SignalsIntelligence_SeaSubsurface:
                        if(ver >= SymbolID.Version_2525E)
                            sdiTemp = ModifierRenderer.processSeaSubSurfaceTextModifiersE(ii, symbolID, modifiers, attributes);
                        else
                            sdiTemp = ModifierRenderer.processSeaSubSurfaceTextModifiers(ii, symbolID, modifiers, attributes);
                        break;
                    case SymbolID.SymbolSet_Activities:
                        if(ver >= SymbolID.Version_2525E)
                            sdiTemp = ModifierRenderer.processActivitiesTextModifiersE(ii, symbolID, modifiers, attributes);
                        else
                            sdiTemp = ModifierRenderer.processActivitiesTextModifiers(ii, symbolID, modifiers, attributes);
                        break;
                    case SymbolID.SymbolSet_CyberSpace:
                        sdiTemp = ModifierRenderer.processCyberSpaceTextModifiers(ii, symbolID, modifiers, attributes);
                        break;
                    case SymbolID.SymbolSet_MineWarfare:
                        break;//no modifiers
                    case SymbolID.SymbolSet_Unknown:
                    default: //in theory, will never get here
                        sdiTemp = ModifierRenderer.processUnknownTextModifiers(ii, symbolID, modifiers, attributes);
                }

            }

            iiNew = (sdiTemp instanceof ImageInfo ? (ImageInfo)sdiTemp : null);
            if (iiNew != null)
            {
                ii = iiNew;
            }
            iiNew = null;

            //cleanup///////////////////////////////////////////////////////////
            //bmp.recycle();
            symbolBounds = null;
            fullBMP = null;
            fullBounds = null;
            mySVG = null;
            ////////////////////////////////////////////////////////////////////

            if (icon == true)
            {
                return ii.getSquareImageInfo();
            }
            else
            {
                return ii;
            }

        }
        catch (Exception exc)
        {
            ErrorLogger.LogException("MilStdIconRenderer", "RenderUnit", exc);
        }
        return null;
    }

    /**
     *
     * @param symbolID
     * @param modifiers
     * @return
     */
    @SuppressWarnings("unused")
    public ImageInfo RenderSP(String symbolID, Map<String,String> modifiers, Map<String,String> attributes)
    {
        ImageInfo temp = null;
        String basicSymbolID = null;

        Color lineColor = SymbolUtilities.getDefaultLineColor(symbolID);
        Color fillColor = null;//SymbolUtilities.getFillColorOfAffiliation(symbolID);

        int alpha = -1;


        //SVG rendering variables
        MSInfo msi = null;
        String iconID = null;
        SVGInfo siIcon = null;
        String mod1ID = null;
        SVGInfo siMod1 = null;
        int top = 0;
        int left = 0;
        int width = 0;
        int height = 0;
        String svgStart = null;
        String strSVG = null;
        SVG mySVG = null;

        float ratio = 0;

        Rect symbolBounds = null;
        RectF fullBounds = null;
        Bitmap fullBMP = null;

        boolean drawAsIcon = false;
        int pixelSize = -1;
        boolean keepUnitRatio = true;
        boolean hasDisplayModifiers = false;
        boolean hasTextModifiers = false;
        boolean drawCustomOutline = false;

        msi = MSLookup.getInstance().getMSLInfo(symbolID);
        int ss = SymbolID.getSymbolSet(symbolID);
        int ec = SymbolID.getEntityCode(symbolID);
        int mod1 = 0;
        int drawRule = 0;
        if(msi!=null){drawRule = msi.getDrawRule();}
        boolean hasAPFill = false;
        if(SymbolUtilities.isActionPoint(symbolID) || //action points
                drawRule==DrawRules.POINT10 || //Sonobuoy
                ec == 180100 || ec == 180200 || ec == 180400) //ACP, CCP, PUP
        {
            if(SymbolID.getSymbolSet(symbolID)==SymbolID.SymbolSet_ControlMeasure)
            {
                lineColor = Color.BLACK;
                hasAPFill = true;
            }
        }

        try
        {
            if (modifiers == null)
            {
                modifiers = new HashMap<>();
            }


            //get symbol info

            msi = MSLookup.getInstance().getMSLInfo(symbolID);

            if (msi == null)//if lookup fails, fix code/use unknown symbol code.
            {
                //TODO: change symbolID to Action Point with bad symbolID  in the T or H field
            }

            /* Fills built into SVG
            if (SymbolUtilities.hasDefaultFill(symbolID))
            {
                fillColor = SymbolUtilities.getFillColorOfAffiliation(symbolID);
            }
            if (SymbolUtilities.isTGSPWithFill(symbolID))
            {
                fillID = SymbolUtilities.getTGFillSymbolCode(symbolID);
                if (fillID != null)
                {
                    charFillIndex = SinglePointLookup.getInstance().getCharCodeFromSymbol(fillID, symStd);
                }
            }
            else if (SymbolUtilities.isWeatherSPWithFill(symbolID))
            {
                charFillIndex = charFrameIndex + 1;
                fillColor = SymbolUtilities.getFillColorOfWeather(symbolID);

            }//*/

            if (attributes != null)
            {
                if (attributes.containsKey(MilStdAttributes.KeepUnitRatio))
                {
                    keepUnitRatio = Boolean.parseBoolean(attributes.get(MilStdAttributes.KeepUnitRatio));
                }

                if (attributes.containsKey(MilStdAttributes.LineColor))
                {
                    lineColor = RendererUtilities.getColorFromHexString(attributes.get(MilStdAttributes.LineColor));
                }

                if (attributes.containsKey(MilStdAttributes.FillColor))
                {
                    fillColor = RendererUtilities.getColorFromHexString(attributes.get(MilStdAttributes.FillColor));
                }

                if (attributes.containsKey(MilStdAttributes.Alpha))
                {
                    alpha = Integer.parseInt(attributes.get(MilStdAttributes.Alpha));
                }

                if (attributes.containsKey(MilStdAttributes.DrawAsIcon))
                {
                    drawAsIcon = Boolean.parseBoolean(attributes.get(MilStdAttributes.DrawAsIcon));
                }

                if (attributes.containsKey(MilStdAttributes.PixelSize))
                {
                    pixelSize = Integer.parseInt(attributes.get(MilStdAttributes.PixelSize));
                }
                else
                {
                    pixelSize = RendererSettings.getInstance().getDefaultPixelSize();
                }
                if(keepUnitRatio == true && msi.getSymbolSet() == SymbolID.SymbolSet_ControlMeasure && msi.getGeometry().equalsIgnoreCase("point"))
                {
                    pixelSize = (pixelSize / 3) * 4;//try to scale to be somewhat in line with units
                }

                if(attributes.containsKey(MilStdAttributes.OutlineSymbol))
                    drawCustomOutline = Boolean.parseBoolean(attributes.get(MilStdAttributes.OutlineSymbol));
                else
                    drawCustomOutline = RendererSettings.getInstance().getOutlineSPControlMeasures();

                if(SymbolUtilities.isMultiPoint(symbolID))
                    drawCustomOutline=false;//icon previews for multipoints do not need outlines since they shouldn't be on the map
            }

            if (drawAsIcon)//icon won't show modifiers or display icons
            {
                keepUnitRatio = false;
                hasDisplayModifiers = false;
                hasTextModifiers = false;
                drawCustomOutline = false;
            }
            else
            {
                hasDisplayModifiers = ModifierRenderer.hasDisplayModifiers(symbolID, modifiers);
                hasTextModifiers = ModifierRenderer.hasTextModifiers(symbolID, modifiers);
            }

            //Check if we need to set 'N' to "ENY"
            int aff = SymbolID.getAffiliation(symbolID);
            //int ss = msi.getSymbolSet();
            if (ss == SymbolID.SymbolSet_ControlMeasure &&
                    (aff == SymbolID.StandardIdentity_Affiliation_Hostile_Faker ||
                 aff == SymbolID.StandardIdentity_Affiliation_Suspect_Joker ) &&
                    modifiers.containsKey(Modifiers.N_HOSTILE) &&
                    drawAsIcon == false)
            {
                modifiers.put(Modifiers.N_HOSTILE, "ENY");
            }

        }
        catch (Exception excModifiers)
        {
            ErrorLogger.LogException("MilStdIconRenderer", "RenderSP", excModifiers);
        }

        try
        {
            ImageInfo ii = null;
            int intFill = -1;
            if (fillColor != null)
            {
                intFill = fillColor.toInt();
            }


            if(msi.getSymbolSet() != SymbolID.SymbolSet_ControlMeasure)
                lineColor = Color.BLACK;//color isn't black but should be fine for weather since colors can't be user defined.



            if (SymbolID.getSymbolSet(symbolID)==SymbolID.SymbolSet_ControlMeasure && SymbolID.getEntityCode(symbolID) == 270701)//static depiction
            {
                //add mine fill to image
                mod1 = SymbolID.getModifier1(symbolID);
                if (!(mod1 >= 13 && mod1 <= 50))
                    symbolID = SymbolID.setModifier1(symbolID, 13);
            }

            String key = makeCacheKey(symbolID, lineColor.toInt(), intFill, pixelSize, keepUnitRatio, drawCustomOutline);

            //see if it's in the cache
            if(_tgCache != null) {
                ii = _tgCache.get(key);
                //safety check in case bitmaps are getting recycled while still in the LRU cache
                if (ii != null && ii.getImage() != null && ii.getImage().isRecycled()) {
                    synchronized (_SinglePointCacheMutex) {
                        _tgCache.remove(key);
                        ii = null;
                    }
                }
            }

            //if not, generate symbol.
            if (ii == null)//*/
            {
                int version = SymbolID.getVersion(symbolID);
                //check symbol size////////////////////////////////////////////
                Rect rect = null;
                iconID = SVGLookup.getMainIconID(symbolID);
                siIcon = SVGLookup.getInstance().getSVGLInfo(iconID, version);
                mod1ID = SVGLookup.getMod1ID(symbolID);
                siMod1 = SVGLookup.getInstance().getSVGLInfo(mod1ID, version);
                float borderPadding = 0;
                if (drawCustomOutline) {
                    borderPadding = RendererUtilities.findWidestStrokeWidth(siIcon.getSVG());
                }
                top = Math.round(siIcon.getBbox().top);
                left = Math.round(siIcon.getBbox().left);
                width = Math.round(siIcon.getBbox().width());
                height = Math.round(siIcon.getBbox().height());
                if(siIcon.getBbox().bottom > 400)
                    svgStart = "<svg xmlns:svg=\"http://www.w3.org/2000/svg\" version=\"1.1\" viewBox=\"0 0 612 792\">";
                else
                    svgStart = "<svg xmlns:svg=\"http://www.w3.org/2000/svg\" version=\"1.1\" viewBox=\"0 0 400 400\">";

                String strSVGIcon = null;


                if(drawRule==DrawRules.POINT1) //action points and a few others
                {//TODO: move this stroke width adjustment to the external took that makes 2525D.SVG & 2525E.SVG
                    siIcon = new SVGInfo(siIcon.getID(),siIcon.getBbox(), siIcon.getSVG().replaceAll("stroke-width=\"4\"","stroke-width=\"6\""));
                }
                if(hasAPFill) //action points and a few others //Sonobuoy //ACP, CCP, PUP
                {
                    String apFill;
                    if(fillColor != null)
                        apFill = RendererUtilities.colorToHexString(fillColor,false);
                    else
                        apFill = RendererUtilities.colorToHexString(SymbolUtilities.getFillColorOfAffiliation(symbolID),false);
                    siIcon = new SVGInfo(siIcon.getID(),siIcon.getBbox(), siIcon.getSVG().replaceAll("fill=\"none\"","fill=\"" + apFill + "\""));
                }

                //update line and fill color of frame SVG
                if(msi.getSymbolSet() == SymbolID.SymbolSet_ControlMeasure && (lineColor != null || fillColor != null)) {
                    if (drawCustomOutline) {
                        // create outline with larger stroke-width first (if selected)
                        strSVGIcon = RendererUtilities.setSVGSPCMColors(symbolID, siIcon.getSVG(), RendererUtilities.getIdealOutlineColor(lineColor), fillColor, true);
                    }

                    // append normal symbol SVG to be layered on top of outline
                    strSVGIcon += RendererUtilities.setSVGSPCMColors(symbolID, siIcon.getSVG(), lineColor, fillColor, false);
                }
                else//weather symbol (don't change color of weather graphics)
                    strSVGIcon = siIcon.getSVG();

                //If symbol is Static Depiction, add internal mine graphic based on sector modifier 1
                if(SymbolID.getEntityCode(symbolID) == 270701 && siMod1 != null)
                {
                    if (drawCustomOutline) {
                        // create outline with larger stroke-width first (if selected)
                        strSVGIcon += RendererUtilities.setSVGSPCMColors(mod1ID, siMod1.getSVG(), RendererUtilities.getIdealOutlineColor(RendererUtilities.getColorFromHexString("#00A651")), RendererUtilities.getColorFromHexString("#00A651"), true);
                    }
                    //strSVGIcon += siMod1.getSVG();
                    strSVGIcon += RendererUtilities.setSVGSPCMColors(mod1ID, siMod1.getSVG(), lineColor, fillColor, false);
                }

                if (pixelSize > 0)
                {
                    symbolBounds = RectUtilities.makeRect(left,top,width,height);
                    rect = new Rect(symbolBounds);

                    //adjust size
                    float p = pixelSize;
                    float h = rect.height();
                    float w = rect.width();

                    ratio = Math.min((p / h), (p / w));

                    symbolBounds = RectUtilities.makeRect(0f, 0f, w * ratio, h * ratio);

                    //make sure border padding isn't excessive.
                    w = symbolBounds.width();
                    h = symbolBounds.height();

                    if(h/(h+borderPadding) > 0.10)
                    {
                        borderPadding = (float)(h * 0.1);
                    }
                    else if(w/(w+borderPadding) > 0.10)
                    {
                        borderPadding = (float)(w * 0.1);
                    }

                }

                //Draw glyphs to bitmap
                Bitmap bmp = Bitmap.createBitmap((symbolBounds.width() + Math.round(borderPadding)), (symbolBounds.height() + Math.round(borderPadding)), Config.ARGB_8888);
                Canvas canvas = new Canvas(bmp);

                symbolBounds = new Rect(0, 0, bmp.getWidth(), bmp.getHeight());

                //grow size SVG to accommodate the outline we added
                int offset = 0;
                if(drawCustomOutline)
                {
                    //TODO: maybe come up with a calculation vs just the #2, although it seems to work well.
                    RectUtilities.grow(rect, 2);
                    offset = 4;
                }//*/

                String strLineJoin = "";

                if(msi.getSymbolSet()==SymbolID.SymbolSet_ControlMeasure && msi.getDrawRule()==DrawRules.POINT1)//smooth out action points
                    strSVGIcon = "/n<g stroke-linejoin=\"round\" >/n" + strSVGIcon + "/n</g>";

                strSVG = svgStart + strSVGIcon + "</svg>";
                mySVG = SVG.getFromString(strSVG);
                //mySVG.setDocumentViewBox(left,top,width,height);
                mySVG.setDocumentViewBox(rect.left,rect.top,rect.width(),rect.height());
                mySVG.renderToCanvas(canvas);

                Point centerPoint = SymbolUtilities.getCMSymbolAnchorPoint(symbolID,new RectF(offset, offset, symbolBounds.right, symbolBounds.bottom));

                ii = new ImageInfo(bmp, centerPoint, symbolBounds);

                if(cacheEnabled && drawAsIcon == false && bmp.getAllocationByteCount() <= maxCachedEntrySize)
                {
                    synchronized (_SinglePointCacheMutex)
                    {
                        if(_tgCache.get(key) == null)
                            _tgCache.put(key, ii);
                    }
                }
                /*if (drawAsIcon == false && pixelSize <= 100)

                    _tgCache.put(key, ii);
                }//*/
            }

            //Process Modifiers
            ImageInfo iiNew = null;
            if (drawAsIcon == false && (hasTextModifiers || hasDisplayModifiers))
            {
                SymbolDimensionInfo sdiTemp = null;
                if (SymbolUtilities.isSPWithSpecialModifierLayout(symbolID))//(SymbolUtilitiesD.isTGSPWithSpecialModifierLayout(symbolID))
                {
                    sdiTemp = ModifierRenderer.ProcessTGSPWithSpecialModifierLayout(ii, symbolID, modifiers, attributes, lineColor);
                }
                else
                {
                    sdiTemp = ModifierRenderer.ProcessTGSPModifiers(ii, symbolID, modifiers, attributes, lineColor);
                }
                iiNew = (sdiTemp instanceof ImageInfo ? (ImageInfo)sdiTemp : null);
            }

            if (iiNew != null)
            {
                ii = iiNew;
            }

            //cleanup
            //bmp.recycle();
            symbolBounds = null;
            fullBMP = null;
            fullBounds = null;
            mySVG = null;


            if (drawAsIcon)
            {
                return ii.getSquareImageInfo();
            }
            else
            {
                return ii;
            }

        }
        catch (Exception exc)
        {
            ErrorLogger.LogException("MilStdIconRenderer", "RenderSP", exc);
        }
        return null;
    }


    /**
     *
     * @param symbolID
     * @return
     */
    @SuppressWarnings("unused")
    public ImageInfo RenderModifier(String symbolID, Map<String,String> attributes)
    {
        ImageInfo temp = null;
        String basicSymbolID = null;

        Color lineColor = null;
        Color fillColor = null;//SymbolUtilities.getFillColorOfAffiliation(symbolID);

        int alpha = -1;


        //SVG rendering variables
        MSInfo msi = null;
        String iconID = null;
        SVGInfo siIcon = null;
        int top = 0;
        int left = 0;
        int width = 0;
        int height = 0;
        String svgStart = null;
        String strSVG = null;
        SVG mySVG = null;

        float ratio = 0;

        Rect symbolBounds = null;
        RectF fullBounds = null;
        Bitmap fullBMP = null;

        boolean drawAsIcon = false;
        int pixelSize = -1;
        boolean keepUnitRatio = true;
        boolean hasDisplayModifiers = false;
        boolean hasTextModifiers = false;
        int symbolOutlineWidth = RendererSettings.getInstance().getSinglePointSymbolOutlineWidth();
        boolean drawCustomOutline = false;

        try
        {

            msi = MSLookup.getInstance().getMSLInfo(symbolID);
            if (attributes != null)
            {
                if (attributes.containsKey(MilStdAttributes.KeepUnitRatio))
                {
                    keepUnitRatio = Boolean.parseBoolean(attributes.get(MilStdAttributes.KeepUnitRatio));
                }

                if (attributes.containsKey(MilStdAttributes.LineColor))
                {
                    lineColor = RendererUtilities.getColorFromHexString(attributes.get(MilStdAttributes.LineColor));
                }

                if (attributes.containsKey(MilStdAttributes.FillColor))
                {
                    fillColor = RendererUtilities.getColorFromHexString(attributes.get(MilStdAttributes.FillColor));
                }

                if (attributes.containsKey(MilStdAttributes.Alpha))
                {
                    alpha = Integer.parseInt(attributes.get(MilStdAttributes.Alpha));
                }

                if (attributes.containsKey(MilStdAttributes.DrawAsIcon))
                {
                    drawAsIcon = Boolean.parseBoolean(attributes.get(MilStdAttributes.DrawAsIcon));
                }

                if (attributes.containsKey(MilStdAttributes.PixelSize))
                {
                    pixelSize = Integer.parseInt(attributes.get(MilStdAttributes.PixelSize));
                    if(msi.getSymbolSet() == SymbolID.SymbolSet_ControlMeasure)
                    {
                        if(SymbolID.getEntityCode(symbolID)==270701)//static depiction
                            pixelSize = (int)(pixelSize * 0.9);//try to scale to be somewhat in line with units
                    }
                }

                if(drawAsIcon==false)//don't outline icons because they're not going on the map
                {
                    if(attributes.containsKey(MilStdAttributes.OutlineSymbol))
                        drawCustomOutline = Boolean.parseBoolean(attributes.get(MilStdAttributes.OutlineSymbol));
                    else
                        drawCustomOutline = RendererSettings.getInstance().getOutlineSPControlMeasures();
                }

                if(SymbolUtilities.isMultiPoint(symbolID))
                    drawCustomOutline=false;//icon previews for multipoints do not need outlines since they shouldn't be on the map

                /*if (attributes.containsKey(MilStdAttributes.OutlineWidth)>=0)
                 symbolOutlineWidth = Integer.parseInt(attributes.get(MilStdAttributes.OutlineWidth));//*/
            }

            int outlineOffset = symbolOutlineWidth;
            if (drawCustomOutline && outlineOffset > 2)
            {
                outlineOffset = (outlineOffset - 1) / 2;
            }
            else
            {
                outlineOffset = 0;
            }

        }
        catch (Exception excModifiers)
        {
            ErrorLogger.LogException("MilStdIconRenderer", "RenderSP", excModifiers);
        }

        try
        {
            ImageInfo ii = null;
            int intFill = -1;
            if (fillColor != null)
            {
                intFill = fillColor.toInt();
            }


            if(msi.getSymbolSet() != SymbolID.SymbolSet_ControlMeasure)
                lineColor = Color.BLACK;//color isn't black but should be fine for weather since colors can't be user defined.


            //if not, generate symbol
            if (ii == null)//*/
            {
                int version = SymbolID.getVersion(symbolID);
                //check symbol size////////////////////////////////////////////
                Rect rect = null;

                iconID = SVGLookup.getMod1ID(symbolID);
                siIcon = SVGLookup.getInstance().getSVGLInfo(iconID, version);
                top = Math.round(siIcon.getBbox().top);
                left = Math.round(siIcon.getBbox().left);
                width = Math.round(siIcon.getBbox().width());
                height = Math.round(siIcon.getBbox().height());
                if(siIcon.getBbox().bottom > 400)
                    svgStart = "<svg xmlns:svg=\"http://www.w3.org/2000/svg\" version=\"1.1\" viewBox=\"0 0 612 792\">";
                else
                    svgStart = "<svg xmlns:svg=\"http://www.w3.org/2000/svg\" version=\"1.1\" viewBox=\"0 0 400 400\">";

                String strSVGIcon = null;
                String strSVGOutline = null;

                //update line and fill color of frame SVG
                if(msi.getSymbolSet() == SymbolID.SymbolSet_ControlMeasure && (lineColor != null || fillColor != null))
                    strSVGIcon = RendererUtilities.setSVGFrameColors(symbolID,siIcon.getSVG(),lineColor,fillColor);
                else
                    strSVGIcon = siIcon.getSVG();

                if (pixelSize > 0)
                {
                    symbolBounds = RectUtilities.makeRect(left,top,width,height);
                    rect = new Rect(symbolBounds);

                    //adjust size
                    float p = pixelSize;
                    float h = rect.height();
                    float w = rect.width();

                    ratio = Math.min((p / h), (p / w));

                    symbolBounds = RectUtilities.makeRect(0f, 0f, w * ratio, h * ratio);

                }


                //TODO: figure out how to draw an outline and adjust the symbol bounds accordingly

                //Draw glyphs to bitmap
                Bitmap bmp = Bitmap.createBitmap((symbolBounds.width()), (symbolBounds.height()), Config.ARGB_8888);
                Canvas canvas = new Canvas(bmp);

                symbolBounds = new Rect(0, 0, bmp.getWidth(), bmp.getHeight());

                strSVG = svgStart + strSVGIcon + "</svg>";
                mySVG = SVG.getFromString(strSVG);
                mySVG.setDocumentViewBox(left,top,width,height);
                mySVG.renderToCanvas(canvas);

                Point centerPoint = SymbolUtilities.getCMSymbolAnchorPoint(symbolID,new RectF(0, 0, symbolBounds.right, symbolBounds.bottom));

                ii = new ImageInfo(bmp, centerPoint, symbolBounds);


                /*if (drawAsIcon == false && pixelSize <= 100)
                {
                    _tgCache.put(key, ii);
                }//*/
            }


            //cleanup
            //bmp.recycle();
            symbolBounds = null;
            fullBMP = null;
            fullBounds = null;
            mySVG = null;


            if (drawAsIcon)
            {
                return ii.getSquareImageInfo();
            }
            else
            {
                return ii;
            }

        }
        catch (Exception exc)
        {
            ErrorLogger.LogException("MilStdIconRenderer", "RenderSP", exc);
        }
        return null;
    }



    /**
     *
     * @param symbolID
     * @param lineColor
     * @param fillColor
     * @param size
     * @param keepUnitRatio
     * @param drawOutline (only for single-point Control Measures)
     * @return
     */
    private static String makeCacheKey(String symbolID, int lineColor, int fillColor, int size, boolean keepUnitRatio, boolean drawOutline)
    {
        return makeCacheKey(symbolID, lineColor, fillColor, "null",size, keepUnitRatio, false);
    }

    private static String makeCacheKey(String symbolID, int lineColor, int fillColor, String iconColor, int size, boolean keepUnitRatio, boolean drawOutline)
    {
        //String key = symbolID.substring(0, 20) + String.valueOf(lineColor) + String.valueOf(fillColor) + String.valueOf(size) + String.valueOf(keepUnitRatio);
        String key = symbolID.substring(0, 7) + symbolID.substring(10, 20) + SymbolID.getFrameShape(symbolID) + lineColor + fillColor + iconColor + size + keepUnitRatio + drawOutline;
        return key;
    }

    public void logError(String tag, Throwable thrown)
    {
        if (tag == null || tag.equals(""))
        {
            tag = "singlePointRenderer";
        }

        String message = thrown.getMessage();
        String stack = getStackTrace(thrown);
        if (message != null)
        {
            Log.e(tag, message);
        }
        if (stack != null)
        {
            Log.e(tag, stack);
        }
    }

    public String getStackTrace(Throwable thrown)
    {
        try
        {
            if (thrown != null)
            {
                if (thrown.getStackTrace() != null)
                {
                    String eol = System.getProperty("line.separator");
                    StringBuilder sb = new StringBuilder();
                    sb.append(thrown.toString());
                    sb.append(eol);
                    for (StackTraceElement element : thrown.getStackTrace())
                    {
                        sb.append("        at ");
                        sb.append(element);
                        sb.append(eol);
                    }
                    return sb.toString();
                }
                else
                {
                    return thrown.getMessage() + "- no stack trace";
                }
            }
            else
            {
                return "no stack trace";
            }
        }
        catch (Exception exc)
        {
            Log.e("getStackTrace", exc.getMessage());
        }
        return thrown.getMessage();
    }//

    /*
     private static String PrintList(ArrayList list)
     {
     String message = "";
     for(Object item : list)
     {

     message += item.toString() + "\n";
     }
     return message;
     }//*/
    /*
     private static String PrintObjectMap(Map<String, Object> map)
     {
     Iterator<Object> itr = map.values().iterator();
     String message = "";
     String temp = null;
     while(itr.hasNext())
     {
     temp = String.valueOf(itr.next());
     if(temp != null)
     message += temp + "\n";
     }
     //ErrorLogger.LogMessage(message);
     return message;
     }//*/
    @Override
    public void onSettingsChanged(SettingsChangedEvent sce)
    {

        if(sce != null && sce.getEventType().equals(SettingsChangedEvent.EventType_FontChanged))
        {
            synchronized (_modifierFont)
            {
                _modifierFont = RendererSettings.getInstance().getModiferFont();
                _modifierOutlineFont = RendererSettings.getInstance().getModiferFont();
                FontMetrics fm = new FontMetrics();
                fm = _modifierFont.getFontMetrics();
                _modifierDescent = fm.descent;
                //_modifierFontHeight = fm.top + fm.bottom;
                _modifierFontHeight = fm.bottom - fm.top;

                _modifierFont.setStrokeWidth(RendererSettings.getInstance().getTextOutlineWidth());
                _modifierOutlineFont.setColor(Color.white.toInt());
                _deviceDPI = RendererSettings.getInstance().getDeviceDPI();

                ModifierRenderer.setModifierFont(_modifierFont, _modifierFontHeight, _modifierDescent);

            }
        }

        if(sce != null && sce.getEventType().equals(SettingsChangedEvent.EventType_CacheSizeChanged))
        {

            int cSize = RendererSettings.getInstance().getCacheSize()/2;
            //adjust unit cache
            if(cSize != cacheSize) {
                cacheSize = cSize;
                if (cacheSize >= 5)
                    maxCachedEntrySize = cacheSize / 5;
                else
                    maxCachedEntrySize = 1;

                if(cacheEnabled) //if cache enabled, update cache
                {

                    synchronized (_UnitCacheMutex) {
                        if(_unitCache != null)
                            _unitCache.evictAll();
                        _unitCache = new LruCache<String, ImageInfo>(cSize) {
                            @Override
                            protected int sizeOf(String key, ImageInfo ii) {
                                return ii.getByteCount();// / 1024;
                            }
                        };
                    }
                    //adjust tg cache
                    synchronized (_SinglePointCacheMutex) {
                        if(_tgCache != null)
                            _tgCache.evictAll();
                        _tgCache = new LruCache<String, ImageInfo>(cSize) {
                            @Override
                            protected int sizeOf(String key, ImageInfo ii) {
                                return ii.getByteCount();// / 1024;
                            }
                        };
                    }
                }
            }
        }
        if(sce != null && sce.getEventType().equals(SettingsChangedEvent.EventType_CacheToggled))
        {
            if(cacheEnabled != RendererSettings.getInstance().getCacheEnabled())
            {
                cacheEnabled = RendererSettings.getInstance().getCacheEnabled();

                if (cacheEnabled == false)
                {
                    synchronized (_SinglePointCacheMutex)
                    {
                        if (_tgCache != null)
                            _tgCache.evictAll();
                        _tgCache = null;
                    }
                    synchronized (_UnitCacheMutex)
                    {
                        if (_unitCache != null)
                            _unitCache.evictAll();
                        _unitCache = null;
                    }
                }
                else
                {
                    int cSize = RendererSettings.getInstance().getCacheSize() / 2;
                    synchronized (_SinglePointCacheMutex)
                    {
                        if(_tgCache != null)
                            _tgCache.evictAll();
                        _tgCache = new LruCache<String, ImageInfo>(cSize) {
                            @Override
                            protected int sizeOf(String key, ImageInfo ii) {
                                return ii.getByteCount();// / 1024;
                            }
                        };
                    }
                    synchronized (_UnitCacheMutex)
                    {
                        if(_unitCache != null)
                            _unitCache.evictAll();
                        _unitCache = new LruCache<String, ImageInfo>(cSize) {
                            @Override
                            protected int sizeOf(String key, ImageInfo ii) {
                                return ii.getByteCount();// / 1024;
                            }
                        };
                    }
                }
            }
        }
    }
}
