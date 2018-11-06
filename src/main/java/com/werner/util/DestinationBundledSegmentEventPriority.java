package com.werner.util;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public enum DestinationBundledSegmentEventPriority {


   I("I",1), IL("IL",2),RL("RL",3),ETD("ETD",4), ETA("ETA",5), X6("X6",6),UR("UR",7),ETG("ETG",8), NT("NT",9), NF("NF",10),OA("OA",11), X1("X1",12),CD("CD",13);

    private String  code;
    private Integer priority;


    private static Map<String,Integer> destinationBundledSegmentEventPriorityMap = prepareSegmentEventMap();

    public Integer getPriority() {
        return priority;
    }

    DestinationBundledSegmentEventPriority(String code, Integer priority){
        this.code = code;
        this.priority = priority;
    }

    private static Map<String, Integer> prepareSegmentEventMap(){

        Map<String,Integer> railSegmentEventPriorityMap = new HashMap<>();
        for (DestinationBundledSegmentEventPriority railSegmentEventPriority : DestinationBundledSegmentEventPriority.values()) {
            railSegmentEventPriorityMap.put(railSegmentEventPriority.code, railSegmentEventPriority.priority);
        }
        return railSegmentEventPriorityMap;
    }

    public static Integer getDestinationBundledSegmentEventPriority(String code) {
        return StringUtils.isNotBlank(code) ? destinationBundledSegmentEventPriorityMap.get(StringUtils.upperCase(code)):null;
    }
}
