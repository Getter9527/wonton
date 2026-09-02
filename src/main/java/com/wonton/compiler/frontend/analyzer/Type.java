package com.wonton.compiler.frontend.analyzer;

import com.wonton.compiler.frontend.lexical.Token;
import com.wonton.interpreter.RuntimeValue;

import java.util.List;

/**
 * 语义分析器类型系统
 * <p>用于静态类型检查和符号表管理</p>
 */
public class Type {

    /**
     * 静态类型常量
     */
    public static final Type INTEGER = new Type(BaseType.INTEGER);
    public static final Type DECIMAL = new Type(BaseType.DECIMAL);
    public static final Type STRING = new Type(BaseType.STRING);
    public static final Type BOOLEAN = new Type(BaseType.BOOLEAN);
    public static final Type VOID = new Type(BaseType.VOID);
    public static final Type UNKNOWN = new Type(BaseType.UNKNOWN);

    private final BaseType baseType;
    private final List<Token> params; // 函数参数列表（仅函数类型使用）

    /**
     * 构造函数
     *
     * @param baseType 基础类型
     */
    public Type(BaseType baseType) {
        this.baseType = baseType;
        this.params = null;
    }

    /**
     * 构造函数（函数类型专用）
     *
     * @param returnType 返回值类型
     * @param params     参数列表
     */
    public Type(BaseType returnType, List<Token> params) {
        this.baseType = returnType;
        this.params = params;
    }

    /**
     * 获取基础类型
     *
     * @return 基础类型
     */
    public BaseType getBaseType() {
        return baseType;
    }

    /**
     * 获取函数参数列表
     *
     * @return 参数列表，非函数类型返回 null
     */
    public List<Token> getParams() {
        return params;
    }

    /**
     * 根据 RuntimeValue.Type 创建对应语义类型
     *
     * @param runtimeType 运行时类型
     * @return 语义分析类型
     */
    public static Type fromRuntimeType(RuntimeValue.Type runtimeType) {
        return switch (runtimeType) {
            case Integer -> INTEGER;
            case Decimal -> DECIMAL;
            case String -> STRING;
            case Boolean -> BOOLEAN;
            case Void -> VOID;
            case Function -> new Type(BaseType.FUNCTION, null);
            default -> UNKNOWN;
        };
    }

    /**
     * 类型兼容性检查
     * <p>整数和小数可以互相转换</p>
     *
     * @param other 另一个类型
     * @return 是否兼容
     */
    public boolean isCompatible(Type other) {
        if (this.equals(other)) {
            return true;
        }

        // 整数和小数可以互相转换
        boolean selfNumeric = this.baseType == BaseType.INTEGER || this.baseType == BaseType.DECIMAL;
        boolean otherNumeric = other.baseType == BaseType.INTEGER || other.baseType == BaseType.DECIMAL;

        return selfNumeric && otherNumeric;
    }

    /**
     * 判断类型是否相等
     *
     * @param o 另一个对象
     * @return 相等返回 true
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Type type = (Type) o;
        return baseType == type.baseType;
    }

    @Override
    public int hashCode() {
        return baseType.hashCode();
    }

    @Override
    public String toString() {
        return switch (baseType) {
            case INTEGER -> "integer";
            case DECIMAL -> "decimal";
            case STRING -> "string";
            case BOOLEAN -> "boolean";
            case VOID -> "void";
            case FUNCTION -> "function";
            case UNKNOWN -> "unknown";
            default -> "unknown";
        };
    }

    /**
     * 基础类型枚举
     */
    public enum BaseType {
        INTEGER,    // 整型
        DECIMAL,    // 小数类型
        STRING,     // 字符串
        BOOLEAN,    // 布尔型
        VOID,       // 无返回值
        FUNCTION,   // 函数类型
        UNKNOWN     // 未知类型（初始状态）
    }

}
