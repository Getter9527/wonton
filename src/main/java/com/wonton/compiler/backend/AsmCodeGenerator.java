package com.wonton.compiler.backend;

import com.wonton.compiler.ir.Triplet;

import java.util.List;

/**
 * 汇编代码生成器
 * <p>将三元式 IR 映射为 x86_64 汇编代码</p>
 */
public class AsmCodeGenerator {

    private final StringBuilder asm = new StringBuilder();
    private int labelCounter = 0;


    /**
     * 生成 x86_64 汇编代码
     *
     * @param ir 三元式列表
     * @return 汇编字符串
     */
    public String generate(List<Triplet> ir) {
        generateHeader();
        generateCode(ir);
        generateFooter();
        return asm.toString();
    }

    /**
     * 生成汇编文件头部
     */
    private void generateHeader() {
        asm.append(".section .text\n");
        asm.append(".global main\n");
        asm.append("main:\n");
        asm.append("    push rbp\n");
        asm.append("    mov rbp, rsp\n");
        asm.append("\n");
    }

    /**
     * 生成汇编代码体
     *
     * @param ir 三元式列表
     */
    private void generateCode(List<Triplet> ir) {
        for (Triplet triplet : ir) {
            emitInstruction(triplet);
        }
    }

    /**
     * 生成单个指令
     *
     * @param triplet 三元式
     */
    private void emitInstruction(Triplet triplet) {
        switch (triplet.operator) {
            case "ADD":
                emitAdd(triplet.arg1, triplet.arg2, triplet.result);
                break;
            case "SUB":
                emitSub(triplet.arg1, triplet.arg2, triplet.result);
                break;
            case "MUL":
                emitMul(triplet.arg1, triplet.arg2, triplet.result);
                break;
            case "DIV":
                emitDiv(triplet.arg1, triplet.arg2, triplet.result);
                break;
            case "MOD":
                emitMod(triplet.arg1, triplet.arg2, triplet.result);
                break;
            case "POW":
                emitPow(triplet.arg1, triplet.arg2, triplet.result);
                break;
            case "EQ":
                emitEq(triplet.arg1, triplet.arg2, triplet.result);
                break;
            case "NE":
                emitNe(triplet.arg1, triplet.arg2, triplet.result);
                break;
            case "LT":
                emitLt(triplet.arg1, triplet.arg2, triplet.result);
                break;
            case "LE":
                emitLe(triplet.arg1, triplet.arg2, triplet.result);
                break;
            case "GT":
                emitGt(triplet.arg1, triplet.arg2, triplet.result);
                break;
            case "GE":
                emitGe(triplet.arg1, triplet.arg2, triplet.result);
                break;
            case "AND":
                emitAnd(triplet.arg1, triplet.arg2, triplet.result);
                break;
            case "OR":
                emitOr(triplet.arg1, triplet.arg2, triplet.result);
                break;
            case "NOT":
                emitNot(triplet.arg1, triplet.result);
                break;
            case "PUSH_INT":
                emitPushInt(triplet.arg1, triplet.result);
                break;
            case "PUSH_DEC":
                emitPushDec(triplet.arg1, triplet.result);
                break;
            case "PUSH_STR":
                emitPushStr(triplet.arg1, triplet.result);
                break;
            case "PUSH_BOOL":
                emitPushBool(triplet.arg1, triplet.result);
                break;
            case "PUSH_NULL":
                emitPushNull(triplet.result);
                break;
            case "MOV":
                emitMov(triplet.arg1, triplet.result);
                break;
            case "FUNC_START":
                emitFunctionStart(triplet.arg1);
                break;
            case "FUNC_END":
                emitFunctionEnd(triplet.arg1);
                break;
            default:
                System.err.println("未知操作符：" + triplet.operator);
        }
    }

    // ==================== 算术运算 ====================

    private void emitAdd(String arg1, String arg2, String result) {
        asm.append("    mov rax, ").append(arg1).append("\n");
        asm.append("    add rax, ").append(arg2).append("\n");
        asm.append("    mov ").append(result).append(", rax\n");
        asm.append("\n");
    }

    private void emitSub(String arg1, String arg2, String result) {
        asm.append("    mov rax, ").append(arg1).append("\n");
        asm.append("    sub rax, ").append(arg2).append("\n");
        asm.append("    mov ").append(result).append(", rax\n");
        asm.append("\n");
    }

    private void emitMul(String arg1, String arg2, String result) {
        asm.append("    mov rax, ").append(arg1).append("\n");
        asm.append("    imul rax, ").append(arg2).append("\n");
        asm.append("    mov ").append(result).append(", rax\n");
        asm.append("\n");
    }

    private void emitDiv(String arg1, String arg2, String result) {
        asm.append("    mov rax, ").append(arg1).append("\n");
        asm.append("    cqo\n");
        asm.append("    idiv ").append(arg2).append("\n");
        asm.append("    mov ").append(result).append(", rax\n");
        asm.append("\n");
    }

    private void emitMod(String arg1, String arg2, String result) {
        asm.append("    mov rax, ").append(arg1).append("\n");
        asm.append("    cqo\n");
        asm.append("    idiv ").append(arg2).append("\n");
        asm.append("    mov ").append(result).append(", rdx\n");
        asm.append("\n");
    }

    private void emitPow(String arg1, String arg2, String result) {
        // 使用 call pow 函数（需要链接 math 库）
        asm.append("    push ").append(arg2).append("\n");
        asm.append("    push ").append(arg1).append("\n");
        asm.append("    call pow@PLT\n");
        asm.append("    add rsp, 16\n");
        asm.append("    movsd ").append(result).append(", xmm0\n");
        asm.append("\n");
    }

    // ==================== 比较运算 ====================

    private void emitEq(String arg1, String arg2, String result) {
        asm.append("    mov rax, ").append(arg1).append("\n");
        asm.append("    cmp rax, ").append(arg2).append("\n");
        asm.append("    sete al\n");
        asm.append("    movzx ").append(result).append(", al\n");
        asm.append("\n");
    }

    private void emitNe(String arg1, String arg2, String result) {
        asm.append("    mov rax, ").append(arg1).append("\n");
        asm.append("    cmp rax, ").append(arg2).append("\n");
        asm.append("    setne al\n");
        asm.append("    movzx ").append(result).append(", al\n");
        asm.append("\n");
    }

    private void emitLt(String arg1, String arg2, String result) {
        asm.append("    mov rax, ").append(arg1).append("\n");
        asm.append("    cmp rax, ").append(arg2).append("\n");
        asm.append("    setl al\n");
        asm.append("    movzx ").append(result).append(", al\n");
        asm.append("\n");
    }

    private void emitLe(String arg1, String arg2, String result) {
        asm.append("    mov rax, ").append(arg1).append("\n");
        asm.append("    cmp rax, ").append(arg2).append("\n");
        asm.append("    setle al\n");
        asm.append("    movzx ").append(result).append(", al\n");
        asm.append("\n");
    }

    private void emitGt(String arg1, String arg2, String result) {
        asm.append("    mov rax, ").append(arg1).append("\n");
        asm.append("    cmp rax, ").append(arg2).append("\n");
        asm.append("    setg al\n");
        asm.append("    movzx ").append(result).append(", al\n");
        asm.append("\n");
    }

    private void emitGe(String arg1, String arg2, String result) {
        asm.append("    mov rax, ").append(arg1).append("\n");
        asm.append("    cmp rax, ").append(arg2).append("\n");
        asm.append("    setge al\n");
        asm.append("    movzx ").append(result).append(", al\n");
        asm.append("\n");
    }

    // ==================== 逻辑运算 ====================

    private void emitAnd(String arg1, String arg2, String result) {
        asm.append("    mov rax, ").append(arg1).append("\n");
        asm.append("    and rax, ").append(arg2).append("\n");
        asm.append("    mov ").append(result).append(", rax\n");
        asm.append("\n");
    }

    private void emitOr(String arg1, String arg2, String result) {
        asm.append("    mov rax, ").append(arg1).append("\n");
        asm.append("    or rax, ").append(arg2).append("\n");
        asm.append("    mov ").append(result).append(", rax\n");
        asm.append("\n");
    }

    private void emitNot(String arg1, String result) {
        asm.append("    mov rax, ").append(arg1).append("\n");
        asm.append("    xor rax, 1\n");
        asm.append("    mov ").append(result).append(", rax\n");
        asm.append("\n");
    }

    // ==================== 字面量加载 ====================

    private void emitPushInt(String value, String result) {
        asm.append("    mov ").append(result).append(", ").append(value).append("\n");
        asm.append("\n");
    }

    private void emitPushDec(String value, String result) {
        // 简化处理：暂时存储为字符串，后续扩展为浮点数
        asm.append("    ; TODO: 支持浮点数常量 ").append(value).append("\n");
    }

    private void emitPushStr(String value, String result) {
        // 简化处理：存储字符串地址
        asm.append("    ; TODO: 支持字符串常量 ").append(value).append("\n");
    }

    private void emitPushBool(String value, String result) {
        boolean boolValue = Boolean.parseBoolean(value);
        asm.append("    mov ").append(result).append(", ").append(boolValue ? "1" : "0").append("\n");
        asm.append("\n");
    }

    private void emitPushNull(String result) {
        asm.append("    xor ").append(result).append(", ").append(result).append("\n");
        asm.append("\n");
    }

    // ==================== 赋值和函数 ====================

    private void emitMov(String arg1, String result) {
        asm.append("    mov ").append(result).append(", ").append(arg1).append("\n");
        asm.append("\n");
    }

    private void emitFunctionStart(String funcName) {
        asm.append("\n").append(funcName).append("_start:\n");
    }

    private void emitFunctionEnd(String funcName) {
        asm.append(funcName).append("_end:\n");
    }

    /**
     * 生成汇编文件尾部
     */
    private void generateFooter() {
        asm.append("    pop rbp\n");
        asm.append("    ret\n");
    }
}
