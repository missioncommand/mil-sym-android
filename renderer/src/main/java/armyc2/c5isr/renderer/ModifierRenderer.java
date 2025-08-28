package armyc2.c5isr.renderer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.Path.Direction;
import android.graphics.Typeface;

import armyc2.c5isr.renderer.utilities.Color;
import armyc2.c5isr.renderer.utilities.ErrorLogger;
import armyc2.c5isr.renderer.utilities.GENCLookup;
import armyc2.c5isr.renderer.utilities.ImageInfo;
import armyc2.c5isr.renderer.utilities.MSInfo;
import armyc2.c5isr.renderer.utilities.MSLookup;
import armyc2.c5isr.renderer.utilities.MilStdAttributes;
import armyc2.c5isr.renderer.utilities.Modifier;
import armyc2.c5isr.renderer.utilities.Modifiers;
import armyc2.c5isr.renderer.utilities.PathUtilities;
import armyc2.c5isr.renderer.utilities.RectUtilities;
import armyc2.c5isr.renderer.utilities.RendererSettings;
import armyc2.c5isr.renderer.utilities.RendererUtilities;
import armyc2.c5isr.renderer.utilities.SVGInfo;
import armyc2.c5isr.renderer.utilities.SVGLookup;
import armyc2.c5isr.renderer.utilities.SVGPath;
import armyc2.c5isr.renderer.utilities.SVGSymbolInfo;
import armyc2.c5isr.renderer.utilities.Shape2SVG;
import armyc2.c5isr.renderer.utilities.SymbolDimensionInfo;
import armyc2.c5isr.renderer.utilities.SymbolID;
import armyc2.c5isr.renderer.utilities.SymbolUtilities;
import armyc2.c5isr.renderer.utilities.TextInfo;

/**
 * This class is used for rendering the labels/amplifiers/modifiers around the single point symbol.
 */
public class ModifierRenderer
{

    private static Paint _modifierFont = null;
    private static float _modifierFontHeight = 10f;
    private static float _modifierFontDescent = 2f;
    private static RendererSettings RS = RendererSettings.getInstance();

    private static final Object modifierFontMutex = new Object();
    public static void setModifierFont(Paint font, float height, float descent)
    {
        synchronized (modifierFontMutex) {
            _modifierFont = font;
            _modifierFontHeight = height;
            _modifierFontDescent = descent;
        }
    }

    public static SymbolDimensionInfo processUnitDisplayModifiers(SymbolDimensionInfo sdi, String symbolID, Map<String,String> modifiers, Boolean hasTextModifiers, Map<String,String> attributes)
    {

        ImageInfo ii = null;
        ImageInfo newii = null;
        SVGSymbolInfo ssi = null;
        SymbolDimensionInfo newsdi = null;
        Rect symbolBounds = new Rect(sdi.getSymbolBounds());
        Rect imageBounds = new Rect(sdi.getImageBounds());
        Point centerPoint = new Point(sdi.getCenterPoint());
        Point symbolCenter = new Point(symbolBounds.centerX(), symbolBounds.centerY());
        TextInfo tiEchelon = null;
        TextInfo tiAM = null;
        Rect echelonBounds = null;
        Rect amBounds = null;
        Color textColor = Color.BLACK;
        Color textBackgroundColor = null;
        float strokeWidth = 3.0f;
        float strokeWidthNL = 3.0f;
        Color lineColor = SymbolUtilities.getLineColorOfAffiliation(symbolID);
        Color fillColor = SymbolUtilities.getFillColorOfAffiliation(symbolID);
        int buffer = 0;
        int alpha = 255;
        //ctx = null;
        int offsetX = 0;
        int offsetY = 0;
        int pixelSize = RendererSettings.getInstance().getDefaultPixelSize();
        SVGPath svgMobilityPath = null;
        String svgMobilityGroup = null;

        Paint modifierFont = getFont(attributes);
        float[] hd = getFontHeightandDescent(modifierFont);
        float modifierFontHeight = hd[0];
        float modifierFontDescent = hd[1];

        String modifierFontName = RendererSettings.getInstance().getModiferFontProps()[0];
        if(attributes != null && attributes.containsKey(MilStdAttributes.FontFamily))
        {
            String temp = attributes.get(MilStdAttributes.FontFamily);
            if(temp != null && !temp.isEmpty())
                modifierFontName = temp;
        }

        int ss = SymbolID.getSymbolSet(symbolID);

        if (attributes.containsKey(MilStdAttributes.Alpha))
        {
            alpha = Integer.parseInt(attributes.get(MilStdAttributes.Alpha));
            textColor.setAlpha(alpha);
        }
        if (attributes.containsKey(MilStdAttributes.TextColor))
        {
            textColor = RendererUtilities.getColorFromHexString(attributes.get(MilStdAttributes.TextColor));
            if(alpha > -1)
                textColor.setAlpha(alpha);
        }
        if (attributes.containsKey(MilStdAttributes.TextBackgroundColor))
        {
            textBackgroundColor = RendererUtilities.getColorFromHexString(attributes.get(MilStdAttributes.TextBackgroundColor));
            if(alpha > -1)
                textBackgroundColor.setAlpha(alpha);
        }
        if (attributes.containsKey(MilStdAttributes.LineColor))
        {
            lineColor = RendererUtilities.getColorFromHexString(attributes.get(MilStdAttributes.LineColor));
        }
        if (attributes.containsKey(MilStdAttributes.FillColor))
        {
            fillColor = RendererUtilities.getColorFromHexString(attributes.get(MilStdAttributes.FillColor));
        }
        if(attributes.containsKey(MilStdAttributes.PixelSize))
        {
            pixelSize = Integer.parseInt(attributes.get(MilStdAttributes.PixelSize));
        }

        //float fontsize = RendererSettings.getInstance().getModiferFont().getTextSize();

        // <editor-fold defaultstate="collapsed" desc="Build Mobility Modifiers">
        float strokeWidthBasedOnDPI = 1;//min//Math.max(RendererSettings.getInstance().getDeviceDPI()/210,1);//min DPI
        strokeWidthBasedOnDPI = Math.max(pixelSize / 25f,strokeWidthBasedOnDPI);//dpi base on symbol size
        strokeWidthBasedOnDPI = Math.min(strokeWidthBasedOnDPI,RendererSettings.getInstance().getDeviceDPI()/32f);//max dpi

        RectF mobilityBounds = null;
        int ad = SymbolID.getAmplifierDescriptor(symbolID);//echelon/mobility
        List<Path> shapes = new ArrayList<Path>();
        Path mobilityPath = null;
        Path mobilityPathFill = null;
        if (ad >= SymbolID.Mobility_WheeledLimitedCrossCountry &&
                (SymbolUtilities.canSymbolHaveModifier(symbolID, Modifiers.R_MOBILITY_INDICATOR) ||
                 SymbolUtilities.canSymbolHaveModifier(symbolID, Modifiers.AG_AUX_EQUIP_INDICATOR)))
        {

            //Draw Mobility
            int fifth = (int) ((symbolBounds.width() * 0.2) + 0.5f);
            mobilityPath = new Path();
            svgMobilityPath = null;
            svgMobilityGroup = null;

            int x = 0;
            int y = 0;
            int centerX = 0;
            int bottomY = 0;
            int height = 0;
            int width = 0;
            int middleY = 0;
            int wheelOffset = 2;
            int wheelSize = fifth;//10;
            int rrHeight = fifth;//10;
            int rrArcWidth = (int) ((fifth * 1.5) + 0.5f);//16;
            float rad = wheelSize/2;


            x = (int) symbolBounds.left + 1;
            y = (int) symbolBounds.top;
            height = Math.round(symbolBounds.height());
            width = Math.round(symbolBounds.width()) - 3;
            bottomY = y + height + 2;



            if (ad >= SymbolID.Mobility_WheeledLimitedCrossCountry && //31, mobility starts above 30
                    SymbolUtilities.canSymbolHaveModifier(symbolID, Modifiers.R_MOBILITY_INDICATOR))
            {
                bottomY = y + height + 2;

                //wheelSize = width / 7;
                //rrHeight = width / 7;
                //rrArcWidth = width / 7;
                if (ad == SymbolID.Mobility_WheeledLimitedCrossCountry)
                {
                    //line
                    PathUtilities.addLine(mobilityPath, x, bottomY, x + width, bottomY);

                    //left circle
                    PathUtilities.addEllipse(mobilityPath, x, bottomY + wheelOffset, wheelSize, wheelSize);

                    //right circle
                    PathUtilities.addEllipse(mobilityPath, x + width - wheelSize, bottomY + wheelOffset, wheelSize, wheelSize);

                    //SVG
                    svgMobilityGroup = "<line x1=\"" + x + "\" y1=\"" + bottomY + "\" x2=\"" + (x + width) + "\" y2=\"" + bottomY + "\" />\n";
                    svgMobilityGroup += "<circle cx=\"" + (x + (wheelSize/2)) + "\" cy=\"" + (bottomY + wheelOffset + (wheelSize/2)) + "\" r=\"" + (wheelSize/2) + "\"  ></circle>\n";
                    svgMobilityGroup += "<circle cx=\"" + (x + width - (wheelSize/2)) + "\" cy=\"" + (bottomY + wheelOffset + (wheelSize/2)) + "\" r=\"" + (wheelSize/2) + "\"  ></circle>\n";
                }
                else if (ad == SymbolID.Mobility_WheeledCrossCountry)
                {
                    //line
                    PathUtilities.addLine(mobilityPath, x, bottomY, x + width, bottomY);

                    //left circle
                    PathUtilities.addEllipse(mobilityPath, x, bottomY + wheelOffset, wheelSize, wheelSize);

                    //right circle
                    PathUtilities.addEllipse(mobilityPath, x + width - wheelSize, bottomY + wheelOffset, wheelSize, wheelSize);

                    //center wheel
                    PathUtilities.addEllipse(mobilityPath, x + (width / 2) - (wheelSize / 2), bottomY + wheelOffset, wheelSize, wheelSize);

                    //SVG
                    svgMobilityGroup = "<line x1=\"" + x + "\" y1=\"" + bottomY + "\" x2=\"" + (x + width) + "\" y2=\"" + bottomY + "\" />\n";
                    svgMobilityGroup += "<circle cx=\"" + (x + (wheelSize/2)) + "\" cy=\"" + (bottomY + wheelOffset + (wheelSize/2)) + "\" r=\"" + (wheelSize/2) + "\" stroke=\"black\" ></circle>\n";
                    svgMobilityGroup += "<circle cx=\"" + (x + width - (wheelSize/2)) + "\" cy=\"" + (bottomY + wheelOffset + (wheelSize/2)) + "\" r=\"" + (wheelSize/2) + "\" stroke=\"black\" ></circle>\n";
                    svgMobilityGroup += "<circle cx=\"" + (x + (width/2)) + "\" cy=\"" + (bottomY + wheelOffset + (wheelSize/2)) + "\" r=\"" + (wheelSize/2) + "\" stroke=\"black\" ></circle>\n";
                }
                else if (ad == SymbolID.Mobility_Tracked)
                {
                    //round rectangle
                    PathUtilities.addRoundedRect(mobilityPath, x, bottomY, width, rrHeight, rrHeight/2, rrHeight);
                    svgMobilityGroup = "<rect width=\"" + width + "\" height=\"" + rrHeight + "\" x=\"" + x + "\" y=\"" + bottomY + "\" rx=\"" + rrHeight/2 + "\" ry=\"" + rrHeight + "\" />\n";

                }
                else if (ad == SymbolID.Mobility_Wheeled_Tracked)
                {
                    //round rectangle
                    PathUtilities.addRoundedRect(mobilityPath, x, bottomY, width, rrHeight, wheelSize/2, rrHeight);
                    svgMobilityGroup = "<rect width=\"" + width + "\" height=\"" + rrHeight + "\" x=\"" + x + "\" y=\"" + bottomY + "\" rx=\"" + rrHeight/2 + "\" ry=\"" + rrHeight + "\" />\n";

                    //left circle
                    PathUtilities.addEllipse(mobilityPath, x - wheelSize - wheelSize, bottomY, wheelSize, wheelSize);
                    svgMobilityGroup += "<circle cx=\"" + (x - wheelSize - wheelSize/2) + "\" cy=\"" + (bottomY + (wheelSize/2)) + "\" r=\"" + (wheelSize/2) + "\"  stroke=\"black\" ></circle>\n";
                }
                else if (ad == SymbolID.Mobility_Towed)
                {
                    //line
                    PathUtilities.addLine(mobilityPath, x + wheelSize, bottomY + (wheelSize / 2),
                            x + width - wheelSize, bottomY + (wheelSize / 2));

                    //left circle
                    PathUtilities.addEllipse(mobilityPath, x, bottomY, wheelSize, wheelSize);

                    //right circle
                    PathUtilities.addEllipse(mobilityPath, x + width - wheelSize, bottomY, wheelSize, wheelSize);

                    //SVG
                    svgMobilityGroup = "<line x1=\"" + (x + wheelSize) + "\" y1=\"" + (bottomY + (wheelSize/2) + "\" x2=\"" + (x + width - wheelSize) + "\" y2=\"" + (bottomY + (wheelSize/2))) + "\" />\n";
                    svgMobilityGroup += "<circle cx=\"" + (x + (wheelSize/2)) + "\" cy=\"" + (bottomY + (wheelSize/2)) + "\" r=\"" + (wheelSize/2) + "\" fill=\"none\" stroke=\"black\" ></circle>\n";
                    svgMobilityGroup += "<circle cx=\"" + (x + width - (wheelSize/2)) + "\" cy=\"" + (bottomY + (wheelSize/2)) + "\" r=\"" + (wheelSize/2) + "\" fill=\"none\" stroke=\"black\" ></circle>\n";
                }
                else if (ad == SymbolID.Mobility_Rail)
                {
                    //line
                    PathUtilities.addLine(mobilityPath, x, bottomY, x + width, bottomY);

                    //left circle
                    PathUtilities.addEllipse(mobilityPath, x + wheelSize, bottomY + wheelOffset, wheelSize, wheelSize);

                    //left circle2
                    PathUtilities.addEllipse(mobilityPath, x, bottomY + wheelOffset, wheelSize, wheelSize);

                    //right circle
                    PathUtilities.addEllipse(mobilityPath, x + width - wheelSize, bottomY + wheelOffset, wheelSize, wheelSize);

                    //right circle2
                    PathUtilities.addEllipse(mobilityPath, x + width - wheelSize - wheelSize, bottomY + wheelOffset, wheelSize, wheelSize);

                    //SVG
                    svgMobilityGroup = "<line x1=\"" + x + "\" y1=\"" + bottomY + "\" x2=\"" + (x + width) + "\" y2=\"" + bottomY + "\" />\n";

                    svgMobilityGroup += "<circle cx=\"" + (x + rad) + "\" cy=\"" + (bottomY + wheelOffset + rad) + "\" r=\"" + rad + "\" ></circle>\n";
                    svgMobilityGroup += "<circle cx=\"" + (x + rad + wheelSize) + "\" cy=\"" + (bottomY + wheelOffset + rad) + "\" r=\"" + rad + "\" ></circle>\n";

                    svgMobilityGroup += "<circle cx=\"" + (x + width - rad) + "\" cy=\"" + (bottomY + wheelOffset + rad) + "\" r=\"" + rad + "\" ></circle>\n";
                    svgMobilityGroup += "<circle cx=\"" + (x + width - rad - wheelSize) + "\" cy=\"" + (bottomY + wheelOffset + rad) + "\" r=\"" + rad + "\" ></circle>\n";

                }
                else if (ad == SymbolID.Mobility_OverSnow)
                {
                	float halfWidth = (rrArcWidth * 0.5f);
                    mobilityPath.moveTo(x, bottomY);
                    mobilityPath.lineTo(x + halfWidth, bottomY + halfWidth);
                    mobilityPath.lineTo(x + width, bottomY + halfWidth);

                    //SVG
                    svgMobilityPath = new SVGPath();
                    svgMobilityPath.moveTo(x,bottomY);
                    svgMobilityPath.lineTo(x + halfWidth, bottomY + halfWidth);
                    svgMobilityPath.lineTo(x + width, bottomY + halfWidth);

                }
                else if (ad == SymbolID.Mobility_Sled)
                {
                    mobilityPath.moveTo(x, bottomY);

                    mobilityPath.cubicTo(x, bottomY, x - rrHeight, bottomY + rrHeight/2, x, bottomY + rrHeight);
                    //mobilityPath.bezierCurveTo(x, bottomY, x-rrArcWidth, bottomY+3, x, bottomY+rrHeight);

                    mobilityPath.lineTo(x + width, bottomY + rrHeight);

                    mobilityPath.cubicTo(x + width, bottomY + rrHeight, x + width + rrHeight, bottomY + rrHeight/2, x + width, bottomY);
                    //shapeMobility.curveTo(x + width, bottomY + rrHeight, x+ width + rrArcWidth, bottomY+3, x + width, bottomY);

                    //SVG
                    svgMobilityPath = new SVGPath();
                    svgMobilityPath.moveTo(x, bottomY);
                    svgMobilityPath.bezierCurveTo(x, bottomY, x - rrHeight, bottomY + rrHeight/2, x, bottomY + rrHeight);
                    svgMobilityPath.lineTo(x + width, bottomY + rrHeight);
                    svgMobilityPath.bezierCurveTo(x + width, bottomY + rrHeight, x + width + rrHeight, bottomY + rrHeight/2, x + width, bottomY);

                }
                else if (ad == SymbolID.Mobility_PackAnimals)
                {
                    centerX = Math.round(RectUtilities.getCenterX(symbolBounds));
                    int angleWidth = rrHeight / 2;
                    mobilityPath.moveTo(centerX, bottomY + rrHeight + 2);
                    mobilityPath.lineTo(centerX - angleWidth, bottomY);
                    mobilityPath.lineTo(centerX - angleWidth*2, bottomY + rrHeight + 2);

                    mobilityPath.moveTo(centerX, bottomY + rrHeight + 2);
                    mobilityPath.lineTo(centerX + angleWidth, bottomY);
                    mobilityPath.lineTo(centerX + angleWidth*2, bottomY + rrHeight + 2);

                    //SVG
                    svgMobilityPath = new SVGPath();
                    svgMobilityPath.moveTo(centerX, bottomY + rrHeight + 2);
                    svgMobilityPath.lineTo(centerX - angleWidth, bottomY);
                    svgMobilityPath.lineTo(centerX - angleWidth*2, bottomY + rrHeight + 2);

                    svgMobilityPath.moveTo(centerX, bottomY + rrHeight + 2);
                    svgMobilityPath.lineTo(centerX + angleWidth, bottomY);
                    svgMobilityPath.lineTo(centerX + angleWidth*2, bottomY + rrHeight + 2);
                }
                else if (ad == SymbolID.Mobility_Barge)
                {
                    centerX = Math.round(RectUtilities.getCenterX(symbolBounds));
                    PathUtilities.addLine(mobilityPath, x + width, bottomY, x, bottomY);
                    //var line = new SO.Line(x + width, bottomY,x, bottomY);

                    float quarterX = (centerX - x) / 2;
                    //var quarterY = (((bottomY + rrHeight) - bottomY)/2);

                    mobilityPath.moveTo(x, bottomY);
                    mobilityPath.cubicTo(x + quarterX, bottomY + rrHeight, centerX + quarterX, bottomY + rrHeight, x + width, bottomY);
                    //shapes.push(new SO.BCurve(x, bottomY,x+quarterX, bottomY+rrHeight, centerX + quarterX, bottomY + rrHeight, x + width, bottomY));

                    //SVG
                    svgMobilityPath = new SVGPath();
                    svgMobilityPath.moveTo(x + width, bottomY);
                    svgMobilityPath.lineTo(x, bottomY);
                    svgMobilityPath.moveTo(x, bottomY);
                    svgMobilityPath.bezierCurveTo(x + quarterX, bottomY + rrHeight, centerX + quarterX, bottomY + rrHeight, x + width, bottomY);
                }

                else if (ad == SymbolID.Mobility_Amphibious)
                {
                    float incrementX = width / 7;
                    middleY = (bottomY + (rrHeight / 2));

                    x = Math.round(x + (incrementX / 2));
                    float r = Math.round(incrementX / 2);

                    //mobilityPath.arcTo(oval, sAngle, sAngle, moveTo);
                    PathUtilities.arc(mobilityPath, x, middleY, r, 180, 180);
                    PathUtilities.arc(mobilityPath, x + incrementX, middleY, r, 180, -180, false);
                    PathUtilities.arc(mobilityPath, x + incrementX * 2, middleY, r, 180, 180, false);
                    PathUtilities.arc(mobilityPath, x + incrementX * 3, middleY, r, 180, -180, false);
                    PathUtilities.arc(mobilityPath, x + incrementX * 4, middleY, r, 180, 180, false);
                    PathUtilities.arc(mobilityPath, x + incrementX * 5, middleY, r, 180, -180, false);
                    PathUtilities.arc(mobilityPath, x + incrementX * 6, middleY, r, 180, 180, false);

                    //SVG
                    x = symbolBounds.left + 1;
                    svgMobilityGroup = "<path d=\"M" + x + " " + middleY + " ";
                    svgMobilityGroup += "C " + x + " " + bottomY + " " + (x + incrementX) + " " + bottomY + " " + (x + incrementX) + " " + middleY + " ";
                    svgMobilityGroup += "C " + (x + incrementX) + " " + (bottomY + rrHeight) + " " + (x + (incrementX * 2)) + " " + (bottomY + rrHeight) + " " + (x + (incrementX*2)) + " " + middleY + " ";
                    svgMobilityGroup += "C " + (x + (incrementX*2))  + " " + bottomY + " " + (x + (incrementX*3)) + " " + bottomY + " " + (x + incrementX*3) + " " + middleY + " ";
                    svgMobilityGroup += "C " + (x + (incrementX*3)) + " " + (bottomY + rrHeight) + " " + (x + (incrementX * 4)) + " " + (bottomY + rrHeight) + " " + (x + (incrementX*4)) + " " + middleY + " ";
                    svgMobilityGroup += "C " + (x + (incrementX*4))  + " " + bottomY + " " + (x + (incrementX*5)) + " " + bottomY + " " + (x + incrementX*5) + " " + middleY + " ";
                    svgMobilityGroup += "C " + (x + (incrementX*5)) + " " + (bottomY + rrHeight) + " " + (x + (incrementX * 6)) + " " + (bottomY + rrHeight) + " " + (x + (incrementX*6)) + " " + middleY + " ";
                    svgMobilityGroup += "C " + (x + (incrementX*6))  + " " + bottomY + " " + (x + (incrementX*7)) + " " + bottomY + " " + (x + incrementX*7) + " " + middleY + " ";
                    svgMobilityGroup += "\"/>";


                }

                if(svgMobilityGroup != null)
                    svgMobilityGroup = "<g stroke-width=\"" + strokeWidthBasedOnDPI + "\" fill=\"none\"" + " stroke=\"" + RendererUtilities.colorToHexString(lineColor,false) + "\"" + ">\n" + svgMobilityGroup + "</g>\n";

            }
            //Draw Towed Array Sonar
            if ((ad == SymbolID.Mobility_ShortTowedArray || ad == SymbolID.Mobility_LongTowedArray) &&
                    SymbolUtilities.canSymbolHaveModifier(symbolID, Modifiers.AG_AUX_EQUIP_INDICATOR)) {
                int boxHeight = (int) ((rrHeight * 0.8f) + 0.5f);
                bottomY = y + height + (boxHeight / 7);
                mobilityPathFill = new Path();
                offsetY = boxHeight / 7;//1;
                centerX = Math.round(symbolBounds.left + (symbolBounds.right - symbolBounds.left) / 2);
                int squareOffset = Math.round(boxHeight * 0.5f);
                middleY = ((boxHeight / 2) + bottomY) + offsetY;//+1 for offset from symbol
                if (ad == SymbolID.Mobility_ShortTowedArray) {
                    //subtract 0.5 because lines 1 pixel thick get aliased into
                    //a line two pixels wide.
                    //line
                    PathUtilities.addLine(mobilityPath, centerX - 1, bottomY - 1, centerX - 1, bottomY + offsetY + boxHeight + offsetY);
                    //shapes.push(new SO.Line(centerX-1,bottomY-1,centerX-1, bottomY + rrHeight + 3));
                    //shapeLines.append(new Line2D.Double(centerX,bottomY - 2,centerX, bottomY + rrHeight + 1), false);
                    //line
                    PathUtilities.addLine(mobilityPath, x, middleY, x + width, middleY);
                    //shapes.push(new SO.Line(x,middleY,x + width, middleY));
                    //shapeLines.append(new Line2D.Double(x,middleY,x + width, middleY), false);
                    //square
                    mobilityPathFill.addRect(PathUtilities.makeRectF(x - squareOffset, bottomY + offsetY, boxHeight, boxHeight), Direction.CW);
                    //shapes.push(new SO.Rectangle(x-squareOffset, bottomY+offsetY, 5, 5));
                    //shapeSquares.append(new Rectangle2D.Double(x-squareOffset, bottomY, 5, 5), false);
                    //square
                    mobilityPathFill.addRect(PathUtilities.makeRectF(Math.round(centerX - squareOffset), bottomY + offsetY, boxHeight, boxHeight), Direction.CW);
                    //shapes.push(new SO.Rectangle(Math.round(centerX-squareOffset), bottomY+offsetY, 5, 5));
                    //shapeSquares.append(new Rectangle2D.Double(centerX-squareOffset, bottomY, 5, 5), false);
                    //square
                    mobilityPathFill.addRect(PathUtilities.makeRectF(x + width - squareOffset, bottomY + offsetY, boxHeight, boxHeight), Direction.CW);
                    //shapes.push(new SO.Rectangle(x + width - squareOffset, bottomY+offsetY, 5, 5));
                    //shapeSquares.append(new Rectangle2D.Double(x + width - squareOffset, bottomY, 5, 5), false);

                    //SVG
                    String svgColor = RendererUtilities.colorToHexString(lineColor,false);
                    svgMobilityGroup = "<line x1=\"" + (centerX - 1) + "\" y1=\"" + (bottomY - 1) + "\" x2=\"" + (centerX - 1) + "\" y2=\"" + (bottomY + offsetY + boxHeight + offsetY) + "\" stroke=\"" + svgColor + "\" stroke-width=\"" + strokeWidthBasedOnDPI + "\" />\n";
                    svgMobilityGroup += "<line x1=\"" + (x) + "\" y1=\"" + (middleY) + "\" x2=\"" + (x + width) + "\" y2=\"" + (middleY) + "\" stroke=\"" + svgColor + "\" stroke-width=\"" + strokeWidthBasedOnDPI + "\" />\n";
                    svgMobilityGroup += "<rect width=\"" + (boxHeight) + "\" height=\"" + (boxHeight) + "\" x=\"" + (x - squareOffset) + "\" y=\"" + (bottomY + offsetY) + "\" fill=\"" + svgColor + "\" stroke-width=\"0\"/>\n";
                    svgMobilityGroup += "<rect width=\"" + (boxHeight) + "\" height=\"" + (boxHeight) + "\" x=\"" + (centerX - squareOffset) + "\" y=\"" + (bottomY + offsetY) + "\" fill=\"" + svgColor + "\" stroke-width=\"0\"/>\n";
                    svgMobilityGroup += "<rect width=\"" + (boxHeight) + "\" height=\"" + (boxHeight) + "\" x=\"" + (x + width - squareOffset) + "\" y=\"" + (bottomY + offsetY) + "\" fill=\"" + svgColor + "\" stroke-width=\"0\"/>\n";


                } else if (ad == SymbolID.Mobility_LongTowedArray) {
                    int leftX = x + (centerX - x) / 2,
                            rightX = centerX + (x + width - centerX) / 2;

                    //line vertical left
                    PathUtilities.addLine(mobilityPath, leftX, bottomY - 1, leftX, bottomY + offsetY + boxHeight + offsetY);
                    //shapes.push(new SO.Line(leftX,bottomY - 1,leftX, bottomY + rrHeight + 3));
                    //shapeLines.append(new Line2D.Double(leftX,bottomY - 2,leftX, bottomY + rrHeight + 1), false);
                    //line vertical right
                    PathUtilities.addLine(mobilityPath, rightX, bottomY - 1, rightX, bottomY + offsetY + boxHeight + offsetY);
                    //shapes.push(new SO.Line(rightX,bottomY - 1,rightX, bottomY + rrHeight + 3));
                    //shapeLines.append(new Line2D.Double(rightX,bottomY - 2,rightX, bottomY + rrHeight + 1), false);
                    //line horizontal
                    PathUtilities.addLine(mobilityPath, x, middleY, x + width, middleY);
                    //shapes.push(new SO.Line(x,middleY,x + width, middleY));
                    //shapeLines.append(new Line2D.Double(x,middleY,x + width, middleY), false);
                    //square left
                    mobilityPathFill.addRect(PathUtilities.makeRectF(x - squareOffset, bottomY + offsetY, boxHeight, boxHeight), Direction.CW);
                    //shapes.push(new SO.Rectangle(x-squareOffset, bottomY+offsetY, 5, 5));
                    //shapeSquares.append(new Rectangle2D.Double(x-squareOffset, bottomY, 5, 5), false);
                    //square middle
                    mobilityPathFill.addRect(PathUtilities.makeRectF(centerX - squareOffset, bottomY + offsetY, boxHeight, boxHeight), Direction.CW);
                    //shapes.push(new SO.Rectangle(centerX-squareOffset, bottomY+offsetY, 5, 5));
                    //shapeSquares.append(new Rectangle2D.Double(centerX-squareOffset, bottomY, 5, 5), false);
                    //square right
                    mobilityPathFill.addRect(PathUtilities.makeRectF(x + width - squareOffset, bottomY + offsetY, boxHeight, boxHeight), Direction.CW);
                    //shapes.push(new SO.Rectangle(x + width - squareOffset, bottomY+offsetY, 5, 5));
                    //shapeSquares.append(new Rectangle2D.Double(x + width - squareOffset, bottomY, 5, 5), false);
                    //square middle left
                    mobilityPathFill.addRect(PathUtilities.makeRectF(leftX - squareOffset, bottomY + offsetY, boxHeight, boxHeight), Direction.CW);
                    //shapes.push(new SO.Rectangle(leftX - squareOffset, bottomY+offsetY, 5, 5));
                    //shapeSquares.append(new Rectangle2D.Double(leftX - squareOffset, bottomY, 5, 5), false);
                    //square middle right
                    mobilityPathFill.addRect(PathUtilities.makeRectF(rightX - squareOffset, bottomY + offsetY, boxHeight, boxHeight), Direction.CW);
                    //shapes.push(new SO.Rectangle(rightX - squareOffset, bottomY+offsetY, 5, 5));
                    //shapeSquares.append(new Rectangle2D.Double(rightX - squareOffset, bottomY, 5, 5), false);

                    //SVG
                    String svgColor = RendererUtilities.colorToHexString(lineColor,false);
                    svgMobilityGroup = "<line x1=\"" + (leftX) + "\" y1=\"" + (bottomY - 1) + "\" x2=\"" + (leftX) + "\" y2=\"" + (bottomY + offsetY + boxHeight + offsetY) + "\" stroke=\"" + svgColor + "\" stroke-width=\"" + strokeWidthBasedOnDPI + "\" />\n";
                    svgMobilityGroup += "<line x1=\"" + (rightX) + "\" y1=\"" + (bottomY - 1) + "\" x2=\"" + (rightX) + "\" y2=\"" + (bottomY + offsetY + boxHeight + offsetY) + "\" stroke=\"" + svgColor + "\" stroke-width=\"" + strokeWidthBasedOnDPI + "\" />\n";
                    svgMobilityGroup += "<line x1=\"" + (x) + "\" y1=\"" + (middleY) + "\" x2=\"" + (x + width) + "\" y2=\"" + (middleY) + "\" stroke=\"" + svgColor + "\" stroke-width=\"" + strokeWidthBasedOnDPI + "\" />\n";
                    svgMobilityGroup += "<rect width=\"" + (boxHeight) + "\" height=\"" + (boxHeight) + "\" x=\"" + (x - squareOffset) + "\" y=\"" + (bottomY + offsetY) + "\" fill=\"" + svgColor + "\" stroke-width=\"0\"/>\n";
                    svgMobilityGroup += "<rect width=\"" + (boxHeight) + "\" height=\"" + (boxHeight) + "\" x=\"" + (centerX - squareOffset) + "\" y=\"" + (bottomY + offsetY) + "\" fill=\"" + svgColor + "\" stroke-width=\"0\"/>\n";
                    svgMobilityGroup += "<rect width=\"" + (boxHeight) + "\" height=\"" + (boxHeight) + "\" x=\"" + (x + width - squareOffset) + "\" y=\"" + (bottomY + offsetY) + "\" fill=\"" + svgColor + "\" stroke-width=\"0\"/>\n";
                    svgMobilityGroup += "<rect width=\"" + (boxHeight) + "\" height=\"" + (boxHeight) + "\" x=\"" + (leftX - squareOffset) + "\" y=\"" + (bottomY + offsetY) + "\" fill=\"" + svgColor + "\" stroke-width=\"0\"/>\n";
                    svgMobilityGroup += "<rect width=\"" + (boxHeight) + "\" height=\"" + (boxHeight) + "\" x=\"" + (rightX - squareOffset) + "\" y=\"" + (bottomY + offsetY) + "\" fill=\"" + svgColor + "\" stroke-width=\"0\"/>\n";

                }
                if(svgMobilityGroup != null)
                    svgMobilityGroup = "<g stroke-width=\"" + strokeWidthBasedOnDPI + "\" fill=\"" + RendererUtilities.colorToHexString(lineColor,false) + "\"" + " stroke=\"" + RendererUtilities.colorToHexString(lineColor,false) + "\"" +  ">\n" + svgMobilityGroup + "\n</g>";
            }

            //get mobility bounds
            if (mobilityPath != null)
            {

                //build mobility bounds
                mobilityBounds = new RectF();
                mobilityPath.computeBounds(mobilityBounds, true);

                RectF mobilityFillBounds = new RectF();
                if (mobilityPathFill != null)
                {
                    mobilityPathFill.computeBounds(mobilityFillBounds, true);
                    mobilityBounds.union(mobilityFillBounds);
                }

                //grow by one because we use a line thickness of 2.
                RectUtilities.grow(mobilityBounds,Math.round(strokeWidthBasedOnDPI/2));
                //mobilityBounds.set(mobilityBounds.left - 1, mobilityBounds.top - 1, mobilityBounds.right + 1, mobilityBounds.bottom + 1);
                imageBounds.union(RectUtilities.makeRectFromRectF(mobilityBounds));
            }
        }
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Leadership Indicator Modifier">
        RectF liBounds = null;
        Path liPath = null;
        PointF liTop = null;
        PointF liLeft = null;
        PointF liRight = null;
        if(ad == SymbolID.Leadership_Individual && ss == SymbolID.SymbolSet_DismountedIndividuals &&
                (SymbolID.getFrameShape(symbolID)==SymbolID.FrameShape_DismountedIndividuals ||
                        SymbolID.getFrameShape(symbolID)==SymbolID.FrameShape_Unknown))
        {
            liPath = new Path();

            int si = SymbolID.getStandardIdentity(symbolID);
            int af = SymbolID.getAffiliation(symbolID);
            int c = SymbolID.getContext(symbolID);
            //int fs = SymbolID.getFrameShape(symbolID);
            double centerOffset = 0;
            double sideOffset = 0;
            double left = symbolBounds.left;
            double right = symbolBounds.left + symbolBounds.width();

            if(af == SymbolID.StandardIdentity_Affiliation_Unknown || af == SymbolID.StandardIdentity_Affiliation_Pending)
            {
                centerOffset = (symbolBounds.height()*0.1012528735632184);
                sideOffset = (right - left)*0.3583513488109785;
                //left = symbolBounds.getCenterX() - ((symbolBounds.getWidth() / 2) * 0.66420458);
                //right = symbolBounds.getCenterX() + ((symbolBounds.getWidth() / 2) * 0.66420458);
            }
            if(af == SymbolID.StandardIdentity_Affiliation_Neutral)
            {
                centerOffset = (symbolBounds.height()*0.25378787878787878);
                sideOffset = (right - left)*0.2051402812352822;
            }
            if(SymbolUtilities.isReality(symbolID) || SymbolUtilities.isSimulation(symbolID))
            {
                if(af==SymbolID.StandardIdentity_Affiliation_Friend || af==SymbolID.StandardIdentity_Affiliation_AssumedFriend)
                {//hexagon friend/assumed friend
                    centerOffset = (symbolBounds.height()*0.08);
                    sideOffset = (right - left)*0.282714524168219;//(symbolBounds.getHeight()*0.29);
                }
                else if(af==SymbolID.StandardIdentity_Affiliation_Hostile_Faker || af==SymbolID.StandardIdentity_Affiliation_Suspect_Joker)
                {//diamond hostile/suspect

                    left = symbolBounds.centerX() - ((symbolBounds.width() / 2) * 1.0653694149);//1.07);//1.0653694149);
                    right = symbolBounds.centerX() + ((symbolBounds.width() / 2) * 1.0653694149);//1.07);//1.0653694149);

                    centerOffset = (symbolBounds.height()*0.08);//0.0751139601139601
                    sideOffset = (right - left)*0.4923255424955992;
                }
            }
            else//Exercise
            {
                //hexagon
                if(af!=SymbolID.StandardIdentity_Affiliation_Unknown ||
                        af==SymbolID.StandardIdentity_Affiliation_Neutral)
                {
                    centerOffset = (symbolBounds.height()*0.08);
                    sideOffset = (right - left)*0.282714524168219;
                }
            }

            //create leadership indicator /\
            liTop = new PointF(symbolBounds.centerX(), (float)(symbolBounds.top - centerOffset));
            liLeft = new PointF((float)left, (float)(liTop.y + sideOffset));
            liRight = new PointF((float)right, (float)(liTop.y + sideOffset));


            liPath.moveTo(liTop.x, liTop.y);
            liPath.lineTo(liLeft.x, liLeft.y);
            liPath.moveTo(liTop.x, liTop.y);
            liPath.lineTo(liRight.x, liRight.y);//*/


            liBounds = new RectF(liLeft.x, liTop.y, liRight.x - liLeft.x, liLeft.y - liTop.y);

            RectUtilities.grow(liBounds,2);

            imageBounds.union(RectUtilities.makeRectFromRectF(liBounds));
        }

        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Build Echelon">
        //Draw Echelon
        int intEchelon = SymbolID.getAmplifierDescriptor(symbolID);
        String strEchelon = SymbolUtilities.getEchelonText(intEchelon);

        if (strEchelon != null
                && SymbolUtilities.hasModifier(symbolID, Modifiers.B_ECHELON))
        {

            int echelonOffset = 2,
                    outlineOffset = RS.getTextOutlineWidth();

            tiEchelon = new TextInfo(strEchelon, 0, 0, modifierFont, modifierFontName);
            echelonBounds = tiEchelon.getTextBounds();

            int y = Math.round(symbolBounds.left - echelonOffset);
            int x = Math.round(symbolBounds.left + (symbolBounds.width() / 2)
                    - (echelonBounds.width() / 2));
            tiEchelon.setLocation(x, y);

            //There will never be lowercase characters in an echelon so trim that fat.
            //Remove the descent from the bounding box.
            tiEchelon.getTextOutlineBounds();//.shiftBR(0,Math.round(-(echelonBounds.height()*0.3)));

            //make echelon bounds a little more spacious for things like nearby labels and Task Force.
            RectUtilities.grow(echelonBounds, outlineOffset);
            //tiEchelon.getTextOutlineBounds();
//                RectUtilities.shift(echelonBounds, x, -outlineOffset);
            //echelonBounds.shift(0,-outlineOffset);// - Math.round(echelonOffset/2));
            tiEchelon.setLocation(x, y - outlineOffset);

            imageBounds.union(echelonBounds);

        }
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Build Affiliation Modifier">
        //Draw Echelon
        String affiliationModifier = null;
        if (RS.getDrawAffiliationModifierAsLabel() == false)
        {
            affiliationModifier = SymbolUtilities.getStandardIdentityModifier(symbolID);
        }
        if (affiliationModifier != null)
        {

            int amOffset = 2;
            int outlineOffset = RS.getTextOutlineWidth();

            tiAM = new TextInfo(affiliationModifier, 0, 0, modifierFont, modifierFontName);
            amBounds = tiAM.getTextBounds();

            int x, y;

            if (echelonBounds != null
                    && ((echelonBounds.left + echelonBounds.width() > symbolBounds.left + symbolBounds.width())))
            {
                y = Math.round(symbolBounds.top - amOffset);
                x = echelonBounds.left + echelonBounds.width();
            }
            else
            {
                y = Math.round(symbolBounds.top - amOffset);
                x = Math.round(symbolBounds.left + symbolBounds.width());
            }
            tiAM.setLocation(x, y);

            //adjust for outline.
            RectUtilities.grow(amBounds, outlineOffset);
            RectUtilities.shift(amBounds, 0, -outlineOffset);
            tiAM.setLocation(x, y - outlineOffset);

            imageBounds.union(amBounds);
        }
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Build Task Force">
        Rect tfBounds = null;
        Rect tfRectangle = null;
        if (SymbolUtilities.isTaskForce(symbolID) && SymbolUtilities.hasModifier(symbolID, Modifiers.D_TASK_FORCE_INDICATOR))
        {

            int height = Math.round(symbolBounds.height() / 4.0f);
            int width = Math.round(symbolBounds.width() / 3.0f);

            if(!SymbolUtilities.hasRectangleFrame(symbolID))
            {
                height = (int)Math.round(symbolBounds.height() / 6.0f);
            }

            tfRectangle = RectUtilities.makeRect((int) symbolBounds.left + width,
                    (int) symbolBounds.top - height,
                    width,
                    height);

            tfBounds = RectUtilities.makeRect(tfRectangle.left + -1,
                    tfRectangle.top - 1,
                    tfRectangle.width() + 2,
                    tfRectangle.height() + 2);

            if (echelonBounds != null)
            {
                /*tfRectangle = new Rect(echelonBounds.left,
                        echelonBounds.top,// + outlineOffset,
                        echelonBounds.right,
                        symbolBounds.top-1);
                tfBounds = new Rect(tfRectangle);*/

                int tfx = tfRectangle.left;
                int tfw = tfRectangle.width();
                int tfy = tfRectangle.top;
                int tfh = tfRectangle.height();

                if(echelonBounds.width() > tfRectangle.width())
                {
                    tfx = symbolBounds.left + symbolBounds.width()/2 - (echelonBounds.width()/2) - 1;
                    tfw = echelonBounds.width()+2;
                }
                if(echelonBounds.height() > tfRectangle.height())
                {
                    tfy = echelonBounds.top-1;
                    tfh = echelonBounds.height()+2;

                }
                tfRectangle = RectUtilities.makeRect((int)tfx,
                        tfy,// + outlineOffset,
                        tfw,
                        tfh);


                tfBounds = RectUtilities.makeRect((tfRectangle.left - 1),
                        (tfRectangle.top - 1),
                        (tfRectangle.width() + 2),
                        (tfRectangle.height() + 2));
            }

            imageBounds.union(tfBounds);
        }
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Build Feint Dummy Indicator">
        Rect fdiBounds = null;
        Point fdiTop = null;
        Point fdiLeft = null;
        Point fdiRight = null;


        if (SymbolUtilities.hasFDI(symbolID)
                && SymbolUtilities.canSymbolHaveModifier(symbolID, Modifiers.AB_FEINT_DUMMY_INDICATOR))
        {
            //create feint indicator /\
            fdiLeft = new Point((int) symbolBounds.left, (int) symbolBounds.top);
            fdiRight = new Point((int) (symbolBounds.left + symbolBounds.width()), (int) symbolBounds.top);
            fdiTop = new Point(Math.round(RectUtilities.getCenterX(symbolBounds)), Math.round(symbolBounds.top - (symbolBounds.width() * .5f)));


            fdiBounds = RectUtilities.makeRect(fdiLeft.x, fdiLeft.y, 1, 1);
            fdiBounds.union(fdiTop.x, fdiTop.y);
            fdiBounds.union(fdiRight.x, fdiRight.y);

            float fdiStrokeWidth = Math.round(RendererSettings.getInstance().getDeviceDPI() / 96f);
            RectUtilities.grow(fdiBounds,Math.round(fdiStrokeWidth/2));

            if (echelonBounds != null)
            {
                int shiftY = Math.round(symbolBounds.top - echelonBounds.height() - 2);
                fdiLeft.offset(0, shiftY);
                fdiTop.offset(0, shiftY);
                fdiRight.offset(0, shiftY);
                fdiBounds.offset(0, shiftY);
            }

            imageBounds.union(fdiBounds);

        }
        // </editor-fold>

        //Using SVG files for installation indicator now
        // <editor-fold defaultstate="collapsed" desc="Build Installation">
        /*//Using SVG files for the installation indicator
        Rect instRectangle = null;
        Rect instBounds = null;
        if (SymbolUtilities.hasInstallationModifier(symbolID)
                && SymbolUtilitiesD.canSymbolHaveModifier(symbolID, Modifiers.AC_INSTALLATION))
        {//the actual installation symbols have the modifier
            //built in.  everything else, we have to draw it.
            //
            ////get indicator dimensions////////////////////////////////
            int width;
            int height;
            char affiliation = SymbolUtilities.getAffiliation(symbolID);

            if (affiliation == 'F'
                    || affiliation == 'A'
                    || affiliation == 'D'
                    || affiliation == 'M'
                    || affiliation == 'J'
                    || affiliation == 'K')
            {
                //4th height, 3rd width
                height = Math.round(symbolBounds.height() / 4);
                width = Math.round(symbolBounds.width() / 3);
            }
            else if (affiliation == 'H' || affiliation == 'S')//hostile,suspect
            {
                //6th height, 3rd width
                height = Math.round(symbolBounds.height() / 6);
                width = Math.round(symbolBounds.width() / 3);
            }
            else if (affiliation == 'N' || affiliation == 'L')//neutral,exercise neutral
            {
                //6th height, 3rd width
                height = Math.round(symbolBounds.height() / 6);
                width = Math.round(symbolBounds.width() / 3);
            }
            else if (affiliation == 'P'
                    || affiliation == 'U'
                    || affiliation == 'G'
                    || affiliation == 'W')
            {
                //6th height, 3rd width
                height = Math.round(symbolBounds.height() / 6);
                width = Math.round(symbolBounds.width() / 3);
            }
            else
            {
                //6th height, 3rd width
                height = Math.round(symbolBounds.height() / 6);
                width = Math.round(symbolBounds.width() / 3);
            }

//                    if(width * 3 < symbolBounds.width())
//                        width++;
            //set installation position/////////////////////////////////
            //set position of indicator
            if (affiliation == 'F'
                    || affiliation == 'A'
                    || affiliation == 'D'
                    || affiliation == 'M'
                    || affiliation == 'J'
                    || affiliation == 'K'
                    || affiliation == 'N'
                    || affiliation == 'L')
            {
                instRectangle = RectUtilities.makeRect((int) (symbolBounds.left + width),
                        (int) (symbolBounds.top - height),
                        width,
                        height);
            }
            else if (affiliation == 'H' || affiliation == 'S')//hostile,suspect
            {
                instRectangle = RectUtilities.makeRect((int) symbolBounds.left + width,
                        Math.round((int) symbolBounds.top - (height * 0.15f)),
                        width,
                        height);
            }
            else if (affiliation == 'P'
                    || affiliation == 'U'
                    || affiliation == 'G'
                    || affiliation == 'W')
            {
                instRectangle = RectUtilities.makeRect((int) symbolBounds.left + width,
                        Math.round(symbolBounds.top - (height * 0.3f)),
                        width,
                        height);
            }
            else
            {
                instRectangle = RectUtilities.makeRect((int) symbolBounds.left + width,
                        Math.round(symbolBounds.top - (height * 0.3f)),
                        width,
                        height);
            }

            //generate installation bounds//////////////////////////////
            instBounds = new Rect(instRectangle.left + -1,
                    instRectangle.top - 1,
                    instRectangle.width() + 2,
                    instRectangle.height() + 2);

            imageBounds.union(instBounds);

        }//*/
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Build Engagement Bar (AO)">
        //A:BBB-CC
        String strAO = null;
        Rect ebRectangle = null;
        Rect ebBounds = null;
        Rect ebTextBounds = null;
        TextInfo ebText = null;

        int ebTop = 0;
        int ebLeft = 0;
        int ebWidth = 0;
        int ebHeight = 0;
        Color ebColor = null;//SymbolUtilities.getFillColorOfAffiliation(symbolID);

        if(attributes.containsKey(MilStdAttributes.EngagementBarColor))
            ebColor = RendererUtilities.getColorFromHexString(attributes.get(MilStdAttributes.EngagementBarColor));
        else
            ebColor = fillColor;

        if(SymbolUtilities.hasModifier(symbolID, Modifiers.AO_ENGAGEMENT_BAR) &&
                modifiers.containsKey(Modifiers.AO_ENGAGEMENT_BAR))
            strAO = modifiers.get(Modifiers.AO_ENGAGEMENT_BAR);
        if(strAO != null)
        {
            ebText = new TextInfo(strAO, 0, 0, modifierFont, modifierFontName);
            ebTextBounds = ebText.getTextBounds();
            ebHeight = ebTextBounds.height() + 4;

            int barOffset = Math.max(RendererSettings.getInstance().getDeviceDPI()/32, 4);

            if(fdiBounds != null)//set bar above FDI if present
            {
                ebTop = fdiBounds.top - ebHeight - barOffset;
            }
            else if(tfBounds != null)//set bar above TF if present
            {
                ebTop = tfBounds.top - ebHeight - barOffset;
            }
            else if(echelonBounds != null)//set bar above echelon if present
            {
                ebTop = echelonBounds.top - ebHeight - barOffset;
            }
            else if((isCOnTop(symbolID) && modifiers.containsKey(Modifiers.C_QUANTITY)) ||
                        SymbolID.getContext(symbolID) == SymbolID.StandardIdentity_Context_Exercise ||
                        SymbolID.getContext(symbolID) == SymbolID.StandardIdentity_Context_Simulation)
            {
                ebTop = symbolBounds.top - (int)(ebHeight*2.5f);
            }
            else if(ss == SymbolID.SymbolSet_LandInstallation)
            {
                ebTop = symbolBounds.top - ebHeight - (barOffset * 2);
            }
            else//position above symbol
            {
                ebTop = symbolBounds.top - ebHeight - barOffset;
            }

            //if text wider than symbol, extend the bar.
            if(ebTextBounds.width() > symbolBounds.width())
            {
                ebWidth = ebTextBounds.width() + 4;
                ebLeft = symbolCenter.x - (ebWidth/2);
            }
            else
            {
                ebLeft = symbolBounds.left + (int)Math.ceil(strokeWidthBasedOnDPI/2f);//1;//leave room for outline
                ebWidth = symbolBounds.width() - (int)Math.ceil(strokeWidthBasedOnDPI);//2;//leave room for outline
            }

            //set text location within the bar
            ebText.setLocation(symbolCenter.x - (ebTextBounds.width()/2), ebTop + ebHeight - ((ebHeight - ebTextBounds.height()) / 2));

            ebRectangle = RectUtilities.makeRect(ebLeft,ebTop,ebWidth,ebHeight);
            ebBounds = new Rect(ebRectangle);
            RectUtilities.grow(ebBounds,(int)Math.ceil(strokeWidthBasedOnDPI/2f));

            imageBounds.union(ebBounds);
        }


        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Build HQ Staff">
        Point pt1HQ = null;
        Point pt2HQ = null;
        Rect hqBounds = null;
        //Draw HQ Staff
        if (SymbolUtilities.isHQ(symbolID))
        {
            int affiliation = SymbolID.getAffiliation(symbolID);
            int context = SymbolID.getContext(symbolID);
            //get points for the HQ staff
            if (SymbolUtilities.hasRectangleFrame(symbolID))
            {
                pt1HQ = new Point((int) symbolBounds.left + 1,
                        (int) (symbolBounds.top + symbolBounds.height() - 1));
            }
            else
            {
                pt1HQ = new Point((int) symbolBounds.left + 1,
                        (int) (symbolBounds.top + (symbolBounds.height() / 2)));
            }
            pt2HQ = new Point((int) pt1HQ.x, (int) (pt1HQ.y + symbolBounds.height()));

            //create bounding rectangle for HQ staff.
            hqBounds = new Rect(pt1HQ.x, pt1HQ.y, pt1HQ.x + 2, pt2HQ.y);
            //adjust the image bounds accordingly.
            imageBounds.union(hqBounds);
            //imageBounds.shiftBR(0,pt2HQ.y-imageBounds.bottom);
            //adjust symbol center
            centerPoint.set(pt2HQ.x, pt2HQ.y);
        }

        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Build DOM Arrow">
        Point[] domPoints = null;
        Rect domBounds = null;
        if (modifiers.containsKey(Modifiers.Q_DIRECTION_OF_MOVEMENT)
                && SymbolUtilities.hasModifier(symbolID, Modifiers.Q_DIRECTION_OF_MOVEMENT))
        {
        	String strQ = modifiers.get(Modifiers.Q_DIRECTION_OF_MOVEMENT);
        	
        	if(strQ != null && SymbolUtilities.isNumber(strQ))
        	{
	            float q = Float.valueOf(strQ);
	
	            boolean isY = (modifiers.containsKey(Modifiers.Y_LOCATION));
	
	            domPoints = createDOMArrowPoints(symbolID, symbolBounds, symbolCenter, q, isY, modifierFontHeight);
	
	            domBounds = new Rect(domPoints[0].x, domPoints[0].y, 1, 1);
	
	            Point temp = null;
	            for (int i = 1; i < 6; i++)
	            {
	                temp = domPoints[i];
	                if (temp != null)
	                {
	                    domBounds.union(temp.x, temp.y);
	                }
	            }
	            imageBounds.union(domBounds);
        	}
        }

        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Build Operational Condition Indicator">
        Rect ociBounds = null;
        RectF ociBoundsF = null;
        Rect ociShape = null;
        Path ociSlashShape = null;
        int ociOffset = Math.max(RendererSettings.getInstance().getDeviceDPI()/32, 4);
        if (SymbolUtilities.canSymbolHaveModifier(symbolID, Modifiers.AL_OPERATIONAL_CONDITION)) {
            if (mobilityBounds != null)
            {
                ociOffset = Math.round(mobilityBounds.bottom - symbolBounds.bottom) + 4;
            }
            if(RendererSettings.getInstance().getOperationalConditionModifierType() == RendererSettings.OperationalConditionModifierType_BAR)
            {
                ociShape = processOperationalConditionIndicator(symbolID, symbolBounds, ociOffset);
                if (ociShape != null)
                {
                    Rect temp = new Rect(ociShape);
                    RectUtilities.grow(temp, 2);
                    ociBounds = temp;
                    imageBounds.union(ociBounds);
                }
            }
            else//slash
            {
                ociSlashShape = processOperationalConditionIndicatorSlash(symbolID, symbolBounds);
                if (ociSlashShape != null)
                {
                    //build mobility bounds
                    ociBoundsF = new RectF();
                    ociBounds = new Rect();
                    ociSlashShape.computeBounds(ociBoundsF, true);
                    ociBoundsF.roundOut(ociBounds);
                    imageBounds.union(ociBounds);
                    ociBounds = null;
                }
            }
        }

        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Build Restricted Indicator">
        RectF rBounds = null;
        Path rPath = null;
        Path rPath2 = null;
        SVGPath rsvgPath = null;
        SVGPath rsvgPath2 = null;
        PointF rcirclePt = null;
        PointF rdotPt = null;
        float rdotRadius = 0;
        float rStrokeWidth = 3;
        if(SymbolID.getContext(symbolID) == SymbolID.StandardIdentity_Context_Restricted_Target_Reality)
        {
            // <path id="primary" d="M380,320l38,-67l40,67h-78m38,-11v-1m0,-10l0,-20" fill="yellow" stroke="black" stroke-linecap="round" stroke-linejoin="round" stroke-width="7" />
            float nsTx = 0;
            float nsTy = 0;
            float ratio = 1;
            SVGInfo si = SVGLookup.getInstance().getSVGLInfo(SVGLookup.getFrameID(symbolID),SymbolID.getVersion(symbolID));
            if(symbolBounds.height() > symbolBounds.width())
            {
                double sHeight = si.getBbox().height();
                ratio = (float)(symbolBounds.height() / sHeight);
            }
            else
            {
                double sWidth = si.getBbox().width();
                ratio = (float)(symbolBounds.width() / sWidth);
            }

            nsTx = (float)(si.getBbox().left * ratio) * -1;
            nsTy = (float)(si.getBbox().top * ratio) * -1;

            //<path d="m373,313l53,-97l57,97l-110,0" fill="yellow" id="triangle" stroke="black" stroke-linecap="round" stroke-linejoin="round" stroke-width="6"/>
            //<path d="m373,313L426,216L483,313L373,313" fill="yellow" id="triangle" stroke="black" stroke-linecap="round" stroke-linejoin="round" stroke-width="6"/>
            rPath = new Path();//triangle
            rPath.moveTo(373f * ratio, 313f * ratio);
            rPath.lineTo(426f * ratio, 216f * ratio);
            rPath.lineTo(483f * ratio, 313f * ratio);
            rPath.lineTo(373f * ratio, 313f * ratio);
            rsvgPath = new SVGPath();//triangle
            rsvgPath.moveTo(373f * ratio, 313f * ratio);
            rsvgPath.lineTo(426f * ratio, 216f * ratio);
            rsvgPath.lineTo(483f * ratio, 313f * ratio);
            rsvgPath.lineTo(373f * ratio, 313f * ratio);

            //<path d="M426.5,276L426.5,244" fill="none" id="line" stroke="black" stroke-linecap="round" stroke-linejoin="round" stroke-width="10"/>
            rPath2 = new Path();//line
            rPath2.moveTo(426.5f * ratio, 276f * ratio);
            rPath2.lineTo(426.5f * ratio, 248f * ratio);
            rsvgPath2 = new SVGPath();//line
            rsvgPath2.moveTo(426.5f * ratio, 276f * ratio);
            rsvgPath2.lineTo(426.5f * ratio, 248f * ratio);

            //<circle cx="426.5" cy="293" r="6" id="dot"/>//DOT
            rdotPt = new PointF(426.5f * ratio, 293 * ratio);
            rdotRadius = 5f * ratio;

            //need to shift like we do the frame and main icons since it's based in that space
            rPath.offset(nsTx, nsTy);
            rPath2.offset(nsTx, nsTy);
            rsvgPath.shift(nsTx,nsTy);
            rsvgPath2.shift(nsTx,nsTy);
            rdotPt.offset(nsTx,nsTy);
            //rCircle = (Ellipse2D) txfm.createTransformedShape(rCircle);


            //RectF bounds = new RectF();
            //bounds = rPath.computeBounds(bounds);  //getBounds();//triangle bounds
            Rect bounds = rsvgPath.getBounds();
            rBounds = RectUtilities.makeRectF(bounds.left,bounds.top,bounds.width(), bounds.height());
            rStrokeWidth = (2/66.666667f) * ((float)symbolBounds.height() / SymbolUtilities.getUnitRatioHeight(symbolID));
            RectUtilities.grow(rBounds,(int)Math.ceil(rStrokeWidth/2));
            RectUtilities.grow(bounds,(int)Math.ceil(rStrokeWidth/2));
            imageBounds.union(bounds);
        }
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Build No Strike Indicator">
        RectF nsBounds = null;
        PointF nsCirclePt = null;
        float nsRadius = 0;
        Path nsLine = null;
        SVGPath nssvgLine = null;
        double nsStrokeWidth = 3;
        if(SymbolID.getContext(symbolID) == SymbolID.StandardIdentity_Context_No_Strike_Entity_Reality)
        {
            //octagon~182.08058166503906~272.0794677734375~245.8407440185547~244.85235595703125
            //restricted~375.44801678047673~248.63298320770264~85.1039714496415~79.36734275822477
            //no-strike~378.0~248.0~80.0~80.0
            //<circle cx="418" cy="288" fill="yellow" r="36" stroke="black" stroke-width="8"/>
            //<line fill="none" stroke="black" stroke-linecap="round" stroke-linejoin="round" stroke-width="8" x1="390" x2="446" y1="265" y2="310"/>
            //nsCircle = new Ellipse(x,y,radius * 2, radius * 2);
            //nsLine = new Line(390 * ratio, 265 * ratio, 446 * ratio, 310 * ratio);
            float nsTx = 0;
            float nsTy = 0;
            float ratio = 1;
            SVGInfo si = SVGLookup.getInstance().getSVGLInfo(SVGLookup.getFrameID(symbolID),SymbolID.getVersion(symbolID));
            if(symbolBounds.height() > symbolBounds.width())
            {
                float sHeight = si.getBbox().height();
                ratio = symbolBounds.height() / sHeight;
            }
            else
            {
                float sWidth = si.getBbox().width();
                ratio = symbolBounds.width() / sWidth;
            }

            nsTx = (si.getBbox().left * ratio) * -1;
            nsTy = (si.getBbox().top * ratio) * -1;

            float radius = 50f * ratio;
            float x = 426f * ratio;
            float y = 267f * ratio;
            nsCirclePt = new PointF(x,y);
            nsRadius = radius;
            nsLine = new Path();
            nsLine.moveTo(390 * ratio, 235 * ratio);
            nsLine.lineTo(463 * ratio, 298 * ratio);
            nssvgLine = new SVGPath();
            nssvgLine.moveTo(390 * ratio, 235 * ratio);
            nssvgLine.lineTo(463 * ratio, 298 * ratio);

            //need to shift like we do the frame and main icons since it's based in that space
            nsCirclePt.offset(nsTx,nsTy);
            nsLine.offset(nsTx,nsTy);
            nssvgLine.shift(nsTx,nsTy);

            nsBounds = RectUtilities.makeRectF(nsCirclePt.x - radius, nsCirclePt.y - radius, radius * 2, radius * 2);

            nsStrokeWidth = (2/66.666667) * (symbolBounds.height() / SymbolUtilities.getUnitRatioHeight(symbolID));
            RectUtilities.grow(nsBounds,(int)Math.ceil(nsStrokeWidth/2));
            imageBounds.union(RectUtilities.makeRectFromRectF(nsBounds));
        }
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Shift Modifiers">
        //adjust points if necessary
        if (sdi instanceof ImageInfo && (imageBounds.left < 0 || imageBounds.top < 0))
        {
            int shiftX = Math.abs(imageBounds.left);
            int shiftY = Math.abs(imageBounds.top);

            if (hqBounds != null)
            {
                pt1HQ.offset(shiftX, shiftY);
                pt2HQ.offset(shiftX, shiftY);
            }
            if (echelonBounds != null)
            {
                tiEchelon.setLocation(tiEchelon.getLocation().x + shiftX, tiEchelon.getLocation().y + shiftY);
            }
            if (amBounds != null)
            {
                tiAM.setLocation(tiAM.getLocation().x + shiftX, tiAM.getLocation().y + shiftY);
            }
            if (tfBounds != null)
            {
                tfRectangle.offset(shiftX, shiftY);
                tfBounds.offset(shiftX, shiftY);
            }
            if(ebBounds != null)
            {
                ebRectangle.offset(shiftX, shiftY);
                ebBounds.offset(shiftX, shiftY);
                ebText.shift(shiftX, shiftY);
                ebTextBounds.offset(shiftX, shiftY);
            }
            /*if (instBounds != null)//part of symbol bounds now
            {
                instRectangle.offset(shiftX, shiftY);
                instBounds.offset(shiftX, shiftY);
            }//*/
            if (fdiBounds != null)
            {
                fdiBounds.offset(shiftX, shiftY);
                fdiLeft.offset(shiftX, shiftY);
                fdiTop.offset(shiftX, shiftY);
                fdiRight.offset(shiftX, shiftY);
            }
            if (liBounds != null)
            {
                liBounds.offset(shiftX, shiftY);
                liLeft.offset(shiftX, shiftY);
                liTop.offset(shiftX, shiftY);
                liRight.offset(shiftX, shiftY);
                if(liPath != null)
                {
                    liPath.offset(shiftX,shiftY);
                }
            }
            if (ociBounds != null)
            {
                ociBounds.offset(shiftX, shiftY);
                ociShape.offset(shiftX, shiftY);
            }
            if(ociBoundsF != null)
            {
                ociBoundsF.offset(shiftX, shiftY);
                ociSlashShape.offset(shiftX, shiftY);
            }
            if(rBounds != null)
            {
                rBounds.offset(shiftX, shiftY);//bounds
                rPath.offset(shiftX, shiftY);//triangle
                rPath2.offset(shiftX, shiftY);//exclamation
                rdotPt.offset(shiftX, shiftY);//dot
            }
            if(nsBounds != null)
            {
                nsBounds.offset(shiftX, shiftY);//bounds
                nsCirclePt.offset(shiftX, shiftY);//circle
                nsLine.offset(shiftX, shiftY);//line
            }
            if (domBounds != null)
            {
                for (int i = 0; i < 6; i++)
                {
                    Point temp = domPoints[i];
                    if (temp != null)
                    {
                        temp.offset(shiftX, shiftY);
                    }
                }
                domBounds.offset(shiftX, shiftY);
            }
            if (mobilityBounds != null)
            {
                //shift mobility points
                mobilityPath.offset(shiftX, shiftY);
                if (mobilityPathFill != null)
                {
                    mobilityPathFill.offset(shiftX, shiftY);
                }

                mobilityBounds.offset(shiftX, shiftY);
            }

            centerPoint.offset(shiftX, shiftY);
            symbolBounds.offset(shiftX, shiftY);
            imageBounds.offset(shiftX, shiftY);
        }
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Convert to SVG (SVGSymbolInfo)">
        if(sdi instanceof SVGSymbolInfo)
        {
            StringBuilder sbSVG = new StringBuilder();
            Path temp = null;
            SVGPath svgtemp = null;
            String svgStroke = RendererUtilities.colorToHexString(lineColor,false);
            String svgFill = RendererUtilities.colorToHexString(fillColor,false);
            String svgTextColor = RendererUtilities.colorToHexString(textColor,false);
            String svgTextBGColor = RendererUtilities.colorToHexString(textBackgroundColor,false);
            String svgStrokeWidth = String.valueOf(strokeWidthBasedOnDPI);
            String svgTextOutlineWidth = String.valueOf(RendererSettings.getInstance().getTextOutlineWidth());
            String svgAlpha = "1";
            if(alpha >= 0 && alpha <= 1)
                svgAlpha = String.valueOf(alpha);
            String svgDashArray = null;

            if(hqBounds != null)
            {
                /*Line2D hqStaff = new Line2D.Double(pt1HQ,pt2HQ);
                temp = new Path2D.Double();
                temp.append(hqStaff,false);
                sbSVG.append(Shape2SVG.Convert(temp, svgStroke, null, svgStrokeWidth, svgAlpha, svgAlpha, null));//*/

                svgtemp = new SVGPath();
                svgtemp.moveTo(pt1HQ.x, pt1HQ.y);
                svgtemp.lineTo(pt2HQ.x, pt2HQ.y);
                svgtemp.toSVGElement(svgStroke,Float.parseFloat(svgStrokeWidth),null,Float.parseFloat(svgAlpha),Float.parseFloat(svgAlpha),null);
            }
            if (echelonBounds != null)
            {
                sbSVG.append(Shape2SVG.Convert(tiEchelon, svgTextBGColor, svgTextColor, svgTextOutlineWidth, svgAlpha, svgAlpha, null));
            }
            if (amBounds != null)
            {
                sbSVG.append(Shape2SVG.Convert(tiAM, svgTextBGColor, svgTextColor, svgTextOutlineWidth, svgAlpha, svgAlpha, null));
            }
            if (tfBounds != null)
            {
                sbSVG.append(Shape2SVG.Convert(tfRectangle, svgStroke, null, svgStrokeWidth, svgAlpha, svgAlpha, null,null));
            }
            if(ebBounds != null)
            {
                String svgEBFill = RendererUtilities.colorToHexString(ebColor,false);
                //create fill and outline
                sbSVG.append(Shape2SVG.Convert(ebRectangle, svgStroke, svgEBFill, svgStrokeWidth, svgAlpha, svgAlpha, null,null));
                //create internal text
                sbSVG.append(Shape2SVG.Convert(ebText, null, "#000000", null, svgAlpha, svgAlpha, null));
            }
            if (fdiBounds != null)
            {
                /*int dpi = RendererSettings.getInstance().getDeviceDPI();
                int lineLength = dpi / 96 * 6;
                int lineGap = dpi / 96 * 4;
                String svgFDIDashArray = "" + lineLength + " " + lineGap;//*/

                /// ///////////////////////////////////
                //Divide line in 14 parts. line is 3 parts to 2 parts gap
                float distance = RendererUtilities.getDistanceBetweenPoints(fdiTop,fdiLeft);
                //distance = distance / 14f;
                int lineGap = (int)((distance / 14f) * 2);
                int lineLength = (int)((distance / 14f) * 3);
                String svgFDIDashArray = "" + lineLength + " " + lineGap;
                /// //////////////////////////////////

                SVGPath fdiPath = new SVGPath();

                fdiPath.moveTo(fdiTop.x, fdiTop.y);
                fdiPath.lineTo(fdiLeft.x, fdiLeft.y);
                fdiPath.moveTo(fdiTop.x, fdiTop.y);
                fdiPath.lineTo(fdiRight.x, fdiRight.y);//*/

                sbSVG.append(Shape2SVG.Convert(fdiPath, svgStroke, null, svgStrokeWidth, svgAlpha, svgAlpha, svgFDIDashArray,"round"));

            }
            if (liBounds != null)
            {
                SVGPath svgliPath = new SVGPath();
                svgliPath.moveTo(liTop.x, liTop.y);
                svgliPath.lineTo(liLeft.x, liLeft.y);
                svgliPath.moveTo(liTop.x, liTop.y);
                svgliPath.lineTo(liRight.x, liRight.y);

                sbSVG.append(svgliPath.toSVGElement(svgStroke,strokeWidthBasedOnDPI,null,Float.parseFloat(svgAlpha),Float.parseFloat(svgAlpha),"round"));
                //sbSVG.append(Shape2SVG.Convert(liPath, svgStroke, null, String.valueOf(liStrokeWidth), svgAlpha, svgAlpha, null));
            }
            if (ociBounds != null && ociShape != null)
            {

                int status = SymbolID.getStatus(symbolID);
                Color statusColor = null;

                switch (status) {
                    //Fully Capable
                    case SymbolID.Status_Present_FullyCapable:
                        statusColor = Color.green;
                        break;
                    //Damaged
                    case SymbolID.Status_Present_Damaged:
                        statusColor = Color.yellow;
                        break;
                    //Destroyed
                    case SymbolID.Status_Present_Destroyed:
                        statusColor = Color.red;
                        break;
                    //full to capacity(hospital)
                    case SymbolID.Status_Present_FullToCapacity:
                        statusColor = Color.blue;
                        break;
                    default:
                        break;
                }

                String svgOCIStatusColor = RendererUtilities.colorToHexString(statusColor,false);
                sbSVG.append(Shape2SVG.Convert(ociBounds, null, svgStroke, svgStrokeWidth, svgAlpha, svgAlpha, null,null));
                sbSVG.append(Shape2SVG.Convert(ociShape, null, svgOCIStatusColor, svgStrokeWidth, svgAlpha, svgAlpha, null,null));

                ociBounds = null;
                ociShape = null;

            }

            if (mobilityBounds != null)
            {
                if(svgMobilityGroup != null)
                    sbSVG.append(svgMobilityGroup);
                else if (svgMobilityPath != null)
                    sbSVG.append(svgMobilityPath.toSVGElement(svgStroke,strokeWidthBasedOnDPI,null,Float.parseFloat(svgAlpha),Float.parseFloat(svgAlpha),null));

                mobilityBounds = null;
            }

            //add symbol
            ssi = (SVGSymbolInfo)sdi;
            sbSVG.append(ssi.getSVG());

            if (ociBounds != null && ociSlashShape != null)
            {
                SVGPath svgociSlash = processOperationalConditionIndicatorSlashSVG(symbolID,symbolBounds);

                double size = symbolBounds.width();
                float ociStrokeWidth = 3f;

                ociStrokeWidth = (float) size / 20f;
                if (ociStrokeWidth < 1f)
                    ociStrokeWidth = 1f;

                sbSVG.append(svgociSlash.toSVGElement(svgStroke,ociStrokeWidth,null,Float.parseFloat(svgAlpha),Float.parseFloat(svgAlpha),null));
                //sbSVG.append(Shape2SVG.Convert(ociSlashShape, svgStroke, null, String.valueOf(ociStrokeWidth), svgAlpha, svgAlpha, null));
                ociBounds = null;
                ociSlashShape = null;
            }

            if(rBounds != null)
            {
                String restrictedGroup = "<g id=\"restricted\" stroke-linecap=\"round\" stroke-linejoin=\"round\">";
                //triangle
                restrictedGroup += Shape2SVG.Convert(rsvgPath, "#000000", "#FFFF00", String.valueOf(rStrokeWidth),svgAlpha,svgAlpha,null,null);
                //exclamation
                restrictedGroup += Shape2SVG.Convert(rsvgPath2, "#000000", null, String.valueOf(rStrokeWidth * 1.66667),svgAlpha,svgAlpha,null,null);
                //dot
                restrictedGroup += Shape2SVG.ConvertCircle(rdotPt, rdotRadius, "#000000", "#000000", String.valueOf(rStrokeWidth),svgAlpha,svgAlpha,null);
                restrictedGroup += "</g>";

                sbSVG.append(restrictedGroup);
            }

            if(nsBounds != null)
            {
                String noStrikeGroup = "<g id=\"nostrike\">";
                noStrikeGroup += Shape2SVG.ConvertCircle(nsCirclePt, nsRadius,"#000000", "#FFFF00", String.valueOf(nsStrokeWidth),svgAlpha,svgAlpha,null);
                noStrikeGroup += Shape2SVG.Convert(nssvgLine, "#000000", null, String.valueOf(nsStrokeWidth),svgAlpha,svgAlpha,null,null);
                noStrikeGroup += "</g>";
                sbSVG.append(noStrikeGroup);
            }

            if (domBounds != null)
            {
                //Path2D domPath = new Path2D.Double();
                SVGPath domPath = new SVGPath();

                domPath.moveTo(domPoints[0].x, domPoints[0].y);
                if (domPoints[1] != null)
                {
                    domPath.lineTo(domPoints[1].x, domPoints[1].y);
                }
                if (domPoints[2] != null)
                {
                    domPath.lineTo(domPoints[2].x, domPoints[2].y);
                }
                //sbSVG.append(Shape2SVG.Convert(domPath, svgStroke, null, svgStrokeWidth, svgAlpha, svgAlpha, null));
                sbSVG.append(domPath.toSVGElement(svgStroke, Float.parseFloat(svgStrokeWidth), null,Float.parseFloat(svgAlpha), Float.parseFloat(svgAlpha),null));


                //domPath.reset();
                domPath = new SVGPath();

                domPath.moveTo(domPoints[3].x, domPoints[3].y);
                domPath.lineTo(domPoints[4].x, domPoints[4].y);
                domPath.lineTo(domPoints[5].x, domPoints[5].y);
                sbSVG.append(Shape2SVG.Convert(domPath, "none", svgStroke, "0", svgAlpha, svgAlpha, null,null));
                sbSVG.append(domPath.toSVGElement("none", 0f, svgStroke,Float.parseFloat(svgAlpha), Float.parseFloat(svgAlpha),null));

                domBounds = null;
                domPoints = null;
            }

            newsdi = new SVGSymbolInfo(sbSVG.toString(),centerPoint,symbolBounds,imageBounds);
        }

        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Draw Modifiers (ImageInfo)">
        if(sdi instanceof ImageInfo)
        {
            ii = (ImageInfo) sdi;

            Bitmap bmp = Bitmap.createBitmap(imageBounds.width(), imageBounds.height(), Config.ARGB_8888);
            Canvas ctx = new Canvas(bmp);

            if (echelonBounds != null || amBounds != null) {
                //   ctx.font = RendererSettings.getModifierFont();
            }

            //render////////////////////////////////////////////////////////
            Paint paint = new Paint();
            paint.setStyle(Style.STROKE);
            //paint.setColor(Color.black.toInt());
            paint.setColor(lineColor.toInt());
            if (alpha > -1)
                paint.setAlpha(alpha);
            paint.setStrokeWidth(2.0f);

            paint.setStrokeWidth(strokeWidthBasedOnDPI);

            if (hqBounds != null) {
                ctx.drawLine(pt1HQ.x, pt1HQ.y, pt2HQ.x, pt2HQ.y, paint);
            }

            if (tfBounds != null) {
                ctx.drawRect(tfRectangle, paint);
            }

        /*if (instBounds != null) Part of frame SVG now
        {
            paint.setStyle(Style.FILL);
            ctx.drawRect(instRectangle, paint);
        }//*/

            if (ebBounds != null) {
                //draw bar fill
                paint.setStyle(Style.FILL);
                paint.setColor(ebColor.toInt());
                ctx.drawRect(ebRectangle, paint);

                //draw bar outline
                paint.setStyle(Style.STROKE);
                paint.setStrokeWidth(4.0f);
                paint.setColor(lineColor.toInt());
                ctx.drawRect(ebRectangle, paint);

                //draw bar text
                modifierFont.setColor(Color.BLACK.toInt());
                modifierFont.setStyle(Style.FILL);
                ctx.drawText(ebText.getText(), ebText.getLocation().x, ebText.getLocation().y, modifierFont);

                ebBounds = null;
                ebText = null;
                ebRectangle = null;
                paint.setStrokeWidth(strokeWidthBasedOnDPI);//2.0f
            }

            if (echelonBounds != null) {
                TextInfo[] aTiEchelon =
                        {
                                tiEchelon
                        };
                renderText(ctx, aTiEchelon, textColor, textBackgroundColor, modifierFont);

                echelonBounds = null;
                tiEchelon = null;
            }

            if (amBounds != null) {
                TextInfo[] aTiAM =
                        {
                                tiAM
                        };
                renderText(ctx, aTiAM, textColor, textBackgroundColor, modifierFont);
                amBounds = null;
                tiAM = null;
            }

            if (fdiBounds != null) {

                Paint fdiPaint = new Paint();
                fdiPaint.setAntiAlias(true);
                fdiPaint.setColor(lineColor.toInt());/// setARGB(255, 0, 0, 0);
                if (alpha > -1)
                    fdiPaint.setAlpha(alpha);
                fdiPaint.setStyle(Style.STROKE);

                /*int dpi = RendererSettings.getInstance().getDeviceDPI();

                int lineLength = dpi / 96 * 6;
                int lineGap = dpi / 96 * 4;//*/

                /// ///////////////////////////////////
                //Divide line in 14 parts. line is 3 parts to 2 parts gap
                float distance = RendererUtilities.getDistanceBetweenPoints(fdiTop,fdiLeft);
                //distance = distance / 14f;
                int lineGap = (int)((distance / 14f) * 2);
                int lineLength = (int)((distance / 14f) * 3);
                /// //////////////////////////////////

                fdiPaint.setPathEffect(new DashPathEffect(new float[]
                        {
                                lineLength, lineGap
                        }, 0));


                fdiPaint.setStrokeCap(Cap.ROUND);
                fdiPaint.setStrokeJoin(Join.MITER);
                fdiPaint.setStrokeWidth(strokeWidthBasedOnDPI);

                Path fdiPath = new Path();

                fdiPath.moveTo(fdiTop.x, fdiTop.y);
                fdiPath.lineTo(fdiLeft.x, fdiLeft.y);
                fdiPath.moveTo(fdiTop.x, fdiTop.y);
                fdiPath.lineTo(fdiRight.x, fdiRight.y);
                ctx.drawPath(fdiPath, fdiPaint);

                fdiBounds = null;

            }

            if (liBounds != null) {

                Paint liPaint = new Paint();
                liPaint.setAntiAlias(true);
                liPaint.setColor(lineColor.toInt());/// setARGB(255, 0, 0, 0);
                if (alpha > -1)
                    liPaint.setAlpha(alpha);
                liPaint.setStyle(Style.STROKE);

                int dpi = RendererSettings.getInstance().getDeviceDPI();

                liPaint.setStrokeCap(Cap.BUTT);
                liPaint.setStrokeJoin(Join.MITER);
                liPaint.setStrokeWidth(strokeWidthBasedOnDPI);

                ctx.drawPath(liPath, liPaint);

                liBounds = null;

            }

            if (mobilityBounds != null) {
                Paint mobilityPaint = new Paint();
                mobilityPaint.setStyle(Style.STROKE);
                //mobilityPaint.setColor(Color.black.toInt());
                mobilityPaint.setColor(lineColor.toInt());
                if (alpha > -1)
                    mobilityPaint.setAlpha(alpha);

                //ctx.lineCap = "butt";
                //ctx.lineJoin = "miter";
                if (ad >= SymbolID.Mobility_WheeledLimitedCrossCountry && ad < SymbolID.Mobility_ShortTowedArray)//mobility
                {
                    //mobilityPaint.setStrokeWidth(3f);
                    mobilityPaint.setStrokeWidth(strokeWidthBasedOnDPI);
                    mobilityPaint.setAntiAlias(true);
                } else //towed-array
                {
                    //mobilityPaint.setStrokeWidth(3f);
                    mobilityPaint.setStrokeWidth(strokeWidthBasedOnDPI);
                    //mobilityPaint.setAntiAlias(true);
                }

                ctx.drawPath(mobilityPath, mobilityPaint);

                if (mobilityPathFill != null) {
                    mobilityPaint.setStyle(Style.FILL);
                    ctx.drawPath(mobilityPathFill, mobilityPaint);
                }

                mobilityBounds = null;

            }

            if (ociBounds != null) {
                Paint ociPaint = new Paint();

                int statusColor = 0;
                int status = SymbolID.getStatus(symbolID);
                if (status == (SymbolID.Status_Present_FullyCapable))//Fully Capable
                {
                    statusColor = Color.green.toInt();
                } else if (status == (SymbolID.Status_Present_Damaged))//Damage
                {
                    statusColor = Color.yellow.toInt();
                } else if (status == (SymbolID.Status_Present_Destroyed)) {
                    statusColor = Color.red.toInt();
                } else if (status == (SymbolID.Status_Present_FullToCapacity))//full to capacity(hospital)
                {
                    statusColor = Color.blue.toInt();
                }

                ociPaint.setColor(lineColor.toInt());
                ociPaint.setStyle(Style.FILL);

                if (alpha > -1)
                    ociPaint.setAlpha(alpha);
                ctx.drawRect(ociBounds, ociPaint);
                ociPaint.setColor(statusColor);
                if (alpha > -1)
                    ociPaint.setAlpha(alpha);
                ctx.drawRect(ociShape, ociPaint);

                ociBounds = null;
                ociShape = null;
            }

            //draw original icon.
            //ctx.drawImage(ii.getImage(),symbolBounds.left, symbolBounds.top);
            ctx.drawBitmap(ii.getImage(), null, symbolBounds, null);

            if(rBounds != null)
            {
                Paint rPaint = new Paint();
                rPaint.setColor(Color.YELLOW.toInt());
                rPaint.setAlpha(alpha);
                rPaint.setStrokeCap(Cap.ROUND);
                rPaint.setStrokeJoin(Join.ROUND);
                rPaint.setStyle(Style.FILL);
                //triangle fill
                ctx.drawPath(rPath, rPaint);

                //triangle outline
                rPaint.setStrokeWidth(rStrokeWidth);
                rPaint.setStyle(Style.STROKE);
                rPaint.setColor(Color.BLACK.toInt());
                ctx.drawPath(rPath, rPaint);

                //exclamation line
                rPaint.setStrokeWidth(rStrokeWidth * 1.66667f);
                ctx.drawPath(rPath2, rPaint);

                //exclamation dot
                rPaint.setStrokeWidth(rStrokeWidth);
                rPaint.setStyle(Style.FILL_AND_STROKE);
                ctx.drawCircle(rdotPt.x,rdotPt.y,rdotRadius,rPaint);

            }
            if(nsBounds != null)
            {
                Paint nsPaint = new Paint();
                nsPaint.setColor(Color.YELLOW.toInt());
                nsPaint.setAlpha(alpha);
                nsPaint.setStrokeCap(Cap.ROUND);
                nsPaint.setStrokeJoin(Join.ROUND);
                nsPaint.setStyle(Style.FILL);

                //circle fill
                ctx.drawCircle(nsCirclePt.x, nsCirclePt.y,nsRadius,nsPaint);

                //circle outline
                nsPaint.setStyle(Style.STROKE);
                nsPaint.setColor(Color.BLACK.toInt());
                nsPaint.setStrokeWidth((float)nsStrokeWidth);
                ctx.drawCircle(nsCirclePt.x,nsCirclePt.y,nsRadius,nsPaint);

                //draw slash
                ctx.drawPath(nsLine,nsPaint);

            }

            if (domBounds != null) {
                drawDOMArrow(ctx, domPoints, alpha, lineColor);

                domBounds = null;
                domPoints = null;
            }

            if (ociBoundsF != null) {
                Paint ociPaint = new Paint();
                int size = symbolBounds.width();
                float ociStrokeWidth = 3f;

                ociStrokeWidth = size / 20f;
                if (ociStrokeWidth < 1f)
                    ociStrokeWidth = 1f;
            /*if(size > 50 && size < 100)
                ociStrokeWidth = 5f;
            else if(size >= 100 && size < 200)
                ociStrokeWidth = 7f;
            else if(size >= 200)
                ociStrokeWidth = 10f;*/
                //ociPaint.setColor(Color.black.toInt());
                ociPaint.setColor(lineColor.toInt());
                if (alpha > -1)
                    ociPaint.setAlpha(alpha);
                ociPaint.setStrokeWidth(ociStrokeWidth);
                ociPaint.setStrokeCap(Cap.BUTT);
                ociPaint.setStyle(Style.STROKE);
                ociPaint.setAntiAlias(true);
                ctx.drawPath(ociSlashShape, ociPaint);

                ociBoundsF = null;
                ociSlashShape = null;
            }


            if (bmp != null)
                newsdi = new ImageInfo(bmp, centerPoint, symbolBounds);
        }
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Cleanup">
        // </editor-fold>
        if (newsdi != null)
        {
            return newsdi;
        }
        else
        {
            return null;
        }

    }

    /**
     *
     * @param symbolID
     * @return
     * @deprecated no longer a thing in 2525D
     * TODO: remove
     */
    private static double getYPositionForSCC(String symbolID)
    {
        double yPosition = 0.32;
        /*int aff = SymbolID.getAffiliation(symbolID);
        int context = SymbolID.getContext(symbolID);
        char affiliation = symbolID.charAt(1);

        if(temp.equals("WMGC--"))//GROUND (BOTTOM) MILCO
        {
            if(affiliation == 'H' || 
                    affiliation == 'S')//suspect
                yPosition = 0.29;
            else if(affiliation == 'N' ||
                    affiliation == 'L')//exercise neutral
                yPosition = 0.32;
            else if(affiliation == 'F' ||
                    affiliation == 'A' ||//assumed friend
                    affiliation == 'D' ||//exercise friend
                    affiliation == 'M' ||//exercise assumed friend
                    affiliation == 'K' ||//faker
                    affiliation == 'J')//joker
                yPosition = 0.32;
            else
                yPosition = 0.34;
        }
        else if(temp.equals("WMMC--"))//MOORED MILCO
        {
            if(affiliation == 'H' || 
                    affiliation == 'S')//suspect
                yPosition = 0.25;
            else if(affiliation == 'N' ||
                    affiliation == 'L')//exercise neutral
                yPosition = 0.25;
            else if(affiliation == 'F' ||
                    affiliation == 'A' ||//assumed friend
                    affiliation == 'D' ||//exercise friend
                    affiliation == 'M' ||//exercise assumed friend
                    affiliation == 'K' ||//faker
                    affiliation == 'J')//joker
                yPosition = 0.25;
            else
                yPosition = 0.28;
        }
        else if(temp.equals("WMFC--"))//FLOATING MILCO
        {
            if(affiliation == 'H' || 
                    affiliation == 'S')//suspect
                yPosition = 0.29;
            else if(affiliation == 'N' ||
                    affiliation == 'L')//exercise neutral
                yPosition = 0.32;
            else if(affiliation == 'F' ||
                    affiliation == 'A' ||//assumed friend
                    affiliation == 'D' ||//exercise friend
                    affiliation == 'M' ||//exercise assumed friend
                    affiliation == 'K' ||//faker
                    affiliation == 'J')//joker
                yPosition = 0.32;
            else
                yPosition= 0.34;
        }
        else if(temp.equals("WMC---"))//GENERAL MILCO
        {
            if(affiliation == 'H' || 
                    affiliation == 'S')//suspect
                yPosition = 0.33;
            else if(affiliation == 'N' ||
                    affiliation == 'L')//exercise neutral
                yPosition = 0.36;
            else if(affiliation == 'F' ||
                    affiliation == 'A' ||//assumed friend
                    affiliation == 'D' ||//exercise friend
                    affiliation == 'M' ||//exercise assumed friend
                    affiliation == 'K' ||//faker
                    affiliation == 'J')//joker
                yPosition = 0.36;
            else
                yPosition = 0.36;
        }*/
        
        return yPosition;
    }

    /**
     *
     * @param {type} symbolID
     * @param {type} bounds symbolBounds SO.Rectangle
     * @param {type} center SO.Point Location where symbol is centered.
     * @param {type} angle in degrees
     * @param {Boolean} isY Boolean.
     * @returns {Array} of SO.Point. First 3 items are the line. Last three are
     * the arrowhead.
     */
    private static Point[] createDOMArrowPoints(String symbolID, Rect bounds, Point center, float angle, boolean isY, float modifierFontHeight)
    {
        Point[] arrowPoints = new Point[6];
        Point pt1 = null;
        Point pt2 = null;
        Point pt3 = null;

        int affiliation = SymbolID.getAffiliation(symbolID);
        int context = SymbolID.getContext(symbolID);
        int length = 40;
        if (SymbolUtilities.isCBRNEvent(symbolID))
        {
            length = Math.round(bounds.height() / 2);
        }
        else if((SymbolUtilities.isHQ(symbolID)))
        {
            if(SymbolUtilities.hasRectangleFrame(symbolID))
                length = bounds.height();
            else
                length = (int)Math.round(bounds.height() * 0.7);
        }
        else// if(bounds.height() >= 100)
        {
            length = (int)Math.round(bounds.height() * 0.7);
        }

        //get endpoint
        int dx2, dy2,
                x1, y1,
                x2, y2;

        x1 = Math.round(center.x);
        y1 = Math.round(center.y);

        pt1 = new Point(x1, y1);

        if (SymbolUtilities.hasModifier(symbolID, Modifiers.Q_DIRECTION_OF_MOVEMENT ) &&
            SymbolUtilities.isCBRNEvent(symbolID) ||
                SymbolUtilities.isLand(symbolID) ||
                SymbolID.getSymbolSet(symbolID)==SymbolID.SymbolSet_DismountedIndividuals)
        {
            //drawStaff = true;
            if(SymbolUtilities.isHQ(symbolID)==false)//has HQ staff to start from
            {
                y1 = bounds.top + bounds.height();
                pt1 = new Point(x1, y1);

                if (isY == true && SymbolUtilities.isCBRNEvent(symbolID))//make room for y modifier
                {
                    int yModifierOffset = (int) modifierFontHeight;

                    yModifierOffset += RS.getTextOutlineWidth();

                    pt1.offset(0, yModifierOffset);
                }

                y1 = y1 + length;
                pt2 = new Point(x1, y1);
            }
            else
            {
                x1 = bounds.left+1;
                pt2 = new Point(x1, y1);
                if(SymbolUtilities.hasRectangleFrame(symbolID))
                {
                    /*y1 = bounds.top + bounds.height();
                    pt1 = new Point(x1, y1);
                    y1 = y1 + length;
                    pt2 = new Point(x1, y1);//*/

                    pt1.x = x1;
                    y1 = bounds.top + (bounds.height());
                    pt1.y = y1;
                    x2 = x1;
                    pt2.x = x2;
                    y1 = pt1.y + bounds.height();
                    pt2.y = y1;//*/

                }
                else
                {
                    pt1.x = x1;
                    y1 = bounds.top + (bounds.height() / 2);
                    pt1.y = y1;
                    x2 = x1;
                    pt2.x = x2;
                    y1 = pt1.y + bounds.height();
                    pt2.y = y1;
                }
            }
        }

	    //get endpoint given start point and an angle
        //x2 = x1 + (length * Math.cos(radians)));
        //y2 = y1 + (length * Math.sin(radians)));
        angle = angle - 90;//in java, east is zero, we want north to be zero
        double radians = 0;
        radians = (angle * (Math.PI / 180));//convert degrees to radians

        dx2 = x1 + (int) (length * Math.cos(radians));
        dy2 = y1 + (int) (length * Math.sin(radians));
        x2 = Math.round(dx2);
        y2 = Math.round(dy2);


        //UPDATED ARROWHEAD CODE
        Point[] head = null;
        Point endPoint = new Point((int) x2, (int) y2);
        if(pt2 != null)
            head = createDOMArrowHead(pt2, endPoint);//pt3);
        else
            head = createDOMArrowHead(pt1, endPoint);//pt3);

        if(head != null)
        {
            arrowPoints[0] = pt1;
            arrowPoints[1] = pt2;
            arrowPoints[2] = pt3;
            arrowPoints[3] = head[0];
            arrowPoints[4] = head[1];
            arrowPoints[5] = head[2];

            //adjusted endpoint
            if(head.length >= 4 && head[3] != null)
            {
                arrowPoints[2] = head[3];
            }
        }

        return arrowPoints;

    }

    private static Point[] createDOMArrowHead(Point lpt1, Point lpt2)
    {
        Point[] arrowPoints = new Point[6];
        Point pt1 = null;
        Point pt2 = null;
        Point pt3 = null;

        int x1 = lpt1.x;
        int y1 = lpt1.y;
        int x2 = lpt2.x;
        int y2 = lpt2.y;

        // Compute direction vector
        double dx = x2 - x1;
        double dy = y2 - y1;
        double length = Math.sqrt(dx * dx + dy * dy);

        // Scale triangle size
        double scale = length * 0.1;  // Scaling factor for size
        double offset = scale * 1.5;  // Move triangle further down the line

        // Normalize direction vector
        double unitX = dx / length;
        double unitY = dy / length;

        // Compute perpendicular vector for triangle base
        double nx = -unitY;
        double ny = unitX;

        // Compute adjusted triangle vertices
        int tipX = x2;
        int tipY = y2;
        int baseX1 = (int) (x2 - offset * unitX + scale * nx);
        int baseY1 = (int) (y2 - offset * unitY + scale * ny);
        int baseX2 = (int) (x2 - offset * unitX - scale * nx);
        int baseY2 = (int) (y2 - offset * unitY - scale * ny);


        //arrowHead = new Polygon(xPoints, yPoints, 3);
        arrowPoints[0] = new Point(tipX,tipY);
        arrowPoints[1] = new Point(baseX1,baseY1);
        arrowPoints[2] = new Point(baseX2,baseY2);
        // Adjust line endpoint to be the middle of the base line of the arrowhead
        int adjustedX2 = (baseX1 + baseX2) / 2;
        int adjustedY2 = (baseY1 + baseY2) / 2;
        arrowPoints[3] = new Point(adjustedX2,adjustedY2);

        return arrowPoints;

    }

    private static void drawDOMArrow(Canvas ctx, Point[] domPoints, int alpha, Color lineColor)
    {
        float domStrokeWidth = Math.round(RendererSettings.getInstance().getDeviceDPI() / 96f);
        if(domStrokeWidth < 1)
            domStrokeWidth=1;


        Paint domPaint = new Paint();
        domPaint.setAntiAlias(true);
        domPaint.setStrokeCap(Cap.BUTT);
        domPaint.setStrokeJoin(Join.MITER);
        domPaint.setStrokeWidth(domStrokeWidth);//3
        domPaint.setColor(lineColor.toInt());
        domPaint.setStyle(Style.STROKE);
        if(alpha > -1)
            domPaint.setAlpha(alpha);

        Path domPath = new Path();
        domPath.moveTo(domPoints[0].x, domPoints[0].y);
        if (domPoints[1] != null)
        {
            domPath.lineTo(domPoints[1].x, domPoints[1].y);
        }
        if (domPoints[2] != null)
        {
            domPath.lineTo(domPoints[2].x, domPoints[2].y);
        }
        ctx.drawPath(domPath, domPaint);

        domPath.reset();
        domPaint.setStyle(Style.FILL);
        domPath.moveTo(domPoints[3].x, domPoints[3].y);
        domPath.lineTo(domPoints[4].x, domPoints[4].y);
        domPath.lineTo(domPoints[5].x, domPoints[5].y);
        ctx.drawPath(domPath, domPaint);
    }

    private static Rect processOperationalConditionIndicator(String symbolID, Rect symbolBounds, int offsetY)
    {
        //create Operational Condition Indicator
        //set color
        Rect bar = null;
        int status;
        Color statusColor;
        int barSize = 0;
        int pixelSize = symbolBounds.height();

        status = SymbolID.getStatus(symbolID);
        if (status == SymbolID.Status_Present_FullyCapable ||
                status == SymbolID.Status_Present_Damaged ||
                status == SymbolID.Status_Present_Destroyed ||
                status == SymbolID.Status_Present_FullToCapacity)
        {
            if (pixelSize > 0)
            {
                barSize = Math.round(pixelSize / 5);
            }

            if (barSize < 2)
            {
                barSize = 2;
            }

            offsetY += Math.round(symbolBounds.top + symbolBounds.height());

            bar = RectUtilities.makeRect(symbolBounds.left + 2, offsetY, Math.round(symbolBounds.width()) - 4, barSize);
        }

        return bar;
    }

    private static Path processOperationalConditionIndicatorSlash(String symbolID, Rect symbolBounds)
    {
        //create Operational Condition Indicator
        Path path = null;
        int status;
        status = SymbolID.getStatus(symbolID);

        if (status == SymbolID.Status_Present_Damaged  || status == SymbolID.Status_Present_Destroyed)
        {
            float widthRatio = SymbolUtilities.getUnitRatioWidth(symbolID);
            float heightRatio = SymbolUtilities.getUnitRatioHeight(symbolID);

            float slashHeight = (symbolBounds.height() / heightRatio * 1.47f);
            float slashWidth = (symbolBounds.width() / widthRatio * 0.85f);
            float centerX = symbolBounds.exactCenterX();
            float centerY = symbolBounds.exactCenterY();
            path = new Path();
            if(status == SymbolID.Status_Present_Damaged)//Damaged /
            {
                path.moveTo(centerX - (slashWidth/2),centerY+(slashHeight/2));
                path.lineTo(centerX + (slashWidth/2),centerY-(slashHeight/2));
            }
            else if(status == SymbolID.Status_Present_Destroyed)//Destroyed X
            {
                path.moveTo(centerX - (slashWidth/2),centerY+(slashHeight/2));
                path.lineTo(centerX + (slashWidth/2),centerY-(slashHeight/2));
                path.moveTo(centerX - (slashWidth/2),centerY-(slashHeight/2));
                path.lineTo(centerX + (slashWidth/2),centerY+(slashHeight/2));
            }
            return path;

        }

        return path;
    }

    private static SVGPath processOperationalConditionIndicatorSlashSVG(String symbolID, Rect symbolBounds)
    {
        //create Operational Condition Indicator
        SVGPath path = null;
        int status;
        status = SymbolID.getStatus(symbolID);

        if (status == SymbolID.Status_Present_Damaged  || status == SymbolID.Status_Present_Destroyed)
        {
            float widthRatio = SymbolUtilities.getUnitRatioWidth(symbolID);
            float heightRatio = SymbolUtilities.getUnitRatioHeight(symbolID);

            float slashHeight = (symbolBounds.height() / heightRatio * 1.47f);
            float slashWidth = (symbolBounds.width() / widthRatio * 0.85f);
            float centerX = symbolBounds.exactCenterX();
            float centerY = symbolBounds.exactCenterY();
            path = new SVGPath();
            if(status == SymbolID.Status_Present_Damaged)//Damaged /
            {
                path.moveTo(centerX - (slashWidth/2),centerY+(slashHeight/2));
                path.lineTo(centerX + (slashWidth/2),centerY-(slashHeight/2));
            }
            else if(status == SymbolID.Status_Present_Destroyed)//Destroyed X
            {
                path.moveTo(centerX - (slashWidth/2),centerY+(slashHeight/2));
                path.lineTo(centerX + (slashWidth/2),centerY-(slashHeight/2));
                path.moveTo(centerX - (slashWidth/2),centerY-(slashHeight/2));
                path.lineTo(centerX + (slashWidth/2),centerY+(slashHeight/2));
            }
            return path;

        }

        return path;
    }


    public static SymbolDimensionInfo processSpeedLeader(SymbolDimensionInfo sdi, String symbolID, Map<String,String> modifiers, Map<String,String> attributes)
    {
        SymbolDimensionInfo rsdi = sdi;

        Rect imageBounds = sdi.getImageBounds();
        Rect symbolBounds = sdi.getSymbolBounds();
        Point symbolCenter = sdi.getCenterPoint();
        int ss = SymbolID.getSymbolSet(symbolID);
        int pixelSize = RendererSettings.getInstance().getDefaultPixelSize();
        int dpi = RendererSettings.getInstance().getDeviceDPI();
        if(attributes.containsKey(MilStdAttributes.PixelSize))
            pixelSize = Integer.parseInt(attributes.get(MilStdAttributes.PixelSize));
        float strokeWidth = 3f;
        strokeWidth = (float) dpi / 48f;
        if (strokeWidth < 1f)
            strokeWidth = 1f;

        Path slPath = null;
        SVGPath slsvgPath = null;
        Rect slBounds = null;
        try {
            if (SymbolUtilities.hasModifier(symbolID, Modifiers.AJ_SPEED_LEADER) &&
                    (modifiers.containsKey(Modifiers.AJ_SPEED_LEADER))) {
                String aj = modifiers.get(Modifiers.AJ_SPEED_LEADER);
                String[] values = aj.split(" ");
                if (values.length >= 3) {
                    int speed = Integer.parseInt(values[0]);
                    String speedUnit = values[1];
                    int angle = 0;
                    if (values[2].length() == 3)
                        angle = Integer.parseInt(values[2]) - 90;
                    else
                        angle = (int) (Integer.parseInt(values[2]) * 0.05625) - 90;

                    slPath = new Path();
                    slPath.moveTo(symbolCenter.x, symbolCenter.y);
                    slsvgPath = new SVGPath();
                    slsvgPath.moveTo(symbolCenter.x, symbolCenter.y);

                    //convert to Knots
                    switch (speedUnit)//KPH, KPS, MPH, NMH, KTS//https://www.aviationhunt.com/speed-converter/
                    {
                        case "KPH":
                            speed = (int) (speed * 0.539957);
                            break;
                        case "KPS"://https://www.metric-conversions.org/speed/kilometers-per-second-to-knots.htm
                            speed = (int) (speed * 1943.84);
                            break;
                        case "MPH":
                            speed = (int) (speed * 0.86897);
                            break;
                    }

                    int distance = 0;
                    char frame = SymbolID.getFrameShape(symbolID);
                    boolean fast = false;
                    if (frame == '0' && ss == SymbolID.SymbolSet_Air ||
                            ss == SymbolID.SymbolSet_AirMissile ||
                            ss == SymbolID.SymbolSet_SignalsIntelligence_Air ||
                            ss == SymbolID.SymbolSet_SpaceMissile ||
                            ss == SymbolID.SymbolSet_Space ||
                            (SymbolID.getVersion(symbolID) <= SymbolID.Version_2525Dch1 && ss == SymbolID.SymbolSet_SignalsIntelligence_Space))
                    {
                        fast = true;
                    }
                    else if(frame == SymbolID.FrameShape_Air || frame == SymbolID.FrameShape_Space)
                    {
                        fast = true;
                    }

                    float distanceScaler = dpi;//spec does scale by inch, but if the symbol is too big, scale by pixel size
                    if(dpi < pixelSize)
                        distanceScaler = pixelSize;

                    if(fast)
                    {//aircraft might be 1/4 inch if its speed is less than 300 knots, 1/2 inch if its speed is between 300 and 600 knots and 3/4 inch if its speed is more than 600 knots.
                        if (speed < 300)
                            distance = (int) (distanceScaler * 0.25)/300 * speed;
                        else if (speed < 600)
                            distance = (int) (distanceScaler * 0.5)/600 * speed;
                        else
                            distance = (int) (distanceScaler * 0.75);
                    }
                    else//submarine might be 1/4 inch if its speed is less than 15 knots, 1/2 inch if its speed is between 15 and 30 knots and 3/4 inch if its speed is more than 30 knots
                    {
                        if (speed < 15)
                            distance = (int) (distanceScaler * 0.25)/15 * speed;
                        else if (speed < 30)
                            distance = (int) (distanceScaler * 0.5)/30 * speed;
                        else
                            distance = (int) (distanceScaler * 0.75);
                    }
                    double radians = (angle * (Math.PI / 180));//convert degrees to radians
                    int x2 = (int) (symbolCenter.x + distance * Math.cos(radians));
                    int y2 = (int) (symbolCenter.y + distance * Math.sin(radians));

                    slPath.lineTo(x2, y2);
                    slsvgPath.lineTo(x2, y2);
                    slBounds = slsvgPath.getBounds();
                    imageBounds.union(slBounds);
                }

                if (sdi instanceof ImageInfo) {
                    Bitmap bmp = Bitmap.createBitmap((int) Math.ceil(imageBounds.width()), (int) Math.ceil(imageBounds.height()), Config.ARGB_8888);
                    Canvas ctx = new Canvas(bmp);

                    int alpha = 1;
                    if (attributes.containsKey(MilStdAttributes.Alpha))
                        alpha = Integer.parseInt(attributes.get(MilStdAttributes.Alpha));


                    Paint slPaint = new Paint();
                    slPaint.setColor(Color.BLACK.toInt());
                    slPaint.setAlpha(alpha * 255);
                    slPaint.setStyle(Style.STROKE);
                    slPaint.setStrokeWidth(strokeWidth);


                    float offsetX = 0;
                    float offsetY = 0;
                    if (imageBounds.left < 0)
                        offsetX = imageBounds.left * -1;
                    if (imageBounds.top < 0)
                        offsetY = imageBounds.top * -1;

                    slPath.offset(offsetX,offsetY);

                    ctx.drawBitmap(((ImageInfo) sdi).getImage(), offsetX, offsetY, null);
                    ctx.drawPath(slPath,slPaint);

                    imageBounds.offset((int)offsetX,(int)offsetY);
                    symbolBounds.offset((int)offsetX,(int)offsetY);
                    symbolCenter.offset((int)offsetX,(int)offsetY);

                    rsdi = new ImageInfo(bmp, symbolCenter, symbolBounds);
                }
                else if (sdi instanceof SVGSymbolInfo)
                {//public static String Convert(Shape shape,String stroke, String fill, String strokeWidth, String strokeOpacity, String fillOpacity, String dashArray, String lineCap)
                    String svg = ((SVGSymbolInfo) sdi).getSVG();
                    svg += slsvgPath.toSVGElement("#000000",strokeWidth,"none",1,1,null);
                    //svg += (Shape2SVG.Convert(slPath, "#000000", "none", String.valueOf(strokeWidth),null,null,null, null));
                    rsdi = new SVGSymbolInfo(svg,symbolCenter,symbolBounds,imageBounds);
                }
            }
        }
        catch(Exception exc)
        {
            ErrorLogger.LogException("ModifierRenderer","processSpeedLineIndicator",exc);
        }

        return rsdi;
    }

    /**
     * uses 2525C layout which shows most modifiers
     * @param sdi
     * @param symbolID
     * @param modifiers
     * @param attributes
     * @return
     */
    public static SymbolDimensionInfo processUnknownTextModifiers(SymbolDimensionInfo sdi, String symbolID, Map<String,String> modifiers, Map<String,String> attributes)
    {
        Paint modifierFont = getFont(attributes);
        float[] hd = getFontHeightandDescent(modifierFont);
        float modifierFontHeight = hd[0];
        float modifierFontDescent = hd[1];

        String modifierFontName = RendererSettings.getInstance().getModiferFontProps()[0];
        if(attributes != null && attributes.containsKey(MilStdAttributes.FontFamily))
        {
            String temp = attributes.get(MilStdAttributes.FontFamily);
            if(temp != null && !temp.isEmpty())
                modifierFontName = temp;
        }

        int bufferHorizontal = (int)modifierFontHeight/2;
        int bufferXL = bufferHorizontal;
        int bufferXR = bufferHorizontal;
        int bufferY = 2;
        int bufferText = 2;
        int x = 0;
        int y = 0;//best y

        SymbolDimensionInfo  newsdi = null;

        ArrayList<TextInfo> tiArray = new ArrayList<TextInfo>(modifiers.size());

        int descent = (int) (modifierFontDescent + 0.5);

        Rect labelBounds = null;
        int labelWidth, labelHeight;

        Rect bounds = new Rect(sdi.getSymbolBounds());
        Rect symbolBounds = new Rect(sdi.getSymbolBounds());
        Point centerPoint = new Point(sdi.getCenterPoint());
        Rect imageBounds = new Rect(sdi.getImageBounds());

        int echelon = SymbolID.getAmplifierDescriptor(symbolID);
        String echelonText = SymbolUtilities.getEchelonText(echelon);
        String amText = SymbolUtilities.getStandardIdentityModifier(symbolID);
        int mobility = SymbolID.getAmplifierDescriptor(symbolID);

        //adjust width of bounds for mobility/echelon/engagement bar which could be wider than the symbol
        bounds = RectUtilities.makeRect(imageBounds.left, bounds.top, imageBounds.width(), bounds.height());


        //check if text is too tall:
        boolean byLabelHeight = false;
        labelHeight = (int) (modifierFontHeight + 0.5);/* RendererUtilities.measureTextHeight(RendererSettings.getModifierFontName(),
         RendererSettings.getModifierFontSize(),
         RendererSettings.getModifierFontStyle()).fullHeight;*/

        int maxHeight = (bounds.height());
        if ((labelHeight * 3) > maxHeight)
        {
            byLabelHeight = true;
        }

        //Affiliation Modifier being drawn as a display modifier
        String affiliationModifier = null;
        if (RS.getDrawAffiliationModifierAsLabel() == true)
        {
            affiliationModifier = SymbolUtilities.getStandardIdentityModifier(symbolID);
        }
        if (affiliationModifier != null)
        {   //Set affiliation modifier
            modifiers.put(Modifiers.E_FRAME_SHAPE_MODIFIER, affiliationModifier);
            //modifiers[Modifiers.E_FRAME_SHAPE_MODIFIER] = affiliationModifier;
        }//*/


        //            int y0 = 0;//W            E/F
        //            int y1 = 0;//X/Y          G
        //            int y2 = 0;//V/AD/AE      H/AF
        //            int y3 = 0;//T            M CC
        //            int y4 = 0;//Z            J/K/L/N/P
        //

        // <editor-fold defaultstate="collapsed" desc="Build Modifiers">
        String modifierValue = null;
        TextInfo tiTemp = null;
        //if(Modifiers.C_QUANTITY in modifiers
        if (modifiers.containsKey(Modifiers.C_QUANTITY)
                && SymbolUtilities.canSymbolHaveModifier(symbolID, Modifiers.C_QUANTITY))
        {
            String text = modifiers.get(Modifiers.C_QUANTITY);
            if(text != null)
            {
	            //bounds = armyc2.c5isr.renderer.utilities.RendererUtilities.getTextOutlineBounds(modifierFont, text, new SO.Point(0,0));
	            tiTemp = new TextInfo(text, 0, 0, modifierFont, modifierFontName);
	            labelBounds = tiTemp.getTextBounds();
	            labelWidth = labelBounds.width();
	            x = Math.round((symbolBounds.left + (symbolBounds.width() * 0.5f)) - (labelWidth * 0.5f));
	            y = Math.round(symbolBounds.top - bufferY - descent);
	            tiTemp.setLocation(x, y);
	            tiArray.add(tiTemp);
            }
        }

        //if(Modifiers.X_ALTITUDE_DEPTH in modifiers || Modifiers.Y_LOCATION in modifiers)
        if (modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH) || modifiers.containsKey(Modifiers.Y_LOCATION))
        {
            modifierValue = null;

            String xm = null,
                    ym = null;

            if (modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH) && SymbolUtilities.canSymbolHaveModifier(symbolID, Modifiers.X_ALTITUDE_DEPTH))
            {
                xm = modifiers.get(Modifiers.X_ALTITUDE_DEPTH);// xm = modifiers.X;
            }
            if (modifiers.containsKey(Modifiers.Y_LOCATION))
            {
                ym = modifiers.get(Modifiers.Y_LOCATION);// ym = modifiers.Y;
            }
            if (xm == null && ym != null)
            {
                modifierValue = ym;
            }
            else if (xm != null && ym == null)
            {
                modifierValue = xm;
            }
            else if (xm != null && ym != null)
            {
                modifierValue = xm + "  " + ym;
            }

            if(modifierValue != null)
            {
	            tiTemp = new TextInfo(modifierValue, 0, 0, modifierFont, modifierFontName);
	            labelBounds = tiTemp.getTextBounds();
	            labelWidth = labelBounds.width();
	
	            if (!byLabelHeight)
	            {
	                x = bounds.left - labelBounds.width() - bufferXL;
	                y = bounds.top + labelHeight - descent;
	            }
	            else
	            {
	                x = bounds.left - labelBounds.width() - bufferXL;
	
	                y = (bounds.height());
	                y = (int) ((y * 0.5) + (labelHeight * 0.5));
	
	                y = y - ((labelHeight + bufferText));
	                y = bounds.top + y;
	            }
	
	            tiTemp.setLocation(x, y);
	            tiArray.add(tiTemp);
            }
        }

        if (modifiers.containsKey(Modifiers.G_STAFF_COMMENTS) )
        {
            modifierValue = modifiers.get(Modifiers.G_STAFF_COMMENTS);

            if(modifierValue != null)
            {
	            tiTemp = new TextInfo(modifierValue, 0, 0, modifierFont, modifierFontName);
	            labelBounds = tiTemp.getTextBounds();
	            labelWidth = labelBounds.width();
	
	            x = bounds.left + bounds.width() + bufferXR;
	            if (!byLabelHeight)
	            {
	                y = bounds.top + labelHeight - descent;
	            }
	            else
	            {
	                y = (bounds.height());
	                y = (int) ((y * 0.5) + (labelHeight * 0.5));
	
	                y = y - ((labelHeight + bufferText));
	                y = bounds.top + y;
	            }
	
	            tiTemp.setLocation(x, y);
	            tiArray.add(tiTemp);

            }
        }

        if ((modifiers.containsKey(Modifiers.V_EQUIP_TYPE)) ||
                (modifiers.containsKey(Modifiers.AD_PLATFORM_TYPE)) ||
                (modifiers.containsKey(Modifiers.AE_EQUIPMENT_TEARDOWN_TIME)))
        {
            String vm = null,
                    adm = null,
                    aem = null;

            if (modifiers.containsKey(Modifiers.V_EQUIP_TYPE) && SymbolUtilities.canSymbolHaveModifier(symbolID, Modifiers.V_EQUIP_TYPE))
            {
                vm = modifiers.get(Modifiers.V_EQUIP_TYPE);
            }
            if (modifiers.containsKey(Modifiers.AD_PLATFORM_TYPE) && SymbolUtilities.canSymbolHaveModifier(symbolID, Modifiers.AD_PLATFORM_TYPE))
            {
                adm = modifiers.get(Modifiers.AD_PLATFORM_TYPE);
            }
            if (modifiers.containsKey(Modifiers.AE_EQUIPMENT_TEARDOWN_TIME) && SymbolUtilities.canSymbolHaveModifier(symbolID, Modifiers.AE_EQUIPMENT_TEARDOWN_TIME))
            {
                aem = modifiers.get(Modifiers.AE_EQUIPMENT_TEARDOWN_TIME);
            }

            modifierValue = "";
            if(vm != null && vm.equals("") == false)
                modifierValue = vm;
            if(adm != null && adm.equals("") == false)
                modifierValue += " " + adm;
            if(aem != null && aem.equals("") == false)
                modifierValue += " " + aem;

            if(modifierValue != null)
                modifierValue = modifierValue.trim();
            if(modifierValue != null && modifierValue.equals("") == false)
            {
	            tiTemp = new TextInfo(modifierValue, 0, 0, modifierFont, modifierFontName);
	            labelBounds = tiTemp.getTextBounds();
	            labelWidth = labelBounds.width();
	
	            x = bounds.left - labelBounds.width() - bufferXL;
	
	            y = (bounds.height());
	            y = (int) ((y * 0.5f) + ((labelHeight - descent) * 0.5f));
	            y = bounds.top + y;
	
	            tiTemp.setLocation(x, y);
	            tiArray.add(tiTemp);
            }
        }

        if (modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1) || modifiers.containsKey(Modifiers.AF_COMMON_IDENTIFIER))
        {
            modifierValue = "";
            String hm = "",
                    afm = "";

            hm = modifiers.get(Modifiers.H_ADDITIONAL_INFO_1);
            if (modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1))
            {
                hm = modifiers.get(Modifiers.H_ADDITIONAL_INFO_1);
            }
            if (modifiers.containsKey(Modifiers.AF_COMMON_IDENTIFIER) && SymbolUtilities.canSymbolHaveModifier(symbolID, Modifiers.AF_COMMON_IDENTIFIER))
            {
                afm = modifiers.get(Modifiers.AF_COMMON_IDENTIFIER);
            }

            modifierValue = hm + " " + afm;
            modifierValue = modifierValue.trim();

            if(modifierValue != null && modifierValue.equals("") == false)
            {
	            tiTemp = new TextInfo(modifierValue, 0, 0, modifierFont, modifierFontName);
	            labelBounds = tiTemp.getTextBounds();
	            labelWidth = labelBounds.width();
	
	            x = bounds.left + bounds.width() + bufferXR;
	
	            y = (bounds.height());
	            y = (int) ((y * 0.5) + ((labelHeight - descent) * 0.5));
	            y = bounds.top + y;
	
	            tiTemp.setLocation(x, y);
	            tiArray.add(tiTemp);

            }
        }

        if (modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1))
        {
            modifierValue = modifiers.get(Modifiers.T_UNIQUE_DESIGNATION_1);

            if(modifierValue != null)
            {
	            tiTemp = new TextInfo(modifierValue, 0, 0, modifierFont, modifierFontName);
	            labelBounds = tiTemp.getTextBounds();
	            labelWidth = labelBounds.width();
	
	            if (!byLabelHeight)
	            {
	                x = bounds.left - labelWidth - bufferXL;
	                y = bounds.top + bounds.height();
	            }
	            else
	            {
	                x = bounds.left - labelWidth - bufferXL;
	
	                y = (bounds.height());
	                y = (int) ((y * 0.5) + (labelHeight * 0.5));
	
	                y = y + ((labelHeight + bufferText) - descent);
	                y = bounds.top + y;
	            }
	
	            tiTemp.setLocation(x, y);
	            tiArray.add(tiTemp);
            }
        }

        if (modifiers.containsKey(Modifiers.M_HIGHER_FORMATION) || modifiers.containsKey(Modifiers.AS_COUNTRY))
        {
            modifierValue = "";

            if (modifiers.containsKey(Modifiers.M_HIGHER_FORMATION) && SymbolUtilities.canSymbolHaveModifier(symbolID, Modifiers.M_HIGHER_FORMATION))
            {
                modifierValue += modifiers.get(Modifiers.M_HIGHER_FORMATION);
            }
            if (modifiers.containsKey(Modifiers.AS_COUNTRY))
            {
                if (modifierValue.length() > 0)
                {
                    modifierValue += " ";
                }
                modifierValue += modifiers.get(Modifiers.AS_COUNTRY);
            }

            if(modifierValue.equals("")==false)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, modifierFont, modifierFontName);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = labelBounds.width();

                x = bounds.left + bounds.width() + bufferXR;
                if (!byLabelHeight)
                {
                    y = bounds.top + bounds.height();
                }
                else
                {
                    y = (bounds.height());
                    y = (int) ((y * 0.5) + (labelHeight * 0.5));

                    y = y + ((labelHeight + bufferText - descent));
                    y = bounds.top + y;
                }

                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);

            }
        }

        if (modifiers.containsKey(Modifiers.Z_SPEED) )
        {
            modifierValue = modifiers.get(Modifiers.Z_SPEED);

            if(modifierValue != null)
            {
	            tiTemp = new TextInfo(modifierValue, 0, 0, modifierFont, modifierFontName);
	            labelBounds = tiTemp.getTextBounds();
	            labelWidth = labelBounds.width();
	
	            x = bounds.left - labelWidth - bufferXL;
	            if (!byLabelHeight)
	            {
	                y = Math.round(bounds.top + bounds.height() + labelHeight + bufferText);
	            }
	            else
	            {
	                y = (bounds.height());
	                y = (int) ((y * 0.5) + (labelHeight * 0.5));
	
	                y = y + ((labelHeight + bufferText) * 2) - (descent * 2);
	                y = Math.round(bounds.top + y);
	            }
	
	            tiTemp.setLocation(x, y);
	            tiArray.add(tiTemp);
            }
        }

        if (modifiers.containsKey(Modifiers.J_EVALUATION_RATING)
                || modifiers.containsKey(Modifiers.K_COMBAT_EFFECTIVENESS)//
                || modifiers.containsKey(Modifiers.L_SIGNATURE_EQUIP)//
                || modifiers.containsKey(Modifiers.N_HOSTILE)//
                || modifiers.containsKey(Modifiers.P_IFF_SIF_AIS))//
        {
            modifierValue = null;

            String jm = null,
                    km = null,
                    lm = null,
                    nm = null,
                    pm = null;

            if (modifiers.containsKey(Modifiers.J_EVALUATION_RATING))
            {
                jm = modifiers.get(Modifiers.J_EVALUATION_RATING);
            }
            if (modifiers.containsKey(Modifiers.K_COMBAT_EFFECTIVENESS) && SymbolUtilities.canSymbolHaveModifier(symbolID, Modifiers.K_COMBAT_EFFECTIVENESS))
            {
                km = modifiers.get(Modifiers.K_COMBAT_EFFECTIVENESS);
            }
            if (modifiers.containsKey(Modifiers.L_SIGNATURE_EQUIP) && SymbolUtilities.canSymbolHaveModifier(symbolID, Modifiers.L_SIGNATURE_EQUIP))
            {
                lm = modifiers.get(Modifiers.L_SIGNATURE_EQUIP);
            }
            if (modifiers.containsKey(Modifiers.N_HOSTILE) && SymbolUtilities.canSymbolHaveModifier(symbolID, Modifiers.N_HOSTILE))
            {
                nm = modifiers.get(Modifiers.N_HOSTILE);
            }
            if (modifiers.containsKey(Modifiers.P_IFF_SIF_AIS) && SymbolUtilities.canSymbolHaveModifier(symbolID, Modifiers.P_IFF_SIF_AIS))
            {
                pm = modifiers.get(Modifiers.P_IFF_SIF_AIS);
            }

            modifierValue = "";
            if (jm != null && jm.equals("") == false)
            {
                modifierValue = modifierValue + jm;
            }
            if (km != null && km.equals("") == false)
            {
                modifierValue = modifierValue + " " + km;
            }
            if (lm != null && lm.equals("") == false)
            {
                modifierValue = modifierValue + " " + lm;
            }
            if (nm != null && nm.equals("") == false)
            {
                modifierValue = modifierValue + " " + nm;
            }
            if (pm != null && pm.equals("") == false)
            {
                modifierValue = modifierValue + " " + pm;
            }

            if (modifierValue.length() > 2 && modifierValue.charAt(0) == ' ')
            {
                modifierValue = modifierValue.substring(1);
            }

            modifierValue = modifierValue.trim();

            if(modifierValue.equals("")==false)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, modifierFont, modifierFontName);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = labelBounds.width();

                x = bounds.left + bounds.width() + bufferXR;
                if (!byLabelHeight)
                {
                    y = Math.round(bounds.top + bounds.height() + labelHeight + bufferText);
                }
                else
                {
                    y = (bounds.height());
                    y = (int) ((y * 0.5) + (labelHeight * 0.5));

                    y = y + ((labelHeight + bufferText) * 2) - (descent * 2);
                    y = Math.round(bounds.top + y);
                }

                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);

            }

        }

        if (modifiers.containsKey(Modifiers.W_DTG_1))
        {
            modifierValue = modifiers.get(Modifiers.W_DTG_1);

            if(modifierValue != null)
            {
	            tiTemp = new TextInfo(modifierValue, 0, 0, modifierFont, modifierFontName);
	            labelBounds = tiTemp.getTextBounds();
	            labelWidth = labelBounds.width();
	
	            if (!byLabelHeight)
	            {
	                x = bounds.left - labelWidth - bufferXL;
	                y = bounds.top - bufferY - descent;
	            }
	            else
	            {
	                x = bounds.left - labelWidth - bufferXL;
	
	                y = (bounds.height());
	                y = (int) ((y * 0.5) + (labelHeight * 0.5));
	
	                y = y - ((labelHeight + bufferText) * 2);
	                y = bounds.top + y;
	            }
	
	            tiTemp.setLocation(x, y);
	            tiArray.add(tiTemp);
            }
        }

        if (modifiers.containsKey(Modifiers.F_REINFORCED_REDUCED) || modifiers.containsKey(Modifiers.E_FRAME_SHAPE_MODIFIER))
        {
            modifierValue = null;
            String E = null,
                    F = null;

            if (modifiers.containsKey(Modifiers.E_FRAME_SHAPE_MODIFIER))
            {
                E = modifiers.get(Modifiers.E_FRAME_SHAPE_MODIFIER);
                modifiers.remove(Modifiers.E_FRAME_SHAPE_MODIFIER);
            }
            if (modifiers.containsKey(Modifiers.F_REINFORCED_REDUCED) && SymbolUtilities.canSymbolHaveModifier(symbolID, Modifiers.F_REINFORCED_REDUCED))
            {
                F = modifiers.get(Modifiers.F_REINFORCED_REDUCED);
            }

            if (E != null && E.equals("") == false)
            {
                modifierValue = E;
            }

            if (F != null && F.equals("") == false)
            {
                if (F.toUpperCase(Locale.US) == ("R"))
                {
                    F = "(+)";
                }
                else if (F.toUpperCase(Locale.US) == ("D"))
                {
                    F = "(-)";
                }
                else if (F.toUpperCase(Locale.US) == ("RD"))
                {
                    F = "(" + (char) (177) + ")";
                }
            }

            if (F != null && F.equals("") == false)
            {
                if (modifierValue != null && modifierValue.equals("") == false)
                {
                    modifierValue = modifierValue + " " + F;
                }
                else
                {
                    modifierValue = F;
                }
            }

            if(modifierValue != null)
            {
	            tiTemp = new TextInfo(modifierValue, 0, 0, modifierFont, modifierFontName);
	            labelBounds = tiTemp.getTextBounds();
	            labelWidth = labelBounds.width();
	
	            if (!byLabelHeight)
	            {
	                x = bounds.left + bounds.width() + bufferXR;
	                y = bounds.top - bufferY - descent;
	            }
	            else
	            {
	                x = bounds.left + bounds.width() + bufferXR;
	
	                y = (bounds.height());
	                y = (int) ((y * 0.5) + (labelHeight * 0.5));
	
	                y = y - ((labelHeight + bufferText) * 2);
	                y = bounds.top + y;
	            }
	
	            tiTemp.setLocation(x, y);
	            tiArray.add(tiTemp);

            }
        }

        if (modifiers.containsKey(Modifiers.AA_SPECIAL_C2_HQ) && SymbolUtilities.canSymbolHaveModifier(symbolID, Modifiers.AA_SPECIAL_C2_HQ))
        {
            modifierValue = modifiers.get(Modifiers.AA_SPECIAL_C2_HQ);

            if(modifierValue != null)
            {
	            tiTemp = new TextInfo(modifierValue, 0, 0, modifierFont, modifierFontName);
	            labelBounds = tiTemp.getTextBounds();
	            labelWidth = labelBounds.width();
	
	            x = (int) ((symbolBounds.left + (symbolBounds.width() * 0.5f)) - (labelWidth * 0.5f));
	
	            y = (symbolBounds.height());//checkpoint, get box above the point
	            y = (int) ((y * 0.5) + ((labelHeight - descent) * 0.5));
	            y = symbolBounds.top + y;
	
	            tiTemp.setLocation(x, y);
	            tiArray.add(tiTemp);
            }
        }


        // </editor-fold>

        //Shift Points and Draw
        newsdi = shiftUnitPointsAndDraw(tiArray,sdi,attributes, modifierFont);

        // <editor-fold defaultstate="collapsed" desc="Cleanup">
        tiArray = null;
        tiTemp = null;
        //tempShape = null;
        //ctx = null;
        //buffer = null;
        // </editor-fold>

        return newsdi;
    }


    public static SymbolDimensionInfo  processSPTextModifiers(SymbolDimensionInfo sdi, String symbolID, Map<String,String> modifiers, Map<String,String> attributes)
    {
        Paint modifierFont = getFont(attributes);
        float[] hd = getFontHeightandDescent(modifierFont);
        float modifierFontHeight = hd[0];
        float modifierFontDescent = hd[1];

        String modifierFontName = RendererSettings.getInstance().getModiferFontProps()[0];
        if(attributes != null && attributes.containsKey(MilStdAttributes.FontFamily))
        {
            String temp = attributes.get(MilStdAttributes.FontFamily);
            if(temp != null && !temp.isEmpty())
                modifierFontName = temp;
        }

        int bufferXL = 7;
        int bufferXR = 7;
        int bufferY = 2;
        int bufferText = 2;
        int x = 0;
        int y = 0;//best y
        SymbolDimensionInfo newsdi = null;

        ArrayList<TextInfo> tiArray = new ArrayList<TextInfo>(modifiers.size());

        int descent = (int) (modifierFontDescent + 0.5);


        Rect labelBounds = null;
        int labelWidth, labelHeight;

        Rect bounds = new Rect(sdi.getSymbolBounds());
        Rect imageBounds = new Rect(sdi.getImageBounds());

        //adjust width of bounds for mobility/echelon/engagement bar which could be wider than the symbol
        bounds = RectUtilities.makeRect(imageBounds.left, bounds.top, imageBounds.width(), bounds.height());

        labelHeight = (int) (modifierFontHeight + 0.5);

        //Affiliation Modifier being drawn as a display modifier
        String affiliationModifier = null;
        if (RS.getDrawAffiliationModifierAsLabel() == true)
        {
            affiliationModifier = SymbolUtilities.getStandardIdentityModifier(symbolID);
        }
        if (affiliationModifier != null)
        {   //Set affiliation modifier
            modifiers.put(Modifiers.E_FRAME_SHAPE_MODIFIER, affiliationModifier);
            //modifiers[Modifiers.E_FRAME_SHAPE_MODIFIER] = affiliationModifier;
        }//*/

        //Check for Valid Country Code
        int cc = SymbolID.getCountryCode(symbolID);
        String scc = "";
        if(cc > 0)
        {
            scc = GENCLookup.getInstance().get3CharCode(cc);
        }
        if(!scc.isEmpty())
            modifiers.put(Modifiers.AS_COUNTRY, scc);

        // <editor-fold defaultstate="collapsed" desc="Build Modifiers">
        String modifierValue = null;
        TextInfo tiTemp = null;


        List<Modifier> mods = getLabelPositionIndexes(symbolID, modifiers, attributes);

        Modifier mod = null;
        if(mods != null) {
            for (int i = 0; i < mods.size(); i++) {
                mod = mods.get(i);

                tiTemp = new TextInfo(mod.getText(), 0, 0, modifierFont, modifierFontName);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int) labelBounds.width();

                //on left
                x = (int) getLabelXPosition(bounds, sdi.getCenterPoint(), labelWidth, mod.getIndexX(), modifierFontHeight);
                //above center V
                y = (int) getLabelYPosition(bounds, labelHeight, descent, bufferText, mod.getCentered(), mod.getIndexY());

                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);
            }
        }


        // </editor-fold>

        //Shift Points and Draw
        newsdi = shiftUnitPointsAndDraw(tiArray,sdi,attributes, modifierFont);

        // <editor-fold defaultstate="collapsed" desc="Cleanup">
        tiArray = null;
        tiTemp = null;
        //tempShape = null;
        //ctx = null;
        //buffer = null;
        // </editor-fold>

        return newsdi;
    }

    public static SymbolDimensionInfo ProcessTGSPWithSpecialModifierLayout(SymbolDimensionInfo sdi, String symbolID, Map<String,String> modifiers, Map<String,String> attributes, Color lineColor)
    {
        Paint modifierFont = getFont(attributes);
        float[] hd = getFontHeightandDescent(modifierFont);
        float modifierFontHeight = hd[0];
        float modifierFontDescent = hd[1];

        String modifierFontName = RendererSettings.getInstance().getModiferFontProps()[0];
        if(attributes != null && attributes.containsKey(MilStdAttributes.FontFamily))
        {
            String temp = attributes.get(MilStdAttributes.FontFamily);
            if(temp != null && !temp.isEmpty())
                modifierFontName = temp;
        }

        ImageInfo ii = null;
        SVGSymbolInfo ssi = null;

        int bufferXL = 6;
        int bufferXR = 4;
        int bufferY = 2;
        int bufferText = 2;
        int centerOffset = 1; //getCenterX/Y function seems to go over by a pixel
        int x = 0;
        int y = 0;
        int x2 = 0;
        int y2 = 0;

        int outlineOffset = RS.getTextOutlineWidth();
        int labelHeight = 0;
        int labelWidth = 0;
        int alpha = -1;
        SymbolDimensionInfo newsdi = null;
        Color textColor = lineColor;
        Color textBackgroundColor = null;
        int ss = SymbolID.getSymbolSet(symbolID);
        int ec = SymbolID.getEntityCode(symbolID);
        int e = SymbolID.getEntity(symbolID);
        int et = SymbolID.getEntityType(symbolID);
        int est = SymbolID.getEntitySubtype(symbolID);

        //Feint Dummy Indicator variables
        Rect fdiBounds = null;
        Point fdiTop = null;
        Point fdiLeft = null;
        Point fdiRight = null;

        ArrayList<TextInfo> arrMods = new ArrayList<TextInfo>();
        boolean duplicate = false;

        Rect bounds = new Rect(sdi.getSymbolBounds());
        Rect symbolBounds = new Rect(sdi.getSymbolBounds());
        Point centerPoint = new Point(sdi.getCenterPoint());
        Rect imageBounds = new Rect(sdi.getImageBounds());

        if (attributes.containsKey(MilStdAttributes.Alpha))
        {
            alpha = Integer.parseInt(attributes.get(MilStdAttributes.Alpha));
            textColor.setAlpha(alpha);
        }

        centerPoint = new Point(Math.round(sdi.getCenterPoint().x), Math.round(sdi.getCenterPoint().y));

        boolean byLabelHeight = false;
        labelHeight = (int) (modifierFontHeight + 0.5f);

        int maxHeight = (symbolBounds.height());
        if ((labelHeight * 3) > maxHeight)
        {
            byLabelHeight = true;
        }

        int descent = (int) (modifierFontDescent + 0.5f);
        int yForY = -1;

        Rect labelBounds1 = null;//text.getPixelBounds(null, 0, 0);
        Rect labelBounds2 = null;
        String strText = "";
        String strText1 = "";
        String strText2 = "";
        TextInfo text1 = null;
        TextInfo text2 = null;


        if (outlineOffset > 2)
        {
            outlineOffset = ((outlineOffset - 1) / 2);
        }
        else
        {
            outlineOffset = 0;
        }


        // <editor-fold defaultstate="collapsed" desc="Process Special Modifiers">
        TextInfo ti = null;
        if (SymbolUtilities.isCBRNEvent(symbolID))//chemical
        {
            if ((labelHeight * 3) > bounds.height())
            {
                byLabelHeight = true;
            }
        }

        if(ss == SymbolID.SymbolSet_ControlMeasure) {
            if (ec == 130500 //contact point
                    || ec == 130700) //decision point
            {
                if (modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1)) {
                    strText = modifiers.get(Modifiers.T_UNIQUE_DESIGNATION_1);
                    if (strText != null) {
                        ti = new TextInfo(strText, 0, 0, modifierFont, modifierFontName);
                        labelWidth = Math.round(ti.getTextBounds().width());
                        //One modifier symbols and modifier goes in center
                        x = bounds.left + (int) (bounds.width() * 0.5f);
                        x = x - (int) (labelWidth * 0.5f);
                        y = bounds.top + (int) (bounds.height() * 0.4f);
                        y = y + (int) (labelHeight * 0.5f);

                        ti.setLocation(Math.round(x), Math.round(y));
                        arrMods.add(ti);
                    }
                }
            } else if (ec == 212800)//harbor
            {
                if (modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1)) {
                    strText = modifiers.get(Modifiers.H_ADDITIONAL_INFO_1);
                    if (strText != null) {
                        ti = new TextInfo(strText, 0, 0, modifierFont, modifierFontName);
                        labelWidth = Math.round(ti.getTextBounds().width());
                        //One modifier symbols and modifier goes in center
                        x = bounds.left + (int) (bounds.width() * 0.5f);
                        x = x - (int) (labelWidth * 0.5f);
                        y = bounds.top + (int) (bounds.height() * 0.5f);
                        y = y + (int) (labelHeight * 0.5f);

                        ti.setLocation(Math.round(x), Math.round(y));
                        arrMods.add(ti);
                    }
                }
            } else if (ec == 131300)//point of interest
            {
                if (modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1)) {
                    strText = modifiers.get(Modifiers.T_UNIQUE_DESIGNATION_1);
                    if (strText != null) {
                        ti = new TextInfo(strText, 0, 0, modifierFont, modifierFontName);
                        labelWidth = Math.round(ti.getTextBounds().width());
                        //One modifier symbols, top third & center
                        x = bounds.left + (int) (bounds.width() * 0.5f);
                        x = x - (int) (labelWidth * 0.5f);
                        y = bounds.top + (int) (bounds.height() * 0.25f);
                        y = y + (int) (labelHeight * 0.5f);

                        ti.setLocation(Math.round(x), Math.round(y));
                        arrMods.add(ti);
                    }
                }
            } else if (ec == 131800//waypoint
                    || ec == 240900)//fire support station
            {
                if (modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1)) {
                    strText = modifiers.get(Modifiers.T_UNIQUE_DESIGNATION_1);
                    if (strText != null) {
                        ti = new TextInfo(strText, 0, 0, modifierFont, modifierFontName);

                        //One modifier symbols and modifier goes right of center
                        if (ec == 131800)
                            x = bounds.left + (int) (bounds.width() * 0.75f);
                        else
                            x = bounds.left + (bounds.width());
                        y = bounds.top + (int) (bounds.height() * 0.5f);
                        y = y + (int) ((labelHeight - descent) * 0.5f);

                        ti.setLocation(Math.round(x), Math.round(y));
                        arrMods.add(ti);
                    }
                }
            }
            else if (ec == 131900)//Airfield (AEGIS Only)
            {
                if (modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1)) {
                    strText = modifiers.get(Modifiers.T_UNIQUE_DESIGNATION_1);
                    if (strText != null) {
                        ti = new TextInfo(strText, 0, 0, modifierFont, modifierFontName);

                        //One modifier symbols and modifier goes right of symbol

                        x = bounds.left + (bounds.width() + bufferXR);

                        y = bounds.top + (int) (bounds.height() * 0.5f);
                        y = y + (int) ((labelHeight - descent) * 0.5f);

                        ti.setLocation(Math.round(x), Math.round(y));
                        arrMods.add(ti);
                    }
                }
            }
            else if (ec == 180100 //Air Control point
                    || ec == 180200) //Communications Check point
            {
                if (modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1)) {
                    strText = modifiers.get(Modifiers.T_UNIQUE_DESIGNATION_1);
                    if (strText != null) {
                        ti = new TextInfo(strText, 0, 0, modifierFont, modifierFontName);
                        labelWidth = ti.getTextBounds().width();
                        //One modifier symbols and modifier goes just below of center
                        x = bounds.left + (int) (bounds.width() * 0.5);
                        x = x - (int) (labelWidth * 0.5);
                        y = bounds.top + (int) (bounds.height() * 0.5f);
                        y = y + (int) (((bounds.height() * 0.5f) - labelHeight) / 2) + labelHeight - descent;

                        ti.setLocation(Math.round(x), Math.round(y));
                        arrMods.add(ti);
                    }
                }
            } else if (ec == 160300 || //T (target reference point)
                    ec == 132000 || //T (Target Handover)
                    ec == 240601 || //ap,ap1,x,h (Point/Single Target)
                    ec == 240602) //ap (nuclear target)
            { //Targets with special modifier positions
                if (modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1)
                        && ec == 240601)//H //point single target
                {
                    strText = modifiers.get(Modifiers.H_ADDITIONAL_INFO_1);
                    if (strText != null) {
                        ti = new TextInfo(strText, 0, 0, modifierFont, modifierFontName);

                        x = RectUtilities.getCenterX(bounds) + (int) (bounds.width() * 0.15f);
                        y = bounds.top + (int) (bounds.height() * 0.75f);
                        y = y + (int) (labelHeight * 0.5f);

                        ti.setLocation(Math.round(x), Math.round(y));
                        arrMods.add(ti);
                    }
                }
                if (modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH)
                        && ec == 240601)//X point or single target
                {
                    strText = modifiers.get(Modifiers.X_ALTITUDE_DEPTH);
                    if (strText != null) {
                        ti = new TextInfo(strText, 0, 0, modifierFont, modifierFontName);
                        labelWidth = Math.round(ti.getTextBounds().width());
                        x = RectUtilities.getCenterX(bounds) - (int) (bounds.width() * 0.15f);
                        x = x - (labelWidth);
                        y = bounds.top + (int) (bounds.height() * 0.75f);
                        y = y + (int) (labelHeight * 0.5f);

                        ti.setLocation(Math.round(x), Math.round(y));
                        arrMods.add(ti);
                    }
                }
                strText = null;
                if (modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1) && (ec == 160300 || ec == 132000)) {
                    strText = modifiers.get(Modifiers.T_UNIQUE_DESIGNATION_1);
                    if (strText != null) {
                        ti = new TextInfo(strText, 0, 0, modifierFont, modifierFontName);

                        x = RectUtilities.getCenterX(bounds) + (int) (bounds.width() * 0.15f);
                        y = bounds.top + (int) (bounds.height() * 0.25f);
                        y = y + (int) (labelHeight * 0.5f);

                        ti.setLocation(Math.round(x), Math.round(y));
                        arrMods.add(ti);
                    }
                }
                if (ec == 240601 || ec == 240602)
                {
                    if (modifiers.containsKey(Modifiers.AP_TARGET_NUMBER)) {
                        strText = modifiers.get(Modifiers.AP_TARGET_NUMBER);
                    }
                    if (ec == 240601 && modifiers.containsKey(Modifiers.AP1_TARGET_NUMBER_EXTENSION)) {
                        if (strText != null)
                            strText = strText + "  " + modifiers.get(Modifiers.AP1_TARGET_NUMBER_EXTENSION);
                        else
                            strText = modifiers.get(Modifiers.AP1_TARGET_NUMBER_EXTENSION);
                    }
                    if (strText != null) {
                        ti = new TextInfo(strText, 0, 0, modifierFont, modifierFontName);

                        x = RectUtilities.getCenterX(bounds) + (int) (bounds.width() * 0.15f);
                        y = bounds.top + (int) (bounds.height() * 0.25f);
                        y = y + (int) (labelHeight * 0.5f);

                        ti.setLocation(Math.round(x), Math.round(y));
                        arrMods.add(ti);
                    }
                }
            }
            else if (ec == 132100)//Key Terrain
            {
                if (modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1)) {
                    strText = modifiers.get(Modifiers.T_UNIQUE_DESIGNATION_1);
                    if (strText != null) {
                        ti = new TextInfo(strText, 0, 0, modifierFont, modifierFontName);

                        //One modifier symbols and modifier goes right of symbol

                        x = bounds.left + (int)(bounds.width() * 0.5) + bufferXL;

                        y = bounds.top + (int) (bounds.height() * 0.5f);
                        y = y + (int) ((labelHeight - descent) * 0.5f);

                        ti.setLocation(Math.round(x), Math.round(y));
                        arrMods.add(ti);
                    }
                }
            }
            else if(ec == 182600)//Isolated Personnel Location
            {
                if (modifiers.containsKey(Modifiers.C_QUANTITY)) {
                    strText = modifiers.get(Modifiers.C_QUANTITY);
                    if (strText != null) {
                        ti = new TextInfo(strText, 0, 0, modifierFont, modifierFontName);
                        labelWidth = (int)Math.round(ti.getTextBounds().width());
                        //subset of NBC, just nuclear
                        x = (int)(bounds.left + (bounds.width() * 0.5));
                        x = x - (int) (labelWidth * 0.5);
                        y = (int)bounds.top - descent;
                        ti.setLocation(Math.round(x), Math.round(y));
                        arrMods.add(ti);
                    }
                }
                if (modifiers.containsKey(Modifiers.W_DTG_1)) {
                    strText = modifiers.get(Modifiers.W_DTG_1);
                    if (strText != null) {
                        ti = new TextInfo(strText, 0, 0, modifierFont, modifierFontName);
                        labelWidth = (int)Math.round(ti.getTextBounds().width());

                        x = (int)bounds.left - labelWidth - bufferXL;
                        if (!byLabelHeight) {
                            y = (int)bounds.top + labelHeight - descent;
                        } else {
                            //y = bounds.y + ((bounds.getHeight * 0.5) + (labelHeight * 0.5) - (labelHeight + bufferText));
                            y = (int)(bounds.top + ((bounds.height() * 0.5) - ((labelHeight - descent) * 0.5) + (-descent - bufferText)));
                        }

                        ti.setLocation(Math.round(x), Math.round(y));
                        arrMods.add(ti);
                    }
                }
                if (modifiers.containsKey(Modifiers.W1_DTG_2)) {
                    strText = modifiers.get(Modifiers.W1_DTG_2);
                    if (strText != null) {
                        ti = new TextInfo(strText, 0, 0, modifierFont, modifierFontName);
                        labelWidth = (int)Math.round(ti.getTextBounds().width());

                        x = (int)bounds.left - labelWidth - bufferXL;
                        if (!byLabelHeight) {
                            y = (int)bounds.top + labelHeight - descent;
                        } else {
                            //y = bounds.y + ((bounds.getHeight * 0.5) + (labelHeight * 0.5) - (labelHeight + bufferText));
                            y = (int)(bounds.top + ((bounds.height() * 0.5) - (((labelHeight * 2) - descent) * 0.5) + (-descent - bufferText)));
                        }

                        ti.setLocation(Math.round(x), Math.round(y));
                        arrMods.add(ti);
                    }
                }
            }
            else if (SymbolUtilities.isCBRNEvent(symbolID)) //CBRN
            {
                if (modifiers.containsKey(Modifiers.N_HOSTILE)) {
                    strText = modifiers.get(Modifiers.N_HOSTILE);
                    if (strText != null) {
                        ti = new TextInfo(strText, 0, 0, modifierFont, modifierFontName);

                        x = bounds.left + bounds.width() + bufferXR;

                        if (!byLabelHeight) {
                            y = bounds.top + bounds.height();
                        } else {
                            y = bounds.top + (int) ((bounds.height() * 0.5f) + ((labelHeight - descent) * 0.5) + (labelHeight - descent + bufferText));
                        }

                        ti.setLocation(Math.round(x), Math.round(y));
                        arrMods.add(ti);
                    }

                }
                if (modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1)) {
                    strText = modifiers.get(Modifiers.H_ADDITIONAL_INFO_1);
                    if (strText != null) {
                        ti = new TextInfo(strText, 0, 0, modifierFont, modifierFontName);

                        x = bounds.left + bounds.width() + bufferXR;
                        if (!byLabelHeight) {
                            y = bounds.top + labelHeight - descent;
                        } else {
                            //y = bounds.y + ((bounds.height * 0.5) + (labelHeight * 0.5) - (labelHeight + bufferText));
                            y = bounds.top + (int) ((bounds.height() * 0.5f) - ((labelHeight - descent) * 0.5) + (-descent - bufferText));
                        }

                        ti.setLocation(Math.round(x), Math.round(y));
                        arrMods.add(ti);
                    }
                }
                if (modifiers.containsKey(Modifiers.W_DTG_1)) {
                    strText = modifiers.get(Modifiers.W_DTG_1);
                    if (strText != null) {
                        ti = new TextInfo(strText, 0, 0, modifierFont, modifierFontName);
                        labelWidth = Math.round(ti.getTextBounds().width());

                        x = bounds.left - labelWidth - bufferXL;
                        if (!byLabelHeight) {
                            y = bounds.top + labelHeight - descent;
                        } else {
                            //y = bounds.y + ((bounds.height * 0.5) + (labelHeight * 0.5) - (labelHeight + bufferText));
                            y = bounds.top + (int) ((bounds.height() * 0.5) - ((labelHeight - descent) * 0.5) + (-descent - bufferText));
                        }

                        ti.setLocation(Math.round(x), Math.round(y));
                        arrMods.add(ti);
                    }
                }
                if ((ec == 281500 || ec == 281600) && modifiers.containsKey(Modifiers.V_EQUIP_TYPE)) {//nuclear event or nuclear fallout producing event
                    strText = modifiers.get(Modifiers.V_EQUIP_TYPE);
                    if (strText != null) {
                        ti = new TextInfo(strText, 0, 0, modifierFont, modifierFontName);

                        //subset of nbc, just nuclear
                        labelWidth = Math.round(ti.getTextBounds().width());
                        x = bounds.left - labelWidth - bufferXL;
                        y = bounds.top + (int) ((bounds.height() * 0.5) + ((labelHeight - descent) * 0.5));//((bounds.height / 2) - (labelHeight/2));

                        ti.setLocation(Math.round(x), Math.round(y));
                        arrMods.add(ti);
                    }
                }
                if (modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1)) {
                    strText = modifiers.get(Modifiers.T_UNIQUE_DESIGNATION_1);
                    if (strText != null) {
                        ti = new TextInfo(strText, 0, 0, modifierFont, modifierFontName);
                        labelWidth = Math.round(ti.getTextBounds().width());
                        x = bounds.left - labelWidth - bufferXL;
                        if (!byLabelHeight) {
                            y = bounds.top + bounds.height();
                        } else {
                            //y = bounds.y + ((bounds.height * 0.5) + ((labelHeight-descent) * 0.5) + (labelHeight + bufferText));
                            y = bounds.top + (int) ((bounds.height() * 0.5) + ((labelHeight - descent) * 0.5) + (labelHeight - descent + bufferText));
                        }
                        ti.setLocation(Math.round(x), Math.round(y));
                        arrMods.add(ti);
                    }
                }
                if (modifiers.containsKey(Modifiers.Y_LOCATION)) {
                    strText = modifiers.get(Modifiers.Y_LOCATION);
                    if (strText != null) {
                        ti = new TextInfo(strText, 0, 0, modifierFont, modifierFontName);
                        labelWidth = Math.round(ti.getTextBounds().width());
                        //just NBC
                        //x = bounds.left + (bounds.width() * 0.5);
                        //x = x - (labelWidth * 0.5);
                        x = bounds.left + (int) (bounds.width() * 0.5f);
                        x = x - (int) (labelWidth * 0.5f);

                        if (!byLabelHeight) {
                            y = bounds.top + bounds.height() + labelHeight - descent + bufferY;
                        } else {
                            y = bounds.top + (int) ((bounds.height() * 0.5) + ((labelHeight - descent) * 0.5) + ((labelHeight + bufferText) * 2) - descent);

                        }
                        yForY = y + descent; //so we know where to start the DOM arrow.
                        ti.setLocation(Math.round(x), Math.round(y));
                        arrMods.add(ti);
                    }

                }
                if (modifiers.containsKey(Modifiers.C_QUANTITY)) {
                    strText = modifiers.get(Modifiers.C_QUANTITY);
                    if (strText != null) {
                        ti = new TextInfo(strText, 0, 0, modifierFont, modifierFontName);
                        labelWidth = Math.round(ti.getTextBounds().width());
                        //subset of NBC, just nuclear
                        x = bounds.left + (int) (bounds.width() * 0.5);
                        x = x - (int) (labelWidth * 0.5);
                        y = bounds.top - descent;
                        ti.setLocation(Math.round(x), Math.round(y));
                        arrMods.add(ti);
                    }

                }
            }
            else if (ec == 270701)//static depiction
            {
                if (modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1)) {
                    strText = modifiers.get(Modifiers.H_ADDITIONAL_INFO_1);
                    if (strText != null) {
                        ti = new TextInfo(strText, 0, 0, modifierFont, modifierFontName);
                        labelWidth = Math.round(ti.getTextBounds().width());
                        x = bounds.left + (int) (bounds.width() * 0.5);
                        x = x - (int) (labelWidth * 0.5);
                        y = bounds.top - descent;// + (bounds.height * 0.5);
                        //y = y + (labelHeight * 0.5);

                        ti.setLocation(Math.round(x), Math.round(y));
                        arrMods.add(ti);
                    }

                }
                if (modifiers.containsKey(Modifiers.W_DTG_1)) {
                    strText = modifiers.get(Modifiers.W_DTG_1);
                    if (strText != null) {
                        ti = new TextInfo(strText, 0, 0, modifierFont, modifierFontName);
                        labelWidth = Math.round(ti.getTextBounds().width());
                        x = bounds.left + (int) (bounds.width() * 0.5);
                        x = x - (int) (labelWidth * 0.5);
                        y = bounds.top + (bounds.height());
                        y = y + (labelHeight);

                        ti.setLocation(Math.round(x), Math.round(y));
                        arrMods.add(ti);
                    }
                }
                if (modifiers.containsKey(Modifiers.N_HOSTILE)) {
                    strText = modifiers.get(Modifiers.N_HOSTILE);
                    if (strText != null) {
                        ti = new TextInfo(strText, 0, 0, modifierFont, modifierFontName);
                        TextInfo ti2 = new TextInfo(strText, 0, 0, modifierFont, modifierFontName);
                        labelWidth = Math.round(ti.getTextBounds().width());
                        x = bounds.left + (bounds.width()) + bufferXR;//right
                        //x = x + labelWidth;//- (labelBounds.width * 0.75);

                        duplicate = true;

                        x2 = bounds.left;//left
                        x2 = x2 - labelWidth - bufferXL;// - (labelBounds.width * 0.25);

                        y = bounds.top + (int) (bounds.height() * 0.5);//center
                        y = y + (int) ((labelHeight - descent) * 0.5);

                        y2 = y;

                        ti.setLocation(Math.round(x), Math.round(y));
                        ti2.setLocation(Math.round(x2), Math.round(y2));
                        arrMods.add(ti);
                        arrMods.add(ti2);
                    }
                }

            }
            else if(e == 21 && et == 35)//sonobuoys
            {
                //H sitting on center of circle to the right
                //T above H
                centerPoint = SymbolUtilities.getCMSymbolAnchorPoint(symbolID,RectUtilities.makeRectFFromRect(bounds));
                if (modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1)) {
                    strText = modifiers.get(Modifiers.H_ADDITIONAL_INFO_1);
                    if (strText != null) {
                        ti = new TextInfo(strText, 0, 0, modifierFont, modifierFontName);
                        TextInfo ti2 = new TextInfo(strText, 0, 0, modifierFont, modifierFontName);
                        labelWidth = Math.round(ti.getTextBounds().width());
                        x = bounds.left + (bounds.width()) + bufferXR;//right
                        y = centerPoint.y;

                        ti.setLocation(Math.round(x), Math.round(y));
                        arrMods.add(ti);
                    }
                }
                if (est == 0 || est == 1 || est == 4 || est == 7 || est == 8 || est == 15) {
                    if (modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1)) {
                        strText = modifiers.get(Modifiers.T_UNIQUE_DESIGNATION_1);
                        if (strText != null) {
                            ti = new TextInfo(strText, 0, 0, modifierFont, modifierFontName);
                            TextInfo ti2 = new TextInfo(strText, 0, 0, modifierFont, modifierFontName);
                            labelWidth = Math.round(ti.getTextBounds().width());
                            x = bounds.left + (bounds.width()) + bufferXR;//right
                            y = centerPoint.y - labelHeight;

                            ti.setLocation(Math.round(x), Math.round(y));
                            arrMods.add(ti);
                        }
                    }
                }
            }
            else if(ec == 282001 || //tower, low
                    ec == 282002)   //tower, high
            {
                if (modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH)) {
                    strText = modifiers.get(Modifiers.X_ALTITUDE_DEPTH);
                    if (strText != null) {
                        ti = new TextInfo(strText, 0, 0, modifierFont, modifierFontName);
                        labelWidth = Math.round(ti.getTextBounds().width());
                        x = bounds.left + (int) (bounds.width() * 0.7);
                        y = bounds.top + labelHeight;// + (bounds.height * 0.5);
                        //y = y + (labelHeight * 0.5);

                        ti.setLocation(Math.round(x), Math.round(y));
                        arrMods.add(ti);
                    }

                }
            }
            else if(ec == 180600)//TACAN
            {
                if (modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1)) {
                    strText = modifiers.get(Modifiers.T_UNIQUE_DESIGNATION_1);
                    if (strText != null) {
                        ti = new TextInfo(strText, 0, 0, modifierFont, modifierFontName);
                        labelWidth = Math.round(ti.getTextBounds().width());
                        x = bounds.left + bounds.width() + bufferXR;
                        y = bounds.top + labelHeight;// + (bounds.height * 0.5);
                        //y = y + (labelHeight * 0.5);

                        ti.setLocation(Math.round(x), Math.round(y));
                        arrMods.add(ti);
                    }

                }
            }
            else if(ec == 210300)//Defended Asset
            {
                if (modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1)) {
                    strText = modifiers.get(Modifiers.T_UNIQUE_DESIGNATION_1);
                    if (strText != null) {
                        ti = new TextInfo(strText, 0, 0, modifierFont, modifierFontName);
                        labelWidth = Math.round(ti.getTextBounds().width());
                        x = bounds.left - labelWidth - bufferXL;
                        y = bounds.top + labelHeight;// + (bounds.height * 0.5);
                        //y = y + (labelHeight * 0.5);

                        ti.setLocation(Math.round(x), Math.round(y));
                        arrMods.add(ti);
                    }

                }
            }
            else if(ec == 210600)//Air Detonation
            {
                if (modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH)) {
                    strText = modifiers.get(Modifiers.X_ALTITUDE_DEPTH);
                    if (strText != null) {
                        ti = new TextInfo(strText, 0, 0, modifierFont, modifierFontName);
                        labelWidth = Math.round(ti.getTextBounds().width());
                        x = bounds.left + (bounds.width() + bufferXR);
                        y = bounds.top + labelHeight;// + (bounds.height * 0.5);
                        //y = y + (labelHeight * 0.5);

                        ti.setLocation(Math.round(x), Math.round(y));
                        arrMods.add(ti);
                    }

                }
            }
            else if(ec == 210800)//Impact Point
            {
                if (modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1)) {
                    strText = modifiers.get(Modifiers.H_ADDITIONAL_INFO_1);
                    if (strText != null) {
                        ti = new TextInfo(strText, 0, 0, modifierFont, modifierFontName);
                        labelWidth = Math.round(ti.getTextBounds().width());
                        x = bounds.left + (int) (bounds.width() * 0.65f);
//                  x = x - (labelBounds.width * 0.5);
                        y = bounds.top + (int) (bounds.height() * 0.25f);
                        y = y + (int) (labelHeight * 0.5f);
                        //y = y + (labelHeight * 0.5);

                        ti.setLocation(Math.round(x), Math.round(y));
                        arrMods.add(ti);
                    }

                }
            }
            else if(ec == 211000)//Launched Torpedo
            {
                if (modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1)) {
                    strText = modifiers.get(Modifiers.H_ADDITIONAL_INFO_1);
                    if (strText != null) {
                        ti = new TextInfo(strText, 0, 0, modifierFont, modifierFontName);
                        labelWidth = Math.round(ti.getTextBounds().width());
                        x = (int)(bounds.left + (bounds.width() * 0.5) - (labelWidth/2));
                        y = bounds.top - bufferY;

                        ti.setLocation(Math.round(x), Math.round(y));
                        arrMods.add(ti);
                    }

                }
            }
            else if(ec == 214900 || ec == 215600)//General Sea SubSurface Station & General Sea Surface Station
            {
                if (modifiers.containsKey(Modifiers.W_DTG_1)) {
                    strText = modifiers.get(Modifiers.W_DTG_1);
                    if (strText != null) {
                        ti = new TextInfo(strText + " - ", 0, 0, modifierFont, modifierFontName);
                        x = bounds.left + (bounds.width() + bufferXR);
                        y = bounds.top + labelHeight;

                        ti.setLocation(Math.round(x), Math.round(y));
                        arrMods.add(ti);
                    }

                }
                if (modifiers.containsKey(Modifiers.W1_DTG_2)) {
                    strText = modifiers.get(Modifiers.W1_DTG_2);
                    if (strText != null) {
                        ti = new TextInfo(strText, 0, 0, modifierFont, modifierFontName);
                        x = bounds.left + (bounds.width() + bufferXR);
                        y = bounds.top + (labelHeight * 2);

                        ti.setLocation(Math.round(x), Math.round(y));
                        arrMods.add(ti);
                    }

                }
                if (modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1)) {
                    strText = modifiers.get(Modifiers.T_UNIQUE_DESIGNATION_1);
                    if (strText != null) {
                        ti = new TextInfo(strText, 0, 0, modifierFont, modifierFontName);
                        x = bounds.left + (bounds.width() + bufferXR);
                        y = bounds.top + (labelHeight * 3);

                        ti.setLocation(Math.round(x), Math.round(y));
                        arrMods.add(ti);
                    }

                }
            }
            else if(ec == 217000)//Shore Control Station
            {
                if (modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1)) {
                    strText = modifiers.get(Modifiers.H_ADDITIONAL_INFO_1);
                    if (strText != null) {
                        ti = new TextInfo(strText, 0, 0, modifierFont, modifierFontName);
                        labelWidth = Math.round(ti.getTextBounds().width());
                        x = (int)(bounds.left + (bounds.width() * 0.5) - (labelWidth/2));
                        y = bounds.top + bounds.height() + labelHeight + bufferY;

                        ti.setLocation(Math.round(x), Math.round(y));
                        arrMods.add(ti);
                    }

                }
            }
            else if(ec == 250600)//Known Point
            {
                if (modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1)) {
                    strText = modifiers.get(Modifiers.T_UNIQUE_DESIGNATION_1);
                    if (strText != null) {
                        ti = new TextInfo(strText, 0, 0, modifierFont, modifierFontName);
                        labelWidth = Math.round(ti.getTextBounds().width());
                        x = bounds.left + (bounds.width() + bufferXR);
//                  x = x - (labelBounds.width * 0.5);
                        y = bounds.top + (int) (bounds.height() * 0.25f);
                        y = y + (int) (labelHeight * 0.5f);
                        //y = y + (labelHeight * 0.5);

                        ti.setLocation(Math.round(x), Math.round(y));
                        arrMods.add(ti);
                    }

                }
            }
        }
        else if(ss == SymbolID.SymbolSet_Atmospheric)
        {
            String modX = null;
            if(modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH))
                modX = (modifiers.get(Modifiers.X_ALTITUDE_DEPTH));

            if(ec == 162300)//Freezing Level
            {
                strText = "0" + (char)(176) + ":";
                if(modX != null)
                    strText += modX;
                else
                    strText += "?";

                ti = new TextInfo(strText, 0, 0, modifierFont, modifierFontName);
                labelWidth = Math.round(ti.getTextBounds().width());
                //One modifier symbols and modifier goes in center
                x = bounds.left + (int) (bounds.width() * 0.5f);
                x = x - (int) (labelWidth * 0.5f);
                y = bounds.top + (int) (bounds.height() * 0.5f);
                y = y + (int) ((labelHeight - modifierFontDescent) * 0.5f);

                ti.setLocation(Math.round(x), Math.round(y));
                arrMods.add(ti);
            }
            else if(ec == 162200)//tropopause Level
            {
                strText = "X?";
                if(modX != null)
                    strText = modX;

                ti = new TextInfo(strText, 0, 0, modifierFont, modifierFontName);
                labelWidth = Math.round(ti.getTextBounds().width());
                //One modifier symbols and modifier goes in center
                x = bounds.left + (int) (bounds.width() * 0.5f);
                x = x - (int) (labelWidth * 0.5f);
                y = bounds.top + (int) (bounds.height() * 0.5f);
                y = y + (int) ((labelHeight - modifierFontDescent) * 0.5f);

                ti.setLocation(Math.round(x), Math.round(y));
                arrMods.add(ti);
            }
            else if(ec == 110102)//tropopause Low
            {
                strText = "X?";
                if(modX != null)
                    strText = modX;

                ti = new TextInfo(strText, 0, 0, modifierFont, modifierFontName);
                labelWidth = Math.round(ti.getTextBounds().width());
                //One modifier symbols and modifier goes in center
                x = bounds.left + (int) (bounds.width() * 0.5f);
                x = x - (int) (labelWidth * 0.5f);
                y = bounds.top + (int) (bounds.height() * 0.5f);
                y = y - descent;

                ti.setLocation(Math.round(x), Math.round(y));
                arrMods.add(ti);
            }
            else if(ec == 110202)//tropopause High
            {
                strText = "X?";
                if(modX != null)
                    strText = modX;

                ti = new TextInfo(strText, 0, 0, modifierFont, modifierFontName);
                labelWidth = Math.round(ti.getTextBounds().width());
                //One modifier symbols and modifier goes in center
                x = bounds.left + (int) (bounds.width() * 0.5f);
                x = x - (int) (labelWidth * 0.5f);
                y = bounds.top + (int) (bounds.height() * 0.5f);
                //y = y + (int) ((labelHeight * 0.5f) + (labelHeight/2));
                y = y + (int) (((labelHeight * 0.5f) - (labelHeight/2)) + labelHeight - descent);

                ti.setLocation(Math.round(x), Math.round(y));
                arrMods.add(ti);
            }
        }
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="DOM Arrow">
        Point[] domPoints = null;
        Rect domBounds = null;

        if (modifiers.containsKey(Modifiers.Q_DIRECTION_OF_MOVEMENT) &&
                SymbolUtilities.isCBRNEvent(symbolID))//CBRN events
        {
            strText = modifiers.get(Modifiers.Q_DIRECTION_OF_MOVEMENT);
            if(strText != null && SymbolUtilities.isNumber(strText))
            {
	            float q = Float.parseFloat(strText);
	            Rect tempBounds = new Rect(bounds);
	            tempBounds.union(RectUtilities.getCenterX(bounds), yForY);
	
	            domPoints = createDOMArrowPoints(symbolID, tempBounds, sdi.getCenterPoint(), q, false, modifierFontHeight);
	
	            domBounds = RectUtilities.makeRect(domPoints[0].x, domPoints[0].y, 1, 1);
	
	            Point temp = null;
	            for (int i = 1; i < 6; i++)
	            {
	                temp = domPoints[i];
	                if (temp != null)
	                {
	                    domBounds.union(temp.x, temp.y);
	                }
	            }
	            imageBounds.union(domBounds);
            }
        }
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Build Feint Dummy Indicator">
        if (SymbolUtilities.hasFDI(symbolID))
        {
            //create feint indicator /\
            fdiLeft = new Point((int) symbolBounds.left, (int) symbolBounds.top);
            fdiRight = new Point((int) (symbolBounds.left + symbolBounds.width()), (int) symbolBounds.top);
            fdiTop = new Point(Math.round(RectUtilities.getCenterX(symbolBounds)), Math.round(symbolBounds.top - (symbolBounds.width() * .5f)));


            fdiBounds = RectUtilities.makeRect(fdiLeft.x, fdiLeft.y, 1, 1);
            fdiBounds.union(fdiTop.x, fdiTop.y);
            fdiBounds.union(fdiRight.x, fdiRight.y);

            float fdiStrokeWidth = Math.round(RendererSettings.getInstance().getDeviceDPI() / 96f);
            RectUtilities.grow(fdiBounds,Math.round(fdiStrokeWidth/2));

            ti = new TextInfo("TEST",0,0, modifierFont, modifierFontName);
            if (ti != null && SymbolUtilities.isCBRNEvent(symbolID))
            {
                int shiftY = Math.round(symbolBounds.top - ti.getTextBounds().height() - 2);
                fdiLeft.offset(0, shiftY);
                fdiTop.offset(0, shiftY);
                fdiRight.offset(0, shiftY);
                fdiBounds.offset(0, shiftY);
            }

            imageBounds.union(fdiBounds);

        }
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Shift Points and Draw">
        Rect modifierBounds = null;
        if (arrMods != null && arrMods.size() > 0)
        {

            //build modifier bounds/////////////////////////////////////////
            modifierBounds = arrMods.get(0).getTextOutlineBounds();
            int size = arrMods.size();
            TextInfo tempShape = null;
            for (int i = 1; i < size; i++)
            {
                tempShape = arrMods.get(i);
                modifierBounds.union(tempShape.getTextOutlineBounds());
            }

        }

        if (modifierBounds != null || domBounds != null || fdiBounds != null)
        {

            if (modifierBounds != null)
            {
                imageBounds.union(modifierBounds);
            }
            if (domBounds != null)
            {
                imageBounds.union(domBounds);
            }
            if (fdiBounds != null)
            {
                imageBounds.union(fdiBounds);
            }

            //shift points if needed////////////////////////////////////////
            if (sdi instanceof ImageInfo && (imageBounds.left < 0 || imageBounds.top < 0))
            {
                int shiftX = Math.abs(imageBounds.left);
                int shiftY = Math.abs(imageBounds.top);

                //shift mobility points
                int size = arrMods.size();
                TextInfo tempShape = null;
                for (int i = 0; i < size; i++)
                {
                    tempShape = arrMods.get(i);
                    tempShape.shift(shiftX, shiftY);
                }
                if(modifierBounds != null)
                    modifierBounds.offset(shiftX, shiftY);

                if (domBounds != null)
                {
                    for (int i = 0; i < 6; i++)
                    {
                        Point temp = domPoints[i];
                        if (temp != null)
                        {
                            temp.offset(shiftX, shiftY);
                        }
                    }
                    domBounds.offset(shiftX, shiftY);
                }

                //If there's an FDI
                if (fdiBounds != null)
                {
                    fdiBounds.offset(shiftX, shiftY);
                    fdiLeft.offset(shiftX, shiftY);
                    fdiTop.offset(shiftX, shiftY);
                    fdiRight.offset(shiftX, shiftY);
                }

                //shift image points
                centerPoint.offset(shiftX, shiftY);
                symbolBounds.offset(shiftX, shiftY);
                imageBounds.offset(shiftX, shiftY);
            }


            if (attributes.containsKey(MilStdAttributes.TextColor))
            {
                textColor = RendererUtilities.getColorFromHexString(attributes.get(MilStdAttributes.TextColor));
                if(alpha > -1)
                    textColor.setAlpha(alpha);
            }
            if (attributes.containsKey(MilStdAttributes.TextBackgroundColor))
            {
                textBackgroundColor = RendererUtilities.getColorFromHexString(attributes.get(MilStdAttributes.TextBackgroundColor));
                if(alpha > -1)
                    textBackgroundColor.setAlpha(alpha);
            }

            if(sdi instanceof ImageInfo) {
                ii = (ImageInfo)sdi;
                //Render modifiers//////////////////////////////////////////////////
                Bitmap bmp = Bitmap.createBitmap(imageBounds.width(), imageBounds.height(), Config.ARGB_8888);
                Canvas ctx = new Canvas(bmp);

                //render////////////////////////////////////////////////////////
                //draw original icon with potential modifiers.
                ctx.drawBitmap(ii.getImage(), symbolBounds.left, symbolBounds.top, null);
                //ctx.drawImage(ii.getImage(),imageBoundsOld.left,imageBoundsOld.top);


                renderText(ctx, arrMods, textColor, textBackgroundColor, modifierFont);

                //draw DOM arrow
                if (domBounds != null)
                {
                    drawDOMArrow(ctx, domPoints, alpha, lineColor);
                }

                //<editor-fold defaultstate="collapsed" desc="Draw FDI">
                if (fdiBounds != null) {

                    Paint fdiPaint = new Paint();
                    fdiPaint.setAntiAlias(true);
                    fdiPaint.setColor(lineColor.toInt());/// setARGB(255, 0, 0, 0);
                    if (alpha > -1)
                        fdiPaint.setAlpha(alpha);
                    fdiPaint.setStyle(Style.STROKE);

                    int dpi = RendererSettings.getInstance().getDeviceDPI();
                    int lineLength = dpi / 96 * 6;
                    int lineGap = dpi / 96 * 4;

                    /// ///////////////////////////////////
                    //Divide line in 14 parts. line is 3 parts to 2 parts gap
                    float distance = RendererUtilities.getDistanceBetweenPoints(fdiTop,fdiLeft);
                    //distance = distance / 14f;
                    lineGap = (int)((distance / 14f) * 2);
                    lineLength = (int)((distance / 14f) * 3);
                    /// //////////////////////////////////

                    fdiPaint.setPathEffect(new DashPathEffect(new float[]
                            {
                                    lineLength, lineGap
                            }, 0));

                    float fdiStrokeWidth = Math.round(dpi / 96f);
                    if (fdiStrokeWidth < 2)
                        fdiStrokeWidth = 2;

                    fdiPaint.setStrokeCap(Cap.ROUND);
                    fdiPaint.setStrokeJoin(Join.MITER);
                    fdiPaint.setStrokeWidth(fdiStrokeWidth);

                    Path fdiPath = new Path();

                    fdiPath.moveTo(fdiTop.x, fdiTop.y);
                    fdiPath.lineTo(fdiLeft.x, fdiLeft.y);
                    fdiPath.moveTo(fdiTop.x, fdiTop.y);
                    fdiPath.lineTo(fdiRight.x, fdiRight.y);
                    ctx.drawPath(fdiPath, fdiPaint);

                    fdiBounds = null;

                }
                //</editor-fold>

                newsdi = new ImageInfo(bmp, centerPoint, symbolBounds);
                ctx = null;
            }
            else if(sdi instanceof SVGSymbolInfo)
            {
                float strokeWidth = Math.round(RendererSettings.getInstance().getDeviceDPI() / 96f);
                if(strokeWidth < 1)
                    strokeWidth=1;
                String svgStroke = RendererUtilities.colorToHexString(lineColor,false);
                String svgStrokeWidth = String.valueOf(strokeWidth);//"3";

                ssi = (SVGSymbolInfo)sdi;
                StringBuilder sbSVG = new StringBuilder();
                sbSVG.append(ssi.getSVG());
                sbSVG.append(renderTextElements(arrMods,textColor,textBackgroundColor));

                // <editor-fold defaultstate="collapsed" desc="DOM arrow">
                if (domBounds != null && domPoints.length == 6)
                {
                    SVGPath domPath = new SVGPath() ;

                    domPath.moveTo(domPoints[0].x, domPoints[0].y);
                    if (domPoints[1] != null)
                    {
                        domPath.lineTo(domPoints[1].x, domPoints[1].y);
                    }
                    if (domPoints[2] != null)
                    {
                        domPath.lineTo(domPoints[2].x, domPoints[2].y);
                    }
                    sbSVG.append(domPath.toSVGElement(svgStroke,Float.parseFloat(svgStrokeWidth),null,1f,1f,null));

                    domPath = new SVGPath();

                    domPath.moveTo(domPoints[3].x, domPoints[3].y);
                    domPath.lineTo(domPoints[4].x, domPoints[4].y);
                    domPath.lineTo(domPoints[5].x, domPoints[5].y);
                    sbSVG.append(domPath.toSVGElement(null,0f,svgStroke,1f,1f,null));

                    domBounds = null;
                    domPoints = null;
                }
                // </editor-fold>

                //<editor-fold defaultstate="collapsed" desc="Draw FDI">
                if (fdiBounds != null)
                {

                    /*int dpi = RendererSettings.getInstance().getDeviceDPI();
                    int lineLength = dpi / 96 * 6;
                    int lineGap = dpi / 96 * 4;
                    String svgFDIDashArray = "" + lineLength + " " + lineGap;//*/

                    /// ///////////////////////////////////
                    //Divide line in 14 parts. line is 3 parts to 2 parts gap
                    float distance = RendererUtilities.getDistanceBetweenPoints(fdiTop,fdiLeft);
                    //distance = distance / 14f;
                    int lineGap = (int)((distance / 14f) * 2);
                    int lineLength = (int)((distance / 14f) * 3);
                    String svgFDIDashArray = "" + lineLength + " " + lineGap;
                    /// //////////////////////////////////

                    SVGPath fdiPath = new SVGPath();
                    fdiPath.moveTo(fdiTop.x, fdiTop.y);
                    fdiPath.lineTo(fdiLeft.x, fdiLeft.y);
                    fdiPath.moveTo(fdiTop.x, fdiTop.y);
                    fdiPath.lineTo(fdiRight.x, fdiRight.y);//*/

                    fdiPath.setLineDash(svgFDIDashArray);

                    sbSVG.append(fdiPath.toSVGElement(svgStroke,Float.parseFloat(svgStrokeWidth),null,1f,1f,"round"));
                    //sbSVG.append(Shape2SVG.Convert(fdiPath, svgStroke, null, svgStrokeWidth, svgAlpha, svgAlpha, svgFDIDashArray));
                }
                //</editor-fold>

                newsdi = new SVGSymbolInfo(sbSVG.toString(),centerPoint,symbolBounds,imageBounds);
            }




            // <editor-fold defaultstate="collapsed" desc="Cleanup">
            //ctx = null;
            // </editor-fold>

            return newsdi;

        }
        else
        {
            return null;
        }
        // </editor-fold>

    }

    /**
     * Process modifiers for action points
     */
    public static SymbolDimensionInfo ProcessTGSPModifiers(SymbolDimensionInfo sdi, String symbolID, Map<String,String> modifiers, Map<String,String> attributes, Color lineColor)
    {

        Paint modifierFont = getFont(attributes);
        float[] hd = getFontHeightandDescent(modifierFont);
        float modifierFontHeight = hd[0];
        float modifierFontDescent = hd[1];

        String modifierFontName = RendererSettings.getInstance().getModiferFontProps()[0];
        if(attributes != null && attributes.containsKey(MilStdAttributes.FontFamily))
        {
            String temp = attributes.get(MilStdAttributes.FontFamily);
            if(temp != null && !temp.isEmpty())
                modifierFontName = temp;
        }

        // <editor-fold defaultstate="collapsed" desc="Variables">
        ImageInfo ii = null;
        SVGSymbolInfo ssi = null;

        int bufferXL = 6;
        int bufferXR = 4;
        int bufferY = 2;
        int bufferText = 2;
        int centerOffset = 1; //getCenterX/Y function seems to go over by a pixel
        int x = 0;
        int y = 0;
        int x2 = 0;
        int y2 = 0;

        //Feint Dummy Indicator variables
        Rect fdiBounds = null;
        Point fdiTop = null;
        Point fdiLeft = null;
        Point fdiRight = null;

        int outlineOffset = RS.getTextOutlineWidth();
        int labelHeight = 0;
        int labelWidth = 0;
        int alpha = -1;
        SymbolDimensionInfo newsdi = null;
        
        Color textColor = lineColor;
        Color textBackgroundColor = null;

        ArrayList<TextInfo> arrMods = new ArrayList<TextInfo>();
        boolean duplicate = false;

        MSInfo msi = MSLookup.getInstance().getMSLInfo(symbolID);


        if (attributes.containsKey(MilStdAttributes.Alpha))
        {
            alpha = Integer.parseInt(attributes.get(MilStdAttributes.Alpha));
        }

        Rect bounds = new Rect(sdi.getSymbolBounds());
        Rect symbolBounds = new Rect(sdi.getSymbolBounds());
        Point centerPoint = new Point(sdi.getCenterPoint());
        Rect imageBounds = new Rect(sdi.getImageBounds());

        centerPoint = new Point(Math.round(sdi.getCenterPoint().x), Math.round(sdi.getCenterPoint().y));

        boolean byLabelHeight = false;

        labelHeight = Math.round(modifierFontHeight + 0.5f);
        int maxHeight = (symbolBounds.height());
        if ((labelHeight * 3) > maxHeight)
        {
            byLabelHeight = true;
        }

        int descent = (int) (modifierFontDescent + 0.5f);
        int yForY = -1;

        Rect labelBounds1 = null;//text.getPixelBounds(null, 0, 0);
        Rect labelBounds2 = null;
        String strText = "";
        String strText1 = "";
        String strText2 = "";
        TextInfo text1 = null;
        TextInfo text2 = null;

        String basicID = SymbolUtilities.getBasicSymbolID(symbolID);

        if (outlineOffset > 2)
        {
            outlineOffset = ((outlineOffset - 1) / 2);
        }
        else
        {
            outlineOffset = 0;
        }

        /*bufferXL += outlineOffset;
         bufferXR += outlineOffset;
         bufferY += outlineOffset;
         bufferText += outlineOffset;*/
        // </editor-fold>
        // <editor-fold defaultstate="collapsed" desc="Process Modifiers">
        TextInfo ti = null;

        {
            if (msi.getModifiers().contains(Modifiers.N_HOSTILE) && modifiers.containsKey(Modifiers.N_HOSTILE))
            {
                strText = modifiers.get(Modifiers.N_HOSTILE);
                if(strText != null)
                {
	                ti = new TextInfo(strText, 0, 0, modifierFont, modifierFontName);
	
	                x = bounds.left + bounds.width() + bufferXR;
	
	                if (!byLabelHeight)
	                {
	                    y = ((bounds.height() / 3) * 2);//checkpoint, get box above the point
	                    y = bounds.top + y;
	                }
	                else
	                {
	                    //y = ((labelHeight + bufferText) * 3);
	                    //y = bounds.y + y - descent;
	                    y = bounds.top + bounds.height();
	                }
	
	                ti.setLocation(x, y);
	                arrMods.add(ti);
                }

            }
            if (msi.getModifiers().contains(Modifiers.H_ADDITIONAL_INFO_1) && modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1))
            {
            	strText = modifiers.get(Modifiers.H_ADDITIONAL_INFO_1);
            	if(strText != null)
            	{
	                ti = new TextInfo(strText, 0, 0, modifierFont, modifierFontName);
	                labelWidth = Math.round(ti.getTextBounds().width());
	
	                x = bounds.left + (int) (bounds.width() * 0.5f);
	                x = x - (int) (labelWidth * 0.5f);
	                y = bounds.top - descent;
	
	                ti.setLocation(x, y);
	                arrMods.add(ti);
            	}
            }
            if (msi.getModifiers().contains(Modifiers.H1_ADDITIONAL_INFO_2) && modifiers.containsKey(Modifiers.H1_ADDITIONAL_INFO_2))
            {
                strText = modifiers.get(Modifiers.H1_ADDITIONAL_INFO_2);
                if(strText != null)
                {
	                ti = new TextInfo(strText, 0, 0, modifierFont, modifierFontName);
	                labelWidth = Math.round(ti.getTextBounds().width());
	
	                x = bounds.left + (int) (bounds.width() * 0.5);
	                x = x - (int) (labelWidth * 0.5);
                    y = bounds.top + labelHeight - descent + (int) (bounds.height() * 0.07);
	
	                ti.setLocation(x, y);
	                arrMods.add(ti);
                }
            }
            if (msi.getModifiers().contains(Modifiers.A_SYMBOL_ICON))
            {
                if(modifiers.containsKey(Modifiers.A_SYMBOL_ICON))
                    strText = modifiers.get(Modifiers.A_SYMBOL_ICON);
                else if(SymbolID.getEntityCode(symbolID)==321706)//NATO Multiple Supply Class Point
                    strText = "ALL?";//make it clear the required 'A' value wasn't set for this symbol.

                if(strText != null)
                {
                    ti = new TextInfo(strText, 0, 0, modifierFont, modifierFontName);
                    labelWidth = Math.round(ti.getTextBounds().width());

                    x = bounds.left + (int) (bounds.width() * 0.5);
                    x = x - (int) (labelWidth * 0.5);
                    y = bounds.top + labelHeight - descent + (int) (bounds.height() * 0.07);

                    ti.setLocation(x, y);
                    arrMods.add(ti);
                }
            }
            if (msi.getModifiers().contains(Modifiers.W_DTG_1) && modifiers.containsKey(Modifiers.W_DTG_1))
            {
                strText = modifiers.get(Modifiers.W_DTG_1);
                if(strText != null)
                {
	                ti = new TextInfo(strText, 0, 0, modifierFont, modifierFontName);
	                labelWidth = Math.round(ti.getTextBounds().width());
	
	                x = bounds.left - labelWidth - bufferXL;
	                y = bounds.top + labelHeight - descent;
	
	                ti.setLocation(x, y);
	                arrMods.add(ti);
                }
            }
            if (msi.getModifiers().contains(Modifiers.W1_DTG_2) && modifiers.containsKey(Modifiers.W1_DTG_2))
            {
                strText = modifiers.get(Modifiers.W1_DTG_2);
                if(strText != null)
                {
	                ti = new TextInfo(strText, 0, 0, modifierFont, modifierFontName);
	                labelWidth = Math.round(ti.getTextBounds().width());
	
	                x = bounds.left - labelWidth - bufferXL;
	
	                y = ((labelHeight - descent + bufferText) * 2);
	                y = bounds.top + y;
	
	                ti.setLocation(x, y);
	                arrMods.add(ti);
                }
            }
            if (msi.getModifiers().contains(Modifiers.T_UNIQUE_DESIGNATION_1) && modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1))
            {
                strText = modifiers.get(Modifiers.T_UNIQUE_DESIGNATION_1);
                if(strText != null)
                {
	                ti = new TextInfo(strText, 0, 0, modifierFont, modifierFontName);
	
	                x = bounds.left + bounds.width() + bufferXR;
	                y = bounds.top + labelHeight - descent;
	
	                ti.setLocation(x, y);
	                arrMods.add(ti);
                }
            }
            if (msi.getModifiers().contains(Modifiers.T1_UNIQUE_DESIGNATION_2) && modifiers.containsKey(Modifiers.T1_UNIQUE_DESIGNATION_2))
            {
                strText = modifiers.get(Modifiers.T1_UNIQUE_DESIGNATION_2);
                if(strText != null)
                {
	                ti = new TextInfo(strText, 0, 0, modifierFont, modifierFontName);
	                labelWidth = Math.round(ti.getTextBounds().width());
	
	                //points
	                x = bounds.left + (int) (bounds.width() * 0.5);
	                x = x - (int) (labelWidth * 0.5);
	                //y = bounds.y + (bounds.height * 0.5);
	
	                y = (int) ((bounds.height() * 0.55));//633333333
	                y = bounds.top + y;
	
	                ti.setLocation(x, y);
	                arrMods.add(ti);
                }
            }
            // <editor-fold defaultstate="collapsed" desc="Build Feint Dummy Indicator">
            if (SymbolUtilities.hasFDI(symbolID))
            {
                //create feint indicator /\
                fdiLeft = new Point((int) symbolBounds.left, (int) symbolBounds.top);
                fdiRight = new Point((int) (symbolBounds.left + symbolBounds.width()), (int) symbolBounds.top);
                fdiTop = new Point(Math.round(RectUtilities.getCenterX(symbolBounds)), Math.round(symbolBounds.top - (symbolBounds.width() * .5f)));


                fdiBounds = RectUtilities.makeRect(fdiLeft.x, fdiLeft.y, 1, 1);
                fdiBounds.union(fdiTop.x, fdiTop.y);
                fdiBounds.union(fdiRight.x, fdiRight.y);

                float fdiStrokeWidth = Math.round(RendererSettings.getInstance().getDeviceDPI() / 96f);
                RectUtilities.grow(fdiBounds,Math.round(fdiStrokeWidth/2));

                ti = new TextInfo("TEST",0,0, modifierFont, modifierFontName);
                if (ti != null)
                {
                    int shiftY = Math.round(symbolBounds.top - ti.getTextBounds().height() - 2);
                    fdiLeft.offset(0, shiftY);
                    fdiTop.offset(0, shiftY);
                    fdiRight.offset(0, shiftY);
                    fdiBounds.offset(0, shiftY);
                }

                imageBounds.union(fdiBounds);

            }
            // </editor-fold>
        }

        // </editor-fold>
        // <editor-fold defaultstate="collapsed" desc="Shift Points and Draw">
        Rect modifierBounds = null;
        if (arrMods != null && arrMods.size() > 0)
        {

            //build modifier bounds/////////////////////////////////////////
            modifierBounds = arrMods.get(0).getTextOutlineBounds();
            int size = arrMods.size();
            TextInfo tempShape = null;
            for (int i = 1; i < size; i++)
            {
                tempShape = arrMods.get(i);
                modifierBounds.union(tempShape.getTextOutlineBounds());
            }

        }

        if(fdiBounds != null)
        {
            if(modifierBounds != null)
                modifierBounds.union(fdiBounds);
            else
                modifierBounds = fdiBounds;
        }

        if (modifierBounds != null)
        {

            imageBounds.union(modifierBounds);

            //shift points if needed////////////////////////////////////////
            if (sdi instanceof ImageInfo && (imageBounds.left < 0 || imageBounds.top < 0))
            {
                int shiftX = Math.abs(imageBounds.left);
                int shiftY = Math.abs(imageBounds.top);

                //shift mobility points
                int size = arrMods.size();
                TextInfo tempShape = null;
                for (int i = 0; i < size; i++)
                {
                    tempShape = arrMods.get(i);
                    tempShape.shift(shiftX, shiftY);
                }
                modifierBounds.offset(shiftX, shiftY);

                //shift image points
                centerPoint.offset(shiftX, shiftY);
                symbolBounds.offset(shiftX, shiftY);
                imageBounds.offset(shiftX, shiftY);

                //If there's an FDI
                if (fdiBounds != null)
                {
                    fdiBounds.offset(shiftX, shiftY);
                    fdiLeft.offset(shiftX, shiftY);
                    fdiTop.offset(shiftX, shiftY);
                    fdiRight.offset(shiftX, shiftY);
                }
            }

            if (attributes.containsKey(MilStdAttributes.TextColor))
            {
                textColor = RendererUtilities.getColorFromHexString(attributes.get(MilStdAttributes.TextColor));
                if(alpha > -1)
                    textColor.setAlpha(alpha);
            }
            if (attributes.containsKey(MilStdAttributes.TextBackgroundColor))
            {
                textBackgroundColor = RendererUtilities.getColorFromHexString(attributes.get(MilStdAttributes.TextBackgroundColor));
                if(alpha > -1)
                    textBackgroundColor.setAlpha(alpha);
            }

            if(sdi instanceof ImageInfo)
            {
                ii = (ImageInfo) sdi;
                //Render modifiers//////////////////////////////////////////////////
                Bitmap bmp = Bitmap.createBitmap(imageBounds.width(), imageBounds.height(), Config.ARGB_8888);
                Canvas ctx = new Canvas(bmp);

                //draw original icon with potential modifiers.
                ctx.drawBitmap(ii.getImage(), symbolBounds.left, symbolBounds.top, null);
                //ctx.drawImage(ii.getImage(),imageBoundsOld.left,imageBoundsOld.top);


                renderText(ctx, arrMods, textColor, textBackgroundColor, modifierFont);

                //<editor-fold defaultstate="collapsed" desc="Draw FDI">
                if (fdiBounds != null) {

                    Paint fdiPaint = new Paint();
                    fdiPaint.setAntiAlias(true);
                    fdiPaint.setColor(lineColor.toInt());/// setARGB(255, 0, 0, 0);
                    if (alpha > -1)
                        fdiPaint.setAlpha(alpha);
                    fdiPaint.setStyle(Style.STROKE);

                    int dpi = RendererSettings.getInstance().getDeviceDPI();
                    int lineLength = dpi / 96 * 6;
                    int lineGap = dpi / 96 * 4;

                    /// ///////////////////////////////////
                    //Divide line in 14 parts. line is 3 parts to 2 parts gap
                    float distance = RendererUtilities.getDistanceBetweenPoints(fdiTop,fdiLeft);
                    //distance = distance / 14f;
                    lineGap = (int)((distance / 14f) * 2);
                    lineLength = (int)((distance / 14f) * 3);
                    /// //////////////////////////////////

                    fdiPaint.setPathEffect(new DashPathEffect(new float[]
                            {
                                    lineLength, lineGap
                            }, 0));

                    float fdiStrokeWidth = Math.round(dpi / 96f);
                    if (fdiStrokeWidth < 2)
                        fdiStrokeWidth = 2;

                    fdiPaint.setStrokeCap(Cap.ROUND);
                    fdiPaint.setStrokeJoin(Join.MITER);
                    fdiPaint.setStrokeWidth(fdiStrokeWidth);

                    Path fdiPath = new Path();

                    fdiPath.moveTo(fdiTop.x, fdiTop.y);
                    fdiPath.lineTo(fdiLeft.x, fdiLeft.y);
                    fdiPath.moveTo(fdiTop.x, fdiTop.y);
                    fdiPath.lineTo(fdiRight.x, fdiRight.y);
                    ctx.drawPath(fdiPath, fdiPaint);

                    fdiBounds = null;

                }
                //</editor-fold>

                newsdi = new ImageInfo(bmp, centerPoint, symbolBounds);
            }
            else if(sdi instanceof SVGSymbolInfo)
            {
                String svgStroke = RendererUtilities.colorToHexString(lineColor,false);
                String svgStrokeWidth = "3";
                String svgAlpha = null;
                if(alpha > -1)
                    svgAlpha = String.valueOf(alpha);
                ssi = (SVGSymbolInfo)sdi;
                StringBuilder sbSVG = new StringBuilder();
                sbSVG.append(ssi.getSVG());
                sbSVG.append(renderTextElements(arrMods,textColor,textBackgroundColor));

                //<editor-fold defaultstate="collapsed" desc="Draw FDI">
                if (fdiBounds != null)
                {
                    /*int dpi = RendererSettings.getInstance().getDeviceDPI();
                    int lineLength = dpi / 96 * 6;
                    int lineGap = dpi / 96 * 4;
                    String svgFDIDashArray = "" + lineLength + " " + lineGap;//*/

                    /// ///////////////////////////////////
                    //Divide line in 14 parts. line is 3 parts to 2 parts gap
                    float distance = RendererUtilities.getDistanceBetweenPoints(fdiTop,fdiLeft);
                    //distance = distance / 14f;
                    int lineGap = (int)((distance / 14f) * 2);
                    int lineLength = (int)((distance / 14f) * 3);
                    String svgFDIDashArray = "" + lineLength + " " + lineGap;
                    /// //////////////////////////////////

                    SVGPath fdiPath = new SVGPath();
                    fdiPath.moveTo(fdiTop.x, fdiTop.y);
                    fdiPath.lineTo(fdiLeft.x, fdiLeft.y);
                    fdiPath.moveTo(fdiTop.x, fdiTop.y);
                    fdiPath.lineTo(fdiRight.x, fdiRight.y);//*/

                    fdiPath.setLineDash(svgFDIDashArray);
                    sbSVG.append(fdiPath.toSVGElement(svgStroke,Float.parseFloat(svgStrokeWidth),null,1f,1f,"round"));
                    //sbSVG.append(Shape2SVG.Convert(fdiPath, svgStroke, null, svgStrokeWidth, svgAlpha, svgAlpha, svgFDIDashArray));
                }
                //</editor-fold>

                newsdi = new SVGSymbolInfo(sbSVG.toString(),centerPoint,symbolBounds,imageBounds);
            }

            // <editor-fold defaultstate="collapsed" desc="Cleanup">
            //ctx = null;

            // </editor-fold>
        }
        // </editor-fold>
        return newsdi;

    }

    private static SymbolDimensionInfo shiftUnitPointsAndDraw(ArrayList<TextInfo> tiArray, SymbolDimensionInfo sdi, Map<String,String> attributes, Paint modifierFont)
    {

        ImageInfo ii = null;
        SVGSymbolInfo ssi = null;
        SymbolDimensionInfo newsdi = null;

        int alpha = -1;


        if (attributes != null && attributes.containsKey(MilStdAttributes.Alpha))
        {
            alpha = Integer.parseInt(attributes.get(MilStdAttributes.Alpha));
        }

        Color textColor = Color.BLACK;
        Color textBackgroundColor = null;

        Rect symbolBounds = new Rect(sdi.getSymbolBounds());
        Point centerPoint = new Point(sdi.getCenterPoint());
        Rect imageBounds = new Rect(sdi.getImageBounds());
        Rect imageBoundsOld = new Rect(sdi.getImageBounds());

        Rect modifierBounds = null;
        if (tiArray != null && tiArray.size() > 0)
        {

            //build modifier bounds/////////////////////////////////////////
            modifierBounds = tiArray.get(0).getTextOutlineBounds();
            int size = tiArray.size();
            TextInfo tempShape = null;
            for (int i = 1; i < size; i++)
            {
                tempShape = tiArray.get(i);
                modifierBounds.union(tempShape.getTextOutlineBounds());
            }

        }

        if (modifierBounds != null)
        {

            imageBounds.union(modifierBounds);

            //shift points if needed////////////////////////////////////////
            if (sdi instanceof ImageInfo && (imageBounds.left < 0 || imageBounds.top < 0))
            {
                int shiftX = Math.round(Math.abs(imageBounds.left)),
                        shiftY = Math.round(Math.abs(imageBounds.top));

                //shift mobility points
                int size = tiArray.size();
                TextInfo tempShape = null;
                for (int i = 0; i < size; i++)
                {
                    tempShape = tiArray.get(i);
                    tempShape.shift(shiftX, shiftY);
                }
                RectUtilities.shift(modifierBounds, shiftX, shiftY);
                //modifierBounds.shift(shiftX,shiftY);

                //shift image points
                centerPoint.offset(shiftX, shiftY);
                RectUtilities.shift(symbolBounds, shiftX, shiftY);
                RectUtilities.shift(imageBounds, shiftX, shiftY);
                RectUtilities.shift(imageBoundsOld, shiftX, shiftY);
                /*centerPoint.shift(shiftX, shiftY);
                 symbolBounds.shift(shiftX, shiftY);
                 imageBounds.shift(shiftX, shiftY);
                 imageBoundsOld.shift(shiftX, shiftY);//*/
            }

            if (attributes != null && attributes.containsKey(MilStdAttributes.TextColor))
            {
                textColor = RendererUtilities.getColorFromHexString(attributes.get(MilStdAttributes.TextColor));
                if(alpha > -1)
                    textColor.setAlpha(alpha);
            }
            if (attributes != null && attributes.containsKey(MilStdAttributes.TextBackgroundColor))
            {
                textBackgroundColor = RendererUtilities.getColorFromHexString(attributes.get(MilStdAttributes.TextBackgroundColor));
                if(alpha > -1)
                    textBackgroundColor.setAlpha(alpha);
            }

            if(sdi instanceof ImageInfo)
            {
                ii = (ImageInfo) sdi;

                Bitmap bmp = Bitmap.createBitmap(imageBounds.width(), imageBounds.height(), Config.ARGB_8888);
                Canvas ctx = new Canvas(bmp);

                //render////////////////////////////////////////////////////////
                //draw original icon with potential modifiers.
                ctx.drawBitmap(ii.getImage(), imageBoundsOld.left, imageBoundsOld.top, null);
                //ctx.drawImage(ii.getImage(),imageBoundsOld.left,imageBoundsOld.top);

                renderText(ctx, tiArray, textColor, textBackgroundColor, modifierFont);

                newsdi = new ImageInfo(bmp, centerPoint, symbolBounds);
            }
            else if(sdi instanceof SVGSymbolInfo)
            {
                ssi = (SVGSymbolInfo)sdi;
                StringBuilder sb = new StringBuilder();
                sb.append(ssi.getSVG());
                sb.append(renderTextElements(tiArray,textColor,textBackgroundColor));
                newsdi = new SVGSymbolInfo(sb.toString(),centerPoint,symbolBounds,imageBounds);
            }
        }

        if(newsdi == null)
            newsdi = sdi;

        return newsdi;
    }
    private static String renderTextElement(ArrayList<TextInfo> tiArray, Color color, Color backgroundColor)
    {
        StringBuilder sbSVG = new StringBuilder();

        String svgStroke = RendererUtilities.colorToHexString(RendererUtilities.getIdealOutlineColor(color),false);
        if(backgroundColor != null)
            svgStroke = RendererUtilities.colorToHexString(backgroundColor,false);

        String svgFill = RendererUtilities.colorToHexString(color,false);
        String svgStrokeWidth = String.valueOf(RendererSettings.getInstance().getTextOutlineWidth());
        for (TextInfo ti : tiArray) {
            sbSVG.append(Shape2SVG.Convert(ti, svgStroke,svgFill,svgStrokeWidth,null,null,null));
            sbSVG.append("\n");
        }

        return sbSVG.toString();
    }

    private static String renderTextElements(ArrayList<TextInfo> tiArray, Color color, Color backgroundColor)
    {
        String style = null;
        String name = tiArray.get(0).getFontName();//"SansSerif";
        if(!name.endsWith("serif"))
            name += ", sans-serif";
        String size = String.valueOf(tiArray.get(0).getFontSize());
        String weight = null;
        String anchor = null;//"start";
        if(tiArray.get(0).getFontStyle() == Typeface.BOLD)
            weight = "bold";
        StringBuilder sbSVG = new StringBuilder();

        String svgStroke = RendererUtilities.colorToHexString(RendererUtilities.getIdealOutlineColor(color),false);
        if(backgroundColor != null)
            svgStroke = RendererUtilities.colorToHexString(backgroundColor,false);

        String svgFill = RendererUtilities.colorToHexString(color,false);
        String svgStrokeWidth = String.valueOf(RendererSettings.getInstance().getTextOutlineWidth());
        sbSVG.append("\n<g");
        sbSVG.append(" font-family=\"").append(name).append('"');
        sbSVG.append(" font-size=\"").append(size).append("px\"");
        if(weight != null)
            sbSVG.append(" font-weight=\"").append(weight).append("\"");
        sbSVG.append(" alignment-baseline=\"alphabetic\"");//
        sbSVG.append(">");

        for (TextInfo ti : tiArray) {
            sbSVG.append(Shape2SVG.ConvertForGroup(ti, svgStroke,svgFill,svgStrokeWidth,null,null,null));
            sbSVG.append("\n");
        }
        sbSVG.append("</g>\n");

        return sbSVG.toString();
    }
    private static void renderText(Canvas ctx, ArrayList<TextInfo> tiArray, Color color, Color backgroundColor, Paint font)
    {
        ModifierRenderer.renderText(ctx, (TextInfo[]) tiArray.toArray(new TextInfo[0]), color, backgroundColor, font);
    }

    /**
     * 
     * @param ctx
     * @param tiArray
     * @param color
     * @param backgroundColor 
     */
    public static void renderText(Canvas ctx, TextInfo[] tiArray, Color color, Color backgroundColor, Paint modifierFont)
    {
        /*for (TextInfo textInfo : tiArray) 
         {
         ctx.drawText(textInfo.getText(), textInfo.getLocation().x, textInfo.getLocation().y, modifierFont);	
         }*/

        int size = tiArray.length;

        int tbm = RS.getTextBackgroundMethod();
        int outlineWidth = RS.getTextOutlineWidth();

        if (color == null)
        {
            color = Color.BLACK;
        }

        Color outlineColor = null;
        
        if(backgroundColor != null)
            outlineColor = backgroundColor;
        else
            outlineColor = RendererUtilities.getIdealOutlineColor(color);

        if (tbm == RendererSettings.TextBackgroundMethod_OUTLINE_QUICK)
        {
            synchronized (modifierFontMutex) {
                //draw text outline
                modifierFont.setStyle(Style.FILL);
                modifierFont.setStrokeWidth(RS.getTextOutlineWidth());
                modifierFont.setColor(outlineColor.toInt());

                if (outlineWidth > 2)
                    outlineWidth = 2;

                if (outlineWidth > 0) {
                    for (int i = 0; i < size; i++) {
                        TextInfo textInfo = tiArray[i];
                        if (outlineWidth > 0) {
                            for (int j = 1; j <= outlineWidth; j++) {
                                if (j == 1) {
                                    ctx.drawText(textInfo.getText(), textInfo.getLocation().x - j, textInfo.getLocation().y - j, modifierFont);
                                    ctx.drawText(textInfo.getText(), textInfo.getLocation().x + j, textInfo.getLocation().y - j, modifierFont);
                                    ctx.drawText(textInfo.getText(), textInfo.getLocation().x - j, textInfo.getLocation().y + j, modifierFont);
                                    ctx.drawText(textInfo.getText(), textInfo.getLocation().x + j, textInfo.getLocation().y + j, modifierFont);

                                    ctx.drawText(textInfo.getText(), textInfo.getLocation().x - j, textInfo.getLocation().y, modifierFont);
                                    ctx.drawText(textInfo.getText(), textInfo.getLocation().x + j, textInfo.getLocation().y, modifierFont);

                                } else {
                                    ctx.drawText(textInfo.getText(), textInfo.getLocation().x - j, textInfo.getLocation().y - j, modifierFont);
                                    ctx.drawText(textInfo.getText(), textInfo.getLocation().x + j, textInfo.getLocation().y - j, modifierFont);
                                    ctx.drawText(textInfo.getText(), textInfo.getLocation().x - j, textInfo.getLocation().y + j, modifierFont);
                                    ctx.drawText(textInfo.getText(), textInfo.getLocation().x + j, textInfo.getLocation().y + j, modifierFont);

                                    ctx.drawText(textInfo.getText(), textInfo.getLocation().x - j, textInfo.getLocation().y, modifierFont);
                                    ctx.drawText(textInfo.getText(), textInfo.getLocation().x + j, textInfo.getLocation().y, modifierFont);

                                    ctx.drawText(textInfo.getText(), textInfo.getLocation().x, textInfo.getLocation().y + j, modifierFont);
                                    ctx.drawText(textInfo.getText(), textInfo.getLocation().x, textInfo.getLocation().y - j, modifierFont);
                                }

                            }

                        }

                    }
                }
                //draw text
                modifierFont.setColor(color.toInt());

                for (int j = 0; j < size; j++) {
                    TextInfo textInfo = tiArray[j];
                    ctx.drawText(textInfo.getText(), textInfo.getLocation().x, textInfo.getLocation().y, modifierFont);
                /*Paint outline = new Paint();
                 outline.setStyle(Style.STROKE);
                 outline.setColor(Color.red.toInt());
                 outline.setAlpha(155);
                 outline.setStrokeWidth(1f);
                 ctx.drawRect(textInfo.getTextBounds(), outline);
                 outline.setColor(Color.blue.toInt());
                 ctx.drawRect(textInfo.getTextOutlineBounds(), outline);//*/
                }
            }
        }
        else if (tbm == RendererSettings.TextBackgroundMethod_OUTLINE)
        {
            synchronized (modifierFontMutex) {
                //draw text outline
                //draw text outline
                modifierFont.setStyle(Style.STROKE);
                modifierFont.setStrokeWidth(RS.getTextOutlineWidth());
                modifierFont.setColor(outlineColor.toInt());
                if (outlineWidth > 0) {
                    for (int i = 0; i < size; i++) {
                        TextInfo textInfo = tiArray[i];
                        ctx.drawText(textInfo.getText(), textInfo.getLocation().x, textInfo.getLocation().y, modifierFont);
                    }
                }
                //draw text
                modifierFont.setColor(color.toInt());
                modifierFont.setStyle(Style.FILL);
                for (int j = 0; j < size; j++) {
                    TextInfo textInfo = tiArray[j];
                    ctx.drawText(textInfo.getText(), textInfo.getLocation().x, textInfo.getLocation().y, modifierFont);
                }
            }
        }
        else if (tbm == RendererSettings.TextBackgroundMethod_COLORFILL)
        {
            synchronized (modifierFontMutex) {
                Paint rectFill = new Paint();
                rectFill.setStyle(Paint.Style.FILL);
                rectFill.setColor(outlineColor.toARGB());


                //draw rectangle
                for (int k = 0; k < size; k++) {
                    TextInfo textInfo = tiArray[k];
                    ctx.drawRect(textInfo.getTextOutlineBounds(), rectFill);
                }
                //draw text
                modifierFont.setColor(color.toInt());
                modifierFont.setStyle(Style.FILL);
                for (int j = 0; j < size; j++) {
                    TextInfo textInfo = tiArray[j];
                    ctx.drawText(textInfo.getText(), textInfo.getLocation().x, textInfo.getLocation().y, modifierFont);
                }
            }
        }
        else if (tbm == RendererSettings.TextBackgroundMethod_NONE)
        {
            synchronized (modifierFontMutex) {
                modifierFont.setColor(color.toInt());
                modifierFont.setStyle(Style.FILL);
                for (int j = 0; j < size; j++) {
                    TextInfo textInfo = tiArray[j];
                    ctx.drawText(textInfo.getText(), textInfo.getLocation().x, textInfo.getLocation().y, modifierFont);
                }
            }
        }
    }

    /**
     *
     * @param symbolID
     * @param modifiers
     * @return int[] where {xposition (-1 left, 0 centered, 1 right), yposition (0 centered, 1+ goes up, 1- goes down),
     * centered (0-no, 1-yes)} -999 means passed modifier is not supported by this symbol
     */
    private static List<Modifier> getLabelPositionIndexes(String symbolID, Map<String,String> modifiers, Map<String,String> attributes)
    {
        List<Modifier> mods = null;
        if(modifiers != null && !modifiers.isEmpty())
            mods = new ArrayList<>();
        else
            return null;

        int ver = SymbolID.getVersion(symbolID);
        int ss = SymbolID.getSymbolSet(symbolID);
        int x = 0;
        int y = 0;
        boolean centered = true;
        int p = RendererSettings.getInstance().getSPModifierPlacement();
        boolean strict = (RendererSettings.getInstance().getSPModifierPlacement() == RendererSettings.ModifierPlacement_STRICT);
        if(attributes != null && attributes.containsKey(MilStdAttributes.ModifierPlacement))
        {
            String mp = attributes.get(MilStdAttributes.ModifierPlacement);
            if(mp.equals("0") || mp.equals("1") || mp.equals("2"))
            {
                p = Integer.parseInt(mp);
                if(p == 0)
                    strict = true;
                else
                    strict = false;
            }
        }
        String temp = null;
        String sep = " ";
        if(ss == SymbolID.SymbolSet_DismountedIndividuals) {
            ver = SymbolID.Version_2525E;
        }

        if(ver < SymbolID.Version_2525E)
        {
            if(ss == SymbolID.SymbolSet_LandUnit ||
                    ss == SymbolID.SymbolSet_LandCivilianUnit_Organization)
            {
                //Do top center label
                x = 0;//centered
                y = 9;//on top of symbol
                if(modifiers.containsKey(Modifiers.B_ECHELON))
                {
                    temp = modifiers.get(Modifiers.B_ECHELON);
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("B", temp, x, y, centered));
                }

                //Do right side labels
                x = 1;//on right
                if(modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1))
                {
                    y = 0;//center
                    centered = true;//vertically centered, only matters for labels on left and right side
                    temp = modifiers.get(Modifiers.H_ADDITIONAL_INFO_1);
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("H", temp, x, y, centered));
                }
                else if(!strict)
                {
                    //if no "H', bring G and M closer to the center
                    centered = false;
                }

                if(modifiers.containsKey(Modifiers.G_STAFF_COMMENTS))
                {
                    y = 1;//above center
                    temp = modifiers.get(Modifiers.G_STAFF_COMMENTS);
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("G", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.F_REINFORCED_REDUCED) || modifiers.containsKey(Modifiers.AS_COUNTRY))
                {
                    y = 2;
                    if(!strict && !modifiers.containsKey(Modifiers.G_STAFF_COMMENTS))
                        y--;
                    temp = "";
                    if(modifiers.containsKey(Modifiers.F_REINFORCED_REDUCED))
                        temp = modifiers.get(Modifiers.F_REINFORCED_REDUCED) + sep;
                    if(modifiers.containsKey(Modifiers.AS_COUNTRY))
                        temp += modifiers.get(Modifiers.AS_COUNTRY);
                    temp = temp.trim();
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("F AS", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.M_HIGHER_FORMATION))
                {
                    y = -1;//below center
                    temp = modifiers.get(Modifiers.M_HIGHER_FORMATION);
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("M", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.J_EVALUATION_RATING) ||
                        modifiers.containsKey(Modifiers.K_COMBAT_EFFECTIVENESS) ||
                        modifiers.containsKey(Modifiers.P_IFF_SIF_AIS))
                {
                    y = -2;
                    if(!strict && !modifiers.containsKey(Modifiers.M_HIGHER_FORMATION))
                        y++;
                    temp = "";
                    if(modifiers.containsKey(Modifiers.J_EVALUATION_RATING))
                        temp = modifiers.get(Modifiers.J_EVALUATION_RATING) + sep;
                    if(modifiers.containsKey(Modifiers.K_COMBAT_EFFECTIVENESS))
                        temp += modifiers.get(Modifiers.K_COMBAT_EFFECTIVENESS) + sep;
                    if(modifiers.containsKey(Modifiers.P_IFF_SIF_AIS))
                        temp += modifiers.get(Modifiers.P_IFF_SIF_AIS);
                    temp = temp.trim();
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("J K P", temp, x, y, centered));
                }

                //Do left side labels
                x = -1;//on left
                centered = false;

                if(modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH) || modifiers.containsKey(Modifiers.Y_LOCATION))
                {
                    y = 1;
                    temp = "";
                    if(modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH))
                        temp = modifiers.get(Modifiers.X_ALTITUDE_DEPTH) + sep;
                    if(modifiers.containsKey(Modifiers.Y_LOCATION))
                        temp += modifiers.get(Modifiers.Y_LOCATION);

                    temp = temp.trim();
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("X Y", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.W_DTG_1))
                {
                    y = 2;//above center
                    if(!strict && !(modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH) || modifiers.containsKey(Modifiers.Y_LOCATION)))
                        y--;
                    temp = modifiers.get(Modifiers.W_DTG_1);
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("W", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1))
                {
                    y = -1;//below center
                    temp = modifiers.get(Modifiers.T_UNIQUE_DESIGNATION_1);
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("T", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.Z_SPEED))
                {
                    y = -2;
                    if(!strict && !modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1))
                        y++;
                    temp = modifiers.get(Modifiers.Z_SPEED);
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier(Modifiers.J_EVALUATION_RATING, temp, x, y, centered));
                }
            }
            else if(ss == SymbolID.SymbolSet_LandEquipment ||
                    ss == SymbolID.SymbolSet_SignalsIntelligence_Land)
            {
                //Do top center label
                x = 0;//centered
                y = 9;//on top of symbol
                if(modifiers.containsKey(Modifiers.C_QUANTITY))
                {
                    temp = modifiers.get(Modifiers.C_QUANTITY);
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("C", temp, x, y, centered));
                }

                //Do right side labels
                x = 1;//on right
                if(modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1) || modifiers.containsKey(Modifiers.AF_COMMON_IDENTIFIER))
                {
                    y = 0;//center
                    centered = true;//vertically centered, only matters for labels on left and right side
                    temp = "";
                    if(modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1))
                        temp = modifiers.get(Modifiers.H_ADDITIONAL_INFO_1) + sep;
                    if(modifiers.containsKey(Modifiers.AF_COMMON_IDENTIFIER))
                        temp += modifiers.get(Modifiers.AF_COMMON_IDENTIFIER);
                    temp = temp.trim();
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("H AF", temp, x, y, centered));
                }
                else if(!strict)
                {
                    //if no "H', bring G and M closer to the center
                    centered = false;
                }

                if(modifiers.containsKey(Modifiers.G_STAFF_COMMENTS) || modifiers.containsKey(Modifiers.AQ_GUARDED_UNIT))
                {
                    y = 1;//above center
                    temp = "";
                    if(modifiers.containsKey(Modifiers.G_STAFF_COMMENTS))
                        temp = modifiers.get(Modifiers.G_STAFF_COMMENTS) + sep;
                    if(modifiers.containsKey(Modifiers.AQ_GUARDED_UNIT))
                        temp += modifiers.get(Modifiers.AQ_GUARDED_UNIT);
                    temp = temp.trim();
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("G AQ", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.AS_COUNTRY))
                {
                    y = 2;
                    if(!strict && !(modifiers.containsKey(Modifiers.G_STAFF_COMMENTS) || modifiers.containsKey(Modifiers.AQ_GUARDED_UNIT)))
                        y--;

                    temp = modifiers.get(Modifiers.AS_COUNTRY);
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("AS", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.J_EVALUATION_RATING) ||
                        modifiers.containsKey(Modifiers.L_SIGNATURE_EQUIP) ||
                        modifiers.containsKey(Modifiers.N_HOSTILE) ||
                        modifiers.containsKey(Modifiers.P_IFF_SIF_AIS))
                {
                    y = -1;

                    temp = "";
                    if(modifiers.containsKey(Modifiers.J_EVALUATION_RATING))
                        temp = modifiers.get(Modifiers.J_EVALUATION_RATING) + sep;
                    if(modifiers.containsKey(Modifiers.L_SIGNATURE_EQUIP))
                        temp += modifiers.get(Modifiers.L_SIGNATURE_EQUIP) + sep;
                    if(modifiers.containsKey(Modifiers.N_HOSTILE))
                        temp += modifiers.get(Modifiers.N_HOSTILE) + sep;
                    if(modifiers.containsKey(Modifiers.P_IFF_SIF_AIS))
                        temp += modifiers.get(Modifiers.P_IFF_SIF_AIS);
                    temp = temp.trim();
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("J L N P", temp, x, y, centered));
                }

                //Do left side labels
                x = -1;//on left

                if(modifiers.containsKey(Modifiers.V_EQUIP_TYPE) ||
                        modifiers.containsKey(Modifiers.AD_PLATFORM_TYPE) ||
                        modifiers.containsKey(Modifiers.AE_EQUIPMENT_TEARDOWN_TIME))
                {
                    y = 0;//center
                    centered = true;//vertically centered, only matters for labels on left and right side

                    temp = "";
                    if(modifiers.containsKey(Modifiers.V_EQUIP_TYPE))
                        temp = modifiers.get(Modifiers.V_EQUIP_TYPE) + sep;
                    if(modifiers.containsKey(Modifiers.AD_PLATFORM_TYPE))
                        temp += modifiers.get(Modifiers.AD_PLATFORM_TYPE) + sep;
                    if(modifiers.containsKey(Modifiers.AE_EQUIPMENT_TEARDOWN_TIME))
                        temp += modifiers.get(Modifiers.AE_EQUIPMENT_TEARDOWN_TIME);
                    temp = temp.trim();
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("V AD AE", temp, x, y, centered));
                }
                else if(!strict)
                {
                    centered = false;
                }

                if(modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH) || modifiers.containsKey(Modifiers.Y_LOCATION))
                {
                    y = 1;
                    temp = "";
                    if(modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH))
                        temp = modifiers.get(Modifiers.X_ALTITUDE_DEPTH) + sep;
                    if(modifiers.containsKey(Modifiers.Y_LOCATION))
                        temp += modifiers.get(Modifiers.Y_LOCATION);

                    temp = temp.trim();
                    mods.add(new Modifier("X Y", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.W_DTG_1) || modifiers.containsKey(Modifiers.AR_SPECIAL_DESIGNATOR))
                {
                    y = 2;//above center
                    if(!strict && !(modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH) || modifiers.containsKey(Modifiers.Y_LOCATION)))
                        y--;

                    temp = modifiers.get(Modifiers.W_DTG_1);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("W", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1))
                {
                    y = -1;//below center
                    temp = modifiers.get(Modifiers.T_UNIQUE_DESIGNATION_1);
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("T", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.Z_SPEED))
                {
                    y = -2;
                    if(!strict && !modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1))
                        y++;
                    temp = modifiers.get(Modifiers.Z_SPEED);
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("Z", temp, x, y, centered));
                }
            }
            else if(ss == SymbolID.SymbolSet_LandInstallation)
            {
                //No top center label

                //Do right side labels
                x = 1;//on right

                if(modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1))
                {
                    y = 0;//center
                    centered = true;//vertically centered, only matters for labels on left and right side
                    temp = modifiers.get(Modifiers.H_ADDITIONAL_INFO_1);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("H", temp, x, y, centered));
                }
                else if(!strict)
                {
                    centered = false;
                }

                if(modifiers.containsKey(Modifiers.G_STAFF_COMMENTS))
                {
                    y = 1;//above center
                    temp = modifiers.get(Modifiers.G_STAFF_COMMENTS);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("G", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.AS_COUNTRY))
                {
                    y = 2;
                    if(!strict && !modifiers.containsKey(Modifiers.G_STAFF_COMMENTS))
                        y--;

                    temp = modifiers.get(Modifiers.AS_COUNTRY);
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("AS", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.J_EVALUATION_RATING) ||
                        modifiers.containsKey(Modifiers.K_COMBAT_EFFECTIVENESS) ||
                        modifiers.containsKey(Modifiers.P_IFF_SIF_AIS))
                {
                    y = -1;

                    temp = "";
                    if(modifiers.containsKey(Modifiers.J_EVALUATION_RATING))
                        temp = modifiers.get(Modifiers.J_EVALUATION_RATING) + sep;
                    if(modifiers.containsKey(Modifiers.K_COMBAT_EFFECTIVENESS))
                        temp += modifiers.get(Modifiers.K_COMBAT_EFFECTIVENESS) + sep;
                    if(modifiers.containsKey(Modifiers.P_IFF_SIF_AIS))
                        temp += modifiers.get(Modifiers.P_IFF_SIF_AIS);
                    temp = temp.trim();
                    mods.add(new Modifier("J K P", temp, x, y, centered));
                }

                //Do left side labels
                x = -1;//on left

                if(modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH) || modifiers.containsKey(Modifiers.Y_LOCATION))
                {
                    y = 0;//center
                    centered = true;//vertically centered, only matters for labels on left and right side

                    temp = "";
                    if(modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH))
                        temp = modifiers.get(Modifiers.X_ALTITUDE_DEPTH) + sep;
                    if(modifiers.containsKey(Modifiers.Y_LOCATION))
                        temp += modifiers.get(Modifiers.Y_LOCATION);
                    temp = temp.trim();
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("X Y", temp, x, y, centered));
                }
                else if(!strict)
                {
                    centered = false;
                }

                if(modifiers.containsKey(Modifiers.W_DTG_1))
                {
                    y = 1;//above center

                    temp = modifiers.get(Modifiers.W_DTG_1);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("W AR", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1))
                {
                    y = -1;//below center
                    temp = modifiers.get(Modifiers.T_UNIQUE_DESIGNATION_1);
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("T", temp, x, y, centered));
                }
            }
            else if(ss == SymbolID.SymbolSet_Air ||
                    ss == SymbolID.SymbolSet_AirMissile ||
                    ss == SymbolID.SymbolSet_SignalsIntelligence_Air)
            {
                //No top center label


                //Do right side labels
                x = 1;//on right
                centered = false;

                if(modifiers.containsKey(Modifiers.P_IFF_SIF_AIS))
                {
                    y = 1;//above center
                    temp = modifiers.get(Modifiers.P_IFF_SIF_AIS);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("P", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1))
                {
                    y = 2;
                    if(!strict && !modifiers.containsKey(Modifiers.P_IFF_SIF_AIS))
                        y--;
                    temp = modifiers.get(Modifiers.T_UNIQUE_DESIGNATION_1);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("T", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.AS_COUNTRY))
                {
                    y = 3;
                    if(!strict)
                    {
                        if(!modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1))
                            y--;
                        if(!modifiers.containsKey(Modifiers.P_IFF_SIF_AIS))
                            y--;
                    }

                    temp = modifiers.get(Modifiers.AS_COUNTRY );

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("AS", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.V_EQUIP_TYPE))
                {
                    y = -1;//below center
                    temp = modifiers.get(Modifiers.V_EQUIP_TYPE);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("V", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.Z_SPEED)  ||
                        modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH))
                {
                    y = -2;//below center
                    if(!modifiers.containsKey(Modifiers.V_EQUIP_TYPE))
                        y++;

                    temp = "";
                    if(modifiers.containsKey(Modifiers.Z_SPEED))
                        temp = modifiers.get(Modifiers.Z_SPEED) + sep;
                    if(modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH))
                        temp += modifiers.get(Modifiers.X_ALTITUDE_DEPTH);
                    temp = temp.trim();

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("Z X", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.G_STAFF_COMMENTS) ||
                        modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1))
                {
                    y = -3;
                    if(!strict)
                    {
                        if(!(modifiers.containsKey(Modifiers.Z_SPEED)  ||
                                modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH)))
                            y++;
                        if(!modifiers.containsKey(Modifiers.V_EQUIP_TYPE))
                            y++;
                    }
                    temp = "";
                    if(modifiers.containsKey(Modifiers.G_STAFF_COMMENTS))
                        temp = modifiers.get(Modifiers.G_STAFF_COMMENTS) + sep;
                    if(modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1))
                        temp += modifiers.get(Modifiers.H_ADDITIONAL_INFO_1);
                    temp = temp.trim();
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("G H", temp, x, y, centered));
                }

                //No left side labels

            }
            else if(ss == SymbolID.SymbolSet_Space ||
                    ss == SymbolID.SymbolSet_SpaceMissile ||
                    ss == SymbolID.SymbolSet_SignalsIntelligence_Space)
            {
                //No top center label


                //Do right side labels
                x = 1;//on right
                centered = false;

                if(modifiers.containsKey(Modifiers.V_EQUIP_TYPE))
                {
                    y = 1;//above center
                    temp = modifiers.get(Modifiers.V_EQUIP_TYPE);
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("V", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1))
                {
                    y = 2;
                    if(!strict && !modifiers.containsKey(Modifiers.V_EQUIP_TYPE))
                        y--;
                    temp = modifiers.get(Modifiers.T_UNIQUE_DESIGNATION_1);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("T", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.AS_COUNTRY))
                {
                    y = 3;
                    if(!strict)
                    {
                        if(!modifiers.containsKey(Modifiers.V_EQUIP_TYPE))
                            y--;
                        if(!modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1))
                            y--;
                    }

                    temp = modifiers.get(Modifiers.AS_COUNTRY );

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("AS", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.Z_SPEED)  ||
                        modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH))
                {
                    y = -1;//below center
                    temp = "";
                    if(modifiers.containsKey(Modifiers.Z_SPEED))
                        temp = modifiers.get(Modifiers.Z_SPEED) + sep;
                    if(modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH))
                        temp += modifiers.get(Modifiers.X_ALTITUDE_DEPTH);
                    temp = temp.trim();

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("Z X", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.G_STAFF_COMMENTS) ||
                        modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1))
                {
                    y = -2;
                    if(!strict &&
                            !(modifiers.containsKey(Modifiers.Z_SPEED) || modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH)))
                        y++;
                    temp = "";
                    if(modifiers.containsKey(Modifiers.G_STAFF_COMMENTS))
                        temp = modifiers.get(Modifiers.G_STAFF_COMMENTS) + sep;
                    if(modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1))
                        temp += modifiers.get(Modifiers.H_ADDITIONAL_INFO_1);
                    temp = temp.trim();
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("G H", temp, x, y, centered));
                }

                //No left side labels
            }
            else if(ss == SymbolID.SymbolSet_SeaSurface ||
                    ss == SymbolID.SymbolSet_SignalsIntelligence_SeaSurface)
            {
                //No top center label


                //Do right side labels
                x = 1;//on right
                centered = true;
                if(modifiers.containsKey(Modifiers.V_EQUIP_TYPE))
                {
                    y = 0;//center
                    centered = true;//vertically centered, only matters for labels on left and right side
                    temp = modifiers.get(Modifiers.V_EQUIP_TYPE);
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("V", temp, x, y, centered));
                }
                else if(!strict)
                {
                    //if no "H', bring G and M closer to the center
                    centered = false;
                }

                if(modifiers.containsKey(Modifiers.P_IFF_SIF_AIS))
                {
                    y = 1;//above center
                    temp = modifiers.get(Modifiers.P_IFF_SIF_AIS);
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("P", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1))
                {
                    y = 2;
                    if(!strict && !modifiers.containsKey(Modifiers.P_IFF_SIF_AIS))
                        y--;
                    temp = modifiers.get(Modifiers.T_UNIQUE_DESIGNATION_1);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("T", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.AS_COUNTRY))
                {
                    y = 3;
                    if(!strict)
                    {
                        if(!modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1))
                            y--;
                        if(!modifiers.containsKey(Modifiers.V_EQUIP_TYPE))
                            y--;
                    }

                    temp = modifiers.get(Modifiers.AS_COUNTRY );

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("AS", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.Z_SPEED)  ||
                        modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH))
                {
                    y = -1;//below center
                    temp = "";
                    if(modifiers.containsKey(Modifiers.Z_SPEED))
                        temp = modifiers.get(Modifiers.Z_SPEED) + sep;
                    if(modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH))
                        temp += modifiers.get(Modifiers.X_ALTITUDE_DEPTH);
                    temp = temp.trim();

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("Z X", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.G_STAFF_COMMENTS) ||
                        modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1))
                {
                    y = -2;
                    if(!strict &&
                            !(modifiers.containsKey(Modifiers.Z_SPEED) || modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH)))
                        y++;
                    temp = "";
                    if(modifiers.containsKey(Modifiers.G_STAFF_COMMENTS))
                        temp = modifiers.get(Modifiers.G_STAFF_COMMENTS) + sep;
                    if(modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1))
                        temp += modifiers.get(Modifiers.H_ADDITIONAL_INFO_1);
                    temp = temp.trim();
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("G H", temp, x, y, centered));
                }

                //Do left side labels
                x = -1;
                centered = false;
                if(modifiers.containsKey(Modifiers.AQ_GUARDED_UNIT) || modifiers.containsKey(Modifiers.AR_SPECIAL_DESIGNATOR))
                {
                    y = 3;//above center
                    if(!strict)
                        y--;

                    temp = "";
                    if(modifiers.containsKey(Modifiers.AQ_GUARDED_UNIT))
                        temp = modifiers.get(Modifiers.AQ_GUARDED_UNIT) + sep;
                    if(modifiers.containsKey(Modifiers.AR_SPECIAL_DESIGNATOR))
                        temp += modifiers.get(Modifiers.AR_SPECIAL_DESIGNATOR);
                    temp = temp.trim();

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("AQ AR", temp, x, y, centered));
                }
            }
            else if(ss == SymbolID.SymbolSet_SeaSubsurface ||
                    ss == SymbolID.SymbolSet_SignalsIntelligence_SeaSubsurface)
            {
                //No top center label


                //Do right side labels
                x = 1;//on right
                centered = false;
                if(modifiers.containsKey(Modifiers.V_EQUIP_TYPE))
                {
                    y = 1;//center
                    temp = modifiers.get(Modifiers.V_EQUIP_TYPE);
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("V", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1))
                {
                    y = 2;
                    if(!strict && !modifiers.containsKey(Modifiers.V_EQUIP_TYPE))
                        y--;
                    temp = modifiers.get(Modifiers.T_UNIQUE_DESIGNATION_1);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("T", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.AS_COUNTRY))
                {
                    y = 3;
                    if(!strict)
                    {
                        if(!modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1))
                            y--;
                        if(!modifiers.containsKey(Modifiers.V_EQUIP_TYPE))
                            y--;
                    }

                    temp = modifiers.get(Modifiers.AS_COUNTRY );

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("AS", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH))
                {
                    y = -1;//below center

                    temp = modifiers.get(Modifiers.X_ALTITUDE_DEPTH);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("X", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.G_STAFF_COMMENTS))
                {
                    y = -2;
                    if(!strict && !(modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH)))
                        y++;
                    temp = modifiers.get(Modifiers.G_STAFF_COMMENTS);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("G", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1))
                {
                    y = -3;//below center
                    if(!strict)
                    {
                        if(!modifiers.containsKey(Modifiers.G_STAFF_COMMENTS))
                            y++;
                        if(!modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH))
                            y++;
                    }

                    temp = modifiers.get(Modifiers.H_ADDITIONAL_INFO_1);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("H", temp, x, y, centered));
                }

                //Do left side labels
                x = -1;
                centered = false;
                if(modifiers.containsKey(Modifiers.AR_SPECIAL_DESIGNATOR))
                {
                    y = 3;//above center
                    if(!strict)
                    {
                        y--;
                    }

                    temp = modifiers.get(Modifiers.AR_SPECIAL_DESIGNATOR);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("AR", temp, x, y, centered));
                }

            }
            else if(ss == SymbolID.SymbolSet_Activities)
            {
                //No top center label

                //Do right side labels
                x = 1;//on right
                centered = false;

                if(modifiers.containsKey(Modifiers.G_STAFF_COMMENTS))
                {
                    y = 1;

                    temp = modifiers.get(Modifiers.G_STAFF_COMMENTS);
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("G", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.AS_COUNTRY))
                {
                    y = 2;
                    if(!strict && !modifiers.containsKey(Modifiers.G_STAFF_COMMENTS))
                        y--;
                    temp = modifiers.get(Modifiers.AS_COUNTRY);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("AS", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1))
                {
                    y = -1;//below center
                    temp = modifiers.get(Modifiers.H_ADDITIONAL_INFO_1);
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("H", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.J_EVALUATION_RATING))
                {
                    y = -2;
                    if(!strict && !modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1))
                        y++;
                    temp = temp = modifiers.get(Modifiers.J_EVALUATION_RATING);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("J", temp, x, y, centered));
                }

                //Do left side labels
                x = -1;//on left
                centered = false;

                if(modifiers.containsKey(Modifiers.Y_LOCATION))
                {
                    y = 1;
                    temp = modifiers.get(Modifiers.Y_LOCATION);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("Y", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.W_DTG_1))
                {
                    y = 2;//above center
                    if(!strict && !modifiers.containsKey(Modifiers.Y_LOCATION))
                        y--;
                    temp = modifiers.get(Modifiers.W_DTG_1);
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("W", temp, x, y, centered));
                }

            }
            else if(ss == SymbolID.SymbolSet_CyberSpace)
            {
                //Do top center label
                x = 0;//centered
                y = 9;//on top of symbol
                if(modifiers.containsKey(Modifiers.B_ECHELON))
                {
                    temp = modifiers.get(Modifiers.B_ECHELON);
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("B", temp, x, y, centered));
                }

                //Do right side labels
                x = 1;//on right
                if(modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1))
                {
                    y = 0;//center
                    centered = true;//vertically centered, only matters for labels on left and right side
                    temp = modifiers.get(Modifiers.H_ADDITIONAL_INFO_1);
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("H", temp, x, y, centered));
                }
                else if(!strict)
                {
                    //if no "H', bring G and M closer to the center
                    centered = false;
                }

                if(modifiers.containsKey(Modifiers.G_STAFF_COMMENTS))
                {
                    y = 1;//above center
                    temp = modifiers.get(Modifiers.G_STAFF_COMMENTS);
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("G", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.F_REINFORCED_REDUCED) || modifiers.containsKey(Modifiers.AS_COUNTRY))
                {
                    y = 2;
                    if(!strict && !modifiers.containsKey(Modifiers.G_STAFF_COMMENTS))
                        y--;
                    temp = "";
                    if(modifiers.containsKey(Modifiers.F_REINFORCED_REDUCED))
                        temp = modifiers.get(Modifiers.F_REINFORCED_REDUCED) + sep;
                    if(modifiers.containsKey(Modifiers.AS_COUNTRY))
                        temp += modifiers.get(Modifiers.AS_COUNTRY);
                    temp = temp.trim();
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("F AS", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.M_HIGHER_FORMATION))
                {
                    y = -1;//below center
                    temp = modifiers.get(Modifiers.M_HIGHER_FORMATION);
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("M", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.K_COMBAT_EFFECTIVENESS) ||
                        modifiers.containsKey(Modifiers.L_SIGNATURE_EQUIP))
                {
                    y = -2;
                    if(!strict && !modifiers.containsKey(Modifiers.M_HIGHER_FORMATION))
                        y++;
                    temp = "";
                    if(modifiers.containsKey(Modifiers.K_COMBAT_EFFECTIVENESS))
                        temp = modifiers.get(Modifiers.K_COMBAT_EFFECTIVENESS) + sep;
                    if(modifiers.containsKey(Modifiers.L_SIGNATURE_EQUIP))
                        temp += modifiers.get(Modifiers.L_SIGNATURE_EQUIP);

                    temp = temp.trim();
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("K L", temp, x, y, centered));
                }

                //Do left side labels
                x = -1;//on left
                centered = true;

                if(modifiers.containsKey(Modifiers.Y_LOCATION))
                {
                    y = 0;
                    temp = modifiers.get(Modifiers.Y_LOCATION);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("Y", temp, x, y, centered));
                }
                else if (!strict)
                {
                    centered = false;
                }

                if(modifiers.containsKey(Modifiers.W_DTG_1))
                {
                    y = 1;//above center
                    temp = modifiers.get(Modifiers.W_DTG_1);
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("W", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1) || modifiers.containsKey(Modifiers.V_EQUIP_TYPE))
                {
                    y = -1;//below center
                    temp = "";
                    if(modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1))
                        temp = modifiers.get(Modifiers.T_UNIQUE_DESIGNATION_1) + sep;
                    if(modifiers.containsKey(Modifiers.V_EQUIP_TYPE))
                        temp += modifiers.get(Modifiers.V_EQUIP_TYPE);

                    temp = temp.trim();
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("T V", temp, x, y, centered));
                }

            }
            /*else if(ver == SymbolID.SymbolSet_MineWarfare)
            {
                //no modifiers
            }//*/
            //else//SymbolSet Unknown
            //processUnknownTextModifiers
        }
        else// if(ver >= SymbolID.Version_2525E)
        {
            int fs = SymbolID.getFrameShape(symbolID);
            if(ss == SymbolID.SymbolSet_LandUnit ||
                    ss == SymbolID.SymbolSet_LandCivilianUnit_Organization ||
                    (ss == SymbolID.SymbolSet_SignalsIntelligence && fs == SymbolID.FrameShape_LandUnit))
            {
                //Do top center label
                x = 0;//centered
                y = 9;//on top of symbol
                if(modifiers.containsKey(Modifiers.B_ECHELON))
                {
                    temp = modifiers.get(Modifiers.B_ECHELON);
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("B", temp, x, y, centered));
                }

                //Do right side labels
                x = 1;//on right
                if(modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1) ||
                        modifiers.containsKey(Modifiers.AF_COMMON_IDENTIFIER))
                {
                    y = 0;//center
                    centered = true;//vertically centered, only matters for labels on left and right side
                    temp = "";
                    if(modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1))
                        temp = modifiers.get(Modifiers.H_ADDITIONAL_INFO_1) + sep;
                    if(modifiers.containsKey(Modifiers.AF_COMMON_IDENTIFIER))
                        temp += modifiers.get(Modifiers.AF_COMMON_IDENTIFIER);
                    temp = temp.trim();
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("H AF", temp, x, y, centered));
                }
                else if(!strict)
                {
                    //if no "H', bring G and M closer to the center
                    centered = false;
                }

                if(modifiers.containsKey(Modifiers.G_STAFF_COMMENTS) || modifiers.containsKey(Modifiers.AQ_GUARDED_UNIT))
                {
                    y = 1;//above center
                    temp = "";
                    if(modifiers.containsKey(Modifiers.G_STAFF_COMMENTS))
                        temp = modifiers.get(Modifiers.G_STAFF_COMMENTS) + sep;
                    if(modifiers.containsKey(Modifiers.AQ_GUARDED_UNIT))
                        temp += modifiers.get(Modifiers.AQ_GUARDED_UNIT);
                    temp = temp.trim();
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("G AQ", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.F_REINFORCED_REDUCED) || modifiers.containsKey(Modifiers.AS_COUNTRY))
                {
                    y = 2;
                    if(!strict && !(modifiers.containsKey(Modifiers.G_STAFF_COMMENTS) || modifiers.containsKey(Modifiers.AQ_GUARDED_UNIT)))
                        y--;
                    temp = "";
                    if(modifiers.containsKey(Modifiers.F_REINFORCED_REDUCED))
                        temp = modifiers.get(Modifiers.F_REINFORCED_REDUCED) + sep;
                    if(modifiers.containsKey(Modifiers.AS_COUNTRY))
                        temp += modifiers.get(Modifiers.AS_COUNTRY);
                    temp = temp.trim();
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("F AS", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.M_HIGHER_FORMATION))
                {
                    y = -1;//below center
                    temp = modifiers.get(Modifiers.M_HIGHER_FORMATION);
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("M", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.J_EVALUATION_RATING) ||
                        modifiers.containsKey(Modifiers.K_COMBAT_EFFECTIVENESS) ||
                        modifiers.containsKey(Modifiers.L_SIGNATURE_EQUIP) ||
                        modifiers.containsKey(Modifiers.P_IFF_SIF_AIS))
                {
                    y = -2;
                    if(!strict && !modifiers.containsKey(Modifiers.M_HIGHER_FORMATION))
                        y++;
                    temp = "";
                    if(modifiers.containsKey(Modifiers.J_EVALUATION_RATING))
                        temp = modifiers.get(Modifiers.J_EVALUATION_RATING) + sep;
                    if(modifiers.containsKey(Modifiers.K_COMBAT_EFFECTIVENESS))
                        temp += modifiers.get(Modifiers.K_COMBAT_EFFECTIVENESS) + sep;
                    if(modifiers.containsKey(Modifiers.L_SIGNATURE_EQUIP))
                        temp += modifiers.get(Modifiers.L_SIGNATURE_EQUIP) + sep;
                    if(modifiers.containsKey(Modifiers.P_IFF_SIF_AIS))
                        temp += modifiers.get(Modifiers.P_IFF_SIF_AIS);
                    temp = temp.trim();
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("J K L P", temp, x, y, centered));
                }

                //Do left side labels
                x = -1;//on left

                if(modifiers.containsKey(Modifiers.V_EQUIP_TYPE) ||
                        modifiers.containsKey(Modifiers.AD_PLATFORM_TYPE) ||
                        modifiers.containsKey(Modifiers.AE_EQUIPMENT_TEARDOWN_TIME))
                {
                    y = 0;//center
                    centered = true;//vertically centered, only matters for labels on left and right side

                    temp = "";
                    if(modifiers.containsKey(Modifiers.V_EQUIP_TYPE))
                        temp = modifiers.get(Modifiers.V_EQUIP_TYPE) + sep;
                    if(modifiers.containsKey(Modifiers.AD_PLATFORM_TYPE))
                        temp += modifiers.get(Modifiers.AD_PLATFORM_TYPE) + sep;
                    if(modifiers.containsKey(Modifiers.AE_EQUIPMENT_TEARDOWN_TIME))
                        temp += modifiers.get(Modifiers.AE_EQUIPMENT_TEARDOWN_TIME);
                    temp = temp.trim();
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("V AD AE", temp, x, y, centered));
                }
                else if(!strict)
                {
                    centered = false;
                }

                if(modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH) || modifiers.containsKey(Modifiers.Y_LOCATION))
                {
                    y = 1;
                    temp = "";
                    if(modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH))
                        temp = modifiers.get(Modifiers.X_ALTITUDE_DEPTH) + sep;
                    if(modifiers.containsKey(Modifiers.Y_LOCATION))
                        temp += modifiers.get(Modifiers.Y_LOCATION);

                    temp = temp.trim();
                    mods.add(new Modifier("X Y", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.W_DTG_1))
                {
                    y = 2;//above center
                    if(!strict && !(modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH) || modifiers.containsKey(Modifiers.Y_LOCATION)))
                        y--;

                    temp = modifiers.get(Modifiers.W_DTG_1);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("W", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.C_QUANTITY) || modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1))
                {
                    y = -1;//below center
                    temp = "";
                    if(modifiers.containsKey(Modifiers.C_QUANTITY))
                        temp = modifiers.get(Modifiers.C_QUANTITY) + sep;
                    if(modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1))
                        temp += modifiers.get(Modifiers.T_UNIQUE_DESIGNATION_1);

                    temp = temp.trim();
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("C T", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.Z_SPEED))
                {
                    y = -2;
                    if(!strict && !(modifiers.containsKey(Modifiers.C_QUANTITY) || modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1)))
                        y++;
                    temp = modifiers.get(Modifiers.Z_SPEED);
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("Z", temp, x, y, centered));
                }
            }
            else if(ss == SymbolID.SymbolSet_LandEquipment ||
                    (ss == SymbolID.SymbolSet_SignalsIntelligence && fs == SymbolID.FrameShape_LandEquipment))
            {
                //Do top center label
                x = 0;//centered
                y = 9;//on top of symbol
                if(modifiers.containsKey(Modifiers.C_QUANTITY))
                {
                    temp = modifiers.get(Modifiers.C_QUANTITY);
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("C", temp, x, y, centered));
                }

                //Do right side labels
                x = 1;//on right
                centered = false;

                if(modifiers.containsKey(Modifiers.G_STAFF_COMMENTS) || modifiers.containsKey(Modifiers.AQ_GUARDED_UNIT))
                {
                    y = 1;//above center
                    temp = "";
                    if(modifiers.containsKey(Modifiers.G_STAFF_COMMENTS))
                        temp = modifiers.get(Modifiers.G_STAFF_COMMENTS) + sep;
                    if(modifiers.containsKey(Modifiers.AQ_GUARDED_UNIT))
                        temp += modifiers.get(Modifiers.AQ_GUARDED_UNIT);
                    temp = temp.trim();
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("G AQ", temp, x, y, centered));
                }

                if( modifiers.containsKey(Modifiers.AS_COUNTRY))
                {
                    y = 2;
                    if(!strict && !(modifiers.containsKey(Modifiers.G_STAFF_COMMENTS) || modifiers.containsKey(Modifiers.AQ_GUARDED_UNIT)))
                        y--;
                    temp = modifiers.get(Modifiers.AS_COUNTRY);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("AS", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1) ||
                        modifiers.containsKey(Modifiers.AF_COMMON_IDENTIFIER))
                {
                    y = -1;
                    temp = "";
                    if(modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1))
                        temp = modifiers.get(Modifiers.H_ADDITIONAL_INFO_1) + sep;
                    if(modifiers.containsKey(Modifiers.AF_COMMON_IDENTIFIER))
                        temp += modifiers.get(Modifiers.AF_COMMON_IDENTIFIER);
                    temp = temp.trim();
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("H AF", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.J_EVALUATION_RATING) ||
                        modifiers.containsKey(Modifiers.K_COMBAT_EFFECTIVENESS) ||
                        modifiers.containsKey(Modifiers.L_SIGNATURE_EQUIP) ||
                        modifiers.containsKey(Modifiers.P_IFF_SIF_AIS))
                {
                    y = -2;
                    if(!strict && !(modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1) ||
                            modifiers.containsKey(Modifiers.AF_COMMON_IDENTIFIER)))
                        y++;
                    temp = "";
                    if(modifiers.containsKey(Modifiers.J_EVALUATION_RATING))
                        temp = modifiers.get(Modifiers.J_EVALUATION_RATING) + sep;
                    if(modifiers.containsKey(Modifiers.K_COMBAT_EFFECTIVENESS))
                        temp += modifiers.get(Modifiers.K_COMBAT_EFFECTIVENESS) + sep;
                    if(modifiers.containsKey(Modifiers.L_SIGNATURE_EQUIP))
                        temp += modifiers.get(Modifiers.L_SIGNATURE_EQUIP) + sep;
                    if(modifiers.containsKey(Modifiers.P_IFF_SIF_AIS))
                        temp += modifiers.get(Modifiers.P_IFF_SIF_AIS);
                    temp = temp.trim();
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("J K L P", temp, x, y, centered));
                }

                //Do left side labels
                x = -1;//on left

                if(modifiers.containsKey(Modifiers.V_EQUIP_TYPE) ||
                        modifiers.containsKey(Modifiers.AD_PLATFORM_TYPE) ||
                        modifiers.containsKey(Modifiers.AE_EQUIPMENT_TEARDOWN_TIME))
                {
                    y = 0;//center
                    centered = true;//vertically centered, only matters for labels on left and right side

                    temp = "";
                    if(modifiers.containsKey(Modifiers.V_EQUIP_TYPE))
                        temp = modifiers.get(Modifiers.V_EQUIP_TYPE) + sep;
                    if(modifiers.containsKey(Modifiers.AD_PLATFORM_TYPE))
                        temp += modifiers.get(Modifiers.AD_PLATFORM_TYPE) + sep;
                    if(modifiers.containsKey(Modifiers.AE_EQUIPMENT_TEARDOWN_TIME))
                        temp += modifiers.get(Modifiers.AE_EQUIPMENT_TEARDOWN_TIME);
                    temp = temp.trim();
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("V AD AE", temp, x, y, centered));
                }
                else if(!strict)
                {
                    centered = false;
                }

                if(modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH) || modifiers.containsKey(Modifiers.Y_LOCATION))
                {
                    y = 1;
                    temp = "";
                    if(modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH))
                        temp = modifiers.get(Modifiers.X_ALTITUDE_DEPTH) + sep;
                    if(modifiers.containsKey(Modifiers.Y_LOCATION))
                        temp += modifiers.get(Modifiers.Y_LOCATION);

                    temp = temp.trim();
                    mods.add(new Modifier("X Y", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.W_DTG_1))
                {
                    y = 2;//above center
                    if(!strict && !(modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH) || modifiers.containsKey(Modifiers.Y_LOCATION)))
                        y--;

                    temp = modifiers.get(Modifiers.W_DTG_1);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("W", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1))
                {
                    y = -1;//below center
                    temp = modifiers.get(Modifiers.T_UNIQUE_DESIGNATION_1);

                    temp = temp.trim();
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("T", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.Z_SPEED))
                {
                    y = -2;
                    if(!strict && !modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1))
                        y++;
                    temp = modifiers.get(Modifiers.Z_SPEED);
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("Z", temp, x, y, centered));
                }
            }
            else if(ss == SymbolID.SymbolSet_LandInstallation)
            {
                //No top center label

                //Do right side labels
                x = 1;//on right
                if(modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1))
                {
                    y = 0;//center
                    centered = true;//vertically centered, only matters for labels on left and right side
                    temp = modifiers.get(Modifiers.H_ADDITIONAL_INFO_1) + sep;

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("H", temp, x, y, centered));
                }
                else if(!strict)
                {
                    //if no "H', bring G and M closer to the center
                    centered = false;
                }

                if(modifiers.containsKey(Modifiers.G_STAFF_COMMENTS) || modifiers.containsKey(Modifiers.AQ_GUARDED_UNIT))
                {
                    y = 1;//above center
                    temp = "";
                    if(modifiers.containsKey(Modifiers.G_STAFF_COMMENTS))
                        temp = modifiers.get(Modifiers.G_STAFF_COMMENTS) + sep;
                    if(modifiers.containsKey(Modifiers.AQ_GUARDED_UNIT))
                        temp += modifiers.get(Modifiers.AQ_GUARDED_UNIT);
                    temp = temp.trim();
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("G AQ", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.AS_COUNTRY))
                {
                    y = 2;
                    if(!strict && !(modifiers.containsKey(Modifiers.G_STAFF_COMMENTS) || modifiers.containsKey(Modifiers.AQ_GUARDED_UNIT)))
                        y--;
                    temp = modifiers.get(Modifiers.AS_COUNTRY);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("AS", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.M_HIGHER_FORMATION))
                {
                    y = -1;//below center
                    temp = modifiers.get(Modifiers.M_HIGHER_FORMATION);
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("M", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.J_EVALUATION_RATING) ||
                        modifiers.containsKey(Modifiers.K_COMBAT_EFFECTIVENESS) ||
                        modifiers.containsKey(Modifiers.P_IFF_SIF_AIS))
                {
                    y = -2;
                    if(!strict && !modifiers.containsKey(Modifiers.M_HIGHER_FORMATION))
                        y++;
                    temp = "";
                    if(modifiers.containsKey(Modifiers.J_EVALUATION_RATING))
                        temp = modifiers.get(Modifiers.J_EVALUATION_RATING) + sep;
                    if(modifiers.containsKey(Modifiers.K_COMBAT_EFFECTIVENESS))
                        temp += modifiers.get(Modifiers.K_COMBAT_EFFECTIVENESS) + sep;
                    if(modifiers.containsKey(Modifiers.P_IFF_SIF_AIS))
                        temp += modifiers.get(Modifiers.P_IFF_SIF_AIS);
                    temp = temp.trim();
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("J K P", temp, x, y, centered));
                }

                //Do left side labels
                x = -1;//on left
                centered = false;

                if(modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH) || modifiers.containsKey(Modifiers.Y_LOCATION))
                {
                    y = 1;
                    temp = "";
                    if(modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH))
                        temp = modifiers.get(Modifiers.X_ALTITUDE_DEPTH) + sep;
                    if(modifiers.containsKey(Modifiers.Y_LOCATION))
                        temp += modifiers.get(Modifiers.Y_LOCATION);

                    temp = temp.trim();
                    mods.add(new Modifier("X Y", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.W_DTG_1))
                {
                    y = 2;//above center
                    if(!strict && !(modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH) || modifiers.containsKey(Modifiers.Y_LOCATION)))
                        y--;

                    temp = modifiers.get(Modifiers.W_DTG_1);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("W", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.C_QUANTITY) || modifiers.containsKey(Modifiers.AE_EQUIPMENT_TEARDOWN_TIME))
                {
                    y = -1;//below center
                    temp = "";
                    if(modifiers.containsKey(Modifiers.C_QUANTITY))
                        temp = modifiers.get(Modifiers.C_QUANTITY) + sep;
                    if(modifiers.containsKey(Modifiers.AE_EQUIPMENT_TEARDOWN_TIME))
                        temp += modifiers.get(Modifiers.AE_EQUIPMENT_TEARDOWN_TIME);

                    temp = temp.trim();
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("C AE", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1))
                {
                    y = -2;
                    if(!strict && !(modifiers.containsKey(Modifiers.C_QUANTITY) || modifiers.containsKey(Modifiers.AE_EQUIPMENT_TEARDOWN_TIME)))
                        y++;
                    temp = modifiers.get(Modifiers.T_UNIQUE_DESIGNATION_1);
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("T", temp, x, y, centered));
                }
            }
            else if(ss == SymbolID.SymbolSet_Space ||
                    ss == SymbolID.SymbolSet_SpaceMissile ||
                    ss == SymbolID.SymbolSet_Air ||
                    ss == SymbolID.SymbolSet_AirMissile ||
                    (ss == SymbolID.SymbolSet_SignalsIntelligence &&
                            (fs == SymbolID.FrameShape_Space || fs == SymbolID.FrameShape_Air)))
            {
                //No top center label
                x = 0;//centered
                y = 9;//on top of symbol

                if(modifiers.containsKey(Modifiers.C_QUANTITY))
                {
                    temp = modifiers.get(Modifiers.C_QUANTITY);
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("C", temp, x, y, centered));
                }
                else if(modifiers.containsKey(Modifiers.B_ECHELON))
                {
                    temp = modifiers.get(Modifiers.B_ECHELON);
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("B", temp, x, y, centered));
                }


                //Do right side labels
                x = 1;//on right
                centered = true;

                if(modifiers.containsKey(Modifiers.V_EQUIP_TYPE) || modifiers.containsKey(Modifiers.AF_COMMON_IDENTIFIER))
                {
                    y = 0;//
                    temp = "";
                    if(modifiers.containsKey(Modifiers.V_EQUIP_TYPE))
                        temp = modifiers.get(Modifiers.V_EQUIP_TYPE) + sep;
                    if(modifiers.containsKey(Modifiers.AF_COMMON_IDENTIFIER))
                        temp += modifiers.get(Modifiers.AF_COMMON_IDENTIFIER);
                    temp = temp.trim();

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("V AF", temp, x, y, centered));
                }
                else
                {
                    centered = false;
                }

                if(modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1) || modifiers.containsKey(Modifiers.Y_LOCATION))
                {
                    y = 1;//above center
                    temp = "";
                    if(modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1))
                        temp = modifiers.get(Modifiers.T_UNIQUE_DESIGNATION_1) + sep;
                    if(modifiers.containsKey(Modifiers.Y_LOCATION))
                        temp += modifiers.get(Modifiers.Y_LOCATION);
                    temp = temp.trim();

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("T Y", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.AS_COUNTRY))
                {
                    y = 2;
                    if(!strict && !(modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1) || modifiers.containsKey(Modifiers.Y_LOCATION)))
                        y--;
                    temp = modifiers.get(Modifiers.AS_COUNTRY);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("AS", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.P_IFF_SIF_AIS)  ||
                        modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH)  ||
                        modifiers.containsKey(Modifiers.Z_SPEED))
                {
                    y = -1;//below center
                    temp = "";
                    if(modifiers.containsKey(Modifiers.P_IFF_SIF_AIS))
                        temp = modifiers.get(Modifiers.P_IFF_SIF_AIS) + sep;
                    if(modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH))
                        temp = modifiers.get(Modifiers.X_ALTITUDE_DEPTH) + sep;
                    if(modifiers.containsKey(Modifiers.Z_SPEED))
                        temp = modifiers.get(Modifiers.Z_SPEED);

                    temp = temp.trim();

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("P X Z", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.G_STAFF_COMMENTS) ||
                        modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1) ||
                        modifiers.containsKey(Modifiers.J_EVALUATION_RATING))
                {
                    y = -2;//below center
                    if(!(modifiers.containsKey(Modifiers.P_IFF_SIF_AIS)  ||
                            modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH)  ||
                            modifiers.containsKey(Modifiers.Z_SPEED)))
                        y++;

                    temp = "";
                    if(modifiers.containsKey(Modifiers.G_STAFF_COMMENTS))
                        temp = modifiers.get(Modifiers.G_STAFF_COMMENTS) + sep;
                    if(modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1))
                        temp = modifiers.get(Modifiers.H_ADDITIONAL_INFO_1) + sep;
                    if(modifiers.containsKey(Modifiers.J_EVALUATION_RATING))
                        temp += modifiers.get(Modifiers.J_EVALUATION_RATING);
                    temp = temp.trim();

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("G H J", temp, x, y, centered));
                }

                //No left side labels
                x = -1;//on right
                centered = true;

                if(modifiers.containsKey(Modifiers.AD_PLATFORM_TYPE))
                {
                    y = 0;//
                    temp = temp += modifiers.get(Modifiers.AD_PLATFORM_TYPE);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("AD", temp, x, y, centered));
                }
                else
                {
                    centered = false;
                }

                if(modifiers.containsKey(Modifiers.AR_SPECIAL_DESIGNATOR))
                {
                    y = 1;//above center
                    temp = modifiers.get(Modifiers.AR_SPECIAL_DESIGNATOR);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("AR", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.W_DTG_1))
                {
                    y = 2;
                    if(!strict && !modifiers.containsKey(Modifiers.AR_SPECIAL_DESIGNATOR))
                        y--;
                    temp = modifiers.get(Modifiers.W_DTG_1);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("W", temp, x, y, centered));
                }
            }
            else if(ss == SymbolID.SymbolSet_SeaSurface ||
                    (ss == SymbolID.SymbolSet_SignalsIntelligence && fs == SymbolID.FrameShape_SeaSurface))
            {
                //No top center label


                //Do right side labels
                x = 1;//on right
                centered = false;

                if(modifiers.containsKey(Modifiers.V_EQUIP_TYPE))
                {
                    y = 1;//above center
                    temp = modifiers.get(Modifiers.V_EQUIP_TYPE);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("V", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1))
                {
                    y = 2;
                    if(!strict && !modifiers.containsKey(Modifiers.V_EQUIP_TYPE))
                        y--;
                    temp = modifiers.get(Modifiers.T_UNIQUE_DESIGNATION_1);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("T", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.AS_COUNTRY))
                {
                    y = 3;
                    if(!strict)
                    {
                        if(!modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1))
                            y--;
                        if(!modifiers.containsKey(Modifiers.V_EQUIP_TYPE))
                            y--;
                    }

                    temp = modifiers.get(Modifiers.AS_COUNTRY );

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("AS", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.P_IFF_SIF_AIS))
                {
                    y = -1;//below center
                    temp = modifiers.get(Modifiers.P_IFF_SIF_AIS);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("P", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.G_STAFF_COMMENTS)  ||
                        modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1))
                {
                    y = -2;//below center
                    if(!modifiers.containsKey(Modifiers.P_IFF_SIF_AIS))
                        y++;

                    temp = "";
                    if(modifiers.containsKey(Modifiers.G_STAFF_COMMENTS))
                        temp = modifiers.get(Modifiers.G_STAFF_COMMENTS) + sep;
                    if(modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1))
                        temp += modifiers.get(Modifiers.H_ADDITIONAL_INFO_1);
                    temp = temp.trim();

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("G H", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.Y_LOCATION) ||
                        modifiers.containsKey(Modifiers.Z_SPEED))
                {
                    y = -3;
                    if(!strict)
                    {
                        if(!(modifiers.containsKey(Modifiers.G_STAFF_COMMENTS)  ||
                                modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1)))
                            y++;
                        if(!modifiers.containsKey(Modifiers.P_IFF_SIF_AIS))
                            y++;
                    }
                    temp = "";
                    if(modifiers.containsKey(Modifiers.Y_LOCATION))
                        temp = modifiers.get(Modifiers.Y_LOCATION) + sep;
                    if(modifiers.containsKey(Modifiers.Z_SPEED))
                        temp += modifiers.get(Modifiers.Z_SPEED);
                    temp = temp.trim();
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("Y Z", temp, x, y, centered));
                }

                //No left side labels
                x = -1;
                centered = false;
                if(modifiers.containsKey(Modifiers.AQ_GUARDED_UNIT) ||
                        modifiers.containsKey(Modifiers.AR_SPECIAL_DESIGNATOR))
                {
                    y = 2;
                    if(!strict)
                    {
                        y--;
                    }
                    temp = "";
                    if(modifiers.containsKey(Modifiers.AQ_GUARDED_UNIT))
                        temp = modifiers.get(Modifiers.AQ_GUARDED_UNIT) + sep;
                    if(modifiers.containsKey(Modifiers.AR_SPECIAL_DESIGNATOR))
                        temp += modifiers.get(Modifiers.AR_SPECIAL_DESIGNATOR);
                    temp = temp.trim();
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("AQ AR", temp, x, y, centered));
                }
            }
            else if(ss == SymbolID.SymbolSet_SeaSubsurface ||
                    (ss == SymbolID.SymbolSet_SignalsIntelligence && fs == SymbolID.FrameShape_SeaSubsurface))
            {
                //No top center label


                //Do right side labels
                x = 1;//on right
                centered = false;

                if(modifiers.containsKey(Modifiers.V_EQUIP_TYPE))
                {
                    y = 1;//above center
                    temp = modifiers.get(Modifiers.V_EQUIP_TYPE);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("V", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1))
                {
                    y = 2;
                    if(!strict && !modifiers.containsKey(Modifiers.V_EQUIP_TYPE))
                        y--;
                    temp = modifiers.get(Modifiers.T_UNIQUE_DESIGNATION_1);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("T", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.AS_COUNTRY))
                {
                    y = 3;
                    if(!strict)
                    {
                        if(!modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1))
                            y--;
                        if(!modifiers.containsKey(Modifiers.V_EQUIP_TYPE))
                            y--;
                    }

                    temp = modifiers.get(Modifiers.AS_COUNTRY );

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("AS", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.P_IFF_SIF_AIS))
                {
                    y = -1;//below center
                    temp = modifiers.get(Modifiers.P_IFF_SIF_AIS);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("P", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.G_STAFF_COMMENTS)  ||
                        modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1))
                {
                    y = -2;//below center
                    if(!modifiers.containsKey(Modifiers.P_IFF_SIF_AIS))
                        y++;

                    temp = "";
                    if(modifiers.containsKey(Modifiers.G_STAFF_COMMENTS))
                        temp = modifiers.get(Modifiers.G_STAFF_COMMENTS) + sep;
                    if(modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1))
                        temp += modifiers.get(Modifiers.H_ADDITIONAL_INFO_1);
                    temp = temp.trim();

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("G H", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.Y_LOCATION) ||
                        modifiers.containsKey(Modifiers.Z_SPEED))
                {
                    y = -3;
                    if(!strict)
                    {
                        if(!(modifiers.containsKey(Modifiers.G_STAFF_COMMENTS)  ||
                                modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1)))
                            y++;
                        if(!modifiers.containsKey(Modifiers.P_IFF_SIF_AIS))
                            y++;
                    }
                    temp = "";
                    if(modifiers.containsKey(Modifiers.Y_LOCATION))
                        temp = modifiers.get(Modifiers.Y_LOCATION) + sep;
                    if(modifiers.containsKey(Modifiers.Z_SPEED))
                        temp += modifiers.get(Modifiers.Z_SPEED);
                    temp = temp.trim();
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("Y Z", temp, x, y, centered));
                }

                //No left side labels
                x = -1;
                centered = false;
                if(modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH))
                {
                    y = 1;
                    temp = modifiers.get(Modifiers.X_ALTITUDE_DEPTH);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("X", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.AR_SPECIAL_DESIGNATOR))
                {
                    y = 2;
                    if(!strict && !modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH))
                    {
                        y--;
                    }
                    temp = modifiers.get(Modifiers.AR_SPECIAL_DESIGNATOR);
                    temp = temp.trim();
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("AR", temp, x, y, centered));
                }
            }
            else if(ss == SymbolID.SymbolSet_DismountedIndividuals)
            {
                //No top center label


                //Do right side labels
                x = 1;//on right
                if(modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1))
                {
                    y = 0;//center
                    centered = true;//vertically centered, only matters for labels on left and right side
                    temp = modifiers.get(Modifiers.H_ADDITIONAL_INFO_1);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("H", temp, x, y, centered));
                }
                else if(!strict)
                {
                    centered = false;
                }

                if(modifiers.containsKey(Modifiers.G_STAFF_COMMENTS))
                {
                    y = 1;//above center
                    temp = modifiers.get(Modifiers.G_STAFF_COMMENTS);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("G", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.AS_COUNTRY))
                {
                    y = 2;
                    if(!strict && !(modifiers.containsKey(Modifiers.G_STAFF_COMMENTS)))
                        y--;
                    temp = modifiers.get(Modifiers.AS_COUNTRY);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("AS", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.M_HIGHER_FORMATION))
                {
                    y = -1;//below center
                    temp = modifiers.get(Modifiers.M_HIGHER_FORMATION);
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("M", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.J_EVALUATION_RATING) ||
                        modifiers.containsKey(Modifiers.K_COMBAT_EFFECTIVENESS) ||
                        modifiers.containsKey(Modifiers.P_IFF_SIF_AIS))
                {
                    y = -2;
                    if(!strict && !modifiers.containsKey(Modifiers.M_HIGHER_FORMATION))
                        y++;
                    temp = "";
                    if(modifiers.containsKey(Modifiers.J_EVALUATION_RATING))
                        temp = modifiers.get(Modifiers.J_EVALUATION_RATING) + sep;
                    if(modifiers.containsKey(Modifiers.K_COMBAT_EFFECTIVENESS))
                        temp += modifiers.get(Modifiers.K_COMBAT_EFFECTIVENESS) + sep;
                    if(modifiers.containsKey(Modifiers.P_IFF_SIF_AIS))
                        temp += modifiers.get(Modifiers.P_IFF_SIF_AIS);
                    temp = temp.trim();
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("J K P", temp, x, y, centered));
                }

                //Do left side labels
                x = -1;//on left

                if(modifiers.containsKey(Modifiers.V_EQUIP_TYPE) ||
                        modifiers.containsKey(Modifiers.AF_COMMON_IDENTIFIER))
                {
                    y = 0;//center
                    centered = true;//vertically centered, only matters for labels on left and right side

                    temp = "";
                    if(modifiers.containsKey(Modifiers.V_EQUIP_TYPE))
                        temp = modifiers.get(Modifiers.V_EQUIP_TYPE) + sep;
                    if(modifiers.containsKey(Modifiers.AF_COMMON_IDENTIFIER))
                        temp += modifiers.get(Modifiers.AF_COMMON_IDENTIFIER);
                    temp = temp.trim();
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("V AF", temp, x, y, centered));
                }
                else if(!strict)
                {
                    centered = false;
                }

                if(modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH) || modifiers.containsKey(Modifiers.Y_LOCATION))
                {
                    y = 1;
                    temp = "";
                    if(modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH))
                        temp = modifiers.get(Modifiers.X_ALTITUDE_DEPTH) + sep;
                    if(modifiers.containsKey(Modifiers.Y_LOCATION))
                        temp += modifiers.get(Modifiers.Y_LOCATION);

                    temp = temp.trim();
                    mods.add(new Modifier("X Y", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.W_DTG_1))
                {
                    y = 2;//above center
                    if(!strict && !(modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH) || modifiers.containsKey(Modifiers.Y_LOCATION)))
                        y--;

                    temp = modifiers.get(Modifiers.W_DTG_1);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("W", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1))
                {
                    y = -1;//below center
                    temp = modifiers.get(Modifiers.T_UNIQUE_DESIGNATION_1);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("T", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.Z_SPEED))
                {
                    y = -2;
                    if(!strict && !(modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1)))
                        y++;
                    temp = modifiers.get(Modifiers.Z_SPEED);
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("Z", temp, x, y, centered));
                }
            }
            else if(ss == SymbolID.SymbolSet_Activities)
            {
                //No top center label


                //Do right side labels
                x = 1;//on right
                if(modifiers.containsKey(Modifiers.G_STAFF_COMMENTS))
                {
                    y = 0;//center
                    centered = true;//vertically centered, only matters for labels on left and right side
                    temp = modifiers.get(Modifiers.G_STAFF_COMMENTS);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("G", temp, x, y, centered));
                }
                else if(!strict)
                {
                    centered = false;
                }

                if(modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1))
                {
                    y = 1;//above center
                    temp = modifiers.get(Modifiers.T_UNIQUE_DESIGNATION_1);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("T", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.AS_COUNTRY))
                {
                    y = 2;
                    if(!strict && !(modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1)))
                        y--;
                    temp = modifiers.get(Modifiers.AS_COUNTRY);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("AS", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1))
                {
                    y = -1;//below center
                    temp = modifiers.get(Modifiers.H_ADDITIONAL_INFO_1);
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("H", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.J_EVALUATION_RATING))
                {
                    y = -2;
                    if(!strict && !modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1))
                        y++;
                    temp = modifiers.get(Modifiers.J_EVALUATION_RATING);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("J", temp, x, y, centered));
                }

                //Do left side labels
                x = -1;//on left

                if(!strict)
                {
                    centered = false;
                }

                if(modifiers.containsKey(Modifiers.Y_LOCATION))
                {
                    y = 1;
                    temp = modifiers.get(Modifiers.Y_LOCATION);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("Y", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.W_DTG_1))
                {
                    y = 2;//above center
                    if(!strict && !(modifiers.containsKey(Modifiers.Y_LOCATION)))
                        y--;

                    temp = modifiers.get(Modifiers.W_DTG_1);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("W", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.C_QUANTITY))
                {
                    y = -1;//below center
                    temp = modifiers.get(Modifiers.C_QUANTITY);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("C", temp, x, y, centered));
                }

            }
            else if(ss == SymbolID.SymbolSet_CyberSpace ||
                    (ss == SymbolID.SymbolSet_SignalsIntelligence && fs == SymbolID.FrameShape_Cyberspace))
            {
                //Do top center label
                x = 0;//centered
                y = 9;//on top of symbol
                if(modifiers.containsKey(Modifiers.B_ECHELON))
                {
                    temp = modifiers.get(Modifiers.B_ECHELON);
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("B", temp, x, y, centered));
                }

                //Do right side labels
                x = 1;//on right
                if(modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1))
                {
                    y = 0;//center
                    centered = true;//vertically centered, only matters for labels on left and right side
                    temp = modifiers.get(Modifiers.H_ADDITIONAL_INFO_1);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("H", temp, x, y, centered));
                }
                else if(!strict)
                {
                    centered = false;
                }

                if(modifiers.containsKey(Modifiers.G_STAFF_COMMENTS))
                {
                    y = 1;//above center
                    temp = modifiers.get(Modifiers.G_STAFF_COMMENTS);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("G", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.F_REINFORCED_REDUCED) || modifiers.containsKey(Modifiers.AS_COUNTRY))
                {
                    y = 2;
                    if(!strict && !(modifiers.containsKey(Modifiers.G_STAFF_COMMENTS)))
                        y--;
                    temp = "";
                    if(modifiers.containsKey(Modifiers.F_REINFORCED_REDUCED))
                        temp = modifiers.get(Modifiers.F_REINFORCED_REDUCED) + sep;
                    if(modifiers.containsKey(Modifiers.AS_COUNTRY))
                        temp += modifiers.get(Modifiers.AS_COUNTRY);
                    temp = temp.trim();
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("F AS", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.M_HIGHER_FORMATION))
                {
                    y = -1;//below center
                    temp = modifiers.get(Modifiers.M_HIGHER_FORMATION);
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("M", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.K_COMBAT_EFFECTIVENESS) || modifiers.containsKey(Modifiers.L_SIGNATURE_EQUIP))
                {
                    y = -2;
                    if(!strict && !modifiers.containsKey(Modifiers.M_HIGHER_FORMATION))
                        y++;
                    temp = "";
                    if(modifiers.containsKey(Modifiers.K_COMBAT_EFFECTIVENESS))
                        temp = modifiers.get(Modifiers.K_COMBAT_EFFECTIVENESS) + sep;
                    if(modifiers.containsKey(Modifiers.L_SIGNATURE_EQUIP))
                        temp += modifiers.get(Modifiers.L_SIGNATURE_EQUIP);
                    temp = temp.trim();
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("K L", temp, x, y, centered));
                }

                //Do left side labels
                x=-1;
                if(modifiers.containsKey(Modifiers.V_EQUIP_TYPE))
                {
                    y = 0;//center
                    centered = true;//vertically centered, only matters for labels on left and right side

                    temp = modifiers.get(Modifiers.V_EQUIP_TYPE);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("V", temp, x, y, centered));
                }
                else if(!strict)
                {
                    centered = false;
                }

                if(modifiers.containsKey(Modifiers.Y_LOCATION))
                {
                    y = 1;
                    temp = modifiers.get(Modifiers.Y_LOCATION);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("Y", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.W_DTG_1))
                {
                    y = 2;//above center
                    if(!strict && !(modifiers.containsKey(Modifiers.Y_LOCATION)))
                        y--;

                    temp = modifiers.get(Modifiers.W_DTG_1);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("W", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1))
                {
                    y = -1;//below center
                    temp = modifiers.get(Modifiers.T_UNIQUE_DESIGNATION_1);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("T", temp, x, y, centered));
                }
            }
            /*else if(ver == SymbolID.SymbolSet_MineWarfare)
            {
                //no modifiers
            }//*/
            //else//SymbolSet Unknown
            //processUnknownTextModifiers
        }

        return mods;
    }


    /**
     *
     * @param bounds bounds of the core icon
     * @param labelWidth height of the label to be placed
     * @param location if 1, label on right side of symbol. On left if -1, center if 0.
     * @returns
     */
    private static double getLabelXPosition(Rect bounds, Point centerPoint, int labelWidth, int location, float modifierFontHeight)
    {
        int buffer = (int)modifierFontHeight/2;

        double x = 0;
        if(location == 1)//on right
        {
            x = bounds.left + bounds.width() + buffer;
        }
        else if(location == -1)//left
        {
            x = bounds.left - labelWidth - buffer;
        }
        else if(location == 0)
        {
            if(centerPoint != null)
                x = (int)Math.round(centerPoint.x - (labelWidth * 0.5));
            else
                x = Math.round((bounds.left + (bounds.width() * 0.5f)) - (labelWidth * 0.5f));
        }
        return x;
    }

    /**
     *
     * @param bounds bounds of the core icon
     * @param labelHeight height of the label to be placed
     * @param descent descent of the label to be placed
     * @param bufferText spacing buffer if desired
     * @param centered if true, there will be a center label location identified by 0
     * @param location positive 1, 2, 3 to be above symbol mid-point or negative values to be below
     * @returns y position
     */
    private static double getLabelYPosition(Rect bounds, int labelHeight, int descent, int bufferText, boolean centered, int location) {
        double y = 0;
        if (bounds != null && !bounds.isEmpty())
        {
            if(centered)
            {
                switch (location)
                {
                    case 3://3 above center
                        y = (bounds.height());
                        y = ((y * 0.5) + (labelHeight * 0.5));
                        y = y - ((labelHeight + bufferText) * 3);
                        y = bounds.top + y;
                        break;
                    case 2://2 above center
                        y = (bounds.height());
                        y = ((y * 0.5) + (labelHeight * 0.5));
                        y = y - ((labelHeight + bufferText) * 2);
                        y = bounds.top + y;
                        break;
                    case 1://1 above center
                        y = (bounds.height());
                        y = ((y * 0.5) + (labelHeight * 0.5));
                        y = y - ((labelHeight + bufferText));
                        y = bounds.top + y;
                        break;
                    case 0: //centered
                        y = (bounds.height());
                        y = ((y * 0.5) + ((labelHeight - descent) * 0.5));
                        y = bounds.top + y;
                        break;
                    case -1://1 below center
                        y = (bounds.height());
                        y = ((y * 0.5) + (labelHeight * 0.5));
                        y = y + ((labelHeight + bufferText - descent));
                        y = bounds.top + y;
                        break;
                    case -2://2 below center
                        y = (bounds.height());
                        y = ((y * 0.5) + (labelHeight * 0.5));
                        y = y + ((labelHeight + bufferText) * 2) - (descent);
                        y = bounds.top + y;
                        break;
                    case -3://3 below center
                        y = (bounds.height());
                        y = ((y * 0.5) + (labelHeight * 0.5));
                        y = y + ((labelHeight + bufferText) * 3) - (descent);
                        y = bounds.top + y;
                        break;
                }
            }
            else//split between top and bottom
            {
                switch (location)
                {
                    case 3:
                        y = (bounds.top + ((bounds.height() / 2) - descent - labelHeight*2 - bufferText));
                        break;
                    case 2:
                        y = (bounds.top + ((bounds.height() / 2) - descent - labelHeight - bufferText));
                        break;
                    case 1:
                        y = (bounds.top + ((bounds.height() / 2) - descent));
                        break;
                    case -1:
                        y = (bounds.top + (bounds.height() / 2) + (labelHeight - descent + bufferText));
                        break;
                    case -2:
                        y = (bounds.top + (bounds.height() / 2) + ((labelHeight*2 - descent + bufferText)));
                        break;
                    case -3:
                        y = (bounds.top + (bounds.height() / 2) + ((labelHeight*3 - descent + bufferText)));
                        break;
                }
            }
        }
        return y;
    }

    private static boolean isCOnTop(String symbolID)
    {
        boolean onTop = false;

        int version = SymbolID.getVersion(symbolID);
        int ss = SymbolID.getSymbolSet(symbolID);
        char frame = SymbolID.getFrameShape(symbolID);

        if(SymbolUtilities.hasModifier(symbolID,Modifiers.C_QUANTITY)) {
            if (frame == SymbolID.FrameShape_Air || frame == SymbolID.FrameShape_Space)
                onTop = true;
            else if(frame == '0')
            {
                if (ss == SymbolID.SymbolSet_Air ||
                        ss == SymbolID.SymbolSet_AirMissile ||
                        ss == SymbolID.SymbolSet_Space ||
                        ss == SymbolID.SymbolSet_SpaceMissile ||
                        ss == SymbolID.SymbolSet_SignalsIntelligence_Air ||
                        (ss == SymbolID.SymbolSet_LandEquipment && version <= SymbolID.Version_2525Dch1)) {
                    onTop = true;
                }
            }
        }
        return onTop;
    }
    public static boolean hasDisplayModifiers(String symbolID, Map<String,String> modifiers)
    {
        boolean hasModifiers = false;
        int ss = SymbolID.getSymbolSet(symbolID);
        int status = SymbolID.getStatus(symbolID);
        int context = SymbolID.getContext(symbolID);

        if(ss == SymbolID.SymbolSet_ControlMeasure)//check control measure
        {
            if (SymbolUtilities.isCBRNEvent(symbolID) == true && modifiers.containsKey(Modifiers.Q_DIRECTION_OF_MOVEMENT))
            {
                hasModifiers = true;
            }
            else if(SymbolUtilities.hasFDI(symbolID))
                hasModifiers = true;
        }
        else if(ss != SymbolID.SymbolSet_Atmospheric &&
                ss != SymbolID.SymbolSet_Oceanographic &&
                ss != SymbolID.SymbolSet_MeteorologicalSpace)
        {//checking units

            if(context > 0) //Exercise or Simulation
                hasModifiers = true;

            //echelon or mobility,
            if (SymbolID.getAmplifierDescriptor(symbolID) > 0 || modifiers.containsKey(Modifiers.Q_DIRECTION_OF_MOVEMENT))
                hasModifiers = true;

            if(modifiers.containsKey(Modifiers.AJ_SPEED_LEADER))
                hasModifiers = true;

            if(modifiers.containsKey(Modifiers.AO_ENGAGEMENT_BAR))
                hasModifiers = true;

            //HQ/Taskforce
            if(SymbolID.getHQTFD(symbolID) > 0)
                hasModifiers = true;

            if(status > 1)//Fully capable, damaged, destroyed
                hasModifiers = true;
        }//no display modifiers for single point weather



        return hasModifiers;
    }

    public static boolean hasTextModifiers(String symbolID, Map<String,String> modifiers)
    {

        int ss = SymbolID.getSymbolSet(symbolID);
        int ec = SymbolID.getEntityCode(symbolID);
        if(ss == SymbolID.SymbolSet_Atmospheric)
        {
            switch(ec)
            {
                case 110102: //tropopause low
                case 110202: //tropopause high
                case 162200: //tropopause level ?
                case 162300: //freezing level ?
                    return true;
                default:
                        return false;
            }
        }
        else if(ss == SymbolID.SymbolSet_Oceanographic || ss == SymbolID.SymbolSet_MeteorologicalSpace)
        {
            return false;
        }
        else if (ss == SymbolID.SymbolSet_ControlMeasure)
        {
            MSInfo msi = MSLookup.getInstance().getMSLInfo(symbolID);

            if( msi.getModifiers().size() > 0 && modifiers != null && modifiers.size() > 0)
                return true;
            else
                return false;
        }
        else
        {

            if (SymbolUtilities.getStandardIdentityModifier(symbolID) != null)
            {
                return true;
            }

            int cc = SymbolID.getCountryCode(symbolID);
            if (cc > 0 && !GENCLookup.getInstance().get3CharCode(cc).isEmpty())
            {
                return true;
            }//*/

            else if (modifiers.size() > 0)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Rerturns either the default font from RendererSettings or font based on properties
     * set in MilStdAttributes.
     * @param attributes
     * @return
     */
    private static Paint getFont(Map<String,String> attributes)
    {
        Paint p = null;
        Typeface tf = null;

        p = RendererSettings.getInstance().getModiferFont();

        String[] props = RendererSettings.getInstance().getModiferFontProps();

        String ff = props[0];
        int fstyle = Integer.parseInt(props[1]);// modifierFont.getTypeface().getStyle();
        float fsize = Float.parseFloat(props[2]);// modifierFont.getTextSize();
        String temp = null;


        if(attributes.containsKey(MilStdAttributes.FontFamily) ||
                attributes.containsKey(MilStdAttributes.FontStyle) ||
                attributes.containsKey(MilStdAttributes.FontSize))
        {
            if(attributes.containsKey(MilStdAttributes.FontStyle))
            {
                temp = attributes.get(MilStdAttributes.FontStyle);
                if (temp != null && !temp.isEmpty()) {
                    fstyle = Integer.parseInt(temp);
                }
            }
            if(attributes.containsKey(MilStdAttributes.FontSize))
            {
                temp = attributes.get(MilStdAttributes.FontSize);
                if (temp != null && !temp.isEmpty()) {
                    fsize = Float.parseFloat(temp);
                }
            }
            if(attributes.containsKey(MilStdAttributes.FontFamily))
            {
                temp = attributes.get(MilStdAttributes.FontFamily);
                if (temp != null && !temp.isEmpty()) {
                    ff =  temp;//Typeface.create(temp,fstyle);
                }
            }
            tf = Typeface.create(ff,fstyle);
            if(tf == null)
                tf = Typeface.create(props[0],fstyle);
            if(tf == null)
                return null;

            p = new Paint();
            p.setTextSize(fsize);
            p.setAntiAlias(true);
            p.setTypeface(tf);
            p.setStrokeCap(Cap.BUTT);
            p.setStrokeJoin(Join.MITER);
            p.setStrokeMiter(3f);

            return p;
        }
        else
            return _modifierFont;

    }

    private static float[] getFontHeightandDescent(Paint font)
    {
        float[] hd = {0f,0f};

        if(font != null) {
            Paint.FontMetrics fm = font.getFontMetrics();
            //_modifierFontHeight = fm.top + fm.bottom;
            hd[0] = fm.bottom - fm.top;
            hd[1] = fm.descent;
        }
        return hd;
    }

}
