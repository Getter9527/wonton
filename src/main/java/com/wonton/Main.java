package com.wonton;

import com.wonton.logger.Logger;

import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        Logger.success("Hello Wonton:");
        Logger.debug("args:" + Arrays.toString(args));
        if (args.length > 1) {
            Logger.primary("用法: wonton [script]");
            // 退出码64：命令使用错误，立即终止程序
            System.exit(64);
        } else if (args.length == 1) {
            // runFile(args[0]);
        } else {
            // runPrompt();
        }
    }
}