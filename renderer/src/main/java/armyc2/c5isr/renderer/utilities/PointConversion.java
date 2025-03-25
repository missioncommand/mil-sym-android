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
public class PointConversion implements IPointConversion {
    int _pixelWidth = 0;
    int _PixelHeight = 0;
    double _geoTop = 0;
    double _geoLeft = 0;
    double _geoBottom = 0;
    double _geoRight = 0;

    //pixels to geo
    //double _geoMultiplierX = 0;
    //double _geoMultiplierY = 0;
    //geo to pixels
    double _pixelMultiplierX = 0;
    double _pixelMultiplierY = 0;
    boolean _normalize = true;
    public void set_normalize(boolean value)
    {
        _normalize=value;
    }

    public PointConversion(int pixelWidth, int pixelHeight,
                            double geoTop, double geoLeft,
                            double geoBottom, double geoRight)
    {/*
            _pixelWidth = pixelWidth;
            _PixelHeight = pixelHeight;
            _geoTop = geoTop;
            _geoLeft = geoLeft;
            _geoBottom = geoBottom;
            _geoRight = geoRight;*/

            UpdateExtents(pixelWidth, pixelHeight, geoTop, geoLeft, geoBottom, geoRight);
    }

    public void UpdateExtents(int pixelWidth, int pixelHeight,
                            double geoTop, double geoLeft,
                            double geoBottom, double geoRight)
    {
            _pixelWidth = pixelWidth;
            _PixelHeight = pixelHeight;
            _geoTop = geoTop;
            _geoLeft = geoLeft;
            _geoBottom = geoBottom;
            _geoRight = geoRight;

            //_geoMultiplierX = ((double)_pixelWidth) / (_geoRight - _geoLeft) ;
            //_geoMultiplierY = ((double)_PixelHeight) / (_geoTop - _geoBottom) ;

            _pixelMultiplierX = (_geoRight - _geoLeft) / ((double)_pixelWidth) ;
            _pixelMultiplierY = (_geoTop - _geoBottom) / ((double)_PixelHeight) ;   
            
            if(_geoRight-_geoLeft < -180)
            {
                _pixelMultiplierX = (_geoRight - _geoLeft + 360) / ((double)_pixelWidth) ;                
            }
            if(_geoRight-_geoLeft > 180)
            {
                _pixelMultiplierX = (360 - (_geoRight - _geoLeft) ) / ((double)_pixelWidth) ;                
            }
            if(_geoTop < _geoBottom)            
                _pixelMultiplierY = -Math.abs(_pixelMultiplierY);
            else
                _pixelMultiplierY = Math.abs(_pixelMultiplierY);
            
//            if(_geoRight < _geoLeft)            
//                _pixelMultiplierX = -Math.abs(_pixelMultiplierX);
//            else
//                _pixelMultiplierX = Math.abs(_pixelMultiplierX);
            //end section
    }

    @Override
    public PointF PixelsToGeo(PointF pixel)
    {
    	PointF coords = new PointF();

        coords.x = (float)(pixel.x * _pixelMultiplierX + _geoLeft); //xMultiplier;
        coords.y = (float)(_geoTop - (pixel.y * _pixelMultiplierY));
        
        //diagnostic 12-18-12
        if(coords.x > 180)
            coords.x -= 360;
        if(coords.x < -180)
            coords.x += 360;
        //end section
        
        return coords;
    }

    @Override
    public PointF GeoToPixels(PointF coord)
    {
    	PointF pixel = new PointF();

        //double xMultiplier = _pixelMultiplierX;//(_geoRight - _geoLeft) / ((double)_pixelWidth) ;
        //double yMultiplier = _pixelMultiplierY;//(_geoTop - _geoBottom) / ((double)_PixelHeight) ;
        double temp;

        //temp = ((coord.x  - _geoLeft) / _pixelMultiplierX);//xMultiplier);
        double calcValue=coord.x  - _geoLeft;
        if(_normalize)
        {
            if(calcValue<-180)
                calcValue+=360;
            else if(calcValue>180)
                calcValue-=360;
        }
        temp = (calcValue / _pixelMultiplierX);//xMultiplier);

        pixel.x = (float)temp;

        temp = ((_geoTop - coord.y) / _pixelMultiplierY);//yMultiplier);
        pixel.y = (float)temp;
                
        return pixel;
    }
    
//	@Override
//	public Point2D PixelsToGeo(Point pixel) {
//		Point2D coords = new Point2D.Double();
//    	
//    	double x = (float)(pixel.x * _pixelMultiplierX + _geoLeft); //xMultiplier;
//        double y = (float)(_geoTop - (pixel.y * _pixelMultiplierY));
//        
//        //diagnostic 12-18-12
//        if(x > 180)
//            x -= 360;
//        if(x < -180)
//            x += 360;
//        //end section
//        
//        coords.setLocation(x, y);
//        
//        return coords;
//	}

//	@Override
//	public Point GeoToPixels(Point2D coord) {
//		Point pixel = new Point();
//		int x = 0;
//		int y = 0;
//        double temp;
//
//        temp = ((coord.getX()  - _geoLeft) / _pixelMultiplierX);//xMultiplier);
//
//        x = (int)temp;
//
//        temp = ((_geoTop - coord.getY()) / _pixelMultiplierY);//yMultiplier);
//        y = (int)temp;
//                
//        pixel.x = x;
//        pixel.y = y;
//        return pixel;
//	}

	@Override
	public Point2D PixelsToGeo(Point2D pixel) {
		
		Point2D coords = new Point2D.Double();
    	
    	double x = (float)(pixel.getX() * _pixelMultiplierX + _geoLeft); //xMultiplier;
        double y = (float)(_geoTop - (pixel.getY() * _pixelMultiplierY));
        
        //diagnostic 12-18-12
        if(x > 180)
            x -= 360;
        if(x < -180)
            x += 360;
        //end section
        
        coords.setLocation(x, y);
        
        return coords;
	}

	//@Override
	public Point2D GeoToPixels(Point2D coord) {
		
		Point2D pixel = new Point2D.Double();
		double x = 0;
		double y = 0;
        double temp;

        //temp = ((coord.getX()  - _geoLeft) / _pixelMultiplierX);//xMultiplier);
        double calcValue=coord.getX()  - _geoLeft;
        if(_normalize)
        {
            if(calcValue<-180)
                calcValue+=360;
            else if(calcValue>180)
                calcValue-=360;
        }
        temp = (calcValue / _pixelMultiplierX);//xMultiplier);

        x = temp;

        temp = ((_geoTop - coord.getY()) / _pixelMultiplierY);//yMultiplier);
        y = temp;
                
        pixel.setLocation(x,y);
        return pixel;
	}


    public int getPixelWidth()
    {
        return _pixelWidth;
    }

    public int getPixelHeight()
    {
        return _PixelHeight;
    }

    public double getUpperLat()
    {
        return _geoTop;
    }

    public double getLowerLat()
    {
        return _geoBottom;
    }

    public double getLeftLon()
    {
        return _geoLeft;
    }

    public double getRightLon()
    {
        return _geoRight;
    }





}
