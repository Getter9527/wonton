package com.wonton.interpreter;

import com.wonton.lexical.TokenType;
import com.wonton.syntax.node.Node;
import com.wonton.syntax.node.expression.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 树遍历解释器
 */
public class TreeWalkingInterpreter {

    public Value interpret(Node node) {
        if(node instanceof IntegerExpr intNode) {
            return new Value(RuntimeType.Number, intNode.getValue());
        }
        if (node instanceof DecimalExpr decimalNode) {
            return new Value(RuntimeType.Number, decimalNode.getValue());
        }
        if (node instanceof ParenExpr parenNode) {
            return interpret(parenNode.getExpression());
        }
        if (node instanceof BinaryExpr binNode) {
            TokenType operator = binNode.getOperator().getType();
            Object leftOperand = interpret(binNode.getLeft());
            Object rightOperand = interpret(binNode.getRight());
            if (operator == TokenType.Plus) {
                BigDecimal leftValue = toBigDecimal(leftOperand);
                BigDecimal rightValue = toBigDecimal(rightOperand);
                BigDecimal result = leftValue.add(rightValue);
                return new Value(RuntimeType.Number, result);
            }
            if (operator == TokenType.Minus) {
                BigDecimal leftValue = toBigDecimal(leftOperand);
                BigDecimal rightValue = toBigDecimal(rightOperand);
                BigDecimal result = leftValue.subtract(rightValue);
                return new Value(RuntimeType.Number, result);
            }
            if (operator == TokenType.Star) {
                BigDecimal leftValue = toBigDecimal(leftOperand);
                BigDecimal rightValue = toBigDecimal(rightOperand);
                BigDecimal result = leftValue.multiply(rightValue);
                return new Value(RuntimeType.Number, result);
            }
            if (operator == TokenType.Slash) {
                // TODO 关于小数位除不尽和取舍的数学问题探讨和解决方案设计
                BigDecimal leftValue = toBigDecimal(leftOperand);
                BigDecimal rightValue = toBigDecimal(rightOperand);
                BigDecimal result = leftValue.divide(rightValue, 2, RoundingMode.HALF_UP);
                return new Value(RuntimeType.Number, result);
            }
        }
        if (node instanceof UnaryExpr unaryNode) {
            TokenType operator = unaryNode.getOperator().getType();
            Object operand = interpret(unaryNode.getOperand());
            if (operator == TokenType.Plus) {
                BigDecimal result = toBigDecimal(operand);
                return new Value(RuntimeType.Number, result);
            }
            if (operator == TokenType.Minus) {
                // 相反数（取反）
                BigDecimal result = toBigDecimal(operand).negate();
                return new Value(RuntimeType.Number, result);
            }
            if (operator == TokenType.NOT) {
                Boolean result = !toBoolean(operand);
                return new Value(RuntimeType.Boolean, result);
            }
        }
        throw new RuntimeException("未知的语法树节点类型: " + node.getClass().getName());
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
