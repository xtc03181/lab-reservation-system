package com.gltu.labreservation.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gltu.labreservation.entity.OperationLog;

public interface OperationLogService extends IService<OperationLog> {

    void record(Long userId, String username, String moduleName, String operation, String detail);
}

