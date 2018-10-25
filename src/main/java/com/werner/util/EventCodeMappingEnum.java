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

    INGATE("R_I", Arrays.asList("Origin Ramp", "Loaded")),
    RL("R_RL", Arrays.asList("Rail Transit", "Loaded")),

    NT("R_NT", Arrays.asList("Destination Ramp", "Loaded")),
    OA("R_OA", Arrays.asList("Destination Transit", "Loaded")),

    ER_INGATE("ER_I", Arrays.asList("Empty Origin Ramp", "Empty")),
    ER_RL("ER_RL", Arrays.asList("Empty Rail Transit", "Empty")),

    ER_NT("ER_NT", Arrays.asList("Empty Destination Ramp", "Empty")),
    ER_OA("ER_OA", Arrays.asList("Empty Destination Street", "Empty")),


    D_X3_LIVE("D_X3_LIVE", Arrays.asList("Destination Ramp", "Loaded")),
    D_X3_DROP("D_X3_DROP", Arrays.asList("Destination Ramp", "Loaded")),

    D_AF_LIVE("D_AF_LIVE", Arrays.asList("Destination Transit", "Loaded")),
    D_AF_DROP("D_AF_DROP", Arrays.asList("Destination Transit", "Loaded")),

    D_X1_LIVE("D_X1_LIVE", Arrays.asList("Destination Street", "Unloading")),
    D_X1_DROP("D_X1_DROP", Arrays.asList("Destination Street", "Placed for Unloading")),

    D_CD_LIVE("D_CD_LIVE", Arrays.asList("Destination Street", "Empty")),
    D_CD_DROP("D_CD_DROP", Arrays.asList("Destination Street", "Placed for Unloading"));

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
