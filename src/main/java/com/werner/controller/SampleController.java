package com.werner.controller;

import com.werner.model.EquipmentLatestStatus;
import com.werner.repository.EquipmentLatestStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;


@RestController
public class SampleController {


    @Autowired
    EquipmentLatestStatusRepository equipmentLatestStatusRepository;


    @GetMapping(value = "/process")
    public String process(){
        equipmentLatestStatusRepository.save(new EquipmentLatestStatus("EMHU123456","2018/10/24","OE","Origin Street","Loading"));
        return  "SUCCESS";
    }

    @GetMapping(value = "/hello")
	public String hello(){
		return  "Hello";
	}


    @GetMapping(value = "/now")
    public String getNow() {
        return LocalDateTime.now().toString();
    }
}
