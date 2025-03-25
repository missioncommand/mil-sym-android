/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package armyc2.c5isr.graphics2d;

/**
 *
*
 */
public final class Font {
    public static int PLAIN=0;
    int _size=10;
    String _text="";
    int _type=0;
    public Font(String s,int type,int size)
    {
        _text=s;
        _type=type;
        _size=size;
        return;
    }
    public int getSize()
    {
        return _size;
    }
}
