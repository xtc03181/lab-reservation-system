package com.gltu.labreservation.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gltu.labreservation.common.ApiResponse;
import com.gltu.labreservation.dto.ReviewRequest;
import com.gltu.labreservation.entity.EquipmentBorrow;
import com.gltu.labreservation.entity.User;
import com.gltu.labreservation.mapper.UserMapper;
import com.gltu.labreservation.service.EquipmentBorrowService;
import com.gltu.labreservation.service.OperationLogService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/equipment-borrows")
public class EquipmentBorrowController {

    private final EquipmentBorrowService borrowService;
    private final OperationLogService operationLogService;
    private final UserMapper userMapper;

    public EquipmentBorrowController(EquipmentBorrowService borrowService,
                                     OperationLogService operationLogService,
                                     UserMapper userMapper) {
        this.borrowService = borrowService;
        this.operationLogService = operationLogService;
        this.userMapper = userMapper;
    }

    @GetMapping
    public ApiResponse<List<EquipmentBorrow>> list(@RequestHeader("X-User-Role") String role,
                                                   @RequestHeader("X-User-Id") Long userId) {
        if ("STUDENT".equalsIgnoreCase(role)) {
            return ApiResponse.success(borrowService.list(new LambdaQueryWrapper<EquipmentBorrow>()
                    .eq(EquipmentBorrow::getUserId, userId)));
        }
        return ApiResponse.success(borrowService.list());
    }

    @PostMapping
    public ApiResponse<Void> create(@RequestHeader("X-User-Id") Long userId, @RequestBody EquipmentBorrow borrow) {
        borrow.setUserId(userId);
        borrowService.create(borrow);
        record(userId, "设备借用", "新增借用", "设备ID：" + borrow.getEquipmentId());
        return ApiResponse.success();
    }

    @PutMapping("/{id}/review")
    public ApiResponse<Void> review(@PathVariable Long id,
                                    @RequestHeader("X-User-Id") Long userId,
                                    @Valid @RequestBody ReviewRequest request) {
        request.setReviewerId(userId);
        borrowService.review(id, request);
        record(userId, "设备借用", "处理借用", "借用ID：" + id + "，结果：" + request.getStatus());
        return ApiResponse.success();
    }

    private void record(Long userId, String moduleName, String operation, String detail) {
        User user = userMapper.selectById(userId);
        String username = user == null ? String.valueOf(userId) : user.getUsername();
        operationLogService.record(userId, username, moduleName, operation, detail);
    }
}
