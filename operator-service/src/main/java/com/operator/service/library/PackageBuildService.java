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

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;

/**
 * 算子包构建服务
 * 负责生成算子包的压缩包
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PackageBuildService {

    private final OperatorPackageRepository packageRepository;
    private final PackageOperatorRepository packageOperatorRepository;
    private final PackageCommonLibraryRepository packageCommonLibraryRepository;
    private final OperatorRepository operatorRepository;

    /**
     * 构建并下载算子包
     *
     * @param packageId 算子包ID
     * @return 算子包下载响应
     */
    @Transactional(readOnly = true)
    public PackageDownloadResponse buildPackage(Long packageId) {
        log.info("构建算子包：packageId={}", packageId);

        // 1. 获取算子包信息
        OperatorPackage pkg = packageRepository.findById(packageId)
                .orElseThrow(() -> new ResourceNotFoundException("算子包不存在"));

        // 2. 获取算子包中的算子
        List<PackageOperator> packageOperators = packageOperatorRepository
                .findByOperatorPackageIdOrderByOrderIndexAsc(packageId);

        // 3. 获取算子包中的公共库
        List<PackageCommonLibrary> packageCommonLibraries = packageCommonLibraryRepository
                .findByOperatorPackageIdWithLibrary(packageId);

        // 4. 生成元数据文件
        String metadataContent = generateMetadataFile(pkg, packageOperators);

        // 5. 构建压缩包
        byte[] zipBytes = buildZip(pkg, packageOperators, packageCommonLibraries, metadataContent);

        log.info("算子包构建完成：packageId={}, size={} bytes", packageId, zipBytes.length);

        return PackageDownloadResponse.builder()
                .packageName(pkg.getName())
                .zipBytes(zipBytes)
                .build();
    }

    /**
     * 生成元数据文件内容
     *
     * @param pkg 算子包
     * @param packageOperators 算子包-算子关联
     * @return YAML 格式的元数据文件内容
     */
    private String generateMetadataFile(OperatorPackage pkg, List<PackageOperator> packageOperators) {
        List<OperatorMetadata> operatorMetadataList = new ArrayList<>();

        for (PackageOperator po : packageOperators) {
            Operator operator = po.getOperator();
            OperatorMetadata metadata = OperatorMetadata.builder()
                    .operatorCode(operator.getOperatorCode())
                    .name(operator.getName())
                    .objectCode(operator.getObjectCode())
                    .dataFormat(operator.getDataFormat())
                    .generator(operator.getGenerator())
                    .orderNo(po.getOrderIndex())
                    .build();
            operatorMetadataList.add(metadata);
        }

        PackageMetadata packageMetadata = PackageMetadata.builder()
                .businessName(pkg.getBusinessScenario())
                .version(pkg.getVersion() != null ? pkg.getVersion() : "1.0.0")
                .operators(operatorMetadataList)
                .build();

        // 转换为 YAML 格式
        StringBuilder yaml = new StringBuilder();
        yaml.append("businessName: ").append(packageMetadata.getBusinessName()).append("\n");
        yaml.append("version: ").append(packageMetadata.getVersion()).append("\n");
        yaml.append("operators:\n");
        yaml.append("  instances:\n");

        for (OperatorMetadata om : operatorMetadataList) {
            yaml.append("    - operator_code: ").append(om.getOperatorCode()).append("\n");
            yaml.append("      name: ").append(om.getName()).append("\n");
            yaml.append("      object_code: ").append(om.getObjectCode()).append("\n");
            yaml.append("      data_format: ").append(om.getDataFormat()).append("\n");
            yaml.append("      generator: ").append(om.getGenerator()).append("\n");
            yaml.append("      order_no: ").append(om.getOrderNo()).append("\n");
        }

        log.info("生成元数据文件内容：\\n{}", yaml);
        return yaml.toString();
    }

    /**
     * 构建 ZIP 压缩包
     *
     * @param pkg 算子包
     * @param packageOperators 算子包-算子关联
     * @param packageCommonLibraries 算子包-公共库关联
     * @param metadataContent 元数据文件内容
     * @return ZIP 压缩包字节数组
     */
    private byte[] buildZip(OperatorPackage pkg,
                           List<PackageOperator> packageOperators,
                           List<PackageCommonLibrary> packageCommonLibraries,
                           String metadataContent) {

        String packageName = pkg.getName();
        String packageVersion = pkg.getVersion() != null ? pkg.getVersion() : "1.0.0";
        String rootPath = packageName + "/" + packageVersion + "/";

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos)) {

            // 添加算子代码文件
            for (PackageOperator packageOperator : packageOperators) {
                Operator operator = packageOperator.getOperator();
                String fileName = operator.getOperatorCode() + ".groovy";
                String filePath = rootPath + "operators/groovy/" + fileName;

                ZipArchiveEntry entry = new ZipArchiveEntry(filePath);
                zos.putArchiveEntry(entry);
                zos.write(operator.getCode().getBytes(StandardCharsets.UTF_8));
                zos.closeArchiveEntry();

                log.info("添加算子文件到 ZIP：{}", filePath);
            }

            // 添加公共库文件
            for (PackageCommonLibrary pcl : packageCommonLibraries) {
                CommonLibrary library = pcl.getLibrary();
                String libraryType = library.getLibraryType().name().toLowerCase();

                for (var file : library.getFiles()) {
                    String filePath;
                    String fileContent;

                    // Legacy 模板的路径规则
                    filePath = rootPath + getLegacyLibraryPath(libraryType, library.getName(), file.getFileName());
                    fileContent = file.getCode();

                    ZipArchiveEntry entry = new ZipArchiveEntry(filePath);
                    zos.putArchiveEntry(entry);
                    zos.write(fileContent.getBytes(StandardCharsets.UTF_8));
                    zos.closeArchiveEntry();

                    log.info("添加公共库文件到 ZIP：{}", filePath);
                }
            }

            // 添加元数据文件
            String metadataPath = rootPath + "operators/metainfo_operators.yml";
            ZipArchiveEntry metadataEntry = new ZipArchiveEntry(metadataPath);
            zos.putArchiveEntry(metadataEntry);
            zos.write(metadataContent.getBytes(StandardCharsets.UTF_8));
            zos.closeArchiveEntry();

            log.info("添加元数据文件到 ZIP：{}", metadataPath);

            zos.finish();
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("构建 ZIP 压缩包失败", e);
            throw new RuntimeException("构建算子包失败", e);
        }


    }

    /**
     * 获取 Legacy 模式的公共库路径
     *
     * @param libraryType 库类型
     * @param libraryName 库名称
     * @param fileName 文件名
     * @return 文件路径
     */
    private String getLegacyLibraryPath(String libraryType, String libraryName, String fileName) {
        return switch (libraryType) {
            case "constant" -> "operators/constants/" + fileName;
            case "method" -> "operators/method/" + fileName;
            case "model" -> "models/" + fileName;
            case "custom" -> "lib/" + libraryName + "/" + fileName;
            default -> "lib/" + fileName;
        };
    }
}
