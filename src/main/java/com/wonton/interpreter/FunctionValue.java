package com.wonton.interpreter;

import com.wonton.lexical.Token;
import com.wonton.syntax.node.statement.BlockStmt;

import java.util.List;

/**
 * 函数值类型
 * <p>函数作为一等公民，以值的形式存储在 Environment 中。
 * closure 记录函数声明时所在的环境，为闭包提供作用域链。</p>
 */
public class FunctionValue {

    private final String name;
    private final List<Token> params;
    private final BlockStmt body;
    private final Environment closure;

    /**
     * @param name    函数名
     * @param params  形参列表
     * @param body    函数体
     * @param closure 声明处的环境（闭包）
     */
    public FunctionValue(String name, List<Token> params, BlockStmt body, Environment closure) {
        this.name = name;
        this.params = params;
        this.body = body;
        this.closure = closure;
    }

    /**
     * 获取函数名
     *
     * @return 函数名
     */
    public String getName() {
        return name;
    }

    /**
     * 获取形参列表
     *
     * @return 形参 Token 列表
     */
    public List<Token> getParams() {
        return params;
    }

    /**
     * 获取函数体
     *
     * @return 函数体语句块
     */
    public BlockStmt getBody() {
        return body;
    }

    /**
     * 获取闭包环境
     *
     * @return 声明处的环境
     */
    public Environment getClosure() {
        return closure;
    }

    @Override
    public String toString() {
        return "<function " + name + ">";
    }
}
