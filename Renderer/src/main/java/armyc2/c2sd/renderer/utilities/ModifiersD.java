package armyc2.c2sd.renderer.utilities;

import java.util.ArrayList;

public class ModifiersD {
    /**
     * The innermost part of a symbol, comprised of an icon and optional modifiers,
     * that represents a joint military object (see 5.3.4).
     * Type: U,E,I,SI,A,C
     * Length: G
     */
    public static final String A_SYMBOL_ICON = "A";
    /**
     * A graphic modifier in a unit symbol that identifies command level
     * Comes from the symbol code so this isn't used.
     * (see 5.3.7.1 and table D-III).
     * (see tables D-III and D-V in the Land appendix and figure H-8 and figure H-14).
     * Type: U,A,CM,C
     * Length: G
     * CM: L, A, BL
     * Length: G
     */
    public static final String B_ECHELON = "B";
    /**
     * A text modifier in an equipment symbol that identifies the number of items present.
     * Type: E,CM
     * Length: 9
     * CM: R/N
     * Length: 6
     */
    public static final String C_QUANTITY = "C";
    /**
     * A graphic amplifier that identifies a unit or activities symbol as a task force
     * (see 5.3.7.2 and figure 13).
     * Type: U,A,C
     * Length: G
     */
    public static final String D_TASK_FORCE_INDICATOR = "D";
    /**
     * A graphic modifier that displays standard identity, battle dimension, or exercise
     * amplifying descriptors of an object (see 5.3.1 and table II).
     * Type: U,E,I,A
     * Length: G
     */
    public static final String E_FRAME_SHAPE_MODIFIER = "E";
    /**
     * A text modifier in a unit symbol that displays (+) for reinforced, (-) for reduced,(+) reinforced and reduced.
     * R = reinforced,D = reduced,RD = reinforced and reduced
     * Type: U,A,C
     * Length: 3
     */
    public static final String F_REINFORCED_REDUCED = "F";
    /**
     * A text modifier for units, equipment and installations; content is implementation specific.
     * Type: U,E,I,SI,A,C
     * Length: 20
     */
    public static final String G_STAFF_COMMENTS = "G";
    /**
     * Text modifier for amplifying free text
     * Type: U,E,I,SI,A,CM,C
     * Length: 20
     *
     * A text modifier for tactical graphics; content is
     * implementation specific.
     * CM: P,L,A,N,B/C,R/N
     * Length: 20
     */
    public static final String H_ADDITIONAL_INFO_1 = "H";

    /**
     * A text modifier for tactical graphics; content is
     * implementation specific.
     * CM: P,L,A,N,B/C,R/N
     * Length: 20
     */
    public static final String H1_ADDITIONAL_INFO_2 = "H1";

    /**
     * A text modifier for tactical graphics; content is
     * implementation specific.
     * CM: P,L,A,N,B/C,R/N
     * Length: 20
     */
    public static final String H2_ADDITIONAL_INFO_3 = "H2";

    /**
     * A text modifier for units, equipment, and installations that consists of
     * a one letter reliability rating and a one-number credibility rating.
     Reliability Ratings: A-completely reliable, B-usually reliable,
     C-fairly reliable, D-not usually reliable, E-unreliable,
     F-reliability cannot be judged.
     Credibility Ratings: 1-confirmed by other sources,
     2-probably true, 3-possibly true, 4-doubtfully true,
     5-improbable, 6-truth cannot be judged.
     Type: U,E,I,SI,A
     Length: 2
     */
    public static final String J_EVALUATION_RATING = "J";
    /**
     * A text modifier for units and installations that indicates unit effectiveness or
     * installation capability.
     * Type: U,I,A,C
     * Length: 5,5,3
     */
    public static final String K_COMBAT_EFFECTIVENESS = "K";
    /**
     * A text modifier for hostile equipment; “!” indicates detectable electronic
     * signatures.
     * Type: E,SI,C
     * Length: 1
     */
    public static final String L_SIGNATURE_EQUIP = "L";
    /**
     * A text modifier for units that indicates number or title of higher echelon
     * command (corps are designated by Roman numerals).
     * Type: U,SI,C
     * Length: 21
     */
    public static final String M_HIGHER_FORMATION = "M";
    /**
     * A text modifier for equipment; letters "ENY" denote hostile symbols.
     * Type: E, CM
     * Length: 3
     * CM: P,L,A,N,BL,B/C,R/N
     * Length: 3
     */
    public static final String N_HOSTILE = "N";
    /**
     * A text modifier displaying IFF/SIF/AIS Identification modes and codes.
     * Type: U,E,I,A
     * Length: 15
     */
    public static final String P_IFF_SIF_AIS = "P";
    /**
     * A graphic amplifier for units and equipment that identifies the
     * direction of movement or intended movement of an object
     * (see 5.3.7.8 and figure 13).
     * Type: U,E,A,CM
     * Length: G
     * (see H.5.1.1.5 and figure H-2).
     * CM: B/C,R/N
     * Length: G
     */
    public static final String Q_DIRECTION_OF_MOVEMENT = "Q";
    /**
     * A graphic amplifier for equipment that depicts the mobility of an object
     * (see 5.3.6.9, figure 13 and table VIII).
     * We feed off of the symbol code for mobility so this isn't used
     * Type: E
     * Length: G
     */
    public static final String R_MOBILITY_INDICATOR = "R";
    /**
     * M = Mobile, S = Static, or U = Uncertain.
     * Type: SI
     * Length: 1
     */
    public static final String R2_SIGNIT_MOBILITY_INDICATOR = "R2";
    /**
     * Headquarters staff indicator: A graphic amplifier for units, equipment
     * and installations that identifies a unit as a headquarters
     * (see table D-III and figure 13),
     * Offset location indicator: A graphic amplifier for units, equipment and
     * installations used when placing an object away from its actual location
     * (see 5.3.6.5 and figure 13).
     * Type: U,I,A,C
     * Length: G
     */
    public static final String S_HQ_STAFF_INDICATOR = "S";
    /**
     * Offset indicator: A graphic amplifier A graphic amplifier used to indicate the offset or precise location
     * of a single point symbol (see 5.3.7.4, 5.3.12, figure 13, H.5.1.1.7
     * and figures H-2, H-8, H-12, H-13, H-14).
     * Type: U,E,I,SI,A,CM,C
     * Length: G
     * CM: P,B/C,R/N
     * Length: G
     */
    public static final String S2_OFFSET_INDICATOR = "S2";
    /**
     * A text amplifier for units, equipment and installations that uniquely identifies a particular
     * symbol or track number. Identifies acquisitions number when used with SIGINT symbology.
     * Type: U,E,I,SI,A,CM,C
     * Length: 30
     *
     * A text amplifier that uniquely identifies a particular control measure symbol.
     * Nuclear: delivery unit (missile, aircraft, satellite, etc.)
     * CM:P,L,A,BL,B/C,R/N
     * Length: 30
     */
    public static final String T_UNIQUE_DESIGNATION_1 = "T";
    /**
     * A text amplifier that uniquely identifies a particular control measure symbol.
     Nuclear: delivery unit (missile, aircraft, satellite, etc.)
     * CM:P,L,A,BL,B/C,R/N
     * Length: 30
     */
    public static final String T1_UNIQUE_DESIGNATION_2 = "T1";
    /**
     * A text amplifier for equipment that indicates types of equipment.
     * Type: E,SI
     * Length: 24
     *
     * A text amplifier for equipment that indicates types of equipment or nuclear weapon type.
     * CM: P,L,A,B/C,R/N
     * Length: 20
     */
    public static final String V_EQUIP_TYPE = "V";
    /**
     * An alphanumeric designator for displaying a date-time group (DDHHMMSSZMONYYYY) or “O/O”
     * for on order. The date-time group is composed of a group of six numeric digits with a
     * time zone suffix and the standardized three-letter abbreviation for the month followed
     * by four digits representing the year. The first pair of digits represents the day;
     * the second pair, the hour; the third pair, the minutes. For automated systems, two digits
     * may be added before the time zone suffix and after the minutes to designate seconds.
     * Type: U,E,I,SI,A,CM,C
     * Length: 16
     * CM: P,L,A,B/C,R/N
     * Length: 16
     */
    public static final String W_DTG_1 = "W";
    /**
     * An alphanumeric designator for displaying a date-time group (DDHHMMSSZMONYYYY) or
     * “O/O” for on order. The date-time group is composed of a group of six numeric digits with
     * a time zone suffix and the standardized three-letter abbreviation for the month followed by
     * four digits. The first pair of digits represents the day; the second pair, the hour;
     * the third pair, the minutes. The last four digits after the month are the year.
     * For automated systems, two digits may be added before the time zone suffix and after the
     * minutes to designate seconds.
     * CM: P,L,A,B/C,R/N
     * Length: 16
     */
    public static final String W1_DTG_2 = "W1";
    /**
     * A text amplifier for units, equipment and installations, that displays either altitude,
     * flight level, depth for submerged objects; or height of equipment or
     * structures on the ground. See 5.3.7.5 for content.
     * Type: U,E,I,A,CM
     * Length: 14
     *
     * A text amplifier that displays the minimum, maximum and/or specific altitude
     * (in feet or meters in relation to a reference datum), flight level, or depth
     * (for submerged objects in feet below sea level). See H.5.1.1.9 for content.
     * CM: P,L,A,B/C,R/N
     * Length: 14
     */
    public static final String X_ALTITUDE_DEPTH = "X";
    /**
     * A text amplifier for units, equipment and installations that displays a symbol’s location
     * in degrees, minutes and decimal minutes
     * (or in MGRS, GARS, or other applicable display formats).
     * Type: U,E,I,SI,A,CM,C
     * Length: 22
     *
     * A text amplifier that displays a graphic’s location in degrees, minutes and seconds
     * (or in UTM or other applicable display format).
     * CM: P,L,A,BL,B/C,R/N
     * Length: 22
     */
    public static final String Y_LOCATION = "Y";
    /**
     * A text amplifier for units and equipment that displays velocity (see 5.3.7.6).
     * Type: U,E
     * Length: 8
     */
    public static final String Z_SPEED = "Z";
    /**
     * A text modifier for units; indicator is contained inside the frame;
     * contains the name of the special C2Headquarters.
     * Type: U,A,C
     * Length: 9
     */
    public static final String AA_SPECIAL_C2_HQ = "AA";
    /**
     * Feint or dummy indicator: A graphic amplifier for units, equipment and
     * installations that identifies an offensive or defensive unit intended to
     * draw the enemy’s attention away from the area of the main attack
     * (see 5.3.6.4 and figure 13).
     * Type: U,E,I,A
     * Length: G
     */
    public static final String AB_FEINT_DUMMY_INDICATOR = "AB";
    /**
     * Installation: A graphic amplifier for units, equipment and installations
     * used to show that a particular symbol denotes an installation
     * (see 5.3.6.2 and figure 13).
     * Not used, we feed off of symbol code for this
     * Type: U,E,I,A
     * Length: G
     */
    public static final String AC_INSTALLATION = "AC";
    /**
     * Electronic intelligence notation (ELNOT) or communications intelligence
     * notation (CENOT)
     * Type: E,SI
     * Length: 6
     */
    public static final String AD_PLATFORM_TYPE = "AD";
    /**
     * Equipment teardown time in minutes.
     * Type: E,SI
     * Length: 3
     */
    public static final String AE_EQUIPMENT_TEARDOWN_TIME = "AE";
    /**
     * Example: “Hawk” for Hawk SAM system.
     * Type: E,SI
     * Length: 12
     */
    public static final String AF_COMMON_IDENTIFIER = "AF";
    /**
     * Towed sonar array indicator: A graphic modifier for equipment that indicates the presence
     * of a towed sonar array (see 5.3.7.10, figure 13 and table IX).
     * Type: E
     * Length: G
     */
    public static final String AG_AUX_EQUIP_INDICATOR = "AG";
    /**
     * A graphic modifier for units, equipment and installations that indicates the area where an
     * object is most likely to be, based on the object’s last report and the reporting accuracy
     * of the sensor that detected the object (see 5.3.7.12.1 and table D-III).
     * responsibility of the clien of the renderer to display
     * Type: U,E,I,A
     * Length: G
     */
    public static final String AH_AREA_OF_UNCERTAINTY = "AH";
    /**
     * A graphic amplifier for units and equipment that identifies where an object should be
     * located at present, given its last reported course and speed (see 5.3.7.12.2).
     * Type: U,E,I,A
     * Length: G
     */
    public static final String AI_DEAD_RECKONING_TRAILER = "AI";
    /**
     * A graphic amplifier for units, equipment and installations that depicts the speed and
     * direction of movement of an object (see 5.3.7.12.3 and figure 17).
     * Responsibility of client of renderer to draw
     * Type: U,E,I,A
     * Length: G
     */
    public static final String AJ_SPEED_LEADER = "AJ";
    /**
     * A graphic amplifier for units, equipment and installations that connects two objects and is
     * updated dynamically as the positions of the two objects change (see 5.3.7.12.4 and figure 17).
     * Responsibility of renderer client
     * Type: U,E,I,A
     * Length: G
     */
    public static final String AK_PAIRING_LINE = "AK";
    /**
     * A graphic amplifier for equipment or installations that indicates operational condition or
     * capacity.
     * Type: E,I,A
     * Length: G
     */
    public static final String AL_OPERATIONAL_CONDITION = "AL";

    /**
     * A numeric amplifier that displays a minimum, maximum, or a specific distance
     * (range, radius, width, length, etc.), in meters.
     * CM: P,L,A
     * Length: 7
     */
    public static final String AM_DISTANCE = "AM";
    /**
     * A numeric amplifier that displays an angle measured from true north to any other
     * line in degrees.
     * CM: P,L,A
     * Length: 3
     */
    public static final String AN_AZIMUTH = "AN";

    /**
     * A graphic amplifier placed immediately atop the symbol. May denote:
     * 1) local/remote status
     * 2) engagement status
     * 3) weapon type.
     *
     * Type: U,E,I,C
     * Length: G/8
     */
    public static final String AO_ENGAGEMENT_BAR = "AO";

    /**
     * A six character text modifier used in Fire Support operations to uniquely
     * designate targets in accordance with STANAG 2147, where characters 1 and
     * 2 are alphabetic, and characters 3-6 are numeric: AANNNN.
     * Type: CM
     * CM: P,L,A
     * Length: 6
     */
    public static final String AP_TARGET_NUMBER = "AP";

    /**
     * A 2-3 character text amplifier. A target number extension is a sequentially assigned number
     * identifying the individual elements in a target (MIL-STD-6017), where character 1 is a dash
     * (-) and characters 2-3 are number, from 1 through 15. It is applicable only to the
     * “Point or Single Target” symbol, is conditional upon the presence of the Target Number
     * amplifier, and is visually displayed appended to the Target Number amplifier.
     *
     * Type: CM
     * CM: P
     * Length: 3
     */
    public static final String AP1_TARGET_NUMBER_EXTENSION = "AP1";

    /**
     * During ballistic missile defense, some tracks are designated as guarded
     * by a particular unit.
     *
     * Type: E
     * Length: 2
     */
    public static final String AQ_GUARDED_UNIT = "AQ";

    /**
     * Special track designators such as Non-Real Time (NRT) and
     * Tactically Significant (SIG) tracks are denoted here.
     *
     * Type: E
     * Length: 3
     */
    public static final String AR_SPECIAL_DESIGNATOR = "AR";

    /**
     * A three-letter code that indicates the country of origin of the organization
     * (US systems shall use GENC). In stability activities,
     * this field can be used for factions or groups.
     *
     * Type: U,A,CM,C
     * Length: 3
     * CM: L,BL
     */
    public static final String AS_COUNTRY = "AS";

    /**
     * Used internally by the renderer.  This value is set via the 13th & 14th
     * characters in the symbol id.  There is no formal definition of how
     * this should be indicated on the symbol in the MilStd or USAS.
     * The renderer will place it to the right of the 'H' label.
     *
     * NOTE: may be deprecated now that 2525D support AS for country code.
     */
    public static final String CC_COUNTRY_CODE = "CC";

    /**
     * A generic name label that goes to the right of the symbol and
     * any existing labels.  If there are no existing labels, it goes right
     * next to the right side of the symbol.  This is a CPOF label that applies
     * to all force elements.  This IS NOT a MilStd or USAS Label.
     */
    public static final String CN_CPOF_NAME_LABEL = "CN";

    /**
     * Sonar Classification Confidence level. valid values are 1-5.
     * Only applies to the 4 subsurface MILCO sea mines
     */
    public static final String SCC_SONAR_CLASSIFICATION_CONFIDENCE = "SCC";



    //public static final String LENGTH = "Length";
    //public static final String WIDTH = "Width";
    //public static final String RADIUS = "Radius";
    //public static final String SEGMENT_DATA = "Segment Data";

    /**
     * Returns an Arraylist of the all the modifers
     * @return
     */
    public static ArrayList<String> GetModifierList()
    {
        ArrayList<String> list = new ArrayList<String>();

        //list.add(A_SYMBOL_ICON);//graphical, feeds off of symbol code,
        //list.add(B_ECHELON);//graphical, feeds off of symbol code,
        list.add(C_QUANTITY);
        //list.add(D_TASK_FORCE_INDICATOR);//graphical, feeds off of symbol code,
        //list.add(E_FRAME_SHAPE_MODIFIER);//symbol frame, feeds off of symbol code, symbol set
        list.add(F_REINFORCED_REDUCED);//R = reinforced, D = reduced, RD = reinforced and reduced
        list.add(G_STAFF_COMMENTS);
        list.add(H_ADDITIONAL_INFO_1);
        list.add(H1_ADDITIONAL_INFO_2);
        list.add(H2_ADDITIONAL_INFO_3);
        list.add(J_EVALUATION_RATING);
        list.add(K_COMBAT_EFFECTIVENESS);
        list.add(L_SIGNATURE_EQUIP);
        list.add(M_HIGHER_FORMATION);
        list.add(N_HOSTILE);
        list.add(P_IFF_SIF_AIS);
        list.add(Q_DIRECTION_OF_MOVEMENT);//number in mils
        //list.add(R_MOBILITY_INDICATOR);//graphical, feeds off of symbol code,
        list.add(R2_SIGNIT_MOBILITY_INDICATOR);
        //list.add(S_HQ_STAFF);//graphical, feeds off of symbol code,
        list.add(T_UNIQUE_DESIGNATION_1);
        list.add(T1_UNIQUE_DESIGNATION_2);
        list.add(V_EQUIP_TYPE);
        list.add(W_DTG_1);
        list.add(W1_DTG_2);
        list.add(X_ALTITUDE_DEPTH);
        list.add(Y_LOCATION);
        list.add(Z_SPEED);

        list.add(AA_SPECIAL_C2_HQ);
        //list.add(AB_FEINT_DUMMY_INDICATOR);//graphical, feeds off of symbol code,
        //list.add(AC_INSTALLATION);//graphical, feeds off of symbol code,
        list.add(AD_PLATFORM_TYPE);
        list.add(AE_EQUIPMENT_TEARDOWN_TIME);
        list.add(AF_COMMON_IDENTIFIER);
        list.add(AG_AUX_EQUIP_INDICATOR);
        list.add(AH_AREA_OF_UNCERTAINTY);
        list.add(AI_DEAD_RECKONING_TRAILER);
        list.add(AJ_SPEED_LEADER);//graphical
        list.add(AK_PAIRING_LINE);
        //list.add(AL_OPERATIONAL_CONDITION);//2525C ////graphical, feeds off of symbol code, SIDC positions 4
        list.add(AO_ENGAGEMENT_BAR);//2525C
        //list.add(AP_TARGET_NUMBER);
        //list.add(AP1_TARGET_NUMBER_EXTENSION);
        list.add((AQ_GUARDED_UNIT));
        list.add((AR_SPECIAL_DESIGNATOR));
        list.add((AS_COUNTRY));
        //list.add((SCC_SONAR_CLASSIFICATION_CONFIDENCE));



        return list;
    }

    /**
     * Returns an Arraylist of the modifer names for units
     * @return
     */
    public static ArrayList<String> GetUnitModifierList()
    {
        ArrayList<String> list = new ArrayList<String>();

        //list.add(ModifierType.A_SYMBOL_ICON);//graphical, feeds off of symbol code, SIDC positions 3, 5-10
        //list.add(ModifierType.B_ECHELON);//graphical, feeds off of symbol code, SIDC positions 11-12
        list.add(C_QUANTITY);
        //list.add(D_TASK_FORCE_INDICATOR);//graphical, feeds off of symbol code, SIDC positions 11-12
        //list.add(E_FRAME_SHAPE_MODIFIER);//symbol frame, feeds off of symbol code, SIDC positions 3-4
        list.add(F_REINFORCED_REDUCED);//R = reinforced, D = reduced, RD = reinforced and reduced
        list.add(G_STAFF_COMMENTS);
        list.add(H_ADDITIONAL_INFO_1);
        //list.add(H1_ADDITIONAL_INFO_2);
        //list.add(H2_ADDITIONAL_INFO_3);
        list.add(J_EVALUATION_RATING);
        list.add(K_COMBAT_EFFECTIVENESS);
        list.add(L_SIGNATURE_EQUIP);
        list.add(M_HIGHER_FORMATION);
        list.add(N_HOSTILE);
        list.add(P_IFF_SIF_AIS);
        list.add(Q_DIRECTION_OF_MOVEMENT);//number in mils
        //list.add(R_MOBILITY_INDICATOR);//graphical, feeds off of symbol code, SIDC positions 11-12
        list.add(R2_SIGNIT_MOBILITY_INDICATOR);
        //list.add(S_HQ_STAFF_OR_OFFSET_INDICATOR);//graphical, feeds off of symbol code, SIDC positions 11-12
        list.add(T_UNIQUE_DESIGNATION_1);
        //list.add(T1_UNIQUE_DESIGNATION_2);
        list.add(V_EQUIP_TYPE);
        list.add(W_DTG_1);
        list.add(W1_DTG_2);
        list.add(X_ALTITUDE_DEPTH);
        list.add(Y_LOCATION);
        list.add(Z_SPEED);

        list.add(AA_SPECIAL_C2_HQ);
        //list.add(AB_FEINT_DUMMY_INDICATOR);//graphical, feeds off of symbol code, SIDC positions 11-12
        //list.add(AC_INSTALLATION);//graphical, feeds off of symbol code, SIDC positions 11-12
        list.add(AD_PLATFORM_TYPE);
        list.add(AE_EQUIPMENT_TEARDOWN_TIME);
        list.add(AF_COMMON_IDENTIFIER);
        //list.add(AG_AUX_EQUIP_INDICATOR);//graphical
        //list.add(AH_AREA_OF_UNCERTAINTY);//graphical and on client of renderer
        //list.add(AI_DEAD_RECKONING_TRAILER);//graphical
        list.add(AJ_SPEED_LEADER);
        list.add(AK_PAIRING_LINE);
        //list.add(AL_OPERATIONAL_CONDITION);//2525C ////graphical, feeds off of symbol code, SIDC positions 4
        list.add(AO_ENGAGEMENT_BAR);//2525C
        list.add(AP_TARGET_NUMBER);
        list.add(AP1_TARGET_NUMBER_EXTENSION);
        list.add((AQ_GUARDED_UNIT));
        list.add((AR_SPECIAL_DESIGNATOR));
        list.add((AS_COUNTRY));
        list.add((SCC_SONAR_CLASSIFICATION_CONFIDENCE));



        return list;
    }

    /**
     * Returns an Arraylist of the modifers for Control Measures
     * @return
     */
    public static ArrayList<String> GetControlMeasureModifierList()
    {
        ArrayList<String> list = new ArrayList<String>();

        //list.add(ModifierType.B_ECHELON);//graphical, feeds off of symbol code,
        list.add(C_QUANTITY);
        list.add(H_ADDITIONAL_INFO_1);
        list.add(H1_ADDITIONAL_INFO_2);
        list.add(H2_ADDITIONAL_INFO_3);
        list.add(N_HOSTILE);
        list.add(Q_DIRECTION_OF_MOVEMENT);//number in mils
        //list.add(S2);//graphical, up to renderer client
        list.add(T_UNIQUE_DESIGNATION_1);
        list.add(T1_UNIQUE_DESIGNATION_2);
        list.add(V_EQUIP_TYPE);
        list.add(W_DTG_1);
        list.add(W1_DTG_2);
        list.add(X_ALTITUDE_DEPTH);
        list.add(Y_LOCATION);

        list.add(AM_DISTANCE);
        list.add(AN_AZIMUTH);
        list.add(AP_TARGET_NUMBER);
        list.add(AP1_TARGET_NUMBER_EXTENSION);
        list.add((AS_COUNTRY));




        return list;
    }
}

