package armyc2.c2sd.renderer.utilities;

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

import armyc2.c2sd.singlepointrenderer.R;

public class MSLookup {

    private static MSLookup _instance = null;
    private static Boolean _initCalled = false;

    private static Map<String, MSLInfo> _MSLookup = null;
    private String TAG = "MSLookup";
    private List<String> _IDList = new ArrayList<String>();


    /*
     * Holds SymbolDefs for all symbols. (basicSymbolID, Description, MinPoint, MaxPoints, etc...) Call
     * getInstance().
     *
     */
    private MSLookup() {
        // init(null);
        // _initCalled=true;
    }

    public static synchronized MSLookup getInstance() {
        if (_instance == null) {
            _instance = new MSLookup();
        }

        return _instance;
    }

    public final synchronized void init(Context context) {
        if (_initCalled == false) {
            _MSLookup = new HashMap<>();
            String[] temp = null;
            String delimiter = "\t";

            try {
                InputStream is = context.getResources().openRawResource(R.raw.msd);

                BufferedReader br = new BufferedReader(new InputStreamReader(is));



                String id = null;
                String ss = null;
                String e = null;
                String et = null;
                String est = null;
                String ec = null;
                String g = null;
                String dr = null;

                String line = br.readLine();
                while (line != null) {
                    //parse first line
                    temp = line.split(delimiter);

                    if(temp.length < 5)
                        ec = "000000";
                    else
                        ec = temp[4];

                    if(temp.length < 4)
                        est = "";
                    else
                        est = temp[3];
                    if(temp.length < 3)
                        et = "";
                    else if(est.equals(""))
                        et = temp[2];
                    if(temp.length < 2)
                        e = "";
                    else if(et.equals(""))
                        e = temp[1];

                    if(!temp[0].equals(""))
                    ss = temp[0];

                    id = ss + ec;

                    if(!ec.equals("000000"))
                    {
                        if (temp.length >= 7)
                        {//Control Measures and METOCS
                            g = temp[5];
                            dr = temp[6];
                            _MSLookup.put(id, new MSLInfo(ss, e, et, est, ec, g, dr));
                        }
                        else
                        {//Everything else
                            _MSLookup.put(id, new MSLInfo(ss, e, et, est, ec));
                        }
                        _IDList.add(id);
                    }


                    //read next line for next loop
                    line = br.readLine();
                }

            } catch (IOException ioe) {
                System.out.println(ioe.getMessage());
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    /**
     *
     * @param id SymbolSet + Entity code like 50110100
     * @return
     */
    public MSLInfo getMSLInfo(String id)
    {
        if(_MSLookup.containsKey(id))
            return _MSLookup.get(id);
        else
            return null;
    }

    /**
     * returns a list of all the keys in the order they are listed in the MilStd 2525D document.
     * @return
     */
    public List<String> getIDList()
    {
        return _IDList;
    }
}
