/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sec.web.render;

import sec.geo.GeoPoint;
import sec.geo.kml.KmlOptions;
import sec.geo.kml.KmlRenderer;
import sec.geo.kml.XsltCoordinateWrapper;
import sec.geo.shape.Cake;
import sec.geo.shape.Radarc;
import sec.geo.shape.Route;
import sec.geo.shape.Track;
import sec.web.exceptions.InvalidNumberOfPointsException;

/**
 * Responsible for generating the KML for all 3D shapes.  This
 * 
 * 
 * @author stephen.pinizzotto
 */
@SuppressWarnings({"unused"})
public class Shape3DHandler {

    // constants for the available shape types that can
    // be generated into KML
    public static final String CYLINDER = "CYLINDER-------";
    public static final String ORBIT = "ORBIT----------";
    public static final String ROUTE = "ROUTE----------";
    public static final String POLYGON = "POLYGON--------";
    public static final String RADARC = "RADARC---------";
    public static final String POLYARC = "POLYARC--------";
    public static final String CAKE = "CAKE-----------";
    public static final String TRACK = "TRACK----------";
    // Attribute names of the 3D shapes
    public static final String ATTRIBUTES = "attributes";
    public static final String MIN_ALT = "minalt";
    public static final String MAX_ALT = "maxalt";
    public static final String RADIUS1 = "radius1";
    public static final String RADIUS2 = "radius2";
    public static final String LEFT_AZIMUTH = "leftAzimuth";
    public static final String RIGHT_AZIMUTH = "rightAzimuth";
    // Arbitrary default values of attributes
    public static final double MIN_ALT_DEFAULT = 0.0D;
    public static final double MAX_ALT_DEFAULT = 100.0D;
    public static final double RADIUS1_DEFAULT = 50.0D;
    public static final double RADIUS2_DEFAULT = 100.0D;
    public static final double LEFT_AZIMUTH_DEFAULT = 0.0D;
    public static final double RIGHT_AZIMUTH_DEFAULT = 90.0D;
    
    public static final String DEFAULT_ATTRIBUTES = "[{radius1:"
            + RADIUS1_DEFAULT + ",radius2:"
            + RADIUS2_DEFAULT + ",minalt:"
            + MIN_ALT_DEFAULT + ",maxalt:"
            + MAX_ALT_DEFAULT + ",rightAzimuth:"
            + RIGHT_AZIMUTH_DEFAULT + ",leftAzimuth:"
            + LEFT_AZIMUTH_DEFAULT + "}]";
    // Error messages
    public static final String ERR_ATTRIBUTES_NOT_FORMATTED = "{\"type\":\"error\","
            + "\"error\":\"The attribute paramaters are not formatted "
            + "correctly";
    public static final String ERR_COORDINATES_NOT_FORMATTED = "{\"type\":\"error\",\""
            + "error\":\"There was an error creating the Symbol - the "
            + "coordinates were not formatted correctly";
    public static final String ERR_GENERAL_ERROR = "{\"type\":\"error\",\"error\""
            + ":\"There was an error creating the Symbol - An unknown error "
            + "occurred.  Please refer to the stack trace";
    public static final String ERR_INVALID_NUMBER_POINTS_ERROR = "{\"type\":\""
            + "error\","
            + "\"error\":\"Not enough points were passed in to create a "
            + "graphic.";	

    /**
     * Generates the KML for a 3D symbol.  Symbol should include the all the
     * attributes and information required by the KML for output.  
     * 
     * @param name The user displayed name for the symbol.  Users will use this 
     * to identify with the symbol.
     * @param id An internally used unique id that developers can use to 
     * uniquely distinguish this symbol from others.
     * @param shapeType A 15 character ID of the type of symbol to draw.
     * @param description A brief description of what the symbol represents.  
     * Generic text that does not require any format.
     * @param color The fill color of the graphic
     * @param altitudeMode Indicates whether the symbol should interpret 
     * altitudes as above sea level or above ground level. Options are 
     * "relativeToGround" (from surface of earth), "absolute" (sea level), 
     * "relativeToSeaFloor" (from the bottom of major bodies of water).
     * @param controlPoints The vertices of the shape.  The number of required
     * vertices varies based on the shapeType of the symbol.  The simplest shape 
     * requires at least one point.  Shapes that require more points than 
     * required will ignore extra points.  Format for numbers is as follows: 
     * <br/><br/>
     * "x,y,z [x,y,z ]..."
     * @param attributes A JSON  A JSON array holding the parameters for the 
     * shape.  Attributes should be of the following format: <br/><br/>
     * <tr><code>{"attributes":[{"<i>attribute1</i>":<i>value</i>,...},{<i>[optional]</i>]}</code></tr>
     * @return A KML string that represents a placemark for the 3D shape
     */
    public static String render3dSymbol(String name, String id, String shapeType,
            String description, String lineColor, String fillColor, String altitudeMode,
            String controlPoints,
            SymbolModifiers attributes) {

        String result = "";
        
        KmlOptions.AltitudeMode convertedAltitudeMode = KmlOptions.AltitudeMode.RELATIVE_TO_GROUND;

        // Convert altitude mode to an enum that we understand.  If it does not
        // understand or is "", then convert to ALTITUDE_RELATIVE_TO_GROUND.
        if (!altitudeMode.equals(""))
        {
            convertedAltitudeMode = KmlOptions.AltitudeMode.fromString(altitudeMode);
        }
                
        if (shapeType.equals(CYLINDER)) {
            result = buildCylinder(controlPoints, id, name, description, lineColor, fillColor, convertedAltitudeMode, attributes);            
        } else if (shapeType.equals(ORBIT)) {
            result = buildOrbit(controlPoints, id, name, description, lineColor, fillColor, convertedAltitudeMode, attributes);
        } else if (shapeType.equals(RADARC)) {
            result = buildRadarc(controlPoints, id, name, description, lineColor, fillColor, convertedAltitudeMode, attributes);
        } else if (shapeType.equals(POLYARC)) {
            result = buildPolyArc(controlPoints, id, name, description, lineColor, fillColor, convertedAltitudeMode, attributes);
        } else if (shapeType.equals(ROUTE)) {
            result = buildRoute(controlPoints, id, name, description, lineColor, fillColor, convertedAltitudeMode, attributes);
        } else if (shapeType.equals(POLYGON)) {
            result = buildPolygon(controlPoints, id, name, description, lineColor, fillColor, convertedAltitudeMode, attributes);
        } else if (shapeType.equals(CAKE)) {
            result = buildCake(controlPoints, id, name, description, lineColor, fillColor, convertedAltitudeMode, attributes);
        } else if (shapeType.equals(TRACK)) {
            result = buildTrack(controlPoints, id, name, description, lineColor, fillColor, convertedAltitudeMode, attributes);
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("Given shape type \""+shapeType+"\" does not match any of the available shape types.");
            sb.append("\n");
            sb.append("Available Types: ");
            sb.append(CYLINDER+"\n");
            sb.append(ORBIT+"\n");
            sb.append(ROUTE+"\n");
            sb.append(POLYGON+"\n");
            sb.append(RADARC+"\n");
            sb.append(POLYARC+"\n");
            sb.append(CAKE+"\n");
            sb.append(TRACK);

            System.out.println(sb.toString());

        }

        //System.out.println(result);
        //System.out.println("Render3DSymbolExited");
        return result;
    }
        

    /**
     * Builds a 3d Polygon with elevation.
     * 
     * @param controlPoints
     * @param attributes
     * @return 
     */
    public static String buildPolygon(String controlPoints, String id, 
            String name, String description, String lineColor, String fillColor, KmlOptions.AltitudeMode altitudeMode, 
            SymbolModifiers attributes) {
                
        StringBuilder output = new StringBuilder();       
        String pointArrayStringList = "";
       
        try {
            
            // Get the points of the icons.  For the polyarc we need only
            // one point, the pivot point, then the rest of the points for the 
            // polygon.  
            String[] latlons = controlPoints.split(" ");
            if (latlons.length >= 2) {

                // Build the polyarc
                pointArrayStringList = XsltCoordinateWrapper.getPolygonKml(
                        latlons, id, name, description, lineColor, fillColor, altitudeMode,
                        attributes.X_ALTITUDE_DEPTH.get(0), 
                        attributes.X_ALTITUDE_DEPTH.get(1));
            } else {
                // throw illegal number of points exception
                throw new InvalidNumberOfPointsException();
            }       
        } catch (Exception e) {
            pointArrayStringList = "";                                     
        }

        return pointArrayStringList;

    }

    /**
     * Builds a cylinder given a point. Can also apply a radius size and an
     * altitude for lower and upper boundaries.
     * @param controlPoints 
     * @param attributes
     * @return 
     */
    public static String buildCylinder(String controlPoints, String id, 
            String name, String description, String lineColor, String fillColor, KmlOptions.AltitudeMode altitudeMode, 
            SymbolModifiers attributes) {
        StringBuilder output = new StringBuilder();        
        String pointArrayStringList = "";

        // variables for the cylinder position
        double pivotx = 0.0D;
        double pivoty = 0.0D;

        try {           
            
            // Get the points of the icons.  For the cylinder we need only
            // one point.  Ignore any more than the first point.
            String[] latlons = controlPoints.split(" ");

            if (latlons.length > 0) {
                String[] pivot = latlons[0].split(",");

                if (pivot.length >= 2) {
                    pivotx = Double.parseDouble(pivot[0]);
                    pivoty = Double.parseDouble(pivot[1]);
                } else {
                    throw new NumberFormatException();
                }
            } else {
                // throw an illegal number of points exception
                throw new InvalidNumberOfPointsException();
            }
                        
            // Build the cylinder
            pointArrayStringList = XsltCoordinateWrapper.getCircleKml(pivotx,
                    pivoty, id, name, description, lineColor, fillColor, altitudeMode,
                    attributes.AM_DISTANCE.get(0),
                    attributes.X_ALTITUDE_DEPTH.get(0),
                    attributes.X_ALTITUDE_DEPTH.get(1));                       
        } catch (Exception e) {
            pointArrayStringList = "";                                     
        }

        return pointArrayStringList;

    }

    public static String buildKml(String[] coords, String id, String name, String lineColor, String fillColor) {
        StringBuilder kml = new StringBuilder();
        kml.append("<Placemark>");
        kml.append("<name>");
        kml.append(name);
        kml.append("</name>");
        kml.append("<id>");
        kml.append(id);
        kml.append("</id>");
        kml.append("<Style>");
        kml.append("<PolyStyle>");
        kml.append("<color>");
        kml.append(fillColor);
        kml.append("</color>");

        kml.append("</PolyStyle>");
        kml.append("<LineStyle>");
        kml.append(lineColor);
        kml.append("</LineStyle>");
        kml.append("</Style>");
        kml.append("<MultiGeometry>");
        for (String s : coords) {
            kml.append("<Polygon>");
            kml.append("<extrude>0</extrude>");
            kml.append("<altitudeMode>relativeToGround</altitudeMode>");
            kml.append("<outerBoundaryIs>");
            kml.append("<LinearRing>");
            kml.append("<coordinates>");
            kml.append(s);
            kml.append("</coordinates>");
            kml.append("</LinearRing>");
            kml.append("</outerBoundaryIs>");
            kml.append("</Polygon>");
        }
        kml.append("</MultiGeometry>");
        kml.append("</Placemark>");
        return kml.toString();
    }

    /**
     * Build an Orbit graphic.  
     * 
     * @param controlPoints
     * @param attributes
     * @return 
     */
    public static String buildOrbit(String controlPoints, String id, 
            String name, String description, String lineColor, String fillColor, KmlOptions.AltitudeMode altitudeMode, 
            SymbolModifiers attributes) {

        StringBuilder output = new StringBuilder();        
        String pointArrayStringList = "";
        // variables for the cylinder position
        double point1x = 0.0D;
        double point1y = 0.0D;
        double point2x = 0.0D;
        double point2y = 0.0D;

        try {
            
            // Get the points of the icons.  For the cylinder we need only
            // one point.  Ignore any more than the first point.
            String[] latlons = controlPoints.split(" ");
            if (latlons.length > 1) {
                String[] point1 = latlons[0].split(",");

                if (point1.length >= 2) {
                    point1x = Double.parseDouble(point1[0]); // x value
                    point1y = Double.parseDouble(point1[1]); // y value
                } else {
                    throw new NumberFormatException();
                }

                String[] point2 = latlons[1].split(",");

                if (point2.length >= 2) {
                    point2x = Double.parseDouble(point2[0]);  // x value
                    point2y = Double.parseDouble(point2[1]);  // y value
                } else {
                    throw new NumberFormatException();
                }
            } else {
                // throw invalid number of points exception
                throw new InvalidNumberOfPointsException();
            }

            // Build the orbit
            pointArrayStringList = XsltCoordinateWrapper.getOrbitKml(point1x, point1y,
                    point2x, point2y, id, name, description, lineColor, fillColor,  altitudeMode, attributes.AM_DISTANCE.get(0), attributes.X_ALTITUDE_DEPTH.get(0), attributes.X_ALTITUDE_DEPTH.get(1));

        } catch (Exception e) {
            pointArrayStringList = "";                                     
        }

        return pointArrayStringList;

    }

    /**
     * Builds the Radarc graphic
     * 
     * @param controlPoints
     * @param attributes 
     * @return 
     */
    public static String buildRadarc(String controlPoints, String id, 
            String name, String description, String lineColor, String fillColor, KmlOptions.AltitudeMode altitudeMode, 
            SymbolModifiers attributes) {

        StringBuilder output = new StringBuilder();      
        String pointArrayStringList = "";
        
        // variables for the cylinder position
        double pivotx = 0.0D;
        double pivoty = 0.0D;

        try {        

            // Get the points of the icons.  For the radarc we need only
            // one point.  Ignore any more than the first point.
            String[] latlons = controlPoints.split(" ");
            if (latlons.length > 0) {
                String[] pivot = latlons[0].split(",");

                if (pivot.length >= 2) {
                    pivotx = Double.parseDouble(pivot[0]);
                    pivoty = Double.parseDouble(pivot[1]);
                } else {
                    throw new NumberFormatException();
                }
            } else {
                // throw invalid number of points exception
                throw new InvalidNumberOfPointsException();
            }

            // Build the orbit
            pointArrayStringList = XsltCoordinateWrapper.getRadarcKml(pivotx, pivoty,
                    id, name, description, lineColor, fillColor, altitudeMode,
                    attributes.AM_DISTANCE.get(0), 
                    attributes.AM_DISTANCE.get(1), 
                    attributes.AN_AZIMUTH.get(0), 
                    attributes.AN_AZIMUTH.get(1), 
                    attributes.X_ALTITUDE_DEPTH.get(0), 
                    attributes.X_ALTITUDE_DEPTH.get(1));


        } catch (Exception e) {
            pointArrayStringList = "";                                     
        }

        return pointArrayStringList;

    }

    /**
     * Builds the PolyArc graphic
     * 
     * @param controlPoints
     * @param attributes
     * @return 
     */
    public static String buildPolyArc(String controlPoints, String id, 
            String name, String description, String lineColor, String fillColor, KmlOptions.AltitudeMode altitudeMode, 
            SymbolModifiers attributes) {
        StringBuilder output = new StringBuilder();
        
        String pointArrayStringList = "";
        
        // variables for the cylinder position
        double pivotx = 0.0D;
        double pivoty = 0.0D;

        try {
            // Get the points of the icons.  For the polyarc we need only
            // one point, the pivot point, then the rest of the points for the 
            // polygon.  
            String[] latlons = controlPoints.split(" ");
            if (latlons.length >= 3) {
                String[] pivot = latlons[0].split(",");

                if (pivot.length >= 2) {
                    pivotx = Double.parseDouble(pivot[0]);
                    pivoty = Double.parseDouble(pivot[1]);
                } else {
                    throw new NumberFormatException();
                }

                int length = latlons.length - 1;
                String[] points = new String[length];
                System.arraycopy(latlons, 1, points, 0, length);
                              

                // Build the polyarc
                pointArrayStringList = XsltCoordinateWrapper.getPolyarcKml(points,
                        pivotx, pivoty, id, name, description, lineColor, fillColor, altitudeMode,
                        attributes.AM_DISTANCE.get(0),
                        attributes.AN_AZIMUTH.get(0), attributes.AN_AZIMUTH.get(1),
                        attributes.X_ALTITUDE_DEPTH.get(0), attributes.X_ALTITUDE_DEPTH.get(1));

            } else {
                // illegal number of points exception
                throw new InvalidNumberOfPointsException();
            }
        } catch (Exception e) {
            pointArrayStringList = "";                                     
        }

        return pointArrayStringList;

    }

    /**
     * Builds a route graphic.
     * @param controlPoints
     * @param attributes
     * @return 
     */
    public static String buildRoute(String controlPoints, String id, 
            String name, String description, String lineColor, String fillColor, KmlOptions.AltitudeMode altitudeMode, 
            SymbolModifiers attributes) {
        
        String pointArrayStringList = "";  
        double width;
        double leftWidth;
        double rightWidth;               

        try {
            // Get the points of the icons.  For the polyarc we need only
            // one point, the pivot point, then the rest of the points for the 
            // polygon.  
            String[] latlons = controlPoints.split(" ");
            if (latlons.length >= 2) {
                width = attributes.AM_DISTANCE.get(0);
                leftWidth = width / 2;
                rightWidth = width / 2;
                
                // Build the polyarc
                pointArrayStringList = XsltCoordinateWrapper.getRouteKml(latlons,
                        id, name, description, lineColor, fillColor, altitudeMode,
                        leftWidth, rightWidth, attributes.X_ALTITUDE_DEPTH.get(0), attributes.X_ALTITUDE_DEPTH.get(1));

            } else {
                // illegal number of points exception
                throw new InvalidNumberOfPointsException();
            }
        } catch (Exception e) {
            pointArrayStringList = "";                                     
        }

        return pointArrayStringList;

    }

    /**
     * Builds a cake graphic--multiple radarcs stacked on top of each other.
     * <br/>
     * This graphic can take up to 6 attributes.  "radius1" is the max radius, 
     * "radius2" is the min radius, "minalt", "maxalt", "leftAzimuth", and 
     * "rightAzimuth"
     * 
     * @param controlPoints
     * @param attributes
     * @return 
     */
    public static String buildCake(String controlPoints, String id, 
            String name, String description, String lineColor, String fillColor, KmlOptions.AltitudeMode altitudeMode, 
            SymbolModifiers attributes) {

        StringBuilder output = new StringBuilder();        
        String pointArrayStringList = "";

        // Creat a new cake to begin adding layers to.
        Cake letThemEat = new Cake();

        // variables for the cylinder position
        double pivotx = 0.0D;
        double pivoty = 0.0D;

        // Used to store all the polygons for the cakelayers
        //Set<KmlPolygon> combinedCakeLayers = null;

        // Used to generate the points
        KmlRenderer kmlRender = new KmlRenderer();

        try {

            // Get the points of graphic.  
            String[] latlons = controlPoints.split(" ");
            int numberOfPoints = latlons.length;

            if (numberOfPoints > 0) {

                //get the pivot point for this graphics.
                // all cake layers will use this pivot point for its 
                //origin.               
                String[] pivotString = latlons[0].split(",");

                if (pivotString.length >= 2) {
                    pivotx = Double.parseDouble(pivotString[0]);
                    pivoty = Double.parseDouble(pivotString[1]);
                    letThemEat.setPivot(new GeoPoint(pivotx, pivoty));
                } else {
                    throw new NumberFormatException();
                }
                
                int attributesArrayLength = attributes.X_ALTITUDE_DEPTH.size();                                
                
                for (int i = 0; i < attributesArrayLength; i++) {
                    // Create a new cake layer and set its pivot point.
                    Radarc layerCake = new Radarc();
                    layerCake.setAltitudeMode(altitudeMode);
                    layerCake.setPivot(new GeoPoint(pivotx, pivoty));
                    
                    layerCake.setMinRadius(attributes.AM_DISTANCE.get(i));
                    layerCake.setRadius(attributes.AM_DISTANCE.get(i + 1));
                    layerCake.setMinAltitude(attributes.X_ALTITUDE_DEPTH.get(i));
                    layerCake.setMaxAltitude(attributes.X_ALTITUDE_DEPTH.get(i + 1));
                    layerCake.setLeftAzimuthDegrees(attributes.AN_AZIMUTH.get(i));
                    layerCake.setRightAzimuthDegrees(attributes.AN_AZIMUTH.get(i + 1));
                    i++;
                    
                    letThemEat.addLayer(layerCake);

                }

                pointArrayStringList = kmlRender.getCakeKml(letThemEat, id, name, description, lineColor, fillColor);
                
            } else {
                // if not enough points throw exception
                throw new InvalidNumberOfPointsException();
            }        
        } catch (Exception e) {
            pointArrayStringList = "";                                     
        }

        return pointArrayStringList;
    }

    /**
     * Builds a track graphic composed of multiple routes
     * 
     * @param controlPoints
     * @param attributes
     * @return 
     */
    public static String buildTrack(String controlPoints, String id, 
            String name, String description, String lineColor, String fillColor, KmlOptions.AltitudeMode altitudeMode, 
            SymbolModifiers attributes) {
              
        String pointArrayStringList = "";        
        
        // generates the points for the track.
        KmlRenderer kmlRender = new KmlRenderer();

        try {

            // Get the points of graphic.  
            String[] latlons = controlPoints.split(" ");
            int numberOfPoints = latlons.length;            
            
            //ensure enough values in distance and altitude arrays for the number of points M. Deutch 4-15-15
            int numAM=attributes.AM_DISTANCE.size();
            int numX=attributes.X_ALTITUDE_DEPTH.size();
            double nextToLastAlt=attributes.X_ALTITUDE_DEPTH.get(numX-2);
            double lastAlt=attributes.X_ALTITUDE_DEPTH.get(numX-1);
            double lastWidth=attributes.AM_DISTANCE.get(numAM-1);
            int delta=2*(numberOfPoints-1)-numAM;   //one width per segment            
            delta=2*(numberOfPoints-1)-numX;    //two alts per segment
            int j=0;
            if(delta>0)
                for(j=0;j<delta;j++)
                    attributes.AM_DISTANCE.add(lastWidth);
            int k=0;
            while(k<delta)
            {
                attributes.X_ALTITUDE_DEPTH.add(nextToLastAlt);
                attributes.X_ALTITUDE_DEPTH.add(lastAlt);
                k+=2;
            }

            // Ensure the track has an appropriate amount of points.
            if (numberOfPoints >= 2) {                               

                Track track = new Track();                

                for (int i = 0; i < numberOfPoints - 1; i++) {
                    // Create a new route from first point to the next point.
                    Route route = new Route();
                    route.setAltitudeMode(altitudeMode);

                    String[] point1String = latlons[i].split(",");
                    String[] point2String = latlons[i + 1].split(",");

                    double point1lon = 0.0D;
                    double point1lat = 0.0D;
                    double point2lon = 0.0D;
                    double point2lat = 0.0D;
                    if (point1String.length >= 2) {
                        point1lon = Double.parseDouble(point1String[0]);
                        point1lat = Double.parseDouble(point1String[1]);
                    } else {
                        throw new NumberFormatException();
                    }

                    if (point2String.length >= 2) {
                        point2lon = Double.parseDouble(point2String[0]);
                        point2lat = Double.parseDouble(point2String[1]);
                    } else {
                        throw new NumberFormatException();
                    }

                    route.addPoint(new GeoPoint(point1lon, point1lat));
                    route.addPoint(new GeoPoint(point2lon, point2lat));                    
                    route.setLeftWidth(attributes.AM_DISTANCE.get(2 * i));
                    route.setRightWidth(attributes.AM_DISTANCE.get(2 * i + 1));
                    route.setMinAltitude(attributes.X_ALTITUDE_DEPTH.get(2 * i));
                    route.setMaxAltitude(attributes.X_ALTITUDE_DEPTH.get(2 * i + 1));
                    
                    // Add the route to our track
                    track.addRoute(route);

                }
                
                pointArrayStringList = kmlRender.getKml(track, id, name, description, lineColor, fillColor);

            } else {                
                //throw invalid number of points exception
                throw new InvalidNumberOfPointsException();
            }        
        } catch (Exception e) {
            pointArrayStringList = "";                                     
        }      
        return pointArrayStringList;

    }
}
