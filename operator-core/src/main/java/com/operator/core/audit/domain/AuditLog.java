package com.operator.core.audit.domain;

import com.operator.core.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Audit Log Entity - tracks system operations for auditing
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_user", columnList = "user_id"),
    @Index(name = "idx_audit_action", columnList = "action"),
    @Index(name = "idx_audit_entity_type", columnList = "entity_type"),
    @Index(name = "idx_audit_created", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AuditLog extends BaseEntity {

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username", length = 100)
    private String username;

    @Column(name = "action", nullable = false, length = 100)
    private String action; // e.g., "CREATE_OPERATOR", "UPDATE_PACKAGE"

    @Column(name = "entity_type", length = 100)
    private String entityType; // e.g., "OPERATOR", "PACKAGE", "TASK"

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "entity_name", length = 255)
    private String entityName;

    @Column(name = "old_values", columnDefinition = "TEXT")
    private String oldValues; // JSON format

    @Column(name = "new_values", columnDefinition = "TEXT")
    private String newValues; // JSON format

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "operation_type", length = 20)
    @Enumerated(EnumType.STRING)
    private OperationType operationType;

    @Column(name = "success", nullable = false)
    @Builder.Default
    private Boolean success = true;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * Operation type enum
     */
    public enum OperationType {
        CREATE,
        READ,
        UPDATE,
        DELETE,
        EXECUTE,
        EXPORT,
        IMPORT
    }
}
