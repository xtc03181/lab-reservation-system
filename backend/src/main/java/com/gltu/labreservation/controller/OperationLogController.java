package com.gltu.labreservation.controller;

import com.gltu.labreservation.common.ApiResponse;
import com.gltu.labreservation.entity.OperationLog;
import com.gltu.labreservation.service.OperationLogService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/operation-logs")
public class OperationLogController {

    private final OperationLogService operationLogService;

    public OperationLogController(OperationLogService operationLogService) {
        this.operationLogService = operationLogService;
    }

    @GetMapping
    public ApiResponse<List<OperationLog>> list() {
        return ApiResponse.success(operationLogService.list());
    }
}

