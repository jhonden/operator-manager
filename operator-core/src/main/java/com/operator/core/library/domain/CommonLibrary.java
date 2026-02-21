package com.operator.core.library.domain;

import com.operator.common.enums.LibraryType;
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
 * 公共库实体类
 * 用于在算子之间共享的代码库，如常量、公共方法、公共数据模型等
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Entity
@Table(name = "common_libraries", indexes = {
    @Index(name = "idx_library_name", columnList = "name"),
    @Index(name = "idx_library_version", columnList = "version"),
    @Index(name = "idx_library_name_version", columnList = "name, version", unique = true)
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CommonLibrary extends BaseEntity {

    /**
     * 公共库名称
     */
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    /**
     * 描述
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * 版本号
     */
    @Column(name = "version", nullable = false, length = 50)
    private String version;

    /**
     * 分类
     */
    @Column(name = "category", length = 100)
    private String category;

    /**
     * 库类型
     */
    @Column(name = "library_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private LibraryType libraryType;

    /**
     * 公共库文件列表
     */
    @OneToMany(mappedBy = "library", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    @Builder.Default
    private List<CommonLibraryFile> files = new ArrayList<>();
}
