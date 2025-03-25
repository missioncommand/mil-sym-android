/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package armyc2.c5isr.graphics2d;
import java.util.ArrayList;
import armyc2.c5isr.JavaLineArray.POINT2;
import armyc2.c5isr.JavaLineArray.ref;
import armyc2.c5isr.JavaLineArray.arraysupport;
import armyc2.c5isr.JavaLineArray.lineutility;
import armyc2.c5isr.JavaLineArray.TacticalLines;

/**
 *
*
 */
public class BasicStroke implements Stroke {

    /**
     * Joins path segments by extending their outside edges until they meet.
     */
    public final static int JOIN_MITER = 0;
    /**
     * Joins path segments by rounding off the corner at a radius of half the
     * line width.
     */
    public final static int JOIN_ROUND = 1;
    /**
     * Joins path segments by connecting the outer corners of their wide
     * outlines with a straight segment.
     */
    public final static int JOIN_BEVEL = 2;
    /**
     * Ends unclosed subpaths and dash segments with no added decoration.
     */
    public final static int CAP_BUTT = 0;
    /**
     * Ends unclosed subpaths and dash segments with a round decoration that has
     * a radius equal to half of the width of the pen.
     */
    public final static int CAP_ROUND = 1;
    /**
     * Ends unclosed subpaths and dash segments with a square projection that
     * extends beyond the end of the segment to a distance equal to half of the
     * line width.
     */
    public final static int CAP_SQUARE = 2;
    float width;
    int join;
    int cap;
    float miterlimit;
    float dash[];
    float dash_phase;

    public BasicStroke(float width, int cap, int join, float miterlimit,
            float dash[], float dash_phase) {
        if (width < 0.0f) {
            throw new IllegalArgumentException("negative width");
        }
        if (cap != CAP_BUTT && cap != CAP_ROUND && cap != CAP_SQUARE) {
            throw new IllegalArgumentException("illegal end cap value");
        }
        if (join == JOIN_MITER) {
            if (miterlimit < 1.0f) {
                throw new IllegalArgumentException("miter limit < 1");
            }
        } else if (join != JOIN_ROUND && join != JOIN_BEVEL) {
            throw new IllegalArgumentException("illegal line join value");
        }
        if (dash != null) {
            if (dash_phase < 0.0f) {
                throw new IllegalArgumentException("negative dash phase");
            }
            boolean allzero = true;
            int n=dash.length;
            //for (int i = 0; i < dash.length; i++) 
            for (int i = 0; i < n; i++) 
            {
                float d = dash[i];
                if (d > 0.0) {
                    allzero = false;
                } else if (d < 0.0) {
                    throw new IllegalArgumentException("negative dash length");
                }
            }
            if (allzero) {
                throw new IllegalArgumentException("dash lengths all zero");
            }
        }
        this.width = width;
        this.cap = cap;
        this.join = join;
        this.miterlimit = miterlimit;
        if (dash != null) {
            this.dash = (float[]) dash.clone();
        }
        this.dash_phase = dash_phase;
    }

    public BasicStroke(float width, int cap, int join, float miterlimit) {
        this(width, cap, join, miterlimit, null, 0.0f);
    }

    public BasicStroke(float width, int cap, int join) {
        this(width, cap, join, 10.0f, null, 0.0f);
    }

    /**
     * Constructs a solid <code>BasicStroke</code> with the specified line width
     * and with default values for the cap and join styles.
     *
     * @param width
     * the width of the <code>BasicStroke</code>
     * @throws IllegalArgumentException
     * if <code>width</code> is negative
     */
    public BasicStroke(float width) {
        this(width, CAP_SQUARE, JOIN_MITER, 10.0f, null, 0.0f);
    }

    /**
     * Constructs a new <code>BasicStroke</code> with defaults for all
     * attributes. The default attributes are a solid line of width 1.0,
     * CAP_SQUARE, JOIN_MITER, a miter limit of 10.0.
     */
    public BasicStroke() {
        this(1.0f, CAP_SQUARE, JOIN_MITER, 10.0f, null, 0.0f);
    }

    /**
     * Returns a <code>Shape</code> whose interior defines the stroked outline
     * of a specified <code>Shape</code>.
     *
     * @param s
     * the <code>Shape</code> boundary be stroked
     * @return the <code>Shape</code> of the stroked outline.
     */
    public Shape createStrokedShape(Shape s) {
        /*
         * sun.java2d.pipe.RenderingEngine re =
         * sun.java2d.pipe.RenderingEngine.getInstance(); return
         * re.createStrokedShape(s, width, cap, join, miterlimit, dash,
         * dash_phase);
         */
        return null;
    }
    public Shape createStrokedShape(Polygon poly) {
        /*
         * sun.java2d.pipe.RenderingEngine re =
         * sun.java2d.pipe.RenderingEngine.getInstance(); return
         * re.createStrokedShape(s, width, cap, join, miterlimit, dash,
         * dash_phase);
         */
        ArrayList<POINT2>pts=poly.getPathIterator(null).getPoints();
        int j=0;
        GeneralPath gp=new GeneralPath();
        POINT2 pt=null;
        POINT2[]ptsx=new POINT2[pts.size()];
        int n=pts.size();
        //for(j=0;j<pts.size();j++)
        for(j=0;j<n;j++)
        {
            pt=pts.get(j);
            ptsx[j]=pt;
        }
        
        pts=GetInteriorPoints(ptsx,pts.size(), TacticalLines.DEPTH_AREA,this.width);
        
        
        //for(j=0;j<pts.size();j++)
        for(j=0;j<n;j++)
        {
            pt=pts.get(j);
            if(j==0)
                gp.moveTo(pt.x, pt.y);
            else
                gp.lineTo(pt.x, pt.y);
        }
        return gp;
    }

    public float getLineWidth() {
        return width;
    }

    /**
     * Returns the end cap style.
     *
     * @return the end cap style of this <code>BasicStroke</code> as one of the
     * static <code>int</code> values that define possible end cap
     * styles.
     */
    public int getEndCap() {
        return cap;
    }

    public int getLineJoin() {
        return join;
    }

    /**
     * Returns the limit of miter joins.
     *
     * @return the limit of miter joins of the <code>BasicStroke</code>.
     */
    public float getMiterLimit() {
        return miterlimit;
    }

    /**
     * Returns the array representing the lengths of the dash segments.
     * Alternate entries in the array represent the user space lengths of the
     * opaque and transparent segments of the dashes. As the pen moves along the
     * outline of the <code>Shape</code> to be stroked, the user space distance
     * that the pen travels is accumulated. The distance value is used to index
     * into the dash array. The pen is opaque when its current cumulative
     * distance maps to an even element of the dash array and transparent
     * otherwise.
     *
     * @return the dash array.
     */
    public float[] getDashArray() {
        if (dash == null) {
            return null;
        }
        return (float[]) dash.clone();
    }

    public float getDashPhase() {
        return dash_phase;
    }

    /**
     * Returns the hashcode for this stroke.
     *
     * @return a hash code for this stroke.
     */
    public int hashCode() {
        int hash = Float.floatToIntBits(width);
        hash = hash * 31 + join;
        hash = hash * 31 + cap;
        hash = hash * 31 + Float.floatToIntBits(miterlimit);
        if (dash != null) {
            hash = hash * 31 + Float.floatToIntBits(dash_phase);
            int n=dash.length;
            //for (int i = 0; i < dash.length; i++) 
            for (int i = 0; i < n; i++) 
            {
                hash = hash * 31 + Float.floatToIntBits(dash[i]);
            }
        }
        return hash;
    }
    
    public static ArrayList<POINT2> GetInteriorPoints(POINT2[] pLinePoints, 
            int vblCounter, 
            int lineType, 
            double dist) {
        //var j:int=0;
        int j = 0;
        //var index:int=-1;
        int index = -1;
        //var pt0:POINT2,pt1:POINT2,pt2:POINT2;
        POINT2 pt0 = null, pt1 = null, pt2 = null;
        ///var m01:refobj=new refobj(),m12:refobj=new refobj();	//slopes for lines pt0-pt1 and pt1-pt2
        ref<double[]> m01 = new ref(), m12 = new ref(), m1 = new ref(), m2 = new ref();
        //var direction:int=-1;
        int direction = -1;
        //var array:Array=new Array();
        //ArrayList<POINT2>array=new ArrayList();
        //var intersectPt:POINT2=null;
        POINT2 intersectPt = null;
        //var m1:refobj=new refobj(),m2:refobj=new refobj();
        //var intersectPoints:Array=new Array();
        ArrayList<POINT2> intersectPoints = new ArrayList();
        //var b01:Number,b12:Number;	//the y intercepts for the lines corresponding to m1,m2 
        double b01 = 0, b12 = 0;
        //var dist:Number=10;
        //double dist = 10;
        //the first set of interior points
        //this assumes the area is closed
        for (j = 0; j < vblCounter; j++) {

            if (j == 0 || j == vblCounter - 1) {
                //pt0=new POINT2(pLinePoints[vblCounter-2]);
                //pt1=new POINT2(pLinePoints[0]);
                //pt2=new POINT2(pLinePoints[1]);
                pt0 = pLinePoints[vblCounter - 2];
                pt1 = pLinePoints[0];
                pt2 = pLinePoints[1];
            } else {
                //pt0=new POINT2(pLinePoints[j-1]);
                //pt1=new POINT2(pLinePoints[j]);
                //pt2=new POINT2(pLinePoints[j+1]);					
                pt0 = pLinePoints[j - 1];
                pt1 = pLinePoints[j];
                pt2 = pLinePoints[j+1];
            }

            //the interiior points
            //var pt00:POINT2,pt01:POINT2;
            //var pt10:POINT2,pt11:POINT2;
            POINT2 pt00 = null, pt01 = null;
            POINT2 pt10 = null, pt11 = null;

            index = j - 1;
            if (index < 0) {
                index = vblCounter - 1;
            }

            direction = arraysupport.GetInsideOutsideDouble2(pt0, pt1, pLinePoints, vblCounter, index, lineType);
            //reverse the directions	 since these are interior points
            //pt00-pt01 will be the interior line inside line pt0-pt1
            //pt00 is inside pt0, pt01 is inside pt1
            switch (direction) {
                case 0:
                    //direction=1;
                    pt00 = lineutility.ExtendDirectedLine(pt0, pt1, pt0, 1, dist);
                    pt01 = lineutility.ExtendDirectedLine(pt0, pt1, pt1, 1, dist);
                    break;
                case 1:
                    //direction=0;
                    pt00 = lineutility.ExtendDirectedLine(pt0, pt1, pt0, 0, dist);
                    pt01 = lineutility.ExtendDirectedLine(pt0, pt1, pt1, 0, dist);
                    break;
                case 2:
                    //direction=3;
                    pt00 = lineutility.ExtendDirectedLine(pt0, pt1, pt0, 3, dist);
                    pt01 = lineutility.ExtendDirectedLine(pt0, pt1, pt1, 3, dist);
                    break;
                case 3:
                    //direction=2;
                    pt00 = lineutility.ExtendDirectedLine(pt0, pt1, pt0, 2, dist);
                    pt01 = lineutility.ExtendDirectedLine(pt0, pt1, pt1, 2, dist);
                    break;
            }

            //pt10-pt11 will be the interior line inside line pt1-pt2
            //pt10 is inside pt1, pt11 is inside pt2
            index = j;
            if (j == vblCounter - 1) {
                index = 0;
            }
            direction = arraysupport.GetInsideOutsideDouble2(pt1, pt2, pLinePoints, vblCounter, index, lineType);
            //reverse the directions	 since these are interior points
            switch (direction) {
                case 0:
                    //direction=1;
                    pt10 = lineutility.ExtendDirectedLine(pt1, pt2, pt1, 1, dist);
                    pt11 = lineutility.ExtendDirectedLine(pt1, pt2, pt2, 1, dist);
                    break;
                case 1:
                    //direction=0;
                    pt10 = lineutility.ExtendDirectedLine(pt1, pt2, pt1, 0, dist);
                    pt11 = lineutility.ExtendDirectedLine(pt1, pt2, pt2, 0, dist);
                    break;
                case 2:
                    //direction=3;
                    pt10 = lineutility.ExtendDirectedLine(pt1, pt2, pt1, 3, dist);
                    pt11 = lineutility.ExtendDirectedLine(pt1, pt2, pt2, 3, dist);
                    break;
                case 3:
                    //direction=2;
                    pt10 = lineutility.ExtendDirectedLine(pt1, pt2, pt1, 2, dist);
                    pt11 = lineutility.ExtendDirectedLine(pt1, pt2, pt2, 2, dist);
                    break;
            }	//end switch
            //intersectPt=new POINT2(null);
            //get the intersection of pt01-p00 and pt10-pt11
            //so it it is the interior intersection of pt0-pt1 and pt1-pt2

            //first handle the case of vertical lines.
            if (pt0.x == pt1.x && pt1.x == pt2.x) {
                intersectPt = new POINT2(pt01);
                intersectPoints.add(intersectPt);
                continue;
            }
            //it's the same situation if the slopes are identical,
            //simply use pt01 or pt10 since they already uniquely define the intesection
            lineutility.CalcTrueSlopeDouble2(pt00, pt01, m01);
            lineutility.CalcTrueSlopeDouble2(pt10, pt11, m12);
            if (m01.value[0] == m12.value[0]) {
                intersectPt = new POINT2(pt01);
                intersectPoints.add(intersectPt);
                continue;
            }
            //now we are assuming a non-trivial intersection
            //calculate the y-intercepts using y=mx+b (use b=y-mx)
            b01 = pt01.y - m01.value[0] * pt01.x;
            b12 = pt11.y - m12.value[0] * pt11.x;

            intersectPt = lineutility.CalcTrueIntersectDouble2(m01.value[0], b01, m12.value[0], b12, 1, 1, 0, 0);
            intersectPoints.add(intersectPt);
        }//end for
        return intersectPoints;
    }
}
