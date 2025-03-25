/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package armyc2.c5isr.graphics2d;
import java.util.ArrayList;
import armyc2.c5isr.JavaLineArray.POINT2;

/**
 *
*
 */
public class PathIterator implements IPathIterator
{
    private int _currentSeg=0;
    private ArrayList<POINT2>_pts=null;
    public PathIterator(AffineTransform tx)
    {
        _currentSeg=0;
        _pts=new ArrayList();
    }
    public ArrayList<POINT2>getPoints()
    {
        return _pts;
    }
    public int currentSegment(double[]coords)
    {       
        int type=_pts.get(_currentSeg).style;
        if(type== SEG_LINETO || type== SEG_MOVETO)
        {
            coords[0]=_pts.get(_currentSeg).x;
            coords[1]=_pts.get(_currentSeg).y;            
        }
        else if(type== SEG_CUBICTO)
        {
            coords[0]=_pts.get(_currentSeg).x;
            coords[1]=_pts.get(_currentSeg).y;                        
            _currentSeg++;
            coords[2]=_pts.get(_currentSeg).x;
            coords[3]=_pts.get(_currentSeg).y;                        
            _currentSeg++;
            coords[4]=_pts.get(_currentSeg).x;
            coords[5]=_pts.get(_currentSeg).y;                        
        } 
        else if(type== SEG_QUADTO)
        {
            coords[0]=_pts.get(_currentSeg).x;
            coords[1]=_pts.get(_currentSeg).y;                        
            _currentSeg++;
            coords[2]=_pts.get(_currentSeg).x;
            coords[3]=_pts.get(_currentSeg).y;            
        }
        return type;
    }

    /**
     * @deprecated use {@link #currentSegment(double[])}
     */
    public int currentSegment(float[] coords)
    {
        coords[0]=(float)_pts.get(_currentSeg).x;
        coords[1]=(float)_pts.get(_currentSeg).y;
        return _pts.get(_currentSeg).style;
    }
    public int getWindingRule()
    {
        return 1;        
    }
    public boolean isDone()
    {
        if(_currentSeg==_pts.size())
            return true;
        
        return false;
    }
    public void next()
    {
        _currentSeg++;        
    }
    
    //public methods to collect the poins and the moves
    //GeneralPath must call this whenever its getPathIterator method is called to reset the iterator
    public void reset()
    {
        _currentSeg=0;
    }
    public void moveTo(double x, double y)
    {
        _pts.add(new POINT2(x,y, SEG_MOVETO));
    }
    public void lineTo(double x, double y)
    {
        _pts.add(new POINT2(x,y, SEG_LINETO));
    }
    public void cubicTo(double x1, double y1, double x2, double y2, double x3, double y3)
    {
        _pts.add(new POINT2(x1,y1, SEG_CUBICTO));
        _pts.add(new POINT2(x2,y2, SEG_CUBICTO));
        _pts.add(new POINT2(x3,y3, SEG_CUBICTO));
    }
    public void curveTo(double x1, double y1, double x2, double y2, double x3, double y3)
    {
        _pts.add(new POINT2(x1,y1, SEG_CUBICTO));
        _pts.add(new POINT2(x2,y2, SEG_CUBICTO));
        _pts.add(new POINT2(x3,y3, SEG_CUBICTO));
    }
    public void quadTo(double x1, double y1, double x2, double y2)
    {
        _pts.add(new POINT2(x1,y1, SEG_QUADTO));
        _pts.add(new POINT2(x2,y2, SEG_QUADTO));
    }
    public Rectangle2D getBounds(){
        int j=0;
        double left=_pts.get(0).x;
        double right=_pts.get(0).x;
        double top=_pts.get(0).y;
        double bottom=_pts.get(0).y;
        int n=_pts.size();
        //for(j=1;j<_pts.size();j++)
        for(j=1;j<n;j++)
        {
            if(_pts.get(j).x<left)
                left=_pts.get(j).x;
            if(_pts.get(j).x>right)
                right=_pts.get(j).x;
            if(_pts.get(j).y<top)
                top=_pts.get(j).y;
            if(_pts.get(j).y>bottom)
                bottom=_pts.get(j).y;
        }
        Rectangle2D rect=new Rectangle2D.Double(left,top,right-left,bottom-top);
        return rect;
    }
    public void setPathIterator(ArrayList<POINT2>pts)
    {
        reset();
        _pts=pts;
    }
}
