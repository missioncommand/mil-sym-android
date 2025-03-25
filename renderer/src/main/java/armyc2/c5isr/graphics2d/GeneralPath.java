/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package armyc2.c5isr.graphics2d;
import android.graphics.Path;
import android.graphics.RectF;
//import android.graphics.Region;
import armyc2.c5isr.JavaLineArray.POINT2;
import java.util.ArrayList;
/**
 *
*
 */
public class GeneralPath implements Shape
{
    private Path _path=null;
    private PathIterator _pathIterator=null;
    public GeneralPath()
    {
        _path=new Path();
        _pathIterator=new PathIterator(null);
    }
    public void lineTo(double x, double y)
    {
        _path.lineTo((float)x, (float)y);
        _pathIterator.lineTo(x, y);    
    }
    public void moveTo(double x, double y)
    {
        _path.moveTo((float)x, (float)y);
        _pathIterator.moveTo(x, y);
    }
    public void quadTo(double x1, double y1, double x2, double y2)
    {
        _path.quadTo((float)x1, (float)y1, (float)x2, (float)y2);
        _pathIterator.quadTo(x1, y1, x2, y2);            
    }
    public void cubicTo(double x1, double y1, double x2, double y2, double x3, double y3)
    {
        _path.cubicTo((float)x1, (float)y1, (float)x2, (float)y2, (float)x3, (float)y3);
        _pathIterator.cubicTo(x1, y1, x2, y2, x3, y3);
    }
    public void curveTo(double x1, double y1, double x2, double y2, double x3, double y3)
    {
        _path.cubicTo((float)x1, (float)y1, (float)x2, (float)y2, (float)x3, (float)y3);
        _pathIterator.cubicTo(x1, y1, x2, y2, x3, y3);    
    }
    public void computeBounds(Rectangle2D rect)
    {
        RectF rectf=new RectF();
        _path.computeBounds(rectf, true);
        rect.x=rectf.left;
        rect.y=rectf.top;
        rect.width=rectf.bottom-rectf.top;
        rect.setRect(rectf.left, rectf.top, rectf.width(), rectf.height());
    }
    public void closePath()
    {
        if(_path != null)
            _path.close();
    }
    public boolean contains (int x, int y)
    {
        return false;
    }
    public boolean contains (Point2D pt)
    {
        return false;
    }
    public boolean contains (int x, int y, int width, int height)
    {
        Rectangle rect2=this.getBounds();
        return rect2.contains(x, y, width, height);
    }
    
    public boolean contains(Rectangle2D r) {        
        Rectangle rect=new Rectangle((int)r.x,(int)r.y,(int)r.width,(int)r.height);
        Rectangle rect2=this.getBounds();
        return rect2.contains(rect.x, rect.y, rect.width, rect.height);
    }
    public Rectangle2D getBounds2D()
    {
        return _pathIterator.getBounds();
    }
    public Rectangle getBounds()
    {
        Rectangle2D rect = _pathIterator.getBounds();
        return new Rectangle((int)rect.x,(int)rect.y,(int)rect.width,(int)rect.height);
    }
    /**
     * Only tests against the bounds, used only when the GeneralPath is a rectangle
     * @param x
     * @param y
     * @param w
     * @param h
     * @return 
     */
    public boolean intersects(double x, double y, double w, double h)
    {        
        return this.getBounds().intersects(x, y, w, h);
    }
    /**
     * called only when the GeneralPath is a rectangle
     * @param rect
     * @return 
     */
    public boolean intersects(Rectangle2D rect)
    {
        return this.getBounds().intersects(rect.x, rect.y, rect.width, rect.height);
    }
    public void append(Shape shape,boolean connect)
    {
        GeneralPath gp=(GeneralPath)shape;
        ArrayList<POINT2>pts=gp._pathIterator.getPoints();
        int j=0;
        POINT2 pt=null;
        POINT2 pt1=null;
        POINT2 pt2=null;
        int n=pts.size();
        //for(j=0;j<pts.size();j++)
        for(j=0;j<n;j++)
        {   
            pt=pts.get(j);
            switch(pt.style)
            {
                case IPathIterator.SEG_MOVETO:
                    _path.moveTo((float)pt.x, (float)pt.y);
                    _pathIterator.moveTo(pt.x, pt.y);
                    break;
                case IPathIterator.SEG_LINETO:
                    _path.lineTo((float)pt.x, (float)pt.y);
                    _pathIterator.lineTo(pt.x, pt.y);
                    break;
                case IPathIterator.SEG_CUBICTO:
                    pt1=pts.get(j+1);j++;
                    pt2=pts.get(j+2);j++;
                    _path.cubicTo((float)pt.x, (float)pt.y, (float)pt1.x, (float)pt1.y, (float)pt2.x, (float)pt2.y);
                    _pathIterator.cubicTo((float)pt.x, (float)pt.y, (float)pt1.x, (float)pt1.y, (float)pt2.x, (float)pt2.y);
                    break;
                default:
                    break;                    
            }
        }        
    }
    public Path getPath()
    {
        return _path;
    }
    public PathIterator getPathIterator(AffineTransform tx)
    {
        _pathIterator.reset();
        return _pathIterator;
    }
}
