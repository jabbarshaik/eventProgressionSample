package com.werner.util;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum EventCodeMappingEnum {

    O_X3_LIVE("O_X3_LIVE", Arrays.asList("Origin Street", "Loading")),
    O_X3_DROP("O_X3_DROP", Arrays.asList("Origin Street", "Placed for Loading")),

    O_AF_LIVE("O_AF_LIVE", Arrays.asList("Origin Transit", "Loaded")),
    O_AF_DROP("O_AF_DROP", Arrays.asList("Origin Transit", "Loaded")),

    O_X1_LIVE("O_X1_LIVE", Arrays.asList("Origin Ramp", "Loaded")),
    O_X1_DROP("O_X1_DROP", Arrays.asList("Origin Street", "Loaded")),

    O_CD_LIVE("O_CD_LIVE", Arrays.asList("Origin Ramp", "Loaded")),
    O_CD_DROP("O_CD_DROP", Arrays.asList("Origin Ramp", "Loaded")),


    B_X3_LIVE("B_X3_LIVE", Arrays.asList("Origin Street", "Loading")),
    B_X3_DROP("B_X3_DROP", Arrays.asList("Origin Street", "Loaded")),

    B_AF_LIVE("B_AF_LIVE", Arrays.asList("Bundled Transit", "Loaded")),
    B_AF_DROP("B_AF_DROP", Arrays.asList("Bundled Transit", "Loaded")),

    B_X1_LIVE("B_X1_LIVE", Arrays.asList("Destination Street", "Unloading")),
    B_X1_DROP("B_X1_DROP", Arrays.asList("Destination Street", "Placed for Unloading")),

    B_CD_LIVE("B_CD_LIVE", Arrays.asList("Destination Street", "Empty")),
    B_CD_DROP("B_CD_DROP", Arrays.asList("Destination Street", "Placed for Unloading")),

    OB_X3_LIVE("OB_X3_LIVE", Arrays.asList("Origin Street", "Loading")),
    OB_X3_DROP("OB_X3_DROP", Arrays.asList("Origin Street", "Loaded")),

    OB_AF_LIVE("OB_AF_LIVE", Arrays.asList("Rail Transit", "Loaded")),
    OB_AF_DROP("OB_AF_DROP", Arrays.asList("Rail Transit", "Loaded")),

    OB_NT("OB_NT", Arrays.asList("Destination Ramp", "Loaded")),
    OB_OA("OB_OA", Arrays.asList("Destination Transit", "Loaded")),

    DB_I("DB_I", Arrays.asList("Origin Ramp", "Loaded")),
    DB_RL("DB_RL", Arrays.asList("Rail Transit", "Loaded")),

    DB_X1_LIVE("DB_X1_LIVE", Arrays.asList("Destination Street", "Unloading")),
    DB_X1_DROP("DB_X1_DROP", Arrays.asList("Destination Street", "Placed for Unloading")),

    DB_CD_LIVE("DB_CD_LIVE", Arrays.asList("Destination Street", "Empty")),
    DB_CD_DROP("DB_CD_DROP", Arrays.asList("Destination Street", "Placed for Unloading")),



    X_X3("X_X3", Arrays.asList("Destination Ramp", "Loaded")),
    X_AF("X_AF", Arrays.asList("Crosstown Transit", "Loaded")),
    X_X1("X_X1", Arrays.asList("Origin Ramp", "Loaded")),
    X_CD("X_CD", Arrays.asList("Origin Ramp", "Loaded")),

// I("I",1),IL("IL",2), RL("RL",3),ETD("ETD",4),ETA("ETA",5),LPH("LPH", 6),DERAMPED("DERAMPED", 7), ETG("ETG",8),NT("NT",9),NF("NF",10),OA("OA",11);


    INGATE("R_I", Arrays.asList("Origin Ramp", "Loaded")),

    IL("R_IL", Arrays.asList("Origin Ramp", "Loaded")),

    RL("R_RL", Arrays.asList("Rail Transit", "Loaded")),

    ETD("R_ETD", Arrays.asList("Rail Transit", "Loaded")),

    ETA("R_ETA", Arrays.asList("Rail Transit", "Loaded")),

    LPH("R_LPH", Arrays.asList("Rail Transit", "Loaded")),

    DERAMPED("R_DERAMPED", Arrays.asList("Destination Ramp", "Loaded")),

    ETG("R_ETG", Arrays.asList("Destination Ramp", "Loaded")),

    NT("R_NT", Arrays.asList("Destination Ramp", "Loaded")),

    NF("R_NF", Arrays.asList("Destination Ramp", "Loaded")),

    OA("R_OA", Arrays.asList("Destination Transit", "Loaded")),

    X_OA("RX_OA", Arrays.asList("Crosstown Transit", "Loaded")),


    ER_INGATE("ER_I", Arrays.asList("Origin Ramp", "Empty")),

    ER_IL("ER_IL", Arrays.asList("Origin Ramp", "Empty")),

    ER_RL("ER_RL", Arrays.asList("Rail Transit", "Empty")),

    ER_ETD("ER_ETD", Arrays.asList("Rail Transit", "Empty")),

    ER_ETA("ER_ETA", Arrays.asList("Rail Transit", "Empty")),

    ER_LPH("ER_LPH", Arrays.asList("Rail Transit", "Empty")),

    ER_DERAMPED("ER_DERAMPED", Arrays.asList("Destination Ramp", "Empty")),

    ER_ETG("ER_ETG", Arrays.asList("Destination Ramp", "Empty")),

    ER_NT("ER_NT", Arrays.asList("Destination Ramp", "Empty")),

    ER_NF("ER_NF", Arrays.asList("Destination Ramp", "Empty")),

    ER_OA("ER_OA", Arrays.asList("Destination Transit", "Empty")),



    D_X3_LIVE("D_X3_LIVE", Arrays.asList("Destination Ramp", "Loaded")),
    D_X3_DROP("D_X3_DROP", Arrays.asList("Destination Ramp", "Loaded")),

    D_AF_LIVE("D_AF_LIVE", Arrays.asList("Destination Transit", "Loaded")),
    D_AF_DROP("D_AF_DROP", Arrays.asList("Destination Transit", "Loaded")),

    D_X1_LIVE("D_X1_LIVE", Arrays.asList("Destination Street", "Unloading")),
    D_X1_DROP("D_X1_DROP", Arrays.asList("Destination Street", "Placed for Unloading")),

    D_CD_LIVE("D_CD_LIVE", Arrays.asList("Destination Street", "Empty")),
    D_CD_DROP("D_CD_DROP", Arrays.asList("Destination Street", "Placed for Unloading")),

//on network events
    RC("RC", Arrays.asList("Origin Street", "Empty")),
    OE("OE", Arrays.asList("Origin Street", "Empty")),
    IT("IT", Arrays.asList("Origin Street", "Empty")),
    ESA_LIVE("ESA_LIVE", Arrays.asList("Origin Street", "Loading")),
    ESA_DROP("ESA_DROP", Arrays.asList("Origin Street", "Loaded")),
    MON("MON", Arrays.asList("Origin Street", "Empty")),

    //off network events

   // ("IF", "IE", "ID", "MOFN", "SOFN");
   IF("IF", Arrays.asList("Off Network", "Empty")),
    IE("IE", Arrays.asList("Off Network", "Empty")),
    ID("ID", Arrays.asList("Off Network", "Empty")),
    MOFN("MOFN", Arrays.asList("Off Network", "Empty")),
    SOFN("SOFN", Arrays.asList("Off Network", "Empty"));

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
}
