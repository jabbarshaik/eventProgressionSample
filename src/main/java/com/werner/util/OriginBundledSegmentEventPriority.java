package com.werner.util;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public enum OriginBundledSegmentEventPriority {


    X3("X3",1),AF("AF",2), I("I",3), IL("IL",4),RL("RL",5),ETD("ETD",6), ETA("ETA",7), X6("X6",8),UR("UR",9),ETG("ETG",10), NT("NT",11), NF("NF",12),OA("OA",13);

    private String  code;
    private Integer priority;


    private static Map<String,Integer> originBundledSegmentEventPriorityMap = prepareSegmentEventMap();

    public Integer getPriority() {
        return priority;
    }

    OriginBundledSegmentEventPriority(String code, Integer priority){
        this.code = code;
        this.priority = priority;
    }

    private static Map<String, Integer> prepareSegmentEventMap(){

        Map<String,Integer> railSegmentEventPriorityMap = new HashMap<>();
        for (OriginBundledSegmentEventPriority railSegmentEventPriority : OriginBundledSegmentEventPriority.values()) {
            railSegmentEventPriorityMap.put(railSegmentEventPriority.code, railSegmentEventPriority.priority);
        }
        return railSegmentEventPriorityMap;
    }

    public static Integer getOriginBundledSegmentEventPriority(String code) {
        return StringUtils.isNotBlank(code) ? originBundledSegmentEventPriorityMap.get(StringUtils.upperCase(code)):null;
    }
}
