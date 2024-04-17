/*
 * A class to serve JavaRendererServer
 */
package armyc2.c2sd.JavaRendererServer.RenderMultipoints;

import armyc2.c2sd.renderer.utilities.*;
import armyc2.c2sd.JavaLineArray.CELineArray;
import armyc2.c2sd.JavaLineArray.POINT2;

import java.util.ArrayList;
import armyc2.c2sd.JavaLineArray.Shape2;
import armyc2.c2sd.JavaLineArray.TacticalLines;
import armyc2.c2sd.JavaLineArray.lineutility;
import armyc2.c2sd.JavaTacticalRenderer.TGLight;
import armyc2.c2sd.JavaTacticalRenderer.Modifier2;
import armyc2.c2sd.JavaTacticalRenderer.clsMETOC;
import armyc2.c2sd.JavaTacticalRenderer.mdlGeodesic;
//import armyc2.c2sd.JavaTacticalRenderer.clsUtility;
import armyc2.c2sd.graphics2d.*;
import android.content.Context;
import android.util.SparseArray;

/**
 * Rendering class
 *
 * @author Michael Deutch
 */
public final class clsRenderer {

    private static final String _className = "clsRenderer";

    /**
     * Set tg geo points from the client points
     *
     * @param milStd
     * @param tg
     */
    private static void setClientCoords(MilStdSymbol milStd,
            TGLight tg) {
        try {
            ArrayList<POINT2> latLongs = new ArrayList();
            int j = 0;
            ArrayList<Point2D> coords = milStd.getCoordinates();
            Point2D pt2d = null;
            POINT2 pt2 = null;
            int n = coords.size();
            //for (j = 0; j < coords.size(); j++) 
            for (j = 0; j < n; j++) {
                pt2d = coords.get(j);
                pt2 = clsUtility.Point2DToPOINT2(pt2d);
                latLongs.add(pt2);
            }
            tg.set_LatLongs(latLongs);
        } catch (Exception exc) {
            ErrorLogger.LogException("clsRenderer", "setClientCoords",
                    new RendererException("Failed to set geo points or pixels for " + milStd.getSymbolID(), exc));
        }
    }

    private static ArrayList<Point2D> getClientCoords(TGLight tg) {
        ArrayList<Point2D> coords = null;
        try {
            int j = 0;
            Point2D pt2d = null;
            POINT2 pt2 = null;
            coords = new ArrayList();
            int n = tg.LatLongs.size();
            //for (j = 0; j < tg.LatLongs.size(); j++) 
            for (j = 0; j < n; j++) {
                pt2 = tg.LatLongs.get(j);
                pt2d = new Point2D.Double(pt2.x, pt2.y);
                coords.add(pt2d);
            }
        } catch (Exception exc) {
            ErrorLogger.LogException("clsRenderer", "getClientCoords",
                    new RendererException("Failed to set geo points or pixels for " + tg.get_SymbolId(), exc));
        }
        return coords;
    }

    /**
     * Create MilStdSymbol from tactical graphic
     *
     * @deprecated
     * @param tg tactical graphic
     * @param converter geographic to pixels to converter
     * @return MilstdSymbol object
     */
    public static MilStdSymbol createMilStdSymboFromTGLight(TGLight tg, IPointConversion converter) {
        MilStdSymbol milStd = null;
        try {
            String symbolId = tg.get_SymbolId();
            int std = tg.getSymbologyStandard();
            armyc2.c2sd.JavaTacticalRenderer.clsUtility.initializeLinetypes(std);
            int lineType = armyc2.c2sd.JavaTacticalRenderer.clsUtility.GetLinetypeFromString(symbolId);
            String status = tg.get_Status();
            if (status != null && status.equals("A")) {
                if (armyc2.c2sd.JavaTacticalRenderer.clsUtility.isBasicShape(lineType) == false) {
                }
            }
            //build tg.Pixels
            tg.Pixels = clsUtility.LatLongToPixels(tg.LatLongs, converter);
            boolean isClosedArea = armyc2.c2sd.JavaTacticalRenderer.clsUtility.isClosedPolygon(lineType);
            if (isClosedArea) {
                armyc2.c2sd.JavaTacticalRenderer.clsUtility.ClosePolygon(tg.Pixels);
                armyc2.c2sd.JavaTacticalRenderer.clsUtility.ClosePolygon(tg.LatLongs);
            }

            ArrayList<Point2D> coords = getClientCoords(tg);
            tg.set_Font(new Font("Arial", Font.PLAIN, 12));
            SparseArray<String> modifiers = new SparseArray<String>();
            modifiers.put(ModifiersTG.W_DTG_1, tg.get_DTG());
            modifiers.put(ModifiersTG.W1_DTG_2, tg.get_DTG1());
            modifiers.put(ModifiersTG.H_ADDITIONAL_INFO_1, tg.get_H());
            modifiers.put(ModifiersTG.H1_ADDITIONAL_INFO_2, tg.get_H1());
            modifiers.put(ModifiersTG.H2_ADDITIONAL_INFO_3, tg.get_H2());
            modifiers.put(ModifiersTG.T_UNIQUE_DESIGNATION_1, tg.get_Name());
            modifiers.put(ModifiersTG.T1_UNIQUE_DESIGNATION_2, tg.get_T1());
            modifiers.put(ModifiersTG.Y_LOCATION, tg.get_Location());
            modifiers.put(ModifiersTG.N_HOSTILE, tg.get_N());

            milStd = new MilStdSymbol(symbolId, "1", coords, modifiers);
            milStd.setFillColor(tg.get_FillColor());
            milStd.setLineColor(tg.get_LineColor());
            milStd.setLineWidth(tg.get_LineThickness());
            milStd.setFillStyle(tg.get_TexturePaint());
        } catch (Exception exc) {
            ErrorLogger.LogException("clsRenderer", "createMilStdSymboFromTGLight",
                    new RendererException("Failed to set geo points or pixels for " + tg.get_SymbolId(), exc));
        }
        return milStd;
    }

    /**
     * Build a tactical graphic object from the client MilStdSymbol
     *
     * @param milStd MilstdSymbol object
     * @param converter geographic to pixels converter
     * @return tactical graphic
     */
    public static TGLight createTGLightFromMilStdSymbol(MilStdSymbol milStd,
            IPointConversion converter) {
        TGLight tg = new TGLight();
        try {
            String symbolId = milStd.getSymbolID();
            int std = milStd.getSymbologyStandard();
            tg.setSymbologyStandard(std);
            armyc2.c2sd.JavaTacticalRenderer.clsUtility.initializeLinetypes(std);
            tg.set_SymbolId(symbolId);
            boolean useLineInterpolation = milStd.getUseLineInterpolation();
            tg.set_UseLineInterpolation(useLineInterpolation);
            //int lineType = armyc2.c2sd.JavaTacticalRenderer.clsUtility.GetLinetypeFromString(symbolId);
            int lineType = getRevDLinetype(tg);
            tg.set_LineType(lineType);
            String status = tg.get_Status();
            if (status != null && status.equals("A")) {
                if (armyc2.c2sd.JavaTacticalRenderer.clsUtility.isBasicShape(lineType) == false) {
                    tg.set_LineStyle(1);
                }
            }
            tg.set_VisibleModifiers(true);
            //set tg latlongs and pixels
            setClientCoords(milStd, tg);
            //build tg.Pixels
            tg.Pixels = clsUtility.LatLongToPixels(tg.LatLongs, converter);
            //tg.set_Font(new Font("Arial", Font.PLAIN, 12));
            RendererSettings r = RendererSettings.getInstance();
            int type = r.getMPModifierFontType();
            String name = r.getMPModifierFontName();
            int sz = r.getMPModifierFontSize();
            Font font = new Font(name, type, sz);
            tg.set_Font(font);
            tg.set_FillColor(milStd.getFillColor());
            tg.set_LineColor(milStd.getLineColor());
            tg.set_LineThickness(milStd.getLineWidth());
            tg.set_TexturePaint(milStd.getFillStyle());
            if(armyc2.c2sd.JavaTacticalRenderer.clsUtility.isBasicShape(lineType))            
                tg.set_Fillstyle(milStd.getPatternFillType());            
            tg.set_FontBackColor(Color.WHITE);
            tg.set_TextColor(tg.get_LineColor());
            if (milStd.getModifier(ModifiersTG.W_DTG_1) != null) {
                tg.set_DTG(milStd.getModifier(ModifiersTG.W_DTG_1));
            }
            if (milStd.getModifier(ModifiersTG.W1_DTG_2) != null) {
                tg.set_DTG1(milStd.getModifier(ModifiersTG.W1_DTG_2));
            }
            if (milStd.getModifier(ModifiersTG.H_ADDITIONAL_INFO_1) != null) {
                tg.set_H(milStd.getModifier(ModifiersTG.H_ADDITIONAL_INFO_1));
            }
            if (milStd.getModifier(ModifiersTG.H1_ADDITIONAL_INFO_2) != null) {
                tg.set_H1(milStd.getModifier(ModifiersTG.H1_ADDITIONAL_INFO_2));
            }
            if (milStd.getModifier(ModifiersTG.H2_ADDITIONAL_INFO_3) != null) {
                tg.set_H2(milStd.getModifier(ModifiersTG.H2_ADDITIONAL_INFO_3));
            }
            if (milStd.getModifier(ModifiersTG.T_UNIQUE_DESIGNATION_1) != null) {
                tg.set_Name(milStd.getModifier(ModifiersTG.T_UNIQUE_DESIGNATION_1));
            }
            if (milStd.getModifier(ModifiersTG.T1_UNIQUE_DESIGNATION_2) != null) {
                tg.set_T1(milStd.getModifier(ModifiersTG.T1_UNIQUE_DESIGNATION_2));
            }
            if (milStd.getModifier(ModifiersTG.Y_LOCATION) != null) {
                tg.set_Location(milStd.getModifier(ModifiersTG.Y_LOCATION));
            }
            if (milStd.getModifier(ModifiersTG.N_HOSTILE) != null) {
                tg.set_N(milStd.getModifier(ModifiersTG.N_HOSTILE));
            }
            tg.set_UseDashArray(milStd.getUseDashArray());
            tg.set_UseHatchFill(milStd.getUseFillPattern());
            //tg.set_UsePatternFill(milStd.getUseFillPattern());
            tg.set_HideOptionalLabels(milStd.getHideOptionalLabels());
            boolean isClosedArea = armyc2.c2sd.JavaTacticalRenderer.clsUtility.isClosedPolygon(lineType);

            //implement two point belt
            if(lineType==TacticalLines.BELT && tg.Pixels.size()==2)
            {
                POINT2 pt0=tg.Pixels.get(0);
                POINT2 pt1=tg.Pixels.get(1);
                POINT2 p0=lineutility.ExtendDirectedLine(pt0,pt1,pt0,2,5);
                POINT2 p1=lineutility.ExtendDirectedLine(pt0,pt1,pt1,2,5);
                POINT2 p2=lineutility.ExtendDirectedLine(pt0,pt1,pt1,3,5);
                POINT2 p3=lineutility.ExtendDirectedLine(pt0,pt1,pt0,3,5);
                tg.Pixels.clear();
                tg.Pixels.add(p0);
                tg.Pixels.add(p1);
                tg.Pixels.add(p2);
                tg.Pixels.add(p3);
                tg.LatLongs = clsUtility.PixelsToLatLong(tg.Pixels, converter);            
            }            
            if (isClosedArea) {
                armyc2.c2sd.JavaTacticalRenderer.clsUtility.ClosePolygon(tg.Pixels);
                armyc2.c2sd.JavaTacticalRenderer.clsUtility.ClosePolygon(tg.LatLongs);
            }

            //implement meters to feet for altitude labels
            String altitudeLabel = milStd.getAltitudeMode();
            if (altitudeLabel == null || altitudeLabel.isEmpty()) {
                altitudeLabel = "MSL";
            }
            DistanceUnit altitudeUnit = milStd.getAltitudeUnit();
            if(altitudeUnit == null){
                altitudeUnit = DistanceUnit.FEET;
            }
            DistanceUnit distanceUnit = milStd.getDistanceUnit();
            if(distanceUnit == null){
                distanceUnit = DistanceUnit.METERS;
            }

            double x_alt = 0;
            int n_alt = 0;
            String strXAlt = "";
            //construct the H1 and H2 modifiers for sector from the mss AM, AN, and X arraylists
            if (lineType == TacticalLines.BS_ELLIPSE || lineType == TacticalLines.PBS_ELLIPSE || lineType == TacticalLines.PBS_CIRCLE) {
                ArrayList<Double> AM = milStd.getModifiers_AM_AN_X(ModifiersTG.AM_DISTANCE);
                ArrayList<Double> AN = milStd.getModifiers_AM_AN_X(ModifiersTG.AN_AZIMUTH);
                //ensure array length 3
                double r2=0;
                double b=0;
                if(AM.size()==1)
                {
                    r2=AM.get(0);
                    AM.add(r2);                
                    AM.add(0d);
                }
                else if(AM.size()==2)
                {
                    r2=AM.get(0);
                    b=AM.get(1);
                    AM.set(1, r2);
                    AM.add(b);
                }
                if (AN == null) {
                    AN = new ArrayList<Double>();
                }
                if (AN.size() < 1) {
                    AN.add(new Double(0));
                }
                if (lineType == TacticalLines.PBS_CIRCLE) //circle
                {
                    double am0 = AM.get(0);
                    if (AM.size() == 1) {
                        AM.add(am0);
                    } else if (AM.size() >= 2) {
                        AM.set(1, am0);
                    }
                }
                if (AM != null && AM.size() >= 2 && AN != null && AN.size() >= 1) {
                    POINT2 ptAzimuth = new POINT2(0, 0);
                    ptAzimuth.x = AN.get(0);
                    POINT2 ptCenter = tg.Pixels.get(0);
                    POINT2 pt0 = mdlGeodesic.geodesic_coordinate(tg.LatLongs.get(0), AM.get(0), 90);//semi-major axis
                    POINT2 pt1 = mdlGeodesic.geodesic_coordinate(tg.LatLongs.get(0), AM.get(1), 0);//semi-minor axis
                    Point2D pt02d = new Point2D.Double(pt0.x, pt0.y);
                    Point2D pt12d = new Point2D.Double(pt1.x, pt1.y);
                    pt02d = converter.GeoToPixels(pt02d);
                    pt12d = converter.GeoToPixels(pt12d);
                    pt0 = new POINT2(pt02d.getX(), pt02d.getY());
                    pt1 = new POINT2(pt12d.getX(), pt12d.getY());
                    tg.Pixels = new ArrayList<POINT2>();
                    tg.Pixels.add(ptCenter);
                    tg.Pixels.add(pt0);
                    tg.Pixels.add(pt1);
                    tg.Pixels.add(ptAzimuth);
                }
                if(AM != null && AM.size()>2)
                {
                    //use AM[2] for the buffer, so PBS_CIRCLE requires AM size 3 like PBS_ELLIPSE to use a buffer
                    double dist=AM.get(2);
                    POINT2 pt0=mdlGeodesic.geodesic_coordinate(tg.LatLongs.get(0), dist, 45);   //azimuth 45 is arbitrary
                    Point2D pt02d = new Point2D.Double(tg.LatLongs.get(0).x,tg.LatLongs.get(0).y);
                    Point2D pt12d = new Point2D.Double(pt0.x, pt0.y);
                    pt02d = converter.GeoToPixels(pt02d);
                    pt12d = converter.GeoToPixels(pt12d);
                    pt0=new POINT2(pt02d.getX(),pt02d.getY());
                    POINT2 pt1=new POINT2(pt12d.getX(),pt12d.getY());                   
                    dist=lineutility.CalcDistanceDouble(pt0, pt1);
                    //arraysupport will use line style to create the buffer shape
                    tg.Pixels.get(0).style=(int)dist;
                }
            }
            if (lineType == TacticalLines.RANGE_FAN_SECTOR) {
                ArrayList<Double> AM = milStd.getModifiers_AM_AN_X(ModifiersTG.AM_DISTANCE);
                ArrayList<Double> AN = milStd.getModifiers_AM_AN_X(ModifiersTG.AN_AZIMUTH);
                ArrayList<Double> X = milStd.getModifiers_AM_AN_X(ModifiersTG.X_ALTITUDE_DEPTH);
                if (AM != null) {
                    String strT1 = "";
                    for (int j = 0; j < AM.size(); j++) {
                        strT1 += Double.toString(AM.get(j));
                        if (j < AM.size() - 1) {
                            strT1 += ",";
                        }
                    }
                    tg.set_T1(strT1);
                }
                if (AN != null) {
                    String strT = "";
                    //String az = "";
                    for (int j = 0; j < AN.size(); j++) {
                        strT += AN.get(j);
                        if (j < AN.size() - 1) {
                            strT += ",";
                        }
                    }
                    tg.set_Name(strT);
                }
                if (X != null) {
                    String strH1 = "";
                    for (int j = 0; j < X.size(); j++) {
                        //strH1 += Double.toString(X.get(j));
                        x_alt = X.get(j);
                        //strXAlt=Double.toString(x_alt)+" ft. "+altitudeLabel;
                        x_alt *= 10.0;
                        x_alt = Math.round(x_alt);
                        n_alt = (int) x_alt;
                        x_alt = n_alt / 10.0;
                        strXAlt = createAltitudeLabel(x_alt, altitudeUnit, altitudeLabel);
                        strH1 += strXAlt;

                        if (j < X.size() - 1) {
                            strH1 += ",";
                        }
                    }
                    tg.set_H1(strH1);
                }
                if (AM != null && AN != null) {
                    int numSectors = AN.size() / 2;
                    double left, right, min = 0, max = 0;
                    //construct left,right,min,max from the arraylists
                    String strLeftRightMinMax = "";
                    for (int j = 0; j < numSectors; j++) {
                        left = AN.get(2 * j);
                        right = AN.get(2 * j + 1);
                        if (j + 1 == AM.size()) {
                            break;
                        }
                        min = AM.get(j);
                        max = AM.get(j + 1);
                        strLeftRightMinMax += Double.toString(left) + "," + Double.toString(right) + "," + Double.toString(min) + "," + Double.toString(max);
                        if (j < numSectors - 1) {
                            strLeftRightMinMax += ",";
                        }

                    }
                    int len = strLeftRightMinMax.length();
                    String c = strLeftRightMinMax.substring(len - 1, len);
                    if (c.equalsIgnoreCase(",")) {
                        strLeftRightMinMax = strLeftRightMinMax.substring(0, len - 1);
                    }
                    tg.set_H2(strLeftRightMinMax);
                }
            }
            int j = 0;
            if (lineType == TacticalLines.BBS_RECTANGLE || lineType == TacticalLines.BS_BBOX) {
                double minLat = tg.LatLongs.get(0).y;
                double maxLat = tg.LatLongs.get(0).y;
                double minLong = tg.LatLongs.get(0).x;
                double maxLong = tg.LatLongs.get(0).x;
                for (j = 1; j < tg.LatLongs.size(); j++) {
                    if (tg.LatLongs.get(j).x < minLong) {
                        minLong = tg.LatLongs.get(j).x;
                    }
                    if (tg.LatLongs.get(j).x > maxLong) {
                        maxLong = tg.LatLongs.get(j).x;
                    }
                    if (tg.LatLongs.get(j).y < minLat) {
                        minLat = tg.LatLongs.get(j).y;
                    }
                    if (tg.LatLongs.get(j).y > maxLat) {
                        maxLat = tg.LatLongs.get(j).y;
                    }
                }
                tg.LatLongs = new ArrayList();
                tg.LatLongs.add(new POINT2(minLong, maxLat));
                tg.LatLongs.add(new POINT2(maxLong, maxLat));
                tg.LatLongs.add(new POINT2(maxLong, minLat));
                tg.LatLongs.add(new POINT2(minLong, minLat));
                if (lineType == TacticalLines.BS_BBOX) {
                    tg.LatLongs.add(new POINT2(minLong, maxLat));
                }
                tg.Pixels = clsUtility.LatLongToPixels(tg.LatLongs, converter);
            }
            //these have a buffer value in meters which we'll stuff tg.H2
            //and use the style member of tg.Pixels to stuff the buffer width in pixels
            switch (lineType) {
                case TacticalLines.BBS_AREA:
                case TacticalLines.BBS_LINE:
                case TacticalLines.BBS_RECTANGLE:
                    String H2 = null;
                    double dist = 0;
                    POINT2 pt0;
                    POINT2 pt1;//45 is arbitrary
                    ArrayList<Double> AM = milStd.getModifiers_AM_AN_X(ModifiersTG.AM_DISTANCE);
                    if (AM != null && AM.size() > 0) {
                        H2 = AM.get(0).toString();
                        tg.set_H2(H2);
                    }
                    if (H2 != null && !H2.isEmpty()) {
                        for (j = 0; j < tg.LatLongs.size(); j++) {
                            if (tg.LatLongs.size() > j) {
                                if (!Double.isNaN(Double.parseDouble(H2))) {
                                    if (j == 0) {
                                        dist = Double.parseDouble(H2);
                                        pt0 = new POINT2(tg.LatLongs.get(0));
                                        pt1 = mdlGeodesic.geodesic_coordinate(pt0, dist, 45);//45 is arbitrary
                                        Point2D pt02d = new Point2D.Double(pt0.x, pt0.y);
                                        Point2D pt12d = new Point2D.Double(pt1.x, pt1.y);
                                        pt02d = converter.GeoToPixels(pt02d);
                                        pt12d = converter.GeoToPixels(pt12d);
                                        pt0.x = pt02d.getX();
                                        pt0.y = pt02d.getY();
                                        pt1.x = pt12d.getX();
                                        pt1.y = pt12d.getY();
                                        dist = lineutility.CalcDistanceDouble(pt0, pt1);
                                    }
                                    tg.Pixels.get(j).style = Math.round((float) dist);
                                } else {
                                    tg.Pixels.get(j).style = 0;
                                }
                            }
                        }
                    }
                    break;
                default:
                    break;
            }
            if (lineType == TacticalLines.LAUNCH_AREA) //geo ellipse
            {
                ArrayList<Double> AM = milStd.getModifiers_AM_AN_X(ModifiersTG.AM_DISTANCE);
                ArrayList<Double> AN = milStd.getModifiers_AM_AN_X(ModifiersTG.AN_AZIMUTH);
                if (AM != null && AM.size() > 1) {
                    String H = AM.get(0).toString();  //major axis
                    tg.set_H(H);
                    String T1 = AM.get(1).toString(); //minor axis
                    tg.set_T1(T1);
                }
                if (AN != null && AN.size() > 0) {
                    String H2 = AN.get(0).toString(); //rotation
                    tg.set_H2(H2);
                }
            }
            switch (lineType) {
                case TacticalLines.ROZ:
                case TacticalLines.FAADZ:
                case TacticalLines.HIDACZ:
                case TacticalLines.MEZ:
                case TacticalLines.LOMEZ:
                case TacticalLines.HIMEZ:
                case TacticalLines.ACA:
                case TacticalLines.ACA_RECTANGULAR:
                case TacticalLines.ACA_CIRCULAR:
                    ArrayList<Double> X = milStd.getModifiers_AM_AN_X(ModifiersTG.X_ALTITUDE_DEPTH);
                    if (X != null && X.size() > 0) {
                        //tg.set_H(Double.toString(X.get(0)));
                        x_alt = X.get(0);
                        //strXAlt=Double.toString(x_alt)+" ft. "+altitudeLabel;
                        x_alt *= 10.0;
                        x_alt = Math.round(x_alt);
                        n_alt = (int) x_alt;
                        x_alt = n_alt / 10.0;
                        strXAlt = createAltitudeLabel(x_alt, altitudeUnit, altitudeLabel);
                        tg.set_H(strXAlt);
                    }
                    if (X != null && X.size() > 1) {
                        //tg.set_H1(Double.toString(X.get(1)));
                        x_alt = X.get(1);
                        //strXAlt=Double.toString(x_alt)+" ft. "+altitudeLabel;
                        x_alt *= 10.0;
                        x_alt = Math.round(x_alt);
                        n_alt = (int) x_alt;
                        x_alt = n_alt / 10.0;
                        strXAlt = createAltitudeLabel(x_alt, altitudeUnit, altitudeLabel);
                        tg.set_H1(strXAlt);
                    }
                    break;
                case TacticalLines.UAV:
                case TacticalLines.MRR:
                case TacticalLines.UAV_USAS:
                case TacticalLines.MRR_USAS:
                case TacticalLines.LLTR:
                case TacticalLines.AC:
                case TacticalLines.SAAFR:
                    POINT2 pt = tg.LatLongs.get(0);
                    Point2D pt2d0 = new Point2D.Double(pt.x, pt.y);
                    Point2D pt2d0Pixels = converter.GeoToPixels(pt2d0);
                    POINT2 pt0Pixels = new POINT2(pt2d0Pixels.getX(), pt2d0Pixels.getY());

                    //get some point 10000 meters away from pt
                    //10000 should work for any scale                    
                    double dist = 10000;
                    POINT2 pt2 = mdlGeodesic.geodesic_coordinate(pt, dist, 0);
                    Point2D pt2d1 = new Point2D.Double(pt2.x, pt2.y);
                    Point2D pt2d1Pixels = converter.GeoToPixels(pt2d1);
                    POINT2 pt1Pixels = new POINT2(pt2d1Pixels.getX(), pt2d1Pixels.getY());
                    //calculate pixels per meter
                    double distPixels = lineutility.CalcDistanceDouble(pt0Pixels, pt1Pixels);
                    double pixelsPerMeter = distPixels / dist;

                    ArrayList<Double> AM = milStd.getModifiers_AM_AN_X(ModifiersTG.AM_DISTANCE);
                    if (AM != null) {
                        String H2 = "";
                        for (j = 0; j < AM.size(); j++) {
                            H2 += AM.get(j).toString();
                            if (j < AM.size() - 1) {
                                H2 += ",";
                            }
                        }
                        tg.set_H2(H2);
                    }
                    String[] strRadii = null;
                    //get the widest vaule
                    //the current requirement is to use the greatest width as the default width
                    double maxWidth = 0,
                     temp = 0;
                    double maxWidthMeters = 0;
                    if (tg.get_H2() != null && tg.get_H2().isEmpty() == false) {
                        strRadii = tg.get_H2().split(",");
                        if (strRadii != null && strRadii.length > 0) {
                            for (j = 0; j < strRadii.length; j++) {
                                if (!Double.isNaN(Double.parseDouble(strRadii[j]))) {
                                    temp = Double.parseDouble(strRadii[j]);
                                    if (temp > maxWidth) {
                                        maxWidth = temp;
                                    }
                                }
                            }
                            maxWidthMeters = maxWidth;
                            maxWidth *= pixelsPerMeter / 2;
                        }
                    }
                    //double defaultPixels=maxWidth;
                    //if AM is null we default to using the value from H2 to set tg.Pixels.get(j).style                                   
                    //hopefully H2 was set, either by the client or by stuffing it from AM
                    if (tg.get_H2() != null && tg.get_H2().isEmpty() == false) {
                        if (strRadii != null && strRadii.length > 0) {
                            //assume it's a comma delimited string
                            //double pixels=Double.valueOf(tg.get_H2())*pixelsPerMeter;
                            double pixels = 0;
                            //defaultPixels=Double.valueOf(strRadii[0])*pixelsPerMeter/2;
                            //for(j=0;j<strRadii.length;j++)
                            for (j = 0; j < tg.Pixels.size(); j++) {
                                //pixels=defaultPixels;
                                if (tg.Pixels.size() > j) {
                                    if (strRadii.length > j) {
                                        if (!Double.isNaN(Double.parseDouble(strRadii[j]))) {
                                            pixels = Double.parseDouble(strRadii[j]) * pixelsPerMeter / 2;
                                            tg.Pixels.get(j).style = (int) pixels;
                                            tg.LatLongs.get(j).style = (int) pixels;
                                        } else {
                                            tg.Pixels.get(j).style = (int) maxWidth;
                                            tg.LatLongs.get(j).style = (int) maxWidth;
                                        }
                                    } else {
                                        tg.Pixels.get(j).style = (int) maxWidth;
                                        tg.LatLongs.get(j).style = (int) maxWidth;
                                    }
                                }
                            }
                        }
                    }

                    maxWidthMeters *= distanceUnit.conversionFactor;
                    maxWidthMeters *= 10.0;
                    maxWidthMeters = Math.round(maxWidthMeters);
                    int tempWidth = (int) maxWidthMeters;
                    maxWidthMeters = tempWidth / 10.0;

                    //now set tg.H2 to the max value so that the H2 modifier will display as the max vaule;
                    tg.set_H2(Double.toString(maxWidthMeters) + " " + distanceUnit.label);
                    //use X, X1 to set tg.H, tg.H1
                    X = milStd.getModifiers_AM_AN_X(ModifiersTG.X_ALTITUDE_DEPTH);
                    if (X != null && X.size() > 0) {
                        //tg.set_H(Double.toString(X.get(0)));
                        x_alt = X.get(0);
                        //strXAlt=Double.toString(x_alt)+" ft. "+altitudeLabel;
                        x_alt *= 10.0;
                        x_alt = Math.round(x_alt);
                        n_alt = (int) x_alt;
                        x_alt = n_alt / 10.0;
                        strXAlt = createAltitudeLabel(x_alt, altitudeUnit, altitudeLabel);
                        tg.set_H(strXAlt);
                    }
                    if (X != null && X.size() > 1) {
                        //tg.set_H1(Double.toString(X.get(1)));
                        x_alt = X.get(1);
                        //strXAlt=Double.toString(x_alt)+" ft. "+altitudeLabel;
                        x_alt *= 10.0;
                        x_alt = Math.round(x_alt);
                        n_alt = (int) x_alt;
                        x_alt = n_alt / 10.0;
                        strXAlt = createAltitudeLabel(x_alt, altitudeUnit, altitudeLabel);
                        tg.set_H1(strXAlt);
                    }
                    break;
                default:
                    break;
            }
            //killl box purple evidently uses the X modifier (Rev C)
            switch (lineType) {
                case TacticalLines.KILLBOXPURPLE:
                case TacticalLines.KILLBOXPURPLE_CIRCULAR:
                case TacticalLines.KILLBOXPURPLE_RECTANGULAR:
                    ArrayList<Double> X = milStd.getModifiers_AM_AN_X(ModifiersTG.X_ALTITUDE_DEPTH);
                    String strH1 = "";
                    if (X != null && !X.isEmpty()) {
                        //strH1 = Double.toString(X.get(0));
                        //tg.set_H1(strH1);
                        x_alt = X.get(0);
                        //strXAlt=Double.toString(x_alt)+" ft. "+altitudeLabel;
                        x_alt *= 10.0;
                        x_alt = Math.round(x_alt);
                        n_alt = (int) x_alt;
                        x_alt = n_alt / 10.0;
                        strXAlt = createAltitudeLabel(x_alt, altitudeUnit, altitudeLabel);
                        tg.set_H1(strXAlt);
                    }
                    break;
                default:
                    break;
            }
            //circular range fans
            if (lineType == TacticalLines.RANGE_FAN) {
                ArrayList<Double> AM = milStd.getModifiers_AM_AN_X(ModifiersTG.AM_DISTANCE);
                ArrayList<Double> X = milStd.getModifiers_AM_AN_X(ModifiersTG.X_ALTITUDE_DEPTH);
                String strH2 = "";
                String strH1 = "";
                if (AM != null) {
                    for (j = 0; j < AM.size(); j++) {
                        strH2 += Double.toString(AM.get(j));
                        if (j < AM.size() - 1) {
                            strH2 += ",";
                        }

                        if (X != null && j < X.size()) {
                            //strH1 += Double.toString(X.get(j));
                            x_alt = X.get(j);
                            //strXAlt=Double.toString(x_alt)+" ft. "+altitudeLabel;
                            x_alt *= 10.0;
                            x_alt = Math.round(x_alt);
                            n_alt = (int) x_alt;
                            x_alt = n_alt / 10.0;
                            strXAlt = createAltitudeLabel(x_alt, altitudeUnit, altitudeLabel);
                            strH1 += strXAlt;
                            if (j < X.size() - 1) {
                                strH1 += ",";
                            }
                        }

                        //rev C has a maxiimum of 3 circles
                        if (j == 2) {
                            break;
                        }
                    }
                }
                tg.set_H2(strH2);
                tg.set_H1(strH1);
            }
            switch (lineType) {
                case TacticalLines.BBS_AREA:
                case TacticalLines.BBS_LINE:
                case TacticalLines.BBS_POINT:
                case TacticalLines.BBS_RECTANGLE:
                    if (tg.get_FillColor() == null) {
                        tg.set_FillColor(Color.LIGHT_GRAY);
                    }
                    break;
                default:
                    break;
            }
            //Mil-Std-2525C stuff
            switch (lineType) {
                case TacticalLines.PAA_RECTANGULAR_REVC:
                case TacticalLines.FSA_RECTANGULAR:
                case TacticalLines.FFA_RECTANGULAR:
                case TacticalLines.ACA_RECTANGULAR:
                case TacticalLines.NFA_RECTANGULAR:
                case TacticalLines.RFA_RECTANGULAR:
                case TacticalLines.ATI_RECTANGULAR:
                case TacticalLines.CFFZ_RECTANGULAR:
                case TacticalLines.SENSOR_RECTANGULAR:
                case TacticalLines.CENSOR_RECTANGULAR:
                case TacticalLines.DA_RECTANGULAR:
                case TacticalLines.CFZ_RECTANGULAR:
                case TacticalLines.ZOR_RECTANGULAR:
                case TacticalLines.TBA_RECTANGULAR:
                case TacticalLines.TVAR_RECTANGULAR:
                case TacticalLines.CIRCULAR:
                case TacticalLines.FSA_CIRCULAR:
                case TacticalLines.ACA_CIRCULAR:
                case TacticalLines.FFA_CIRCULAR:
                case TacticalLines.NFA_CIRCULAR:
                case TacticalLines.RFA_CIRCULAR:
                case TacticalLines.PAA_CIRCULAR:
                case TacticalLines.ATI_CIRCULAR:
                case TacticalLines.CFFZ_CIRCULAR:
                case TacticalLines.SENSOR_CIRCULAR:
                case TacticalLines.CENSOR_CIRCULAR:
                case TacticalLines.DA_CIRCULAR:
                case TacticalLines.CFZ_CIRCULAR:
                case TacticalLines.ZOR_CIRCULAR:
                case TacticalLines.TBA_CIRCULAR:
                case TacticalLines.TVAR_CIRCULAR:
                case TacticalLines.KILLBOXBLUE_CIRCULAR:
                case TacticalLines.KILLBOXPURPLE_CIRCULAR:
                case TacticalLines.KILLBOXBLUE_RECTANGULAR:
                case TacticalLines.KILLBOXPURPLE_RECTANGULAR:
                case TacticalLines.BBS_POINT:
                    ArrayList<Double> AM = milStd.getModifiers_AM_AN_X(ModifiersTG.AM_DISTANCE);
                    if (AM != null && AM.size() > 0) {
                        String strT1 = Double.toString(AM.get(0));
                        //set width for rectangles or radius for circles
                        tg.set_T1(strT1);
                    } else if (lineType == TacticalLines.BBS_POINT && tg.LatLongs.size() > 1) {
                        double dist = mdlGeodesic.geodesic_distance(tg.LatLongs.get(0), tg.LatLongs.get(1), null, null);
                        String strT1 = Double.toString(dist);
                        tg.set_T1(strT1);
                    }
                    break;
                default:
                    break;
            }
            //Mil-std-2525C
            if (lineType == TacticalLines.RECTANGULAR || lineType == TacticalLines.PBS_RECTANGLE || lineType == TacticalLines.PBS_SQUARE) {
                ArrayList<Double> AM = milStd.getModifiers_AM_AN_X(ModifiersTG.AM_DISTANCE);
                ArrayList<Double> AN = milStd.getModifiers_AM_AN_X(ModifiersTG.AN_AZIMUTH);
                if (lineType == TacticalLines.PBS_SQUARE) //for square
                {
                    double r2=AM.get(0);
                    double b=0;
                    if(AM.size()==1)
                    {
                        AM.add(r2);
                        AM.add(b);
                    }
                    else if(AM.size()==2)
                    {
                        b=AM.get(1);
                        AM.set(1,r2);
                        AM.add(b);
                    }
                    else if(AM.size()>2)
                        AM.set(1, r2);
                }
                //if all these conditions are not met we do not want to set any tg modifiers
                if (lineType == TacticalLines.PBS_SQUARE) //square
                {
                    double am0 = AM.get(0);
                    if (AM.size() == 1) {
                        AM.add(am0);
                    } else if (AM.size() >= 2) {
                        AM.set(1, am0);
                    }
                }
                if (AN == null) {
                    AN = new ArrayList();
                }

                if (AN.isEmpty()) {
                    AN.add(0d);
                }
                if (AM != null && AM.size() > 1 && AN != null && AN.size() > 0) {
                    String strT1 = Double.toString(AM.get(0));    //width
                    String strH = Double.toString(AM.get(1));     //length
                    //set width and length in meters for rectangular target
                    tg.set_T1(strT1);
                    tg.set_H(strH);
                    //set attitude in mils
                    String strH2 = Double.toString(AN.get(0));
                    tg.set_H2(strH2);
                }
                if(AM.size()>2)
                {
                    String strH1 = Double.toString(AM.get(2));     //buffer size
                    tg.set_H1(strH1);
                }
            }
            //set rev D properties
            //render_GE does this
            //setTGProperties(tg);
        } catch (Exception exc) {
            ErrorLogger.LogException("clsRenderer", "createTGLightfromMilStdSymbol",
                    new RendererException("Failed to build multipoint TG for " + milStd.getSymbolID(), exc));
        }
        return tg;
    }

    private static String createAltitudeLabel(double distance, DistanceUnit altitudeUnit, String altitudeLabel){
        // Truncate the result
        double result = distance * altitudeUnit.conversionFactor;
        result *= 10.0;
        result = Math.round(result);
        int tempResult = (int) result;
        result = tempResult / 10.0;

        return result + " " + altitudeUnit.label + " " + altitudeLabel;
    }

    /**
     * @deprecated @param milStd
     * @param converter
     * @param computeChannelPt
     * @return
     */
    public static TGLight createTGLightFromMilStdSymbol(MilStdSymbol milStd,
            IPointConversion converter, Boolean computeChannelPt) {
        TGLight tg = new TGLight();
        try {
            String symbolId = milStd.getSymbolID();
            tg.set_SymbolId(symbolId);
            String status = tg.get_Status();
            if (status != null && status.equals("A")) {
                //lineStyle=GraphicProperties.LINE_TYPE_DASHED;
                tg.set_LineStyle(1);
            }
            tg.set_VisibleModifiers(true);
            //set tg latlongs and pixels
            setClientCoords(milStd, tg);
            //build tg.Pixels
            tg.Pixels = clsUtility.LatLongToPixels(tg.LatLongs, converter);
            tg.set_Font(new Font("Arial", Font.PLAIN, 12));
            tg.set_FillColor(milStd.getFillColor());
            tg.set_LineColor(milStd.getLineColor());
            tg.set_LineThickness(milStd.getLineWidth());
            tg.set_TexturePaint(milStd.getFillStyle());
            tg.set_FontBackColor(Color.WHITE);
            tg.set_TextColor(tg.get_LineColor());

//            tg.set_DTG(milStd.getModifier(ModifiersTG.W_DTG_1));
//            tg.set_DTG1(milStd.getModifier(ModifiersTG.W1_DTG_2));
//            tg.set_H(milStd.getModifier(ModifiersTG.H_ADDITIONAL_INFO_1));
//            tg.set_H1(milStd.getModifier(ModifiersTG.H1_ADDITIONAL_INFO_2));
//            tg.set_H2(milStd.getModifier(ModifiersTG.H2_ADDITIONAL_INFO_3));
//            tg.set_Name(milStd.getModifier(ModifiersTG.T_UNIQUE_DESIGNATION_1));
//            tg.set_T1(milStd.getModifier(ModifiersTG.T1_UNIQUE_DESIGNATION_2));
//            tg.set_Location(milStd.getModifier(ModifiersTG.Y_LOCATION));
//            tg.set_N(ModifiersTG.N_HOSTILE);
            if (milStd.getModifier(ModifiersTG.W_DTG_1) != null) {
                tg.set_DTG(milStd.getModifier(ModifiersTG.W_DTG_1));
            }
            if (milStd.getModifier(ModifiersTG.W1_DTG_2) != null) {
                tg.set_DTG1(milStd.getModifier(ModifiersTG.W1_DTG_2));
            }
            if (milStd.getModifier(ModifiersTG.H_ADDITIONAL_INFO_1) != null) {
                tg.set_H(milStd.getModifier(ModifiersTG.H_ADDITIONAL_INFO_1));
            }
            if (milStd.getModifier(ModifiersTG.H1_ADDITIONAL_INFO_2) != null) {
                tg.set_H1(milStd.getModifier(ModifiersTG.H1_ADDITIONAL_INFO_2));
            }
            if (milStd.getModifier(ModifiersTG.H2_ADDITIONAL_INFO_3) != null) {
                tg.set_H2(milStd.getModifier(ModifiersTG.H2_ADDITIONAL_INFO_3));
            }
            if (milStd.getModifier(ModifiersTG.T_UNIQUE_DESIGNATION_1) != null) {
                tg.set_Name(milStd.getModifier(ModifiersTG.T_UNIQUE_DESIGNATION_1));
            }
            if (milStd.getModifier(ModifiersTG.T1_UNIQUE_DESIGNATION_2) != null) {
                tg.set_T1(milStd.getModifier(ModifiersTG.T1_UNIQUE_DESIGNATION_2));
            }
            if (milStd.getModifier(ModifiersTG.Y_LOCATION) != null) {
                tg.set_Location(milStd.getModifier(ModifiersTG.Y_LOCATION));
            }
            if (milStd.getModifier(ModifiersTG.N_HOSTILE) != null) {
                tg.set_N(milStd.getModifier(ModifiersTG.N_HOSTILE));
            }

            //int lineType=CELineArray.CGetLinetypeFromString(tg.get_SymbolId());
            //int rev=tg.getSymbologyStandard();
            int lineType = armyc2.c2sd.JavaTacticalRenderer.clsUtility.GetLinetypeFromString(symbolId);
            boolean isClosedArea = armyc2.c2sd.JavaTacticalRenderer.clsUtility.isClosedPolygon(lineType);

            if (isClosedArea) {
                armyc2.c2sd.JavaTacticalRenderer.clsUtility.ClosePolygon(tg.Pixels);
                armyc2.c2sd.JavaTacticalRenderer.clsUtility.ClosePolygon(tg.LatLongs);
            }

            //these channels need a channel point added
            if (computeChannelPt) {
                switch (lineType) {
                    case TacticalLines.CATK:
                    case TacticalLines.CATKBYFIRE:
                    case TacticalLines.AAFNT:
                    case TacticalLines.AAAAA:
                    case TacticalLines.AIRAOA:
                    case TacticalLines.MAIN:
                    case TacticalLines.SPT:
                    case TacticalLines.AXAD:
                        POINT2 ptPixels = armyc2.c2sd.JavaTacticalRenderer.clsUtility.ComputeLastPoint(tg.Pixels);
                        tg.Pixels.add(ptPixels);
                        //Point pt = clsUtility.POINT2ToPoint(ptPixels);
                        Point2D pt = new Point2D.Double(ptPixels.x, ptPixels.y);
                        //in case it needs the corresponding geo point
                        Point2D ptGeo2d = converter.PixelsToGeo(pt);
                        POINT2 ptGeo = clsUtility.Point2DToPOINT2(ptGeo2d);
                        tg.LatLongs.add(ptGeo);
                        //}
                        break;
                    default:
                        break;
                }
            }
        } catch (Exception exc) {
            ErrorLogger.LogException("clsRenderer", "createTGLightfromMilStdSymbol",
                    new RendererException("Failed to build multipoint TG for " + milStd.getSymbolID(), exc));
        }
        return tg;
    }

    /**
     * @desc Render the symbol to return the ShapeInfo data using JavaLineArray.
     * This is for the generic client.
     *
     * @param tg
     * @param converter
     * @param shapeInfos
     * @param modifierShapeInfos
     */
//    private static void GetLineArray(TGLight tg,
//            IPointConversion converter,
//            ArrayList<ShapeInfo> shapeInfos,
//            ArrayList<ShapeInfo> modifierShapeInfos) {
//        try {
//            ArrayList<Shape2> shapes = new ArrayList();//ShapeInfoToShape2(shapeInfos);
//            ArrayList<Shape2> modifierShapes = new ArrayList();//ShapeInfoToShape2(shapeInfos);
//            int lineType = tg.get_LineType();
//            int minPoints2 = armyc2.c2sd.JavaTacticalRenderer.clsUtility.GetMinPoints(lineType);
//            ref<int[]> minPoints = new ref();
//            ArrayList<POINT2> channelPoints = new ArrayList();
//            boolean bolChange1 = armyc2.c2sd.JavaTacticalRenderer.clsUtility.IsChange1Area(lineType, minPoints);
//            int bolMeTOC = armyc2.c2sd.JavaTacticalRenderer.clsMETOC.IsWeather(tg.get_SymbolId());
//
//            tg.modifiers = new ArrayList();
//            BufferedImage bi = new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);
//            Graphics2D g2d = bi.createGraphics();
//            //Modifier2.AddModifiers(tg,g2d,null);
//            Modifier2.AddModifiersGeo(tg, g2d, null, converter);
//            int rev = tg.getSymbologyStandard();
//            //Modifier2.AddModifiers(tg,g2d);//flipped only for 3d for change 1 symbols
//            Shape2 hatchShape = null;
//            if (converter == null) {
//                armyc2.c2sd.JavaTacticalRenderer.clsUtility.getHatchShape(tg, bi);
//            }
//
//            if (tg.Pixels.size() < minPoints2) {
//                lineType = TacticalLines.MIN_POINTS;
//                bolChange1 = false;
//            }
//
//            if (bolChange1) {
//                tg.Pixels.clear();
//                //fills tg.Pixels
//                bolChange1 = armyc2.c2sd.JavaRendererServer.RenderMultipoints.clsUtilityCPOF.Change1TacticalAreas(tg, lineType, converter, shapes);
//                //points = tg.Pixels;
//            } else if (bolMeTOC > 0) {
//                try {
//                    clsMETOC.GetMeTOCShape(tg, shapes, rev);
//                } catch (Exception ex) {
//                    armyc2.c2sd.JavaTacticalRenderer.clsUtility.WriteFile("Error in ClsMETOC.GetMeTOCShape");
//                }
//            } else {
//                if (CELineArray.CIsChannel(lineType) == 0) {
//                    if (lineType != TacticalLines.BELT1) {
//                        tg.Pixels = arraysupport.GetLineArray2(lineType, tg.Pixels, shapes, null, rev);
//                        Modifier2.GetIntegralTextShapes(tg, g2d, shapes);
//                    }
//                    //points = arraysupport.points;
//                    //tg.Pixels=points;
//                    if (lineType == TacticalLines.BELT1) {
//                        //get the partitions
//                        ArrayList<Shape2> tempShapes = null;
//                        ArrayList<P1> partitions = clsChannelUtility.GetPartitions2(tg);
//                        ArrayList<POINT2> pixels = null;
//                        int l = 0, k = 0;
//                        for (l = 0; l < partitions.size(); l++) {
//                            tempShapes = new ArrayList();
//                            pixels = new ArrayList();
//                            for (k = partitions.get(l).start; k <= partitions.get(l).end_Renamed + 1; k++) {
//                                pixels.add(tg.Pixels.get(k));
//                            }
//                            pixels = arraysupport.GetLineArray2(lineType, pixels, tempShapes, null, rev);
//                            shapes.addAll(tempShapes);
//                        }
//                    }
//                } else //channel type
//                {
//                    clsChannelUtility.DrawChannel(tg.Pixels, lineType, tg, shapes, channelPoints, rev);
//                    tg.Pixels = channelPoints;
//                }
//            }
//            //if(converter.get_BestFit()==true)
//            //{
//            //assumes tg.LatLongs is filled
//            //    tg.LatLongs=clsUtility.PixelsToLatLong(tg.Pixels, converter);
//            //}
//            //BufferedImage bi=new BufferedImage(8,8,BufferedImage.TYPE_INT_ARGB);
//            armyc2.c2sd.JavaTacticalRenderer.clsUtility.SetShapeProperties(tg, shapes, bi);
//
//            //at this point tg.Pixels has the points from CELineArray
//            //the following line adds modifiers for those sybmols which require
//            //the calculated points to use for the modifiers.
//            //currentlly only BLOCK and CONTAIN use tg.Pixels for computing
//            //the modifiers after the call to GetLineArray
//            //so points will usually have nothing in it
//            Modifier2.AddModifiers2(tg);
//            //BestFitModifiers(tg,converter);
//            //build the modifier shapes
//            if (hatchShape != null) {
//                shapes.add(hatchShape);
//            }
//
//            Shape2ToShapeInfo(shapeInfos, shapes);
//
//            if (modifierShapeInfos != null)//else client is not using shapes to display modifiers
//            {
//                //bi=new BufferedImage(10,10,BufferedImage.TYPE_INT_ARGB);
//                //Graphics2D g2d=bi.createGraphics();
//                Modifier2.DisplayModifiers2(tg, g2d, modifierShapes, false, converter);
//
//                //convert to ShapeInfo ArrayLists
//                Shape2ToShapeInfo(modifierShapeInfos, modifierShapes);
//                bi.flush();
//                g2d.dispose();
//                bi = null;
//                g2d = null;
//            }
//        } catch (Exception exc) {
//            ErrorLogger.LogException("clsRenderer", "GetLineArray",
//                    new RendererException("Points calculator failed for " + tg.get_SymbolId(), exc));
//        }
//    }
    private static void Shape2ToShapeInfo(ArrayList<ShapeInfo> shapeInfos, ArrayList<Shape2> shapes) {
        try {
            int j = 0;
            Shape2 shape = null;
            if (shapes == null || shapeInfos == null || shapes.size() == 0) {
                return;
            }

            for (j = 0; j < shapes.size(); j++) {
                shape = shapes.get(j);
                shapeInfos.add((ShapeInfo) shape);
            }
        } catch (Exception exc) {
            ErrorLogger.LogException("clsRenderer", "Shape2ToShapeInfo",
                    new RendererException("Failed to build ShapeInfo ArrayList", exc));
        }
    }

    /**
     * Added function to handle when coords or display area spans IDL but not
     * both, it prevents the symbol from rendering if the bounding rectangles
     * don't intersect.
     *
     * @param tg
     * @param converter
     * @param clipArea
     * @return
     */
    public static boolean intersectsClipArea(TGLight tg, IPointConversion converter, Object clipArea)
    {
        boolean result=false;
        try
        {
            if (clipArea==null || tg.LatLongs.size() < 2)
                return true;
            Rectangle2D clipBounds = null;
            ArrayList<Point2D> clipPoints = null;
            
//            if (clipArea != null) {
//                if (clipArea.getClass().isAssignableFrom(Rectangle2D.Double.class)) {
//                    clipBounds = (Rectangle2D.Double) clipArea;
//                } else if (clipArea.getClass().isAssignableFrom(Rectangle.class)) {
//                    clipBounds = (Rectangle2D) clipArea;
//                } else if (clipArea.getClass().isAssignableFrom(ArrayList.class)) {
//                    clipPoints = (ArrayList<Point2D>) clipArea;
//                }
//            }
            if (clipArea != null) {
                if (clipArea.getClass().isAssignableFrom(Rectangle2D.Double.class)) {
                    clipBounds = (Rectangle2D.Double) clipArea;
                } else if (clipArea.getClass().isAssignableFrom(Rectangle.class)) {
                    Rectangle rectx = (Rectangle) clipArea;
                    clipBounds = new Rectangle2D.Double(rectx.x, rectx.y, rectx.width, rectx.height);
                } else if (clipArea.getClass().isAssignableFrom(ArrayList.class)) {
                    clipPoints = (ArrayList<Point2D>) clipArea;                    
                    //double x0=clipPoints.get(0).getX(),y0=clipPoints.get(0).getY();
                    //double w=clipPoints.get(1).getX()-x0,h=clipPoints.get(3).getY()-y0;
                    //clipBounds = new Rectangle2D.Double(x0, y0, w, h);                    
                    clipBounds=clsUtility.getMBR(clipPoints);
                }
            }
            //assumes we are using clipBounds
            int j = 0;
            double x = clipBounds.getMinX();
            double y = clipBounds.getMinY();
            double width = clipBounds.getWidth();
            double height = clipBounds.getHeight();
            POINT2 tl = new POINT2(x, y);
            POINT2 br = new POINT2(x + width, y + height);
            tl = clsUtility.PointPixelsToLatLong(tl, converter);
            br = clsUtility.PointPixelsToLatLong(br, converter);
            //the latitude range
            //boolean ptInside = false, ptAbove = false, ptBelow = false;
            double coordsLeft = tg.LatLongs.get(0).x;
            double coordsRight = coordsLeft;
            double coordsTop=tg.LatLongs.get(0).y;
            double coordsBottom=coordsTop;
            boolean intersects=false;
            double minx=tg.LatLongs.get(0).x,maxx=minx,maxNegX=0;
            for (j = 0; j < tg.LatLongs.size(); j++)
            {                
                POINT2 pt=tg.LatLongs.get(j);
                if (pt.x < minx)
                    minx = pt.x;
                if (pt.x > maxx)
                    maxx = pt.x;
                if(maxNegX==0 && pt.x<0)
                    maxNegX=pt.x;
                if(maxNegX<0 && pt.x<0 && pt.x>maxNegX)
                    maxNegX=pt.x;
                if (pt.y < coordsBottom)
                    coordsBottom = pt.y;
                if (pt.y > coordsTop)
                    coordsTop = pt.y;                
            }
            boolean coordSpanIDL = false;
            if(maxx==180 || minx==-180)
                coordSpanIDL=true;
            if(maxx-minx>=180)
            {
                coordSpanIDL=true;
                coordsLeft=maxx;
                coordsRight=maxNegX;
            }else
            {
                coordsLeft=minx;
                coordsRight=maxx;
            }
            //if(canClipPoints)
            //{                
                if(br.y<=coordsBottom && coordsBottom <= tl.y)
                    intersects=true;
                else if(coordsBottom<=br.y && br.y <=coordsTop)
                    intersects=true;
                else
                    return false;
            //}
            //if it gets this far then the latitude ranges intersect
            //re-initialize intersects for the longitude ranges
            intersects=false;
            //the longitude range
            //the min and max coords longitude
            boolean boxSpanIDL = false;
            //boolean coordSpanIDL = false;
            if(tl.x==180 || tl.x==-180 || br.x==180 || br.x==-180)
                boxSpanIDL=true;
            else if (Math.abs(br.x - tl.x) > 180)
                boxSpanIDL = true;
            
//            if (coordsRight - coordsLeft > 180)
//            {
//                double temp = coordsLeft;
//                coordsLeft = coordsRight;
//                coordsRight = temp;
//                coordSpanIDL=true;
//            }
            //boolean intersects=false;
            if(coordSpanIDL && boxSpanIDL)
                intersects=true;
            else if(!coordSpanIDL && !boxSpanIDL)   //was && canclipPoints
            {
                if(coordsLeft<=tl.x && tl.x<=coordsRight)
                    intersects=true;
                if(coordsLeft<=br.x && br.x<=coordsRight)
                    intersects=true;
                if(tl.x<=coordsLeft && coordsLeft<=br.x)
                    intersects=true;
                if(tl.x<=coordsRight && coordsRight<=br.x)
                    intersects=true;
            }
            else if(!coordSpanIDL && boxSpanIDL)    //box spans IDL and coords do not
            {   
                if(tl.x<coordsRight && coordsRight<180)
                    intersects=true;
                if(-180<coordsLeft && coordsLeft<br.x)
                    intersects=true;
            }
            else if(coordSpanIDL && !boxSpanIDL)    //coords span IDL and box does not
            {   
                if(coordsLeft<br.x && br.x<180)
                    intersects=true;
                if(-180<tl.x && tl.x<coordsRight)
                    intersects=true;
            }
            return intersects;
            
        }
        catch (Exception exc) {
            ErrorLogger.LogException("clsRenderer", "intersectsClipArea",
                    new RendererException("Failed inside intersectsClipArea", exc));
        }    
        return result;
    }

    public static void renderWithPolylines(MilStdSymbol mss,
            IPointConversion converter,
            Object clipArea,
            Context context) {
        try {
            TGLight tg = clsRenderer.createTGLightFromMilStdSymbol(mss, converter);
            double scale = getScale(tg, converter, clipArea);
            ArrayList<ShapeInfo> shapeInfos = new ArrayList();
            ArrayList<ShapeInfo> modifierShapeInfos = new ArrayList();
            if (intersectsClipArea(tg, converter, clipArea)) {
                render_GE(tg, shapeInfos, modifierShapeInfos, converter, clipArea, context);
            }
            mss.setSymbolShapes(shapeInfos);
            mss.setModifierShapes(modifierShapeInfos);
        } catch (Exception exc) {
            ErrorLogger.LogException("clsRenderer", "renderWithPolylines",
                    new RendererException("Failed inside renderWithPolylines", exc));
        }
    }

    /**
     * GoogleEarth renderer uses polylines for rendering
     *
     * @param mss MilStdSymbol object
     * @param converter the geographic to pixels coordinate converter
     * @param clipArea the clip bounds
     */
    public static void renderWithPolylines(MilStdSymbol mss,
            IPointConversion converter,
            Object clipArea) {
        try {
            TGLight tg = clsRenderer.createTGLightFromMilStdSymbol(mss, converter);
            double scale = getScale(tg, converter, clipArea);
            ArrayList<ShapeInfo> shapeInfos = new ArrayList();
            ArrayList<ShapeInfo> modifierShapeInfos = new ArrayList();
            if (intersectsClipArea(tg, converter, clipArea)) {
                render_GE(tg, shapeInfos, modifierShapeInfos, converter, clipArea);
            }
            mss.setSymbolShapes(shapeInfos);
            mss.setModifierShapes(modifierShapeInfos);
            mss.set_WasClipped(tg.get_WasClipped());
        } catch (Exception exc) {
            ErrorLogger.LogException("clsRenderer", "renderWithPolylines",
                    new RendererException("Failed inside renderWithPolylines", exc));
        }
    }

    /**
     * See render_GE below for comments
     *
     * @param tg
     * @param shapeInfos
     * @param modifierShapeInfos
     * @param converter
     * @param clipArea
     * @param context test android-gradle
     */
    public static void render_GE(TGLight tg,
            ArrayList<ShapeInfo> shapeInfos,
            ArrayList<ShapeInfo> modifierShapeInfos,
            IPointConversion converter,
            Object clipArea,
            Context context) //was Rectangle2D
    {
        setTGProperties(tg);
        render_GE(tg, shapeInfos, modifierShapeInfos, converter, clipArea);
        //set the BitmapShader for the 0th shape
        if (shapeInfos != null && shapeInfos.size() > 0) {
            ShapeInfo shape = shapeInfos.get(0);
            clsUtility.createBitmapShader(tg, shape, context);
        }
//        if (shapeInfos != null && !shapeInfos.isEmpty() && !tg.get_UseHatchFill()) {
//            ShapeInfo shape = shapeInfos.get(0);
//            clsUtility.createBitmapShader(tg, shape, context);
//        }
    }

    /**
     * Google Earth renderer: Called by mapfragment-demo This is the public
     * interface for Google Earth renderer assumes tg.Pixels is filled assumes
     * the caller instantiated the ShapeInfo arrays
     *
     * @param tg tactical graphic
     * @param shapeInfos symbol ShapeInfo array
     * @param modifierShapeInfos modifier ShapeInfo array
     * @param converter geographic to pixels coordinate converter
     * @param clipArea clipping bounds in pixels
     */
    public static void render_GE(TGLight tg,
            ArrayList<ShapeInfo> shapeInfos,
            ArrayList<ShapeInfo> modifierShapeInfos,
            IPointConversion converter,
            Object clipArea) {
        try {
            //set linetype for overhead wire
            getScale(tg, converter, clipArea);

            Rectangle2D clipBounds = null;
            CELineArray.setClient("ge");
//            ArrayList<POINT2> origPixels = null;
//            ArrayList<POINT2> origLatLongs = null;
//            if (clsUtilityGE.segmentColorsSet(tg)) {
//                origPixels=lineutility.getDeepCopy(tg.Pixels);
//                origLatLongs=lineutility.getDeepCopy(tg.LatLongs);
//            }
            ArrayList<POINT2> origFillPixels = lineutility.getDeepCopy(tg.Pixels);

            if (tg.get_LineType() == TacticalLines.LC || tg.get_LineType() == TacticalLines.LC_HOSTILE)
                armyc2.c2sd.JavaTacticalRenderer.clsUtility.SegmentLCPoints(tg, converter);

//            boolean shiftLines = Channels.getShiftLines();
//            if (shiftLines) {
//                String affiliation = tg.get_Affiliation();
//                Channels.setAffiliation(affiliation);
//            }
            //CELineArray.setMinLength(2.5);    //2-27-2013
            ArrayList<Point2D> clipPoints = null;
            if (clipArea != null) {
                if (clipArea.getClass().isAssignableFrom(Rectangle2D.Double.class)) {
                    clipBounds = (Rectangle2D.Double) clipArea;
                } else if (clipArea.getClass().isAssignableFrom(Rectangle.class)) {
                    Rectangle rectx = (Rectangle) clipArea;
                    clipBounds = new Rectangle2D.Double(rectx.x, rectx.y, rectx.width, rectx.height);
                } else if (clipArea.getClass().isAssignableFrom(ArrayList.class)) {
                    clipPoints = (ArrayList<Point2D>) clipArea;
                }
            }
            double zoomFactor = clsUtilityGE.getZoomFactor(clipBounds, clipPoints, tg.Pixels);
            //add sub-section to test clipArea if client passes the rectangle
            boolean useClipPoints = false;    //currently not used
            if (useClipPoints == true && clipBounds != null) {
                double x = clipBounds.getMinX();
                double y = clipBounds.getMinY();
                double width = clipBounds.getWidth();
                double height = clipBounds.getHeight();
                clipPoints = new ArrayList();
                clipPoints.add(new Point2D.Double(x, y));
                clipPoints.add(new Point2D.Double(x + width, y));
                clipPoints.add(new Point2D.Double(x + width, y + height));
                clipPoints.add(new Point2D.Double(x, y + height));
                clipPoints.add(new Point2D.Double(x, y));
                clipBounds = null;
            }
            //end section

            if (tg.get_Client() == null || tg.get_Client().isEmpty()) {
                tg.set_client("ge");
            }

            armyc2.c2sd.JavaRendererServer.RenderMultipoints.clsUtility.RemoveDuplicatePoints(tg);

            int rev = RendererSettings.getInstance().getSymbologyStandard();
            armyc2.c2sd.JavaTacticalRenderer.clsUtility.initializeLinetypes(rev);
            armyc2.c2sd.JavaTacticalRenderer.clsUtility.setRevC(tg);

            int linetype = tg.get_LineType();
            if (linetype < 0) {
                linetype = armyc2.c2sd.JavaTacticalRenderer.clsUtility.GetLinetypeFromString(tg.get_SymbolId());
                //clsUtilityCPOF.SegmentGeoPoints(tg, converter);
                tg.set_LineType(linetype);
            }

            Boolean isTextFlipped = false;
            ArrayList<Shape2> shapes = null;   //use this to collect all the shapes
            clsUtilityGE.setSplineLinetype(tg);
            setHostileLC(tg);

            clsUtilityCPOF.SegmentGeoPoints(tg, converter, zoomFactor);
            if (clipBounds != null || clipPoints != null) {
                if (clsUtilityCPOF.canClipPoints(tg)) {
                    //check assignment
                    if (clipBounds != null) {
                        clsClipPolygon2.ClipPolygon(tg, clipBounds);
                    } else if (clipPoints != null) {
                        clsClipQuad.ClipPolygon(tg, clipPoints);
                    }

                    clsUtilityGE.removeTrailingPoints(tg, clipArea);
                    tg.LatLongs = clsUtility.PixelsToLatLong(tg.Pixels, converter);
                }
            }

            //if MSR segment data set use original pixels unless tg.Pixels is empty from clipping
//            if (origPixels != null) {
//                if (tg.Pixels.isEmpty()) {
//                    return;
//                } else {
//                    tg.Pixels = origPixels;
//                    tg.LatLongs = origLatLongs;
//                    clipArea = null;
//                }
//            }
            armyc2.c2sd.JavaTacticalRenderer.clsUtility.InterpolatePixels(tg);

            tg.modifiers = new ArrayList();
            BufferedImage bi = new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = bi.createGraphics();
            //Modifier2.AddModifiersGeo(tg, g2d, clipArea, converter);
            Modifier2.AddModifiersGeo2(tg, g2d, clipArea, converter);

            clsUtilityCPOF.FilterPoints2(tg, converter);
            armyc2.c2sd.JavaTacticalRenderer.clsUtility.FilterVerticalSegments(tg);
            clsUtility.FilterAXADPoints(tg, converter);
            clsUtilityCPOF.ClearPixelsStyle(tg);

            ArrayList<Shape2> linesWithFillShapes = null;

            ArrayList<POINT2> savePixels = tg.Pixels;
            tg.Pixels = origFillPixels;

            //check assignment
            if (clipBounds != null) {
                linesWithFillShapes = clsClipPolygon2.LinesWithFill(tg, clipBounds);
            } else if (clipPoints != null) {
                linesWithFillShapes = clsClipQuad.LinesWithFill(tg, clipPoints);
            } else if (clipArea == null) {
                linesWithFillShapes = clsClipPolygon2.LinesWithFill(tg, clipBounds);
            }

            tg.Pixels = savePixels;

            ArrayList rangeFanFillShapes = null;
            //do not fill the original shapes for circular range fans
            int savefillStyle = tg.get_FillStyle();
            if (linetype == TacticalLines.RANGE_FAN) {
                tg.set_Fillstyle(0);
            }

            //check assignment (pass which clip object is not null)
            if (clipBounds != null) {
                shapes = clsRenderer2.GetLineArray(tg, converter, isTextFlipped, clipBounds); //takes clip object           
            } else if (clipPoints != null) {
                shapes = clsRenderer2.GetLineArray(tg, converter, isTextFlipped, clipPoints);
            } else if (clipArea == null) {
                shapes = clsRenderer2.GetLineArray(tg, converter, isTextFlipped, clipBounds);
            }

            switch (linetype) {
                case TacticalLines.RANGE_FAN:
                case TacticalLines.RANGE_FAN_SECTOR:
                    if (tg.get_FillColor() == null || tg.get_FillColor().getAlpha() < 2) {
                        break;
                    }
                    TGLight tg1 = clsUtilityCPOF.GetCircularRangeFanFillTG(tg);
                    tg1.set_Fillstyle(savefillStyle);
                    tg1.set_SymbolId(tg.get_SymbolId());
                    //check assignment (pass which clip object is not null)
                    if (clipBounds != null) {
                        rangeFanFillShapes = clsRenderer2.GetLineArray(tg1, converter, isTextFlipped, clipBounds);
                    } else if (clipPoints != null) {
                        rangeFanFillShapes = clsRenderer2.GetLineArray(tg1, converter, isTextFlipped, clipPoints);
                    } else if (clipArea == null) {
                        rangeFanFillShapes = clsRenderer2.GetLineArray(tg1, converter, isTextFlipped, clipBounds);
                    }

                    if (rangeFanFillShapes != null) {
                        if (shapes == null) {
                            System.out.println("shapes is null");
                            break;
                        } else {
                            shapes.addAll(0, rangeFanFillShapes);
                        }

                    }
                    break;
                default:
                    clsRenderer2.getAutoshapeFillShape(tg, shapes);
                    break;
            }
            //end section

            //undo any fillcolor for lines with fill
            clsUtilityCPOF.LinesWithSeparateFill(tg.get_LineType(), shapes);
            clsClipPolygon2.addAbatisFill(tg, shapes);

            //if this line is commented then the extra line in testbed goes away
            if (shapes != null && linesWithFillShapes != null && linesWithFillShapes.size() > 0) {
                shapes.addAll(0, linesWithFillShapes);
            }

            if (clsUtilityCPOF.canClipPoints(tg) == false && clipBounds != null) {
                shapes = clsUtilityCPOF.postClipShapes(tg, shapes, clipBounds);
            } else if (clsUtilityCPOF.canClipPoints(tg) == false && clipPoints != null) {
                shapes = clsUtilityCPOF.postClipShapes(tg, shapes, clipPoints);
            }
            resolvePostClippedShapes(tg,shapes);
            //returns early if textSpecs are null
            //currently the client is ignoring these
            if (modifierShapeInfos != null) {
                ArrayList<Shape2> textSpecs = new ArrayList();
                armyc2.c2sd.JavaTacticalRenderer.Modifier2.DisplayModifiers2(tg, g2d, textSpecs, isTextFlipped, converter);
                Shape2ToShapeInfo(modifierShapeInfos, textSpecs);
            }
            Shape2ToShapeInfo(shapeInfos, shapes);
            //GE has no utility for building hatch fills from the texturepaint
            clsUtilityGE.buildHatchFills(tg, shapeInfos);

            //check assignment (pass which clip object is not null)
            if (clipBounds != null) {
                clsUtilityGE.SetShapeInfosPolylines(tg, shapeInfos, clipBounds);//takes a clip object            
            } else if (clipPoints != null) {
                clsUtilityGE.SetShapeInfosPolylines(tg, shapeInfos, clipPoints);
            } else if (clipArea == null) {
                clsUtilityGE.SetShapeInfosPolylines(tg, shapeInfos, clipBounds);
            }
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "render_GE",
                    new RendererException("Failed inside render_GE", exc));

        }
    }
    /**
     * creates a shape for known symbols. The intent is to use client points for
     * the shape and is intended for use with ellipse. If hatch > 1 it creates 2 shapes
     * one for the hatch pattern, the second one is for the outline.
     *
     * @param milStd
     * @param ipc
     * @param clipArea
     * @param shapeType
     * @param lineColor
     * @param fillColor
     * @param hatch
     */
    public static void render_Shape(MilStdSymbol milStd,
            IPointConversion ipc,
            Object clipArea,
            int shapeType,
            Color lineColor,
            Color fillColor,
            int hatch) {
        try {
            Rectangle2D clipBounds = null;
            //CELineArray.setClient("ge");
            ArrayList<Point2D> clipPoints = null;

            if (clipArea != null) {
                if (clipArea.getClass().isAssignableFrom(Rectangle2D.Double.class)) {
                    clipBounds = (Rectangle2D.Double) clipArea;
                } else if (clipArea.getClass().isAssignableFrom(Rectangle.class)) {
                    clipBounds = (Rectangle2D) clipArea;
                } else if (clipArea.getClass().isAssignableFrom(ArrayList.class)) {
                    clipPoints = (ArrayList<Point2D>) clipArea;
                }
            }
            
            //can't use following line because it resets the pixels
            //TGLight tg = createTGLightFromMilStdSymbol(milStd, ipc);
            TGLight tg = new TGLight();
            tg.set_SymbolId(milStd.getSymbolID());
            //tg.set_VisibleModifiers(true);
            //set tg latlongs and pixels
            setClientCoords(milStd, tg);
            //build tg.Pixels
            tg.Pixels = clsUtility.LatLongToPixels(tg.LatLongs, ipc);            
            
            //int fillStyle = milStd.getPatternFillType();
            Shape2 shape = new Shape2(shapeType);
            shape.setFillColor(fillColor);
            if (lineColor != null) {
                shape.setLineColor(lineColor);
                shape.setStroke(new BasicStroke(milStd.getLineWidth()));
            }
            //the client has already set the coordinates for the shape
            POINT2 pt;
            for (int j = 0; j < tg.Pixels.size(); j++) {
                pt = tg.Pixels.get(j);
                if (j == 0) {
                    shape.moveTo(pt);
                } else {
                    shape.lineTo(pt);
                }
            }

            //post clip the shape and set the polylines
            ArrayList<Shape2> shapes = new ArrayList();
            shapes.add(shape);
            //post-clip the shape
            if (clsUtilityCPOF.canClipPoints(tg) == false && clipBounds != null) {
                shapes = clsUtilityCPOF.postClipShapes(tg, shapes, clipBounds);
            } else if (clsUtilityCPOF.canClipPoints(tg) == false && clipPoints != null) {
                shapes = clsUtilityCPOF.postClipShapes(tg, shapes, clipPoints);
            }
            shape=shapes.get(0);
            if (hatch > 1) 
            {
                shape = clsUtilityGE.buildHatchFill(tg, shape, hatch);
                shape.setLineColor(lineColor);
                shape.setStroke(new BasicStroke(1));
                //shapes.clear();
                shapes.add(shape);
            }
            ArrayList<ShapeInfo> shapeInfos = new ArrayList();
            Shape2ToShapeInfo(shapeInfos, shapes);
            //set the shapeInfo polylines
            if (clipBounds != null) {
                clsUtilityGE.SetShapeInfosPolylines(tg, shapeInfos, clipBounds);
            } else if (clipPoints != null) {
                clsUtilityGE.SetShapeInfosPolylines(tg, shapeInfos, clipPoints);
            } else if (clipArea == null) {
                clsUtilityGE.SetShapeInfosPolylines(tg, shapeInfos, clipBounds);
            }
            //set milStd symbol shapes
            if (milStd.getSymbolShapes() == null) {
                milStd.setSymbolShapes(shapeInfos);
            } else {
                milStd.getSymbolShapes().addAll(shapeInfos);
            }
            return;
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "render_Shape",
                    new RendererException("Failed inside render_Shape", exc));

        }
    }
    private static void resolvePostClippedShapes(TGLight tg, ArrayList<Shape2> shapes) {
        try {
            //resolve the PBS and BBS shape properties after the post clip, regardless whether they were clipped
            switch (tg.get_LineType()) {
                case TacticalLines.BBS_RECTANGLE:
                case TacticalLines.BBS_POINT:
                case TacticalLines.BBS_LINE:
                case TacticalLines.BBS_AREA:
                case TacticalLines.PBS_RECTANGLE:
                case TacticalLines.PBS_SQUARE:
                case TacticalLines.PBS_CIRCLE:
                case TacticalLines.PBS_ELLIPSE:
                    break;
                default:
                    return;
            }
            Color fillColor = tg.get_FillColor();
            shapes.get(0).setFillColor(fillColor);
            shapes.get(1).setFillColor(null);
            int fillStyle = tg.get_FillStyle();
            shapes.get(0).set_Fillstyle(0);
            shapes.get(1).set_Fillstyle(fillStyle);
            return;

        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "render_GE",
                    new RendererException("Failed inside resolvePostClippedShapes", exc));

        }
    }
    /**
     * to follow right hand rule for LC when affiliation is hostile. also fixes
     * MSDZ point order and maybe various other wayward symbols
     *
     * @param tg
     */
    private static void setHostileLC(TGLight tg) {
        try {
            Boolean usas1314 = true;
            ArrayList<POINT2> pts = new ArrayList();
            int j = 0;
            switch (tg.get_LineType()) {
                case TacticalLines.LC:
                    if (usas1314 == false) {
                        break;
                    }
                    if (tg.get_Affiliation() != null && !tg.get_Affiliation().equals("H")) {
                        break;
                    }
                    pts = (ArrayList<POINT2>) tg.Pixels.clone();
                    for (j = 0; j < tg.Pixels.size(); j++) {
                        tg.Pixels.set(j, pts.get(pts.size() - j - 1));
                    }
                    //reverse the latlongs also
                    pts = (ArrayList<POINT2>) tg.LatLongs.clone();
                    for (j = 0; j < tg.LatLongs.size(); j++) {
                        tg.LatLongs.set(j, pts.get(pts.size() - j - 1));
                    }
                    break;
                case TacticalLines.LINE:    //CPOF client requests reverse orientation
                    pts = (ArrayList<POINT2>) tg.Pixels.clone();
                    for (j = 0; j < tg.Pixels.size(); j++) {
                        tg.Pixels.set(j, pts.get(pts.size() - j - 1));
                    }
                    //reverse the latlongs also
                    pts = (ArrayList<POINT2>) tg.LatLongs.clone();
                    for (j = 0; j < tg.LatLongs.size(); j++) {
                        tg.LatLongs.set(j, pts.get(pts.size() - j - 1));
                    }
                    break;
                default:
                    return;
            }
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "setHostileLC",
                    new RendererException("Failed inside setHostileLC", exc));

        }
    }

    /**
     * Calculates the scale using the converter and the clipBounds and sets
     * overhead wire type based on the scale
     *
     * @param tg
     * @param converter
     * @param clipBounds
     * @return
     */
    protected static double getScale(TGLight tg,
            IPointConversion converter,
            Object clipBounds) {
        double scale = 0;
        try {
            int lineType = tg.get_LineType();
            if (lineType != TacticalLines.OVERHEAD_WIRE) {
                return 0;
            }
            if (clipBounds == null || converter == null) {
                return 0;
            }

            Rectangle2D clipRect = null;
            //ArrayList<Point2D> clipArray=null;            
            if (clipBounds.getClass().isAssignableFrom(Rectangle2D.Double.class)) {
                clipRect = (Rectangle2D) clipBounds;
            } else if (clipBounds.getClass().isAssignableFrom(Rectangle2D.class)) {
                clipRect = (Rectangle2D) clipBounds;
            } else if (clipBounds.getClass().isAssignableFrom(Rectangle.class)) {
                Rectangle rectx = (Rectangle) clipBounds;
                clipRect = new Rectangle2D.Double(rectx.x, rectx.y, rectx.width, rectx.height);
            } else if (clipBounds.getClass().isAssignableFrom(ArrayList.class)) {
                ArrayList<Point2D> clipArray = (ArrayList<Point2D>) clipBounds;
                clipRect = armyc2.c2sd.JavaRendererServer.RenderMultipoints.clsUtility.getMBR(clipArray);
            }

            double left = clipRect.getMinX();
            double right = clipRect.getMaxX();
            double distanceInPixels = Math.abs(right - left);
            double top = clipRect.getMinY();
            Point2D ul = new Point2D.Double(left, top);
            Point2D ur = new Point2D.Double(right, top);
            Point2D ulGeo = converter.PixelsToGeo(ul);
            Point2D urGeo = converter.PixelsToGeo(ur);
            POINT2 pt2ulGeo = new POINT2(ulGeo.getX(), ulGeo.getY());
            POINT2 pt2urGeo = new POINT2(urGeo.getX(), urGeo.getY());
            double distanceInMeters = mdlGeodesic.geodesic_distance(pt2ulGeo, pt2urGeo, null, null);
            //sccale=(distanceInPixels pixels/distanceInMeters meters)*(1 inch/96 pixels)*(1 meter/ 39.37 inch)
            scale = (distanceInPixels / distanceInMeters) * (1.0d / 96.0d) * (1.0d / 39.37d);
            scale = 1.0d / scale;
            //reset the linetype for overhead wire if the sclae is large
            if (lineType == TacticalLines.OVERHEAD_WIRE && scale >= 250000 && tg.get_SymbolId().length() < 20) {
                tg.set_LineType(TacticalLines.OVERHEAD_WIRE_LS);
            }
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "getScale",
                    new RendererException("Failed inside getScale", exc));

        }
        return scale;
    }

    /**
     * set the clip rectangle as an arraylist or a Rectangle2D depending on the
     * object
     *
     * @param clipBounds
     * @param clipRect
     * @param clipArray
     * @return
     */
    private static boolean setClip(Object clipBounds, Rectangle2D clipRect, ArrayList<Point2D> clipArray) {
        try {
            if (clipBounds == null) {
                return false;
            } else if (clipBounds.getClass().isAssignableFrom(Rectangle2D.Double.class)) {
                clipRect.setRect((Rectangle2D) clipBounds);
            } else if (clipBounds.getClass().isAssignableFrom(Rectangle2D.class)) {
                clipRect.setRect((Rectangle2D) clipBounds);
            } else if (clipBounds.getClass().isAssignableFrom(Rectangle.class)) {
                //clipRect.setRect((Rectangle2D)clipBounds);
                Rectangle rectx = (Rectangle) clipBounds;
                //clipBounds=new Rectangle2D.Double(rectx.x,rectx.y,rectx.width,rectx.height);
                clipRect.setRect(rectx.x, rectx.y, rectx.width, rectx.height);
            } else if (clipBounds.getClass().isAssignableFrom(ArrayList.class)) {
                clipArray.addAll((ArrayList) clipBounds);
            }
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "setClip",
                    new RendererException("Failed inside setClip", exc));

        }
        return true;
    }

    /**
     * public render function transferred from JavaLineArrayCPOF project. Use
     * this function to replicate CPOF renderer functionality.
     *
     * @param mss the milStdSymbol object
     * @param converter the geographic to pixels coordinate converter
     * @param clipBounds the pixels based clip bounds
     */
    public static void render(MilStdSymbol mss,
            IPointConversion converter,
            Object clipBounds) {
        try {
            ArrayList<ShapeInfo> shapeInfos = new ArrayList();
            ArrayList<ShapeInfo> modifierShapeInfos = new ArrayList();
            render(mss, converter, shapeInfos, modifierShapeInfos, clipBounds);
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "render",
                    new RendererException("render", exc));

        }
    }

    /**
     * Generic tester button says Tiger or use JavaRendererSample. Generic
     * renderer testers: called by JavaRendererSample and TestJavaLineArray
     * public render function transferred from JavaLineArrayCPOF project. Use
     * this function to replicate CPOF renderer functionality.
     *
     * @param tg tactical graphic
     * @param converter geographic to pixels converter
     * @param shapeInfos ShapeInfo array
     * @param modifierShapeInfos modifier ShapeInfo array
     * @param clipBounds clip bounds
     */
    public static void render(MilStdSymbol mss,
            IPointConversion converter,
            ArrayList<ShapeInfo> shapeInfos,
            ArrayList<ShapeInfo> modifierShapeInfos,
            Object clipBounds) {
        try {
            //boolean shiftLines = Channels.getShiftLines();
            //end section

            Rectangle2D clipRect = new Rectangle2D.Double();
            ArrayList<Point2D> clipArray = new ArrayList();
            setClip(clipBounds, clipRect, clipArray);

            int rev = mss.getSymbologyStandard();
            armyc2.c2sd.JavaTacticalRenderer.clsUtility.initializeLinetypes(rev);
            TGLight tg = createTGLightFromMilStdSymbol(mss, converter);
            CELineArray.setClient("generic");
//            if (shiftLines) {
//                String affiliation = tg.get_Affiliation();
//                Channels.setAffiliation(affiliation);
//            }
            //CELineArray.setMinLength(2.5);    //2-27-2013

            //if(rev==RendererSettings.Symbology_2525C)
            //{
            armyc2.c2sd.JavaTacticalRenderer.clsUtility.setRevC(tg);
            //}
            //resets the linetypes for overhead wire and other rev c symbols
            double scale = getScale(tg, converter, clipBounds);

            int linetype = tg.get_LineType();
            //replace calls to MovePixels
            armyc2.c2sd.JavaRendererServer.RenderMultipoints.clsUtility.RemoveDuplicatePoints(tg);

            setHostileLC(tg);

            BufferedImage bi = new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = bi.createGraphics();

            clsUtilityCPOF.SegmentGeoPoints(tg, converter, 1);
            clsUtility.FilterAXADPoints(tg, converter);

            //prevent vertical segments for oneway, twoway, alt
            armyc2.c2sd.JavaTacticalRenderer.clsUtility.FilterVerticalSegments(tg);
            boolean isChange1Area = armyc2.c2sd.JavaTacticalRenderer.clsUtility.IsChange1Area(linetype, null);
            boolean isTextFlipped = false;
            //for 3d change 1 symbols we do not transform the points

            //if it is world view then we want to flip the far points about
            //the left and right sides to get two symbols
            ArrayList<POINT2> farLeftPixels = new ArrayList();
            ArrayList<POINT2> farRightPixels = new ArrayList();
            if (isChange1Area == false) {
                clsUtilityCPOF.GetFarPixels(tg, converter, farLeftPixels, farRightPixels);
            }

            ArrayList<Shape2> shapesLeft = new ArrayList();
            ArrayList<Shape2> shapesRight = new ArrayList();
            ArrayList<Shape2> shapes = null;   //use this to collect all the shapes

            //CPOF 6.0 diagnostic
            ArrayList<Shape2> textSpecsLeft = null;
            ArrayList<Shape2> textSpecsRight = null;
            //Note: DisplayModifiers3 returns early if textSpecs are null
            textSpecsLeft = new ArrayList();
            textSpecsRight = new ArrayList();

            if (farLeftPixels.size() > 0) {
                tg.Pixels = farLeftPixels;
                shapesLeft = clsRenderer2.GetLineArray(tg, converter, isTextFlipped, clipBounds);
                //CPOF 6.0
                //returns early if textSpecs are null
                armyc2.c2sd.JavaTacticalRenderer.Modifier2.DisplayModifiers2(tg, g2d, textSpecsLeft, isTextFlipped, null);
            }
            if (farRightPixels.size() > 0) {
                tg.Pixels = farRightPixels;
                shapesRight = clsRenderer2.GetLineArray(tg, converter, isTextFlipped, clipBounds);
                //CPOF 6.0
                //returns early if textSpecs are null
                armyc2.c2sd.JavaTacticalRenderer.Modifier2.DisplayModifiers2(tg, g2d, textSpecsRight, isTextFlipped, null);
            }

            //CPOF 6.0 diagnostic
            ArrayList<Shape2> textSpecs = new ArrayList();

            if (shapesLeft.isEmpty() || shapesRight.isEmpty()) {
                ArrayList<Shape2> linesWithFillShapes = null;
                if (clipArray != null && !clipArray.isEmpty()) {
                    linesWithFillShapes = clsClipQuad.LinesWithFill(tg, clipArray);
                } else if (clipRect != null && clipRect.getWidth() != 0) {
                    linesWithFillShapes = clsClipPolygon2.LinesWithFill(tg, clipRect);
                } else {
                    linesWithFillShapes = clsClipPolygon2.LinesWithFill(tg, null);
                }

                //diagnostic: comment two lines if using the WW tester
                if (clsUtilityCPOF.canClipPoints(tg) && clipBounds != null) {
                    if (clipArray != null && !clipArray.isEmpty()) {
                        clsClipQuad.ClipPolygon(tg, clipArray);
                    } else if (clipRect != null && clipRect.getWidth() != 0) {
                        clsClipPolygon2.ClipPolygon(tg, clipRect);
                    }

                    tg.LatLongs = clsUtility.PixelsToLatLong(tg.Pixels, converter);
                }

                //diagnostic 1-28-13
                armyc2.c2sd.JavaTacticalRenderer.clsUtility.InterpolatePixels(tg);

                tg.modifiers = new ArrayList();
                Modifier2.AddModifiersGeo(tg, g2d, clipBounds, converter);

                clsUtilityCPOF.FilterPoints2(tg, converter);
                clsUtilityCPOF.ClearPixelsStyle(tg);
                //add section to replace preceding line M. Deutch 11-4-2011
                ArrayList rangeFanFillShapes = null;
                //do not fill the original shapes for circular range fans
                int savefillStyle = tg.get_FillStyle();
                if (linetype == TacticalLines.RANGE_FAN) {
                    tg.set_Fillstyle(0);
                }

                shapes = clsRenderer2.GetLineArray(tg, converter, isTextFlipped, clipBounds);

                switch (linetype) {
                    case TacticalLines.RANGE_FAN:
                    case TacticalLines.RANGE_FAN_SECTOR:
                        TGLight tg1 = clsUtilityCPOF.GetCircularRangeFanFillTG(tg);
                        tg1.set_Fillstyle(savefillStyle);
                        rangeFanFillShapes = clsRenderer2.GetLineArray(tg1, converter, isTextFlipped, clipBounds);

                        if (rangeFanFillShapes != null) {
                            shapes.addAll(0, rangeFanFillShapes);
                        }
                        break;
                    default:
                        break;
                }

                //undo any fillcolor for lines with fill
                clsUtilityCPOF.LinesWithSeparateFill(tg.get_LineType(), shapes);
                clsClipPolygon2.addAbatisFill(tg, shapes);

                //if this line is commented then the extra line in testbed goes away
                if (shapes != null && linesWithFillShapes != null && linesWithFillShapes.size() > 0) {
                    shapes.addAll(0, linesWithFillShapes);
                }

                if (shapes != null && shapes.size() > 0) {
                    armyc2.c2sd.JavaTacticalRenderer.Modifier2.DisplayModifiers2(tg, g2d, textSpecs, isTextFlipped, null);
                    Shape2ToShapeInfo(modifierShapeInfos, textSpecs);
                    mss.setModifierShapes(modifierShapeInfos);
                }
            } else //symbol was more than 180 degrees wide, use left and right symbols
            {
                shapes = shapesLeft;
                shapes.addAll(shapesRight);

                if (textSpecs != null) {
                    textSpecs.addAll(textSpecsLeft);
                    textSpecs.addAll(textSpecsRight);
                }
            }
            //post-clip the points if the tg could not be pre-clipped
            if (clsUtilityCPOF.canClipPoints(tg) == false && clipBounds != null) {
                shapes = clsUtilityCPOF.postClipShapes(tg, shapes, clipBounds);
            }

            Shape2ToShapeInfo(shapeInfos, shapes);
            mss.setSymbolShapes(shapeInfos);
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "render",
                    new RendererException("Failed inside render", exc));

        }
    }

    /**
     * for Rev D symbols
     *
     * @param SymbolSet
     * @param entityCode
     * @return
     */
    public static int getCMLineType(String SymbolSet, String entityCode) {
        int symbolSet = Integer.parseInt(SymbolSet);
        if (symbolSet != 25) {
            return -1;
        }
        int nCode = Integer.parseInt(entityCode);
        switch (nCode) {
            case 200101:
            case 200201:
                return TacticalLines.LAUNCH_AREA;
            case 120100:
                return TacticalLines.AO;
            case 120200:
                return TacticalLines.NAI;
            case 120300:
                return TacticalLines.TAI;
            case 120400:
                return TacticalLines.AIRFIELD;
            case 151401:
                return TacticalLines.AIRAOA;
            case 151402:
                return TacticalLines.AAAAA;
            case 151403:
                return TacticalLines.MAIN;
            case 151404:
            case 151405:
            case 151407:
            case 151408:
                return TacticalLines.SPT;
            case 151406:
                return TacticalLines.AAFNT;
            case 110101:                        //lateral bdry
            case 110102:                        //fwd bdry
            case 110103:                        //rear bdry
                return TacticalLines.BOUNDARY;
            case 110200:
                return TacticalLines.LL;
            case 120101:
                return TacticalLines.AO;
            case 120102:
                return TacticalLines.NAI;
            case 120103:
                return TacticalLines.TAI;
            case 120104:
                return TacticalLines.AIRFIELD;
            case 140100:
            case 140101:
            case 140102:
            case 140103:
            case 140104:
                return TacticalLines.FLOT;
            case 140200:
                return TacticalLines.LC;
            case 140300:
                return TacticalLines.PL;
            case 140400:                //new FEBA as a line, new label
            case 140401:
                return TacticalLines.PL;
            case 140500:
                return TacticalLines.PDF;
            case 140601:
                return TacticalLines.DIRATKAIR;
            case 140602:
                return TacticalLines.DIRATKGND;
            case 140603:
            case 140604:
            case 140606:
            case 140607:
                return TacticalLines.DIRATKSPT;
            case 140605:
                return TacticalLines.DIRATKFNT;
            case 140700:
                return TacticalLines.FCL;
            case 140800:
                return TacticalLines.IL;
            case 140900:
                return TacticalLines.LOA;
            case 141000:
                return TacticalLines.LOD;
            case 141100:
                return TacticalLines.LDLC;
            case 141200:
                return TacticalLines.PLD;
            case 150101:
            case 150102:
            case 150103:
            case 150104:
            case 200401:
                return TacticalLines.PEN;
            case 150200:
            case 150300:
            case 150301:
            case 150302:
            case 150400:
                return TacticalLines.ASSY;
            case 150501:
            case 150502:
            case 150503:
                return TacticalLines.GENERAL;
            case 150600:    //dz no eny
                return TacticalLines.DZ;
            case 150700:    //ez no eny
                return TacticalLines.EZ;
            case 150800:    //lz no eny
                return TacticalLines.LZ;
            case 150900:    //pz no eny
                return TacticalLines.PZ;
            case 151000:
                return TacticalLines.FORT;
            case 151100:
                return TacticalLines.LAA;
            case 151200:
            case 151201:
                return TacticalLines.BATTLE;
            case 151202:
                return TacticalLines.PNO;
            case 151204:
                return TacticalLines.CONTAIN;
            case 151205:
                return TacticalLines.RETAIN;
            case 151300:
                return TacticalLines.EA;
            case 151203:
                return TacticalLines.STRONG;
            case 151500:
                return TacticalLines.ASSAULT;
            case 151600:
                return TacticalLines.ATKPOS;
            case 151700:
                return TacticalLines.OBJ;
            case 151801:
            case 151802:
                return TacticalLines.ENCIRCLE;
            case 151900:
                return TacticalLines.PEN;
            case 152000:
                return TacticalLines.ATKBYFIRE;
            case 152100:
                return TacticalLines.SPTBYFIRE;
            case 152200:
                return TacticalLines.SARA;
            case 141300:
                return TacticalLines.AIRHEAD;
            case 141400:
                return TacticalLines.BRDGHD;
            case 141500:
                return TacticalLines.HOLD;
            case 141600:
                return TacticalLines.RELEASE;
            case 141700:
                return TacticalLines.AMBUSH;
            case 170100:
            case 170101:
                return TacticalLines.AC;
            case 170200:
                return TacticalLines.LLTR;
            case 170300:
                return TacticalLines.MRR;
            case 170400:                    //SL new label
                return TacticalLines.MRR;
            case 170500:
                return TacticalLines.SAAFR;
            case 170600:                    //TC new label
                return TacticalLines.MRR;
            case 170700:
                return TacticalLines.UAV;
            case 170800:
                return TacticalLines.PEN;   //BDZ new label
            case 170900:
                return TacticalLines.HIDACZ;
            case 171000:
                return TacticalLines.ROZ;
            case 171100:                    // new label type AAROZ
            case 171200:                    // new label UAROZ
            case 171300:                    // new label WEZ
            case 171400:                    // new label FEZ
            case 171500:                    // new label JEZ
                return TacticalLines.ROZ;
            case 171600:
                return TacticalLines.MEZ;
            case 171700:
                return TacticalLines.LOMEZ;
            case 171800:
                return TacticalLines.HIMEZ;
            case 171900:
                return TacticalLines.FAADZ;
            case 172000:
                return TacticalLines.WFZ;
            case 190100:    //iff off new label
            case 190200:    //iff on new label
                return TacticalLines.FSCL;
            case 200202:    //defended area rect
            case 200402:
            case 240804:
                return TacticalLines.FSA_RECTANGULAR;    //DA new label
            case 200300:    //no atk
                return TacticalLines.FSA_CIRCULAR;  //no atk new label
            case 220100:
                return TacticalLines.BEARING;
            case 220101:
                return TacticalLines.ELECTRO;
            case 220102:    //EW                //new label
                return TacticalLines.BEARING;
            case 220103:
            case 220104:
                return TacticalLines.ACOUSTIC;
            case 220105:
                return TacticalLines.TORPEDO;
            case 220106:
                return TacticalLines.OPTICAL;
            case 218400:
                return TacticalLines.NAVIGATION;
            case 220107:    //Jammer                //new label
            case 220108:    //RDF                   //new label
                return TacticalLines.BEARING;
            case 230100:
            case 230200:
                return TacticalLines.DECEIVE;
            case 240101:
                return TacticalLines.ACA;
            case 240102:
                return TacticalLines.ACA_RECTANGULAR;
            case 240103:
                return TacticalLines.ACA_CIRCULAR;

            case 240201:
                return TacticalLines.FFA;
            case 240202:
                return TacticalLines.FFA_RECTANGULAR;
            case 240203:
                return TacticalLines.FFA_CIRCULAR;

            case 240301:
                return TacticalLines.NFA;
            case 240302:
                return TacticalLines.NFA_RECTANGULAR;
            case 240303:
                return TacticalLines.NFA_CIRCULAR;

            case 240401:
                return TacticalLines.RFA;
            case 240402:
                return TacticalLines.RFA_RECTANGULAR;
            case 240403:
                return TacticalLines.RFA_CIRCULAR;

            case 240501:
                return TacticalLines.PAA_RECTANGULAR;
            case 240502:
                return TacticalLines.PAA_CIRCULAR;
            case 260100:
                return TacticalLines.FSCL;
            case 260200:
                return TacticalLines.CFL;
            case 260300:
                return TacticalLines.NFL;
            case 260400:    //BCL               new label
                return TacticalLines.FSCL;
            case 260500:
                return TacticalLines.RFL;
            case 260600:
                return TacticalLines.MFP;
            case 240701:
                return TacticalLines.LINTGT;
            case 240702:
                return TacticalLines.LINTGTS;
            case 240703:
                return TacticalLines.FPF;
            case 240801:
                return TacticalLines.AT;
            case 240802:
                return TacticalLines.RECTANGULAR;
            case 240803:
                return TacticalLines.CIRCULAR;
            case 240805:
                return TacticalLines.SERIES;
            case 240806:
            case 240807:
                return TacticalLines.SMOKE;
            case 240808:
                return TacticalLines.BOMB;
            case 241001:
                return TacticalLines.FSA;
            case 241002:
                return TacticalLines.FSA_RECTANGULAR;
            case 241003:
                return TacticalLines.FSA_CIRCULAR;
            case 241101:
                return TacticalLines.ATI;
            case 241102:
                return TacticalLines.ATI_RECTANGULAR;
            case 241103:
                return TacticalLines.ATI_CIRCULAR;
            case 241201:
                return TacticalLines.CFFZ;
            case 241202:
                return TacticalLines.CFFZ_RECTANGULAR;
            case 241203:
                return TacticalLines.CFFZ_CIRCULAR;
            case 241301:
                return TacticalLines.CENSOR;
            case 241302:
                return TacticalLines.CENSOR_RECTANGULAR;
            case 241303:
                return TacticalLines.CENSOR_CIRCULAR;
            case 241401:
                return TacticalLines.CFZ;
            case 241402:
                return TacticalLines.CFZ_RECTANGULAR;
            case 241403:
                return TacticalLines.CFZ_CIRCULAR;
            case 241501:
                return TacticalLines.DA;
            case 241502:
                return TacticalLines.DA_RECTANGULAR;
            case 241503:
                return TacticalLines.DA_CIRCULAR;
            case 241601:
                return TacticalLines.SENSOR;
            case 241602:
                return TacticalLines.SENSOR_RECTANGULAR;
            case 241603:
                return TacticalLines.SENSOR_CIRCULAR;
            case 241701:
                return TacticalLines.TBA;
            case 241702:
                return TacticalLines.TBA_RECTANGULAR;
            case 241703:
                return TacticalLines.TBA_CIRCULAR;
            case 241801:
                return TacticalLines.TVAR;
            case 241802:
                return TacticalLines.TVAR_RECTANGULAR;
            case 241803:
                return TacticalLines.TVAR_CIRCULAR;
            case 241901:
                return TacticalLines.ZOR;
            case 241902:
                return TacticalLines.ZOR_RECTANGULAR;
            case 241903:
                return TacticalLines.ZOR_CIRCULAR;
            case 242000:
                return TacticalLines.TGMF;
            case 242100:
                return TacticalLines.RANGE_FAN;
            case 242200:
                return TacticalLines.RANGE_FAN_SECTOR;
            case 242301:
                return TacticalLines.KILLBOXBLUE;
            case 242302:
                return TacticalLines.KILLBOXBLUE_RECTANGULAR;
            case 242303:
                return TacticalLines.KILLBOXBLUE_CIRCULAR;
            case 242304:
                return TacticalLines.KILLBOXPURPLE;
            case 242305:
                return TacticalLines.KILLBOXPURPLE_RECTANGULAR;
            case 242306:
                return TacticalLines.KILLBOXPURPLE_CIRCULAR;
            case 270100:
                return TacticalLines.BELT;
            case 270200:
                return TacticalLines.ZONE;
            case 270300:
                return TacticalLines.OBSFAREA;
            case 270400:
                return TacticalLines.OBSAREA;
            case 270501:
                return TacticalLines.MNFLDBLK;
            case 270502:
                return TacticalLines.MNFLDDIS;
            case 270503:
                return TacticalLines.MNFLDFIX;
            case 270504:
                return TacticalLines.TURN;
            case 270601:
                return TacticalLines.EASY;
            case 270602:
                return TacticalLines.BYDIF;
            case 270603:
                return TacticalLines.BYIMP;
            case 271100:
                return TacticalLines.GAP;
            case 271201:
                return TacticalLines.PLANNED;
            case 271202:
                return TacticalLines.ESR1;
            case 271203:
                return TacticalLines.ESR2;
            case 271204:
                return TacticalLines.ROADBLK;
            case 280100:
                return TacticalLines.ABATIS;
            case 290100:
                return TacticalLines.LINE;
            case 290201:
                return TacticalLines.ATDITCH;
            case 290202:
                return TacticalLines.ATDITCHC;
            case 290203:
                return TacticalLines.ATDITCHM;
            case 290204:
                return TacticalLines.ATWALL;
            case 290301:
                return TacticalLines.UNSP;
            case 290302:
                return TacticalLines.SFENCE;
            case 290303:
                return TacticalLines.DFENCE;
            case 290304:
                return TacticalLines.DOUBLEA;
            case 290305:
                return TacticalLines.LWFENCE;
            case 290306:
                return TacticalLines.HWFENCE;
            case 290307:
                return TacticalLines.SINGLEC;
            case 290308:
                return TacticalLines.DOUBLEC;
            case 290309:
                return TacticalLines.TRIPLE;
            case 290600:
                return TacticalLines.MFLANE;
            case 270706:
                return TacticalLines.DUMMY;
            case 270707:
                return TacticalLines.DEPICT;
            case 270800:
                return TacticalLines.MINED;
            case 270900:
                return TacticalLines.DMA;
            case 270901:
                return TacticalLines.DMAF;
            case 271000:
                return TacticalLines.UXO;
            case 290400:
                return TacticalLines.CLUSTER;
            case 290500:
                return TacticalLines.TRIP;
            case 282003:
                return TacticalLines.OVERHEAD_WIRE;
            case 271300:
                return TacticalLines.ASLTXING;
            case 271400:
                return TacticalLines.BRIDGE;
            case 271500:
                return TacticalLines.FORDSITE;
            case 271600:
                return TacticalLines.FORDIF;
            case 290700:
                return TacticalLines.FERRY;
            case 290800:
                return TacticalLines.RAFT;
            case 290900:
                return TacticalLines.FORTL;
            case 291000:
                return TacticalLines.FOXHOLE;
            case 272100:
                return TacticalLines.MSDZ;
            case 272200:
                return TacticalLines.DRCL;

            case 310100:
                return TacticalLines.DHA;
            case 310200:
                return TacticalLines.EPW;
            case 310300:
                return TacticalLines.FARP;
            case 310400:
                return TacticalLines.RHA;
            case 310500:
                return TacticalLines.RSA;
            case 310600:
                return TacticalLines.BSA;
            case 310700:
                return TacticalLines.DSA;
            case 330100:
                return TacticalLines.CONVOY;
            case 330200:
                return TacticalLines.HCONVOY;
            case 330300:
                return TacticalLines.MSR;
            case 330301:
                return TacticalLines.ONEWAY;
            case 330302:
                return TacticalLines.TWOWAY;
            case 330303:
                return TacticalLines.ALT;

            case 330400:
                return TacticalLines.ASR;
            case 330401:                    //asr one way   new label
                return TacticalLines.ONEWAY;
            case 330402:                    //asr two way   new label
                return TacticalLines.TWOWAY;
            case 330403:                    //asr alt       new label
                return TacticalLines.ALT;

            case 340100:
                return TacticalLines.BLOCK;
            case 340200:
                return TacticalLines.BREACH;
            case 340300:
                return TacticalLines.BYPASS;
            case 340400:
                return TacticalLines.CANALIZE;
            case 340500:
                return TacticalLines.CLEAR;
            case 340600:
                return TacticalLines.CATK;
            case 340700:
                return TacticalLines.CATKBYFIRE;

            case 340800:
                return TacticalLines.DELAY;
            case 341000:
                return TacticalLines.DISRUPT;
            case 341100:
                return TacticalLines.FIX;
            case 341200:
                return TacticalLines.FOLLA;
            case 341300:
                return TacticalLines.FOLSP;
            case 341500:
                return TacticalLines.ISOLATE;
            case 341700:
                return TacticalLines.OCCUPY;
            case 341800:
                return TacticalLines.PENETRATE;
            case 341900:
                return TacticalLines.RIP;
            case 342000:
                return TacticalLines.RETIRE;
            case 342100:
                return TacticalLines.SECURE;
            case 342201:
                return TacticalLines.COVER;
            case 342202:
                return TacticalLines.GUARD;
            case 342203:
                return TacticalLines.SCREEN;
            case 342300:
                return TacticalLines.SEIZE;
            case 342400:
                return TacticalLines.WITHDRAW;
            case 342500:
                return TacticalLines.WDRAWUP;
            case 300100:    //ICL               new label
                return TacticalLines.FSCL;
            default:
                break;
        }
        return -1;
    }

    /**
     * Rev D symbols
     *
     * @param tg
     * @param setA
     * @param setB
     */
    public static void setTGProperties(TGLight tg) {
        try {
            if (tg.get_SymbolId().length() < 20) {
                return;
            }
            String setA = tg.get_SymbolId().substring(0, 10);
            String setB = tg.get_SymbolId().substring(10);
            //String symbolSet = getSymbolSet(setA);
            String symbolSet = setA.substring(4, 6);
            int nSymbolSet = Integer.parseInt(symbolSet);
            if (nSymbolSet != 25) {
                return;
            }
            //String code = Modifier2.getCode(setB);
            String code = setB.substring(0, 6);
            int nCode = Integer.parseInt(code);
            switch (nCode) {
                case 140101:    //friendly present flot
                    break;
                case 140102:
                    tg.set_LineStyle(1);
                    break;
                case 140103:
                    break;
                case 140104:
                case 140607:
                case 150102:
                case 150104:
                    tg.set_LineStyle(1);
                    break;
                case 140604:
                case 140401:
                case 220104:
                case 240807:
                case 151405:
                case 150400:
                    tg.set_LineStyle(1);
                    break;
                case 151802:
                case 140606:
                case 150501:
                case 150502:
                case 150503:
                    break;
                case 151407:
                    tg.set_Name("");
                    break;
                case 151408:
                    tg.set_Name("");
                    tg.set_LineStyle(1);
                    break;
                case 200101:
                    tg.set_FillColor(new Color(255, 155, 0, 191));
                    break;
                case 200201:
                case 200202:
                    tg.set_FillColor(new Color(85, 119, 136, 191));
                    break;
                case 270100:
                    tg.set_T1("");
                    break;
                case 290301:
                case 290305:
                case 290306:
                case 290307:
                case 290308:
                case 290309:
                    clsRenderer.reversePointsRevD(tg);
                    break;
                default:
                    break;
            }
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "setTGProperties",
                    new RendererException("Failed inside setTGProperties", exc));
        }
    }

    private static void reversePointsRevD(TGLight tg) {
        try {
            int j = 0;
            ArrayList pts = null;
            if (tg.get_SymbolId().length() < 20) {
                return;
            }
            String setB = tg.get_SymbolId().substring(10);
            String entityCode = setB.substring(0, 6);
            int nCode = Integer.parseInt(entityCode);
            switch (nCode) {
                case 290301:
                case 290305:
                case 290306:
                case 290307:
                case 290308:
                case 290309:
                    if (tg.Pixels != null) {
                        pts = (ArrayList<POINT2>) tg.Pixels.clone();
                        for (j = 0; j < tg.Pixels.size(); j++) {
                            tg.Pixels.set(j, (POINT2) pts.get(pts.size() - j - 1));
                        }
                    }
                    if (tg.LatLongs != null) {
                        pts = (ArrayList<POINT2>) tg.LatLongs.clone();
                        for (j = 0; j < tg.LatLongs.size(); j++) {
                            tg.LatLongs.set(j, (POINT2) pts.get(pts.size() - j - 1));
                        }
                    }
                    break;
                default:
                    break;
            }

        } catch (Exception exc) {
            ErrorLogger.LogException("clsRenderer", "renderWithPolylines",
                    new RendererException("Failed inside renderWithPolylines", exc));
        }
    }

    /**
     * sets tactical graphic line type for rev D and below
     *
     * @param tg
     */
    public static int getRevDLinetype(TGLight tg) {
        int linetype = -1;
        try {
            String symbolId = tg.get_SymbolId();
            if (symbolId.length() > 15) //rev D
            {
                //String setA = Modifier2.getSetA(symbolId);
                String setA = symbolId.substring(0, 10);
                //String setB = Modifier2.getSetB(symbolId);
                String setB = symbolId.substring(10);
                //String code = Modifier2.getCode(setB);
                String code = setB.substring(0, 6);
                //String symbolSet = Modifier2.getSymbolSet(setA);
                String symbolSet = setA.substring(4, 6);
                int nSymbol = Integer.parseInt(symbolSet);
                if (nSymbol == 25) {
                    linetype = getCMLineType(symbolSet, code);
                    //setTGProperties(tg);
                } else if (nSymbol == 45 || nSymbol == 46) {
                    linetype = clsMETOC.getWeatherLinetype(symbolSet, code);
                }

            } else //not rev D            
            {
                linetype = armyc2.c2sd.JavaTacticalRenderer.clsUtility.GetLinetypeFromString(symbolId);
            }

            tg.set_LineType(linetype);
        } catch (Exception exc) {
            ErrorLogger.LogException("clsRenderer", "setRevDLinetype",
                    new RendererException("Failed in setRevDLinetype ", exc));
        }
        return linetype;
    }

}
