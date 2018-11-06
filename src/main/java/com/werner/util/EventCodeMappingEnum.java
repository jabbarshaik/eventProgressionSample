package com.werner.util;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public enum EventCodeMappingEnum {

    O_X3_LIVE("O_X3_LIVE", Arrays.asList(StreetAndEquipConstants.ORIGIN_STREET, StreetAndEquipConstants.LOADING)),
    O_X3_DROP("O_X3_DROP", Arrays.asList(StreetAndEquipConstants.ORIGIN_STREET, StreetAndEquipConstants.PLACED_FOR_LOADING)),

    O_AF_LIVE("O_AF_LIVE", Arrays.asList(StreetAndEquipConstants.ORIGIN_TRANSIT, StreetAndEquipConstants.LOADED)),
    O_AF_DROP("O_AF_DROP", Arrays.asList(StreetAndEquipConstants.ORIGIN_TRANSIT, StreetAndEquipConstants.LOADED)),

    O_X1_LIVE("O_X1_LIVE", Arrays.asList(StreetAndEquipConstants.ORIGIN_RAMP, StreetAndEquipConstants.LOADED)),
    O_X1_DROP("O_X1_DROP", Arrays.asList(StreetAndEquipConstants.ORIGIN_STREET, StreetAndEquipConstants.LOADED)),

    O_CD_LIVE("O_CD_LIVE", Arrays.asList(StreetAndEquipConstants.ORIGIN_RAMP, StreetAndEquipConstants.LOADED)),
    O_CD_DROP("O_CD_DROP", Arrays.asList(StreetAndEquipConstants.ORIGIN_RAMP, StreetAndEquipConstants.LOADED)),


    B_X3_LIVE("B_X3_LIVE", Arrays.asList(StreetAndEquipConstants.ORIGIN_STREET, StreetAndEquipConstants.LOADING)),
    B_X3_DROP("B_X3_DROP", Arrays.asList(StreetAndEquipConstants.ORIGIN_STREET, StreetAndEquipConstants.LOADED)),

    B_AF_LIVE("B_AF_LIVE", Arrays.asList(StreetAndEquipConstants.BUNDLED_TRANSIT, StreetAndEquipConstants.LOADED)),
    B_AF_DROP("B_AF_DROP", Arrays.asList(StreetAndEquipConstants.BUNDLED_TRANSIT, StreetAndEquipConstants.LOADED)),

    B_X1_LIVE("B_X1_LIVE", Arrays.asList(StreetAndEquipConstants.DESTINATION_STREET, StreetAndEquipConstants.UNLOADING)),
    B_X1_DROP("B_X1_DROP", Arrays.asList(StreetAndEquipConstants.DESTINATION_STREET, StreetAndEquipConstants.PLACED_FOR_UNLOADING)),

    B_CD_LIVE("B_CD_LIVE", Arrays.asList(StreetAndEquipConstants.DESTINATION_STREET, StreetAndEquipConstants.EMPTY)),
    B_CD_DROP("B_CD_DROP", Arrays.asList(StreetAndEquipConstants.DESTINATION_STREET, StreetAndEquipConstants.PLACED_FOR_UNLOADING)),

    OB_X3_LIVE("OB_X3_LIVE", Arrays.asList(StreetAndEquipConstants.ORIGIN_STREET, StreetAndEquipConstants.LOADING)),
    OB_X3_DROP("OB_X3_DROP", Arrays.asList(StreetAndEquipConstants.ORIGIN_STREET, StreetAndEquipConstants.LOADED)),

    OB_AF_LIVE("OB_AF_LIVE", Arrays.asList(StreetAndEquipConstants.ORIGIN_STREET, StreetAndEquipConstants.LOADED)),
    OB_AF_DROP("OB_AF_DROP", Arrays.asList(StreetAndEquipConstants.ORIGIN_STREET, StreetAndEquipConstants.LOADED)),


    OB_I("OB_I", Arrays.asList(StreetAndEquipConstants.ORIGIN_RAMP, StreetAndEquipConstants.LOADED)),
    OB_IL("OB_IL", Arrays.asList(StreetAndEquipConstants.ORIGIN_RAMP, StreetAndEquipConstants.LOADED)),
    OB_RL("OB_RL", Arrays.asList(StreetAndEquipConstants.RAIL_TRANSIT, StreetAndEquipConstants.LOADED)),
    OB_ETD("OB_ETD", Arrays.asList(StreetAndEquipConstants.RAIL_TRANSIT, StreetAndEquipConstants.LOADED)),
    OB_ETA("OB_ETA", Arrays.asList(StreetAndEquipConstants.RAIL_TRANSIT, StreetAndEquipConstants.LOADED)),
    OB_X6("OB_X6", Arrays.asList(StreetAndEquipConstants.RAIL_TRANSIT, StreetAndEquipConstants.LOADED)),
    OB_UR("OB_UR", Arrays.asList(StreetAndEquipConstants.DESTINATION_RAMP, StreetAndEquipConstants.LOADED)),
    OB_ETG("OB_ETG", Arrays.asList(StreetAndEquipConstants.DESTINATION_RAMP, StreetAndEquipConstants.LOADED)),
    OB_NT("OB_NT", Arrays.asList(StreetAndEquipConstants.DESTINATION_RAMP, StreetAndEquipConstants.LOADED)),
    OB_NF("OB_NF", Arrays.asList(StreetAndEquipConstants.DESTINATION_RAMP, StreetAndEquipConstants.LOADED)),
    OB_OA("OB_OA", Arrays.asList(StreetAndEquipConstants.DESTINATION_TRANSIT, StreetAndEquipConstants.LOADED)),

    DB_I("DB_I", Arrays.asList(StreetAndEquipConstants.ORIGIN_RAMP, StreetAndEquipConstants.LOADED)),
    DB_IL("DB_IL", Arrays.asList(StreetAndEquipConstants.ORIGIN_RAMP, StreetAndEquipConstants.LOADED)),
    DB_RL("DB_RL", Arrays.asList(StreetAndEquipConstants.RAIL_TRANSIT, StreetAndEquipConstants.LOADED)),
    DB_ETD("DB_ETD", Arrays.asList(StreetAndEquipConstants.RAIL_TRANSIT, StreetAndEquipConstants.LOADED)),
    DB_ETA("DB_ETA", Arrays.asList(StreetAndEquipConstants.RAIL_TRANSIT, StreetAndEquipConstants.LOADED)),
    DB_X6("DB_X6", Arrays.asList(StreetAndEquipConstants.RAIL_TRANSIT, StreetAndEquipConstants.LOADED)),
    DB_UR("DB_UR", Arrays.asList(StreetAndEquipConstants.DESTINATION_RAMP, StreetAndEquipConstants.LOADED)),
    DB_ETG("DB_ETG", Arrays.asList(StreetAndEquipConstants.DESTINATION_RAMP, StreetAndEquipConstants.LOADED)),
    DB_NT("DB_NT", Arrays.asList(StreetAndEquipConstants.DESTINATION_RAMP, StreetAndEquipConstants.LOADED)),
    DB_NF("DB_NF", Arrays.asList(StreetAndEquipConstants.DESTINATION_RAMP, StreetAndEquipConstants.LOADED)),
    DB_OA("DB_OA", Arrays.asList(StreetAndEquipConstants.DESTINATION_STREET, StreetAndEquipConstants.LOADED)),

    DB_X1_LIVE("DB_X1_LIVE", Arrays.asList(StreetAndEquipConstants.DESTINATION_STREET, StreetAndEquipConstants.UNLOADING)),
    DB_X1_DROP("DB_X1_DROP", Arrays.asList(StreetAndEquipConstants.DESTINATION_STREET, StreetAndEquipConstants.PLACED_FOR_UNLOADING)),

    DB_CD_LIVE("DB_CD_LIVE", Arrays.asList(StreetAndEquipConstants.DESTINATION_STREET, StreetAndEquipConstants.EMPTY)),
    DB_CD_DROP("DB_CD_DROP", Arrays.asList(StreetAndEquipConstants.DESTINATION_STREET, StreetAndEquipConstants.PLACED_FOR_UNLOADING)),



    X_X3("X_X3", Arrays.asList(StreetAndEquipConstants.DESTINATION_RAMP, StreetAndEquipConstants.LOADED)),
    X_AF("X_AF", Arrays.asList(StreetAndEquipConstants.CROSSTOWN_TRANSIT, StreetAndEquipConstants.LOADED)),
    X_X1("X_X1", Arrays.asList(StreetAndEquipConstants.ORIGIN_RAMP, StreetAndEquipConstants.LOADED)),
    X_CD("X_CD", Arrays.asList(StreetAndEquipConstants.ORIGIN_RAMP, StreetAndEquipConstants.LOADED)),

// I("I",1),IL("IL",2), RL("RL",3),ETD("ETD",4),ETA("ETA",5),LPH("LPH", 6),DERAMPED("DERAMPED", 7), ETG("ETG",8),NT("NT",9),NF("NF",10),OA("OA",11);


    INGATE("R_I", Arrays.asList(StreetAndEquipConstants.ORIGIN_RAMP, StreetAndEquipConstants.LOADED)),

    IL("R_IL", Arrays.asList(StreetAndEquipConstants.ORIGIN_RAMP, StreetAndEquipConstants.LOADED)),

    RL("R_RL", Arrays.asList(StreetAndEquipConstants.RAIL_TRANSIT, StreetAndEquipConstants.LOADED)),

    ETD("R_ETD", Arrays.asList(StreetAndEquipConstants.RAIL_TRANSIT, StreetAndEquipConstants.LOADED)),

    ETA("R_ETA", Arrays.asList(StreetAndEquipConstants.RAIL_TRANSIT, StreetAndEquipConstants.LOADED)),

    LPH("R_LPH", Arrays.asList(StreetAndEquipConstants.RAIL_TRANSIT, StreetAndEquipConstants.LOADED)),

    DERAMPED("R_DERAMPED", Arrays.asList(StreetAndEquipConstants.DESTINATION_RAMP, StreetAndEquipConstants.LOADED)),

    ETG("R_ETG", Arrays.asList(StreetAndEquipConstants.DESTINATION_RAMP, StreetAndEquipConstants.LOADED)),

    NT("R_NT", Arrays.asList(StreetAndEquipConstants.DESTINATION_RAMP, StreetAndEquipConstants.LOADED)),

    NF("R_NF", Arrays.asList(StreetAndEquipConstants.DESTINATION_RAMP, StreetAndEquipConstants.LOADED)),

    OA("R_OA", Arrays.asList(StreetAndEquipConstants.DESTINATION_TRANSIT, StreetAndEquipConstants.LOADED)),

    X_OA("RX_OA", Arrays.asList(StreetAndEquipConstants.CROSSTOWN_TRANSIT, StreetAndEquipConstants.LOADED)),


    ER_INGATE("ER_I", Arrays.asList(StreetAndEquipConstants.ORIGIN_RAMP, StreetAndEquipConstants.EMPTY)),

    ER_IL("ER_IL", Arrays.asList(StreetAndEquipConstants.ORIGIN_RAMP, StreetAndEquipConstants.EMPTY)),

    ER_RL("ER_RL", Arrays.asList(StreetAndEquipConstants.RAIL_TRANSIT, StreetAndEquipConstants.EMPTY)),

    ER_ETD("ER_ETD", Arrays.asList(StreetAndEquipConstants.RAIL_TRANSIT, StreetAndEquipConstants.EMPTY)),

    ER_ETA("ER_ETA", Arrays.asList(StreetAndEquipConstants.RAIL_TRANSIT, StreetAndEquipConstants.EMPTY)),

    ER_LPH("ER_LPH", Arrays.asList(StreetAndEquipConstants.RAIL_TRANSIT, StreetAndEquipConstants.EMPTY)),

    ER_DERAMPED("ER_DERAMPED", Arrays.asList(StreetAndEquipConstants.DESTINATION_RAMP, StreetAndEquipConstants.EMPTY)),

    ER_ETG("ER_ETG", Arrays.asList(StreetAndEquipConstants.DESTINATION_RAMP, StreetAndEquipConstants.EMPTY)),

    ER_NT("ER_NT", Arrays.asList(StreetAndEquipConstants.DESTINATION_RAMP, StreetAndEquipConstants.EMPTY)),

    ER_NF("ER_NF", Arrays.asList(StreetAndEquipConstants.DESTINATION_RAMP, StreetAndEquipConstants.EMPTY)),

    ER_OA("ER_OA", Arrays.asList(StreetAndEquipConstants.DESTINATION_TRANSIT, StreetAndEquipConstants.EMPTY)),



    D_X3_LIVE("D_X3_LIVE", Arrays.asList(StreetAndEquipConstants.DESTINATION_RAMP, StreetAndEquipConstants.LOADED)),
    D_X3_DROP("D_X3_DROP", Arrays.asList(StreetAndEquipConstants.DESTINATION_RAMP, StreetAndEquipConstants.LOADED)),

    D_AF_LIVE("D_AF_LIVE", Arrays.asList(StreetAndEquipConstants.DESTINATION_TRANSIT, StreetAndEquipConstants.LOADED)),
    D_AF_DROP("D_AF_DROP", Arrays.asList(StreetAndEquipConstants.DESTINATION_TRANSIT, StreetAndEquipConstants.LOADED)),

    D_X1_LIVE("D_X1_LIVE", Arrays.asList(StreetAndEquipConstants.DESTINATION_STREET, StreetAndEquipConstants.UNLOADING)),
    D_X1_DROP("D_X1_DROP", Arrays.asList(StreetAndEquipConstants.DESTINATION_STREET, StreetAndEquipConstants.PLACED_FOR_UNLOADING)),

    D_CD_LIVE("D_CD_LIVE", Arrays.asList(StreetAndEquipConstants.DESTINATION_STREET, StreetAndEquipConstants.EMPTY)),
    D_CD_DROP("D_CD_DROP", Arrays.asList(StreetAndEquipConstants.DESTINATION_STREET, StreetAndEquipConstants.PLACED_FOR_UNLOADING)),

//on network events
    RC("RC", Arrays.asList(StreetAndEquipConstants.ORIGIN_RAMP, StreetAndEquipConstants.LOADED)),
    OE("OE", Arrays.asList(StreetAndEquipConstants.ORIGIN_STREET, StreetAndEquipConstants.EMPTY)),
    IT("IT", Arrays.asList(StreetAndEquipConstants.ORIGIN_STREET, StreetAndEquipConstants.EMPTY)),
    ESA_LIVE("ESA_LIVE", Arrays.asList(StreetAndEquipConstants.ORIGIN_STREET, StreetAndEquipConstants.LOADING)),
    ESA_DROP("ESA_DROP", Arrays.asList(StreetAndEquipConstants.ORIGIN_STREET, StreetAndEquipConstants.LOADED)),
    MON("MON", Arrays.asList(StreetAndEquipConstants.ORIGIN_STREET, StreetAndEquipConstants.EMPTY)),

    //off network events

   // ("IF", "IE", "ID", "MOFN", "SOFN");
   IF("IF", Arrays.asList(StreetAndEquipConstants.OFF_NETWORK, StreetAndEquipConstants.EMPTY)),
    IE("IE", Arrays.asList(StreetAndEquipConstants.OFF_NETWORK, StreetAndEquipConstants.EMPTY)),
    ID("ID", Arrays.asList(StreetAndEquipConstants.OFF_NETWORK, StreetAndEquipConstants.EMPTY)),
    MOFN("MOFN", Arrays.asList(StreetAndEquipConstants.OFF_NETWORK, StreetAndEquipConstants.EMPTY)),
    SOFN("SOFN", Arrays.asList(StreetAndEquipConstants.OFF_NETWORK, StreetAndEquipConstants.EMPTY));

    private final String code;
    private final List<String> equipStreetStatus;
    private static Map<String,List<String>> eventCodeMappingEnum = prepareEnumMap();

    EventCodeMappingEnum(String code, List<String> equipStreetStatus) {
        this.equipStreetStatus = equipStreetStatus;
        this.code = code;
    }

    public List<String> getEquipStreetStatuses() {
        return equipStreetStatus;
    }

    public String getCode() {
        return code;
    }

    private static Map<String, List<String>> prepareEnumMap(){

        Map<String,List<String>> eventStatusMap = new HashMap<>();
        for (EventCodeMappingEnum eventCodeMappingEnum : EventCodeMappingEnum.values()) {
            eventStatusMap.put(eventCodeMappingEnum.code, eventCodeMappingEnum.equipStreetStatus);
        }
        return eventStatusMap;
    }

    public static List<String> getEquipStreet(String eventCode) {
        return StringUtils.isEmpty(eventCode) ? null : eventCodeMappingEnum.get(StringUtils.upperCase(eventCode));
    }


    public static List<String> getKeyUsingValue(List<String> values){

        return eventCodeMappingEnum.entrySet().stream()
                .filter(v -> v.getValue().containsAll(values))
                .map(Map.Entry::getKey).collect(Collectors.toList());
    }
  
}
