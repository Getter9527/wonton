package com.wonton.interpreter;

import com.wonton.lexical.TokenType;
import com.wonton.syntax.node.Node;
import com.wonton.syntax.node.expression.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 树遍历解释器
 */
public class Interpreter {

    public RuntimeValue interpret(Node node) {
        if(node instanceof IntegerExpr intNode) {
            return RuntimeValue.of(intNode.getValue());
        }
        if (node instanceof DecimalExpr decimalNode) {
            return RuntimeValue.of(decimalNode.getValue());
        }
        if (node instanceof StringExpr strNode) {
            return RuntimeValue.of(strNode.getValue());
        }
        if (node instanceof BooleanExpr boolNode) {
            return RuntimeValue.of(boolNode.getValue());
        }
        if (node instanceof ParenExpr parenNode) {
            return interpret(parenNode.getExpression());
        }
        if (node instanceof BinaryExpr binNode) {
            TokenType operator = binNode.getOperator().getType();
            RuntimeValue left = interpret(binNode.getLeft());
            RuntimeValue right = interpret(binNode.getRight());
            if (operator == TokenType.Plus) {
                return add(left, right);
            }
            if (operator == TokenType.Minus) {
                return subtract(left, right);
            }
            if (operator == TokenType.Star) {
                return multiply(left, right);
            }
            if (operator == TokenType.Slash) {
                // TODO 关于小数位除不尽和取舍的数学问题探讨和解决方案设计
                return divide(left, right);
            }
            if (operator == TokenType.Less) {
                return less(left, right);
            }
            if (operator == TokenType.LessEqual) {
                return lessEqual(left, right);
            }
            if (operator == TokenType.Greater) {
                return greater(left, right);
            }
            if (operator == TokenType.GreaterEqual) {
                return greaterEqual(left, right);
            }
        }
        if (node instanceof UnaryExpr unaryNode) {
            TokenType operator = unaryNode.getOperator().getType();
            RuntimeValue operand = interpret(unaryNode.getOperand());
            if (operator == TokenType.Plus) {
                return positive(operand);
            }
            if (operator == TokenType.Minus) {
                // 相反数（取反）
                return negative(operand);
            }
            if (operator == TokenType.Not) {
                return not(operand);
            }
        }
        throw new RuntimeException("未知的语法树节点类型: " + node.getClass().getName());
    }

    private RuntimeValue less(RuntimeValue left, RuntimeValue right) {
        if (left.isBoolean() && right.isBoolean()) {
            Boolean leftValue = toBoolean(left);
            Boolean rightValue = toBoolean(right);
            return RuntimeValue.of(leftValue.compareTo(rightValue) < 0);
        }
        if (left.isNumeric() && right.isNumeric()) {
            BigDecimal leftValue = toDecimal(left);
            BigDecimal rightValue = toDecimal(right);
            return RuntimeValue.of(leftValue.compareTo(rightValue) < 0);
        }
        throw new UnsupportedOperationException(
                String.format("不支持比较的数据类型。操作类型=less 左操作数=%s 右操作数=%s", left.getValue(), right.getValue())
        );
    }

    private RuntimeValue lessEqual(RuntimeValue left, RuntimeValue right) {
        if (left.isBoolean() && right.isBoolean()) {
            Boolean leftValue = toBoolean(left);
            Boolean rightValue = toBoolean(right);
            return RuntimeValue.of(leftValue.compareTo(rightValue) <= 0);
        }
        if (left.isNumeric() && right.isNumeric()) {
            BigDecimal leftValue = toDecimal(left);
            BigDecimal rightValue = toDecimal(right);
            return RuntimeValue.of(leftValue.compareTo(rightValue) <= 0);
        }
        throw new UnsupportedOperationException(
                String.format("不支持比较的数据类型。操作类型=lessEqual 左操作数=%s 右操作数=%s", left.getValue(), right.getValue())
        );
    }

    private RuntimeValue greater(RuntimeValue left, RuntimeValue right) {
        if (left.isBoolean() && right.isBoolean()) {
            Boolean leftValue = toBoolean(left);
            Boolean rightValue = toBoolean(right);
            return RuntimeValue.of(leftValue.compareTo(rightValue) > 0);
        }
        if (left.isNumeric() && right.isNumeric()) {
            BigDecimal leftValue = toDecimal(left);
            BigDecimal rightValue = toDecimal(right);
            return RuntimeValue.of(leftValue.compareTo(rightValue) > 0);
        }
        throw new UnsupportedOperationException(
                String.format("不支持比较的数据类型。操作类型=greater 左操作数=%s 右操作数=%s", left.getValue(), right.getValue())
        );
    }

    private RuntimeValue greaterEqual(RuntimeValue left, RuntimeValue right) {
        if (left.isBoolean() && right.isBoolean()) {
            Boolean leftValue = toBoolean(left);
            Boolean rightValue = toBoolean(right);
            return RuntimeValue.of(leftValue.compareTo(rightValue) >= 0);
        }
        if (left.isNumeric() && right.isNumeric()) {
            BigDecimal leftValue = toDecimal(left);
            BigDecimal rightValue = toDecimal(right);
            return RuntimeValue.of(leftValue.compareTo(rightValue) >= 0);
        }
        throw new UnsupportedOperationException(
                String.format("不支持比较的数据类型。操作类型=greaterEqual 左操作数=%s 右操作数=%s", left.getValue(), right.getValue())
        );
    }


    private RuntimeValue positive(RuntimeValue operand) {
        // 恒等的，原样返回即可（目前还没想到有什么特殊操作）
        return operand;
    }

    private RuntimeValue negative(RuntimeValue operand) {
        if (isIntegerOperation(operand)) {
            Long negateValue = - ((Long) operand.getValue());
            return RuntimeValue.of(negateValue);
        }
        if (isDecimalOperation(operand)) {
            BigDecimal negateValue = ((BigDecimal) operand.getValue()).negate();
            return RuntimeValue.of(negateValue);
        }
        throw new UnsupportedOperationException("不支持的操作数：" + operand.getType());
    }

    private RuntimeValue not(RuntimeValue operand) {
        if (isBooleanOperation(operand)) {
            Boolean result = !toBoolean(operand);
            return RuntimeValue.of(result);
        }
        throw new UnsupportedOperationException("不支持的操作数：" + operand.getType());
    }

    private RuntimeValue add(RuntimeValue left, RuntimeValue right) {
        // 只要有1个操作数是字符串，就进行字符串拼接
        if (isStringOperation(left, right)) {
            String leftValue = anyToString(left);
            String rightValue = anyToString(right);
            return RuntimeValue.of(leftValue + rightValue);
        }
        // 不合法操作：如果不是字符串，且包含 boolean or null，则报错
        if (isBooleanOperation(left, right) || isNullOperation(left, right)) {
            throw new RuntimeException("不合法的加法运算");
        }
        // 如果都是整数，那么就进行整数加法
        if (isIntegerOperation(left, right)) {
            Long leftValue = (Long) left.getValue();
            Long rightValue = (Long) right.getValue();
            return RuntimeValue.of(leftValue + rightValue);
        }
        // 如果其中1个是小数，那么就进行小数加法
        if (isDecimalOperation(left, right)) {
            BigDecimal leftValue = toDecimal(left);
            BigDecimal rightValue = toDecimal(right);
            BigDecimal result = leftValue.add(rightValue);
            return RuntimeValue.of(result);
        }
        throw new RuntimeException("未知的运算类型: " + left.getType() + " " + right.getType());
    }

    private RuntimeValue subtract(RuntimeValue left, RuntimeValue right) {
        if (isBooleanOperation(left, right) || isNullOperation(left, right)) {
            throw new RuntimeException("不合法的减法运算");
        }
        if (isIntegerOperation(left, right)) {
            Long leftValue = (Long) left.getValue();
            Long rightValue = (Long) right.getValue();
            return RuntimeValue.of(leftValue - rightValue);
        }
        if (isDecimalOperation(left, right)) {
            BigDecimal leftValue = toDecimal(left);
            BigDecimal rightValue = toDecimal(right);
            BigDecimal result = leftValue.subtract(rightValue);
            return RuntimeValue.of(result);
        }
        throw new RuntimeException("未知的运算类型: " + left.getType() + " " + right.getType());
    }

    private RuntimeValue multiply(RuntimeValue left, RuntimeValue right) {
        if (isBooleanOperation(left, right) || isNullOperation(left, right)) {
            throw new RuntimeException("不合法的乘法运算");
        }
        if (isIntegerOperation(left, right)) {
            Long leftValue = (Long) left.getValue();
            Long rightValue = (Long) right.getValue();
            return RuntimeValue.of(leftValue * rightValue);
        }
        if (isDecimalOperation(left, right)) {
            BigDecimal leftValue = toDecimal(left);
            BigDecimal rightValue = toDecimal(right);
            BigDecimal result = leftValue.multiply(rightValue);
            return RuntimeValue.of(result);
        }
        throw new RuntimeException("未知的运算类型: " + left.getType() + " " + right.getType());
    }

    private RuntimeValue divide(RuntimeValue left, RuntimeValue right) {
        if (isBooleanOperation(left, right) || isNullOperation(left, right)) {
            throw new RuntimeException("不合法的除法运算");
        }
        if (isIntegerOperation(left, right)) {
            Long leftValue = (Long) left.getValue();
            Long rightValue = (Long) right.getValue();
            return RuntimeValue.of(leftValue / rightValue);
        }
        if (isDecimalOperation(left, right)) {
            BigDecimal leftValue = toDecimal(left);
            BigDecimal rightValue = toDecimal(right);
            BigDecimal result = leftValue.divide(rightValue, 2, RoundingMode.HALF_UP);
            return RuntimeValue.of(result);
        }
        throw new RuntimeException("未知的运算类型: " + left.getType() + " " + right.getType());
    }

    private String anyToString(RuntimeValue value) {
        RuntimeValue.Type runtimeType = value.getType();
        if (runtimeType.isNull()) {
            return "null";
        }
        if (runtimeType.isBoolean() || runtimeType.isInteger()) {
            return String.valueOf(value.getValue());
        }
        if (runtimeType.isDecimal()) {
            // 去掉多余的尾零，并避免科学计数法
            return ((BigDecimal) value.getValue()).stripTrailingZeros().toPlainString();
        }
        return String.valueOf(value.getValue());
    }

    private Boolean toBoolean(RuntimeValue value) {
        return switch (value.getType()) {
            case Boolean -> (Boolean) value.getValue();
            default -> throw new RuntimeException("不是布尔类型: " + value.getClass().getName());
        };
    }

    private BigDecimal toDecimal(RuntimeValue value) {
        return switch (value.getType()) {
            case Decimal -> (BigDecimal) value.getValue();
            case Integer -> BigDecimal.valueOf((Long) value.getValue());
            default -> throw new RuntimeException("不是数值类型: " + value.getClass().getName());
        };
    }

    private boolean isStringOperation(RuntimeValue left, RuntimeValue right) {
        return left.getType().isString() || right.getType().isString();
    }

    private boolean isNullOperation(RuntimeValue left, RuntimeValue right) {
        return left.getType().isNull() || right.getType().isNull();
    }


    private boolean isBooleanOperation(RuntimeValue operand) {
        return operand.getType().isBoolean();
    }

    private boolean isBooleanOperation(RuntimeValue left, RuntimeValue right) {
        return left.getType().isBoolean() || right.getType().isBoolean();
    }


    private boolean isIntegerOperation(RuntimeValue operand) {
        return operand.getType().isInteger();
    }

    private boolean isIntegerOperation(RuntimeValue left, RuntimeValue right) {
        return left.getType().isInteger() && right.getType().isInteger();
    }


    private boolean isDecimalOperation(RuntimeValue operand) {
        return operand.getType().isDecimal();
    }

    private boolean isDecimalOperation(RuntimeValue left, RuntimeValue right) {
        // 如果都是数值类型，且其中有一个是小数
        boolean isNumeric = left.getType().isNumeric() && right.getType().isNumeric();
        boolean hasDecimal = left.getType().isDecimal() || right.getType().isDecimal();
        return isNumeric && hasDecimal;
    }
}
