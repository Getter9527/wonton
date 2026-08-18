package com.wonton.logger;

import com.wonton.logger.console.ConsoleAppender;

public class Logger {

    private static LogAppender appender = new ConsoleAppender();

    private Logger() {
        throw new UnsupportedOperationException("工具类禁止实例化");
    }

    public static void setAppender(LogAppender appender) {
        Logger.appender = appender;
    }

    public static void setLevel(LogLevel level) {
        appender.setLevel(level);
    }

    public static void debug(String message) {
        appender.log(LogLevel.DEBUG, message);
    }

    public static void info(String message) {
        appender.log(LogLevel.INFO, message);
    }

    public static void primary(String message) {
        appender.log(LogLevel.PRIMARY, message);
    }

    public static void warn(String message) {
        appender.log(LogLevel.WARN, message);
    }

    public static void error(String message) {
        appender.log(LogLevel.ERROR, message);
    }

    public static void success(String message) {
        appender.log(LogLevel.SUCCESS, message);
    }

}
