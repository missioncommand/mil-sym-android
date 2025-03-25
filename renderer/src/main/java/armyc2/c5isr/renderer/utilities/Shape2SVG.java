package armyc2.c5isr.renderer.utilities;

import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;

import java.util.Base64;

public class Shape2SVG {

    /**
     *
     * @param shape like {@link Object}
     * @param stroke like "#000000
     * @param fill like "#0000FF" or "none"
     * @param strokeWidth "#"
     * @param strokeOpacity "1.0"
     * @param fillOpacity "1.0"
     * @param dashArray "4 1 2 3"
     * @return
     */
    public static String Convert(Object shape,String stroke, String fill, String strokeWidth, String strokeOpacity, String fillOpacity, String dashArray)
    {
        if(shape instanceof SVGPath)
            return convertSVGPath((SVGPath)shape, stroke, fill, strokeWidth, strokeOpacity, fillOpacity, dashArray);
        else if(shape instanceof Rect)
            return convertRect((Rect)shape, stroke, fill, strokeWidth, strokeOpacity, fillOpacity, dashArray);
        else if(shape instanceof RectF)
            return convertRectF((RectF)shape, stroke, fill, strokeWidth, strokeOpacity, fillOpacity, dashArray);
        /*else if(shape instanceof Point[])
            return convertPoints((Rect)shape, stroke, fill, strokeWidth, strokeOpacity, fillOpacity, dashArray);
        else if(shape instanceof PointF[])
            return convertPoints((Rect)shape, stroke, fill, strokeWidth, strokeOpacity, fillOpacity, dashArray);//*/
        else
            return null;
    }

    public static String Convert(String text, int x, int y, Paint font, String stroke, String fill, String strokeWidth, String strokeOpacity, String fillOpacity, String dashArray)
    {
        //(String text, int x, int y, Font font, FontRenderContext frc)
        TextInfo textInfo = new TextInfo(text, x, y, font);
        return Convert(textInfo, stroke, fill, strokeWidth, strokeOpacity, fillOpacity, dashArray);
    }

    public static String Convert(TextInfo textInfo, String stroke, String fill, String strokeWidth, String strokeOpacity, String fillOpacity, String dashArray)
    {
        String svg = null;
        StringBuilder sb = new StringBuilder();
        if(textInfo != null)
        {
            String style = null;
            String name = RendererSettings.getInstance().getModiferFontProps()[0] + ", sans-serif";//"SansSerif";
            String size = RendererSettings.getInstance().getModiferFontProps()[2];
            String weight = null;
            String anchor = null;//"start";
            String text = textInfo.getText();

            text = text.replaceAll("&","&amp;");
            text = text.replaceAll("<","&lt;");
            text = text.replaceAll(">","&gt;");

            Point location = new Point(textInfo.getLocation().x,textInfo.getLocation().y);

            if(textInfo.getLocation().x < 0)
            {
                if(textInfo.getLocation().x + textInfo.getTextBounds().width() > 0)
                {
                    anchor = "middle";
                    location.set(textInfo.getTextBounds().centerX(), location.y);
                }
                else
                {
                    anchor = "end";
                    location.set(textInfo.getTextBounds().right, location.y);
                }
            }

            if(RendererSettings.getInstance().getModiferFont().getTypeface().isBold())
                weight = "bold";

            sb.append("<text x=\"" + location.x + "\" y=\"" + location.y + "\"");

            if(anchor != null)
                sb.append(" text-anchor=\"" + anchor + "\"");
            sb.append(" font-family=\"" + name + '"');
            sb.append(" font-size=\"" + size + "px\"");
            if(weight != null)
                sb.append(" font-weight=\"" + weight + "\"");
            sb.append(" alignment-baseline=\"alphabetic\"");//
            sb.append(" stroke-miterlimit=\"3\"");

            //sb.append(" text-anchor=\"" + anchor + "\"");//always start for single points and default SVG behavior

            /*if(this._angle)
            {
                se += ' transform="rotate(' + this._angle + ' ' + this._anchor.getX() + ' ' + this._anchor.getY() + ')"';
            }*/

            String seStroke = "",
                    seFill = "";



            if(stroke != null)
            {
                seStroke = sb.toString();

                seStroke += " stroke=\"" + stroke + "\"";
                /*else
                    seStroke = se + ' stroke="' + stroke.replace(/#/g,"&#35;") + '"';*/

                if(strokeWidth != null)
                    seStroke += " stroke-width=\"" + strokeWidth + "\"";
                seStroke += " fill=\"none\"";
                seStroke += ">";
                seStroke += text;
                seStroke += "</text>";
            }

            if(fill != null)
            {
                seFill = sb.toString();

                seFill += " fill=\"" + fill + "\"";
                seFill += ">";
                seFill += text;
                seFill += "</text>";
            }

            sb = new StringBuilder();
            if(stroke != null && fill != null)
                sb.append(seStroke + "\n" + seFill).append("\n");
            else if(fill != null)
                sb.append(seFill);
            else
                return null;
            return sb.toString();
        }
        return null;
    }

    /**
     * Assumes common font properties will be defined in the group.
     * @param textInfo
     * @param stroke
     * @param fill
     * @param strokeWidth
     * @param strokeOpacity
     * @param fillOpacity
     * @param dashArray
     * @return
     */
    public static String ConvertForGroup(TextInfo textInfo, String stroke, String fill, String strokeWidth, String strokeOpacity, String fillOpacity, String dashArray)
    {
        String svg = null;
        StringBuilder sb = new StringBuilder();
        if(textInfo != null)
        {
            String anchor = null;//"start";
            String text = textInfo.getText();

            text = text.replaceAll("&","&amp;");
            text = text.replaceAll("<","&lt;");
            text = text.replaceAll(">","&gt;");

            Point location = new Point(textInfo.getLocation().x,textInfo.getLocation().y);

            if(textInfo.getLocation().x < 0)
            {
                if(textInfo.getLocation().x + textInfo.getTextBounds().width() > 0)
                {
                    anchor = "middle";
                    location.set(textInfo.getTextBounds().centerX(), location.y);
                }
                else
                {
                    anchor = "end";
                    location.set(textInfo.getTextBounds().right, location.y);
                }
            }



            sb.append("<text x=\"" + location.x + "\" y=\"" + location.y + "\"");

            if(anchor != null)
                sb.append(" text-anchor=\"" + anchor + "\"");

            //sb.append(" text-anchor=\"" + anchor + "\"");//always start for single points and default SVG behavior

            /*if(this._angle)
            {
                se += ' transform="rotate(' + this._angle + ' ' + this._anchor.getX() + ' ' + this._anchor.getY() + ')"';
            }*/

            String seStroke = "",
                    seFill = "";



            if(stroke != null)
            {
                seStroke = sb.toString();

                seStroke += " stroke=\"" + stroke + "\"";
                /*else
                    seStroke = se + ' stroke="' + stroke.replace(/#/g,"&#35;") + '"';*/

                if(strokeWidth != null)
                    seStroke += " stroke-width=\"" + strokeWidth + "\"";
                seStroke += " fill=\"none\"";
                seStroke += ">";
                seStroke += text;
                seStroke += "</text>";
            }

            if(fill != null)
            {
                seFill = sb.toString();


                seFill += " fill=\"" + fill + "\"";
                seFill += ">";
                seFill += text;
                seFill += "</text>";
            }

            sb = new StringBuilder();
            if(stroke != null && fill != null)
                sb.append(seStroke + "\n" + seFill).append("\n");
            else if(fill != null)
                sb.append(seFill);
            else
                return null;
            return sb.toString();
        }
        return null;
    }

    public static String makeBase64Safe(String svg)
    {
        if(svg != null)
        {
            //Base64 encoding
            //return new String(Base64.getEncoder().encode(svg.getBytes()));
            //URL-safe Base64 encoding
            return new String(android.util.Base64.encode(svg.getBytes(),0));
        }
        else
            return null;
    }

    private static String convertArc(Object arc)
    {
        return null;
    }

    /**
     *
     * @param svgPath like {@link Path}
     * @param stroke like "#000000
     * @param fill like "#0000FF" or "none"
     * @param strokeWidth "#"
     * @param strokeOpacity "1.0"
     * @param fillOpacity "1.0"
     * @param dashArray "4 1 2 3", will override dashArray currently specified in the SVGPath object
     * @return
     */
    private static String convertSVGPath(SVGPath svgPath, String stroke, String fill, String strokeWidth, String strokeOpacity, String fillOpacity, String dashArray)
    {
        if(dashArray != null)
            svgPath.setLineDash(dashArray);
        return svgPath.toSVGElement(stroke,Float.parseFloat(strokeWidth),fill,Float.parseFloat(strokeOpacity),Float.parseFloat(fillOpacity));
    }

    private static String convertRect(Rect rect, String stroke, String fill, String strokeWidth, String strokeOpacity, String fillOpacity, String dashArray)
    {
        StringBuilder sb = new StringBuilder();
        if(rect != null && rect.isEmpty() != true)
        {
            sb.append("<rect x=\"" + rect.left + "\" y=\"" + rect.top);
            sb.append("\" width=\"" + rect.width() + "\" height=\"" + rect.height() + "\"");

            if(stroke != null)
            {
                sb.append(" stroke=\"" + stroke + "\"");

                if(strokeWidth != null)
                    sb.append(" stroke-width=\"" + strokeWidth + "\"");
                else
                    sb.append(" stroke-width=\"2\"");
            }

            if(fill != null)
                sb.append(" fill=\"" + fill + "\"");
            else
                sb.append(" fill=\"none\"");

            sb.append("/>");

            return sb.toString();
        }
        else
            return null;
    }

    private static String convertRectF(RectF rect, String stroke, String fill, String strokeWidth, String strokeOpacity, String fillOpacity, String dashArray)
    {
        StringBuilder sb = new StringBuilder();
        if(rect != null && rect.isEmpty() != true)
        {
            sb.append("<rect x=\"" + rect.left + "\" y=\"" + rect.top);
            sb.append("\" width=\"" + rect.width() + "\" height=\"" + rect.height() + "\"");

            if(stroke != null)
            {
                sb.append(" stroke=\"" + stroke + "\"");

                if(strokeWidth != null)
                    sb.append(" stroke-width=\"" + strokeWidth + "\"");
                else
                    sb.append(" stroke-width=\"2\"");
            }

            if(fill != null)
                sb.append(" fill=\"" + fill + "\"");
            else
                sb.append(" fill=\"none\"");

            sb.append("/>");

            return sb.toString();
        }
        else
            return null;
    }
}
