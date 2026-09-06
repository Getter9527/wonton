package com.wonton.compiler.frontend.analyzer;

import java.util.List;

/**
 * 语义分析器类型系统
 * <p>用于静态类型检查和符号表管理</p>
 */
public class SemanticType {

    // 下列这些类型都是可以复用的，采用单例
    public static final SemanticType INTEGER  = SemanticType.newBasicType(Tag.INTEGER);
    public static final SemanticType DECIMAL  = SemanticType.newBasicType(Tag.DECIMAL);
    public static final SemanticType STRING   = SemanticType.newBasicType(Tag.STRING);
    public static final SemanticType BOOLEAN  = SemanticType.newBasicType(Tag.BOOLEAN);
    public static final SemanticType VOID     = SemanticType.newBasicType(Tag.VOID);
    public static final SemanticType NULL     = SemanticType.newBasicType(Tag.NULL);
    public static final SemanticType UNKNOWN  = SemanticType.newBasicType(Tag.UNKNOWN);

    private final Tag tag;

    // 函数类型专用字段
    private final SemanticType returnType; // 函数返回值类型
    private final List<SemanticType> paramTypes; // 函数的参数类型列表

    private SemanticType(Tag tag, SemanticType returnType, List<SemanticType> paramTypes) {
        this.tag = tag;
        this.returnType = returnType;
        this.paramTypes = paramTypes;
    }

    public static SemanticType newBasicType(Tag tag) {
        return new SemanticType(tag, null, null);
    }

    public static SemanticType newFunctionType(SemanticType returnType, List<SemanticType> paramTypes) {
        return new SemanticType(Tag.FUNCTION, returnType, paramTypes);
    }

    public Tag getTag() {
        return tag;
    }

    public SemanticType getReturnType() {
        return returnType;
    }

    public List<SemanticType> getParamTypes() {
        return paramTypes;
    }

    public boolean isNumber() {
        return tag == Tag.INTEGER || tag == Tag.DECIMAL;
    }

    public boolean isInteger() {
        return tag == Tag.INTEGER;
    }

    public boolean isDecimal() {
        return tag == Tag.DECIMAL;
    }

    public boolean isBoolean() {
        return tag == Tag.BOOLEAN;
    }

    public boolean isString() {
        return tag == Tag.STRING;
    }

    public boolean isVoid() {
        return tag == Tag.VOID;
    }

    public boolean isNullType() {
        return tag == Tag.NULL;
    }

    public boolean isFunction() {
        return tag == Tag.FUNCTION;
    }

    /**
     * 类型兼容性检查
     */
    public boolean isCompatible(SemanticType other) {
        // UNKNOWN 兼容一切（类型推断未完成时的兜底）
        if (this.tag == Tag.UNKNOWN || other.tag == Tag.UNKNOWN) {
            return true;
        }

        // 向下兼容 DECIMAL => INTEGER
        if (this.isDecimal() && (other.isDecimal() || this.isInteger())) {
            return true;
        }

        // 类型完全相同
        return this.tag == other.tag;
    }

    /**
     * 基础类型枚举
     */
    public enum Tag {
        INTEGER,    // 整型
        DECIMAL,    // 小数类型
        STRING,     // 字符串
        BOOLEAN,    // 布尔型
        VOID,       // 无返回值
        FUNCTION,   // 函数类型
        NULL,       // 空值
        UNKNOWN     // 未知类型（初始状态）
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SemanticType that)) return false;
        return this.tag == that.tag;
    }

    @Override
    public int hashCode() {
        return tag.hashCode();
    }

    @Override
    public String toString() {
        return switch (tag) {
            case INTEGER  -> "integer";
            case DECIMAL  -> "decimal";
            case STRING   -> "string";
            case BOOLEAN  -> "boolean";
            case VOID     -> "void";
            case NULL     -> "null";
            case FUNCTION -> "function(" + (paramTypes != null ? paramTypes.size() + " params" : "?") + ") -> " + returnType;
            case UNKNOWN  -> "unknown";
        };
    }
}
