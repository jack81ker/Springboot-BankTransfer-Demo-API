package com.example.wide.service.impl;

import com.example.wide.entities.TransferLog;
import com.example.wide.repository.TransferLogRepository;
import com.example.wide.service.TransferLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransferLogServiceImpl implements TransferLogService {
    private final TransferLogRepository transferLogRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveTransferLog(TransferLog transferLog) {
        transferLogRepository.save(transferLog);
    }
}
