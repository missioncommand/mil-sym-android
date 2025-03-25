/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package armyc2.c5isr.renderer.test3;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import armyc2.c5isr.graphics2d.Point;
import armyc2.c5isr.graphics2d.Point2D;
import armyc2.c5isr.renderer.utilities.IPointConversion;
import java.util.ArrayList;

import armyc2.c5isr.renderer.utilities.MSInfo;
import armyc2.c5isr.renderer.utilities.MSLookup;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import armyc2.c5isr.renderer.utilities.RendererSettings;
import armyc2.c5isr.web.render.GeoPixelConversion;
import armyc2.c5isr.web.render.PointConverter;

/**
 *
 */
public class MyView extends View {

    private static final int BACKGROUND_COLOR = Color.LTGRAY;

    public MyView(Context context) {
        super(context);
        this.context = context;
        this.setBackgroundColor(BACKGROUND_COLOR);
    }
    private static ArrayList<Point> _points = new ArrayList();
    private static Context context = null;

    /**
     * assumes utility extents have been set before call
     * @param event 
     */
    private void displayGeo(MotionEvent event)
    {
        
        double sizeSquare = Math.abs(utility.rightLongitude - utility.leftLongitude);
        if (sizeSquare > 180) {
            sizeSquare = 360 - sizeSquare;
        }

        double screenLength = (double) getWidth() / RendererSettings.getInstance().getDeviceDPI() / GeoPixelConversion.INCHES_PER_METER;
        double metersOnScreen = sizeSquare * GeoPixelConversion.METERS_PER_DEG;
        double scale = metersOnScreen / screenLength;

        Point2D ptPixels = null;
        Point2D ptGeo = null;

        IPointConversion converter = null;
        converter = new PointConverter(utility.leftLongitude, utility.upperLatitude, scale);
        Point pt=new Point((int) event.getAxisValue(MotionEvent.AXIS_X), (int) event.getAxisValue(MotionEvent.AXIS_Y));
        Point2D pt2d=new Point2D.Double(pt.x,pt.y);
        ptGeo=converter.PixelsToGeo(pt2d);
        int n = Log.i("onTouchEvent", "longitude = " + Double.toString(ptGeo.getX()));
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == event.ACTION_DOWN) {
            _points.add(new Point((int) event.getAxisValue(MotionEvent.AXIS_X), (int) event.getAxisValue(MotionEvent.AXIS_Y)));

            displayGeo(event);

            String symbolID  = MainActivity.lineType;
            while (symbolID.length() < 30) {
                symbolID += "0";
            }

            int maxPointCount;
            MSInfo msInfo = MSLookup.getInstance().getMSLInfo(symbolID);
            if (msInfo != null) {
                maxPointCount = msInfo.getMaxPointCount();
            } else {
                maxPointCount = 1000;
            }

            if (_points.size() >= maxPointCount || _points.size() >= 6) {
                invalidate();
            }
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (_points==null || _points.size() < 1) {
            return;
        }
        super.onDraw(canvas);
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(BACKGROUND_COLOR);
        canvas.drawPaint(paint);

        double width = getWidth();
        double height = getHeight();

        utility.set_displayPixelsWidth(width);
        utility.set_displayPixelsHeight(height);

        double left = 49.9;
        double right = 50;
        double top = 20;
        double bottom = top - ((right - left) * height / width);
        if (!MainActivity.extents.isEmpty()) {
            String[] ex = MainActivity.extents.split(",");
            left = Double.parseDouble(ex[0]);
            right = Double.parseDouble(ex[1]);
            top = Double.parseDouble(ex[2]);
            bottom = Double.parseDouble(ex[3]);
        }
        utility.SetExtents(left, right, top, bottom);
        utility.DoubleClickGE(_points, MainActivity.lineType, canvas, context);
        _points.clear();
    }
}
