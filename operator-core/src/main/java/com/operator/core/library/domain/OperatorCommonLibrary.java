package com.operator.core.library.domain;

import com.operator.core.domain.BaseEntity;
import com.operator.core.operator.domain.Operator;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 算子-公共库关联实体类
 * 表示算子依赖的公共库（不指定版本，版本在算子包层面统一管理）
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Entity
@Table(name = "operator_common_libraries", indexes = {
    @Index(name = "idx_op_common_library_op", columnList = "operator_id"),
    @Index(name = "idx_op_common_library_lib", columnList = "library_id"),
    @Index(name = "idx_op_lib_unique", columnList = "operator_id, library_id", unique = true)
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OperatorCommonLibrary extends BaseEntity {

    /**
     * 关联的算子
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_id", nullable = false)
    private Operator operator;

    /**
     * 关联的公共库
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "library_id", nullable = false)
    private CommonLibrary library;
}
