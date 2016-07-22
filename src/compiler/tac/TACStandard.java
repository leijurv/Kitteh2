/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac;
import compiler.Context.VarInfo;
import compiler.Operator;
import compiler.X86Emitter;

/**
 *
 * @author leijurv
 */
public class TACStandard extends TACStatement {
    VarInfo result;
    VarInfo first;
    VarInfo second;
    String resultName;
    String firstName;
    String secondName;
    Operator op;
    public TACStandard(String resultName, String firstName, String secondName, Operator op) {
        this.resultName = resultName;
        this.firstName = firstName;
        this.secondName = secondName;
        this.op = op;
    }
    @Override
    public String toString0() {
        return result + " = " + first + " " + op + " " + second;
    }
    @Override
    public void onContextKnown() {//TODO clean this up somehow, because this pattern is duplicated in all the TACs. maybe a hashmap in the superclass. idk
        result = context.get(resultName);
        first = context.get(firstName);
        second = context.get(secondName);
    }
    @Override
    public void printx86(X86Emitter emit) {
        emit.addStatement("movl " + second.x86() + ", %ebx");
        emit.addStatement("movl " + first.x86() + ", %eax");
        switch (op) {
            case PLUS:
                emit.addStatement("addl %eax, %ebx");
                emit.addStatement("movl %ebx, " + result.x86());
                break;
            case MINUS:
                emit.addStatement("subl %ebx, %eax");
                emit.addStatement("movl %eax, " + result.x86());
                break;
            case MOD:
                emit.addStatement("xor %edx, %edx");
                emit.addStatement("idivl %ebx");
                emit.addStatement("movl %edx, " + result.x86());
                break;
            case DIVIDE:
                emit.addStatement("xor %edx, %edx");
                emit.addStatement("idivl %ebx");
                emit.addStatement("movl %ebx, " + result.x86());
                break;
        }
    }
}
