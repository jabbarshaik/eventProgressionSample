package com.werner.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

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

    @OneToOne
    @JoinColumn(name = "transactionId", referencedColumnName = "transactionId")
    private Transaction transaction;

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
