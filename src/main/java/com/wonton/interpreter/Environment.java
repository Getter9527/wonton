package com.wonton.interpreter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 运行时环境，作用域
 */
public class Environment {

    // 嵌套环境
    private final Environment parent;
    // 当前作用域的变量表（常量也存储在这里）
    private final Map<String, RuntimeValue> variables = new HashMap<>();
    // 用于记录哪些变量的值是不可变的，也就是用户角度看到的常量
    private final Set<String> constants = new HashSet<>();

    public Environment() {
        // 当前环境为顶级环境时，无父级环境
        this(null);
    }

    public Environment(Environment parent) {
        this.parent = parent;
    }

    public void define(String name, RuntimeValue value, boolean isConstant) {
        if (variables.containsKey(name)) {
            throw new RuntimeException("变量重复定义，不允许重复声明：" + name);
        }
        variables.put(name, value);
        // 如果是常量，则将该变量标记为常量
        if (isConstant) {
            constants.add(name);
        }
    }

    public RuntimeValue get(String name) {
        // 先从当前环境变量表中查找
        if (variables.containsKey(name)) {
            return variables.get(name);
        }
        // 向上查找
        if (parent != null) {
            return parent.get(name);
        }
        throw new RuntimeException("上下文中未定义该变量：" + name);
    }

    public void assign(String name, RuntimeValue newValue) {
        if (variables.containsKey(name)) {
            // 常量不可赋值
            if (constants.contains(name)) {
                throw new RuntimeException("常量不能被重新赋值：" + name);
            }
            // 变量可赋值
            variables.put(name, newValue);
            return;
        }
        // 如果当前层处理不了，向上递归
        if (parent != null) {
            parent.assign(name, newValue);
            return;
        }
        throw new RuntimeException("上下文中未定义该变量：" + name);
    }

}
