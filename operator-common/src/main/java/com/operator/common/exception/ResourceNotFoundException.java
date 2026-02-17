package com.operator.common.exception;

/**
 * Exception thrown when a resource is not found
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue));
    }

    public ResourceNotFoundException(String resourceName, Long id) {
        this(resourceName, "id", id);
    }
}
