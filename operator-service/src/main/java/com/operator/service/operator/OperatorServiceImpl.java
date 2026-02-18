package com.operator.service.operator;

import com.operator.common.enums.*;
import com.operator.common.dto.operator.*;
import com.operator.common.exception.ResourceNotFoundException;
import com.operator.common.utils.PageResponse;
import com.operator.core.operator.domain.Operator;
import com.operator.core.operator.domain.Parameter;
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
    private final ParameterRepository parameterRepository;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public OperatorResponse createOperator(OperatorRequest request, String username) {
        log.info("=== CREATE OPERATOR START ===");
        log.info("Creating operator: {} by user: {}", request.getName(), username);
        log.info("Request parameters count: {}, code length: {}, version: {}",
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

        Operator operator = new Operator();
        operator.setName(request.getName());
        operator.setDescription(request.getDescription());
        operator.setLanguage(request.getLanguage());
        operator.setStatus(request.getStatus() != null ? request.getStatus() : OperatorStatus.DRAFT);
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
        operator = operatorRepository.save(operator);

        // Save parameters
        if (request.getParameters() != null) {
            saveParameters(request.getParameters(), operator);
        }

        log.info("=== CREATE OPERATOR END ===");
        return mapToResponse(operator);
    }

    @Override
    public OperatorResponse getOperatorById(Long id) {
        log.info("Getting operator by id: {}", id);
        Operator operator = operatorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Operator", id));
        return mapToResponse(operator);
    }

    @Override
    public PageResponse<OperatorResponse> getAllOperators(OperatorSearchRequest request) {
        log.info("Searching operators with filters: {}", request);

        // Build criteria
        CriteriaBuilder<Operator> criteriaBuilder = new CriteriaBuilder<>();
        Root<Operator> root = criteriaBuilder.from(Operator.class);

        // Name filter
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            criteriaBuilder.add(root.get("name").like("%" + request.getName() + "%"));
        }

        // Status filter
        if (request.getStatus() != null) {
            criteriaBuilder.add(root.get("status").equalTo(request.getStatus()));
        }

        // Language filter
        if (request.getLanguage() != null) {
            criteriaBuilder.add(root.get("language").equalTo(request.getLanguage()));
        }

        // Is Public filter
        if (request.getIsPublic() != null) {
            criteriaBuilder.add(root.get("isPublic").equalTo(request.getIsPublic()));
        }

        // Page and sort
        Pageable pageable = PageRequest.of(
                request.getPage() != null ? request.getPage() : 0,
                request.getSize() != null ? request.getSize() : 20,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        CriteriaQuery<Operator> query = criteriaBuilder.build();
        Page<Operator> page = operatorRepository.findAll(query, pageable);

        return PageResponse.of(page.map(this::mapToResponse));
    }

    @Override
    @Transactional
    public OperatorResponse updateOperator(Long id, OperatorRequest request, String username) {
        log.info("=== UPDATE OPERATOR START ===");
        log.info("Updating operator: {} by user: {}", id, username);

        Operator operator = operatorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Operator", id));

        // Update fields
        if (request.getName() != null) {
            operator.setName(request.getName());
        }
        if (request.getDescription() != null) {
            operator.setDescription(request.getDescription());
        }
        if (request.getLanguage() != null) {
            operator.setLanguage(request.getLanguage());
        }
        if (request.getStatus() != null) {
            operator.setStatus(request.getStatus());
        }
        if (request.getIsPublic() != null) {
            operator.setIsPublic(request.getIsPublic());
        }
        if (request.getVersion() != null && !request.getVersion().trim().isEmpty()) {
            operator.setVersion(request.getVersion());
        }
        if (request.getTags() != null) {
            operator.setTags(request.getTags());
        }

        log.info("=== UPDATE OPERATOR END ===");
        return mapToResponse(operator);
    }

    @Override
    @Transactional
    public void deleteOperator(Long id) {
        log.info("Deleting operator: {}", id);

        Operator operator = operatorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Operator", id));

        // Check if operator has associated parameters
        List<Parameter> parameters = parameterRepository.findByOperatorId(id);
        if (parameters != null && !parameters.isEmpty()) {
            log.warn("Operator {} has {} parameters. Deleting them first.", id, parameters.size());
            parameterRepository.deleteAll(parameters);
        }

        operatorRepository.delete(operator);
        log.info("Operator deleted: {}", id);
    }

    @Override
    @Transactional
    public void publishOperator(Long id, String username) {
        log.info("Publishing operator: {} by user: {}", id, username);

        Operator operator = operatorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Operator", id));

        operator.setStatus(OperatorStatus.PUBLISHED);
        operatorRepository.save(operator);

        log.info("Operator published: {}", id);
    }

    @Override
    @Transactional
    public PageResponse<OperatorResponse> getPublishedOperators(Pageable pageable) {
        log.info("Getting published operators");

        Pageable pageRequest = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<Operator> page = operatorRepository.findByStatus(OperatorStatus.PUBLISHED, pageRequest);

        return PageResponse.of(page.map(this::mapToResponse));
    }

    private OperatorResponse mapToResponse(Operator operator) {
        return OperatorResponse.builder()
                .id(operator.getId())
                .name(operator.getName())
                .description(operator.getDescription())
                .language(operator.getLanguage())
                .status(operator.getStatus())
                .version(operator.getVersion())
                .isPublic(operator.getIsPublic())
                .parameters(operator.getParameters().stream()
                        .map(this::mapParameterToResponse)
                        .collect(Collectors.toList()))
                .createdAt(operator.getCreatedAt())
                .updatedAt(operator.getUpdatedAt())
                .createdBy(operator.getCreatedBy())
                .updatedBy(operator.getUpdatedBy())
                .build();
    }

    private ParameterResponse mapParameterToResponse(Parameter param) {
        return ParameterResponse.builder()
                .id(param.getId())
                .name(param.getName())
                .description(param.getDescription())
                .parameterType(param.getParameterType())
                .ioType(param.getIoType())
                .isRequired(param.getIsRequired())
                .defaultValue(param.getDefaultValue())
                .validationRules(param.getValidationRules())
                .orderIndex(param.getOrderIndex())
                .build();
    }

    private void saveParameters(List<OperatorRequest.Parameter> parameters, Operator operator) {
        if (parameters == null || parameters.isEmpty()) {
            return;
        }

        List<Parameter> existingParams = parameterRepository.findByOperatorId(operator.getId());
        for (OperatorRequest.Parameter paramRequest : parameters) {
            Parameter param;
            boolean isNew = true;

            // Find matching parameter
            for (Parameter existing : existingParams) {
                if (existing.getName().equals(paramRequest.getName())) {
                    param = existing;
                    isNew = false;
                    break;
                }
            }

            if (isNew) {
                param = new Parameter();
                param.setName(paramRequest.getName());
                param.setDescription(paramRequest.getDescription());
                param.setParameterType(paramRequest.getParameterType());
                param.setIoType(paramRequest.getIoType());
                param.setIsRequired(paramRequest.getIsRequired());
                param.setDefaultValue(paramRequest.getDefaultValue());
                param.setValidationRules(paramRequest.getValidationRules());
                param.setOrderIndex(paramRequest.getOrderIndex() != null ? paramRequest.getOrderIndex() : getNextOrderIndex(operator.getId()));
                param.setOperator(operator);
                parameterRepository.save(param);
            }
        }
    }

    private int getNextOrderIndex(Long operatorId) {
        List<Parameter> existingParams = parameterRepository.findByOperatorId(operatorId);
        return existingParams.stream()
                .mapToInt(Parameter::getOrderIndex)
                .max()
                .orElse(0) + 1;
    }
}
