package com.wonton.interpreter;

import com.wonton.lexical.TokenType;
import com.wonton.syntax.node.Node;
import com.wonton.syntax.node.expression.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class TreeWalkingInterpreter {

    public Object interpret(Node ast) {
        if(ast instanceof IntegerExpr node) {
            return node.getValue();
        }
        if (ast instanceof DecimalExpr node) {
            return node.getValue();
        }
        if (ast instanceof ParenExpr paren) {
            return interpret(paren.getExpression());
        }
        if (ast instanceof BinaryExpr binary) {
            TokenType operator = binary.getOperator().getType();
            Object leftOperand = interpret(binary.getLeft());
            Object rightOperand = interpret(binary.getRight());
            if (operator == TokenType.Plus) {
                BigDecimal leftValue = toBigDecimal(leftOperand);
                BigDecimal rightValue = toBigDecimal(rightOperand);
                return leftValue.add(rightValue);
            }
            if (operator == TokenType.Minus) {
                BigDecimal leftValue = toBigDecimal(leftOperand);
                BigDecimal rightValue = toBigDecimal(rightOperand);
                return leftValue.subtract(rightValue);
            }
            if (operator == TokenType.Star) {
                BigDecimal leftValue = toBigDecimal(leftOperand);
                BigDecimal rightValue = toBigDecimal(rightOperand);
                return leftValue.multiply(rightValue);
            }
            if (operator == TokenType.Slash) {
                // TODO 关于小数位除不尽和取舍的数学问题探讨和解决方案设计
                BigDecimal leftValue = toBigDecimal(leftOperand);
                BigDecimal rightValue = toBigDecimal(rightOperand);
                return leftValue.divide(rightValue, 2, RoundingMode.HALF_UP);
            }
        }
        if (ast instanceof UnaryExpr unary) {
            TokenType operator = unary.getOperator().getType();
            Object operand = interpret(unary.getOperand());
            if (operator == TokenType.Plus) {
                return toBigDecimal( operand );
            }
            if (operator == TokenType.Minus) {
                // 相反数（取反）
                return toBigDecimal(operand).negate();
            }
            if (operator == TokenType.NOT) {
                return !toBoolean(operand);
            }
        }
        throw new RuntimeException("Unknown node type: " + ast.getClass().getName());
    }

    private Boolean toBoolean(Object value) {
        return switch (value) {
            case null -> false;
            case Boolean bool -> bool;
            default -> throw new RuntimeException("不是布尔类型: " + value.getClass().getName());
        };
    }

    private BigDecimal toBigDecimal(Object value) {
        return switch (value) {
            case null -> throw new RuntimeException("值不存在，无法参与运算");
            case BigDecimal decimal -> decimal;
            case Long longValue -> BigDecimal.valueOf(longValue);
            case Number number -> BigDecimal.valueOf(number.doubleValue()); // 针对其它数值类型的兜底处理
            case Boolean bool -> throw new RuntimeException("布尔值不能参与算术运算:" + bool);
            default -> throw new RuntimeException("不是数值类型: " + value.getClass().getName());
        };
    }
}
