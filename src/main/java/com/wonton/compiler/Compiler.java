package com.wonton.compiler;

import com.wonton.compiler.backend.AsmCodeGenerator;
import com.wonton.compiler.backend.Assembler;
import com.wonton.compiler.backend.AssemblerException;
import com.wonton.compiler.frontend.analyzer.SemanticAnalyzer;
import com.wonton.compiler.frontend.lexical.Lexer;
import com.wonton.compiler.frontend.lexical.Token;
import com.wonton.compiler.frontend.syntax.Parser;
import com.wonton.compiler.frontend.syntax.node.Node;
import com.wonton.compiler.ir.IRGenerator;
import com.wonton.compiler.ir.Triplet;
import com.wonton.logger.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * 编译器主入口
 * <p>完成从源码到原生可执行文件的完整编译流程：</p>
 * 1. 词法分析 → Token 流
 * 2. 语法分析 → AST
 * 3. 语义分析 → IR
 * 4. IR 生成 → 三元式/四元式
 * 5. 代码生成 → x86_64 汇编
 * 6. 汇编/链接 → 原生 exe
 */
public class Compiler {

    /**
     * 将源文件编译为可执行文件
     * @param filePath 源码路径（.wonton）
     * @param outPath 输出路径（.exe）
     */
    public static void compile(String filePath, String outPath) {
        // 1. 读取源码
        String source = null;
        try {
            source = Files.readString(Path.of(filePath));
        } catch (IOException e) {
            System.err.println("读取源文件时出错：" + e.getMessage());
            System.exit(66);
        }

        if (source.isEmpty()) {
            Logger.success("由于源码内容为空！结束编译。");
            System.exit(66);
        }

        // 2. 词法分析 → Token 流
        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.tokenize();

        // 3. 语法分析 → AST
        Parser parser = new Parser(tokens);
        Node ast = parser.parse();

        // 4. 语义分析 → 类型检查
        SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer();
        semanticAnalyzer.analyze(ast);

        // 5. IR 生成 → 三元式/四元式
        IRGenerator irGenerator = new IRGenerator();
        List<Triplet> ir = irGenerator.generate(ast);
        for (Triplet t : ir) {
            System.out.println(t);
        }

        // 6. 代码生成 → x86_64 汇编
        AsmCodeGenerator asmGenerator = new AsmCodeGenerator();
        String assemblyCode = asmGenerator.generate(ir);
        System.out.println(assemblyCode);

        // 7. 保存汇编文件（临时文件）
        Path asmFile = Path.of("build/temp_" + System.currentTimeMillis() + ".asm");
        try {
            // 显式使用 StandardCharsets.UTF_8，确保无 BOM
            Files.writeString(asmFile, assemblyCode, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("写入汇编文件时出错：" + e.getMessage());
            System.exit(66);
        }

        // 8. 调用 NASM 汇编为机器码
        String objFile = asmFile.toString().replace(".asm", ".obj");
        Assembler assembler = new Assembler();
        try {
            assembler.assemble(asmFile.toString(), objFile, "win64");
        } catch (AssemblerException e) {
            System.err.println("汇编时出错：" + e.getMessage());
            System.exit(66);
        }


        // 9. 链接为可执行文件
        try {
            assembler.link(objFile, outPath);
        } catch (AssemblerException e) {
            System.err.println(e.getMessage());
            System.exit(65);
        }

        // 10. 清理临时文件
        try {
            Files.deleteIfExists(asmFile);
            Files.deleteIfExists(Path.of(objFile));
        } catch (IOException e) {
            System.err.println("删除临时文件时出错：" + e.getMessage());
            System.exit(66);
        }

        System.out.println("编译成功！输出文件:" + outPath);
    }
}
