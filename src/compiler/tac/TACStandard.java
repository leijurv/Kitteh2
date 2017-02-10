/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac;
import compiler.Context.VarInfo;
import compiler.Operator;
import static compiler.Operator.*;
import compiler.type.Type;
import compiler.type.TypeFloat;
import compiler.type.TypeInt16;
import compiler.type.TypeInt32;
import compiler.type.TypeInt64;
import compiler.type.TypeInt8;
import compiler.type.TypeNumerical;
import compiler.type.TypePointer;
import compiler.x86.X86Comparison;
import compiler.asm.ASMConst;
import compiler.x86.X86Emitter;
import compiler.x86.X86Register;
import compiler.x86.X86TypedRegister;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.IllegalChannelGroupException;
import java.util.Arrays;
import java.util.List;
import compiler.asm.ASMParam;

/**
 *
 * @author leijurv
 */
public class TACStandard extends TACStatement {
    public final Operator op;
    public TACStandard(String resultName, String firstName, String secondName, Operator op) {
        super(firstName, secondName, resultName);
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
        return Arrays.asList(paramNames[0], paramNames[1]);
    }
    @Override
    public List<String> modifiedVariables() {
        return Arrays.asList(paramNames[2]);
    }
    @Override
    public String toString0() {
        return params[2] + " = " + params[0] + " " + op + " " + params[1];
    }
    @Override
    public void onContextKnown() {
        if (!params[2].getType().equals(op.onApplication(params[0].getType(), params[1].getType()))) {
            throw new IllegalThreadStateException();
        }
        if (!params[0].getType().equals(params[1].getType())) {
            if (params[0].getType() instanceof TypePointer && (op == PLUS || op == MINUS)) {
                return;
            }
            throw new IllegalStateException(this + "");
        }
    }
    @Override
    public boolean usesDRegister() {//I'm sorry. I'm really really sorry.
        if (op == MOD || op == DIVIDE) {//even though the x86 string for divide won't contain the d register, it will overwrite the d register (via cqto)
            return true;
        }
        X86Emitter emit = new X86Emitter("");
        printx86(emit);
        String aoeu = emit.toX86();
        for (TypeNumerical tn : new TypeNumerical[]{new TypeInt8(), new TypeInt16(), new TypeInt32(), new TypeInt64()}) {
            if (aoeu.contains(X86Register.D.getRegister(tn).x86())) {
                return true;
            }
        }
        return false;
    }
    @Override
    public void printx86(X86Emitter emit) {
        if (op.inputsReversible()) {
            X86Emitter ns = new X86Emitter("");
            x86(ns, paramNames[0], paramNames[1], paramNames[2], params[0], params[1], params[2], op);
            X86Emitter s = new X86Emitter("");
            x86(s, paramNames[1], paramNames[0], paramNames[2], params[1], params[0], params[2], op);
            if (s.withoutComments().length() < ns.withoutComments().length()) {//heuristic of the actual x86 length is pretty good. if it's actully an instruction shorter, the x86 will def be shorter. and if it can replace with cltq, that's also shorter
                if (compiler.Compiler.verbose()) {
                    emit.addComment("Operands swapped");
                    /*System.out.println("SWAPPING FROM");
                    System.out.println(ns.withoutComments());
                    System.out.println("TO");
                    System.out.println(s.withoutComments());*/ // a little spammy
                }
                x86(emit, paramNames[1], paramNames[0], paramNames[2], params[1], params[0], params[2], op);
                return;
            }
        }
        x86(emit, paramNames[0], paramNames[1], paramNames[2], params[0], params[1], params[2], op);
    }
    public static void x86(X86Emitter emit, String firstName, String secondName, String resultName, ASMParam fst, ASMParam snd, ASMParam result, Operator op) {//oh god, this function.
        ASMParam first = fst;
        ASMParam second = snd;
        //i literally can't be bothered
        TypeNumerical type = (TypeNumerical) result.getType();
        if (secondName.equals("1") && !(result instanceof VarInfo && !firstName.equals(resultName))) {
            if (!firstName.equals(resultName) && (op == PLUS || op == MINUS)) {
                TACConst.move(result, first, emit);
            }
            if (op == PLUS) {
                emit.addStatement("inc" + type.x86typesuffix() + " " + result.x86());
                return;
            }
            if (op == MINUS) {
                emit.addStatement("dec" + type.x86typesuffix() + " " + result.x86());
                return;
            }
        }
        if (firstName.equals("1") && op == PLUS && !(result instanceof VarInfo && !firstName.equals(resultName))) {
            if (!secondName.equals(resultName)) {
                TACConst.move(result, second, emit);
            }
            emit.addStatement("inc" + type.x86typesuffix() + " " + result.x86());
            return;
        }
        if (firstName.equals("0") && op == MINUS && resultName.equals(secondName)) {
            emit.addStatement("neg" + type.x86typesuffix() + " " + result.x86());
            return;
        }
        if (op == PLUS && type.getSizeBytes() >= 4 && first instanceof X86TypedRegister && result instanceof X86TypedRegister && !firstName.equals(resultName) && !secondName.equals(resultName) && first.getType().equals(type) && second.getType().equals(type)) {
            //TODO more benchmarks to determine if/when this is a performance boost
            if (second instanceof X86TypedRegister) {
                emit.addStatement("lea" + type.x86typesuffix() + " (" + first.x86() + ", " + second.x86() + ", 1), " + result.x86());
                return;
            }
            if (second instanceof ASMConst) {
                emit.addStatement("lea" + type.x86typesuffix() + " " + second.x86().substring(1) + "(" + first.x86() + "), " + result.x86());
                return;
            }
        }
        if (op == LESS || op == GREATER || op == EQUAL || op == NOT_EQUAL || op == GREATER_OR_EQUAL || op == LESS_OR_EQUAL) {
            Operator o = TACJumpCmp.createCompare(first, second, op, emit);
            String set = X86Comparison.tox86set(o);
            if (first.getType() instanceof TypeFloat) {
                set = set.replace("l", "b").replace("g", "a");//i actually want to die
            }
            emit.addStatement(set + " " + result.x86());
            return;
        }
        X86TypedRegister aa = X86Register.A.getRegister(type);
        X86TypedRegister cc = X86Register.C.getRegister(type);
        if (type instanceof TypeFloat) {
            aa = X86Register.XMM0.getRegister(type);
            cc = X86Register.XMM1.getRegister(type);
        }
        String a = aa.x86();
        String c = cc.x86();
        boolean ma = false;
        String shaft = X86Register.C.getRegister(new TypeInt8()).x86();
        if (type instanceof TypePointer) {//if second is null that means it's a const in secondName, and if that's the case we don't need to do special cases
            //pointer arithmetic, oh boy pls no
            //what are we adding to the pointer
            ASMParam nonPointer = first.getType() instanceof TypePointer ? second : first;
            if (!(nonPointer.getType() instanceof TypeNumerical)) {
                throw new ClosedSelectorException();
            }
            Type secondType = nonPointer.getType();
            if (!(secondType instanceof TypeInt8 || secondType instanceof TypeInt16 || secondType instanceof TypeInt32 || secondType instanceof TypeInt64)) {
                throw new IllegalStateException(nonPointer.getType().toString() + " " + nonPointer.getClass());
            }
            //we put the pointer in A
            //and the integer in C
            if (nonPointer instanceof ASMConst) {
                nonPointer = new ASMConst(((ASMConst) nonPointer).getValue(), type);//its probably a const int that we are trying to add to an 8 byte pointer
                //since its literally a const number, just change the type
                if (first.getType() instanceof TypePointer) {
                    second = nonPointer;
                } else {
                    first = nonPointer;
                }
            }
        }
        if ((second instanceof ASMConst || second instanceof X86TypedRegister || second instanceof VarInfo) && !(op == MOD || op == DIVIDE || ((second instanceof X86TypedRegister || second instanceof VarInfo) && (op == USHIFT_L || op == USHIFT_R || op == SHIFT_L || op == SHIFT_R)))) {
            if (second.getType().getSizeBytes() == cc.getType().getSizeBytes()) {
                c = second.x86();
                shaft = c;
            } else {
                if (second instanceof ASMConst) {
                    throw new IllegalStateException();
                }
                TACCast.cast(second, cc, emit);
            }
        } else {
            try {
                TACConst.move(cc, second, emit);
            } catch (Exception e) {
                throw new UnsupportedOperationException(type + " " + firstName + " " + secondName, e);
            }
        }
        if (result instanceof X86TypedRegister && op != MOD && op != DIVIDE && (!secondName.equals(resultName) || op == USHIFT_L || op == USHIFT_R || op == SHIFT_L || op == SHIFT_R)) {//TODO secondName.equals(resultName) may cause unintended behavior...
            aa = (X86TypedRegister) result;
            a = result.x86();
            ma = true;
        }
        if (second instanceof X86TypedRegister && aa.getRegister() == ((X86TypedRegister) second).getRegister() && !ma && ((X86TypedRegister) cc).getRegister() == X86Register.C) {
            aa = (cc.x86().equals(c) ? X86Register.D : X86Register.C).getRegister(type);
            a = aa.x86();
        }
        if (aa.getType().getSizeBytes() == first.getType().getSizeBytes()) {
            TACConst.move(aa, first, emit);
        } else {
            TACCast.cast(first, aa, emit);
        }
        switch (op) {
            case DIVIDE:
                if (type instanceof TypeFloat) {
                    emit.addStatement("div" + type.x86typesuffix() + " " + c + ", " + a);
                    break;
                }
                emit.move(aa, X86Register.A);
                emit.move(cc, X86Register.C);
                if (type.getSizeBytes() == 8) {
                    emit.addStatement("cqto");
                } else {
                    ASMParam d = X86Register.D.getRegister(type);
                    emit.move(aa, d);
                    emit.addStatement("sar" + type.x86typesuffix() + " $" + (type.getSizeBytes() * 8 - 1) + ", " + d.x86());
                }
                emit.addStatement("idiv" + type.x86typesuffix() + " " + X86Register.C.getRegister(type).x86());
                emit.move(X86Register.A, result);
                return;
            case MOD:
                emit.move(aa, X86Register.A);
                emit.move(cc, X86Register.C);
                if (type.getSizeBytes() == 8) {
                    emit.addStatement("cqto");
                } else {
                    ASMParam d = X86Register.D.getRegister(type);
                    emit.move(aa, d);
                    emit.addStatement("sar" + type.x86typesuffix() + " $" + (type.getSizeBytes() * 8 - 1) + ", " + d.x86());
                }
                emit.addStatement("idiv" + type.x86typesuffix() + " " + X86Register.C.getRegister(type).x86());
                emit.move(X86Register.D, result);
                return;
            case MULTIPLY:
                if (!(type instanceof TypeFloat)) {
                    emit.addStatement("imul" + type.x86typesuffix() + " " + c + ", " + a);
                    break;
                }
            case PLUS:
                if (c.equals("$1")) {//you can never multiply the $1 literal by a float (0x00 00 00 01 isn't a valid packed float anyway), so this is okay
                    emit.addStatement("inc" + type.x86typesuffix() + " " + a);//even if it does somehow happen, "incss" isn't valid x86 so it won't fail silently *shrug*
                    break;
                }
            case MINUS:
                if (c.equals("$1")) {//same scenario
                    emit.addStatement("dec" + type.x86typesuffix() + " " + a);
                    break;
                }
            default:
                emit.addStatement(op.x86() + type.x86typesuffix() + " " + (op == USHIFT_L || op == USHIFT_R || op == SHIFT_L || op == SHIFT_R ? shaft : c) + ", " + a);
                break;
        }
        if (!ma) {
            emit.move(aa, result);
        }
    }
}
