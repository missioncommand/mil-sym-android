package sec.web.render;
// This import is if we need to call a javascript function
// It requires that you import the plugins.jar from the jdk folder into the project libraries
//import netscape.javascript.JSObject;

import android.content.Context;
import android.graphics.BitmapShader;
import android.graphics.Shader;
import android.util.SparseArray;
import armyc2.c2sd.renderer.MilStdIconRenderer;
import armyc2.c2sd.renderer.PatternFillRenderer;
import armyc2.c2sd.renderer.utilities.ErrorLogger;
import armyc2.c2sd.renderer.utilities.MilStdAttributes;
import armyc2.c2sd.renderer.utilities.MilStdSymbol;
import armyc2.c2sd.renderer.utilities.ModifiersTG;
import armyc2.c2sd.renderer.utilities.RendererSettings;
import armyc2.c2sd.renderer.utilities.ShapeInfo;
import armyc2.c2sd.renderer.utilities.SymbolUtilities;
import armyc2.c2sd.renderer.utilities.Color;
import armyc2.c2sd.graphics2d.*;
import sec.web.render.utilities.JavaRendererUtilities;
import java.util.logging.Level;
import sec.geo.kml.KmlOptions;
import sec.web.json.utilities.JSONArray;
import sec.web.json.utilities.JSONException;
import sec.web.json.utilities.JSONObject;
import sec.geo.utilities.StringBuilder;
import java.util.ArrayList;

/**
 *
 * @author Administrator
 */
//@SuppressWarnings("unused")
public final class SECWebRenderer /* extends Applet */ {
	private static final long serialVersionUID = -2691218568602318366L;
	
	// constants for the available shape types that can be generated into KML
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
                    MilStdIconRenderer.getInstance().init(context, cacheDir);
	            //use SECWebRenderer.setLoggingLevel()
	            
	            //sets default value for single point symbology to have an outline.
	            //outline color will be automatically determined based on line color
	            //unless a color value is manually set.
	            
	            //Set Renderer Settings/////////////////////////////////////////////
	            RendererSettings.getInstance().setSinglePointSymbolOutlineWidth(1);
	            RendererSettings.getInstance().setTextRenderMethod(RendererSettings.RenderMethod_NATIVE);
	            RendererSettings.getInstance().setTextBackgroundMethod(
	                            RendererSettings.TextBackgroundMethod_OUTLINE_QUICK);
	            RendererSettings.getInstance().setTextOutlineWidth(2);
	            RendererSettings.getInstance().setLabelForegroundColor(Color.BLACK.toARGB());
	            RendererSettings.getInstance().setLabelBackgroundColor(new Color(255, 255, 255, 200).toARGB());
	            RendererSettings.getInstance().setModifierFont("arial", Font.PLAIN, 12);
	            ErrorLogger.setLevel(Level.FINE);
	            _initSuccess = true;
        	}
            
        }
        catch(Exception exc)
        {
            ErrorLogger.LogException("SECWebRenderer", "init", exc, Level.WARNING);
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
     * Use like SECWebRenderer.setLoggingLevel(Level.INFO);
     * or
     * Use like SECWebRenderer.setLoggingLevel(800);
     * @param level java.util.logging.level
     */
    public static void setLoggingLevel(Level level)
    {
        try
        {
            ErrorLogger.setLevel(level,true);
            ErrorLogger.LogMessage("SECWebRenderer", "setLoggingLevel(Level)", 
                    "Logging level set to: " + ErrorLogger.getLevel().getName(), 
                    Level.CONFIG);
        }
        catch(Exception exc)
        {
            ErrorLogger.LogException("SECWebRenderer", "setLoggingLevel(Level)", exc, Level.INFO);
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
     * Use like SECWebRenderer.setLoggingLevel(Level.INFO);
     * or
     * Use like SECWebRenderer.setLoggingLevel(800);
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
            
            ErrorLogger.LogMessage("SECWebRenderer", "setLoggingLevel(int)", 
                    "Logging level set to: " + ErrorLogger.getLevel().getName(), 
                    Level.CONFIG);
        }
        catch(Exception exc)
        {
            ErrorLogger.LogException("SECWebRenderer", "setLoggingLevel(int)", exc, Level.INFO);
        }
    }
    
    /**
     * Let's user choose between 2525Bch2 and 2525C.
     * Ideally, set only once at startup.
     * 2525Bch2 = 0, 2525C = 1.
     * @param symStd 
     */
    public static void setDefaultSymbologyStandard(int symStd)
    {
        RendererSettings.getInstance().setSymbologyStandard(symStd);
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
    public static void setModifierTextColor(String hexColor)
    {
        Color textColor = SymbolUtilities.getColorFromHexString(hexColor);
        if(textColor==null)
        {
            textColor = Color.black;
        }
        RendererSettings.getInstance().setLabelForegroundColor(textColor.toARGB());
    }


    


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
     * @param symbolCode A 15 character symbolID corresponding to one of the
     * graphics in the MIL-STD-2525C
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
     * @param modifiers SparseArray<String>, keyed using constants from ModifiersTG.  
     * Pass in comma delimited String for modifiers with multiple values like AM, AN & X
     * @param attributes SparseArray<String>, keyed using constants from MilStdAttributes.  
     * @param format An enumeration: 0 for KML, 1 for JSON.
     * @param symStd An enumeration: 0 for 2525Bch2, 1 for 2525C.
     * @return A JSON string representation of the graphic.
     */
    public static String RenderSymbol(String id, String name, String description, 
            String symbolCode, String controlPoints, String altitudeMode,
            double scale, String bbox, SparseArray<String> modifiers, SparseArray<String> attributes,  int format, int symStd) {
        
        String output = "";
        try {         
        	
        	JavaRendererUtilities.addAltModeToModifiersString(attributes,altitudeMode);
        
            //if (JavaRendererUtilities.is3dSymbol(symbolCode, modifiers))
            if (altitudeMode.equals("clampToGround") == false && format == 0 && JavaRendererUtilities.is3dSymbol(symbolCode, modifiers))
            {
                if (altitudeMode.isEmpty())
                    altitudeMode = "absolute";
        
                output = RenderMilStd3dSymbol(name, id, symbolCode, description, altitudeMode, controlPoints,
                        modifiers, attributes);
                
                //get modifiers/////////////////////////////////////////////////
                String modifierKML = MultiPointHandler.getModififerKML(id, name, description, symbolCode, controlPoints,
                        scale, bbox, modifiers, attributes, format,symStd);

                modifierKML += "</Folder>";

                output = output.replaceFirst("</Folder>", modifierKML);

                ////////////////////////////////////////////////////////////////
                
                
                
                // Check the output of the 3D Symbol Drawing.  If this returned an error
                // it should either be "" or it should be a JSON string starting with "{".
                // This really is not a good solution, but was up to 13.0.6 and had to make
                // this bug fix in quick turnaround.  More consistent error handling should
                // be done through code.
               
                if (output.equals("") || output.startsWith("{")) {
                    output = MultiPointHandler.RenderSymbol(id, name, description, symbolCode, controlPoints,
                        scale, bbox, modifiers, attributes, format,symStd);
                }
            }
            else
            {            
                output = MultiPointHandler.RenderSymbol(id, name, description, symbolCode, controlPoints,
                        scale, bbox, modifiers, attributes, format,symStd);
                
                //DEBUGGING
                if(ErrorLogger.getLevel().intValue() <= Level.FINER.intValue())
                {
                    System.out.println("");
                    StringBuilder sb = new StringBuilder();
                    sb.append("\nID: " + id + "\n");
                    sb.append("Name: " + name + "\n");
                    sb.append("Description: " + description + "\n");
                    sb.append("SymbolID: " + symbolCode + "\n");
                    sb.append("SymStd: " + String.valueOf(symStd) + "\n");
                    sb.append("Scale: " + String.valueOf(scale) + "\n");
                    sb.append("BBox: " + bbox + "\n");
                    sb.append("Coords: " + controlPoints + "\n");
                    sb.append("Modifiers: " + modifiers + "\n");
                    ErrorLogger.LogMessage("SECWebRenderer", "RenderSymbol", sb.toString(),Level.FINER);
                }
                if(ErrorLogger.getLevel().intValue() <= Level.FINEST.intValue())
                {
                    String briefOutput = output.replaceAll("</Placemark>", "</Placemark>\n");
                    briefOutput = output.replaceAll("(?s)<description[^>]*>.*?</description>", "<description></description>");
                    ErrorLogger.LogMessage("SECWebRenderer", "RenderSymbol", "Output:\n" + briefOutput,Level.FINEST);
                }
            }
            
            
        } catch (Exception ea) {
            
            output = "{\"type\":'error',error:'There was an error creating the MilStdSymbol - " + ea.toString() + "'}";
            ErrorLogger.LogException("SECWebRenderer", "RenderSymbol", ea, Level.WARNING);
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
     * @param symbolCode A 15 character symbolID corresponding to one of the
     * graphics in the MIL-STD-2525C
     * @param controlPoints The vertices of the graphics that make up the
     * graphic.  Passed in the format of a string, using decimal degrees
     * separating lat and lon by a comma, separating coordinates by a space.
     * The following format shall be used "x1,y1 [xn,yn]..."
     * @param pixelWidth pixel dimensions of the viewable map area
     * @param pixelHeight pixel dimensions of the viewable map area
     * @param bbox The viewable area of the map.  Passed in the format of a
     * string "lowerLeftX,lowerLeftY,upperRightX,upperRightY."
     * example: "-50.4,23.6,-42.2,24.2"
     * @param modifiers SparseArray<String>, keyed using constants from ModifiersTG.  
     * Pass in comma delimited String for modifiers with multiple values like AM, AN & X
     * @param attributes SparseArray<String>, keyed using constants from MilStdAttributes.
     * @param format An enumeration: 0 for KML, 1 for JSON.
     * @param symStd An enumeration: 0 for 2525Bch2, 1 for 2525C.
     * @return A JSON (1) or KML (0) string representation of the graphic.
     */
    public static String RenderSymbol2D(String id, String name, String description, String symbolCode, String controlPoints,
            int pixelWidth, int pixelHeight, String bbox, SparseArray<String> modifiers, 
            SparseArray<String> attributes, int format, int symStd)
    {
        String output = "";
        try
        {
            output = MultiPointHandler.RenderSymbol2D(id, name, description, 
                    symbolCode, controlPoints, pixelWidth, pixelHeight, bbox, 
                    modifiers, attributes, format, symStd);
        }
        catch(Exception exc)
        {
            output = "{\"type\":'error',error:'There was an error creating the MilStdSymbol: " + symbolCode + " - " + exc.toString() + "'}";
        }
        return output;
    }

    /**
     * Creates a 3D symbol to be displayed on some 3D globe surface.  Generates 
     * Keyhole Markup Language (KML) to return that specifies the points and format of 
     * the rendering.
     * <br/>
     * Control points should be of the format of:
     * <tr><code>"x,y,z [x,y,z]..."</code></tr>
     * Attributes should be passed in as a JSON array.  If more than one set of 
     * parameters are passed in as an array or more than one item, they will map 
     * to the vertex specified in the control points.  The attributes are
     * of the format:
     * <tr><code>{"attributes":[{"<i>attribute1</i>":<i>value</i>,...},{<i>[optional]</i>]}</code></tr>
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
     * @param attributes A JSON  array holding the parameters for the 
     * shape.  Attributes should be of the following format: <br/><br/>
     * <tr><code>{"attributes":[{"<i>attribute1</i>":<i>value</i>,...},{<i>[optional]</i>]}</code></tr>
     * @return A KML string that represents a placemark for the 3D shape
     */
    public static String Render3dSymbol(String name, String id, String shapeType, 
            String description, String lineColor, String fillColor, String altitudeMode, 
            String controlPoints,
            String attributes) {
        
        String returnValue = "";
        
        try {
            
            StringBuilder output = new StringBuilder();
            SymbolModifiers modifiers = new SymbolModifiers();
            JSONObject attributesJSON;
                        
            // Retrieve the attributes from the attributes object
            // Attributes should be a JSON array string of this format 
            // { "attributes":[{"radius1":50, "minalt":0, "maxalt:100"}]}
            // There should only be one item in the JSON array, but if 
            // there is more items this will ignore them.            
            //attributes="{attributes:[{radius1:5000, minalt:0, maxalt:100}]}";
            attributesJSON = new JSONObject(attributes);

            // If no attributes passed in or attributes set to null
            // default to an empty string.
            if (attributesJSON == null || attributes.equals("")) {
                attributesJSON = new JSONObject(DEFAULT_ATTRIBUTES);
            }

            JSONArray attributesArray = attributesJSON.getJSONArray(ATTRIBUTES);
            int attributesArrayLength = attributesArray.length();
            if (attributesArrayLength> 0) {
                
                for(int i = 0; i < attributesArrayLength; i++) {
                
                    // get the first item in the array and use those parameters.
                    // if any of the parameters don't exist, it will use defaults.
                    // Defaults are arbitrary, no reason not to change them.
                    JSONObject currentAttributeSet = attributesArray.getJSONObject(i);

                    if (currentAttributeSet.has(RADIUS1)) {
                        modifiers.AM_DISTANCE.add(currentAttributeSet.getDouble(RADIUS1));
                    }                                    

                    if (currentAttributeSet.has(RADIUS2)) {
                        modifiers.AM_DISTANCE.add(currentAttributeSet.getDouble(RADIUS2));
                    }

                    if (currentAttributeSet.has(MIN_ALT)) {
                        modifiers.X_ALTITUDE_DEPTH.add(currentAttributeSet.getDouble(MIN_ALT));
                    }

                    if (currentAttributeSet.has(MAX_ALT)) {
                        modifiers.X_ALTITUDE_DEPTH.add(currentAttributeSet.getDouble(MAX_ALT));
                    }

                    if (currentAttributeSet.has(LEFT_AZIMUTH)) {
                        modifiers.AN_AZIMUTH.add(currentAttributeSet.getDouble(LEFT_AZIMUTH));
                    }

                    if (currentAttributeSet.has(RIGHT_AZIMUTH)) {
                        modifiers.AN_AZIMUTH.add(currentAttributeSet.getDouble(RIGHT_AZIMUTH));
                    }
                }
            }
            
            // Send to the 3D renderer for generating the 3D point and creating
            // the KML to return.            
            returnValue = Shape3DHandler.render3dSymbol(name, id, shapeType, 
                description, lineColor, fillColor, altitudeMode, controlPoints, modifiers);            
        } 
        catch (JSONException je) {

            return ERR_ATTRIBUTES_NOT_FORMATTED;
        }            
        catch (Exception ea) {
            ErrorLogger.LogException("SECWebRenderer", "Render3dSymbol()", ea);
            return "";
        }            
        
        return returnValue;
    }
    
     /**
     * Creates a 3D symbol from the MilStd2525B USAS or MIL-STD-2525C to be 
     * displayed on a 3D globe surface.  Only certain symbols from the MIL-STD
     * can be displayed in 3D.   Most of these are graphics that fall under Fire
     * Support.  Any graphic that has an X modifier (altitude/depth) should
     * have a 3D representation.  Generates 
     * Keyhole Markup Language (KML) to return that 
     * specifies the points and format of 
     * the rendering.
     * <br/>
     * Control points should be of the format of:
     * <tr><code>"x,y,z [x,y,z]..."</code></tr>
     * 
     * 
     * @param name The user displayed name for the symbol.  Users will use this 
     * to identify with the symbol.
     * @param id An internally used unique id that developers can use to 
     * uniquely distinguish this symbol from others.
     * @param symbolCode A 15 character ID of the type of symbol to draw.  Only
     * symbols with an X modifier from the standard will draw.
     * @param description A brief description of what the symbol represents.  
     * Generic text that does not require any format.  
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
     * @saModifiers SparseArray<String> keyed on ModifiersTG. Only using values AM, AN and X.
     * Strings should be comma delimited for multiple values.
     * @saAttributes SparseArray<String> keyed on MilStdAttributes. Only using value FillColor.
     * @return A KML string that represents a placemark for the 3D shape
     */
    private static String RenderMilStd3dSymbol(String name, String id, String symbolCode, 
            String description, 
            String altitudeMode,
            String controlPoints,
            SparseArray<String> saModifiers,
            SparseArray<String> saAttributes) {
               
        String symbolId = symbolCode.substring(4,10);
        SymbolModifiers attributes = new SymbolModifiers();
        String output = "";
        
        KmlOptions.AltitudeMode convertedAltitudeMode = KmlOptions.AltitudeMode.RELATIVE_TO_GROUND;

        // Convert altitude mode to an enum that we understand.  If it does not
        // understand or is "", then convert to ALTITUDE_RELATIVE_TO_GROUND.
        if (!altitudeMode.equals(""))
        {
            convertedAltitudeMode = KmlOptions.AltitudeMode.fromString(altitudeMode);
        }
        
        try
        {
        
            
            String[] altitudeDepth = null;
            String[] distance = null;
            String[] azimuth = null;
            int altitudeDepthLength = 0;
            int distanceLength = 0;
            int azimuthLength = 0;
            String lineColor = "";
            String fillColor = "";
            
            if (saModifiers.indexOfKey(ModifiersTG.X_ALTITUDE_DEPTH)>=0)
            {
                altitudeDepth = saModifiers.get(ModifiersTG.X_ALTITUDE_DEPTH).split(",");
                altitudeDepthLength = altitudeDepth.length;
            }
            
            if (saModifiers.indexOfKey(ModifiersTG.AN_AZIMUTH)>=0)
            {
            	azimuth = saModifiers.get(ModifiersTG.AN_AZIMUTH).split(",");
            	azimuthLength = azimuth.length;
            }
            
            if (saModifiers.indexOfKey(ModifiersTG.AM_DISTANCE)>=0)
            {
            	distance = saModifiers.get(ModifiersTG.AM_DISTANCE).split(",");
            	distanceLength = distance.length;
            }
            
            if (saAttributes.indexOfKey(MilStdAttributes.LineColor)>=0)
            {
            	lineColor = saAttributes.get(MilStdAttributes.LineColor);
            }
            else
            {   
                Color c = SymbolUtilities.getFillColorOfAffiliation(symbolCode);
                lineColor = c.toHexString();
                // ensure that some color is selected.  If no color can be
                // found, use black.
                if (lineColor == null)
                {
                	lineColor = "FF000000";
                }
            }
            
            if (saAttributes.indexOfKey(MilStdAttributes.FillColor)>=0)
            {
            	fillColor = saAttributes.get(MilStdAttributes.FillColor);
            }
            else
            {   
                Color c = SymbolUtilities.getFillColorOfAffiliation(symbolCode);
                fillColor = c.toHexString();
                // ensure that some color is selected.  If no color can be
                // found, use black.
                if (fillColor == null)
                {
                	fillColor = "AA000000";
                }
            }
            
            lineColor = JavaRendererUtilities.ARGBtoABGR(lineColor);
            fillColor = JavaRendererUtilities.ARGBtoABGR(fillColor);
                            
            for (int i=0; i < altitudeDepthLength; i++)
            {
                // if it's a killbox, need to set minimum alt to 0.
                if (symbolId.startsWith("AJP"))
                {
                    attributes.X_ALTITUDE_DEPTH.add(0D);
                    i++;
                }                                        
                attributes.X_ALTITUDE_DEPTH.add(Double.parseDouble(altitudeDepth[i]));
            }
            for (int i=0; i < distanceLength; i++)
            {
                // If this is a 'track' type graphic, then we need to take the distance
                // and divide it by half, than add it twice.  This is due 
                // to the TAIS requirement that Tracks must have a left width 
                // and a right width. 
                if (symbolId.equals("ACAR--") || // ACA - rectangular
                    symbolId.equals("AKPR--") || // Killbox - rectangular
                    symbolId.equals("ALC---") || // air corricor
                    symbolId.equals("ALM---") || // MRR
                    symbolId.equals("ALS---") || // SAAFR
                    symbolId.equals("ALU---") || // unmanned aircraft
                    symbolId.equals("ALL---")) {  // LLTR) {
                    double width = Double.parseDouble(distance[i]) / 2;
                    attributes.AM_DISTANCE.add(width);
                    attributes.AM_DISTANCE.add(width);
                } else {
                    attributes.AM_DISTANCE.add(Double.parseDouble(distance[i]));
                }
            }  

            if (symbolId.equals("ACAI--") || // ACA - irregular
                    symbolId.equals("AKPI--") || // Killbox - irregular
                    symbolId.equals("AAR---") || // ROZ
                    symbolId.equals("AAF---") || // SHORADEZ
                    symbolId.equals("AAH---") || // HIDACZ
                    symbolId.equals("AAM---") || // MEZ
                    symbolId.equals("AAML--") || // LOMEZ
                    symbolId.equals("AAMH--")) // HIMEZ
            {
                output = Shape3DHandler.buildPolygon(controlPoints, id, name, 
                    description, lineColor, fillColor, convertedAltitudeMode, attributes);
            }
            else if (symbolId.equals("ACAR--") || // ACA - rectangular
                    symbolId.equals("AKPR--") || // Killbox - rectangular
                    symbolId.equals("ALC---") || // air corricor
                    symbolId.equals("ALM---") || // MRR
                    symbolId.equals("ALS---") || // SAAFR
                    symbolId.equals("ALU---") || // unmanned aircraft
                    symbolId.equals("ALL---"))   // LLTR
            {
                output = Shape3DHandler.buildTrack(controlPoints, id, name, 
                    description, lineColor, fillColor, convertedAltitudeMode, attributes);
            }
            else if (symbolId.equals("ACAC--") || // ACA - circular
                    symbolId.equals("AKPC--"))    // Killbox - circular
            {
                output = Shape3DHandler.buildCylinder(controlPoints, id, name, 
                    description, lineColor, fillColor, convertedAltitudeMode, attributes);

            }   

            
        }
        catch (Exception exc)
        {
            output = "";
        } 
        return output;
    }

    /**
	 * Renders all MilStd 2525 multi-point symbols, creating MilStdSymbol that contains the
	 * information needed to draw the symbol on the map.
     * DOES NOT support RADARC, CAKE, TRACK etc...
	 * ArrayList<Point2D> milStdSymbol.getSymbolShapes.get(index).getPolylines()
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
	 *            A 15 character symbolID corresponding to one of the graphics
	 *            in the MIL-STD-2525C
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
	 *            modifiers.put(ModifiersTG.T_UNIQUE_DESIGNATION_1, "T");
	 *            Or
	 *            modifiers.put(ModifiersTG.AM_DISTANCE, "1000,2000,3000");
	 * @param attributes
	 * 			  Used like:
	 *            attributes.put(MilStdAttributes.LineWidth, "3");
	 *            Or
	 *            attributes.put(MilStdAttributes.LineColor, "#00FF00");
	 * @param symStd
	 *            An enumeration: 0 for 2525Bch2, 1 for 2525C.
     * @return MilStdSymbol
     */
    public static MilStdSymbol RenderMultiPointAsMilStdSymbol(String id, String name, String description, String symbolCode,
			String controlPoints, String altitudeMode, double scale, String bbox, SparseArray<String> modifiers, SparseArray<String> attributes, int symStd)
    {
		MilStdSymbol mSymbol = null;
		try 
		{
			mSymbol = MultiPointHandler.RenderSymbolAsMilStdSymbol(id, name, description, symbolCode,
                    controlPoints, scale, bbox, modifiers, attributes, symStd);

            String basicID = SymbolUtilities.getBasicSymbolID(symbolCode);
            if(basicID.charAt(0)=='G' && ((basicID.charAt(2)=='G' &&  basicID.substring(4,7).equals("PC-")) ||  (basicID.charAt(2)=='M' &&  basicID.substring(4,7).equals("OFD"))))
            {
                String A = "G*MPOMU---****X";//unspecified mine, default if not specified
                if(modifiers.indexOfKey(ModifiersTG.A_SYMBOL_ICON) >= 0)
                    A = modifiers.get(ModifiersTG.A_SYMBOL_ICON);

                //test
                //A = "G*MPOMW---****X,G*MPOMD---****X,G*MPOME---****X";
                //A = "G*MPOMEXXX****X";
                int size = RendererSettings.getInstance().getDefaultPixelSize();

                ArrayList<ShapeInfo> shapes = mSymbol.getSymbolShapes();
                if(shapes.size() > 0){
                    ShapeInfo shape = shapes.get(0);
                    shape.setPatternFillImage(PatternFillRenderer.MakeSymbolPatternFill(A,size));
                    if(shape.getPatternFillImage() != null)
                        shape.setShader(new BitmapShader(shape.getPatternFillImage(), Shader.TileMode.REPEAT, Shader.TileMode.REPEAT));
                }
            }
            else if(basicID.charAt(0) == 'W')
            {
                ArrayList<ShapeInfo> shapes = mSymbol.getSymbolShapes();
                if(shapes.size() > 0)
                {
                    ShapeInfo shape = shapes.get(0);
                    shape.setPatternFillImage(PatternFillRenderer.MakeMetocPatternFill(symbolCode));
                    if(shape.getPatternFillImage() != null)
                        shape.setShader(new BitmapShader(shape.getPatternFillImage(), Shader.TileMode.REPEAT, Shader.TileMode.REPEAT));
                }
            }

		}
		catch (Exception ea) 
		{
			mSymbol=null;
			ErrorLogger.LogException("SECRenderer", "RenderMultiPointAsMilStdSymbol" + " - " + symbolCode, ea, Level.WARNING);
		}
		
		//System.out.println("RenderMultiPointAsMilStdSymbol exit");
		return mSymbol;
    }


    
    /**
     * Given a symbol code meant for a single point symbol, returns the
     * anchor point at which to display that image based off the image returned
     * from the URL of the SinglePointServer.
     * 
     * @param symbolID - the 15 character symbolID of a single point MilStd2525
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
     * @param symbolID - the 15 character symbolID of a single point MilStd2525
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
     * @param symbolID - the 15 character symbolID of a single point MilStd2525
     * symbol.
     * @return byte array.
     */
    public static byte[] getSinglePointByteArray(String symbolID)
    {
        //return sps.getSinglePointByteArray(symbolID);
    	return null;
    }    
     /**
     * Put this here rather than in multipointhandler so that I could get the
     * port info from the single point server.
     * @param modifiers
     * @param clip
     * @return 
     */
    public static String GenerateSymbolLineFillUrl(SparseArray<String> modifiers, ArrayList<Point2D> pixels, Rectangle clip)
    {
        int shapeType = 0;
        String url = "";
        String symbolFillIDs=null;
        String symbolLineIDs=null;
        String strClip=null;
        //int symbolSize = AreaSymbolFill.DEFAULT_SYMBOL_SIZE;
        int symbolSize = 25;
        int imageoffset = 0;
        ArrayList<ArrayList<Point2D>> lines = null;
        ArrayList<Point2D> points = null;
        Point2D point = null;
        
        Shape shape = null;
        //PathIterator itr = null;
        double height = 0;
        double width = 0;
        int offsetX = 0;
        int offsetY = 0;
        int x = 0;
        int y = 0;
        //Rectangle2D bounds = null;
        Rectangle bounds = null;
        try
        {
            //Path2D path = new GeneralPath();
            GeneralPath path = new GeneralPath();
            Point2D temp = null;
            //Get bounds of the polygon/polyline path
            for(int i=0; i<pixels.size();i++)
            {
                temp = pixels.get(i);
                if(i>0)
                {
                    path.lineTo(temp.getX(), temp.getY());
                }
                else if(i==0)
                {
                    path.moveTo(temp.getX(), temp.getY());
                }
            }
            
            bounds = path.getBounds();
            height = bounds.getHeight();
            width = bounds.getWidth();

//            System.out.println("bounds: "+ bounds.toString());
//                    System.out.println("height: "+ String.valueOf(height));
//            System.out.println("width: "+ String.valueOf(width));
            
            //pixels may be in negative space so get offsets to put everything
            //in the positive
            if(bounds.getX()<0)
            {
                offsetX = (int)(bounds.getX()*-1);
            }
            else if((bounds.getX()+bounds.getWidth()) > width)
            {
                offsetX = (int)((bounds.getX()+bounds.getWidth())-width)*-1;
            }
            
            if(bounds.getY()<0)
            {
                offsetY = (int)(bounds.getY()*-1);
            }
            else if((bounds.getY()+bounds.getHeight()) > height)
            {
                offsetY = (int)((bounds.getY()+bounds.getHeight())-height)*-1;
            }

            //build clip string
            if(clip!=null)
            {
                StringBuilder sbClip = new StringBuilder();
                sbClip.append("&clip=");
                sbClip.append(Integer.toString((int)clip.getX()));
                sbClip.append(",");
                sbClip.append(Integer.toString((int)clip.getY()));
                sbClip.append(",");
                sbClip.append(Integer.toString((int)clip.getWidth()));
                sbClip.append(",");
                sbClip.append(Integer.toString((int)clip.getHeight()));
                strClip=sbClip.toString();
            }

                    
            StringBuilder sbCoords = new StringBuilder();
            StringBuilder sbUrl = new StringBuilder();
            sbCoords.append("coords=");

            if (modifiers.indexOfKey(ModifiersTG.SYMBOL_FILL_IDS) >= 0) {
                symbolFillIDs = (String) modifiers.get(ModifiersTG.SYMBOL_FILL_IDS);
            }
                        
            //build coordinate string
            for(int i = 0; i< pixels.size(); i++)
            {
                if(i>0)
                {
                    sbCoords.append(",");
                }
                point = pixels.get(i);
                x = (int)(point.getX() + offsetX);
                y = (int)(point.getY() + offsetY);
                sbCoords.append(Integer.toString(x));
                sbCoords.append(",");
                sbCoords.append(Integer.toString(y));
            }
            
            //build image url
            sbUrl.append("http://127.0.0.1:");
            //sbUrl.append(String.valueOf(spsPortNumber));
            sbUrl.append("6789");
            sbUrl.append("/AREASYMBOLFILL?");
            sbUrl.append("renderer=AreaSymbolFillRenderer&");
            sbUrl.append(sbCoords.toString());
            if(symbolFillIDs != null)
            {
                sbUrl.append("&symbolFillIds=");
                sbUrl.append(symbolFillIDs);
            }
            if(symbolLineIDs != null)
            {
                sbUrl.append("&symbolLineIds=");
                sbUrl.append(symbolLineIDs);
            }
            if(symbolSize>0)
            {
                sbUrl.append("&symbolFillIconSize=");                sbUrl.append(Integer.toString(symbolSize));
            }
            if(strClip!=null)
            {
                sbUrl.append(strClip);
            }



            sbUrl.append("&height=");
            //sbUrl.append(Integer.valueOf((int)height));
            sbUrl.append(Integer.toString((int)height));
            sbUrl.append("&width=");
            //sbUrl.append(Integer.valueOf((int)width));
            sbUrl.append(Integer.toString((int)width));

            url = sbUrl.toString();

        }
        catch(Exception exc)
        {
            System.out.println(exc.getMessage());
            exc.printStackTrace();
        }
        return url;
    }

}
