package armyc2.c5isr.renderer.test3;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import armyc2.c5isr.JavaLineArray.POINT2;
import armyc2.c5isr.JavaLineArray.lineutility;
import armyc2.c5isr.graphics2d.AffineTransform;
import armyc2.c5isr.graphics2d.BasicStroke;
import armyc2.c5isr.graphics2d.Point;
import armyc2.c5isr.graphics2d.Point2D;
import armyc2.c5isr.graphics2d.TextLayout;
import armyc2.c5isr.renderer.utilities.Color;
import armyc2.c5isr.renderer.utilities.ErrorLogger;
import armyc2.c5isr.renderer.utilities.IPointConversion;
import armyc2.c5isr.renderer.utilities.MilStdAttributes;
import armyc2.c5isr.renderer.utilities.MilStdSymbol;
import armyc2.c5isr.renderer.utilities.Modifiers;
import armyc2.c5isr.renderer.utilities.PointConversion;
import armyc2.c5isr.renderer.utilities.RendererException;
import armyc2.c5isr.renderer.utilities.RendererSettings;
import armyc2.c5isr.renderer.utilities.ShapeInfo;
import armyc2.c5isr.web.render.GeoPixelConversion;
import armyc2.c5isr.web.render.MultiPointHandler;
import armyc2.c5isr.web.render.WebRenderer;
/**
 */
public final class utility {

    protected static double leftLongitude;
    protected static double rightLongitude;
    protected static double upperLatitude;
    protected static double lowerLatitude;

    /**
     * uses the PointConversion to convert to geo
     *
     * @param pts
     * @param converter
     * @return
     */
    private static ArrayList<POINT2> PixelsToLatLong(ArrayList<Point> pts,
            IPointConversion converter) {
        int j = 0;
        Point pt = null;
        Point2D pt2d = null;
        ArrayList<Point2D> pts2d = new ArrayList();
        for (j = 0; j < pts.size(); j++) {
            pt = pts.get(j);
            pt2d=new Point2D.Double(pt.x,pt.y);
            pt2d = converter.PixelsToGeo(pt2d);
            pts2d.add(pt2d);
        }
        ArrayList<POINT2> pts2 = new ArrayList();
        int n=pts2d.size();
        //for (j = 0; j < pts2d.size(); j++) 
        for (j = 0; j < n; j++) 
        {
            pts2.add(new POINT2(pts2d.get(j).getX(), pts2d.get(j).getY()));
        }

        return pts2;
    }

    private static double displayWidth;
    private static double displayHeight;

    protected static void set_displayPixelsWidth(double value) {
        displayWidth = value;
    }

    protected static void set_displayPixelsHeight(double value) {
        displayHeight = value;
    }

    protected static void SetExtents(double ullon, double lrlon, double ullat,
            double lrlat) {
        leftLongitude = ullon;// -95.5;
        rightLongitude = lrlon;// -93;
        upperLatitude = ullat;// 33.5;
        lowerLatitude = lrlat;// 32;
    }

    /**
     * string format is lon,lat lon,lat ...
     *
     * @param pts
     * @return
     */
    private static String controlPointsToString(ArrayList<POINT2> pts) {
        String str = "";
        int j = 0;
        int n=pts.size();
        //for (j = 0; j < pts.size(); j++) 
        for (j = 0; j < n; j++) 
        {
            str += Double.toString(pts.get(j).x);
            str += ",";
            str += Double.toString(pts.get(j).y);
            if (j < pts.size() - 1) {
                str += " ";
            }
        }
        return str;
    }

    /**
     * The tester for the Google Earth plugin. Assumes pixels only are provided.
     * If the linetype is fire support area then assume they are geo coords.
     * Then use a best fit approach to convert them to pixels.
     *
     * @param pts
     * @param symbolCode
     * @param g2d
     */
     static void DoubleClickGE(ArrayList<Point> pts,
            String symbolCode, Canvas g2d, Context context) {
        //test text scaling
        //RendererSettings.getInstance().setMPLabelFont("arial", Typeface.BOLD, 18, 2f);
        //Object obj = System.getProperty("java.version");
        ArrayList<Point2D> clipArea = new ArrayList();
        clipArea.add(new Point2D.Double(0, 0));
        clipArea.add(new Point2D.Double(displayWidth, 0));
        clipArea.add(new Point2D.Double(displayWidth, displayHeight));
        clipArea.add(new Point2D.Double(0, displayHeight));
        clipArea.add(new Point2D.Double(0, 0));
        //compute channel point for axis of advance
        //computePoint(pts,linetype);
        //utility.ClosePolygon(pts, linetype);
        //"-84.102854,39.799488,-84.100343,39.801342"
        //leftLongitude=-84.102854;
        //rightLongitude=-84.100343;
        //lowerLatitude=39.799488;
        //upperLatitude=39.801342;
        IPointConversion converter = new PointConversion((int) displayWidth,
                (int) displayHeight, upperLatitude, leftLongitude,
                lowerLatitude, rightLongitude);
        ArrayList<POINT2> pts2 = PixelsToLatLong(pts, converter);

        while (symbolCode.length() < 30) {
            symbolCode += "0";
        }
        boolean useDashArray=true;
        //uncomment the line to allow renderer to calculate the dashes
        //comment following line if client intends to calculate dashed lines to improve performance
        //useDashArray=false;

            double sizeSquare = Math.abs(rightLongitude - leftLongitude);
            if (sizeSquare > 180) {
                sizeSquare = 360 - sizeSquare;
            }

            // physical screen length (in meters) = pixels in screen / pixels per inch / inch per meter
            double screenLength = displayWidth / RendererSettings.getInstance().getDeviceDPI() / GeoPixelConversion.INCHES_PER_METER;
            // meters on screen = degrees on screen * meters per degree
            double metersOnScreen = sizeSquare * GeoPixelConversion.METERS_PER_DEG;
            double scale = metersOnScreen / screenLength;

            String rectStr = getRectString(0, 0);
            String controlPtsStr = controlPointsToString(pts2);
            String altitudeMode = "";
            Map<String,String> modifiers = new HashMap<>();
            Map<String,String> attributes = new HashMap<>();
            modifiers.put(Modifiers.T_UNIQUE_DESIGNATION_1, MainActivity.T);
            modifiers.put(Modifiers.T1_UNIQUE_DESIGNATION_2, MainActivity.T1);
            modifiers.put(Modifiers.AM_DISTANCE, MainActivity.AM);
            modifiers.put(Modifiers.AN_AZIMUTH, MainActivity.AN);
            modifiers.put(Modifiers.X_ALTITUDE_DEPTH, MainActivity.X);
            modifiers.put(Modifiers.H_ADDITIONAL_INFO_1, MainActivity.H);
            modifiers.put(Modifiers.H1_ADDITIONAL_INFO_2, MainActivity.H1);
            modifiers.put(Modifiers.W_DTG_1, MainActivity.W);
            modifiers.put(Modifiers.W1_DTG_2, MainActivity.W1);
            modifiers.put(Modifiers.V_EQUIP_TYPE, MainActivity.V);
            modifiers.put(Modifiers.Y_LOCATION, MainActivity.Y);
            modifiers.put(Modifiers.AP_TARGET_NUMBER, "1234");
            attributes.put(MilStdAttributes.FillColor, MainActivity.fillColor);
            attributes.put(MilStdAttributes.LineColor, MainActivity.lineColor);
            attributes.put(MilStdAttributes.TextColor, MainActivity.textColor);
            if (!MainActivity.lineWidth.isEmpty())
                attributes.put(MilStdAttributes.LineWidth, MainActivity.lineWidth);
            attributes.put(MilStdAttributes.UseDashArray, Boolean.toString(useDashArray));
            MilStdSymbol mss= WebRenderer.RenderMultiPointAsMilStdSymbol("id", "name", "description", symbolCode, controlPtsStr, altitudeMode, scale, rectStr, modifiers, attributes);
            String canRender = MultiPointHandler.canRenderMultiPoint(symbolCode, modifiers, pts2.size());

            String strGeoJSON = WebRenderer.RenderSymbol("ID", "name", "description", symbolCode, controlPtsStr, altitudeMode , scale, rectStr, modifiers, attributes,WebRenderer.OUTPUT_FORMAT_GEOJSON);
            Log.i(symbolCode, strGeoJSON);

            if (canRender.equals("true")) {
                // drawControlPoints(g2d, mss.getCoordinates(), converter);
                drawShapeInfosGE(g2d, mss.getSymbolShapes(),useDashArray,mss.getSymbolID(),converter);
                drawShapeInfosText(g2d, mss.getModifierShapes(),mss.getTextColor(),converter);
            } else {
                Log.e("Utility", "Cannot Render multipoint: " + canRender);
            }
    }

    private static String getRectString(double deltax, double deltay) {
        String str = "";
        // normalize deltas to start
        deltax = Math.abs(deltax);
        deltay = Math.abs(deltay);
        double deltaLHS = 0, deltaRHS = 0, deltaTop = 0, deltaBottom = 0;
        if (leftLongitude - rightLongitude > 180)// 179 to -179
        {
            deltaLHS = deltax;
            deltaRHS = -deltax;
        } else if (leftLongitude - rightLongitude < -180)// -179 to 179
        {
            deltaLHS = -deltax;
            deltaRHS = deltax;
        } else if (leftLongitude < rightLongitude) {
            deltaLHS = deltax;
            deltaRHS = -deltax;
        } else if (leftLongitude > rightLongitude) {
            deltaLHS = -deltax;
            deltaRHS = deltax;
        }

        if (upperLatitude > lowerLatitude) {
            deltaTop = -deltay;
            deltaBottom = deltay;
        } else {
            deltaTop = deltay;
            deltaBottom = -deltay;
        }

        str += Double.toString(leftLongitude + deltaLHS) + ",";
        str += Double.toString(lowerLatitude + deltaBottom) + ",";
        str += Double.toString(rightLongitude + deltaRHS) + ",";
        str += Double.toString(upperLatitude + deltaTop);
        return str;
    }

    private static void drawControlPoints(Canvas canvas, ArrayList<Point2D> coords, IPointConversion converter) {
        Paint paint = new Paint();
        paint.setColor(Color.MAGENTA.toInt());
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        for (Point2D coord : coords) {
            Point2D pt = converter.GeoToPixels(coord);
            canvas.drawCircle((float) pt.getX(), (float) pt.getY(), 8, paint);
        }
    }

    /**
     * draw the ArrayLists of polylines for the GoogleEarth project tester
     *
     * @param canvas
     * @param l
     */
    private static void drawShapeInfosGE(Canvas canvas, List<ShapeInfo> l, boolean useDashedLines, String symbolId, IPointConversion converter) {
        try {
            Iterator i = l.iterator();
            int j = 0;
            ArrayList<ArrayList<Point2D>> polylines = null;
            ArrayList<Point2D> polyline = null;
            int type = -1;
            Path path = new Path();
            Paint paint = new Paint();
            BasicStroke stroke = null;
            while (i.hasNext()) {
                ShapeInfo spec = (ShapeInfo) i.next();
                polylines = spec.getPolylines();
                type = spec.getShapeType();
                stroke = spec.getStroke();
                if (spec.getFillColor() != null) {
                    paint.setColor(spec.getFillColor().toARGB());
                }

                paint.setStyle(Paint.Style.FILL);
                if (spec.getShader() != null) {
                    paint.setShader(spec.getShader());
                }
                Point2D pt2d=null;
                Point2D pixels=null;
                if (spec.getShader() != null || (spec.getFillColor() != null && spec.getFillColor().getAlpha() > 0)) {
                    int n=polylines.size();
                    for (j = 0; j < n; j++) 
                    {
                        path = new Path();
                        polyline = polylines.get(j);
                        //diagnostic
                        if(converter==null)
                            path.moveTo((int) polyline.get(0).getX(), (int) polyline.get(0).getY());
                        else
                        {
                            pt2d=polyline.get(0);
                            pixels=converter.GeoToPixels(pt2d);
                            path.moveTo((int) pixels.getX(), (int) pixels.getY());
                        }
                        int t=polyline.size();
                        for (int k = 1; k < t; k++) 
                        {
                            if(converter==null)
                                path.lineTo((int) polyline.get(k).getX(), (int) polyline.get(k).getY());
                            else
                            {
                                pt2d=polyline.get(k);
                                pixels=converter.GeoToPixels(pt2d);
                                path.lineTo((int) pixels.getX(), (int) pixels.getY());
                            }
                        }
                        //end diagnostic
                        canvas.drawPath(path, paint);
                    }

                }


                BasicStroke s = spec.getStroke();
                float[] dash = s.getDashArray();
                
                
                if (spec.getLineColor() != null)
                    if(dash==null || useDashedLines==false)
                {
                    paint = new Paint();
                    paint.setColor(spec.getLineColor().toARGB());
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(stroke.getLineWidth());
                    paint.setStrokeCap(Paint.Cap.values()[stroke.getEndCap()]);
                    paint.setStrokeJoin(Paint.Join.values()[stroke.getLineJoin()]);
                    int n=polylines.size();
                    //for (j = 0; j < polylines.size(); j++) 
                    for (j = 0; j < n; j++) 
                    {
                        polyline = polylines.get(j);
                        path = new Path();
                        //diagnostic
                        if(converter==null)
                            path.moveTo((int) polyline.get(0).getX(), (int) polyline.get(0).getY());
                        else
                        {
                            pt2d=polyline.get(0);
                            pixels=converter.GeoToPixels(pt2d);
                            path.moveTo((int) pixels.getX(), (int) pixels.getY());
                        }
                        int t=polyline.size();
                        //for (int k = 1; k < polyline.size(); k++) 
                        for (int k = 1; k < t; k++) 
                        {
                            if(converter==null)
                                path.lineTo((int) polyline.get(k).getX(), (int) polyline.get(k).getY());
                            else
                            {
                                pt2d=polyline.get(k);
                                pixels=converter.GeoToPixels(pt2d);
                                path.lineTo((int) pixels.getX(), (int) pixels.getY());
                            }
                        }
                        //end diagnostic
                        canvas.drawPath(path, paint);
                    }
                }
                if (spec.getLineColor() != null && dash!=null && useDashedLines==true)
                {
                    drawDashedPolylines(polylines,spec,canvas,converter);
                }

                //test for pattern fill image generation////////////////////////////////////////////
                /*if(spec.getPatternFillImage() != null)
                {
                    canvas.drawBitmap(spec.getPatternFillImage(),10,10,null);
                    paint = new Paint();
                    paint.setStyle(Paint.Style.FILL);
                    paint.setShader(spec.getShader());
                    canvas.drawPath(path, paint);
                }///////////////////////////////////////////////////////////////////////////////////*/
            }
        } catch (Exception e) {
            String s = e.getMessage();
            return;
        }
    }

    /**
     *
     * @param g2d
     * @param l
     */
    private static void drawShapeInfosText(Canvas g2d, List<ShapeInfo> l, Color textColor, IPointConversion converter) {
        try {
            Iterator i = l.iterator();
            AffineTransform tx = null;
            Point2D position = null;
            double stringAngle = 0;
            Paint paint = null;
            int size = 0;
            float x = 0, y = 0;
            String str = "";
            while (i.hasNext()) {
                int n = g2d.save();
                ShapeInfo spec = (ShapeInfo) i.next();

                position = spec.getGlyphPosition();
                if (converter != null) {
                    position = spec.getModifierPosition();
                    position = converter.GeoToPixels(position);
                }
                stringAngle = spec.getModifierAngle();
                g2d.rotate((float) stringAngle, (float) position.getX(), (float) position.getY());
                //draw the text twice
                paint = new Paint();

                x = (float) position.getX();
                y = (float) position.getY();

                if(spec.getModifierString() != null) {
                    TextLayout tl = spec.getTextLayout();
                    size = spec.getModifierString() != null ? tl.getBounds().height : spec.getModifierImage().getHeight();

                    paint.setTextAlign(Paint.Align.values()[spec.getTextJustify()]);
                    paint.setStrokeWidth(2);
                    //paint.setColor(Color.WHITE.toARGB());
                    if (textColor != null)
                        paint.setColor(textColor.toARGB());
                    paint.setTextSize(size);
                    paint.setStyle(Paint.Style.FILL_AND_STROKE);
                    str = spec.getModifierString();
                    g2d.drawText(str, x, y, paint);
                    //g2d.drawText(spec.getModifierString(), (float)position.getX(), (float)position.getY(),paint);
                    //g2d.rotate(-(float)stringAngle);
                    g2d.restore();
                    //paint.setTextSize(10);
                }
                else if(spec.getModifierImage() != null){
                    x = x-(spec.getModifierImage().getWidth()/2);
                    //Should center on 'y' for minefields too but i can't tell from here if I have LAA or a mine field
                    //y = y-(spec.getModifierImage().getHeight()/2);
                    g2d.drawBitmap(spec.getModifierImage(), x, y, paint);
                    g2d.restore();
                }
            }//end whilc
        }//end try
        catch (Exception e) {
            Log.e("utility", "Failed to render", e);
            return;
        }
    }

    /**
     * Copy of clsUtilityGE.createDashedPolylines()
     */
    private static void drawDashedPolylines(ArrayList<ArrayList<Point2D>> polylines, ShapeInfo shape, Canvas g2d, IPointConversion converter) {
        try {
            if (shape.getLineColor() == null) {
                return;
            }

            BasicStroke stroke = shape.getStroke();
            float[] dash = stroke.getDashArray();
            if (dash == null || dash.length < 2) {
                return;
            }

            Paint paint = new Paint();
            paint.setColor(shape.getLineColor().toARGB());
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(stroke.getLineWidth());
            paint.setStrokeCap(Paint.Cap.values()[stroke.getEndCap()]);
            paint.setStrokeJoin(Paint.Join.values()[stroke.getLineJoin()]);

            for (ArrayList<Point2D> polyline : polylines) {
                int dashIndex = 0; // Current index in dash array
                double remainingInIndex = dash[dashIndex]; // Length remaining in current dash array index
                for (int i = 0; i < polyline.size() - 1; i++) {
                    Point2D segStartPt = polyline.get(i); // segment start, moves as segment is processed
                    Point2D segEndPt = polyline.get(i + 1); // Segment end
                    if (converter != null) {
                        segStartPt = converter.GeoToPixels(segStartPt);
                        segEndPt = converter.GeoToPixels(segEndPt);
                    }
                    double segLength; // distance remaining in segment
                    while ((segLength = lineutility.CalcDistanceDouble(segStartPt, segEndPt)) > 0) {
                        // If the line segment length is shorter than the current dash then move to the end of the segment continuing to draw or move
                        // Otherwise move to the end of the current dash and start the next dash there
                        if (segLength < remainingInIndex) {
                            if (dashIndex % 2 == 0) {
                                // Continue line
                                g2d.drawLine((float) segStartPt.getX(), (float) segStartPt.getY(), (float) segEndPt.getX(), (float) segEndPt.getY(), paint);
                            }
                            remainingInIndex -= segLength;
                            break; // Next segment
                        } else {
                            // Flip to line or space at dashFlipPoint
                            Point2D dashFlipPoint = lineutility.ExtendAlongLineDouble2(segStartPt, segEndPt, remainingInIndex);
                            if (dashIndex % 2 == 0) {
                                // Continue line
                                g2d.drawLine((float) segStartPt.getX(), (float) segStartPt.getY(), (float) dashFlipPoint.getX(), (float) dashFlipPoint.getY(), paint);
                            }
                            // Next dash
                            dashIndex++;
                            if (dashIndex >= dash.length)
                                dashIndex = 0;
                            remainingInIndex = dash[dashIndex];
                            segStartPt = dashFlipPoint;
                        }
                    }
                }
            }
        } catch (Exception exc) {
            ErrorLogger.LogException("utility", "drawDashedPolylines",
                    new RendererException("Failed inside drawDashedPolylines", exc));
        }
    }
}
