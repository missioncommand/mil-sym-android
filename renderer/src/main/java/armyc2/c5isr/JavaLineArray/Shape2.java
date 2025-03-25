/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package armyc2.c5isr.JavaLineArray;
import armyc2.c5isr.renderer.utilities.ShapeInfo;
import armyc2.c5isr.graphics2d.*;
import armyc2.c5isr.graphics2d.BasicStroke;
import armyc2.c5isr.graphics2d.GeneralPath;
import armyc2.c5isr.graphics2d.Rectangle;

import android.graphics.RectF;
import android.graphics.Path;

import java.util.ArrayList;

/**
 *
 */
public class Shape2 extends ShapeInfo
{

    public Shape2(int value)
    {
        setShapeType(value);
        _Shape=new GeneralPath();
        BasicStroke stroke=new BasicStroke();
        this.setStroke(stroke);
    }
    private int style=0;  //e.g. 26 for enemy flots
    public void set_Style(int value)
    {
        style = value;
    }
    private int fillStyle;
    public void set_Fillstyle(int value)
    {
        fillStyle=value;
    }
    public int get_FillStyle()
    {
        return fillStyle;
    }
    public int get_Style()  //used by TacticalRenderer but not client
    {
        return style;
    }
    public void lineTo(POINT2 pt)
    {
        ((GeneralPath)_Shape).lineTo(pt.x, pt.y);
    }
    public void moveTo(POINT2 pt)
    {       
        ((GeneralPath)_Shape).moveTo(pt.x, pt.y);
    }
    @Override
    public Rectangle getBounds()
    {
        RectF rectf=new RectF();
        if(_Shape instanceof GeneralPath)
        {
            Path path=((GeneralPath)_Shape).getPath();
            path.computeBounds(rectf, true);
            int width=(int)rectf.right-(int)rectf.left;
            int height=(int)rectf.bottom-(int)rectf.top;
            Rectangle rect=new Rectangle((int)rectf.left,(int)rectf.top,width,height);        
            return rect;
        }
        else
            return this.getBounds();
    }

    public ArrayList<POINT2> getPoints() {
        return this.getShape().getPathIterator(null).getPoints();
    }
}
