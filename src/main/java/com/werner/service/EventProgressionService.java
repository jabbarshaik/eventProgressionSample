package com.werner.service;

import com.werner.model.EquipmentLatestStatus;
import com.werner.vo.EquipmentEventRequest;
import com.werner.vo.EquipmentEventResponse;

public interface EventProgressionService {

    EquipmentEventResponse processEquipmentEvent(EquipmentEventRequest request);

    EquipmentLatestStatus getLatestEquipStatus(String equipmentNumber);
}
