/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package armyc2.c5isr.web.render;
import armyc2.c5isr.renderer.utilities.IPointConversion;
import armyc2.c5isr.graphics2d.Point2D;
import armyc2.c5isr.graphics2d.Point;
import android.graphics.PointF;
/**
 *
*
 */
public class PointConverter implements IPointConversion
{
    private double _controlLat=0;
    private double _controlLong=0;
    private double _scale=0;
    private double _metersPerPixel=0;
    private boolean _normalize=true;
    
    public PointF PixelsToGeo(PointF pixel)
    {
    	return  null;
    }

    public PointF GeoToPixels(PointF coord)
    {
    	return null;
    }
    
    public void set_normalize(boolean value)
    {
        _normalize=value;
    }
    
    public PointConverter(double controlLong, double controlLat, double scale)
    {
        try
        {
            this._controlLat=controlLat;
            this._controlLong=controlLong;
            this._scale=scale;
            _metersPerPixel=GeoPixelConversion.metersPerPixel(scale);
        }
        catch(Error e)
        {
            throw e;
        }
    }
    /**
     * add constructor to handle when earth is flipped about it's X axis (South is on top)
     * @param left
     * @param right
     * @param top
     * @param bottom
     * @param scale 
     */
    public PointConverter(double left, double top, double right, double bottom, double scale)
    {
        try
        {
            this._controlLat=top;
            this._controlLong=left;
            this._scale=scale;
            _metersPerPixel=GeoPixelConversion.metersPerPixel(scale);
            if(top<bottom)
                _metersPerPixel=-_metersPerPixel;
        }
        catch(Error e)
        {
            throw e;
        }
    }
    public Point2D PixelsToGeo(Point pixel)
    {
        Point2D pt2dGeo=null;
        try
        {
            double y=GeoPixelConversion.y2lat(pixel.getY(), _scale, _controlLat, _metersPerPixel);
            double x=GeoPixelConversion.x2long(pixel.getX(), _scale, _controlLong, y, _metersPerPixel);
            pt2dGeo=new Point2D.Double(x,y);
        }
        catch(Error e)
        {
            throw e;
        }
        return pt2dGeo;
    }

    public Point2D PixelsToGeo(Point2D pixel)
    {
        Point2D pt2dGeo=null;
        try
        {
            double y=GeoPixelConversion.y2lat(pixel.getY(), _scale, _controlLat, _metersPerPixel);
            double x=GeoPixelConversion.x2long(pixel.getX(), _scale, _controlLong, y, _metersPerPixel);
            pt2dGeo=new Point2D.Double(x,y);
        }
        catch(Error e)
        {
            throw e;
        }
        return pt2dGeo;
    }

    public Point2D GeoToPixels(Point2D coord)
    {
        Point2D pt2DPixels=null;
        try
        {
            double y=GeoPixelConversion.lat2y(coord.getY(), _scale, _controlLat, _metersPerPixel);
            double x=GeoPixelConversion.long2x(coord.getX(), _scale, _controlLong, coord.getY(), _metersPerPixel, _normalize);
            pt2DPixels=new Point2D.Double(x, y);
        }
        catch(Error e)
        {
            throw e;
        }
        return pt2DPixels;
    }
}
