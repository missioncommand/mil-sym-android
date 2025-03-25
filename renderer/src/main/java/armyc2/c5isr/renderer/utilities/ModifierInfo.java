package armyc2.c5isr.renderer.utilities;

import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.shapes.Shape;

/**
 * @deprecated
 */
public class ModifierInfo {

	private String _key = null;
	private String _text = null;
	private PointF _drawPoint = null;
	private RectF _bounds = null;
	private Paint _paint = null;
	private Shape _shape = null;
	
	/**
	 * 
	 * @param text
	 * @param key like Modifiers, MilStdAttributes
	 * @param drawPoint
	 * @param paint
	 */
	public ModifierInfo(String text, String key, PointF drawPoint, Paint paint)
	{
		_key = key;
		_text = text;
		_drawPoint = drawPoint;
		_paint = paint;
		Rect rTemp = new Rect();
		paint.getTextBounds(text, 0, text.length(), rTemp);
		_bounds = new RectF(rTemp.left,rTemp.top,rTemp.width(),rTemp.height());
	}
	
	public ModifierInfo(Shape shape, String key, Paint paint, RectF bounds)
	{
		_shape = shape;
		_key = key;
		_paint = paint;
		if(bounds != null)
			_bounds = bounds;
		else
			_bounds = new RectF(0,0,shape.getWidth(),shape.getWidth());
	}
	
	public Paint getPaint()
	{
		return _paint;
	}
	
	public Shape getShape()
	{
		return _shape;
	}
	
	public String getKey()
	{
		return _key;
	}
	
	public String getText()
	{
		return _text;
	}
	
	public RectF getBounds()
	{
		return _bounds;
	}
	
	public PointF getDrawPoint()
	{
		return _drawPoint;
	}
	
	public void setDrawPoint(PointF value)
	{
		_drawPoint = value;
	}
}
