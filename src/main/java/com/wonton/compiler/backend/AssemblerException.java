package com.wonton.compiler.backend;

/**
 * 汇编器/链接器异常
 * <p>专门用于处理 NASM 和 Clang 相关的错误</p>
 */
public class AssemblerException extends Exception {

    /**
     * 构造函数
     *
     * @param message 错误消息
     */
    public AssemblerException(String message) {
        super(message);
    }

    /**
     * 构造函数
     *
     * @param message 错误消息
     * @param cause    causae
     */
    public AssemblerException(String message, Throwable cause) {
        super(message, cause);
    }

}
