USE lab_reservation;

DROP PROCEDURE IF EXISTS add_lab_column_if_missing;

DELIMITER //
CREATE PROCEDURE add_lab_column_if_missing(IN column_name VARCHAR(64), IN column_definition TEXT)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'lab'
          AND COLUMN_NAME = column_name
    ) THEN
        SET @ddl = CONCAT('ALTER TABLE lab ADD COLUMN ', column_definition);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END//
DELIMITER ;

CALL add_lab_column_if_missing(
    'open_days',
    'open_days VARCHAR(30) NOT NULL DEFAULT ''1,2,3,4,5'' COMMENT ''开放星期，1-7表示周一到周日'' AFTER description'
);

CALL add_lab_column_if_missing(
    'open_start_time',
    'open_start_time TIME NOT NULL DEFAULT ''08:00:00'' COMMENT ''每日开放开始时间'' AFTER open_days'
);

CALL add_lab_column_if_missing(
    'open_end_time',
    'open_end_time TIME NOT NULL DEFAULT ''18:00:00'' COMMENT ''每日开放结束时间'' AFTER open_start_time'
);

DROP PROCEDURE IF EXISTS add_lab_column_if_missing;
