package com.wonton.logger;

/**
 * 日志输出器接口
 * <p>
 * 定义日志输出的标准接口，不同的实现类负责输出到不同的目的地
 * </p>
 */
public abstract class LogAppender {

    private LogLevel level = LogLevel.DEBUG;

    public LogLevel getLevel() {
        return level;
    }

    public void setLevel(LogLevel level) {
        this.level = level;
    }

    public abstract void log(LogLevel level, String message);
}
