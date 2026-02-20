package com.operator.service.pkg;

import com.operator.common.dto.pkg.*;
import com.operator.common.enums.PackageStatus;
import com.operator.common.enums.LanguageType;
import com.operator.common.exception.ResourceNotFoundException;
import com.operator.common.utils.PageResponse;
import com.operator.core.operator.domain.Operator;
import com.operator.core.operator.repository.OperatorRepository;
import com.operator.core.pkg.domain.OperatorPackage;
import com.operator.core.pkg.domain.PackageOperator;
import com.operator.core.pkg.repository.OperatorPackageRepository;
import com.operator.core.pkg.repository.PackageOperatorRepository;

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
        response.setOperators(loadPackageOperators(pkg.getId()));
        return response;
    }

    private List<PackageOperatorResponse> loadPackageOperators(Long packageId) {
        return packageOperatorRepository.findByOperatorPackageIdOrderByOrderIndexAsc(packageId).stream()
                .map(this::mapPackageOperatorToResponse)
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
}
