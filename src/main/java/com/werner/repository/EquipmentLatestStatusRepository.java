package com.werner.repository;

import com.werner.model.EquipmentLatestStatus;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EquipmentLatestStatusRepository extends CrudRepository<EquipmentLatestStatus, Long> {

    EquipmentLatestStatus findByEquipNumber(String equipNumber);
}
