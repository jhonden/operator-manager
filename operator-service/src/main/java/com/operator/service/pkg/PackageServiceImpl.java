package com.operator.service.pkg;

import com.operator.common.dto.library.*;
import com.operator.common.dto.pkg.*;
import com.operator.common.enums.LibraryType;
import com.operator.common.enums.PackageStatus;
import com.operator.common.enums.LanguageType;
import com.operator.common.exception.ResourceNotFoundException;
import com.operator.common.utils.PageResponse;
import com.operator.core.library.domain.PackageCommonLibrary;
import com.operator.core.library.repository.CommonLibraryRepository;
import com.operator.core.library.repository.PackageCommonLibraryRepository;
import com.operator.core.library.domain.OperatorCommonLibrary;
import com.operator.core.library.repository.OperatorCommonLibraryRepository;
import com.operator.core.operator.domain.Operator;
import com.operator.core.operator.repository.OperatorRepository;
import com.operator.core.pkg.domain.OperatorPackage;
import com.operator.core.pkg.domain.PackageOperator;
import com.operator.core.pkg.repository.OperatorPackageRepository;
import com.operator.core.pkg.repository.PackageOperatorRepository;
import com.operator.service.library.PackagePathResolver;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Operator Package Service Implementation (Stub)
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PackageServiceImpl implements PackageService {

    private final OperatorPackageRepository packageRepository;
    private final PackageOperatorRepository packageOperatorRepository;
    private final OperatorRepository operatorRepository;
    private final OperatorCommonLibraryRepository operatorCommonLibraryRepository;
    private final PackageCommonLibraryRepository packageCommonLibraryRepository;
    private final CommonLibraryRepository commonLibraryRepository;
    private final PackagePathResolver pathResolver;

    @Override
    @Transactional
    public PackageResponse createPackage(PackageRequest request, String username) {
        log.info("Creating package: {} by user: {}", request.getName(), username);

        OperatorPackage pkg = new OperatorPackage();
        pkg.setName(request.getName());
        pkg.setDescription(request.getDescription());
        pkg.setBusinessScenario(request.getBusinessScenario());
        pkg.setStatus(convertToEntityPackageStatus(request.getStatus() != null ? request.getStatus() : PackageStatus.DRAFT));
        pkg.setIcon(request.getIcon());
        pkg.setVersion(request.getVersion() != null ? request.getVersion() : "1.0.0");
        pkg.setIsPublic(request.getIsPublic() != null ? request.getIsPublic() : false);
        pkg.setOperatorCount(0);
        pkg.setCreatedBy(username);

        pkg = packageRepository.save(pkg);

        log.info("Package created with ID: {}", pkg.getId());
        return mapToResponse(pkg);
    }

    @Override
    @Transactional
    public PackageResponse updatePackage(Long id, PackageRequest request, String username) {
        log.info("Updating package: {} by user: {}", id, username);

        OperatorPackage pkg = packageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Package", id));

        pkg.setName(request.getName());
        pkg.setDescription(request.getDescription());
        pkg.setBusinessScenario(request.getBusinessScenario());
        if (request.getStatus() != null) {
            pkg.setStatus(convertToEntityPackageStatus(request.getStatus()));
        }
        pkg.setIcon(request.getIcon());
        if (request.getVersion() != null && !request.getVersion().trim().isEmpty()) {
            pkg.setVersion(request.getVersion());
        }
        pkg.setIsPublic(request.getIsPublic() != null ? request.getIsPublic() : pkg.getIsPublic());
        pkg.setUpdatedBy(username);

        pkg = packageRepository.save(pkg);

        return mapToResponse(pkg);
    }

    @Override
    @Transactional
    public void deletePackage(Long id, String username) {
        log.info("Deleting package: {} by user: {}", id, username);

        OperatorPackage pkg = packageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Package", id));

        packageRepository.delete(pkg);
    }

    @Override
    public PackageResponse getPackageById(Long id) {
        OperatorPackage pkg = packageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Package", id));
        return mapToResponse(pkg);
    }

    @Override
    public PageResponse<PackageResponse> getAllPackages(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<OperatorPackage> packages = packageRepository.findAll(pageable);
        return PageResponse.of(packages.map(this::mapToResponse));
    }

    @Override
    public PageResponse<PackageResponse> searchPackages(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<OperatorPackage> packages = packageRepository.searchPackages(keyword, pageable);
        return PageResponse.of(packages.map(this::mapToResponse));
    }

    @Override
    public List<PackageResponse> getPackagesByCreator(String username) {
        return packageRepository.findByCreatedBy(username).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PackageOperatorResponse addOperator(Long packageId, PackageOperatorRequest request, String username) {
        log.info("Adding operator {} to package: {}", request.getOperatorId(), packageId);

        OperatorPackage pkg = packageRepository.findById(packageId)
                .orElseThrow(() -> new ResourceNotFoundException("Package", packageId));

        // Check if operator already exists in package
        packageOperatorRepository.findByOperatorPackageIdAndOperatorId(packageId, request.getOperatorId())
                .ifPresent(po -> {
                    throw new IllegalArgumentException("Operator already exists in package");
                });

        // Load actual operator from database
        Operator operator = operatorRepository.findById(request.getOperatorId())
                .orElseThrow(() -> new ResourceNotFoundException("Operator", request.getOperatorId()));

        PackageOperator packageOperator = PackageOperator.builder()
                .operatorPackage(pkg)
                .operator(operator)
                .version(request.getVersion() != null ? request.getVersion() : (operator.getVersion() != null ? "1.0.0" : operator.getVersion()))
                .orderIndex(request.getOrderIndex())
                .parameterMapping(request.getParameterMapping())
                .enabled(request.getEnabled())
                .notes(request.getNotes())
                .build();

        packageOperator.setCreatedBy(username);

        packageOperator = packageOperatorRepository.save(packageOperator);

        // Update operator count
        pkg.setOperatorCount((int) packageOperatorRepository.countByOperatorPackageId(packageId));
        packageRepository.save(pkg);

        // 同步算子的公共库到算子包
        try {
            syncOperatorLibrariesToPackage(packageId, request.getOperatorId(), "system");
        } catch (Exception e) {
            log.error("同步公共库到算子包失败：packageId={}, operatorId={}",
                packageId, request.getOperatorId(), e);
        }

        return mapPackageOperatorToResponse(packageOperator);
    }

    @Override
    @Transactional
    public PackageOperatorResponse updatePackageOperator(Long packageId, Long packageOperatorId,
                                                         PackageOperatorRequest request, String username) {
        PackageOperator packageOperator = packageOperatorRepository.findById(packageOperatorId)
                .orElseThrow(() -> new ResourceNotFoundException("PackageOperator", packageOperatorId));

        if (request.getVersion() != null) {
            packageOperator.setVersion(request.getVersion());
        }
        packageOperator.setOrderIndex(request.getOrderIndex());
        packageOperator.setParameterMapping(request.getParameterMapping());
        packageOperator.setEnabled(request.getEnabled());
        packageOperator.setNotes(request.getNotes());
        packageOperator.setUpdatedBy(username);

        packageOperator = packageOperatorRepository.save(packageOperator);

        return mapPackageOperatorToResponse(packageOperator);
    }

    @Override
    @Transactional
    public void removeOperator(Long packageId, Long packageOperatorId, String username) {
        PackageOperator packageOperator = packageOperatorRepository.findById(packageOperatorId)
                .orElseThrow(() -> new ResourceNotFoundException("PackageOperator", packageOperatorId));

        packageOperatorRepository.delete(packageOperator);

        // Update operator count
        OperatorPackage pkg = packageRepository.findById(packageId)
                .orElseThrow(() -> new ResourceNotFoundException("Package", packageId));
        pkg.setOperatorCount((int) packageOperatorRepository.countByOperatorPackageId(packageId));
        packageRepository.save(pkg);
    }

    @Override
    @Transactional
    public void reorderOperators(Long packageId, ReorderOperatorsRequest request, String username) {
        log.info("Reordering operators in package: {}", packageId);

        for (ReorderOperatorsRequest.OperatorOrderItem item : request.getOperators()) {
            PackageOperator packageOperator = packageOperatorRepository.findById(item.getPackageOperatorId())
                    .orElseThrow(() -> new ResourceNotFoundException("PackageOperator", item.getPackageOperatorId()));

            packageOperator.setOrderIndex(item.getOrderIndex());
            packageOperatorRepository.save(packageOperator);
        }
    }

    @Override
    public List<PackageOperatorResponse> getPackageOperators(Long packageId) {
        return packageOperatorRepository.findByOperatorPackageIdOrderByOrderIndexAsc(packageId).stream()
                .map(this::mapPackageOperatorToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PackageResponse updatePackageStatus(Long id, String status, String username) {
        OperatorPackage pkg = packageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Package", id));

        pkg.setStatus(convertToEntityPackageStatus(PackageStatus.valueOf(status)));
        pkg.setUpdatedBy(username);
        pkg = packageRepository.save(pkg);

        return mapToResponse(pkg);
    }

    @Override
    @Transactional
    public PackageResponse toggleFeatured(Long id, String username) {
        OperatorPackage pkg = packageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Package", id));

        pkg.setFeatured(!pkg.getFeatured());
        pkg.setUpdatedBy(username);
        pkg = packageRepository.save(pkg);

        return mapToResponse(pkg);
    }

    @Override
    @Transactional
    public void incrementDownloadCount(Long id) {
        OperatorPackage pkg = packageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Package", id));

        pkg.setDownloadsCount(pkg.getDownloadsCount() + 1);
        packageRepository.save(pkg);
    }

    private PackageResponse mapToResponse(OperatorPackage pkg) {
        PackageResponse response = new PackageResponse();
        response.setId(pkg.getId());
        response.setName(pkg.getName());
        response.setDescription(pkg.getDescription());
        response.setBusinessScenario(pkg.getBusinessScenario());
        response.setStatus(pkg.getStatus() != null ? pkg.getStatus().name() : null);
        response.setVersion(pkg.getVersion());
        response.setPackageTemplate(pkg.getPackageTemplate());
        response.setOperators(loadPackageOperators(pkg.getId()));
        response.setCommonLibraries(loadPackageCommonLibraries(pkg.getId()));
        return response;
    }

    private List<PackageOperatorResponse> loadPackageOperators(Long packageId) {
        return packageOperatorRepository.findByOperatorPackageIdOrderByOrderIndexAscWithFetch(packageId).stream()
                .map(this::mapPackageOperatorToResponse)
                .collect(Collectors.toList());
    }

    private List<LibraryPathConfigResponse> loadPackageCommonLibraries(Long packageId) {
        PackagePathResolver.PackageTemplate packageTemplate = PackagePathResolver.PackageTemplate.LEGACY;
        OperatorPackage pkg = packageRepository.findById(packageId).orElse(null);
        if (pkg != null && pkg.getPackageTemplate() != null) {
            packageTemplate = PackagePathResolver.PackageTemplate.valueOf(pkg.getPackageTemplate().toUpperCase());
        }

        final PackagePathResolver.PackageTemplate template = packageTemplate;
        return packageCommonLibraryRepository.findByOperatorPackageIdWithLibrary(packageId).stream()
                .map(pcl -> convertToLibraryPathConfig(pcl, template))
                .collect(Collectors.toList());
    }

    private PackageOperatorResponse mapPackageOperatorToResponse(PackageOperator po) {
        return PackageOperatorResponse.builder()
                .id(po.getId())
                .operatorId(po.getOperator() != null ? po.getOperator().getId() : null)
                .operatorName(po.getOperator() != null ? po.getOperator().getName() : null)
                .operatorLanguage(po.getOperator() != null && po.getOperator().getLanguage() != null
                        ? convertToDtoLanguageType(po.getOperator().getLanguage()) : null)
                .version(po.getVersion())
                .orderIndex(po.getOrderIndex())
                .parameterMapping(po.getParameterMapping())
                .enabled(po.getEnabled())
                .notes(po.getNotes())
                .customPackagePath(po.getCustomPackagePath())
                .useCustomPath(po.getUseCustomPath())
                .createdAt(po.getCreatedAt())
                .build();
    }

    // Helper methods for enum conversion

    private OperatorPackage.PackageStatus convertToEntityPackageStatus(PackageStatus dtoType) {
        if (dtoType == null) {
            return OperatorPackage.PackageStatus.DRAFT;
        }
        return OperatorPackage.PackageStatus.valueOf(dtoType.name());
    }

    private PackageStatus convertToDtoPackageStatus(OperatorPackage.PackageStatus entityType) {
        if (entityType == null) {
            return PackageStatus.DRAFT;
        }
        return PackageStatus.valueOf(entityType.name());
    }

    private LanguageType convertToDtoLanguageType(LanguageType entityType) {
        if (entityType == null) {
            return LanguageType.JAVA;
        }
        return entityType;
    }

    // ========== 公共库相关方法 ==========

    @Override
    @Transactional
    public void syncOperatorLibrariesToPackage(Long packageId, Long operatorId, String username) {
        log.info("同步算子的公共库到算子包：packageId={}, operatorId={}", packageId, operatorId);

        // 获取算子包
        OperatorPackage pkg = packageRepository.findById(packageId)
                .orElseThrow(() -> new ResourceNotFoundException("算子包", packageId));

        // 获取算子
        Operator operator = operatorRepository.findById(operatorId)
                .orElseThrow(() -> new ResourceNotFoundException("算子", operatorId));

        // 检查算子是否属于该包
        if (!packageOperatorRepository.existsByOperatorPackageIdAndOperatorId(packageId, operatorId)) {
            throw new IllegalArgumentException("该算子不属于此算子包");
        }

        // 获取算子的公共库依赖
        List<OperatorCommonLibrary> operatorLibraries = operatorCommonLibraryRepository
                .findByOperatorId(operatorId);

        // 删除该算子在 package_common_libraries 中的旧记录
        packageCommonLibraryRepository.deleteByOperatorPackageIdAndOperatorId(packageId, operatorId);

        // 为每个公共库依赖创建 package_common_libraries 记录
        int orderIndex = 0;
        int createdCount = 0;
        int skippedCount = 0;
        for (OperatorCommonLibrary opLib : operatorLibraries) {
            com.operator.core.library.domain.CommonLibrary library = opLib.getLibrary();
            String version = library.getVersion();

            // 检查该公共库是否已经存在于该算子包中（来自其他算子）
            boolean exists = packageCommonLibraryRepository.existsByOperatorPackageIdAndLibraryId(packageId, library.getId());

            if (!exists) {
                PackageCommonLibrary pcl = PackageCommonLibrary.builder()
                        .operatorPackage(pkg)
                        .operator(operator)
                        .library(library)
                        .version(version)
                        .orderIndex(orderIndex++)
                        .useCustomPath(false)
                        .build();

                packageCommonLibraryRepository.save(pcl);
                createdCount++;
            } else {
                // 该公共库已从其他算子同步过，跳过
                orderIndex++;
                skippedCount++;
            }
        }

        log.info("同步完成：创建 {} 个，跳过 {} 个", createdCount, skippedCount);
    }

    @Override
    @Transactional(readOnly = true)
    public PackagePathConfigResponse getPackagePathConfig(Long packageId) {
        OperatorPackage pkg = packageRepository.findById(packageId)
                .orElseThrow(() -> new ResourceNotFoundException("算子包", packageId));

        PackagePathResolver.PackageTemplate template =
                PackagePathResolver.PackageTemplate.valueOf(pkg.getPackageTemplate().toUpperCase());

        // 获取算子配置
        List<OperatorPathConfigResponse> operatorConfigs = packageOperatorRepository
                .findByOperatorPackageIdOrderByOrderIndexAsc(packageId).stream()
                .map(po -> convertToOperatorPathConfig(po, template))
                .collect(Collectors.toList());

        // 获取公共库配置
        List<LibraryPathConfigResponse> libraryConfigs = packageCommonLibraryRepository
                .findByOperatorPackageIdWithLibrary(packageId).stream()
                .map(pcl -> convertToLibraryPathConfig(pcl, template))
                .collect(Collectors.toList());

        return PackagePathConfigResponse.builder()
                .packageTemplate(pkg.getPackageTemplate())
                .operatorConfigs(operatorConfigs)
                .libraryConfigs(libraryConfigs)
                .build();
    }

    @Override
    @Transactional
    public PackagePathConfigResponse updatePackageConfig(Long packageId, PackageConfigRequest request, String username) {
        log.info("更新算子包整体配置：packageId={}, template={}", packageId, request.getPackageTemplate());

        OperatorPackage pkg = packageRepository.findById(packageId)
                .orElseThrow(() -> new ResourceNotFoundException("算子包", packageId));

        // 更新打包模板
        pkg.setPackageTemplate(request.getPackageTemplate());
        pkg.setUpdatedBy(username);
        packageRepository.save(pkg);

        PackagePathResolver.PackageTemplate template =
                PackagePathResolver.PackageTemplate.valueOf(request.getPackageTemplate().toUpperCase());

        // 更新算子路径配置
        if (request.getOperatorConfigs() != null) {
            for (OperatorPathConfigRequest config : request.getOperatorConfigs()) {
                updateOperatorPathConfigInternal(packageId, config, template);
            }
        }

        // 更新公共库路径配置
        if (request.getLibraryConfigs() != null) {
            for (LibraryPathConfigRequest config : request.getLibraryConfigs()) {
                updateLibraryPathConfigInternal(packageId, config, template);
            }
        }

        return getPackagePathConfig(packageId);
    }

    @Override
    @Transactional
    public void updateOperatorPathConfig(Long packageId, Long operatorId, OperatorPathConfigRequest request, String username) {
        log.info("更新算子路径配置：packageId={}, operatorId={}", packageId, operatorId);

        OperatorPackage pkg = packageRepository.findById(packageId)
                .orElseThrow(() -> new ResourceNotFoundException("算子包", packageId));

        PackagePathResolver.PackageTemplate template =
                PackagePathResolver.PackageTemplate.valueOf(pkg.getPackageTemplate().toUpperCase());

        updateOperatorPathConfigInternal(packageId, request, template);
    }

    @Override
    @Transactional
    public void batchUpdateOperatorPathConfig(Long packageId, BatchPathConfigRequest request, String username) {
        log.info("批量更新算子路径配置：packageId={}", packageId);

        OperatorPackage pkg = packageRepository.findById(packageId)
                .orElseThrow(() -> new ResourceNotFoundException("算子包", packageId));

        PackagePathResolver.PackageTemplate template =
                PackagePathResolver.PackageTemplate.valueOf(pkg.getPackageTemplate().toUpperCase());

        if (request.getUseRecommendedPath()) {
            // 使用推荐路径
            for (Long operatorId : request.getOperatorIds()) {
                OperatorPathConfigRequest config = OperatorPathConfigRequest.builder()
                        .useCustomPath(false)
                        .build();
                updateOperatorPathConfigByOperatorId(packageId, operatorId, config);
            }
        }
    }

    @Override
    @Transactional
    public void updateLibraryPathConfig(Long packageId, Long libraryId, LibraryPathConfigRequest request, String username) {
        log.info("更新公共库路径配置：packageId={}, libraryId={}", packageId, libraryId);

        OperatorPackage pkg = packageRepository.findById(packageId)
                .orElseThrow(() -> new ResourceNotFoundException("算子包", packageId));

        PackagePathResolver.PackageTemplate template =
                PackagePathResolver.PackageTemplate.valueOf(pkg.getPackageTemplate().toUpperCase());

        updateLibraryPathConfigByLibraryId(packageId, libraryId, request);
    }

    @Override
    @Transactional
    public void batchUpdateLibraryPathConfig(Long packageId, BatchPathConfigRequest request, String username) {
        log.info("批量更新公共库路径配置：packageId={}", packageId);

        OperatorPackage pkg = packageRepository.findById(packageId)
                .orElseThrow(() -> new ResourceNotFoundException("算子包", packageId));

        PackagePathResolver.PackageTemplate template =
                PackagePathResolver.PackageTemplate.valueOf(pkg.getPackageTemplate().toUpperCase());

        if (request.getUseRecommendedPath()) {
            // 使用推荐路径
            for (Long libraryId : request.getLibraryIds()) {
                LibraryPathConfigRequest config = LibraryPathConfigRequest.builder()
                        .libraryId(libraryId)
                        .useCustomPath(false)
                        .build();
                updateLibraryPathConfigByLibraryId(packageId, libraryId, config);
            }
        }
    }

    // ========== 私有辅助方法 ==========

    private void updateOperatorPathConfigInternal(Long packageId, OperatorPathConfigRequest request,
                                             PackagePathResolver.PackageTemplate template) {
        PackageOperator packageOperator = packageOperatorRepository
                .findByOperatorPackageIdAndOperatorId(packageId, request.getOperatorId())
                .orElseThrow(() -> new ResourceNotFoundException("算子包-算子关联", packageId));

        if (request.getUseCustomPath() != null) {
            packageOperator.setUseCustomPath(request.getUseCustomPath());
        }
        if (request.getCustomPackagePath() != null) {
            packageOperator.setCustomPackagePath(request.getCustomPackagePath());
        }
        if (request.getOrderIndex() != null) {
            packageOperator.setOrderIndex(request.getOrderIndex());
        }

        packageOperatorRepository.save(packageOperator);
    }

    private void updateOperatorPathConfigByOperatorId(Long packageId, Long operatorId, OperatorPathConfigRequest request) {
        PackageOperator packageOperator = packageOperatorRepository
                .findByOperatorPackageIdAndOperatorId(packageId, operatorId)
                .orElseThrow(() -> new ResourceNotFoundException("算子包-算子关联", packageId));

        if (request.getUseCustomPath() != null) {
            packageOperator.setUseCustomPath(request.getUseCustomPath());
        }
        if (request.getCustomPackagePath() != null) {
            packageOperator.setCustomPackagePath(request.getCustomPackagePath());
        }
        if (request.getOrderIndex() != null) {
            packageOperator.setOrderIndex(request.getOrderIndex());
        }

        packageOperatorRepository.save(packageOperator);
    }

    private void updateLibraryPathConfigInternal(Long packageId, LibraryPathConfigRequest request,
                                             PackagePathResolver.PackageTemplate template) {
        PackageCommonLibrary pcl = packageCommonLibraryRepository
                .findByOperatorPackageIdAndLibraryId(packageId, request.getLibraryId())
                .orElseThrow(() -> new ResourceNotFoundException("算子包-公共库关联", packageId));

        if (request.getUseCustomPath() != null) {
            pcl.setUseCustomPath(request.getUseCustomPath());
        }
        if (request.getCustomPackagePath() != null) {
            pcl.setCustomPackagePath(request.getCustomPackagePath());
        }
        if (request.getOrderIndex() != null) {
            pcl.setOrderIndex(request.getOrderIndex());
        }

        packageCommonLibraryRepository.save(pcl);
    }

    private void updateLibraryPathConfigByLibraryId(Long packageId, Long libraryId, LibraryPathConfigRequest request) {
        PackageCommonLibrary pcl = packageCommonLibraryRepository
                .findByOperatorPackageIdAndLibraryId(packageId, libraryId)
                .orElseThrow(() -> new ResourceNotFoundException("算子包-公共库关联", packageId));

        if (request.getUseCustomPath() != null) {
            pcl.setUseCustomPath(request.getUseCustomPath());
        }
        if (request.getCustomPackagePath() != null) {
            pcl.setCustomPackagePath(request.getCustomPackagePath());
        }
        if (request.getOrderIndex() != null) {
            pcl.setOrderIndex(request.getOrderIndex());
        }

        packageCommonLibraryRepository.save(pcl);
    }

    private OperatorPathConfigResponse convertToOperatorPathConfig(PackageOperator packageOperator,
                                                             PackagePathResolver.PackageTemplate template) {
        Operator operator = packageOperator.getOperator();
        String fileName = operator.getOperatorCode() + ".groovy";

        return OperatorPathConfigResponse.builder()
                .operatorId(operator.getId())
                .operatorCode(operator.getOperatorCode())
                .operatorName(operator.getName())
                .currentPath(pathResolver.resolveOperatorPath(operator, packageOperator, template, fileName))
                .recommendedPath(pathResolver.getRecommendedOperatorPath(template))
                .useCustomPath(packageOperator.getUseCustomPath())
                .orderIndex(packageOperator.getOrderIndex())
                .build();
    }

    private LibraryPathConfigResponse convertToLibraryPathConfig(PackageCommonLibrary pcl,
                                                             PackagePathResolver.PackageTemplate template) {
        com.operator.core.library.domain.CommonLibrary library = pcl.getLibrary();

        return LibraryPathConfigResponse.builder()
                .libraryId(library.getId())
                .libraryName(library.getName())
                .libraryType(library.getLibraryType())
                .version(pcl.getVersion())
                .currentPath(pathResolver.resolveLibraryPath(library, pcl, template, "file"))
                .recommendedPath(pathResolver.getRecommendedLibraryPath(template, library.getLibraryType()))
                .useCustomPath(pcl.getUseCustomPath())
                .orderIndex(pcl.getOrderIndex())
                .build();
    }
}
