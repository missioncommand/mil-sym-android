/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package armyc2.c5isr.graphics2d;
//import android.graphics.RectF;
/**
 *
*
 * @deprecated
 */
public class TexturePaint {
    private Rectangle2D _rect=null;
    private Graphics2D _g2d=null;
    private BufferedImage _bi=null;
    public TexturePaint(BufferedImage bi, Rectangle2D rect)
    {
        _rect=rect;
        _bi=bi;
    }
}
