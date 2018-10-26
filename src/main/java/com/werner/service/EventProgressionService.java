package com.werner.service;

import com.werner.model.EquipmentLatestStatus;
import com.werner.vo.EquipmentEventRequest;
import com.werner.vo.EquipmentEventResponse;

import java.util.List;

public interface EventProgressionService {

    EquipmentEventResponse processEquipmentEvent(List<EquipmentEventRequest> requests);

    EquipmentLatestStatus getLatestEquipStatus(String equipmentNumber);
}
