package com.werner.repository;

import com.werner.model.Transaction;
import org.springframework.data.repository.CrudRepository;

public interface EventTransactionRepository extends CrudRepository<Transaction, Long> {
}
