/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
/**
 * @author Denis M. Kishenko
 * @version $Revision$
 */
//THIS CLASS MODIFIED TO WORK ON ANDROID

package armyc2.c5isr.graphics2d;


import android.graphics.Path;
import android.graphics.RectF;
import java.util.Arrays;

/**
 *
*
 */
public class Polygon {

    /**
     * The points buffer capacity
     */
    private static final int BUFFER_CAPACITY = 4;

    public int npoints;
    public int xpoints[];
    public int ypoints[];
    protected Rectangle bounds;

    public Polygon() {
        xpoints = new int[BUFFER_CAPACITY];
        ypoints = new int[BUFFER_CAPACITY];
    }

    /**
     *
     * @param xpoints
     * @param ypoints
     * @param npoints
     */
    public Polygon(int xpoints[], int ypoints[], int npoints) {

        if (npoints > xpoints.length || npoints > ypoints.length) {
            // awt.111=Parameter npoints is greater than array length
            throw new IndexOutOfBoundsException("Parameter npoints is greater than array length");
        }

        if (npoints < 0) {
            // awt.112=Negative number of points
            throw new NegativeArraySizeException("Negative number of points");
        }

        this.npoints = npoints;
        this.xpoints = Arrays.copyOf(xpoints, npoints);
        this.ypoints = Arrays.copyOf(ypoints, npoints);
    }

    public void reset() {
        npoints = 0;
        bounds = null;
    }
    public void invalidate() {
        bounds = null;
    }


    public void addPoint(int px, int py) {
        if (npoints == xpoints.length) {
            int[] tmp;

            tmp = new int[xpoints.length + BUFFER_CAPACITY];
            System.arraycopy(xpoints, 0, tmp, 0, xpoints.length);
            xpoints = tmp;

            tmp = new int[ypoints.length + BUFFER_CAPACITY];
            System.arraycopy(ypoints, 0, tmp, 0, ypoints.length);
            ypoints = tmp;
        }
        xpoints[npoints] = px;
        ypoints[npoints] = py;
        npoints++;
        if (bounds != null) {

            Rectangle temp = new Rectangle(Math.min(bounds.getMinX(), px),
                    Math.min(bounds.getMinY(), py),
                    Math.max(bounds.getMaxX() - bounds.getMinX(), px - bounds.getMinX()),
                    Math.max(bounds.getMaxY() - bounds.getMinY(), py - bounds.getMinY()));

            bounds.setRect(temp.getX(), temp.getY(), temp.getWidth(), temp.getHeight());
        }
    }


    public Rectangle getBounds() {
        if (bounds != null) {
            return bounds;
        }
        if (npoints == 0) {
            return new Rectangle();
        }

        int bx1 = xpoints[0];
        int by1 = ypoints[0];
        int bx2 = bx1;
        int by2 = by1;

        for (int i = 1; i < npoints; i++) {
            int x = xpoints[i];
            int y = ypoints[i];
            if (x < bx1) {
                bx1 = x;
            } else if (x > bx2) {
                bx2 = x;
            }
            if (y < by1) {
                by1 = y;
            } else if (y > by2) {
                by2 = y;
            }
        }

        return bounds = new Rectangle(bx1, by1, bx2 - bx1, by2 - by1);

    }

    public Rectangle getBoundingBox() {
        return getBounds();
    }

    public boolean contains(Point p) {
        return contains(p.x, p.y);
    }

    public boolean contains(int x, int y) {
        return contains((double) x, (double) y);
    }



    public Rectangle2D getBounds2D() {
        //return getBounds();
        return null;
    }//


    public boolean contains(double x, double y) {
        if (npoints <= 2 || !getBoundingBox().contains((int)x, (int)y)) {
            return false;
        }
        int hits = 0;

        int lastx = xpoints[npoints - 1];
        int lasty = ypoints[npoints - 1];
        int curx, cury;

        for (int i = 0; i < npoints; lastx = curx, lasty = cury, i++) {
            curx = xpoints[i];
            cury = ypoints[i];

            if (cury == lasty) {
                continue;
            }

            int leftx;
            if (curx < lastx) {
                if (x >= lastx) {
                    continue;
                }
                leftx = curx;
            } else {
                if (x >= curx) {
                    continue;
                }
                leftx = lastx;
            }

            double test1, test2;
            if (cury < lasty) {
                if (y < cury || y >= lasty) {
                    continue;
                }
                if (x < leftx) {
                    hits++;
                    continue;
                }
                test1 = x - curx;
                test2 = y - cury;
            } else {
                if (y < lasty || y >= cury) {
                    continue;
                }
                if (x < leftx) {
                    hits++;
                    continue;
                }
                test1 = x - lastx;
                test2 = y - lasty;
            }

            if (test1 < (test2 / (lasty - cury) * (lastx - curx))) {
                hits++;
            }
        }

        return ((hits & 1) != 0);
    }



    public boolean contains(Point2D p) {
        return contains(p.getX(), p.getY());
    }


    public boolean intersects(double x, double y, double w, double h) {
        if (npoints <= 0 || !getBoundingBox().intersects(x, y, w, h)) {
            return false;
        }

        if (bounds != null) {
            float fx = (float) x;
            float fy = (float) y;
            float fw = (float) w;
            float fh = (float) h;
//not sure if math is correct here
            Path that = new Path();
//start
            that.moveTo(fx, fy);
//go right
            that.lineTo(fx + fw, fy);
//go down
            that.lineTo(fx + fw, fy - fh);
//go left
            that.lineTo(fx, fy - fh);
//close
            that.close();
//bounds holder
            RectF thatBounds = new RectF();
            RectF rectf=new RectF((float)bounds.x,(float)bounds.y,(float)bounds.x+(float)bounds.width,(float)bounds.y+(float)bounds.height);
            return RectF.intersects(rectf, thatBounds);
        } 
        else 
        {
            return false;
        }        
    }

    public boolean intersects(Rectangle2D r) {
        return intersects(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }

    public boolean contains(double x, double y, double w, double h) {
        if (npoints <= 0 || !getBoundingBox().intersects(x, y, w, h)) {
            return false;
        }

        return false;
    }

    public boolean contains(Rectangle2D r) {
        return contains(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }


    public PathIterator getPathIterator(AffineTransform at) {

        PathIterator pi=new PathIterator(null);
        int j=0;
        if(npoints>0)
        {
            pi.moveTo(xpoints[0], ypoints[0]);
            for(j=1;j<npoints;j++)
            {
                pi.lineTo(xpoints[j], ypoints[j]);
            }
        }
        pi.reset();
        return pi;        
    }
    public PathIterator getPathIterator(AffineTransform at, double flatness) {
        return getPathIterator(at);
    }
}
