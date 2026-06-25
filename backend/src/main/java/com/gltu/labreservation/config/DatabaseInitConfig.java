package com.gltu.labreservation.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class DatabaseInitConfig {

    @Bean
    public CommandLineRunner initExtraTables(JdbcTemplate jdbcTemplate) {
        return args -> {
            jdbcTemplate.execute("""
                    CREATE TABLE IF NOT EXISTS operation_log (
                        id BIGINT PRIMARY KEY AUTO_INCREMENT,
                        user_id BIGINT DEFAULT NULL,
                        username VARCHAR(50) DEFAULT NULL,
                        module_name VARCHAR(50) NOT NULL,
                        operation VARCHAR(50) NOT NULL,
                        detail VARCHAR(500) DEFAULT NULL,
                        create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                        update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        deleted TINYINT NOT NULL DEFAULT 0
                    ) COMMENT='操作日志表'
                    """);

            jdbcTemplate.execute("""
                    CREATE TABLE IF NOT EXISTS password_reset_code (
                        id BIGINT PRIMARY KEY AUTO_INCREMENT,
                        account VARCHAR(50) NOT NULL,
                        receiver VARCHAR(100) NOT NULL,
                        code VARCHAR(10) NOT NULL,
                        expire_time DATETIME NOT NULL,
                        used TINYINT NOT NULL DEFAULT 0,
                        create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                        update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        deleted TINYINT NOT NULL DEFAULT 0
                    ) COMMENT='密码重置验证码表'
                    """);

            Integer emailColumnCount = jdbcTemplate.queryForObject("""
                    SELECT COUNT(*)
                    FROM information_schema.COLUMNS
                    WHERE TABLE_SCHEMA = DATABASE()
                      AND TABLE_NAME = 'sys_user'
                      AND COLUMN_NAME = 'email'
                    """, Integer.class);
            if (emailColumnCount == null || emailColumnCount == 0) {
                jdbcTemplate.execute("ALTER TABLE sys_user ADD COLUMN email VARCHAR(100) DEFAULT NULL COMMENT '邮箱'");
            }
        };
    }
}
