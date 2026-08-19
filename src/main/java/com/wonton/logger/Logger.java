package com.wonton.logger;

import com.wonton.logger.console.ConsoleAppender;

import java.text.MessageFormat;

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

    public static void debug(String pattern, Object... args) {
        appender.log(LogLevel.DEBUG, MessageFormat.format(pattern, args));
    }

    public static void info(String pattern, Object... args) {
        appender.log(LogLevel.INFO, MessageFormat.format(pattern, args));
    }

    public static void primary(String pattern, Object... args) {
        appender.log(LogLevel.PRIMARY, MessageFormat.format(pattern, args));
    }

    public static void warn(String pattern, Object... args) {
        appender.log(LogLevel.WARN, MessageFormat.format(pattern, args));
    }

    public static void error(String pattern, Object... args) {
        appender.log(LogLevel.ERROR, MessageFormat.format(pattern, args));
    }

    public static void success(String pattern, Object... args) {
        appender.log(LogLevel.SUCCESS, MessageFormat.format(pattern, args));
    }

}
