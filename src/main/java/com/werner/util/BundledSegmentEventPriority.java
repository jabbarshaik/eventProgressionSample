package com.werner.util;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public enum BundledSegmentEventPriority {


    X3("X3",1),AF("AF",2), X1("X1",3), CD("CD",4);

    private String  code;
    private Integer priority;


    private static Map<String,Integer> bundledSegmentEventPriorityMap = prepareSegmentEventMap();

    public Integer getPriority() {
        return priority;
    }

    BundledSegmentEventPriority(String code, Integer priority){
        this.code = code;
        this.priority = priority;
    }

    private static Map<String, Integer> prepareSegmentEventMap(){

        Map<String,Integer> railSegmentEventPriorityMap = new HashMap<>();
        for (BundledSegmentEventPriority railSegmentEventPriority : BundledSegmentEventPriority.values()) {
            railSegmentEventPriorityMap.put(railSegmentEventPriority.code, railSegmentEventPriority.priority);
        }
        return railSegmentEventPriorityMap;
    }

    public static Integer getBundledSegmentEventPriority(String code) {
        return StringUtils.isNotBlank(code) ? bundledSegmentEventPriorityMap.get(StringUtils.upperCase(code)):null;
    }
}
