/*
 * To change this template, choose Tools | Templates and open the template in the editor.
 */

package armyc2.c2sd.renderer.utilities;

import armyc2.c2sd.singlepointrenderer.R;

import android.content.Context;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author michael.spinelli
 */
public class UnitDefTable
{

    private static Boolean _initCalled = false;
    private static UnitDefTable _instance = null;
    // private static SymbolTableThingy
    private static Map<String, UnitDef> _UnitDefinitionsB = null;
    private static ArrayList<UnitDef> _UnitDefDupsB = null;

    private static Map<String, UnitDef> _UnitDefinitionsC = null;
    private static ArrayList<UnitDef> _UnitDefDupsC = null;

    private String TAG = "UnitDefTable";

    private UnitDefTable()
    {

    }

    public static synchronized UnitDefTable getInstance()
    {
        if (_instance == null)
            _instance = new UnitDefTable();

        return _instance;
    }

    /*
     * public String[] searchByHierarchy(String hierarchy) { for(UnitDef foo : _UnitDefinitions.values() ) {
     * if(foo.getHierarchy().equalsIgnoreCase(hierarchy)) { return } } }
     */

    public synchronized void init(Context context)
    {
        if (_initCalled == false) {

            _UnitDefinitionsB = new HashMap<>();
            _UnitDefDupsB = new ArrayList<>();

            _UnitDefinitionsC = new HashMap<>();
            _UnitDefDupsC = new ArrayList<>();

            try {
                DataInputStream dis = new DataInputStream(new BufferedInputStream(context.getResources().openRawResource(R.raw.unitconstants)));
                readBinary(dis);
                dis.close();
            } catch (IOException e) {
                Log.e("UnitDefTable", "Could not load", e);
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
     * @return SymbolDef whose Symbol Id matches what is passed in
     */
    public UnitDef getUnitDef(String basicSymbolID, int symStd)
    {
        UnitDef returnVal = null;
        try {
            if (symStd == RendererSettings.Symbology_2525B) {
                returnVal = _UnitDefinitionsB.get(basicSymbolID);
            } else if (symStd == RendererSettings.Symbology_2525C) {
                returnVal = _UnitDefinitionsC.get(basicSymbolID);
            }
        } catch (Exception exc) {
            Log.e("UnitDefTable", exc.getMessage(), exc);
        } catch (Throwable thrown) {
            Log.wtf("UnitDefTable", thrown.getMessage(), thrown);
        }
        return returnVal;
    }

    /**
     *
     * @return
     */
    public Map<String, UnitDef> getAllUnitDefs(int symStd)
    {
        if (symStd == RendererSettings.Symbology_2525B)
            return _UnitDefinitionsB;
        else
            return _UnitDefinitionsC;
    }

    /**
     * SymbolIDs are no longer unique thanks to 2525C and some EMS symbols.
     * Here are the EMS symbols that reused symbol IDs.
     * Like how EMS.INCDNT.CVDIS.DISPOP uses the same symbol code as STBOPS.ITM.RFG (O*I*R-----*****)
     * @param symStd
     * @return
     */
    public ArrayList<UnitDef> getUnitDefDups(int symStd)
    {
        if (symStd == RendererSettings.Symbology_2525B)
            return _UnitDefDupsB;
        else
            return _UnitDefDupsC;
    }

    /**
     * 
     * @param basicSymbolID
     * @return
     */
    public Boolean hasUnitDef(String basicSymbolID, int symStd)
    {
        if (basicSymbolID != null && basicSymbolID.length() == 15) {
            if (symStd == RendererSettings.Symbology_2525B)
                return _UnitDefinitionsB.containsKey(basicSymbolID);
            else if (symStd == RendererSettings.Symbology_2525C)
                return _UnitDefinitionsC.containsKey(basicSymbolID);
            else
                return false;
        } else
            return false;
    }

    private void readBinary(DataInputStream dis) throws IOException
    {
        int count = dis.readInt();
        for (int i = 0; i < count; i++) {
            UnitDef def = UnitDef.readBinary(dis);
            _UnitDefinitionsB.put(def.getBasicSymbolId(), def);
        }

        count = dis.readInt();
        for (int i = 0; i < count; i++) {
            UnitDef def = UnitDef.readBinary(dis);
            _UnitDefDupsB.add(def);
        }

        count = dis.readInt();
        for (int i = 0; i < count; i++) {
            UnitDef def = UnitDef.readBinary(dis);
            _UnitDefinitionsC.put(def.getBasicSymbolId(), def);
        }

        count = dis.readInt();
        for (int i = 0; i < count; i++) {
            UnitDef def = UnitDef.readBinary(dis);
            _UnitDefDupsC.add(def);
        }
    }
}
