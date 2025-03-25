/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package armyc2.c5isr.graphics2d;

/**
 *
*
 */
public class Point {
    public int x=0;
    public int y=0;
    public Point()
    {
        x=0;
        y=0;
    }
    public Point(int x1, int y1)
    {
        x=x1;
        y=y1;
    }
    public int getX()
    {
        return x;
    }
    public int getY()
    {
        return y;
    }
    public void setLocation(int x1, int y1)
    {
        x=x1;
        y=y1;
    }
    public void setLocation(double x1, double y1)
    {
        x=(int)x1;
        y=(int)y1;
    }
    public void setLocation(float x1, float y1)
    {
        x=(int)x1;
        y=(int)y1;
    }
}
