/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package armyc2.c5isr.renderer.utilities;


//import android.graphics.Point;
import android.graphics.PointF;
//import Point2D;
//import Point2D;
import armyc2.c5isr.graphics2d.*;
import armyc2.c5isr.graphics2d.Point2D;

/**
 *
*
 */
public class PointConverter3D implements IPointConversion
{
    private double _controlLat=0;
    private double _controlLong=0;
    private double _scale=0;
    private double _metersPerPixel=0;
    public PointConverter3D(double controlLong, double controlLat, double scale)
    {
        try
        {
            this._controlLat=controlLat;
            this._controlLong=controlLong;
            this._scale=scale;
            _metersPerPixel=GeoPixelConversion3D.metersPerPixel(scale);
        }
        catch(Error e)
        {
            throw e;
        }
    }
    public PointF PixelsToGeo(PointF pixel)
    {
    	PointF pt2dGeo=null;
        try
        {
            double y=GeoPixelConversion3D.y2lat(pixel.y, _scale, _controlLat, _metersPerPixel);
            double x=GeoPixelConversion3D.x2long(pixel.x, _scale, _controlLong, y, _metersPerPixel);
            pt2dGeo=new PointF((float)x,(float)y);
        }
        catch(Error e)
        {
            throw e;
        }
        return pt2dGeo;
    }

    public PointF GeoToPixels(PointF coord)
    {
    	PointF ptPixels=null;
        try
        {
            double y=GeoPixelConversion3D.lat2y(coord.y, _scale, _controlLat, _metersPerPixel);
            double x=GeoPixelConversion3D.long2x(coord.x, _scale, _controlLong, coord.y, _metersPerPixel);
            ptPixels=new PointF((float)x,(float)y);
        }
        catch(Error e)
        {
            throw e;
        }
        return ptPixels;
    }
//	@Override
//	public Point2D PixelsToGeo(Point pixel) {
//
//		return (Point2D)PixelsToGeo(new Point2D.Double(pixel.x, pixel.y));
//	}
//	@Override
//	public Point GeoToPixels(Point2D coord) {

//		Point2D temp = PixelsToGeo(coord);
//		return new Point((int)temp.getX(),(int)temp.getY());
//	}
	@Override
	public Point2D PixelsToGeo(Point2D pixel) {
		
		Point2D pt2dGeo=null;
        try
        {
            double y=GeoPixelConversion3D.y2lat(pixel.getY(), _scale, _controlLat, _metersPerPixel);
            double x=GeoPixelConversion3D.x2long(pixel.getX(), _scale, _controlLong, y, _metersPerPixel);
            pt2dGeo=new Point2D.Double(x,y);
        }
        catch(Error e)
        {
            throw e;
        }
        return pt2dGeo;
	}
	//@Override
	public Point2D GeoToPixels(Point2D coord) {
		Point2D ptPixels=null;
        try
        {
            double y=GeoPixelConversion3D.lat2y(coord.getY(), _scale, _controlLat, _metersPerPixel);
            double x=GeoPixelConversion3D.long2x(coord.getX(), _scale, _controlLong, coord.getY(), _metersPerPixel);
            ptPixels=new Point2D.Double(x,y);
        }
        catch(Error e)
        {
            throw e;
        }
        return ptPixels;
	}

}
