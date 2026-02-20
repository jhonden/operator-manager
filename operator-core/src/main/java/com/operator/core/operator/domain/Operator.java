package com.operator.core.operator.domain;

import com.operator.core.domain.BaseEntity;
import com.operator.common.enums.LanguageType;
import com.operator.common.enums.OperatorStatus;
import com.operator.common.validation.OperatorCode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Operator Entity - represents a code operator
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Entity
@Table(name = "operators", indexes = {
    @Index(name = "idx_operator_name", columnList = "name"),
    @Index(name = "idx_operator_status", columnList = "status"),
    @Index(name = "idx_operator_language", columnList = "language"),
    @Index(name = "idx_operator_created_by", columnList = "created_by")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Operator extends BaseEntity {

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "language", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private LanguageType language;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private OperatorStatus status = OperatorStatus.DRAFT;

    @Column(name = "code_file_path", length = 500)
    private String codeFilePath;

    @Column(name = "code", columnDefinition = "TEXT")
    private String code;

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "is_public", nullable = false)
    @Builder.Default
    private Boolean isPublic = false;

    @Column(name = "downloads_count")
    @Builder.Default
    private Integer downloadsCount = 0;

    @Column(name = "featured", nullable = false)
    @Builder.Default
    private Boolean featured = false;

    @Column(name = "version", length = 50)
    @Builder.Default
    private String version = "1.0";

    @Column(name = "operator_code", nullable = false, unique = true, length = 64)
    @OperatorCode
    private String operatorCode;

    @Column(name = "object_code", nullable = false, length = 64)
    @OperatorCode
    private String objectCode;

    @Column(name = "data_format", length = 20)
    private String dataFormat;

    @Column(name = "generator", length = 20)
    private String generator;

    @Column(name = "business_logic", columnDefinition = "TEXT")
    private String businessLogic;

    @OneToMany(mappedBy = "operator", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    private List<Parameter> parameters = new ArrayList<>();
}
