-- =========================================================
-- 知识产权代理事务所管理系统
-- Database Schema V2.1
-- Target: MySQL 8.x / Aiven defaultdb
-- =========================================================

SET NAMES utf8mb4;


-- =========================================================
-- 1. 用户与客户
-- =========================================================

CREATE TABLE sys_user (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    username        VARCHAR(50) NOT NULL COMMENT '登录名',
    password_hash   VARCHAR(100) NOT NULL COMMENT 'BCrypt密码哈希',
    real_name       VARCHAR(50) NOT NULL COMMENT '真实姓名',
    role            VARCHAR(20) NOT NULL COMMENT 'CLIENT/AGENT/ADMIN',
    phone           VARCHAR(20) NULL COMMENT '联系电话',
    email           VARCHAR(100) NULL COMMENT '邮箱',
    status          TINYINT NOT NULL DEFAULT 1 COMMENT '0禁用 1正常',
    last_login_time DATETIME NULL COMMENT '最后登录时间',
    create_time     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted      TINYINT NOT NULL DEFAULT 0 COMMENT '0正常 1逻辑删除',

    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_user_username (username),
    KEY idx_sys_user_role_status (role, status),
    KEY idx_sys_user_phone (phone),
    KEY idx_sys_user_email (email),

    CONSTRAINT ck_sys_user_role
        CHECK (role IN ('CLIENT', 'AGENT', 'ADMIN')),
    CONSTRAINT ck_sys_user_status
        CHECK (status IN (0, 1)),
    CONSTRAINT ck_sys_user_deleted
        CHECK (is_deleted IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户';

CREATE TABLE client_profile (
    id                    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '客户ID',
    user_id               BIGINT UNSIGNED NOT NULL COMMENT '关联CLIENT用户',
    client_type           VARCHAR(30) NOT NULL COMMENT 'INDIVIDUAL/COMPANY/UNIVERSITY/RESEARCH_INSTITUTE',
    client_name           VARCHAR(200) NOT NULL COMMENT '客户名称',
    credit_or_id_no       VARCHAR(100) NULL COMMENT '统一社会信用代码/身份证号',
    registered_address    VARCHAR(500) NULL COMMENT '注册地址',
    contact_address       VARCHAR(500) NULL COMMENT '通讯地址',
    primary_contact_name  VARCHAR(50) NULL COMMENT '主联系人',
    primary_contact_phone VARCHAR(20) NULL COMMENT '主联系人电话',
    primary_contact_email VARCHAR(100) NULL COMMENT '主联系人邮箱',
    industry              VARCHAR(100) NULL COMMENT '所属行业',
    technical_preference  VARCHAR(500) NULL COMMENT '技术领域偏好',
    invoice_title         VARCHAR(200) NULL COMMENT '发票抬头',
    taxpayer_no           VARCHAR(100) NULL COMMENT '纳税人识别号',
    bank_name             VARCHAR(200) NULL COMMENT '开户行',
    bank_account          VARCHAR(100) NULL COMMENT '银行账号',
    create_time           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted            TINYINT NOT NULL DEFAULT 0,

    PRIMARY KEY (id),
    UNIQUE KEY uk_client_profile_user (user_id),
    UNIQUE KEY uk_client_profile_credit (credit_or_id_no),
    KEY idx_client_profile_type (client_type),
    KEY idx_client_profile_industry (industry),
    KEY idx_client_profile_name (client_name),

    CONSTRAINT fk_client_profile_user
        FOREIGN KEY (user_id) REFERENCES sys_user(id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT ck_client_profile_type
        CHECK (client_type IN ('INDIVIDUAL', 'COMPANY', 'UNIVERSITY', 'RESEARCH_INSTITUTE')),
    CONSTRAINT ck_client_profile_deleted
        CHECK (is_deleted IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客户主体资料';

CREATE TABLE agent_profile (
    id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '代理人ID',
    user_id             BIGINT UNSIGNED NOT NULL COMMENT '关联AGENT用户',
    employee_no         VARCHAR(50) NOT NULL COMMENT '工号',
    license_no          VARCHAR(100) NULL COMMENT '执业证号/备案号',
    department          VARCHAR(30) NULL COMMENT 'PATENT/TRADEMARK/COPYRIGHT/LITIGATION/PROCESS',
    professional_field  VARCHAR(200) NULL COMMENT '专业领域',
    practice_years      INT NOT NULL DEFAULT 0 COMMENT '执业年限',
    education           VARCHAR(200) NULL COMMENT '学历背景',
    ipc_scope           VARCHAR(500) NULL COMMENT '擅长IPC范围',
    profile             TEXT NULL COMMENT '个人简介',
    signature_path      VARCHAR(500) NULL COMMENT '电子签名文件路径',
    create_time         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted          TINYINT NOT NULL DEFAULT 0,

    PRIMARY KEY (id),
    UNIQUE KEY uk_agent_profile_user (user_id),
    UNIQUE KEY uk_agent_profile_employee (employee_no),
    KEY idx_agent_profile_department (department),
    KEY idx_agent_profile_field (professional_field),

    CONSTRAINT fk_agent_profile_user
        FOREIGN KEY (user_id) REFERENCES sys_user(id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT ck_agent_profile_department
        CHECK (
            department IS NULL OR
            department IN ('PATENT', 'TRADEMARK', 'COPYRIGHT', 'LITIGATION', 'PROCESS')
        ),
    CONSTRAINT ck_agent_profile_years
        CHECK (practice_years >= 0),
    CONSTRAINT ck_agent_profile_deleted
        CHECK (is_deleted IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='代理人职业资料';

CREATE TABLE client_contact (
    id               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '联系人ID',
    client_id        BIGINT UNSIGNED NOT NULL COMMENT '客户ID',
    name             VARCHAR(50) NOT NULL COMMENT '联系人姓名',
    position         VARCHAR(100) NULL COMMENT '职位/职责',
    phone            VARCHAR(20) NULL COMMENT '联系电话',
    email            VARCHAR(100) NULL COMMENT '邮箱',
    permission_scope VARCHAR(30) NOT NULL DEFAULT 'ALL_CASES'
                     COMMENT 'ALL_CASES/SPECIFIED_CASES/FEE_ONLY',
    remark           VARCHAR(500) NULL COMMENT '备注',
    create_time      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted       TINYINT NOT NULL DEFAULT 0,

    PRIMARY KEY (id),
    KEY idx_client_contact_client (client_id),
    KEY idx_client_contact_email (email),

    CONSTRAINT fk_client_contact_client
        FOREIGN KEY (client_id) REFERENCES client_profile(id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT ck_client_contact_scope
        CHECK (permission_scope IN ('ALL_CASES', 'SPECIFIED_CASES', 'FEE_ONLY')),
    CONSTRAINT ck_client_contact_deleted
        CHECK (is_deleted IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客户联系人';

-- =========================================================
-- 2. 游客公开数据
-- =========================================================

CREATE TABLE service_product (
    id                 BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    service_no         VARCHAR(50) NOT NULL COMMENT '服务编号',
    service_name       VARCHAR(200) NOT NULL COMMENT '服务名称',
    service_type       VARCHAR(40) NOT NULL COMMENT '服务类型',
    target_type        VARCHAR(200) NULL COMMENT '适用对象，可展示多个类型',
    description        TEXT NULL COMMENT '服务内容说明',
    process_desc       TEXT NULL COMMENT '服务流程',
    official_fee       DECIMAL(12,2) NULL COMMENT '官方费用',
    agency_fee         DECIMAL(12,2) NULL COMMENT '代理服务费',
    estimated_cycle    VARCHAR(100) NULL COMMENT '预计办理周期',
    required_materials TEXT NULL COMMENT '所需材料清单',
    advantages         TEXT NULL COMMENT '服务优势',
    status             TINYINT NOT NULL DEFAULT 1 COMMENT '0下架 1上架',
    create_time        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted         TINYINT NOT NULL DEFAULT 0,

    PRIMARY KEY (id),
    UNIQUE KEY uk_service_product_no (service_no),
    KEY idx_service_product_type_status (service_type, status),

    CONSTRAINT ck_service_product_type
        CHECK (service_type IN (
            'PATENT_APPLICATION',
            'TRADEMARK_REGISTRATION',
            'COPYRIGHT_REGISTRATION',
            'IP_STANDARD',
            'IP_PROTECTION',
            'PATENT_ANALYSIS'
        )),
    CONSTRAINT ck_service_product_status
        CHECK (status IN (0, 1)),
    CONSTRAINT ck_service_product_fee
        CHECK (
            (official_fee IS NULL OR official_fee >= 0) AND
            (agency_fee IS NULL OR agency_fee >= 0)
        ),
    CONSTRAINT ck_service_product_deleted
        CHECK (is_deleted IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务产品';

CREATE TABLE success_case (
    id               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    case_no          VARCHAR(50) NOT NULL COMMENT '公开案例编号',
    case_name        VARCHAR(200) NOT NULL COMMENT '案例名称',
    service_type     VARCHAR(40) NOT NULL COMMENT '服务类型',
    client_industry  VARCHAR(100) NULL COMMENT '客户行业',
    technical_field  VARCHAR(200) NULL COMMENT '技术领域/IPC/尼斯分类',
    highlights       TEXT NULL COMMENT '案件亮点',
    result           TEXT NULL COMMENT '处理结果',
    grant_date       DATE NULL COMMENT '授权/注册日期',
    description      TEXT NULL COMMENT '案例简介',
    publish_status   TINYINT NOT NULL DEFAULT 1 COMMENT '0隐藏 1公开',
    create_time      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted       TINYINT NOT NULL DEFAULT 0,

    PRIMARY KEY (id),
    UNIQUE KEY uk_success_case_no (case_no),
    KEY idx_success_case_type (service_type),
    KEY idx_success_case_industry (client_industry),
    KEY idx_success_case_field (technical_field),
    KEY idx_success_case_publish (publish_status),

    CONSTRAINT ck_success_case_publish
        CHECK (publish_status IN (0, 1)),
    CONSTRAINT ck_success_case_deleted
        CHECK (is_deleted IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公开成功案例';

CREATE TABLE announcement (
    id                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    announcement_no   VARCHAR(50) NOT NULL COMMENT '公告编号',
    title             VARCHAR(200) NOT NULL COMMENT '公告标题',
    announcement_type VARCHAR(30) NOT NULL COMMENT '公告类型',
    target_scope      VARCHAR(50) NOT NULL DEFAULT 'ALL' COMMENT '影响范围',
    content           LONGTEXT NOT NULL COMMENT '公告内容',
    publish_time      DATETIME NULL COMMENT '发布时间',
    start_date        DATE NULL COMMENT '公示开始日期',
    end_date          DATE NULL COMMENT '公示截止日期',
    is_top            TINYINT NOT NULL DEFAULT 0 COMMENT '0否 1置顶',
    status            TINYINT NOT NULL DEFAULT 1 COMMENT '0草稿/失效 1有效',
    publisher_user_id BIGINT UNSIGNED NULL COMMENT '发布管理员',
    create_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted        TINYINT NOT NULL DEFAULT 0,

    PRIMARY KEY (id),
    UNIQUE KEY uk_announcement_no (announcement_no),
    KEY idx_announcement_type_time (announcement_type, publish_time),
    KEY idx_announcement_status_top (status, is_top, publish_time),

    CONSTRAINT fk_announcement_publisher
        FOREIGN KEY (publisher_user_id) REFERENCES sys_user(id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT ck_announcement_type
        CHECK (announcement_type IN (
            'POLICY',
            'BUSINESS',
            'PROMOTION',
            'RECRUITMENT',
            'SYSTEM',
            'TRAINING'
        )),
    CONSTRAINT ck_announcement_top
        CHECK (is_top IN (0, 1)),
    CONSTRAINT ck_announcement_status
        CHECK (status IN (0, 1)),
    CONSTRAINT ck_announcement_deleted
        CHECK (is_deleted IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='事务所公告';

-- =========================================================
-- 3. 案件核心
-- =========================================================

CREATE TABLE case_info (
    id                          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '案件ID',
    case_no                     VARCHAR(60) NULL COMMENT '正式案件编号，审核立案后生成',
    case_name                   VARCHAR(300) NOT NULL COMMENT '案件名称',
    client_id                   BIGINT UNSIGNED NOT NULL COMMENT '客户ID',
    service_product_id          BIGINT UNSIGNED NULL COMMENT '所选服务产品',
    case_type                   VARCHAR(40) NOT NULL COMMENT '案件类型',
    technical_field             VARCHAR(300) NULL COMMENT '技术领域/IPC/尼斯分类',
    application_no              VARCHAR(100) NULL COMMENT '申请/注册号',
    principal_agent_id          BIGINT UNSIGNED NULL COMMENT '当前主办代理人',
    status                      VARCHAR(40) NOT NULL DEFAULT 'SUBMITTED' COMMENT '案件状态',
    current_stage               VARCHAR(100) NULL COMMENT '当前阶段',
    priority_level              VARCHAR(20) NOT NULL DEFAULT 'MEDIUM' COMMENT 'URGENT/HIGH/MEDIUM/LOW',
    submit_time                 DATETIME NULL COMMENT '委托提交时间',
    accept_time                 DATETIME NULL COMMENT '正式受理/立案时间',
    expected_next_official_date DATE NULL COMMENT '预计下次官文日期',
    confidential_review         TINYINT NOT NULL DEFAULT 0 COMMENT '是否请求保密审查',
    description                 TEXT NULL COMMENT '案件说明',
    create_time                 DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time                 DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted                  TINYINT NOT NULL DEFAULT 0,

    PRIMARY KEY (id),
    UNIQUE KEY uk_case_info_case_no (case_no),
    UNIQUE KEY uk_case_info_application_no (application_no),

    KEY idx_case_info_client_status (client_id, status),
    KEY idx_case_info_agent_status (principal_agent_id, status),
    KEY idx_case_info_type_status (case_type, status),
    KEY idx_case_info_submit_time (submit_time),
    KEY idx_case_info_next_official (expected_next_official_date),
    KEY idx_case_info_stage (current_stage),

    CONSTRAINT fk_case_info_client
        FOREIGN KEY (client_id) REFERENCES client_profile(id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_case_info_service
        FOREIGN KEY (service_product_id) REFERENCES service_product(id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_case_info_principal_agent
        FOREIGN KEY (principal_agent_id) REFERENCES agent_profile(id)
        ON UPDATE CASCADE ON DELETE RESTRICT,

    CONSTRAINT ck_case_info_type
        CHECK (case_type IN (
            'INVENTION_PATENT',
            'UTILITY_MODEL',
            'DESIGN_PATENT',
            'TRADEMARK',
            'COPYRIGHT',
            'INVALIDATION',
            'INFRINGEMENT_LITIGATION',
            'OTHER'
        )),
    CONSTRAINT ck_case_info_status
        CHECK (status IN (
            'SUBMITTED',
            'PENDING_REVIEW',
            'RETURNED',
            'PENDING_ASSIGNMENT',
            'PROCESSING',
            'FORMAL_EXAM',
            'SUBSTANTIVE_EXAM',
            'PRELIMINARY_PASSED',
            'GRANTED',
            'REJECTED',
            'REEXAMINATION',
            'WITHDRAWN',
            'EXPIRED',
            'CLOSED'
        )),
    CONSTRAINT ck_case_info_priority
        CHECK (priority_level IN ('URGENT', 'HIGH', 'MEDIUM', 'LOW')),
    CONSTRAINT ck_case_info_confidential
        CHECK (confidential_review IN (0, 1)),
    CONSTRAINT ck_case_info_deleted
        CHECK (is_deleted IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='案件主表';

CREATE TABLE case_party (
    id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    case_id       BIGINT UNSIGNED NOT NULL COMMENT '案件ID',
    party_type    VARCHAR(30) NOT NULL COMMENT 'APPLICANT/INVENTOR/DESIGNER/RIGHT_HOLDER',
    name          VARCHAR(200) NOT NULL COMMENT '姓名/主体名称',
    id_no         VARCHAR(100) NULL COMMENT '身份证号/证件号',
    nationality   VARCHAR(100) NULL COMMENT '国籍',
    address       VARCHAR(500) NULL COMMENT '地址',
    is_primary    TINYINT NOT NULL DEFAULT 0 COMMENT '是否第一申请人/第一发明人等',
    remark        VARCHAR(500) NULL,
    create_time   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted    TINYINT NOT NULL DEFAULT 0,

    PRIMARY KEY (id),
    KEY idx_case_party_case_type (case_id, party_type),
    KEY idx_case_party_name (name),

    CONSTRAINT fk_case_party_case
        FOREIGN KEY (case_id) REFERENCES case_info(id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT ck_case_party_type
        CHECK (party_type IN ('APPLICANT', 'INVENTOR', 'DESIGNER', 'RIGHT_HOLDER')),
    CONSTRAINT ck_case_party_primary
        CHECK (is_primary IN (0, 1)),
    CONSTRAINT ck_case_party_deleted
        CHECK (is_deleted IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='案件主体：申请人/发明人/设计人等';

CREATE TABLE case_priority (
    id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    case_id       BIGINT UNSIGNED NOT NULL,
    country       VARCHAR(100) NOT NULL COMMENT '优先权国家/地区',
    priority_no   VARCHAR(100) NOT NULL COMMENT '优先权号',
    priority_date DATE NOT NULL COMMENT '优先权日',
    create_time   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted    TINYINT NOT NULL DEFAULT 0,

    PRIMARY KEY (id),
    UNIQUE KEY uk_case_priority_no (case_id, priority_no),
    KEY idx_case_priority_date (priority_date),

    CONSTRAINT fk_case_priority_case
        FOREIGN KEY (case_id) REFERENCES case_info(id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT ck_case_priority_deleted
        CHECK (is_deleted IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='案件优先权';

CREATE TABLE case_assignment (
    id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    case_id             BIGINT UNSIGNED NOT NULL COMMENT '案件ID',
    agent_id            BIGINT UNSIGNED NOT NULL COMMENT '代理人ID',
    assigned_by_user_id BIGINT UNSIGNED NOT NULL COMMENT '分配人用户ID',
    assignment_role     VARCHAR(30) NOT NULL DEFAULT 'PRINCIPAL' COMMENT 'PRINCIPAL/COLLABORATOR',
    assign_time         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '分配时间',
    end_time            DATETIME NULL COMMENT '结束时间',
    reason              VARCHAR(500) NULL COMMENT '分配/调整原因',
    is_current          TINYINT NOT NULL DEFAULT 1 COMMENT '是否当前有效分配',
    create_time         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted          TINYINT NOT NULL DEFAULT 0,

    PRIMARY KEY (id),
    KEY idx_case_assignment_case_current (case_id, is_current),
    KEY idx_case_assignment_agent_current (agent_id, is_current),
    KEY idx_case_assignment_time (assign_time),

    CONSTRAINT fk_case_assignment_case
        FOREIGN KEY (case_id) REFERENCES case_info(id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_case_assignment_agent
        FOREIGN KEY (agent_id) REFERENCES agent_profile(id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_case_assignment_assigner
        FOREIGN KEY (assigned_by_user_id) REFERENCES sys_user(id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT ck_case_assignment_role
        CHECK (assignment_role IN ('PRINCIPAL', 'COLLABORATOR')),
    CONSTRAINT ck_case_assignment_current
        CHECK (is_current IN (0, 1)),
    CONSTRAINT ck_case_assignment_deleted
        CHECK (is_deleted IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='案件代理人分配历史';

CREATE TABLE case_stage (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    case_id         BIGINT UNSIGNED NOT NULL,
    stage_type      VARCHAR(40) NOT NULL COMMENT '阶段类型',
    stage_name      VARCHAR(100) NOT NULL COMMENT '阶段名称',
    start_time      DATETIME NOT NULL COMMENT '阶段开始时间',
    end_time        DATETIME NULL COMMENT '阶段结束时间',
    handler_user_id BIGINT UNSIGNED NULL COMMENT '处理人用户ID',
    status          VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS' COMMENT 'NOT_STARTED/IN_PROGRESS/COMPLETED',
    description     TEXT NULL COMMENT '阶段说明',
    create_time     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted      TINYINT NOT NULL DEFAULT 0,

    PRIMARY KEY (id),
    KEY idx_case_stage_case_time (case_id, start_time),
    KEY idx_case_stage_type (stage_type),
    KEY idx_case_stage_handler (handler_user_id),

    CONSTRAINT fk_case_stage_case
        FOREIGN KEY (case_id) REFERENCES case_info(id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_case_stage_handler
        FOREIGN KEY (handler_user_id) REFERENCES sys_user(id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT ck_case_stage_type
        CHECK (stage_type IN (
            'SUBMISSION',
            'ACCEPTANCE',
            'PRELIMINARY_EXAM',
            'PUBLICATION',
            'SUBSTANTIVE_EXAM',
            'OFFICE_ACTION',
            'CLIENT_RESPONSE',
            'GRANT',
            'CERTIFICATE',
            'OTHER'
        )),
    CONSTRAINT ck_case_stage_status
        CHECK (status IN ('NOT_STARTED', 'IN_PROGRESS', 'COMPLETED')),
    CONSTRAINT ck_case_stage_deleted
        CHECK (is_deleted IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='案件阶段时间轴';

-- =========================================================
-- 4. 文件、官文、时限、审核
-- =========================================================

CREATE TABLE case_document (
    id                    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    case_id               BIGINT UNSIGNED NOT NULL COMMENT '案件ID',
    stage_id              BIGINT UNSIGNED NULL COMMENT '关联案件阶段',
    document_no           VARCHAR(100) NULL COMMENT '文档/官文编号',
    document_name         VARCHAR(300) NOT NULL COMMENT '文件显示名',
    document_type         VARCHAR(40) NOT NULL COMMENT '文件类型',
    source_type           VARCHAR(30) NOT NULL DEFAULT 'INTERNAL'
                          COMMENT 'CLIENT/AGENT/ADMIN/OFFICIAL/EXTERNAL_SYSTEM',
    file_path             VARCHAR(1000) NOT NULL COMMENT '文件实际存储路径/对象存储Key',
    original_file_name    VARCHAR(300) NULL COMMENT '原始文件名',
    file_size             BIGINT UNSIGNED NULL COMMENT '文件大小，字节',
    mime_type             VARCHAR(100) NULL COMMENT 'MIME类型',
    uploader_user_id      BIGINT UNSIGNED NULL COMMENT '上传用户',
    review_status         VARCHAR(30) NOT NULL DEFAULT 'NOT_REQUIRED' COMMENT '审核状态',
    official_issue_date   DATE NULL COMMENT '官文发文日',
    receive_time          DATETIME NULL COMMENT '官文接收时间',
    official_deadline     DATE NULL COMMENT '官文对应期限',
    fee_amount_extracted  DECIMAL(12,2) NULL COMMENT 'OCR/人工提取费用金额',
    ocr_status            VARCHAR(20) NOT NULL DEFAULT 'NOT_STARTED'
                          COMMENT 'NOT_STARTED/PROCESSING/SUCCESS/FAILED/MANUAL',
    ocr_text              LONGTEXT NULL COMMENT 'OCR原始文本',
    ocr_extracted_json    JSON NULL COMMENT 'OCR结构化提取结果',
    external_system_code  VARCHAR(50) NULL COMMENT '来源外部系统代码',
    external_document_id  VARCHAR(200) NULL COMMENT '外部系统文档ID',
    remark                VARCHAR(1000) NULL,
    create_time           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted            TINYINT NOT NULL DEFAULT 0,

    PRIMARY KEY (id),
    KEY idx_case_document_case_type_time (case_id, document_type, create_time),
    KEY idx_case_document_stage (stage_id),
    KEY idx_case_document_review (review_status),
    KEY idx_case_document_deadline (official_deadline),
    KEY idx_case_document_external (external_system_code, external_document_id),

    CONSTRAINT fk_case_document_case
        FOREIGN KEY (case_id) REFERENCES case_info(id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_case_document_stage
        FOREIGN KEY (stage_id) REFERENCES case_stage(id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_case_document_uploader
        FOREIGN KEY (uploader_user_id) REFERENCES sys_user(id)
        ON UPDATE CASCADE ON DELETE RESTRICT,

    CONSTRAINT ck_case_document_type
        CHECK (document_type IN (
            'TECHNICAL_DISCLOSURE',
            'TRADEMARK_IMAGE',
            'APPLICATION',
            'OFFICE_ACTION_RESPONSE',
            'OFFICIAL',
            'SUPPLEMENT',
            'INTERNAL',
            'CERTIFICATE',
            'OTHER'
        )),
    CONSTRAINT ck_case_document_source
        CHECK (source_type IN ('CLIENT', 'AGENT', 'ADMIN', 'OFFICIAL', 'EXTERNAL_SYSTEM')),
    CONSTRAINT ck_case_document_review
        CHECK (review_status IN (
            'NOT_REQUIRED',
            'PENDING',
            'APPROVED',
            'MINOR_REVISION',
            'MAJOR_REVISION',
            'REJECTED',
            'RESUBMIT_REQUIRED'
        )),
    CONSTRAINT ck_case_document_ocr
        CHECK (ocr_status IN ('NOT_STARTED', 'PROCESSING', 'SUCCESS', 'FAILED', 'MANUAL')),
    CONSTRAINT ck_case_document_file_size
        CHECK (file_size IS NULL OR file_size >= 0),
    CONSTRAINT ck_case_document_fee
        CHECK (fee_amount_extracted IS NULL OR fee_amount_extracted >= 0),
    CONSTRAINT ck_case_document_deleted
        CHECK (is_deleted IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='案件业务文件、官文和补充材料';

CREATE TABLE deadline_task (
    id                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    case_id           BIGINT UNSIGNED NOT NULL,
    agent_id          BIGINT UNSIGNED NULL COMMENT '负责代理人',
    document_id       BIGINT UNSIGNED NULL COMMENT '由某官文触发时关联文档',
    deadline_type     VARCHAR(40) NOT NULL COMMENT '时限类型',
    task_name         VARCHAR(200) NOT NULL,
    official_deadline DATETIME NOT NULL COMMENT '官方期限',
    internal_deadline DATETIME NULL COMMENT '内部提前期限',
    status            VARCHAR(20) NOT NULL DEFAULT 'NOT_STARTED',
    priority          VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    remind_type       VARCHAR(50) NULL COMMENT 'SYSTEM/EMAIL/SMS/APP等',
    completed_time    DATETIME NULL,
    description       VARCHAR(1000) NULL,
    create_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted        TINYINT NOT NULL DEFAULT 0,

    PRIMARY KEY (id),
    KEY idx_deadline_case_status (case_id, status),
    KEY idx_deadline_agent_status_date (agent_id, status, official_deadline),
    KEY idx_deadline_official_date (official_deadline),
    KEY idx_deadline_priority_status (priority, status),

    CONSTRAINT fk_deadline_case
        FOREIGN KEY (case_id) REFERENCES case_info(id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_deadline_agent
        FOREIGN KEY (agent_id) REFERENCES agent_profile(id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_deadline_document
        FOREIGN KEY (document_id) REFERENCES case_document(id)
        ON UPDATE CASCADE ON DELETE RESTRICT,

    CONSTRAINT ck_deadline_status
        CHECK (status IN ('NOT_STARTED', 'IN_PROGRESS', 'COMPLETED', 'OVERDUE')),
    CONSTRAINT ck_deadline_priority
        CHECK (priority IN ('URGENT', 'HIGH', 'MEDIUM', 'LOW')),
    CONSTRAINT ck_deadline_deleted
        CHECK (is_deleted IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='案件法定/内部时限任务';

CREATE TABLE review_record (
    id               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    case_id          BIGINT UNSIGNED NOT NULL,
    target_type      VARCHAR(30) NOT NULL COMMENT 'CASE/DOCUMENT/SUPPLEMENT/OTHER',
    target_id        BIGINT UNSIGNED NOT NULL COMMENT '被审核对象ID',
    reviewer_user_id BIGINT UNSIGNED NOT NULL COMMENT '审核人',
    review_type      VARCHAR(30) NOT NULL COMMENT 'CASE_ACCEPTANCE/INTERNAL_QUALITY/MATERIAL',
    review_result    VARCHAR(30) NOT NULL,
    review_comment   TEXT NULL,
    review_time      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_time      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted       TINYINT NOT NULL DEFAULT 0,

    PRIMARY KEY (id),
    KEY idx_review_case_time (case_id, review_time),
    KEY idx_review_target (target_type, target_id),
    KEY idx_review_reviewer_time (reviewer_user_id, review_time),

    CONSTRAINT fk_review_case
        FOREIGN KEY (case_id) REFERENCES case_info(id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_review_reviewer
        FOREIGN KEY (reviewer_user_id) REFERENCES sys_user(id)
        ON UPDATE CASCADE ON DELETE RESTRICT,

    CONSTRAINT ck_review_target_type
        CHECK (target_type IN ('CASE', 'DOCUMENT', 'SUPPLEMENT', 'OTHER')),
    CONSTRAINT ck_review_result
        CHECK (review_result IN (
            'APPROVED',
            'MINOR_REVISION',
            'MAJOR_REVISION',
            'REJECTED',
            'RETURNED',
            'RESUBMIT_REQUIRED'
        )),
    CONSTRAINT ck_review_deleted
        CHECK (is_deleted IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='统一审核记录';

-- =========================================================
-- 5. 财务
-- =========================================================

CREATE TABLE fee_bill (
    id               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    bill_no          VARCHAR(60) NOT NULL COMMENT '账单编号',
    case_id          BIGINT UNSIGNED NOT NULL,
    client_id        BIGINT UNSIGNED NOT NULL,
    fee_type         VARCHAR(20) NOT NULL COMMENT 'OFFICIAL/AGENCY/EXPEDITE/OTHER',
    fee_item         VARCHAR(200) NOT NULL COMMENT '费用项目',
    amount           DECIMAL(14,2) NOT NULL COMMENT '原始金额',
    discount_amount  DECIMAL(14,2) NOT NULL DEFAULT 0 COMMENT '优惠/减免金额',
    payable_amount   DECIMAL(14,2) NOT NULL COMMENT '应付金额',
    status           VARCHAR(30) NOT NULL DEFAULT 'PENDING_CONFIRM',
    due_date         DATE NULL COMMENT '缴费截止日期',
    remark           VARCHAR(1000) NULL,
    created_by_user_id BIGINT UNSIGNED NULL COMMENT '创建人',
    create_time      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted       TINYINT NOT NULL DEFAULT 0,

    PRIMARY KEY (id),
    UNIQUE KEY uk_fee_bill_no (bill_no),
    KEY idx_fee_bill_client_status (client_id, status),
    KEY idx_fee_bill_case (case_id),
    KEY idx_fee_bill_status_due (status, due_date),
    KEY idx_fee_bill_create_time (create_time),

    CONSTRAINT fk_fee_bill_case
        FOREIGN KEY (case_id) REFERENCES case_info(id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_fee_bill_client
        FOREIGN KEY (client_id) REFERENCES client_profile(id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_fee_bill_creator
        FOREIGN KEY (created_by_user_id) REFERENCES sys_user(id)
        ON UPDATE CASCADE ON DELETE RESTRICT,

    CONSTRAINT ck_fee_bill_type
        CHECK (fee_type IN ('OFFICIAL', 'AGENCY', 'EXPEDITE', 'OTHER')),
    CONSTRAINT ck_fee_bill_status
        CHECK (status IN (
            'PENDING_CONFIRM',
            'PENDING_PAYMENT',
            'PAID',
            'INVOICED',
            'REFUNDED',
            'CANCELLED'
        )),
    CONSTRAINT ck_fee_bill_amount
        CHECK (
            amount >= 0 AND
            discount_amount >= 0 AND
            payable_amount >= 0 AND
            payable_amount <= amount
        ),
    CONSTRAINT ck_fee_bill_deleted
        CHECK (is_deleted IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='费用账单';

CREATE TABLE payment_record (
    id                      BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    bill_id                 BIGINT UNSIGNED NOT NULL,
    payment_no              VARCHAR(80) NOT NULL COMMENT '系统支付流水号',
    payment_method          VARCHAR(20) NOT NULL COMMENT 'BANK/ALIPAY/WECHAT/SIMULATED',
    amount                  DECIMAL(14,2) NOT NULL,
    payment_time            DATETIME NULL,
    status                  VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    external_transaction_no VARCHAR(200) NULL COMMENT '第三方交易号',
    remark                  VARCHAR(500) NULL,
    create_time             DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time             DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted              TINYINT NOT NULL DEFAULT 0,

    PRIMARY KEY (id),
    UNIQUE KEY uk_payment_record_no (payment_no),
    KEY idx_payment_bill_status (bill_id, status),
    KEY idx_payment_time (payment_time),

    CONSTRAINT fk_payment_bill
        FOREIGN KEY (bill_id) REFERENCES fee_bill(id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT ck_payment_method
        CHECK (payment_method IN ('BANK', 'ALIPAY', 'WECHAT', 'SIMULATED')),
    CONSTRAINT ck_payment_status
        CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED')),
    CONSTRAINT ck_payment_amount
        CHECK (amount > 0),
    CONSTRAINT ck_payment_deleted
        CHECK (is_deleted IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付记录';

CREATE TABLE invoice_record (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    bill_id         BIGINT UNSIGNED NOT NULL,
    invoice_no      VARCHAR(100) NULL COMMENT '发票号',
    invoice_type    VARCHAR(30) NOT NULL COMMENT 'NORMAL/SPECIAL',
    invoice_title   VARCHAR(200) NOT NULL,
    taxpayer_no     VARCHAR(100) NULL,
    amount          DECIMAL(14,2) NOT NULL,
    issue_time      DATETIME NULL,
    file_path       VARCHAR(1000) NULL COMMENT '模拟/电子发票文件路径',
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    create_time     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted      TINYINT NOT NULL DEFAULT 0,

    PRIMARY KEY (id),
    UNIQUE KEY uk_invoice_no (invoice_no),
    KEY idx_invoice_bill (bill_id),
    KEY idx_invoice_status_time (status, issue_time),

    CONSTRAINT fk_invoice_bill
        FOREIGN KEY (bill_id) REFERENCES fee_bill(id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT ck_invoice_type
        CHECK (invoice_type IN ('NORMAL', 'SPECIAL')),
    CONSTRAINT ck_invoice_status
        CHECK (status IN ('PENDING', 'ISSUED', 'VOID')),
    CONSTRAINT ck_invoice_amount
        CHECK (amount >= 0),
    CONSTRAINT ck_invoice_deleted
        CHECK (is_deleted IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='发票记录';

-- =========================================================
-- 6. 通知与移动端
-- =========================================================

CREATE TABLE notification (
    id                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id           BIGINT UNSIGNED NOT NULL COMMENT '接收用户',
    notification_type VARCHAR(30) NOT NULL COMMENT '通知类型',
    title             VARCHAR(200) NOT NULL,
    content           TEXT NOT NULL,
    business_type     VARCHAR(40) NULL COMMENT 'CASE/DOCUMENT/DEADLINE/BILL/SYSTEM等',
    business_id       BIGINT UNSIGNED NULL COMMENT '关联业务ID',
    is_read           TINYINT NOT NULL DEFAULT 0,
    read_time         DATETIME NULL,
    send_channel      VARCHAR(30) NOT NULL DEFAULT 'SYSTEM' COMMENT 'SYSTEM/WEB_PUSH/EMAIL/SMS/APP_PUSH',
    send_status       VARCHAR(20) NOT NULL DEFAULT 'SUCCESS' COMMENT 'PENDING/SUCCESS/FAILED',
    create_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted        TINYINT NOT NULL DEFAULT 0,

    PRIMARY KEY (id),
    KEY idx_notification_user_read_time (user_id, is_read, create_time),
    KEY idx_notification_business (business_type, business_id),
    KEY idx_notification_send_status (send_status),

    CONSTRAINT fk_notification_user
        FOREIGN KEY (user_id) REFERENCES sys_user(id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT ck_notification_type
        CHECK (notification_type IN (
            'DEADLINE',
            'OFFICIAL_DOCUMENT',
            'CASE_STATUS',
            'MATERIAL_REVIEW',
            'PAYMENT',
            'SYSTEM'
        )),
    CONSTRAINT ck_notification_read
        CHECK (is_read IN (0, 1)),
    CONSTRAINT ck_notification_channel
        CHECK (send_channel IN ('SYSTEM', 'WEB_PUSH', 'EMAIL', 'SMS', 'APP_PUSH')),
    CONSTRAINT ck_notification_send_status
        CHECK (send_status IN ('PENDING', 'SUCCESS', 'FAILED')),
    CONSTRAINT ck_notification_deleted
        CHECK (is_deleted IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='站内/移动端通知';

CREATE TABLE user_device (
    id               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id          BIGINT UNSIGNED NOT NULL,
    device_type      VARCHAR(20) NOT NULL COMMENT 'WEB/ANDROID/IOS',
    device_token     VARCHAR(1000) NULL COMMENT 'Push token / Web Push endpoint标识',
    push_platform    VARCHAR(30) NULL COMMENT 'WEB_PUSH/FCM/APNS/HUAWEI等',
    device_name      VARCHAR(200) NULL,
    last_active_time DATETIME NULL,
    status           TINYINT NOT NULL DEFAULT 1 COMMENT '0失效 1有效',
    create_time      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted       TINYINT NOT NULL DEFAULT 0,

    PRIMARY KEY (id),
    KEY idx_user_device_user_status (user_id, status),
    KEY idx_user_device_last_active (last_active_time),
    UNIQUE KEY uk_user_device_token (device_token(255)),

    CONSTRAINT fk_user_device_user
        FOREIGN KEY (user_id) REFERENCES sys_user(id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT ck_user_device_type
        CHECK (device_type IN ('WEB', 'ANDROID', 'IOS')),
    CONSTRAINT ck_user_device_status
        CHECK (status IN (0, 1)),
    CONSTRAINT ck_user_device_deleted
        CHECK (is_deleted IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户移动端/推送设备';

-- =========================================================
-- 7. 外部系统接入
-- =========================================================

CREATE TABLE external_system (
    id             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    system_code    VARCHAR(50) NOT NULL COMMENT 'CPC/TRADEMARK/PATENT_PAYMENT',
    system_name    VARCHAR(200) NOT NULL,
    system_type    VARCHAR(50) NOT NULL COMMENT 'PATENT/TRADEMARK/PAYMENT/OTHER',
    base_url       VARCHAR(1000) NULL COMMENT '外部API基础地址',
    auth_type      VARCHAR(30) NULL COMMENT 'API_KEY/OAUTH2/CERT/NONE',
    credential_ref VARCHAR(500) NULL COMMENT '凭据引用，不直接存真实密钥',
    status         TINYINT NOT NULL DEFAULT 1 COMMENT '0停用 1启用',
    remark         VARCHAR(1000) NULL,
    create_time    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted     TINYINT NOT NULL DEFAULT 0,

    PRIMARY KEY (id),
    UNIQUE KEY uk_external_system_code (system_code),
    KEY idx_external_system_status (status),

    CONSTRAINT ck_external_system_status
        CHECK (status IN (0, 1)),
    CONSTRAINT ck_external_system_deleted
        CHECK (is_deleted IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='外部系统定义';

CREATE TABLE external_case_binding (
    id                      BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    case_id                 BIGINT UNSIGNED NOT NULL,
    external_system_id      BIGINT UNSIGNED NOT NULL,
    external_case_id        VARCHAR(200) NULL COMMENT '外部系统案件ID',
    external_application_no VARCHAR(200) NULL COMMENT '外部申请/注册号',
    sync_status             VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    last_sync_time          DATETIME NULL,
    last_error              VARCHAR(1000) NULL,
    create_time             DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time             DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted              TINYINT NOT NULL DEFAULT 0,

    PRIMARY KEY (id),
    UNIQUE KEY uk_external_binding_case_system (case_id, external_system_id),
    UNIQUE KEY uk_external_binding_external_case (external_system_id, external_case_id),
    KEY idx_external_binding_sync (sync_status, last_sync_time),

    CONSTRAINT fk_external_binding_case
        FOREIGN KEY (case_id) REFERENCES case_info(id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_external_binding_system
        FOREIGN KEY (external_system_id) REFERENCES external_system(id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT ck_external_binding_sync
        CHECK (sync_status IN ('PENDING', 'SUCCESS', 'FAILED')),
    CONSTRAINT ck_external_binding_deleted
        CHECK (is_deleted IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='内部案件与外部系统案件映射';

CREATE TABLE external_sync_task (
    id                 BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    external_system_id BIGINT UNSIGNED NOT NULL,
    business_type      VARCHAR(40) NOT NULL COMMENT 'CASE/DOCUMENT/PAYMENT等',
    business_id        BIGINT UNSIGNED NOT NULL,
    sync_type          VARCHAR(30) NOT NULL COMMENT 'PUSH/PULL/STATUS_QUERY',
    request_payload    JSON NULL,
    response_payload   JSON NULL,
    status             VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    retry_count        INT NOT NULL DEFAULT 0,
    max_retry_count    INT NOT NULL DEFAULT 3,
    next_retry_time    DATETIME NULL,
    last_error         VARCHAR(2000) NULL,
    create_time        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    start_time         DATETIME NULL,
    finish_time        DATETIME NULL,
    update_time        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted         TINYINT NOT NULL DEFAULT 0,

    PRIMARY KEY (id),
    KEY idx_sync_task_status_retry (status, next_retry_time),
    KEY idx_sync_task_business (business_type, business_id),
    KEY idx_sync_task_system_time (external_system_id, create_time),

    CONSTRAINT fk_sync_task_system
        FOREIGN KEY (external_system_id) REFERENCES external_system(id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT ck_sync_task_type
        CHECK (sync_type IN ('PUSH', 'PULL', 'STATUS_QUERY')),
    CONSTRAINT ck_sync_task_status
        CHECK (status IN ('PENDING', 'PROCESSING', 'SUCCESS', 'FAILED')),
    CONSTRAINT ck_sync_task_retry
        CHECK (retry_count >= 0 AND max_retry_count >= 0),
    CONSTRAINT ck_sync_task_deleted
        CHECK (is_deleted IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='外部系统异步同步任务';

-- =========================================================
-- 8. 审计日志
-- =========================================================

CREATE TABLE operation_log (
    id             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id        BIGINT UNSIGNED NULL COMMENT '操作用户，游客可为空',
    username       VARCHAR(50) NULL COMMENT '冗余保存用户名，便于长期审计',
    role           VARCHAR(20) NULL COMMENT '操作时角色',
    module         VARCHAR(50) NOT NULL COMMENT '业务模块',
    operation      VARCHAR(100) NOT NULL COMMENT '操作名称',
    business_type  VARCHAR(50) NULL,
    business_id    BIGINT UNSIGNED NULL,
    request_method VARCHAR(20) NULL,
    request_path   VARCHAR(1000) NULL,
    ip_address     VARCHAR(64) NULL,
    device_type    VARCHAR(30) NULL COMMENT 'PC/MOBILE/OTHER',
    user_agent     VARCHAR(1000) NULL,
    detail_json    JSON NULL COMMENT '必要的业务变更摘要，避免记录敏感密码',
    result         VARCHAR(20) NOT NULL DEFAULT 'SUCCESS',
    error_message  VARCHAR(2000) NULL,
    create_time    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    KEY idx_operation_log_user_time (user_id, create_time),
    KEY idx_operation_log_business (business_type, business_id),
    KEY idx_operation_log_module_time (module, create_time),
    KEY idx_operation_log_create_time (create_time),

    CONSTRAINT fk_operation_log_user
        FOREIGN KEY (user_id) REFERENCES sys_user(id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT ck_operation_log_result
        CHECK (result IN ('SUCCESS', 'FAILED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统操作审计日志，逻辑上至少保留5年';

-- =========================================================
-- 9. 基础外部系统定义
--    仅初始化“系统类型”，不包含真实API密钥
-- =========================================================

INSERT INTO external_system
(system_code, system_name, system_type, base_url, auth_type, credential_ref, status, remark)
VALUES
('CPC', '国家知识产权局电子申请网（CPC）', 'PATENT', NULL, NULL, NULL, 1,
 '课程项目中使用Mock Adapter；真实接入时配置正式API/证书'),
('TRADEMARK', '商标网上申请系统', 'TRADEMARK', NULL, NULL, NULL, 1,
 '课程项目中使用Mock Adapter；真实接入取决于官方接口授权'),
('PATENT_PAYMENT', '专利缴费信息网上补充及管理系统', 'PAYMENT', NULL, NULL, NULL, 1,
 '课程项目中使用Mock Adapter；真实接入取决于官方接口授权');

-- =========================================================
-- 10. Fork C V3 扩展表（与 sql/migration_c_r2.sql 保持一致）
-- =========================================================
-- 已初始化的 V2 库请单独执行 sql/migration_c_r2.sql

CREATE TABLE IF NOT EXISTS agent_skill (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    agent_id BIGINT UNSIGNED NOT NULL,
    skill_type VARCHAR(40) NULL,
    skill_code VARCHAR(80) NULL,
    proficiency VARCHAR(40) NULL,
    valid_from DATE NULL,
    valid_to DATE NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_agent_skill_agent (agent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS work_item (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    case_id BIGINT UNSIGNED NOT NULL,
    stage_id BIGINT UNSIGNED NULL,
    deadline_id BIGINT UNSIGNED NULL,
    parent_id BIGINT UNSIGNED NULL,
    work_type VARCHAR(40) NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT NULL,
    assignee_agent_id BIGINT UNSIGNED NULL,
    creator_user_id BIGINT UNSIGNED NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'TODO',
    priority VARCHAR(20) NULL DEFAULT 'MEDIUM',
    start_time DATETIME NULL,
    due_time DATETIME NULL,
    completed_time DATETIME NULL,
    estimated_hours DECIMAL(10,2) NULL,
    actual_hours DECIMAL(10,2) NULL,
    transfer_policy VARCHAR(30) NULL DEFAULT 'FOLLOW_PRINCIPAL',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_work_item_case_status (case_id, status),
    KEY idx_work_item_assignee (assignee_agent_id, status, due_time),
    KEY idx_work_item_deadline (deadline_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS work_dependency (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    case_id BIGINT UNSIGNED NOT NULL,
    predecessor_id BIGINT UNSIGNED NOT NULL,
    successor_id BIGINT UNSIGNED NOT NULL,
    dependency_type VARCHAR(30) NOT NULL DEFAULT 'FINISH_TO_START',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_work_dep_pred (predecessor_id),
    KEY idx_work_dep_succ (successor_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS timesheet (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    work_item_id BIGINT UNSIGNED NOT NULL,
    case_id BIGINT UNSIGNED NOT NULL,
    agent_id BIGINT UNSIGNED NOT NULL,
    work_date DATE NOT NULL,
    hours DECIMAL(10,2) NOT NULL,
    description VARCHAR(500) NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_timesheet_item (work_item_id),
    KEY idx_timesheet_agent_date (agent_id, work_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS document_series (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    case_id BIGINT UNSIGNED NOT NULL,
    document_type VARCHAR(40) NOT NULL,
    logical_name VARCHAR(300) NULL,
    current_version_id BIGINT UNSIGNED NULL,
    approved_version_id BIGINT UNSIGNED NULL,
    status VARCHAR(30) NULL DEFAULT 'ACTIVE',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_doc_series_case (case_id, document_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS document_version (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    series_id BIGINT UNSIGNED NOT NULL,
    document_id BIGINT UNSIGNED NOT NULL,
    version_no INT NOT NULL,
    parent_document_id BIGINT UNSIGNED NULL,
    file_hash VARCHAR(128) NULL,
    version_status VARCHAR(30) NULL DEFAULT 'CURRENT',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_doc_version (series_id, version_no),
    KEY idx_doc_version_doc (document_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS change_request (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    series_id BIGINT UNSIGNED NOT NULL,
    source_version_id BIGINT UNSIGNED NOT NULL,
    new_document_id BIGINT UNSIGNED NULL,
    requester_user_id BIGINT UNSIGNED NOT NULL,
    approver_user_id BIGINT UNSIGNED NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    reason VARCHAR(1000) NULL,
    impact VARCHAR(1000) NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS workflow_definition (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    code VARCHAR(80) NOT NULL,
    name VARCHAR(200) NOT NULL,
    business_type VARCHAR(40) NULL,
    status TINYINT NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_wf_def_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS workflow_version (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    definition_id BIGINT UNSIGNED NOT NULL,
    version_no INT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    published_at DATETIME NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_wf_ver (definition_id, version_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS workflow_state (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    version_id BIGINT UNSIGNED NOT NULL,
    state_code VARCHAR(80) NOT NULL,
    state_name VARCHAR(200) NULL,
    is_initial TINYINT NOT NULL DEFAULT 0,
    is_final TINYINT NOT NULL DEFAULT 0,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_wf_state_ver (version_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS workflow_transition (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    version_id BIGINT UNSIGNED NOT NULL,
    from_state VARCHAR(80) NOT NULL,
    event_code VARCHAR(80) NOT NULL,
    to_state VARCHAR(80) NOT NULL,
    allowed_role VARCHAR(40) NULL,
    guard_rule VARCHAR(80) NULL,
    actions VARCHAR(500) NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_wf_trans_ver (version_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS workflow_instance (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    case_id BIGINT UNSIGNED NOT NULL,
    definition_id BIGINT UNSIGNED NOT NULL,
    version_id BIGINT UNSIGNED NOT NULL,
    current_state VARCHAR(80) NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_wf_inst_case (case_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS workflow_history (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    instance_id BIGINT UNSIGNED NOT NULL,
    case_id BIGINT UNSIGNED NOT NULL,
    from_state VARCHAR(80) NULL,
    to_state VARCHAR(80) NULL,
    event_code VARCHAR(80) NULL,
    operator_user_id BIGINT UNSIGNED NULL,
    reason VARCHAR(500) NULL,
    occurred_at DATETIME NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_wf_hist_inst (instance_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS checklist_template (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    code VARCHAR(80) NOT NULL,
    name VARCHAR(200) NULL,
    gate_code VARCHAR(80) NULL,
    items_json TEXT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_chk_tpl (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS checklist_instance (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    case_id BIGINT UNSIGNED NOT NULL,
    template_id BIGINT UNSIGNED NOT NULL,
    gate_code VARCHAR(80) NULL,
    status VARCHAR(20) NULL DEFAULT 'OPEN',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_chk_inst_case (case_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS checklist_item (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    instance_id BIGINT UNSIGNED NOT NULL,
    item_label VARCHAR(200) NOT NULL,
    required_flag TINYINT NOT NULL DEFAULT 1,
    checked_flag TINYINT NOT NULL DEFAULT 0,
    missing_message VARCHAR(300) NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_chk_item_inst (instance_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS dynamic_form_definition (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    code VARCHAR(80) NOT NULL,
    name VARCHAR(200) NULL,
    case_type VARCHAR(40) NULL,
    version_no INT NULL DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS dynamic_form_field (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    definition_id BIGINT UNSIGNED NOT NULL,
    field_key VARCHAR(80) NOT NULL,
    label VARCHAR(200) NULL,
    field_type VARCHAR(30) NULL,
    required_flag TINYINT NULL DEFAULT 0,
    options_json TEXT NULL,
    validation VARCHAR(200) NULL,
    visible_condition VARCHAR(200) NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS dynamic_form_instance (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    case_id BIGINT UNSIGNED NOT NULL,
    definition_id BIGINT UNSIGNED NOT NULL,
    form_version INT NULL,
    json_value TEXT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS ocr_job (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    document_id BIGINT UNSIGNED NOT NULL,
    case_id BIGINT UNSIGNED NOT NULL,
    engine VARCHAR(50) NULL DEFAULT 'MOCK',
    engine_version VARCHAR(50) NULL DEFAULT 'v1',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    started_at DATETIME NULL,
    finished_at DATETIME NULL,
    raw_text TEXT NULL,
    error_message VARCHAR(1000) NULL,
    retry_count INT NOT NULL DEFAULT 0,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_ocr_job_doc (document_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS ocr_extracted_field (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    job_id BIGINT UNSIGNED NOT NULL,
    field_name VARCHAR(80) NOT NULL,
    raw_value VARCHAR(500) NULL,
    parsed_value VARCHAR(500) NULL,
    confidence DECIMAL(5,4) NULL,
    confirmed_value VARCHAR(500) NULL,
    confirmed_by BIGINT UNSIGNED NULL,
    confirmed_at DATETIME NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_ocr_field_job (job_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS ocr_correction (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    field_id BIGINT UNSIGNED NOT NULL,
    old_value VARCHAR(500) NULL,
    new_value VARCHAR(500) NULL,
    operator_user_id BIGINT UNSIGNED NULL,
    reason VARCHAR(500) NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS business_calendar (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    code VARCHAR(80) NOT NULL,
    name VARCHAR(200) NULL,
    status TINYINT NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_cal_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS calendar_day (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    calendar_id BIGINT UNSIGNED NOT NULL,
    day_date DATE NOT NULL,
    is_workday TINYINT NOT NULL DEFAULT 1,
    holiday_name VARCHAR(100) NULL,
    override_type VARCHAR(30) NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_cal_day (calendar_id, day_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS deadline_rule (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    rule_code VARCHAR(80) NOT NULL,
    business_type VARCHAR(40) NULL,
    trigger_event VARCHAR(80) NULL,
    base_days INT NULL,
    day_type VARCHAR(20) NULL DEFAULT 'CALENDAR',
    adjustment_policy VARCHAR(40) NULL,
    internal_offset_days INT NULL,
    effective_from DATE NULL,
    effective_to DATE NULL,
    version_no INT NULL DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_dl_rule (rule_code, version_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS deadline_history (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    deadline_id BIGINT UNSIGNED NOT NULL,
    change_type VARCHAR(40) NULL,
    old_value DATETIME NULL,
    new_value DATETIME NULL,
    operator_user_id BIGINT UNSIGNED NULL,
    reason VARCHAR(500) NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_dl_hist (deadline_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS deadline_adjustment (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    deadline_id BIGINT UNSIGNED NOT NULL,
    old_value DATETIME NULL,
    new_value DATETIME NULL,
    operator_user_id BIGINT UNSIGNED NULL,
    reason VARCHAR(500) NULL,
    attachment VARCHAR(500) NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_dl_adj (deadline_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS deadline_reminder_log (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    deadline_id BIGINT UNSIGNED NOT NULL,
    node_code VARCHAR(20) NULL,
    remind_date DATE NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_dl_remind (deadline_id, remind_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS risk_record (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    case_id BIGINT UNSIGNED NOT NULL,
    risk_type VARCHAR(40) NULL,
    level_code VARCHAR(20) NULL,
    title VARCHAR(200) NULL,
    description VARCHAR(1000) NULL,
    status VARCHAR(20) NULL DEFAULT 'OPEN',
    related_id BIGINT UNSIGNED NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_risk_case (case_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS exception_case (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    case_id BIGINT UNSIGNED NOT NULL,
    exception_type VARCHAR(60) NULL,
    title VARCHAR(200) NULL,
    detail VARCHAR(1000) NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    related_id BIGINT UNSIGNED NULL,
    assignee_user_id BIGINT UNSIGNED NULL,
    resolution_note VARCHAR(1000) NULL,
    resolved_by BIGINT UNSIGNED NULL,
    resolved_at DATETIME NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_exc_case (case_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS data_quality_issue (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    issue_code VARCHAR(80) NULL,
    case_id BIGINT UNSIGNED NULL,
    detail VARCHAR(1000) NULL,
    status VARCHAR(20) NULL DEFAULT 'OPEN',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO workflow_definition (id, code, name, business_type, status)
VALUES (1, 'INVENTION_PATENT', '发明专利办案流程', 'INVENTION_PATENT', 1);
INSERT IGNORE INTO workflow_version (id, definition_id, version_no, status, published_at)
VALUES (1, 1, 1, 'PUBLISHED', NOW());
INSERT IGNORE INTO workflow_state (id, version_id, state_code, state_name, is_initial, is_final) VALUES
(1,1,'PROCESSING','办理中',1,0),(2,1,'FORMAL_EXAM','形式审查',0,0),(3,1,'GRANTED','已授权',0,0),(4,1,'CLOSED','已结案',0,1);
INSERT IGNORE INTO workflow_transition (id, version_id, from_state, event_code, to_state, allowed_role, guard_rule) VALUES
(1,1,'PROCESSING','START_FORMAL','FORMAL_EXAM','AGENT',NULL),
(2,1,'FORMAL_EXAM','FILE_APPLICATION','GRANTED','AGENT','SUBMIT_GATE'),
(3,1,'GRANTED','CLOSE','CLOSED','ADMIN',NULL);

INSERT IGNORE INTO checklist_template (id, code, name, gate_code, items_json) VALUES
(1,'SUBMIT_GATE','正式提交 Gate','SUBMIT_GATE','["APPLICATION document approved","required fields complete"]');

INSERT IGNORE INTO business_calendar (id, code, name, status) VALUES (1,'CN_WORKDAY','中国工作日日历',1);
INSERT IGNORE INTO deadline_rule (id, rule_code, business_type, trigger_event, base_days, day_type, internal_offset_days, version_no)
VALUES (1,'OA_RESPONSE_RULE_V1','PATENT','OCR_CONFIRMED',30,'CALENDAR',3,1);
