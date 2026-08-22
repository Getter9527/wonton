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
            return new Value(RuntimeType.Integer, intNode.getValue());
        }
        if (node instanceof DecimalExpr decimalNode) {
            return new Value(RuntimeType.Decimal, decimalNode.getValue());
        }
        if (node instanceof StringExpr strNode) {
            return new Value(RuntimeType.String, strNode.getValue());
        }
        if (node instanceof BooleanExpr boolNode) {
            return new Value(RuntimeType.Boolean, boolNode.getValue());
        }
        if (node instanceof ParenExpr parenNode) {
            return interpret(parenNode.getExpression());
        }
        if (node instanceof BinaryExpr binNode) {
            TokenType operator = binNode.getOperator().getType();
            Value left = interpret(binNode.getLeft());
            Value right = interpret(binNode.getRight());
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
        }
        if (node instanceof UnaryExpr unaryNode) {
            TokenType operator = unaryNode.getOperator().getType();
            Value operand = interpret(unaryNode.getOperand());
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


    private Value positive(Value operand) {
        // 恒等的，原样返回即可（目前还没想到有什么特殊操作）
        return operand;
    }

    private Value negative(Value operand) {
        if (isIntegerOperation(operand)) {
            var negateValue = - ((Long) operand.getValue());
            return new Value(operand.getRuntimeType(), negateValue);
        }
        if (isDecimalOperation(operand)) {
            var negateValue = ((BigDecimal) operand.getValue()).negate();
            return new Value(operand.getRuntimeType(), negateValue);
        }
        throw new UnsupportedOperationException("不支持的操作数：" + operand.getRuntimeType());
    }

    private Value not(Value operand) {
        if (isBooleanOperation(operand)) {
            Boolean result = !toBoolean(operand);
            return new Value(RuntimeType.Boolean, result);
        }
        throw new UnsupportedOperationException("不支持的操作数：" + operand.getRuntimeType());
    }

    private Value add(Value left, Value right) {
        // 只要有1个操作数是字符串，就进行字符串拼接
        if (isStringOperation(left, right)) {
            String leftValue = anyToString(left);
            String rightValue = anyToString(right);
            return new Value(RuntimeType.String, leftValue + rightValue);
        }
        // 不合法操作：如果不是字符串，且包含 boolean or null，则报错
        if (isBooleanOperation(left, right) || isNullOperation(left, right)) {
            throw new RuntimeException("不合法的加法运算");
        }
        // 如果都是整数，那么就进行整数加法
        if (isIntegerOperation(left, right)) {
            Long leftValue = (Long) left.getValue();
            Long rightValue = (Long) right.getValue();
            return new Value(RuntimeType.Integer, leftValue + rightValue);
        }
        // 如果其中1个是小数，那么就进行小数加法
        if (isDecimalOperation(left, right)) {
            BigDecimal leftValue = toDecimal(left);
            BigDecimal rightValue = toDecimal(right);
            BigDecimal result = leftValue.add(rightValue);
            return new Value(RuntimeType.Decimal, result);
        }
        throw new RuntimeException("未知的运算类型: " + left.getRuntimeType() + " " + right.getRuntimeType());
    }

    private Value subtract(Value left, Value right) {
        if (isBooleanOperation(left, right) || isNullOperation(left, right)) {
            throw new RuntimeException("不合法的减法运算");
        }
        if (isIntegerOperation(left, right)) {
            Long leftValue = (Long) left.getValue();
            Long rightValue = (Long) right.getValue();
            return new Value(RuntimeType.Integer, leftValue - rightValue);
        }
        if (isDecimalOperation(left, right)) {
            BigDecimal leftValue = toDecimal(left);
            BigDecimal rightValue = toDecimal(right);
            BigDecimal result = leftValue.subtract(rightValue);
            return new Value(RuntimeType.Decimal, result);
        }
        throw new RuntimeException("未知的运算类型: " + left.getRuntimeType() + " " + right.getRuntimeType());
    }

    private Value multiply(Value left, Value right) {
        if (isBooleanOperation(left, right) || isNullOperation(left, right)) {
            throw new RuntimeException("不合法的乘法运算");
        }
        if (isIntegerOperation(left, right)) {
            Long leftValue = (Long) left.getValue();
            Long rightValue = (Long) right.getValue();
            return new Value(RuntimeType.Integer, leftValue * rightValue);
        }
        if (isDecimalOperation(left, right)) {
            BigDecimal leftValue = toDecimal(left);
            BigDecimal rightValue = toDecimal(right);
            BigDecimal result = leftValue.multiply(rightValue);
            return new Value(RuntimeType.Decimal, result);
        }
        throw new RuntimeException("未知的运算类型: " + left.getRuntimeType() + " " + right.getRuntimeType());
    }

    private Value divide(Value left, Value right) {
        if (isBooleanOperation(left, right) || isNullOperation(left, right)) {
            throw new RuntimeException("不合法的除法运算");
        }
        if (isIntegerOperation(left, right)) {
            Long leftValue = (Long) left.getValue();
            Long rightValue = (Long) right.getValue();
            return new Value(RuntimeType.Integer, leftValue / rightValue);
        }
        if (isDecimalOperation(left, right)) {
            BigDecimal leftValue = toDecimal(left);
            BigDecimal rightValue = toDecimal(right);
            BigDecimal result = leftValue.divide(rightValue, 2, RoundingMode.HALF_UP);
            return new Value(RuntimeType.Decimal, result);
        }
        throw new RuntimeException("未知的运算类型: " + left.getRuntimeType() + " " + right.getRuntimeType());
    }

    private String anyToString(Value value) {
        RuntimeType runtimeType = value.getRuntimeType();
        if (runtimeType == RuntimeType.Null) {
            return "null";
        }
        if (runtimeType == RuntimeType.Boolean || runtimeType == RuntimeType.Integer) {
            return String.valueOf(value.getValue());
        }
        if (runtimeType == RuntimeType.Decimal) {
            // 去掉多余的尾零，并避免科学计数法
            return ((BigDecimal) value.getValue()).stripTrailingZeros().toPlainString();
        }
        return String.valueOf(value.getValue());
    }

    private Boolean toBoolean(Value value) {
        return switch (value.getRuntimeType()) {
            case Boolean -> (Boolean) value.getValue();
            default -> throw new RuntimeException("不是布尔类型: " + value.getClass().getName());
        };
    }

    private BigDecimal toDecimal(Value value) {
        return switch (value.getRuntimeType()) {
            case Decimal -> (BigDecimal) value.getValue();
            case Integer -> BigDecimal.valueOf((Long) value.getValue());
            default -> throw new RuntimeException("不是数值类型: " + value.getClass().getName());
        };
    }

    private boolean isStringOperation(Value left, Value right) {
        return left.getRuntimeType() == RuntimeType.String || right.getRuntimeType() == RuntimeType.String;
    }


    private boolean isNullOperation(Value operand) {
        return operand.getRuntimeType() == RuntimeType.Null;
    }

    private boolean isNullOperation(Value left, Value right) {
        return left.getRuntimeType() == RuntimeType.Null || right.getRuntimeType() == RuntimeType.Null;
    }


    private boolean isBooleanOperation(Value operand) {
        return operand.getRuntimeType() == RuntimeType.Boolean;
    }

    private boolean isBooleanOperation(Value left, Value right) {
        return left.getRuntimeType() == RuntimeType.Boolean || right.getRuntimeType() == RuntimeType.Boolean;
    }


    private boolean isIntegerOperation(Value operand) {
        return operand.getRuntimeType() == RuntimeType.Integer;
    }

    private boolean isIntegerOperation(Value left, Value right) {
        return left.getRuntimeType() == RuntimeType.Integer && right.getRuntimeType() == RuntimeType.Integer;
    }


    private boolean isDecimalOperation(Value operand) {
        return operand.getRuntimeType() == RuntimeType.Decimal;
    }

    private boolean isDecimalOperation(Value left, Value right) {
        // 如果都是数值类型，且其中有一个是小数
        return RuntimeType.isNumber(left.getRuntimeType())
                && RuntimeType.isNumber(right.getRuntimeType())
                && (left.getRuntimeType() == RuntimeType.Decimal || right.getRuntimeType() == RuntimeType.Decimal);
    }
}
