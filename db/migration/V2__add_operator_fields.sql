-- 添加算子基本信息扩展字段
-- 执行日期: 2026-02-20
-- 说明: 为 operators 表新增 operator_code、object_code、data_format、generator 4 个字段

-- 新增 operator_code 字段（算子唯一编码）
ALTER TABLE public.operators
ADD COLUMN operator_code VARCHAR(64) NOT NULL;

-- 新增 object_code 字段（数据对象编码）
ALTER TABLE public.operators
ADD COLUMN object_code VARCHAR(64) NOT NULL;

-- 新增 data_format 字段（数据格式）
ALTER TABLE public.operators
ADD COLUMN data_format VARCHAR(20);

-- 新增 generator 字段（生成方式）
ALTER TABLE public.operators
ADD COLUMN generator VARCHAR(20);

-- 为 operator_code 添加唯一索引
CREATE UNIQUE INDEX idx_operator_code ON public.operators (operator_code);

-- 为查询优化添加普通索引
CREATE INDEX idx_object_code ON public.operators (object_code);
CREATE INDEX idx_data_format ON public.operators (data_format);
CREATE INDEX idx_generator ON public.operators (generator);
