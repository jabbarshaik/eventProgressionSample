package com.werner.repository;

import com.werner.model.Transaction;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TransactionRepository extends CrudRepository<Transaction,Long> {

    Transaction findByEquipNumberAndStatus(String equipNumber, String status);

    Transaction findByRezTrackingNumberAndStatus(String rezTrackingNumber, String status);

    List<Transaction> findByRezTrackingNumberAndStatusIn(String rezTrackingNumber, List<String> statuses);

    List<Transaction> findByEquipNumberAndStatusInAndShipmentNumberNotNull(String equipNumber,  List<String> statuses);

    List<Transaction> findByEquipNumberAndStatusInOrderByCreateDateDesc(String equipNumber,  List<String> statuses);

    List<Transaction> findByEquipNumberAndStatusInAndShipmentNumberIsNull(String equipNumber,  List<String> statuses);

    Transaction findByRezTrackingNumber(String rezTrackingNumber);

    Transaction findByEquipNumberAndShipmentNumber(String equipNumber, String shipmentNumber);

    Transaction findByEquipNumberAndShipmentNumberAndStatus(String equipNumber, String shipmentNumber, String status);


    List<Transaction> findByShipmentNumberAndStatus(String shipmentNumber, String status);

    List<Transaction> findByShipmentNumberAndStatusAndEquipNumberNotNull(String shipmentNumber, String status);

    List<Transaction> findByEquipNumberAndStatusAndRezTrackingNumberNotNull(String equipNumber, String status);

    Transaction findByEquipNumberAndRezTrackingNumberNotNull(String equipNumber);

}
