/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac;
import compiler.Context.VarInfo;
import compiler.Operator;
import compiler.type.Type;
import compiler.type.TypeBoolean;
import compiler.type.TypeFloat;
import compiler.type.TypeInt16;
import compiler.type.TypeInt32;
import compiler.type.TypeInt64;
import compiler.type.TypeInt8;
import compiler.type.TypeNumerical;
import compiler.type.TypePointer;
import compiler.x86.X86Comparison;
import compiler.x86.X86Const;
import compiler.x86.X86Emitter;
import compiler.x86.X86Param;
import compiler.x86.X86Register;
import compiler.x86.X86TypedRegister;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.IllegalChannelGroupException;
import java.util.Arrays;
import java.util.List;

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
            if (params[0].getType() instanceof TypePointer && (op == Operator.PLUS || op == Operator.MINUS)) {
                return;
            }
            throw new IllegalStateException(this + "");
        }
    }
    @Override
    public void printx86(X86Emitter emit) {
        String firstName = paramNames[0];//i literally can't be bothered
        String secondName = paramNames[1];
        X86Param first = params[0];
        X86Param second = params[1];
        TypeNumerical type;
        //if (firstName.startsWith(X86Register.REGISTER_PREFIX)) {
        //    type = X86Register.typeFromRegister(firstName);
        //    }
        /*else if (first == null) {
            if (second == null) {
                throw new RuntimeException("that optimization related exception again " + this);
                //type = (TypeNumerical) result.getType(); //this is a workaround for when i'm lazy
            } else {
                type = (TypeNumerical) second.getType();
            }
       // } else {
         */
        type = (TypeNumerical) first.getType();
        //}
        X86Param result = params[2];
        if (secondName.equals("1") && firstName.equals(paramNames[2])) {
            if (op == Operator.PLUS) {
                emit.addStatement("inc" + type.x86typesuffix() + " " + result.x86());
                return;
            }
            if (op == Operator.MINUS) {
                emit.addStatement("dec" + type.x86typesuffix() + " " + result.x86());
                return;
            }
        }
        if (firstName.equals("1") && secondName.equals(paramNames[2]) && op == Operator.PLUS) {
            emit.addStatement("inc" + type.x86typesuffix() + " " + result.x86());
            return;
        }
        X86TypedRegister aa = X86Register.A.getRegister(type);
        X86TypedRegister cc = X86Register.C.getRegister(type);
        X86TypedRegister dd = X86Register.D.getRegister(type);
        if (type instanceof TypeFloat) {
            aa = X86Register.XMM0.getRegister(type);
            cc = X86Register.XMM1.getRegister(type);
        }
        String a = aa.x86();
        String c = cc.x86();
        String d = type instanceof TypeFloat ? null : dd.x86();
        TACConst.move(aa, first, emit);
        String shaft = X86Register.C.getRegister(new TypeInt8()).x86();
        boolean thing = false;
        if (type instanceof TypePointer && (second instanceof VarInfo || second instanceof X86Const || second instanceof X86TypedRegister)) {//if second is null that means it's a const in secondName, and if that's the case we don't need to do special cases
            //pointer arithmetic, oh boy pls no
            //what are we adding to the pointer
            if (!(second.getType() instanceof TypeNumerical)) {
                throw new ClosedSelectorException();
            }
            Type secondType = second.getType();
            if (!(secondType instanceof TypeInt8 || secondType instanceof TypeInt16 || secondType instanceof TypeInt32 || secondType instanceof TypeInt64)) {
                throw new IllegalStateException(this + " " + second.getType().toString() + " " + second.getClass());
            }
            //we put the pointer in A
            //and the integer in C
            if (second instanceof X86Const) {
                second = new X86Const(((X86Const) second).getValue(), new TypeInt64());//its probably a const int that we are trying to add to an 8 byte pointer
                //since its literally a const number, just change the type
            }
            if (second.getType().getSizeBytes() == first.getType().getSizeBytes() || second instanceof X86Const) {
                thing = true;
            } else {
                emit.cast(second, X86Register.C.getRegister(new TypeInt64()));
            }
        } else {
            thing = true;
        }
        if (thing) {
            if ((second instanceof X86Const || second instanceof X86TypedRegister) && !(op == Operator.MOD || op == Operator.DIVIDE || ((second instanceof X86TypedRegister) && (op == Operator.USHIFT_L || op == Operator.USHIFT_R || op == Operator.SHIFT_L || op == Operator.SHIFT_R)))) {
                c = second.x86();
                shaft = c;
            } else {
                try {
                    TACConst.move(cc, second, emit);
                } catch (Exception e) {
                    throw new UnsupportedOperationException(this + " " + type + " " + firstName + " " + secondName, e);
                }
            }
        }
        switch (op) {
            case PLUS:
                if (c.equals("$1")) {
                    emit.addStatement("inc" + type.x86typesuffix() + " " + a);
                    emit.move(aa, result);
                    break;
                }
                emit.addStatement("add" + type.x86typesuffix() + " " + c + ", " + a);
                emit.move(aa, result);
                break;
            case MINUS:
                if (c.equals("$1")) {
                    emit.addStatement("dec" + type.x86typesuffix() + " " + a);
                    emit.move(aa, result);
                    break;
                }
                emit.addStatement("sub" + type.x86typesuffix() + " " + c + ", " + a);
                emit.move(aa, result);
                break;
            case MOD:
                if (type.getSizeBytes() == 8) {
                    emit.addStatement(signExtend(type.getSizeBytes()));
                } else {
                    emit.move(aa, dd);
                    emit.addStatement("sar" + type.x86typesuffix() + " $" + (type.getSizeBytes() * 8 - 1) + ", " + d);
                }
                emit.addStatement("idiv" + type.x86typesuffix() + " " + c);
                emit.move(dd, result);
                break;
            case USHIFT_L:
                emit.addStatement("shl" + type.x86typesuffix() + " " + shaft + ", " + a);
                emit.move(aa, result);
                break;
            case USHIFT_R:
                emit.addStatement("shr" + type.x86typesuffix() + " " + shaft + ", " + a);
                emit.move(aa, result);
                break;
            case SHIFT_L:
                emit.addStatement("sal" + type.x86typesuffix() + " " + shaft + ", " + a);
                emit.move(aa, result);
                break;
            case SHIFT_R:
                emit.addStatement("sar" + type.x86typesuffix() + " " + shaft + ", " + a);
                emit.move(aa, result);
                break;
            case L_XOR:
                emit.addStatement("xor" + type.x86typesuffix() + " " + c + ", " + a);
                emit.move(aa, result);
                break;
            case L_AND:
                emit.addStatement("and" + type.x86typesuffix() + " " + c + ", " + a);
                emit.move(aa, result);
                break;
            case L_OR:
                emit.addStatement("or" + type.x86typesuffix() + " " + c + ", " + a);
                emit.move(aa, result);
                break;
            case DIVIDE:
                if (type instanceof TypeFloat) {
                    emit.addStatement("div" + type.x86typesuffix() + " " + c + ", " + a);
                    emit.move(aa, result);
                    break;
                }
                if (type.getSizeBytes() == 8) {
                    emit.addStatement(signExtend(type.getSizeBytes()));
                } else {
                    emit.move(aa, dd);
                    emit.addStatement("sar" + type.x86typesuffix() + " $" + (type.getSizeBytes() * 8 - 1) + ", " + d);
                }
                emit.addStatement("idiv" + type.x86typesuffix() + " " + c);
                emit.move(aa, result);
                break;
            case MULTIPLY:
                emit.addStatement((type instanceof TypeFloat ? "" : "i") + "mul" + type.x86typesuffix() + " " + c + ", " + a);
                emit.move(aa, result);
                break;
            case LESS:
            case GREATER:
            case EQUAL:
            case NOT_EQUAL:
            case GREATER_OR_EQUAL:
            case LESS_OR_EQUAL:
                String comparison = "cmp" + type.x86typesuffix();
                if (type instanceof TypeFloat) {
                    comparison = "ucomiss";//please, x86, why
                }
                emit.addStatement(comparison + " " + c + ", " + a);
                String set = X86Comparison.tox86set(op);
                if (first.getType() instanceof TypeFloat) {
                    set = set.replace("l", "b").replace("g", "a");//i actually want to die
                }
                emit.addStatement(set + " %cl");
                emit.move(X86Register.C.getRegister(new TypeBoolean()), result);
                break;
            default:
                throw new IllegalStateException(op + "");
        }
    }
    private static String signExtend(int size) {
        switch (size) {
            case 8:
                return "cqto";
            case 4:
                return "cltq";
            case 2:
                return "cwtl";
            case 1:
                return "cbtw";
            default:
                throw new RuntimeException();
        }
    }
}
