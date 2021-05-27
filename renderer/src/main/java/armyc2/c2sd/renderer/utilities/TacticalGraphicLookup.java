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
 *
 * @author michael.spinelli
 */
public class TacticalGraphicLookup
{

    private Map<String, Integer> symbolMap = new HashMap<String, Integer>();

    private static TacticalGraphicLookup _instance = null;

    private TacticalGraphicLookup()
    {

    }

    public static synchronized TacticalGraphicLookup getInstance()
    {
        if (_instance == null) {
            _instance = new TacticalGraphicLookup();
        }
        return _instance;
    }

    public synchronized void init(Context context)
    {
        try {
            DataInputStream dis = new DataInputStream(new BufferedInputStream(context.getResources().openRawResource(R.raw.tacticalgraphic)));
            readBinary(dis);
            dis.close();
        } catch (IOException e) {
            Log.e("TacticalGraphicLookup", "Could not load", e);
        }
    }

    /**
     * given the milstd symbol code, find the font index for the symbol.
     * 
     * @param symbolCode
     * @return
     */
    public int getCharCodeFromSymbol(String symbolCode)
    {
        int symStd = RendererSettings.getInstance().getSymbologyStandard();

        return getCharCodeFromSymbol(symbolCode, symStd);

    }

    public int getCharCodeFromSymbol(String symbolCode, int symStd)
    {

        try {
            String basicID = symbolCode;
            int charCode = -1;
            if (SymbolUtilities.is3dAirspace(symbolCode) == false) {
                basicID = SymbolUtilities.getBasicSymbolID(symbolCode);
            }
            if (symbolMap.containsKey(basicID)) {
                charCode = symbolMap.get(basicID);
                if (charCode == 59053) {
                    if (symStd == 1) {
                        charCode = 59052;
                    }
                }
            }
            return charCode;
        } catch (Exception exc) {
            ErrorLogger.LogException("TacticalGraphicLookup", "getCharCodeFromSymbol", exc, Level.WARNING);
        }
        return -1;

    }

    public void readBinary(DataInputStream dis) throws IOException
    {
        int size = dis.readInt();
        for (int i = 0; i < size; i++) {
            String key = dis.readUTF();
            boolean wasValueWritten = dis.readBoolean();
            if (wasValueWritten) {
                symbolMap.put(key, dis.readInt());
            }
        }
    }
}
