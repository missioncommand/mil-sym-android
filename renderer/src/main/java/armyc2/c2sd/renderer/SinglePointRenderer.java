package armyc2.c2sd.renderer;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetrics;
import android.graphics.Typeface;
import android.os.Environment;
import android.util.Log;
import android.util.LruCache;
import android.util.SparseArray;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import armyc2.c2sd.renderer.utilities.Color;
import armyc2.c2sd.renderer.utilities.ErrorLogger;
import armyc2.c2sd.renderer.utilities.FontManager;
import armyc2.c2sd.renderer.utilities.ImageInfo;
import armyc2.c2sd.renderer.utilities.MilStdAttributes;
import armyc2.c2sd.renderer.utilities.ModifiersTG;
import armyc2.c2sd.renderer.utilities.RectUtilities;
import armyc2.c2sd.renderer.utilities.RendererSettings;
import armyc2.c2sd.renderer.utilities.RendererUtilities;
import armyc2.c2sd.renderer.utilities.SettingsChangedEvent;
import armyc2.c2sd.renderer.utilities.SettingsChangedEventListener;
import armyc2.c2sd.renderer.utilities.SinglePointLookup;
import armyc2.c2sd.renderer.utilities.SinglePointLookupInfo;
import armyc2.c2sd.renderer.utilities.SymbolDef;
import armyc2.c2sd.renderer.utilities.SymbolDimensions;
import armyc2.c2sd.renderer.utilities.SymbolUtilities;
import armyc2.c2sd.renderer.utilities.UnitFontLookup;
import armyc2.c2sd.renderer.utilities.UnitFontLookupInfo;

public class SinglePointRenderer implements SettingsChangedEventListener
{

    private final String TAG = "SinglePointRenderer";
    private static SinglePointRenderer _instance = null;

    private Typeface _tfUnits = null;
    private Typeface _tfSP = null;
    private Typeface _tfTG = null;

    private final Object _SinglePointFontMutex = new Object();
    private final Object _UnitFontMutex = new Object();
    private final Object _ModifierFontMutex = new Object();

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

    private SinglePointRenderer()
    {
        _tfUnits = FontManager.getInstance().getTypeface(FontManager.FONT_UNIT);
        _tfSP = FontManager.getInstance().getTypeface(FontManager.FONT_SPTG);
        _tfTG = FontManager.getInstance().getTypeface(FontManager.FONT_MPTG);
        TacticalGraphicIconRenderer.setTGTypeFace(_tfTG);
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
    public ImageInfo RenderUnit(String symbolID, SparseArray<String> modifiers, SparseArray<String> attributes)
    {
        ImageInfo temp = null;

        float fontSize = RendererSettings.getInstance().getUnitFontSize();
        Color lineColor = SymbolUtilities.getLineColorOfAffiliation(symbolID);
        Color fillColor = SymbolUtilities.getFillColorOfAffiliation(symbolID);
        Color iconColor = null;
        int alpha = -1;

        int symStd = RendererSettings.getInstance().getSymbologyStandard();
        //get fill character
        int charFillIndex = -1;
        //get frame character
        int charFrameIndex = -1;
        int charSymbol1Index = -1;
        int charSymbol2Index = -1;
        int charFrameAssumeIndex = -1;
        char frameAssume;

        Paint fillPaint = null;
        Paint framePaint = null;
        Paint symbol1Paint = null;
        Paint symbol2Paint = null;
        Paint frameAssumePaint = null;

        UnitFontLookupInfo lookup = null;

        Rect symbolBounds = null;
        Rect fullBounds = null;
        Bitmap fullBMP = null;

        boolean hasDisplayModifiers = false;
        boolean hasTextModifiers = false;

        int pixelSize = -1;
        boolean keepUnitRatio = true;
        boolean icon = false;
        float[] dimensions =
        {
            0f, 0f, 0f, 0f
        };

        try
        {

            //check for symbology standard override.
            if (attributes.indexOfKey(MilStdAttributes.SymbologyStandard) >= 0)
            {
                symStd = Integer.parseInt(attributes.get(MilStdAttributes.SymbologyStandard));
            }
            //get fill character
            charFillIndex = UnitFontLookup.getFillCode(symbolID, symStd);
            //get frame character
            charFrameIndex = UnitFontLookup.getFrameCode(symbolID, charFillIndex);

            if (symStd > RendererSettings.Symbology_2525B)
            {
                char affiliation = symbolID.charAt(1);
                switch (affiliation)
                {
                    case 'P':
                    case 'A':
                    case 'S':
                    case 'G':
                    case 'M':
                        if(symbolID.charAt(2) == 'U' &&
                                (symbolID.substring(4, 6).equals("WM") ||
                                 symbolID.substring(4, 7).equals("WDM")))
                        {
                            if(symbolID.charAt(3) != 'A')
                            {
                                charFillIndex++;
                            }
                            charFrameAssumeIndex = charFillIndex - 1;
                            charFrameIndex = -1;
                        }
                        else
                        {
                            charFrameIndex = charFillIndex + 2;
                            charFrameAssumeIndex = charFillIndex + 1;
                        }
                        break;
                }
                if (charFrameAssumeIndex > 0)
                {
                    frameAssume = (char) (charFrameAssumeIndex);
                }
            }

            if (attributes.indexOfKey(MilStdAttributes.PixelSize) >= 0)
            {
                pixelSize = Integer.parseInt(attributes.get(MilStdAttributes.PixelSize));
            }
            else
            {
                pixelSize = RendererSettings.getInstance().getDefaultPixelSize();
            }

            if (attributes.indexOfKey(MilStdAttributes.KeepUnitRatio) >= 0)
            {
                keepUnitRatio = Boolean.parseBoolean(attributes.get(MilStdAttributes.KeepUnitRatio));
            }

            if (attributes.indexOfKey(MilStdAttributes.DrawAsIcon) >= 0)
            {
                icon = Boolean.parseBoolean(attributes.get(MilStdAttributes.DrawAsIcon));
            }

            if (icon)//icon won't show modifiers or display icons
            {
                keepUnitRatio = false;
                hasDisplayModifiers = false;
                hasTextModifiers = false;
                symbolID = symbolID.substring(0, 10) + "-----";
            }
            else
            {
                hasDisplayModifiers = ModifierRenderer.hasDisplayModifiers(symbolID, modifiers);
                hasTextModifiers = ModifierRenderer.hasTextModifiers(symbolID, modifiers, attributes);
            }

            if (attributes.indexOfKey(MilStdAttributes.LineColor) >= 0)
            {
                lineColor = new Color(attributes.get(MilStdAttributes.LineColor));
            }
            if (attributes.indexOfKey(MilStdAttributes.FillColor) >= 0)
            {
                fillColor = new Color(attributes.get(MilStdAttributes.FillColor));
            }
            if (attributes.indexOfKey(MilStdAttributes.IconColor) >= 0)
            {
                iconColor = new Color(attributes.get(MilStdAttributes.IconColor));
            }
            if (attributes.indexOfKey(MilStdAttributes.Alpha) >= 0)
            {
                alpha = Integer.parseInt(attributes.get(MilStdAttributes.Alpha));
            }

            //get symbol info
            lookup = UnitFontLookup.getInstance().getLookupInfo(symbolID, symStd);
            if (lookup == null)//if lookup fails, fix code/use unknown symbol code.
            {
                //if symbolID bad, do best to find a workable code
                lookup = ResolveUnitFontLookupInfo(symbolID, symStd);
            }

            
            ////////////////////////////////////////////////////////////////////
            dimensions = SymbolDimensions.getUnitBounds(charFillIndex, 50);
            symbolBounds = RectUtilities.makeRect(0f, 0f, dimensions[2], dimensions[3]);
            Rect rect = new Rect(symbolBounds);
            float ratio = -1;

            if (pixelSize > 0 && keepUnitRatio == true)
            {
                float heightRatio = UnitFontLookup.getUnitRatioHeight(charFillIndex);
                float widthRatio = UnitFontLookup.getUnitRatioWidth(charFillIndex);

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

                float fontsize = 50;
                //ratio = ratio / 72 * 96;
                fontSize = (((fontsize * ratio)));
	            //fontSize = (((fontsize * ratio) / 96) * 72);
                //fontSize = (((fontsize * ratio) / 96) * _deviceDPI);

                //ctx.font= "75pt UnitFontsC";
                //symbolBounds = SymbolDimensions.getUnitBounds(charFillIndex, (50 * ratio));
                dimensions = SymbolDimensions.getUnitBounds(charFillIndex, 50 * ratio);
                symbolBounds = RectUtilities.makeRect(0f, 0f, dimensions[2], dimensions[3]);
            }//*/

            ////////////////////////////////////////////////////////////////////
            fillPaint = new Paint();
            fillPaint.setStyle(Paint.Style.FILL);
            fillPaint.setColor(fillColor.toARGB());
            fillPaint.setTextSize(fontSize);
            fillPaint.setAntiAlias(true);
            fillPaint.setTextAlign(Align.CENTER);
            fillPaint.setTypeface(_tfUnits);
            if(alpha != -1)
                fillPaint.setAlpha(alpha);

            framePaint = new Paint();
            framePaint.setStyle(Paint.Style.FILL);
            framePaint.setColor(lineColor.toARGB());
            framePaint.setTextSize(fontSize);
            framePaint.setAntiAlias(true);
            framePaint.setTextAlign(Align.CENTER);
            framePaint.setTypeface(_tfUnits);
            if(alpha != -1)
                framePaint.setAlpha(alpha);

            symbol1Paint = new Paint();
            symbol1Paint.setStyle(Paint.Style.FILL);
            if(iconColor != null)
            {
                symbol1Paint.setColor(iconColor.toARGB());
            }
            else
            {
                symbol1Paint.setColor(lookup.getColor1().toARGB());
            }
            symbol1Paint.setTextSize(fontSize);
            symbol1Paint.setAntiAlias(true);
            symbol1Paint.setTextAlign(Align.CENTER);
            symbol1Paint.setTypeface(_tfUnits);
            if(alpha != -1)
                symbol1Paint.setAlpha(alpha);

            symbol2Paint = new Paint();
            symbol2Paint.setStyle(Paint.Style.FILL);
            symbol2Paint.setColor(lookup.getColor2().toARGB());
            symbol2Paint.setTextSize(fontSize);
            symbol2Paint.setAntiAlias(true);
            symbol2Paint.setTextAlign(Align.CENTER);
            symbol2Paint.setTypeface(_tfUnits);
            if(alpha != -1)
                symbol2Paint.setAlpha(alpha);

            frameAssumePaint = new Paint();
            frameAssumePaint.setStyle(Paint.Style.FILL);
            frameAssumePaint.setColor(Color.WHITE.toARGB());
            frameAssumePaint.setTextSize(fontSize);
            frameAssumePaint.setAntiAlias(true);
            frameAssumePaint.setTextAlign(Align.CENTER);
            frameAssumePaint.setTypeface(_tfUnits);
            if(alpha != -1)
                frameAssumePaint.setAlpha(alpha);

            
          //Just for sea mines
            if(symbolID.charAt(2) == 'U' &&
                            symbolID.substring(4, 6).equals("WM"))
            {
                if(symStd == RendererSettings.Symbology_2525B)
                {
                    fillPaint.setColor(fillColor.toARGB());
                    if(alpha != -1)
                        fillPaint.setAlpha(alpha);
                    symbol1Paint.setColor(lineColor.toARGB());
                    if(alpha != -1)
                        symbol1Paint.setAlpha(alpha);

                }
                else if(symStd == RendererSettings.Symbology_2525C)
                {
                	fillPaint.setColor(lineColor.toARGB());
                    if(alpha != -1)
                        fillPaint.setAlpha(alpha);
                }
                
            }
            else if(symbolID.charAt(2) == 'S' &&
                    symbolID.charAt(4) == 'O')//own track, //SUSPO
            {
            	fillPaint.setColor(lineColor.toARGB());
                if(alpha != -1)
                    fillPaint.setAlpha(alpha);
            }
        }
        catch (Exception excModifiers)
        {
            ErrorLogger.LogException("MilStdIconRenderer", "RenderUnit", excModifiers);
        }

        try
        {
            ImageInfo ii = null;
            String key = makeCacheKey(symbolID, lineColor.toInt(), fillColor.toInt(), symbol1Paint.getColor(), pixelSize, keepUnitRatio, symStd);

            //see if it's in the cache
            ii = _unitCache.get(key);
            //safety check in case bitmaps are getting recycled while still in the LRU cache
            if(ii != null && ii.getImage() != null && ii.getImage().isRecycled())
            {
                synchronized (_UnitCacheMutex) {
                    _unitCache.remove(key);
                    ii = null;
                }
            }
            //if not, generate symbol
            if (ii == null)//*/
            {

                if (lookup != null)
                {
                    //get Symbol1 character mapping
                    charSymbol1Index = lookup.getMapping1(symbolID);
                    //get Symbol2 character mapping
                    charSymbol2Index = lookup.getMapping2();
                }

                //dimensions of the unit at specified font size
                //dimensions = SymbolDimensions.getUnitBounds(charFillIndex, fontSize);
                //symbolBounds = RectUtilities.makeRect(0, 0, dimensions[2], dimensions[3]);
                //get centerpoint of the image
                Point centerPoint = new Point(Math.round(symbolBounds.width() / 2), Math.round(symbolBounds.height() / 2));
                Point centerCache = new Point(centerPoint);
                //y offset to get centerpoint so we set back to zero when done.
                symbolBounds.top = 0;

                //Draw glyphs to bitmap
                Bitmap bmp = Bitmap.createBitmap((int) (symbolBounds.width()), (int) (symbolBounds.height()), Config.ARGB_8888);
                Canvas canvas = new Canvas(bmp);

                //Log.i("HWA?","HWA: " + String.valueOf(canvas.isHardwareAccelerated()));
                String strFill = null;
                String strFrame = null;
                String strSymbol1 = null;
                String strSymbol2 = null;
                String strFrameAssume = null;
                if (charFillIndex > 0)
                {
                    strFill = String.valueOf((char) charFillIndex);
                }
                if (charFrameIndex > 0)
                {
                    strFrame = String.valueOf((char) charFrameIndex);
                }
                if (charSymbol1Index > 0)
                {
                    strSymbol1 = String.valueOf((char) charSymbol1Index);
                }
                if (charSymbol2Index > 0)
                {
                    strSymbol2 = String.valueOf((char) charSymbol2Index);
                }
                if (charFrameAssumeIndex > 0)
                {
                    strFrameAssume = String.valueOf((char) charFrameAssumeIndex);
                }

                //test
                /*Paint ptTest = new Paint();
                 ptTest.setColor(Color.GREEN);
                 Rect rTest = new Rect(0,0,bmp.getWidth(),bmp.getHeight());
                 canvas.drawRect(rTest, ptTest);//*/
                //end test
                synchronized(_UnitFontMutex)
                {
                    if (strFrameAssume != null && charFillIndex == -1) {
                        canvas.drawText(strFrameAssume, centerPoint.x, centerPoint.y + (int) dimensions[1], symbol2Paint);
                        strFrameAssume = null;
                    }
                    if (strFill != null) {
                        canvas.drawText(strFill, centerPoint.x, centerPoint.y + (int) dimensions[1], fillPaint);
                    }
                    if (strFrameAssume != null) {
                        canvas.drawText(strFrameAssume, centerPoint.x, centerPoint.y + (int) dimensions[1], frameAssumePaint);
                    }
                    if (strFrame != null) {
                        canvas.drawText(strFrame, centerPoint.x, centerPoint.y + (int) dimensions[1], framePaint);
                    }
                    if (strSymbol2 != null) {
                        canvas.drawText(strSymbol2, centerPoint.x, centerPoint.y + (int) dimensions[1], symbol2Paint);
                    }
                    if (strSymbol1 != null) {
                        canvas.drawText(strSymbol1, centerPoint.x, centerPoint.y + (int) dimensions[1], symbol1Paint);
                    }
                }

                //adjust centerpoint for HQStaff if present
                if (SymbolUtilities.isHQ(symbolID))
                {
                    PointF point1 = new PointF();
                    PointF point2 = new PointF();
                    char affiliation = symbolID.charAt(1);
                    if (affiliation == ('F')
                            || affiliation == ('A')
                            || affiliation == ('D')
                            || affiliation == ('M')
                            || affiliation == ('J')
                            || affiliation == ('K')
                            || affiliation == ('N')
                            || affiliation == ('L'))
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

                if(icon == false && bmp.getByteCount() <= maxCachedEntrySize)
                {
                    synchronized (_UnitCacheMutex)
                    {
                        if(_unitCache.get(key) == null)
                            _unitCache.put(key, new ImageInfo(bmp, new Point(centerCache), new Rect(symbolBounds)));
                    }
                }

                /*if(icon == false && pixelSize <= 100)
                {
                    _unitCache.put(key, new ImageInfo(bmp, new Point(centerCache), new Rect(symbolBounds)));
                }//*/
            }

            ImageInfo iinew = null;

            ////////////////////////////////////////////////////////////////////
            //process display modifiers
            if (hasDisplayModifiers)
            {
                iinew = ModifierRenderer.processUnitDisplayModifiers(ii, symbolID, modifiers, hasTextModifiers, attributes);
            }

            if (iinew != null)
            {
                ii = iinew;
            }
            iinew = null;

            //process test modifiers
            if (hasTextModifiers)
            {
                iinew = ModifierRenderer.processUnitTextModifiers(ii, symbolID, modifiers, attributes);
            }

            if (iinew != null)
            {
                ii = iinew;
            }
            iinew = null;

            //cleanup///////////////////////////////////////////////////////////
            //bmp.recycle();
            symbolBounds = null;
            fullBMP = null;
            fullBounds = null;
            //fullCanvas = null;

            fillPaint = null;
            framePaint = null;
            symbol1Paint = null;
            symbol2Paint = null;
            lookup = null;
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
        return temp;
    }

    /**
     *
     * @param symbolID
     * @param modifiers
     * @return
     */
    @SuppressWarnings("unused")
    public ImageInfo RenderSP(String symbolID, SparseArray<String> modifiers, SparseArray<String> attributes)
    {
        ImageInfo temp = null;
        String basicSymbolID = null;
        float fontSize = RendererSettings.getInstance().getSPFontSize();

        Color lineColor = SymbolUtilities.getLineColorOfAffiliation(symbolID);
        Color fillColor = null;//SymbolUtilities.getFillColorOfAffiliation(symbolID);

        int alpha = -1;

        int symStd = RendererSettings.getInstance().getSymbologyStandard();
        //fill character
        int charFillIndex = -1;
        //frame character
        int charFrameIndex = -1;
        //made up symbol ID for fill characters
        String fillID = null;

        SymbolDef sd = null;

        Paint fillPaint = null;
        Paint framePaint = null;

        SinglePointLookupInfo lookup = null;

        Rect symbolBounds = null;
        RectF fullBounds = null;
        Bitmap fullBMP = null;

        boolean drawAsIcon = false;
        int pixelSize = -1;
        boolean keepUnitRatio = true;
        boolean hasDisplayModifiers = false;
        boolean hasTextModifiers = false;
        int symbolOutlineWidth = RendererSettings.getInstance().getSinglePointSymbolOutlineWidth();
        float scale = -999;

        try
        {
            if (modifiers == null)
            {
                modifiers = new SparseArray<String>();
            }
            //get MilStdAttributes
            if (attributes != null && attributes.indexOfKey(MilStdAttributes.SymbologyStandard) >= 0)
            {
                symStd = Integer.parseInt(attributes.get(MilStdAttributes.SymbologyStandard));
            }

            //get symbol info
            basicSymbolID = SymbolUtilities.getBasicSymbolIDStrict(symbolID);
            lookup = SinglePointLookup.getInstance().getSPLookupInfo(basicSymbolID, symStd);
            if (lookup == null)//if lookup fails, fix code/use unknown symbol code.
            {
                //if symbolID bad, do best to find a workable code
                if (modifiers.get(ModifiersTG.H_ADDITIONAL_INFO_1) != null)
                {
                    modifiers.put(ModifiersTG.H1_ADDITIONAL_INFO_2, modifiers.get(ModifiersTG.H_ADDITIONAL_INFO_1));
                }
                modifiers.put(ModifiersTG.H_ADDITIONAL_INFO_1, symbolID.substring(0, 10));

                symbolID = "G" + SymbolUtilities.getAffiliation(symbolID)
                        + "G" + SymbolUtilities.getStatus(symbolID) + "GPP---****X";
                basicSymbolID = SymbolUtilities.getBasicSymbolIDStrict(symbolID);
                lookup = SinglePointLookup.getInstance().getSPLookupInfo(basicSymbolID, symStd);
                lineColor = SymbolUtilities.getLineColorOfAffiliation(symbolID);
                fillColor = null;//SymbolUtilities.getFillColorOfAffiliation(symbolID);
            }

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

            }

            if (attributes != null)
            {
                if (attributes.indexOfKey(MilStdAttributes.KeepUnitRatio) >= 0)
                {
                    keepUnitRatio = Boolean.parseBoolean(attributes.get(MilStdAttributes.KeepUnitRatio));
                }

                if (attributes.indexOfKey(MilStdAttributes.LineColor) >= 0)
                {
                    lineColor = SymbolUtilities.getColorFromHexString(attributes.get(MilStdAttributes.LineColor));
                }

                if (attributes.indexOfKey(MilStdAttributes.FillColor) >= 0)
                {
                    fillColor = SymbolUtilities.getColorFromHexString(attributes.get(MilStdAttributes.FillColor));
                }

                if (attributes.indexOfKey(MilStdAttributes.Alpha) >= 0)
                {
                    alpha = Integer.parseInt(attributes.get(MilStdAttributes.Alpha));
                }

                if (attributes.indexOfKey(MilStdAttributes.DrawAsIcon) >= 0)
                {
                    drawAsIcon = Boolean.parseBoolean(attributes.get(MilStdAttributes.DrawAsIcon));
                }

                if (attributes.indexOfKey(MilStdAttributes.PixelSize) >= 0)
                {
                    pixelSize = Integer.parseInt(attributes.get(MilStdAttributes.PixelSize));
                }
                else
                {
                    if(keepUnitRatio == true)
                        pixelSize = 35;
                    else
                    {
                        try {
                            Rect sb = SymbolDimensions.getSymbolBounds(SymbolUtilities.getBasicSymbolIDStrict(symbolID),symStd,60.0f);
                            pixelSize = Math.max(sb.width(), sb.height());
                        }
                        catch(Exception exc){
                            Log.e(TAG,exc.getMessage());
                            exc.printStackTrace();
                        }

                    }
                }



                /*if (attributes.indexOfKey(MilStdAttributes.OutlineWidth)>=0)
                 symbolOutlineWidth = Integer.parseInt(attributes.get(MilStdAttributes.OutlineWidth));//*/
            }

            if (drawAsIcon)//icon won't show modifiers or display icons
            {
                keepUnitRatio = false;
                hasDisplayModifiers = false;
                hasTextModifiers = false;
                symbolOutlineWidth = 0;
            }
            else
            {
                hasDisplayModifiers = ModifierRenderer.hasDisplayModifiers(symbolID, modifiers);
                hasTextModifiers = ModifierRenderer.hasTextModifiers(symbolID, modifiers, attributes);
            }

            int outlineOffset = symbolOutlineWidth;
            if (outlineOffset > 2)
            {
                outlineOffset = (outlineOffset - 1) / 2;
            }
            else
            {
                outlineOffset = 0;
            }

            //check symbol font size////////////////////////////////////////////
            Rect rect = null;

            float ratio = 0;

            if (pixelSize > 0)
            {
                symbolBounds = SymbolDimensions.getSymbolBounds(basicSymbolID, symStd, fontSize);
                rect = new Rect(symbolBounds);

                if (keepUnitRatio == true)
                {
	                   //scale it somehow for consistency with units.

                    //when SymbolSizeMedium = 80;
                    //a pixel size of 35 = scale value of 1.0
                    scale = pixelSize / 35.0f;
                }

                //adjust size
                float fPixelSize = (float)pixelSize;
                ratio = Math.min((fPixelSize / rect.height()), (fPixelSize / rect.width()));

            }

            //scale overrides pixel size.
            if (scale != -999)
            {
                ratio = scale;
            }

            if (ratio > 0)
            {
                fontSize = fontSize * ratio;
            }

            //symbolBounds = SymbolDimensions.getSymbolBounds(basicSymbolID, symStd, fontSize);

            ////////////////////////////////////////////////////////////////////
            if (SymbolUtilities.isTGSPWithFill(symbolID) && fillColor != null)
            {
                fillPaint = new Paint();
                fillPaint.setStyle(Paint.Style.FILL);
                fillPaint.setColor(fillColor.toARGB());
                fillPaint.setTextSize(fontSize);
                fillPaint.setAntiAlias(true);
                fillPaint.setTextAlign(Align.CENTER);
                fillPaint.setTypeface(_tfSP);
                if(alpha != -1)
                fillPaint.setAlpha(alpha);
            }

            framePaint = new Paint();
            framePaint.setStyle(Paint.Style.FILL);
            framePaint.setColor(lineColor.toARGB());
            framePaint.setTextSize(fontSize);
            framePaint.setAntiAlias(true);
            framePaint.setTextAlign(Align.CENTER);
            framePaint.setTypeface(_tfSP);
            if(alpha != -1)
                framePaint.setAlpha(alpha);

            //Check if we need to set 'N' to "ENY"
            if (symbolID.charAt(1) == 'H'
                    && modifiers.indexOfKey(MilStdAttributes.DrawAsIcon) >= 0
                    && (Boolean.parseBoolean(modifiers.get(MilStdAttributes.DrawAsIcon)) == false))
            {
                modifiers.put(ModifiersTG.N_HOSTILE, "ENY");
            }

        }
        catch (Exception excModifiers)
        {
            ErrorLogger.LogException("MilStdIconRenderer", "RenderUnit", excModifiers);
        }

        try
        {
            ImageInfo ii = null;
            int intFill = -1;
            if (fillColor != null)
            {
                intFill = fillColor.toInt();
            }
            String key = makeCacheKey(symbolID, lineColor.toInt(), intFill, pixelSize, keepUnitRatio, symStd);

            //see if it's in the cache
            ii = _tgCache.get(key);
            //safety check in case bitmaps are getting recycled while still in the LRU cache
            if(ii != null && ii.getImage() != null && ii.getImage().isRecycled())
            {
                synchronized (_SinglePointCacheMutex) {
                    _tgCache.remove(key);
                    ii = null;
                }
            }
            //if not, generate symbol
            if (ii == null)//*/
            {
                //get fill character
                //get frame character
                //get symbol info
                charFrameIndex = -1;//SinglePointLookup.instance.getCharCodeFromSymbol(symbolID);
                charFillIndex = -1;

                if (SymbolUtilities.getStatus(symbolID).equals("A"))
                {
                    charFrameIndex = lookup.getMappingA();
                }
                else
                {
                    charFrameIndex = lookup.getMappingP();
                }

                if (SymbolUtilities.isTGSPWithFill(symbolID) && fillColor != null)
                {
                    fillID = SymbolUtilities.getTGFillSymbolCode(symbolID);
                    if (fillID != null)
                    {
                        charFillIndex = SinglePointLookup.getInstance().getCharCodeFromSymbol(fillID, symStd);
                    }
                }

                //dimensions of the unit at specified font size
                RectF rect = new RectF(0, 0, lookup.getWidth(), lookup.getHeight());

                if (fontSize != 60.0)//adjust boundaries ratio if font size is not at the default setting.
                {
                    double ratio = fontSize / 60;

                    rect = new RectF(0, 0, Math.round(rect.width() * ratio), Math.round(rect.height() * ratio));
                }

                //matrix to place the symbol centered in the MilStdBmp
                Matrix matrix = new Matrix();
                Point centerPoint = null;
                centerPoint = SymbolDimensions.getSymbolCenter(lookup.getBasicSymbolID(), rect);

                if (symbolOutlineWidth > 0)
                {	//adjust matrix and centerpoint to account for outline if present
                    matrix.postTranslate(centerPoint.x + symbolOutlineWidth, centerPoint.y + symbolOutlineWidth);
                    centerPoint.offset(symbolOutlineWidth, symbolOutlineWidth);
                    rect = new RectF(0, 0, (rect.width() + (symbolOutlineWidth * 2)), (rect.height() + (symbolOutlineWidth * 2)));
                }
                else
                {
                    matrix.postTranslate(centerPoint.x, centerPoint.y);
                }

                //Draw glyphs to bitmap
                Bitmap bmp = Bitmap.createBitmap((int) (rect.width() + 0.5), (int) (rect.height() + 0.5), Config.ARGB_8888);
                Canvas canvas = new Canvas(bmp);

                symbolBounds = new Rect(0, 0, bmp.getWidth(), bmp.getHeight());

                String strFill = null;
                String strFrame = null;
                if (charFillIndex > 0)
                {
                    strFill = String.valueOf((char) charFillIndex);
                }
                if (charFrameIndex > 0)
                {
                    strFrame = String.valueOf((char) charFrameIndex);
                }

                canvas.setMatrix(matrix);
                synchronized (_SinglePointFontMutex)
                {
                    if (strFill != null)
                    {
                        canvas.drawText(strFill, 0, 0, fillPaint);
                    }

                    if (strFrame != null)
                    {
                        //try
                        //{
                        RendererUtilities.renderSymbolCharacter(canvas, strFrame, 0, 0, framePaint, lineColor, symbolOutlineWidth);
                        //}
                        //catch( Exception e){
                        //    logError(TAG,e);
                        //}

                    }
                }


                ii = new ImageInfo(bmp, centerPoint, symbolBounds);

                if(drawAsIcon == false && bmp.getByteCount() <= maxCachedEntrySize)
                {
                    synchronized (_SinglePointCacheMutex)
                    {
                        if(_tgCache.get(key) == null)
                            _tgCache.put(key, ii);
                    }
                }
                /*if (drawAsIcon == false && pixelSize <= 100)
                {
                    _tgCache.put(key, ii);
                }//*/
            }

            //Process Modifiers
            ImageInfo iiNew = null;
            if (drawAsIcon == false && (hasTextModifiers || hasDisplayModifiers || SymbolUtilities.isTGSPWithIntegralText(symbolID)))
            {
                if (SymbolUtilities.isTGSPWithSpecialModifierLayout(symbolID)
                        || SymbolUtilities.isTGSPWithIntegralText(symbolID))
                {
                    iiNew = ModifierRenderer.ProcessTGSPWithSpecialModifierLayout(ii, symbolID, modifiers, attributes, lineColor);
                }
                else
                {
                    iiNew = ModifierRenderer.ProcessTGSPModifiers(ii, symbolID, modifiers, attributes, lineColor);
                }

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

            fillPaint = null;
            framePaint = null;

            lookup = null;


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
     * Tries to get a valid UnitFontLookupInfo object when the symbolID is
     * poorly formed or there's no match in the lookup. Use this if you get a
     * null return value from:
     * "UnitFontLookupC.getInstance().getLookupInfo(symbolID)" or "CanRender"
     * returns false.
     *
     * @param symbolID
     * @return
     */
    private UnitFontLookupInfo ResolveUnitFontLookupInfo(String symbolID, int symStd)
    {
        String id = symbolID;
        UnitFontLookupInfo lookup = null;
        String affiliation = "";
        String status = "";
        if (id != null && id.length() >= 10)//if lookup fails, fix code/use unknown symbol code.
        {
            StringBuilder sb = new StringBuilder("");
            sb.append(id.charAt(0));

            if (SymbolUtilities.hasValidAffiliation(id) == false)
            {
                sb.append('U');
                affiliation = "U";
            }
            else
            {
                sb.append(id.charAt(1));
                affiliation = id.substring(1, 2);
            }

            if (SymbolUtilities.hasValidBattleDimension(id) == false)
            {
                sb.append('Z');
                sb.replace(0, 1, "S");
            }
            else
            {
                sb.append(id.charAt(2));
            }

            if (SymbolUtilities.hasValidStatus(id) == false)
            {
                sb.append('P');
                status = "P";
            }
            else
            {
                sb.append(id.charAt(3));
                status = id.substring(3, 4);
            }

            sb.append("------");
            if (id.length() >= 15)
            {
                sb.append(id.substring(10, 15));
            }
            else
            {
                sb.append("*****");
            }
            id = sb.toString();

            lookup = UnitFontLookup.getInstance().getLookupInfo(id, symStd);
        }
        else if (symbolID == null || symbolID.equals(""))
        {
            lookup = UnitFontLookup.getInstance().getLookupInfo("SUZP------*****", symStd);
        }
        return lookup;
    }

    public Bitmap getTestSymbol()
    {
        Bitmap temp = null;
        try
        {
            temp = Bitmap.createBitmap(70, 70, Config.ARGB_8888);

            Canvas canvas = new Canvas(temp);

            if (canvas.isHardwareAccelerated())
            {
                System.out.println("HW acceleration supported");
            }
			//canvas.drawColor(Color.WHITE);

            //Typeface tf = Typeface.createFromAsset(_am, "fonts/unitfonts.ttf");
            Typeface tf = _tfUnits;

            Paint fillPaint = new Paint();
            fillPaint.setStyle(Paint.Style.FILL);
            fillPaint.setColor(Color.CYAN.toInt());
            fillPaint.setTextSize(50);
            fillPaint.setAntiAlias(true);
            fillPaint.setTextAlign(Align.CENTER);
            fillPaint.setTypeface(tf);

            Paint framePaint = new Paint();
            framePaint.setStyle(Paint.Style.FILL);
            framePaint.setColor(Color.BLACK.toInt());
            framePaint.setTextSize(50);
            framePaint.setAntiAlias(true);
            framePaint.setTextAlign(Align.CENTER);
            framePaint.setTypeface(tf);

            Paint symbolPaint = new Paint();
            symbolPaint.setStyle(Paint.Style.FILL);
            symbolPaint.setColor(Color.BLACK.toInt());
            symbolPaint.setTextSize(50);
            symbolPaint.setAntiAlias(true);
            symbolPaint.setTextAlign(Align.CENTER);
            symbolPaint.setTypeface(tf);

            String strFill = String.valueOf((char) 800);
            String strFrame = String.valueOf((char) 801);
            String strSymbol = String.valueOf((char) 1121);

            canvas.drawText(strFill, 35, 35, fillPaint);
            canvas.drawText(strFrame, 35, 35, framePaint);
            canvas.drawText(strSymbol, 35, 35, symbolPaint);

            FontMetrics mf = framePaint.getFontMetrics();
            float height = mf.bottom - mf.top;
            float width = fillPaint.measureText(strFrame);

            Log.i(TAG, "top: " + String.valueOf(mf.top));
            Log.i(TAG, "bottom: " + String.valueOf(mf.bottom));
            Log.i(TAG, "ascent: " + String.valueOf(mf.ascent));
            Log.i(TAG, "descent: " + String.valueOf(mf.descent));
            Log.i(TAG, "leading: " + String.valueOf(mf.leading));
            Log.i(TAG, "width: " + String.valueOf(width));
            Log.i(TAG, "height: " + String.valueOf(height));

        }
        catch (Exception exc)
        {
            Log.e(TAG, exc.getMessage());
            Log.e(TAG, getStackTrace(exc));
        }

        return temp;
    }//*/

    public static String makeCacheKey(String symbolID, int lineColor, int fillColor, int iconColor, int size, boolean keepUnitRatio, int symStd)
    {
        String key = symbolID.substring(0, 10) + String.valueOf(lineColor) + String.valueOf(fillColor) + String.valueOf(iconColor) + String.valueOf(size) + String.valueOf(keepUnitRatio) + String.valueOf(symStd);
        return key;
    }

    public static String makeCacheKey(String symbolID, int lineColor, int fillColor, int size, boolean keepUnitRatio, int symStd)
    {
        String key = symbolID.substring(0, 10) + String.valueOf(lineColor) + String.valueOf(fillColor) + String.valueOf(size) + String.valueOf(keepUnitRatio) + String.valueOf(symStd);
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
    }//*/

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
            if(cSize != cacheSize)
            {
                synchronized(_unitCache)
                {
                    _unitCache.evictAll();
                    _unitCache = new LruCache<String, ImageInfo>(cSize)
                    {	
                        @Override
                        protected int sizeOf(String key, ImageInfo ii)
                        {
                            return ii.getByteCount();// / 1024;
                        }
                    };
                }
                //adjust tg cache
                synchronized(_tgCache)
                {
                    _tgCache.evictAll();
                    _tgCache = new LruCache<String, ImageInfo>(cSize)
                    {	
                        @Override
                        protected int sizeOf(String key, ImageInfo ii)
                        {
                            return ii.getByteCount();// / 1024;
                        }
                    };
                }
                cacheSize = cSize;
                if(cacheSize >= 5)
                    maxCachedEntrySize = cacheSize / 5;
                else
                    maxCachedEntrySize = 1;
            }
        }
    }
}
