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
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * responsible for character index lookups for single point.
 * 
 * @author michael.spinelli
 *
 */
public class SinglePointLookup
{

    private static boolean _initCalled = false;
    private static SinglePointLookup _instance;
    private boolean _ready = false;
    private static Map<String, SinglePointLookupInfo> hashMapB = null;
    private static Map<String, SinglePointLookupInfo> hashMapC = null;

    /**
     *
     */
    public boolean getReady()
    {
        return this._ready;
    }

    private SinglePointLookup()
    {

    }

    /**
     *
     */
    public static synchronized SinglePointLookup getInstance()
    {
        if (_instance == null) {
            _instance = new SinglePointLookup();
        }
        return _instance;
    }

    public synchronized void init(Context context)
    {

        if (_initCalled == false) {
            _instance = new SinglePointLookup();

            try {
                DataInputStream dis = new DataInputStream(new BufferedInputStream(context.getResources().openRawResource(R.raw.singlepoint)));
                readBinary(dis);
                dis.close();
            } catch (IOException e) {
                Log.e("SinglePointLookup", "Could not load", e);
            }

            _initCalled = true;
        }
    }

    /**
     * given the milstd symbol code, find the font index for the symbol.
     * 
     * @param symbolCode
     * @return
     */
    public int getCharCodeFromSymbol(String symbolCode, int symStd)
    {

        try {
            String strSymbolLookup = symbolCode;

            Map<String, SinglePointLookupInfo> hashMap = null;

            if (symStd == RendererSettings.Symbology_2525B)
                hashMap = hashMapB;
            else if (symStd == RendererSettings.Symbology_2525C)
                hashMap = hashMapC;

            SinglePointLookupInfo spli = null;
            if (SymbolUtilities.isWeather(strSymbolLookup) || symbolCode.contains("FILL")) {
                spli = hashMap.get(strSymbolLookup);
                if (spli != null)
                    return spli.getMappingP();
                else
                    return -1;

            } else {
                if (!hashMap.containsKey(strSymbolLookup))
                    strSymbolLookup = SymbolUtilities.getBasicSymbolID(strSymbolLookup);

                spli = hashMap.get(strSymbolLookup);
                if (spli != null) {
                    if (SymbolUtilities.getStatus(symbolCode).equals("A") == true)
                        return spli.getMappingA();
                    else
                        return spli.getMappingP();
                } else {
                    return -1;
                }
            }
        } catch (Exception exc) {
            ErrorLogger.LogException("SinglePointLookup", "getCharCodeFromSymbol", exc, Level.WARNING);
        }
        return -1;

    }

    /**
     * Method that retrieves a reference to a SPSymbolDef object from the SinglePointLookup Dictionary.
     *
     * @param strSymbolID - IN - The 15 character symbol Id.
     * @return SPSymbolDef, or null if there was an error.
     */
    /*
     * public SPSymbolDef getSPSymbolDef(String strSymbolID) { String strGenericSymbolID = "";
     * if(strSymbolID.substring(0, 1).equals("G")) { strGenericSymbolID = strSymbolID.substring(0, 1) + "*" +
     * strSymbolID.substring(2, 12); } else { strGenericSymbolID = strSymbolID.substring(0, 10); }
     * 
     * SPSymbolDef data = hashMap.get(strGenericSymbolID); return data; }
     */

    /**
     * Method that retrieves a reference to a SinglePointLookupInfo object from the SinglePointLookup
     * Dictionary.
     * 
     * @param basicSymbolID
     * @return
     */
    public SinglePointLookupInfo getSPLookupInfo(String basicSymbolID, int symStd)
    {
        SinglePointLookupInfo spli = null;
        if (symStd == RendererSettings.Symbology_2525B)
            spli = hashMapB.get(basicSymbolID);
        else if (symStd == RendererSettings.Symbology_2525C)
            spli = hashMapC.get(basicSymbolID);
        return spli;
    }

    /*
     * public static void main(String[] args) { SPSymbolDef data =
     * SinglePointLookup.instance().getSPSymbolDef("G*FPPTC---****X"); int mapping =
     * SinglePointLookup.instance().getCharCodeFromSymbol("G*FPPTC---****X"); String junk = ""; }
     */

    private void readBinary(DataInputStream dis) throws IOException
    {
        int count = dis.readInt();
        hashMapB = new HashMap<>(count);

        for (int i = 0; i < count; i++) {
            SinglePointLookupInfo def = SinglePointLookupInfo.readBinary(dis);
            hashMapB.put(def.getBasicSymbolID(), def);
        }

        count = dis.readInt();
        hashMapC = new HashMap<>(count);
        for (int i = 0; i < count; i++) {
            SinglePointLookupInfo def = SinglePointLookupInfo.readBinary(dis);
            hashMapC.put(def.getBasicSymbolID(), def);
        }
    }
}
