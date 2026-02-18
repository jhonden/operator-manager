-- Code Operator Management System Database Schema
-- Version 1.0.0
-- This script creates all tables, indexes, and constraints
--
-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================================================
-- USERS AND AUTHENTICATION
-- ============================================================================

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    full_name VARCHAR(255),
    avatar_url VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    role VARCHAR(50) NOT NULL DEFAULT 'USER',
    phone VARCHAR(20),
    department VARCHAR(100),
    last_login_at TIMESTAMP,
    last_login_ip VARCHAR(50),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    account_non_expired BOOLEAN NOT NULL DEFAULT TRUE,
    account_non_locked BOOLEAN NOT NULL DEFAULT TRUE,
    credentials_non_expired BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX idx_user_username ON users(username);
CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_user_status ON users(status);

-- ============================================================================
-- OPERATORS
-- ============================================================================

CREATE TABLE operators (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    language VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    version VARCHAR(50),
    code_file_path VARCHAR(500),
    file_name VARCHAR(255),
    file_size BIGINT,
    is_public BOOLEAN NOT NULL DEFAULT FALSE,
    downloads_count INTEGER DEFAULT 0,
    featured BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX idx_operator_name ON operators(name);
CREATE INDEX idx_operator_status ON operators(status);
CREATE INDEX idx_operator_language ON operators(language);
CREATE INDEX idx_operator_created_by ON operators(created_by);

-- ============================================================================
-- OPERATOR PARAMETERS
-- ============================================================================

CREATE TABLE operator_parameters (
    id BIGSERIAL PRIMARY KEY,
    operator_id BIGINT NOT NULL REFERENCES operators(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    parameter_type VARCHAR(20) NOT NULL,
    io_type VARCHAR(10) NOT NULL,
    is_required BOOLEAN NOT NULL DEFAULT FALSE,
    default_value TEXT,
    validation_rules TEXT,
    order_index INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX idx_parameter_operator ON operator_parameters(operator_id);
CREATE INDEX idx_parameter_type ON operator_parameters(parameter_type);

-- ============================================================================
-- OPERATOR PACKAGES
-- ============================================================================

CREATE TABLE operator_packages (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    business_scenario VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    version VARCHAR(50),
    icon VARCHAR(100),
    is_public BOOLEAN NOT NULL DEFAULT FALSE,
    downloads_count INTEGER DEFAULT 0,
    featured BOOLEAN NOT NULL DEFAULT FALSE,
    operator_count INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX idx_package_name ON operator_packages(name);
CREATE INDEX idx_package_status ON operator_packages(status);
CREATE INDEX idx_package_business_scenario ON operator_packages(business_scenario);

-- ============================================================================
-- PACKAGE OPERATORS (Link table with order)
-- ============================================================================

CREATE TABLE package_operators (
    id BIGSERIAL PRIMARY KEY,
    operator_id BIGINT NOT NULL REFERENCES operators(id) ON DELETE CASCADE,
    package_id BIGINT NOT NULL REFERENCES operator_packages(id) ON DELETE CASCADE,
    order_index INTEGER NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    version VARCHAR(50),
    parameter_mapping TEXT,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX idx_pkg_op_package ON package_operators(package_id);
CREATE INDEX idx_pkg_op_operator ON package_operators(operator_id);
CREATE INDEX idx_pkg_op_order ON package_operators(package_id, order_index);

-- ============================================================================
-- PUBLISH DESTINATIONS
-- ============================================================================

CREATE TABLE publish_destinations (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    destination_type VARCHAR(20) NOT NULL,
    endpoint VARCHAR(500),
    credentials TEXT,
    configuration TEXT,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX idx_publish_dest_type ON publish_destinations(destination_type);
CREATE INDEX idx_publish_dest_enabled ON publish_destinations(enabled);

-- ============================================================================
-- PUBLISH HISTORY
-- ============================================================================

CREATE TABLE publish_history (
    id BIGSERIAL PRIMARY KEY,
    publish_destination_id BIGINT NOT NULL REFERENCES publish_destinations(id) ON DELETE CASCADE,
    item_type VARCHAR(20) NOT NULL,
    item_id BIGINT NOT NULL,
    package_version_id BIGINT,
    version VARCHAR(50),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    error_message TEXT,
    published_path VARCHAR(500),
    metadata TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX idx_publish_dest ON publish_history(publish_destination_id);
CREATE INDEX idx_publish_status ON publish_history(status);
CREATE INDEX idx_publish_item_type ON publish_history(item_type);

-- ============================================================================
-- OPERATOR PERMISSIONS
-- ============================================================================

CREATE TABLE operator_permissions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    operator_id BIGINT REFERENCES operators(id) ON DELETE CASCADE,
    package_id BIGINT REFERENCES operator_packages(id) ON DELETE CASCADE,
    permission_type VARCHAR(20) NOT NULL,
    granted_by VARCHAR(100),
    granted_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX idx_permission_user ON operator_permissions(user_id);
CREATE INDEX idx_permission_operator ON operator_permissions(operator_id);
CREATE INDEX idx_permission_package ON operator_permissions(package_id);
CREATE INDEX idx_permission_type ON operator_permissions(permission_type);

-- Add constraint to ensure either operator_id or package_id is set
ALTER TABLE operator_permissions ADD CONSTRAINT check_permission_target
CHECK (operator_id IS NOT NULL OR package_id IS NOT NULL);

-- ============================================================================
-- AUDIT LOGS
-- ============================================================================

CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    username VARCHAR(100),
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100),
    entity_id BIGINT,
    entity_name VARCHAR(255),
    old_values TEXT,
    new_values TEXT,
    ip_address VARCHAR(50),
    user_agent VARCHAR(500),
    operation_type VARCHAR(20),
    success BOOLEAN NOT NULL DEFAULT TRUE,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_user ON audit_logs(user_id);
CREATE INDEX idx_audit_action ON audit_logs(action);
CREATE INDEX idx_audit_entity_type ON audit_logs(entity_type);
CREATE INDEX idx_audit_created ON audit_logs(created_at);

COMMIT;
