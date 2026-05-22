package armyc2.c5isr.renderer.utilities;

import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;


public class SVGSymbolInfo implements SymbolDimensionInfo{

    private String _svg = null;
    private String _svgDataURI = null;

    private float _anchorX = 0;
    private float _anchorY = 0;

    private Rect _symbolBounds = null;
    private Rect _bounds = null;
    private RectF _symbolBoundsF = null;
    private RectF _boundsF = null;

    public SVGSymbolInfo(String svg, Point anchorPoint, Rect symbolBounds, Rect svgBounds)
    {
        _svg = svg;
        _anchorX = anchorPoint.x;
        _anchorY = anchorPoint.y;
        _symbolBounds = symbolBounds;
        _bounds = svgBounds;
        _symbolBoundsF = RectUtilities.makeRectFFromRect(symbolBounds);
        _boundsF = RectUtilities.makeRectFFromRect(svgBounds);
    }

    public SVGSymbolInfo(String svg, PointF anchorPoint, RectF symbolBounds, RectF svgBounds)
    {
        _svg = svg;
        _anchorX = anchorPoint.x;
        _anchorY = anchorPoint.y;
        _symbolBounds = RectUtilities.makeRectFromRectF(symbolBounds);
        _bounds = RectUtilities.makeRectFromRectF(svgBounds);
        _symbolBoundsF = symbolBounds;
        _boundsF = svgBounds;
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
        return (int)_anchorX;
    }

    /**
     * The y value the image should be centered on or the "anchor point".
     * @return {@link Integer}
     */
    public int getCenterY()
    {
        return (int)_anchorY;
    }

    /**
     * The point the image should be centered on or the "anchor point".
     * @return {@link Point}
     */
    public Point getCenterPoint()
    {
        return new Point((int)_anchorX, (int)_anchorY);
    }

    /**
     * The point the image should be centered on or the "anchor point".
     * @return {@link PointF}
     */
    public PointF getCenterPointF()
    {
        return new PointF(_anchorX, _anchorY);
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
     * minimum bounding rectangle for the core symbol. Does
     * not include modifiers, display or otherwise.
     * @return {@link RectF}
     */
    public RectF getSymbolBoundsF()
    {
        return _symbolBoundsF;
    }

    /**
     * Dimension of the entire image.
     * @return {@link Rect}
     */

    public Rect getImageBounds()
    {
        return new Rect(_bounds.left,_bounds.top,_bounds.right,_bounds.bottom);
    }

    /**
     * Dimension of the entire image.
     * @return {@link RectF}
     */
    public RectF getImageBoundsF()
    {
        return new RectF(_boundsF.left,_boundsF.top,_boundsF.right,_boundsF.bottom);
    }


}
