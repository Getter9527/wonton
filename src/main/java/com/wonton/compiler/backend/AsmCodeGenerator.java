package com.wonton.compiler.backend;

import com.wonton.compiler.ir.Triplet;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 汇编代码生成器（NASM / Windows x64）
 * <p>将三元式 IR 映射为 x86_64 汇编代码（NASM 语法）</p>
 */
public class AsmCodeGenerator {

    private final StringBuilder asm = new StringBuilder();
    private int labelCounter = 0;

    /** 字符串常量表: key=IR中的原始字符串值, value=数据段标签 */
    private final Map<String, String> stringTable = new LinkedHashMap<>();

    /**
     * 生成 x86_64 汇编代码（NASM win64 格式）
     *
     * @param ir 三元式列表
     * @return 汇编字符串
     */
    public String generate(List<Triplet> ir) {
        // 第一遍：收集所有 PRINT_STR 引用的字符串常量
        collectStrings(ir);

        generateDataSection();
        generateHeader();
        generateCode(ir);
        generateFooter();
        return asm.toString();
    }

    // ==================== 字符串常量收集 ====================

    private void collectStrings(List<Triplet> ir) {
        stringTable.clear();
        int idx = 0;
        for (Triplet t : ir) {
            if ("PRINT_STR".equals(t.operator) && t.arg1 != null) {
                if (!stringTable.containsKey(t.arg1)) {
                    stringTable.put(t.arg1, "str_" + idx++);
                }
            }
        }
    }

    // ==================== 文件结构 ====================

    /**
     * 生成 .data 段：所有字符串常量定义
     */
    private void generateDataSection() {
        if (stringTable.isEmpty()) return;
        asm.append("section .data\n");
        for (Map.Entry<String, String> entry : stringTable.entrySet()) {
            String rawValue = entry.getKey();
            String label = entry.getValue();
            String escaped = escapeNasmString(rawValue);
            // 字符串内容 + CR LF + 终止符；长度不含终止符
            asm.append("    ").append(label)
                    .append(" db '").append(escaped).append("', 13, 10, 0\n");
            asm.append("    ").append(label).append("_len equ $ - ")
                    .append(label).append(" - 1\n");
        }
        asm.append("\n");
    }

    /**
     * 生成汇编文件头部（NASM 语法）
     */
    private void generateHeader() {
        asm.append("section .text\n");
        asm.append("global main\n");
        asm.append("extern GetStdHandle\n");
        asm.append("extern WriteConsoleA\n");
        asm.append("\n");
        asm.append("main:\n");
        asm.append("    push rbp\n");
        asm.append("    mov rbp, rsp\n");
        asm.append("    sub rsp, 40          ; 影子空间(32) + 栈对齐(8)\n");
        asm.append("\n");
    }

    /**
     * 生成汇编文件尾部
     */
    private void generateFooter() {
        asm.append("\n");
        asm.append("    xor eax, eax           ; return 0\n");
        asm.append("    add rsp, 40\n");
        asm.append("    pop rbp\n");
        asm.append("    ret\n");
    }

    // ==================== IR 遍历 ====================

    private void generateCode(List<Triplet> ir) {
        for (Triplet triplet : ir) {
            emitInstruction(triplet);
        }
    }

    private void emitInstruction(Triplet triplet) {
        switch (triplet.operator) {
            case "ADD":        emitAdd(triplet.arg1, triplet.arg2, triplet.result); break;
            case "SUB":        emitSub(triplet.arg1, triplet.arg2, triplet.result); break;
            case "MUL":        emitMul(triplet.arg1, triplet.arg2, triplet.result); break;
            case "DIV":        emitDiv(triplet.arg1, triplet.arg2, triplet.result); break;
            case "MOD":        emitMod(triplet.arg1, triplet.arg2, triplet.result); break;
            case "POW":        emitPow(triplet.arg1, triplet.arg2, triplet.result); break;
            case "EQ":         emitCmp(triplet.arg1, triplet.arg2, triplet.result, "sete"); break;
            case "NE":         emitCmp(triplet.arg1, triplet.arg2, triplet.result, "setne"); break;
            case "LT":         emitCmp(triplet.arg1, triplet.arg2, triplet.result, "setl"); break;
            case "LE":         emitCmp(triplet.arg1, triplet.arg2, triplet.result, "setle"); break;
            case "GT":         emitCmp(triplet.arg1, triplet.arg2, triplet.result, "setg"); break;
            case "GE":         emitCmp(triplet.arg1, triplet.arg2, triplet.result, "setge"); break;
            case "AND":        emitLogic(triplet.arg1, triplet.arg2, triplet.result, "and"); break;
            case "OR":         emitLogic(triplet.arg1, triplet.arg2, triplet.result, "or"); break;
            case "NOT":        emitNot(triplet.arg1, triplet.result); break;
            case "PUSH_INT":   emitPushInt(triplet.arg1, triplet.result); break;
            case "PUSH_DEC":   emitPushDec(triplet.arg1, triplet.result); break;
            case "PUSH_STR":   emitPushStr(triplet.arg1, triplet.result); break;
            case "PUSH_BOOL":  emitPushBool(triplet.arg1, triplet.result); break;
            case "PUSH_NULL":  emitPushNull(triplet.result); break;
            case "MOV":        emitMov(triplet.arg1, triplet.result); break;
            case "FUNC_START": emitFunctionStart(triplet.arg1); break;
            case "FUNC_END":   emitFunctionEnd(triplet.arg1); break;
            case "PRINT_STR":  emitPrintStr(triplet.arg1); break;
            default:
                asm.append("    ; 未知操作符: ").append(triplet.operator).append("\n");
        }
    }

    // ==================== 控制台输出 ====================

    /**
     * 调用 WriteConsoleA 输出字符串常量
     * Windows x64 调用约定: rcx, rdx, r8, r9 + [rsp+32]
     */
    private void emitPrintStr(String strValue) {
        String label = stringTable.get(strValue);
        if (label == null) {
            asm.append("    ; PRINT_STR: 未找到字符串 \"").append(strValue).append("\"\n");
            return;
        }
        String lenLabel = label + "_len";

        // GetStdHandle(STD_OUTPUT_HANDLE = -11)
        asm.append("    mov ecx, -11             ; STD_OUTPUT_HANDLE\n");
        asm.append("    call GetStdHandle\n");
        asm.append("\n");

        // WriteConsoleA(hStdOut, lpBuffer, nChars, &written, NULL)
        asm.append("    mov rcx, rax             ; hStdOut\n");
        asm.append("    lea rdx, [rel ").append(label).append("]\n");
        asm.append("    mov r8d, ").append(lenLabel).append("\n");
        asm.append("    lea r9, [rbp-8]          ; &written\n");
        asm.append("    mov qword [rsp+32], 0    ; lpReserved = NULL\n");
        asm.append("    call WriteConsoleA\n");
        asm.append("\n");
    }

    // ==================== 算术运算 ====================

    private void emitAdd(String arg1, String arg2, String result) {
        asm.append("    mov rax, ").append(arg1).append("\n");
        asm.append("    add rax, ").append(arg2).append("\n");
        asm.append("    mov ").append(result).append(", rax\n\n");
    }

    private void emitSub(String arg1, String arg2, String result) {
        asm.append("    mov rax, ").append(arg1).append("\n");
        asm.append("    sub rax, ").append(arg2).append("\n");
        asm.append("    mov ").append(result).append(", rax\n\n");
    }

    private void emitMul(String arg1, String arg2, String result) {
        asm.append("    mov rax, ").append(arg1).append("\n");
        asm.append("    imul rax, ").append(arg2).append("\n");
        asm.append("    mov ").append(result).append(", rax\n\n");
    }

    private void emitDiv(String arg1, String arg2, String result) {
        asm.append("    mov rax, ").append(arg1).append("\n");
        asm.append("    cqo                  ; 符号扩展 rax -> rdx:rax\n");
        asm.append("    idiv ").append(arg2).append("\n");
        asm.append("    mov ").append(result).append(", rax\n\n");
    }

    private void emitMod(String arg1, String arg2, String result) {
        asm.append("    mov rax, ").append(arg1).append("\n");
        asm.append("    cqo\n");
        asm.append("    idiv ").append(arg2).append("\n");
        asm.append("    mov ").append(result).append(", rdx\n\n");
    }

    private void emitPow(String arg1, String arg2, String result) {
        String loopLabel = ".pow_loop_" + labelCounter;
        String endLabel  = ".pow_end_"  + labelCounter;
        labelCounter++;

        asm.append("    mov rcx, ").append(arg1).append("    ; base\n");
        asm.append("    mov rdx, ").append(arg2).append("    ; exponent\n");
        asm.append("    mov rax, 1                  ; accumulator\n");
        asm.append("    cmp rdx, 0\n");
        asm.append("    jle ").append(endLabel).append("\n");
        asm.append(loopLabel).append(":\n");
        asm.append("    imul rax, rcx\n");
        asm.append("    dec rdx\n");
        asm.append("    jnz ").append(loopLabel).append("\n");
        asm.append(endLabel).append(":\n");
        asm.append("    mov ").append(result).append(", rax\n\n");
    }

    // ==================== 比较运算 ====================

    private void emitCmp(String arg1, String arg2, String result, String setInsn) {
        asm.append("    mov rax, ").append(arg1).append("\n");
        asm.append("    cmp rax, ").append(arg2).append("\n");
        asm.append("    ").append(setInsn).append(" al\n");
        asm.append("    movzx rax, al\n");
        asm.append("    mov ").append(result).append(", rax\n\n");
    }

    // ==================== 逻辑运算 ====================

    private void emitLogic(String arg1, String arg2, String result, String insn) {
        asm.append("    mov rax, ").append(arg1).append("\n");
        asm.append("    ").append(insn).append(" rax, ").append(arg2).append("\n");
        asm.append("    mov ").append(result).append(", rax\n\n");
    }

    private void emitNot(String arg1, String result) {
        asm.append("    mov rax, ").append(arg1).append("\n");
        asm.append("    xor rax, 1\n");
        asm.append("    mov ").append(result).append(", rax\n\n");
    }

    // ==================== 字面量加载 ====================

    private void emitPushInt(String value, String result) {
        asm.append("    mov ").append(result).append(", ").append(value).append("\n\n");
    }

    private void emitPushDec(String value, String result) {
        asm.append("    ; TODO: 浮点数常量 ").append(value).append("\n");
        asm.append("    xor ").append(result).append(", ").append(result).append("\n\n");
    }

    private void emitPushStr(String value, String result) {
        asm.append("    ; TODO: 字符串常量 \"").append(value).append("\"\n");
        asm.append("    xor ").append(result).append(", ").append(result).append("\n\n");
    }

    private void emitPushBool(String value, String result) {
        boolean b = Boolean.parseBoolean(value);
        asm.append("    mov ").append(result).append(", ").append(b ? "1" : "0").append("\n\n");
    }

    private void emitPushNull(String result) {
        asm.append("    xor ").append(result).append(", ").append(result).append("\n\n");
    }

    // ==================== 赋值 & 函数 ====================

    private void emitMov(String arg1, String result) {
        asm.append("    mov ").append(result).append(", ").append(arg1).append("\n\n");
    }

    private void emitFunctionStart(String funcName) {
        asm.append("\n").append(funcName).append("_start:\n");
        asm.append("    push rbp\n");
        asm.append("    mov rbp, rsp\n");
        asm.append("    sub rsp, 40          ; 影子空间(32) + 栈对齐(8)\n\n");
    }

    private void emitFunctionEnd(String funcName) {
        asm.append("\n").append(funcName).append("_end:\n");
        asm.append("    add rsp, 40\n");
        asm.append("    pop rbp\n");
        asm.append("    ret\n\n");
    }

    // ==================== 工具方法 ====================

    /**
     * 将 Java 字符串转为 NASM 单引号安全内容
     */
    private String escapeNasmString(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (c == '\'') {
                sb.append("','");
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}