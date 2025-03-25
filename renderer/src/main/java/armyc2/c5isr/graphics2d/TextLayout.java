/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package armyc2.c5isr.graphics2d;
//import android.graphics.drawable.shapes.Shape;
//import android.graphics.drawable.shapes.PathShape;
//import android.graphics.Path;
/**
 *
*
 */
public class TextLayout {
    Font _font=null;    
    String _str="";
    public TextLayout(String s, Font font, FontRenderContext frc)
    {
        _font=font;
        _str=s;
        //return;
    }
    public Shape getOutline(AffineTransform tx)
    {
        return new GeneralPath();
    }
    //used by ShapeInfo
    public Rectangle getPixelBounds(FontRenderContext frc, float x, float y)
    {        
        return null;
    }
    public Rectangle getBounds()
    {
        int width=_font._size/2*_str.length();
        int height=_font.getSize();
        Rectangle rect=new Rectangle(0,0,width,height);
        return rect;
    }
}
