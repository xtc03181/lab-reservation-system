package com.gltu.labreservation.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalTime;

@TableName("lab")
public class Lab extends BaseEntity {

    private String name;
    private String location;
    private Integer capacity;
    private String manager;
    private String description;
    private String openDays;
    private LocalTime openStartTime;
    private LocalTime openEndTime;
    private Integer status;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public String getManager() {
        return manager;
    }

    public void setManager(String manager) {
        this.manager = manager;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOpenDays() {
        return openDays;
    }

    public void setOpenDays(String openDays) {
        this.openDays = openDays;
    }

    public LocalTime getOpenStartTime() {
        return openStartTime;
    }

    public void setOpenStartTime(LocalTime openStartTime) {
        this.openStartTime = openStartTime;
    }

    public LocalTime getOpenEndTime() {
        return openEndTime;
    }

    public void setOpenEndTime(LocalTime openEndTime) {
        this.openEndTime = openEndTime;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
