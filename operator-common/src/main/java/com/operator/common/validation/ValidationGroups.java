package com.operator.common.validation;

/**
 * Validation groups for conditional validation
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
public interface ValidationGroups {

    /**
     * Validation group for draft operations - relaxed validation
     */
    interface Draft {
    }

    /**
     * Validation group for publish operations - strict validation
     */
    interface Publish {
    }
}
