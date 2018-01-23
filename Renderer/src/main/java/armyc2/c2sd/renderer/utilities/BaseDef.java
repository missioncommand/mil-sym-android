package armyc2.c2sd.renderer.utilities;

/** 
 * Consolidates the {@link UnitDef} and {@link SymbolDef}'s common fields into a base class.
 * This can be used to make designing a symbol picker easier.
 *
 * @see UnitDefTable
 * @see SymbolDefTable
 * @see UnitDef
 * @see SymbolDef
 */

public abstract class BaseDef {

    protected String _basicSymbolId = "";
    protected String _description = "";
    protected int _drawCategory = 0;
    protected String _hierarchy = "";
    protected String _path = "";
    protected int _intMinPoints = 1;
    protected int _intMaxPoints = 1;

    /**
     * The basic 15 character basic symbol Id.
     */
    public String getBasicSymbolId()
    {
        return _basicSymbolId;
    }


    /**
     * The description of this tactical graphic.  Typically the name of the tactical graphic in MIL-STD-2525B.
     */
    public String getDescription()
    {
        return _description;
    }

    /**
     * How does this draw? (autoshape, superautoshape, polygon)
     * See implementation class public static final int's for possible values
     *
     * For UnitDef's<br>
     * <ul>
     *     <li>8 is singlepoint unit</li>
     *     <li>0 is category (do not draw because it's just a category node in the tree)</li>
     * </ul>
     *
     *
     */
    public int getDrawCategory()
    {
        return _drawCategory;
    }


    /**
     * Defines where the symbol goes in the ms2525 hierarchy.
     * 2.X.whatever
     */

    public String getHierarchy()
    {
        return _hierarchy;
    }



    /**
     * Defines where the symbol goes in the ms2525 hierarchy.
     * Warfighting/something/something
     * TacticalGraphics/Areas/stuff...
     */
    public String getFullPath()
    {
        return _path;
    }


    /**
     * Defines the minimum points fields.
     */
    public int getMinPoints()
    {
        return _intMinPoints;
    }

    /**
     * Defines the maximum points fields.
     */
    public int getMaxPoints()
    {
        return _intMaxPoints;
    }

    /**
     * returns true if this is a multipoint symbol
     * @return
     */
    public Boolean isMultiPoint()
    {
        char codingScheme = _basicSymbolId.charAt(0);
        Boolean returnVal = false;
        if (codingScheme == 'G' || codingScheme == 'W')
        {

            if(_intMaxPoints > 1)
            {
                returnVal = true;
            }
            else
            {
                switch(_drawCategory)
                {
                    case SymbolDef.DRAW_CATEGORY_RECTANGULAR_PARAMETERED_AUTOSHAPE:
                    case SymbolDef.DRAW_CATEGORY_SECTOR_PARAMETERED_AUTOSHAPE:
                    case SymbolDef.DRAW_CATEGORY_TWO_POINT_RECT_PARAMETERED_AUTOSHAPE:
                    case SymbolDef.DRAW_CATEGORY_CIRCULAR_PARAMETERED_AUTOSHAPE:
                    case SymbolDef.DRAW_CATEGORY_CIRCULAR_RANGEFAN_AUTOSHAPE:
                    case SymbolDef.DRAW_CATEGORY_ROUTE:
                        returnVal = true;
                        break;
                    default:
                        returnVal = false;
                }
            }
            return returnVal;
        }
        else if(_basicSymbolId.startsWith("BS_") || _basicSymbolId.startsWith("BBS_") || _basicSymbolId.startsWith("PBS_"))
        {
            return true;
        }
        else
        {
            return false;
        }
    }
}
