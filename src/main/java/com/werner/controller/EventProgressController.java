package com.werner.controller;

import com.werner.model.EquipmentLatestStatus;
import com.werner.service.EventProgressionServiceImpl;
import com.werner.vo.EquipmentEventRequest;
import com.werner.vo.EquipmentEventResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EventProgressController {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    EventProgressionServiceImpl service;

    @PostMapping(value =  "/process")
    public ResponseEntity<EquipmentEventResponse> processEquipEvent(@RequestBody EquipmentEventRequest request){
        return new ResponseEntity<>(service.processEquipmentEvent(request), HttpStatus.OK);
    }

    @GetMapping(value =  "/getEquipLatestStatus/{equipmentNumber}")
    public ResponseEntity<EquipmentLatestStatus> getEquipLatestStatus(@PathVariable("equipmentNumber") String equipmentNumber){
        return new ResponseEntity<>(service.getLatestEquipStatus(equipmentNumber), HttpStatus.OK);
    }

}
