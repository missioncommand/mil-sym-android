package armyc2.c5isr.web.render.utilities;

import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;

import armyc2.c5isr.graphics2d.FontMetrics;
import armyc2.c5isr.graphics2d.Point2D;
import armyc2.c5isr.graphics2d.Rectangle;
import armyc2.c5isr.graphics2d.Font;
import armyc2.c5isr.renderer.utilities.RectUtilities;
import armyc2.c5isr.renderer.utilities.RendererSettings;

/**
*
 */
public class SVGTextInfo {
    protected String _text;
    protected String _fontName;
    protected int _fontSize = 0;
    protected String _fontStyle;
    protected Point2D _location;
    protected Rectangle _bounds;
    private String justification = "start";
    private double angle = 0;
    private String alignmentBaseline = "middle";

    public SVGTextInfo(String text, Point2D position, String justification, double angle) {
        RendererSettings RS = RendererSettings.getInstance();
        this._fontName = RS.getMPLabelFontName();
        this._fontSize = RS.getMPLabelFontSize();
        this._fontStyle = RS.getMPLabelFontType() == Typeface.BOLD ? "bold" : "normal";

        this._text = text;

        this._location = new Point2D.Double(position.getX(), position.getY());

        this.justification = justification;
        this.angle = angle;

        Paint p = new Paint();
        p.setTypeface(Typeface.create(this._fontName, RS.getMPLabelFontType()));
        p.setTextSize(this._fontSize);
        Rect r = new Rect();
        p.getTextBounds(text, 0, text.length(), r);
        this._bounds = new Rectangle(0, 0, r.width(), _fontSize);
        if (_fontStyle.equals("bold")) // paint.getTextBounds() not handling bold text well
            this._bounds.width *= 1.2;


        if (this.justification.equals("middle"))
            this._bounds = new Rectangle((int) position.getX() - this._bounds.getWidth() / 2, (int) position.getY() - this._bounds.getHeight() / 2, this._bounds.getWidth(), this._bounds.getHeight());
        else if (this.justification.equals("end"))
            this._bounds = new Rectangle((int) position.getX() - this._bounds.getWidth(), (int) position.getY() - this._bounds.getHeight() / 2, this._bounds.getWidth(), this._bounds.getHeight());
        else
            this._bounds = new Rectangle((int) position.getX(), (int) position.getY() - this._bounds.getHeight() / 2, this._bounds.getWidth(), this._bounds.getHeight());

        RectUtilities.grow(this._bounds, 1);

        if (this.angle != 0)
            this._bounds = SVGTextInfo.getRotatedRectangleBounds(this._bounds, position, this.angle, this.justification);
    }


    public Rectangle getTextBounds() {
        return this._bounds;
    }

    public String toSVGElement(String textColor, String outlineColor, double outlineWidth) {
        String fill = textColor;
        String stroke = outlineColor;
        double strokeWidth = outlineWidth;

        double x = this._location.getX();
        double y = this._location.getY();

        String se = "<text";
        if (this.angle == 0)
            se += " x=\"" + x + "\" y=\"" + y + "\"";
        else
            se += " transform=\"translate(" + x + ',' + y + ") rotate(" + this.angle + ")\"";
        se += " font-family=\"" + this._fontName + '"';
        se += " font-size=\"" + this._fontSize + "px\"";
        se += " font-weight=\"" + this._fontStyle + '"';
        se += " alignment-baseline=\"" + this.alignmentBaseline + "\"";
        se += " stroke-miterlimit=\"3\"";
        se += " text-anchor=\"" + justification + '"';

        /*if(this._angle)
        {
            se += ' transform="rotate(' + this._angle + ' ' + this._location.getX() + ' ' + this._location.getY() + ')"';
        }//*/

        String seStroke = null,
                seFill = null;

        String text = this._text;

        if (stroke != null && strokeWidth > 0) {

            seStroke = se + " stroke=\"" + stroke + '"';
            /*else
                seStroke = se + ' stroke="' + stroke.replace(/#/g,"&#35;") + '"';*/

            if (strokeWidth != 0)
                seStroke += " stroke-width=\"" + (strokeWidth + 2) + '"';
            seStroke += " fill=\"none\"";
            seStroke += ">";
            seStroke += text;
            seStroke += "</text>";
        }

        if (fill != null) {

            seFill = se + " fill=\"" + fill + '"';
            /*else
                seFill = se + ' fill="' + fill.replace(/#/g,"%23") + '"';*/
            seFill += ">";
            seFill += text;
            seFill += "</text>";
        }

        if (stroke != null && !stroke.isEmpty() && fill != null && !fill.isEmpty())
            se = seStroke + seFill;
        else if (fill != null && !fill.isEmpty())
            se = seFill;
        else
            se = "";
        return se;
    }

    public static Rectangle getRotatedRectangleBounds(Rectangle rectangle, Point2D pivotPt, double angle, String justification) {
        double textWidth = rectangle.getWidth();

        if (justification.equals("start"))
            rectangle.setRect((int) (rectangle.getX() - textWidth / 2), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight());
        else if (justification.equals("end"))
            rectangle.setRect((int) (rectangle.getX() + textWidth / 2), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight());

        Point2D ptTL = new Point2D.Double(rectangle.getMinX(), rectangle.getMinY());
        Point2D ptTR = new Point2D.Double(rectangle.getMaxX(), rectangle.getMinY());
        Point2D ptBL = new Point2D.Double(rectangle.getMinX(), rectangle.getMaxY());
        Point2D ptBR = new Point2D.Double(rectangle.getMaxX(), rectangle.getMaxY());

        SVGTextInfo.rotatePoint(ptTL, pivotPt, angle);
        SVGTextInfo.rotatePoint(ptTR, pivotPt, angle);
        SVGTextInfo.rotatePoint(ptBL, pivotPt, angle);
        SVGTextInfo.rotatePoint(ptBR, pivotPt, angle);

        rectangle = new Rectangle((int) ptTL.getX(), (int) ptTL.getY(), 0, 0);
        rectangle.add(ptTR);
        rectangle.add(ptBL);
        rectangle.add(ptBR);

        if (justification == "start") {
            double s = Math.sin(angle * 2 * Math.PI / 360);
            double c = Math.cos(angle * 2 * Math.PI / 360);
            rectangle.setRect((int) (rectangle.getX() + textWidth / 2 * c), (int) (rectangle.getY() + textWidth / 2 * s), rectangle.getWidth(), rectangle.getHeight());
        } else if (justification == "end") {
            double s = Math.sin(angle * 2 * Math.PI / 360);
            double c = Math.cos(angle * 2 * Math.PI / 360);
            rectangle.setRect((int) (rectangle.getX() - textWidth / 2 * c), (int) (rectangle.getY() - textWidth / 2 * s), rectangle.getWidth(), rectangle.getHeight());

        }

        return rectangle;
    }

    static void rotatePoint(Point2D pt, Point2D pivotPt, double angle) {
        final double s = Math.sin(-angle * 2 * Math.PI / 360);
        final double c = Math.cos(-angle * 2 * Math.PI / 360);

        pt.setLocation(pt.getX() - pivotPt.getX(), pt.getY() - pivotPt.getY());

        double xnew = pt.getX() * c - pt.getY() * s;
        double ynew = pt.getX() * s + pt.getY() * c;

        pt.setLocation(xnew + pivotPt.getX(), ynew + pivotPt.getY());

    }
}
