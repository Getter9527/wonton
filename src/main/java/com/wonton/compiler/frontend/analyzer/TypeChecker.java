package com.wonton.compiler.frontend.analyzer;

import com.wonton.compiler.frontend.lexical.Token;
import com.wonton.compiler.frontend.syntax.node.expression.*;
import com.wonton.compiler.frontend.syntax.node.statement.BlockStmt;
import com.wonton.compiler.frontend.syntax.node.expression.FunctionCallExpr;
import com.wonton.compiler.frontend.syntax.node.statement.ReturnStmt;
import com.wonton.compiler.frontend.syntax.node.statement.Stmt;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * 类型检查与推导
 * <p>根据表达式推断其类型</p>
 */
public class TypeChecker {

    public TypeChecker() {}

    /**
     * 类型推断
     */
    public SemanticType inferType(Expr expr, ProgramScope scope) {
        return switch (expr) {
            case null -> SemanticType.VOID;
            // 根据表达式类型推断
            case IntegerExpr intExpr -> SemanticType.INTEGER;
            case DecimalExpr decimalExpr -> SemanticType.DECIMAL;
            case StringExpr strExpr -> SemanticType.STRING;
            case NullExpr nullExpr -> SemanticType.NULL;
            case BooleanExpr booleanExpr -> SemanticType.BOOLEAN;
            case LogicalExpr logicalExpr -> SemanticType.BOOLEAN;
            case VariableExpr varExpr -> inferVariable(varExpr, scope);
            case BinaryExpr binExpr -> inferBinary(binExpr, scope);
            case UnaryExpr unaryExpr -> inferUnary(unaryExpr, scope);
            case ParenExpr parenExpr -> inferParen(parenExpr, scope);
            case FunctionCallExpr callExpr -> inferFunctionCall(callExpr, scope);
            default -> SemanticType.UNKNOWN;
        };

    }

    private SemanticType inferParen(ParenExpr parenExpr, ProgramScope scope) {
        return inferType(parenExpr.getExpression(), scope);
    }

    /**
     * 变量表达式：从作用域中查找类型
     */
    private SemanticType inferVariable(VariableExpr varExpr, ProgramScope scope) {
        String name = varExpr.getIdentifier().getLexeme();
        Symbol symbol = scope.resolve(name);
        if (symbol == null) {
            throw new SemanticAnalysisException("未定义的变量：" + name, varExpr.getIdentifier().getLine());
        }
        return symbol.getType();
    }

    /**
     * 推导二元表达式的运算结果类型
     */
    private SemanticType inferBinary(BinaryExpr binExpr, ProgramScope scope) {

        SemanticType leftType = inferType(binExpr.getLeft(), scope);
        SemanticType rightType = inferType(binExpr.getRight(), scope);
        Token operator = binExpr.getOperator();

        switch (operator.getType()) {
            case Plus -> {
                if (allNumbers(leftType, rightType)) {
                    if (hasDecimal(leftType, rightType)) {
                        return SemanticType.DECIMAL;
                    }
                    return SemanticType.INTEGER;
                }
                if (hasString(leftType, rightType)) {
                    return SemanticType.STRING;
                }
            }
            case Minus, Star, Slash, Modulo -> {
                if (allNumbers(leftType, rightType)) {
                    if (hasDecimal(leftType, rightType)) {
                        return SemanticType.DECIMAL;
                    }
                    return SemanticType.INTEGER;
                }
            }
            case Caret -> {
                // 幂运算比较特殊，由于负整数的幂运算结果可能为小数，因此返回 DECIMAL
                if (allNumbers(leftType, rightType)) {
                    return SemanticType.DECIMAL;
                }
            }
            case Less, LessEqual, Greater, GreaterEqual -> {
                // TODO 目前仅支持数值之间的比较，其它可后续扩展
                if (allNumbers(leftType, rightType)) {
                    return SemanticType.BOOLEAN;
                }
            }
            case Equalx2, NotEqual -> {
                // 支持数值和布尔值之间的比较
                if (allNumbers(leftType, rightType) || allBooleans(leftType, rightType) || allStrings(leftType, rightType)) {
                    return SemanticType.BOOLEAN;
                }
            }
            default -> {
                return SemanticType.UNKNOWN;
            }
        }
        // 不满足上述规则之一时，则走到这
        throw new SemanticAnalysisException(
                MessageFormat.format(
                        "二元运算失败，不支持的数据类型：{0} {1} {2}，在 {3} 行",
                        leftType.getTag(),
                        operator.getLexeme(),
                        rightType.getTag(),
                        operator.getLine()
                )
        );
    }

    /**
     * 推导一元表达式的运算结果类型
     */
    private SemanticType inferUnary(UnaryExpr unaryExpr, ProgramScope scope) {
        Token operator = unaryExpr.getOperator();
        SemanticType operandType = inferType(unaryExpr.getOperand(), scope); // 操作数类型
        switch (operator.getType()) {
            case Plus, Minus -> {
                // 仅作用于数值类型
                if (operandType.isNumber()) {
                    return operandType;
                }
            }
            case Not -> {
                // 仅作用于布尔类型
                if (operandType.isBoolean()) {
                    return SemanticType.BOOLEAN;
                }
            }
            default -> {
                return SemanticType.UNKNOWN;
            }
        }
        // 不满足上述规则之一时，则走到这
        throw new SemanticAnalysisException(
                MessageFormat.format(
                        "一元运算失败，不支持的数据类型：{0} {1}，在 {2} 行",
                        operator.getLexeme(),
                        operandType.getTag(),
                        operator.getLine()
                )
        );
    }

    /**
     * 推导函数调用的运算结果类型
     */
    private SemanticType inferFunctionCall(FunctionCallExpr callExpr, ProgramScope scope) {
        // callee可能是VarExpr，或者是FunctionCallExpr
        // 如果是VarExpr，那么去symbols表中查找这个"函数变量"，来推导其是否为函数
        SemanticType calleeType = inferType(callExpr.getCallee(), scope);
        if (!calleeType.isFunction()) {
            throw new SemanticAnalysisException("不可调用非函数类型：" + calleeType);
        }

        // 参数个数检查
        List<SemanticType> paramTypes = calleeType.getParamTypes();
        List<Expr> args = callExpr.getArgs();
        int paramsSize = paramTypes == null ? 0 : paramTypes.size();
        int argsSize = args == null ? 0 : args.size();
        if (paramsSize != argsSize) {
            throw new SemanticAnalysisException(
                    MessageFormat.format(
                            "函数参数个数不匹配：形参 {0} 个，实参 {1} 个",
                            paramsSize,
                            argsSize
                    )
            );
        }

        // 参数列表检查
        for (int i = 0; i < argsSize; i++) {
            SemanticType paramType = paramTypes.get(i);
            SemanticType argType = inferType(args.get(i), scope);
            // 如果参数类型不同，且不兼容，则报错
            if (!paramType.isCompatible(argType)) {
                throw new SemanticAnalysisException(
                        MessageFormat.format(
                                "第 {0} 个参数类型不匹配：形参 {1}，实参 {2}",
                                i + 1,
                                paramType,
                                argType
                        )
                );
            }
        }
        return calleeType.getReturnType();
    }

    /**
     * 推断函数的返回类型
     *
     * @param block 函数体
     * @return 返回类型
     */
    private SemanticType inferReturnType(BlockStmt block, ProgramScope scope) {
        // 函数体为空时，返回值类型为 void
        if (isEmptyBlockStmt(block)) {
            return SemanticType.VOID;
        }

        // 遍历函数体，查找 return 语句
        List<Stmt> stmts = block.getStmts();
        // 倒着遍历，能更快地找到第一个 return 语句
        for (int i = stmts.size() - 1; i >= 0; i--) {
            Stmt stmt = stmts.get(i);
            // TODO 一段代码中，return语句通常会有好多
            // TODO 如果是强类型语言，取其中一个return就能推测出来具体类型；如果是弱类型语言，那就难搞了
            if (stmt instanceof ReturnStmt returnStmt) {
                return inferType(returnStmt.getValue(), scope);
            }
            // TODO 需要考虑 if, else, while, for, 语句块等嵌套情况
        }

        return SemanticType.VOID;
    }

    /**
     * 是否为空语句块？
     */
    private boolean isEmptyBlockStmt(BlockStmt block) {
        return block == null || block.getStmts() == null || block.getStmts().isEmpty();
    }

    private  <T> Stream<T> toSafeStream(T[] array) {
        if (array == null) {
            return Stream.empty();
        }
        return Arrays.stream(array);
    }

    private boolean hasDecimal(SemanticType... types) {
        return toSafeStream(types).anyMatch(SemanticType::isDecimal);
    }

    private boolean hasString(SemanticType... types) {
        return toSafeStream(types).anyMatch(SemanticType::isString);
    }

    private boolean allStrings(SemanticType... types) {
        return toSafeStream(types).allMatch(SemanticType::isString);
    }

    private boolean allBooleans(SemanticType... types) {
        return toSafeStream(types).allMatch(SemanticType::isBoolean);
    }

    private boolean allIntegers(SemanticType... types) {
        return toSafeStream(types).allMatch(SemanticType::isInteger);
    }

    private boolean allNumbers(SemanticType... types) {
        return toSafeStream(types).allMatch(SemanticType::isNumber);
    }

}
