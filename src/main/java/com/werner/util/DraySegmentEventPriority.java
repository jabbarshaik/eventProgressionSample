package com.werner.util;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public enum DraySegmentEventPriority {


    X3("X3",1),AF("AF",2), X1("X1",3), CD("CD",4);

    private String  code;
    private Integer priority;


    private static Map<String,Integer> draySegmentEventPriorityMap = prepareSegmentEventMap();

    public Integer getPriority() {
        return priority;
    }

    DraySegmentEventPriority(String code,Integer priority){
        this.code = code;
        this.priority = priority;
    }

    private static Map<String, Integer> prepareSegmentEventMap(){

        Map<String,Integer> draySegmentEventPriorityMap = new HashMap<>();
        for (DraySegmentEventPriority draySegmentEventPriority : DraySegmentEventPriority.values()) {
            draySegmentEventPriorityMap.put(draySegmentEventPriority.code, draySegmentEventPriority.priority);
        }
        return draySegmentEventPriorityMap;
    }

    public static Integer getDraySegmentEventPriority(String code) {
        return StringUtils.isNotBlank(code) ? draySegmentEventPriorityMap.get(StringUtils.upperCase(code)):null;
    }
}
