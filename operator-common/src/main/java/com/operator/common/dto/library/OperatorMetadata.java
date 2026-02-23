package com.operator.common.dto.library;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 算子元数据 DTO
 * 用于生成 metainfo_operators.yml 文件
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperatorMetadata {
    /**
     * 算子编码
     */
    private String operatorCode;

    /**
     * 算子名称
     */
    private String name;

    /**
     * 对象编码
     */
    private String objectCode;

    /**
     * 数据格式
     */
    private String dataFormat;

    /**
     * 生成方式
     */
    private String generator;

    /**
     * 在包中的顺序
     */
    private Integer orderNo;
}
