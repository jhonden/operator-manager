package com.operator.common.enums;

/**
 * Data Format Enum
 * 数据格式枚举，用于标识算子处理的原数据类型
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
public enum DataFormat {
    STATIC_MML("1", "静态MML"),
    DYNAMIC_MML("10", "动态MML"),
    HUA_TONG("12", "话统");

    private final String code;
    private final String name;

    DataFormat(String code, String name) {
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
    public static DataFormat fromCode(String code) {
        for (DataFormat format : DataFormat.values()) {
            if (format.getCode().equals(code)) {
                return format;
            }
        }
        throw new IllegalArgumentException("Invalid DataFormat code: " + code);
    }

    /**
     * 根据名称获取枚举
     */
    public static DataFormat fromName(String name) {
        for (DataFormat format : DataFormat.values()) {
            if (format.getName().equals(name)) {
                return format;
            }
        }
        throw new IllegalArgumentException("Invalid DataFormat name: " + name);
    }
}
