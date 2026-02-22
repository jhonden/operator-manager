-- 公共库支持功能
-- 创建日期：2026-02-21
-- 描述：添加公共库、算子包打包路径配置等功能

-- 公共库表
CREATE TABLE common_libraries (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    version VARCHAR(50) NOT NULL,
    category VARCHAR(100),
    library_type VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- 公共库文件表
CREATE TABLE common_library_files (
    id BIGSERIAL PRIMARY KEY,
    library_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500),
    code TEXT,
    order_index INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT fk_library_file FOREIGN KEY (library_id)
        REFERENCES common_libraries(id) ON DELETE CASCADE
);

-- 算子-公共库关联表
CREATE TABLE operator_common_libraries (
    id BIGSERIAL PRIMARY KEY,
    operator_id BIGINT NOT NULL,
    library_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT fk_op_common_library FOREIGN KEY (operator_id)
        REFERENCES operators(id) ON DELETE CASCADE,
    CONSTRAINT fk_library_operator FOREIGN KEY (library_id)
        REFERENCES common_libraries(id) ON DELETE CASCADE
);

-- 算子包-公共库关联表
CREATE TABLE package_common_libraries (
    id BIGSERIAL PRIMARY KEY,
    package_id BIGINT NOT NULL,
    library_id BIGINT NOT NULL,
    version VARCHAR(50) NOT NULL,
    order_index INTEGER,
    custom_package_path VARCHAR(500),
    use_custom_path BOOLEAN DEFAULT false,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pkg_common_library FOREIGN KEY (package_id)
        REFERENCES operator_packages(id) ON DELETE CASCADE,
    CONSTRAINT fk_library_package FOREIGN KEY (library_id)
        REFERENCES common_libraries(id) ON DELETE CASCADE
);

-- 在现有 package_operators 表中增加字段
ALTER TABLE package_operators
    ADD COLUMN custom_package_path VARCHAR(500),
    ADD COLUMN use_custom_path BOOLEAN DEFAULT false;

-- 在现有 operator_packages 表中增加字段
ALTER TABLE operator_packages
    ADD COLUMN package_template VARCHAR(50) DEFAULT 'legacy';

-- 索引
CREATE INDEX idx_library_name ON common_libraries(name);
CREATE INDEX idx_library_version ON common_libraries(version);
CREATE INDEX idx_library_file_library ON common_library_files(library_id);
CREATE INDEX idx_op_common_library_op ON operator_common_libraries(operator_id);
CREATE INDEX idx_op_common_library_lib ON operator_common_libraries(library_id);
CREATE INDEX idx_pkg_common_library_pkg ON package_common_libraries(package_id);
CREATE INDEX idx_pkg_common_library_lib ON package_common_libraries(library_id);
CREATE INDEX idx_pkg_op_custom_path ON package_operators(custom_package_path);
CREATE INDEX idx_pkg_lib_custom_path ON package_common_libraries(custom_package_path);

-- 添加唯一约束：公共库名称+版本唯一
CREATE UNIQUE INDEX idx_library_name_version ON common_libraries(name, version);
