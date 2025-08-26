package armyc2.c5isr.web.render;

import android.graphics.Bitmap;
import android.graphics.Typeface;

import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;

import armyc2.c5isr.JavaLineArray.POINT2;
import armyc2.c5isr.JavaTacticalRenderer.TGLight;
import armyc2.c5isr.RenderMultipoints.clsRenderer;
import armyc2.c5isr.graphics2d.BasicStroke;
import armyc2.c5isr.graphics2d.Point2D;
import armyc2.c5isr.graphics2d.Rectangle;
import armyc2.c5isr.renderer.utilities.Color;
import armyc2.c5isr.renderer.utilities.DrawRules;
import armyc2.c5isr.renderer.utilities.ErrorLogger;
import armyc2.c5isr.renderer.utilities.IPointConversion;
import armyc2.c5isr.renderer.utilities.MSLookup;
import armyc2.c5isr.renderer.utilities.MilStdAttributes;
import armyc2.c5isr.renderer.utilities.MilStdSymbol;
import armyc2.c5isr.renderer.utilities.RendererSettings;
import armyc2.c5isr.renderer.utilities.RendererUtilities;
import armyc2.c5isr.renderer.utilities.ShapeInfo;
import armyc2.c5isr.web.render.utilities.JavaRendererUtilities;

public class Shape3DHandler {


    public static String RenderMilStd3dSymbol(String id,
                                      String name,
                                      String description,
                                      String symbolCode,
                                      String controlPoints,
                                      Double scale,
                                      String bbox,
                                      Map<String,String> symbolModifiers,
                                      Map<String,String> symbolAttributes,
                                      int format)//,
    {
        //System.out.println("MultiPointHandler.RenderSymbol()");
        boolean normalize = true;
        //Double controlLat = 0.0;
        //Double controlLong = 0.0;
        //Double metPerPix = GeoPixelConversion.metersPerPixel(scale);
        //String bbox2=getBoundingRectangle(controlPoints,bbox);
        StringBuilder jsonOutput = new StringBuilder();
        String jsonContent = "";

        Rectangle rect = null;
        String[] coordinates = controlPoints.split(" ");
        TGLight tgl = new TGLight();
        ArrayList<ShapeInfo> shapes = new ArrayList<ShapeInfo>();
        ArrayList<ShapeInfo> modifiers = new ArrayList<ShapeInfo>();
        //ArrayList<Point2D> pixels = new ArrayList<Point2D>();
        ArrayList<Point2D> geoCoords = new ArrayList<Point2D>();
        int len = coordinates.length;
        //diagnostic create geoCoords here
        Point2D coordsUL=null;

        String symbolIsValid = MultiPointHandler.canRenderMultiPoint(symbolCode, symbolModifiers, len);
        if (!symbolIsValid.equals("true")) {
            String ErrorOutput = "";
            ErrorOutput += ("{\"type\":\"error\",\"error\":\"There was an error creating the MilStdSymbol " + symbolCode + " - ID: " + id + " - ");
            ErrorOutput += symbolIsValid; //reason for error
            ErrorOutput += ("\"}");
            ErrorLogger.LogMessage("MultiPointHandler","RenderSymbol",symbolIsValid, Level.WARNING);
            return ErrorOutput;
        }

        if (MSLookup.getInstance().getMSLInfo(symbolCode).getDrawRule() != DrawRules.AREA10) // AREA10 can support infinite points
            len = Math.min(len, MSLookup.getInstance().getMSLInfo(symbolCode).getMaxPointCount());
        for (int i = 0; i < len; i++)
        {
            String[] coordPair = coordinates[i].split(",");
            Double latitude = Double.valueOf(coordPair[1].trim()).doubleValue();
            Double longitude = Double.valueOf(coordPair[0].trim()).doubleValue();
            geoCoords.add(new Point2D.Double(longitude, latitude));
        }
        ArrayList<POINT2> tgPoints = null;
        IPointConversion ipc = null;

        //Deutch moved section 6-29-11
        Double left = 0.0;
        Double right = 0.0;
        Double top = 0.0;
        Double bottom = 0.0;
        Point2D temp = null;
        Point2D ptGeoUL = null;
        int width = 0;
        int height = 0;
        int leftX = 0;
        int topY = 0;
        int bottomY = 0;
        int rightX = 0;
        int j = 0;
        ArrayList<Point2D> bboxCoords = null;
        if (bbox != null && bbox.equals("") == false) {
            String[] bounds = null;
            if (bbox.contains(" "))//trapezoid
            {
                bboxCoords = new ArrayList<Point2D>();
                double x = 0;
                double y = 0;
                String[] coords = bbox.split(" ");
                String[] arrCoord;
                for (String coord : coords) {
                    arrCoord = coord.split(",");
                    x = Double.valueOf(arrCoord[0]);
                    y = Double.valueOf(arrCoord[1]);
                    bboxCoords.add(new Point2D.Double(x, y));
                }
                //use the upper left corner of the MBR containing geoCoords
                //to set the converter
                ptGeoUL = MultiPointHandler.getGeoUL(bboxCoords);
                left = ptGeoUL.getX();
                top = ptGeoUL.getY();
                String bbox2=MultiPointHandler.getBboxFromCoords(bboxCoords);
                scale = MultiPointHandler.getReasonableScale(bbox2, scale);
                ipc = new PointConverter(left, top, scale);
                Point2D ptPixels = null;
                Point2D ptGeo = null;
                int n = bboxCoords.size();
                //for (j = 0; j < bboxCoords.size(); j++)
                for (j = 0; j < n; j++) {
                    ptGeo = bboxCoords.get(j);
                    ptPixels = ipc.GeoToPixels(ptGeo);
                    x = ptPixels.getX();
                    y = ptPixels.getY();
                    if (x < 20) {
                        x = 20;
                    }
                    if (y < 20) {
                        y = 20;
                    }
                    ptPixels.setLocation(x, y);
                    //end section
                    bboxCoords.set(j, (Point2D) ptPixels);
                }
            } else//rectangle
            {
                bounds = bbox.split(",");
                left = Double.valueOf(bounds[0]);
                right = Double.valueOf(bounds[2]);
                top = Double.valueOf(bounds[3]);
                bottom = Double.valueOf(bounds[1]);
                scale = MultiPointHandler.getReasonableScale(bbox, scale);
                ipc = new PointConverter(left, top, scale);
            }

            Point2D pt2d = null;
            if (bboxCoords == null) {
                pt2d = new Point2D.Double(left, top);
                temp = ipc.GeoToPixels(pt2d);

                leftX = (int) temp.getX();
                topY = (int) temp.getY();

                pt2d = new Point2D.Double(right, bottom);
                temp = ipc.GeoToPixels(pt2d);

                bottomY = (int) temp.getY();
                rightX = (int) temp.getX();
                //diagnostic clipping does not work at large scales
//                if(scale>10e6)
//                {
//                    //diagnostic replace above by using a new ipc based on the coordinates MBR
//                    coordsUL=getGeoUL(geoCoords);
//                    temp = ipc.GeoToPixels(coordsUL);
//                    left=coordsUL.getX();
//                    top=coordsUL.getY();
//                    //shift the ipc to coordsUL origin so that conversions will be more accurate for large scales.
//                    ipc = new PointConverter(left, top, scale);
//                    //shift the rect to compenstate for the shifted ipc so that we can maintain the original clipping area.
//                    leftX -= (int)temp.getX();
//                    rightX -= (int)temp.getX();
//                    topY -= (int)temp.getY();
//                    bottomY -= (int)temp.getY();
//                    //end diagnostic
//                }
                //end section

                width = (int) Math.abs(rightX - leftX);
                height = (int) Math.abs(bottomY - topY);

                rect = new Rectangle(leftX, topY, width, height);
            }
        } else {
            rect = null;
        }
        //end section

//        for (int i = 0; i < len; i++) {
//            String[] coordPair = coordinates[i].split(",");
//            Double latitude = Double.valueOf(coordPair[1].trim());
//            Double longitude = Double.valueOf(coordPair[0].trim());
//            geoCoords.add(new Point2D.Double(longitude, latitude));
//        }
        if (ipc == null) {
            Point2D ptCoordsUL = MultiPointHandler.getGeoUL(geoCoords);
            ipc = new PointConverter(ptCoordsUL.getX(), ptCoordsUL.getY(), scale);
        }
        //if (crossesIDL(geoCoords) == true)
//        if(Math.abs(right-left)>180)
//        {
//            normalize = true;
//            ((PointConverter)ipc).set_normalize(true);
//        }
//        else {
//            normalize = false;
//            ((PointConverter)ipc).set_normalize(false);
//        }

        //seems to work ok at world view
//        if (normalize) {
//            NormalizeGECoordsToGEExtents(0, 360, geoCoords);
//        }

        //M. Deutch 10-3-11
        //must shift the rect pixels to synch with the new ipc
        //the old ipc was in synch with the bbox, so rect x,y was always 0,0
        //the new ipc synchs with the upper left of the geocoords so the boox is shifted
        //and therefore the clipping rectangle must shift by the delta x,y between
        //the upper left corner of the original bbox and the upper left corner of the geocoords
        ArrayList<Point2D> geoCoords2 = new ArrayList<Point2D>();
        geoCoords2.add(new Point2D.Double(left, top));
        geoCoords2.add(new Point2D.Double(right, bottom));

//        if (normalize) {
//            NormalizeGECoordsToGEExtents(0, 360, geoCoords2);
//        }

        //disable clipping
        if (MultiPointHandler.ShouldClipSymbol(symbolCode) == false)
            if(MultiPointHandler.crossesIDL(geoCoords)==false)
            {
                rect = null;
                bboxCoords = null;
            }

        tgl.set_SymbolId(symbolCode);// "GFGPSLA---****X" AMBUSH symbol code
        tgl.set_Pixels(null);

        try {

            //String fillColor = null;
            MilStdSymbol mSymbol = new MilStdSymbol(symbolCode, null, geoCoords, null);

            if (format == WebRenderer.OUTPUT_FORMAT_GEOSVG){
                // Use dash array and hatch pattern fill for SVG output
                symbolAttributes.put(MilStdAttributes.UseDashArray, "true");
                symbolAttributes.put(MilStdAttributes.UsePatternFill, "true");
            }

            if (symbolModifiers != null || symbolAttributes != null) {
                MultiPointHandler.populateModifiers(symbolModifiers, symbolAttributes, mSymbol);
            } else {
                mSymbol.setFillColor(null);
            }

            if (bboxCoords == null) {
                clsRenderer.renderWithPolylines(mSymbol, ipc, rect);
            } else {
                clsRenderer.renderWithPolylines(mSymbol, ipc, bboxCoords);
            }

            shapes = mSymbol.getSymbolShapes();
            modifiers = mSymbol.getModifierShapes();

            if (format == WebRenderer.OUTPUT_FORMAT_JSON) {
                jsonOutput.append("{\"type\":\"symbol\",");
                jsonContent = JSONize(shapes, modifiers, ipc, true, normalize);
                jsonOutput.append(jsonContent);
                jsonOutput.append("}");
            } else if (format == WebRenderer.OUTPUT_FORMAT_KML) {
                Color textColor = mSymbol.getTextColor();
                if(textColor==null)
                    textColor=mSymbol.getLineColor();

                jsonContent = KMLize(id, name, description, symbolCode, shapes, modifiers, ipc, normalize, textColor);
                jsonOutput.append(jsonContent);
            } else if (format == WebRenderer.OUTPUT_FORMAT_GEOJSON)
            {
                jsonOutput.append("{\"type\":\"FeatureCollection\",\"features\":");
                jsonContent = GeoJSONize(shapes, modifiers, ipc, normalize, mSymbol.getTextColor(), mSymbol.getTextBackgroundColor());
                jsonOutput.append(jsonContent);

                //moving meta data properties to the last feature with no coords as feature collection doesn't allow properties
                jsonOutput.replace(jsonOutput.toString().length()-1,jsonOutput.toString().length(),"" );
                if (jsonContent.length() > 2)
                    jsonOutput.append(",");
                jsonOutput.append("{\"type\": \"Feature\",\"geometry\": { \"type\": \"Polygon\",\"coordinates\": [ ]}");

                jsonOutput.append(",\"properties\":{\"id\":\"");
                jsonOutput.append(id);
                jsonOutput.append("\",\"name\":\"");
                jsonOutput.append(name);
                jsonOutput.append("\",\"description\":\"");
                jsonOutput.append(description);
                jsonOutput.append("\",\"symbolID\":\"");
                jsonOutput.append(symbolCode);
                jsonOutput.append("\",\"wasClipped\":\"");
                jsonOutput.append(String.valueOf(mSymbol.get_WasClipped()));
                //jsonOutput.append("\"}}");

                jsonOutput.append("\"}}]}");
            } else if (format == WebRenderer.OUTPUT_FORMAT_GEOSVG) {
                String textColor = mSymbol.getTextColor() != null ? RendererUtilities.colorToHexString(mSymbol.getTextColor(), false) : "";
                String backgroundColor = mSymbol.getTextBackgroundColor() != null ? RendererUtilities.colorToHexString(mSymbol.getTextBackgroundColor(), false) : "";
                //returns an svg with a geoTL and geoBR value to use to place the canvas on the map
                jsonContent = MultiPointHandlerSVG.GeoSVGize(id, name, description, symbolCode, shapes, modifiers, ipc, normalize, textColor, backgroundColor, mSymbol.get_WasClipped());
                jsonOutput.append(jsonContent);
            }
        } catch (Exception exc) {
            String st = JavaRendererUtilities.getStackTrace(exc);
            jsonOutput = new StringBuilder();
            jsonOutput.append("{\"type\":\"error\",\"error\":\"There was an error creating the MilStdSymbol " + symbolCode + ": " + "- ");
            jsonOutput.append(exc.getMessage() + " - ");
            jsonOutput.append(st);
            jsonOutput.append("\"}");

            ErrorLogger.LogException("MultiPointHandler", "RenderSymbol", exc);
        }

        boolean debug = false;
        if (debug == true) {
            System.out.println("Symbol Code: " + symbolCode);
            System.out.println("Scale: " + scale);
            System.out.println("BBOX: " + bbox);
            if (controlPoints != null) {
                System.out.println("Geo Points: " + controlPoints);
            }
            if (tgl != null && tgl.get_Pixels() != null)//pixels != null
            {
                System.out.println("Pixel: " + tgl.get_Pixels().toString());
            }
            if (bbox != null) {
                System.out.println("geo bounds: " + bbox);
            }
            if (rect != null) {
                System.out.println("pixel bounds: " + rect.toString());
            }
            if (jsonOutput != null) {
                System.out.println(jsonOutput.toString());
            }
        }

        ErrorLogger.LogMessage("MultiPointHandler", "RenderSymbol()", "exit RenderSymbol", Level.FINER);
        return jsonOutput.toString();

    }


    private static String KMLize(String id, String name,
                                 String description,
                                 String symbolCode,
                                 ArrayList<ShapeInfo> shapes,
                                 ArrayList<ShapeInfo> modifiers,
                                 IPointConversion ipc,
                                 boolean normalize, Color textColor) {

        java.lang.StringBuilder kml = new java.lang.StringBuilder();

        ShapeInfo tempModifier = null;

        String cdataStart = "<![CDATA[";
        String cdataEnd = "]]>";

        int len = shapes.size();
        kml.append("<Folder id=\"" + id + "\">");
        kml.append("<name>" + cdataStart + name + cdataEnd + "</name>");
        kml.append("<visibility>1</visibility>");
        for (int i = 0; i < len; i++) {

            String shapesToAdd = ShapeToKMLString(name, description, symbolCode, shapes.get(i), ipc, normalize);
            kml.append(shapesToAdd);
        }

        int len2 = modifiers.size();

        for (int j = 0; j < len2; j++) {

            tempModifier = modifiers.get(j);

            //if(geMap)//if using google earth
            //assume kml text is going to be centered
            //AdjustModifierPointToCenter(tempModifier);

            String labelsToAdd = LabelToKMLString(tempModifier, ipc, normalize, textColor);
            kml.append(labelsToAdd);
        }

        kml.append("</Folder>");
        return kml.toString();
    }
    private static String ShapeToKMLString(String name,
                                           String description,
                                           String symbolCode,
                                           ShapeInfo shapeInfo,
                                           IPointConversion ipc,
                                           boolean normalize) {

        java.lang.StringBuilder kml = new java.lang.StringBuilder();

        Color lineColor = null;
        Color fillColor = null;
        String googleLineColor = null;
        String googleFillColor = null;

        //String lineStyleId = "lineColor";

        BasicStroke stroke = null;
        int lineWidth = 4;

        symbolCode = JavaRendererUtilities.normalizeSymbolCode(symbolCode);

        String cdataStart = "<![CDATA[";
        String cdataEnd = "]]>";

        kml.append("<Placemark>");//("<Placemark id=\"" + id + "_mg" + "\">");
        kml.append("<description>" + cdataStart + "<b>" + name + "</b><br/>" + "\n" + description + cdataEnd + "</description>");
        //kml.append("<Style id=\"" + lineStyleId + "\">");
        kml.append("<Style>");

        lineColor = shapeInfo.getLineColor();
        if (lineColor != null) {
            googleLineColor = Integer.toHexString(shapeInfo.getLineColor().toARGB());

            stroke = shapeInfo.getStroke();

            if (stroke != null) {
                lineWidth = (int) stroke.getLineWidth();
            }

            googleLineColor = JavaRendererUtilities.ARGBtoABGR(googleLineColor);

            kml.append("<LineStyle>");
            kml.append("<color>" + googleLineColor + "</color>");
            kml.append("<colorMode>normal</colorMode>");
            kml.append("<width>" + String.valueOf(lineWidth) + "</width>");
            kml.append("</LineStyle>");
        }

        fillColor = shapeInfo.getFillColor();
        Bitmap fillPattern = shapeInfo.getPatternFillImage();
        if (fillColor != null || fillPattern != null) {
            kml.append("<PolyStyle>");

            if (fillColor != null) {
                googleFillColor = Integer.toHexString(shapeInfo.getFillColor().toARGB());
                googleFillColor = JavaRendererUtilities.ARGBtoABGR(googleFillColor);
                kml.append("<color>" + googleFillColor + "</color>");
                kml.append("<colorMode>normal</colorMode>");
            }
            if (fillPattern != null){
                kml.append("<shader>" + MultiPointHandler.bitmapToString(fillPattern) + "</shader>");
            }

            kml.append("<fill>1</fill>");
            if (lineColor != null) {
                kml.append("<outline>1</outline>");
            } else {
                kml.append("<outline>0</outline>");
            }
            kml.append("</PolyStyle>");
        }

        kml.append("</Style>");

        ArrayList shapesArray = shapeInfo.getPolylines();
        int len = shapesArray.size();
        kml.append("<MultiGeometry>");

        for (int i = 0; i < len; i++) {
            ArrayList shape = (ArrayList) shapesArray.get(i);
            normalize = MultiPointHandler.normalizePoints(shape, ipc);
            if (lineColor != null && fillColor == null) {
                kml.append("<LineString>");
                kml.append("<tessellate>1</tessellate>");
                kml.append("<altitudeMode>clampToGround</altitudeMode>");
                kml.append("<coordinates>");
                int n = shape.size();
                //for (int j = 0; j < shape.size(); j++)
                for (int j = 0; j < n; j++) {
                    Point2D coord = (Point2D) shape.get(j);
                    Point2D geoCoord = ipc.PixelsToGeo(coord);
                    if (normalize) {
                        geoCoord = MultiPointHandler.NormalizeCoordToGECoord(geoCoord);
                    }

                    double latitude = Math.round(geoCoord.getY() * 100000000.0) / 100000000.0;
                    double longitude = Math.round(geoCoord.getX() * 100000000.0) / 100000000.0;

                    kml.append(longitude);
                    kml.append(",");
                    kml.append(latitude);
                    if(j<shape.size()-1)
                        kml.append(" ");
                }

                kml.append("</coordinates>");
                kml.append("</LineString>");
            }

            if (fillColor != null) {

                if (i == 0) {
                    kml.append("<Polygon>");
                }
                //kml.append("<outerBoundaryIs>");
                if (i == 1 && len > 1) {
                    kml.append("<innerBoundaryIs>");
                } else {
                    kml.append("<outerBoundaryIs>");
                }
                kml.append("<LinearRing>");
                kml.append("<altitudeMode>clampToGround</altitudeMode>");
                kml.append("<tessellate>1</tessellate>");
                kml.append("<coordinates>");

                //this section is a workaround for a google earth bug. Issue 417 was closed
                //for linestrings but they did not fix the smae issue for fills. If Google fixes the issue
                //for fills then this section will need to be commented or it will induce an error.
                double lastLongitude = Double.MIN_VALUE;
                if (normalize == false && MultiPointHandler.IsOnePointSymbolCode(symbolCode)) {
                    int n = shape.size();
                    //for (int j = 0; j < shape.size(); j++)
                    for (int j = 0; j < n; j++) {
                        Point2D coord = (Point2D) shape.get(j);
                        Point2D geoCoord = ipc.PixelsToGeo(coord);
                        double longitude = geoCoord.getX();
                        if (lastLongitude != Double.MIN_VALUE) {
                            if (Math.abs(longitude - lastLongitude) > 180d) {
                                normalize = true;
                                break;
                            }
                        }
                        lastLongitude = longitude;
                    }
                }
                int n = shape.size();
                //for (int j = 0; j < shape.size(); j++)
                for (int j = 0; j < n; j++) {
                    Point2D coord = (Point2D) shape.get(j);
                    Point2D geoCoord = ipc.PixelsToGeo(coord);

                    double latitude = Math.round(geoCoord.getY() * 100000000.0) / 100000000.0;
                    double longitude = Math.round(geoCoord.getX() * 100000000.0) / 100000000.0;

                    //fix for fill crossing DTL
                    if (normalize) {
                        if (longitude > 0) {
                            longitude -= 360;
                        }
                    }

                    kml.append(longitude);
                    kml.append(",");
                    kml.append(latitude);
                    if(j<shape.size()-1)
                        kml.append(" ");
                }

                kml.append("</coordinates>");
                kml.append("</LinearRing>");
                if (i == 1 && len > 1) {
                    kml.append("</innerBoundaryIs>");
                } else {
                    kml.append("</outerBoundaryIs>");
                }
                if (i == len - 1) {
                    kml.append("</Polygon>");
                }
            }
        }

        kml.append("</MultiGeometry>");
        kml.append("</Placemark>");

        return kml.toString();
    }

    private static String LabelToKMLString(ShapeInfo shapeInfo, IPointConversion ipc, boolean normalize, Color textColor) {
        java.lang.StringBuilder kml = new java.lang.StringBuilder();

        //Point2D coord = (Point2D) new Point2D.Double(shapeInfo.getGlyphPosition().getX(), shapeInfo.getGlyphPosition().getY());
        Point2D coord = (Point2D) new Point2D.Double(shapeInfo.getModifierPosition().getX(), shapeInfo.getModifierPosition().getY());
        Point2D geoCoord = ipc.PixelsToGeo(coord);
        //M. Deutch 9-26-11
        if (normalize) {
            geoCoord = MultiPointHandler.NormalizeCoordToGECoord(geoCoord);
        }
        double latitude = Math.round(geoCoord.getY() * 100000000.0) / 100000000.0;
        double longitude = Math.round(geoCoord.getX() * 100000000.0) / 100000000.0;
        long angle = Math.round(shapeInfo.getModifierAngle());

        String text = shapeInfo.getModifierString();

        String cdataStart = "<![CDATA[";
        String cdataEnd = "]]>";

        String color = Integer.toHexString(textColor.toARGB());
        color = JavaRendererUtilities.ARGBtoABGR(color);
        float kmlScale = RendererSettings.getInstance().getKMLLabelScale();

        if (kmlScale > 0 && text != null && text.equals("") == false) {
            kml.append("<Placemark>");//("<Placemark id=\"" + id + "_lp" + i + "\">");
            kml.append("<name>" + cdataStart + text + cdataEnd + "</name>");
            kml.append("<Style>");
            kml.append("<IconStyle>");
            kml.append("<scale>" + kmlScale + "</scale>");
            kml.append("<heading>" + angle + "</heading>");
            kml.append("<Icon>");
            kml.append("<href></href>");
            kml.append("</Icon>");
            kml.append("</IconStyle>");
            kml.append("<LabelStyle>");
            kml.append("<color>" + color + "</color>");
            kml.append("<scale>" + String.valueOf(kmlScale) +"</scale>");
            kml.append("</LabelStyle>");
            kml.append("</Style>");
            kml.append("<Point>");
            kml.append("<extrude>1</extrude>");
            kml.append("<altitudeMode>relativeToGround</altitudeMode>");
            kml.append("<coordinates>");
            kml.append(longitude);
            kml.append(",");
            kml.append(latitude);
            kml.append("</coordinates>");
            kml.append("</Point>");
            kml.append("</Placemark>");
        } else {
            return "";
        }

        return kml.toString();
    }


    private static String GeoJSONize(ArrayList<ShapeInfo> shapes, ArrayList<ShapeInfo> modifiers, IPointConversion ipc, boolean normalize, Color textColor, Color textBackgroundColor) {

        String jstr = "";
        ShapeInfo tempModifier = null;
        StringBuilder fc = new StringBuilder();//JSON feature collection

        fc.append("[");

        int len = shapes.size();
        for (int i = 0; i < len; i++) {

            String shapesToAdd = ShapeToGeoJSONString(shapes.get(i), ipc, normalize);
            if (shapesToAdd.length() > 0) {
                fc.append(shapesToAdd);
                if (i < len - 1) {
                    fc.append(",");
                }
            }
        }

        int len2 = modifiers.size();

        for (int j = 0; j < len2; j++) {
            tempModifier = modifiers.get(j);

            String modifiersToAdd = null;
            if(modifiers.get(j).getModifierImage() != null) {
                modifiersToAdd = ImageToGeoJSONString(tempModifier, ipc, normalize);
            } else {
                modifiersToAdd = LabelToGeoJSONString(tempModifier, ipc, normalize, textColor, textBackgroundColor);
            }
            if (modifiersToAdd.length() > 0) {
                if (fc.length() > 1)
                    fc.append(",");
                fc.append(modifiersToAdd);
            }
        }
        fc.append("]");
        String GeoJSON = fc.toString();
        return GeoJSON;
    }

    private static String ShapeToGeoJSONString(ShapeInfo shapeInfo, IPointConversion ipc, boolean normalize) {
        StringBuilder JSONed = new StringBuilder();
        StringBuilder properties = new StringBuilder();
        StringBuilder geometry = new StringBuilder();
        String geometryType = null;
        String sda = null;
        /*
         NOTE: Google Earth / KML colors are backwards.
         They are ordered Alpha,Blue,Green,Red, not Red,Green,Blue,Aplha like the rest of the world
         * */
        Color lineColor = shapeInfo.getLineColor();
        Color fillColor = shapeInfo.getFillColor();

        if (shapeInfo.getShapeType() == ShapeInfo.SHAPE_TYPE_FILL || fillColor != null || shapeInfo.getPatternFillImage() != null) {
            geometryType = "\"Polygon\"";
        } else //if(shapeInfo.getShapeType() == ShapeInfo.SHAPE_TYPE_POLYLINE)
        {
            geometryType = "\"MultiLineString\"";
        }

        BasicStroke stroke = null;
        stroke = shapeInfo.getStroke();
        int lineWidth = 4;

        if (stroke != null) {
            lineWidth = (int) stroke.getLineWidth();
            //lineWidth++;
            //System.out.println("lineWidth: " + String.valueOf(lineWidth));
        }

        //generate JSON properties for feature
        properties.append("\"properties\":{");
        properties.append("\"label\":\"\",");
        if (lineColor != null) {
            properties.append("\"strokeColor\":\"" + RendererUtilities.colorToHexString(lineColor, false) + "\",");
            properties.append("\"lineOpacity\":" + String.valueOf(lineColor.getAlpha() / 255f) + ",");
        }
        if (fillColor != null) {
            properties.append("\"fillColor\":\"" + RendererUtilities.colorToHexString(fillColor, false) + "\",");
            properties.append("\"fillOpacity\":" + String.valueOf(fillColor.getAlpha() / 255f) + ",");
        }
        if (shapeInfo.getPatternFillImage() != null) {
            properties.append("\"fillPattern\":\"" + MultiPointHandler.bitmapToString(shapeInfo.getPatternFillImage()) + "\",");
        }
        if(stroke.getDashArray() != null)
        {
            float[] arrSDA = stroke.getDashArray();
            sda = "[";
            sda += String.valueOf(arrSDA[0]);
            if(arrSDA.length > 1)
            {
                for(int i = 1; i < arrSDA.length; i++)
                {
                    sda = sda + ", " + String.valueOf(arrSDA[i]);
                }
            }
            sda += "]";
            sda = "\"strokeDasharray\":" + sda + ",";
            properties.append(sda);
        }

        int lineCap = stroke.getEndCap();
        properties.append("\"lineCap\":" + lineCap + ",");

        String strokeWidth = String.valueOf(lineWidth);
        properties.append("\"strokeWidth\":" + strokeWidth + ",");
        properties.append("\"strokeWeight\":" + strokeWidth + "");
        properties.append("},");


        properties.append("\"style\":{");
        if (lineColor != null) {
            properties.append("\"stroke\":\"" + RendererUtilities.colorToHexString(lineColor, false) + "\",");
            properties.append("\"line-opacity\":" + String.valueOf(lineColor.getAlpha() / 255f) + ",");
        }
        if (fillColor != null) {
            properties.append("\"fill\":\"" + RendererUtilities.colorToHexString(fillColor, false) + "\",");
            properties.append("\"fill-opacity\":" + String.valueOf(fillColor.getAlpha() / 255f) + ",");
        }
        if(stroke.getDashArray() != null)
        {
            float[] da = stroke.getDashArray();
            sda = String.valueOf(da[0]);
            if(da.length > 1)
            {
                for(int i = 1; i < da.length; i++)
                {
                    sda = sda + " " + String.valueOf(da[i]);
                }
            }
            sda = "\"stroke-dasharray\":\"" + sda + "\",";
            properties.append(sda);
            sda = null;
        }

        if(lineCap == BasicStroke.CAP_SQUARE)
            properties.append("\"stroke-linecap\":\"square\",");
        else if(lineCap == BasicStroke.CAP_ROUND)
            properties.append("\"stroke-linecap\":\"round\",");
        else if(lineCap == BasicStroke.CAP_BUTT)
            properties.append("\"stroke-linecap\":\"butt\",");

        strokeWidth = String.valueOf(lineWidth);
        properties.append("\"stroke-width\":" + strokeWidth);
        properties.append("}");


        //generate JSON geometry for feature
        geometry.append("\"geometry\":{\"type\":");
        geometry.append(geometryType);
        geometry.append(",\"coordinates\":[");

        ArrayList shapesArray = shapeInfo.getPolylines();

        for (int i = 0; i < shapesArray.size(); i++) {
            ArrayList pointList = (ArrayList) shapesArray.get(i);

            normalize = MultiPointHandler.normalizePoints(pointList, ipc);

            geometry.append("[");

            //System.out.println("Pixel Coords:");
            for (int j = 0; j < pointList.size(); j++) {
                Point2D coord = (Point2D) pointList.get(j);
                Point2D geoCoord = ipc.PixelsToGeo(coord);
                //M. Deutch 9-27-11
                if (normalize) {
                    geoCoord = MultiPointHandler.NormalizeCoordToGECoord(geoCoord);
                }
                double latitude = Math.round(geoCoord.getY() * 100000000.0) / 100000000.0;
                double longitude = Math.round(geoCoord.getX() * 100000000.0) / 100000000.0;

                //fix for fill crossing DTL
                if (normalize && fillColor != null) {
                    if (longitude > 0) {
                        longitude -= 360;
                    }
                }

                //diagnostic M. Deutch 10-18-11
                //set the point as geo so that the
                //coord.setLocation(longitude, latitude);
                coord = new Point2D.Double(longitude, latitude);
                pointList.set(j, coord);
                //end section

                geometry.append("[");
                geometry.append(longitude);
                geometry.append(",");
                geometry.append(latitude);
                geometry.append("]");

                if (j < (pointList.size() - 1)) {
                    geometry.append(",");
                }
            }

            geometry.append("]");

            if (i < (shapesArray.size() - 1)) {
                geometry.append(",");
            }
        }
        geometry.append("]}");

        JSONed.append("{\"type\":\"Feature\",");
        JSONed.append(properties.toString());
        JSONed.append(",");
        JSONed.append(geometry.toString());
        JSONed.append("}");

        return JSONed.toString();
    }

    private static String ImageToGeoJSONString(ShapeInfo shapeInfo, IPointConversion ipc, boolean normalize) {

        StringBuilder JSONed = new StringBuilder();
        StringBuilder properties = new StringBuilder();
        StringBuilder geometry = new StringBuilder();

        //AffineTransform at = shapeInfo.getAffineTransform();
        //Point2D coord = (Point2D)new Point2D.Double(at.getTranslateX(), at.getTranslateY());
        //Point2D coord = (Point2D) new Point2D.Double(shapeInfo.getGlyphPosition().getX(), shapeInfo.getGlyphPosition().getY());
        Point2D coord = (Point2D) new Point2D.Double(shapeInfo.getModifierPosition().getX(), shapeInfo.getModifierPosition().getY());
        Point2D geoCoord = ipc.PixelsToGeo(coord);
        //M. Deutch 9-27-11
        if (normalize) {
            geoCoord = MultiPointHandler.NormalizeCoordToGECoord(geoCoord);
        }
        double latitude = Math.round(geoCoord.getY() * 100000000.0) / 100000000.0;
        double longitude = Math.round(geoCoord.getX() * 100000000.0) / 100000000.0;
        double angle = shapeInfo.getModifierAngle();
        coord.setLocation(longitude, latitude);

        //diagnostic M. Deutch 10-18-11
        shapeInfo.setGlyphPosition(coord);

        Bitmap image = shapeInfo.getModifierImage();

        RendererSettings RS = RendererSettings.getInstance();

        if (image != null) {

            JSONed.append("{\"type\":\"Feature\",\"properties\":{\"image\":\"");
            JSONed.append(MultiPointHandler.bitmapToString(image));
            JSONed.append("\",\"rotation\":");
            JSONed.append(angle);
            JSONed.append(",\"angle\":");
            JSONed.append(angle);
            JSONed.append("},");
            JSONed.append("\"geometry\":{\"type\":\"Point\",\"coordinates\":[");
            JSONed.append(longitude);
            JSONed.append(",");
            JSONed.append(latitude);
            JSONed.append("]");
            JSONed.append("}}");

        } else {
            return "";
        }

        return JSONed.toString();
    }

    private static String LabelToGeoJSONString(ShapeInfo shapeInfo, IPointConversion ipc, boolean normalize, Color textColor, Color textBackgroundColor) {

        StringBuilder JSONed = new StringBuilder();
        StringBuilder properties = new StringBuilder();
        StringBuilder geometry = new StringBuilder();

        Color outlineColor = MultiPointHandler.getIdealTextBackgroundColor(textColor);
        if(textBackgroundColor != null)
            outlineColor = textBackgroundColor;

        //AffineTransform at = shapeInfo.getAffineTransform();
        //Point2D coord = (Point2D)new Point2D.Double(at.getTranslateX(), at.getTranslateY());
        //Point2D coord = (Point2D) new Point2D.Double(shapeInfo.getGlyphPosition().getX(), shapeInfo.getGlyphPosition().getY());
        Point2D coord = (Point2D) new Point2D.Double(shapeInfo.getModifierPosition().getX(), shapeInfo.getModifierPosition().getY());
        Point2D geoCoord = ipc.PixelsToGeo(coord);
        //M. Deutch 9-27-11
        if (normalize) {
            geoCoord = MultiPointHandler.NormalizeCoordToGECoord(geoCoord);
        }
        double latitude = Math.round(geoCoord.getY() * 100000000.0) / 100000000.0;
        double longitude = Math.round(geoCoord.getX() * 100000000.0) / 100000000.0;
        double angle = shapeInfo.getModifierAngle();
        coord.setLocation(longitude, latitude);

        //diagnostic M. Deutch 10-18-11
        shapeInfo.setGlyphPosition(coord);

        String text = shapeInfo.getModifierString();

        int justify=shapeInfo.getTextJustify();
        String strJustify="left";
        if(justify==0)
            strJustify="left";
        else if(justify==1)
            strJustify="center";
        else if(justify==2)
            strJustify="right";


        RendererSettings RS = RendererSettings.getInstance();

        if (text != null && text.equals("") == false) {

            JSONed.append("{\"type\":\"Feature\",\"properties\":{\"label\":\"");
            JSONed.append(text);
            JSONed.append("\",\"pointRadius\":0,\"fontColor\":\"");
            JSONed.append(RendererUtilities.colorToHexString(textColor, false));
            JSONed.append("\",\"fontSize\":\"");
            JSONed.append(String.valueOf(RS.getMPLabelFontSize()) + "pt\"");
            JSONed.append(",\"fontFamily\":\"");
            JSONed.append(RS.getMPLabelFontName());
            JSONed.append(", sans-serif");

            if (RS.getMPLabelFontType() == Typeface.BOLD) {
                JSONed.append("\",\"fontWeight\":\"bold\"");
            } else {
                JSONed.append("\",\"fontWeight\":\"normal\"");
            }

            //JSONed.append(",\"labelAlign\":\"lm\"");
            JSONed.append(",\"labelAlign\":\"");
            JSONed.append(strJustify);
            JSONed.append("\",\"labelBaseline\":\"alphabetic\"");
            JSONed.append(",\"labelXOffset\":0");
            JSONed.append(",\"labelYOffset\":0");
            JSONed.append(",\"labelOutlineColor\":\"");
            JSONed.append(RendererUtilities.colorToHexString(outlineColor, false));
            JSONed.append("\",\"labelOutlineWidth\":");
            JSONed.append("4");
            JSONed.append(",\"rotation\":");
            JSONed.append(angle);
            JSONed.append(",\"angle\":");
            JSONed.append(angle);
            JSONed.append("},");

            JSONed.append("\"geometry\":{\"type\":\"Point\",\"coordinates\":[");
            JSONed.append(longitude);
            JSONed.append(",");
            JSONed.append(latitude);
            JSONed.append("]");
            JSONed.append("}}");

        } else {
            return "";
        }

        return JSONed.toString();
    }

}
