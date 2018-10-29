package com.werner.vo;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class EquipmentEventResponse implements Serializable {

    private String equipNumber;
    private String shipmentNumber;
    private String segmentNumber;
    private String equipmentStatus;
    private String streetStatus;
    private String message;
    private String errorMessage;
}
