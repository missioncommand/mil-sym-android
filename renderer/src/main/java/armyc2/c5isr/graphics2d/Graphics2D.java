/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package armyc2.c5isr.graphics2d;
import armyc2.c5isr.renderer.utilities.Color;

/**
 *
*
 */
public class Graphics2D {
    private Font _font=null;
    private FontMetrics _fontMetrics=null;
    private FontRenderContext _fontRenderContext=null;
    public Graphics2D()
    {
        _font=new Font("arial",10,10);
        _fontMetrics=new FontMetrics(_font);
    }
    public void setFont(Font value)
    {
        _font=value;
        _fontMetrics=new FontMetrics(_font);
    }
    public Font getFont()
    {
        return null;
    }
    public void setFontMetrics(FontMetrics value)
    {
        _fontMetrics=value;
    }
    public FontMetrics getFontMetrics()
    {
        return _fontMetrics;
    }
    public void setColor(Color color)
    {
        //return;
    }
    public void setBackground(Color color)
    {
        //return;
    }
    public void setTransform(AffineTransform id)
    {
        //return;
    }
    public AffineTransform getTransform()
    {
        return null;
    }
    public void setStroke(BasicStroke stroke)
    {
        //return;
    }
    public void drawLine(double x1, double y1, double x2, double y2 )
    {
        //return;
    }
    public void dispose()
    {
        //return;
    }
    public void rotate(double theta, double x, double y)
    {
        //return;
    }
    public void clearRect(double x, double y, double width, double height)
    {
        //return;
    }
    public void drawString(String s, double x, double y)
    {
        //return;
    }
    public FontRenderContext getFontRenderContext()
    {
        return _fontRenderContext;
    }
}
