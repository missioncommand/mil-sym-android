/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package armyc2.c5isr.graphics2d;
/**
 *
*
 */
public class Rectangle implements Shape {
    public int x=0;
    public int y=0;
    public int width=0;
    public int height=0;
    public Rectangle()
    {
        x=0;
        y=0;
        width=0;
        height=0;
    }

    public Rectangle(Rectangle2D rect)
    {
        this((int) rect.x, (int) rect.y, (int) rect.width, (int) rect.height);
    }

    public Rectangle(Rectangle rect)
    {
        this(rect.x, rect.y, rect.width, rect.height);
    }

    public Rectangle(int x1, int y1, int width1, int height1)
    {
        x=x1;
        y=y1;
        width=width1;
        height=height1;
    }
    public Rectangle getBounds()
    {
        return this;
    }
    public PathIterator getPathIterator(AffineTransform at)
    {
        return null;
    }
    public boolean intersects(Rectangle2D rect)
    {
        if(x+width<rect.x)
            return false;
        if(x>rect.x+rect.width)
            return false;
        if(y+height<rect.y)
            return false;
        if(y>rect.y+rect.height)
            return false;
        
        return true;
    }

    public boolean intersects(Rectangle rect)
    {
        return this.intersects(rect.x, rect.y, rect.width, rect.height);
    }

    public boolean intersects(double x1, double y1, double width1, double height1)
    {
        if(x+width<x1)
            return false;
        if(x>x1+width1)
            return false;
        if(y+height<y1)
            return false;
        if(y>y1+height1)
            return false;
        
        return true;
    }
    public boolean contains (int x1, int y1)
    {
        if(x<=x1 && x1<=x+width  && 
                y<=y1 && y1<=y+height)
            return true;
        else return false;
    }
    public boolean contains (int x1, int y1, int width1, int height1)
    {                             
        if(this.contains(x1, y1) && this.contains(x1+width1, y1+height1))
            return true;
        else return false;
    }
    public boolean contains (Point2D pt)
    {
        if(x<=pt.getX() && pt.getX()<=x+width  && 
                y<=pt.getY() && pt.getY()<=y+height)
            return true;
        else return false;
    }
    public Rectangle2D getBounds2D()
    {
        return new Rectangle2D.Double(x,y,width,height);
    }
     public int getX()
    {
        return x;
    }
    public int getY()
    {
        return y;
    }
    public int getMinX()
    {
        return x;
    }
    public int getMinY()
    {
        return y;
    }
    public int getMaxX()
    {
        return x+width;
    }
    public int getMaxY()
    {
        return y+height;
    }
    public int getHeight()
    {
        return height;
    }
    public int getWidth()
    {
        return width;
    }

    public void grow(int h, int v)
    {
        long x0 = this.x;
        long y0 = this.y;
        long x1 = this.width;
        long y1 = this.height;
        x1 += x0;
        y1 += y0;

        x0 -= h;
        y0 -= v;
        x1 += h;
        y1 += v;

        if (x1 < x0) {
            // Non-existant in X direction
            // Final width must remain negative so subtract x0 before
            // it is clipped so that we avoid the risk that the clipping
            // of x0 will reverse the ordering of x0 and x1.
            x1 -= x0;
            if (x1 < Integer.MIN_VALUE) x1 = Integer.MIN_VALUE;
            if (x0 < Integer.MIN_VALUE) x0 = Integer.MIN_VALUE;
            else if (x0 > Integer.MAX_VALUE) x0 = Integer.MAX_VALUE;
        } else { // (x1 >= x0)
            // Clip x0 before we subtract it from x1 in case the clipping
            // affects the representable area of the rectangle.
            if (x0 < Integer.MIN_VALUE) x0 = Integer.MIN_VALUE;
            else if (x0 > Integer.MAX_VALUE) x0 = Integer.MAX_VALUE;
            x1 -= x0;
            // The only way x1 can be negative now is if we clipped
            // x0 against MIN and x1 is less than MIN - in which case
            // we want to leave the width negative since the result
            // did not intersect the representable area.
            if (x1 < Integer.MIN_VALUE) x1 = Integer.MIN_VALUE;
            else if (x1 > Integer.MAX_VALUE) x1 = Integer.MAX_VALUE;
        }

        if (y1 < y0) {
            // Non-existant in Y direction
            y1 -= y0;
            if (y1 < Integer.MIN_VALUE) y1 = Integer.MIN_VALUE;
            if (y0 < Integer.MIN_VALUE) y0 = Integer.MIN_VALUE;
            else if (y0 > Integer.MAX_VALUE) y0 = Integer.MAX_VALUE;
        } else { // (y1 >= y0)
            if (y0 < Integer.MIN_VALUE) y0 = Integer.MIN_VALUE;
            else if (y0 > Integer.MAX_VALUE) y0 = Integer.MAX_VALUE;
            y1 -= y0;
            if (y1 < Integer.MIN_VALUE) y1 = Integer.MIN_VALUE;
            else if (y1 > Integer.MAX_VALUE) y1 = Integer.MAX_VALUE;
        }

        this.x = (int) x0;
        this.y = (int) y0;
        this.width = (int) x1;
        this.height = (int) y1;
    }

    public void setRect(Rectangle rect)
    {
        x=rect.x;
        y=rect.y;
        width=rect.width;
        height=rect.height;
    }

    public void setRect(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void setLocation(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void add(Point2D pt) {
        int newx = (int) pt.getX();
        int newy = (int) pt.getY();
        if ((width | height) < 0) {
            this.x = newx;
            this.y = newy;
            this.width = this.height = 0;
            return;
        }
        int x1 = this.x;
        int y1 = this.y;
        long x2 = this.width;
        long y2 = this.height;
        x2 += x1;
        y2 += y1;
        if (x1 > newx) x1 = newx;
        if (y1 > newy) y1 = newy;
        if (x2 < newx) x2 = newx;
        if (y2 < newy) y2 = newy;
        x2 -= x1;
        y2 -= y1;
        if (x2 > Integer.MAX_VALUE) x2 = Integer.MAX_VALUE;
        if (y2 > Integer.MAX_VALUE) y2 = Integer.MAX_VALUE;
        this.x = x1;
        this.y = y1;
        this.width = (int) x2;
        this.height = (int) y2;
    }

    public Rectangle union(Rectangle r) {
        long tx2 = this.width;
        long ty2 = this.height;
        if ((tx2 | ty2) < 0) {
            // This rectangle has negative dimensions...
            // If r has non-negative dimensions then it is the answer.
            // If r is non-existant (has a negative dimension), then both
            // are non-existant and we can return any non-existant rectangle
            // as an answer.  Thus, returning r meets that criterion.
            // Either way, r is our answer.
            return new Rectangle(r);
        }
        long rx2 = r.width;
        long ry2 = r.height;
        if ((rx2 | ry2) < 0) {
            return new Rectangle(this);
        }
        int tx1 = this.x;
        int ty1 = this.y;
        tx2 += tx1;
        ty2 += ty1;
        int rx1 = r.x;
        int ry1 = r.y;
        rx2 += rx1;
        ry2 += ry1;
        if (tx1 > rx1) tx1 = rx1;
        if (ty1 > ry1) ty1 = ry1;
        if (tx2 < rx2) tx2 = rx2;
        if (ty2 < ry2) ty2 = ry2;
        tx2 -= tx1;
        ty2 -= ty1;
        // tx2,ty2 will never underflow since both original rectangles
        // were already proven to be non-empty
        // they might overflow, though...
        if (tx2 > Integer.MAX_VALUE) tx2 = Integer.MAX_VALUE;
        if (ty2 > Integer.MAX_VALUE) ty2 = Integer.MAX_VALUE;
        return new Rectangle(tx1, ty1, (int) tx2, (int) ty2);
    }
}
