package com.werner.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Getter
@Setter
public class EquipmentLatestStatus extends  BaseAuditType{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long equipmentLatestStatusId;

    private String equipNumber;
    private String eventDate;
    private String eventType;

    private String equipStatus;
    private String streetStatus;
    private String segmentNumber;

    public EquipmentLatestStatus(){

    }


    public EquipmentLatestStatus(String equipNumber, String eventDate, String eventType, String equipStatus, String streetStatus) {
        this.equipNumber = equipNumber;
        this.eventDate = eventDate;
        this.eventType = eventType;
        this.equipStatus = equipStatus;
        this.streetStatus = streetStatus;
    }
}
