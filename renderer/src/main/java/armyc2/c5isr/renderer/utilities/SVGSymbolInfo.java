package armyc2.c5isr.renderer.utilities;

import android.graphics.Point;
import android.graphics.Rect;


public class SVGSymbolInfo implements SymbolDimensionInfo{

    private String _svg = null;
    private String _svgDataURI = null;

    private int _anchorX = 0;
    private int _anchorY = 0;
    private Rect _symbolBounds = null;
    private Rect _bounds = null;

    public SVGSymbolInfo(String svg, Point anchorPoint, Rect symbolBounds, Rect svgBounds)
    {
        _svg = svg;
        _anchorX = (int)anchorPoint.x;
        _anchorY = (int)anchorPoint.y;
        _symbolBounds = symbolBounds;
        _bounds = svgBounds;
    }

    public String getSVGDataURI()
    {
        if(_svgDataURI==null)
        {
            //_svgDataURI = new String(Base64.getEncoder().encode(_svg.getBytes()));//Java
            _svgDataURI = new String(android.util.Base64.encode(_svg.getBytes(),0));
        }
        return _svgDataURI;
    }

    public String getSVG(){return _svg;}

    /**
     * The x value the image should be centered on or the "anchor point".
     * @return {@link Integer}
     */
    public int getCenterX()
    {
        return _anchorX;
    }

    /**
     * The y value the image should be centered on or the "anchor point".
     * @return {@link Integer}
     */
    public int getCenterY()
    {
        return _anchorY;
    }

    /**
     * The point the image should be centered on or the "anchor point".
     * @return {@link Point}
     */
    public Point getCenterPoint()
    {
        return new Point(_anchorX, _anchorY);
    }

    /**
     * minimum bounding rectangle for the core symbol. Does
     * not include modifiers, display or otherwise.
     * @return {@link Rect}
     */
    public Rect getSymbolBounds()
    {
        return _symbolBounds;
    }

    /**
     * Dimension of the entire image.
     * @return {@link Rect}
     */

    public Rect getImageBounds()
    {
        return new Rect(_bounds.left,_bounds.top,_bounds.right,_bounds.bottom);
    }



}
