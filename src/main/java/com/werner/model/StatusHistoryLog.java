package com.werner.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
@Getter
@Setter
public class StatusHistoryLog extends  BaseAuditType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long statusHistoryLogId;

    private String equipNumber;
    private String eventDate;
    private String eventType;

    private String equipStatus;
    private String streetStatus;
    private String trackingNumber;
    private String shipmentNumber;
    private String segmentNumber;
    private String loadOption;
    private String eventCarrier;
    private String eventSplc;
    private Integer segmentPriority;
    private Integer segmentEventPriority;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "transactionId", referencedColumnName = "transactionId")
    private Transaction transaction;





}
