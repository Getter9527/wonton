package com.wonton.interpreter;

import com.wonton.lexical.TokenType;
import com.wonton.syntax.node.Node;
import com.wonton.syntax.node.expression.*;
import com.wonton.syntax.node.statement.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * 树遍历解释器
 */
public class Interpreter {

    public RuntimeValue interpret(Node node, Environment env) {
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

        if (node instanceof NullExpr) {
            return RuntimeValue.ofNull();
        }

        if (node instanceof ParenExpr parenNode) {
            return interpret(parenNode.getExpression(), env);
        }

        if (node instanceof UnaryExpr unaryNode) {
            TokenType operator = unaryNode.getOperator().getType();
            RuntimeValue operand = interpret(unaryNode.getOperand(), env);
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
            RuntimeValue left = interpret(binNode.getLeft(), env);
            RuntimeValue right = interpret(binNode.getRight(), env);

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

            if (operator == TokenType.Caret) {
                return exponent(left, right);
            }
        }

        if (node instanceof LogicalExpr logicalNode) {

            TokenType operator = logicalNode.getOperator().getType();
            Expr left = logicalNode.getLeft();
            Expr right = logicalNode.getRight();

            if (operator == TokenType.And) {
                return and(left, right, env);
            }
            if (operator == TokenType.Or) {
                return or(left, right, env);
            }
        }

        // 如果是变量表达式，则从环境中获取变量的值
        if (node instanceof VariableExpr varNode) {
            String name = varNode.getIdentifier().getLexeme();
            return env.get(name);
        }

        if (node instanceof Stmts stmtsNode) {
            for (Stmt stmt : stmtsNode.getStmts()) {
                // 语句不返回运算结果，只需要被执行
                interpret(stmt, env);
            }
            return RuntimeValue.ofVoid();
        }

        if (node instanceof PrintStmt printNode) {
            // print的值有可能是一个表达式，因此需要被解释成运行时值
            RuntimeValue printRuntimeVal = interpret(printNode.getValue(), env);
            if (printRuntimeVal == RuntimeValue.ofVoid()) {
                throw new UnsupportedOperationException("由于函数没有返回值，所以不可以打印它的调用结果!");
            }
            System.out.print(printRuntimeVal.getValue());
            return RuntimeValue.ofVoid();
        }

        if (node instanceof IfStmt ifNode) {
            RuntimeValue condition = interpret(ifNode.getCondition(), env);
            if (!condition.isBoolean()) {
                throw new RuntimeException("条件表达式必须返回布尔值");
            }
            boolean conditionValue = (boolean) condition.getValue();
            // if
            if (conditionValue) {
                return interpret(ifNode.getIfBlock(), env);
            }
            // else
            if (ifNode.getElseBlock() != null) {
                return interpret(ifNode.getElseBlock(), env);
            }
            // 如果没有走进if也没有走进else
            return RuntimeValue.ofVoid();
        }

        if (node instanceof WhileStmt whileNode) {
            while(true) {
                RuntimeValue condition = interpret(whileNode.getCondition(), env);
                if (!condition.isBoolean()) {
                    throw new RuntimeException("条件表达式必须是布尔值");
                }
                // 不满足条件则不循环
                if (!(boolean) condition.getValue()) {
                    break;
                }
                // 满足条件则循环语句块中的内容
                interpret(whileNode.getWhileBlock(), env);
            }
            return RuntimeValue.ofVoid();
        }

        if (node instanceof BlockStmt blockNode) {
            // 创建一个嵌套的新环境，并立即使用它
            Environment subEnv = new Environment(env);
            for (Stmt stmt : blockNode.getStmts()) {
                interpret(stmt, subEnv);
            }
            return RuntimeValue.ofVoid();
        }

        if (node instanceof VariableDeclarationStmt varDeclarationNode) {
            Expr initializer = varDeclarationNode.getInitializer();
            RuntimeValue runtimeVal = initializer == null
                    ? RuntimeValue.ofNull()
                    : interpret(initializer, env);
            // 变量名称
            String name = varDeclarationNode.getIdentifier().getLexeme();
            // 定义变量，并存入环境
            env.define(name, runtimeVal, false);
            return RuntimeValue.ofVoid();
        }

        if (node instanceof ConstantDeclarationStmt constDeclarationNode) {
            Expr initializer = constDeclarationNode.getInitializer();
            RuntimeValue runtimeVal = (initializer == null)
                    ? RuntimeValue.ofNull()
                    : interpret(initializer, env);
            // 变量名称
            String name = constDeclarationNode.getIdentifier().getLexeme();
            // 定义常量
            env.define(name, runtimeVal, true);
            return RuntimeValue.ofVoid();
        }

        if (node instanceof AssignmentStmt assignNode) {
            RuntimeValue runtimeVal = interpret(assignNode.getValue(), env);
            String name = assignNode.getIdentifier().getLexeme();
            env.assign(name, runtimeVal);
            return RuntimeValue.ofVoid();
        }

        if (node instanceof FunctionDeclarationStmt funcDeclarationNode) {
            String funcName = funcDeclarationNode.getName().getLexeme();
            FunctionValue funcValue = new FunctionValue(
                    funcName,
                    funcDeclarationNode.getParams(),
                    funcDeclarationNode.getBody(),
                    env
            );
            env.define(funcName, RuntimeValue.of(funcValue), false);
            return RuntimeValue.ofVoid();
        }

        if (node instanceof ReturnStmt returnNode) {
            // 注意：这里表示没有返回值，用 Void表示，而不是返回值为Null
            // 如果想表示返回值为Null，应该判断 RuntimeValue.Type == Null
            RuntimeValue returnValue = returnNode.getValue() == null
                    ? RuntimeValue.ofVoid()
                    : interpret(returnNode.getValue(), env);
            // 抛出信号，请求终止函数调用
            throw new ReturnSignal(returnValue);
        }

        if (node instanceof FunctionCallExpr funcCallNode) {
            RuntimeValue callee = interpret(funcCallNode.getCallee(), env);
            if (!callee.isFunction()) {
                throw new RuntimeException("不是函数，无法被调用：" + callee.getValue());
            }
            FunctionValue funcValue = (FunctionValue) callee.getValue();
            // 先在调用者环境中求值所有实参
            List<RuntimeValue> argValues = new ArrayList<>();
            for (Expr arg : funcCallNode.getArgs()) {
                argValues.add(interpret(arg, env));
            }
            return call(funcValue, argValues);
        }

        throw new RuntimeException("未知的语法树节点类型: " + node.getClass().getName());
    }

    /**
     * 执行函数调用
     * <p>基于闭包环境创建函数局部环境，绑定形参后执行函数体，
     * 捕获 ReturnSignal 得到返回值；无 return 语句时默认返回 null。</p>
     *
     * @param funcValue 函数值
     * @param argValues     实参值列表
     * @return 函数返回值
     */
    private RuntimeValue call(FunctionValue funcValue, List<RuntimeValue> argValues) {
        // 检查定义的形参数量 和 调用者传递的实参数量 是否一致
        if (funcValue.getParams().size() != argValues.size()) {
            throw new RuntimeException(
                String.format(
                    "函数 %s 参数个数不匹配：期望 %d 个，实际 %d 个",
                    funcValue.getName(), funcValue.getParams().size(), argValues.size()
                )
            );
        }
        // 为本次函数调用创建一个独立的环境
        // 将函数定义时所处的环境作为新环境的“父级”。这就是闭包（Closure）的实现原理 —— 让函数内部能访问外部变量，但外部访问不到内部变量。
        Environment funcEnv = new Environment(funcValue.getClosure());
        for (int i = 0; i < argValues.size(); i++) {
            String paramName = funcValue.getParams().get(i).getLexeme();
            // 绑定参数：将函数的参数定义到当前函数作用域范围内
            funcEnv.define(paramName, argValues.get(i), false);
        }
        try {
            // 函数调用：本质就是执行函数代码块
            interpret(funcValue.getBody(), funcEnv);
        } catch (ReturnSignal signal) {
            // 当捕捉到return信号时，则会跳转至函数调用的地方，并完成此次函数调用
            return signal.getValue();
        }
        // 如果函数没有显示的return语句，那么则默认返回void
        return RuntimeValue.ofVoid();
    }

    private RuntimeValue or(Expr left, Expr right, Environment env) {
        RuntimeValue leftRuntimeVal = interpret(left, env);
        // 我们要求左值必须是布尔类型
        if (leftRuntimeVal.isBoolean()) {
            Boolean leftValue = (Boolean) leftRuntimeVal.getValue();
            // 两者满足其一即可
            if (leftValue) {
                return leftRuntimeVal;
            }
            // 当左值为false，必须向后求右值
            return interpret(right, env);
        }
        throw new UnsupportedOperationException(
                String.format("不支持的数据类型。操作类型=or 左操作数=%s", leftRuntimeVal.getValue())
        );
    }

    private RuntimeValue and(Expr left, Expr right, Environment env) {
        RuntimeValue leftRuntimeVal = interpret(left, env);
        if (leftRuntimeVal.isBoolean()) {
            Boolean leftValue = (Boolean) leftRuntimeVal.getValue();
            // 前者不满足，后者也无需计算
            if (!leftValue) {
                return leftRuntimeVal;
            }
            return interpret(right, env);
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

    private RuntimeValue exponent(RuntimeValue left, RuntimeValue right) {
        if (allNumbers(left, right)) {
            double leftValue = anyToDecimal(left).doubleValue();
            double rightValue = anyToDecimal(right).doubleValue();
            double result = Math.pow(leftValue, rightValue);
            return RuntimeValue.of(BigDecimal.valueOf(result));
        }
        throw new UnsupportedOperationException(
                String.format("不支持的数据类型。操作类型=exponent 左操作数=%s 右操作数=%s", left.getValue(), right.getValue())
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
