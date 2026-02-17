package com.operator.core.operator.repository;

import com.operator.core.operator.domain.Parameter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Parameter entity
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Repository
public interface ParameterRepository extends JpaRepository<Parameter, Long> {

    /**
     * Find parameters by operator
     */
    List<Parameter> findByOperatorIdOrderByOrderIndexAsc(Long operatorId);

    /**
     * Find parameters by operator and type
     */
    List<Parameter> findByOperatorIdAndIoTypeOrderByOrderIndexAsc(Long operatorId, com.operator.common.enums.IOType ioType);

    /**
     * Delete parameters by operator
     */
    void deleteByOperatorId(Long operatorId);
}
