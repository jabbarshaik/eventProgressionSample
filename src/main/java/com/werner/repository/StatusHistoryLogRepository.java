package com.werner.repository;

import com.werner.model.StatusHistoryLog;
import org.springframework.data.repository.CrudRepository;

public interface StatusHistoryLogRepository extends CrudRepository<StatusHistoryLog, Long> {
}
