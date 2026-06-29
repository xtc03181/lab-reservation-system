CREATE DATABASE IF NOT EXISTS lab_reservation DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE lab_reservation;
SET NAMES utf8mb4;

DROP TABLE IF EXISTS notice;
DROP TABLE IF EXISTS sys_message;
DROP TABLE IF EXISTS reservation_rule;
DROP TABLE IF EXISTS equipment_borrow;
DROP TABLE IF EXISTS lab_reservation;
DROP TABLE IF EXISTS equipment;
DROP TABLE IF EXISTS lab;
DROP TABLE IF EXISTS sys_user;

CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '登录账号',
    password VARCHAR(100) NOT NULL COMMENT '登录密码',
    real_name VARCHAR(50) NOT NULL COMMENT '真实姓名',
    phone VARCHAR(30) DEFAULT NULL COMMENT '联系电话',
    role VARCHAR(20) NOT NULL COMMENT 'STUDENT/TEACHER/ADMIN',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1启用 0禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0
) COMMENT='系统用户表';

CREATE TABLE lab (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '实验室名称',
    location VARCHAR(100) NOT NULL COMMENT '实验室位置',
    capacity INT NOT NULL DEFAULT 0 COMMENT '容纳人数',
    manager VARCHAR(50) DEFAULT NULL COMMENT '负责人',
    description VARCHAR(500) DEFAULT NULL COMMENT '实验室介绍',
    open_days VARCHAR(30) NOT NULL DEFAULT '1,2,3,4,5' COMMENT '开放星期，1-7表示周一到周日',
    open_start_time TIME NOT NULL DEFAULT '08:00:00' COMMENT '每日开放开始时间',
    open_end_time TIME NOT NULL DEFAULT '18:00:00' COMMENT '每日开放结束时间',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1开放 0停用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0
) COMMENT='实验室信息表';

CREATE TABLE equipment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '设备名称',
    code VARCHAR(50) NOT NULL UNIQUE COMMENT '设备编号',
    lab_id BIGINT NOT NULL COMMENT '所属实验室',
    model VARCHAR(100) DEFAULT NULL COMMENT '型号',
    total_count INT NOT NULL DEFAULT 1 COMMENT '设备总数',
    available_count INT NOT NULL DEFAULT 1 COMMENT '可借数量',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1可用 0停用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0
) COMMENT='设备信息表';

CREATE TABLE lab_reservation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '申请人',
    lab_id BIGINT NOT NULL COMMENT '实验室',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME NOT NULL COMMENT '结束时间',
    purpose VARCHAR(500) NOT NULL COMMENT '预约用途',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/REJECTED/CANCELED',
    reviewer_id BIGINT DEFAULT NULL COMMENT '审核人',
    review_opinion VARCHAR(500) DEFAULT NULL COMMENT '审核意见',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0
) COMMENT='实验室预约表';

CREATE TABLE reservation_rule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    max_advance_days INT NOT NULL DEFAULT 7 COMMENT '最多提前预约天数',
    max_duration_hours INT NOT NULL DEFAULT 4 COMMENT '单次最长预约小时数',
    daily_limit INT NOT NULL DEFAULT 2 COMMENT '每人每天最多预约次数',
    allow_weekend TINYINT NOT NULL DEFAULT 1 COMMENT '1允许周末 0不允许周末',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0
) COMMENT='预约规则配置表';

CREATE TABLE equipment_borrow (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '申请人',
    equipment_id BIGINT NOT NULL COMMENT '设备',
    borrow_count INT NOT NULL DEFAULT 1 COMMENT '借用数量',
    borrow_time DATETIME NOT NULL COMMENT '借用时间',
    return_time DATETIME NOT NULL COMMENT '预计归还时间',
    purpose VARCHAR(500) NOT NULL COMMENT '借用用途',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/REJECTED/RETURNED',
    reviewer_id BIGINT DEFAULT NULL COMMENT '审核人',
    review_opinion VARCHAR(500) DEFAULT NULL COMMENT '审核意见',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0
) COMMENT='设备借用表';

CREATE TABLE notice (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(100) NOT NULL COMMENT '公告标题',
    content TEXT NOT NULL COMMENT '公告内容',
    publisher_id BIGINT NOT NULL COMMENT '发布人',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1发布 0下架',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0
) COMMENT='公告表';

CREATE TABLE sys_message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    receiver_id BIGINT NOT NULL COMMENT '接收人',
    title VARCHAR(100) NOT NULL COMMENT '消息标题',
    content VARCHAR(500) NOT NULL COMMENT '消息内容',
    type VARCHAR(30) NOT NULL COMMENT '消息类型',
    read_status TINYINT NOT NULL DEFAULT 0 COMMENT '0未读 1已读',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0
) COMMENT='站内消息表';

INSERT INTO sys_user(username, password, real_name, phone, role, status)
VALUES
('admin', 'Abc12345!', '系统管理员', '13800000000', 'ADMIN', 1),
('teacher', 'Abc12345!', '审核教师', '13800000001', 'TEACHER', 1),
('student', 'Abc12345!', '学生用户', '13800000002', 'STUDENT', 1);

INSERT INTO lab(name, location, capacity, manager, description, open_days, open_start_time, open_end_time, status)
VALUES
('网络安全实验室', '14102', 50, '王老师', '用于网络攻防、协议分析和安全实验。', '1,2,3,4,5', '08:00:00', '18:00:00', 1),
('软件开发实验室', '14103', 45, '陈老师', '用于 Java Web、数据库和综合项目开发。', '1,2,3,4,5', '08:00:00', '18:00:00', 1);

INSERT INTO equipment(name, code, lab_id, model, total_count, available_count, status)
VALUES
('交换机', 'SW-001', 1, 'H3C S5120', 10, 8, 1),
('实验服务器', 'SV-001', 2, 'Dell PowerEdge', 4, 3, 1);

INSERT INTO reservation_rule(max_advance_days, max_duration_hours, daily_limit, allow_weekend, status)
VALUES
(7, 4, 2, 1, 1);

INSERT INTO notice(title, content, publisher_id, status)
VALUES
('系统试运行通知', '实验室预约申请与设备管理系统进入试运行阶段。', 1, 1);
