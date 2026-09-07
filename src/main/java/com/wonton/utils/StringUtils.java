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
            builder.append(unescape(c));
        }
        return builder.toString();
    }

    public static String unescape(char c) {
        return switch (c) {
            case '\n' -> "\\n";
            case '\t' -> "\\t";
            case '\r' -> "\\r";
            case '\b' -> "\\b";
            case '\f' -> "\\f";
            case '\0' -> "\\0";
            case '\\' -> "\\\\";
            case '"' -> "\\\"";
            default -> {
                // 其余不可打印控制字符统一用十六进制转义表示
                if (c < 32 || c == 127) {
                    yield String.format("\\u%04x", (int) c);
                }
                yield String.valueOf(c);
            }
        };
    }

}
