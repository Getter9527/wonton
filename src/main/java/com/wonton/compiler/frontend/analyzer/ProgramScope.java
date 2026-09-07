package com.wonton.compiler.frontend.analyzer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 编译期作用域（链式结构，与运行时 Environment 对称）
 */
public class ProgramScope {

    // 嵌套作用域
    private final ProgramScope parent;
    private final Map<String, Symbol> symbols = new HashMap<>();
    private final Set<String> constants = new HashSet<>();

    // 存储当前环境的返回值类型
    private SemanticType returnType = SemanticType.VOID_INSTANCE;

    public ProgramScope() {
        this(null);
    }

    public ProgramScope(ProgramScope parent) {
        this.parent = parent;
    }

    public ProgramScope getParent() {
        return parent;
    }

    public SemanticType getReturnType() {
        return returnType;
    }

    public void setReturnType(SemanticType returnType) {
        this.returnType = returnType;
    }

    /**
     * 定义符号
     */
    public void define(String name, SemanticType type, boolean isConstant) {
        // 如果当前作用域中，存在同名的符号，则报错
        if (symbols.containsKey(name)) {
            throw new RuntimeException("当前作用域已经存在相同名称的符号：" + name);
        }
        // 添加到符号表
        symbols.put(name, new Symbol(name, type));
        // 如果是常量，则将该符号标记为常量
        if (isConstant) {
            constants.add(name);
        }
    }

    /**
     * 查找符号（向上递归）
     */
    public Symbol resolve(String name) {
        // 如果当前作用域中就存在该符号
        if (symbols.containsKey(name)) {
            return symbols.get(name);
        }
        // 当前作用域未找到时，向上查找
        if (parent != null) {
            return parent.resolve(name);
        }
        // 如果还是找不到
        // TODO 报错还是返回null，有待考虑
        throw new RuntimeException("未能从上下文中找到对应的符号：" + name);
    }

    /**
     * 判断当前作用域中是否存在指定的符号
     */
    public boolean hasLocal(String name) {
        return symbols.containsKey(name);
    }

    /**
     * 判断当前作用域中，是否存在指定的常量
     */
    public boolean hasConstant(String name) {
        return constants.contains(name);
    }

}
