package armyc2.c5isr.renderer.utilities;

import android.content.Context;
import android.graphics.RectF;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


import armyc2.c5isr.renderer.R;

public class SVGLookup {
    private static SVGLookup _instance = null;
    private static Boolean _initCalled = false;
    private static Map<String, SVGInfo> _SVGLookupD = null;
    private static Map<String, SVGInfo> _SVGLookupE = null;
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
        if (_initCalled == false)
        {
            _SVGLookupD = new HashMap<>();
            _SVGLookupE = new HashMap<>();

            try
            {
                InputStream isD = context.getResources().openRawResource(R.raw.svgd);
                loadData(isD, SymbolID.Version_2525Dch1);
                isD.close();

                InputStream isE = context.getResources().openRawResource(R.raw.svge);
                loadData(isE, SymbolID.Version_2525E);
                isE.close();

                _initCalled = true;
            }
            catch(Exception e)
            {
                System.out.println(e.getMessage());
            }
        }

    }

    private void loadData(InputStream is, int version)
    {
        Map<String, SVGInfo> lookup;
        if(version == SymbolID.Version_2525E)
            lookup = _SVGLookupE;
        else
            lookup = _SVGLookupD;

        String[] temp = null;
        String id = null;
        RectF bbox = null;
        String svg = null;
        String delimiter = "~";

        try
        {
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            String line = br.readLine();

            while (line != null)
            {
                //parse first line
                temp = line.split(delimiter);
                id = temp[0];
                float left, top, width, height;
                left = Float.parseFloat(temp[1]);
                top = Float.parseFloat(temp[2]);
                width = Float.parseFloat(temp[3]);
                height = Float.parseFloat(temp[4]);
                bbox = RectUtilities.makeRectF(left, top, width, height);

                    /*if(id.equals("25130100"))
                        Log.e("action point",id);//*/

                //read 2nd line to get SVG
                svg = br.readLine();

                lookup.put(id, new SVGInfo(id, bbox, svg));

                //read next line for next loop
                line = br.readLine();
            }
            _initCalled = true;
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

    /**
     *
     * @param id
     * @param version
     * @return
     */
    public SVGInfo getSVGLInfo(String id, int version)
    {
        if(version >= SymbolID.Version_2525E)
        {
            if (_SVGLookupE.containsKey(id))
                return _SVGLookupE.get(id);
        }
        else
        {
            if (_SVGLookupD.containsKey(id))
                return _SVGLookupD.get(id);
        }

        return null;
    }

    public SVGInfo getSVGOctagon()
    {
        return _SVGLookupD.getOrDefault("octagon", null);
    }

    public static String getFrameID(String symbolID)
    {
        //SIDC positions 3_456_7
        // String frameID = symbolID.charAt(2) + "_" + symbolID.substring(3, 6) + "_" + symbolID.charAt(6);

        String frameID = null;
        String ss;
        int affiliation = SymbolID.getAffiliation(symbolID);
        int status = SymbolID.getStatus(symbolID);
        //Some affiliations are always dashed and only have one SVG for status with a value of 0
        if(affiliation == SymbolID.StandardIdentity_Affiliation_Pending ||
                affiliation == SymbolID.StandardIdentity_Affiliation_AssumedFriend ||
                affiliation == SymbolID.StandardIdentity_Affiliation_Suspect_Joker)
        {
            // "When the frame is assumed friend, suspect, or pending, the status shall not be displayed."
            status = 0;
        }
        if(status > 1) // Anything above 1 for status means present for the frame
            status = 0;

        int context = SymbolID.getContext(symbolID);
        //they didn't make duplicate frame so I have to change the number for
        //the lookup to work.

        if(SymbolID.getVersion(symbolID) < SymbolID.Version_2525E)//2525Dch1 or less
        {
            switch(SymbolID.getSymbolSet(symbolID))
            {
                case 01: //Air
                case 02: //Air Missile
                case 51: //Air SIGINT
                    ss = "01";
                    break;
                case 05: //Space
                case 06: //Space Missile
                case 50: //Space SIGINT
                    ss = "05";
                    break;
                case 10: //Land Unit
                case 11://Land Civilian Unit/Org
                    ss = "10";
                    break;
                case 15://Land Equipment
                case 52://Land SigInt
                case 53://Sea Surface SIGINT
                    ss = "30";
                    break;
                case 30://Sea Surface
                    ss = "30";
                    if(SymbolID.getEntityCode(symbolID)==150000)
                        return "octagon";//this symbol has no frame and a size of 1L x 1L.
                    break;
                case 20: //Land Installation
                    ss = "20";
                    break;
                case SymbolID.SymbolSet_DismountedIndividuals: //Dismounted Individuals
                    ss = "27";
                    break;
                case 35: //Sea Subsurface
                case 36: //Mine Warfare
                case 54: //Sea Subsurface SigInt
                    ss = "35";
                    break;
                case 40: //Activities/Events
                    ss = "40";
                    break;
                case 60: //Cyberspace
                    ss = "60"; //No cyberspace SVG frame at the moment so setting to activities
                    break;
                default:
                    ss = "00";

                    if(context == SymbolID.StandardIdentity_Context_Exercise && affiliation > SymbolID.StandardIdentity_Affiliation_Unknown)
                    {
                        //really there are no unknown exercise symbols outside of pending and unknown
                        //default to unknown
                        affiliation = SymbolID.StandardIdentity_Affiliation_Unknown;
                    }
            }
            frameID = context + "_" + affiliation + ss + "_" + status;
        }
        else//2525E or above
        {
            char frameShape = SymbolID.getFrameShape(symbolID);
            if(frameShape==SymbolID.FrameShape_Unknown)
            {

                /*if(SymbolID.getSymbolSet(symbolID) != SymbolID.SymbolSet_SignalsIntelligence)
                {//get frame shape associated with symbol set
                    frameShape=SymbolID.getDefaultFrameShape(symbolID);
                }//*/
                frameShape=SymbolID.getDefaultFrameShape(symbolID);
                if(context == SymbolID.StandardIdentity_Context_Exercise &&
                        SymbolID.getSymbolSet(symbolID) == SymbolID.SymbolSet_Unknown &&
                        affiliation > SymbolID.StandardIdentity_Affiliation_Unknown)
                {
                    //really there are no unknown exercise symbols outside pending and unknown affiliations
                    //default to unknown
                    affiliation = SymbolID.StandardIdentity_Affiliation_Unknown;
                }
            }
            if(SymbolID.getSymbolSet(symbolID)==SymbolID.SymbolSet_SeaSurface &&
                    SymbolID.getEntityCode(symbolID)==150000 &&  //Own Ship
                    (frameShape == SymbolID.FrameShape_LandEquipment_SeaSurface || frameShape == SymbolID.FrameShape_Unknown))
            {
                return "octagon";
            }
            frameID = context + "_" + affiliation + frameShape + "_" + status;
        }
        return frameID;
    }

    public static String getMainIconID(String symbolID)
    {
        //SIDC positions 5-6 + 11-16
        String mainIconID = symbolID.substring(4, 6) + symbolID.substring(10, 16);
        int ss = SymbolID.getSymbolSet(symbolID);

        if(ss == SymbolID.SymbolSet_MineWarfare)
        {
            if(RendererSettings.getInstance().getSeaMineRenderMethod() == RendererSettings.SeaMineRenderMethod_ALT ||
                    mainIconID.equals("36110600") || mainIconID.equals("36110700"))
            {
                mainIconID += "_a";
            }
        }
        else if(ss == SymbolID.SymbolSet_LandUnit)
        {
            switch(SymbolID.getEntityCode(symbolID))
            {
                case 111000:
                case 111001:
                case 111002:
                case 111003:
                case 111004:
                case 111005:
                case 120100:
                case 120400:
                case 120401:
                case 120402:
                case 120501:
                case 120502:
                case 120601:
                case 120801:
                case 121100:
                case 121101:
                case 121102:
                case 121103:
                case 121104:
                case 121105:
                case 121106:
                case 121300:
                case 121301:
                case 121302:
                case 121303:
                case 121802:
                case 130100:
                case 130101:
                case 130102:
                case 130103:
                case 130200:
                case 130302:
                case 140102:
                case 140103:
                case 140104:
                case 140105:
                case 140702:
                case 140703:
                case 141702:
                case 150504:
                case 150800:
                case 160200:
                case 161200:
                case 161300:
                case 161400:
                case 161700:
                case 161800:
                case 161900:
                case 162000:
                case 162100:
                case 162200:
                case 163400:
                case 163700:
                case 163800:
                case 163900:
                case 164000:
                case 164100:
                case 164200:
                case 164300:
                case 164400:
                case 164500:
                case 164600:
                case 165000://NATO Only
                    //do thing to append correct number
                    mainIconID += getPostFixForIcon(symbolID);
                    break;
                default:
                    break;
            }

        }
        else if(ss == SymbolID.SymbolSet_LandEquipment)
        {
            switch (SymbolID.getEntityCode(symbolID))
            {
                case 120111:
                    //do thing to append correct number
                    mainIconID += getPostFixForIcon(symbolID);
                    break;
                default:
                    break;
            }
        }
        else if(ss == SymbolID.SymbolSet_LandInstallation)
        {
            switch (SymbolID.getEntityCode(symbolID))
            {
                case 110300:
                case 111200:
                case 120103:
                case 120105:
                case 120106:
                case 120107:
                case 120701:
                case 120702:
                    //do thing to append correct number
                    mainIconID += getPostFixForIcon(symbolID);
                    break;
                default:
                    break;
            }
        }
        else if(ss == SymbolID.SymbolSet_Activities)
        {
            switch (SymbolID.getEntityCode(symbolID))
            {
                case 110303:
                case 130201:
                case 131202:
                case 131208:
                    //do thing to append correct number
                    mainIconID += getPostFixForIcon(symbolID);
                    break;
                default:
                    break;
            }
        }
        else if(ss == SymbolID.SymbolSet_Unknown)
            mainIconID = "00000000";//unknown with question mark
        else if (ss != SymbolID.SymbolSet_Air &&
                ss != SymbolID.SymbolSet_AirMissile &&
                ss != SymbolID.SymbolSet_Space &&
                ss != SymbolID.SymbolSet_SpaceMissile &&
                ss != SymbolID.SymbolSet_LandCivilianUnit_Organization &&
                ss != SymbolID.SymbolSet_DismountedIndividuals &&
                ss != SymbolID.SymbolSet_ControlMeasure &&
                ss != SymbolID.SymbolSet_SeaSurface &&
                ss != SymbolID.SymbolSet_SeaSubsurface &&
                ss != SymbolID.SymbolSet_Atmospheric &&
                ss != SymbolID.SymbolSet_Oceanographic &&
                ss != SymbolID.SymbolSet_MeteorologicalSpace &&
                ss != SymbolID.SymbolSet_SignalsIntelligence_Space &&
                ss != SymbolID.SymbolSet_SignalsIntelligence_Air &&
                ss != SymbolID.SymbolSet_SignalsIntelligence_Land &&
                ss != SymbolID.SymbolSet_SignalsIntelligence_SeaSurface &&
                ss != SymbolID.SymbolSet_SignalsIntelligence_SeaSubsurface &&
                ss != SymbolID.SymbolSet_CyberSpace)
        {
            mainIconID = "98100000";//invalid symbol, inverted question mark
        }

        return mainIconID;
    }


    private static String getPostFixForIcon(String symbolID)
    {
        int aff = SymbolID.getAffiliation(symbolID);
        String pf = "";
        if(aff == SymbolID.StandardIdentity_Affiliation_Friend ||
                aff == SymbolID.StandardIdentity_Affiliation_AssumedFriend)
            pf += "_1";
        else if(aff == SymbolID.StandardIdentity_Affiliation_Neutral)
            pf += "_2";
        else if(aff == SymbolID.StandardIdentity_Affiliation_Hostile_Faker ||
                aff == SymbolID.StandardIdentity_Affiliation_Suspect_Joker)
            pf += "_3";
        else if(aff == SymbolID.StandardIdentity_Affiliation_Unknown ||
                aff == SymbolID.StandardIdentity_Affiliation_Pending)
            pf += "_0";

        return pf;
    }

    public static String getMod1ID(String symbolID)
    {
        String mod1ID = null;


        if((SymbolID.getVersion(symbolID)>=SymbolID.Version_2525E) && symbolID.charAt(20) !='0')
        {//2525E with Modifier 1 Indicator set
            mod1ID = symbolID.substring(20,21) + symbolID.substring(16, 18) + "_1";
        }
        else //2525D or no Modifier 1 Indicator set
        {
            //SIDC positions 5-6 + 17-18 + "1"

            if(SymbolID.getEntity(symbolID)>=11)
                mod1ID = symbolID.substring(4, 6) + symbolID.substring(16, 18) + "1";
            else
                mod1ID = symbolID.substring(4, 6) + "001";

            if(SymbolID.getSymbolSet(symbolID) == SymbolID.SymbolSet_LandUnit)
            {
                switch(SymbolID.getModifier1(symbolID))
                {
                    case 98:
                        mod1ID += getPostFixForIcon(symbolID);
                        break;
                    default:
                        break;
                }
            }
        }
        return mod1ID;
    }

    public static String getMod2ID(String symbolID)
    {
        String mod2ID = null;
        if((SymbolID.getVersion(symbolID)>=SymbolID.Version_2525E) && symbolID.charAt(21) != '0')
        {//2525E with Modifier 1 Indicator set
            mod2ID = symbolID.substring(21,22) + symbolID.substring(18, 20) + "_2";
        }
        else //2525D or no Modifier 1 Indicator set
        {
            //SIDC positions 5-6 + 19-20 + "2"
            if(SymbolID.getEntity(symbolID)>=11)
                mod2ID = symbolID.substring(4, 6) + symbolID.substring(18, 20) + "2";
            else
                mod2ID = symbolID.substring(4, 6) + "002";

            if(SymbolID.getSymbolSet(symbolID) == SymbolID.SymbolSet_LandUnit)
            {
                switch(SymbolID.getModifier2(symbolID))
                {
                    case 60:
                    case 62:
                    case 84:
                    case 89:
                        mod2ID += getPostFixForIcon(symbolID);
                        break;
                    default:
                        break;
                }
            }
        }
        return mod2ID;
    }

    public static String getEchelonAmplifier(String symbolID)
    {
        String amp = null;
        int ver = SymbolID.getVersion(symbolID);
        if (ver < SymbolID.Version_2525E)
        {
            amp = symbolID.charAt(3) + symbolID.substring(8,10);
        }
        else // >= 2525E
        {
            //This will eventually be different with the introduction of the frame shape modifier
            amp = symbolID.charAt(3) + symbolID.substring(8,10);
        }
        return amp;
    }

    public static String getHQTFFD(String symbolID) {
        String hqtffd = null;
        int ver = SymbolID.getVersion(symbolID);
        if (ver < SymbolID.Version_2525E)
        {
            hqtffd = symbolID.substring(3, 6) + symbolID.charAt(7);
        }
        else // >= 2525E
        {
            //This will eventually be different with the introduction of the frame shape modifier
            hqtffd = symbolID.substring(3, 6) + symbolID.charAt(7);
        }
        return hqtffd;
    }

    public static String getOCA(String symbolID, boolean useSlash)
    {
        if(useSlash)
        {
            int status = SymbolID.getStatus(symbolID);
            if(status == SymbolID.Status_Present_Damaged || status == SymbolID.Status_Present_Destroyed)
                return String.valueOf(status);
            else
                return null;
        }
        else//get the bar
        {
            String oca = null;
            int ver = SymbolID.getVersion(symbolID);
            if(ver < SymbolID.Version_2525E)
            {
                oca = symbolID.substring(2,7) + "2";
            }
            else // >= 2525E
            {
                //This will eventually be different with the introduction of the frame shape modifier
                oca = symbolID.substring(2,7) + "2";
            }
            return oca;
        }
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
     * <a href="https://stackoverflow.com/questions/4412848/xml-node-to-string-in-java">
     *     https://stackoverflow.com/questions/4412848/xml-node-to-string-in-java</a>
     */
    /*private static String nodeToString(Node node)
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
    }//*/

    /*
     * For use only by MilStdIconRenderer.addCustomSymbol()
     * @param svgInfo with the basic symbol id, bounds of the SVG, SVG group
     * @param version like SymbolID.Version_2525E
     * @return
     */
    public boolean addCustomSymbol(SVGInfo svgInfo, int version)
    {
        boolean success = false;
        try
        {
            String basicID = svgInfo.getID();
            if (version < SymbolID.Version_2525E)
            {
                if(SVGLookup._SVGLookupD.containsKey(svgInfo.getID()) == false)
                {
                    SVGLookup._SVGLookupD.put(svgInfo.getID(),svgInfo);
                }
            }
            else if (version == SymbolID.Version_2525E)
            {
                if(SVGLookup._SVGLookupE.containsKey(svgInfo.getID()) == false)
                {
                    SVGLookup._SVGLookupE.put(svgInfo.getID(),svgInfo);
                }
            }
        }
        catch(Exception e)
        {
            ErrorLogger.LogException("SVGLookup","addCUstomSymbol",e);
        }
        return success;
    }
}


