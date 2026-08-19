package com.wonton.logger.console;

import com.wonton.logger.LogAppender;
import com.wonton.logger.LogLevel;
import com.wonton.logger.TrueColor;

public class ConsoleAppender extends LogAppender {

    @Override
    public void log(LogLevel level, String message) {
        // 日志级别过滤功能
        if (level.ordinal() < this.getLevel().ordinal()) {
            return;
        }
        StringBuilder builder = new StringBuilder();
        String trueColor = TrueColor.foreground(level.getHexColor());
        builder.append(trueColor);
        builder.append("[").append(level.getLabel()).append("] ");
        builder.append(message);
        builder.append(TrueColor.reset());
        System.out.println(builder);
    }
}
