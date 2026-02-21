package com.operator.core.library.domain;

import com.operator.core.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 公共库文件实体类
 * 一个公共库可以包含多个代码文件
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Entity
@Table(name = "common_library_files", indexes = {
    @Index(name = "idx_library_file_library", columnList = "library_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CommonLibraryFile extends BaseEntity {

    /**
     * 所属公共库
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "library_id", nullable = false)
    private CommonLibrary library;

    /**
     * 文件名
     */
    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    /**
     * MinIO 存储路径
     */
    @Column(name = "file_path", length = 500)
    private String filePath;

    /**
     * 代码内容
     */
    @Column(name = "code", columnDefinition = "TEXT")
    private String code;

    /**
     * 文件顺序
     */
    @Column(name = "order_index")
    private Integer orderIndex;
}
