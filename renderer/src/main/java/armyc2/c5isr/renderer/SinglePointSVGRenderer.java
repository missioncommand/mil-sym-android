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
import armyc2.c5isr.renderer.utilities.SVGSymbolInfo;
import armyc2.c5isr.renderer.utilities.SettingsChangedEvent;
import armyc2.c5isr.renderer.utilities.SettingsChangedEventListener;
import armyc2.c5isr.renderer.utilities.SymbolDimensionInfo;
import armyc2.c5isr.renderer.utilities.SymbolID;
import armyc2.c5isr.renderer.utilities.SymbolUtilities;

public class SinglePointSVGRenderer implements SettingsChangedEventListener
{

    private final String TAG = "SinglePointRenderer";
    private static SinglePointSVGRenderer _instance = null;

    private final Object _SinglePointCacheMutex = new Object();
    private final Object _UnitCacheMutex = new Object();

    private Paint _modifierFont = new Paint();
    private Paint _modifierOutlineFont = new Paint();
    private float _modifierDescent = 2;
    private float _modifierFontHeight = 10;
    private int _deviceDPI = 72;


    private SinglePointSVGRenderer()
    {
        RendererSettings.getInstance().addEventListener(this);
        
        //get modifier font values.
        onSettingsChanged(new SettingsChangedEvent(SettingsChangedEvent.EventType_FontChanged));
    }

    public static synchronized SinglePointSVGRenderer getInstance()
    {
        if (_instance == null)
        {
            _instance = new SinglePointSVGRenderer();
        }

        return _instance;
    }

    /**
     *
     * @param symbolID
     * @param modifiers
     * @return
     */
    public SVGSymbolInfo RenderUnit(String symbolID, Map<String,String> modifiers, Map<String,String> attributes)
    {
        SVGSymbolInfo si = null;
        SymbolDimensionInfo newSDI = null;

        String lineColor = null;//SymbolUtilitiesD.getLineColorOfAffiliation(symbolID);
        String fillColor = null;

        if(SymbolID.getSymbolSet(symbolID)==SymbolID.SymbolSet_MineWarfare && RendererSettings.getInstance().getSeaMineRenderMethod()==RendererSettings.SeaMineRenderMethod_MEDAL)
        {
            lineColor = RendererUtilities.colorToHexString(SymbolUtilities.getLineColorOfAffiliation(symbolID), false);
            fillColor = RendererUtilities.colorToHexString(SymbolUtilities.getFillColorOfAffiliation(symbolID), true);
        }

        String iconColor = null;

        int alpha = 255;

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
            if(attributes != null)
            {
                if (attributes.containsKey(MilStdAttributes.PixelSize)) {
                    pixelSize = Integer.parseInt(attributes.get(MilStdAttributes.PixelSize));
                } else {
                    pixelSize = RendererSettings.getInstance().getDefaultPixelSize();
                }

                if (attributes.containsKey(MilStdAttributes.KeepUnitRatio)) {
                    keepUnitRatio = Boolean.parseBoolean(attributes.get(MilStdAttributes.KeepUnitRatio));
                }

                if (attributes.containsKey(MilStdAttributes.DrawAsIcon)) {
                    icon = Boolean.parseBoolean(attributes.get(MilStdAttributes.DrawAsIcon));
                }

                if (icon)//icon won't show modifiers or display icons
                {
                    //TODO: symbolID modifications as necessary
                    keepUnitRatio = false;
                    hasDisplayModifiers = false;
                    hasTextModifiers = false;
                    //symbolID = symbolID.substring(0, 10) + "-----";
                } else {
                    hasDisplayModifiers = ModifierRenderer.hasDisplayModifiers(symbolID, modifiers);
                    hasTextModifiers = ModifierRenderer.hasTextModifiers(symbolID, modifiers);
                }

                if (attributes.containsKey(MilStdAttributes.LineColor)) {
                    lineColor = (attributes.get(MilStdAttributes.LineColor));
                }
                if (attributes.containsKey(MilStdAttributes.FillColor)) {
                    fillColor = (attributes.get(MilStdAttributes.FillColor));
                }
                if (attributes.containsKey(MilStdAttributes.IconColor)) {
                    iconColor = (attributes.get(MilStdAttributes.IconColor));
                }//*/
                if (attributes.containsKey(MilStdAttributes.Alpha)) {
                    alpha = Integer.parseInt(attributes.get(MilStdAttributes.Alpha));
                }
            }
        }
        catch (Exception excModifiers)
        {
            ErrorLogger.LogException("MilStdIconRenderer", "RenderUnit", excModifiers);
        }
        // </editor-fold>

        try
        {

            //if not, generate symbol
            if (si == null)//*/
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
                    strSVGFrame = RendererUtilities.setSVGFrameColors(symbolID,siFrame.getSVG(),RendererUtilities.getColorFromHexString(lineColor),RendererUtilities.getColorFromHexString(fillColor));
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

                //StringBuilder sbGroupUnit = new StringBuilder();
                String sbGroupUnit = "";
                if(siFrame != null)
                {
                    sbGroupUnit += ("<g transform=\"translate(" + (siFrame.getBbox().left * -ratio) + ',' + (siFrame.getBbox().top * -ratio) + ") scale(" + ratio + "," + ratio + ")\"" + ">");
                    if(siFrame != null)
                        sbGroupUnit += (strSVGFrame);//(siFrame.getSVG());

                    String color = "";
                    if(iconColor != null)
                    {
                        //make sure string is properly formatted.
                        iconColor = RendererUtilities.colorToHexString(RendererUtilities.getColorFromHexString(iconColor),false);
                        if(iconColor != null && iconColor != "#000000" && iconColor != "")
                            color = " stroke=\"" + iconColor + "\" fill=\"" + iconColor + "\" ";
                        else
                            iconColor = null;
                    }
                    String unit = "<g" + color + ">";
                    if (siIcon != null)
                        unit += (siIcon.getSVG());
                    if (siMod1 != null)
                        unit += (siMod1.getSVG());
                    if (siMod2 != null)
                        unit += (siMod2.getSVG());
                    if(iconColor != null)
                        unit = unit.replaceAll("#000000",iconColor);
                    unit += "</g>";

                    sbGroupUnit += unit + "</g>";
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
                //symbolBounds.top = 0;
                RectUtilities.shift(symbolBounds,0,(int)-symbolBounds.top);

                //Add core symbol to SVGSymbolInfo
                Point anchor = new Point(symbolBounds.centerX(),symbolBounds.centerY());
                si =  new SVGSymbolInfo(sbGroupUnit.toString(), anchor,symbolBounds,symbolBounds);

                hasDisplayModifiers = ModifierRenderer.hasDisplayModifiers(symbolID, modifiers);
                hasTextModifiers = ModifierRenderer.hasTextModifiers(symbolID, modifiers);

                //process display modifiers
                if (hasDisplayModifiers)
                {
                    newSDI = ModifierRenderer.processUnitDisplayModifiers(si, symbolID, modifiers, hasTextModifiers, attributes);
                    if(newSDI != null)
                    {
                        si = (SVGSymbolInfo) newSDI;
                        newSDI = null;
                    }
                }
            }

            //process text modifiers
            if (hasTextModifiers)
            {
                int ss = SymbolID.getSymbolSet(symbolID);
                switch(ss)
                {
                    case SymbolID.SymbolSet_LandUnit:
                    case SymbolID.SymbolSet_LandCivilianUnit_Organization:
                        if(ver >= SymbolID.Version_2525E)
                            newSDI = ModifierRenderer.processLandUnitTextModifiersE(si, symbolID, modifiers, attributes);
                        else
                            newSDI = ModifierRenderer.processLandUnitTextModifiers(si, symbolID, modifiers, attributes);
                        break;
                    case SymbolID.SymbolSet_LandEquipment:
                    case SymbolID.SymbolSet_SignalsIntelligence_Land:
                        if(ver >= SymbolID.Version_2525E)
                            newSDI = ModifierRenderer.processLandEquipmentTextModifiersE(si, symbolID, modifiers, attributes);
                        else
                            newSDI = ModifierRenderer.processLandEquipmentTextModifiers(si, symbolID, modifiers, attributes);
                        break;
                    case SymbolID.SymbolSet_LandInstallation:
                        if(ver >= SymbolID.Version_2525E)
                            newSDI = ModifierRenderer.processLandInstallationTextModifiersE(si, symbolID, modifiers, attributes);
                        else
                            newSDI = ModifierRenderer.processLandInstallationTextModifiers(si, symbolID, modifiers, attributes);
                        break;
                    case SymbolID.SymbolSet_DismountedIndividuals:
                        newSDI = ModifierRenderer.processDismountedIndividualsTextModifiers(si, symbolID, modifiers, attributes);
                        break;
                    case SymbolID.SymbolSet_Space:
                    case SymbolID.SymbolSet_SpaceMissile:
                    case SymbolID.SymbolSet_Air:
                    case SymbolID.SymbolSet_AirMissile:
                    case SymbolID.SymbolSet_SignalsIntelligence_Air:
                        if(ver >= SymbolID.Version_2525E)
                            newSDI = ModifierRenderer.processAirSpaceUnitTextModifiersE(si, symbolID, modifiers, attributes);
                        else
                            newSDI = ModifierRenderer.processAirSpaceUnitTextModifiers(si, symbolID, modifiers, attributes);
                        break;
                    case SymbolID.SymbolSet_SignalsIntelligence_Space:
                        if(ver < SymbolID.Version_2525E)
                            newSDI = ModifierRenderer.processAirSpaceUnitTextModifiers(si, symbolID, modifiers, attributes);
                        else//SIGINT in 2525E+ uses modifer places based on frame shape
                        {
                            char frameShape = SymbolID.getFrameShape(symbolID);
                            if(frameShape == SymbolID.FrameShape_Space || frameShape == SymbolID.FrameShape_Air)
                                newSDI = ModifierRenderer.processAirSpaceUnitTextModifiersE(si, symbolID, modifiers, attributes);
                            else if(frameShape == SymbolID.FrameShape_LandEquipment_SeaSurface)//sea surface, but can't tell which so default land equip
                                newSDI = ModifierRenderer.processLandEquipmentTextModifiersE(si, symbolID, modifiers, attributes);
                            else if(frameShape == SymbolID.FrameShape_SeaSubsurface)
                                newSDI = ModifierRenderer.processSeaSubSurfaceTextModifiersE(si, symbolID, modifiers, attributes);
                            else//default land equipment
                                newSDI = ModifierRenderer.processLandEquipmentTextModifiersE(si, symbolID, modifiers, attributes);
                        }
                        break;
                    case SymbolID.SymbolSet_SeaSurface:
                    case SymbolID.SymbolSet_SignalsIntelligence_SeaSurface:
                        if(ver >= SymbolID.Version_2525E)
                            newSDI = ModifierRenderer.processSeaSurfaceTextModifiersE(si, symbolID, modifiers, attributes);
                        else
                            newSDI = ModifierRenderer.processSeaSurfaceTextModifiers(si, symbolID, modifiers, attributes);
                        break;
                    case SymbolID.SymbolSet_SeaSubsurface:
                    case SymbolID.SymbolSet_SignalsIntelligence_SeaSubsurface:
                        if(ver >= SymbolID.Version_2525E)
                            newSDI = ModifierRenderer.processSeaSubSurfaceTextModifiersE(si, symbolID, modifiers, attributes);
                        else
                            newSDI = ModifierRenderer.processSeaSubSurfaceTextModifiers(si, symbolID, modifiers, attributes);
                        break;
                    case SymbolID.SymbolSet_Activities:
                        if(ver >= SymbolID.Version_2525E)
                            newSDI = ModifierRenderer.processActivitiesTextModifiersE(si, symbolID, modifiers, attributes);
                        else
                            newSDI = ModifierRenderer.processActivitiesTextModifiers(si, symbolID, modifiers, attributes);
                        break;
                    case SymbolID.SymbolSet_CyberSpace:
                        newSDI = ModifierRenderer.processCyberSpaceTextModifiers(si, symbolID, modifiers, attributes);
                        break;
                    case SymbolID.SymbolSet_MineWarfare:
                        break;//no modifiers
                    case SymbolID.SymbolSet_Unknown:
                    default: //in theory, will never get here
                        newSDI = ModifierRenderer.processUnknownTextModifiers(si, symbolID, modifiers, attributes);
                }

            }

            if (newSDI != null)
            {
                si = (SVGSymbolInfo) newSDI;
            }
            newSDI = null;

            int widthOffset = 0;
            if(hasTextModifiers)
                widthOffset = 2;//add for the text outline

            int svgWidth = (int)(si.getImageBounds().width() + widthOffset);
            int svgHeight = (int)si.getImageBounds().height();
            //add SVG tag with dimensions
            //draw unit from SVG
            String svgAlpha = "";
            if(alpha >=0 && alpha <= 255)
                svgAlpha = " opacity=\"" + alpha/255f + "\"";
            svgStart = "<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\" width=\"" + svgWidth + "\" height=\"" + svgHeight +"\" viewBox=\"" + 0 + " " + 0 + " " + svgWidth + " " + svgHeight + "\"" + svgAlpha + ">\n";
            String svgTranslateGroup = null;

            double transX = si.getImageBounds().left * -1;
            double transY = si.getImageBounds().top * -1;
            Point anchor = si.getCenterPoint();
            Rect imageBounds = si.getImageBounds();
            if(transX > 0 || transY > 0)
            {
                anchor.offset((int)transX,(int)transY);
                //ShapeUtilities.offset(anchor,transX,transY);
                RectUtilities.shift(symbolBounds,(int)transX,(int)transY);
                //ShapeUtilities.offset(symbolBounds,transX,transY);
                RectUtilities.shift(imageBounds,(int)transX,(int)transY);
                //ShapeUtilities.offset(imageBounds,transX,transY);
                svgTranslateGroup = "<g transform=\"translate(" + transX + "," + transY + ")" +"\">\n";
            }
            imageBounds = RectUtilities.makeRect(imageBounds.left,imageBounds.top,svgWidth,svgHeight);

            si = new SVGSymbolInfo(si.getSVG(),anchor,symbolBounds,imageBounds);
            StringBuilder sbSVG = new StringBuilder();
            sbSVG.append(svgStart);
            sbSVG.append(makeDescTag(si));
            sbSVG.append(makeMetadataTag(symbolID, si));
            if(svgTranslateGroup != null)
                sbSVG.append(svgTranslateGroup);
            sbSVG.append(si.getSVG());
            if(svgTranslateGroup != null)
                sbSVG.append("\n</g>");
            sbSVG.append("\n</svg>");
            si =  new SVGSymbolInfo(sbSVG.toString(),anchor,symbolBounds,imageBounds);

        }
        catch (Exception exc)
        {
            ErrorLogger.LogException("MilStdIconRenderer", "RenderUnit", exc);
        }
        return si;
    }

    /**
     *
     * @param symbolID
     * @param modifiers
     * @return
     */
    @SuppressWarnings("unused")
    public SVGSymbolInfo RenderSP(String symbolID, Map<String,String> modifiers, Map<String,String> attributes)
    {

        SVGSymbolInfo si = null;

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
        if (msi != null) {
            drawRule = msi.getDrawRule();
        }
        boolean hasAPFill = false;
        if (SymbolUtilities.isActionPoint(symbolID) || //action points
                drawRule == DrawRules.POINT10 || //Sonobuoy
                ec == 180100 || ec == 180200 || ec == 180400) //ACP, CCP, PUP
        {
            if (SymbolID.getSymbolSet(symbolID) == SymbolID.SymbolSet_ControlMeasure) {
                lineColor = Color.BLACK;
                hasAPFill = true;
            }
        }

        try
        {
            if (modifiers == null)
                modifiers = new HashMap<>();



            //get symbol info

            msi = MSLookup.getInstance().getMSLInfo(symbolID);

            if (msi == null)//if lookup fails, fix code/use unknown symbol code.
            {
                //TODO: change symbolID to Action Point with bad symbolID  in the T or H field
            }


            if (attributes != null) {
                if (attributes.containsKey(MilStdAttributes.KeepUnitRatio)) {
                    keepUnitRatio = Boolean.parseBoolean(attributes.get(MilStdAttributes.KeepUnitRatio));
                }

                if (attributes.containsKey(MilStdAttributes.LineColor)) {
                    lineColor = RendererUtilities.getColorFromHexString(attributes.get(MilStdAttributes.LineColor));
                }

                if (attributes.containsKey(MilStdAttributes.FillColor)) {
                    fillColor = RendererUtilities.getColorFromHexString(attributes.get(MilStdAttributes.FillColor));
                }

                if (attributes.containsKey(MilStdAttributes.Alpha)) {
                    alpha = Integer.parseInt(attributes.get(MilStdAttributes.Alpha));
                }

                if (attributes.containsKey(MilStdAttributes.DrawAsIcon)) {
                    drawAsIcon = Boolean.parseBoolean(attributes.get(MilStdAttributes.DrawAsIcon));
                }

                if (attributes.containsKey(MilStdAttributes.PixelSize)) {
                    pixelSize = Integer.parseInt(attributes.get(MilStdAttributes.PixelSize));
                } else {
                    pixelSize = RendererSettings.getInstance().getDefaultPixelSize();
                }
                if (keepUnitRatio == true && msi.getSymbolSet() == SymbolID.SymbolSet_ControlMeasure && msi.getGeometry().equalsIgnoreCase("point")) {
                    pixelSize = (pixelSize / 3) * 4;//try to scale to be somewhat in line with units
                }

                if (attributes.containsKey(MilStdAttributes.OutlineSymbol))
                    drawCustomOutline = Boolean.parseBoolean(attributes.get(MilStdAttributes.OutlineSymbol));
                else
                    drawCustomOutline = RendererSettings.getInstance().getOutlineSPControlMeasures();

                if (SymbolUtilities.isMultiPoint(symbolID))
                    drawCustomOutline = false;//icon previews for multipoints do not need outlines since they shouldn't be on the map
            }

            if (drawAsIcon)//icon won't show modifiers or display icons
            {
                keepUnitRatio = false;
                hasDisplayModifiers = false;
                hasTextModifiers = false;
                drawCustomOutline = false;
            } else {
                hasDisplayModifiers = ModifierRenderer.hasDisplayModifiers(symbolID, modifiers);
                hasTextModifiers = ModifierRenderer.hasTextModifiers(symbolID, modifiers);
            }

            //Check if we need to set 'N' to "ENY"
            int aff = SymbolID.getAffiliation(symbolID);
            //int ss = msi.getSymbolSet();
            if (ss == SymbolID.SymbolSet_ControlMeasure &&
                    (aff == SymbolID.StandardIdentity_Affiliation_Hostile_Faker ||
                            aff == SymbolID.StandardIdentity_Affiliation_Suspect_Joker) &&
                    modifiers.containsKey(Modifiers.N_HOSTILE) &&
                    drawAsIcon == false) {
                modifiers.put(Modifiers.N_HOSTILE, "ENY");
            }

        } catch (Exception excModifiers) {
            ErrorLogger.LogException("SinglePointSVGRenderer", "RenderSP-ParseModifiers", excModifiers);
        }

        try
        {
            int intFill = -1;
            if (fillColor != null) {
                intFill = fillColor.toInt();
            }


            if (msi.getSymbolSet() != SymbolID.SymbolSet_ControlMeasure)
                lineColor = Color.BLACK;//color isn't black but should be fine for weather since colors can't be user defined.


            if (SymbolID.getSymbolSet(symbolID) == SymbolID.SymbolSet_ControlMeasure && SymbolID.getEntityCode(symbolID) == 270701)//static depiction
            {
                //add mine fill to image
                mod1 = SymbolID.getModifier1(symbolID);
                if (!(mod1 >= 13 && mod1 <= 50))
                    symbolID = SymbolID.setModifier1(symbolID, 13);
            }


            //if not, generate symbol.
            if (si == null)//*/
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
                if (siIcon.getBbox().bottom > 400)
                    svgStart = "<svg xmlns:svg=\"http://www.w3.org/2000/svg\" version=\"1.1\" viewBox=\"0 0 612 792\">";
                else
                    svgStart = "<svg xmlns:svg=\"http://www.w3.org/2000/svg\" version=\"1.1\" viewBox=\"0 0 400 400\">";

                String strSVGIcon = null;


                if (drawRule == DrawRules.POINT1) //action points and a few others
                {//TODO: move this stroke width adjustment to the external took that makes 2525D.SVG & 2525E.SVG
                    siIcon = new SVGInfo(siIcon.getID(), siIcon.getBbox(), siIcon.getSVG().replaceAll("stroke-width=\"4\"", "stroke-width=\"6\""));
                }
                if (hasAPFill) //action points and a few others //Sonobuoy //ACP, CCP, PUP
                {
                    String apFill;
                    if (fillColor != null)
                        apFill = RendererUtilities.colorToHexString(fillColor, false);
                    else
                        apFill = RendererUtilities.colorToHexString(SymbolUtilities.getFillColorOfAffiliation(symbolID), false);
                    siIcon = new SVGInfo(siIcon.getID(), siIcon.getBbox(), siIcon.getSVG().replaceAll("fill=\"none\"", "fill=\"" + apFill + "\""));
                }

                //update line and fill color of frame SVG
                if (msi.getSymbolSet() == SymbolID.SymbolSet_ControlMeasure && (lineColor != null || fillColor != null)) {
                    if (drawCustomOutline) {
                        // create outline with larger stroke-width first (if selected)
                        strSVGIcon = RendererUtilities.setSVGSPCMColors(symbolID, siIcon.getSVG(), RendererUtilities.getIdealOutlineColor(lineColor), fillColor, true);
                    }

                    // append normal symbol SVG to be layered on top of outline
                    strSVGIcon += RendererUtilities.setSVGSPCMColors(symbolID, siIcon.getSVG(), lineColor, fillColor, false);
                } else//weather symbol (don't change color of weather graphics)
                    strSVGIcon = siIcon.getSVG();

                //If symbol is Static Depiction, add internal mine graphic based on sector modifier 1
                if (SymbolID.getEntityCode(symbolID) == 270701 && siMod1 != null) {
                    if (drawCustomOutline) {
                        // create outline with larger stroke-width first (if selected)
                        strSVGIcon += RendererUtilities.setSVGSPCMColors(mod1ID, siMod1.getSVG(), RendererUtilities.getIdealOutlineColor(RendererUtilities.getColorFromHexString("#00A651")), RendererUtilities.getColorFromHexString("#00A651"), true);
                    }
                    //strSVGIcon += siMod1.getSVG();
                    strSVGIcon += RendererUtilities.setSVGSPCMColors(mod1ID, siMod1.getSVG(), lineColor, fillColor, false);
                }

                if (pixelSize > 0) {
                    symbolBounds = RectUtilities.makeRect(left, top, width, height);
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

                    if (h / (h + borderPadding) > 0.10) {
                        borderPadding = (float) (h * 0.03);
                    } else if (w / (w + borderPadding) > 0.10) {
                        borderPadding = (float) (w * 0.03);
                    }

                }

                Rect borderPaddingBounds = null;
                int offset = 0;
                if(msi.getSymbolSet()==SymbolID.SymbolSet_ControlMeasure && drawCustomOutline && borderPadding != 0)
                {
                    borderPaddingBounds = RectUtilities.makeRect(0, 0, (rect.width()+(borderPadding)) * ratio, (rect.height()+(borderPadding)) * ratio);//.makeRect(0f, 0f, w * ratio, h * ratio);
                    symbolBounds = borderPaddingBounds;

                    //grow size SVG to accommodate the outline we added
                    offset = (int)borderPadding/2;//4;
                    RectUtilities.grow(rect, offset);
                }

                String strLineJoin = "";

                if(msi.getSymbolSet()==SymbolID.SymbolSet_ControlMeasure && msi.getDrawRule()==DrawRules.POINT1)//smooth out action points
                    strLineJoin = " stroke-linejoin=\"round\" ";

                StringBuilder sbGroupUnit = new StringBuilder();
                if(siIcon != null)
                {
                    sbGroupUnit.append("<g transform=\"translate(" + (rect.left * -ratio) + ',' + (rect.top * -ratio) + ") scale(" + ratio + "," + ratio + ")\"" + strLineJoin + ">");
                    sbGroupUnit.append(strSVGIcon);//(siIcon.getSVG());
                    sbGroupUnit.append("</g>");
                }

                //Point centerPoint = SymbolUtilities.getCMSymbolAnchorPoint(symbolID, RectUtilities.makeRectangle2DFromRect(offset, offset, symbolBounds.getWidth()-offset, symbolBounds.getHeight()-offset));
                Point centerPoint = SymbolUtilities.getCMSymbolAnchorPoint(symbolID, RectUtilities.makeRectF(0, 0, symbolBounds.width(), symbolBounds.height()));

                /*if(borderPaddingBounds != null) {
                    RectUtilities.grow(symbolBounds, 4);
                }//*/

                si = new SVGSymbolInfo(sbGroupUnit.toString(), centerPoint,symbolBounds,symbolBounds);

            }

            //Process Modifiers
            SVGSymbolInfo siNew = null;
            if (drawAsIcon == false && (hasTextModifiers || hasDisplayModifiers)) {
                SymbolDimensionInfo sdiTemp = null;
                if (SymbolUtilities.isSPWithSpecialModifierLayout(symbolID))//(SymbolUtilitiesD.isTGSPWithSpecialModifierLayout(symbolID))
                {
                    sdiTemp = ModifierRenderer.ProcessTGSPWithSpecialModifierLayout(si, symbolID, modifiers, attributes, lineColor);
                } else {
                    sdiTemp = ModifierRenderer.ProcessTGSPModifiers(si, symbolID, modifiers, attributes, lineColor);
                }
                siNew = (sdiTemp instanceof SVGSymbolInfo ? (SVGSymbolInfo)sdiTemp : null);

            }

            if (siNew != null) {
                si = siNew;
            }

            //add SVG tag with dimensions
            //draw unit from SVG
            String svgAlpha = "";
            if(alpha >=0 && alpha <= 255)
                svgAlpha = " opacity=\"" + alpha/255f + "\"";
            svgStart = "<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\" width=\"" + (int)si.getImageBounds().width() + "\" height=\"" + (int)si.getImageBounds().height() +"\" viewBox=\"" + 0 + " " + 0 + " " + (int)si.getImageBounds().width() + " " + (int)si.getImageBounds().height() + "\"" + svgAlpha + ">\n";
            String svgTranslateGroup = null;

            double transX = si.getImageBounds().left * -1;
            double transY = si.getImageBounds().top * -1;
            Point anchor = si.getCenterPoint();
            Rect imageBounds = si.getImageBounds();
            if(transX > 0 || transY > 0)
            {
                //ShapeUtilities.offset(anchor,transX,transY);
                anchor.offset(Math.round((float)transX),Math.round((float)transY));
                //ShapeUtilities.offset(symbolBounds,transX,transY);
                symbolBounds.offset((int)transX,(int)Math.ceil(transY));
                //ShapeUtilities.offset(imageBounds,transX,transY);
                imageBounds.offset((int)transX,(int)Math.ceil(transY));

                svgTranslateGroup = "<g transform=\"translate(" + transX + "," + transY + ")" +"\">\n";
            }
            si = new SVGSymbolInfo(si.getSVG(),anchor,symbolBounds,imageBounds);
            StringBuilder sbSVG = new StringBuilder();
            sbSVG.append(svgStart);
            sbSVG.append(makeDescTag(si));
            sbSVG.append(makeMetadataTag(symbolID, si));
            if(svgTranslateGroup != null)
                sbSVG.append(svgTranslateGroup);
            sbSVG.append(si.getSVG());
            if(svgTranslateGroup != null)
                sbSVG.append("\n</g>");
            sbSVG.append("\n</svg>");
            si =  new SVGSymbolInfo(sbSVG.toString(),anchor,symbolBounds,imageBounds);

            //cleanup
            //bmp.recycle();
            symbolBounds = null;
            fullBMP = null;
            fullBounds = null;
            mySVG = null;


        } catch (Exception exc) {
            ErrorLogger.LogException("SinglePointSVGRenderer", "RenderSP", exc);
            return null;
        }

        return si;

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

    private String makeDescTag(SVGSymbolInfo si)
    {
        StringBuilder sbDesc = new StringBuilder();

        if(si != null)
        {
            Rect bounds = si.getSymbolBounds();
            Rect iBounds = si.getImageBounds();
            sbDesc.append("<desc>").append(si.getCenterX()).append(" ").append(si.getCenterY()).append(" ");
            sbDesc.append(bounds.left).append(" ").append(bounds.top).append(" ").append(bounds.width()).append(" ").append(bounds.height()).append(" ");
            sbDesc.append(iBounds.left).append(" ").append(iBounds.top).append(" ").append(iBounds.width()).append(" ").append(iBounds.height());
            sbDesc.append("</desc>\n");
        }
        return sbDesc.toString();
    }

    private String makeMetadataTag(String symbolID, SVGSymbolInfo si)
    {
        StringBuilder sbDesc = new StringBuilder();

        if(si != null)
        {
            Rect bounds = si.getSymbolBounds();
            Rect iBounds = si.getImageBounds();
            sbDesc.append("<metadata>\n");
            sbDesc.append("<symbolID>").append(symbolID).append("</symbolID>\n");
            sbDesc.append("<anchor>").append(si.getCenterX()).append(" ").append(si.getCenterY()).append("</anchor>\n");
            sbDesc.append("<symbolBounds>").append(bounds.left).append(" ").append(bounds.top).append(" ").append(bounds.width()).append(" ").append(bounds.height()).append("</symbolBounds>\n");
            sbDesc.append("<imageBounds>").append(iBounds.left).append(" ").append(iBounds.top).append(" ").append(iBounds.width()).append(" ").append(iBounds.height()).append("</imageBounds>\n");;
            sbDesc.append("</metadata>\n");
        }
        return sbDesc.toString();
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
    }
}
