package com.gltu.labreservation.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gltu.labreservation.entity.OperationLog;
import com.gltu.labreservation.mapper.OperationLogMapper;
import com.gltu.labreservation.service.OperationLogService;
import org.springframework.stereotype.Service;

@Service
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog>
        implements OperationLogService {

    @Override
    public void record(Long userId, String username, String moduleName, String operation, String detail) {
        OperationLog log = new OperationLog();
        log.setUserId(userId);
        log.setUsername(username);
        log.setModuleName(moduleName);
        log.setOperation(operation);
        log.setDetail(detail);
        save(log);
    }
}

