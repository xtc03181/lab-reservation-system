package com.gltu.labreservation.entity;

import com.baomidou.mybatisplus.annotation.TableName;

@TableName("reservation_rule")
public class ReservationRule extends BaseEntity {

    private Integer maxAdvanceDays;
    private Integer maxDurationHours;
    private Integer dailyLimit;
    private Integer allowWeekend;
    private Integer status;

    public Integer getMaxAdvanceDays() {
        return maxAdvanceDays;
    }

    public void setMaxAdvanceDays(Integer maxAdvanceDays) {
        this.maxAdvanceDays = maxAdvanceDays;
    }

    public Integer getMaxDurationHours() {
        return maxDurationHours;
    }

    public void setMaxDurationHours(Integer maxDurationHours) {
        this.maxDurationHours = maxDurationHours;
    }

    public Integer getDailyLimit() {
        return dailyLimit;
    }

    public void setDailyLimit(Integer dailyLimit) {
        this.dailyLimit = dailyLimit;
    }

    public Integer getAllowWeekend() {
        return allowWeekend;
    }

    public void setAllowWeekend(Integer allowWeekend) {
        this.allowWeekend = allowWeekend;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
