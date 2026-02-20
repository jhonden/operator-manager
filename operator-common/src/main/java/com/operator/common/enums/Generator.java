package com.operator.common.enums;

/**
 * Generator Enum
 * 生成方式枚举，用于标识算子是动态生成还是静态内置
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
public enum Generator {
    DYNAMIC("dynamic", "动态"),
    STATIC("static", "静态");

    private final String code;
    private final String name;

    Generator(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    /**
     * 根据代码获取枚举
     */
    public static Generator fromCode(String code) {
        for (Generator generator : Generator.values()) {
            if (generator.getCode().equals(code)) {
                return generator;
            }
        }
        throw new IllegalArgumentException("Invalid Generator code: " + code);
    }

    /**
     * 根据名称获取枚举
     */
    public static Generator fromName(String name) {
        for (Generator generator : Generator.values()) {
            if (generator.getName().equals(name)) {
                return generator;
            }
        }
        throw new IllegalArgumentException("Invalid Generator name: " + name);
    }
}
