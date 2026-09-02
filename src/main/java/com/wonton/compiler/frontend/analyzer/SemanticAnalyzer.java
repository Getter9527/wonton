package com.wonton.compiler.frontend.analyzer;

import com.wonton.compiler.frontend.syntax.node.Node;
import com.wonton.compiler.frontend.syntax.node.expression.BinaryExpr;
import com.wonton.compiler.frontend.syntax.node.expression.Expr;
import com.wonton.compiler.frontend.syntax.node.expression.VariableExpr;
import com.wonton.compiler.frontend.syntax.node.statement.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 语义分析器
 * <p>完成类型检查、符号表构建、作用域分析等语义验证</p>
 */
public class SemanticAnalyzer {

    private final Map<String, Type> symbolTable = new HashMap<>();
    private final TypeChecker typeChecker = new TypeChecker();

    /**
     * 分析整个 AST
     *
     * @param node 语法树节点
     */
    public void analyze(Node node) {
        if (node instanceof FunctionDeclarationStmt func) {
            analyzeFunction(func);
        } else if (node instanceof VariableDeclarationStmt var) {
            analyzeVariable(var);
        } else if (node instanceof AssignmentStmt assign) {
            analyzeAssignment(assign);
        }
    }

    /**
     * 分析函数声明
     *
     * @param func 函数声明节点
     */
    private void analyzeFunction(FunctionDeclarationStmt func) {
        String funcName = func.getName().getLexeme();

        // 将函数加入符号表
        Type funcType = new Type(Type.BaseType.FUNCTION, func.getParams());
        symbolTable.put(funcName, funcType);

        // 递归分析函数体
        analyze(func.getBody());
    }

    /**
     * 分析变量声明
     *
     * @param var 变量声明节点
     */
    private void analyzeVariable(VariableDeclarationStmt var) {
        String varName = var.getIdentifier().getLexeme();
        Type varType = typeChecker.inferType(var.getInitializer());

        // 检查变量是否重复定义
        if (symbolTable.containsKey(varName)) {
            throw new RuntimeException(
                    "变量重复定义：" + varName
            );
        }

        // 将变量加入符号表
        symbolTable.put(varName, varType);
    }

    /**
     * 分析赋值语句
     *
     * @param assign 赋值语句节点
     */
    private void analyzeAssignment(AssignmentStmt assign) {
        String varName = assign.getIdentifier().getLexeme();

        // 检查变量是否已定义
        if (!symbolTable.containsKey(varName)) {
            throw new RuntimeException("未定义的变量：" + varName);
        }

        // 类型检查
        Type assignType = typeChecker.inferType(assign.getValue());
        Type varType = symbolTable.get(varName);

        // 检查类型兼容性
        if (!varType.isCompatible(assignType)) {
            throw new RuntimeException(
                    "类型不匹配：变量 " + varName + " 类型为 " + varType +
                            "，赋值表达式类型为 " + assignType
            );
        }
    }

    /**
     * 分析语句块
     *
     * @param block 语句块
     */
    private void analyze(BlockStmt block) {
        if (block == null) {
            return;
        }

        List<Stmt> stmts = block.getStmts();
        for (Stmt stmt : stmts) {
            analyze(stmt);
        }
    }

    /**
     * 分析表达式
     *
     * @param expr 表达式
     */
    private void analyze(Expr expr) {
        if (expr == null) {
            return;
        }

        // 根据表达式类型递归分析
        if (expr instanceof BinaryExpr) {
            BinaryExpr binary = (BinaryExpr) expr;
            analyze(binary.getLeft());
            analyze(binary.getRight());
        } else if (expr instanceof VariableExpr) {
            VariableExpr var = (VariableExpr) expr;
            String varName = var.getIdentifier().getLexeme();

            // 检查变量是否已定义
            if (!symbolTable.containsKey(varName)) {
                throw new RuntimeException("未定义的变量：" + varName);
            }
        }
        // 其他表达式类型后续扩展
    }

    /**
     * 获取符号表中的类型信息
     *
     * @param name 标识符名称
     * @return 类型，不存在返回 null
     */
    public Type getType(String name) {
        return symbolTable.get(name);
    }

}
