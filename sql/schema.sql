-- ============================================================
-- 知识产权代理事务所管理系统
-- schema.sql
-- PRD Version: V1.1
-- Target: MySQL 8.x / Aiven defaultdb
--
-- 说明：
-- 1. 本脚本不 CREATE DATABASE，直接在当前已连接数据库中建表。
-- 2. 删除采用逻辑删除：is_deleted = 0/1。
-- 3. 所有核心业务表统一包含 update_time 与 is_deleted。
-- 4. 枚举值按 PRD V1.1 使用英文常量，并通过 CHECK 约束限制。
-- ============================================================

SET NAMES utf8mb4;

-- ------------------------------------------------------------
-- 1. 系统用户表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(100) NOT NULL,
    real_name VARCHAR(50) NOT NULL,
    role VARCHAR(20) NOT NULL,
    phone VARCHAR(20) NULL,
    email VARCHAR(100) NULL,
    status TINYINT NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,

    CONSTRAINT uk_sys_user_username UNIQUE (username),
    CONSTRAINT chk_sys_user_role
        CHECK (role IN ('ADMIN', 'AGENT', 'ASSISTANT')),
    CONSTRAINT chk_sys_user_status
        CHECK (status IN (0, 1)),
    CONSTRAINT chk_sys_user_deleted
        CHECK (is_deleted IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- 2. 客户信息表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS client_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    client_name VARCHAR(100) NOT NULL,
    client_type VARCHAR(20) NOT NULL,
    contact_name VARCHAR(50) NULL,
    phone VARCHAR(20) NULL,
    email VARCHAR(100) NULL,
    address VARCHAR(255) NULL,
    remark VARCHAR(500) NULL,
    creator_id BIGINT NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,

    CONSTRAINT fk_client_creator
        FOREIGN KEY (creator_id) REFERENCES sys_user(id),
    CONSTRAINT chk_client_type
        CHECK (client_type IN ('COMPANY', 'INDIVIDUAL')),
    CONSTRAINT chk_client_deleted
        CHECK (is_deleted IN (0, 1)),

    INDEX idx_client_name (client_name),
    INDEX idx_client_creator (creator_id),
    INDEX idx_client_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- 3. 知识产权案件表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS case_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    case_no VARCHAR(50) NOT NULL,
    case_name VARCHAR(150) NOT NULL,
    client_id BIGINT NOT NULL,
    case_type VARCHAR(30) NOT NULL,
    business_type VARCHAR(50) NOT NULL,
    application_no VARCHAR(100) NULL,
    principal_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL,
    priority VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    start_date DATE NULL,
    close_date DATE NULL,
    description VARCHAR(1000) NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,

    CONSTRAINT uk_case_no UNIQUE (case_no),
    CONSTRAINT fk_case_client
        FOREIGN KEY (client_id) REFERENCES client_info(id),
    CONSTRAINT fk_case_principal
        FOREIGN KEY (principal_id) REFERENCES sys_user(id),

    CONSTRAINT chk_case_type
        CHECK (case_type IN ('PATENT', 'TRADEMARK', 'COPYRIGHT')),
    CONSTRAINT chk_case_status
        CHECK (status IN (
            'PENDING',
            'PROCESSING',
            'WAITING_CLIENT',
            'WAITING_OFFICIAL',
            'COMPLETED',
            'TERMINATED'
        )),
    CONSTRAINT chk_case_priority
        CHECK (priority IN ('LOW', 'NORMAL', 'HIGH')),
    CONSTRAINT chk_case_deleted
        CHECK (is_deleted IN (0, 1)),

    INDEX idx_case_client (client_id),
    INDEX idx_case_principal (principal_id),
    INDEX idx_case_status (status),
    INDEX idx_case_type (case_type),
    INDEX idx_case_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- 4. 案件成员表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS case_member (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    case_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    member_role VARCHAR(20) NOT NULL,
    join_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,

    CONSTRAINT uk_case_member_case_user UNIQUE (case_id, user_id),
    CONSTRAINT fk_case_member_case
        FOREIGN KEY (case_id) REFERENCES case_info(id),
    CONSTRAINT fk_case_member_user
        FOREIGN KEY (user_id) REFERENCES sys_user(id),
    CONSTRAINT chk_case_member_role
        CHECK (member_role IN ('PRINCIPAL', 'COLLABORATOR')),
    CONSTRAINT chk_case_member_deleted
        CHECK (is_deleted IN (0, 1)),

    INDEX idx_case_member_user (user_id),
    INDEX idx_case_member_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- 5. 案件任务表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS case_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    case_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    description VARCHAR(1000) NULL,
    assignee_id BIGINT NOT NULL,
    creator_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    priority VARCHAR(20) NOT NULL,
    due_date DATE NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,

    CONSTRAINT fk_task_case
        FOREIGN KEY (case_id) REFERENCES case_info(id),
    CONSTRAINT fk_task_assignee
        FOREIGN KEY (assignee_id) REFERENCES sys_user(id),
    CONSTRAINT fk_task_creator
        FOREIGN KEY (creator_id) REFERENCES sys_user(id),
    CONSTRAINT chk_task_status
        CHECK (status IN ('TODO', 'DOING', 'DONE')),
    CONSTRAINT chk_task_priority
        CHECK (priority IN ('LOW', 'NORMAL', 'HIGH')),
    CONSTRAINT chk_task_deleted
        CHECK (is_deleted IN (0, 1)),

    INDEX idx_task_case (case_id),
    INDEX idx_task_assignee (assignee_id),
    INDEX idx_task_status (status),
    INDEX idx_task_due_date (due_date),
    INDEX idx_task_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- 6. 案件进度记录表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS case_progress (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    case_id BIGINT NOT NULL,
    task_id BIGINT NULL,
    content VARCHAR(1000) NOT NULL,
    progress_percent INT NULL,
    operator_id BIGINT NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,

    CONSTRAINT fk_progress_case
        FOREIGN KEY (case_id) REFERENCES case_info(id),
    CONSTRAINT fk_progress_task
        FOREIGN KEY (task_id) REFERENCES case_task(id),
    CONSTRAINT fk_progress_operator
        FOREIGN KEY (operator_id) REFERENCES sys_user(id),
    CONSTRAINT chk_progress_percent
        CHECK (
            progress_percent IS NULL
            OR progress_percent BETWEEN 0 AND 100
        ),
    CONSTRAINT chk_progress_deleted
        CHECK (is_deleted IN (0, 1)),

    INDEX idx_progress_case (case_id),
    INDEX idx_progress_task (task_id),
    INDEX idx_progress_operator (operator_id),
    INDEX idx_progress_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- 7. 案件期限表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS case_deadline (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    case_id BIGINT NOT NULL,
    deadline_name VARCHAR(100) NOT NULL,
    deadline_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    responsible_id BIGINT NOT NULL,
    remark VARCHAR(500) NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,

    CONSTRAINT fk_deadline_case
        FOREIGN KEY (case_id) REFERENCES case_info(id),
    CONSTRAINT fk_deadline_responsible
        FOREIGN KEY (responsible_id) REFERENCES sys_user(id),
    CONSTRAINT chk_deadline_status
        CHECK (status IN ('PENDING', 'DONE')),
    CONSTRAINT chk_deadline_deleted
        CHECK (is_deleted IN (0, 1)),

    INDEX idx_deadline_case (case_id),
    INDEX idx_deadline_responsible (responsible_id),
    INDEX idx_deadline_date (deadline_date),
    INDEX idx_deadline_status (status),
    INDEX idx_deadline_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- 8. 案件文档表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS case_document (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    case_id BIGINT NOT NULL,
    document_name VARCHAR(150) NOT NULL,
    document_type VARCHAR(50) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    uploader_id BIGINT NOT NULL,
    remark VARCHAR(500) NULL,
    upload_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,

    CONSTRAINT fk_document_case
        FOREIGN KEY (case_id) REFERENCES case_info(id),
    CONSTRAINT fk_document_uploader
        FOREIGN KEY (uploader_id) REFERENCES sys_user(id),
    CONSTRAINT chk_document_type
        CHECK (document_type IN (
            'CLIENT',
            'APPLICATION',
            'OFFICIAL',
            'INTERNAL',
            'OTHER'
        )),
    CONSTRAINT chk_document_deleted
        CHECK (is_deleted IN (0, 1)),

    INDEX idx_document_case (case_id),
    INDEX idx_document_uploader (uploader_id),
    INDEX idx_document_type (document_type),
    INDEX idx_document_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- 9. 案件费用表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS case_fee (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    case_id BIGINT NOT NULL,
    fee_type VARCHAR(30) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    direction VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    pay_date DATE NULL,
    operator_id BIGINT NOT NULL,
    remark VARCHAR(500) NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,

    CONSTRAINT fk_fee_case
        FOREIGN KEY (case_id) REFERENCES case_info(id),
    CONSTRAINT fk_fee_operator
        FOREIGN KEY (operator_id) REFERENCES sys_user(id),
    CONSTRAINT chk_fee_type
        CHECK (fee_type IN ('AGENCY', 'OFFICIAL', 'OTHER')),
    CONSTRAINT chk_fee_direction
        CHECK (direction IN ('RECEIVABLE', 'EXPENSE')),
    CONSTRAINT chk_fee_status
        CHECK (status IN ('PENDING', 'PAID')),
    CONSTRAINT chk_fee_amount
        CHECK (amount > 0),
    CONSTRAINT chk_fee_deleted
        CHECK (is_deleted IN (0, 1)),

    INDEX idx_fee_case (case_id),
    INDEX idx_fee_operator (operator_id),
    INDEX idx_fee_type (fee_type),
    INDEX idx_fee_status (status),
    INDEX idx_fee_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
