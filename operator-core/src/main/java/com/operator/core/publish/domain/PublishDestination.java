package com.operator.core.publish.domain;

import com.operator.core.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Publish Destination Entity - represents where operators/packages are published
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Entity
@Table(name = "publish_destinations", indexes = {
    @Index(name = "idx_publish_dest_type", columnList = "destination_type"),
    @Index(name = "idx_publish_dest_enabled", columnList = "enabled")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PublishDestination extends BaseEntity {

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "destination_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private DestinationType destinationType;

    @Column(name = "endpoint", length = 500)
    private String endpoint;

    @Column(name = "credentials", columnDefinition = "TEXT")
    private String credentials; // Encrypted JSON

    @Column(name = "configuration", columnDefinition = "TEXT")
    private String configuration; // JSON configuration

    @Column(name = "enabled", nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private Boolean isDefault = false;

    @OneToMany(mappedBy = "publishDestination", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt DESC")
    private List<PublishHistory> publishHistories = new ArrayList<>();

    /**
     * Destination type enum
     */
    public enum DestinationType {
        LOCAL_FILE,
        REST_API,
        S3,
        GIT
    }
}
