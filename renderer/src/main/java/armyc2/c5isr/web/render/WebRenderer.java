package armyc2.c5isr.web.render;
// This import is if we need to call a javascript function
// It requires that you import the plugins.jar from the jdk folder into the project libraries
//import netscape.javascript.JSObject;

import android.content.Context;
import armyc2.c5isr.renderer.MilStdIconRenderer;
import armyc2.c5isr.renderer.utilities.ErrorLogger;
import armyc2.c5isr.renderer.utilities.MilStdAttributes;
import armyc2.c5isr.renderer.utilities.MilStdSymbol;
import armyc2.c5isr.renderer.utilities.Modifiers;
import armyc2.c5isr.renderer.utilities.RendererSettings;
import armyc2.c5isr.renderer.utilities.Color;
import armyc2.c5isr.graphics2d.Font;
import armyc2.c5isr.graphics2d.Point2D;
import armyc2.c5isr.graphics2d.Rectangle2D;
import armyc2.c5isr.renderer.utilities.SymbolUtilities;
import armyc2.c5isr.web.render.utilities.JavaRendererUtilities;

import java.util.Map;
import java.util.logging.Level;
import armyc2.c5isr.web.json.utilities.JSONArray;
import armyc2.c5isr.web.json.utilities.JSONException;
import armyc2.c5isr.web.json.utilities.JSONObject;

/**
 * Main class for rendering multi-point graphics such as Control Measures, Atmospheric, and Oceanographic.
 */
//@SuppressWarnings("unused")
public final class WebRenderer /* extends Applet */ {

    @Deprecated
    public static final int OUTPUT_FORMAT_JSON = 1;
    public static final int OUTPUT_FORMAT_GEOJSON = 2;
    public static final int OUTPUT_FORMAT_GEOSVG = 3;


    // Arbitrary default values of attributes
    public static final double MIN_ALT_DEFAULT = 0.0D;
    public static final double MAX_ALT_DEFAULT = 100.0D;
    public static final double RADIUS1_DEFAULT = 50.0D;
    public static final double RADIUS2_DEFAULT = 100.0D;
    public static final double LEFT_AZIMUTH_DEFAULT = 0.0D;
    public static final double RIGHT_AZIMUTH_DEFAULT = 90.0D;
    
    public static final String ERR_ATTRIBUTES_NOT_FORMATTED = "{\"type\":\"error\","
            + "\"error\":\"The attribute paramaters are not formatted "
            + "correctly";
    
    public static final String DEFAULT_ATTRIBUTES = "[{radius1:"
            + RADIUS1_DEFAULT + ",radius2:"
            + RADIUS2_DEFAULT + ",minalt:"
            + MIN_ALT_DEFAULT + ",maxalt:"
            + MAX_ALT_DEFAULT + ",rightAzimuth:"
            + RIGHT_AZIMUTH_DEFAULT + ",leftAzimuth:"
            + LEFT_AZIMUTH_DEFAULT + "}]";

    
    private static boolean _initSuccess = false;
    

    public static synchronized void init(Context context, String cacheDir) {

        try
        {
        	if(_initSuccess == false)
        	{
                    MilStdIconRenderer.getInstance().init(context);
	            //use WebRenderer.setLoggingLevel()
	            
	            //sets default value for single point symbology to have an outline.
	            //outline color will be automatically determined based on line color
	            //unless a color value is manually set.
	            
	            //Set Renderer Settings/////////////////////////////////////////////
	            //RendererSettings.getInstance().setSinglePointSymbolOutlineWidth(1);
                RendererSettings.getInstance().setTextBackgroundMethod(RendererSettings.TextBackgroundMethod_OUTLINE);
	            //RendererSettings.getInstance().setTextBackgroundMethod(RendererSettings.TextBackgroundMethod_OUTLINE_QUICK);
	            //RendererSettings.getInstance().setTextOutlineWidth(2);
	            //RendererSettings.getInstance().setLabelForegroundColor(Color.BLACK.toARGB());
	            //RendererSettings.getInstance().setLabelBackgroundColor(new Color(255, 255, 255, 200).toARGB());
	            RendererSettings.getInstance().setModifierFont("arial", Font.PLAIN, 12);
	            ErrorLogger.setLevel(Level.FINE);
	            _initSuccess = true;
        	}
            
        }
        catch(Exception exc)
        {
            ErrorLogger.LogException("WebRenderer", "init", exc, Level.WARNING);
        }


    }

    
    /**\
     * Set minimum level at which an item can be logged.
     * In descending order:
     * OFF = Integer.MAX_VALUE
     * Severe = 1000
     * Warning = 900
     * Info = 800
     * Config = 700
     * Fine = 500
     * Finer = 400 
     * Finest = 300
     * All = Integer.MIN_VALUE
     * Use like WebRenderer.setLoggingLevel(Level.INFO);
     * or
     * Use like WebRenderer.setLoggingLevel(800);
     * @param level java.util.logging.level
     */
    public static void setLoggingLevel(Level level)
    {
        try
        {
            ErrorLogger.setLevel(level,true);
            ErrorLogger.LogMessage("WebRenderer", "setLoggingLevel(Level)",
                    "Logging level set to: " + ErrorLogger.getLevel().getName(), 
                    Level.CONFIG);
        }
        catch(Exception exc)
        {
            ErrorLogger.LogException("WebRenderer", "setLoggingLevel(Level)", exc, Level.INFO);
        }
    }
    
    /**\
     * Set minimum level at which an item can be logged.
     * In descending order:
     * OFF = Integer.MAX_VALUE
     * Severe = 1000
     * Warning = 900
     * Info = 800
     * Config = 700
     * Fine = 500
     * Finer = 400 
     * Finest = 300
     * All = Integer.MIN_VALUE
     * Use like WebRenderer.setLoggingLevel(Level.INFO);
     * or
     * Use like WebRenderer.setLoggingLevel(800);
     * @param level int
     */
    public static void setLoggingLevel(int level)
    {
        try
        {
            if(level > 1000)
                  ErrorLogger.setLevel(Level.OFF,true);
            else if(level > 900)
                  ErrorLogger.setLevel(Level.SEVERE,true);
            else if(level > 800)
                  ErrorLogger.setLevel(Level.WARNING,true);
            else if(level > 700)
                  ErrorLogger.setLevel(Level.INFO,true);
            else if(level > 500)
                  ErrorLogger.setLevel(Level.CONFIG,true);
            else if(level > 400)
                  ErrorLogger.setLevel(Level.FINE,true);
            else if(level > 300)
                  ErrorLogger.setLevel(Level.FINER,true);
            else if(level > Integer.MIN_VALUE)
                  ErrorLogger.setLevel(Level.FINEST,true);
            else
                ErrorLogger.setLevel(Level.ALL,true);
            
            ErrorLogger.LogMessage("WebRenderer", "setLoggingLevel(int)",
                    "Logging level set to: " + ErrorLogger.getLevel().getName(), 
                    Level.CONFIG);
        }
        catch(Exception exc)
        {
            ErrorLogger.LogException("WebRenderer", "setLoggingLevel(int)", exc, Level.INFO);
        }
    }

    /**
     * Single Point Tactical Graphics are rendered from font files.
     * The font size you specify here determines how big the symbols will 
     * be rendered.  This should be set once at startup.
     * @param size
     */
    public static void setTacticalGraphicPointSize(int size)
    {
//        sps.setTacticalGraphicPointSize(size);
    }
    
    /**
     * Units are rendered from font files.
     * The font size you specify here determines how big the symbols will 
     * be rendered.  This should be set once at startup. 
     * @param size
     */
    public static void setUnitPointSize(int size)
    {
//        sps.setUnitPointSize(size);
    }
    
    /**
     * Modifier Text Color will by default match the line color.
     * This will override all modifier text color.
     * @param hexColor 
     */
/*    public static void setModifierTextColor(String hexColor)
    {
        Color textColor = RendererUtilities.getColorFromHexString(hexColor);
        if(textColor==null)
        {
            textColor = Color.black;
        }
        RendererSettings.getInstance().setLabelForegroundColor(textColor.toARGB());
    }*/


    


    /**
     * Renders all multi-point symbols, creating KML that can be used to draw
     * it on a Google map.  Multipoint symbols cannot be draw the same 
     * at different scales. For instance, graphics with arrow heads will need to 
     * redraw arrowheads when you zoom in on it.  Similarly, graphics like a 
     * Forward Line of Troops drawn with half circles can improve performance if 
     * clipped when the parts of the graphic that aren't on the screen.  To help 
     * readjust graphics and increase performance, this function requires the 
     * scale and bounding box to help calculate the new locations.
     * @param id A unique identifier used to identify the symbol by Google map. 
     * The id will be the folder name that contains the graphic.
     * @param name a string used to display to the user as the name of the 
     * graphic being created.
     * @param description a brief description about the graphic being made and 
     * what it represents.
     * @param symbolCode A 20-30 digit symbolID corresponding to one of the
     * graphics in the MIL-STD-2525D
     * @param controlPoints The vertices of the graphics that make up the
     * graphic.  Passed in the format of a string, using decimal degrees 
     * separating lat and lon by a comma, separating coordinates by a space.  
     * The following format shall be used "x1,y1[,z1] [xn,yn[,zn]]..."
     * @param altitudeMode Indicates whether the symbol should interpret 
     * altitudes as above sea level or above ground level. Options are 
     * "clampToGround", "relativeToGround" (from surface of earth), "absolute" 
     * (sea level), "relativeToSeaFloor" (from the bottom of major bodies of 
     * water).
     * @param scale A number corresponding to how many meters one meter of our 
     * map represents. A value "50000" would mean 1:50K which means for every 
     * meter of our map it represents 50000 meters of real world distance.
     * @param bbox The viewable area of the map.  Passed in the format of a
     * string "lowerLeftX,lowerLeftY,upperRightX,upperRightY." Not required
     * but can speed up rendering in some cases.
     * example: "-50.4,23.6,-42.2,24.2"
     * @param modifiers keyed using constants from Modifiers.
     * Pass in comma delimited String for modifiers with multiple values like AM, AN &amp; X
     * @param attributes keyed using constants from MilStdAttributes.
     * @param format An enumeration: 2 for GeoJSON.
     * @return A JSON string representation of the graphic.
     */
    public static String RenderSymbol(String id, String name, String description,
                                      String symbolCode, String controlPoints, String altitudeMode,
                                      double scale, String bbox, Map<String,String> modifiers, Map<String,String> attributes, int format) {
        String output = "";
        try {         
        	
        	JavaRendererUtilities.addAltModeToModifiersString(attributes,altitudeMode);
        

            output = MultiPointHandler.RenderSymbol(id, name, description, symbolCode, controlPoints,
                    scale, bbox, modifiers, attributes, format);

            //DEBUGGING
            if(ErrorLogger.getLevel().intValue() <= Level.FINER.intValue())
            {
                System.out.println("");
                StringBuilder sb = new StringBuilder();
                sb.append("\nID: " + id + "\n");
                sb.append("Name: " + name + "\n");
                sb.append("Description: " + description + "\n");
                sb.append("SymbolID: " + symbolCode + "\n");
                sb.append("Scale: " + String.valueOf(scale) + "\n");
                sb.append("BBox: " + bbox + "\n");
                sb.append("Coords: " + controlPoints + "\n");
                sb.append("Modifiers: " + modifiers + "\n");
                ErrorLogger.LogMessage("WebRenderer", "RenderSymbol", sb.toString(),Level.FINER);
            }
            if(ErrorLogger.getLevel().intValue() <= Level.FINEST.intValue())
            {
                String briefOutput = output.replaceAll("</Placemark>", "</Placemark>\n");
                briefOutput = output.replaceAll("(?s)<description[^>]*>.*?</description>", "<description></description>");
                ErrorLogger.LogMessage("WebRenderer", "RenderSymbol", "Output:\n" + briefOutput,Level.FINEST);
            }

            
            
        } catch (Exception ea) {
            
            output = "{\"type\":'error',error:'There was an error creating the MilStdSymbol - " + ea.toString() + "'}";
            ErrorLogger.LogException("WebRenderer", "RenderSymbol", ea, Level.WARNING);
        }
        
        return output;
    }
    

         


    /**
     * Renders all multi-point symbols, creating KML or JSON for the user to
     * parse and render as they like.
     * This function requires the bounding box to help calculate the new
     * locations.
     * @param id A unique identifier used to identify the symbol by Google map.
     * The id will be the folder name that contains the graphic.
     * @param name a string used to display to the user as the name of the 
     * graphic being created.
     * @param description a brief description about the graphic being made and 
     * what it represents.
     * @param symbolCode A 20-30 digit symbolID corresponding to one of the
     * graphics in the MIL-STD-2525D
     * @param controlPoints The vertices of the graphics that make up the
     * graphic.  Passed in the format of a string, using decimal degrees
     * separating lat and lon by a comma, separating coordinates by a space.
     * The following format shall be used "x1,y1 [xn,yn]..."
     * @param pixelWidth pixel dimensions of the viewable map area
     * @param pixelHeight pixel dimensions of the viewable map area
     * @param bbox The viewable area of the map.  Passed in the format of a
     * string "lowerLeftX,lowerLeftY,upperRightX,upperRightY."
     * example: "-50.4,23.6,-42.2,24.2"
     * @param modifiers keyed using constants from Modifiers.
     * Pass in comma delimited String for modifiers with multiple values like AM, AN &amp; X
     * @param attributes keyed using constants from MilStdAttributes.
     * @param format An enumeration: 2 for GeoJSON.
     * @return A JSON (1) or KML (0) string representation of the graphic.
     */
    public static String RenderSymbol2D(String id, String name, String description, String symbolCode, String controlPoints,
            int pixelWidth, int pixelHeight, String bbox, Map<String,String> modifiers, Map<String,String> attributes, int format)
    {
        String output = "";
        try
        {
            output = MultiPointHandler.RenderSymbol2D(id, name, description, 
                    symbolCode, controlPoints, pixelWidth, pixelHeight, bbox, 
                    modifiers, attributes, format);
        }
        catch(Exception exc)
        {
            output = "{\"type\":'error',error:'There was an error creating the MilStdSymbol: " + symbolCode + " - " + exc.toString() + "'}";
        }
        return output;
    }



    /**
	 * Renders all MilStd 2525 multi-point symbols, creating MilStdSymbol that contains the
	 * information needed to draw the symbol on the map.
     * DOES NOT support RADARC, CAKE, TRACK etc...
	 * ArrayList&lt;Point2D&gt; milStdSymbol.getSymbolShapes.get(index).getPolylines()
	 * and 
	 * ShapeInfo = milStdSymbol.getModifierShapes.get(index). 
	 * 
	 * 
	 * @param id
	 *            A unique identifier used to identify the symbol by Google map.
	 *            The id will be the folder name that contains the graphic.
	 * @param name
	 *            a string used to display to the user as the name of the
	 *            graphic being created.
	 * @param description
	 *            a brief description about the graphic being made and what it
	 *            represents.
	 * @param symbolCode
	 *            A 20-30 digit symbolID corresponding to one of the graphics
	 *            in the MIL-STD-2525D
	 * @param controlPoints
	 *            The vertices of the graphics that make up the graphic. Passed
	 *            in the format of a string, using decimal degrees separating
	 *            lat and lon by a comma, separating coordinates by a space. The
	 *            following format shall be used "x1,y1[,z1] [xn,yn[,zn]]..."
	 * @param altitudeMode
	 *            Indicates whether the symbol should interpret altitudes as
	 *            above sea level or above ground level. Options are
	 *            "clampToGround", "relativeToGround" (from surface of earth),
	 *            "absolute" (sea level), "relativeToSeaFloor" (from the bottom
	 *            of major bodies of water).
	 * @param scale
	 *            A number corresponding to how many meters one meter of our map
	 *            represents. A value "50000" would mean 1:50K which means for
	 *            every meter of our map it represents 50000 meters of real
	 *            world distance.
	 * @param bbox
	 *            The viewable area of the map. Passed in the format of a string
	 *            "lowerLeftX,lowerLeftY,upperRightX,upperRightY." Not required
	 *            but can speed up rendering in some cases. example:
	 *            "-50.4,23.6,-42.2,24.2"
	 * @param modifiers
	 *            Used like:
	 *            modifiers.put(Modifiers.T_UNIQUE_DESIGNATION_1, "T");
	 *            Or
	 *            modifiers.put(Modifiers.AM_DISTANCE, "1000,2000,3000");
	 * @param attributes
	 * 			  Used like:
	 *            attributes.put(MilStdAttributes.LineWidth, "3");
	 *            Or
	 *            attributes.put(MilStdAttributes.LineColor, "#00FF00");
     * @return MilStdSymbol
     */
    public static MilStdSymbol RenderMultiPointAsMilStdSymbol(String id, String name, String description, String symbolCode,
			String controlPoints, String altitudeMode, double scale, String bbox, Map<String,String> modifiers, Map<String,String> attributes)
    {
		MilStdSymbol mSymbol = null;
		try 
		{
			mSymbol = MultiPointHandler.RenderSymbolAsMilStdSymbol(id, name, description, symbolCode,
                    controlPoints, scale, bbox, modifiers, attributes);

            //Uncomment to show sector1 modifiers as fill pattern
//            int symbolSet = SymbolID.getEntityCode(symbolCode);
//            if(symbolSet == 270707 || symbolSet == 270800 || symbolSet == 270801 || symbolSet == 151100) //Mined Areas
//            {
//                int size = RendererSettings.getInstance().getDefaultPixelSize();
//
//                ArrayList<ShapeInfo> shapes = mSymbol.getSymbolShapes();
//                if(shapes.size() > 0){
//                    ShapeInfo shape = shapes.get(0);
//                    shape.setPatternFillImage(PatternFillRendererD.MakeSymbolPatternFill(symbolCode,size));
//                    if(shape.getPatternFillImage() != null)
//                        shape.setShader(new BitmapShader(shape.getPatternFillImage(), Shader.TileMode.REPEAT, Shader.TileMode.REPEAT));
//                }
//            }
		}
		catch (Exception ea) 
		{
			mSymbol=null;
			ErrorLogger.LogException("WebRenderer", "RenderMultiPointAsMilStdSymbol" + " - " + symbolCode, ea, Level.WARNING);
		}
		
		//System.out.println("RenderMultiPointAsMilStdSymbol exit");
		return mSymbol;
    }


    
    /**
     * Given a symbol code meant for a single point symbol, returns the
     * anchor point at which to display that image based off the image returned
     * from the URL of the SinglePointServer.
     * 
     * @param symbolID - the 20-30 digit symbolID of a single point MilStd2525
     * symbol. 
     * @return A pixel coordinate of the format "x,y".
     * Returns an empty string if an error occurs.
     * @deprecated 
     */
	public String getSinglePointAnchor(String symbolID) {
        String anchorPoint = "";
        Point2D anchor = new Point2D.Double();
        anchorPoint = anchor.getX() + "," + anchor.getY();        
        return anchorPoint;
    }

    /**
     * Given a symbol code meant for a single point symbol, returns the
     * anchor point at which to display that image based off the image returned
     * from the URL of the SinglePointServer.
     *
     * @param symbolID - the 20-30 digit symbolID of a single point MilStd2525
     * symbol.
     * @return A pixel coordinate of the format "anchorX,anchorY,SymbolBoundsX,
     * SymbolBoundsY,SymbolBoundsWidth,SymbolBoundsHeight,IconWidth,IconHeight".
     * Anchor, represents the center point of the core symbol within the image.
     * The image should be centered on this point.
     * Symbol bounds represents the bounding rectangle of the core symbol within
     * the image.
     * IconWidth/Height represents the height and width of the image in its
     * entirety.
     * Returns an empty string if an error occurs.
     * @deprecated
     */
    public static String getSinglePointInfo(String symbolID)
    {
        String info = "";
        Point2D anchor = new Point2D.Double();
        Rectangle2D symbolBounds = new Rectangle2D.Double();
        return info;
    }
        
    /**
     * Returns true if we recommend clipping a particular symbol.
     * Would return false for and Ambush but would return true for a Line of 
     * Contact due to the decoration on the line.
     * @param symbolID
     * @return 
     */
    public static String ShouldClipMultipointSymbol(String symbolID)
    {
        if(MultiPointHandler.ShouldClipSymbol(symbolID))
            return "true";
        else
            return "false";
    }
    
     /**
     * Given a symbol code meant for a single point symbol, returns the
     * symbol as a byte array.
     *
     * @param symbolID - the 20-30 digit symbolID of a single point MilStd2525
     * symbol.
     * @return byte array.
      * @deprecated
     */
    public static byte[] getSinglePointByteArray(String symbolID)
    {
        //return sps.getSinglePointByteArray(symbolID);
    	return null;
    }
}
