package com.operator.core.security.domain;

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
 * Operator Permission Entity - represents access permissions for operators and packages
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Entity
@Table(name = "operator_permissions", indexes = {
    @Index(name = "idx_permission_user", columnList = "user_id"),
    @Index(name = "idx_permission_operator", columnList = "operator_id"),
    @Index(name = "idx_permission_package", columnList = "package_id"),
    @Index(name = "idx_permission_type", columnList = "permission_type")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OperatorPermission extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_id")
    private Operator operator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id")
    private OperatorPackage operatorPackage;

    @Column(name = "permission_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private PermissionType permissionType;

    @Column(name = "granted_by", length = 100)
    private String grantedBy;

    @Column(name = "granted_at")
    private java.time.LocalDateTime grantedAt;

    /**
     * Permission type enum
     */
    public enum PermissionType {
        READ,
        WRITE,
        EXECUTE,
        ADMIN,
        OWNER
    }
}
