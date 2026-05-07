/*
 Navicat Premium Dump SQL

 Source Server         : root
 Source Server Type    : MySQL
 Source Server Version : 80041 (8.0.41)
 Source Host           : localhost:3306
 Source Schema         : building_energy_standard

 Target Server Type    : MySQL
 Target Server Version : 80041 (8.0.41)
 File Encoding         : 65001

 Date: 07/05/2026 11:32:18
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for ai_chat_context
-- ----------------------------
DROP TABLE IF EXISTS `ai_chat_context`;
CREATE TABLE `ai_chat_context`  (
  `conversation_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `user_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '新对话',
  `system_prompt` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
  `is_pinned` tinyint(1) NULL DEFAULT 0,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`conversation_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for alert_record
-- ----------------------------
DROP TABLE IF EXISTS `alert_record`;
CREATE TABLE `alert_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `device_id` int UNSIGNED NOT NULL COMMENT '设备ID，关联devices表',
  `device_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '设备编号（冗余字段，便于快速展示，也可通过JOIN获取）',
  `building_id` int UNSIGNED NULL DEFAULT NULL COMMENT '建筑ID，冗余便于按建筑筛选',
  `reading_id` bigint NULL DEFAULT NULL COMMENT '触发告警的能耗读数ID，关联energy_readings表',
  `threshold_id` bigint NULL DEFAULT NULL COMMENT '触发的阈值规则ID，关联threshold_range表',
  `metric_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '异常指标名称：power_consumption, water_consumption, ac_power_consumption等',
  `abnormal_value` decimal(12, 3) NOT NULL COMMENT '异常时刻的实际读数',
  `min_value` decimal(10, 2) NULL DEFAULT NULL COMMENT '阈值下限（告警时快照）',
  `max_value` decimal(10, 2) NULL DEFAULT NULL COMMENT '阈值上限（告警时快照）',
  `unit` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '单位（快照）',
  `alert_type` enum('ABOVE_MAX','BELOW_MIN') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '异常类型：超上限 / 低于下限',
  `alert_level` tinyint NULL DEFAULT 1 COMMENT '告警级别：1-提示，2-一般，3-严重，可根据业务扩展',
  `status` tinyint NULL DEFAULT 0 COMMENT '处理状态：0-未处理，1-已确认，2-已忽略，3-已解决',
  `handled_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '处理人（用户标识）',
  `handled_at` datetime NULL DEFAULT NULL COMMENT '处理时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '处理备注或系统备注',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '告警生成时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_device_id`(`device_id` ASC) USING BTREE,
  INDEX `idx_building_id`(`building_id` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_metric_name`(`metric_name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4589 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '设备能耗异常告警记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for buildings
-- ----------------------------
DROP TABLE IF EXISTS `buildings`;
CREATE TABLE `buildings`  (
  `building_id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '建筑自增ID',
  `building_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '建筑编号，如B001',
  `building_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '建筑类型：高层住宅/小高层住宅',
  `building_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '建筑名称：1号楼',
  `location` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '位置：南区/北区',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`building_id`) USING BTREE,
  UNIQUE INDEX `building_code`(`building_code` ASC) USING BTREE,
  INDEX `idx_building_code`(`building_code` ASC) USING BTREE,
  INDEX `idx_building_type`(`building_type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 18 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '建筑基础信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for chat_record
-- ----------------------------
DROP TABLE IF EXISTS `chat_record`;
CREATE TABLE `chat_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `sender_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '发送角色',
  `sender_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '发送者ID',
  `sender_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '发送者昵称',
  `receiver_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '接收者ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '消息内容',
  `type` int NULL DEFAULT 0 COMMENT '0:文本',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 42 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '聊天记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for customer
-- ----------------------------
DROP TABLE IF EXISTS `customer`;
CREATE TABLE `customer`  (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `phone` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `avatar_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT NULL,
  `building_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `device_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for daily_energy_summary
-- ----------------------------
DROP TABLE IF EXISTS `daily_energy_summary`;
CREATE TABLE `daily_energy_summary`  (
  `building_id` int UNSIGNED NOT NULL,
  `summary_date` date NOT NULL,
  `total_power` decimal(12, 2) NULL DEFAULT 0.00,
  `total_water` decimal(12, 2) NULL DEFAULT 0.00,
  `avg_env_temp` decimal(5, 2) NULL DEFAULT NULL,
  `max_power` decimal(10, 2) NULL DEFAULT NULL,
  `record_count` int UNSIGNED NULL DEFAULT 0,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`building_id`, `summary_date`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '日能耗汇总表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for devices
-- ----------------------------
DROP TABLE IF EXISTS `devices`;
CREATE TABLE `devices`  (
  `device_id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '设备自增ID',
  `device_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '能耗监测设备编号，如B001-CH-01',
  `building_id` int UNSIGNED NOT NULL COMMENT '所属建筑ID',
  `device_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '综合采集器' COMMENT '设备类型',
  `device_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '正常' COMMENT '运行状态：正常/设备故障/维护保养/其他',
  `install_date` date NULL DEFAULT NULL COMMENT '安装日期（可选）',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`device_id`) USING BTREE,
  UNIQUE INDEX `device_code`(`device_code` ASC) USING BTREE,
  INDEX `idx_device_code`(`device_code` ASC) USING BTREE,
  INDEX `idx_device_status`(`device_status` ASC) USING BTREE,
  INDEX `idx_building_id`(`building_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 54 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '能耗监测设备表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for energy_readings
-- ----------------------------
DROP TABLE IF EXISTS `energy_readings`;
CREATE TABLE `energy_readings`  (
  `reading_id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '监测记录ID',
  `building_id` int UNSIGNED NOT NULL COMMENT '建筑ID（冗余存储，方便查询）',
  `device_id` int UNSIGNED NOT NULL COMMENT '设备ID（关联devices表）',
  `monitoring_time` datetime NOT NULL COMMENT '监测时间（精确到小时）',
  `power_consumption` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '电力能耗（单位：kWh）',
  `water_flow_rate` decimal(10, 2) NULL DEFAULT NULL COMMENT '水流量（单位m³/h）',
  `water_consumption` decimal(10, 2) NULL DEFAULT NULL COMMENT '水耗（单位：m³，可选）',
  `ac_power` decimal(10, 2) NULL DEFAULT NULL COMMENT '空调实时功率',
  `ac_power_consumption` decimal(10, 2) NULL DEFAULT NULL COMMENT '空调系统能耗（单位：kWh，可选）',
  `ac_outlet_temp` decimal(5, 2) NULL DEFAULT NULL COMMENT '空调系统出水温度（单位：℃）',
  `ac_inlet_temp` decimal(5, 2) NULL DEFAULT NULL COMMENT '空调系统回水温度（单位：℃）',
  `env_temp` decimal(5, 2) NULL DEFAULT NULL COMMENT '环境温度（单位：℃）',
  `humidity` decimal(5, 2) NULL DEFAULT NULL COMMENT '湿度（单位：%RH）',
  `power_load` decimal(10, 2) NULL DEFAULT NULL COMMENT '电力负载',
  `occupancy_density` decimal(5, 2) NULL DEFAULT NULL COMMENT '人员密度（单位：人/100m²，可选）',
  `data_source` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '数据来源：Michigan_SHIFDR/AlphaBuilding/BDGP/模拟生成',
  `raw_file` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '原始文件名（溯源用）',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '数据入库时间',
  `end_time` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`reading_id`, `monitoring_time`) USING BTREE,
  UNIQUE INDEX `uk_device_time`(`device_id` ASC, `monitoring_time` ASC) USING BTREE,
  INDEX `idx_monitoring_time`(`monitoring_time` ASC) USING BTREE,
  INDEX `idx_building_time`(`building_id` ASC, `monitoring_time` ASC) USING BTREE,
  INDEX `idx_device_time`(`device_id` ASC, `monitoring_time` ASC) USING BTREE,
  INDEX `idx_data_source`(`data_source` ASC) USING BTREE,
  INDEX `idx_building_time_power`(`building_id` ASC, `monitoring_time` ASC, `power_consumption` ASC, `water_consumption` ASC, `env_temp` ASC, `humidity` ASC) USING BTREE,
  INDEX `idx_device_time_ac`(`device_id` ASC, `monitoring_time` ASC, `ac_power_consumption` ASC, `ac_outlet_temp` ASC, `ac_inlet_temp` ASC) USING BTREE,
  INDEX `idx_time_id_desc`(`monitoring_time` DESC, `reading_id` DESC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10013 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '能耗监测数据主表（事实表）' ROW_FORMAT = Dynamic PARTITION BY RANGE (to_days(`monitoring_time`))
PARTITIONS 5
(PARTITION `p2024` VALUES LESS THAN (739617) ENGINE = InnoDB MAX_ROWS = 0 MIN_ROWS = 0 ,
PARTITION `p2025` VALUES LESS THAN (739982) ENGINE = InnoDB MAX_ROWS = 0 MIN_ROWS = 0 ,
PARTITION `p2026` VALUES LESS THAN (740347) ENGINE = InnoDB MAX_ROWS = 0 MIN_ROWS = 0 ,
PARTITION `p2027` VALUES LESS THAN (740712) ENGINE = InnoDB MAX_ROWS = 0 MIN_ROWS = 0 ,
PARTITION `p_future` VALUES LESS THAN (MAXVALUE) ENGINE = InnoDB MAX_ROWS = 0 MIN_ROWS = 0 )
;

-- ----------------------------
-- Table structure for pdf_document
-- ----------------------------
DROP TABLE IF EXISTS `pdf_document`;
CREATE TABLE `pdf_document`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文档标题',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文档描述',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '分类（如教务、科研等）',
  `publish_date` date NULL DEFAULT NULL COMMENT '发布日期',
  `file_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'PDF文件名（含扩展名）',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 381 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'PDF文档元数据表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for spring_ai_chat_memory
-- ----------------------------
DROP TABLE IF EXISTS `spring_ai_chat_memory`;
CREATE TABLE `spring_ai_chat_memory`  (
  `conversation_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `type` enum('USER','ASSISTANT','SYSTEM','TOOL') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `timestamp` timestamp NOT NULL,
  INDEX `SPRING_AI_CHAT_MEMORY_CONVERSATION_ID_TIMESTAMP_IDX`(`conversation_id` ASC, `timestamp` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for threshold_range
-- ----------------------------
DROP TABLE IF EXISTS `threshold_range`;
CREATE TABLE `threshold_range`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `building_id` int UNSIGNED NULL DEFAULT NULL COMMENT '建筑ID，NULL表示适用于所有建筑',
  `device_id` int UNSIGNED NULL DEFAULT NULL COMMENT '设备ID，NULL表示适用于该建筑下所有设备或全局',
  `metric_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '指标名称，与能耗表字段对应：power_consumption, water_consumption, ac_power_consumption, ac_outlet_temp, ac_inlet_temp, env_temp, humidity, occupancy_density',
  `min_value` decimal(10, 2) NOT NULL COMMENT '正常范围下限（含）',
  `max_value` decimal(10, 2) NOT NULL COMMENT '正常范围上限（含）',
  `unit` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '单位，如 kWh, m³, ℃, %RH, 人/100m²',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述，如“夏季空调功率范围”',
  `effective_from` datetime NULL DEFAULT NULL COMMENT '生效起始时间，NULL表示永久有效',
  `effective_to` datetime NULL DEFAULT NULL COMMENT '生效结束时间，NULL表示永久有效',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_threshold_unique`(`building_id` ASC, `device_id` ASC, `metric_name` ASC, `effective_from` ASC, `effective_to` ASC) USING BTREE,
  INDEX `idx_metric_name`(`metric_name` ASC) USING BTREE,
  INDEX `idx_effective_time`(`effective_from` ASC, `effective_to` ASC) USING BTREE,
  INDEX `idx_threshold_lookup`(`building_id` ASC, `device_id` ASC, `metric_name` ASC, `effective_from` ASC, `effective_to` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 152 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '指标正常范围配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` int(10) UNSIGNED ZEROFILL NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `phone` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `now_status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `avatar_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for work_order
-- ----------------------------
DROP TABLE IF EXISTS `work_order`;
CREATE TABLE `work_order`  (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '工单ID',
  `order_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '工单编号（唯一）',
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '工单类型（设备故障、维保计划等）',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '故障描述',
  `location` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '位置描述（冗余字段）',
  `building_id` int UNSIGNED NULL DEFAULT NULL COMMENT '关联建筑ID（buildings.building_id）',
  `equipment_id` int UNSIGNED NULL DEFAULT NULL COMMENT '关联设备ID（devices.device_id）',
  `priority` enum('高','中','低') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '中' COMMENT '优先级',
  `status` enum('待处理','处理中','已完成','已关闭') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '待处理' COMMENT '状态',
  `submit_time` datetime NOT NULL COMMENT '提交时间',
  `expected_deadline` datetime NULL DEFAULT NULL COMMENT '期望完成时间（用于超时计算）',
  `completed_time` datetime NULL DEFAULT NULL COMMENT '实际完成时间',
  `handler_id` int UNSIGNED NULL DEFAULT NULL COMMENT '处理人ID（关联user.id）',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '备注',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `order_no`(`order_no` ASC) USING BTREE,
  INDEX `idx_order_no`(`order_no` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_priority`(`priority` ASC) USING BTREE,
  INDEX `idx_submit_time`(`submit_time` ASC) USING BTREE,
  INDEX `idx_expected_deadline`(`expected_deadline` ASC) USING BTREE,
  INDEX `idx_building_id`(`building_id` ASC) USING BTREE,
  INDEX `idx_equipment_id`(`equipment_id` ASC) USING BTREE,
  INDEX `idx_handler_id`(`handler_id` ASC) USING BTREE,
  CONSTRAINT `fk_work_order_building` FOREIGN KEY (`building_id`) REFERENCES `buildings` (`building_id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `fk_work_order_equipment` FOREIGN KEY (`equipment_id`) REFERENCES `devices` (`device_id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `fk_work_order_handler` FOREIGN KEY (`handler_id`) REFERENCES `user` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 18 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '工单主表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for work_order_log
-- ----------------------------
DROP TABLE IF EXISTS `work_order_log`;
CREATE TABLE `work_order_log`  (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `order_id` int UNSIGNED NOT NULL COMMENT '工单ID',
  `action` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '操作类型：创建、认领、处理、关闭、重新打开等',
  `operator_id` int UNSIGNED NULL DEFAULT NULL COMMENT '操作人ID（关联user.id）',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '操作内容（如处理意见）',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE,
  INDEX `idx_operator_id`(`operator_id` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  CONSTRAINT `fk_work_order_log_operator` FOREIGN KEY (`operator_id`) REFERENCES `user` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `fk_work_order_log_order` FOREIGN KEY (`order_id`) REFERENCES `work_order` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 18 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '工单操作记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- View structure for v_overdue_work_order
-- ----------------------------
DROP VIEW IF EXISTS `v_overdue_work_order`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_overdue_work_order` AS select `wo`.`id` AS `id`,`wo`.`order_no` AS `order_no`,`wo`.`type` AS `type`,`wo`.`description` AS `description`,`wo`.`location` AS `location`,`wo`.`building_id` AS `building_id`,`wo`.`equipment_id` AS `equipment_id`,`wo`.`priority` AS `priority`,`wo`.`status` AS `status`,`wo`.`submit_time` AS `submit_time`,`wo`.`expected_deadline` AS `expected_deadline`,`wo`.`completed_time` AS `completed_time`,`wo`.`handler_id` AS `handler_id`,`wo`.`remark` AS `remark`,`wo`.`created_at` AS `created_at`,`wo`.`updated_at` AS `updated_at`,timestampdiff(HOUR,`wo`.`expected_deadline`,now()) AS `overdue_hours`,timestampdiff(MINUTE,`wo`.`expected_deadline`,now()) AS `overdue_minutes` from `work_order` `wo` where ((`wo`.`status` in ('待处理','处理中')) and (`wo`.`expected_deadline` is not null) and (`wo`.`expected_deadline` < now()));

-- ----------------------------
-- Procedure structure for add_next_partition
-- ----------------------------
DROP PROCEDURE IF EXISTS `add_next_partition`;
delimiter ;;
CREATE PROCEDURE `add_next_partition`()
BEGIN
    DECLARE next_year INT;
    SET next_year = YEAR(CURDATE()) + 1;
    SET @sql = CONCAT(
        'ALTER TABLE energy_readings REORGANIZE PARTITION p_future INTO (',
        'PARTITION p', next_year, ' VALUES LESS THAN (TO_DAYS(\'', next_year, '-01-01\')),',
        'PARTITION p_future VALUES LESS THAN MAXVALUE)'
    );
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
END
;;
delimiter ;

-- ----------------------------
-- Event structure for add_partition_event
-- ----------------------------
DROP EVENT IF EXISTS `add_partition_event`;
delimiter ;;
CREATE EVENT `add_partition_event`
ON SCHEDULE
EVERY '1' YEAR STARTS '2027-01-01 02:00:00'
DO CALL add_next_partition()
;;
delimiter ;

-- ----------------------------
-- Event structure for daily_energy_summary_event
-- ----------------------------
DROP EVENT IF EXISTS `daily_energy_summary_event`;
delimiter ;;
CREATE EVENT `daily_energy_summary_event`
ON SCHEDULE
EVERY '1' DAY STARTS '2025-01-01 01:00:00'
DO BEGIN
    INSERT INTO daily_energy_summary (building_id, summary_date, total_power, total_water, avg_env_temp, max_power, record_count)
    SELECT 
        building_id,
        DATE(monitoring_time) AS summary_date,
        SUM(power_consumption) AS total_power,
        SUM(COALESCE(water_consumption, 0)) AS total_water,
        AVG(env_temp) AS avg_env_temp,
        MAX(power_consumption) AS max_power,
        COUNT(*) AS record_count
    FROM energy_readings
    WHERE monitoring_time >= CURDATE() - INTERVAL 1 DAY
      AND monitoring_time < CURDATE()
    GROUP BY building_id, DATE(monitoring_time)
    ON DUPLICATE KEY UPDATE
        total_power = VALUES(total_power),
        total_water = VALUES(total_water),
        avg_env_temp = VALUES(avg_env_temp),
        max_power = VALUES(max_power),
        record_count = VALUES(record_count),
        updated_at = NOW();
END
;;
delimiter ;

SET FOREIGN_KEY_CHECKS = 1;
