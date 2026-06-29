package com.gltu.labreservation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gltu.labreservation.entity.ReservationRule;
import com.gltu.labreservation.mapper.ReservationRuleMapper;
import com.gltu.labreservation.service.ReservationRuleService;
import org.springframework.stereotype.Service;

@Service
public class ReservationRuleServiceImpl extends ServiceImpl<ReservationRuleMapper, ReservationRule>
        implements ReservationRuleService {

    @Override
    public ReservationRule getActiveRule() {
        ReservationRule rule = getOne(new LambdaQueryWrapper<ReservationRule>()
                .eq(ReservationRule::getStatus, 1)
                .orderByDesc(ReservationRule::getId)
                .last("LIMIT 1"));
        if (rule != null) {
            return rule;
        }
        ReservationRule defaultRule = new ReservationRule();
        defaultRule.setMaxAdvanceDays(7);
        defaultRule.setMaxDurationHours(4);
        defaultRule.setDailyLimit(2);
        defaultRule.setAllowWeekend(1);
        defaultRule.setStatus(1);
        save(defaultRule);
        return defaultRule;
    }
}
