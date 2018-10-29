package com.werner.repository;

import com.werner.model.StatusHistoryLog;
import com.werner.model.Transaction;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface StatusHistoryLogRepository extends CrudRepository<StatusHistoryLog, Long> {

    List<StatusHistoryLog> findByTransaction(Transaction transaction);
}
