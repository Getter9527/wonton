package com.wonton.logger;

public enum LogLevel {

    // DEBUG < INFO < PRIMARY < WARN < (ERROR or SUCCESS)
    DEBUG, INFO, PRIMARY, WARN, ERROR, SUCCESS;

    public String getLabel() {
        return switch (this) {
            case DEBUG -> "DEBUG";
            case INFO -> "INFO";
            case PRIMARY -> "PRIMARY";
            case WARN -> "WARN";
            case ERROR -> "ERROR";
            case SUCCESS -> "SUCCESS";
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
