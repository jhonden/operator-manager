package com.operator.service.library;

import com.operator.common.dto.library.*;
import com.operator.common.exception.ResourceNotFoundException;
import com.operator.core.library.domain.CommonLibrary;
import com.operator.core.library.domain.PackageCommonLibrary;
import com.operator.core.library.repository.PackageCommonLibraryRepository;
import com.operator.core.operator.domain.Operator;
import com.operator.core.operator.repository.OperatorRepository;
import com.operator.core.pkg.domain.OperatorPackage;
import com.operator.core.pkg.domain.PackageOperator;
import com.operator.core.pkg.repository.OperatorPackageRepository;
import com.operator.core.pkg.repository.PackageOperatorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 打包预览服务
 * 负责生成算子包的打包结构预览和冲突检测
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PackagePreviewService {

    private final OperatorPackageRepository packageRepository;
    private final PackageOperatorRepository packageOperatorRepository;
    private final PackageCommonLibraryRepository packageCommonLibraryRepository;
    private final OperatorRepository operatorRepository;
    private final PackagePathResolver pathResolver;

    /**
     * 生成打包预览
     *
     * @param packageId 算子包ID
     * @param template 打包模板
     * @return 打包预览响应
     */
    @Transactional(readOnly = true)
    public PackagePreviewResponse generatePreview(Long packageId, String template) {
        log.info("生成打包预览：packageId={}, template={}", packageId, template);

        OperatorPackage operatorPackage = packageRepository.findById(packageId)
                .orElseThrow(() -> new ResourceNotFoundException("算子包不存在"));

        PackagePathResolver.PackageTemplate packageTemplate =
                PackagePathResolver.PackageTemplate.valueOf(template.toUpperCase());

        // 获取算子包中的所有算子
        List<PackageOperator> packageOperators = packageOperatorRepository
                .findByOperatorPackageIdOrderByOrderIndexAsc(packageId);

        // 获取算子包中的所有公共库
        List<PackageCommonLibrary> packageCommonLibraries = packageCommonLibraryRepository
                .findByOperatorPackageIdWithLibrary(packageId);

        // 构建打包结构树
        List<PackagePreviewResponse.TreeNode> structure = buildStructure(
                operatorPackage, packageOperators, packageCommonLibraries, packageTemplate);

        // 检测冲突
        List<PackagePreviewResponse.Conflict> conflicts = detectConflicts(structure);

        // 生成警告
        List<String> warnings = generateWarnings(operatorPackage, packageOperators, packageCommonLibraries);

        return PackagePreviewResponse.builder()
                .packageName(operatorPackage.getName())
                .template(packageTemplate.name().toLowerCase())
                .structure(structure)
                .conflicts(conflicts)
                .warnings(warnings)
                .build();
    }

    /**
     * 构建打包结构树
     */
    private List<PackagePreviewResponse.TreeNode> buildStructure(
            OperatorPackage pkg,
            List<PackageOperator> packageOperators,
            List<PackageCommonLibrary> packageCommonLibraries,
            PackagePathResolver.PackageTemplate template) {

        Map<String, PackagePreviewResponse.TreeNode> directoryMap = new HashMap<>();
        List<PackagePreviewResponse.TreeNode> rootNodes = new ArrayList<>();

        // 构建根目录：{算子包名称}/{算子包版本}
        String packageName = pkg.getName();
        String packageVersion = pkg.getVersion() != null ? pkg.getVersion() : "1.0.0";
        String rootPrefix = packageName + "/" + packageVersion + "/";

        log.info("构建打包结构树：根路径前缀={}", rootPrefix);

        // 处理算子代码
        for (PackageOperator packageOperator : packageOperators) {
            Operator operator = packageOperator.getOperator();
            String fileName = operator.getOperatorCode() + ".groovy";
            String path = pathResolver.resolveOperatorPath(operator, packageOperator, template, fileName);

            // 添加根路径前缀
            path = rootPrefix + path;

            addFileToStructure(rootNodes, directoryMap, path, PackagePreviewResponse.Source.builder()
                    .type("operator")
                    .id(operator.getId())
                    .name(operator.getName())
                    .build());
        }

        // 处理公共库文件
        for (PackageCommonLibrary pcl : packageCommonLibraries) {
            CommonLibrary library = pcl.getLibrary();
            for (var file : library.getFiles()) {
                String path = pathResolver.resolveLibraryPath(library, pcl, template, file.getFileName());

                // 添加根路径前缀
                path = rootPrefix + path;

                addFileToStructure(rootNodes, directoryMap, path, PackagePreviewResponse.Source.builder()
                        .type("library")
                        .id(library.getId())
                        .name(library.getName())
                        .version(pcl.getVersion())
                        .build());
            }
        }

        return rootNodes;
    }

    /**
     * 添加文件到结构树
     */
    private void addFileToStructure(List<PackagePreviewResponse.TreeNode> rootNodes,
                                  Map<String, PackagePreviewResponse.TreeNode> directoryMap,
                                  String path, PackagePreviewResponse.Source source) {
        String[] parts = path.split("/");
        List<PackagePreviewResponse.TreeNode> currentLevel = rootNodes;

        // 遍历路径的每个部分
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            boolean isLast = (i == parts.length - 1);

            if (isLast) {
                // 文件节点
                currentLevel.add(PackagePreviewResponse.TreeNode.builder()
                        .type("file")
                        .path(path)
                        .source(source)
                        .build());
            } else {
                // 目录节点
                PackagePreviewResponse.TreeNode directory = directoryMap.get(part);
                if (directory == null) {
                    List<String> pathParts = new java.util.ArrayList<>();
                    for (int j = 0; j <= i; j++) {
                        pathParts.add(parts[j]);
                    }
                    directory = PackagePreviewResponse.TreeNode.builder()
                            .type("directory")
                            .path(String.join("/", pathParts))
                            .children(new ArrayList<>())
                            .build();
                    currentLevel.add(directory);
                    directoryMap.put(part, directory);
                }
                currentLevel = directory.getChildren();
            }
        }
    }

    /**
     * 检测冲突
     */
    private List<PackagePreviewResponse.Conflict> detectConflicts(
            List<PackagePreviewResponse.TreeNode> structure) {

        List<PackagePreviewResponse.Conflict> conflicts = new ArrayList<>();
        Map<String, List<PackagePreviewResponse.Source>> pathMap = new HashMap<>();

        // 收集所有文件路径
        collectFilePaths(structure, pathMap);

        // 检查路径冲突
        for (Map.Entry<String, List<PackagePreviewResponse.Source>> entry : pathMap.entrySet()) {
            String path = entry.getKey();
            List<PackagePreviewResponse.Source> sources = entry.getValue();

            if (sources.size() > 1) {
                conflicts.add(PackagePreviewResponse.Conflict.builder()
                        .type("path_conflict")
                        .path(path)
                        .message("路径冲突：多个资源映射到同一路径")
                        .conflictingResources(sources)
                        .build());
            }
        }

        return conflicts;
    }

    /**
     * 收集所有文件路径
     */
    private void collectFilePaths(List<PackagePreviewResponse.TreeNode> nodes,
                                 Map<String, List<PackagePreviewResponse.Source>> pathMap) {
        for (PackagePreviewResponse.TreeNode node : nodes) {
            if ("file".equals(node.getType())) {
                String path = node.getPath();
                PackagePreviewResponse.Source source = node.getSource();

                pathMap.computeIfAbsent(path, k -> new ArrayList<>()).add(source);
            } else if (node.getChildren() != null) {
                collectFilePaths(node.getChildren(), pathMap);
            }
        }
    }

    /**
     * 生成警告
     */
    private List<String> generateWarnings(OperatorPackage pkg,
                                         List<PackageOperator> packageOperators,
                                         List<PackageCommonLibrary> packageCommonLibraries) {
        List<String> warnings = new ArrayList<>();

        // 检查空包
        if (packageOperators.isEmpty()) {
            warnings.add("算子包未添加任何算子");
        }

        // 检查依赖缺失（可选功能，暂未实现）

        return warnings;
    }
}
