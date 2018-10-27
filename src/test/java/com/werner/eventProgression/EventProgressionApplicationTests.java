package com.werner.eventProgression;

import com.werner.service.EventProgressionService;
import com.werner.vo.EquipmentEventRequest;
import com.werner.vo.EquipmentEventResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.util.Arrays;

@RunWith(SpringRunner.class)
@SpringBootTest
public class EventProgressionApplicationTests {


	@Autowired
	EventProgressionService service;

	@Test
	public void contextLoads() {


	}

	@Test
	public void testSendRCEvent(){
		final EquipmentEventResponse response = service.processEquipmentEvent(Arrays.asList(prepareRequestObject("RC", null,"track1")));
	}


	@Test
	public void testSendOEEvent(){
		final EquipmentEventResponse response = service.processEquipmentEvent(Arrays.asList(prepareRequestObject("OE", "UMXU123456","track1")));
	}

	private EquipmentEventRequest prepareRequestObject(String eventCode,String equipmentNumber,String tackingNumber) {

		EquipmentEventRequest request = new EquipmentEventRequest();

		request.setEquipmentNumber(equipmentNumber);
		request.setEventCarrier("ABC Carrier INC");
		request.setEventCode(eventCode);
		request.setEventDate(LocalDate.now().toString());
		request.setTrackingNum(tackingNumber);
		return request;
	}
}
