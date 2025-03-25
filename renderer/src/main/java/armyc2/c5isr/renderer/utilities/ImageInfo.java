package armyc2.c5isr.renderer.utilities;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Bitmap.Config;
import android.os.Build;

/**
 * Object that holds an image of the symbol and all the information
 * needed to place the symbol on the screen.
*
 */
public class ImageInfo implements SymbolDimensionInfo{
	
	private Point _centerPoint = null;
	private Rect _symbolBounds = null;
	private Rect _imageBounds = null;
	private Bitmap _image = null;
	private int _byteCount = 0;

	/**
	 * Creates a new Image info with copied values but a link to the original image.
	 * @param original {@link ImageInfo}
	 */
	public ImageInfo(ImageInfo original)
	{
		_centerPoint = new Point(original.getCenterPoint());
		_symbolBounds = new Rect(original.getSymbolBounds());
		_image = original.getImage();
		_imageBounds = new Rect(original.getImageBounds());
		_byteCount = original.getByteCount();
	}

	/**
	 * Creates a new ImageInfo object
	 * @param image {@link Bitmap}
	 * @param centerPoint {@link Point} center/anchor point of the symbol within the image
	 * @param symbolBounds {@link Rect} bounds of the symbol within the image
	 */
	public ImageInfo(Bitmap image, Point centerPoint, Rect symbolBounds)
	{
		_centerPoint = centerPoint;
		_symbolBounds = symbolBounds;
		_image = image;

		_imageBounds = RectUtilities.makeRect(0, 0, image.getWidth(), image.getHeight());

		_byteCount = image.getAllocationByteCount();

	}
	
	/**
	 * Not a full clone.  Only centerPoint and symbolBounds are copies.  Bitmap is still a reference. 
	 */
	public ImageInfo getLightClone()
	{
		return new ImageInfo(_image, new Point(_centerPoint), new Rect(_symbolBounds));
	}
	
	public ImageInfo getClone(ImageInfo original)
	{
		Point centerPoint = new Point(original.getCenterPoint());
		Rect symbolBounds = new Rect(original.getSymbolBounds());
		Bitmap image = original.getImage().copy(Config.ARGB_8888, false);
		return new ImageInfo(_image, new Point(_centerPoint), new Rect(_symbolBounds));
	}
	
	public Bitmap getImage()
	{
		return _image;
	}

	/**
	 * The x value the image should be centered on or the "anchor point".
	 * @return {@link Integer}
	 */
	public int getCenterX()
	{
		return _centerPoint.x;
	}

	/**
	 * The y value the image should be centered on or the "anchor point".
	 * @return {@link Integer}
	 */
	public int getCenterY()
	{
		return _centerPoint.y;
	}
	public Point getCenterPoint()
	{
		return _centerPoint;
	}
	
	public Rect getSymbolBounds()
	{
		return _symbolBounds;
	}
	
	public Rect getImageBounds()
	{
		return _imageBounds;
	}

	public int getByteCount()
	{
		return _byteCount;
	}
	public ImageInfo getSquareImageInfo()
	{
		ImageInfo ii = null;
        int iwidth, iheight, x, y;
        int width = this._imageBounds.width();
        int height = this._imageBounds.height();
        
        if(this._imageBounds.width() > this._imageBounds.height())
        {
            iwidth = this._imageBounds.width();
            iheight = this._imageBounds.width();
            x=0;
            y=(iheight - height)/2;
        }
        else if(this._imageBounds.width() < this._imageBounds.height())
        {
            iwidth = this._imageBounds.height();
            iheight = this._imageBounds.height();
            x = (iwidth - width)/2;
            y = 0;
        }
        else
        {
            iwidth = this._imageBounds.width();
            iheight = this._imageBounds.height();
            x=0;
            y=0;
        }

      //Draw glyphs to bitmap
		Bitmap bmp = Bitmap.createBitmap(iwidth, iheight, Config.ARGB_8888);
		Canvas ctx = new Canvas(bmp);

        
        ctx.drawBitmap(_image,x,y,null);
        
        //create new ImageInfo
        Point center = new Point(_centerPoint);
        center.offset(x,y);
        Rect symbolBounds = new Rect(_symbolBounds);
        symbolBounds.offset(x,y);

        ii = new ImageInfo(bmp,center, symbolBounds);
        
        
        return ii;
	}
}
