package com.werner.repository;

import com.werner.model.Transaction;
import org.springframework.data.repository.CrudRepository;

public interface TransactionRepository extends CrudRepository<Transaction,Long> {

    Transaction findByEquipNumberAndStatus(String equipNumber, String status);

    Transaction findByRezTrackingNumberAndStatus(String rezTrackingNumber, String status);
}
