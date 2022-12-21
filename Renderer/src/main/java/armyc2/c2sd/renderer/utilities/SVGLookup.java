package armyc2.c2sd.renderer.utilities;

import android.content.Context;
import android.graphics.RectF;

import org.w3c.dom.Node;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import armyc2.c2sd.singlepointrenderer.R;

public class SVGLookup {
    private static SVGLookup _instance = null;
    private static Boolean _initCalled = false;
    // private static SymbolTableThingy
    //private static Map<String, String> _SVGLookupD = null;
    private static Map<String, SVGLInfo> _SVGLookupD = null;
    private String TAG = "SVGLookup";


    /*
     * Holds SymbolDefs for all symbols. (basicSymbolID, Description, MinPoint, MaxPoints, etc...) Call
     * getInstance().
     *
     */
    private SVGLookup() {
        // init(null);
        // _initCalled=true;
    }

    public static synchronized SVGLookup getInstance() {
        if (_instance == null) {
            _instance = new SVGLookup();
        }

        return _instance;
    }

    public final synchronized void init(Context context)
    {
        if (_initCalled == false) {
            _SVGLookupD = new HashMap<>();
            String[] temp = null;
            String id = null;
            RectF bbox = null;
            String svg = null;
            String delimiter = "~";

        try
        {
            InputStream is = context.getResources().openRawResource(R.raw.svgd);

            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            String line = br.readLine();

            while (line != null)
            {
                //parse first line
                temp = line.split(delimiter);
                id = temp[0];
                bbox = new RectF(Float.parseFloat(temp[1]),Float.parseFloat(temp[2]),Float.parseFloat(temp[3]),Float.parseFloat(temp[4]));

                //read 2nd line to get SVG
                svg = br.readLine();

                _SVGLookupD.put(id, new SVGLInfo(id, bbox, svg));

                //read next line for next loop
                line = br.readLine();
            }

        }
        catch(IOException ioe)
        {
            System.out.println(ioe.getMessage());
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
        }
    }

    /*public final synchronized void init(Context context) {

        if (_initCalled == false) {
            _SVGLookupD = new HashMap<>();

            try {

                InputStream is = context.getResources().openRawResource(R.raw.svgd);


                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document doc = db.parse(is);

                //NodeList nl = doc.getElementsByTagName("g");
                NodeList nl = doc.getElementsByTagName("XML");
                nl = nl.item(0).getChildNodes();
                //NodeList nl = doc.getChildNodes();
                int nodeCount = nl.getLength();
                for(int i = 0; i < nodeCount; i++)
                {
                    if(nl.item(i) instanceof  Element){
                        Element e = (Element) nl.item(i);
                        String id = e.getAttribute("id");
                        //String svg = e.getNodeValue();
                        String svg = nodeToString(e);

                        System.out.println("id : " + id);
                        System.out.println("SVG : " + svg);

                        if(id != null && svg != null)
                            _SVGLookupD.put(id, svg);
                    }
                }

                System.out.println("SVG Map Count: " + String.valueOf(_SVGLookupD.size()));

                is.close();
            } catch (IOException | ParserConfigurationException | SAXException e) {
                Log.e("SVGLookup", "Could not load", e);
            }
            _initCalled = true;//
        }//*/
    }

    public SVGLInfo getSVGLInfo(String id)
    {
        if(_SVGLookupD.containsKey(id))
            return _SVGLookupD.get(id);
        else
            return null;
    }

    /**
     *
     * @param symbolID
     * @return
     * @deprecated doesn't seem to be needed as the SVG frame has frame and fill info.
     */
    public static String getFillID(String symbolID)
    {
        return "";
    }

    public static String getFrameID(String symbolID)
    {
        //SIDC positions 3_456_7
        String frameID = symbolID.charAt(2) + "_" + symbolID.substring(3, 6) + "_" + symbolID.charAt(6);
        return frameID;
    }

    public static String getMainIconID(String symbolID)
    {
        //SIDC positions 5-6 + 11-16
        String mainIconID = symbolID.substring(4, 6) + symbolID.substring(10, 16);
        return mainIconID;
    }

    public static String getMod1ID(String symbolID)
    {
        //SIDC positions 5-6 + 17-18 + "1"
        String mod1ID = symbolID.substring(4, 6) + symbolID.substring(16, 18) + "1";
        return mod1ID;
    }

    public static String getMod2ID(String symbolID)
    {
        //SIDC positions 5-6 + 19-20 + "2"
        String mod2ID = symbolID.substring(4, 6) + symbolID.substring(18, 20) + "2";
        return mod2ID;
    }

    public static List<String> getAllKeys()
    {
        List<String> kl = new ArrayList();
        Set<String> keys = _SVGLookupD.keySet();
        for(String key: keys) {
            //System.out.println(key);
            kl.add(key);
        }
        return kl;
    }


    /**
     *
     * @param node
     * https://stackoverflow.com/questions/4412848/xml-node-to-string-in-java
     */
    private static String nodeToString(Node node)
    {
        StringWriter sw = new StringWriter();
        try
        {
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            t.setOutputProperty(OutputKeys.INDENT, "yes");
            t.transform(new DOMSource(node), new StreamResult(sw));
        }
        catch(TransformerException te)
        {
            System.out.println(te.getMessage());
        }
        return sw.toString();
    }
}


