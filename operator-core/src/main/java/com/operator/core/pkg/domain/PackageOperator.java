package com.operator.core.pkg.domain;

import com.operator.core.domain.BaseEntity;
import com.operator.core.operator.domain.Operator;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Package Operator Entity - represents the relationship between package and operator
 * Includes execution order and version specification
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Entity
@Table(name = "package_operators", indexes = {
    @Index(name = "idx_pkg_op_package", columnList = "package_id"),
    @Index(name = "idx_pkg_op_operator", columnList = "operator_id"),
    @Index(name = "idx_pkg_op_order", columnList = "package_id, order_index")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PackageOperator extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id", nullable = false)
    private OperatorPackage operatorPackage;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "operator_id", nullable = false)
    private Operator operator;

    @Column(name = "version", length = 50)
    @Builder.Default
    private String version = "1.0";

    @Column(name = "order_index", nullable = false)
    @Builder.Default
    private Integer orderIndex = 0;

    @Column(name = "parameter_mapping", columnDefinition = "TEXT")
    private String parameterMapping; // JSON format for parameter mapping between operators

    @Column(name = "enabled", nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
