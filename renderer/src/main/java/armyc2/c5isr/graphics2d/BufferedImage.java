/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package armyc2.c5isr.graphics2d;

/**
 *
*
 */
public class BufferedImage {
    public static final int TYPE_INT_ARGB = 2;
    public BufferedImage(int width, 
                         int height, 
                         int imageType) {
    }   
    public Graphics2D createGraphics()
    {
        return new Graphics2D();
    }
    public void flush()
    {
        return;
    }
    public double getWidth()
    {
        return 0;
    }
    public double getHeight()
    {
        return 0;
    }
}
