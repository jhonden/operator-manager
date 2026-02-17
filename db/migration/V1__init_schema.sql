-- Code Operator Management System Database Schema
-- Version 1.0.0
-- This script creates all tables, indexes, and constraints

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
-- CATEGORIES
-- ============================================================================

CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    icon VARCHAR(100),
    color VARCHAR(20),
    parent_id BIGINT REFERENCES categories(id) ON DELETE SET NULL,
    order_index INTEGER,
    operator_count INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX idx_category_name ON categories(name);
CREATE INDEX idx_category_parent ON categories(parent_id);

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
    category_id BIGINT REFERENCES categories(id) ON DELETE SET NULL,
    tags TEXT,
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
CREATE INDEX idx_operator_category ON operators(category_id);
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
    tags TEXT,
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
    package_id BIGINT NOT NULL REFERENCES operator_packages(id) ON DELETE CASCADE,
    operator_id BIGINT NOT NULL REFERENCES operators(id) ON DELETE CASCADE,
    version_id BIGINT NOT NULL REFERENCES versions(id) ON DELETE CASCADE,
    order_index INTEGER NOT NULL DEFAULT 0,
    parameter_mapping TEXT,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    UNIQUE(package_id, operator_id)
);

CREATE INDEX idx_pkg_op_package ON package_operators(package_id);
CREATE INDEX idx_pkg_op_operator ON package_operators(operator_id);
CREATE INDEX idx_pkg_op_order ON package_operators(package_id, order_index);

-- ============================================================================
-- VERSIONS (Operator versions)
-- ============================================================================

CREATE TABLE versions (
    id BIGSERIAL PRIMARY KEY,
    operator_id BIGINT NOT NULL REFERENCES operators(id) ON DELETE CASCADE,
    version_number VARCHAR(50) NOT NULL,
    description TEXT,
    changelog TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    code_file_path VARCHAR(500),
    file_name VARCHAR(255),
    file_size BIGINT,
    git_commit_hash VARCHAR(100),
    git_tag VARCHAR(100),
    is_released BOOLEAN NOT NULL DEFAULT FALSE,
    release_date TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX idx_version_operator ON versions(operator_id);
CREATE INDEX idx_version_number ON versions(version_number);
CREATE INDEX idx_version_status ON versions(status);

-- ============================================================================
-- PACKAGE VERSIONS
-- ============================================================================

CREATE TABLE package_versions (
    id BIGSERIAL PRIMARY KEY,
    package_id BIGINT NOT NULL REFERENCES operator_packages(id) ON DELETE CASCADE,
    version_number VARCHAR(50) NOT NULL,
    description TEXT,
    changelog TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    operator_versions TEXT,
    git_commit_hash VARCHAR(100),
    git_tag VARCHAR(100),
    is_released BOOLEAN NOT NULL DEFAULT FALSE,
    release_date TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX idx_pkg_version_package ON package_versions(package_id);
CREATE INDEX idx_pkg_version_number ON package_versions(version_number);
CREATE INDEX idx_pkg_version_status ON package_versions(status);

-- ============================================================================
-- MARKET ITEMS
-- ============================================================================

CREATE TABLE market_items (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    item_type VARCHAR(20) NOT NULL,
    operator_id BIGINT,
    package_id BIGINT,
    featured BOOLEAN NOT NULL DEFAULT FALSE,
    average_rating NUMERIC(3,2) DEFAULT 0.00,
    ratings_count INTEGER DEFAULT 0,
    reviews_count INTEGER DEFAULT 0,
    downloads_count INTEGER DEFAULT 0,
    views_count INTEGER DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'PUBLISHED',
    published_date TIMESTAMP,
    tags TEXT,
    business_scenario VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX idx_market_item_type ON market_items(item_type);
CREATE INDEX idx_market_item_featured ON market_items(featured);
CREATE INDEX idx_market_item_operator ON market_items(operator_id);
CREATE INDEX idx_market_item_package ON market_items(package_id);
CREATE INDEX idx_market_item_rating ON market_items(average_rating);

-- ============================================================================
-- RATINGS
-- ============================================================================

CREATE TABLE ratings (
    id BIGSERIAL PRIMARY KEY,
    market_item_id BIGINT NOT NULL REFERENCES market_items(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL,
    rating INTEGER NOT NULL,
    review TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX idx_rating_market_item ON ratings(market_item_id);
CREATE INDEX idx_rating_user ON ratings(user_id);

-- ============================================================================
-- REVIEWS
-- ============================================================================

CREATE TABLE reviews (
    id BIGSERIAL PRIMARY KEY,
    market_item_id BIGINT NOT NULL REFERENCES market_items(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL,
    user_name VARCHAR(100),
    content TEXT NOT NULL,
    rating INTEGER,
    likes_count INTEGER DEFAULT 0,
    parent_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX idx_review_market_item ON reviews(market_item_id);
CREATE INDEX idx_review_user ON reviews(user_id);

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
    package_version_id BIGINT REFERENCES package_versions(id) ON DELETE SET NULL,
    item_type VARCHAR(20) NOT NULL,
    item_id BIGINT NOT NULL,
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
-- TASKS (Execution)
-- ============================================================================

CREATE TABLE tasks (
    id BIGSERIAL PRIMARY KEY,
    task_name VARCHAR(255) NOT NULL,
    task_type VARCHAR(20) NOT NULL,
    operator_id BIGINT REFERENCES operators(id) ON DELETE SET NULL,
    package_id BIGINT REFERENCES operator_packages(id) ON DELETE SET NULL,
    operator_version_id BIGINT,
    package_version_id BIGINT,
    user_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    priority INTEGER NOT NULL DEFAULT 0,
    input_parameters TEXT,
    output_data TEXT,
    progress INTEGER NOT NULL DEFAULT 0,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    error_message TEXT,
    container_id VARCHAR(100),
    retry_count INTEGER DEFAULT 0,
    max_retries INTEGER DEFAULT 3,
    timeout_seconds INTEGER DEFAULT 300,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX idx_task_status ON tasks(status);
CREATE INDEX idx_task_type ON tasks(task_type);
CREATE INDEX idx_task_operator ON tasks(operator_id);
CREATE INDEX idx_task_package ON tasks(package_id);
CREATE INDEX idx_task_user ON tasks(user_id);
CREATE INDEX idx_task_created ON tasks(created_at);

-- ============================================================================
-- TASK LOGS
-- ============================================================================

CREATE TABLE task_logs (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    log_level VARCHAR(20) NOT NULL,
    message TEXT NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    source VARCHAR(100),
    exception_trace TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_task_log_task ON task_logs(task_id);
CREATE INDEX idx_task_log_level ON task_logs(log_level);
CREATE INDEX idx_task_log_created ON task_logs(created_at);

-- ============================================================================
-- TASK ARTIFACTS
-- ============================================================================

CREATE TABLE task_artifacts (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    artifact_name VARCHAR(255) NOT NULL,
    artifact_type VARCHAR(50) NOT NULL,
    file_path VARCHAR(500),
    file_size BIGINT,
    content_type VARCHAR(100),
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX idx_task_artifact_task ON task_artifacts(task_id);
CREATE INDEX idx_task_artifact_type ON task_artifacts(artifact_type);

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

-- ============================================================================
-- INITIAL DATA
-- ============================================================================

-- Insert default admin user (password: admin123, encrypted with BCrypt)
INSERT INTO users (username, password, email, full_name, role, status, enabled)
VALUES ('admin', '$2a$10$YVJbZJhPqY8mZKqYWqmYVeUqFpPmj7qC5QJ6eEPJhLJ7mNxFqYqK', 'admin@operator-manager.com', 'System Administrator', 'ADMIN', 'ACTIVE', TRUE);

-- Insert default categories
INSERT INTO categories (name, description, icon, color, order_index) VALUES
('Data Processing', 'Data processing and transformation operators', 'database', '#1890ff', 1),
('File Operations', 'File reading, writing, and manipulation', 'file', '#52c41a', 2),
('API Integration', 'REST API and web service integration', 'api', '#722ed1', 3),
('Machine Learning', 'ML model training and inference', 'machine-learning', '#fa8c16', 4),
('Utilities', 'General utility operators', 'tool', '#13c2c2', 5);

-- Insert sub-categories
INSERT INTO categories (name, description, icon, color, parent_id, order_index) VALUES
('Data Cleaning', 'Clean and sanitize data', 'clean', '#1890ff', (SELECT id FROM categories WHERE name = 'Data Processing'), 1),
('Data Transformation', 'Transform data formats', 'transform', '#1890ff', (SELECT id FROM categories WHERE name = 'Data Processing'), 2),
('Data Validation', 'Validate data quality', 'check', '#1890ff', (SELECT id FROM categories WHERE name = 'Data Processing'), 3);

COMMIT;
