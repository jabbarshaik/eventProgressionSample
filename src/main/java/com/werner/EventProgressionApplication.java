package com.werner;

import com.werner.repository.EquipmentLatestStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EventProgressionApplication /*implements CommandLineRunner*/ {

	@Autowired
	EquipmentLatestStatusRepository repository;

	public static void main(String[] args) {
		SpringApplication.run(EventProgressionApplication.class, args);
	}


/*
	@Override
	public void run(String... args) throws Exception {
		repository.save(new EquipmentLatestStatus("EMHU123456","2018/10/24","OE","Origin Street","Loading"));
	}*/
}
