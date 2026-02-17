package com.operator.common.exception;

/**
 * Exception thrown when request is invalid
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
