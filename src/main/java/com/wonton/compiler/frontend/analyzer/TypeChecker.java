package com.wonton.compiler.frontend.analyzer;

import com.wonton.compiler.frontend.syntax.node.expression.*;
import com.wonton.compiler.frontend.syntax.node.statement.BlockStmt;
import com.wonton.compiler.frontend.syntax.node.statement.Stmt;

import java.util.List;

/**
 * 类型推导器
 * <p>根据表达式推断其类型</p>
 */
public class TypeChecker {

    private final Type[] types = new Type[10];

    /**
     * 根据表达式节点推断类型
     *
     * @param expr 表达式节点
     * @return 推断的类型
     */
    public Type inferType(Expr expr) {
        if (expr == null) {
            return Type.VOID;
        }

        // 根据表达式类型推断
        if (expr instanceof IntegerExpr) {
            return Type.INTEGER;
        } else if (expr instanceof DecimalExpr) {
            return Type.DECIMAL;
        } else if (expr instanceof StringExpr) {
            return Type.STRING;
        } else if (expr instanceof BooleanExpr) {
            return Type.BOOLEAN;
        } else if (expr instanceof NullExpr) {
            return Type.UNKNOWN;
        } else if (expr instanceof VariableExpr) {
            // 变量类型从符号表获取，这里返回 UNKNOWN
            // 实际使用时需要在 SemanticAnalyzer 中维护符号表
            return Type.UNKNOWN;
        }

        return Type.UNKNOWN;
    }

    /**
     * 推断语句块的类型
     *
     * @param block 语句块
     * @return 语句块类型（通常为 void）
     */
    public Type inferType(BlockStmt block) {
        if (block == null) {
            return Type.VOID;
        }

        List<Stmt> stmts = block.getStmts();
        if (stmts.isEmpty()) {
            return Type.VOID;
        }

        // 获取最后一个语句的类型
        Stmt lastStmt = stmts.get(stmts.size() - 1);
        // 这里简化处理，返回 VOID
        return Type.VOID;
    }

    /**
     * 推断函数的返回类型
     *
     * @param block 函数体
     * @return 返回类型
     */
    public Type inferFunctionReturnType(BlockStmt block) {
        if (block == null) {
            return Type.VOID;
        }

        // 遍历函数体，查找 return 语句
        List<Stmt> stmts = block.getStmts();
        for (int i = stmts.size() - 1; i >= 0; i--) {
            Stmt stmt = stmts.get(i);
            // TODO: 实现 return 语句的类型推断
            // 这里简化处理，返回 VOID
            return Type.VOID;
        }

        return Type.VOID;
    }

}
