package com.werner.util;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public enum OriginBundledSegmentEventPriority {


    X3("X3",1),AF("AF",2), NT("NT",3), OA("OA",4);

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
