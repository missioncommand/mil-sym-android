/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package armyc2.c2sd.renderer.utilities;

import java.util.ArrayList;

/**
 * Symbol attributes for use as url parameters
 * @author michael.spinelli
 */
public class MilStdAttributes {
     
    /*
     * Line color of the symbol. hex value.
     */
    public static final int LineColor = 0;
    
    /*
     * Fill color of the symbol. hex value
     */
    public static final int FillColor = 1;

    /*
     * Color of internal icon. hex value
     */
    public static final int IconColor = 17;
    
    /*
     * font size to use when rendering symbol
     */
    public static final int FontSize = 2;
    
    /*
     * size of the single point image
     */
    public static final int PixelSize = 3;
    
    /*
     * scale value to grow or shrink single point tactical graphics.
     */
    public static final int Scale = 4;
    
    /**
     * defaults to true
     */
    public static final int KeepUnitRatio = 5;
    
    /*
     * transparency value of the symbol. values from 0-255
     */
    public static final int Alpha = 6;
    
    /*
     * outline the symbol, true/false
     */
    public static final int OutlineSymbol = 7;
    
    /*
     * specify and outline color rather than letting renderer picking 
     * the best contrast color. hex value
     */
    public static final int OutlineColor = 8;
    
    /**
     * specifies thickness of the symbol outline
     */
    //public static final int OutlineWidth = 9;
    
    /*
     * just draws the core symbol
     */
    public static final int DrawAsIcon = 10;
    
    /*
     * 2525B vs 2525C. 
     * like:
     * RendererSettings.Symbology_2525Bch2_USAS_13_14
     * OR
     * RendererSettings.Symbology_2525C
     */
    public static final int SymbologyStandard = 11;
    
    public static final int LineWidth = 12;
   
    public static final int TextColor = 13;
    
    public static final int TextBackgroundColor = 14;    
    
    /**
     * If false, the renderer will create a bunch of little lines to create
     * the "dash" effect (expensive but necessary for KML).  
     * If true, it will be on the user to create the dash effect using the
     * DashArray from the Stroke object from the ShapeInfo object.
     */
    public static final int UseDashArray = 15;
    
    public static final int AltitudeMode = 16;

    /**
     * At the moment, this refers to the optional range fan labels.
     */
    public static final int HideOptionalLabels = 17;

    public static final int UsePatternFill = 18;

    public static final int PatternFillType = 19;

    /**
     * Set the modifier color for unit modifiers. If you want to change the color of the echelon indicator you
     * still have to use {@link #TextColor}
     */
    public static final int ModifierColor = 20;

    public static ArrayList<Integer> GetModifierList()
    {
        ArrayList<Integer> list = new ArrayList<Integer>();

        list.add(LineColor);
        list.add(FillColor);
        list.add(IconColor);
        list.add(FontSize);
        list.add(PixelSize);
        list.add(Scale);
        list.add(KeepUnitRatio);
        list.add(Alpha);
        list.add(OutlineSymbol);
        list.add(OutlineColor);
        //list.add(OutlineWidth);
        list.add(DrawAsIcon);
        list.add(SymbologyStandard);
        list.add(HideOptionalLabels);
        list.add(ModifierColor);

        return list;
    }
}
