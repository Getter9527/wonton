package com.wonton.utils;

public class StringUtils {

    private StringUtils() {
        throw new UnsupportedOperationException("工具类禁止实例化");
    }

    /**
     * 反转义渲染
     * <p>把字符串中的真实控制字符还原为反斜杠转义写法，用于展示场景，
     * 避免控制字符（如真实换行符 0x0A）破坏输出格式。与词法分析中 Lexer.escape() 的转义解析互为逆操作。</p>
     *
     * @param text 原始字符串，可能包含控制字符
     * @return 适合展示的转义表示字符串
     */
    public static String unescape(String text) {
        if (text == null) {
            return "null";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            // 常用控制字符还原为对应的命名转义写法
            switch (c) {
                case '\n' -> builder.append("\\n");
                case '\t' -> builder.append("\\t");
                case '\r' -> builder.append("\\r");
                case '\b' -> builder.append("\\b");
                case '\f' -> builder.append("\\f");
                case '\0' -> builder.append("\\0");
                case '\\' -> builder.append("\\\\");
                case '"' -> builder.append("\\\"");
                default -> {
                    // 其余不可打印控制字符统一用十六进制转义表示
                    if (c < 32 || c == 127) {
                        builder.append(String.format("\\u%04x", (int) c));
                    } else {
                        builder.append(c);
                    }
                }
            }
        }
        return builder.toString();
    }

}
