-- 授予 operator_user 数据库访问权限
-- 创建日期：2026-02-22
-- 描述：为应用运行用户 operator_user 授予 public schema 中所有表和序列的访问权限
--
-- 此脚本使用版本号 9999，确保它始终是最后执行的迁移脚本

-- 授予表访问权限
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO operator_user;

-- 授予序列访问权限（用于自增 ID）
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO operator_user;

-- 授予 schema 使用权限
GRANT USAGE ON SCHEMA public TO operator_user;
