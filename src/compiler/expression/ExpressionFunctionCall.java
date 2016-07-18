/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.expression;
import compiler.Context;
import compiler.tac.IREmitter;
import compiler.tac.TACFunctionCall;
import compiler.tac.TACFunctionParam;
import compiler.tac.TempVarUsage;
import compiler.type.Type;
import compiler.type.TypeVoid;
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
    public ExpressionFunctionCall(String funcName, ArrayList<Expression> args) {
        this.funcName = funcName;
        this.args = args;
    }
    @Override
    public Type calcType() {
        return new TypeVoid();
        //return new TypeInt32();
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
        for (int i = 0; i < argNames.size(); i++) {
            emit.emit(new TACFunctionParam(argNames.get(i), i));
        }
        emit.emit(new TACFunctionCall(resultLocation, funcName));
    }
    @Override
    public int calculateTACLength() {
        int sum = args.parallelStream().mapToInt(com -> com.getTACLength()).sum();//parallel because calculating tac length can be slow, and it can be multithreaded /s
        return sum + 1 + args.size();
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
}
