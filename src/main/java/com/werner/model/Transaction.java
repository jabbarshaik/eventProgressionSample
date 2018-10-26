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
public class Transaction extends  BaseAuditType {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer transactionId;

    private String equipNumber;
    private String segmentNumber;
    private String shipmentNumber;
    private String rezTrackingNumber;
    private String splc;
    private String programName;
    private String custRefNumber;
    private String currentEventCode;
    private String startEventCode;
    private String startDate;
    private String endEventCode;
    private String endDate;
    private String status;



}
