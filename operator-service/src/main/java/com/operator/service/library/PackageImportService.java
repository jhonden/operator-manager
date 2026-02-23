package com.operator.service.library;

import com.operator.common.dto.pkg.PackageImportMetadata;
import com.operator.common.dto.pkg.PackageImportResponse;
import com.operator.common.enums.LanguageType;
import com.operator.common.enums.LibraryType;
import com.operator.common.exception.BadRequestException;
import com.operator.core.library.domain.CommonLibrary;
import com.operator.core.library.domain.CommonLibraryFile;
import com.operator.core.library.domain.OperatorCommonLibrary;
import com.operator.core.library.domain.PackageCommonLibrary;
import com.operator.core.library.repository.CommonLibraryFileRepository;
import com.operator.core.library.repository.CommonLibraryRepository;
import com.operator.core.library.repository.OperatorCommonLibraryRepository;
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
import org.yaml.snakeyaml.Yaml;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;

/**
 * 算子包导入服务
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PackageImportService {

    private final OperatorPackageRepository packageRepository;
    private final PackageOperatorRepository packageOperatorRepository;
    private final PackageCommonLibraryRepository packageCommonLibraryRepository;
    private final OperatorCommonLibraryRepository operatorCommonLibraryRepository;
    private final OperatorRepository operatorRepository;
    private final CommonLibraryRepository commonLibraryRepository;
    private final CommonLibraryFileRepository commonLibraryFileRepository;

    /**
     * 导入算子包
     *
     * @param zipBytes ZIP 压缩包字节数组
     * @param originalFileName 原始文件名
     * @param username 导入用户
     * @return 导入结果
     */
    @Transactional
    public PackageImportResponse importPackage(byte[] zipBytes, String originalFileName, String username) {
        log.info("开始导入算子包：fileName={}, user={}", originalFileName, username);

        // 创建临时文件
        File tempFile = null;
        try {
            tempFile = File.createTempFile("import_package", ".zip");
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(zipBytes);
            }

            try (ZipFile zipFile = new ZipFile(tempFile)) {
                // 1. 解析元数据文件
                PackageImportMetadata metadata = parseMetadata(zipFile);

                // 2. 提取包名和版本号（从 ZIP 根目录结构）
                String packageName = extractPackageName(zipFile);
                String packageVersion = metadata.getVersion() != null ? metadata.getVersion() : "1.0.0";

                // 3. 处理算子包名称冲突
                String finalPackageName = handlePackageNameConflict(packageName);

                // 4. 提取公共库文件
                Map<String, LibraryContent> libraryContents = extractLibraryFiles(zipFile);

                // 5. 提取算子代码文件
                Map<String, String> operatorCodes = extractOperatorCodes(zipFile);

                // 6. 验证数据完整性
                validateDataIntegrity(metadata, libraryContents, operatorCodes);

                // 7. 处理公共库（查询/更新/创建）
                ImportStatistics stats = new ImportStatistics();
                Map<String, CommonLibrary> libraryMap = processLibraries(libraryContents, username, stats);

                // 8. 处理算子（查询/更新/创建）
                Map<String, Operator> operatorMap = processOperators(metadata, operatorCodes, libraryMap, username, stats);

                // 9. 创建算子包
                OperatorPackage pkg = createPackage(finalPackageName, metadata.getBusinessName(), packageVersion, username, stats);

                // 10. 建立算子包-算子关联
                createPackageOperators(pkg, metadata.getOperators().getInstances(), operatorMap, stats);

                // 11. 自动同步公共库到算子包
                syncLibrariesToPackage(pkg, libraryMap, stats);

                log.info("算子包导入成功：packageName={}, operatorsUpdated={}, operatorsCreated={}, librariesUpdated={}, librariesCreated={}",
                        finalPackageName, stats.operatorsUpdated, stats.operatorsCreated,
                        stats.librariesUpdated, stats.librariesCreated);

                return PackageImportResponse.builder()
                        .id(pkg.getId())
                        .name(finalPackageName)
                        .operatorsUpdated(stats.operatorsUpdated)
                        .operatorsCreated(stats.operatorsCreated)
                        .librariesUpdated(stats.librariesUpdated)
                        .librariesCreated(stats.librariesCreated)
                        .build();

            }
        } catch (IOException e) {
            log.error("解析 ZIP 文件失败", e);
            throw new BadRequestException("解析 ZIP 文件失败：" + e.getMessage());
        } finally {
            // 删除临时文件
            if (tempFile != null && tempFile.exists()) {
                try {
                    Files.delete(tempFile.toPath());
                } catch (IOException e) {
                    log.warn("删除临时文件失败", e);
                }
            }
        }
    }

    /**
     * 解析元数据文件
     */
    private PackageImportMetadata parseMetadata(ZipFile zipFile) throws IOException {
        // 查找元数据文件
        ZipArchiveEntry metadataEntry = findMetadataEntry(zipFile);
        if (metadataEntry == null) {
            throw new BadRequestException("导入包缺少元数据文件 metainfo_operators.yml");
        }

        // 读取并解析 YAML
        String yamlContent = new String(zipFile.getInputStream(metadataEntry).readAllBytes(), StandardCharsets.UTF_8);
        Yaml yaml = new Yaml();

        Map<String, Object> data = yaml.load(yamlContent);

        // 手动构建 PackageImportMetadata
        PackageImportMetadata metadata = new PackageImportMetadata();
        metadata.setBusinessName((String) data.get("businessName"));
        metadata.setVersion((String) data.get("version"));

        // 解析 operators
        Map<String, Object> operatorsData = (Map<String, Object>) data.get("operators");
        if (operatorsData != null && operatorsData.containsKey("instances")) {
            List<Map<String, Object>> instances = (List<Map<String, Object>>) operatorsData.get("instances");

            PackageImportMetadata.Operators operators = new PackageImportMetadata.Operators();
            List<PackageImportMetadata.OperatorMetadata> operatorList = new ArrayList<>();

            for (Map<String, Object> instance : instances) {
                PackageImportMetadata.OperatorMetadata om = new PackageImportMetadata.OperatorMetadata();
                om.setOperator_code(convertToString(instance.get("operator_code")));
                om.setName(convertToString(instance.get("name")));
                om.setObject_code(convertToString(instance.get("object_code")));
                om.setData_format(convertToString(instance.get("data_format")));
                om.setGenerator(convertToString(instance.get("generator")));
                om.setOrder_no(instance.get("order_no") != null ? ((Number) instance.get("order_no")).intValue() : 1);
                operatorList.add(om);
            }

            operators.setInstances(operatorList);
            metadata.setOperators(operators);
        }

        log.info("解析元数据文件成功：businessName={}, version={}, operatorsCount={}",
                metadata.getBusinessName(), metadata.getVersion(),
                metadata.getOperators() != null ? metadata.getOperators().getInstances().size() : 0);

        return metadata;
    }

    /**
     * 查找元数据文件
     */
    private ZipArchiveEntry findMetadataEntry(ZipFile zipFile) {
        for (ZipArchiveEntry entry : Collections.list(zipFile.getEntries())) {
            if (entry.getName().endsWith("metainfo_operators.yml")) {
                return entry;
            }
        }
        return null;
    }

    /**
     * 从 ZIP 根目录结构提取包名
     */
    private String extractPackageName(ZipFile zipFile) {
        for (ZipArchiveEntry entry : Collections.list(zipFile.getEntries())) {
            String path = entry.getName();
            if (path.contains("/")) {
                String firstPart = path.split("/")[0];
                if (!firstPart.isEmpty()) {
                    return firstPart;
                }
            }
        }
        return "imported_package";
    }

    /**
     * 处理算子包名称冲突
     */
    private String handlePackageNameConflict(final String originalName) {
        final String finalOriginalName = originalName;
        String name = originalName;
        int counter = 1;

        // 检查现有包名列表
        while (checkPackageNameExists(name)) {
            name = finalOriginalName + "_" + counter++;
        }

        if (!name.equals(finalOriginalName)) {
            log.info("算子包名称冲突，自动重命名为：{} -> {}", finalOriginalName, name);
        }

        return name;
    }

    /**
     * 检查算子包名称是否存在
     */
    private boolean checkPackageNameExists(String name) {
        return packageRepository.findAll().stream().anyMatch(p -> p.getName().equals(name));
    }

    /**
     * 提取公共库文件
     */
    private Map<String, LibraryContent> extractLibraryFiles(ZipFile zipFile) throws IOException {
        Map<String, LibraryContent> libraryContents = new HashMap<>();

        for (ZipArchiveEntry entry : Collections.list(zipFile.getEntries())) {
            String path = entry.getName();

            // 跳过目录和元数据文件
            if (entry.isDirectory() || path.endsWith("metainfo_operators.yml")) {
                continue;
            }

            // 解析库类型和库名
            LibraryInfo libInfo = parseLibraryInfo(path);
            if (libInfo != null) {
                String key = libInfo.libraryName;

                LibraryContent content = libraryContents.computeIfAbsent(key, k -> new LibraryContent());
                content.libraryName = libInfo.libraryName;
                content.libraryType = libInfo.libraryType;

                // 读取文件内容
                String fileContent = new String(zipFile.getInputStream(entry).readAllBytes(), StandardCharsets.UTF_8);
                content.files.add(new LibraryFile(libInfo.fileName, fileContent));

                log.info("提取公共库文件：library={}, type={}, file={}", libInfo.libraryName, libInfo.libraryType, libInfo.fileName);
            }
        }

        return libraryContents;
    }

    /**
     * 解析库信息
     */
    private LibraryInfo parseLibraryInfo(String path) {
        // 去掉根路径前缀（如 "my_package/1.0.0/"）
        String relativePath = path;
        int firstSlash = path.indexOf('/');
        if (firstSlash > 0) {
            int secondSlash = path.indexOf('/', firstSlash + 1);
            if (secondSlash > 0) {
                relativePath = path.substring(secondSlash + 1);
            }
        }

        // Legacy 模板路径解析
        if (relativePath.startsWith("operators/constants/")) {
            // constants 文件夹下的所有文件合并为一个库，库名为 "constants"
            return new LibraryInfo("constants", LibraryType.CONSTANT, extractFileName(relativePath));
        } else if (relativePath.startsWith("operators/method/")) {
            // method 文件夹下的所有文件合并为一个库，库名为 "method"
            return new LibraryInfo("method", LibraryType.METHOD, extractFileName(relativePath));
        } else if (relativePath.startsWith("models/")) {
            // models 文件夹下的所有文件合并为一个库，库名为 "models"
            return new LibraryInfo("models", LibraryType.MODEL, extractFileName(relativePath));
        } else if (relativePath.startsWith("lib/")) {
            // 自定义库路径：lib/libraryName/fileName
            String[] parts = relativePath.split("/");
            if (parts.length >= 3) {
                return new LibraryInfo(parts[1], LibraryType.CUSTOM, parts[2]);
            }
        }

        return null;
    }

    /**
     * 提取库名（从文件名推断）
     */
    private String extractLibraryName(String path) {
        String fileName = extractFileName(path);
        // 从文件名推断库名（如 DateUtils.groovy -> DateUtils）
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) {
            return fileName.substring(0, dotIndex);
        }
        return fileName;
    }

    /**
     * 提取文件名
     */
    private String extractFileName(String path) {
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash >= 0) {
            return path.substring(lastSlash + 1);
        }
        return path;
    }

    /**
     * 提取算子代码文件
     */
    private Map<String, String> extractOperatorCodes(ZipFile zipFile) throws IOException {
        Map<String, String> operatorCodes = new HashMap<>();

        for (ZipArchiveEntry entry : Collections.list(zipFile.getEntries())) {
            String path = entry.getName();

            // 匹配算子代码文件路径：operators/groovy/{operatorCode}.groovy
            if (path.matches(".*/operators/groovy/[^/]+\\.groovy$")) {
                String operatorCode = extractFileName(path);
                operatorCode = operatorCode.substring(0, operatorCode.lastIndexOf('.'));

                String code = new String(zipFile.getInputStream(entry).readAllBytes(), StandardCharsets.UTF_8);
                operatorCodes.put(operatorCode, code);

                log.info("提取算子代码文件：operatorCode={}", operatorCode);
            }
        }

        return operatorCodes;
    }

    /**
     * 验证数据完整性
     */
    private void validateDataIntegrity(PackageImportMetadata metadata,
                                       Map<String, LibraryContent> libraryContents,
                                       Map<String, String> operatorCodes) {

        // 验证元数据
        if (metadata.getBusinessName() == null || metadata.getBusinessName().isEmpty()) {
            throw new BadRequestException("元数据文件缺少 businessName 字段");
        }

        if (metadata.getOperators() == null || metadata.getOperators().getInstances() == null ||
                metadata.getOperators().getInstances().isEmpty()) {
            throw new BadRequestException("元数据文件缺少 operators 或 operators.instances 字段");
        }

        // 验证算子代码文件
        for (PackageImportMetadata.OperatorMetadata om : metadata.getOperators().getInstances()) {
            if (om.getOperator_code() == null || om.getOperator_code().isEmpty()) {
                throw new BadRequestException("元数据中的算子缺少 operator_code 字段");
            }

            if (!operatorCodes.containsKey(om.getOperator_code())) {
                throw new BadRequestException("缺少算子代码文件：" + om.getOperator_code() + ".groovy");
            }
        }

        // 验证公共库文件
        for (LibraryContent content : libraryContents.values()) {
            if (content.files.isEmpty()) {
                throw new BadRequestException("公共库 " + content.libraryName + " 没有文件");
            }

            for (LibraryFile file : content.files) {
                if (file.content == null || file.content.isEmpty()) {
                    throw new BadRequestException("公共库 " + content.libraryName + " 的文件 " + file.fileName + " 内容为空");
                }
            }
        }

        log.info("数据完整性验证通过");
    }

    /**
     * 处理公共库
     */
    private Map<String, CommonLibrary> processLibraries(Map<String, LibraryContent> libraryContents,
                                                      String username,
                                                      ImportStatistics stats) {
        Map<String, CommonLibrary> libraryMap = new HashMap<>();

        for (LibraryContent content : libraryContents.values()) {
            // 查询现有公共库（按名称查询，不区分版本）
            java.util.Optional<CommonLibrary> libraryOpt = commonLibraryRepository
                    .findByName(content.libraryName);

            CommonLibrary library;
            if (libraryOpt.isPresent()) {
                // 复用现有公共库，替换所有代码文件
                library = libraryOpt.get();
                log.info("复用现有公共库：libraryName={}, version={}",
                         content.libraryName, library.getVersion());

                // 如果现有公共库的版本号为空，设置为默认值 "1.0"
                if (library.getVersion() == null) {
                    library.setVersion("1.0");
                    commonLibraryRepository.save(library);
                    log.info("更新公共库版本号：libraryName={}, version=1.0", content.libraryName);
                }

                // 删除所有旧文件
                commonLibraryFileRepository.deleteByLibraryId(library.getId());

                // 添加新文件
                List<CommonLibraryFile> files = new ArrayList<>();
                for (LibraryFile file : content.files) {
                    CommonLibraryFile libraryFile = CommonLibraryFile.builder()
                            .library(library)
                            .fileName(file.fileName)
                            .code(file.content)
                            .orderIndex(1)
                            .build();
                    files.add(libraryFile);
                }
                commonLibraryFileRepository.saveAll(files);

                stats.librariesUpdated++;

            } else {
                // 创建新公共库
                log.info("创建新公共库：libraryName={}, type={}", content.libraryName, content.libraryType);

                library = CommonLibrary.builder()
                        .name(content.libraryName)
                        .description("从算子包导入")
                        .version("1.0")
                        .libraryType(content.libraryType)
                        .build();

                library = commonLibraryRepository.save(library);
                library.setCreatedBy(username);

                // 添加文件
                List<CommonLibraryFile> files = new ArrayList<>();
                int orderIndex = 1;
                for (LibraryFile file : content.files) {
                    CommonLibraryFile libraryFile = CommonLibraryFile.builder()
                            .library(library)
                            .fileName(file.fileName)
                            .code(file.content)
                            .orderIndex(orderIndex++)
                            .build();
                    files.add(libraryFile);
                }
                commonLibraryFileRepository.saveAll(files);

                stats.librariesCreated++;
            }

            libraryMap.put(content.libraryName, library);
        }

        return libraryMap;
    }

    /**
     * 处理算子
     */
    private Map<String, Operator> processOperators(PackageImportMetadata metadata,
                                                   Map<String, String> operatorCodes,
                                                   Map<String, CommonLibrary> libraryMap,
                                                   String username,
                                                   ImportStatistics stats) {
        Map<String, Operator> operatorMap = new HashMap<>();

        for (PackageImportMetadata.OperatorMetadata om : metadata.getOperators().getInstances()) {
            String operatorCode = om.getOperator_code();
            String code = operatorCodes.get(operatorCode);

            // 查询现有算子
            java.util.Optional<Operator> operatorOpt = operatorRepository.findByOperatorCode(operatorCode);

            Operator operator;
            if (operatorOpt.isPresent()) {
                // 复用现有算子，更新基本信息和代码
                operator = operatorOpt.get();
                log.info("复用现有算子：operatorCode={}", operatorCode);

                operator.setName(om.getName());
                operator.setObjectCode(om.getObject_code());
                if (om.getData_format() != null) {
                    operator.setDataFormat(om.getData_format());
                }
                if (om.getGenerator() != null) {
                    operator.setGenerator(om.getGenerator());
                }
                operator.setCode(code);

                operator = operatorRepository.save(operator);
                stats.operatorsUpdated++;

            } else {
                // 创建新算子
                log.info("创建新算子：operatorCode={}", operatorCode);

                operator = Operator.builder()
                        .name(om.getName())
                        .operatorCode(operatorCode)
                        .objectCode(om.getObject_code())
                        .dataFormat(om.getData_format())
                        .generator(om.getGenerator())
                        .language(LanguageType.GROOVY)
                        .description("从算子包导入")
                        .fileName(operatorCode + ".groovy")
                        .code(code)
                        .fileSize((long) code.getBytes(StandardCharsets.UTF_8).length)
                        .build();

                operator = operatorRepository.save(operator);
                operator.setCreatedBy(username);

                stats.operatorsCreated++;
            }

            operatorMap.put(operatorCode, operator);
        }

        return operatorMap;
    }

    /**
     * 创建算子包
     */
    private OperatorPackage createPackage(String packageName, String businessScenario, String version, String username, ImportStatistics stats) {
        OperatorPackage pkg = OperatorPackage.builder()
                .name(packageName)
                .businessScenario(businessScenario)
                .version(version)
                .status(com.operator.core.pkg.domain.OperatorPackage.PackageStatus.DRAFT)
                .isPublic(false)
                .downloadsCount(0)
                .featured(false)
                .operatorCount(stats.operatorsCreated + stats.operatorsUpdated)
                .build();

        pkg = packageRepository.save(pkg);
        pkg.setCreatedBy(username);
        log.info("创建算子包成功：packageName={}", packageName);

        return pkg;
    }

    /**
     * 创建算子包-算子关联
     */
    private void createPackageOperators(OperatorPackage pkg,
                                       List<PackageImportMetadata.OperatorMetadata> operatorMetadatas,
                                       Map<String, Operator> operatorMap,
                                       ImportStatistics stats) {
        List<PackageOperator> packageOperators = new ArrayList<>();

        for (PackageImportMetadata.OperatorMetadata om : operatorMetadatas) {
            Operator operator = operatorMap.get(om.getOperator_code());
            if (operator != null) {
                PackageOperator po = PackageOperator.builder()
                        .operatorPackage(pkg)
                        .operator(operator)
                        .orderIndex(om.getOrder_no() != null ? om.getOrder_no() : 1)
                        .enabled(true)
                        .build();
                packageOperators.add(po);
            }
        }

        packageOperatorRepository.saveAll(packageOperators);
        log.info("创建算子包-算子关联成功：count={}", packageOperators.size());
    }

    /**
     * 自动同步公共库到算子包
     */
    private void syncLibrariesToPackage(OperatorPackage pkg,
                                       Map<String, CommonLibrary> libraryMap,
                                       ImportStatistics stats) {
        List<OperatorCommonLibrary> operatorCommonLibraries = new ArrayList<>();
        List<PackageCommonLibrary> packageCommonLibraries = new ArrayList<>();

        // 获取算子包的算子关联
        List<PackageOperator> packageOperators = packageOperatorRepository
                .findByOperatorPackageIdOrderByOrderIndexAsc(pkg.getId());

        // 先处理包级别关联（每个库只创建一条记录，去重）
        int orderIndex = 0;
        for (CommonLibrary library : libraryMap.values()) {
            boolean exists = packageCommonLibraryRepository
                    .existsByOperatorPackageIdAndLibraryId(pkg.getId(), library.getId());
            if (!exists) {
                PackageCommonLibrary pcl = PackageCommonLibrary.builder()
                        .operatorPackage(pkg)
                        .operator(packageOperators.get(0).getOperator()) // 使用第一个算子作为来源
                        .library(library)
                        .version(library.getVersion())
                        .orderIndex(orderIndex++)
                        .useCustomPath(false)
                        .build();
                packageCommonLibraries.add(pcl);
            }
        }

        // 再处理算子级别关联（每个算子都需要创建记录）
        for (PackageOperator po : packageOperators) {
            for (CommonLibrary library : libraryMap.values()) {
                OperatorCommonLibrary ocl = OperatorCommonLibrary.builder()
                        .operator(po.getOperator())
                        .library(library)
                        .build();
                operatorCommonLibraries.add(ocl);
            }
        }

        operatorCommonLibraryRepository.saveAll(operatorCommonLibraries);
        packageCommonLibraryRepository.saveAll(packageCommonLibraries);
        log.info("同步公共库到算子包成功：operatorLevel={}, packageLevel={}",
                operatorCommonLibraries.size(), packageCommonLibraries.size());
    }

    // ========== 内部类 ==========

    /**
     * 导入统计
     */
    private static class ImportStatistics {
        int operatorsCreated = 0;
        int operatorsUpdated = 0;
        int librariesCreated = 0;
        int librariesUpdated = 0;
    }

    /**
     * 库内容
     */
    private static class LibraryContent {
        String libraryName;
        LibraryType libraryType;
        java.util.List<LibraryFile> files = new ArrayList<>();
    }

    /**
     * 库文件
     */
    private static class LibraryFile {
        final String fileName;
        final String content;

        LibraryFile(String fileName, String content) {
            this.fileName = fileName;
            this.content = content;
        }
    }

    /**
     * 库信息
     */
    private static class LibraryInfo {
        final String libraryName;
        final LibraryType libraryType;
        final String fileName;

        LibraryInfo(String libraryName, LibraryType libraryType, String fileName) {
            this.libraryName = libraryName;
            this.libraryType = libraryType;
            this.fileName = fileName;
        }
    }

    /**
     * 安全地将 Object 转换为 String
     * 支持 Integer 等数字类型转换为 String
     */
    private String convertToString(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            return (String) value;
        }
        return String.valueOf(value);
    }
}
