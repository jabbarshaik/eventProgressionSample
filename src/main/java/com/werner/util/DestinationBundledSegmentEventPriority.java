package com.werner.util;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public enum DestinationBundledSegmentEventPriority {


    I("I",1),RL("RL",2), X1("X1",3), CD("CD",4);

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
