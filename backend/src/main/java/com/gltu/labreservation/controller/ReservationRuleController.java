package com.gltu.labreservation.controller;

import com.gltu.labreservation.common.ApiResponse;
import com.gltu.labreservation.entity.ReservationRule;
import com.gltu.labreservation.service.ReservationRuleService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reservation-rules")
public class ReservationRuleController {

    private final ReservationRuleService reservationRuleService;

    public ReservationRuleController(ReservationRuleService reservationRuleService) {
        this.reservationRuleService = reservationRuleService;
    }

    @GetMapping("/active")
    public ApiResponse<ReservationRule> active() {
        return ApiResponse.success(reservationRuleService.getActiveRule());
    }

    @PutMapping("/active")
    public ApiResponse<Void> update(@RequestBody ReservationRule rule) {
        ReservationRule current = reservationRuleService.getActiveRule();
        current.setMaxAdvanceDays(rule.getMaxAdvanceDays());
        current.setMaxDurationHours(rule.getMaxDurationHours());
        current.setDailyLimit(rule.getDailyLimit());
        current.setAllowWeekend(rule.getAllowWeekend());
        current.setStatus(1);
        reservationRuleService.updateById(current);
        return ApiResponse.success();
    }
}
