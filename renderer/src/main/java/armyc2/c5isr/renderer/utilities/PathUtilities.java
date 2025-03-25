package armyc2.c5isr.renderer.utilities;

import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.RectF;

public class PathUtilities {

	public static RectF makeRectF(float x, float y, float w, float h)
	{
		return new RectF(x, y, x + w, y + h);
	}
	
	public static void addLine(Path path, float x1, float y1, float x2, float y2)
	{
		path.moveTo(x1,y1);
        path.lineTo(x2,y2);
	}
	
	public static void addEllipse(Path path, float x, float y, float w, float h)
	{
		path.addOval(new RectF(x, y, x + w, y + h), Direction.CW);
	}
	
	public static void addEllipse(Path path, float x, float y, float w, float h, Direction dir)
	{
		path.addOval(new RectF(x, y, x + w, y + h), dir);
	}
	/*
	public static void addEllipse(Path path, int x, int y, int w, int h)
	{
		path.addOval(new RectF(x, y, x + w, y + h), Direction.CW);
	}
	
	public static void addEllipse(Path path, int x, int y, int w, int h, Direction dir)
	{
		path.addOval(new RectF(x, y, x + w, y + h), dir);
	}//*/
	
	/**
	 * 
	 * @param path
	 * @param x
	 * @param y
	 * @param r radius
	 * @param sAngle start angle in degrees
	 * @param eAngle how many degrees relative to sAngle
	 */
	public static void arc(Path path, float x, float y, float r, float sAngle, float eAngle)
	{
		RectF oval = new RectF(x-r, y-r, x+r, y+r);
		path.arcTo(oval, sAngle, eAngle, true);
	}
	
	/**
	 * 
	 * @param path
	 * @param x
	 * @param y
	 * @param r radius
	 * @param sAngle start angle in degrees
	 * @param eAngle how many degrees relative to sAngle
	 * @param moveTo If true, begin a new contour
	 */
	public static void arc(Path path, float x, float y, float r, float sAngle, float eAngle, boolean moveTo)
	{
		RectF oval = new RectF(x-r, y-r, x+r, y+r);
		path.arcTo(oval, sAngle, eAngle, moveTo);
	}
	
	public static void addRoundedRect(Path path, float x, float y, float w, float h, float rw, float rh)
	{
		path.addRoundRect(PathUtilities.makeRectF(x, y, w, h),rw, rh,Direction.CW);
	}
	
	public static void addRoundedRect(Path path, float x, float y, float w, float h, float rw, float rh, Direction dir)
	{
		path.addRoundRect(PathUtilities.makeRectF(x, y, w, h),rw, rh, dir);
	}
	

}
