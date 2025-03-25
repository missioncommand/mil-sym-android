package armyc2.c5isr.renderer.utilities;
import android.graphics.PointF;

import armyc2.c5isr.graphics2d.Point2D;

public interface IPointConversion {

    public PointF PixelsToGeo(PointF pixel);

    public PointF GeoToPixels(PointF coord);
    
    //public Point2D PixelsToGeo(Point pixel);
    
    //public Point GeoToPixels(Point2D coord);
    
    public Point2D PixelsToGeo(Point2D pixel);
    
    public Point2D GeoToPixels(Point2D coord);

}