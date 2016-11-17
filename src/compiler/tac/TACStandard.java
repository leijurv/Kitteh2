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
import compiler.type.TypeInt64;
import compiler.type.TypeNumerical;
import compiler.type.TypePointer;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.IllegalChannelGroupException;
import java.util.Arrays;
import java.util.List;

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
        try {
            Integer.parseInt(firstName);
            Integer.parseInt(secondName);
            throw new IllegalChannelGroupException();
        } catch (NumberFormatException e) {
        }
        this.op = op;
    }
    @Override
    public List<String> requiredVariables() {
        return Arrays.asList(firstName, secondName);
    }
    @Override
    public List<String> modifiedVariables() {
        return Arrays.asList(resultName);
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
            throw new IllegalThreadStateException();
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
        TypeNumerical type;
        if (firstName.startsWith(X86Register.REGISTER_PREFIX)) {
            type = typeFromRegister(firstName);
        } else if (first == null) {
            if (second == null) {
                throw new RuntimeException("that optimization related exception again " + this);
                //type = (TypeNumerical) result.getType(); //this is a workaround for when i'm lazy
            } else {
                type = (TypeNumerical) second.getType();
            }
        } else {
            type = (TypeNumerical) first.getType();
        }
        String a = X86Register.A.getRegister(type);
        String c = X86Register.C.getRegister(type);
        String d = X86Register.D.getRegister(type);
        String mov = "mov" + type.x86typesuffix() + " ";
        TACConst.move(a, null, first, firstName, emit);
        if (type instanceof TypePointer && second != null) {//if second is null that means it's a const in secondName, and if that's the case we don't need to do special cases
            //pointer arithmetic, oh boy pls no
            //what are we adding to the pointer
            if (!(second.getType() instanceof TypeNumerical)) {
                throw new ClosedSelectorException();
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
            try {
                TACConst.move(c, null, second, secondName, emit);
            } catch (Exception e) {
                throw new UnsupportedOperationException(this + " " + type + " " + firstName + " " + secondName, e);
            }
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
            case LESS:
            case GREATER:
            case EQUAL:
            case NOT_EQUAL:
            case GREATER_OR_EQUAL:
            case LESS_OR_EQUAL:
                emit.addStatement("cmp" + type.x86typesuffix() + " " + c + ", " + a);
                emit.addStatement(op.tox86set() + " %cl");
                emit.addStatement("movb %cl, " + result.x86());
                break;
            default:
                throw new IllegalStateException(op + "");
        }
    }
}
