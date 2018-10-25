package com.werner.util;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public enum SegmentEventPriority {

     X3("X3",1),AF("AF",2), X1("X1",3), CD("CD",4),I("I",5), RL("RL",6),NT("NT",7),OA("OA",8);

     private String  code;
     private Integer priority;


    private static Map<String,Integer> segmentEventPriorityMap = prepareSegmentEventMap();

    public Integer getPriority() {
        return priority;
    }

    SegmentEventPriority(String code,Integer priority){
        this.code = code;
        this.priority = priority;
    }

    private static Map<String, Integer> prepareSegmentEventMap(){

        Map<String,Integer> segPriorityMap = new HashMap<>();
        for (SegmentEventPriority segmentEventPriority : SegmentEventPriority.values()) {
            segPriorityMap.put(segmentEventPriority.code, segmentEventPriority.priority);
        }
        return segPriorityMap;
    }

    public static Integer getSegmentPriority(String code) {
        return StringUtils.isNotBlank(code) ? segmentEventPriorityMap.get(StringUtils.upperCase(code)):null;
    }

}
