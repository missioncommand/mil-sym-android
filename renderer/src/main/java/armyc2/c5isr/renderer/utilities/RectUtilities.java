package armyc2.c5isr.renderer.utilities;

import android.graphics.Rect;
import android.graphics.RectF;

import armyc2.c5isr.graphics2d.Rectangle;

public class RectUtilities {
	
	public static Rect makeRect(int x, int y, int w, int h)
	{
		return new Rect(x, y, x + w, y + h);
	}
	
	public static Rect makeRect(float x, float y, float w, float h)
	{
		return new Rect((int)x, (int)y, (int)(w+0.5f), (int)(h+0.5f));
	}
	
	public static RectF makeRectF(float x, float y, float w, float h)
	{
		return new RectF(x, y, x + w, y + h);
	}
	
	public static Rect makeRectFromRectF(RectF rect)
	{
		return new Rect((int)rect.left, (int)rect.top, (int)(rect.right+0.5), (int)(rect.bottom+0.5));
	}

	public static RectF makeRectFFromRect(Rect rect)
	{
		return new RectF((float)rect.left, (float)rect.top, (float)rect.right, (float)rect.bottom);
	}
	
	public static void grow(Rect rect, int size)
	{
		rect.set(rect.left - size, rect.top - size, rect.right + (size*2), rect.bottom + (size*2));
		//return new Rect(rect.left - size, rect.top - size, rect.right + size, rect.bottom + size);
	}
	
	public static void grow(RectF rect, int size)
	{
		rect.set(rect.left - size, rect.top - size, rect.right + (size*2), rect.bottom + (size*2));
	}

	public static void grow(Rectangle rect, int size) {
		rect.setRect(new Rectangle(rect.getX() - size, rect.getY() - size, rect.getWidth() + (size*2), rect.getHeight() + (size*2)));
		//return new Rectangle2D.Double(rect.left - size, rect.top - size, rect.right + size, rect.bottom + size);
	}
	
	public static void shift(Rect rect, int x, int y)
	{
		rect.offset(x, y);
	}
	
	public static void shift(RectF rect, int x, int y)
	{
		rect.offset(x, y);
	}
	
	public static int getCenterX(Rect rect)
	{
		return Math.round(rect.left + (rect.right - rect.left)/2);
	}
	public static float getCenterX(RectF rect)
	{
		return (rect.left + (rect.right - rect.left)/2);
	}
	
	public static int getCenterY(Rect rect)
	{
		return Math.round(rect.top + (rect.bottom - rect.top)/2);
	}
	public static float getCenterY(RectF rect)
	{
		return (rect.top + (rect.bottom - rect.top)/2);
	}
	
	public static void shiftBR(Rect rect, int x, int y)
	{
		rect.set(rect.left, rect.top, rect.right + x, rect.bottom + y);
		//return new Rect(rect.left - size, rect.top - size, rect.right + size, rect.bottom + size);
	}
	
	public static void shiftBR(RectF rect, int x, int y)
	{
		rect.set(rect.left, rect.top, rect.right + x, rect.bottom + y);
	}

}
