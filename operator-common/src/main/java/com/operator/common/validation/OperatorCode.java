package com.operator.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Operator Code Validation Annotation
 * 算子编码校验注解
 *
 * 格式要求：
 * - 字符串格式，长度1-64
 * - 只允许字母、数字和下划线组成
 * - 数字不能开头
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Target({FIELD, PARAMETER})
@Retention(RUNTIME)
@Constraint(validatedBy = OperatorCodeValidator.class)
@Documented
public @interface OperatorCode {

    String message() default "Invalid operator code format. Must start with a letter or underscore, followed by letters, numbers, or underscores, 1-64 characters";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
