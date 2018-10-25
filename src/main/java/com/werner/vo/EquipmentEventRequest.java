package com.werner.vo;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class EquipmentEventRequest implements Serializable {

    private String equipmentNumber;
    private String eventCode;
    private String shipmentNumber;
    private String segmentNumber;
    private String segmentType;
    private String loadOption;
    private String eventDate;
    private String trackingNum;
    private String eventCarrier;
    private String eventSplc;
    private List<SegmentOrder> segmentOrders = new ArrayList<>();
}
