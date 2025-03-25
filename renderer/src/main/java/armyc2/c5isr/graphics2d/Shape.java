/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package armyc2.c5isr.graphics2d;

/**
 *
*
 */
public interface Shape {
    public boolean contains (int x, int y);
    public boolean contains (int x, int y, int width, int height);
    public boolean contains (Point2D pt);
    public Rectangle2D getBounds2D();
    public Rectangle getBounds();
    public boolean intersects(double x, double y, double w, double h);
    public boolean intersects(Rectangle2D rect);
    public PathIterator getPathIterator(AffineTransform at);
}
