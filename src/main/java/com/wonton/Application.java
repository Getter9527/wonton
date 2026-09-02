package com.wonton;

import com.wonton.interpreter.Environment;
import com.wonton.interpreter.Interpreter;
import com.wonton.lexical.Lexer;
import com.wonton.lexical.Token;
import com.wonton.logger.Logger;
import com.wonton.syntax.Parser;
import com.wonton.syntax.node.Node;
import com.wonton.utils.FileUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Application {

    private static final int EX_USAGE = 64; // 命令使用错误
    private static final int EX_DATA_ERR = 65; // 输入数据错误
    private static final int EX_NO_INPUT = 66; // 输入文件不存在 或 不可读取

    public static void main(String[] args) {
        if (args.length > 1) {
            // 超过1个参数，不符合标准
            Logger.info("【用法】wonton <文件地址>");
            Logger.info("【用户实际输入】" + String.join(" ", Arrays.toString(args)));
            // 退出码64：命令使用错误，立即终止程序
            System.exit(EX_USAGE);
        } else if (args.length == 1) {
            // 1个参数，运行文件
            runFile(args[0]);
        } else {
            // 交互式模式
            runPrompt();
        }
    }

    /**
     * 运行一段源码：词法分析 → 语法分析 → 解释执行
     *
     * @param source 源码
     * @param env    运行环境
     */
    private static void run(String source, Environment env) {
        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens);
        Node ast = parser.parse();
        Interpreter interpreter = new Interpreter();
        interpreter.interpret(ast, env);
    }

    /**
     * 运行脚本文件
     *
     * @param filePath 脚本路径
     */
    private static void runFile(String filePath) {
        final String source = FileUtils.readSource(filePath);
        try {
            run(source, new Environment());
        } catch (RuntimeException e) {
            Logger.error("运行文件时出错");
            Logger.error(e.getMessage());
            // 失败时退出码 65
            System.exit(EX_DATA_ERR);
        }
    }

    /**
     * 交互式命令行（REPL）
     * <p>
     *  所有输入共用同一个环境，跨行保留变量与函数定义；
     *  输入 exit 或 quit 退出。
     * </p>
     */
    private static void runPrompt() {
        Logger.primary("欢迎使用【Wonton】程序语言，这里是交互式命令行程序（REPL）");
        Logger.primary("（输入 exit 可退出交互式命令工具）");
        Environment globalEnv = new Environment();
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print(">> ");
            // 按 Ctrl + Z，或者读取到文件末尾 EOF，则表示输入结束
            if (!scanner.hasNextLine()) {
                break;
            }
            // 阻塞等待用户输入
            String line = scanner.nextLine();
            // 遇到空行则跳过，不处理空行
            if (line.isEmpty()) {
                continue;
            }
            // 退出交互式命令
            if (line.equals("exit")) {
                break;
            }
            // 处理跨行输入：大括号未配平时继续读取后续行
            String source = readCompleteInput(scanner, line);
            try {
                run(source, globalEnv);
            } catch (RuntimeException e) {
                Logger.error(e.getMessage());
            }
        }
    }

    /**
     * 读取完整输入：若大括号未配平，继续读取后续行拼接
     *
     * @param scanner   输入扫描器
     * @param firstLine 首行输入
     * @return 大括号配平后的完整源码
     */
    private static String readCompleteInput(Scanner scanner, String firstLine) {
        StringBuilder builder = new StringBuilder(firstLine);
        while (braceDepth(builder.toString()) > 0) {
            System.out.print("......> ");
            if (!scanner.hasNextLine()) {
                break;
            }
            builder.append("\n").append(scanner.nextLine());
        }
        return builder.toString();
    }

    /**
     * 统计大括号深度
     *
     * @param text 源码文本
     * @return 未闭合的左大括号数量
     */
    private static int braceDepth(String text) {
        int depth = 0;
        for (char c : text.toCharArray()) {
            if (c == '{') {
                depth++;
            } else if (c == '}') {
                depth--;
            }
        }
        return depth;
    }

}