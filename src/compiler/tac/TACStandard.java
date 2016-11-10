/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac;
import compiler.Context.VarInfo;
import compiler.Operator;
import compiler.X86Emitter;
import compiler.X86Register;
import static compiler.tac.TACConst.typeFromRegister;
import compiler.type.TypeInt32;
import compiler.type.TypeNumerical;
import compiler.type.TypePointer;

/**
 *
 * @author leijurv
 */
public class TACStandard extends TACStatement {
    public VarInfo result;
    public VarInfo first;
    public VarInfo second;
    public String resultName;
    public String firstName;
    public String secondName;
    public final Operator op;
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
        result = context.getRequired(resultName);
        first = context.getRequired(firstName);
        second = context.getRequired(secondName);
        if (!result.getType().equals(op.onApplication(first.getType(), second.getType()))) {
            throw new IllegalStateException();
        }
        if (!first.getType().equals(second.getType())) {
            if (first.getType() instanceof TypePointer) {
                return;
            }
            throw new IllegalStateException(this + "");
        }
    }
    @Override
    public void printx86(X86Emitter emit) {
        TypeNumerical type = firstName.startsWith(X86Register.REGISTER_PREFIX) ? typeFromRegister(firstName) : (first == null ? new TypeInt32() : (TypeNumerical) first.getType());
        String a = X86Register.A.getRegister(type);
        String b = X86Register.B.getRegister(type);
        String d = X86Register.D.getRegister(type);
        String mov = "mov" + type.x86typesuffix() + " ";
        TACConst.move(a, null, first, firstName, emit);
        TACConst.move(b, null, second, secondName, emit);
        if (op != Operator.PLUS && op != Operator.MINUS) {
            if (!(type instanceof TypeInt32)) {
                throw new IllegalStateException("You can only do " + op + " on int32s and not other types of ints becasue I wrote this on a plane and I can't google the right syntax and my guesses were wrong");
            }
        }
        switch (op) {
            case PLUS:
                emit.addStatement("add" + type.x86typesuffix() + " " + a + ", " + b);
                emit.addStatement(mov + b + ", " + result.x86());
                break;
            case MINUS:
                emit.addStatement("sub" + type.x86typesuffix() + " " + b + ", " + a);
                emit.addStatement(mov + a + ", " + result.x86());
                break;
            case MOD:
                emit.addStatement("xor " + d + ", " + d);
                emit.addStatement("idivl " + b);
                emit.addStatement(mov + d + ", " + result.x86());
                break;
            case DIVIDE:
                emit.addStatement("xor " + d + ", " + d);
                emit.addStatement("idivl " + b);
                emit.addStatement(mov + a + ", " + result.x86());
                break;
            case MULTIPLY:
                emit.addStatement("imull " + b + ", " + a);
                emit.addStatement(mov + a + ", " + result.x86());
                break;
            default:
                throw new IllegalStateException(op + "");
        }
    }
}
