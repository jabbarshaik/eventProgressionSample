package com.werner.util;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public enum RailSegmentEventPriority {


    I("I",1),IL("IL",2), RL("RL",3),ETD("ETD",4),ETA("ETA",5),LPH("LPH", 6),DERAMPED("DERAMPED", 7), ETG("ETG",8),NT("NT",9),NF("NF",10),OA("OA",11);

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
