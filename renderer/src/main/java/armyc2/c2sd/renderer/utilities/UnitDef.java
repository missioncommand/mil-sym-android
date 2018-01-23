/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package armyc2.c2sd.renderer.utilities;

/**
*
* @author michael.spinelli
*/
public class UnitDef extends BaseDef {

	/**
	* Just a category in the milstd hierarchy.
	* Not something we draw.
	* WILL NOT RENDER
	*/
   static public final int DRAW_CATEGORY_DONOTDRAW = 0;
    /**
	* Shape is defined by a single point
	* 0 control points
	*/
   static public final int DRAW_CATEGORY_POINT = 8;


   /**
    * 
    * @param basicSymbolID
    * @param description
    * @param drawCategory
    * @param hierarchy
    * @param path
    */
   public UnitDef(String basicSymbolID, String description, int drawCategory, String hierarchy, String path)
   {
           //Set fields to their default values.
           _basicSymbolId = basicSymbolID;
           _description = description;
           _drawCategory = drawCategory;
           _hierarchy = hierarchy;
           _path = path;
           _intMinPoints=1;
           _intMaxPoints=1;
   }





    /**
    * Defines where the symbol goes in the ms2525 hierarchy.
    * STBOPS.INDIV.WHATEVER
    */
   /*private String _strAlphaHierarchy;
   public String getAlphaHierarchy()
   {
           return _strAlphaHierarchy;
   }//*/




}
