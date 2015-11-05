package sec.web.render;

import java.util.ArrayList;

import android.util.Log;
import android.util.SparseArray;
import armyc2.c2sd.graphics2d.*;

import java.util.Map;
import sec.web.render.utilities.JavaRendererUtilities;
import sec.web.render.utilities.LineInfo;
import sec.web.render.utilities.SymbolInfo;
import sec.web.render.utilities.TextInfo;
import armyc2.c2sd.renderer.utilities.IPointConversion;
import armyc2.c2sd.renderer.utilities.MilStdAttributes;
import armyc2.c2sd.renderer.utilities.MilStdSymbol;
import armyc2.c2sd.renderer.utilities.ModifiersTG;
import armyc2.c2sd.renderer.utilities.PointConversion;
import armyc2.c2sd.renderer.utilities.RendererSettings;
import armyc2.c2sd.renderer.utilities.ShapeInfo;
import armyc2.c2sd.renderer.utilities.Color;
import armyc2.c2sd.renderer.utilities.SymbolUtilities;
import armyc2.c2sd.JavaLineArray.POINT2;
import armyc2.c2sd.JavaTacticalRenderer.TGLight;
import armyc2.c2sd.JavaRendererServer.RenderMultipoints.clsRenderer;
import armyc2.c2sd.JavaRendererServer.RenderMultipoints.clsClipPolygon2;
import armyc2.c2sd.JavaTacticalRenderer.mdlGeodesic;
import java.util.LinkedList;
import java.util.List;
import armyc2.c2sd.renderer.utilities.SymbolDef;
import armyc2.c2sd.renderer.utilities.SymbolDefTable;
import armyc2.c2sd.renderer.utilities.ErrorLogger;
import armyc2.c2sd.renderer.utilities.RendererException;
import java.util.logging.Level;
import armyc2.c2sd.renderer.MilStdIconRenderer;
//import java.awt.font.TextAttribute;
//import java.awt.font.NumericShaper;
import android.graphics.Typeface;
import armyc2.c2sd.renderer.utilities.RendererUtilities;
import armyc2.c2sd.graphics2d.*;

@SuppressWarnings({"unused", "rawtypes", "unchecked"})
public class MultiPointHandler {

    private final static int SYMBOL_FILL_IDS = 90;
    private final static int SYMBOL_LINE_IDS = 91;
    private final static int SYMBOL_FILL_ICON_SIZE = 92;
    /**
     * 2525Bch2 and USAS 13/14 symbology
     */
    private static final int _maxPixelWidth = 1000;
    private static final int _minPixelWidth = 100;

    public static final int Symbology_2525Bch2_USAS_13_14 = 0;
    //private final static String TEXT_COLOR = "textColor";
    //private final static String TEXT_BACKGROUND_COLOR = "textBackgroundColor";
    /**
     * 2525C, which includes 2525Bch2 & USAS 13/14
     */
    public static final int Symbology_2525C = 1;

    public static String getModififerKML(String id,
            String name,
            String description,
            String symbolCode,
            String controlPoints,
            Double scale,
            String bbox,
            SparseArray symbolModifiers,
            SparseArray symbolAttributes,
            int format, int symStd) {
        String output = "";
        List<String> placemarks = new LinkedList<String>();

        try {
            double maxAlt = 0;
            double minAlt = 0;

            output = RenderSymbol(id, name, description, symbolCode, controlPoints, scale, bbox, symbolModifiers, symbolAttributes, format, symStd);
            int pmiStart = output.indexOf("<Placemark");
            int pmiEnd = 0;
            int curr = 0;
            int count = 0;
            String tempPlacemark = "";
            while (pmiStart > 0) {
                if (count > 0) {
                    pmiEnd = output.indexOf("</Placemark>", pmiStart) + 12;
                    tempPlacemark = output.substring(pmiStart, pmiEnd);

                    if (tempPlacemark.contains("<Point>")) {
                        placemarks.add(output.substring(pmiStart, pmiEnd));
                    }
                    //System.out.println(placemarks.get(count));
                    //end, check for more
                    pmiStart = output.indexOf("<Placemark", pmiEnd - 2);
                }
                count++;
            }
            java.lang.StringBuilder sb = new java.lang.StringBuilder();
            for (String pm : placemarks) {
                sb.append(pm);
            }
            return sb.toString();
        } catch (Exception exc) {

        }

        return output;
    }

    /**
     * GE has the unusual distinction of being an application with coordinates
     * outside its own extents. It appears to only be a problem when lines cross
     * the IDL
     *
     * @param pts2d the client points
     */
    public static void NormalizeGECoordsToGEExtents(double leftLongitude,
            double rightLongitude,
            ArrayList<Point2D> pts2d) {
        try {
            int j = 0;
            double x = 0, y = 0;
            Point2D pt2d = null;
            int n = pts2d.size();
            //for (j = 0; j < pts2d.size(); j++) 
            for (j = 0; j < n; j++) {
                pt2d = pts2d.get(j);
                x = pt2d.getX();
                y = pt2d.getY();
                while (x < leftLongitude) {
                    x += 360;
                }
                while (x > rightLongitude) {
                    x -= 360;
                }

                pt2d = new Point2D.Double(x, y);
                pts2d.set(j, pt2d);
            }
        } catch (Exception exc) {
        }
    }

    /**
     * GE recognizes coordinates in the range of -180 to +180
     *
     * @param pt2d
     * @return
     */
    private static Point2D NormalizeCoordToGECoord(Point2D pt2d) {
        Point2D ptGeo = null;
        try {
            double x = pt2d.getX(), y = pt2d.getY();
            while (x < -180) {
                x += 360;
            }
            while (x > 180) {
                x -= 360;
            }

            ptGeo = new Point2D.Double(x, y);
        } catch (Exception exc) {
        }
        return ptGeo;
    }

    /**
     * We have to ensure the bounding rectangle at least includes the symbol or
     * there are problems rendering, especially when the symbol crosses the IDL
     *
     * @param controlPoints the client symbol anchor points
     * @param bbox the original bounding box
     * @return the modified bounding box
     */
    private static String getBoundingRectangle(String controlPoints,
            String bbox) {
        String bbox2 = "";
        try {
            //first get the minimum bounding rect for the geo coords
            Double left = 0.0;
            Double right = 0.0;
            Double top = 0.0;
            Double bottom = 0.0;

            String[] coordinates = controlPoints.split(" ");
            int len = coordinates.length;
            int i = 0;
            left = Double.MAX_VALUE;
            right = -Double.MAX_VALUE;
            top = -Double.MAX_VALUE;
            bottom = Double.MAX_VALUE;
            for (i = 0; i < len; i++) {
                String[] coordPair = coordinates[i].split(",");
                Double latitude = Double.valueOf(coordPair[1].trim());
                Double longitude = Double.valueOf(coordPair[0].trim());
                if (longitude < left) {
                    left = longitude;
                }
                if (longitude > right) {
                    right = longitude;
                }
                if (latitude > top) {
                    top = latitude;
                }
                if (latitude < bottom) {
                    bottom = latitude;
                }
            }
            bbox2 = left.toString() + "," + bottom.toString() + "," + right.toString() + "," + top.toString();
        } catch (Exception ex) {
            System.out.println("Failed to create bounding rectangle in MultiPointHandler.getBoundingRect");
        }
        return bbox2;
    }

    /**
     * need to use the symbol to get the upper left control point in order to
     * produce a valid PointConverter
     *
     * @param geoCoords
     * @return
     */
    private static Point2D getControlPoint(ArrayList<Point2D> geoCoords) {
        Point2D pt2d = null;
        try {
            double left = Double.MAX_VALUE;
            double right = -Double.MAX_VALUE;
            double top = -Double.MAX_VALUE;
            double bottom = Double.MAX_VALUE;
            Point2D ptTemp = null;
            int n = geoCoords.size();
            //for (int j = 0; j < geoCoords.size(); j++) 
            for (int j = 0; j < n; j++) {
                ptTemp = geoCoords.get(j);
                if (ptTemp.getX() < left) {
                    left = ptTemp.getX();
                }
                if (ptTemp.getX() > right) {
                    right = ptTemp.getX();
                }
                if (ptTemp.getY() > top) {
                    top = ptTemp.getY();
                }
                if (ptTemp.getY() < bottom) {
                    bottom = ptTemp.getY();
                }
            }
            pt2d = new Point2D.Double(left, top);
        } catch (Exception ex) {
            System.out.println("Failed to create control point in MultiPointHandler.getControlPoint");
        }
        return pt2d;
    }

    /**
     * Assumes a reference in which the north pole is on top.
     *
     * @param geoCoords the geographic coordinates
     * @return the upper left corner of the MBR containing the geographic
     * coordinates
     */
    private static Point2D getGeoUL(ArrayList<Point2D> geoCoords) {
        Point2D ptGeo = null;
        try {
            int j = 0;
            Point2D pt = null;
            double left = geoCoords.get(0).getX();
            double top = geoCoords.get(0).getY();
            double right = geoCoords.get(0).getX();
            double bottom = geoCoords.get(0).getY();
            int n = geoCoords.size();
            //for (j = 1; j < geoCoords.size(); j++) 
            for (j = 1; j < n; j++) {
                pt = geoCoords.get(j);
                if (pt.getX() < left) {
                    left = pt.getX();
                }
                if (pt.getX() > right) {
                    right = pt.getX();
                }
                if (pt.getY() > top) {
                    top = pt.getY();
                }
                if (pt.getY() < bottom) {
                    bottom = pt.getY();
                }
            }
            //if geoCoords crosses the IDL
            if (right - left > 180) {
                //There must be at least one x value on either side of +/-180. Also, there is at least
                //one positive value to the left of +/-180 and negative x value to the right of +/-180.
                //We are using the orientation with the north pole on top so we can keep
                //the existing value for top. Then the left value will be the least positive x value
                left = geoCoords.get(0).getX();
                //for (j = 1; j < geoCoords.size(); j++) 
                n = geoCoords.size();
                for (j = 1; j < n; j++) {
                    pt = geoCoords.get(j);
                    if (pt.getX() > 0 && pt.getX() < left) {
                        left = pt.getX();
                    }
                }
            }
            ptGeo = new Point2D.Double(left, top);
        } catch (Exception ex) {
            System.out.println("Failed to create control point in MultiPointHandler.getControlPoint");
        }
        return ptGeo;
    }

    private static boolean crossesIDL(ArrayList<Point2D> geoCoords) {
        boolean result = false;
        Point2D pt2d = getControlPoint(geoCoords);
        double left = pt2d.getX();
        Point2D ptTemp = null;
        int n = geoCoords.size();
        //for (int j = 0; j < geoCoords.size(); j++) 
        for (int j = 0; j < n; j++) {
            ptTemp = geoCoords.get(j);
            if (Math.abs(ptTemp.getX() - left) > 180) {
                return true;
            }
        }
        return result;
    }

    /**
     * Checks if a symbol is one with decorated lines which puts a strain on
     * google earth when rendering like FLOT. These complicated lines should be
     * clipped when possible.
     *
     * @param symbolID
     * @return
     */
    public static Boolean ShouldClipSymbol(String symbolID) {
        String affiliation = SymbolUtilities.getStatus(symbolID);

        if (symbolID.substring(0, 1).equals("G") && affiliation.equals("A")) {
            return true;
        }

        if (SymbolUtilities.isWeather(symbolID)) {
            return true;
        }

        String id = SymbolUtilities.getBasicSymbolID(symbolID);
        if (id.equals("G*T*F-----****X")
                || id.equals("G*F*LCC---****X") ||//CFL
                id.equals("G*G*GLB---****X")
                || id.equals("G*G*GLF---****X")
                || id.equals("G*G*GLC---****X")
                || id.equals("G*G*GAF---****X")
                || id.equals("G*G*AAW---****X")
                || id.equals("G*G*DABP--****X")
                || id.equals("G*G*OLP---****X")
                || id.equals("G*G*PY----****X")
                || id.equals("G*G*PM----****X")
                || id.equals("G*G*ALL---****X")
                || id.equals("G*G*ALU---****X")
                || id.equals("G*G*ALM---****X")
                || id.equals("G*G*ALC---****X")
                || id.equals("G*G*ALS---****X")
                || id.equals("G*M*OFA---****X")
                || id.equals("G*M*OGB---****X")
                || id.equals("G*M*OGL---****X")
                || id.equals("G*M*OGZ---****X")
                || id.equals("G*M*OGF---****X")
                || id.equals("G*M*OGR---****X")
                || id.equals("G*M*OADU--****X")
                || id.equals("G*M*OADC--****X")
                || id.equals("G*M*OAR---****X")
                || id.equals("G*M*OAW---****X")
                || id.equals("G*M*OEF---****X") || //Obstacles Effect Fix
                id.equals("G*M*OMC---****X")
                || id.equals("G*M*OWU---****X")
                || id.equals("G*M*OWS---****X")
                || id.equals("G*M*OWD---****X")
                || id.equals("G*M*OWA---****X")
                || id.equals("G*M*OWL---****X")
                || id.equals("G*M*OWH---****X")
                || id.equals("G*M*OWCS--****X")
                || id.equals("G*M*OWCD--****X")
                || id.equals("G*M*OWCT--****X")
                || id.equals("G*M*OHO---****X")
                || id.equals("G*M*BDD---****X") || //Bypass Difficult
                id.equals("G*M*BCD---****X") || //Ford Difficult
                id.equals("G*M*BCE---****X") || //Ford Easy
                id.equals("G*M*SL----****X")
                || id.equals("G*M*SP----****X")
                || id.equals("G*M*NR----****X")
                || id.equals("G*M*NB----****X")
                || id.equals("G*M*NC----****X")
                || id.equals("G*F*ACNI--****X")
                || id.equals("G*F*ACNR--****X")
                || id.equals("G*F*ACNC--****X")
                || id.equals("G*F*AKBC--****X")
                || id.equals("G*F*AKBI--****X")
                || id.equals("G*F*AKBR--****X")
                || id.equals("G*F*AKPC--****X")
                || id.equals("G*F*AKPI--****X")
                || id.equals("G*F*AKPR--****X")
                || id.equals("G*F*LT----****X")
                || id.equals("G*F*LTS---****X")
                || id.equals("G*G*SAE---****X")
                || //id.equals("G*G*SLA---****X") || //Ambush
                id.equals("G*S*LRA---****X")
                || id.equals("G*S*LRM---****X")
                || id.equals("G*S*LRO---****X")
                || id.equals("G*S*LRT---****X")
                || id.equals("G*S*LRW---****X")
                || id.equals("G*T*Q-----****X")
                || id.equals("G*T*E-----****X")
                || id.equals("G*T*F-----****X") || //Tasks Fix
                id.equals("G*T*K-----****X") || //counterattack.
                id.equals("G*T*KF----****X") || //counterattack by fire.
                id.equals("G*M*ORP---****X")
                || id.equals("G*M*ORS---****X")
                || id.equals("G*T*A-----****X")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     *
     * @param id
     * @param name
     * @param description
     * @param symbolCode
     * @param controlPoints
     * @param scale
     * @param bbox
     * @param symbolModifiers
     * @param symbolAttributes
     * @param format
     * @return
     */
    public static String RenderSymbol(String id,
            String name,
            String description,
            String symbolCode,
            String controlPoints,
            Double scale,
            String bbox,
            SparseArray<String> symbolModifiers,
            SparseArray<String> symbolAttributes,
            int format) {
        return RenderSymbol(id, name, description, symbolCode, controlPoints,
                scale, bbox, symbolModifiers, symbolAttributes, format,
                RendererSettings.getInstance().getSymbologyStandard());
    }

    /**
     * Assumes bbox is of form left, right, bottom, top and it is currently only
     * using the width to calculate a reasonable scale. If the original scale is
     * within the max and min range it returns the original scale.
     *
     * @param bbox
     * @param origScale
     * @return
     */
    private static double getReasonableScale(String bbox, double origScale) {
        double scale = origScale;
        try {
            String[] bounds = bbox.split(",");
            double left = Double.valueOf(bounds[0]);
            double right = Double.valueOf(bounds[2]);
            double top = Double.valueOf(bounds[3]);
            double bottom = Double.valueOf(bounds[1]);
            if (left == -180 && right == 180) {
                return origScale;
            } else if (left == 180 && right == -180) {
                return origScale;
            }
            POINT2 ul = new POINT2(left, top);
            POINT2 ur = new POINT2(right, top);
            double widthInMeters = mdlGeodesic.geodesic_distance(ul, ur, null, null);
            double maxWidthInPixels = _maxPixelWidth;
            double minScale = (maxWidthInPixels / widthInMeters) * (1.0d / 96.0d) * (1.0d / 39.37d);
            minScale = 1.0d / minScale;
            if (origScale < minScale) {
                return minScale;
            }

            double minWidthInPixels = _minPixelWidth;
            double maxScale = (minWidthInPixels / widthInMeters) * (1.0d / 96.0d) * (1.0d / 39.37d);
            maxScale = 1.0d / maxScale;
            if (origScale > maxScale) {
                return maxScale;
            }
        } catch (NumberFormatException exc) {
        }
        return scale;
    }

    /**
     *
     * @param id
     * @param name
     * @param description
     * @param symbolCode
     * @param controlPoints
     * @param scale
     * @param bbox
     * @param symbolModifiers SparseArray<String>, keyed using constants from
     * ModifiersTG. Pass in comma delimited String for modifiers with multiple
     * values like AM, AN & X
     * @param symbolAttributes SparseArray<String>, keyed using constants from
     * MilStdAttributes. pass in double[] for AM, AN and X; Strings for the
     * rest.
     * @param format
     * @param symStd 0=2525Bch2, 1=2525C
     * @return
     */
    public static String RenderSymbol(String id,
            String name,
            String description,
            String symbolCode,
            String controlPoints,
            Double scale,
            String bbox,
            SparseArray<String> symbolModifiers,
            SparseArray<String> symbolAttributes,
            int format, int symStd)//,
    {
        //System.out.println("MultiPointHandler.RenderSymbol()");
        boolean normalize = false;
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
                ptGeoUL = getGeoUL(bboxCoords);
                left = ptGeoUL.getX();
                top = ptGeoUL.getY();
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
                scale = getReasonableScale(bbox, scale);
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
                //diagnostic clipping does not work for large scales
                if (scale > 10e6) {
                    //get widest point in the AOI
                    double midLat = 0;
                    if (bottom < 0 && top > 0) {
                        midLat = 0;
                    } else if (bottom < 0 && top < 0) {
                        midLat = top;
                    } else if (bottom > 0 && top > 0) {
                        midLat = bottom;
                    }

                    temp = ipc.GeoToPixels(new Point2D.Double(right, midLat));
                    rightX = (int) temp.getX();
                }
                //end section

                width = (int) Math.abs(rightX - leftX);
                height = (int) Math.abs(bottomY - topY);

                rect = new Rectangle(leftX, topY, width, height);
            }
        } else {
            rect = null;
        }
        //end section

        for (int i = 0; i < len; i++) {
            String[] coordPair = coordinates[i].split(",");
            Double latitude = Double.valueOf(coordPair[1].trim());
            Double longitude = Double.valueOf(coordPair[0].trim());
            geoCoords.add(new Point2D.Double(longitude, latitude));
        }
        if (ipc == null) {
            Point2D ptCoordsUL = getGeoUL(geoCoords);
            ipc = new PointConverter(ptCoordsUL.getX(), ptCoordsUL.getY(), scale);
        }
        //if (crossesIDL(geoCoords) == true) 
        if(Math.abs(right-left)>180)
        {
            normalize = true;
            ((PointConverter)ipc).set_normalize(true);
        } 
        else {
            normalize = false;
            ((PointConverter)ipc).set_normalize(false);
        }

        //seems to work ok at world view
        if (normalize) {
            NormalizeGECoordsToGEExtents(0, 360, geoCoords);
        }

        //M. Deutch 10-3-11
        //must shift the rect pixels to synch with the new ipc
        //the old ipc was in synch with the bbox, so rect x,y was always 0,0
        //the new ipc synchs with the upper left of the geocoords so the boox is shifted
        //and therefore the clipping rectangle must shift by the delta x,y between
        //the upper left corner of the original bbox and the upper left corner of the geocoords
        ArrayList<Point2D> geoCoords2 = new ArrayList<Point2D>();
        geoCoords2.add(new Point2D.Double(left, top));
        geoCoords2.add(new Point2D.Double(right, bottom));

        if (normalize) {
            NormalizeGECoordsToGEExtents(0, 360, geoCoords2);
        }

        //disable clipping
        if (ShouldClipSymbol(symbolCode) == false) 
            if(crossesIDL(geoCoords)==false)
                rect = null;
        
        tgl.set_SymbolId(symbolCode);// "GFGPSLA---****X" AMBUSH symbol code
        tgl.set_Pixels(null);

        try {

            //String fillColor = null;
            MilStdSymbol mSymbol = new MilStdSymbol(symbolCode, null, geoCoords, null);
            //mSymbol.setUseDashArray(false);
            //set milstd symbology standard.
            mSymbol.setSymbologyStandard(symStd);

            if (symbolModifiers != null || symbolAttributes != null) {
                populateModifiers(symbolModifiers, symbolAttributes, mSymbol);
            } else {
                mSymbol.setFillColor(null);
            }
            String symbolIsValid = canRenderMultiPoint(mSymbol);
            if (symbolIsValid.equals("true") == false) {
                String ErrorOutput = "";
                ErrorOutput += ("{\"type\":\"error\",\"error\":\"There was an error creating the MilStdSymbol " + symbolCode + ": " + "- ");
                ErrorOutput += (symbolIsValid + " - ");
                ErrorOutput += ("\"}");
                //ErrorLogger.LogMessage("MultiPointHandler","RenderSymbol",symbolIsValid,Level.WARNING);
                return ErrorOutput;
            }//*/
            //get pixel values in case we need to do a fill.
            if(symbolModifiers.indexOfKey(SYMBOL_FILL_IDS)>=0 || 
                    symbolModifiers.indexOfKey(SYMBOL_LINE_IDS)>=0)
            {
                tgl = clsRenderer.createTGLightFromMilStdSymbol(mSymbol, ipc);
                if(rect != null)
                {
                    Rectangle2D rect2d=new Rectangle2D.Double(rect.x,rect.y,rect.width,rect.height);
                    clsClipPolygon2.ClipPolygon(tgl, rect2d);
                }
                tgPoints = tgl.get_Pixels();                
            }

            if (bboxCoords == null) {
                clsRenderer.renderWithPolylines(mSymbol, ipc, rect);
            } else {
                clsRenderer.renderWithPolylines(mSymbol, ipc, bboxCoords);
            }

            shapes = mSymbol.getSymbolShapes();
            modifiers = mSymbol.getModifierShapes();

            if (format == 1) {
                jsonOutput.append("{\"type\":\"symbol\",");
                jsonContent = JSONize(shapes, modifiers, ipc, true, normalize);
                jsonOutput.append(jsonContent);
                jsonOutput.append("}");
            } else if (format == 0) {

                Color textColor = null;

                //textColor = mSymbol.getLineColor();
                textColor = mSymbol.getTextColor();
                if(textColor==null)
                    textColor=mSymbol.getLineColor();
                /*String hexColor = textColor.toHexString();
                if (hexColor.equals("#FF000000"))//black
                {
                    textColor = Color.white;//textColor = "#FFFFFFFF";
                }*/

                jsonContent = KMLize(id, name, description, symbolCode, shapes, modifiers, ipc, normalize, textColor);

                //if there's a symbol fill or line pattern, add to KML//////////
                if (symbolModifiers.indexOfKey(SYMBOL_FILL_IDS) >= 0
                        || symbolModifiers.indexOfKey(SYMBOL_LINE_IDS) >= 0) {
                    //String fillKML = AddImageFillToKML(tgPoints, jsonContent, mSymbol, ipc, normalize);
                    String fillKML = AddImageFillToKML(tgPoints, jsonContent, symbolModifiers, ipc, normalize);
                    if(fillKML != null && fillKML.equals("")==false)
                    {
                        jsonContent = fillKML;
                    }
                }///end if symbol fill or line pattern//////////////////////////

                jsonOutput.append(jsonContent);
            }
            else if (format == 2)
            {
                jsonOutput.append("{\"type\":\"FeatureCollection\",\"features\":");
                jsonContent = GeoJSONize(shapes, modifiers, ipc, normalize, mSymbol.getTextColor(), mSymbol.getTextBackgroundColor());
                jsonOutput.append(jsonContent);
                jsonOutput.append(",\"properties\":{\"id\":\"");
                jsonOutput.append(id);
                jsonOutput.append("\",\"name\":\"");
                jsonOutput.append(name);
                jsonOutput.append("\",\"description\":\"");
                jsonOutput.append(description);
                jsonOutput.append("\",\"symbolID\":\"");
                jsonOutput.append(symbolCode);
                jsonOutput.append("\"}}");                
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

    /**
     *
     * @param id
     * @param name
     * @param description
     * @param symbolCode
     * @param controlPoints
     * @param scale
     * @param bbox
     * @param symbolModifiers
     * @param symbolAttributes
     * @param symStd
     * @return
     */
    public static MilStdSymbol RenderSymbolAsMilStdSymbol(String id,
            String name,
            String description,
            String symbolCode,
            String controlPoints,
            Double scale,
            String bbox,
            SparseArray<String> symbolModifiers,
            SparseArray<String> symbolAttributes,
            int symStd)//,
    //ArrayList<ShapeInfo>shapes)
    {
        MilStdSymbol mSymbol = null;
        //System.out.println("MultiPointHandler.RenderSymbol()");
        boolean normalize = false;
        Double controlLat = 0.0;
        Double controlLong = 0.0;
        //String jsonContent = "";

        Rectangle rect = null;

        //for symbol & line fill
        ArrayList<POINT2> tgPoints = null;

        String[] coordinates = controlPoints.split(" ");
        TGLight tgl = new TGLight();
        ArrayList<ShapeInfo> shapes = null;//new ArrayList<ShapeInfo>();
        ArrayList<ShapeInfo> modifiers = null;//new ArrayList<ShapeInfo>();
        //ArrayList<Point2D> pixels = new ArrayList<Point2D>();
        ArrayList<Point2D> geoCoords = new ArrayList<Point2D>();
        int len = coordinates.length;

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
                ptGeoUL = getGeoUL(bboxCoords);
                left = ptGeoUL.getX();
                top = ptGeoUL.getY();
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
                scale = getReasonableScale(bbox, scale);
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
                //diagnostic clipping does not work for large scales
                if (scale > 10e6) {
                    //get widest point in the AOI
                    double midLat = 0;
                    if (bottom < 0 && top > 0) {
                        midLat = 0;
                    } else if (bottom < 0 && top < 0) {
                        midLat = top;
                    } else if (bottom > 0 && top > 0) {
                        midLat = bottom;
                    }

                    temp = ipc.GeoToPixels(new Point2D.Double(right, midLat));
                    rightX = (int) temp.getX();
                }
                //end section

                width = (int) Math.abs(rightX - leftX);
                height = (int) Math.abs(bottomY - topY);

                rect = new Rectangle(leftX, topY, width, height);
            }
        } else {
            rect = null;
        }
        //end section

        for (int i = 0; i < len; i++) {
            String[] coordPair = coordinates[i].split(",");
            Double latitude = Double.valueOf(coordPair[1].trim());
            Double longitude = Double.valueOf(coordPair[0].trim());
            geoCoords.add(new Point2D.Double(longitude, latitude));
        }
        if (ipc == null) {
            Point2D ptCoordsUL = getGeoUL(geoCoords);
            ipc = new PointConverter(ptCoordsUL.getX(), ptCoordsUL.getY(), scale);
        }
        //if (crossesIDL(geoCoords) == true) 
        if(Math.abs(right-left)>180)
        {
            normalize = true;
            ((PointConverter)ipc).set_normalize(true);
        } 
        else {
            normalize = false;
            ((PointConverter)ipc).set_normalize(false);
        }

        //seems to work ok at world view
        if (normalize) {
            NormalizeGECoordsToGEExtents(0, 360, geoCoords);
        }

        //M. Deutch 10-3-11
        //must shift the rect pixels to synch with the new ipc
        //the old ipc was in synch with the bbox, so rect x,y was always 0,0
        //the new ipc synchs with the upper left of the geocoords so the boox is shifted
        //and therefore the clipping rectangle must shift by the delta x,y between
        //the upper left corner of the original bbox and the upper left corner of the geocoords
        ArrayList<Point2D> geoCoords2 = new ArrayList<Point2D>();
        geoCoords2.add(new Point2D.Double(left, top));
        geoCoords2.add(new Point2D.Double(right, bottom));

        if (normalize) {
            NormalizeGECoordsToGEExtents(0, 360, geoCoords2);
        }

        //disable clipping
        if (ShouldClipSymbol(symbolCode) == false) 
            if(crossesIDL(geoCoords)==false)
                rect = null;
        
        tgl.set_SymbolId(symbolCode);// "GFGPSLA---****X" AMBUSH symbol code
        tgl.set_Pixels(null);
        
        try {

            String fillColor = null;
            mSymbol = new MilStdSymbol(symbolCode, null, geoCoords, null);

            mSymbol.setUseDashArray(true);
            //set milstd symbology standard.
            mSymbol.setSymbologyStandard(symStd);

            if (symbolModifiers != null || symbolAttributes != null) {
                populateModifiers(symbolModifiers, symbolAttributes, mSymbol);
            } else {
                mSymbol.setFillColor(null);
            }

            if (mSymbol.getFillColor() != null) {
                Color fc = mSymbol.getFillColor();
                //fillColor = Integer.toHexString(fc.getRGB());                
                fillColor = Integer.toHexString(fc.toARGB());
            }

            if (bboxCoords == null) {
                clsRenderer.renderWithPolylines(mSymbol, ipc, rect);
            } else {
                clsRenderer.renderWithPolylines(mSymbol, ipc, bboxCoords);
            }

            shapes = mSymbol.getSymbolShapes();
            modifiers = mSymbol.getModifierShapes();

            //convert points////////////////////////////////////////////////////
            ArrayList<ArrayList<Point2D>> polylines = null;
            ArrayList<ArrayList<Point2D>> newPolylines = null;
            ArrayList<Point2D> newLine = null;
            for (ShapeInfo shape : shapes) {
                polylines = shape.getPolylines();
                //System.out.println("pixel polylines: " + String.valueOf(polylines));
                newPolylines = ConvertPolylinePixelsToCoords(polylines, ipc, normalize);
                shape.setPolylines(newPolylines);
            }

            for (ShapeInfo label : modifiers) {
                Point2D pixelCoord = label.getModifierStringPosition();
                if (pixelCoord == null) {
                    pixelCoord = label.getGlyphPosition();
                }
                Point2D geoCoord = ipc.PixelsToGeo(pixelCoord);

                if (normalize) {
                    geoCoord = NormalizeCoordToGECoord(geoCoord);
                }

                double latitude = geoCoord.getY();
                double longitude = geoCoord.getX();
                label.setModifierStringPosition(new Point2D.Double(longitude, latitude));

            }   

            ////////////////////////////////////////////////////////////////////
            mSymbol.setModifierShapes(modifiers);
            mSymbol.setSymbolShapes(shapes);

        } catch (Exception exc) {

            System.out.println(exc.getMessage());
            exc.printStackTrace();
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
                //System.out.println("Pixel: " + pixels.toString());
                System.out.println("Pixel: " + tgl.get_Pixels().toString());
            }
            if (bbox != null) {
                System.out.println("geo bounds: " + bbox);
            }
            if (rect != null) {
                System.out.println("pixel bounds: " + rect.toString());
            }
        }

        return mSymbol;

    }

    private static ArrayList<ArrayList<Point2D>> ConvertPolylinePixelsToCoords(ArrayList<ArrayList<Point2D>> polylines, IPointConversion ipc, Boolean normalize) {
        ArrayList<ArrayList<Point2D>> newPolylines = new ArrayList<ArrayList<Point2D>>();

        double latitude = 0;
        double longitude = 0;
        ArrayList<Point2D> newLine = null;
        try {
            for (ArrayList<Point2D> line : polylines) {
                newLine = new ArrayList<Point2D>();
                for (Point2D pt : line) {
                    Point2D geoCoord = ipc.PixelsToGeo(pt);

                    if (normalize) {
                        geoCoord = NormalizeCoordToGECoord(geoCoord);
                    }

                    latitude = geoCoord.getY();
                    longitude = geoCoord.getX();
                    newLine.add(new Point2D.Double(longitude, latitude));
                }
                newPolylines.add(newLine);
            }
        } catch (Exception exc) {
            System.out.println(exc.getMessage());
            exc.printStackTrace();
        }
        return newPolylines;
    }

    /**
     * Multipoint Rendering on flat 2D maps
     *
     * @param id A unique ID for the symbol. only used in KML currently
     * @param name
     * @param description
     * @param symbolCode
     * @param controlPoints
     * @param pixelWidth pixel dimensions of the viewable map area
     * @param pixelHeight pixel dimensions of the viewable map area
     * @param bbox The viewable area of the map. Passed in the format of a
     * string "lowerLeftX,lowerLeftY,upperRightX,upperRightY." example:
     * "-50.4,23.6,-42.2,24.2"
     * @param symbolModifiers Modifier with multiple values should be comma
     * delimited
     * @param symbolAttributes
     * @param format An enumeration: 0 for KML, 1 for JSON.
     * @return A JSON or KML string representation of the graphic.
     */
    public static String RenderSymbol2D(String id,
            String name,
            String description,
            String symbolCode,
            String controlPoints,
            int pixelWidth,
            int pixelHeight,
            String bbox,
            SparseArray<String> symbolModifiers,
            SparseArray<String> symbolAttributes,
            int format) {
        return RenderSymbol2D(id, name, description, symbolCode, controlPoints,
                pixelWidth, pixelHeight, bbox, symbolModifiers, symbolAttributes, format,
                RendererSettings.getInstance().getSymbologyStandard());
    }

    /**
     * Multipoint Rendering on flat 2D maps
     *
     * @param id A unique ID for the symbol. only used in KML currently
     * @param name
     * @param description
     * @param symbolCode
     * @param controlPoints
     * @param pixelWidth pixel dimensions of the viewable map area
     * @param pixelHeight pixel dimensions of the viewable map area
     * @param bbox The viewable area of the map. Passed in the format of a
     * string "lowerLeftX,lowerLeftY,upperRightX,upperRightY." example:
     * "-50.4,23.6,-42.2,24.2"
     * @param symbolModifiers Modifier with multiple values should be comma
     * delimited
     * @param symbolAttributes
     * @param format An enumeration: 0 for KML, 1 for JSON.
     * @param symStd An enumeration: 0 for 2525Bch2, 1 for 2525C.
     * @return A JSON or KML string representation of the graphic.
     */
    public static String RenderSymbol2D(String id,
            String name,
            String description,
            String symbolCode,
            String controlPoints,
            int pixelWidth,
            int pixelHeight,
            String bbox,
            SparseArray<String> symbolModifiers,
            SparseArray<String> symbolAttributes,
            int format, int symStd) {
        StringBuilder jsonOutput = new StringBuilder();
        String jsonContent = "";

        Rectangle rect = null;

        ArrayList<POINT2> tgPoints = null;

        String[] coordinates = controlPoints.split(" ");
        TGLight tgl = new TGLight();
        ArrayList<ShapeInfo> shapes = new ArrayList<ShapeInfo>();
        ArrayList<ShapeInfo> modifiers = new ArrayList<ShapeInfo>();
        ArrayList<Point2D> geoCoords = new ArrayList<Point2D>();
        IPointConversion ipc = null;

        Double left = 0.0;
        Double right = 0.0;
        Double top = 0.0;
        Double bottom = 0.0;
        if (bbox != null && bbox.equals("") == false) {
            String[] bounds = bbox.split(",");

            left = Double.valueOf(bounds[0]).doubleValue();
            right = Double.valueOf(bounds[2]).doubleValue();
            top = Double.valueOf(bounds[3]).doubleValue();
            bottom = Double.valueOf(bounds[1]).doubleValue();

            ipc = new PointConversion(pixelWidth, pixelHeight, top, left, bottom, right);
        } else {
            System.out.println("Bad bbox value: " + bbox);
            System.out.println("bbox is viewable area of the map.  Passed in the format of a string \"lowerLeftX,lowerLeftY,upperRightX,upperRightY.\" example: \"-50.4,23.6,-42.2,24.2\"");
            return "ERROR - Bad bbox value: " + bbox;
        }
        //end section

        //get coordinates
        int len = coordinates.length;
        for (int i = 0; i < len; i++) {
            String[] coordPair = coordinates[i].split(",");
            Double latitude = Double.valueOf(coordPair[1].trim()).doubleValue();
            Double longitude = Double.valueOf(coordPair[0].trim()).doubleValue();
            geoCoords.add(new Point2D.Double(longitude, latitude));
        }

        try {
            MilStdSymbol mSymbol = new MilStdSymbol(symbolCode, null, geoCoords, null);
            mSymbol.setUseDashArray(false);
            //set milstd symbology standard.
            mSymbol.setSymbologyStandard(symStd);

            if (symbolModifiers != null && symbolModifiers.equals("") == false) {
                populateModifiers(symbolModifiers, symbolAttributes, mSymbol);
            } else {
                mSymbol.setFillColor(null);
            }

            //build clipping bounds
            Point2D temp = null;
            int leftX;
            int topY;
            int bottomY;
            int rightX;
            int width;
            int height;
            boolean normalize = false;
            if(Math.abs(right-left)>180)
            {
                ((PointConversion)ipc).set_normalize(true);                
                normalize=true;
            }
            else      
            {
                ((PointConversion)ipc).set_normalize(false);
            }
            if (ShouldClipSymbol(symbolCode)  || crossesIDL(geoCoords)) 
            {
                Point2D lt=new Point2D.Double(left,top);
                //temp = ipc.GeoToPixels(new Point2D.Double(left, top));
                temp = ipc.GeoToPixels(lt);
                leftX = (int) temp.getX();
                topY = (int) temp.getY();

                Point2D rb=new Point2D.Double(right,bottom);
                //temp = ipc.GeoToPixels(new Point2D.Double(right, bottom));
                temp = ipc.GeoToPixels(rb);
                bottomY = (int) temp.getY();
                rightX = (int) temp.getX();
                //////////////////

                width = (int) Math.abs(rightX - leftX);
                height = (int) Math.abs(bottomY - topY);

                rect = new Rectangle(leftX, topY, width, height);
            }

            //check for required points & parameters
            String symbolIsValid = canRenderMultiPoint(mSymbol);
            if (symbolIsValid.equals("true") == false) {
                String ErrorOutput = "";
                ErrorOutput += ("{\"type\":\"error\",\"error\":\"There was an error creating the MilStdSymbol " + symbolCode + ": " + "- ");
                ErrorOutput += (symbolIsValid + " - ");
                ErrorOutput += ("\"}");
                ErrorLogger.LogMessage("MultiPointHandler", "RenderSymbol", symbolIsValid, Level.WARNING);
                return ErrorOutput;
            }//*/

            if(symbolModifiers.indexOfKey(SYMBOL_FILL_IDS)>=0 || 
                    symbolModifiers.indexOfKey(SYMBOL_LINE_IDS)>=0)
            {
                tgl = clsRenderer.createTGLightFromMilStdSymbol(mSymbol, ipc);
                if(rect != null)
                {
                    Rectangle2D rect2d=new Rectangle2D.Double(rect.x,rect.y,rect.width,rect.height);
                    clsClipPolygon2.ClipPolygon(tgl, rect2d);
                }
                tgPoints = tgl.get_Pixels();                
            }

            //new interface
            //IMultiPointRenderer mpr = MultiPointRenderer.getInstance();
            clsRenderer.renderWithPolylines(mSymbol, ipc, rect);
            shapes = mSymbol.getSymbolShapes();
            modifiers = mSymbol.getModifierShapes();

            //boolean normalize = false;

            if (format == 1) {
                jsonOutput.append("{\"type\":\"symbol\",");
                //jsonContent = JSONize(shapes, modifiers, ipc, normalize);
                jsonOutput.append(jsonContent);
                jsonOutput.append("}");
            } else if (format == 0) {
                String fillColor = null;
                if (mSymbol.getFillColor() != null) {
                    //fillColor = Integer.toHexString(mSymbol.getFillColor().getRGB());
                    fillColor = Integer.toHexString(mSymbol.getFillColor().toARGB());
                }

                Color textColor = null;

                textColor = mSymbol.getTextColor();
                if(textColor==null)
                    textColor=mSymbol.getLineColor();
                /*String hexColor = SymbolUtilities.colorToHexString(textColor, true);
                if (hexColor.equals("#FF000000"))//black
                {
                    textColor = Color.white;//textColor = "#FFFFFFFF";
                }*/
                jsonContent = KMLize(id, name, description, symbolCode, shapes, modifiers, ipc, normalize, textColor);
                jsonOutput.append(jsonContent);
                //if there's a symbol fill or line pattern, add to KML//////////
                if(symbolModifiers.indexOfKey(SYMBOL_FILL_IDS)>=0 || 
                    symbolModifiers.indexOfKey(SYMBOL_LINE_IDS)>=0)
                {
                    //String fillKML = AddImageFillToKML(tgPoints, jsonContent, mSymbol, ipc, normalize);
                    String fillKML = AddImageFillToKML(tgPoints, jsonContent, symbolModifiers, ipc, normalize);
                    if(fillKML != null && !fillKML.isEmpty())
                    {
                        jsonOutput.append(fillKML);
                    }
                }///end if symbol fill or line pattern//////////////////////////

//                if(mSymbol.getModifierMap().indexOfKey(MilStdAttributes.LookAtTag) &&
//                        mSymbol.getModifierMap().get(MilStdAttributes.LookAtTag).toLowerCase().equals("true"))
//                {
//                    String LookAtTag = JavaRendererUtilities.generateLookAtTag(geoCoords,mSymbol.getModifiers_AM_AN_X(ModifiersTG.X_ALTITUDE_DEPTH));
//                    if(LookAtTag != null && LookAtTag.endsWith("</LookAt>") == true)
//                    {
//                        int idx = jsonContent.indexOf("<visibility>");
//                        jsonContent = jsonContent.substring(0,idx) + LookAtTag + jsonContent.substring(idx);
//                    }
//                }
            } else if (format == 2) {
                jsonOutput.append("{\"type\":\"FeatureCollection\",\"features\":");
                jsonContent = GeoJSONize(shapes, modifiers, ipc, normalize, mSymbol.getTextColor(), mSymbol.getTextBackgroundColor());
                jsonOutput.append(jsonContent);
                jsonOutput.append(",\"properties\":{\"id\":\"");
                jsonOutput.append(id);
                jsonOutput.append("\",\"name\":\"");
                jsonOutput.append(name);
                jsonOutput.append("\",\"description\":\"");
                jsonOutput.append(description);
                jsonOutput.append("\",\"symbolID\":\"");
                jsonOutput.append(symbolCode);
                jsonOutput.append("\"}}");

            }

        } catch (Exception exc) {
            jsonOutput = new StringBuilder();
            jsonOutput.append("{\"type\":\"error\",\"error\":\"There was an error creating the MilStdSymbol " + symbolCode + ": " + "- ");
            jsonOutput.append(exc.getMessage() + " - ");
            jsonOutput.append(ErrorLogger.getStackTrace(exc));
            jsonOutput.append("\"}");
        }

        boolean debug = false;
        if (debug == true) {
            System.out.println("Symbol Code: " + symbolCode);
            System.out.println("BBOX: " + bbox);
            if (controlPoints != null) {
                System.out.println("Geo Points: " + controlPoints);
            }
            if (tgl != null && tgl.get_Pixels() != null)//pixels != null
            {
                //System.out.println("Pixel: " + pixels.toString());
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

        return jsonOutput.toString();

    }

    /**
     * For Mike Deutch testing
     *
     * @param id
     * @param name
     * @param description
     * @param symbolCode
     * @param controlPoints
     * @param pixelWidth
     * @param pixelHeight
     * @param bbox
     * @param symbolModifiers
     * @param shapes
     * @param modifiers
     * @param format
     * @return
     * @deprecated
     */
    public static String RenderSymbol2DX(String id,
            String name,
            String description,
            String symbolCode,
            String controlPoints,
            int pixelWidth,
            int pixelHeight,
            String bbox,
            SparseArray<String> symbolModifiers,
            SparseArray<String> symbolAttributes,
            ArrayList<ShapeInfo> shapes,
            ArrayList<ShapeInfo> modifiers,
            int format)//,
    //ArrayList<ShapeInfo>shapes)
    {

        StringBuilder jsonOutput = new StringBuilder();
        String jsonContent = "";

        Rectangle rect = null;

        String[] coordinates = controlPoints.split(" ");
        TGLight tgl = new TGLight();
        ArrayList<Point2D> geoCoords = new ArrayList<Point2D>();
        IPointConversion ipc = null;

        Double left = 0.0;
        Double right = 0.0;
        Double top = 0.0;
        Double bottom = 0.0;
        if (bbox != null && bbox.equals("") == false) {
            String[] bounds = bbox.split(",");

            left = Double.valueOf(bounds[0]).doubleValue();
            right = Double.valueOf(bounds[2]).doubleValue();
            top = Double.valueOf(bounds[3]).doubleValue();
            bottom = Double.valueOf(bounds[1]).doubleValue();

            ipc = new PointConversion(pixelWidth, pixelHeight, top, left, bottom, right);
        } else {
            System.out.println("Bad bbox value: " + bbox);
            System.out.println("bbox is viewable area of the map.  Passed in the format of a string \"lowerLeftX,lowerLeftY,upperRightX,upperRightY.\" example: \"-50.4,23.6,-42.2,24.2\"");
            return "ERROR - Bad bbox value: " + bbox;
        }
        //end section

        //get coordinates
        int len = coordinates.length;
        for (int i = 0; i < len; i++) {
            String[] coordPair = coordinates[i].split(",");
            Double latitude = Double.valueOf(coordPair[1].trim()).doubleValue();
            Double longitude = Double.valueOf(coordPair[0].trim()).doubleValue();
            geoCoords.add(new Point2D.Double(longitude, latitude));
        }

        try {
            MilStdSymbol mSymbol = new MilStdSymbol(symbolCode, null, geoCoords, null);

            if (symbolModifiers != null && symbolModifiers.equals("") == false) {
                populateModifiers(symbolModifiers, symbolAttributes, mSymbol);
            } else {
                mSymbol.setFillColor(null);
            }

            clsRenderer.renderWithPolylines(mSymbol, ipc, rect);
            shapes = mSymbol.getSymbolShapes();
            modifiers = mSymbol.getModifierShapes();

            boolean normalize = false;

            if (format == 1) {
                jsonOutput.append("{\"type\":\"symbol\",");
                jsonContent = JSONize(shapes, modifiers, ipc, false, normalize);
                jsonOutput.append(jsonContent);
                jsonOutput.append("}");
            } else if (format == 0) {
                String fillColor = null;
                if (mSymbol.getFillColor() != null) {
                    fillColor = Integer.toHexString(mSymbol.getFillColor().toARGB());//Integer.toHexString(shapeInfo.getFillColor().getRGB()
                }
                jsonContent = KMLize(id, name, description, symbolCode, shapes, modifiers, ipc, normalize, mSymbol.getLineColor());
                jsonOutput.append(jsonContent);
            }

        } catch (Exception exc) {
            jsonOutput = new StringBuilder();
            jsonOutput.append("{\"type\":\"error\",\"error\":\"There was an error creating the MilStdSymbol " + symbolCode + ": " + "- ");
            jsonOutput.append(exc.getMessage() + " - ");
            jsonOutput.append("\"}");
        }

        boolean debug = true;
        if (debug == true) {
            System.out.println("Symbol Code: " + symbolCode);
            System.out.println("BBOX: " + bbox);
            if (controlPoints != null) {
                System.out.println("Geo Points: " + controlPoints);
            }
            if (tgl != null && tgl.get_Pixels() != null)//pixels != null
            {
                //System.out.println("Pixel: " + pixels.toString());
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
        return jsonOutput.toString();

    }

    private static SymbolInfo MilStdSymbolToSymbolInfo(MilStdSymbol symbol) {
        SymbolInfo si = null;

        ArrayList<TextInfo> tiList = new ArrayList<TextInfo>();
        ArrayList<LineInfo> liList = new ArrayList<LineInfo>();

        TextInfo tiTemp = null;
        LineInfo liTemp = null;
        ShapeInfo siTemp = null;

        ArrayList<ShapeInfo> lines = symbol.getSymbolShapes();
        ArrayList<ShapeInfo> modifiers = symbol.getModifierShapes();

        int lineCount = lines.size();
        int modifierCount = modifiers.size();
        for (int i = 0; i < lineCount; i++) {
            siTemp = lines.get(i);
            if (siTemp.getPolylines() != null) {
                liTemp = new LineInfo();
                liTemp.setFillColor(siTemp.getFillColor());
                liTemp.setLineColor(siTemp.getLineColor());
                liTemp.setPolylines(siTemp.getPolylines());
                liTemp.setStroke(siTemp.getStroke());
                liList.add(liTemp);
            }
        }

        for (int j = 0; j < modifierCount; j++) {
            tiTemp = new TextInfo();
            siTemp = modifiers.get(j);
            if (siTemp.getModifierString() != null) {
                tiTemp.setModifierString(siTemp.getModifierString());
                tiTemp.setModifierStringPosition(siTemp.getModifierStringPosition());
                tiTemp.setModifierStringAngle(siTemp.getModifierStringAngle());
                tiList.add(tiTemp);
            }
        }
        si = new SymbolInfo(tiList, liList);
        return si;
    }

    /**
     * Populates a symbol with the modifiers from a JSON string. This function
     * will overwrite any previously populated modifier data.
     *
     * @param jsonString a JSON formatted string containing all the symbol
     * modifier data.
     * @param symbol An existing MilStdSymbol
     * @return
     */
    private static boolean populateModifiers(SparseArray<String> saModifiers, SparseArray<String> saAttributes, MilStdSymbol symbol) {
        SparseArray<String> modifiers = new SparseArray<String>();
        SparseArray<String> attributes = saAttributes.clone();

        // Stores array graphic modifiers for MilStdSymbol;
        ArrayList<Double> altitudes = null;
        ArrayList<Double> azimuths = null;
        ArrayList<Double> distances = null;

        // Stores colors for symbol.
        String fillColor = null;
        String lineColor = null;
        String textColor = null;
        String textBackgroundColor = null;

        int lineWidth = 0;
        int symstd = 0;
        String altMode = null;
        boolean useDashArray = symbol.getUseDashArray();

        String symbolFillIDs = null;
        String symbolFillIconSize = null;

        try {

            // The following attirubtes are labels.  All of them
            // are strings and can be added on the creation of the
            // MilStdSymbol by adding to a Map and passing in the
            // modifiers parameter.
            if (saModifiers != null) {
                if (saModifiers.indexOfKey(ModifiersTG.C_QUANTITY) >= 0) {
                    modifiers.put(ModifiersTG.C_QUANTITY, String.valueOf(saModifiers.get(ModifiersTG.C_QUANTITY)));
                }

                if (saModifiers.indexOfKey(ModifiersTG.H_ADDITIONAL_INFO_1) >= 0) {
                    modifiers.put(ModifiersTG.H_ADDITIONAL_INFO_1, String.valueOf(saModifiers.get(ModifiersTG.H_ADDITIONAL_INFO_1)));
                }

                if (saModifiers.indexOfKey(ModifiersTG.H1_ADDITIONAL_INFO_2) >= 0) {
                    modifiers.put(ModifiersTG.H1_ADDITIONAL_INFO_2, String.valueOf(saModifiers.get(ModifiersTG.H1_ADDITIONAL_INFO_2)));
                }

                if (saModifiers.indexOfKey(ModifiersTG.H2_ADDITIONAL_INFO_3) >= 0) {
                    modifiers.put(ModifiersTG.H2_ADDITIONAL_INFO_3, String.valueOf(saModifiers.get(ModifiersTG.H2_ADDITIONAL_INFO_3)));
                }

                if (saModifiers.indexOfKey(ModifiersTG.N_HOSTILE) >= 0) {
                    modifiers.put(ModifiersTG.N_HOSTILE, String.valueOf(saModifiers.get(ModifiersTG.N_HOSTILE)));
                }

                if (saModifiers.indexOfKey(ModifiersTG.Q_DIRECTION_OF_MOVEMENT) >= 0) {
                    modifiers.put(ModifiersTG.Q_DIRECTION_OF_MOVEMENT, String.valueOf(saModifiers.get(ModifiersTG.Q_DIRECTION_OF_MOVEMENT)));
                }

                if (saModifiers.indexOfKey(ModifiersTG.T_UNIQUE_DESIGNATION_1) >= 0) {
                    modifiers.put(ModifiersTG.T_UNIQUE_DESIGNATION_1, String.valueOf(saModifiers.get(ModifiersTG.T_UNIQUE_DESIGNATION_1)));
                }

                if (saModifiers.indexOfKey(ModifiersTG.T1_UNIQUE_DESIGNATION_2) >= 0) {
                    modifiers.put(ModifiersTG.T1_UNIQUE_DESIGNATION_2, String.valueOf(saModifiers.get(ModifiersTG.T1_UNIQUE_DESIGNATION_2)));
                }

                if (saModifiers.indexOfKey(ModifiersTG.V_EQUIP_TYPE) >= 0) {
                    modifiers.put(ModifiersTG.V_EQUIP_TYPE, String.valueOf(saModifiers.get(ModifiersTG.V_EQUIP_TYPE)));
                }

                if (saModifiers.indexOfKey(ModifiersTG.W_DTG_1) >= 0) {
                    modifiers.put(ModifiersTG.W_DTG_1, String.valueOf(saModifiers.get(ModifiersTG.W_DTG_1)));
                }

                if (saModifiers.indexOfKey(ModifiersTG.W1_DTG_2) >= 0) {
                    modifiers.put(ModifiersTG.W1_DTG_2, String.valueOf(saModifiers.get(ModifiersTG.W1_DTG_2)));
                }

                //Required multipoint modifier arrays
                if (saModifiers.indexOfKey(ModifiersTG.X_ALTITUDE_DEPTH) >= 0) {
                    altitudes = new ArrayList<Double>();
                    String[] arrAltitudes = String.valueOf(saModifiers.get(ModifiersTG.X_ALTITUDE_DEPTH)).split(",");
                    for (String x : arrAltitudes) {
                        if (x.equals("") != true) {
                            altitudes.add(Double.parseDouble(x));
                        }
                    }
                }

                if (saModifiers.indexOfKey(ModifiersTG.AM_DISTANCE) >= 0) {
                    distances = new ArrayList<Double>();
                    String[] arrDistances = String.valueOf(saModifiers.get(ModifiersTG.AM_DISTANCE)).split(",");
                    for (String am : arrDistances) {
                        if (am.equals("") != true) {
                            distances.add(Double.parseDouble(am));
                        }
                    }
                }

                if (saModifiers.indexOfKey(ModifiersTG.AN_AZIMUTH) >= 0) {
                    azimuths = new ArrayList<Double>();
                    String[] arrAzimuths = String.valueOf(saModifiers.get(ModifiersTG.AN_AZIMUTH)).split(",");;
                    for (String an : arrAzimuths) {
                        if (an.equals("") != true) {
                            azimuths.add(Double.parseDouble(an));
                        }
                    }
                }
            }
            if (saAttributes != null) {
                // These properties are ints, not labels, they are colors.//////////////////
                if (saAttributes.indexOfKey(MilStdAttributes.FillColor) >= 0) {
                    fillColor = (String) saAttributes.get(MilStdAttributes.FillColor);
                }

                if (saAttributes.indexOfKey(MilStdAttributes.LineColor) >= 0) {
                    lineColor = (String) saAttributes.get(MilStdAttributes.LineColor);
                }

                if (saAttributes.indexOfKey(MilStdAttributes.LineWidth) >= 0) {
                    lineWidth = Integer.parseInt(saAttributes.get(MilStdAttributes.LineWidth));
                }
                
                if (saAttributes.indexOfKey(MilStdAttributes.TextColor) >= 0) {
                    textColor = (String) saAttributes.get(MilStdAttributes.TextColor);
                }
                
                if (saAttributes.indexOfKey(MilStdAttributes.TextBackgroundColor) >= 0) {
                    textBackgroundColor = (String) saAttributes.get(MilStdAttributes.TextBackgroundColor);
                }

                if (saAttributes.indexOfKey(MilStdAttributes.SymbologyStandard) >= 0) {
                    symstd = Integer.parseInt(saAttributes.get(MilStdAttributes.SymbologyStandard));
                    symbol.setSymbologyStandard(symstd);
                }

                if (saAttributes.indexOfKey(MilStdAttributes.AltitudeMode) >= 0) {
                    altMode = saAttributes.get(MilStdAttributes.AltitudeMode);
                }

                if (saAttributes.indexOfKey(MilStdAttributes.UseDashArray) >= 0) {
                    useDashArray = Boolean.parseBoolean(saAttributes.get(MilStdAttributes.UseDashArray));
                }
            }

            symbol.setModifierMap(modifiers);

            if (fillColor != null && fillColor.equals("") == false) {
                symbol.setFillColor(SymbolUtilities.getColorFromHexString(fillColor));
            } 

            if (lineColor != null && lineColor.equals("") == false) {
                symbol.setLineColor(SymbolUtilities.getColorFromHexString(lineColor));
            }
            else if(symbol.getLineColor()==null)
                symbol.setLineColor(Color.black);

            if (lineWidth > 0) {
                symbol.setLineWidth(lineWidth);
            }
            
            if (textColor != null && textColor.equals("") == false) {
                symbol.setTextColor(SymbolUtilities.getColorFromHexString(textColor));
            }
            else
                symbol.setTextColor(symbol.getLineColor());
                
            if (textBackgroundColor != null && textBackgroundColor.equals("") == false) {
                symbol.setTextBackgroundColor(SymbolUtilities.getColorFromHexString(textBackgroundColor));
            }

            if (altMode != null) {
                symbol.setAltitudeMode(altMode);
            }

            symbol.setUseDashArray(useDashArray);

            // Check grpahic modifiers variables.  If we set earlier, populate
            // the fields, otherwise, ignore.
            if (altitudes != null) {
                symbol.setModifiers_AM_AN_X(ModifiersTG.X_ALTITUDE_DEPTH, altitudes);
            }
            if (distances != null) {
                symbol.setModifiers_AM_AN_X(ModifiersTG.AM_DISTANCE, distances);
            }

            if (azimuths != null) {
                symbol.setModifiers_AM_AN_X(ModifiersTG.AN_AZIMUTH, azimuths);
            }

            //Check if sector range fan has required min range
            if (SymbolUtilities.getBasicSymbolID(symbol.getSymbolID()).equals("G*F*AXS---****X")) {
                if (symbol.getModifiers_AM_AN_X(ModifiersTG.AN_AZIMUTH) != null
                        && symbol.getModifiers_AM_AN_X(ModifiersTG.AM_DISTANCE) != null) {
                    int anCount = symbol.getModifiers_AM_AN_X(ModifiersTG.AN_AZIMUTH).size();
                    int amCount = symbol.getModifiers_AM_AN_X(ModifiersTG.AM_DISTANCE).size();
                    ArrayList<Double> am = null;
                    if (amCount < ((anCount / 2) + 1)) {
                        am = symbol.getModifiers_AM_AN_X(ModifiersTG.AM_DISTANCE);
                        if (am.get(0) != 0.0) {
                            am.add(0, 0.0);
                        }
                    }
                }
            }
        } catch (Exception exc2) {
            Log.e("MultiPointHandler.populateModifiers", exc2.getMessage(), exc2);
        }
        return true;

    }

    /**
     * FOR DEUTCH USE ONLY
     *
     * @param symbolCode
     * @param controlPoints
     * @param scale
     * @param bbox
     * @param shapes
     * @deprecated to make sure no one else is using it.
     */
    public static IPointConversion RenderSymbol2(String symbolCode,
            String controlPoints,
            Double scale,
            String bbox,
            ArrayList<ShapeInfo> shapes,
            ArrayList<ShapeInfo> modifiers)//,
    {
        boolean normalize = false;
        StringBuilder jsonOutput = new StringBuilder();
        String jsonContent = "";
        Rectangle rect = null;
        int j = 0;
        String[] coordinates = controlPoints.split(" ");
        TGLight tgl = new TGLight();
        ArrayList<Point2D> geoCoords = new ArrayList<Point2D>();
        int len = coordinates.length;

        IPointConversion ipc = null;

        Double left = 0.0;
        Double right = 0.0;
        Double top = 0.0;
        Double bottom = 0.0;
        Point2D temp = null;
        int width = 0;
        int height = 0;
        int leftX = 0;
        int topY = 0;
        int bottomY = 0;
        int rightX = 0;
        Point2D pt2d = null;
        ArrayList<Point2D> bboxCoords = null;
        Point2D ptGeoUL;
        if (bbox != null && bbox.equals("") == false) {
            String[] bounds = null;
            if (bbox.contains(" "))//trapezoid or polygon
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
                //so lowest possible pxiels values for the trapezoid points are 0,0
                ptGeoUL = getGeoUL(bboxCoords);
                left = ptGeoUL.getX();
                top = ptGeoUL.getY();
                //bboxCoords need to be in pixels
                ipc = new PointConverter(left, top, scale);
                //diagnostic
                //the renderer is going to expand the trapezoid by 20 pixels
                //so that it can cut off the connector lines on the boundaries.
                //Shift the converter by 20x20 pixels here to shift the trapezoid   
                //so that it will effectively have the same origin after it is expanded
                Point2D ptPixels = null;
                ptPixels = new Point2D.Double(20, 20);
                Point2D ptGeo = ipc.PixelsToGeo(ptPixels);
                IPointConversion ipcTemp = new PointConverter(ptGeo.getX(), ptGeo.getY(), scale);
                int n = bboxCoords.size();
                //for (j = 0; j < bboxCoords.size(); j++) 
                for (j = 0; j < n; j++) {
                    ptGeo = bboxCoords.get(j);
                    ptPixels = ipcTemp.GeoToPixels(ptGeo);
                    bboxCoords.set(j, (Point2D) ptPixels);
                }
            } else//rectangle
            {
                bounds = bbox.split(",");
                left = Double.valueOf(bounds[0]).doubleValue();
                right = Double.valueOf(bounds[2]).doubleValue();
                top = Double.valueOf(bounds[3]).doubleValue();
                bottom = Double.valueOf(bounds[1]).doubleValue();
                ipc = new PointConverter(left, top, scale);
            }

            //added 2 lines Deutch 6-29-11
            //controlLong = left;
            //controlLat = top;
            //end section
            //new conversion
            //swap two lines below when ready for coordinate update
            //ipc = new PointConverter(left, top, scale);
            //ipc = new PointConverter(left, top, right, bottom, scale);
            if (bboxCoords == null) {
                //temp = ipc.GeoToPixels(new Point2D(left, top));
                pt2d = new Point2D.Double(left, top);
                temp = ipc.GeoToPixels(pt2d);
                leftX = (int) temp.getX();
                topY = (int) temp.getY();

                //temp = ipc.GeoToPixels(new Point2D(right, bottom));
                pt2d = new Point2D.Double(right, bottom);
                temp = ipc.GeoToPixels(pt2d);
                bottomY = (int) temp.getY();
                rightX = (int) temp.getX();
                //////////////////

                width = (int) Math.abs(rightX - leftX);
                height = (int) Math.abs(bottomY - topY);

                rect = new Rectangle(leftX, topY, width, height);
            }
        } else {
            rect = null;
        }
        //end section

        //System.out.println("Pixel Coords: ");
        for (int i = 0; i < len; i++) {
            String[] coordPair = coordinates[i].split(",");
            Double latitude = Double.valueOf(coordPair[1].trim()).doubleValue();
            Double longitude = Double.valueOf(coordPair[0].trim()).doubleValue();
            geoCoords.add(new Point2D.Double(longitude, latitude));
        }
        if (ipc == null) {
            Point2D ptCoordsUL = getGeoUL(geoCoords);
            ipc = new PointConverter(ptCoordsUL.getX(), ptCoordsUL.getY(), scale);
        }
        //if (crossesIDL(geoCoords) == true) 
        if(Math.abs(right-left)>180)
        {
            normalize = true;
            ((PointConverter)ipc).set_normalize(true);
        } 
        else {
            normalize = false;
            ((PointConverter)ipc).set_normalize(false);
        }

        //seems to work ok at world view
        if (normalize) {
            NormalizeGECoordsToGEExtents(0, 360, geoCoords);
        }

        tgl.set_SymbolId(symbolCode);// "GFGPSLA---****X" AMBUSH symbol code
        tgl.set_Pixels(null);

        try {
            //Map<String, String> modifierMap = new HashMap<String, String>();
            SparseArray<String> modifierMap = new SparseArray<String>();
            MilStdSymbol mSymbol = new MilStdSymbol(symbolCode, null, geoCoords, modifierMap);
            tgl = clsRenderer.createTGLightFromMilStdSymbol(mSymbol, ipc);
            //diagnostic
            tgl.set_FillColor(new Color(150, 150, 150, 20));
            tgl.set_T1("5000");
            tgl.set_H("10000");
            tgl.set_H2("5400");
            if (bboxCoords == null) {
                clsRenderer.render_GE(tgl, shapes, modifiers, ipc, rect);
            } else {
                clsRenderer.render_GE(tgl, shapes, modifiers, ipc, bboxCoords);
            }

            jsonOutput.append("{\"type\":\"symbol\",");
            jsonContent = JSONize(shapes, modifiers, ipc, true, normalize);
            jsonOutput.append(jsonContent);
            jsonOutput.append("}");

        } catch (Exception exc) {
            jsonOutput = new StringBuilder();
            jsonOutput.append("{\"type\":\"error\",\"error\":\"There was an error creating the MilStdSymbol - ");
            jsonOutput.append(exc.getMessage() + " - ");
            jsonOutput.append("\"}");
        }

        boolean debug = true;
        if (debug == true) {
            System.out.println("Symbol Code: " + symbolCode);
            System.out.println("Scale: " + scale);
            System.out.println("BBOX: " + bbox);
            if (controlPoints != null) {
                System.out.println("Geo Points: " + controlPoints);
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
        return ipc;

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

    /**
     * 
     * @param shapes
     * @param modifiers
     * @param ipc
     * @param geMap
     * @param normalize
     * @return 
     * @deprecated Use GeoJSONize()
     */
    private static String JSONize(ArrayList<ShapeInfo> shapes, ArrayList<ShapeInfo> modifiers, IPointConversion ipc, Boolean geMap, boolean normalize) {
        String polygons = "";
        String lines = "";
        String labels = "";
        String jstr = "";
        ShapeInfo tempModifier = null;

        int len = shapes.size();
        for (int i = 0; i < len; i++) {
            if (jstr.length() > 0) {
                jstr += ",";
            }
            String shapesToAdd = ShapeToJSONString(shapes.get(i), ipc, geMap, normalize);
            if (shapesToAdd.length() > 0) {
                if (shapesToAdd.startsWith("line", 2)) {
                    if (lines.length() > 0) {
                        lines += ",";
                    }

                    lines += shapesToAdd;
                } else if (shapesToAdd.startsWith("polygon", 2)) {
                    if (polygons.length() > 0) {
                        polygons += ",";
                    }

                    polygons += shapesToAdd;
                }
            }
        }

        jstr += "\"polygons\": [" + polygons + "],"
                + "\"lines\": [" + lines + "],";
        int len2 = modifiers.size();
        labels = "";
        for (int j = 0; j < len2; j++) {
            tempModifier = modifiers.get(j);
            if (geMap) {
                AdjustModifierPointToCenter(tempModifier);
            }
            String labelsToAdd = LabelToJSONString(tempModifier, ipc, normalize);
            if (labelsToAdd.length() > 0) {
                if (labels.length() > 0) {
                    labels += ",";
                }

                labels += labelsToAdd;

            }
        }
        jstr += "\"labels\": [" + labels + "]";
        return jstr;
    }

    private static Color getIdealTextBackgroundColor(Color fgColor) {
        //ErrorLogger.LogMessage("SymbolDraw","getIdealtextBGColor", "in function", Level.SEVERE);
        try {
            //an array of three elements containing the
            //hue, saturation, and brightness (in that order),
            //of the color with the indicated red, green, and blue components/
            float hsbvals[] = new float[3];

            if (fgColor != null) {/*
                 Color.RGBtoHSB(fgColor.getRed(), fgColor.getGreen(), fgColor.getBlue(), hsbvals);

                 if(hsbvals != null)
                 {
                 //ErrorLogger.LogMessage("SymbolDraw","getIdealtextBGColor", "length: " + String.valueOf(hsbvals.length));
                 //ErrorLogger.LogMessage("SymbolDraw","getIdealtextBGColor", "H: " + String.valueOf(hsbvals[0]) + " S: " + String.valueOf(hsbvals[1]) + " B: " + String.valueOf(hsbvals[2]),Level.SEVERE);
                 if(hsbvals[2] > 0.6)
                 return Color.BLACK;
                 else
                 return Color.WHITE;
                 }*/

                int nThreshold = RendererSettings.getInstance().getTextBackgroundAutoColorThreshold();//160;
                int bgDelta = (int) ((fgColor.getRed() * 0.299) + (fgColor.getGreen() * 0.587) + (fgColor.getBlue() * 0.114));
                //ErrorLogger.LogMessage("bgDelta: " + String.valueOf(255-bgDelta));
                //if less than threshold, black, otherwise white.
                //return (255 - bgDelta < nThreshold) ? Color.BLACK : Color.WHITE;//new Color(0, 0, 0, fgColor.getAlpha())
                return (255 - bgDelta < nThreshold) ? new Color(0, 0, 0, fgColor.getAlpha()) : new Color(255, 255, 255, fgColor.getAlpha());
            }
        } catch (Exception exc) {
            ErrorLogger.LogException("SymbolDraw", "getIdealtextBGColor", exc);
        }
        return Color.WHITE;
    }

    private static String LabelToGeoJSONString(ShapeInfo shapeInfo, IPointConversion ipc, boolean normalize, Color textColor, Color textBackgroundColor) {

        StringBuilder JSONed = new StringBuilder();
        StringBuilder properties = new StringBuilder();
        StringBuilder geometry = new StringBuilder();

        Color outlineColor = getIdealTextBackgroundColor(textColor);
        if(textBackgroundColor != null)
        	outlineColor = textBackgroundColor;

        //AffineTransform at = shapeInfo.getAffineTransform();
        //Point2D coord = (Point2D)new Point2D.Double(at.getTranslateX(), at.getTranslateY());
        //Point2D coord = (Point2D) new Point2D.Double(shapeInfo.getGlyphPosition().getX(), shapeInfo.getGlyphPosition().getY());
        Point2D coord = (Point2D) new Point2D.Double(shapeInfo.getModifierStringPosition().getX(), shapeInfo.getModifierStringPosition().getY());
        Point2D geoCoord = ipc.PixelsToGeo(coord);
        //M. Deutch 9-27-11
        if (normalize) {
            geoCoord = NormalizeCoordToGECoord(geoCoord);
        }
        double latitude = Math.round(geoCoord.getY() * 100000000.0) / 100000000.0;
        double longitude = Math.round(geoCoord.getX() * 100000000.0) / 100000000.0;
        double angle = shapeInfo.getModifierStringAngle();
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
            JSONed.append(SymbolUtilities.colorToHexString(textColor, false));
            JSONed.append("\",\"fontSize\":\"");
            JSONed.append(String.valueOf(RS.getMPModifierFontSize()) + "pt\"");
            JSONed.append(",\"fontFamily\":\"");
            JSONed.append(RS.getMPModifierFontName());
            JSONed.append(", sans-serif");

            if (RS.getMPModifierFontType() == Typeface.BOLD) {
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
            JSONed.append(SymbolUtilities.colorToHexString(outlineColor, false));
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

    private static String ShapeToGeoJSONString(ShapeInfo shapeInfo, IPointConversion ipc, boolean normalize) {
        StringBuilder JSONed = new StringBuilder();
        StringBuilder properties = new StringBuilder();
        StringBuilder geometry = new StringBuilder();
        String geometryType = null;
        /*
         NOTE: Google Earth / KML colors are backwards.
         They are ordered Alpha,Blue,Green,Red, not Red,Green,Blue,Aplha like the rest of the world
         * */
        Color lineColor = shapeInfo.getLineColor();
        Color fillColor = shapeInfo.getFillColor();

        if (shapeInfo.getShapeType() == ShapeInfo.SHAPE_TYPE_FILL || fillColor != null) {
            geometryType = "\"Polygon\"";
        } else //if(shapeInfo.getShapeType() == ShapeInfo.SHAPE_TYPE_POLYLINE)
        {
            geometryType = "\"MultiLineString\"";
        }

        BasicStroke stroke = null;
        stroke = (BasicStroke) shapeInfo.getStroke();
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
            properties.append("\"strokeColor\":\"" + SymbolUtilities.colorToHexString(lineColor, false) + "\",");
            properties.append("\"lineOpacity\":" + String.valueOf(lineColor.getAlpha() / 255f) + ",");
        }
        if (fillColor != null) {
            properties.append("\"fillColor\":\"" + SymbolUtilities.colorToHexString(fillColor, false) + "\",");
            properties.append("\"fillOpacity\":" + String.valueOf(fillColor.getAlpha() / 255f) + ",");
        }
        String strokeWidth = String.valueOf(lineWidth);
        properties.append("\"strokeWidth\":" + strokeWidth + ",");
        properties.append("\"strokeWeight\":" + strokeWidth + "");
        properties.append("}");

        //generate JSON geometry for feature
        geometry.append("\"geometry\":{\"type\":");
        geometry.append(geometryType);
        geometry.append(",\"coordinates\":[");

        ArrayList shapesArray = shapeInfo.getPolylines();

        for (int i = 0; i < shapesArray.size(); i++) {
            ArrayList pointList = (ArrayList) shapesArray.get(i);

            normalize = normalizePoints(pointList, ipc);

            geometry.append("[");

            //System.out.println("Pixel Coords:");
            for (int j = 0; j < pointList.size(); j++) {
                Point2D coord = (Point2D) pointList.get(j);
                Point2D geoCoord = ipc.PixelsToGeo(coord);
                //M. Deutch 9-27-11
                if (normalize) {
                    geoCoord = NormalizeCoordToGECoord(geoCoord);
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
            }
            if (i < len - 1) {
                fc.append(",");
            }
        }

        int len2 = modifiers.size();

        for (int j = 0; j < len2; j++) {
            tempModifier = modifiers.get(j);

            String labelsToAdd = LabelToGeoJSONString(tempModifier, ipc, normalize, textColor, textBackgroundColor);
            if (labelsToAdd.length() > 0) {
                fc.append(",");
                fc.append(labelsToAdd);
            }
        }
        fc.append("]");
        String GeoJSON = fc.toString();
        return GeoJSON;
    }

    /**
     *
     * @param urlImage
     * @param ipc
     * @param symbolBounds
     * @param normalize
     * @return
     */
    private static String GenerateGroundOverlayKML(
            String urlImage, IPointConversion ipc,
            Rectangle symbolBounds,
            boolean normalize)//, ArrayList<ShapeInfo> shapes)
    {
        //int shapeType = -1;
        double x = 0;
        double y = 0;
        double height = 0;
        double width = 0;
        StringBuilder sb = new StringBuilder();
        Boolean lineFill = false;
        Map<String, String> params = null;
        int symbolSize = 0;
        int imageOffset = 0;

        try {
            //if it's a line pattern, we need to know how big the symbols
            //are so we can increase the size of the image.
            int index = -1;
            index = urlImage.indexOf(SYMBOL_LINE_IDS);

            if (index > 0)//if(urlImage contains SYMBOL_LINE_IDS)
            {
                lineFill = true;
                if (params.containsKey(SYMBOL_FILL_ICON_SIZE)) {
                    String size = (String) params.get(SYMBOL_FILL_ICON_SIZE);
                    symbolSize = Integer.decode(size);// getInteger(size);
                } else {
                }
                imageOffset = (symbolSize / 2) + 3;//+3 to make room for rotation
            }

            Rectangle bounds = null;
            bounds = symbolBounds;
            height = bounds.getHeight() + (imageOffset * 2);
            width = bounds.getWidth() + (imageOffset * 2);
            x = bounds.getX() - imageOffset;
            y = bounds.getY() - imageOffset;

            Point2D coord = (Point2D) new Point2D.Double(x, y);
            Point2D topLeft = ipc.PixelsToGeo(coord);
            coord = (Point2D) new Point2D.Double(x + width, y + height);
            Point2D bottomRight = ipc.PixelsToGeo(coord);

            if (normalize) {
                topLeft = NormalizeCoordToGECoord(topLeft);
                bottomRight = NormalizeCoordToGECoord(bottomRight);
            }

            String cdataStart = "<![CDATA[";
            String cdataEnd = "]]>";
            //build kml
            sb.append("<GroundOverlay>");
            sb.append("<name>symbol fill</name>");
            sb.append("<description>symbol fill</description>");
            sb.append("<Icon>");
            sb.append("<href>");
            sb.append(cdataStart);
            sb.append(urlImage);
            sb.append(cdataEnd);
            sb.append("</href>");
            sb.append("</Icon>");
            sb.append("<LatLonBox>");
            sb.append("<north>");
            sb.append(String.valueOf(topLeft.getY()));
            sb.append("</north>");
            sb.append("<south>");
            sb.append(String.valueOf(bottomRight.getY()));
            sb.append("</south>");
            sb.append("<east>");
            sb.append(String.valueOf(bottomRight.getX()));
            sb.append("</east>");
            sb.append("<west>");
            sb.append(String.valueOf(topLeft.getX()));
            sb.append("</west>");
            sb.append("<rotation>");
            sb.append(0);
            sb.append("</rotation>");
            sb.append("</LatLonBox>");
            sb.append("</GroundOverlay>");
        } catch (Exception exc) {
            System.out.println(exc.getMessage());
            exc.printStackTrace();
        }
        String kml = sb.toString();
        return kml;
    }

    /**
     * 
     * @param shapes
     * @param modifiers
     * @param ipc
     * @param normalize
     * @deprecated
     */
    private static void MakeWWReady(
            ArrayList<ShapeInfo> shapes,
            ArrayList<ShapeInfo> modifiers,
            IPointConversion ipc,
            boolean normalize) {
        ShapeInfo temp = null;
        int len = shapes.size();
        for (int i = 0; i < len; i++) {

            temp = ShapeToWWReady(shapes.get(i), ipc, normalize);
            shapes.set(i, temp);

        }

        int len2 = modifiers.size();
        ShapeInfo tempModifier = null;
        for (int j = 0; j < len2; j++) {

            tempModifier = modifiers.get(j);

            //Do we need this for World Wind?
            tempModifier = LabelToWWReady(tempModifier, ipc, normalize);
            modifiers.set(j, tempModifier);

        }

    }

    private static Boolean normalizePoints(ArrayList<Point2D.Double> shape, IPointConversion ipc) {
        ArrayList geoCoords = new ArrayList();
        int n = shape.size();
        //for (int j = 0; j < shape.size(); j++) 
        for (int j = 0; j < n; j++) {
            Point2D coord = shape.get(j);
            Point2D geoCoord = ipc.PixelsToGeo(coord);
            geoCoord = NormalizeCoordToGECoord(geoCoord);
            double latitude = geoCoord.getY();
            double longitude = geoCoord.getX();
            Point2D pt2d = new Point2D.Double(longitude, latitude);
            geoCoords.add(pt2d);
        }
        Boolean normalize = crossesIDL(geoCoords);
        return normalize;
    }

    private static Boolean IsOnePointSymbolCode(String symbolCode) {
        int symStd = RendererSettings.getInstance().getSymbologyStandard();
        String basicCode = SymbolUtilities.getBasicSymbolID(symbolCode);

        //some airspaces affected
        if (symbolCode.equals("CAKE-----------")) {
            return true;
        } else if (symbolCode.equals("CYLINDER-------")) {
            return true;
        } else if (symbolCode.equals("RADARC---------")) {
            return true;
        }

        return false;
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

        String lineStyleId = "lineColor";

        BasicStroke stroke = null;
        int lineWidth = 4;

        symbolCode = JavaRendererUtilities.normalizeSymbolCode(symbolCode);

        String cdataStart = "<![CDATA[";
        String cdataEnd = "]]>";

        kml.append("<Placemark>");//("<Placemark id=\"" + id + "_mg" + "\">");
        kml.append("<description>" + cdataStart + "<b>" + name + "</b><br/>" + "\n" + description + cdataEnd + "</description>");
        kml.append("<Style id=\"" + lineStyleId + "\">");

        lineColor = shapeInfo.getLineColor();
        if (lineColor != null) {
            googleLineColor = Integer.toHexString(shapeInfo.getLineColor().toARGB());

            stroke = (BasicStroke) shapeInfo.getStroke();

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
        if (fillColor != null) {
            googleFillColor = Integer.toHexString(shapeInfo.getFillColor().toARGB());

            googleFillColor = JavaRendererUtilities.ARGBtoABGR(googleFillColor);

            kml.append("<PolyStyle>");
            kml.append("<color>" + googleFillColor + "</color>");
            kml.append("<colorMode>normal</colorMode>");
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
            normalize = normalizePoints(shape, ipc);
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
                        geoCoord = NormalizeCoordToGECoord(geoCoord);
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
                if (normalize == false && IsOnePointSymbolCode(symbolCode)) {
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

    /**
     * 
     * @param shapeInfo
     * @param ipc
     * @param normalize
     * @return
     * @deprecated
     */
    private static ShapeInfo ShapeToWWReady(
            ShapeInfo shapeInfo,
            IPointConversion ipc,
            boolean normalize) {

        ArrayList shapesArray = shapeInfo.getPolylines();
        int len = shapesArray.size();

        for (int i = 0; i < len; i++) {
            ArrayList shape = (ArrayList) shapesArray.get(i);

            if (shapeInfo.getLineColor() != null) {
                int n = shape.size();
                //for (int j = 0; j < shape.size(); j++) 
                for (int j = 0; j < n; j++) {
                    Point2D coord = (Point2D) shape.get(j);
                    Point2D geoCoord = ipc.PixelsToGeo(coord);
                    //M. Deutch 9-26-11
                    if (normalize) {
                        geoCoord = NormalizeCoordToGECoord(geoCoord);
                    }

                    shape.set(j, geoCoord);

                }

            }

            if (shapeInfo.getFillColor() != null) {
                int n = shape.size();
                //for (int j = 0; j < shape.size(); j++) 
                for (int j = 0; j < n; j++) {
                    Point2D coord = (Point2D) shape.get(j);
                    Point2D geoCoord = ipc.PixelsToGeo(coord);
                    //M. Deutch 9-26-11
                    //commenting these two lines seems to help with fill not go around the pole
                    //if(normalize)
                    //geoCoord=NormalizeCoordToGECoord(geoCoord);

                    shape.set(j, geoCoord);
                }
            }
        }

        return shapeInfo;
    }

    private static ShapeInfo LabelToWWReady(ShapeInfo shapeInfo,
            IPointConversion ipc,
            boolean normalize) {

        try {
            Point2D coord = (Point2D) new Point2D.Double(shapeInfo.getGlyphPosition().getX(), shapeInfo.getGlyphPosition().getY());
            Point2D geoCoord = ipc.PixelsToGeo(coord);
            //M. Deutch 9-26-11
            if (normalize) {
                geoCoord = NormalizeCoordToGECoord(geoCoord);
            }
            double latitude = geoCoord.getY();
            double longitude = geoCoord.getX();
            long angle = Math.round(shapeInfo.getModifierStringAngle());

            String text = shapeInfo.getModifierString();

            if (text != null && text.equals("") == false) {
                shapeInfo.setModifierStringPosition(geoCoord);
            } else {
                return null;
            }
        } catch (Exception exc) {
            System.err.println(exc.getMessage());
            exc.printStackTrace();
        }

        return shapeInfo;
    }

    /**
     * Google earth centers text on point rather than drawing from that point.
     * So we need to adjust the point to where the center of the text would be.
     *
     * @param modifier
     */
    private static void AdjustModifierPointToCenter(ShapeInfo modifier) {
        AffineTransform at = null;
        try {
            Rectangle bounds2 = modifier.getTextLayout().getBounds();
            Rectangle2D bounds = new Rectangle2D.Double(bounds2.x, bounds2.y, bounds2.width, bounds2.height);
        } catch (Exception exc) {
            System.err.println(exc.getMessage());
            exc.printStackTrace();
        }
    }

    /**
     * 
     * @param shapeInfo
     * @param ipc
     * @param geMap
     * @param normalize
     * @return
     * @deprecated
     */
    private static String ShapeToJSONString(ShapeInfo shapeInfo, IPointConversion ipc, Boolean geMap, boolean normalize) {
        StringBuilder JSONed = new StringBuilder();
        /*
         NOTE: Google Earth / KML colors are backwards.
         They are ordered Alpha,Blue,Green,Red, not Red,Green,Blue,Aplha like the rest of the world
         * */
        String fillColor = null;
        String lineColor = null;

        if (shapeInfo.getLineColor() != null) {
            lineColor = Integer.toHexString(shapeInfo.getLineColor().toARGB());
            if (geMap) {
                lineColor = JavaRendererUtilities.ARGBtoABGR(lineColor);
            }

        }
        if (shapeInfo.getFillColor() != null) {
            fillColor = Integer.toHexString(shapeInfo.getFillColor().toARGB());
            if (geMap) {
                fillColor = JavaRendererUtilities.ARGBtoABGR(fillColor);
            }
        }

        BasicStroke stroke = null;
        stroke = (BasicStroke) shapeInfo.getStroke();
        int lineWidth = 4;

        if (stroke != null) {
            lineWidth = (int) stroke.getLineWidth();
        }

        ArrayList shapesArray = shapeInfo.getPolylines();
        int n = shapesArray.size();
        //for (int i = 0; i < shapesArray.size(); i++) 
        for (int i = 0; i < n; i++) {
            ArrayList shape = (ArrayList) shapesArray.get(i);

            if (fillColor != null) {
                JSONed.append("{\"polygon\":[");
            } else {
                JSONed.append("{\"line\":[");
            }

            int t = shape.size();
            //for (int j = 0; j < shape.size(); j++) 
            for (int j = 0; j < t; j++) {
                Point2D coord = (Point2D) shape.get(j);
                Point2D geoCoord = ipc.PixelsToGeo(coord);
                //M. Deutch 9-27-11
                if (normalize) {
                    geoCoord = NormalizeCoordToGECoord(geoCoord);
                }
                double latitude = geoCoord.getY();
                double longitude = geoCoord.getX();

                //diagnostic M. Deutch 10-18-11
                //set the point as geo so that the 
                coord = new Point2D.Double(longitude, latitude);
                shape.set(j, coord);

                JSONed.append("[");
                JSONed.append(longitude);
                JSONed.append(",");
                JSONed.append(latitude);
                JSONed.append("]");

                if (j < (shape.size() - 1)) {
                    JSONed.append(",");
                }
            }

            JSONed.append("]");
            if (lineColor != null) {
                JSONed.append(",\"lineColor\":\"");
                JSONed.append(lineColor);

                JSONed.append("\"");
            }
            if (fillColor != null) {
                JSONed.append(",\"fillColor\":\"");
                JSONed.append(fillColor);
                JSONed.append("\"");
            }

            JSONed.append(",\"lineWidth\":\"");
            JSONed.append(String.valueOf(lineWidth));
            JSONed.append("\"");

            JSONed.append("}");

            if (i < (shapesArray.size() - 1)) {
                JSONed.append(",");
            }
        }

        return JSONed.toString();
    }

    private static String LabelToKMLString(ShapeInfo shapeInfo, IPointConversion ipc, boolean normalize, Color textColor) {
        java.lang.StringBuilder kml = new java.lang.StringBuilder();

        //Point2D coord = (Point2D) new Point2D.Double(shapeInfo.getGlyphPosition().getX(), shapeInfo.getGlyphPosition().getY());
        Point2D coord = (Point2D) new Point2D.Double(shapeInfo.getModifierStringPosition().getX(), shapeInfo.getModifierStringPosition().getY());
        Point2D geoCoord = ipc.PixelsToGeo(coord);
        //M. Deutch 9-26-11
        if (normalize) {
            geoCoord = NormalizeCoordToGECoord(geoCoord);
        }
        double latitude = Math.round(geoCoord.getY() * 100000000.0) / 100000000.0;
        double longitude = Math.round(geoCoord.getX() * 100000000.0) / 100000000.0;
        long angle = Math.round(shapeInfo.getModifierStringAngle());

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
            kml.append("<scale>.7</scale>");
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

    /**
     * 
     * @param shapeInfo
     * @param ipc
     * @param normalize
     * @return
     * @deprecated
     */
    private static String LabelToJSONString(ShapeInfo shapeInfo, IPointConversion ipc, boolean normalize) {
        StringBuilder JSONed = new StringBuilder();
        /*
         NOTE: Google Earth / KML colors are backwards.
         They are ordered Alpha,Blue,Green,Red, not Red,Green,Blue,Aplha like the rest of the world
         * */
        JSONed.append("{\"label\":");

        Point2D coord = (Point2D) new Point2D.Double(shapeInfo.getGlyphPosition().getX(), shapeInfo.getGlyphPosition().getY());
        Point2D geoCoord = ipc.PixelsToGeo(coord);
        if (normalize) {
            geoCoord = NormalizeCoordToGECoord(geoCoord);
        }
        double latitude = geoCoord.getY();
        double longitude = geoCoord.getX();
        double angle = shapeInfo.getModifierStringAngle();
        coord.setLocation(longitude, latitude);

        shapeInfo.setGlyphPosition(coord);

        String text = shapeInfo.getModifierString();

        if (text != null && text.equals("") == false) {
            JSONed.append("[");
            JSONed.append(longitude);
            JSONed.append(",");
            JSONed.append(latitude);
            JSONed.append("]");

            JSONed.append(",\"text\":\"");
            JSONed.append(text);
            JSONed.append("\"");

            JSONed.append(",\"angle\":\"");
            JSONed.append(angle);
            JSONed.append("\"}");
        } else {
            return "";
        }

        return JSONed.toString();
    }

    static String canRenderMultiPoint(MilStdSymbol symbol) {
        int symStd = symbol.getSymbologyStandard();
        String symbolID = symbol.getSymbolID();
        String basicID = SymbolUtilities.getBasicSymbolID(symbolID);
        SymbolDef sd = null;
        int dc = 99;
        int coordCount = symbol.getCoordinates().size();

        try {

            String message = "";
            if (SymbolDefTable.getInstance().HasSymbolDef(basicID, symStd)) {
                sd = SymbolDefTable.getInstance().getSymbolDef(basicID, symStd);
            }

            if (sd != null) {
                dc = sd.getDrawCategory();
                if (coordCount < sd.getMinPoints()) {
                    message = ("symbolID: \"" + symbolID + "\" requires a minimum of " + String.valueOf(sd.getMinPoints()) + " points. " + String.valueOf(coordCount) + " are present.");
                    return message;
                }
            } else if (symbolID.startsWith("BS_")) {
                //Will need to be updated to do a more thorough check for
                //basic shapes and buffered basic shapes.
                //Return true for now.
                return "true";
            }else if (symbolID.startsWith("BBS_")) {
            	ArrayList<Double> AM = symbol.getModifiers_AM_AN_X(ModifiersTG.AM_DISTANCE);
            	if(AM != null && AM.size() > 0 && AM.get(0) >= 0)
            		return "true";
            	else
            		return "false: Buffered Basic Shapes require a width (AM)";
            } else {
                return ("symbolID: \"" + symbolID + "\" not recognized.");
            }

            //now check for required modifiers\
            ArrayList<Double> AM = symbol.getModifiers_AM_AN_X(ModifiersTG.AM_DISTANCE);
            ArrayList<Double> AN = symbol.getModifiers_AM_AN_X(ModifiersTG.AN_AZIMUTH);
            String result = hasRequiredModifiers(symbolID, dc, AM, AN);

            if (result.equals("true") == false) {
                return result;
            } else {
                return "true";
            }
        } catch (Exception exc) {
            ErrorLogger.LogException("MultiPointHandler", "canRenderMultiPoint", exc);
            return "true";
        }
    }
    static private String AddImageFillToKML(ArrayList<POINT2> tgPoints,
            String jsonContent, SparseArray symbolModifiers, IPointConversion ipc, Boolean normalize)  //symbolModifiers was MilStdSymbol mSymbol
    {
        if(tgPoints==null || tgPoints.size()==0)
            return null;
        //get original point values in pixel form                    
        ArrayList<Point2D> pixelPoints = new ArrayList<Point2D>();
        //Path2D path = new Path2D.Double();
        GeneralPath path = new GeneralPath();

        //for(JavaLineArray.POINT2 pt : tgPoints)
        int kcount = tgPoints.size();
        POINT2 tpTemp = null;
        for(int k = 0; k < kcount;k++)
        {
            tpTemp = tgPoints.get(k);
            pixelPoints.add(new Point2D.Double(tpTemp.x, tpTemp.y));
            if(k>0)
            {
                path.lineTo(tpTemp.x, tpTemp.y);
            }
            else
            {
                path.moveTo(tpTemp.x, tpTemp.y);
            }
        }
        Rectangle rect = path.getBounds();
        //get url for the fill or line pattern PNG
        //String goImageUrl = SECWebRenderer.GenerateSymbolLineFillUrl(mSymbol.getModifierMap(), pixelPoints,rect);
        String goImageUrl = SECWebRenderer.GenerateSymbolLineFillUrl(symbolModifiers, pixelPoints,rect);
        //generate the extra KML needed to insert the image
        String goKML = GenerateGroundOverlayKML(goImageUrl,ipc,rect,normalize);
        goKML += "</Folder>";

        //StringBuilder sb = new StringBuilder();
        //sb.replace(start, end, str)
        jsonContent = jsonContent.replace("</Folder>", goKML);
        
        return jsonContent;
    }

    static private String hasRequiredModifiers(String symbolID, int dc, ArrayList<Double> AM, ArrayList<Double> AN) {

        String message = symbolID;
        try {
            if ((dc >= 16 && dc <= 20)) {
                if (dc == SymbolDef.DRAW_CATEGORY_CIRCULAR_PARAMETERED_AUTOSHAPE)//16
                {
                    if (AM != null && AM.size() > 0) {
                        return "true";
                    } else {
                        message += " requires a modifiers object that has 1 distance/AM value.";
                        return message;
                    }
                } else if (dc == SymbolDef.DRAW_CATEGORY_RECTANGULAR_PARAMETERED_AUTOSHAPE)//17
                {
                    if (AM != null && AM.size() >= 2
                            && AN != null && AN.size() >= 1) {
                        return "true";
                    } else {
                        message += (" requires a modifiers object that has 2 distance/AM values and 1 azimuth/AN value.");
                        return message;
                    }
                } else if (dc == SymbolDef.DRAW_CATEGORY_SECTOR_PARAMETERED_AUTOSHAPE)//18
                {
                    if (AM != null && AM.size() >= 2
                            && AN != null && AN.size() >= 2) {
                        return "true";
                    } else {
                        message += (" requires a modifiers object that has 2 distance/AM values and 2 azimuth/AN values per sector.  The first sector can have just one AM value although it is recommended to always use 2 values for each sector.");
                        return message;
                    }
                } else if (dc == SymbolDef.DRAW_CATEGORY_CIRCULAR_RANGEFAN_AUTOSHAPE)//19
                {
                    if (AM != null && AM.size() > 0) {
                        return "true";
                    } else {
                        message += (" requires a modifiers object that has at least 1 distance/AM value");
                        return message;
                    }
                } else if (dc == SymbolDef.DRAW_CATEGORY_TWO_POINT_RECT_PARAMETERED_AUTOSHAPE)//20
                {
                    if (AM != null && AM.size() > 0) {
                        return "true";
                    } else {
                        message += (" requires a modifiers object that has 1 distance/AM value.");
                        return message;
                    }
                } else {
                    //should never get here
                    return "true";
                }
            } else {
                //no required parameters
                return "true";
            }
        } catch (Exception exc) {
            ErrorLogger.LogException("MultiPointHandler", "hasRequiredModifiers", exc);
            return "true";
        }
    }
}
