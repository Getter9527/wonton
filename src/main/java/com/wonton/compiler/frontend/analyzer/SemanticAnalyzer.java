package com.wonton.compiler.frontend.analyzer;

import com.wonton.compiler.frontend.lexical.Token;
import com.wonton.compiler.frontend.syntax.node.Node;
import com.wonton.compiler.frontend.syntax.node.Program;
import com.wonton.compiler.frontend.syntax.node.expression.*;
import com.wonton.compiler.frontend.syntax.node.statement.*;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * 语义分析器
 * <p>完成作用域构建、类型检查、常量保护等语义验证</p>
 */
public class SemanticAnalyzer {

    private final TypeChecker typeChecker = new TypeChecker();

    /**
     * 分析整个 AST（入口）
     */
    public void analyze(Node node, ProgramScope scope) {
        switch (node) {
            case Program program -> analyzeProgram(program, scope);
            case Stmt stmt       -> analyzeStmt(stmt, scope);
            case Expr expr       -> analyzeExpr(expr, scope);
            default -> throw new IllegalStateException("analyze -> Unexpected value: " + node);
        }
    }

    private void analyzeProgram(Program program, ProgramScope scope) {
        for (Stmt stmt : program.getStmts()) {
            analyze(stmt, scope);
        }
    }

    private void analyzeStmt(Stmt stmt, ProgramScope scope) {
        switch (stmt) {
            case PrintStmt               printStmt  -> analyzePrint(printStmt, scope);
            case VariableDeclarationStmt varStmt    -> analyzeVariableDeclaration(varStmt, scope);
            case ConstantDeclarationStmt constStmt  -> analyzeConstantDeclaration(constStmt, scope);
            case AssignmentStmt          assignStmt -> analyzeAssignment(assignStmt, scope);
            case IfStmt                  ifStmt     -> analyzeIf(ifStmt, scope);
            case WhileStmt               whileStmt  -> analyzeWhile(whileStmt, scope);
            case FunctionDeclarationStmt funcStmt   -> analyzeFunctionDeclaration(funcStmt, scope);
            case BlockStmt               blockStmt  -> analyzeBlock(blockStmt, scope);
            case ReturnStmt              returnStmt -> analyzeReturn(returnStmt, scope);
            case ExpressionStmt          exprStmt   -> analyzeExpr(exprStmt.getExpr(), scope);
            default -> throw new IllegalStateException("analyzeStmt -> Unexpected value: " + stmt);
        }
    }

    private void analyzeExpr(Expr expr, ProgramScope scope) {
        switch (expr) {
            case BinaryExpr       binary   -> analyzeBinaryExpr(binary, scope);
            case UnaryExpr        unary    -> analyzeUnaryExpr(unary, scope);
            case LogicalExpr      logical  -> analyzeLogicalExpr(logical, scope);
            case ParenExpr        paren    -> analyzeParenExpr(paren, scope);
            case VariableExpr     variable -> analyzeVariableExpr(variable, scope);
            case FunctionCallExpr call     -> analyzeFunctionCallExpr(call, scope);
            default -> throw new IllegalStateException("analyzeExpr -> Unexpected value: " + expr);
        }
    }

    /**
     * 函数声明语句
     */
    private void analyzeFunctionDeclaration(FunctionDeclarationStmt func, ProgramScope scope) {
        // 检查函数重复定义
        String funcName = func.getName().getLexeme();

        if (scope.hasLocal(funcName)) {
            throw new SemanticAnalysisException("相同作用域中，存在重复名称的函数：" + funcName, func.getName().getLine());
        }

        // 推导形参类型类型
        List<SemanticType> paramTypes = new ArrayList<>();
        for (Token param : func.getParams()) {
            // TODO 需要根据类型注解来决定形参是什么类型
            // TODO var a = 1;
            paramTypes.add(SemanticType.UNKNOWN_INSTANCE);
        }

        // TODO 推导函数返回值类型
        SemanticType returnType = SemanticType.UNKNOWN_INSTANCE;

        // 注册函数符号
        SemanticType funcType = SemanticType.newFunctionType(returnType, paramTypes);
        scope.define(funcName, funcType, false);

        // 进入函数内部，创建一个全新的作用域
        ProgramScope subScope = new ProgramScope(scope);

        // 注册形参
        List<Token> params = func.getParams();
        for (int i = 0; i < params.size(); i++) {
            String paramName = params.get(i).getLexeme();
            subScope.define(paramName, paramTypes.get(i), false);
        }

        // 递归分析函数体（返回值也在这一步被分析）
        analyzeBlock(func.getBody(), subScope);
    }

    /**
     * 变量声明语句
     */
    private void analyzeVariableDeclaration(VariableDeclarationStmt variable, ProgramScope scope) {
        String varName = variable.getIdentifier().getLexeme();

        // 检查变量是否重复定义
        if (scope.hasLocal(varName)) {
            throw new SemanticAnalysisException("变量重复定义：" + varName, variable.getIdentifier().getLine());
        }

        // 声明的类型
        SemanticType varType = SemanticType.from(variable.getType());
        // 如果初始化表达式不为空（说白了就是有赋值动作）
        if (variable.getInitializer() != null) {
            // 先对表达式，做语义化分析检查；然后才可以被后续使用
            analyzeExpr(variable.getInitializer(), scope);
            // 赋的值类型（动态推导）
            SemanticType initializerType = typeChecker.inferType(variable.getInitializer(), scope);
            // 判断声明类型和赋值类型是否匹配
            if (!varType.isCompatible(initializerType)) {
                throw new SemanticAnalysisException(
                        MessageFormat.format(
                                "类型不匹配：变量 {0} 声明类型为 {1}，初始化表达式类型为 {2}，在 {3} 行",
                                varName, varType, initializerType, variable.getIdentifier().getLine()
                        )
                );
            }
        }
        scope.define(varName, varType, false);
    }

    /**
     * 常量声明语句
     */
    private void analyzeConstantDeclaration(ConstantDeclarationStmt constant, ProgramScope scope) {
        String constName = constant.getIdentifier().getLexeme();
        if (scope.hasLocal(constName)) {
            throw new SemanticAnalysisException("常量重复定义：" + constName, constant.getIdentifier().getLine());
        }

        // 常量必须初始化
        if (constant.getInitializer() == null) {
            throw new SemanticAnalysisException("常量声明必须初始化：" + constName, constant.getIdentifier().getLine());
        }
        // 常量声明的类型
        SemanticType constType = SemanticType.from(constant.getType());
        // 对表达式做语义分析
        analyzeExpr(constant.getInitializer(), scope);
        // 推导常量类型
        SemanticType initializerType = typeChecker.inferType(constant.getInitializer(), scope);
        if (!constType.isCompatible(initializerType)) {
            throw new SemanticAnalysisException(
                    MessageFormat.format(
                            "类型不匹配：常量 {0} 声明类型为 {1}，初始化表达式类型为 {2}，在 {3} 行",
                            constName, constType, initializerType, constant.getIdentifier().getLine()
                    )
            );
        }
        scope.define(constName, constType, true);
    }

    /**
     * 赋值语句
     */
    private void analyzeAssignment(AssignmentStmt assign, ProgramScope scope) {
        String varName = assign.getIdentifier().getLexeme();

        // 确保变量已定义
        Symbol varSymbol = scope.resolve(varName);
        if (varSymbol == null) {
            throw new SemanticAnalysisException("未定义的变量：" + varName, assign.getIdentifier().getLine());
        }

        // 常量不可赋值
        if (scope.hasConstant(varName)) {
            throw new SemanticAnalysisException("常量不允许被重新赋值：" + varName, assign.getIdentifier().getLine());
        }

        // 检查赋值表达式
        analyzeExpr(assign.getValue(), scope);

        // 推断赋值类型
        SemanticType valueType = typeChecker.inferType(assign.getValue(), scope);

        // 检查类型兼容性
        if (!varSymbol.getType().isCompatible(valueType)) {
            throw new SemanticAnalysisException(
                    MessageFormat.format(
                            "类型不匹配：变量 {0} 类型为 {1}，赋值表达式类型为 {2}，在 {3} 行",
                            varName,
                            varSymbol.getType(),
                            valueType,
                            assign.getIdentifier().getLine()
                    )
            );
        }
    }

    /**
     * 语句块
     */
    private void analyzeBlock(BlockStmt block, ProgramScope scope) {
        if (block == null) {
            return;
        }

        // 创建子作用域
        ProgramScope subScope = new ProgramScope(scope);

        for (Stmt stmt : block.getStmts()) {
            analyze(stmt, subScope);
        }
    }

    /**
     * if 语句
     */
    private void analyzeIf(IfStmt ifStmt, ProgramScope scope) {
        // 检查条件表达式
        analyzeExpr(ifStmt.getCondition(), scope);

        SemanticType conditionType = typeChecker.inferType(ifStmt.getCondition(), scope);
        if (!conditionType.isBoolean()) {
            throw new SemanticAnalysisException("if 语句的条件表达式类型必须是布尔类型");
        }

        // 检查if语句块（创建子作用域的责任由block负责）
        analyzeBlock(ifStmt.getIfBlock(), scope);
        // 检查else语句块
        if (ifStmt.getElseBlock() != null) {
            analyzeBlock(ifStmt.getElseBlock(), scope);
        }
    }

    /**
     * while 语句
     */
    private void analyzeWhile(WhileStmt whileStmt, ProgramScope scope) {
        // 检查条件表达式
        analyzeExpr(whileStmt.getCondition(), scope);

        SemanticType conditionType = typeChecker.inferType(whileStmt.getCondition(), scope);
        if (!conditionType.isBoolean()) {
            throw new SemanticAnalysisException("while 语句的条件表达式类型必须是布尔类型");
        }

        // 检查while语句块（创建子作用域的责任由block负责）
        analyzeBlock(whileStmt.getWhileBlock(), scope);
    }

    private void analyzeReturn(ReturnStmt returnStmt, ProgramScope scope) {
        if (returnStmt.getValue() != null) {
            analyzeExpr(returnStmt.getValue(), scope);
        }
        // TODO 推导返回值类型，然后返回值类型检查，确保函数声明的返回值类型一致
    }

    /**
     * 打印语句
     */
    private void analyzePrint(PrintStmt printStmt, ProgramScope scope) {
        Expr printExpr = printStmt.getValue();
        if (printExpr != null) {
            analyzeExpr(printExpr, scope);
        }
    }

    /**
     * 函数调用表达式
     */
    private void analyzeFunctionCallExpr(FunctionCallExpr callExpr, ProgramScope scope) {
        // 有可能是 函数名 或 链式调用，所以这里采用向下递归处理
        analyzeExpr(callExpr.getCallee(), scope);
        // 检查实参
        for (Expr arg : callExpr.getArgs()) {
            analyzeExpr(arg, scope);
        }
    }

    /**
     * 二元表达式
     */
    private void analyzeBinaryExpr(BinaryExpr binaryExpr, ProgramScope scope) {
        analyzeExpr(binaryExpr.getLeft(), scope);
        analyzeExpr(binaryExpr.getRight(), scope);
    }

    /**
     * 一元表达式
     */
    private void analyzeUnaryExpr(UnaryExpr unaryExpr, ProgramScope scope) {
        analyzeExpr(unaryExpr.getOperand(), scope);
    }

    /**
     * 逻辑表达式
     */
    private void analyzeLogicalExpr(LogicalExpr logicalExpr, ProgramScope scope) {
        analyzeExpr(logicalExpr.getLeft(), scope);
        analyzeExpr(logicalExpr.getRight(), scope);
    }

    /**
     * 括号表达式
     */
    private void analyzeParenExpr(ParenExpr parenExpr, ProgramScope scope) {
        analyzeExpr(parenExpr.getExpression(), scope);
    }

    /**
     * 变量表达式
     */
    private void analyzeVariableExpr(VariableExpr varExpr, ProgramScope scope) {
        // 检查上下文中是否存在该变量
        String varName = varExpr.getIdentifier().getLexeme();
        if (scope.resolve(varName) == null) {
            throw new SemanticAnalysisException("未定义的变量：" + varName, varExpr.getIdentifier().getLine());
        }
    }

}
