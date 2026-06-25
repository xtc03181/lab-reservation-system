package com.gltu.labreservation.entity;

import com.baomidou.mybatisplus.annotation.TableName;

@TableName("operation_log")
public class OperationLog extends BaseEntity {

    private Long userId;
    private String username;
    private String moduleName;
    private String operation;
    private String detail;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }
}

