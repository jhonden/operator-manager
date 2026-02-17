package com.operator.common.exception;

/**
 * Exception thrown when user is unauthorized
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException() {
        super("You are not authorized to perform this action");
    }
}
