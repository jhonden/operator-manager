package com.operator.service.library;

import com.operator.common.enums.LibraryType;
import com.operator.common.dto.library.LibraryPathConfigResponse;
import com.operator.common.dto.library.OperatorPathConfigResponse;
import com.operator.core.library.domain.CommonLibrary;
import com.operator.core.library.domain.PackageCommonLibrary;
import com.operator.core.operator.domain.Operator;
import com.operator.core.pkg.domain.PackageOperator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 打包路径解析器
 * 负责解析打包路径中的变量，并生成实际的文件路径
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PackagePathResolver {

    /**
     * 打包模板类型
     */
    public enum PackageTemplate {
        LEGACY,  // 兼容现有格式
        MODERN,  // 推荐新格式
        CUSTOM    // 完全自定义
    }

    /**
     * 解析算子的打包路径
     *
     * @param operator 算子
     * @param packageOperator 算子包-算子关联
     * @param template 打包模板
     * @return 实际的打包路径
     */
    public String resolveOperatorPath(Operator operator, PackageOperator packageOperator,
                                   PackageTemplate template, String fileName) {
        // 如果使用自定义路径，使用自定义路径
        if (packageOperator.getUseCustomPath() && packageOperator.getCustomPackagePath() != null) {
            return resolveVariables(packageOperator.getCustomPackagePath(),
                    buildOperatorVariables(operator, fileName));
        }

        // 使用模板推荐路径
        String templatePath = getRecommendedOperatorPath(template);
        return resolveVariables(templatePath, buildOperatorVariables(operator, fileName));
    }

    /**
     * 解析公共库的打包路径
     *
     * @param library 公共库
     * @param packageCommonLibrary 算子包-公共库关联
     * @param template 打包模板
     * @return 实际的打包路径
     */
    public String resolveLibraryPath(CommonLibrary library, PackageCommonLibrary packageCommonLibrary,
                                  PackageTemplate template, String fileName) {
        // 如果使用自定义路径，使用自定义路径
        if (packageCommonLibrary.getUseCustomPath() && packageCommonLibrary.getCustomPackagePath() != null) {
            return resolveVariables(packageCommonLibrary.getCustomPackagePath(),
                    buildLibraryVariables(library, fileName));
        }

        // 使用模板推荐路径
        String templatePath = getRecommendedLibraryPath(template, library.getLibraryType());
        return resolveVariables(templatePath, buildLibraryVariables(library, fileName));
    }

    /**
     * 获取算子的推荐路径
     */
    public String getRecommendedOperatorPath(PackageTemplate template) {
        return switch (template) {
            case LEGACY -> "operators/groovy/${operatorCode}.groovy";
            case MODERN, CUSTOM -> "operators/${operatorCode}/${fileName}";
        };
    }

    /**
     * 获取公共库的推荐路径
     */
    public String getRecommendedLibraryPath(PackageTemplate template, LibraryType libraryType) {
        return switch (template) {
            case LEGACY -> switch (libraryType) {
                case CONSTANT -> "operators/constants/${fileName}";
                case METHOD -> "operators/method/${fileName}";
                case MODEL -> "models/${fileName}";
                case CUSTOM -> "lib/${libraryName}/${fileName}";
            };
            case MODERN, CUSTOM -> "lib/${libraryName}/${fileName}";
        };
    }

    /**
     * 解析路径中的变量
     */
    public String resolveVariables(String pathTemplate, Map<String, String> variables) {
        String result = pathTemplate;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            String varName = entry.getKey();
            String varValue = entry.getValue() != null ? entry.getValue() : "";
            result = result.replace("${" + varName + "}", varValue);
        }
        return result;
    }

    /**
     * 构建算子的变量映射
     */
    private Map<String, String> buildOperatorVariables(Operator operator, String fileName) {
        Map<String, String> variables = new HashMap<>();
        variables.put("operatorCode", operator.getOperatorCode());
        variables.put("packageName", operator.getName());
        variables.put("fileName", fileName);
        if (fileName != null && fileName.contains(".")) {
            variables.put("fileExt", fileName.substring(fileName.lastIndexOf(".")));
        } else {
            variables.put("fileExt", "");
        }
        return variables;
    }

    /**
     * 构建公共库的变量映射
     */
    private Map<String, String> buildLibraryVariables(CommonLibrary library, String fileName) {
        Map<String, String> variables = new HashMap<>();
        variables.put("libraryName", library.getName());
        variables.put("libraryVersion", library.getVersion());
        variables.put("fileName", fileName);
        if (fileName != null && fileName.contains(".")) {
            variables.put("fileExt", fileName.substring(fileName.lastIndexOf(".")));
        } else {
            variables.put("fileExt", "");
        }
        return variables;
    }
}
