package com.operator.core.pkg.domain;

import com.operator.core.domain.BaseEntity;
import com.operator.common.enums.*;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Operator Package Entity - represents a package of operators for business scenarios
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Entity
@Table(name = "operator_packages", indexes = {
    @Index(name = "idx_package_name", columnList = "name"),
    @Index(name = "idx_package_status", columnList = "status"),
    @Index(name = "idx_package_business_scenario", columnList = "business_scenario")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OperatorPackage extends BaseEntity {

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "business_scenario", nullable = false, length = 255)
    private String businessScenario;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PackageStatus status = PackageStatus.DRAFT;

    @Column(name = "icon", length = 100)
    private String icon;

    @Column(name = "is_public", nullable = false)
    @Builder.Default
    private Boolean isPublic = false;

    @Column(name = "downloads_count")
    @Builder.Default
    private Integer downloadsCount = 0;

    @Column(name = "featured", nullable = false)
    @Builder.Default
    private Boolean featured = false;

    @Column(name = "operator_count")
    @Builder.Default
    private Integer operatorCount = 0;

    @Column(name = "version", length = 50)
    @Builder.Default
    private String version = "1.0";

    @OneToMany(mappedBy = "operatorPackage", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    private List<PackageOperator> packageOperators = new ArrayList<>();

    /**
     * Package status enum
     */
    public enum PackageStatus {
        DRAFT, PUBLISHED, ARCHIVED
    }
}
