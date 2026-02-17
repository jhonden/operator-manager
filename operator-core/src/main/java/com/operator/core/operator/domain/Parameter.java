package com.operator.core.operator.domain;

import com.operator.common.enums.IOType;
import com.operator.common.enums.ParameterType;
import com.operator.core.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/**
 * Parameter Entity - represents operator parameters
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Entity
@Table(name = "operator_parameters", indexes = {
    @Index(name = "idx_parameter_operator", columnList = "operator_id"),
    @Index(name = "idx_parameter_type", columnList = "parameter_type")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Parameter extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Operator operator;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "parameter_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ParameterType parameterType;

    @Column(name = "io_type", nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private IOType ioType;

    @Column(name = "is_required", nullable = false)
    @Builder.Default
    private Boolean isRequired = false;

    @Column(name = "default_value", columnDefinition = "TEXT")
    private String defaultValue;

    @Column(name = "validation_rules", columnDefinition = "TEXT")
    private String validationRules; // JSON format

    @Column(name = "order_index")
    @Builder.Default
    private Integer orderIndex = 0;

    /**
     * Parameter data type enum
     */

    /**
     * Input/Output type enum
     */
}
