-- 调整算子包-公共库关联表设计
-- 创建日期：2026-02-22
-- 描述：
--   1. 添加 operator_id 字段，用于追踪来源算子
--   2. 添加唯一约束，防止同一算子的同一公共库重复添加

-- 删除旧索引（如果存在）
DROP INDEX IF EXISTS idx_pkg_common_library_lib;

-- 添加 operator_id 字段
ALTER TABLE package_common_libraries
    ADD COLUMN IF NOT EXISTS operator_id BIGINT,
    ADD CONSTRAINT fk_pkg_library_operator
        FOREIGN KEY (operator_id) REFERENCES operators(id) ON DELETE CASCADE;

-- 添加唯一约束：同一算子包+算子+公共库只能有一条记录
CREATE UNIQUE INDEX IF NOT EXISTS idx_pkg_operator_library_unique
    ON package_common_libraries(package_id, operator_id, library_id);

-- 重建 library_id 索引
CREATE INDEX idx_pkg_common_library_lib
    ON package_common_libraries(library_id);
