package armyc2.c2sd.renderer.utilities;

import android.graphics.RectF;

public class SVGLInfo {

    private String _ID = null;
    private RectF _Bbox = null;
    private String _SVG = null;
    public SVGLInfo(String id, RectF measurements, String svg)
    {
        _ID = id;
        _Bbox = measurements;
        _SVG = svg;
    }

    public String getID()
    {
        return _ID;
    }

    public RectF getBbox()
    {
        return _Bbox;
    }

    public String getSVG()
    {
        return _SVG;
    }

    public String toString()
    {
        return _ID + "\n" + _Bbox.toString() + "\n" + _SVG;
    }
}