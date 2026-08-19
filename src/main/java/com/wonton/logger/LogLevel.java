package com.wonton.logger;

public enum LogLevel {

    // DEBUG < INFO < PRIMARY < WARN < (ERROR or SUCCESS)
    DEBUG, INFO, PRIMARY, WARN, ERROR, SUCCESS;

    public String getLabel() {
        return switch (this) {
            case DEBUG -> "调试";
            case INFO -> "信息";
            case PRIMARY -> "重要";
            case WARN -> "警告";
            case ERROR -> "异常";
            case SUCCESS -> "成功";
        };
    }

    public String getHexColor() {
        return switch (this) {
            case DEBUG -> "#909399";
            case INFO -> "#909399";
            case PRIMARY -> "#409EFF";
            case WARN -> "#E6A23C";
            case ERROR -> "#F56C6C";
            case SUCCESS -> "#67C23A";
        };
    }
}
