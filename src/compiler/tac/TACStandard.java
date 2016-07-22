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
        if (op != Operator.PLUS) {
            throw new IllegalStateException();
        }
        emit.addStatement("movl " + second.x86() + ", %ecx");
        emit.addStatement("movl " + first.x86() + ", %eax");
        emit.addStatement("addl %eax, %ecx");
        emit.addStatement("movl %ecx, " + result.x86());
    }
}
