package armyc2.c2sd.renderer.utilities;

public class MSLInfo {

    private String _ID = null;
    private String _Name = null;
    private String _Path = null;
    private String _SymbolSet = null;
    private String _Entity = null;
    private String _EntityType = null;
    private String _EntitySubType = null;
    private String _EntityCode = null;
    private String _Geometry = "point";
    private int _MinPointCount = 0;
    private int _MaxPointCount = 0;
    private int _DrawRule = 0;

    /**
     *
     * @param symbolSet the 5th & 6th character in the symbol Code, represents Battle Dimension
     * @param entity descriptor
     * @param entityType descriptor
     * @param entitySubType  descriptor
     * @param entityCode characters 11 - 16 in the symbol code
     */
    public MSLInfo(String symbolSet, String entity, String entityType, String entitySubType, String entityCode)
    {
        _ID = symbolSet + entityCode;
        _SymbolSet = parseSymbolSetName(symbolSet);
        if(entitySubType != null && !entitySubType.equals(""))
        {
            _Name = entitySubType;
            _EntitySubType = entitySubType;
            _Path = _SymbolSet + " / " + entity + " / " + entityType + " / ";
        }
        if(entityType != null && !entityType.equals(""))
        {
            _EntityType = entityType;
            if(_Name == null)
                _Name = entityType;
            if(_Path == null)
                _Path = _SymbolSet + " / " + entity + " / ";
        }
        if(entity != null && !entity.equals(""))
        {
            _Entity = entity;
            if(_Name == null)
                _Name = entity;
            if(_Path == null)
                _Path = _SymbolSet + " / ";
        }
        if(entityCode != null & entityCode.length()==6)
            _EntityCode = entityCode;

        _Geometry = "point";

        int rule = DrawRules.DONOTDRAW;
        if(!entityCode.equals("000000"))
            rule = DrawRules.POINT1;
        _DrawRule = rule;


        _MinPointCount = 1;
        _MaxPointCount = 1;
    }

    /**
     *
     * @param symbolSet the 5th & 6th character in the symbol Code, represents Battle Dimension
     * @param entity descriptor
     * @param entityType descriptor
     * @param entitySubType  descriptor
     * @param entityCode characters 11 - 16 in the symbol code
     * @param geometry "point", "line", "area"
     * @param drawRule as defined in 2525D for Control Measures and METOC (i.e. "Point1")
     */
    public MSLInfo(String symbolSet, String entity, String entityType, String entitySubType, String entityCode, String geometry, String drawRule)
    {
        _ID = symbolSet + entityCode;
        _SymbolSet = parseSymbolSetName(symbolSet);
        if(entitySubType != null && !entitySubType.equals(""))
        {
            _Name = entitySubType;
            _EntitySubType = entitySubType;
        }
        if(entityType != null && !entityType.equals(""))
        {
            _EntityType = entityType;
            if(_Name == null)
                _Name = entityType;
        }
        if(entity != null && !entity.equals(""))
        {
            _Entity = entity;
            if(_Name == null)
                _Name = entity;
        }
        if(entityCode != null & entityCode.length()==6)
            _EntityCode = entityCode;

        _Geometry = geometry;

        _DrawRule = parseDrawRule(drawRule);

        int[] pointCounts = null;
        if(symbolSet.equals("25"))
        {
            pointCounts = getMinMaxPointsFromDrawRule(_DrawRule);
        }
        else if(symbolSet.equals("45") || symbolSet.equals("46"))
        {//Atmospheric, Oceanographic, Meteorological Space (last one has no symbols so not included)
            pointCounts = getMinMaxPointsFromMODrawRule(_DrawRule);
        }
        _MinPointCount = pointCounts[0];
        _MaxPointCount = pointCounts[1];
    }

    private int parseDrawRule(String drawRule)
    {
        String dr = drawRule.toLowerCase();
        int idr = 0;
        switch(dr)
        {
            case "area1":
                idr = DrawRules.AREA1;
                break;
            case "area2":
                idr = DrawRules.AREA2;
                break;
            case "area3":
                idr = DrawRules.AREA3;
                break;
            case "area4":
                idr = DrawRules.AREA4;
                break;
            case "area5":
                idr = DrawRules.AREA5;
                break;
            case "area6":
                idr = DrawRules.AREA6;
                break;
            case "area7":
                idr = DrawRules.AREA7;
                break;
            case "area8":
                idr = DrawRules.AREA8;
                break;
            case "area9":
                idr = DrawRules.AREA9;
                break;
            case "area10":
                idr = DrawRules.AREA10;
                break;
            case "area11":
                idr = DrawRules.AREA11;
                break;
            case "area12":
                idr = DrawRules.AREA12;
                break;
            case "area13":
                idr = DrawRules.AREA13;
                break;
            case "area14":
                idr = DrawRules.AREA14;
                break;
            case "area15":
                idr = DrawRules.AREA15;
                break;
            case "area16":
                idr = DrawRules.AREA16;
                break;
            case "area17":
                idr = DrawRules.AREA17;
                break;
            case "area18":
                idr = DrawRules.AREA18;
                break;
            case "area19":
                idr = DrawRules.AREA19;
                break;
            case "area20":
                idr = DrawRules.AREA20;
                break;
            case "area21":
                idr = DrawRules.AREA21;
                break;
            case "area22":
                idr = DrawRules.AREA22;
                break;
            case "area23":
                idr = DrawRules.AREA23;
                break;
            case "area24":
                idr = DrawRules.AREA24;
                break;
            case "area25":
                idr = DrawRules.AREA25;
                break;
            case "area26":
                idr = DrawRules.AREA26;
                break;
            case "point1":
                idr = DrawRules.POINT1;
                break;
            case "point2":
                idr = DrawRules.POINT2;
                break;
            case "point3":
                idr = DrawRules.POINT3;
                break;
            case "point4":
                idr = DrawRules.POINT4;
                break;
            case "point5":
                idr = DrawRules.POINT5;
                break;
            case "point6":
                idr = DrawRules.POINT6;
                break;
            case "point7":
                idr = DrawRules.POINT7;
            case "point8":
                idr = DrawRules.POINT8;
                break;
            case "point9":
                idr = DrawRules.POINT9;
                break;
            case "point10":
                idr = DrawRules.POINT10;
                break;
            case "point11":
                idr = DrawRules.POINT11;
                break;
            case "point12":
                idr = DrawRules.POINT12;
                break;
            case "point13":
                idr = DrawRules.POINT13;
                break;
            case "point14":
                idr = DrawRules.POINT14;
                break;
            case "point15":
                idr = DrawRules.POINT15;
                break;
            case "point16":
                idr = DrawRules.POINT16;
                break;
            case "point17":
                idr = DrawRules.POINT17;
                break;
            case "point18":
                idr = DrawRules.POINT18;
                break;
            case "line1":
                idr = DrawRules.LINE1;
                break;
            case "line2":
                idr = DrawRules.LINE2;
                break;
            case "line3":
                idr = DrawRules.LINE3;
                break;
            case "line4":
                idr = DrawRules.LINE4;
                break;
            case "line5":
                idr = DrawRules.LINE5;
                break;
            case "line6":
                idr = DrawRules.LINE6;
                break;
            case "line7":
                idr = DrawRules.LINE7;
                break;
            case "line8":
                idr = DrawRules.LINE8;
                break;
            case "line9":
                idr = DrawRules.LINE9;
                break;
            case "line10":
                idr = DrawRules.LINE10;
                break;
            case "line11":
                idr = DrawRules.LINE11;
                break;
            case "line12":
                idr = DrawRules.LINE12;
                break;
            case "line13":
                idr = DrawRules.LINE13;
                break;
            case "line14":
                idr = DrawRules.LINE14;
                break;
            case "line15":
                idr = DrawRules.LINE15;
                break;
            case "line16":
                idr = DrawRules.LINE16;
                break;
            case "line17":
                idr = DrawRules.LINE17;
                break;
            case "line18":
                idr = DrawRules.LINE18;
                break;
            case "line19":
                idr = DrawRules.LINE19;
                break;
            case "line20":
                idr = DrawRules.LINE20;
                break;
            case "line21":
                idr = DrawRules.LINE21;
                break;
            case "line22":
                idr = DrawRules.LINE22;
                break;
            case "line23":
                idr = DrawRules.LINE23;
                break;
            case "line24":
                idr = DrawRules.LINE24;
                break;
            case "line25":
                idr = DrawRules.LINE25;
                break;
            case "line26":
                idr = DrawRules.LINE26;
                break;
            case "line27":
                idr = DrawRules.LINE27;
                break;
            case "line28":
                idr = DrawRules.LINE28;
                break;
            case "line29":
                idr = DrawRules.LINE29;
                break;
            case "corridor1":
                idr = DrawRules.CORRIDOR1;
                break;
            case "axis1":
                idr = DrawRules.AXIS1;
                break;
            case "axis2":
                idr = DrawRules.AXIS2;
                break;
            case "polyline1":
                idr = DrawRules.POLYLINE1;
                break;
            case "ellipse1":
                idr = DrawRules.ELLIPSE1;
                break;
            case "rectangular1":
                idr = DrawRules.RECTANGULAR1;
                break;
            case "rectangular2":
                idr = DrawRules.RECTANGULAR2;
                break;
            case "rectangular3":
                idr = DrawRules.RECTANGULAR3;
                break;
            case "circular1":
                idr = DrawRules.CIRCULAR1;
                break;
            case "circular2":
                idr = DrawRules.CIRCULAR2;
                break;
            case "arc1":
                idr = DrawRules.ARC1;
                break;
            default:
                idr = DrawRules.DONOTDRAW;
        }
        return idr;
    }

    private String parseSymbolSetName(String ss)
    {
        String name = null;
        switch(ss)
        {
            case "01":
                name = "Air";
                break;
            case "02":
                name = "Air Missile";
                break;
            case "05":
                name = "Space";
                break;
            case "06":
                name = "Space Missile";
                break;
            case "10":
                name = "Land Unit";
                break;
            case "11":
                name = "Land Civilian Unit-Org";
                break;
            case "15":
                name = "Land Equipment";
                break;
            case "20":
                name = "Land Installations";
                break;
            case "25":
                name = "Control Measure";
                break;
            case "30":
                name = "Sea Surface";
                break;
            case "35":
                name = "Sea Subsurface";
                break;
            case "36":
                name = "Mine Warfare";
                break;
            case "40":
                name = "Activities";
                break;
            case "45":
                name = "Atmospheric";
                break;
            case "46":
                name = "Oceanographic";
                break;
            case "47":
                name = "Meteorological Space";
                break;
            case "50":
                name = "Space SIGINT";
                break;
            case "51":
                name = "Air SIGINT";
                break;
            case "52":
                name = "Land SIGINT";
                break;
            case "53":
                name = "Sea Surface SIGINT";
                break;
            case "54":
                name = "Sea Subsurface SIGINT";
                break;
            case "60":
                name = "Cyberspace";
                break;
            default:
                name = "UNKNOWN";
        }
        return name;
    }

    /**
     *
     * @param drawRule - Like DrawRules.CIRCULAR2
     * @return int[] where the first index is the minimum required points and
     * the next index is the maximum allowed points
     */
    private static int[] getMinMaxPointsFromDrawRule(int drawRule)
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

    private static int[] getMinMaxPointsFromMODrawRule(int drawRule)
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

    public String getName()
    {
        return _Name;
    }

    public String getPath()
    {
        return _Path;
    }

    public String getGeometry()
    {
        return _Geometry;
    }

    public int getDrawRule()
    {
        return _DrawRule;
    }

    public int getSymbolSetInt()
    {
        return Integer.valueOf(_EntityCode.substring(0,2));
    }

    public int getMinPointCount()
    {
        return _MinPointCount;
    }

    public int getMaxPointCount()
    {
        return _MaxPointCount;
    }
}
