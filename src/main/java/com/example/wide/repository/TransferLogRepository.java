package com.example.wide.repository;

import com.example.wide.entities.TransferLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransferLogRepository extends JpaRepository<TransferLog, Long> {
}
