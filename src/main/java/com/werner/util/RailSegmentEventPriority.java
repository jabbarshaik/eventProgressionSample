package com.werner.util;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public enum RailSegmentEventPriority {


    I("I",1),IL("IL",2), RD("RD",3),RC("RC", 4),RL("RL",5),ETD("ETD",6),ETA("ETA",7),LPH("LPH", 8),DERAMPED("DERAMPED", 9), ETG("ETG",10),NT("NT",11),NF("NF",12),OA("OA",13);

    private String  code;
    private Integer priority;


    private static Map<String,Integer> railSegmentEventPriorityMap = prepareSegmentEventMap();

    public Integer getPriority() {
        return priority;
    }

    RailSegmentEventPriority(String code, Integer priority){
        this.code = code;
        this.priority = priority;
    }

    private static Map<String, Integer> prepareSegmentEventMap(){

        Map<String,Integer> railSegmentEventPriorityMap = new HashMap<>();
        for (RailSegmentEventPriority railSegmentEventPriority : RailSegmentEventPriority.values()) {
            railSegmentEventPriorityMap.put(railSegmentEventPriority.code, railSegmentEventPriority.priority);
        }
        return railSegmentEventPriorityMap;
    }

    public static Integer getRailSegmentEventPriority(String code) {
        return StringUtils.isNotBlank(code) ? railSegmentEventPriorityMap.get(StringUtils.upperCase(code)):null;
    }
}
