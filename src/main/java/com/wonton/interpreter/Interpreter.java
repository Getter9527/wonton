package com.wonton.interpreter;

import com.wonton.lexical.TokenType;
import com.wonton.syntax.node.Node;
import com.wonton.syntax.node.expression.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.stream.Stream;

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

        if (node instanceof BinaryExpr binNode) {

            TokenType operator = binNode.getOperator().getType();
            RuntimeValue left = interpret(binNode.getLeft());
            RuntimeValue right = interpret(binNode.getRight());

            if (operator == TokenType.Star) {
                return multiply(left, right);
            }
            if (operator == TokenType.Slash) {
                // TODO 关于小数位除不尽和取舍的数学问题探讨和解决方案设计
                return divide(left, right);
            }

            if (operator == TokenType.Plus) {
                return add(left, right);
            }
            if (operator == TokenType.Minus) {
                return subtract(left, right);
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

            if (operator == TokenType.Equalx2) {
                return equality(left, right);
            }
            if (operator == TokenType.NotEqual) {
                return notEquality(left, right);
            }

            if (operator == TokenType.Modulo) {
                return modulo(left, right);
            }
        }

        if (node instanceof LogicalExpr logicalNode) {

            TokenType operator = logicalNode.getOperator().getType();

            if (operator == TokenType.And) {
                return and(logicalNode.getLeft(), logicalNode.getRight());
            }
            if (operator == TokenType.Or) {
                return or(logicalNode.getLeft(), logicalNode.getRight());
            }
        }

        throw new RuntimeException("未知的语法树节点类型: " + node.getClass().getName());
    }

    private RuntimeValue or(Expr left, Expr right) {
        RuntimeValue leftRuntimeVal = interpret(left);
        // 我们要求左值必须是布尔类型
        if (leftRuntimeVal.isBoolean()) {
            Boolean leftValue = (Boolean) leftRuntimeVal.getValue();
            // 两者满足其一即可
            if (leftValue) {
                return leftRuntimeVal;
            }
            // 当左值为false，必须向后求右值
            return interpret(right);
        }
        throw new UnsupportedOperationException(
                String.format("不支持的数据类型。操作类型=or 左操作数=%s", leftRuntimeVal.getValue())
        );
    }

    private RuntimeValue and(Expr left, Expr right) {
        RuntimeValue leftRuntimeVal = interpret(left);
        if (leftRuntimeVal.isBoolean()) {
            Boolean leftValue = (Boolean) leftRuntimeVal.getValue();
            // 前者不满足，后者也无需计算
            if (!leftValue) {
                return leftRuntimeVal;
            }
            return interpret(right);
        }
        throw new UnsupportedOperationException(
                String.format("不支持的数据类型。操作类型=and 左操作数=%s", leftRuntimeVal.getValue())
        );
    }

    private RuntimeValue modulo(RuntimeValue left, RuntimeValue right) {
        if (allNumbers(left, right)) {
            BigDecimal leftValue = anyToDecimal(left);
            BigDecimal rightValue = anyToDecimal(right);
            return RuntimeValue.of(leftValue.remainder(rightValue));
        }
        throw new UnsupportedOperationException(
                String.format("不支持的数据类型。操作类型=modulo 左操作数=%s 右操作数=%s", left.getValue(), right.getValue())
        );
    }

    private RuntimeValue less(RuntimeValue left, RuntimeValue right) {
        if (allStrings(left, right)) {
            String leftValue = (String) left.getValue();
            String rightValue = (String) right.getValue();
            return RuntimeValue.of(leftValue.compareTo(rightValue) < 0);
        }
        if (allNumbers(left, right)) {
            BigDecimal leftValue = anyToDecimal(left);
            BigDecimal rightValue = anyToDecimal(right);
            return RuntimeValue.of(leftValue.compareTo(rightValue) < 0);
        }
        throw new UnsupportedOperationException(
                String.format("不支持的数据类型。操作类型=Less 左操作数=%s 右操作数=%s", left.getValue(), right.getValue())
        );
    }

    private RuntimeValue lessEqual(RuntimeValue left, RuntimeValue right) {
        if (allStrings(left, right)) {
            String leftValue = (String) left.getValue();
            String rightValue = (String) right.getValue();
            return RuntimeValue.of(leftValue.compareTo(rightValue) <= 0);
        }
        if (allNumbers(left, right)) {
            BigDecimal leftValue = anyToDecimal(left);
            BigDecimal rightValue = anyToDecimal(right);
            return RuntimeValue.of(leftValue.compareTo(rightValue) <= 0);
        }
        throw new UnsupportedOperationException(
                String.format("不支持的数据类型。操作类型=LessEqual 左操作数=%s 右操作数=%s", left.getValue(), right.getValue())
        );
    }

    private RuntimeValue greater(RuntimeValue left, RuntimeValue right) {
        if (allStrings(left, right)) {
            String leftValue = (String) left.getValue();
            String rightValue = (String) right.getValue();
            return RuntimeValue.of(leftValue.compareTo(rightValue) > 0);
        }
        if (allNumbers(left, right)) {
            BigDecimal leftValue = anyToDecimal(left);
            BigDecimal rightValue = anyToDecimal(right);
            return RuntimeValue.of(leftValue.compareTo(rightValue) > 0);
        }
        throw new UnsupportedOperationException(
                String.format("不支持的数据类型。操作类型=Greater 左操作数=%s 右操作数=%s", left.getValue(), right.getValue())
        );
    }

    private RuntimeValue greaterEqual(RuntimeValue left, RuntimeValue right) {
        if (allStrings(left, right)) {
            String leftValue = (String) left.getValue();
            String rightValue = (String) right.getValue();
            return RuntimeValue.of(leftValue.compareTo(rightValue) >= 0);
        }
        if (allNumbers(left, right)) {
            BigDecimal leftValue = anyToDecimal(left);
            BigDecimal rightValue = anyToDecimal(right);
            return RuntimeValue.of(leftValue.compareTo(rightValue) >= 0);
        }
        throw new UnsupportedOperationException(
            String.format("不支持的数据类型。操作类型=GreaterEqual 左操作数=%s 右操作数=%s", left.getValue(), right.getValue())
        );
    }

    private RuntimeValue equality(RuntimeValue left, RuntimeValue right) {
        if (allStrings(left, right)) {
            String leftValue = (String) left.getValue();
            String rightValue = (String) right.getValue();
            return RuntimeValue.of(leftValue.equals(rightValue));
        }
        if (allBooleans(left, right)) {
            boolean leftValue = (boolean) left.getValue();
            boolean rightValue = (boolean) right.getValue();
            return RuntimeValue.of(leftValue == rightValue);
        }
        if (allNumbers(left, right)) {
            BigDecimal leftValue = anyToDecimal(left);
            BigDecimal rightValue = anyToDecimal(right);
            return RuntimeValue.of(leftValue.compareTo(rightValue) == 0);
        }
        throw new UnsupportedOperationException(
            String.format("不支持的数据类型。操作类型=Equalx2 左操作数=%s 右操作数=%s", left.getValue(), right.getValue())
        );
    }

    private RuntimeValue notEquality(RuntimeValue left, RuntimeValue right) {
        if (allStrings(left, right)) {
            String leftValue = (String) left.getValue();
            String rightValue = (String) right.getValue();
            return RuntimeValue.of(!leftValue.equals(rightValue));
        }
        if (allBooleans(left, right)) {
            boolean leftValue = (boolean) left.getValue();
            boolean rightValue = (boolean) right.getValue();
            return RuntimeValue.of(leftValue != rightValue);
        }
        if (allNumbers(left, right)) {
            BigDecimal leftValue = anyToDecimal(left);
            BigDecimal rightValue = anyToDecimal(right);
            return RuntimeValue.of(leftValue.compareTo(rightValue) != 0);
        }
        throw new UnsupportedOperationException(
            String.format("不支持的数据类型。操作类型=NotEqual 左操作数=%s 右操作数=%s", left.getValue(), right.getValue())
        );
    }


    private RuntimeValue positive(RuntimeValue operand) {
        // 恒等的，原样返回即可（目前还没想到有什么特殊操作）
        return operand;
    }

    private RuntimeValue negative(RuntimeValue operand) {
        if (operand.isInteger()) {
            Long negateValue = - ((Long) operand.getValue());
            return RuntimeValue.of(negateValue);
        }
        if (operand.isDecimal()) {
            BigDecimal negateValue = ((BigDecimal) operand.getValue()).negate();
            return RuntimeValue.of(negateValue);
        }
        throw new UnsupportedOperationException("不支持的操作数：" + operand.getType());
    }

    private RuntimeValue not(RuntimeValue operand) {
        if (operand.isBoolean()) {
            Boolean result = !anyToBoolean(operand);
            return RuntimeValue.of(result);
        }
        throw new UnsupportedOperationException("不支持的操作数：" + operand.getType());
    }

    private RuntimeValue add(RuntimeValue left, RuntimeValue right) {
        // 只要有1个操作数是字符串，就进行字符串拼接
        if (hasString(left, right)) {
            String leftValue = anyToString(left);
            String rightValue = anyToString(right);
            return RuntimeValue.of(leftValue + rightValue);
        }
        // 不合法操作：如果不是字符串，且包含 boolean or null，则报错
        if (hasBoolean(left, right) || hasNullType(left, right)) {
            throw new RuntimeException("不合法的加法运算");
        }
        // 如果都是整数，那么就进行整数加法
        if (allIntegers(left, right)) {
            Long leftValue = (Long) left.getValue();
            Long rightValue = (Long) right.getValue();
            return RuntimeValue.of(leftValue + rightValue);
        }
        // 如果其中1个是小数，那么就进行小数加法
        if (allNumbers(left, right) && hasDecimal(left, right)) {
            BigDecimal leftValue = anyToDecimal(left);
            BigDecimal rightValue = anyToDecimal(right);
            BigDecimal result = leftValue.add(rightValue);
            return RuntimeValue.of(result);
        }
        throw new RuntimeException("未知的运算类型: " + left.getType() + " " + right.getType());
    }

    private RuntimeValue subtract(RuntimeValue left, RuntimeValue right) {
        if (hasBoolean(left, right) || hasNullType(left, right)) {
            throw new RuntimeException("不合法的减法运算");
        }
        if (allIntegers(left, right)) {
            Long leftValue = (Long) left.getValue();
            Long rightValue = (Long) right.getValue();
            return RuntimeValue.of(leftValue - rightValue);
        }
        if (allNumbers(left, right) && hasDecimal(left, right)) {
            BigDecimal leftValue = anyToDecimal(left);
            BigDecimal rightValue = anyToDecimal(right);
            BigDecimal result = leftValue.subtract(rightValue);
            return RuntimeValue.of(result);
        }
        throw new RuntimeException("未知的运算类型: " + left.getType() + " " + right.getType());
    }

    private RuntimeValue multiply(RuntimeValue left, RuntimeValue right) {
        if (hasBoolean(left, right) || hasNullType(left, right)) {
            throw new RuntimeException("不合法的乘法运算");
        }
        if (allIntegers(left, right)) {
            Long leftValue = (Long) left.getValue();
            Long rightValue = (Long) right.getValue();
            return RuntimeValue.of(leftValue * rightValue);
        }
        if (allNumbers(left, right) && hasDecimal(left, right)) {
            BigDecimal leftValue = anyToDecimal(left);
            BigDecimal rightValue = anyToDecimal(right);
            BigDecimal result = leftValue.multiply(rightValue);
            return RuntimeValue.of(result);
        }
        throw new RuntimeException("未知的运算类型: " + left.getType() + " " + right.getType());
    }

    private RuntimeValue divide(RuntimeValue left, RuntimeValue right) {
        if (hasBoolean(left, right) || hasNullType(left, right)) {
            throw new RuntimeException("不合法的除法运算");
        }
        if (allIntegers(left, right)) {
            Long leftValue = (Long) left.getValue();
            Long rightValue = (Long) right.getValue();
            return RuntimeValue.of(leftValue / rightValue);
        }
        if (allNumbers(left, right) && hasDecimal(left, right)) {
            BigDecimal leftValue = anyToDecimal(left);
            BigDecimal rightValue = anyToDecimal(right);
            BigDecimal result = leftValue.divide(rightValue, 2, RoundingMode.HALF_UP);
            return RuntimeValue.of(result);
        }
        throw new RuntimeException("未知的运算类型: " + left.getType() + " " + right.getType());
    }

    /**
     * 转换为字符串类型
     * <p>在非确定性转换时的场景下使用<p/>
     * @param value 非确定性类型
     * @return 字符串类型
     */
    private String anyToString(RuntimeValue value) {
        if (value.isNullType()) {
            return "null";
        }
        if (value.isBoolean()|| value.isInteger()) {
            return String.valueOf(value.getValue());
        }
        if (value.isDecimal()) {
            // 去掉多余的尾零，并避免科学计数法
            return ((BigDecimal) value.getValue()).stripTrailingZeros().toPlainString();
        }
        return String.valueOf(value.getValue());
    }

    /**
     * 转换为布尔类型
     * <p>在非确定性转换时的场景下使用<p/>
     * @param value 非确定性类型
     * @return 布尔类型
     */
    private Boolean anyToBoolean(RuntimeValue value) {
        if (value.isBoolean()) {
            return (Boolean) value.getValue();
        }
        throw new RuntimeException("不是布尔类型: " + value.getClass().getName());
    }

    /**
     * 转换为小数类型
     * <p>在非确定性转换时的场景下使用<p/>
     * @param value 非确定性类型
     * @return 小数类型
     */
    private BigDecimal anyToDecimal(RuntimeValue value) {
        if (value.isDecimal()) {
            return (BigDecimal) value.getValue();
        }
        if (value.isInteger()) {
            return BigDecimal.valueOf( (Long) value.getValue() );
        }
        throw new RuntimeException("不是数值类型: " + value.getClass().getName());
    }

    public <T> Stream<T> toSafeStream(T[] array) {
        if (array == null) {
            return Stream.empty();
        }
        return Arrays.stream(array);
    }

    /**
     * 包含至少1个字符串类型，其它类型不做限制
     */
    private boolean hasString(RuntimeValue... values) {
        return toSafeStream(values).anyMatch(RuntimeValue::isString);
    }

    /**
     * 包含至少1个Null类型，其它类型不做限制
     */
    private boolean hasNullType(RuntimeValue... values) {
        return toSafeStream(values).anyMatch(RuntimeValue::isNullType);
    }

    /**
     * 包含至少1个布尔类型，其它类型不做限制
     */
    private boolean hasBoolean(RuntimeValue... values) {
        return toSafeStream(values).anyMatch(RuntimeValue::isBoolean);
    }

    /**
     * 包含至少1个小数类型，其它类型不做限制
     */
    private boolean hasDecimal(RuntimeValue... values) {
        return toSafeStream(values).anyMatch(RuntimeValue::isDecimal);
    }

    /**
     * 都是字符串类型
     */
    private boolean allStrings(RuntimeValue... values) {
        return toSafeStream(values).allMatch(RuntimeValue::isString);
    }

    /**
     * 都是布尔类型
     */
    private boolean allBooleans(RuntimeValue... values) {
        return toSafeStream(values).allMatch(RuntimeValue::isBoolean);
    }

    /**
     * 都是整数类型
     */
    private boolean allIntegers(RuntimeValue... values) {
        return toSafeStream(values).allMatch(RuntimeValue::isInteger);
    }

    /**
     * 都是数值类型
     */
    private boolean allNumbers(RuntimeValue... values) {
        return toSafeStream(values).allMatch(RuntimeValue::isNumbers);
    }

}
