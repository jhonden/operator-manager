package com.operator.common.dto.library;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 算子包打包预览响应 DTO
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackagePreviewResponse {

    /**
     * 算子包名称
     */
    private String packageName;

    /**
     * 打包模板
     */
    private String template;

    /**
     * 打包结构树
     */
    private List<TreeNode> structure;

    /**
     * 冲突列表
     */
    private List<Conflict> conflicts;

    /**
     * 警告列表
     */
    private List<String> warnings;

    /**
     * 树节点
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TreeNode {
        private String type; // "directory" 或 "file"
        private String path;
        private List<TreeNode> children;
        private Source source; // 仅文件有效
    }

    /**
     * 文件来源
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Source {
        private String type; // "operator" 或 "library"
        private Long id;
        private String name;
        private String version; // 仅 library 有效
    }

    /**
     * 冲突信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Conflict {
        private String type; // "path_conflict" 或 "dependency_missing"
        private String path;
        private String message;
        private List<Source> conflictingResources;
    }
}
