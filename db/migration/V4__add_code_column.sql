-- 添加算子代码字段
-- 执行日期: 2026-02-20
-- 说明: 为 operators 表新增 code 字段，用于存储算子的实现代码

-- 新增 code 列（实现代码）
ALTER TABLE public.operators
ADD COLUMN code TEXT;
