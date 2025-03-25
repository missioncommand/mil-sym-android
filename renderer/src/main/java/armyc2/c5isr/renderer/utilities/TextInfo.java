package armyc2.c5isr.renderer.utilities;

import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;

public class TextInfo {

	String _text = "";
	Point _location = null;
	Rect _bounds = null;
	public TextInfo(String text, int x, int y, Paint font)
	{
		if(text != null)
		{
			_text = text;
		}

		_location = new Point(x,y);
		_bounds = new Rect();

		font.getTextBounds(_text, 0, _text.length(), _bounds);
	
	}
	
	public void setLocation(int x, int y)
	{
		_bounds.offset(x - _location.x, y - _location.y);
		_location = new Point(x,y);
		//_bounds.offsetTo(x, y - (_bounds.bottom - _bounds.top));
	}
	
	public Point getLocation()
	{
		return _location;
	}
	
	public void shift(int x, int y)
	{
		_location.offset(x, y);
		_bounds.offset(x, y);
	}
	
	public String getText()
	{
		return _text;
	}
	
	public Rect getTextBounds()
	{
		return _bounds;
	}
	

	public Rect getTextOutlineBounds()
	{
		RendererSettings RS = RendererSettings.getInstance();
		int outlineOffset = RS.getTextOutlineWidth();
		Rect bounds = new Rect(_bounds);
		
		if(outlineOffset > 0)
		{
			if(RS.getTextBackgroundMethod() == RendererSettings.TextBackgroundMethod_OUTLINE)
				RectUtilities.grow(bounds, outlineOffset / 2);
			else
				RectUtilities.grow(bounds, outlineOffset);
		}
		
		return bounds;
	}
}
