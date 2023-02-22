package armyc2.c2sd.renderer.utilities;

public class SymbolID {
/*
    //Version, 1-2

    //Standard Identity, First Digit (3)
    public static final char StandardIdentity_Context_Reality = '0';
    public static final char StandardIdentity_Context_Exercise = '1';
    public static final char StandardIdentity_Context_Simulation = '2';

    //Standard Identity, Second Digit (4)
    public static final char StandardIdentity_Affiliation_Pending = '0';
    public static final char StandardIdentity_Affiliation_Unknown = '1';
    public static final char StandardIdentity_Affiliation_AssumedFriend = '2';
    public static final char StandardIdentity_Affiliation_Friend = '3';
    public static final char StandardIdentity_Affiliation_Neutral = '4';
    public static final char StandardIdentity_Affiliation_Suspect_Joker = '5';
    public static final char StandardIdentity_Affiliation_Hostile_Faker = '6';

    //Symbol Set, 2 Digits (5-6)
    public static final String SymbolSet_Unknown = "00";
    public static final String SymbolSet_Air = "01";
    public static final String SymbolSet_AirMissile = "02";
    public static final String SymbolSet_Space = "05";
    public static final String SymbolSet_SpaceMissile = "06";
    public static final String SymbolSet_LandUnit = "10";
    public static final String SymbolSet_LandCivilianUnit_Organization = "11";
    public static final String SymbolSet_LandEquipment = "15";
    public static final String SymbolSet_LandInstallation = "20";
    public static final String SymbolSet_ControlMeasure = "25";
    public static final String SymbolSet_SeaSurface = "30";
    public static final String SymbolSet_SeaSubsurface = "35";
    public static final String SymbolSet_MineWarfare = "36";
    public static final String SymbolSet_Activities = "40";
    public static final String SymbolSet_Atmospheric = "45";
    public static final String SymbolSet_Oceanographic = "46";
    public static final String SymbolSet_MeteorologicalSpace = "47";
    public static final String SymbolSet_SignalsIntelligence_Space = "50";
    public static final String SymbolSet_SignalsIntelligence_Air = "51";
    public static final String SymbolSet_SignalsIntelligence_Land = "52";
    public static final String SymbolSet_SignalsIntelligence_Surface = "53";
    public static final String SymbolSet_SignalsIntelligence_Subsurface = "54";
    public static final String SymbolSet_CyberSpace = "60";
    public static final String SymbolSet_VersionExtensionFlag = "99";

    //Status, 1 Digit
    public static final char Status_Present = '0';
    public static final char Status_Planned_Anticipated_Suspect = '1';
    public static final char Status_Present_FullyCapable = '2';
    public static final char Status_Present_Damaged = '3';
    public static final char Status_Present_Destroyed = '4';
    public static final char Status_Present_FullToCapacity = '5';
    public static final char Status_Present_VersionExtensionFlag = '9';

    //Headquarters/Task Force/Dummy
    public static final char HQTFD_Unknown = '0';
    public static final char HQTFD_FeintDummy = '1';
    public static final char HQTFD_Headquarters = '2';
    public static final char HQTFD_FeintDummy_Headquarters = '3';
    public static final char HQTFD_TaskForce = '4';
    public static final char HQTFD_FeintDummy_TaskForce = '5';
    public static final char HQTFD_TaskForce_Headquarters = '6';
    public static final char HQTFD_FeintDummy_TaskForce_Headquarters = '7';
    public static final char HQTFD_VersionExtensionFlag = '9';

    //Echelon/Mobility/Towed Array Amplifier
    public static final String Echelon_Unknown = "00";
    public static final String Echelon_Team_Crew = "11";
    public static final String Echelon_Squad = "12";
    public static final String Echelon_Section = "13";
    public static final String Echelon_Platoon_Detachment = "14";
    public static final String Echelon_Company_Battery_Troop = "15";
    public static final String Echelon_Battalion_Squadron = "16";
    public static final String Echelon_Regiment_Group = "17";
    public static final String Echelon_Brigade = "18";
    public static final String Echelon_VersionExtensionFlag = "19";
    public static final String Echelon_Division = "21";
    public static final String Echelon_Corps_MEF = "22";
    public static final String Echelon_Army = "23";
    public static final String Echelon_ArmyGroup_Front = "24";
    public static final String Echelon_Region_Theater = "25";
    public static final String Echelon_Region_Command = "26";
    public static final String Echelon_VersionExtensionFlag2 = "29";

    public static final String Mobility_Unknown = "00";
    //equipment mobility on land
    public static final String Mobility_WheeledLimitedCrossCountry = "31";
    public static final String Mobility_WheeledCrossCountry = "32";
    public static final String Mobility_Tracked = "33";
    public static final String Mobility_Wheeled_Tracked = "34";
    public static final String Mobility_Towed = "35";
    public static final String Mobility_Rail = "36";
    public static final String Mobility_PackAnimals = "37";
    //equipment mobility on snow
    public static final String Mobility_OverSnow = "41";
    public static final String Mobility_Sled = "42";
    //equipment mobility on water
    public static final String Mobility_Barge = "51";
    public static final String Mobility_Amphibious = "52";
    //naval towed array
    public static final String Mobility_ShortTowedArray = "61";
    public static final String Mobility_LongTowedArray = "62";//*/


    //Version, 1-2 (Can't start with zero, will be 10 at a minimum)

    //Standard Identity, First Digit (3)
    public static final int StandardIdentity_Context_Reality = 0;
    public static final int StandardIdentity_Context_Exercise = 1;
    public static final int StandardIdentity_Context_Simulation = 2;

    //Standard Identity, Second Digit (4)
    public static final int StandardIdentity_Affiliation_Pending = 0;
    public static final int StandardIdentity_Affiliation_Unknown = 1;
    public static final int StandardIdentity_Affiliation_AssumedFriend = 2;
    public static final int StandardIdentity_Affiliation_Friend = 3;
    public static final int StandardIdentity_Affiliation_Neutral = 4;
    public static final int StandardIdentity_Affiliation_Suspect_Joker = 5;
    public static final int StandardIdentity_Affiliation_Hostile_Faker = 6;

    //Symbol Set, 2 Digits (5-6)
    public static final int SymbolSet_Unknown = 00;
    public static final int SymbolSet_Air = 01;
    public static final int SymbolSet_AirMissile = 02;
    public static final int SymbolSet_Space = 05;
    public static final int SymbolSet_SpaceMissile = 06;
    public static final int SymbolSet_LandUnit = 10;
    public static final int SymbolSet_LandCivilianUnit_Organization = 11;
    public static final int SymbolSet_LandEquipment = 15;
    public static final int SymbolSet_LandInstallation = 20;
    public static final int SymbolSet_ControlMeasure = 25;
    public static final int SymbolSet_SeaSurface = 30;
    public static final int SymbolSet_SeaSubsurface = 35;
    public static final int SymbolSet_MineWarfare = 36;
    public static final int SymbolSet_Activities = 40;
    public static final int SymbolSet_Atmospheric = 45;
    public static final int SymbolSet_Oceanographic = 46;
    public static final int SymbolSet_MeteorologicalSpace = 47;
    public static final int SymbolSet_SignalsIntelligence_Space = 50;
    public static final int SymbolSet_SignalsIntelligence_Air = 51;
    public static final int SymbolSet_SignalsIntelligence_Land = 52;
    public static final int SymbolSet_SignalsIntelligence_SeaSurface = 53;
    public static final int SymbolSet_SignalsIntelligence_SeaSubsurface = 54;
    public static final int SymbolSet_CyberSpace = 60;
    public static final int SymbolSet_VersionExtensionFlag = 99;

    //Status, 1 Digit
    public static final int Status_Present = 0;
    public static final int Status_Planned_Anticipated_Suspect = 1;
    public static final int Status_Present_FullyCapable = 2;
    public static final int Status_Present_Damaged = 3;
    public static final int Status_Present_Destroyed = 4;
    public static final int Status_Present_FullToCapacity = 5;
    public static final int Status_Present_VersionExtensionFlag = 9;

    //Headquarters/Task Force/Dummy
    public static final int HQTFD_Unknown = 0;
    public static final int HQTFD_FeintDummy = 1;
    public static final int HQTFD_Headquarters = 2;
    public static final int HQTFD_FeintDummy_Headquarters = 3;
    public static final int HQTFD_TaskForce = 4;
    public static final int HQTFD_FeintDummy_TaskForce = 5;
    public static final int HQTFD_TaskForce_Headquarters = 6;
    public static final int HQTFD_FeintDummy_TaskForce_Headquarters = 7;
    public static final int HQTFD_VersionExtensionFlag = 9;

    //Echelon/Mobility/Towed Array Amplifier
    public static final int Echelon_Unknown = 00;
    public static final int Echelon_Team_Crew = 11;
    public static final int Echelon_Squad = 12;
    public static final int Echelon_Section = 13;
    public static final int Echelon_Platoon_Detachment = 14;
    public static final int Echelon_Company_Battery_Troop = 15;
    public static final int Echelon_Battalion_Squadron = 16;
    public static final int Echelon_Regiment_Group = 17;
    public static final int Echelon_Brigade = 18;
    public static final int Echelon_VersionExtensionFlag = 19;
    public static final int Echelon_Division = 21;
    public static final int Echelon_Corps_MEF = 22;
    public static final int Echelon_Army = 23;
    public static final int Echelon_ArmyGroup_Front = 24;
    public static final int Echelon_Region_Theater = 25;
    public static final int Echelon_Region_Command = 26;
    public static final int Echelon_VersionExtensionFlag2 = 29;

    public static final int Mobility_Unknown = 00;
    //equipment mobility on land
    public static final int Mobility_WheeledLimitedCrossCountry = 31;
    public static final int Mobility_WheeledCrossCountry = 32;
    public static final int Mobility_Tracked = 33;
    public static final int Mobility_Wheeled_Tracked = 34;
    public static final int Mobility_Towed = 35;
    public static final int Mobility_Rail = 36;
    public static final int Mobility_PackAnimals = 37;
    //equipment mobility on snow
    public static final int Mobility_OverSnow = 41;
    public static final int Mobility_Sled = 42;
    //equipment mobility on water
    public static final int Mobility_Barge = 51;
    public static final int Mobility_Amphibious = 52;
    //naval towed array
    public static final int Mobility_ShortTowedArray = 61;
    public static final int Mobility_LongTowedArray = 62;


    public static String reconcileSymbolID(String symbolID)
    {
        String id = symbolID;
        /*
        StringBuilder sb = new StringBuilder();
        if(symbolID == null)
            return null;

        int length = symbolID.length();

        if(length < 20)
        {
            while(id.length() < 20)
            {
                id += "0";
            }
        }

        if(length >= 20)
        {

        }
        */



        return id;
    }



    public static int getStandardIdentity(String symbolID)
    {
        if(symbolID != null && symbolID.length() >= 20)
        {
            return Integer.parseInt(symbolID.substring(2, 4));
        }
        else
        {
            return 00;
        }
    }

    public static String setStandardIdentity(String symbolID, int si)
    {
        String strSI = String.valueOf(si);

        if(symbolID != null && symbolID.length() >= 20)
        {
            if(si < 10 && strSI.length() == 1)
                strSI = "0" + strSI;
            else
                strSI = String.valueOf(si);

            return symbolID.substring(0,2) + strSI + symbolID.substring(4);
        }
        else
        {
            return symbolID;
        }
    }


    /**
     * Get Context (Reality (0), Exercise (1), Simulation (2))
     * @param symbolID
     * @return
     */
    public static int getContext(String symbolID)
    {
        if(symbolID != null && symbolID.length() >= 20)
        {
            return Integer.parseInt(symbolID.substring(2,3));
        }
        else
        {
            return 0;
        }
    }

    /**
     * Set Context (Reality (0), Exercise (1), Simulation (2))
     * @param symbolID
     * @param context
     * @return
     */
    public static String setContext(String symbolID, int context)
    {
        if(symbolID != null && symbolID.length() >= 20 && context < 4)
        {
            return symbolID.substring(0,2) + String.valueOf(context) + symbolID.substring(3);
        }
        else
        {
            return symbolID;
        }
    }

    public static int getAffiliation(String symbolID)
    {
        if(symbolID != null && symbolID.length() >= 20)
        {
            return Integer.parseInt(symbolID.substring(3,4));
        }
        else
        {
            return 0;
        }
    }

    public static String setAffiliation(String symbolID, int affiliation)
    {
        if(symbolID != null && symbolID.length() >= 20)
        {
            return symbolID.substring(0,3) + String.valueOf(affiliation) + symbolID.substring(4);
//            return Integer.parseInt(symbolID.substring(2,4));
        }
        else
        {
            return symbolID;
        }
    }

    public static int getSymbolSet(String symbolID)
    {
        if(symbolID != null && symbolID.length() >= 20)
        {
            return Integer.parseInt(symbolID.substring(4, 6));
        }
        else
        {
            return 00;
        }
    }

    public static String setSymbolSet(String symbolID, int ss)
    {
        String strSS = String.valueOf(ss);
        if(ss < 10 && strSS.length()==1)
            strSS = "0" + strSS;
        if(symbolID != null && symbolID.length() >= 20)
        {
            return symbolID.substring(0,4) + strSS + symbolID.substring(6);
        }
        else
        {
            return symbolID;
        }
    }

    public static int getStatus(String symbolID)
    {
        if(symbolID != null && symbolID.length() >= 20)
        {
            return Integer.parseInt(symbolID.substring(6,7));
        }
        else
        {
            return 0;
        }
    }

    public static String setStatus(String symbolID, int status)
    {
        String strStatus = String.valueOf(status);

        if(symbolID != null && symbolID.length() >= 20 && strStatus.length() == 1)
        {
            return symbolID.substring(0,6) + strStatus + symbolID.substring(7);
        }
        else
        {
            return symbolID;
        }
    }

    public static int getHQTFD(String symbolID)
    {
        if(symbolID != null && symbolID.length() >= 20)
        {
            return Integer.parseInt(symbolID.substring(7,8));
        }
        else
        {
            return 0;
        }
    }

    public static String setHQTFD(String symbolID, int HQTFD)
    {
        String strHQTFD = String.valueOf(HQTFD);

        if(symbolID != null && symbolID.length() >= 20 && strHQTFD.length() == 1)
        {
            return symbolID.substring(0,7) + strHQTFD + symbolID.substring(8);
        }
        else
        {
            return symbolID;
        }
    }

    /**
     * get Echelon / Mobility / Towed Array
     * @param symbolID
     * @return
     */
    public static int getAmplifierDescriptor(String symbolID)
    {
        if(symbolID != null && symbolID.length() >= 20)
        {
            return Integer.parseInt(symbolID.substring(8, 10));
        }
        else
        {
            return 00;
        }
    }

    /**
     * set Echelon / Mobility / Towed Array
     * @param symbolID
     * @param ad
     * @return
     */
    public static String setAmplifierDescriptor(String symbolID, int ad)
    {
        String strAD = String.valueOf(ad);
        if(ad < 10 && strAD.length() == 1)
            strAD = "0" + strAD;

        if(symbolID != null && symbolID.length() >= 20 && strAD.length() == 2)
        {
            return symbolID.substring(0,8) + strAD + symbolID.substring(10);
        }
        else
        {
            return symbolID;
        }
    }


    public static int getEntityCode(String symbolID)
    {
        if(symbolID != null && symbolID.length() >= 20)
        {
            return Integer.parseInt(symbolID.substring(10, 16));
        }
        else
        {
            return 000000;
        }
    }

    public static int getEntity(String symbolID)
    {
        if(symbolID != null && symbolID.length() >= 20)
        {
            return Integer.parseInt(symbolID.substring(10, 12));
        }
        else
        {
            return 00;
        }
    }

    public static int getEntityType(String symbolID)
    {
        if(symbolID != null && symbolID.length() >= 20)
        {
            return Integer.parseInt(symbolID.substring(12, 14));
        }
        else
        {
            return 00;
        }
    }

    public static int getEntitySubtype(String symbolID)
    {
        if(symbolID != null && symbolID.length() >= 20)
        {
            return Integer.parseInt(symbolID.substring(14, 16));
        }
        else
        {
            return 00;
        }
    }

    public static int getModifier1(String symbolID)
    {
        if(symbolID != null && symbolID.length() >= 20)
        {
            return Integer.parseInt(symbolID.substring(16, 18));
        }
        else
        {
            return 00;
        }
    }

    public static int getModifier2(String symbolID)
    {
        if(symbolID != null && symbolID.length() >= 20)
        {
            return Integer.parseInt(symbolID.substring(18, 20));
        }
        else
        {
            return 00;
        }
    }

    /*
    public static String getStandardIdentity(String symbolID)
    {
        if(symbolID != null && symbolID.length() >= 20)
        {
            return symbolID.substring(2, 4);
        }
        else
        {
            return "00";
        }
    }

    public static char getContext(String symbolID)
    {
        if(symbolID != null && symbolID.length() >= 20)
        {
            return symbolID.charAt(3);
        }
        else
        {
            return '0';
        }
    }

    public static char getAffiliation(String symbolID)
    {
        if(symbolID != null && symbolID.length() >= 20)
        {
            return symbolID.charAt(4);
        }
        else
        {
            return '0';
        }
    }

    public static String getSymbolSet(String symbolID)
    {
        if(symbolID != null && symbolID.length() >= 20)
        {
            return symbolID.substring(4, 6);
        }
        else
        {
            return "00";
        }
    }

    public static char getStatus(String symbolID)
    {
        if(symbolID != null && symbolID.length() >= 20)
        {
            return symbolID.charAt(6);
        }
        else
        {
            return '0';
        }
    }

    public static char getHQTFD(String symbolID)
    {
        if(symbolID != null && symbolID.length() >= 20)
        {
            return symbolID.charAt(7);
        }
        else
        {
            return '0';
        }
    }


    //  get Echelon / Mobility / Towed Array
    //  @param symbolID
    //  @return

    public static String getAmplifierDescriptor(String symbolID)
    {
        if(symbolID != null && symbolID.length() >= 20)
        {
            return symbolID.substring(8, 10);
        }
        else
        {
            return "00";
        }
    }

    public static String getEntityCode(String symbolID)
    {
        if(symbolID != null && symbolID.length() >= 20)
        {
            return symbolID.substring(10, 16);
        }
        else
        {
            return "000000";
        }
    }

    public static String getEntity(String symbolID)
    {
        if(symbolID != null && symbolID.length() >= 20)
        {
            return symbolID.substring(10, 12);
        }
        else
        {
            return "00";
        }
    }

    public static String getEntityType(String symbolID)
    {
        if(symbolID != null && symbolID.length() >= 20)
        {
            return symbolID.substring(12, 14);
        }
        else
        {
            return "00";
        }
    }

    public static String getEntitySubtype(String symbolID)
    {
        if(symbolID != null && symbolID.length() >= 20)
        {
            return symbolID.substring(14, 16);
        }
        else
        {
            return "00";
        }
    }

    public static String getModifier1(String symbolID)
    {
        if(symbolID != null && symbolID.length() >= 20)
        {
            return symbolID.substring(16, 18);
        }
        else
        {
            return "00";
        }
    }

    public static String getModifier2(String symbolID)
    {
        if(symbolID != null && symbolID.length() >= 20)
        {
            return symbolID.substring(18, 20);
        }
        else
        {
            return "00";
        }
    }//*/

    public static Boolean isMETOC(String symbolID) {
        int ss = SymbolID.getSymbolSet(symbolID);
        //SymbolID.SymbolSet_Atmospheric
        //SymbolID.SymbolSet_Oceanographic
        //SymbolID.SymbolSet_MeteorologicalSpace
        if (ss >= 45 && ss <= 47)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public static Boolean isTacticalGraphic(String symbolID) {
        if (SymbolID.getSymbolSet(symbolID) == SymbolID.SymbolSet_ControlMeasure)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    //Functions to load SVG data
    public static String getFrameID(String symbolID) {
        //SIDC positions 3_456_7
        String frameID = symbolID.charAt(2) + "_" + symbolID.substring(3, 6) + "_" + symbolID.charAt(6);
        return frameID;
    }

    public static String getMainIconID(String symbolID) {
        //SIDC positions 5-6 + 11-16
        String mainIconID = symbolID.substring(4, 6) + symbolID.substring(10, 16);
        return mainIconID;
    }

    public static String getFillID(String symbolID) {
        return "";
    }

    public static String getMod2ID(String symbolID) {
        //SIDC positions 5-6 + 19-20 + "2"
        String mod2ID = symbolID.substring(4, 6) + symbolID.substring(18, 20) + "0";
        return mod2ID;
    }

    public static String getMod1ID(String symbolID) {
        //SIDC positions 5-6 + 17-18 + "1"
        String mod1ID = symbolID.substring(4, 6) + symbolID.substring(16, 18) + "1";
        return mod1ID;
    }
}


