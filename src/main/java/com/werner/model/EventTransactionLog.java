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
public class EventTransactionLog extends  BaseAuditType{


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long eventTransactionLogId;

    private String equipNumber;
    private String segmentNumber;
    private String shipmentNumber;
    private String rezTrackingNumber;
    private String equipStatus;
    private String streetStatus;
    private String eventDate;
    private String eventType;


}
