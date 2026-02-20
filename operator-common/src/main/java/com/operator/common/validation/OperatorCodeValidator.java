package com.operator.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Operator Code Validator
 * 算子编码校验器实现
 *
 * 格式规则：
 * - 必须是字符串
 * - 长度 1-64
 * - 首字符必须是字母或下划线
 * - 后续字符可以是字母、数字或下划线
 * - 正则表达式: ^[a-zA-Z_][a-zA-Z0-9_]{0,63}$
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
public class OperatorCodeValidator implements ConstraintValidator<OperatorCode, String> {

    // 正则表达式：首字符是字母或下划线，后续是字母数字下划线，总长度1-64
    private static final String PATTERN = "^[a-zA-Z_][a-zA-Z0-9_]{0,63}$";

    @Override
    public void initialize(OperatorCode constraintAnnotation) {
        // 初始化方法，可以读取注解参数
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // null 或空值交给 @NotNull 或 @NotBlank 校验
        if (value == null || value.trim().isEmpty()) {
            return true;
        }

        return value.matches(PATTERN);
    }
}
