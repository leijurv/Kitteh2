/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.expression;
import compiler.Context;
import compiler.Context.VarInfo;
import compiler.command.CommandDefineFunction.FunctionHeader;
import compiler.tac.IREmitter;
import compiler.tac.TACFunctionCall;
import compiler.tac.TACJumpBoolVar;
import compiler.tac.TempVarUsage;
import compiler.type.Type;
import compiler.type.TypeBoolean;
import compiler.type.TypeNumerical;
import compiler.type.TypePointer;
import compiler.x86.X86Param;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author leijurv
 */
public class ExpressionFunctionCall extends ExpressionConditionalJumpable {
    private final List<Expression> args;
    private final FunctionHeader calling;
    public ExpressionFunctionCall(Context context, String pkgName, String funcName, List<Expression> args) {
        this.args = args;
        String fn = funcName;
        if (pkgName == null && funcName.equals("print") && args.size() == 1 && args.get(0).getType() instanceof TypePointer) {
            //print out the value at the pointer addr not the numerical pointer address
            fn = "writeNullTerm";
        }
        this.calling = context.gc.getHeader(pkgName, fn);
        verifyTypes();
    }
    public String callingName() {
        return calling.name;
    }
    public List<Expression> calling() {
        return args;
    }
    public FunctionHeader header() {
        return calling;
    }
    private void verifyTypes() {
        List<Type> expected = calling.inputs();
        if (expected.size() != args.size() && !calling.name.equals("syscall")) {
            throw new IllegalStateException("Expected " + expected.size() + " args, actually got " + args.size());
        }
        List<Type> got = args.stream().<Type>map(Expression::getType).collect(Collectors.toList());
        if (!got.equals(expected)) {
            if (calling.name.endsWith("__print") && got.get(0) instanceof TypeNumerical) {
                //good enough
                return;
            }
            if (calling.name.equals("free") && got.get(0) instanceof TypePointer) {
                //good enough
                return;
            }
            if (calling.name.equals("syscall")) {
                //good enough
                return;
            }
            throw new IllegalStateException(calling.name + " expected types " + expected + ", got types " + got);
        }
    }
    @Override
    public Type calcType() {
        return calling.getReturnType();
    }
    @Override
    public String toString() {
        return calling.name + args;
    }
    public void multipleReturns(IREmitter emit, TempVarUsage tempVars, X86Param... resultLocation) {
        List<X86Param> argNames = args.stream().map(exp -> {
            VarInfo tempName = tempVars.getTempVar(exp.getType());
            exp.generateTAC(emit, tempVars, tempName);
            return tempName;
        }).collect(Collectors.toList());
        emit.emit(new TACFunctionCall(emit.getContext(), calling, argNames, resultLocation));
    }
    @Override
    public void generateTAC(IREmitter emit, TempVarUsage tempVars, VarInfo resultLocation) {
        if (resultLocation == null) {
            multipleReturns(emit, tempVars);
        } else {
            multipleReturns(emit, tempVars, resultLocation);
        }
    }
    @Override
    public int calculateTACLength() {
        int sum = args.stream().mapToInt(Expression::getTACLength).sum();
        return sum + 1;
    }
    @Override
    public Expression insertKnownValues(Context context) {
        /*IntStream.range(0, args.size()).parallel().forEach(i -> {//gotta go fast
            args.set(i, args.get(i).insertKnownValues(context));//.parallel() == sanik
        });*/
        for (int i = 0; i < args.size(); i++) {
            args.set(i, args.get(i).insertKnownValues(context));
        }
        return this;
    }
    @Override
    public Expression calculateConstants() {
        /*IntStream.range(0, args.size()).parallel().forEach(i -> {//gotta go fast
            args.set(i, args.get(i).calculateConstants());//.parallel() == sanik
        });*/
        for (int i = 0; i < args.size(); i++) {
            args.set(i, args.get(i).calculateConstants());
        }
        return this;
    }
    @Override
    public boolean canBeCommand() {
        return true;
    }
    @Override
    public void generateConditionalJump(IREmitter emit, TempVarUsage tempVars, int jumpTo, boolean invert) {
        VarInfo tmp = tempVars.getTempVar(new TypeBoolean());
        generateTAC(emit, tempVars, tmp);
        emit.emit(new TACJumpBoolVar(tmp, jumpTo, invert));
    }
    @Override
    public int condLength() {
        return 1 + getTACLength();
    }
}
