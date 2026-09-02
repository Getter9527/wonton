package com.wonton.compiler.backend;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 汇编器和链接器
 * <p>调用外部工具（NASM/Clang）完成汇编和链接</p>
 */
public class Assembler {

    /**
     * 调用 NASM 汇编为机器码
     *
     * @param asmFile  汇编文件路径
     * @param objFile  输出目标文件路径
     * @param format   目标格式（win64/linux64/macos）
     */
    public void assemble(String asmFile, String objFile, String format) throws AssemblerException {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "nasm",
                    "-f", format,
                    asmFile,
                    "-o", objFile
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();

            // 读取错误输出
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.err.println("NASM: " + line);
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new AssemblerException("NASM 汇编失败，退出码：" + exitCode + "，汇编文件：" + asmFile);
            }

            // 检查输出文件是否生成成功
            if (!java.nio.file.Files.exists(java.nio.file.Path.of(objFile))) {
                throw new AssemblerException("NASM 未生成目标文件：" + objFile);
            }

        } catch (IOException e) {
            throw new AssemblerException("执行 NASM 失败：" + e.getMessage() + "，请确保 NASM 已安装并配置到 PATH", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssemblerException("NASM 执行被中断", e);
        }
    }

    /**
     * 链接为可执行文件
     *
     * @param objFile 目标文件路径
     * @param exeFile 输出可执行文件路径
     */
    public void link(String objFile, String exeFile) throws AssemblerException {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "clang",
                    objFile,
                    "-o", exeFile
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();

            // 读取错误输出
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.err.println("Clang: " + line);
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new AssemblerException("Clang 链接失败，退出码：" + exitCode + "，目标文件：" + objFile + "，输出文件：" + exeFile);
            }

            // 检查输出文件是否生成成功
            if (!java.nio.file.Files.exists(java.nio.file.Path.of(exeFile))) {
                throw new AssemblerException("Clang 未生成可执行文件：" + exeFile);
            }

        } catch (IOException e) {
            throw new AssemblerException("执行 Clang 失败：" + e.getMessage() + "，请确保 Clang 已安装并配置到 PATH", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssemblerException("Clang 执行被中断", e);
        }
    }

    /**
     * 检查外部工具是否可用
     *
     * @param tool 工具名称（如 nasm, clang）
     * @return 是否可用
     */
    public static boolean checkTool(String tool) {
        try {
            ProcessBuilder pb = new ProcessBuilder(tool, "--version");
            pb.redirectErrorStream(true);
            Process process = pb.start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            return false;
        }
    }
}
