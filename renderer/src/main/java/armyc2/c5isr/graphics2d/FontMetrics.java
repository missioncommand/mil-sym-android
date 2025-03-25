/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package armyc2.c5isr.graphics2d;
/**
 *
*
 */
public class FontMetrics {
    FontRenderContext _fontRenderContext=null;
    Font _font=null;
    public FontMetrics(Font font)
    {
        //_fontRenderContext=new FontRenderContext();
        _font=font;
    }
    public int stringWidth(String str)
    {
        return _font._size/2*str.length();
    }
    public FontRenderContext getFontRenderContext()
    {
        return _fontRenderContext;
    }
}
