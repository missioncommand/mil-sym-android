/*
 * To change this template, choose Tools | Templates and open the template in the editor.
 */
package armyc2.c2sd.renderer.utilities;

/*
 * import javax.xml.parsers.DocumentBuilder; import javax.xml.parsers.DocumentBuilderFactory; import
 * javax.xml.parsers.ParserConfigurationException; import org.w3c.dom.*; import org.xml.sax.SAXException;
 * import java.io.*; import java.util.HashMap; import java.util.Map;
 */

import armyc2.c2sd.singlepointrenderer.R;

import android.content.Context;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Responsible for loading tactical graphic symbol definitions into a hash table.
 *
 * @author michael.spinelli
 */
@SuppressWarnings("unused")
public class SymbolDefTable
{

    private static SymbolDefTable _instance = null;
    private static Boolean _initCalled = false;
    // private static SymbolTableThingy
    private static Map<String, SymbolDef> _SymbolDefinitionsB = null;
    private static ArrayList<SymbolDef> _SymbolDefDupsB = null;
    private String TAG = "SymbolDefTable";

    private static Map<String, SymbolDef> _SymbolDefinitionsC = null;
    private static ArrayList<SymbolDef> _SymbolDefDupsC = null;

    private static String propSymbolID = "SYMBOLID";
    private static String propGeometry = "GEOMETRY";
    private static String propDrawCategory = "DRAWCATEGORY";
    private static String propMaxPoint = "MAXPOINTS";
    private static String propMinPoints = "MINPOINTS";
    private static String propHasWidth = "HASWIDTH";
    private static String propModifiers = "MODIFIERS";
    private static String propDescription = "DESCRIPTION";
    private static String propHierarchy = "HIERARCHY";

    /*
     * Holds SymbolDefs for all symbols. (basicSymbolID, Description, MinPoint, MaxPoints, etc...) Call
     * getInstance().
     *
     */
    private SymbolDefTable()
    {
        // init(null);
        // _initCalled=true;
    }

    public static synchronized SymbolDefTable getInstance()
    {
        if (_instance == null) {
            _instance = new SymbolDefTable();
        }

        return _instance;
    }

    public final synchronized void init(Context context)
    {
        if (_initCalled == false) {
            _SymbolDefinitionsB = new HashMap<>();
            _SymbolDefDupsB = new ArrayList<>();

            _SymbolDefinitionsC = new HashMap<>();
            _SymbolDefDupsC = new ArrayList<>();

            try {
                DataInputStream dis = new DataInputStream(new BufferedInputStream(context.getResources().openRawResource(R.raw.symbolconstants)));
                readBinary(dis);
                dis.close();
            } catch (IOException e) {
                Log.e("SymbolDefTable", "Could not load", e);
            }
            _initCalled = true;
        }
    }

    /**
     * @name getSymbolDef
     *
     * @desc Returns a SymbolDef from the SymbolDefTable that matches the passed in Symbol Id
     *
     * @param basicSymbolID - IN - A 15 character MilStd code
     * @param symStd 0 or 1.
     * @see ArmyC2.C2SD.Utilities.RendererSettings#Symbology_2525Bch2_USAS_13_14
     * @see ArmyC2.C2SD.Utilities.RendererSettings#Symbology_2525C
     * @return SymbolDef whose Symbol Id matches what is passed in
     */
    public SymbolDef getSymbolDef(String basicSymbolID, int symStd)
    {
        SymbolDef returnVal = null;
        if (symStd == RendererSettings.Symbology_2525B) {
            returnVal = (SymbolDef) _SymbolDefinitionsB.get(basicSymbolID);
        } else if (symStd == RendererSettings.Symbology_2525C) {
            returnVal = (SymbolDef) _SymbolDefinitionsC.get(basicSymbolID);
        }
        return returnVal;
    }

    /**
     * Returns a Map of all the symbol definitions, keyed on basic symbol code.
     *
     * @param symStd 0 or 1.
     * @see ArmyC2.C2SD.Utilities.RendererSettings#Symbology_2525Bch2_USAS_13_14
     * @see ArmyC2.C2SD.Utilities.RendererSettings#Symbology_2525C
     * @return
     */
    public Map<String, SymbolDef> GetAllSymbolDefs(int symStd)
    {
        if (symStd == RendererSettings.Symbology_2525B) {
            return _SymbolDefinitionsB;
        } else if (symStd == RendererSettings.Symbology_2525C) {
            return _SymbolDefinitionsC;
        } else {
            return null;
        }
    }

    /**
     * SymbolIDs are no longer unique thanks to 2525C and some EMS symbols.
     * Here are the EMS symbols that reused symbol IDs.
     * Like how EMS.INCDNT.CVDIS.DISPOP uses the same symbol code as STBOPS.ITM.RFG (O*I*R-----*****)
     *
     * @param symStd 0 or 1.
     * @see ArmyC2.C2SD.Utilities.RendererSettings#Symbology_2525Bch2_USAS_13_14
     * @see ArmyC2.C2SD.Utilities.RendererSettings#Symbology_2525C
     * @return
     */
    public ArrayList<SymbolDef> GetAllSymbolDefDups(int symStd)
    {
        if (symStd == RendererSettings.Symbology_2525B) {
            return _SymbolDefDupsB;
        } else if (symStd == RendererSettings.Symbology_2525C) {
            return _SymbolDefDupsC;
        } else {
            return null;
        }
    }

    /**
     *
     * @param basicSymbolID
     * @param symStd 0 or 1.
     * @see ArmyC2.C2SD.Utilities.RendererSettings#Symbology_2525Bch2_USAS_13_14
     * @see ArmyC2.C2SD.Utilities.RendererSettings#Symbology_2525C
     * @return
     */
    public Boolean HasSymbolDef(String basicSymbolID, int symStd)
    {
        if (basicSymbolID != null && basicSymbolID.length() == 15) {
            if (symStd == RendererSettings.Symbology_2525B) {
                return _SymbolDefinitionsB.containsKey(basicSymbolID);
            } else if (symStd == RendererSettings.Symbology_2525C) {
                return _SymbolDefinitionsC.containsKey(basicSymbolID);
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Checks if symbol is a multipoint symbol
     *
     * @param symbolID
     * @param symStd
     * @return
     */
    public Boolean isMultiPoint(String symbolID, int symStd)
    {

        String basicSymbolID;

        char codingScheme = symbolID.charAt(0);
        Boolean returnVal = false;
        if (codingScheme == 'G' || codingScheme == 'W') {
            if (symbolID.charAt(1) != '*') {
                basicSymbolID = SymbolUtilities.getBasicSymbolID(symbolID);
            } else {
                basicSymbolID = symbolID;
            }
            SymbolDef sd = this.getSymbolDef(basicSymbolID, symStd);
            if (sd != null) {
                if (sd.getMaxPoints() > 1) {
                    returnVal = true;
                } else {
                    switch (sd.getDrawCategory()) {
                        case 15:// SymbolDef.DRAW_CATEGORY_RECTANGULAR_PARAMETERED_AUTOSHAPE:
                        case 16:// SymbolDef.DRAW_CATEGORY_SECTOR_PARAMETERED_AUTOSHAPE:
                        case 17:// SymbolDef.DRAW_CATEGORY_TWO_POINT_RECT_PARAMETERED_AUTOSHAPE:
                        case 18:// SymbolDef.DRAW_CATEGORY_CIRCULAR_PARAMETERED_AUTOSHAPE:
                        case 19:// SymbolDef.DRAW_CATEGORY_CIRCULAR_RANGEFAN_AUTOSHAPE:
                        case 20:// SymbolDef.DRAW_CATEGORY_ROUTE:
                            returnVal = true;
                            break;
                        default:
                            returnVal = false;
                    }
                }
                return returnVal;
            } else {
                return false;
            }
        } else if (symbolID.startsWith("BS_") || symbolID.startsWith("BBS_") || symbolID.startsWith("PBS_")) {
            return true;
        } else {
            return false;
        }
    }

    private void readBinary(DataInputStream dis) throws IOException
    {
        int count = dis.readInt();
        for (int i = 0; i < count; i++) {
            SymbolDef def = SymbolDef.readBinary(dis);
            _SymbolDefinitionsB.put(def._strBasicSymbolId, def);
        }

        count = dis.readInt();
        for (int i = 0; i < count; i++) {
            SymbolDef def = SymbolDef.readBinary(dis);
            _SymbolDefDupsB.add(def);
        }

        count = dis.readInt();
        for (int i = 0; i < count; i++) {
            SymbolDef def = SymbolDef.readBinary(dis);
            _SymbolDefinitionsC.put(def._strBasicSymbolId, def);
        }

        count = dis.readInt();
        for (int i = 0; i < count; i++) {
            SymbolDef def = SymbolDef.readBinary(dis);
            _SymbolDefDupsC.add(def);
        }
    }
}
