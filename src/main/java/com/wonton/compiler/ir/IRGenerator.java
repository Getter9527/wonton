package com.wonton.compiler.ir;

import com.wonton.compiler.frontend.lexical.TokenType;
import com.wonton.compiler.frontend.syntax.node.Node;
import com.wonton.compiler.frontend.syntax.node.expression.*;
import com.wonton.compiler.frontend.syntax.node.statement.*;

import java.util.ArrayList;
import java.util.List;

/**
 * IR 生成器
 * <p>将 AST 转换为三元式中间表示</p>
 */
public class IRGenerator {

    private final List<Triplet> ir = new ArrayList<>();
    private int tempCounter = 0;

    /**
     * 生成 IR
     *
     * @param node 语法树节点
     * @return 三元式列表
     */
    public List<Triplet> generate(Node node) {
        visit(node);
        return ir;
    }

    /**
     * 访问节点
     *
     * @param node 节点
     */
    private void visit(com.wonton.compiler.frontend.syntax.node.Node node) {
        if (node instanceof FunctionDeclarationStmt) {
            generateFunction((FunctionDeclarationStmt) node);
        } else if (node instanceof VariableDeclarationStmt) {
            generateVariable((VariableDeclarationStmt) node);
        } else if (node instanceof AssignmentStmt) {
            generateAssignment((AssignmentStmt) node);
        } else if (node instanceof BlockStmt) {
            generateBlock((BlockStmt) node);
        }
    }

    /**
     * 生成语句的 IR
     *
     * @param stmt 语句节点
     */
    private void generateStmt(Stmt stmt) {
        if (stmt instanceof FunctionDeclarationStmt) {
            generateFunction((FunctionDeclarationStmt) stmt);
        } else if (stmt instanceof VariableDeclarationStmt) {
            generateVariable((VariableDeclarationStmt) stmt);
        } else if (stmt instanceof AssignmentStmt) {
            generateAssignment((AssignmentStmt) stmt);
        }
    }

    /**
     * 生成新的临时变量名
     *
     * @return 临时变量名
     */
    private String newTemp() {
        return "t" + (tempCounter++);
    }

    /**
     * 生成函数声明的 IR
     *
     * @param func 函数声明节点
     */
    private void generateFunction(FunctionDeclarationStmt func) {
        String funcName = func.getName().getLexeme();

        // 函数开始标记
        ir.add(new Triplet("FUNC_START", funcName, null, null));

        // 生成函数体 IR
        generateBlock(func.getBody());

        // 函数结束标记
        ir.add(new Triplet("FUNC_END", funcName, null, null));
    }

    /**
     * 生成变量声明的 IR
     *
     * @param var 变量声明节点
     */
    private void generateVariable(VariableDeclarationStmt var) {
        Expr initializer = var.getInitializer();
        if (initializer != null) {
            String exprResult = generateExpr(initializer);
            String varName = var.getIdentifier().getLexeme();
            // 赋值 IR：t1 = var
            ir.add(new Triplet("MOV", exprResult, null, varName));
        }
    }

    /**
     * 生成赋值语句的 IR
     *
     * @param assign 赋值语句节点
     */
    private void generateAssignment(AssignmentStmt assign) {
        String exprResult = generateExpr(assign.getValue());
        String varName = assign.getIdentifier().getLexeme();
        // 赋值 IR：t1 = var
        ir.add(new Triplet("MOV", exprResult, null, varName));
    }

    /**
     * 生成语句块的 IR
     *
     * @param block 语句块
     */
    private void generateBlock(BlockStmt block) {
        if (block == null) {
            return;
        }

        List<Stmt> stmts = block.getStmts();
        for (Stmt stmt : stmts) {
            generateStmt(stmt);
        }
    }

    /**
     * 生成表达式的 IR
     *
     * @param expr 表达式节点
     * @return 表达式结果变量名
     */
    private String generateExpr(Expr expr) {
        if (expr == null) {
            return null;
        }

        // 字面量
        if (expr instanceof IntegerExpr) {
            IntegerExpr intExpr = (IntegerExpr) expr;
            String temp = newTemp();
            ir.add(new Triplet("PUSH_INT", String.valueOf(intExpr.getValue()), temp));
            return temp;
        } else if (expr instanceof DecimalExpr) {
            DecimalExpr decExpr = (DecimalExpr) expr;
            String temp = newTemp();
            ir.add(new Triplet("PUSH_DEC", String.valueOf(decExpr.getValue()), temp));
            return temp;
        } else if (expr instanceof StringExpr) {
            StringExpr strExpr = (StringExpr) expr;
            String temp = newTemp();
            ir.add(new Triplet("PUSH_STR", strExpr.getValue(), temp));
            return temp;
        } else if (expr instanceof BooleanExpr) {
            BooleanExpr boolExpr = (BooleanExpr) expr;
            String temp = newTemp();
            ir.add(new Triplet("PUSH_BOOL", String.valueOf(boolExpr.getValue()), temp));
            return temp;
        } else if (expr instanceof NullExpr) {
            String temp = newTemp();
            ir.add(new Triplet("PUSH_NULL", null, temp));
            return temp;
        } else if (expr instanceof VariableExpr) {
            VariableExpr varExpr = (VariableExpr) expr;
            String varName = varExpr.getIdentifier().getLexeme();
            // 如果是变量引用，直接返回变量名
            // 如果需要加载值，可以添加 LOAD 指令
            return varName;
        } else if (expr instanceof BinaryExpr) {
            return generateBinaryExpr((BinaryExpr) expr);
        } else if (expr instanceof LogicalExpr) {
            return generateLogicalExpr((LogicalExpr) expr);
        }

        return null;
    }

    /**
     * 生成二元表达式的 IR
     *
     * @param expr 二元表达式
     * @return 结果变量名
     */
    private String generateBinaryExpr(BinaryExpr expr) {
        // 先递归生成左右操作数的 IR
        String left = generateExpr(expr.getLeft());
        String right = generateExpr(expr.getRight());

        // 生成三元式
        String result = newTemp();
        String operator = getOperatorCode(expr.getOperator().getType());

        ir.add(new Triplet(operator, left, right, result));

        return result;
    }

    /**
     * 生成交错表达式的 IR
     *
     * @param expr 交错表达式
     * @return 结果变量名
     */
    private String generateLogicalExpr(LogicalExpr expr) {
        // 先递归生成左右操作数的 IR
        String left = generateExpr(expr.getLeft());
        String right = generateExpr(expr.getRight());

        // 生成三元式
        String result = newTemp();
        String operator = getLogicalOperatorCode(expr.getOperator().getType());

        ir.add(new Triplet(operator, left, right, result));

        return result;
    }

    /**
     * 将运算符类型转换为 IR 操作符
     *
     * @param op 运算符类型
     * @return IR 操作符
     */
    private String getOperatorCode(TokenType op) {
        return switch (op) {
            case Plus -> "ADD";
            case Minus -> "SUB";
            case Star -> "MUL";
            case Slash -> "DIV";
            case Modulo -> "MOD";
            case Caret -> "POW";
            case Equalx2 -> "EQ";
            case NotEqual -> "NE";
            case Less -> "LT";
            case Greater -> "GT";
            case LessEqual -> "LE";
            case GreaterEqual -> "GE";
            default -> "UNKNOWN";
        };
    }

    /**
     * 将逻辑运算符类型转换为 IR 操作符
     *
     * @param op 运算符类型
     * @return IR 操作符
     */
    private String getLogicalOperatorCode(TokenType op) {
        return switch (op) {
            case And -> "AND";
            case Or -> "OR";
            case Not -> "NOT";
            default -> "UNKNOWN";
        };
    }

}
