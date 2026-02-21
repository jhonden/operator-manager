-- 修复公共库相关表的审计字段类型
-- 创建日期：2026-02-21
-- 描述：将 common_libraries 和 common_library_files 表的 created_by、updated_by 列从 BIGINT 修改为 VARCHAR(100)

-- 修改 common_libraries 表的审计字段
ALTER TABLE common_libraries
    ALTER COLUMN created_by TYPE VARCHAR(100),
    ALTER COLUMN updated_by TYPE VARCHAR(100);

-- 修改 common_library_files 表的审计字段
ALTER TABLE common_library_files
    ALTER COLUMN created_by TYPE VARCHAR(100),
    ALTER COLUMN updated_by TYPE VARCHAR(100);
