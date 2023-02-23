package armyc2.c2sd.renderer.utilities;


import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;

import java.util.ArrayList;

import armyc2.c2sd.graphics2d.Point2D;

/**
 *
 * @author Mike Spinelli
 */
public class SymbolUtilitiesD {


    public static Boolean hasModifier(String symbolID, String modifier)
    {
        MSInfo msi = MSLookup.getInstance().getMSLInfo(symbolID);

        ArrayList<String> modifiers = msi.getModifiers();

        if(ModifiersD.GetModifierList().contains(modifier) &&  //If modifier is a valid modifier
            modifiers.contains(modifier)) //and modifier is in the list of modifiers
            return true;//return true
        else
            return false;

    }

    private static String convert(int integer)
    {
        String hexAlphabet = "0123456789ABCDEF";
        String foo = "gfds" + "dhs";
        char char1 =  hexAlphabet.charAt((integer - integer % 16)/16);
        char char2 = hexAlphabet.charAt(integer % 16);
        String returnVal = String.valueOf(char1) + String.valueOf(char2);
        return returnVal;
    }
    public static String colorToHexString(Color color, Boolean withAlpha)
    {
        String hex = "";
        if(withAlpha == false)
        {
            hex = "#" + convert(color.getRed()) +
                    convert(color.getGreen()) +
                    convert(color.getBlue());
        }
        else
        {
            hex = "#" + convert(color.getAlpha()) +
                    convert(color.getRed()) +
                    convert(color.getGreen()) +
                    convert(color.getBlue());
        }
        return hex;
    }

    /**
     * Gets line color used if no line color has been set. The color is specified based on the affiliation of
     * the symbol and whether it is a unit or not.
     * @param symbolID
     * @return
     */
    public static Color getLineColorOfAffiliation(String symbolID)
    {
        Color retColor = null;

        int symbolSet = SymbolID.getSymbolSet(symbolID);
        int set = SymbolID.getSymbolSet(symbolID);
        int affiliation = SymbolID.getAffiliation(symbolID);

        try
        {
            // We can't get the fill color if there is no symbol id, since that also means there is no affiliation
            if((symbolID == null) || (symbolID.equals("")))
            {
                return retColor;
            }

            if(symbolSet == SymbolID.SymbolSet_ControlMeasure)
            {
                int entity = SymbolID.getEntity(symbolID);
                int entityType = SymbolID.getEntityType(symbolID);
                int entitySubtype = SymbolID.getEntitySubtype(symbolID);

                //Protection Graphics, some are green obstacles and we need to
                //check for those.
                if(entity >= 27 && entity <= 29)
                {
                    //check for NBC, then:
                    //(basicSymbolID.equals("G*M*NR----****X")==true || //Radioactive Area
                    // basicSymbolID.equals("G*M*NC----****X")==true || //Chemically Contaminated Area
                    //basicSymbolID.equals("G*M*NB----****X")==true)) //Biologically Contaminated Area

                    if(SymbolUtilitiesD.isGreenProtectionGraphic(entity, entityType, entitySubtype))
                        retColor = Color.GREEN;
                }
                switch(affiliation)
                {
                    case SymbolID.StandardIdentity_Affiliation_Friend:
                    case SymbolID.StandardIdentity_Affiliation_AssumedFriend:
                        retColor = Color.BLACK;//0x000000;	// Black
                        break;
                    case SymbolID.StandardIdentity_Affiliation_Hostile_Faker:
                    case SymbolID.StandardIdentity_Affiliation_Suspect_Joker:
                        retColor = Color.RED;//0xff0000;	// Red
                        break;
                    case SymbolID.StandardIdentity_Affiliation_Neutral:
                        retColor = Color.GREEN;//0x00ff00;	// Green
                        break;
                    default:
                        retColor = Color.YELLOW;//0xffff00;	// Yellow
                        break;
                }
            }
            else if (set >= 45 && set <= 47)//METOC
            {
                //getLineColor for weather
            }
            else//everything else
            {
                //stopped doing check because all warfighting
                //should have black for line color.
                retColor = Color.BLACK;
            }
        }
        catch(Exception e)
        {
            // Log Error
            ErrorLogger.LogException("SymbolUtilties", "getLineColorOfAffiliation", e);
            //throw e;
        }	// End catch
        return retColor;
    }	// End get LineColorOfAffiliation

    public static Color getFillColorOfAffiliation(String symbolID)
    {
        Color retColor = null;
        int entityCode = SymbolID.getEntityCode(symbolID);
        int entity = SymbolID.getEntity(symbolID);
        int entityType = SymbolID.getEntityType(symbolID);
        int entitySubtype = SymbolID.getEntitySubtype(symbolID);

        int affiliation = SymbolID.getAffiliation(symbolID);

        try
        {
            // We can't get the fill color if there is no symbol id, since that also means there is no affiliation
            if ((symbolID == null) || (symbolID.equals("")))
            {
                return retColor;
            }
            //CBRN check 2717## - 2720## 271700 <= entityCode < 272100
            if (entityCode >= 271700 && entityCode <= 272100)
            {
                retColor = AffiliationColors.UnknownUnitFillColor;//  Color.yellow;
            }
            else if (SymbolID.isTacticalGraphic(symbolID) && !SymbolUtilities.isTGSPWithFill(symbolID))
            {
                switch(affiliation)
                {
                    case SymbolID.StandardIdentity_Affiliation_Friend:
                    case SymbolID.StandardIdentity_Affiliation_AssumedFriend:
                        retColor = AffiliationColors.FriendlyGraphicFillColor;//0x00ffff;	// Cyan
                        break;
                    case SymbolID.StandardIdentity_Affiliation_Hostile_Faker:
                    case SymbolID.StandardIdentity_Affiliation_Suspect_Joker:
                        retColor = AffiliationColors.HostileGraphicFillColor;//0xfa8072;	// Salmon
                        break;
                    case SymbolID.StandardIdentity_Affiliation_Neutral:
                        retColor = AffiliationColors.NeutralGraphicFillColor;//0x7fff00;	// Light Green
                        break;
                    default://unknown, pending, everything else
                        retColor = new Color(255, 250, 205); //0xfffacd;	// LemonChiffon 255 250 205
                        break;
                }
            } // End if(SymbolUtilities.IsTacticalGraphic(this._strSymbolID))
            else
            {
                switch(affiliation)
                {
                    case SymbolID.StandardIdentity_Affiliation_Friend:
                    case SymbolID.StandardIdentity_Affiliation_AssumedFriend:
                        retColor = AffiliationColors.FriendlyUnitFillColor;//0x00ffff;	// Cyan
                        break;
                    case SymbolID.StandardIdentity_Affiliation_Hostile_Faker:
                    case SymbolID.StandardIdentity_Affiliation_Suspect_Joker:
                        retColor = AffiliationColors.HostileUnitFillColor;//0xfa8072;	// Salmon
                        break;
                    case SymbolID.StandardIdentity_Affiliation_Neutral:
                        retColor = AffiliationColors.NeutralUnitFillColor;//0x7fff00;	// Light Green
                        break;
                    default://unknown, pending, everything else
                        retColor = AffiliationColors.UnknownUnitFillColor;//new Color(255,250, 205); //0xfffacd;	// LemonChiffon 255 250 205
                        break;
                }
            }	// End else
        } // End try
        catch (Exception e)
        {
            // Log Error
            ErrorLogger.LogException("SymbolUtilties", "getFillColorOfAffiliation", e);
            //throw e;
        }	// End catch

        return retColor;
    }	// End FillColorOfAffiliation

    /**
     * For Renderer Use Only
     * Assumes a fresh SVG String from the SVGLookup with its default values
     * @param symbolID
     * @param svg
     * @param strokeColor hex value like "#FF0000";
     * @param fillColor hex value like "#FF0000";
     * @return SVG String
     */
    public static String setSVGFrameColors(String symbolID, String svg, String strokeColor, String fillColor)
    {
        String returnSVG = null;
        int affiliation = SymbolID.getAffiliation(symbolID);
        String defaultFillColor = null;
        if(strokeColor != null)
        {
            returnSVG = svg.replaceAll("#000000","strokeColor");
        }
        if(fillColor != null)
        {
            switch(affiliation)
            {
                case SymbolID.StandardIdentity_Affiliation_Friend:
                case SymbolID.StandardIdentity_Affiliation_AssumedFriend:
                    defaultFillColor = "fill=\"#80E0FF\"";//friendly frame fill
                    break;
                case SymbolID.StandardIdentity_Affiliation_Hostile_Faker:
                case SymbolID.StandardIdentity_Affiliation_Suspect_Joker:
                    defaultFillColor = "fill=\"#FF8080\"";//hostile frame fill
                    break;
                case SymbolID.StandardIdentity_Affiliation_Unknown:
                case SymbolID.StandardIdentity_Affiliation_Pending:
                    defaultFillColor = "fill=\"#FFFF80\"";//unknown frame fill
                    break;
                case SymbolID.StandardIdentity_Affiliation_Neutral:
                    defaultFillColor = "fill=\"#AAFFAA\"";//neutral frame fill
                    break;
                default:
                    defaultFillColor = "fill=\"#80E0FF\"";//friendly frame fill
                    break;
            }

            if(returnSVG == null)
                return svg.replaceFirst(defaultFillColor, "fill=\"" + fillColor + "\"");
            else
                return returnSVG.replaceFirst(defaultFillColor, "fill=\"" + fillColor + "\"");
        }

        if(returnSVG != null)
            return returnSVG;
        else
            return svg;
    }

    /***
     *
     * @param entity
     * @param entityType
     * @param entitySubtype
     * @return
     */
    public static boolean isGreenProtectionGraphic(int entity, int entityType, int entitySubtype)
    {
        if(entity >= 27 && entity <= 29)//Protection Areas, Points and Lines
        {
            if(entity == 27)
            {
                if(entityType > 0 && entityType < 5 || entityType == 12)
                    return true;
                else if(entityType == 7 && entitySubtype > 2 && entitySubtype < 5)
                {
                    return true;
                }
                else if(entityType >= 8 && entityType <= 10)
                {
                    return true;
                }
                else
                    return false;
            }
            else if(entity == 28)
            {
                if(entityType > 0 && entityType <= 7)
                    return true;
                if(entityType == 19)
                    return true;
                else
                    return false;
            }
            else if(entity == 29)
            {
                if(entityType > 01 && entityType < 05)
                    return true;
                else
                    return false;
            }

        }
        else
        {
            return false;
        }
        return false;
    }

    /**
     * Checks if this is a single point control measure graphic with a unique layout.
     * Basically anything that's not an action point style graphic with modifiers
     * @param symbolID
     * @return
     */
    public static boolean isSPCMWithSpecialModifierLayout(String symbolID)
    {
        int ss = SymbolID.getSymbolSet(symbolID);
        int ec = SymbolID.getEntityCode(symbolID);

        if(ss == SymbolID.SymbolSet_ControlMeasure)
        {
            switch(ec)
            {
                case 130500: //Control Point
                case 130700: //Decision Point
                case 131300: //Point of Interest
                case 131800: //Waypoint
                case 131900: //Airfield (AEGIS Only)
                case 132000: //Target Handover
                case 132100: //Key Terrain
                case 160300: //Target Point Reference
                case 180100: //Air Control Point
                case 180200: //Communications Check Point
                case 180600: //TACAN
                case 210300: //Defended Asset
                case 210600: //Air Detonation
                case 210800: //Impact Point
                case 211000: //Launched Torpedo
                case 212800: //Harbor
                case 213500: //Sonobuoy
                case 213501: //Ambient Noise Sonobuoy
                case 213502: //Air Transportable Communication (ATAC) (Sonobuoy)
                case 213503: //Barra (Sonobuoy)
                case 213505:
                case 213506:
                case 213507:
                case 213508:
                case 213509:
                case 213510:
                case 213511:
                case 213512:
                case 213513:
                case 213514:
                case 213515:
                case 214900: //General Sea Subsurface Station
                case 215600: //General Sea Station
                case 217000: //Shore Control Station
                case 240601: //Point or Single Target
                case 240602: //Nuclear Target
                case 240900: //Fire Support Station
                case 250600: //Known Point
                case 270701: //Static Depiction
                case 282001: //Tower, Low
                case 282002: //Tower, High
                case 281300: //Chemical Event
                case 281400: //Biological Event
                case 281500: //Nuclear Event
                case 281600: //Nuclear Fallout Producing Event
                case 281700: //Radiological Event
                    return true;
                default:
                    return false;
            }
        }
        return false;
    }

    public static Point getCMSymbolAnchorPoint(String symbolID, RectF bounds)
    {

        PointF center = new PointF();
        double centerX = bounds.width()/2;
        double centerY = bounds.height()/2;

        int ss = SymbolID.getSymbolSet(symbolID);
        int ec = SymbolID.getEntityCode(symbolID);
        MSInfo msi = null;
        int drawRule = 0;

        //center/anchor point is always half width and half height except for control measures
        //and meteorological
        if(ss == SymbolID.SymbolSet_ControlMeasure)
        {
            drawRule = MSLookup.getInstance().getMSLInfo(symbolID).getDrawRule();
            switch(drawRule)//here we check the 'Y' value for the anchor point
            {
                case DrawRules.POINT1://action points //bottom center
                case DrawRules.POINT5://entry point
                case DrawRules.POINT6://ground zero
                case DrawRules.POINT7://missile detection point
                    centerY = bounds.height();
                    break;
                case DrawRules.POINT4://drop point  //almost bottom and center
                    centerY = (bounds.height() * 0.87);//TODO: check!
                    break;
                case DrawRules.POINT10://Sonobuoy  //center of circle which isn't center of symbol
                    centerY = (bounds.height() * 0.75);//TODO: check!
                    break;
                case DrawRules.POINT13://booby trap  //almost bottom and center
                    centerY = (bounds.height() * 0.87);//TODO: check!
                    break;
                case DrawRules.POINT15://Marine Life  //center left
                    centerX = 0;
                    break;
                case DrawRules.POINT16://Tower  //circle at base of tower
                    centerY = (bounds.height() * 0.95);//TODO: check!
                    break;
                default:
            }

            switch (ec)
            //have to adjust center X as some graphics have integrated text outside the symbol
            {
                case 180400: //Pickup Point (PUP)
                    centerX = bounds.width() * 0.31;
                case 240900: //Fire Support Station
                    centerX = bounds.width() * 0.38;
            }
        }


        return new Point(Math.round(center.x),Math.round(center.y));
    }

    public static Point2D getCenterPoint(String symbolID, double width, double height, int drawRule)
    {
        Point2D center = null;

        Boolean hbc = false;
        double centerX = width/2;
        double centerY = height/2;
        double y = 0.5;
        int ss = SymbolID.getSymbolSet(symbolID);
        int code = SymbolID.getEntityCode(symbolID);
        int e = SymbolID.getEntity(symbolID);
        int et = SymbolID.getEntityType(symbolID);
        int est = SymbolID.getEntitySubtype(symbolID);

        //center/anchor point is always half width and half height except for control measures
        //and meteorological
        if(ss == SymbolID.SymbolSet_ControlMeasure)
        {

        }
        else if(ss == SymbolID.SymbolSet_Atmospheric ||
                ss == SymbolID.SymbolSet_Oceanographic ||
                ss == SymbolID.SymbolSet_MeteorologicalSpace)
        {

        }

        /*if(ss == SymbolID.SymbolSet_ControlMeasure)
        {
            if(e == 13)
            {
                if(et > 0 && et < 4 ||
                        et >= 8 && et <= 16)
                {
                    y = 1;
                    centerY = height;
                }

            }
            else if(e == 16)
            {
                if(et == 04)
                {
                    y = 1;
                    centerY = height;
                }
            }
            else if(e == 18)
            {
                if(et == 03)
                {
                    y = 1;
                    centerY = height;
                }
            }
            else if (e == 21 && et == 04)
                y = 0.9;
            else if(e == 21)
            {
                if(et == 04 || //drop point isn't all the way to the bottom
                        et == 05 ||// entry point
                        et == 07 ||//ground zero
                        (et >= 10 && et <= 12) ||//
                        et == 14 || //brief contact
                        (et >= 20 && et <= 22))//
                {
                    y = 1;
                    centerY = height;
                }
                else if(et == 35)//(e == 21 && et == 35)//sonobuoy
                {
                    y = 0.8;
                    centerY = height * y;
                }
                else if((et >= 80 && et <= 82) || et == 87 || et == 88)
                {
                    y = 1;
                    centerY = height;
                }
                else if(et == 89)
                    centerX = 0;
                else if(et == 90)
                {
                    y = 0.8;
                    centerY = height * y;
                }
            }
            else if (e == 25)
            {
                y = 1;
                centerY = height;
            }
            else if (e == 28)
            {
                y = 1;
                centerY = height;
            }
            else if (e == 32)
            {
                y = 1;
                centerY = height;
            }
        }//*/

        center = new Point2D.Double(centerX, centerY);
        return center;
    }



    /**
     *
     * @param drawRule - Like DrawRules.CIRCULAR2
     * @return int[] where the first index is the minimum required points and
     * the next index is the maximum allowed points
     */
    public static int[] getMinMaxPointsFromDrawRule(int drawRule)
    {
        int[] points = {1,1};

        switch(drawRule)
        {
            case DrawRules.AREA1:
            case DrawRules.AREA2:
            case DrawRules.AREA3:
            case DrawRules.AREA4:
            case DrawRules.AREA9:
            case DrawRules.AREA20:
            case DrawRules.AREA23:
                points[0] = 3;
                points[1] = Integer.MAX_VALUE;
                break;
            case DrawRules.AREA5:
            case DrawRules.AREA7:
            case DrawRules.AREA11:
            case DrawRules.AREA12:
            case DrawRules.AREA17:
            case DrawRules.AREA21:
            case DrawRules.AREA24:
            case DrawRules.AREA25:
            case DrawRules.POINT12:
            case DrawRules.LINE3:
            case DrawRules.LINE6://doesn't seem to be used
            case DrawRules.LINE10:
            case DrawRules.LINE12:
            case DrawRules.LINE17:
            case DrawRules.LINE22:
            case DrawRules.LINE23:
            case DrawRules.LINE24:
            case DrawRules.LINE27:
            case DrawRules.LINE29://Ambush
            case DrawRules.POLYLINE1:
                points[0] = 3;
                points[1] = 3;
                break;
            case DrawRules.AREA6:
            case DrawRules.AREA13:
            case DrawRules.AREA15:
            case DrawRules.AREA16:
            case DrawRules.AREA19:
            case DrawRules.LINE4:
            case DrawRules.LINE5:
            case DrawRules.LINE9:
            case DrawRules.LINE14:
            case DrawRules.LINE15:
            case DrawRules.LINE18:
            case DrawRules.LINE19:
            case DrawRules.LINE20:
            case DrawRules.LINE25:
            case DrawRules.LINE28:
            case DrawRules.RECTANGULAR1://requires AM
            case DrawRules.RECTANGULAR3://requires AM
                points[0] = 2;
                points[1] = 2;
                break;
            case DrawRules.AREA8:
            case DrawRules.AREA18:
            case DrawRules.LINE11:
            case DrawRules.LINE16:
                points[0] = 4;
                points[1] = 4;
                break;
            case DrawRules.AREA10:
                points[0] = 3;
                points[1] = 6;
                break;
            case DrawRules.AREA14:
            case DrawRules.LINE1:
            case DrawRules.LINE2:
            case DrawRules.LINE7:
            case DrawRules.LINE13:
            case DrawRules.LINE21:
            case DrawRules.CORRIDOR1://Airspace Control Corridors
                points[0] = 2;
                points[1] = Integer.MAX_VALUE;
                break;
            case DrawRules.AREA26:
                //Min 6, no Max but number of points has to be even
                points[0] = 6;
                points[1] = Integer.MAX_VALUE;
                break;
            case DrawRules.LINE8:
                points[0] = 2;
                points[1] = 300;
                break;
            case DrawRules.LINE26:
                points[0] = 3;
                points[1] = 4;
                break;
            case DrawRules.AXIS1:
            case DrawRules.AXIS2:
                points[0] = 3;
                points[1] = 50;
                break;
            case 0://do not draw
                points[0] = 0;
                points[1] = 0;
                break;
            //Rest are single points
            case DrawRules.AREA22://Basic Defense Zone (BDZ) requires AM for radius
            case DrawRules.POINT17://requires AM & AM1
            case DrawRules.POINT18://requires AM & AN values
            case DrawRules.ELLIPSE1://required AM, AM1, AN
            case DrawRules.RECTANGULAR2://requires AM, AM1, AN
            default:
        }

        return points;
    }

    public static int[] getMinMaxPointsFromMODrawRule(int drawRule)
    {
        int[] points = {1,1};

        switch(drawRule)
        {
            case MODrawRules.AREA1:
            case MODrawRules.AREA2:
                points[0]=3;
                points[1]=Integer.MAX_VALUE;
                break;
            case MODrawRules.POINT5:
                points[0]=2;
                points[1]=2;
                break;
            case MODrawRules.LINE1:
            case MODrawRules.LINE2:
            case MODrawRules.LINE3:
            case MODrawRules.LINE4:
            case MODrawRules.LINE6:
            case MODrawRules.LINE7:
            case MODrawRules.LINE8:
                points[0]=2;
                points[1]=Integer.MAX_VALUE;
                break;
            case MODrawRules.LINE5:
                points[0]=3;
                points[1]=Integer.MAX_VALUE;
                break;
            case 0://do not draw
                points[0] = 0;
                points[1] = 0;
                break;
            //Rest are single points
            default:

        }

        return points;
    }


    public static Boolean isInstallation(String symbolID)
    {
        int ss = SymbolID.getSymbolSet(symbolID);
        int entity = SymbolID.getEntity(symbolID);
        if(ss == SymbolID.SymbolSet_LandInstallation && entity == 11)
            return true;
        else
            return false;
    }
    // </editor-fold>


    /**
     * Reads the Symbol ID string and returns the text that represents the echelon
     * code.
     * @param echelon
     * @return
     */
    public static String getEchelonText(int echelon)
    {
        char[] dots = new char[3];
        dots[0] = (char)8226;
        dots[1] = (char)8226;
        dots[2] = (char)8226;
        String dot = new String(dots);
        String text = null;
        if(echelon == SymbolID.Echelon_Team_Crew)
        {
            text = "0";
        }
        else if(echelon == SymbolID.Echelon_Squad)
        {
            text = dot.substring(0, 1);
        }
        else if(echelon == SymbolID.Echelon_Section)
        {
            text = dot.substring(0, 2);
        }
        else if(echelon == SymbolID.Echelon_Platoon_Detachment)
        {
            text = dot;
        }
        else if(echelon == SymbolID.Echelon_Company_Battery_Troop)
        {
            text = "|";
        }
        else if(echelon == SymbolID.Echelon_Battalion_Squadron)
        {
            text = "||";
        }
        else if(echelon == SymbolID.Echelon_Regiment_Group)
        {
            text = "|||";
        }
        else if(echelon == SymbolID.Echelon_Brigade)
        {
            text = "X";
        }
        else if(echelon == SymbolID.Echelon_Division)
        {
            text = "XX";
        }
        else if(echelon == SymbolID.Echelon_Corps_MEF)
        {
            text = "XXX";
        }
        else if(echelon == SymbolID.Echelon_Army)
        {
            text = "XXXX";
        }
        else if(echelon == SymbolID.Echelon_ArmyGroup_Front)
        {
            text = "XXXXX";
        }
        else if(echelon == SymbolID.Echelon_Region_Theater)
        {
            text = "XXXXXX";
        }
        else if(echelon == SymbolID.Echelon_Region_Command)
        {
            text = "++";
        }
        return text;
    }

}

