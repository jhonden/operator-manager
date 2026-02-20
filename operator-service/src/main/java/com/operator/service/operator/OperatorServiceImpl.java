package com.operator.service.operator;

import com.operator.common.dto.operator.*;
import com.operator.common.enums.IOType;
import com.operator.common.enums.LanguageType;
import com.operator.common.enums.OperatorStatus;
import com.operator.common.enums.ParameterType;
import com.operator.common.exception.ResourceNotFoundException;
import com.operator.common.utils.PageResponse;
import com.operator.core.operator.domain.Operator;
import com.operator.core.operator.domain.Parameter;
import com.operator.core.operator.repository.OperatorRepository;
import com.operator.core.operator.repository.ParameterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
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
    private final ParameterRepository parameterRepository;

    @Override
    @Transactional
    public OperatorResponse createOperator(OperatorRequest request, String username) {
        log.info("Creating operator: {} by user: {}", request.getName(), username);

        // Check if operatorCode is unique
        if (request.getOperatorCode() != null && operatorRepository.existsByOperatorCode(request.getOperatorCode())) {
            throw new IllegalArgumentException("Operator code '" + request.getOperatorCode() + "' already exists");
        }

        Operator operator = new Operator();
        operator.setName(request.getName());
        operator.setDescription(request.getDescription());
        operator.setLanguage(convertToEntityLanguageType(request.getLanguage()));
        operator.setStatus(convertToEntityOperatorStatus(request.getStatus()));
        operator.setIsPublic(request.getIsPublic() != null ? request.getIsPublic() : false);
        operator.setCreatedBy(username);
        operator.setUpdatedBy(username);
        operator.setVersion(request.getVersion() != null ? request.getVersion() : "1.0.0");
        operator.setOperatorCode(request.getOperatorCode());
        operator.setObjectCode(request.getObjectCode());
        operator.setDataFormat(request.getDataFormat());
        operator.setGenerator(request.getGenerator());
        operator.setBusinessLogic(request.getBusinessLogic());
        operator.setCode(request.getCode());

        log.info("Business logic from request: {}", request.getBusinessLogic());
        log.info("Code from request: {} (length: {})",
                 request.getCode() != null ? "present" : "null",
                 request.getCode() != null ? request.getCode().length() : 0);

        operator = operatorRepository.save(operator);

        log.info("Code after save, length: {}",
                 operator.getCode() != null ? operator.getCode().length() : 0);

        log.info("Business logic after save: {}", operator.getBusinessLogic());

        // Save parameters
        if (request.getParameters() != null) {
            for (ParameterRequest paramRequest : request.getParameters()) {
                Parameter parameter = mapToParameter(paramRequest);
                parameter.setOperator(operator);
                parameter.setCreatedBy(username);
                parameter.setUpdatedBy(username);
                parameterRepository.save(parameter);
            }
        }

        return mapToResponse(operator);
    }

    @Override
    @Transactional
    public OperatorResponse updateOperator(Long id, OperatorRequest request, String username) {
        log.info("=== SERVICE updateOperator: Updating operator: {} by user: {}", id, username);
        log.info("=== SERVICE updateOperator: Request code present: {}, code length: {}",
                request.getCode() != null,
                request.getCode() != null ? request.getCode().length() : 0);
        log.info("=== SERVICE updateOperator: Request business logic present: {}, business logic length: {}",
                request.getBusinessLogic() != null,
                request.getBusinessLogic() != null ? request.getBusinessLogic().length() : 0);

        Operator operator = operatorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Operator", id));

        log.info("=== SERVICE updateOperator: Before update - code present: {}, code length: {}",
                operator.getCode() != null,
                operator.getCode() != null ? operator.getCode().length() : 0);
        log.info("=== SERVICE updateOperator: Before update - business logic present: {}, business logic length: {}",
                operator.getBusinessLogic() != null,
                operator.getBusinessLogic() != null ? operator.getBusinessLogic().length() : 0);

        // Check if operatorCode is unique (if changed)
        if (request.getOperatorCode() != null && !request.getOperatorCode().equals(operator.getOperatorCode())) {
            if (operatorRepository.existsByOperatorCode(request.getOperatorCode())) {
                throw new IllegalArgumentException("Operator code '" + request.getOperatorCode() + "' already exists");
            }
            operator.setOperatorCode(request.getOperatorCode());
        }

        // Update fields
        if (request.getName() != null) {
            operator.setName(request.getName());
        }
        if (request.getDescription() != null) {
            operator.setDescription(request.getDescription());
        }
        if (request.getLanguage() != null) {
            operator.setLanguage(convertToEntityLanguageType(request.getLanguage()));
        }
        if (request.getStatus() != null) {
            operator.setStatus(convertToEntityOperatorStatus(request.getStatus()));
        }
        if (request.getIsPublic() != null) {
            operator.setIsPublic(request.getIsPublic());
        }
        if (request.getVersion() != null) {
            operator.setVersion(request.getVersion());
        }
        if (request.getObjectCode() != null) {
            operator.setObjectCode(request.getObjectCode());
        }
        if (request.getDataFormat() != null) {
            operator.setDataFormat(request.getDataFormat());
        }
        if (request.getGenerator() != null) {
            operator.setGenerator(request.getGenerator());
        }
        if (request.getCode() != null) {
            operator.setCode(request.getCode());
            log.info("=== SERVICE updateOperator: Updated code, length: {}", request.getCode().length());
        }

        // Always update businessLogic if it's present in request (including empty string)
        if (request.getBusinessLogic() != null) {
            operator.setBusinessLogic(request.getBusinessLogic());
            log.info("=== SERVICE updateOperator: Updated business logic, length: {}",
                    request.getBusinessLogic() != null ? request.getBusinessLogic().length() : 0);
        }

        // Log existing parameters before update
        List<Parameter> existingParams = parameterRepository.findByOperatorIdOrderByOrderIndexAsc(id);
        log.info("=== SERVICE updateOperator: Existing parameters count: {}", existingParams.size());

        operator.setUpdatedBy(username);
        operator = operatorRepository.save(operator);

        log.info("=== SERVICE updateOperator: After save - code present: {}, code length: {}",
                operator.getCode() != null,
                operator.getCode() != null ? operator.getCode().length() : 0);
        log.info("=== SERVICE updateOperator: After save - business logic present: {}, business logic length: {}",
                operator.getBusinessLogic() != null,
                operator.getBusinessLogic() != null ? operator.getBusinessLogic().length() : 0);

        // Handle parameters - update if provided, or delete all if null/empty
        if (request.getParameters() != null) {
            log.info("=== SERVICE updateOperator: Request parameters count: {}", request.getParameters().size());

            // Delete all existing parameters first
            parameterRepository.deleteByOperatorId(id);
            log.info("=== SERVICE updateOperator: Deleted {} existing parameters", existingParams.size());

            // Create new parameters
            for (ParameterRequest paramRequest : request.getParameters()) {
                Parameter parameter = mapToParameter(paramRequest);
                parameter.setOperator(operator);
                parameter.setCreatedBy(username);
                parameter.setUpdatedBy(username);
                Parameter savedParam = parameterRepository.save(parameter);
                log.info("=== SERVICE updateOperator: Saved parameter: {} (type: {}, order: {})",
                        savedParam.getName(),
                        savedParam.getIoType(),
                        savedParam.getOrderIndex());
            }
            log.info("=== SERVICE updateOperator: Saved {} new parameters", request.getParameters().size());
        } else {
            log.info("=== SERVICE updateOperator: No parameters in request, skipping parameter update");
        }

        return mapToResponse(operator);
    }

    @Override
    @Transactional
    public void deleteOperator(Long id, String username) {
        log.info("Deleting operator: {} by user: {}", id, username);

        Operator operator = operatorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Operator", id));

        // Parameters will be cascade deleted
        operatorRepository.delete(operator);
        log.info("Operator deleted: {}", id);
    }

    @Override
    public OperatorResponse getOperatorById(Long id) {
        log.info("Getting operator by id: {}", id);
        Operator operator = operatorRepository.findByIdWithAssociations(id)
                .orElseThrow(() -> new ResourceNotFoundException("Operator", id));
        return mapToResponse(operator);
    }

    @Override
    public OperatorResponse getOperatorByName(String name) {
        log.info("Getting operator by name: {}", name);
        Operator operator = operatorRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Operator", "name", name));
        return mapToResponse(operator);
    }

    @Override
    public PageResponse<OperatorResponse> searchOperators(OperatorSearchRequest request) {
        log.info("Searching operators with filters: {}", request);

        Pageable pageable = org.springframework.data.domain.PageRequest.of(
                request.getPage() != null ? request.getPage() : 0,
                request.getSize() != null ? request.getSize() : 20,
                org.springframework.data.domain.Sort.by(
                        org.springframework.data.domain.Sort.Direction.DESC, "createdAt")
        );

        Page<Operator> page;
        if (request.getKeyword() != null && !request.getKeyword().trim().isEmpty()) {
            page = operatorRepository.searchOperators(request.getKeyword(), pageable);
        } else {
            page = operatorRepository.findAll(pageable);
        }

        return PageResponse.of(page.map(this::mapToResponse));
    }

    @Override
    public Page<OperatorResponse> getAllOperators(Pageable pageable) {
        log.info("Getting all operators");
        Page<Operator> page = operatorRepository.findAll(pageable);
        return page.map(this::mapToResponse);
    }

    @Override
    public List<OperatorResponse> getOperatorsByCategory(Long categoryId) {
        log.info("Getting operators by category: {} (category feature removed, returning empty list)", categoryId);
        // Category feature has been removed, return empty list
        return List.of();
    }

    @Override
    public List<OperatorResponse> getOperatorsByCreator(String username) {
        log.info("Getting operators by creator: {}", username);
        List<Operator> operators = operatorRepository.findByCreatedBy(username);
        return operators.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OperatorResponse uploadCodeFile(Long operatorId, MultipartFile file, String username) {
        log.info("Uploading code file for operator: {} by user: {}", operatorId, username);

        Operator operator = operatorRepository.findById(operatorId)
                .orElseThrow(() -> new ResourceNotFoundException("Operator", operatorId));

        operator.setCodeFilePath("/uploads/" + file.getOriginalFilename());
        operator.setFileName(file.getOriginalFilename());
        operator.setFileSize(file.getSize());
        operator.setUpdatedBy(username);

        operator = operatorRepository.save(operator);
        return mapToResponse(operator);
    }

    @Override
    @Transactional
    public OperatorResponse updateOperatorStatus(Long id, String status, String username) {
        log.info("Updating operator status: {} to {} by user: {}", id, status, username);

        Operator operator = operatorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Operator", id));

        OperatorStatus entityStatus = OperatorStatus.valueOf(status);
        operator.setStatus(entityStatus);
        operator.setUpdatedBy(username);

        operator = operatorRepository.save(operator);
        return mapToResponse(operator);
    }

    @Override
    @Transactional
    public ParameterResponse addParameter(Long operatorId, ParameterRequest request, String username) {
        log.info("Adding parameter to operator: {} by user: {}", operatorId, username);

        Operator operator = operatorRepository.findById(operatorId)
                .orElseThrow(() -> new ResourceNotFoundException("Operator", operatorId));

        Parameter parameter = mapToParameter(request);
        parameter.setOperator(operator);
        parameter.setCreatedBy(username);
        parameter.setUpdatedBy(username);

        parameter = parameterRepository.save(parameter);
        return mapToParameterResponse(parameter);
    }

    @Override
    @Transactional
    public ParameterResponse updateParameter(Long operatorId, Long parameterId, ParameterRequest request, String username) {
        log.info("Updating parameter: {} for operator: {} by user: {}", parameterId, operatorId, username);

        Operator operator = operatorRepository.findById(operatorId)
                .orElseThrow(() -> new ResourceNotFoundException("Operator", operatorId));

        Parameter parameter = parameterRepository.findById(parameterId)
                .orElseThrow(() -> new ResourceNotFoundException("Parameter", parameterId));

        if (!parameter.getOperator().getId().equals(operatorId)) {
            throw new IllegalArgumentException("Parameter does not belong to the specified operator");
        }

        if (request.getName() != null) {
            parameter.setName(request.getName());
        }
        if (request.getDescription() != null) {
            parameter.setDescription(request.getDescription());
        }
        if (request.getParameterType() != null) {
            parameter.setParameterType(request.getParameterType());
        }
        if (request.getIoType() != null) {
            parameter.setIoType(request.getIoType());
        }
        if (request.getIsRequired() != null) {
            parameter.setIsRequired(request.getIsRequired());
        }
        if (request.getDefaultValue() != null) {
            parameter.setDefaultValue(request.getDefaultValue());
        }
        if (request.getValidationRules() != null) {
            parameter.setValidationRules(request.getValidationRules());
        }
        if (request.getOrderIndex() != null) {
            parameter.setOrderIndex(request.getOrderIndex());
        }

        parameter.setUpdatedBy(username);
        parameter = parameterRepository.save(parameter);
        return mapToParameterResponse(parameter);
    }

    @Override
    @Transactional
    public void deleteParameter(Long operatorId, Long parameterId, String username) {
        log.info("Deleting parameter: {} for operator: {} by user: {}", parameterId, operatorId, username);

        Operator operator = operatorRepository.findById(operatorId)
                .orElseThrow(() -> new ResourceNotFoundException("Operator", operatorId));

        Parameter parameter = parameterRepository.findById(parameterId)
                .orElseThrow(() -> new ResourceNotFoundException("Parameter", parameterId));

        if (!parameter.getOperator().getId().equals(operatorId)) {
            throw new IllegalArgumentException("Parameter does not belong to the specified operator");
        }

        parameterRepository.delete(parameter);
        log.info("Parameter deleted: {}", parameterId);
    }

    @Override
    @Transactional
    public OperatorResponse toggleFeatured(Long id, String username) {
        log.info("Toggling featured status for operator: {} by user: {}", id, username);

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
        log.info("Incrementing download count for operator: {}", id);

        Operator operator = operatorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Operator", id));

        operator.setDownloadsCount((operator.getDownloadsCount() != null ? operator.getDownloadsCount() : 0) + 1);
        operatorRepository.save(operator);
    }

    // Helper methods

    private OperatorResponse mapToResponse(Operator operator) {
        log.info("=== SERVICE mapToResponse: operator ID: {}, code present: {}, code length: {}",
                operator.getId(),
                operator.getCode() != null,
                operator.getCode() != null ? operator.getCode().length() : 0);
        log.info("=== SERVICE mapToResponse: business logic present: {}, business logic length: {}",
                operator.getBusinessLogic() != null,
                operator.getBusinessLogic() != null ? operator.getBusinessLogic().length() : 0);

        return OperatorResponse.builder()
                .id(operator.getId())
                .name(operator.getName())
                .description(operator.getDescription())
                .language(convertToDtoLanguageType(operator.getLanguage()))
                .status(convertToDtoOperatorStatus(operator.getStatus()))
                .version(operator.getVersion())
                .codeFilePath(operator.getCodeFilePath())
                .code(operator.getCode())
                .fileName(operator.getFileName())
                .fileSize(operator.getFileSize())
                .tags(new ArrayList<>()) // Empty list since tags are removed
                .isPublic(operator.getIsPublic())
                .downloadsCount(operator.getDownloadsCount())
                .featured(operator.getFeatured())
                .parameters(operator.getParameters().stream()
                        .map(this::mapToParameterResponse)
                        .collect(Collectors.toList()))
                .createdBy(operator.getCreatedBy())
                .createdAt(operator.getCreatedAt())
                .updatedAt(operator.getUpdatedAt())
                .operatorCode(operator.getOperatorCode())
                .objectCode(operator.getObjectCode())
                .dataFormat(operator.getDataFormat())
                .generator(operator.getGenerator())
                .businessLogic(operator.getBusinessLogic())
                .build();
    }

    private ParameterResponse mapToParameterResponse(Parameter parameter) {
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
                .build();
    }

    private Parameter mapToParameter(ParameterRequest request) {
        return Parameter.builder()
                .name(request.getName())
                .description(request.getDescription())
                .parameterType(request.getParameterType())
                .ioType(request.getIoType())
                .isRequired(request.getIsRequired())
                .defaultValue(request.getDefaultValue())
                .validationRules(request.getValidationRules())
                .orderIndex(request.getOrderIndex())
                .build();
    }

    private LanguageType convertToEntityLanguageType(LanguageType dtoType) {
        if (dtoType == null) {
            return LanguageType.JAVA;
        }
        return dtoType;
    }

    private LanguageType convertToDtoLanguageType(LanguageType entityType) {
        if (entityType == null) {
            return LanguageType.JAVA;
        }
        return entityType;
    }

    private OperatorStatus convertToEntityOperatorStatus(OperatorStatus dtoType) {
        if (dtoType == null) {
            return OperatorStatus.DRAFT;
        }
        return dtoType;
    }

    private OperatorStatus convertToDtoOperatorStatus(OperatorStatus entityType) {
        if (entityType == null) {
            return OperatorStatus.DRAFT;
        }
        return entityType;
    }
}
