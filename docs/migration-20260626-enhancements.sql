USE lab_reservation;

CREATE TABLE IF NOT EXISTS sys_message (
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

CREATE TABLE IF NOT EXISTS reservation_rule (
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

INSERT INTO reservation_rule(max_advance_days, max_duration_hours, daily_limit, allow_weekend, status)
SELECT 7, 4, 2, 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM reservation_rule WHERE status = 1
);
