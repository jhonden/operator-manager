-- 添加算子业务逻辑字段
-- 执行日期: 2026-02-20
-- 说明: 为 operators 表新增 business_logic 字段，用于存储算子的业务逻辑说明（Markdown 格式）

-- 新增 business_logic 字段（业务逻辑）
ALTER TABLE public.operators
ADD COLUMN business_logic TEXT;
