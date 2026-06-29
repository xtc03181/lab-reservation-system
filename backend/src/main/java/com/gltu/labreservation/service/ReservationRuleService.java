package com.gltu.labreservation.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gltu.labreservation.entity.ReservationRule;

public interface ReservationRuleService extends IService<ReservationRule> {

    ReservationRule getActiveRule();
}
