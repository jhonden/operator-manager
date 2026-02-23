package com.operator.common.dto.pkg;

import lombok.Data;

import java.util.List;

/**
 * 算子包导入元数据 DTO
 *
 * 用于解析 metainfo_operators.yml 文件
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Data
public class PackageImportMetadata {
    /**
     * 业务场景名称
     */
    private String businessName;

    /**
     * 版本号
     */
    private String version;

    /**
     * 算子列表
     */
    private Operators operators;

    /**
     * 算子列表容器
     */
    @Data
    public static class Operators {
        /**
         * 算子实例列表
         */
        private List<OperatorMetadata> instances;
    }

    /**
     * 算子元数据
     */
    @Data
    public static class OperatorMetadata {
        /**
         * 算子编码
         */
        private String operator_code;

        /**
         * 算子名称
         */
        private String name;

        /**
         * 对象编码
         */
        private String object_code;

        /**
         * 数据格式
         */
        private String data_format;

        /**
         * 生成方式
         */
        private String generator;

        /**
         * 执行顺序
         */
        private Integer order_no;
    }
}
