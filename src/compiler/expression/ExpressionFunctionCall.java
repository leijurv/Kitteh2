/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.expression;
import compiler.Context;
import compiler.command.CommandDefineFunction.FunctionHeader;
import compiler.tac.IREmitter;
import compiler.tac.TACFunctionCall;
import compiler.tac.TempVarUsage;
import compiler.type.Type;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 *
 * @author leijurv
 */
public class ExpressionFunctionCall extends Expression {
    String funcName;
    ArrayList<Expression> args;
    FunctionHeader calling;
    public ExpressionFunctionCall(Context context, String funcName, ArrayList<Expression> args) {
        this.funcName = funcName;
        this.args = args;
        this.calling = context.gc.getHeader(funcName);
        verifyTypes();
    }
    private void verifyTypes() {
        ArrayList<Type> expected = calling.inputs();
        if (expected.size() != args.size()) {
            throw new IllegalStateException("Expected " + expected.size() + " args, actually got " + args.size());
        }
        ArrayList<Type> got = args.stream().map(arg -> arg.getType()).collect(Collectors.toCollection(ArrayList::new));
        if (!got.equals(expected)) {
            throw new IllegalStateException("Expected types " + expected + ", got types " + got);
        }
    }
    @Override
    public Type calcType() {
        return calling.getReturnType();
    }
    @Override
    public String toString() {
        return funcName + args;
    }
    @Override
    public void generateTAC(IREmitter emit, TempVarUsage tempVars, String resultLocation) {
        ArrayList<String> argNames = args.stream().map((exp) -> {
            String tempName = tempVars.getTempVar(exp.getType());
            exp.generateTAC(emit, tempVars, tempName);
            return tempName;
        }).collect(Collectors.toCollection(ArrayList::new));
        emit.emit(new TACFunctionCall(resultLocation, funcName, argNames));
    }
    @Override
    public int calculateTACLength() {
        int sum = args.parallelStream().mapToInt(com -> com.getTACLength()).sum();//parallel because calculating tac length can be slow, and it can be multithreaded /s
        return sum + 1;
    }
    @Override
    public Expression insertKnownValues(Context context) {
        IntStream.range(0, args.size()).parallel().forEach(i -> {//gotta go fast
            args.set(i, args.get(i).insertKnownValues(context));//.parallel() == sanik
        });
        return this;
    }
    @Override
    public Expression calculateConstants() {
        IntStream.range(0, args.size()).parallel().forEach(i -> {//gotta go fast
            args.set(i, args.get(i).calculateConstants());//.parallel() == sanik
        });
        return this;
    }
    @Override
    public boolean canBeCommand() {
        return true;
    }
}
