package armyc2.c2sd.renderer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.Path.Direction;
import android.util.SparseArray;
import armyc2.c2sd.renderer.utilities.Color;
import armyc2.c2sd.renderer.utilities.ImageInfo;
import armyc2.c2sd.renderer.utilities.MilStdAttributes;
import armyc2.c2sd.renderer.utilities.ModifiersTG;
import armyc2.c2sd.renderer.utilities.ModifiersUnits;
import armyc2.c2sd.renderer.utilities.PathUtilties;
import armyc2.c2sd.renderer.utilities.RectUtilities;
import armyc2.c2sd.renderer.utilities.RendererSettings;
import armyc2.c2sd.renderer.utilities.RendererUtilities;
import armyc2.c2sd.renderer.utilities.SymbolUtilities;
import armyc2.c2sd.renderer.utilities.TextInfo;
import armyc2.c2sd.renderer.utilities.UnitFontLookup;

public class ModifierRenderer
{

    private static Paint _modifierFont = null;
    private static float _modifierFontHeight = 10f;
    private static float _modifierFontDescent = 2f;
    private static RendererSettings RS = RendererSettings.getInstance();

    public static void setModifierFont(Paint font, float height, float descent)
    {
        _modifierFont = font;
        _modifierFontHeight = height;
        _modifierFontDescent = descent;
    }

    public static ImageInfo processUnitDisplayModifiers(ImageInfo ii, String symbolID, SparseArray<String> modifiers, Boolean hasTextModifiers, SparseArray<String> attributes)
    {

        ImageInfo newii = null;
        Rect symbolBounds = new Rect(ii.getSymbolBounds());
        Rect imageBounds = new Rect(ii.getImageBounds());
        Point centerPoint = new Point(ii.getCenterPoint());
        Point symbolCenter = new Point(symbolBounds.centerX(), symbolBounds.centerY());
        TextInfo tiEchelon = null;
        TextInfo tiAM = null;
        Rect echelonBounds = null;
        Rect amBounds = null;
        Color textColor = Color.BLACK;
        Color textBackgroundColor = null;
        Color modifierColor = Color.BLACK;

        int buffer = 0;
        int alpha = -1;
        //ctx = null;
        int offsetX = 0;
        int offsetY = 0;
        int symStd = RS.getSymbologyStandard();
        if (attributes.indexOfKey(MilStdAttributes.SymbologyStandard) >= 0)
        {
            symStd = Integer.parseInt(attributes.get(MilStdAttributes.SymbologyStandard));
        }
        if (attributes.indexOfKey(MilStdAttributes.Alpha) >= 0)
        {
            alpha = Integer.parseInt(attributes.get(MilStdAttributes.Alpha));
            textColor.setAlpha(alpha);
        }
        if (attributes.indexOfKey(MilStdAttributes.TextColor) >= 0)
        {
            textColor = SymbolUtilities.getColorFromHexString(attributes.get(MilStdAttributes.TextColor));
            if(alpha > -1)
                textColor.setAlpha(alpha);
        }
        if (attributes.indexOfKey(MilStdAttributes.TextBackgroundColor) >= 0)
        {
            textBackgroundColor = SymbolUtilities.getColorFromHexString(attributes.get(MilStdAttributes.TextBackgroundColor));
            if(alpha > -1)
                textBackgroundColor.setAlpha(alpha);
        }
        if (attributes.indexOfKey(MilStdAttributes.ModifierColor) >= 0)
        {
            modifierColor = SymbolUtilities.getColorFromHexString(attributes.get(MilStdAttributes.ModifierColor));
            if (alpha > -1)
                modifierColor.setAlpha(alpha);
        }


        // <editor-fold defaultstate="collapsed" desc="Build Mobility Modifiers">
        RectF mobilityBounds = null;
        
        List<Path> shapes = new ArrayList<Path>();
        Path mobilityPath = null;
        Path mobilityPathFill = null;
        if (symbolID.charAt(10) == ('M') && SymbolUtilities.canUnitHaveModifier(symbolID, ModifiersUnits.R_MOBILITY_INDICATOR) ||
                symbolID.charAt(10) == ('N')  && SymbolUtilities.canUnitHaveModifier(symbolID, ModifiersUnits.AG_AUX_EQUIP_INDICATOR) )
        {

            //Draw Mobility
            int fifth = (int) ((symbolBounds.width() * 0.2) + 0.5f);
            mobilityPath = new Path();
            int x = 0;
            int y = 0;
            int centerX = 0;
            int bottomY = 0;
            int height = 0;
            int width = 0;
            int middleY = 0;
            int wheelOffset = 2;
            int wheelSize = fifth;//10;
            int rrHeight = fifth;//10;
            int rrArcWidth = (int) ((fifth * 1.5) + 0.5f);//16;

            String mobility = symbolID.substring(10, 12);
            x = (int) symbolBounds.left + 1;
            y = (int) symbolBounds.top;
            height = Math.round(symbolBounds.height());
            width = Math.round(symbolBounds.width()) - 3;
            bottomY = y + height + 2;

            if (symbolID.charAt(10) == ('M'))
            {
                bottomY = y + height + 2;

                //wheelSize = width / 7;
                //rrHeight = width / 7;
                //rrArcWidth = width / 7;
                if (mobility.equals("MO"))
                {
                    //line
                    PathUtilties.addLine(mobilityPath, x, bottomY, x + width, bottomY);

                    //left circle
                    PathUtilties.addEllipse(mobilityPath, x, bottomY + wheelOffset, wheelSize, wheelSize);

                    //right circle
                    PathUtilties.addEllipse(mobilityPath, x + width - wheelSize, bottomY + wheelOffset, wheelSize, wheelSize);
                }
                else if (mobility.equals("MP"))
                {
                    //line
                    PathUtilties.addLine(mobilityPath, x, bottomY, x + width, bottomY);

                    //left circle
                    PathUtilties.addEllipse(mobilityPath, x, bottomY + wheelOffset, wheelSize, wheelSize);

                    //right circle
                    PathUtilties.addEllipse(mobilityPath, x + width - wheelSize, bottomY + wheelOffset, wheelSize, wheelSize);

                    //center wheel
                    PathUtilties.addEllipse(mobilityPath, x + (width / 2) - (wheelSize / 2), bottomY + wheelOffset, wheelSize, wheelSize);
                }
                else if (mobility.equals("MQ"))
                {
                    //round rectangle
                    PathUtilties.addRoundedRect(mobilityPath, x, bottomY, width, rrHeight, rrHeight/2, rrHeight);

                }
                else if (mobility.equals("MR"))
                {
                    //round rectangle
                    PathUtilties.addRoundedRect(mobilityPath, x, bottomY, width, rrHeight, wheelSize/2, rrHeight);

                    //left circle
                    PathUtilties.addEllipse(mobilityPath, x - wheelSize - wheelSize, bottomY, wheelSize, wheelSize);
                }
                else if (mobility.equals("MS"))
                {
                    //line
                    PathUtilties.addLine(mobilityPath, x + wheelSize, bottomY + (wheelSize / 2),
                            x + width - wheelSize, bottomY + (wheelSize / 2));

                    //left circle
                    PathUtilties.addEllipse(mobilityPath, x, bottomY, wheelSize, wheelSize);

                    //right circle
                    PathUtilties.addEllipse(mobilityPath, x + width - wheelSize, bottomY, wheelSize, wheelSize);
                }
                else if (mobility.equals("MT"))
                {

                    //line
                    PathUtilties.addLine(mobilityPath, x, bottomY, x + width, bottomY);

                    //left circle
                    PathUtilties.addEllipse(mobilityPath, x + wheelSize, bottomY + wheelOffset, wheelSize, wheelSize);

                    //left circle2
                    PathUtilties.addEllipse(mobilityPath, x, bottomY + wheelOffset, wheelSize, wheelSize);

                    //right circle
                    PathUtilties.addEllipse(mobilityPath, x + width - wheelSize, bottomY + wheelOffset, wheelSize, wheelSize);

                    //right circle2
                    PathUtilties.addEllipse(mobilityPath, x + width - wheelSize - wheelSize, bottomY + wheelOffset, wheelSize, wheelSize);

                }
                else if (mobility.equals("MU"))
                {
                	float halfWidth = (rrArcWidth * 0.5f);
                    mobilityPath.moveTo(x, bottomY);
                    mobilityPath.lineTo(x + halfWidth, bottomY + halfWidth);
                    mobilityPath.lineTo(x + width, bottomY + halfWidth);
                }
                else if (mobility.equals("MV"))
                {
                    mobilityPath.moveTo(x, bottomY);

                    mobilityPath.cubicTo(x, bottomY, x - rrHeight, bottomY + rrHeight/2, x, bottomY + rrHeight);
                    //mobilityPath.bezierCurveTo(x, bottomY, x-rrArcWidth, bottomY+3, x, bottomY+rrHeight);

                    mobilityPath.lineTo(x + width, bottomY + rrHeight);

                    mobilityPath.cubicTo(x + width, bottomY + rrHeight, x + width + rrHeight, bottomY + rrHeight/2, x + width, bottomY);
                    //shapeMobility.curveTo(x + width, bottomY + rrHeight, x+ width + rrArcWidth, bottomY+3, x + width, bottomY);
                }
                else if (mobility.equals("MW"))
                {
                    centerX = Math.round(RectUtilities.getCenterX(symbolBounds));
                    int angleWidth = rrHeight / 2;
                    mobilityPath.moveTo(centerX, bottomY + rrHeight + 2);
                    mobilityPath.lineTo(centerX - angleWidth, bottomY);
                    mobilityPath.lineTo(centerX - angleWidth*2, bottomY + rrHeight + 2);

                    mobilityPath.moveTo(centerX, bottomY + rrHeight + 2);
                    mobilityPath.lineTo(centerX + angleWidth, bottomY);
                    mobilityPath.lineTo(centerX + angleWidth*2, bottomY + rrHeight + 2);
                }
                else if (mobility.equals("MX"))
                {
                    centerX = Math.round(RectUtilities.getCenterX(symbolBounds));
                    PathUtilties.addLine(mobilityPath, x + width, bottomY, x, bottomY);
                    //var line = new SO.Line(x + width, bottomY,x, bottomY);

                    float quarterX = (centerX - x) / 2;
                    //var quarterY = (((bottomY + rrHeight) - bottomY)/2);

                    mobilityPath.moveTo(x, bottomY);
                    mobilityPath.cubicTo(x + quarterX, bottomY + rrHeight, centerX + quarterX, bottomY + rrHeight, x + width, bottomY);
                    //shapes.push(new SO.BCurve(x, bottomY,x+quarterX, bottomY+rrHeight, centerX + quarterX, bottomY + rrHeight, x + width, bottomY));
                }

                else if (mobility.equals("MY"))
                {
                    float incrementX = width / 7;
                    middleY = (bottomY + (rrHeight / 2));

                    x = Math.round(x + (incrementX / 2));
                    float r = Math.round(incrementX / 2);

                    //mobilityPath.arcTo(oval, sAngle, sAngle, moveTo);
                    PathUtilties.arc(mobilityPath, x, middleY, r, 180, 180);
                    PathUtilties.arc(mobilityPath, x + incrementX, middleY, r, 180, -180, false);
                    PathUtilties.arc(mobilityPath, x + incrementX * 2, middleY, r, 180, 180, false);
                    PathUtilties.arc(mobilityPath, x + incrementX * 3, middleY, r, 180, -180, false);
                    PathUtilties.arc(mobilityPath, x + incrementX * 4, middleY, r, 180, 180, false);
                    PathUtilties.arc(mobilityPath, x + incrementX * 5, middleY, r, 180, -180, false);
                    PathUtilties.arc(mobilityPath, x + incrementX * 6, middleY, r, 180, 180, false);

                }

            }
            //Draw Towed Array Sonar
            else if (symbolID.charAt(10) == ('N'))
            {

                int boxHeight = (int) ((rrHeight * 0.8f) + 0.5f);
                bottomY = y + height + (boxHeight / 7);
                mobilityPathFill = new Path();
                offsetY = boxHeight / 7;//1;
                centerX = Math.round(symbolBounds.left + (symbolBounds.right - symbolBounds.left) / 2);
                int squareOffset = Math.round(boxHeight * 0.5f);
                middleY = ((boxHeight / 2) + bottomY) + offsetY;//+1 for offset from symbol
                if (symbolID.substring(10, 12).equals("NS"))
                {
                    //subtract 0.5 becase lines 1 pixel thick get aliased into
                    //a line two pixels wide.
                    //line
                    PathUtilties.addLine(mobilityPath, centerX - 1, bottomY - 1, centerX - 1, bottomY + boxHeight + offsetY);
                    //shapes.push(new SO.Line(centerX-1,bottomY-1,centerX-1, bottomY + rrHeight + 3));
                    //shapeLines.append(new Line2D.Double(centerX,bottomY - 2,centerX, bottomY + rrHeight + 1), false);
                    //line
                    PathUtilties.addLine(mobilityPath, x, middleY, x + width, middleY);
                    //shapes.push(new SO.Line(x,middleY,x + width, middleY));
                    //shapeLines.append(new Line2D.Double(x,middleY,x + width, middleY), false);
                    //square
                    mobilityPathFill.addRect(PathUtilties.makeRectF(x - squareOffset, bottomY + offsetY, boxHeight, boxHeight), Direction.CW);
                    //shapes.push(new SO.Rectangle(x-squareOffset, bottomY+offsetY, 5, 5));
                    //shapeSquares.append(new Rectangle2D.Double(x-squareOffset, bottomY, 5, 5), false);
                    //square
                    mobilityPathFill.addRect(PathUtilties.makeRectF(Math.round(centerX - squareOffset), bottomY + offsetY, boxHeight, boxHeight), Direction.CW);
                    //shapes.push(new SO.Rectangle(Math.round(centerX-squareOffset), bottomY+offsetY, 5, 5));
                    //shapeSquares.append(new Rectangle2D.Double(centerX-squareOffset, bottomY, 5, 5), false);
                    //square
                    mobilityPathFill.addRect(PathUtilties.makeRectF(x + width - squareOffset, bottomY + offsetY, boxHeight, boxHeight), Direction.CW);
                    //shapes.push(new SO.Rectangle(x + width - squareOffset, bottomY+offsetY, 5, 5));
                    //shapeSquares.append(new Rectangle2D.Double(x + width - squareOffset, bottomY, 5, 5), false);
                }
                else if (symbolID.substring(10, 12).equals("NL"))
                {
                    int leftX = x + (centerX - x) / 2,
                            rightX = centerX + (x + width - centerX) / 2;

                    //line vertical left
                    PathUtilties.addLine(mobilityPath, leftX, bottomY - 1, leftX, bottomY + offsetY + boxHeight + offsetY);
                    //shapes.push(new SO.Line(leftX,bottomY - 1,leftX, bottomY + rrHeight + 3));
                    //shapeLines.append(new Line2D.Double(leftX,bottomY - 2,leftX, bottomY + rrHeight + 1), false);
                    //line vertical right
                    PathUtilties.addLine(mobilityPath, rightX, bottomY - 1, rightX, bottomY + offsetY + boxHeight + offsetY);
                    //shapes.push(new SO.Line(rightX,bottomY - 1,rightX, bottomY + rrHeight + 3));
                    //shapeLines.append(new Line2D.Double(rightX,bottomY - 2,rightX, bottomY + rrHeight + 1), false);
                    //line horizontal
                    PathUtilties.addLine(mobilityPath, x, middleY, x + width, middleY);
                    //shapes.push(new SO.Line(x,middleY,x + width, middleY));
                    //shapeLines.append(new Line2D.Double(x,middleY,x + width, middleY), false);
                    //square left
                    mobilityPathFill.addRect(PathUtilties.makeRectF(x - squareOffset, bottomY + offsetY, boxHeight, boxHeight), Direction.CW);
                    //shapes.push(new SO.Rectangle(x-squareOffset, bottomY+offsetY, 5, 5));
                    //shapeSquares.append(new Rectangle2D.Double(x-squareOffset, bottomY, 5, 5), false);
                    //square middle
                    mobilityPathFill.addRect(PathUtilties.makeRectF(centerX - squareOffset, bottomY + offsetY, boxHeight, boxHeight), Direction.CW);
                    //shapes.push(new SO.Rectangle(centerX-squareOffset, bottomY+offsetY, 5, 5));
                    //shapeSquares.append(new Rectangle2D.Double(centerX-squareOffset, bottomY, 5, 5), false);
                    //square right
                    mobilityPathFill.addRect(PathUtilties.makeRectF(x + width - squareOffset, bottomY + offsetY, boxHeight, boxHeight), Direction.CW);
                    //shapes.push(new SO.Rectangle(x + width - squareOffset, bottomY+offsetY, 5, 5));
                    //shapeSquares.append(new Rectangle2D.Double(x + width - squareOffset, bottomY, 5, 5), false);
                    //square middle left
                    mobilityPathFill.addRect(PathUtilties.makeRectF(leftX - squareOffset, bottomY + offsetY, boxHeight, boxHeight), Direction.CW);
                    //shapes.push(new SO.Rectangle(leftX - squareOffset, bottomY+offsetY, 5, 5));
                    //shapeSquares.append(new Rectangle2D.Double(leftX - squareOffset, bottomY, 5, 5), false);
                    //square middle right
                    mobilityPathFill.addRect(PathUtilties.makeRectF(rightX - squareOffset, bottomY + offsetY, boxHeight, boxHeight), Direction.CW);
                    //shapes.push(new SO.Rectangle(rightX - squareOffset, bottomY+offsetY, 5, 5));
                    //shapeSquares.append(new Rectangle2D.Double(rightX - squareOffset, bottomY, 5, 5), false);

                }
            }

            //get mobility bounds
            if (mobilityPath != null)
            {

                //build mobility bounds
                mobilityBounds = new RectF();
                mobilityPath.computeBounds(mobilityBounds, true);

                RectF mobilityFillBounds = new RectF();
                if (mobilityPathFill != null)
                {
                    mobilityPathFill.computeBounds(mobilityFillBounds, true);
                    mobilityBounds.union(mobilityFillBounds);
                }

                //grow by one because we use a line thickness of 2.
                mobilityBounds.set(mobilityBounds.left - 1, mobilityBounds.top - 1, mobilityBounds.right + 1, mobilityBounds.bottom + 1);
                imageBounds.union(RectUtilities.makeRectFromRectF(mobilityBounds));
            }
        }
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Build Echelon">
        //Draw Echelon
        String strEchelon = SymbolUtilities.getEchelon(symbolID);//symbolID.substring(11, 12);
        if (strEchelon != null)
        {
            strEchelon = SymbolUtilities.getEchelonText(strEchelon);
        }
        if (strEchelon != null && SymbolUtilities.hasInstallationModifier(symbolID) == false
                && SymbolUtilities.canUnitHaveModifier(symbolID, ModifiersUnits.B_ECHELON))
        {

            if (strEchelon != null)
            {
                int echelonOffset = 2,
                        outlineOffset = RS.getTextOutlineWidth();

                tiEchelon = new TextInfo(strEchelon, 0, 0, _modifierFont);
                echelonBounds = tiEchelon.getTextBounds();

                int y = Math.round(symbolBounds.left - echelonOffset);
                int x = Math.round(symbolBounds.left + (symbolBounds.width() / 2)
                        - (echelonBounds.width() / 2));
                tiEchelon.setLocation(x, y);

                //There will never be lowercase characters in an echelon so trim that fat.    
                //Remove the descent from the bounding box.
                tiEchelon.getTextOutlineBounds();//.shiftBR(0,Math.round(-(echelonBounds.height()*0.3)));                         

                //make echelon bounds a little more spacious for things like nearby labels and Task Force.
                RectUtilities.grow(echelonBounds, outlineOffset);
                //tiEchelon.getTextOutlineBounds();
//                RectUtilities.shift(echelonBounds, x, -outlineOffset);
                //echelonBounds.shift(0,-outlineOffset);// - Math.round(echelonOffset/2));
                tiEchelon.setLocation(x, y - outlineOffset);

                imageBounds.union(echelonBounds);
            }
        }
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Build Affiliation Modifier">
        //Draw Echelon
        String affiliationModifier = null;
        if (RS.getDrawAffiliationModifierAsLabel() == false)
        {
            affiliationModifier = SymbolUtilities.getUnitAffiliationModifier(symbolID, symStd);
        }
        if (affiliationModifier != null)
        {

            int amOffset = 2;
            int outlineOffset = RS.getTextOutlineWidth();

            tiAM = new TextInfo(affiliationModifier, 0, 0, _modifierFont);
            amBounds = tiAM.getTextBounds();

            int x, y;

            if (echelonBounds != null
                    && ((echelonBounds.left + echelonBounds.width() > symbolBounds.left + symbolBounds.width())))
            {
                y = Math.round(symbolBounds.top - amOffset);
                x = echelonBounds.left + echelonBounds.width();
            }
            else
            {
                y = Math.round(symbolBounds.top - amOffset);
                x = Math.round(symbolBounds.left + symbolBounds.width());
            }
            tiAM.setLocation(x, y);

            //adjust for outline.
            RectUtilities.grow(amBounds, outlineOffset);
            RectUtilities.shift(amBounds, 0, -outlineOffset);
            tiAM.setLocation(x, y - outlineOffset);

            imageBounds.union(amBounds);
        }
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Build Task Force">
        Rect tfBounds = null;
        Rect tfRectangle = null;
        if (SymbolUtilities.isTaskForce(symbolID) && SymbolUtilities.canUnitHaveModifier(symbolID, ModifiersUnits.D_TASK_FORCE_INDICATOR))
        {
            if (echelonBounds != null)
            {
                tfRectangle = new Rect(echelonBounds.left,
                        echelonBounds.top,// + outlineOffset,
                        echelonBounds.right,
                        symbolBounds.top-1);
                tfBounds = new Rect(tfRectangle);
            }
            else
            {
                int height = Math.round(symbolBounds.height() / 4);
                int width = Math.round(symbolBounds.width() / 3);

                tfRectangle = RectUtilities.makeRect((int) symbolBounds.left + width,
                        (int) symbolBounds.top - height,
                        width,
                        height);

                tfBounds = RectUtilities.makeRect(tfRectangle.left + -1,
                        tfRectangle.top - 1,
                        tfRectangle.width() + 2,
                        tfRectangle.height() + 2);

            }
            imageBounds.union(tfBounds);
        }
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Build Feint Dummy Indicator">
        Rect fdiBounds = null;
        Point fdiTop = null;
        Point fdiLeft = null;
        Point fdiRight = null;

        if ((SymbolUtilities.isFeintDummy(symbolID)
                || SymbolUtilities.isFeintDummyInstallation(symbolID))
                && SymbolUtilities.canUnitHaveModifier(symbolID, ModifiersUnits.AB_FEINT_DUMMY_INDICATOR))
        {
            //create feint indicator /\
            fdiLeft = new Point((int) symbolBounds.left, (int) symbolBounds.top);
            fdiRight = new Point((int) (symbolBounds.left + symbolBounds.width()), (int) symbolBounds.top);

            char affiliation = symbolID.charAt(1);
            if (affiliation == ('F')
                    || affiliation == ('A')
                    || affiliation == ('D')
                    || affiliation == ('M')
                    || affiliation == ('J')
                    || affiliation == ('K'))
            {
                fdiTop = new Point(Math.round(RectUtilities.getCenterX(symbolBounds)), Math.round(symbolBounds.top - (symbolBounds.height() * .75f)));
            }
            else
            {
                fdiTop = new Point(Math.round(RectUtilities.getCenterX(symbolBounds)), Math.round(symbolBounds.top - (symbolBounds.height() * .54f)));
            }

            fdiBounds = RectUtilities.makeRect(fdiLeft.x, fdiLeft.y, 1, 1);
            fdiBounds.union(fdiTop.x, fdiTop.y);
            fdiBounds.union(fdiRight.x, fdiRight.y);

            if (echelonBounds != null)
            {
                int shiftY = Math.round(symbolBounds.top - echelonBounds.height() - 2);
                fdiLeft.offset(0, shiftY);
                fdiTop.offset(0, shiftY);
                fdiRight.offset(0, shiftY);
                fdiBounds.offset(0, shiftY);
            }

            imageBounds.union(fdiBounds);

        }
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Build Installation">
        Rect instRectangle = null;
        Rect instBounds = null;
        if (SymbolUtilities.hasInstallationModifier(symbolID)
                && SymbolUtilities.canUnitHaveModifier(symbolID, ModifiersUnits.AC_INSTALLATION))
        {//the actual installation symbols have the modifier
            //built in.  everything else, we have to draw it.
            //
            ////get indicator dimensions////////////////////////////////
            int width;
            int height;
            char affiliation = SymbolUtilities.getAffiliation(symbolID);

            if (affiliation == 'F'
                    || affiliation == 'A'
                    || affiliation == 'D'
                    || affiliation == 'M'
                    || affiliation == 'J'
                    || affiliation == 'K')
            {
                //4th height, 3rd width
                height = Math.round(symbolBounds.height() / 4);
                width = Math.round(symbolBounds.width() / 3);
            }
            else if (affiliation == 'H' || affiliation == 'S')//hostile,suspect
            {
                //6th height, 3rd width
                height = Math.round(symbolBounds.height() / 6);
                width = Math.round(symbolBounds.width() / 3);
            }
            else if (affiliation == 'N' || affiliation == 'L')//neutral,exercise neutral
            {
                //6th height, 3rd width
                height = Math.round(symbolBounds.height() / 6);
                width = Math.round(symbolBounds.width() / 3);
            }
            else if (affiliation == 'P'
                    || affiliation == 'U'
                    || affiliation == 'G'
                    || affiliation == 'W')
            {
                //6th height, 3rd width
                height = Math.round(symbolBounds.height() / 6);
                width = Math.round(symbolBounds.width() / 3);
            }
            else
            {
                //6th height, 3rd width
                height = Math.round(symbolBounds.height() / 6);
                width = Math.round(symbolBounds.width() / 3);
            }

//                    if(width * 3 < symbolBounds.width())
//                        width++;
            //set installation position/////////////////////////////////
            //set position of indicator
            if (affiliation == 'F'
                    || affiliation == 'A'
                    || affiliation == 'D'
                    || affiliation == 'M'
                    || affiliation == 'J'
                    || affiliation == 'K'
                    || affiliation == 'N'
                    || affiliation == 'L')
            {
                instRectangle = RectUtilities.makeRect((int) (symbolBounds.left + width),
                        (int) (symbolBounds.top - height),
                        width,
                        height);
            }
            else if (affiliation == 'H' || affiliation == 'S')//hostile,suspect
            {
                instRectangle = RectUtilities.makeRect((int) symbolBounds.left + width,
                        Math.round((int) symbolBounds.top - (height * 0.15f)),
                        width,
                        height);
            }
            else if (affiliation == 'P'
                    || affiliation == 'U'
                    || affiliation == 'G'
                    || affiliation == 'W')
            {
                instRectangle = RectUtilities.makeRect((int) symbolBounds.left + width,
                        Math.round(symbolBounds.top - (height * 0.3f)),
                        width,
                        height);
            }
            else
            {
                instRectangle = RectUtilities.makeRect((int) symbolBounds.left + width,
                        Math.round(symbolBounds.top - (height * 0.3f)),
                        width,
                        height);
            }

            /*instRectangle = new SO.Rectangle(symbolBounds.left + width,
             symbolBounds.top - height,
             width,
             height);//*/
            //generate installation bounds//////////////////////////////
            instBounds = new Rect(instRectangle.left + -1,
                    instRectangle.top - 1,
                    instRectangle.width() + 2,
                    instRectangle.height() + 2);

            imageBounds.union(instBounds);

        }
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Build HQ Staff">
        Point pt1HQ = null;
        Point pt2HQ = null;
        Rect hqBounds = null;
        //Draw HQ Staff
        if (SymbolUtilities.isHQ(symbolID)
                && SymbolUtilities.canUnitHaveModifier(symbolID, ModifiersUnits.S_HQ_STAFF_OR_OFFSET_INDICATOR))
        {

            char affiliation = symbolID.charAt(1);
            //get points for the HQ staff
            if (affiliation == ('F')
                    || affiliation == ('A')
                    || affiliation == ('D')
                    || affiliation == ('M')
                    || affiliation == ('J')
                    || affiliation == ('K')
                    || affiliation == ('N')
                    || affiliation == ('L'))
            {
                pt1HQ = new Point((int) symbolBounds.left + 1,
                        (int) (symbolBounds.top + symbolBounds.height() - 1));
                pt2HQ = new Point((int) pt1HQ.x, (int) (pt1HQ.y + symbolBounds.height()));
            }
            else
            {
                pt1HQ = new Point((int) symbolBounds.left + 1,
                        (int) (symbolBounds.top + (symbolBounds.height() / 2)));
                pt2HQ = new Point((int) pt1HQ.x, (int) (pt1HQ.y + symbolBounds.height()));
            }

            //create bounding rectangle for HQ staff.
            hqBounds = new Rect(pt1HQ.x, pt1HQ.y, pt1HQ.x + 2, pt2HQ.y);
            //adjust the image bounds accordingly.
            imageBounds.union(hqBounds);
            //imageBounds.shiftBR(0,pt2HQ.y-imageBounds.bottom);
            //adjust symbol center
            centerPoint.set(pt2HQ.x, pt2HQ.y);
        }

        // </editor-fold>         
        // <editor-fold defaultstate="collapsed" desc="Build DOM Arrow">
        Point[] domPoints = null;
        Rect domBounds = null;
        if (modifiers.indexOfKey(ModifiersUnits.Q_DIRECTION_OF_MOVEMENT) >= 0
                && SymbolUtilities.canUnitHaveModifier(symbolID, ModifiersUnits.Q_DIRECTION_OF_MOVEMENT))
        {
        	String strQ = modifiers.get(ModifiersUnits.Q_DIRECTION_OF_MOVEMENT);
        	
        	if(strQ != null && SymbolUtilities.isNumber(strQ))
        	{
	            float q = Float.valueOf(strQ);
	
	            boolean isY = (modifiers.indexOfKey(ModifiersUnits.Y_LOCATION) >= 0);
	
	            domPoints = createDOMArrowPoints(symbolID, symbolBounds, symbolCenter, q, isY);
	
	            domBounds = new Rect(domPoints[0].x, domPoints[0].y, 1, 1);
	
	            Point temp = null;
	            for (int i = 1; i < 6; i++)
	            {
	                temp = domPoints[i];
	                if (temp != null)
	                {
	                    domBounds.union(temp.x, temp.y);
	                }
	            }
	            imageBounds.union(domBounds);
        	}
        }

        // </editor-fold>
        // <editor-fold defaultstate="collapsed" desc="Build Operational Condition Indicator">
        Rect ociBounds = null;
        RectF ociBoundsF = null;
        Rect ociShape = null;
        Path ociSlashShape = null;
        int ociOffset = 4;
        if (mobilityBounds != null)
        {
            ociOffset = Math.round(mobilityBounds.bottom - symbolBounds.bottom) + 4;
        }
        if(RendererSettings.getInstance().getOperationalConditionModifierType() == RendererSettings.OperationalConditionModifierType_BAR)
        {
            ociShape = processOperationalConditionIndicator(symbolID, symbolBounds, ociOffset);
            if (ociShape != null)
            {
                Rect temp = new Rect(ociShape);
                RectUtilities.grow(temp, 2);
                ociBounds = temp;
                imageBounds.union(ociBounds);
            }
        }
        else//slash
        {
            ociSlashShape = processOperationalConditionIndicatorSlash(symbolID, symbolBounds);
            if (ociSlashShape != null)
            {
                //build mobility bounds
                ociBoundsF = new RectF();
                ociBounds = new Rect();
                ociSlashShape.computeBounds(ociBoundsF, true);
                ociBoundsF.roundOut(ociBounds);
                imageBounds.union(ociBounds);
                ociBounds = null;
            }
        }


        // </editor-fold>
        // 
        // <editor-fold defaultstate="collapsed" desc="Shift Modifiers">
        //adjust points if necessary
        if (imageBounds.left < 0 || imageBounds.top < 0)
        {
            int shiftX = Math.abs(imageBounds.left);
            int shiftY = Math.abs(imageBounds.top);

            if (hqBounds != null)
            {
                pt1HQ.offset(shiftX, shiftY);
                pt2HQ.offset(shiftX, shiftY);
            }
            if (echelonBounds != null)
            {
                tiEchelon.setLocation(tiEchelon.getLocation().x + shiftX, tiEchelon.getLocation().y + shiftY);
            }
            if (amBounds != null)
            {
                tiAM.setLocation(tiAM.getLocation().x + shiftX, tiAM.getLocation().y + shiftY);
            }
            if (tfBounds != null)
            {
                tfRectangle.offset(shiftX, shiftY);
                tfBounds.offset(shiftX, shiftY);
            }
            if (instBounds != null)
            {
                instRectangle.offset(shiftX, shiftY);
                instBounds.offset(shiftX, shiftY);
            }
            if (fdiBounds != null)
            {
                fdiBounds.offset(shiftX, shiftY);
                fdiLeft.offset(shiftX, shiftY);
                fdiTop.offset(shiftX, shiftY);
                fdiRight.offset(shiftX, shiftY);
            }
            if (ociBounds != null)
            {
                ociBounds.offset(shiftX, shiftY);
                ociShape.offset(shiftX, shiftY);
            }
            if(ociBoundsF != null)
            {
                ociBoundsF.offset(shiftX, shiftY);
                ociSlashShape.offset(shiftX, shiftY);
            }
            if (domBounds != null)
            {
                for (int i = 0; i < 6; i++)
                {
                    Point temp = domPoints[i];
                    if (temp != null)
                    {
                        temp.offset(shiftX, shiftY);
                    }
                }
                domBounds.offset(shiftX, shiftY);
            }
            if (mobilityBounds != null)
            {
                //shift mobility points
                mobilityPath.offset(shiftX, shiftY);
                if (mobilityPathFill != null)
                {
                    mobilityPathFill.offset(shiftX, shiftY);
                }

                mobilityBounds.offset(shiftX, shiftY);
            }

            centerPoint.offset(shiftX, shiftY);
            symbolBounds.offset(shiftX, shiftY);
            imageBounds.offset(shiftX, shiftY);
        }
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Draw Modifiers">
        /*if(useBuffer.equalstrue)
         {
         buffer = _bufferDisplayModifiers;
         ctx = buffer.getContext('2d');
         ctx.clearRect(0,0,250,250);
         }
         else
         {
         buffer = this.createBuffer(imageBounds.getWidth(),imageBounds.getHeight());
         ctx = buffer.getContext('2d');
         //}*/
        Bitmap bmp = Bitmap.createBitmap(imageBounds.width(), imageBounds.height(), Config.ARGB_8888);
        Canvas ctx = new Canvas(bmp);

        if (echelonBounds != null || amBounds != null)
        {
            //   ctx.font = RendererSettings.getModifierFont();
        }

        //render////////////////////////////////////////////////////////
        Paint paint = new Paint();
        paint.setStyle(Style.STROKE);
        paint.setColor(modifierColor.toInt());
        if(alpha > -1)
            paint.setAlpha(alpha);
        paint.setStrokeWidth(2.0f);

        if (hqBounds != null)
        {
            ctx.drawLine(pt1HQ.x, pt1HQ.y, pt2HQ.x, pt2HQ.y, paint);
        }

        if (tfBounds != null)
        {
            ctx.drawRect(tfRectangle, paint);
        }

        if (instBounds != null)
        {
            paint.setStyle(Style.FILL);
            ctx.drawRect(instRectangle, paint);
        }

        if (echelonBounds != null)
        {
            TextInfo[] aTiEchelon =
            {
                tiEchelon
            };
            renderText(ctx, aTiEchelon, textColor, textBackgroundColor);

            echelonBounds = null;
            tiEchelon = null;
        }

        if (amBounds != null)
        {
            TextInfo[] aTiAM =
            {
                tiAM
            };
            renderText(ctx, aTiAM, textColor, textBackgroundColor);
            amBounds = null;
            tiAM = null;
        }

        if (fdiBounds != null)
        {

            Paint fdiPaint = new Paint();
            fdiPaint.setAntiAlias(true);
            fdiPaint.setColor(modifierColor.toInt());
            fdiPaint.setAlpha(255);
            fdiPaint.setStyle(Style.STROKE);
            fdiPaint.setPathEffect(new DashPathEffect(new float[]
            {
                6, 4
            }, 0));

            if (symbolBounds.width() > 19)
            {
                fdiPaint.setPathEffect(new DashPathEffect(new float[]
                {
                    6, 4
                }, 0));
            }
            else
            {
                fdiPaint.setPathEffect(new DashPathEffect(new float[]
                {
                    5, 3
                }, 0));
            }
            fdiPaint.setStrokeCap(Cap.BUTT);
            fdiPaint.setStrokeJoin(Join.MITER);
            fdiPaint.setStrokeWidth(2);

            Path fdiPath = new Path();

            fdiPath.moveTo(fdiLeft.x, fdiLeft.y);
            fdiPath.lineTo(fdiTop.x, fdiTop.y);
            fdiPath.lineTo(fdiRight.x, fdiRight.y);
            ctx.drawPath(fdiPath, fdiPaint);

            fdiBounds = null;

        }

        if (mobilityBounds != null)
        {
            Paint mobilityPaint = new Paint();
            mobilityPaint.setStyle(Style.STROKE);
            mobilityPaint.setColor(modifierColor.toInt());
            if(alpha > -1)
                mobilityPaint.setAlpha(alpha);
            
            //ctx.lineCap = "butt";
            //ctx.lineJoin = "miter";
            if (symbolID.charAt(10) == ('M'))
            {
                mobilityPaint.setStrokeWidth(3f);
                mobilityPaint.setAntiAlias(true);
            }
            else //NS or NL
            {
                mobilityPaint.setStrokeWidth(3f);
                //mobilityPaint.setAntiAlias(true);
            }

            ctx.drawPath(mobilityPath, mobilityPaint);

            if (mobilityPathFill != null)
            {
                mobilityPaint.setStyle(Style.FILL);
                ctx.drawPath(mobilityPathFill, mobilityPaint);
            }

            mobilityBounds = null;

        }

        if (ociBounds != null)
        {
            Paint ociPaint = new Paint();

            int statusColor = 0;
            char status = symbolID.charAt(3);
            if (status == ('C'))//Fully Capable
            {
                statusColor = Color.green.toInt();
            }
            else if (status == ('D'))//Damage
            {
                statusColor = Color.yellow.toInt();
            }
            else if (status == ('X'))
            {
                statusColor = Color.red.toInt();
            }
            else if (status == ('F'))//full to capacity(hospital)
            {
                statusColor = Color.blue.toInt();
            }

            ociPaint.setColor(Color.black.toInt());
            ociPaint.setStyle(Style.FILL);

            if(alpha > -1)
                ociPaint.setAlpha(alpha);
            ctx.drawRect(ociBounds, ociPaint);
            ociPaint.setColor(statusColor);
            if(alpha > -1)
                ociPaint.setAlpha(alpha);
            ctx.drawRect(ociShape, ociPaint);

            ociBounds = null;
            ociShape = null;
        }

        //draw original icon.        
        //ctx.drawImage(ii.getImage(),symbolBounds.left, symbolBounds.top);
        ctx.drawBitmap(ii.getImage(), null, symbolBounds, null);

        if (domBounds != null)
        {
            drawDOMArrow(ctx, domPoints, modifierColor, alpha);

            domBounds = null;
            domPoints = null;
        }

        if(ociBoundsF != null)
        {
            Paint ociPaint = new Paint();
            int size = symbolBounds.width();
            float ociStrokeWidth = 3f;

            ociStrokeWidth = size/20f;
            if(ociStrokeWidth < 1f)
                ociStrokeWidth = 1f;
            /*if(size > 50 && size < 100)
                ociStrokeWidth = 5f;
            else if(size >= 100 && size < 200)
                ociStrokeWidth = 7f;
            else if(size >= 200)
                ociStrokeWidth = 10f;*/
            ociPaint.setColor(modifierColor.toInt());
            ociPaint.setStrokeWidth(ociStrokeWidth);
            ociPaint.setStrokeCap(Cap.BUTT);
            ociPaint.setStyle(Style.STROKE);
            ociPaint.setAntiAlias(true);
            ctx.drawPath(ociSlashShape,ociPaint);

            ociBoundsF = null;
            ociSlashShape = null;
        }

        // </editor-fold>
        newii = new ImageInfo(bmp, centerPoint, symbolBounds);

        // <editor-fold defaultstate="collapsed" desc="Cleanup">
        // </editor-fold>
        //return newii;    
        if (newii != null)
        {
            return newii;
        }
        else
        {
            return null;
        }

    }
    
    private static double getYPositionForSCC(String symbolID)
    {
        double yPosition = 0.32;
        String temp = symbolID.substring(4, 10);
        char affiliation = symbolID.charAt(1);

        if(temp.equals("WMGC--"))//GROUND (BOTTOM) MILCO
        {
            if(affiliation == 'H' || 
                    affiliation == 'S')//suspect
                yPosition = 0.29;
            else if(affiliation == 'N' ||
                    affiliation == 'L')//exercise neutral
                yPosition = 0.32;
            else if(affiliation == 'F' ||
                    affiliation == 'A' ||//assumed friend
                    affiliation == 'D' ||//exercise friend
                    affiliation == 'M' ||//exercise assumed friend
                    affiliation == 'K' ||//faker
                    affiliation == 'J')//joker
                yPosition = 0.32;
            else
                yPosition = 0.34;
        }
        else if(temp.equals("WMMC--"))//MOORED MILCO
        {
            if(affiliation == 'H' || 
                    affiliation == 'S')//suspect
                yPosition = 0.25;
            else if(affiliation == 'N' ||
                    affiliation == 'L')//exercise neutral
                yPosition = 0.25;
            else if(affiliation == 'F' ||
                    affiliation == 'A' ||//assumed friend
                    affiliation == 'D' ||//exercise friend
                    affiliation == 'M' ||//exercise assumed friend
                    affiliation == 'K' ||//faker
                    affiliation == 'J')//joker
                yPosition = 0.25;
            else
                yPosition = 0.28;
        }
        else if(temp.equals("WMFC--"))//FLOATING MILCO
        {
            if(affiliation == 'H' || 
                    affiliation == 'S')//suspect
                yPosition = 0.29;
            else if(affiliation == 'N' ||
                    affiliation == 'L')//exercise neutral
                yPosition = 0.32;
            else if(affiliation == 'F' ||
                    affiliation == 'A' ||//assumed friend
                    affiliation == 'D' ||//exercise friend
                    affiliation == 'M' ||//exercise assumed friend
                    affiliation == 'K' ||//faker
                    affiliation == 'J')//joker
                yPosition = 0.32;
            else
                yPosition = 0.34;
        }
        else if(temp.equals("WMC---"))//GENERAL MILCO
        {
            if(affiliation == 'H' || 
                    affiliation == 'S')//suspect
                yPosition = 0.33;
            else if(affiliation == 'N' ||
                    affiliation == 'L')//exercise neutral
                yPosition = 0.36;
            else if(affiliation == 'F' ||
                    affiliation == 'A' ||//assumed friend
                    affiliation == 'D' ||//exercise friend
                    affiliation == 'M' ||//exercise assumed friend
                    affiliation == 'K' ||//faker
                    affiliation == 'J')//joker
                yPosition = 0.36;
            else
                yPosition = 0.36;
        }
        
        return yPosition;
    }

    /**
     *
     * @param {type} symbolID
     * @param {type} bounds symbolBounds SO.Rectangle
     * @param {type} center SO.Point Location where symbol is centered.
     * @param {type} angle in degrees
     * @param {Boolean} isY Boolean.
     * @returns {Array} of SO.Point. First 3 items are the line. Last three are
     * the arrowhead.
     */
    private static Point[] createDOMArrowPoints(String symbolID, Rect bounds, Point center, float angle, boolean isY)
    {
        Point[] arrowPoints = new Point[6];
        Point pt1 = null;
        Point pt2 = null;
        Point pt3 = null;

        int length = 40;
        if (SymbolUtilities.isNBC(symbolID))
        {
            length = Math.round(bounds.height() / 2);
        }
        else
        {
            length = bounds.height();
        }

        //get endpoint
        int dx2, dy2,
                x1, y1,
                x2, y2;

        x1 = Math.round(center.x);
        y1 = Math.round(center.y);

        pt1 = new Point(x1, y1);
        char scheme = symbolID.charAt(0);
        if (SymbolUtilities.isNBC(symbolID)
                || (scheme == 'S' && symbolID.charAt(2) == ('G')) ||
                scheme == 'O' || scheme == 'E')
        {
            y1 = bounds.top + bounds.height();
            pt1 = new Point(x1, y1);

            if (isY == true && SymbolUtilities.isNBC(symbolID))//make room for y modifier
            {
                int yModifierOffset = (int) _modifierFontHeight;

                yModifierOffset += RS.getTextOutlineWidth();

                pt1.offset(0, yModifierOffset);
            }

            y1 = y1 + length;
            pt2 = new Point(x1, y1);
        }

	    //get endpoint given start point and an angle
        //x2 = x1 + (length * Math.cos(radians)));
        //y2 = y1 + (length * Math.sin(radians)));
        angle = angle - 90;//in java, east is zero, we want north to be zero
        double radians = 0;
        radians = (angle * (Math.PI / 180));//convert degrees to radians

        dx2 = x1 + (int) (length * Math.cos(radians));
        dy2 = y1 + (int) (length * Math.sin(radians));
        x2 = Math.round(dx2);
        y2 = Math.round(dy2);

        //create arrowhead//////////////////////////////////////////////////////
        float arrowWidth = 16.0f,//8.0f,//6.5f;//7.0f;//6.5f;//10.0f//default
                theta = 0.423f;//higher value == shorter arrow head//*/

        if (length < 50)
        {
            theta = 0.55f;
        }
        /*float arrowWidth = length * .09f,// 16.0f,//8.0f,//6.5f;//7.0f;//6.5f;//10.0f//default
         theta = length * .0025f;//0.423f;//higher value == shorter arrow head
         if(arrowWidth < 8)
         arrowWidth = 8f;//*/

        int[] xPoints = new int[3];//3
        int[] yPoints = new int[3];//3
        int[] vecLine = new int[2];//2
        int[] vecLeft = new int[2];//2
        double fLength;
        double th;
        double ta;
        double baseX, baseY;

        xPoints[0] = x2;
        yPoints[0] = y2;

        //build the line vector
        vecLine[0] = (xPoints[0] - x1);
        vecLine[1] = (yPoints[0] - y1);

        //build the arrow base vector - normal to the line
        vecLeft[0] = -vecLine[1];
        vecLeft[1] = vecLine[0];

        //setup length parameters
        fLength = Math.sqrt(vecLine[0] * vecLine[0] + vecLine[1] * vecLine[1]);
        th = arrowWidth / (2.0 * fLength);
        ta = arrowWidth / (2.0 * (Math.tan(theta) / 2.0) * fLength);

        //find base of the arrow
        baseX = (xPoints[0] - ta * vecLine[0]);
        baseY = (yPoints[0] - ta * vecLine[1]);

        //build the points on the sides of the arrow
        xPoints[1] = (int) Math.round(baseX + th * vecLeft[0]);
        yPoints[1] = (int) Math.round(baseY + th * vecLeft[1]);
        xPoints[2] = (int) Math.round(baseX - th * vecLeft[0]);
        yPoints[2] = (int) Math.round(baseY - th * vecLeft[1]);

        //line.lineTo((int)baseX, (int)baseY);
        pt3 = new Point((int) Math.round(baseX), (int) Math.round(baseY));

        //arrowHead = new Polygon(xPoints, yPoints, 3);
        arrowPoints[0] = pt1;
        arrowPoints[1] = pt2;
        arrowPoints[2] = pt3;
        arrowPoints[3] = new Point(xPoints[0], yPoints[0]);
        arrowPoints[4] = new Point(xPoints[1], yPoints[1]);
        arrowPoints[5] = new Point(xPoints[2], yPoints[2]);

        return arrowPoints;

    }

    private static void drawDOMArrow(Canvas ctx, Point[] domPoints, Color modifierColor, int alpha)
    {
        Paint domPaint = new Paint();
        domPaint.setStrokeCap(Cap.BUTT);
        domPaint.setStrokeJoin(Join.MITER);
        domPaint.setStrokeWidth(3);
        domPaint.setColor(modifierColor.toInt());
        domPaint.setStyle(Style.STROKE);
        if(alpha > -1)
            domPaint.setAlpha(alpha);

        Path domPath = new Path();
        domPath.moveTo(domPoints[0].x, domPoints[0].y);
        if (domPoints[1] != null)
        {
            domPath.lineTo(domPoints[1].x, domPoints[1].y);
        }
        if (domPoints[2] != null)
        {
            domPath.lineTo(domPoints[2].x, domPoints[2].y);
        }
        ctx.drawPath(domPath, domPaint);

        domPath.reset();
        domPaint.setStyle(Style.FILL);
        domPath.moveTo(domPoints[3].x, domPoints[3].y);
        domPath.lineTo(domPoints[4].x, domPoints[4].y);
        domPath.lineTo(domPoints[5].x, domPoints[5].y);
        ctx.drawPath(domPath, domPaint);
    }

    private static Rect processOperationalConditionIndicator(String symbolID, Rect symbolBounds, int offsetY)
    {
        //create Operational Condition Indicator
        //set color
        Rect bar = null;
        char status;
        Color statusColor;
        int barSize = 0;
        int pixelSize = symbolBounds.height();

        status = symbolID.charAt(3);

        if (status == 'C' || status == 'D' || status == 'X' || status == 'F')
        {
            if (pixelSize > 0)
            {
                barSize = Math.round(pixelSize / 5);
            }

            if (barSize < 2)
            {
                barSize = 2;
            }

            offsetY += Math.round(symbolBounds.top + symbolBounds.height());

            bar = RectUtilities.makeRect(symbolBounds.left + 2, offsetY, Math.round(symbolBounds.width()) - 4, barSize);
        }

        return bar;
    }

    private static Path processOperationalConditionIndicatorSlash(String symbolID, Rect symbolBounds)
    {
        //create Operational Condition Indicator
        Path path = null;
        char status;
        status = symbolID.charAt(3);

        if (status == 'D' || status == 'X')
        {
            int fillCode = UnitFontLookup.getFillCode(symbolID,RendererSettings.Symbology_2525C);
            float widthRatio = UnitFontLookup.getUnitRatioWidth(fillCode);
            float heightRatio = UnitFontLookup.getUnitRatioHeight(fillCode);

            float slashHeight = (symbolBounds.height() / heightRatio * 1.47f);
            float slashWidth = (symbolBounds.width() / widthRatio * 0.85f);
            float centerX = symbolBounds.exactCenterX();
            float centerY = symbolBounds.exactCenterY();
            path = new Path();
            if(status == 'D')//Damaged /
            {
                path.moveTo(centerX - (slashWidth/2),centerY+(slashHeight/2));
                path.lineTo(centerX + (slashWidth/2),centerY-(slashHeight/2));
            }
            else if(status == 'X')//Destroyed X
            {
                path.moveTo(centerX - (slashWidth/2),centerY+(slashHeight/2));
                path.lineTo(centerX + (slashWidth/2),centerY-(slashHeight/2));
                path.moveTo(centerX - (slashWidth/2),centerY-(slashHeight/2));
                path.lineTo(centerX + (slashWidth/2),centerY+(slashHeight/2));
            }
            return path;

        }

        return path;
    }

    public static ImageInfo processUnitTextModifiers(ImageInfo ii, String symbolID, SparseArray<String> modifiers, SparseArray<String> attributes)
    {

        int bufferXL = 5;
        int bufferXR = 5;
        int bufferY = 2;
        int bufferText = 2;
        int x = 0;
        int y = 0;//best y
        int cpofNameX = 0;
        ImageInfo newii = null;
        int alpha = -1;
        
        Color textColor = Color.BLACK;
        Color textBackgroundColor = null;

        ArrayList<TextInfo> tiArray = new ArrayList<TextInfo>(modifiers.size());

        int descent = (int) (_modifierFontDescent + 0.5);

        int symStd = RS.getSymbologyStandard();
        if (attributes.indexOfKey(MilStdAttributes.SymbologyStandard) >= 0)
        {
            symStd = Integer.parseInt(attributes.get(MilStdAttributes.SymbologyStandard));
        }
        if (attributes.indexOfKey(MilStdAttributes.Alpha) >= 0)
        {
            alpha = Integer.parseInt(attributes.get(MilStdAttributes.Alpha));
        }

        Rect labelBounds = null;
        int labelWidth, labelHeight;

        Rect bounds = new Rect(ii.getSymbolBounds());
        Rect symbolBounds = new Rect(ii.getSymbolBounds());
        Point centerPoint = new Point(ii.getCenterPoint());
        Rect imageBounds = new Rect(ii.getImageBounds());
        Rect imageBoundsOld = new Rect(ii.getImageBounds());

        String echelon = SymbolUtilities.getEchelon(symbolID);
        String echelonText = SymbolUtilities.getEchelonText(echelon);
        String amText = SymbolUtilities.getUnitAffiliationModifier(symbolID, symStd);

        //make room for echelon & mobility.
        if (modifiers.indexOfKey(ModifiersUnits.Q_DIRECTION_OF_MOVEMENT) < 0 || SymbolUtilities.canUnitHaveModifier(symbolID, ModifiersUnits.Q_DIRECTION_OF_MOVEMENT)==false)
        {
            //if no DOM, we can just use the image bounds
            bounds = RectUtilities.makeRect(imageBounds.left, symbolBounds.top,
                    imageBounds.width(), symbolBounds.height());
        }
        else //dom exists so we need to change our math
        {
            if (echelonText != null || amText != null)
            {
                bounds = RectUtilities.makeRect(imageBounds.left, bounds.top,
                        imageBounds.width(), bounds.height());
            }
            else if (symbolID.substring(10, 12).equals("MR"))
            {
                x = -(Math.round((symbolBounds.width() - 1) / 7) * 2);
                if (x < bounds.left)
                {
                    bounds.set(x, 0, bounds.right, bounds.bottom);
                    //bounds.shiftTL(x,0);
                }
            }
        }

        cpofNameX = bounds.left + bounds.width() + bufferXR;

        //check if text is too tall:
        boolean byLabelHeight = false;
        labelHeight = (int) (_modifierFontHeight + 0.5);/* RendererUtilities.measureTextHeight(RendererSettings.getModifierFontName(),
         RendererSettings.getModifierFontSize(),
         RendererSettings.getModifierFontStyle()).fullHeight;*/

        int maxHeight = (bounds.height());
        if ((labelHeight * 3) > maxHeight)
        {
            byLabelHeight = true;
        }

        //Affiliation Modifier being drawn as a display modifier
        String affiliationModifier = null;
        if (RS.getDrawAffiliationModifierAsLabel() == true)
        {
            affiliationModifier = SymbolUtilities.getUnitAffiliationModifier(symbolID, symStd);
        }
        if (affiliationModifier != null)
        {   //Set affiliation modifier
            modifiers.put(ModifiersUnits.E_FRAME_SHAPE_MODIFIER, affiliationModifier);
            //modifiers[ModifiersUnits.E_FRAME_SHAPE_MODIFIER] = affiliationModifier;
        }//*/

        //Check for Valid Country Code
        if (SymbolUtilities.hasValidCountryCode(symbolID))
        {
            modifiers.put(ModifiersUnits.CC_COUNTRY_CODE, symbolID.substring(12, 14));
            //modifiers[ModifiersUnits.CC_COUNTRY_CODE] = symbolID.substring(12,14);
        }

        //            int y0 = 0;//W            E/F
        //            int y1 = 0;//X/Y          G
        //            int y2 = 0;//V/AD/AE      H/AF
        //            int y3 = 0;//T            M CC
        //            int y4 = 0;//Z            J/K/L/N/P
        //
        //            y0 = bounds.y - 0;
        //            y1 = bounds.y - labelHeight;
        //            y2 = bounds.y - (labelHeight + (int)bufferText) * 2;
        //            y3 = bounds.y - (labelHeight + (int)bufferText) * 3;
        //            y4 = bounds.y - (labelHeight + (int)bufferText) * 4;
        // <editor-fold defaultstate="collapsed" desc="Build Modifiers">
        String modifierValue = null;
        TextInfo tiTemp = null;
        //if(ModifiersUnits.C_QUANTITY in modifiers 
        if (modifiers.indexOfKey(ModifiersUnits.C_QUANTITY) >= 0
                && SymbolUtilities.canUnitHaveModifier(symbolID, ModifiersUnits.C_QUANTITY))
        {
            String text = modifiers.get(ModifiersUnits.C_QUANTITY);
            if(text != null)
            {
	            //bounds = armyc2.c2sd.renderer.utilities.RendererUtilities.getTextOutlineBounds(_modifierFont, text, new SO.Point(0,0));
	            tiTemp = new TextInfo(text, 0, 0, _modifierFont);
	            labelBounds = tiTemp.getTextBounds();
	            labelWidth = labelBounds.width();
	            x = Math.round((symbolBounds.left + (symbolBounds.width() * 0.5f)) - (labelWidth * 0.5f));
	            y = Math.round(symbolBounds.top - bufferY - descent);
	            tiTemp.setLocation(x, y);
	            tiArray.add(tiTemp);
            }
        }

        //if(ModifiersUnits.X_ALTITUDE_DEPTH in modifiers || ModifiersUnits.Y_LOCATION in modifiers)
        if (modifiers.indexOfKey(ModifiersUnits.X_ALTITUDE_DEPTH) >= 0 || modifiers.indexOfKey(ModifiersUnits.Y_LOCATION) >= 0)
        {
            modifierValue = null;

            String xm = null,
                    ym = null;

            if (modifiers.indexOfKey(ModifiersUnits.X_ALTITUDE_DEPTH) >= 0 && SymbolUtilities.canUnitHaveModifier(symbolID, ModifiersUnits.X_ALTITUDE_DEPTH))
            {
                xm = modifiers.get(ModifiersUnits.X_ALTITUDE_DEPTH);// xm = modifiers.X;
            }
            if (modifiers.indexOfKey(ModifiersUnits.Y_LOCATION) >= 0)
            {
                ym = modifiers.get(ModifiersUnits.Y_LOCATION);// ym = modifiers.Y;
            }
            if (xm == null && ym != null)
            {
                modifierValue = ym;
            }
            else if (xm != null && ym == null)
            {
                modifierValue = xm;
            }
            else if (xm != null && ym != null)
            {
                modifierValue = xm + "  " + ym;
            }

            if(modifierValue != null)
            {
	            tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont);
	            labelBounds = tiTemp.getTextBounds();
	            labelWidth = labelBounds.width();
	
	            if (!byLabelHeight)
	            {
	                x = bounds.left - labelBounds.width() - bufferXL;
	                y = bounds.top + labelHeight - descent;
	            }
	            else
	            {
	                x = bounds.left - labelBounds.width() - bufferXL;
	
	                y = (bounds.height());
	                y = (int) ((y * 0.5) + (labelHeight * 0.5));
	
	                y = y - ((labelHeight + bufferText));
	                y = bounds.top + y;
	            }
	
	            tiTemp.setLocation(x, y);
	            tiArray.add(tiTemp);
            }
        }

        if (modifiers.indexOfKey(ModifiersUnits.G_STAFF_COMMENTS) >= 0 && SymbolUtilities.canUnitHaveModifier(symbolID, ModifiersUnits.G_STAFF_COMMENTS))
        {
            modifierValue = modifiers.get(ModifiersUnits.G_STAFF_COMMENTS);

            if(modifierValue != null)
            {
	            tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont);
	            labelBounds = tiTemp.getTextBounds();
	            labelWidth = labelBounds.width();
	
	            x = bounds.left + bounds.width() + bufferXR;
	            if (!byLabelHeight)
	            {
	                y = bounds.top + labelHeight - descent;
	            }
	            else
	            {
	                y = (bounds.height());
	                y = (int) ((y * 0.5) + (labelHeight * 0.5));
	
	                y = y - ((labelHeight + bufferText));
	                y = bounds.top + y;
	            }
	
	            tiTemp.setLocation(x, y);
	            tiArray.add(tiTemp);
	
	            //Concession for cpof name label
	            if ((x + labelWidth + 3) > cpofNameX)
	            {
	                cpofNameX = x + labelWidth + 3;
	            }
            }
        }

        if ((modifiers.indexOfKey(ModifiersUnits.V_EQUIP_TYPE) >= 0) ||
                (modifiers.indexOfKey(ModifiersUnits.AD_PLATFORM_TYPE) >= 0) ||
                (modifiers.indexOfKey(ModifiersUnits.AE_EQUIPMENT_TEARDOWN_TIME) >= 0))
        {
            String vm = null,
                    adm = null,
                    aem = null;

            if (modifiers.indexOfKey(ModifiersUnits.V_EQUIP_TYPE) >= 0 && SymbolUtilities.canUnitHaveModifier(symbolID, ModifiersUnits.V_EQUIP_TYPE))
            {
                vm = modifiers.get(ModifiersUnits.V_EQUIP_TYPE);
            }
            if (modifiers.indexOfKey(ModifiersUnits.AD_PLATFORM_TYPE) >= 0 && SymbolUtilities.canUnitHaveModifier(symbolID, ModifiersUnits.AD_PLATFORM_TYPE))
            {
                adm = modifiers.get(ModifiersUnits.AD_PLATFORM_TYPE);
            }
            if (modifiers.indexOfKey(ModifiersUnits.AE_EQUIPMENT_TEARDOWN_TIME) >= 0 && SymbolUtilities.canUnitHaveModifier(symbolID, ModifiersUnits.AE_EQUIPMENT_TEARDOWN_TIME))
            {
                aem = modifiers.get(ModifiersUnits.AE_EQUIPMENT_TEARDOWN_TIME);
            }

            modifierValue = "";
            if(vm != null && vm.equals("") == false)
                modifierValue = vm;
            if(adm != null && adm.equals("") == false)
                modifierValue += " " + adm;
            if(aem != null && aem.equals("") == false)
                modifierValue += " " + aem;

            if(modifierValue != null)
                modifierValue = modifierValue.trim();
            if(modifierValue != null && modifierValue.equals("") == false)
            {
	            tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont);
	            labelBounds = tiTemp.getTextBounds();
	            labelWidth = labelBounds.width();
	
	            x = bounds.left - labelBounds.width() - bufferXL;
	
	            y = (bounds.height());
	            y = (int) ((y * 0.5f) + ((labelHeight - descent) * 0.5f));
	            y = bounds.top + y;
	
	            tiTemp.setLocation(x, y);
	            tiArray.add(tiTemp);
            }
        }

        if (modifiers.indexOfKey(ModifiersUnits.H_ADDITIONAL_INFO_1) >= 0 || modifiers.indexOfKey(ModifiersUnits.AF_COMMON_IDENTIFIER) >= 0)
        {
            modifierValue = "";
            String hm = "",
                    afm = "";

            hm = modifiers.get(ModifiersUnits.H_ADDITIONAL_INFO_1);
            if (modifiers.indexOfKey(ModifiersUnits.H_ADDITIONAL_INFO_1) >= 0)
            {
                hm = modifiers.get(ModifiersUnits.H_ADDITIONAL_INFO_1);
            }
            if (modifiers.indexOfKey(ModifiersUnits.AF_COMMON_IDENTIFIER) >= 0 && SymbolUtilities.canUnitHaveModifier(symbolID, ModifiersUnits.AF_COMMON_IDENTIFIER))
            {
                afm = modifiers.get(ModifiersUnits.AF_COMMON_IDENTIFIER);
            }

            modifierValue = hm + " " + afm;
            modifierValue = modifierValue.trim();

            if(modifierValue != null && modifierValue.equals("") == false)
            {
	            tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont);
	            labelBounds = tiTemp.getTextBounds();
	            labelWidth = labelBounds.width();
	
	            x = bounds.left + bounds.width() + bufferXR;
	
	            y = (bounds.height());
	            y = (int) ((y * 0.5) + ((labelHeight - descent) * 0.5));
	            y = bounds.top + y;
	
	            tiTemp.setLocation(x, y);
	            tiArray.add(tiTemp);
	
	            //Concession for cpof name label
	            if ((x + labelWidth + 3) > cpofNameX)
	            {
	                cpofNameX = x + labelWidth + 3;
	            }
            }
        }

        if (modifiers.indexOfKey(ModifiersUnits.T_UNIQUE_DESIGNATION_1) >= 0)
        {
            modifierValue = modifiers.get(ModifiersUnits.T_UNIQUE_DESIGNATION_1);

            if(modifierValue != null)
            {
	            tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont);
	            labelBounds = tiTemp.getTextBounds();
	            labelWidth = labelBounds.width();
	
	            if (!byLabelHeight)
	            {
	                x = bounds.left - labelWidth - bufferXL;
	                y = bounds.top + bounds.height();
	            }
	            else
	            {
	                x = bounds.left - labelWidth - bufferXL;
	
	                y = (bounds.height());
	                y = (int) ((y * 0.5) + (labelHeight * 0.5));
	
	                y = y + ((labelHeight + bufferText) - descent);
	                y = bounds.top + y;
	            }
	
	            tiTemp.setLocation(x, y);
	            tiArray.add(tiTemp);
            }
        }

        if ((modifiers.indexOfKey(ModifiersUnits.M_HIGHER_FORMATION) >= 0 && SymbolUtilities.canUnitHaveModifier(symbolID, ModifiersUnits.M_HIGHER_FORMATION)) || modifiers.indexOfKey(ModifiersUnits.CC_COUNTRY_CODE) >= 0)
        {
            modifierValue = "";

            if (modifiers.indexOfKey(ModifiersUnits.M_HIGHER_FORMATION) >= 0)
            {
                modifierValue += modifiers.get(ModifiersUnits.M_HIGHER_FORMATION);
            }
            if (modifiers.indexOfKey(ModifiersUnits.CC_COUNTRY_CODE) >= 0)
            {
                if (modifierValue.length() > 0)
                {
                    modifierValue += " ";
                }
                modifierValue += modifiers.get(ModifiersUnits.CC_COUNTRY_CODE);
            }

            tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont);
            labelBounds = tiTemp.getTextBounds();
            labelWidth = labelBounds.width();

            x = bounds.left + bounds.width() + bufferXR;
            if (!byLabelHeight)
            {
                y = bounds.top + bounds.height();
            }
            else
            {
                y = (bounds.height());
                y = (int) ((y * 0.5) + (labelHeight * 0.5));

                y = y + ((labelHeight + bufferText - descent));
                y = bounds.top + y;
            }

            tiTemp.setLocation(x, y);
            tiArray.add(tiTemp);

            //Concession for cpof name label
            if ((x + labelWidth + 3) > cpofNameX)
            {
                cpofNameX = x + labelWidth + 3;
            }
        }

        if (modifiers.indexOfKey(ModifiersUnits.Z_SPEED) >= 0 && SymbolUtilities.canUnitHaveModifier(symbolID, ModifiersUnits.Z_SPEED))
        {
            modifierValue = modifiers.get(ModifiersUnits.Z_SPEED);

            if(modifierValue != null)
            {
	            tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont);
	            labelBounds = tiTemp.getTextBounds();
	            labelWidth = labelBounds.width();
	
	            x = bounds.left - labelWidth - bufferXL;
	            if (!byLabelHeight)
	            {
	                y = Math.round(bounds.top + bounds.height() + labelHeight + bufferText);
	            }
	            else
	            {
	                y = (bounds.height());
	                y = (int) ((y * 0.5) + (labelHeight * 0.5));
	
	                y = y + ((labelHeight + bufferText) * 2) - (descent * 2);
	                y = Math.round(bounds.top + y);
	            }
	
	            tiTemp.setLocation(x, y);
	            tiArray.add(tiTemp);
            }
        }

        if (modifiers.indexOfKey(ModifiersUnits.J_EVALUATION_RATING) >= 0
                || modifiers.indexOfKey(ModifiersUnits.K_COMBAT_EFFECTIVENESS) >= 0//
                || modifiers.indexOfKey(ModifiersUnits.L_SIGNATURE_EQUIP) >= 0//
                || modifiers.indexOfKey(ModifiersUnits.N_HOSTILE) >= 0//
                || modifiers.indexOfKey(ModifiersUnits.P_IFF_SIF) >= 0)//
        {
            modifierValue = null;

            String jm = null,
                    km = null,
                    lm = null,
                    nm = null,
                    pm = null;

            if (modifiers.indexOfKey(ModifiersUnits.J_EVALUATION_RATING) >= 0)
            {
                jm = modifiers.get(ModifiersUnits.J_EVALUATION_RATING);
            }
            if (modifiers.indexOfKey(ModifiersUnits.K_COMBAT_EFFECTIVENESS) >= 0 && SymbolUtilities.canUnitHaveModifier(symbolID, ModifiersUnits.K_COMBAT_EFFECTIVENESS))
            {
                km = modifiers.get(ModifiersUnits.K_COMBAT_EFFECTIVENESS);
            }
            if (modifiers.indexOfKey(ModifiersUnits.L_SIGNATURE_EQUIP) >= 0 && SymbolUtilities.canUnitHaveModifier(symbolID, ModifiersUnits.L_SIGNATURE_EQUIP))
            {
                lm = modifiers.get(ModifiersUnits.L_SIGNATURE_EQUIP);
            }
            if (modifiers.indexOfKey(ModifiersUnits.N_HOSTILE) >= 0 && SymbolUtilities.canUnitHaveModifier(symbolID, ModifiersUnits.N_HOSTILE))
            {
                nm = modifiers.get(ModifiersUnits.N_HOSTILE);
            }
            if (modifiers.indexOfKey(ModifiersUnits.P_IFF_SIF) >= 0 && SymbolUtilities.canUnitHaveModifier(symbolID, ModifiersUnits.P_IFF_SIF))
            {
                pm = modifiers.get(ModifiersUnits.P_IFF_SIF);
            }

            modifierValue = "";
            if (jm != null && jm.equals("") == false)
            {
                modifierValue = modifierValue + jm;
            }
            if (km != null && km.equals("") == false)
            {
                modifierValue = modifierValue + " " + km;
            }
            if (lm != null && lm.equals("") == false)
            {
                modifierValue = modifierValue + " " + lm;
            }
            if (nm != null && nm.equals("") == false)
            {
                modifierValue = modifierValue + " " + nm;
            }
            if (pm != null && pm.equals("") == false)
            {
                modifierValue = modifierValue + " " + pm;
            }

            if (modifierValue.length() > 2 && modifierValue.charAt(0) == ' ')
            {
                modifierValue = modifierValue.substring(1);
            }

            tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont);
            labelBounds = tiTemp.getTextBounds();
            labelWidth = labelBounds.width();

            x = bounds.left + bounds.width() + bufferXR;
            if (!byLabelHeight)
            {
                y = Math.round(bounds.top + bounds.height() + labelHeight + bufferText);
            }
            else
            {
                y = (bounds.height());
                y = (int) ((y * 0.5) + (labelHeight * 0.5));

                y = y + ((labelHeight + bufferText) * 2) - (descent * 2);
                y = Math.round(bounds.top + y);
            }

            tiTemp.setLocation(x, y);
            tiArray.add(tiTemp);

            //Concession for cpof name label
            if ((x + labelWidth + 3) > cpofNameX)
            {
                cpofNameX = x + labelWidth + 3;
            }
        }

        if (modifiers.indexOfKey(ModifiersUnits.W_DTG_1) >= 0)
        {
            modifierValue = modifiers.get(ModifiersUnits.W_DTG_1);

            if(modifierValue != null)
            {
	            tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont);
	            labelBounds = tiTemp.getTextBounds();
	            labelWidth = labelBounds.width();
	
	            if (!byLabelHeight)
	            {
	                x = bounds.left - labelWidth - bufferXL;
	                y = bounds.top - bufferY - descent;
	            }
	            else
	            {
	                x = bounds.left - labelWidth - bufferXL;
	
	                y = (bounds.height());
	                y = (int) ((y * 0.5) + (labelHeight * 0.5));
	
	                y = y - ((labelHeight + bufferText) * 2);
	                y = bounds.top + y;
	            }
	
	            tiTemp.setLocation(x, y);
	            tiArray.add(tiTemp);
            }
        }

        if (modifiers.indexOfKey(ModifiersUnits.F_REINFORCED_REDUCED) >= 0 || modifiers.indexOfKey(ModifiersUnits.E_FRAME_SHAPE_MODIFIER) >= 0)
        {
            modifierValue = null;
            String E = null,
                    F = null;

            if (modifiers.indexOfKey(ModifiersUnits.E_FRAME_SHAPE_MODIFIER) >= 0)
            {
                E = modifiers.get(ModifiersUnits.E_FRAME_SHAPE_MODIFIER);
                modifiers.delete(ModifiersUnits.E_FRAME_SHAPE_MODIFIER);
            }
            if (modifiers.indexOfKey(ModifiersUnits.F_REINFORCED_REDUCED) >= 0 && SymbolUtilities.canUnitHaveModifier(symbolID, ModifiersUnits.F_REINFORCED_REDUCED))
            {
                F = modifiers.get(ModifiersUnits.F_REINFORCED_REDUCED);
            }

            if (E != null && E.equals("") == false)
            {
                modifierValue = E;
            }

            if (F != null && F.equals("") == false)
            {
                if (F.toUpperCase(Locale.US) == ("R"))
                {
                    F = "(+)";
                }
                else if (F.toUpperCase(Locale.US) == ("D"))
                {
                    F = "(-)";
                }
                else if (F.toUpperCase(Locale.US) == ("RD"))
                {
                    F = "(" + (char) (177) + ")";
                }
            }

            if (F != null && F.equals("") == false)
            {
                if (modifierValue != null && modifierValue.equals("") == false)
                {
                    modifierValue = modifierValue + " " + F;
                }
                else
                {
                    modifierValue = F;
                }
            }

            if(modifierValue != null)
            {
	            tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont);
	            labelBounds = tiTemp.getTextBounds();
	            labelWidth = labelBounds.width();
	
	            if (!byLabelHeight)
	            {
	                x = bounds.left + bounds.width() + bufferXR;
	                y = bounds.top - bufferY - descent;
	            }
	            else
	            {
	                x = bounds.left + bounds.width() + bufferXR;
	
	                y = (bounds.height());
	                y = (int) ((y * 0.5) + (labelHeight * 0.5));
	
	                y = y - ((labelHeight + bufferText) * 2);
	                y = bounds.top + y;
	            }
	
	            tiTemp.setLocation(x, y);
	            tiArray.add(tiTemp);
	
	            //Concession for cpof name label
	            if ((x + labelWidth + 3) > cpofNameX)
	            {
	                cpofNameX = x + labelWidth + 3;
	            }
            }
        }

        if (modifiers.indexOfKey(ModifiersUnits.AA_SPECIAL_C2_HQ) >= 0 && SymbolUtilities.canUnitHaveModifier(symbolID, ModifiersUnits.AA_SPECIAL_C2_HQ))
        {
            modifierValue = modifiers.get(ModifiersUnits.AA_SPECIAL_C2_HQ);

            if(modifierValue != null)
            {
	            tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont);
	            labelBounds = tiTemp.getTextBounds();
	            labelWidth = labelBounds.width();
	
	            x = (int) ((symbolBounds.left + (symbolBounds.width() * 0.5f)) - (labelWidth * 0.5f));
	
	            y = (symbolBounds.height());//checkpoint, get box above the point
	            y = (int) ((y * 0.5) + ((labelHeight - descent) * 0.5));
	            y = symbolBounds.top + y;
	
	            tiTemp.setLocation(x, y);
	            tiArray.add(tiTemp);
            }
        }
        
        if (modifiers.indexOfKey(ModifiersUnits.SCC_SONAR_CLASSIFICATION_CONFIDENCE) >= 0 && SymbolUtilities.canUnitHaveModifier(symbolID, ModifiersUnits.SCC_SONAR_CLASSIFICATION_CONFIDENCE))
        {
        	int scc = 0;
            modifierValue = modifiers.get(ModifiersUnits.SCC_SONAR_CLASSIFICATION_CONFIDENCE);
            
            if(modifierValue != null && SymbolUtilities.isNumber(modifierValue) && SymbolUtilities.hasModifier(symbolID, ModifiersUnits.SCC_SONAR_CLASSIFICATION_CONFIDENCE))
            {
	            tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont);
	            labelBounds = tiTemp.getTextBounds();
	            labelWidth = labelBounds.width();
	
	            x = (int) ((symbolBounds.left + (symbolBounds.width() * 0.5f)) - (labelWidth * 0.5f));
	
	            double yPosition = getYPositionForSCC(symbolID);
	            y = (bounds.height() );//checkpoint, get box above the point
                y = (int)(((y * yPosition) + ((labelHeight-descent) * 0.5)));
                y = bounds.top + y;
	
	            tiTemp.setLocation(x, y);
	            tiArray.add(tiTemp);
            }
        }

        if (modifiers.indexOfKey(ModifiersUnits.CN_CPOF_NAME_LABEL) >= 0)
        {
            modifierValue = modifiers.get(ModifiersUnits.CN_CPOF_NAME_LABEL);

            if(modifierValue != null)
            {
	            tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont);
	            labelBounds = tiTemp.getTextBounds();
	            labelWidth = labelBounds.width();
	
	            x = cpofNameX;
	
	            y = (bounds.height());//checkpoint, get box above the point
	            y = (int) ((y * 0.5) + (labelHeight * 0.5));
	            y = bounds.top + y;
	
	            tiTemp.setLocation(x, y);
	            tiArray.add(tiTemp);
            }
        }

        // </editor-fold>
        // <editor-fold defaultstate="collapsed" desc="Shift Points and Draw">
        Rect modifierBounds = null;
        if (tiArray != null && tiArray.size() > 0)
        {

            //build modifier bounds/////////////////////////////////////////
            modifierBounds = tiArray.get(0).getTextOutlineBounds();
            int size = tiArray.size();
            TextInfo tempShape = null;
            for (int i = 1; i < size; i++)
            {
                tempShape = tiArray.get(i);
                modifierBounds.union(tempShape.getTextOutlineBounds());
            }

        }

        if (modifierBounds != null)
        {

            imageBounds.union(modifierBounds);

            //shift points if needed////////////////////////////////////////
            if (imageBounds.left < 0 || imageBounds.top < 0)
            {
                int shiftX = Math.round(Math.abs(imageBounds.left)),
                        shiftY = Math.round(Math.abs(imageBounds.top));

                //shift mobility points
                int size = tiArray.size();
                TextInfo tempShape = null;
                for (int i = 0; i < size; i++)
                {
                    tempShape = tiArray.get(i);
                    tempShape.shift(shiftX, shiftY);
                }
                RectUtilities.shift(modifierBounds, shiftX, shiftY);
                //modifierBounds.shift(shiftX,shiftY);

                //shift image points
                centerPoint.offset(shiftX, shiftY);
                RectUtilities.shift(symbolBounds, shiftX, shiftY);
                RectUtilities.shift(imageBounds, shiftX, shiftY);
                RectUtilities.shift(imageBoundsOld, shiftX, shiftY);
                /*centerPoint.shift(shiftX, shiftY);
                 symbolBounds.shift(shiftX, shiftY);
                 imageBounds.shift(shiftX, shiftY);
                 imageBoundsOld.shift(shiftX, shiftY);//*/
            }

            Bitmap bmp = Bitmap.createBitmap(imageBounds.width(), imageBounds.height(), Config.ARGB_8888);
            Canvas ctx = new Canvas(bmp);

            //render////////////////////////////////////////////////////////
            //draw original icon with potential modifiers.
            ctx.drawBitmap(ii.getImage(), imageBoundsOld.left, imageBoundsOld.top, null);
            //ctx.drawImage(ii.getImage(),imageBoundsOld.left,imageBoundsOld.top);
            
            
            if (attributes.indexOfKey(MilStdAttributes.TextColor) >= 0)
            {
                textColor = SymbolUtilities.getColorFromHexString(attributes.get(MilStdAttributes.TextColor));
                if(alpha > -1)
                    textColor.setAlpha(alpha);
            }
            if (attributes.indexOfKey(MilStdAttributes.TextBackgroundColor) >= 0)
            {
                textBackgroundColor = SymbolUtilities.getColorFromHexString(attributes.get(MilStdAttributes.TextBackgroundColor));
                if(alpha > -1)
                    textBackgroundColor.setAlpha(alpha);
            }

            renderText(ctx, tiArray, textColor, textBackgroundColor);

            newii = new ImageInfo(bmp, centerPoint, symbolBounds);

        }
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Cleanup">
        tiArray = null;
        tiTemp = null;
        //tempShape = null;
        imageBoundsOld = null;
        //ctx = null;
        //buffer = null;
        // </editor-fold>

        return newii;
    }

    public static ImageInfo ProcessTGSPWithSpecialModifierLayout(ImageInfo ii, String symbolID, SparseArray<String> modifiers, SparseArray<String> attributes, Color lineColor)
    {
        int bufferXL = 6;
        int bufferXR = 4;
        int bufferY = 2;
        int bufferText = 2;
        int centerOffset = 1; //getCenterX/Y function seems to go over by a pixel
        int x = 0;
        int y = 0;
        int x2 = 0;
        int y2 = 0;
        int symStd = RS.getSymbologyStandard();
        int outlineOffset = RS.getTextOutlineWidth();
        int labelHeight = 0;
        int labelWidth = 0;
        int alpha = -1;
        ImageInfo newii = null;
        Color textColor = lineColor;
        Color textBackgroundColor = null;
        Color modifierColor = Color.BLACK;

        ArrayList<TextInfo> arrMods = new ArrayList<TextInfo>();
        boolean duplicate = false;

        Rect bounds = new Rect(ii.getSymbolBounds());
        Rect symbolBounds = new Rect(ii.getSymbolBounds());
        Point centerPoint = new Point(ii.getCenterPoint());
        Rect imageBounds = new Rect(ii.getImageBounds());

        if (attributes.indexOfKey(MilStdAttributes.SymbologyStandard) >= 0)
        {
            symStd = Integer.parseInt(attributes.get(MilStdAttributes.SymbologyStandard));
        }
        if (attributes.indexOfKey(MilStdAttributes.Alpha) >= 0)
        {
            alpha = Integer.parseInt(attributes.get(MilStdAttributes.Alpha));
            textColor.setAlpha(alpha);
        }
        if (attributes.indexOfKey(MilStdAttributes.ModifierColor) >= 0)
        {
            modifierColor = SymbolUtilities.getColorFromHexString(attributes.get(MilStdAttributes.ModifierColor));
            if (alpha > -1 && modifierColor != null)
                modifierColor.setAlpha(alpha);
        }

        centerPoint = new Point(Math.round(ii.getCenterPoint().x), Math.round(ii.getCenterPoint().y));

        boolean byLabelHeight = false;
        labelHeight = (int) (_modifierFontHeight + 0.5f);

        int maxHeight = (symbolBounds.height());
        if ((labelHeight * 3) > maxHeight)
        {
            byLabelHeight = true;
        }

        int descent = (int) (_modifierFontDescent + 0.5f);
        int yForY = -1;

        Rect labelBounds1 = null;//text.getPixelBounds(null, 0, 0);
        Rect labelBounds2 = null;
        String strText = "";
        String strText1 = "";
        String strText2 = "";
        TextInfo text1 = null;
        TextInfo text2 = null;

        String basicID = SymbolUtilities.getBasicSymbolID(symbolID);

        if (outlineOffset > 2)
        {
            outlineOffset = ((outlineOffset - 1) / 2);
        }
        else
        {
            outlineOffset = 0;
        }

        // <editor-fold defaultstate="collapsed" desc="Process Integral Text">
        if (basicID.equals("G*G*GPRD--****X"))//DLRP (D)
        {

            strText1 = "D";

            text1 = new TextInfo(strText1, 0, 0, _modifierFont);

            labelBounds1 = text1.getTextBounds();
            if (symStd == RendererSettings.Symbology_2525B)
            {
                y = symbolBounds.top + symbolBounds.height();
                x = symbolBounds.left - labelBounds1.width() - bufferXL;
                text1.setLocation(Math.round(x), Math.round(y));
            }
            else//2525C built in
            {
                text1 = null;
                //y = symbolBounds.top + symbolBounds.getHeight() - bufferY;
                //x = symbolBounds.left + symbolBounds.width()/2 - labelBounds1.getWidth()/2;
            }

            //ErrorLogger.LogMessage("D: " + String.valueOf(x)+ ", " + String.valueOf(y));
        }
        else if (basicID.equals("G*G*APU---****X")) //pull-up point (PUP)
        {
            strText1 = "PUP";
            text1 = new TextInfo(strText1, 0, 0, _modifierFont);

            labelBounds1 = text1.getTextBounds();
            y = RectUtilities.getCenterY(symbolBounds) + ((labelBounds1.height() - descent) / 2);
            x = symbolBounds.left + symbolBounds.width() + bufferXR;

            text1.setLocation(Math.round(x), Math.round(y));
        }
        else if (basicID.equals("G*M*NZ----****X")) //Nuclear Detonation Ground Zero (N)
        {
//                strText1 = "N";
//                text1 = new TextLayout(strText1, labelFont, frc);
//                labelBounds1 = text1.getPixelBounds(null, 0, 0);
//                y = symbolBounds.top + (symbolBounds.getHeight() * 0.8) - centerOffset;
//                x = symbolBounds.getCenterX() - centerOffset - (labelBounds1.getWidth()/2);
        }
        else if (basicID.equals("G*M*NF----****X"))//Fallout Producing (N)
        {
//                strText1 = "N";
//                text1 = new TextLayout(strText1, labelFont, frc);
//                descent = text1.getDescent();
//                labelBounds1 = text1.getPixelBounds(null, 0, 0);
//                y = symbolBounds.top + (symbolBounds.getHeight() * 0.8) - centerOffset;
//                x = symbolBounds.getCenterX() - centerOffset - (labelBounds1.getWidth()/2);
        }
        else if (basicID.equals("G*M*NEB---****X"))//Release Events Biological (BIO, B)
        {
            //strText1 = "B";
            //text1 = new TextLayout(strText1, labelFont, frc);
            int offset = 1;
            strText2 = "BIO";
            text2 = new TextInfo(strText2, 0, 0, _modifierFont);

            labelBounds2 = text2.getTextBounds();
            //y = symbolBounds.top + (symbolBounds.getHeight() * 0.9);
            //x = symbolBounds.getCenterX() - centerOffset - (labelBounds1.getWidth()/2);

            y2 = (int) (RectUtilities.getCenterY(symbolBounds) + ((labelBounds2.height() - descent) * 0.5f));

            x2 = symbolBounds.left - labelBounds2.width() - bufferXL;

            text2.setLocation(Math.round(x2), Math.round(y2 - offset));
            //ErrorLogger.LogMessage("BIO: " + String.valueOf(x2)+ ", " + String.valueOf(y2));
        }
        else if (basicID.equals("G*M*NEC---****X"))//Release Events Chemical (CML, C)
        {
            //strText1 = "C";
            //text1 = new TextLayout(strText1, labelFont, frc);
            int offset = 1;
            strText2 = "CML";
            text2 = new TextInfo(strText2, 0, 0, _modifierFont);

            labelBounds2 = text2.getTextBounds();
            //y = symbolBounds.top + (symbolBounds.getHeight() * 0.9);
            //x = symbolBounds.getCenterX() - centerOffset - (labelBounds1.getWidth()/2);

            y2 = RectUtilities.getCenterY(symbolBounds) + ((labelBounds2.height() - descent) / 2);

            x2 = symbolBounds.left - labelBounds2.width() - bufferXL;

            text2.setLocation(Math.round(x2), Math.round(y2 - offset));
        }
        if (text1 != null)
        {
            arrMods.add(text1);
        }
        if (text2 != null)
        {
            arrMods.add(text2);
        }

        // </editor-fold>
        // <editor-fold defaultstate="collapsed" desc="Process Special Modifiers">
        TextInfo ti = null;
        if (basicID.equals("G*M*NZ----****X") ||//ground zero
                basicID.equals("G*M*NEB---****X") ||//biological
                basicID.equals("G*M*NEC---****X"))//chemical
        {
            if ((labelHeight * 3) > bounds.height())
            {
                byLabelHeight = true;
            }
        }

        if (basicID.equals("G*G*GPPC--****X")
                || basicID.equals("G*G*GPPD--****X"))
        {
            if (modifiers.indexOfKey(ModifiersTG.T_UNIQUE_DESIGNATION_1) >= 0)
            {
                strText = modifiers.get(ModifiersTG.T_UNIQUE_DESIGNATION_1);
                if(strText != null)
                {
	                ti = new TextInfo(strText, 0, 0, _modifierFont);
	                labelWidth = Math.round(ti.getTextBounds().width());
	                //One modifier symbols and modifier goes in center
	                x = bounds.left + (int) (bounds.width() * 0.5f);
	                x = x - (int) (labelWidth * 0.5f);
	                y = bounds.top + (int) (bounds.height() * 0.4f);
	                y = y + (int) (labelHeight * 0.5f);
	
	                ti.setLocation(Math.round(x), Math.round(y));
	                arrMods.add(ti);
                }
            }
        }
        else if (basicID.equals("G*G*GPH---****X"))
        {
            if (modifiers.indexOfKey(ModifiersTG.H_ADDITIONAL_INFO_1) >= 0)
            {
                strText = modifiers.get(ModifiersTG.H_ADDITIONAL_INFO_1);
                if(strText != null)
                {
	                ti = new TextInfo(strText, 0, 0, _modifierFont);
	                labelWidth = Math.round(ti.getTextBounds().width());
	                //One modifier symbols and modifier goes in center
	                x = bounds.left + (int) (bounds.width() * 0.5f);
	                x = x - (int) (labelWidth * 0.5f);
	                y = bounds.top + (int) (bounds.height() * 0.5f);
	                y = y + (int) (labelHeight * 0.5f);
	
	                ti.setLocation(Math.round(x), Math.round(y));
	                arrMods.add(ti);
                }
            }
        }
        else if (basicID.equals("G*G*GPRI--****X"))
        {
            if (modifiers.indexOfKey(ModifiersTG.T_UNIQUE_DESIGNATION_1) >= 0)
            {
                strText = modifiers.get(ModifiersTG.T_UNIQUE_DESIGNATION_1);
                if(strText != null)
                {
	                ti = new TextInfo(strText, 0, 0, _modifierFont);
	                labelWidth = Math.round(ti.getTextBounds().width());
	                //One modifier symbols, top third & center
	                x = bounds.left + (int) (bounds.width() * 0.5f);
	                x = x - (int) (labelWidth * 0.5f);
	                y = bounds.top + (int) (bounds.height() * 0.25f);
	                y = y + (int) (labelHeight * 0.5f);
	
	                ti.setLocation(Math.round(x), Math.round(y));
	                arrMods.add(ti);
                }
            }
        }
        else if (basicID.equals("G*G*GPPW--****X")
                || basicID.equals("G*F*PCF---****X"))
        {
            if (modifiers.indexOfKey(ModifiersTG.T_UNIQUE_DESIGNATION_1) >= 0)
            {
                strText = modifiers.get(ModifiersTG.T_UNIQUE_DESIGNATION_1);
                if(strText != null)
                {
	                ti = new TextInfo(strText, 0, 0, _modifierFont);
	
	                //One modifier symbols and modifier goes right of center
	                x = bounds.left + (int) (bounds.width() * 0.75f);
	                y = bounds.top + (int) (bounds.height() * 0.5f);
	                y = y + (int) ((labelHeight - descent) * 0.5f);
	
	                ti.setLocation(Math.round(x), Math.round(y));
	                arrMods.add(ti);
                }
            }
        }
        else if (basicID.equals("G*G*APP---****X")
                || basicID.equals("G*G*APC---****X"))
        {
            if (modifiers.indexOfKey(ModifiersTG.T_UNIQUE_DESIGNATION_1) >= 0)
            {
                strText = modifiers.get(ModifiersTG.T_UNIQUE_DESIGNATION_1);
                if(strText != null)
                {
	                ti = new TextInfo(strText, 0, 0, _modifierFont);
	                labelWidth = ti.getTextBounds().width();
	                //One modifier symbols and modifier goes just below of center
	                x = bounds.left + (int) (bounds.width() * 0.5);
	                x = x - (int) (labelWidth * 0.5);
	                y = bounds.top + (int) (bounds.height() * 0.5f);
	                y = y + (int) (((bounds.height() * 0.5f) - labelHeight) / 2) + labelHeight - descent;
	
	                ti.setLocation(Math.round(x), Math.round(y));
	                arrMods.add(ti);
                }
            }
        }
        else if (basicID.equals("G*G*DPT---****X") || //T (target reference point)
                basicID.equals("G*F*PTS---****X") || //t,h,h1 (Point/Single Target)
                basicID.equals("G*F*PTN---****X")) //T (nuclear target)
        { //Targets with special modifier positions
            if (modifiers.indexOfKey(ModifiersTG.H_ADDITIONAL_INFO_1) >= 0
                    && basicID.equals("G*F*PTS---****X"))//H
            {
                strText = modifiers.get(ModifiersTG.H_ADDITIONAL_INFO_1);
                if(strText != null)
                {
	                ti = new TextInfo(strText, 0, 0, _modifierFont);
	
	                x = RectUtilities.getCenterX(bounds) + (int) (bounds.width() * 0.15f);
	                y = bounds.top + (int) (bounds.height() * 0.75f);
	                y = y + (int) (labelHeight * 0.5f);
	
	                ti.setLocation(Math.round(x), Math.round(y));
	                arrMods.add(ti);
                }
            }
            if (modifiers.indexOfKey(ModifiersTG.H1_ADDITIONAL_INFO_2) >= 0
                    && basicID.equals("G*F*PTS---****X"))//H1
            {
                strText = modifiers.get(ModifiersTG.H1_ADDITIONAL_INFO_2);
                if(strText != null)
                {
	                ti = new TextInfo(strText, 0, 0, _modifierFont);
	                labelWidth = Math.round(ti.getTextBounds().width());
	                x = RectUtilities.getCenterX(bounds) - (int) (bounds.width() * 0.15f);
	                x = x - (labelWidth);
	                y = bounds.top + (int) (bounds.height() * 0.75f);
	                y = y + (int) (labelHeight * 0.5f);
	
	                ti.setLocation(Math.round(x), Math.round(y));
	                arrMods.add(ti);
                }
            }
            if (modifiers.indexOfKey(ModifiersTG.T_UNIQUE_DESIGNATION_1) >= 0)
            {
                strText = modifiers.get(ModifiersTG.T_UNIQUE_DESIGNATION_1);
                if(strText != null)
                {
	                ti = new TextInfo(strText, 0, 0, _modifierFont);
	
	                x = RectUtilities.getCenterX(bounds) + (int) (bounds.width() * 0.15f);
//                  x = x - (labelBounds.width * 0.5);
	                y = bounds.top + (int) (bounds.height() * 0.25f);
	                y = y + (int) (labelHeight * 0.5f);
	
	                ti.setLocation(Math.round(x), Math.round(y));
	                arrMods.add(ti);
                }
            }

        }
        else if (basicID.equals("G*M*NZ----****X") ||//ground zero
                basicID.equals("G*M*NEB---****X") ||//biological
                basicID.equals("G*M*NEC---****X"))//chemical
        {//NBC
            if (modifiers.indexOfKey(ModifiersTG.N_HOSTILE) >= 0)
            {
                strText = modifiers.get(ModifiersTG.N_HOSTILE);
                if(strText != null)
                {
	                ti = new TextInfo(strText, 0, 0, _modifierFont);
	
	                x = bounds.left + bounds.width() + bufferXR;
	
	                if (!byLabelHeight)
	                {
	                    y = bounds.top + bounds.height();
	                }
	                else
	                {
	                    y = bounds.top + (int) ((bounds.height() * 0.5f) + ((labelHeight - descent) * 0.5) + (labelHeight - descent + bufferText));
	                }
	
	                ti.setLocation(Math.round(x), Math.round(y));
	                arrMods.add(ti);
                }

            }
            if (modifiers.indexOfKey(ModifiersTG.H_ADDITIONAL_INFO_1) >= 0)
            {
                strText = modifiers.get(ModifiersTG.H_ADDITIONAL_INFO_1);
                if(strText != null)
                {
	                ti = new TextInfo(strText, 0, 0, _modifierFont);
	
	                x = bounds.left + bounds.width() + bufferXR;
	                if (!byLabelHeight)
	                {
	                    y = bounds.top + labelHeight - descent;
	                }
	                else
	                {
	                    //y = bounds.y + ((bounds.height * 0.5) + (labelHeight * 0.5) - (labelHeight + bufferText));
	                    y = bounds.top + (int) ((bounds.height() * 0.5f) - ((labelHeight - descent) * 0.5) + (-descent - bufferText));
	                }
	
	                ti.setLocation(Math.round(x), Math.round(y));
	                arrMods.add(ti);
                }
            }
            if (modifiers.indexOfKey(ModifiersTG.W_DTG_1) >= 0)
            {
                strText = modifiers.get(ModifiersTG.W_DTG_1);
                if(strText != null)
                {
	                ti = new TextInfo(strText, 0, 0, _modifierFont);
	                labelWidth = Math.round(ti.getTextBounds().width());
	
	                x = bounds.left - labelWidth - bufferXL;
	                if (!byLabelHeight)
	                {
	                    y = bounds.top + labelHeight - descent;
	                }
	                else
	                {
	                    //y = bounds.y + ((bounds.height * 0.5) + (labelHeight * 0.5) - (labelHeight + bufferText));
	                    y = bounds.top + (int) ((bounds.height() * 0.5) - ((labelHeight - descent) * 0.5) + (-descent - bufferText));
	                }
	
	                ti.setLocation(Math.round(x), Math.round(y));
	                arrMods.add(ti);
                }
            }
            if (basicID.equals("G*M*NZ----****X") == true && modifiers.indexOfKey(ModifiersTG.V_EQUIP_TYPE) >= 0)
            {
                strText = modifiers.get(ModifiersTG.V_EQUIP_TYPE);
                if(strText != null)
                {
	                ti = new TextInfo(strText, 0, 0, _modifierFont);
	
	                //subset of nbc, just nuclear
	                labelWidth = Math.round(ti.getTextBounds().width());
	                x = bounds.left - labelWidth - bufferXL;
	                y = bounds.top + (int) ((bounds.height() * 0.5) + ((labelHeight - descent) * 0.5));//((bounds.height / 2) - (labelHeight/2));
	
	                ti.setLocation(Math.round(x), Math.round(y));
	                arrMods.add(ti);
                }
            }
            if (modifiers.indexOfKey(ModifiersTG.T_UNIQUE_DESIGNATION_1) >= 0)
            {
                strText = modifiers.get(ModifiersTG.T_UNIQUE_DESIGNATION_1);
                if(strText != null)
                {
                	ti = new TextInfo(strText, 0, 0, _modifierFont);
	                labelWidth = Math.round(ti.getTextBounds().width());
	                x = bounds.left - labelWidth - bufferXL;
	                if (!byLabelHeight)
	                {
	                    y = bounds.top + bounds.height();
	                }
	                else
	                {
	                    //y = bounds.y + ((bounds.height * 0.5) + ((labelHeight-descent) * 0.5) + (labelHeight + bufferText));
	                    y = bounds.top + (int) ((bounds.height() * 0.5) + ((labelHeight - descent) * 0.5) + (labelHeight - descent + bufferText));
	                }
	                ti.setLocation(Math.round(x), Math.round(y));
	                arrMods.add(ti);
                }
            }
            if (modifiers.indexOfKey(ModifiersTG.Y_LOCATION) >= 0)
            {
                strText = modifiers.get(ModifiersTG.Y_LOCATION);
                if(strText != null)
                {
	                ti = new TextInfo(strText, 0, 0, _modifierFont);
	                labelWidth = Math.round(ti.getTextBounds().width());
	                //just NBC
	                //x = bounds.getX() + (bounds.getWidth() * 0.5);
	                //x = x - (labelWidth * 0.5);
	                x = bounds.left + (int) (bounds.width() * 0.5f);
	                x = x - (int) (labelWidth * 0.5f);
	
	                if (!byLabelHeight)
	                {
	                    y = bounds.top + bounds.height() + labelHeight - descent + bufferY;
	                }
	                else
	                {
	                    y = bounds.top + (int) ((bounds.height() * 0.5) + ((labelHeight - descent) * 0.5) + ((labelHeight + bufferText) * 2) - descent);
	
	                }
	                yForY = y + descent; //so we know where to start the DOM arrow.
	                ti.setLocation(Math.round(x), Math.round(y));
	                arrMods.add(ti);
                }

            }
            if (modifiers.indexOfKey(ModifiersTG.C_QUANTITY) >= 0)
            {
                strText = modifiers.get(ModifiersTG.C_QUANTITY);
                if(strText != null)
                {
	                ti = new TextInfo(strText, 0, 0, _modifierFont);
	                labelWidth = Math.round(ti.getTextBounds().width());
	                //subset of NBC, just nuclear
	                x = bounds.left + (int) (bounds.width() * 0.5);
	                x = x - (int) (labelWidth * 0.5);
	                y = bounds.top - descent;
	                ti.setLocation(Math.round(x), Math.round(y));
	                arrMods.add(ti);
                }

            }
        }
        else if (basicID.equals("G*M*OFS---****X"))
        {
            if (modifiers.indexOfKey(ModifiersTG.H_ADDITIONAL_INFO_1) >= 0)
            {
                strText = modifiers.get(ModifiersTG.H_ADDITIONAL_INFO_1);
                if(strText != null)
                {
	                ti = new TextInfo(strText, 0, 0, _modifierFont);
	                labelWidth = Math.round(ti.getTextBounds().width());
	                x = bounds.left + (int) (bounds.width() * 0.5);
	                x = x - (int) (labelWidth * 0.5);
	                y = bounds.top - descent;// + (bounds.height * 0.5);
	                //y = y + (labelHeight * 0.5);
	
	                ti.setLocation(Math.round(x), Math.round(y));
	                arrMods.add(ti);
                }

            }
            if (modifiers.indexOfKey(ModifiersTG.W_DTG_1) >= 0)
            {
                strText = modifiers.get(ModifiersTG.W_DTG_1);
                if(strText != null)
                {
	                ti = new TextInfo(strText, 0, 0, _modifierFont);
	                labelWidth = Math.round(ti.getTextBounds().width());
	                x = bounds.left + (int) (bounds.width() * 0.5);
	                x = x - (int) (labelWidth * 0.5);
	                y = bounds.top + (bounds.height());
	                y = y + (labelHeight);
	
	                ti.setLocation(Math.round(x), Math.round(y));
	                arrMods.add(ti);
                }
            }
            if (modifiers.indexOfKey(ModifiersTG.N_HOSTILE) >= 0)
            {
                strText = modifiers.get(ModifiersTG.N_HOSTILE);
                if(strText != null)
                {
	                ti = new TextInfo(strText, 0, 0, _modifierFont);
	                TextInfo ti2 = new TextInfo(strText, 0, 0, _modifierFont);
	                labelWidth = Math.round(ti.getTextBounds().width());
	                x = bounds.left + (bounds.width()) + bufferXR;//right
	                //x = x + labelWidth;//- (labelBounds.width * 0.75);
	
	                duplicate = true;
	
	                x2 = bounds.left;//left
	                x2 = x2 - labelWidth - bufferXL;// - (labelBounds.width * 0.25);
	
	                y = bounds.top + (int) (bounds.height() * 0.5);//center
	                y = y + (int) ((labelHeight - descent) * 0.5);
	
	                y2 = y;
	
	                ti.setLocation(Math.round(x), Math.round(y));
	                ti2.setLocation(Math.round(x2), Math.round(y2));
	                arrMods.add(ti);
	                arrMods.add(ti2);
                }
            }

        }
        else if(basicID.charAt(0) == 'W')
        {
            String modX = null;
            if(modifiers.indexOfKey(ModifiersTG.X_ALTITUDE_DEPTH) > -1)
                modX = (modifiers.get(ModifiersTG.X_ALTITUDE_DEPTH));

            if(basicID.equals("WAS-WSF-LVP----"))//Freezing Level
            {
                strText = "0" + (char)(176) + ":";
                if(modX != null)
                    strText += modX;
                else
                    strText += "?";

                ti = new TextInfo(strText, 0, 0, _modifierFont);
                labelWidth = Math.round(ti.getTextBounds().width());
                //One modifier symbols and modifier goes in center
                x = bounds.left + (int) (bounds.width() * 0.5f);
                x = x - (int) (labelWidth * 0.5f);
                y = bounds.top + (int) (bounds.height() * 0.5f);
                y = y + (int) ((labelHeight - _modifierFontDescent) * 0.5f);

                ti.setLocation(Math.round(x), Math.round(y));
                arrMods.add(ti);
            }
            else if(basicID.equals("WAS-WST-LVP----"))//tropopause Level
            {
                strText = "X?";
                if(modX != null)
                    strText = modX;

                ti = new TextInfo(strText, 0, 0, _modifierFont);
                labelWidth = Math.round(ti.getTextBounds().width());
                //One modifier symbols and modifier goes in center
                x = bounds.left + (int) (bounds.width() * 0.5f);
                x = x - (int) (labelWidth * 0.5f);
                y = bounds.top + (int) (bounds.height() * 0.5f);
                y = y + (int) ((labelHeight - _modifierFontDescent) * 0.5f);

                ti.setLocation(Math.round(x), Math.round(y));
                arrMods.add(ti);
            }
            else if(basicID.equals("WAS-PLT---P----"))//tropopause Low
            {
                strText = "X?";
                if(modX != null)
                    strText = modX;

                ti = new TextInfo(strText, 0, 0, _modifierFont);
                labelWidth = Math.round(ti.getTextBounds().width());
                //One modifier symbols and modifier goes in center
                x = bounds.left + (int) (bounds.width() * 0.5f);
                x = x - (int) (labelWidth * 0.5f);
                y = bounds.top + (int) (bounds.height() * 0.5f);
                y = y - descent;

                ti.setLocation(Math.round(x), Math.round(y));
                arrMods.add(ti);
            }
            else if(basicID.equals("WAS-PHT---P----"))//tropopause High
            {
                strText = "X?";
                if(modX != null)
                    strText = modX;

                ti = new TextInfo(strText, 0, 0, _modifierFont);
                labelWidth = Math.round(ti.getTextBounds().width());
                //One modifier symbols and modifier goes in center
                x = bounds.left + (int) (bounds.width() * 0.5f);
                x = x - (int) (labelWidth * 0.5f);
                y = bounds.top + (int) (bounds.height() * 0.5f);
                //y = y + (int) ((labelHeight * 0.5f) + (labelHeight/2));
                y = y + (int) (((labelHeight * 0.5f) - (labelHeight/2)) + labelHeight - descent);

                ti.setLocation(Math.round(x), Math.round(y));
                arrMods.add(ti);
            }
        }
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="DOM Arrow">
        Point[] domPoints = null;
        Rect domBounds = null;
        if (modifiers.indexOfKey(ModifiersTG.Q_DIRECTION_OF_MOVEMENT) >= 0
                && (basicID.equals("G*M*NZ----****X") ||//ground zero
                basicID.equals("G*M*NEB---****X") ||//biological
                basicID.equals("G*M*NEC---****X")))//chemical)
        {
            strText = modifiers.get(ModifiersTG.Q_DIRECTION_OF_MOVEMENT);
            if(strText != null && SymbolUtilities.isNumber(strText))
            {
	            float q = Float.parseFloat(strText);
	            Rect tempBounds = new Rect(bounds);
	            tempBounds.union(RectUtilities.getCenterX(bounds), yForY);
	
	            domPoints = createDOMArrowPoints(symbolID, tempBounds, ii.getCenterPoint(), q, false);
	
	            domBounds = RectUtilities.makeRect(domPoints[0].x, domPoints[0].y, 1, 1);
	
	            Point temp = null;
	            for (int i = 1; i < 6; i++)
	            {
	                temp = domPoints[i];
	                if (temp != null)
	                {
	                    domBounds.union(temp.x, temp.y);
	                }
	            }
	            imageBounds.union(domBounds);
            }
        }
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Shift Points and Draw">
        Rect modifierBounds = null;
        if (arrMods != null && arrMods.size() > 0)
        {

            //build modifier bounds/////////////////////////////////////////
            modifierBounds = arrMods.get(0).getTextOutlineBounds();
            int size = arrMods.size();
            TextInfo tempShape = null;
            for (int i = 1; i < size; i++)
            {
                tempShape = arrMods.get(i);
                modifierBounds.union(tempShape.getTextOutlineBounds());
            }

        }

        if (modifierBounds != null || domBounds != null)
        {

            if (modifierBounds != null)
            {
                imageBounds.union(modifierBounds);
            }
            if (domBounds != null)
            {
                imageBounds.union(domBounds);
            }

            //shift points if needed////////////////////////////////////////
            if (imageBounds.left < 0 || imageBounds.top < 0)
            {
                int shiftX = Math.abs(imageBounds.left);
                int shiftY = Math.abs(imageBounds.top);

                //shift mobility points
                int size = arrMods.size();
                TextInfo tempShape = null;
                for (int i = 0; i < size; i++)
                {
                    tempShape = arrMods.get(i);
                    tempShape.shift(shiftX, shiftY);
                }
                modifierBounds.offset(shiftX, shiftY);

                if (domBounds != null)
                {
                    for (int i = 0; i < 6; i++)
                    {
                        Point temp = domPoints[i];
                        if (temp != null)
                        {
                            temp.offset(shiftX, shiftY);
                        }
                    }
                    domBounds.offset(shiftX, shiftY);
                }

                //shift image points
                centerPoint.offset(shiftX, shiftY);
                symbolBounds.offset(shiftX, shiftY);
                imageBounds.offset(shiftX, shiftY);
            }

            //Render modifiers//////////////////////////////////////////////////
            Bitmap bmp = Bitmap.createBitmap(imageBounds.width(), imageBounds.height(), Config.ARGB_8888);
            Canvas ctx = new Canvas(bmp);

            //render////////////////////////////////////////////////////////
            //draw original icon with potential modifiers.
            ctx.drawBitmap(ii.getImage(), symbolBounds.left, symbolBounds.top, null);
            //ctx.drawImage(ii.getImage(),imageBoundsOld.left,imageBoundsOld.top);

            if (attributes.indexOfKey(MilStdAttributes.TextColor) >= 0)
            {
                textColor = SymbolUtilities.getColorFromHexString(attributes.get(MilStdAttributes.TextColor));
                if(alpha > -1)
                    textColor.setAlpha(alpha);
            }
            if (attributes.indexOfKey(MilStdAttributes.TextBackgroundColor) >= 0)
            {
                textBackgroundColor = SymbolUtilities.getColorFromHexString(attributes.get(MilStdAttributes.TextBackgroundColor));
                if(alpha > -1)
                    textBackgroundColor.setAlpha(alpha);
            }


            
            renderText(ctx, arrMods, textColor, textBackgroundColor);

            newii = new ImageInfo(bmp, centerPoint, symbolBounds);

            //draw DOM arrow
            if (domBounds != null)
            {
                drawDOMArrow(ctx, domPoints, modifierColor, alpha);
            }

            newii = new ImageInfo(bmp, centerPoint, symbolBounds);

            // <editor-fold defaultstate="collapsed" desc="Cleanup">
            ctx = null;
            // </editor-fold>

            return newii;

        }
        else
        {
            return null;
        }
        // </editor-fold>

    }

    public static ImageInfo ProcessTGSPModifiers(ImageInfo ii, String symbolID, SparseArray<String> modifiers, SparseArray<String> attributes, Color lineColor)
    {

        // <editor-fold defaultstate="collapsed" desc="Variables">
        int bufferXL = 6;
        int bufferXR = 4;
        int bufferY = 2;
        int bufferText = 2;
        int centerOffset = 1; //getCenterX/Y function seems to go over by a pixel
        int x = 0;
        int y = 0;
        int x2 = 0;
        int y2 = 0;
        int symStd = RS.getSymbologyStandard();
        int outlineOffset = RS.getTextOutlineWidth();
        int labelHeight = 0;
        int labelWidth = 0;
        int alpha = -1;
        ImageInfo newii = null;
        
        Color textColor = lineColor;
        Color textBackgroundColor = null;

        ArrayList<TextInfo> arrMods = new ArrayList<TextInfo>();
        boolean duplicate = false;

        if (attributes.indexOfKey(MilStdAttributes.SymbologyStandard) >= 0)
        {
            symStd = Integer.parseInt(attributes.get(MilStdAttributes.SymbologyStandard));
        }
        if (attributes.indexOfKey(MilStdAttributes.Alpha) >= 0)
        {
            alpha = Integer.parseInt(attributes.get(MilStdAttributes.Alpha));
        }

        Rect bounds = new Rect(ii.getSymbolBounds());
        Rect symbolBounds = new Rect(ii.getSymbolBounds());
        Point centerPoint = new Point(ii.getCenterPoint());
        Rect imageBounds = new Rect(ii.getImageBounds());

        centerPoint = new Point(Math.round(ii.getCenterPoint().x), Math.round(ii.getCenterPoint().y));

        boolean byLabelHeight = false;

        labelHeight = Math.round(_modifierFontHeight + 0.5f);
        int maxHeight = (symbolBounds.height());
        if ((labelHeight * 3) > maxHeight)
        {
            byLabelHeight = true;
        }

        int descent = (int) (_modifierFontDescent + 0.5f);
        int yForY = -1;

        Rect labelBounds1 = null;//text.getPixelBounds(null, 0, 0);
        Rect labelBounds2 = null;
        String strText = "";
        String strText1 = "";
        String strText2 = "";
        TextInfo text1 = null;
        TextInfo text2 = null;

        String basicID = SymbolUtilities.getBasicSymbolID(symbolID);

        if (outlineOffset > 2)
        {
            outlineOffset = ((outlineOffset - 1) / 2);
        }
        else
        {
            outlineOffset = 0;
        }

        /*bufferXL += outlineOffset;
         bufferXR += outlineOffset;
         bufferY += outlineOffset;
         bufferText += outlineOffset;*/
        // </editor-fold>
        // <editor-fold defaultstate="collapsed" desc="Process Modifiers">
        TextInfo ti = null;

        {
            if (modifiers.indexOfKey(ModifiersTG.N_HOSTILE) >= 0)
            {
                strText = modifiers.get(ModifiersTG.N_HOSTILE);
                if(strText != null)
                {
	                ti = new TextInfo(strText, 0, 0, _modifierFont);
	
	                x = bounds.left + bounds.width() + bufferXR;
	
	                if (!byLabelHeight)
	                {
	                    y = ((bounds.height() / 3) * 2);//checkpoint, get box above the point
	                    y = bounds.top + y;
	                }
	                else
	                {
	                    //y = ((labelHeight + bufferText) * 3);
	                    //y = bounds.y + y - descent;
	                    y = bounds.top + bounds.height();
	                }
	
	                ti.setLocation(x, y);
	                arrMods.add(ti);
                }

            }
            if (modifiers.indexOfKey(ModifiersTG.H_ADDITIONAL_INFO_1) >= 0)
            {
            	strText = modifiers.get(ModifiersTG.H_ADDITIONAL_INFO_1);
            	if(strText != null)
            	{
	                ti = new TextInfo(strText, 0, 0, _modifierFont);
	                labelWidth = Math.round(ti.getTextBounds().width());
	
	                x = bounds.left + (int) (bounds.width() * 0.5f);
	                x = x - (int) (labelWidth * 0.5f);
	                y = bounds.top - descent;
	
	                ti.setLocation(x, y);
	                arrMods.add(ti);
            	}
            }
            if (modifiers.indexOfKey(ModifiersTG.H1_ADDITIONAL_INFO_2) >= 0)
            {
                strText = modifiers.get(ModifiersTG.H1_ADDITIONAL_INFO_2);
                if(strText != null)
                {
	                ti = new TextInfo(strText, 0, 0, _modifierFont);
	                labelWidth = Math.round(ti.getTextBounds().width());
	
	                x = bounds.left + (int) (bounds.width() * 0.5);
	                x = x - (int) (labelWidth * 0.5);
	                y = bounds.top + labelHeight + (int) (bounds.height() * 0.2);
	
	                ti.setLocation(x, y);
	                arrMods.add(ti);
                }
            }
            if (modifiers.indexOfKey(ModifiersTG.W_DTG_1) >= 0)
            {
                strText = modifiers.get(ModifiersTG.W_DTG_1);
                if(strText != null)
                {
	                ti = new TextInfo(strText, 0, 0, _modifierFont);
	                labelWidth = Math.round(ti.getTextBounds().width());
	
	                x = bounds.left - labelWidth - bufferXL;
	                y = bounds.top + labelHeight - descent;
	
	                ti.setLocation(x, y);
	                arrMods.add(ti);
                }
            }
            if (modifiers.indexOfKey(ModifiersTG.W1_DTG_2) >= 0)
            {
                strText = modifiers.get(ModifiersTG.W1_DTG_2);
                if(strText != null)
                {
	                ti = new TextInfo(strText, 0, 0, _modifierFont);
	                labelWidth = Math.round(ti.getTextBounds().width());
	
	                x = bounds.left - labelWidth - bufferXL;
	
	                y = ((labelHeight - descent + bufferText) * 2);
	                y = bounds.top + y;
	
	                ti.setLocation(x, y);
	                arrMods.add(ti);
                }
            }
            if (modifiers.indexOfKey(ModifiersTG.T_UNIQUE_DESIGNATION_1) >= 0)
            {
                strText = modifiers.get(ModifiersTG.T_UNIQUE_DESIGNATION_1);
                if(strText != null)
                {
	                ti = new TextInfo(strText, 0, 0, _modifierFont);
	
	                x = bounds.left + bounds.width() + bufferXR;
	                y = bounds.top + labelHeight - descent;
	
	                ti.setLocation(x, y);
	                arrMods.add(ti);
                }
            }
            if ((modifiers.indexOfKey(ModifiersTG.T1_UNIQUE_DESIGNATION_2) >= 0) &&//T1
                    (basicID.equals("G*O*ES----****X") || //emergency distress call
                    basicID.equals("G*S*PP----****X") || //medevac pick-up point
                    basicID.equals("G*S*PX----****X")))//ambulance exchange point
            {
                strText = modifiers.get(ModifiersTG.T1_UNIQUE_DESIGNATION_2);
                if(strText != null)
                {
	                ti = new TextInfo(strText, 0, 0, _modifierFont);
	                labelWidth = Math.round(ti.getTextBounds().width());
	
	                //points
	                x = bounds.left + (int) (bounds.width() * 0.5);
	                x = x - (int) (labelWidth * 0.5);
	                //y = bounds.y + (bounds.height * 0.5);
	
	                y = (int) ((bounds.height() * 0.60));//633333333
	                y = bounds.top + y;
	
	                ti.setLocation(x, y);
	                arrMods.add(ti);
                }
            }

        }

        // </editor-fold>
        // <editor-fold defaultstate="collapsed" desc="Shift Points and Draw">
        Rect modifierBounds = null;
        if (arrMods != null && arrMods.size() > 0)
        {

            //build modifier bounds/////////////////////////////////////////
            modifierBounds = arrMods.get(0).getTextOutlineBounds();
            int size = arrMods.size();
            TextInfo tempShape = null;
            for (int i = 1; i < size; i++)
            {
                tempShape = arrMods.get(i);
                modifierBounds.union(tempShape.getTextOutlineBounds());
            }

        }

        if (modifierBounds != null)
        {

            imageBounds.union(modifierBounds);

            //shift points if needed////////////////////////////////////////
            if (imageBounds.left < 0 || imageBounds.top < 0)
            {
                int shiftX = Math.abs(imageBounds.left);
                int shiftY = Math.abs(imageBounds.top);

                //shift mobility points
                int size = arrMods.size();
                TextInfo tempShape = null;
                for (int i = 0; i < size; i++)
                {
                    tempShape = arrMods.get(i);
                    tempShape.shift(shiftX, shiftY);
                }
                modifierBounds.offset(shiftX, shiftY);

                //shift image points
                centerPoint.offset(shiftX, shiftY);
                symbolBounds.offset(shiftX, shiftY);
                imageBounds.offset(shiftX, shiftY);
            }

            //Render modifiers//////////////////////////////////////////////////
            Bitmap bmp = Bitmap.createBitmap(imageBounds.width(), imageBounds.height(), Config.ARGB_8888);
            Canvas ctx = new Canvas(bmp);

            //draw original icon with potential modifiers.
            ctx.drawBitmap(ii.getImage(), symbolBounds.left, symbolBounds.top, null);
            //ctx.drawImage(ii.getImage(),imageBoundsOld.left,imageBoundsOld.top);

            if (attributes.indexOfKey(MilStdAttributes.TextColor) >= 0)
            {
                textColor = SymbolUtilities.getColorFromHexString(attributes.get(MilStdAttributes.TextColor));
                if(alpha > -1)
                    textColor.setAlpha(alpha);
            }
            if (attributes.indexOfKey(MilStdAttributes.TextBackgroundColor) >= 0)
            {
                textBackgroundColor = SymbolUtilities.getColorFromHexString(attributes.get(MilStdAttributes.TextBackgroundColor));
                if(alpha > -1)
                    textBackgroundColor.setAlpha(alpha);
            }


            renderText(ctx, arrMods, textColor, textBackgroundColor);

            newii = new ImageInfo(bmp, centerPoint, symbolBounds);

            // <editor-fold defaultstate="collapsed" desc="Cleanup">
            ctx = null;

            // </editor-fold>
        }
        // </editor-fold>
        return newii;

    }

    private static void renderText(Canvas ctx, ArrayList<TextInfo> tiArray, Color color, Color backgroundColor)
    {
        ModifierRenderer.renderText(ctx, (TextInfo[]) tiArray.toArray(new TextInfo[0]), color, backgroundColor);
    }

    /**
     * 
     * @param ctx
     * @param tiArray
     * @param color
     * @param backgroundColor 
     */
    public static void renderText(Canvas ctx, TextInfo[] tiArray, Color color, Color backgroundColor)
    {
        /*for (TextInfo textInfo : tiArray) 
         {
         ctx.drawText(textInfo.getText(), textInfo.getLocation().x, textInfo.getLocation().y, _modifierFont);	
         }*/

        int size = tiArray.length;

        int tbm = RS.getTextBackgroundMethod();
        int outlineWidth = RS.getTextOutlineWidth();

        if (color != null)
        {
            _modifierFont.setColor(color.toInt());
        }
        else
        {
            _modifierFont.setColor(Color.BLACK.toInt());
            //_modifierFont.setColor(RS.getLabelForegroundColor());
        }

        Color outlineColor = null;
        
        if(backgroundColor != null)
            outlineColor = backgroundColor;
        else
            outlineColor = RendererUtilities.getIdealOutlineColor(color);

        if (tbm == RendererSettings.TextBackgroundMethod_OUTLINE_QUICK)
        {
            //draw text outline
            _modifierFont.setStyle(Style.FILL);
            _modifierFont.setStrokeWidth(RS.getTextOutlineWidth());
            _modifierFont.setColor(outlineColor.toInt());
            if (outlineWidth > 0)
            {
                for (int i = 0; i < size; i++)
                {
                    TextInfo textInfo = tiArray[i];
                    if (outlineWidth > 0)
                    {
                        for (int j = 1; j <= outlineWidth; j++)
                        {
                            if (j % 2 == 1)
                            {
                                ctx.drawText(textInfo.getText(), textInfo.getLocation().x - j, textInfo.getLocation().y, _modifierFont);
                                ctx.drawText(textInfo.getText(), textInfo.getLocation().x + j, textInfo.getLocation().y, _modifierFont);
                                ctx.drawText(textInfo.getText(), textInfo.getLocation().x - j, textInfo.getLocation().y + j, _modifierFont);
                                ctx.drawText(textInfo.getText(), textInfo.getLocation().x - j, textInfo.getLocation().y - j, _modifierFont);
                            }
                            else
                            {
                                ctx.drawText(textInfo.getText(), textInfo.getLocation().x - j, textInfo.getLocation().y - j, _modifierFont);
                                ctx.drawText(textInfo.getText(), textInfo.getLocation().x + j, textInfo.getLocation().y - j, _modifierFont);
                                ctx.drawText(textInfo.getText(), textInfo.getLocation().x - j, textInfo.getLocation().y + j, _modifierFont);
                                ctx.drawText(textInfo.getText(), textInfo.getLocation().x + j, textInfo.getLocation().y + j, _modifierFont);
                            }

                        }

                    }

                }
            }
            //draw text
            _modifierFont.setColor(color.toInt());

            for (int j = 0; j < size; j++)
            {
                TextInfo textInfo = tiArray[j];
                ctx.drawText(textInfo.getText(), textInfo.getLocation().x, textInfo.getLocation().y, _modifierFont);
                /*Paint outline = new Paint();
                 outline.setStyle(Style.STROKE);
                 outline.setColor(Color.red.toInt());
                 outline.setAlpha(155);
                 outline.setStrokeWidth(1f);
                 ctx.drawRect(textInfo.getTextBounds(), outline);
                 outline.setColor(Color.blue.toInt());
                 ctx.drawRect(textInfo.getTextOutlineBounds(), outline);//*/
            }
        }
        else if (tbm == RendererSettings.TextBackgroundMethod_OUTLINE)
        {

        	//draw text outline
            //draw text outline
            _modifierFont.setStyle(Style.STROKE);
            _modifierFont.setStrokeWidth(RS.getTextOutlineWidth());
            _modifierFont.setColor(outlineColor.toInt());
            if (outlineWidth > 0)
            {
                for (int i = 0; i < size; i++)
                {
                    TextInfo textInfo = tiArray[i];
                    ctx.drawText(textInfo.getText(), textInfo.getLocation().x, textInfo.getLocation().y, _modifierFont);
                }
            }
            //draw text
            _modifierFont.setColor(color.toInt());
            _modifierFont.setStyle(Style.FILL);
            for (int j = 0; j < size; j++)
            {
                TextInfo textInfo = tiArray[j];
                ctx.drawText(textInfo.getText(), textInfo.getLocation().x, textInfo.getLocation().y, _modifierFont);
            }
        }
        else if (tbm == RendererSettings.TextBackgroundMethod_COLORFILL)
        {
            Paint rectFill = new Paint();
            rectFill.setStyle(Paint.Style.FILL);
            rectFill.setColor(outlineColor.toARGB());
            
            
            //draw rectangle
            for (int k = 0; k < size; k++)
            {
                TextInfo textInfo = tiArray[k];
                ctx.drawRect(textInfo.getTextOutlineBounds(), rectFill);
            }
            //draw text
            _modifierFont.setColor(color.toInt());
            _modifierFont.setStyle(Style.FILL);
            for (int j = 0; j < size; j++)
            {
                TextInfo textInfo = tiArray[j];
                ctx.drawText(textInfo.getText(), textInfo.getLocation().x, textInfo.getLocation().y, _modifierFont);
            }
        }
        else if (tbm == RendererSettings.TextBackgroundMethod_NONE)
        {
            _modifierFont.setColor(color.toInt());
            _modifierFont.setStyle(Style.FILL);
            for (int j = 0; j < size; j++)
            {
                TextInfo textInfo = tiArray[j];
                ctx.drawText(textInfo.getText(), textInfo.getLocation().x, textInfo.getLocation().y, _modifierFont);
            }
        }
    }

    public static boolean hasDisplayModifiers(String symbolID, SparseArray<String> modifiers)
    {
        boolean hasModifiers = false;
        char scheme = symbolID.charAt(0);
        char status = symbolID.charAt(3);
        char affiliation = symbolID.charAt(1);
        if (scheme != 'W')
        {
            if (scheme != 'G' && (SymbolUtilities.isEMSNaturalEvent(symbolID) == false))
            {
                switch (status)
                {
                    case 'C':
                    case 'D':
                    case 'X':
                    case 'F':
                        hasModifiers = true;

                    default:
                        break;
                }

                if ((symbolID.substring(10, 12).equals("--") == false && symbolID.substring(10, 12).equals("**") == false) || modifiers.indexOfKey(ModifiersUnits.Q_DIRECTION_OF_MOVEMENT) >= 0)
                {
                    hasModifiers = true;
                }

                if(SymbolUtilities.isHQ(symbolID))
                    hasModifiers = true;
            }
            else
            {
                if (SymbolUtilities.isNBC(symbolID) == true && modifiers.indexOfKey(ModifiersTG.Q_DIRECTION_OF_MOVEMENT) >= 0)
                {
                    hasModifiers = true;
                }
            }

        }

        return hasModifiers;
    }

    public static boolean hasTextModifiers(String symbolID, SparseArray<String> modifiers, SparseArray<String> attributes)
    {

        int symStd = RS.getSymbologyStandard();
        if (attributes.indexOfKey(MilStdAttributes.SymbologyStandard) >= 0)
        {
            symStd = Integer.parseInt(attributes.get(MilStdAttributes.SymbologyStandard));
        }
        char scheme = symbolID.charAt(0);
        if(scheme == 'W')
        {
            if(symbolID.equals("WAS-WSF-LVP----") || //freezing level
                    symbolID.equals("WAS-PHT---P----") || //tropopause high
                    symbolID.equals("WAS-PLT---P----") || //tropopause low
                    symbolID.equals("WAS-WST-LVP----")) ////tropopause level
                return true;
            else
                return false;
        }
        if (scheme == 'G')
        {
            if (modifiers.indexOfKey(ModifiersTG.Q_DIRECTION_OF_MOVEMENT) >= 0)
            {
                if (modifiers.size() > 1)
                {
                    return true;
                }
            }
            else if (modifiers.size() > 0)
            {
                return true;
            }

        }
        else if (SymbolUtilities.isEMSNaturalEvent(symbolID) == false)
        {

            if (SymbolUtilities.getUnitAffiliationModifier(symbolID, symStd) != null)
            {
                return true;
            }

            if (SymbolUtilities.hasValidCountryCode(symbolID))
            {
                return true;
            }

            if (SymbolUtilities.isEMSNaturalEvent(symbolID))
            {
                return false;
            }

            if (modifiers.indexOfKey(ModifiersUnits.Q_DIRECTION_OF_MOVEMENT) >= 0)
            {
                if (modifiers.size() > 1)
                {
                    return true;
                }
            }
            else if (modifiers.size() > 0)
            {
                return true;
            }
        }
        return false;
    }

}
