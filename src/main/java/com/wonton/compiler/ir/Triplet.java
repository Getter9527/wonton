package com.wonton.compiler.ir;

/**
 * 三元式中间表示
 * <p>格式：(operator, arg1, arg2, result)</p>
 * 例如：(ADD, a, b, t1) 表示 t1 = a + b
 */
public class Triplet {

    public final String operator;  // 操作符（ADD, SUB, MUL, etc.）
    public final String arg1;      // 第一个操作数
    public final String arg2;      // 第二个操作数（如果有）
    public final String result;    // 结果变量

    /**
     * 构造函数（二元运算）
     *
     * @param operator 操作符
     * @param arg1     第一个操作数
     * @param arg2     第二个操作数
     * @param result   结果变量
     */
    public Triplet(String operator, String arg1, String arg2, String result) {
        this.operator = operator;
        this.arg1 = arg1;
        this.arg2 = arg2;
        this.result = result;
    }

    /**
     * 构造函数（一元运算）
     *
     * @param operator 操作符
     * @param arg1     操作数
     * @param result   结果变量
     */
    public Triplet(String operator, String arg1, String result) {
        this.operator = operator;
        this.arg1 = arg1;
        this.arg2 = null;
        this.result = result;
    }

    @Override
    public String toString() {
        if (arg2 == null) {
            return String.format("(%s, %s, %s)", operator, arg1, result);
        }
        return String.format("(%s, %s, %s, %s)", operator, arg1, arg2, result);
    }

}
