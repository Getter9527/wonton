package com.wonton.logger;

import java.awt.*;

public class TrueColor {

    private TrueColor() {
        throw new UnsupportedOperationException("工具类禁止实例化");
    }

    private static String rgb(int styles, int r, int g, int b) {
        return "\u001B[" + styles + ";2;" + r + ";" + g + ";" + b + "m";
    }

    public static String foreground(String hexColor) {
        Color color = Color.decode(hexColor);
        return foreground(color.getRed(), color.getGreen(), color.getBlue());
    }

    public static String foreground(int r, int g, int b) {
        // \u001B[38;2;r;g;bm
        return rgb(38, r, g, b);
    }

    public static String background(String hexColor) {
        Color color = Color.decode(hexColor);
        return background(color.getRed(), color.getGreen(), color.getBlue());
    }

    public static String background(int r, int g, int b) {
        // \u001B[48;2;r;g;bm
        return rgb(48, r, g, b);
    }

    /**
     * 复位
     * <p>
     *     重置所有样式
     * </p>
     */
    public static String reset() {
        return "\u001B[0m";
    }

    /**
     * 加粗
     */
    public static String bold() {
        return "\u001B[1m";
    }

    /**
     * 暗色
     */
    public static String dim() {
        return "\u001B[2m";
    }

    /**
     * 斜体
     */
    public static String italic() {
        return "\u001B[3m";
    }

    /**
     * 下划线
     */
    public static String underline() {
        return "\u001B[4m";
    }

    /**
     * 文本慢速闪烁
     */
    public static String slowBlink() {
        return "\u001B[5m";
    }

    /**
     * 文本快速闪烁
     */
    public static String rapidBlink() {
        return "\u001B[6m";
    }


    /**
     * 反色
     * <p>
     *     前景色与背景色交换
     * </p>
     */
    public static String reverse() {
        return "\u001B[7m";
    }

    /**
     * 隐藏文本
     */
    public static String hidden() {
        return "\u001B[8m";
    }

    /**
     * 删除线
     */
    public static String strikethrough() {
        return "\u001B[9m";
    }
}
