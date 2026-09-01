package com.wonton.interpreter;

/**
 * return 控制流信号
 * <p>树遍历解释器借助异常实现 return 跳转：return 语句抛出此信号，
 * 信号自动穿透任意深度的嵌套语句（if、while、block），
 * 最终在函数调用点被捕获，取出携带的返回值。</p>
 */
public class ReturnSignal extends RuntimeException {

    /**
     * 存储 return 语句的返回值
     */
    private final RuntimeValue value;

    public ReturnSignal(RuntimeValue value) {
        this.value = value;
    }

    public RuntimeValue getValue() {
        return value;
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;   // 不生成堆栈信息，性能大幅提升
    }

}
