package armyc2.c5isr.renderer.utilities;

import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;

import java.util.ArrayList;

public class SVGPath {

    public static float ACTION_MOVE_TO = 0;
    public static float ACTION_LINE_TO = 1;
    public static float ACTION_CURVE_TO = 2;//cubic bezier curve
    public static float ACTION_QUAD_TO = 3;//quadratic bezier curve
    public static float ACTION_ARC_TO = 4;
    public static float ACTION_ARC = 5;
    public static float ACTION_DASHED_LINE_TO = 6;

    private ArrayList<float[]> _actions = new ArrayList<>();
    private String _dashArray = null;
    private PointF _startPoint=null;
    private PointF _endPoint=null;
    private PointF _lastMoveTo = null;
    private RectF _rectangle = null;
    private String _method = null;//stroke,fill,fillPattern

    public void setLineDash(String dashArray)
    {
        this._dashArray = dashArray;
    }

    public Rect getBounds()
    {
        if(this._rectangle != null)
        {
            return RectUtilities.makeRectFromRectF(this._rectangle);
        }
        else
        {
            return null;
        }
    }

    public void shift(int x, int y)
    {
        int size = this._actions.size();
        float[] temp = null;
        RectUtilities.shift(this._rectangle,x,y);

        for(int i=0; i<size;i++)
        {
            temp = this._actions.get(i);
            if(temp[0]==ACTION_MOVE_TO)
            {
                temp[1] = temp[1] + x;
                temp[2] = temp[2] + y;
            }
            else if(temp[0]==ACTION_LINE_TO)
            {
                temp[1] = temp[1] + x;
                temp[2] = temp[2] + y;
            }
            else if(temp[0]==ACTION_CURVE_TO)
            {
                temp[1] = temp[1] + x;
                temp[2] = temp[2] + y;
                temp[3] = temp[3] + x;
                temp[4] = temp[4] + y;
                temp[5] = temp[5] + x;
                temp[6] = temp[6] + y;
            }
            else if(temp[0]==ACTION_QUAD_TO)
            {
                temp[1] = temp[1] + x;
                temp[2] = temp[2] + y;
                temp[3] = temp[3] + x;
                temp[4] = temp[4] + y;
            }
            else if(temp[0]==ACTION_ARC_TO)
            {
                temp[1] = temp[1] + x;
                temp[2] = temp[2] + y;
                temp[3] = temp[3] + x;
                temp[4] = temp[4] + y;
            }
            else if(temp[0]==ACTION_ARC)
            {
                temp[1] = temp[1] + x;
                temp[2] = temp[2] + y;
            }
        }

        this._startPoint.offset(x,y);
        this._endPoint.offset(x,y);
        this._lastMoveTo.offset(x,y);
    }

    /**
     * The number of this._actions on the path
     */
    public int getLength()
    {
        return this._actions.size();
    };

    /**
     * Adds a point to the path by moving to the specified coordinates specified
     * @param x
     * @param y
     */
    public void moveTo(float x, float y)
    {

        if(this._actions.size() == 0)
        {
            this._rectangle = new RectF(x,y,1,1);
            this._startPoint = new PointF(x,y);
            this._endPoint = new PointF(x,y);
            //curr_startPoint = new armyc2.c2sd.renderer.Point(x,y);
            //curr_endPoint = new armyc2.c2sd.renderer.Point(x,y);
        }
        this._rectangle.union(x,y);
        float[] actions = {ACTION_MOVE_TO,x,y};
        this._actions.add(actions);
        this._lastMoveTo = new PointF(x,y);
        this._endPoint = new PointF(x,y);
    }

    /**
     * Adds a point to the path by drawing a straight line from the current
     * coordinates to the new specified coordinates specified
     * @param x
     * @param y
     */
    public void lineTo(float x, float y)
    {

        if(this._actions.size() == 0)
        {
            this.moveTo(0,0);
        }
        float[] actions = {ACTION_LINE_TO,x,y};
        this._actions.add(actions);
        this._rectangle.union(x,y);
        this._endPoint = new PointF(x,y);
    }

    /**
     * Adds a curved segment, defined by three new points, to the path by
     * drawing a Bézier curve that intersects both the current coordinates
     * and the specified coordinates (x,y), using the specified points
     * (cp1x,xp1y) and (cp2x,cp2y) as Bézier control points.
     * @param cp1x
     * @param cp1y
     * @param cp2x
     * @param cp2y
     * @param x
     * @param y
     */
    public void bezierCurveTo(float cp1x, float cp1y, float cp2x, float cp2y, float x, float y){

        if(this._actions.size() == 0)
        {
            this.moveTo(0,0);
        }
        float[] actions = {ACTION_CURVE_TO,cp1x,cp1y,cp2x,cp2y,x,y};
        this._actions.add(actions);
        this._rectangle.union(cp1x,cp1y);
        this._rectangle.union(cp2x,cp2y);
        this._rectangle.union(x,y);
        this._endPoint = new PointF(x,y);
    }

    /**
     * Adds a curved segment, defined by two new points, to the path by
     * drawing a Quadratic curve that intersects both the current
     * coordinates and the specified coordinates (x,y), using the
     * specified point (cpx,cpy) as a quadratic parametric control point.
     * @param cpx
     * @param cpy
     * @param x
     * @param y
     * @returns
     */
    public void quadraticCurveTo(float cpx, float cpy, float x, float y)
    {
        if(this._actions.size() == 0)
        {
            this.moveTo(0,0);
        }
        float[] actions = {ACTION_QUAD_TO,cpx,cpy,x,y};
        this._actions.add(actions);
        this._rectangle.union(cpx,cpy);
        this._rectangle.union(x,y);
        this._endPoint = new PointF(x,y);
    }

    /**
     * The arcTo() method creates an arc/curve between two tangents on the canvas.
     * @param x1 The x-coordinate of the beginning of the arc
     * @param y1 The y-coordinate of the beginning of the arc
     * @param x2 The x-coordinate of the end of the arc
     * @param y2 The y-coordinate of the end of the arc
     * @param r The radius of the arc
     * @returns
     */
    public void arcTo(float x1, float y1, float x2, float y2, float r)
    {
        if(this._actions.size() == 0)
        {
            this.moveTo(0,0);
        }
        float[] actions = {ACTION_ARC_TO,x1,y1,x2,y2};
        this._actions.add(actions);
        this._rectangle.union(x1,y1);
        this._rectangle.union(x2,y2);
        this._endPoint = new PointF(x2,y2);
    }

    /**
     * The arc() method creates an arc/curve
     * (use to create circles. or parts of circles).
     * @param x The x-coordinate of the center of the circle
     * @param y The y-coordinate of the center of the circle
     * @param r The radius of the circle
     * @param sAngle The starting angle, in degrees
     * (0 is at the 3 -'clock position of the arc's circle)
     * @param eAngle The ending angle, in degrees
     * @param counterclockwise Optional. Specifies wheter the drawing
     * should be counterclockwise or clockwise.  False=clockwise,
     * true=counter-clockwise;
     * @returns
     */
    public void arc(float x, float y, float r, float sAngle, float eAngle, boolean counterclockwise)
    {
/*
        if(counterclockwise != true)
        {
            counterclockwise = false;
        }

        //degrees to radians
        float sa = (float)(sAngle * (Math.PI / 180));
        float ea = (float)(eAngle * (Math.PI / 180));


        if(this._startPoint===null)
        {
            double sX = r * Math.cos(sa) + x;
            double sY = r * Math.sin(sa) + y;
            this._startPoint = new PointF((float)sX,(float)sY);
            this._rectangle = new RectF((float)sX,(float)sY,1,1);
        }

        float[] actions = {ACTION_ARC,x,y,r,sa,ea,counterclockwise};
        this._actions.add(actions);
        this._rectangle.union(new Rectangle(x-r,y-r,r*2,r*2));

        var newX = r * Math.cos(ea) + x;
        var newY = r * Math.sin(ea) + y;
        this._endPoint = new Point(newX,newY);
        this.moveTo(newX,newY);//*/

    }

    /**
     * Arc and ArcTo do not covert currently
     */
    public String toSVGElement(String stroke, float strokeWidth, String fill, float strokeOpacity, float fillOpacity)
    {
        int format = 1;


        //context.beginPath();
        int size = this._actions.size();
        float[] temp = null;
        String path = "";
        String line = null;

        try {
            for (int i = 0; i < size; i++)
            {
                temp = this._actions.get(i);

            /*if(path !== "")
                path += " ";*/

                if (temp[0] == ACTION_LINE_TO) {
                    path += "L" + temp[1] + " " + temp[2];
                    //context.lineTo(temp[1],temp[2]);
                } else if (temp[0] == ACTION_MOVE_TO) {
                    //context.moveTo(temp[1],temp[2]);

                    if (i == 0 || this._method != "fillPattern") {
                        path += "M" + temp[1] + " " + temp[2];
                        //context.moveTo(temp[1],temp[2]);
                    } else//no moves in a fill shape except maybe for the first one
                    {
                        path += "L" + temp[1] + " " + temp[2];
                        //context.lineTo(temp[1],temp[2]);
                    }//*/
                } else if (temp[0] == ACTION_DASHED_LINE_TO) {
                    path += "L" + temp[3] + " " + temp[4];
                /*if(this._method === "stroke")
                {
                    context.dashedLineTo(temp[1],temp[2],temp[3],temp[4],temp[5]);
                }
                else //you don't dash a fill shape
                {
                    context.lineTo(temp[3],temp[4]);
                }//*/
                } else if (temp[0] == ACTION_CURVE_TO) {
                    //C100 100 250 100 250 200
                    path += "C" + temp[1] + " " + temp[2] + " " + temp[3] + " " + temp[4] + " " + temp[5] + " " + temp[6];
                    //context.bezierCurveTo(temp[1],temp[2],temp[3],temp[4],temp[5],temp[6]);
                } else if (temp[0] == ACTION_QUAD_TO) {
                    path += "Q" + temp[1] + " " + temp[2] + " " + temp[3] + " " + temp[4];
                    //context.quadraticCurveTo(temp[1],temp[2],temp[3],temp[4]);
                } else if (temp[0] == ACTION_ARC_TO) {
                    //path += "C" + temp[1] + " " + temp[2] + " " + temp[3] + " " + temp[4] + " " + temp[5];
                    //context.arcTo(temp[1],temp[2],temp[3],temp[4],temp[5]);
                } else if (temp[0] == ACTION_ARC) {
                    //context.arc(temp[1],temp[2],temp[3],temp[4],temp[5],temp[6]);
                }//*/
            }
            //TODO: generate path svg element
            line = "<path d=\"" + path + "\"";

            if (stroke != null) {

                line += " stroke=\"" + stroke + "\"";
            /*else
                line += ' stroke="' + stroke.replace(/#/g,"&#35;") + '"';*/

                if (strokeWidth > 0)
                    line += " stroke-width=\"" + strokeWidth + '"';
                else
                    line += " stroke-width=\"2\"";

                if (strokeOpacity != 1.0) {
                    //stroke-opacity="0.4"
                    line += " stroke-opacity=\"" + strokeOpacity + "\"";
                }

                //line += ' stroke-linejoin="round"';
                line += " stroke-linecap=\"round\"";
                //line += ' stroke-linecap="square"';
            }

            if (this._dashArray != null)
                line += " stroke-dasharray=\"" + this._dashArray + "\"";

            if (fill != null) {
                if (fill.indexOf("url") == 0) {
                    line += " fill=\"url(#fillPattern)\"";
                    //line += ' fill="url(&#35;fillPattern)"';
                } else {
                    //line += ' fill="' + fill + '"';
                    line += " fill=\"" + fill + '"';//text = text.replace(/\</g,"&gt;");
                /*else
                    line += ' fill="' + fill.replace(/#/g,"&#35;") + '"';//text = text.replace(/\</g,"&gt;");*/

                    if (fillOpacity != 1.0) {
                        //fill-opacity="0.4"
                        line += " fill-opacity=\"" + fillOpacity + "\"";
                    }
                }

            } else
                line += " fill=\"none\"";

            line += " />";
        }
        catch(Exception exc)
        {
            ErrorLogger.LogException("SVGPath", "toSVGElement", exc);
            line = null;
        }
        return line;

    }

}
