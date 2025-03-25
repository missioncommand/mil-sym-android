/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package armyc2.c5isr.graphics2d;

/**
 *
*
 */
public abstract class Rectangle2D {
    public double x=0;
    public double y=0;
    public double width=0;
    public double height=0;
    //do not instantiate the abstract class
    protected Rectangle2D() {
    }

    public void add(double newx, double newy) {
        double x1 = Math.min(getMinX(), newx);
        double x2 = Math.max(getMaxX(), newx);
        double y1 = Math.min(getMinY(), newy);
        double y2 = Math.max(getMaxY(), newy);
        setRect(x1, y1, x2 - x1, y2 - y1);
    }

    public Rectangle2D createIntersection(Rectangle2D r)
    {
        if(r.x>this.x+this.width)
            return null;
        if(r.x+r.width<this.x)
            return null;
        if(r.y>this.y+this.height)
            return null;
        if(r.y+r.height<this.y)
            return null;
        if(r.contains(this))
            return this;
        if(this.contains(r))
            return r;
        
        //if it gets to this point we have a normal intersection
        double x1=0,y1=0,x2=0,y2=0;
        if(this.x<r.x)
        {
            x1=r.x;
            x2=this.x+this.width;
        }
        else
        {
            x1=this.x;
            x2=r.x+r.width;            
        }
        if(this.y<r.y)
        {
            y1=r.y;
            y2=this.y+this.height;
        }
        else
        {
            y1=this.y;
            y2=r.y+r.height;
        }
        return new Rectangle2D.Double(x1,y1,x2-x1,y2-y1);
    }
    public Rectangle2D createUnion(Rectangle2D r)
    {
        return null;
    }
    public double getX()
    {
        return x;
    }
    public double getY()
    {
        return y;
    }
    public double getMinX()
    {
        return x;
    }
    public double getMinY()
    {
        return y;
    }
    public double getMaxX()
    {
        return x+width;
    }
    public double getMaxY()
    {
        return y+height;
    }
    public double getHeight()
    {
        return height;
    }
    public double getWidth()
    {
        return width;
    }
    public boolean contains(double x1, double y1)
    {
        if(x<=x1 && x1<=x+width  && 
                y<=y1 && y1<=y+height)
            return true;
        else return false;
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
    public boolean intersects(int x1, int y1, int width1, int height1)
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
    public boolean contains(Rectangle2D rect)
    {
        double x1=rect.getX();
        double y1=rect.getY();
        if(this.contains(x1, y1))
        {
            x1+=rect.getWidth();
            y1+=rect.getHeight();
            if(this.contains(x1,y1))
                return true;
        }            
        return false;
    }
    public boolean contains(Point2D pt)
    {
        if(x<=pt.getX() && pt.getX()<=x+width)
            if(y<=pt.getY() && pt.getY()<=y+height)
                return true;
        
        return false;
    }
    public boolean intersectsLine(Line2D line)
    {        
        return false;
    }
    public boolean contains(int x, int y, int width, int height)
    {
        double x1=x;
        double y1=y;
        if(this.contains(x1, y1))
        {
            x1+=width;
            y1+=height;
            if(this.contains(x1,y1))
                return true;
        }            
        return false;
    }
    public boolean isEmpty()
    {
        if(width==0 && height==0)
            return true;
        else
            return false;
    }
    public void setRect(double x1, double y1, double width1, double height1)
    {
        x=x1;
        y=y1;
        width=width1;
        height=height1;
    }
    public void setRect(Rectangle2D r)
    {
        x=r.getX();
        y=r.getY();
        width=r.getWidth();
        height=r.getHeight();
    }

    public static class Double extends Rectangle2D{
        public Double()
        {
            x=0;
            y=0;
            width=0;
            height=0;
        }
        public Double(double x1, double y1, double width1, double height1)                
        {
            x=x1;
            y=y1;
            width=width1;
            height=height1;
        }
    }
}
