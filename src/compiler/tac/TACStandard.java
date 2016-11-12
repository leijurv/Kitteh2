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
import compiler.type.TypeInt64;
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
        return result + " = " + (first == null ? firstName : first) + " " + op + " " + (second == null ? secondName : second);
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
        String c = X86Register.C.getRegister(type);
        String d = X86Register.D.getRegister(type);
        String mov = "mov" + type.x86typesuffix() + " ";
        TACConst.move(a, null, first, firstName, emit);
        if (type instanceof TypePointer && second != null) {//if second is null that means it's a const in secondName, and if that's the case we don't need to do special cases
            //pointer arithmetic, oh boy pls no
            //what are we adding to the pointer
            if (!(second.getType() instanceof TypeNumerical)) {
                throw new IllegalStateException();
            }
            if (!second.getType().getClass().toString().contains("TypeInt")) {//look bud i'm not perfect
                throw new IllegalStateException(second.getType().toString());
            }
            //we put the pointer in A
            //and the integer in B
            if (second.getType().getSizeBytes() == first.getType().getSizeBytes()) {
                TACConst.move(c, null, second, secondName, emit);
            } else {
                emit.addStatement("movs" + ((TypeNumerical) second.getType()).x86typesuffix() + "q " + second.x86() + "," + X86Register.C.getRegister(new TypeInt64()));
            }
        } else {
            TACConst.move(c, null, second, secondName, emit);
        }
        switch (op) {
            case PLUS:
                emit.addStatement("add" + type.x86typesuffix() + " " + c + ", " + a);
                emit.addStatement(mov + a + ", " + result.x86());
                break;
            case MINUS:
                emit.addStatement("sub" + type.x86typesuffix() + " " + c + ", " + a);
                emit.addStatement(mov + a + ", " + result.x86());
                break;
            case MOD:
                emit.addStatement("xor" + type.x86typesuffix() + " " + d + ", " + d);
                emit.addStatement("idiv" + type.x86typesuffix() + " " + c);
                emit.addStatement(mov + d + ", " + result.x86());
                break;
            case DIVIDE:
                emit.addStatement("xor" + type.x86typesuffix() + " " + d + ", " + d);
                emit.addStatement("idiv" + type.x86typesuffix() + " " + c);
                emit.addStatement(mov + a + ", " + result.x86());
                break;
            case MULTIPLY:
                emit.addStatement("imul" + type.x86typesuffix() + " " + c + ", " + a);
                emit.addStatement(mov + a + ", " + result.x86());
                break;
            default:
                throw new IllegalStateException(op + "");
        }
    }
}
