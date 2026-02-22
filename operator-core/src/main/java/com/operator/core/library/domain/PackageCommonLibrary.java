package com.operator.core.library.domain;

import com.operator.core.domain.BaseEntity;
import com.operator.core.operator.domain.Operator;
import com.operator.core.pkg.domain.OperatorPackage;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 算子包-公共库关联实体类
 * 表示算子包包含的公共库，包含版本和打包路径配置
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Entity
@Table(name = "package_common_libraries", indexes = {
    @Index(name = "idx_pkg_common_library_pkg", columnList = "package_id"),
    @Index(name = "idx_pkg_common_library_lib", columnList = "library_id"),
    @Index(name = "idx_pkg_lib_custom_path", columnList = "custom_package_path")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PackageCommonLibrary extends BaseEntity {

    /**
     * 关联的算子包
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id", nullable = false)
    private OperatorPackage operatorPackage;

    /**
     * 来源算子（公共库来自哪个算子的依赖）
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

    /**
     * 指定使用的版本
     */
    @Column(name = "version", nullable = false, length = 50)
    private String version;

    /**
     * 打包顺序
     */
    @Column(name = "order_index")
    private Integer orderIndex;

    /**
     * 自定义打包路径
     */
    @Column(name = "custom_package_path", length = 500)
    private String customPackagePath;

    /**
     * 是否使用自定义路径
     */
    @Column(name = "use_custom_path", nullable = false)
    @Builder.Default
    private Boolean useCustomPath = false;
}
