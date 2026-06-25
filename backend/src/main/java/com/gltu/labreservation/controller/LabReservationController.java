package com.gltu.labreservation.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gltu.labreservation.common.ApiResponse;
import com.gltu.labreservation.dto.ReviewRequest;
import com.gltu.labreservation.entity.LabReservation;
import com.gltu.labreservation.entity.User;
import com.gltu.labreservation.mapper.UserMapper;
import com.gltu.labreservation.service.LabReservationService;
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
@RequestMapping("/lab-reservations")
public class LabReservationController {

    private final LabReservationService reservationService;
    private final OperationLogService operationLogService;
    private final UserMapper userMapper;

    public LabReservationController(LabReservationService reservationService,
                                    OperationLogService operationLogService,
                                    UserMapper userMapper) {
        this.reservationService = reservationService;
        this.operationLogService = operationLogService;
        this.userMapper = userMapper;
    }

    @GetMapping
    public ApiResponse<List<LabReservation>> list(@RequestHeader("X-User-Role") String role,
                                                  @RequestHeader("X-User-Id") Long userId) {
        if ("STUDENT".equalsIgnoreCase(role)) {
            return ApiResponse.success(reservationService.list(new LambdaQueryWrapper<LabReservation>()
                    .eq(LabReservation::getUserId, userId)));
        }
        return ApiResponse.success(reservationService.list());
    }

    @PostMapping
    public ApiResponse<Void> create(@RequestHeader("X-User-Id") Long userId,
                                    @RequestBody LabReservation reservation) {
        reservation.setUserId(userId);
        reservationService.create(reservation);
        record(userId, "实验室预约", "新增预约", "提交实验室ID：" + reservation.getLabId());
        return ApiResponse.success();
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id,
                                    @RequestHeader("X-User-Id") Long userId,
                                    @RequestBody LabReservation reservation) {
        reservationService.updateReservation(id, userId, reservation);
        record(userId, "实验室预约", "修改预约", "预约ID：" + id);
        return ApiResponse.success();
    }

    @PutMapping("/{id}/cancel")
    public ApiResponse<Void> cancel(@PathVariable Long id, @RequestHeader("X-User-Id") Long userId) {
        reservationService.cancel(id, userId);
        record(userId, "实验室预约", "取消预约", "预约ID：" + id);
        return ApiResponse.success();
    }

    @PutMapping("/{id}/review")
    public ApiResponse<Void> review(@PathVariable Long id,
                                    @RequestHeader("X-User-Id") Long userId,
                                    @Valid @RequestBody ReviewRequest request) {
        request.setReviewerId(userId);
        reservationService.review(id, request);
        record(userId, "实验室预约", "审核预约", "预约ID：" + id + "，结果：" + request.getStatus());
        return ApiResponse.success();
    }

    private void record(Long userId, String moduleName, String operation, String detail) {
        User user = userMapper.selectById(userId);
        String username = user == null ? String.valueOf(userId) : user.getUsername();
        operationLogService.record(userId, username, moduleName, operation, detail);
    }
}
