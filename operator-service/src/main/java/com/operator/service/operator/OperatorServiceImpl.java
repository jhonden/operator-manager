package com.operator.service.operator;

import com.operator.common.enums.*;
import com.operator.common.dto.operator.*;
import com.operator.common.exception.ResourceNotFoundException;
import com.operator.common.utils.PageResponse;
import com.operator.core.operator.domain.Category;
import com.operator.core.operator.domain.Operator;
import com.operator.core.operator.domain.Parameter;
import com.operator.core.operator.repository.CategoryRepository;
import com.operator.core.operator.repository.OperatorRepository;
import com.operator.core.operator.repository.ParameterRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Operator Service Implementation
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OperatorServiceImpl implements OperatorService {

    private final OperatorRepository operatorRepository;
    private final CategoryRepository categoryRepository;
    private final ParameterRepository parameterRepository;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public OperatorResponse createOperator(OperatorRequest request, String username) {
        log.info("=== CREATE OPERATOR START ===");
        log.info("Creating operator: {} by user: {}", request.getName(), username);
        log.info("Request parameters count: {}, code: {}, version: {}",
                request.getParameters() != null ? request.getParameters().size() : 0,
                request.getCode() != null ? request.getCode().length() : 0,
                request.getVersion());

        // Validate required fields for published operators
        OperatorStatus status = request.getStatus() != null ? request.getStatus() : OperatorStatus.DRAFT;
        if (status == OperatorStatus.PUBLISHED) {
            if (request.getName() == null || request.getName().trim().isEmpty()) {
                throw new IllegalArgumentException("Name is required for published operators");
            }
            if (request.getLanguage() == null) {
                throw new IllegalArgumentException("Language is required for published operators");
            }
        }

        // Validate category if provided
        if (request.getCategoryId() != null) {
            categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", request.getCategoryId()));
        }

        Operator operator = new Operator();
        operator.setName(request.getName());
        operator.setDescription(request.getDescription());
        operator.setLanguage(request.getLanguage());
        operator.setStatus(request.getStatus() != null ? request.getStatus() : OperatorStatus.DRAFT);

        if (request.getCategoryId() != null) {
            Category category = new Category();
            category.setId(request.getCategoryId());
            operator.setCategory(category);
        }

        operator.setTags(request.getTags() != null ? String.join(",", request.getTags()) : null);
        operator.setIsPublic(request.getIsPublic() != null ? request.getIsPublic() : false);

        // Set version number - use request value if provided, otherwise use default
        if (request.getVersion() != null && !request.getVersion().trim().isEmpty()) {
            operator.setVersion(request.getVersion());
            log.info("Setting version from request: {}", request.getVersion());
        } else {
            operator.setVersion("1.0.0");
            log.info("Setting default version: 1.0.0");
        }

        log.info("Operator before save - name: {}, version: {}", operator.getName(), operator.getVersion());

        // Save code content if provided
        if (request.getCode() != null && !request.getCode().trim().isEmpty()) {
            // Store code in codeFilePath field for now (has 500 character limit)
            operator.setCodeFilePath(request.getCode());
            // TODO: Later, save code file to MinIO
        }

        operator.setCreatedBy(username);
        operator = operatorRepository.save(operator);

        log.info("Operator after save - id: {}, name: {}, version: {}", operator.getId(), operator.getName(), operator.getVersion());

        // Save parameters in a separate transaction
        if (request.getParameters() != null && !request.getParameters().isEmpty()) {
            saveParametersForOperator(operator.getId(), operator, request.getParameters(), username);
        }

        log.info("Operator created with ID: {}", operator.getId());
        return mapToResponse(operator);
    }

    /**
     * Save parameters for an operator in a separate transaction
     */
    @org.springframework.transaction.annotation.Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    protected void saveParametersForOperator(Long operatorId, Operator operatorEntity, List<ParameterRequest> parameters, String username) {
        if (parameters == null || parameters.isEmpty()) {
            return;
        }

        for (ParameterRequest paramReq : parameters) {
            com.operator.core.operator.domain.Parameter parameter = new com.operator.core.operator.domain.Parameter();
            parameter.setOperator(operatorEntity);
            parameter.setName(paramReq.getName());
            parameter.setDescription(paramReq.getDescription());
            parameter.setParameterType(paramReq.getParameterType());
            parameter.setIoType(paramReq.getIoType());
            parameter.setIsRequired(paramReq.getIsRequired() != null ? paramReq.getIsRequired() : false);
            parameter.setDefaultValue(paramReq.getDefaultValue());
            parameter.setValidationRules(paramReq.getValidationRules());
            parameter.setOrderIndex(paramReq.getOrderIndex() != null ? paramReq.getOrderIndex() : 0);
            parameter.setCreatedBy(username);
            parameterRepository.save(parameter);
        }
        log.info("Saved {} parameters for operator: {}", parameters.size(), operatorId);
    }

    /**
     * Delete parameters for an operator in a separate transaction
     */
    @org.springframework.transaction.annotation.Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    protected void deleteParametersForOperator(Long operatorId) {
        log.info("Deleting parameters for operator: {}", operatorId);
        parameterRepository.deleteByOperatorId(operatorId);
        entityManager.flush();
        log.info("Flushed transaction after parameter deletion: {}", operatorId);
    }

    @Override
    @Transactional
    public OperatorResponse updateOperator(Long id, OperatorRequest request, String username) {
        log.info("=== UPDATE OPERATOR START ===");
        log.info("Updating operator: {} by user: {}", id, username);
        log.info("Request version: {}", request.getVersion());

        Operator operator = operatorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Operator", id));

        // Validate required fields for published operators
        OperatorStatus status = request.getStatus() != null ? request.getStatus() : operator.getStatus();
        if (status == OperatorStatus.PUBLISHED) {
            if (request.getName() == null || request.getName().trim().isEmpty()) {
                if (operator.getName() == null || operator.getName().trim().isEmpty()) {
                    throw new IllegalArgumentException("Name is required for published operators");
                }
            }
            if (request.getLanguage() == null && operator.getLanguage() == null) {
                throw new IllegalArgumentException("Language is required for published operators");
            }
        }

        // Validate category if provided
        if (request.getCategoryId() != null) {
            categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", request.getCategoryId()));
        }

        operator.setName(request.getName());
        operator.setDescription(request.getDescription());
        operator.setLanguage(request.getLanguage());
        if (request.getStatus() != null) {
            operator.setStatus(request.getStatus());
        }
        if (request.getCategoryId() != null) {
            Category category = new Category();
            category.setId(request.getCategoryId());
            operator.setCategory(category);
        } else {
            operator.setCategory(null);
        }
        operator.setTags(request.getTags() != null ? String.join(",", request.getTags()) : null);
        operator.setIsPublic(request.getIsPublic() != null ? request.getIsPublic() : operator.getIsPublic());

        // Update version number if provided
        if (request.getVersion() != null && !request.getVersion().trim().isEmpty()) {
            operator.setVersion(request.getVersion());
            log.info("Updating version from request: {}", request.getVersion());
        } else if (operator.getVersion() == null || operator.getVersion().trim().isEmpty()) {
            operator.setVersion("1.0.0");
            log.info("Setting default version: 1.0.0");
        } else {
            log.info("Keeping existing version: {}", operator.getVersion());
        }

        log.info("Operator before update - id: {}, name: {}, version: {}", operator.getId(), operator.getName(), operator.getVersion());

        // Update code content if provided
        if (request.getCode() != null && !request.getCode().trim().isEmpty()) {
            // Store code in codeFilePath field for now (has 500 character limit)
            operator.setCodeFilePath(request.getCode());
            // TODO: Later, save code file to MinIO
        }

        operator.setUpdatedBy(username);
        operator = operatorRepository.save(operator);

        log.info("Operator after update - id: {}, name: {}, version: {}", operator.getId(), operator.getName(), operator.getVersion());

        // Save new parameters without deleting existing ones
        // Frontend should delete old parameters first if needed
        if (request.getParameters() != null && !request.getParameters().isEmpty()) {
            saveParametersForOperator(id, operator, request.getParameters(), username);
        }

        log.info("Operator updated: {}", id);
        return mapToResponse(operator);
    }


    @Override
    @Transactional(readOnly = true)
    public OperatorResponse getOperatorById(Long id) {
        Operator operator = operatorRepository.findByIdWithAssociations(id)
                .orElseThrow(() -> new ResourceNotFoundException("Operator", id));
        return mapToResponse(operator);
    }

    @Override
    public OperatorResponse getOperatorByName(String name) {
        Operator operator = operatorRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Operator", "name", name));
        return mapToResponse(operator);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OperatorResponse> searchOperators(OperatorSearchRequest request) {
        log.info("Searching operators with request: {}", request);

        Pageable pageable = createPageable(request);

        Page<Operator> operators;
        if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
            operators = operatorRepository.searchOperators(request.getKeyword(), pageable);
        } else if (request.getCategoryId() != null) {
            operators = operatorRepository.findByCategoryId(request.getCategoryId(), pageable);
        } else if (request.getLanguage() != null) {
            List<Operator> operatorList = operatorRepository.findByLanguage(LanguageType.valueOf(request.getLanguage()));
            operators = createPageFromList(operatorList, pageable);
        } else {
            operators = operatorRepository.findAll(pageable);
        }

        return PageResponse.of(operators.map(this::mapToResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OperatorResponse> getAllOperators(Pageable pageable) {
        // Fetch all operators with associations
        List<Operator> operators = operatorRepository.findAllWithAssociations();

        log.info("getAllOperators: fetched {} operators, page: {}, size: {}", operators.size(), pageable.getPageNumber(), pageable.getPageSize());

        // Log each operator's language for debugging
        for (int i = 0; i < operators.size(); i++) {
            Operator op = operators.get(i);
            log.info("Operator[{}]: id={}, name={}, language={}, languageValue={}",
                    i, op.getId(), op.getName(), op.getLanguage(),
                    op.getLanguage() != null ? "NULL" : op.getLanguage().name());
        }

        // Manual pagination for fetched results
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), operators.size());
        List<Operator> pagedOperators = operators.subList(start, end);

        return new PageImpl<>(pagedOperators.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList()), pageable, operators.size());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OperatorResponse> getOperatorsByCategory(Long categoryId) {
        return operatorRepository.findByCategoryIdWithAssociations(categoryId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OperatorResponse> getOperatorsByCreator(String username) {
        return operatorRepository.findByCreatedBy(username).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OperatorResponse uploadCodeFile(Long operatorId, org.springframework.web.multipart.MultipartFile file, String username) {
        // TODO: Implement file upload to MinIO
        log.info("Uploading code file for operator: {} by user: {}", operatorId, username);
        return getOperatorById(operatorId);
    }

    @Override
    @Transactional
    public OperatorResponse updateOperatorStatus(Long id, String status, String username) {
        Operator operator = operatorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Operator", id));

        operator.setStatus(OperatorStatus.valueOf(status));
        operator.setUpdatedBy(username);
        operator = operatorRepository.save(operator);

        return mapToResponse(operator);
    }

    @Override
    @Transactional
    public ParameterResponse addParameter(Long operatorId, ParameterRequest request, String username) {
        Operator operator = operatorRepository.findById(operatorId)
                .orElseThrow(() -> new ResourceNotFoundException("Operator", operatorId));

        com.operator.core.operator.domain.Parameter parameter = new com.operator.core.operator.domain.Parameter();
        parameter.setOperator(operator);
        parameter.setName(request.getName());
        parameter.setDescription(request.getDescription());
        parameter.setParameterType(request.getParameterType());
        parameter.setIoType(request.getIoType());
        parameter.setIsRequired(request.getIsRequired());
        parameter.setDefaultValue(request.getDefaultValue());
        parameter.setValidationRules(request.getValidationRules());
        parameter.setOrderIndex(request.getOrderIndex());
        parameter.setCreatedBy(username);

        parameter = parameterRepository.save(parameter);

        return mapParameterToResponse(parameter);
    }

    @Override
    @Transactional
    public ParameterResponse updateParameter(Long operatorId, Long parameterId, ParameterRequest request, String username) {
        com.operator.core.operator.domain.Parameter parameter = parameterRepository.findById(parameterId)
                .orElseThrow(() -> new ResourceNotFoundException("Parameter", parameterId));

        if (!parameter.getOperator().getId().equals(operatorId)) {
            throw new IllegalArgumentException("Parameter does not belong to operator");
        }

        parameter.setName(request.getName());
        parameter.setDescription(request.getDescription());
        parameter.setParameterType(request.getParameterType());
        parameter.setIoType(request.getIoType());
        parameter.setIsRequired(request.getIsRequired());
        parameter.setDefaultValue(request.getDefaultValue());
        parameter.setValidationRules(request.getValidationRules());
        parameter.setOrderIndex(request.getOrderIndex());
        parameter.setUpdatedBy(username);

        parameter = parameterRepository.save(parameter);

        return mapParameterToResponse(parameter);
    }

    @Override
    @Transactional
    public void deleteParameter(Long operatorId, Long parameterId, String username) {
        com.operator.core.operator.domain.Parameter parameter = parameterRepository.findById(parameterId)
                .orElseThrow(() -> new ResourceNotFoundException("Parameter", parameterId));

        if (!parameter.getOperator().getId().equals(operatorId)) {
            throw new IllegalArgumentException("Parameter does not belong to operator");
        }

        parameterRepository.delete(parameter);
    }

    @Override
    @Transactional
    public OperatorResponse toggleFeatured(Long id, String username) {
        Operator operator = operatorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Operator", id));

        operator.setFeatured(!operator.getFeatured());
        operator.setUpdatedBy(username);
        operator = operatorRepository.save(operator);

        return mapToResponse(operator);
    }

    @Override
    @Transactional
    public void incrementDownloadCount(Long id) {
        Operator operator = operatorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Operator", id));

        operator.setDownloadsCount(operator.getDownloadsCount() + 1);
        operatorRepository.save(operator);
    }

    @Override
    @Transactional
    public void deleteOperator(Long id, String username) {
        log.info("Deleting operator: {} by user: {}", id, username);

        Operator operator = operatorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Operator", id));

        // First, delete all parameters for this operator using native SQL
        jakarta.persistence.Query deleteParamsQuery = entityManager.createNativeQuery(
                "DELETE FROM operator_parameters WHERE operator_id = ?");
        deleteParamsQuery.setParameter(1, id);
        int paramsDeleted = deleteParamsQuery.executeUpdate();
        log.info("Deleted {} parameters for operator: {}", paramsDeleted, id);

        // Flush and clear the session to ensure parameter delete is committed
        entityManager.flush();
        entityManager.clear();

        // Then delete the operator using native SQL as well
        jakarta.persistence.Query deleteOperatorQuery = entityManager.createNativeQuery(
                "DELETE FROM operators WHERE id = ?");
        deleteOperatorQuery.setParameter(1, id);
        deleteOperatorQuery.executeUpdate();

        log.info("Operator deleted: {}", id);
    }

    private OperatorResponse mapToResponse(Operator operator) {
        // Fetch parameters separately to avoid lazy loading issues
        List<ParameterResponse> parameterResponses = parameterRepository.findByOperatorIdOrderByOrderIndexAsc(operator.getId()).stream()
                .map(this::mapParameterToResponse)
                .collect(Collectors.toList());

        return OperatorResponse.builder()
                .id(operator.getId())
                .name(operator.getName())
                .description(operator.getDescription())
                .language(operator.getLanguage())
                .status(operator.getStatus())
                .version(operator.getVersion())
                .code(operator.getCodeFilePath())  // Code is stored in codeFilePath field now
                .codeFilePath(operator.getCodeFilePath())
                .fileName(operator.getFileName())
                .fileSize(operator.getFileSize())
                .category(operator.getCategory() != null ?
                        mapCategoryToResponse(operator.getCategory()) : null)
                .tags(operator.getTags() != null ?
                        List.of(operator.getTags().split(",")) : null)
                .isPublic(operator.getIsPublic())
                .downloadsCount(operator.getDownloadsCount())
                .featured(operator.getFeatured())
                .parameters(parameterResponses)
                .createdBy(operator.getCreatedBy())
                .createdAt(operator.getCreatedAt())
                .updatedAt(operator.getUpdatedAt())
                .build();
    }

    private ParameterResponse mapParameterToResponse(com.operator.core.operator.domain.Parameter parameter) {
        return ParameterResponse.builder()
                .id(parameter.getId())
                .name(parameter.getName())
                .description(parameter.getDescription())
                .parameterType(parameter.getParameterType())
                .ioType(parameter.getIoType())
                .isRequired(parameter.getIsRequired())
                .defaultValue(parameter.getDefaultValue())
                .validationRules(parameter.getValidationRules())
                .orderIndex(parameter.getOrderIndex())
                .createdAt(parameter.getCreatedAt())
                .build();
    }

    private com.operator.common.dto.operator.CategoryResponse mapCategoryToResponse(
            com.operator.core.operator.domain.Category category) {
        return com.operator.common.dto.operator.CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .icon(category.getIcon())
                .color(category.getColor())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .orderIndex(category.getOrderIndex())
                .operatorCount(category.getOperatorCount())
                .createdAt(category.getCreatedAt())
                .build();
    }

    private Pageable createPageable(OperatorSearchRequest request) {
        Sort sort = Sort.by(
                "desc".equalsIgnoreCase(request.getSortOrder()) ? Sort.Direction.DESC : Sort.Direction.ASC,
                request.getSortBy()
        );
        return PageRequest.of(request.getPage(), request.getSize(), sort);
    }

    private <T> Page<T> createPageFromList(List<T> list, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());

        if (start >= list.size()) {
            return new org.springframework.data.domain.PageImpl<>(java.util.Collections.emptyList(), pageable, list.size());
        }

        List<T> subList = list.subList(start, end);
        return new org.springframework.data.domain.PageImpl<>(subList, pageable, list.size());
    }
}
