package armyc2.c5isr.renderer.utilities;

import android.graphics.Point;
import android.graphics.Rect;


public interface SymbolDimensionInfo {


    /**
     * The x value the image should be centered on or the "anchor point".
     * @return {@link Integer}
     */
    public int getCenterX();

    /**
     * The y value the image should be centered on or the "anchor point".
     * @return {@link Integer}
     */
    public int getCenterY();

    /**
     * The point the image should be centered on or the "anchor point".
     * @return {@link Point}
     */
    public Point getCenterPoint();

    /**
     * minimum bounding rectangle for the core symbol. Does
     * not include modifiers, display or otherwise.
     * @return {@link Rect}
     */
    public Rect getSymbolBounds();

    /**
     * Dimension of the entire image.
     * @return {@link Rect}
     */

    public Rect getImageBounds();
}
