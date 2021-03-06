/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.command;
import compiler.Context;
import compiler.Context.VarInfo;
import compiler.expression.Expression;
import compiler.expression.ExpressionConst;
import compiler.tac.IREmitter;
import compiler.tac.TempVarUsage;
import compiler.type.TypeFloat;
import java.util.stream.Stream;

/**
 *
 * @author leijurv
 */
public class CommandSetVar extends Command {
    private Expression val;
    private final String var;
    public CommandSetVar(String var, Expression val, Context context) {
        super(context);
        this.val = val;
        this.var = var;
    }
    @Override
    public void generateTAC0(IREmitter emit) {
        /*if (val.getType() instanceof TypeBoolean) {
            TempVarUsage hm = new TempVarUsage(context);
            int ifTrue = emit.lineNumberOfNextStatement() + ((ExpressionConditionalJumpable) val).condLength() + 2;
            int ifFalse = ifTrue + 1;
            ((ExpressionConditionalJumpable) val).generateConditionalJump(emit, hm, ifTrue, false);
            emit.emit(new TACConst(var, "0"));
            emit.emit(new TACJump(ifFalse));
            emit.emit(new TACConst(var, "1"));
        } else {*/
        val.generateTAC(emit, new TempVarUsage(context), (VarInfo) context.get(var));//this one, at least, is easy
        //}
    }
    public String varSet() {
        return var;
    }
    public Expression setTo() {
        return val;
    }
    @Override
    protected int calculateTACLength() {
        /*if (val.getType() instanceof TypeBoolean) {
            return ((ExpressionConditionalJumpable) val).condLength() + 3;
        }*/
        return val.getTACLength();
    }
    @Override
    public void staticValues() {
        val = val.insertKnownValues(context);
        val = val.calculateConstants();
        if (val instanceof ExpressionConst && !(val.getType() instanceof TypeFloat)) {
            //System.out.println(var + " is known to be " + val);
            context.setKnownValue(var, (ExpressionConst) val);
        } else {
            context.clearKnownValue(var);//we are setting it to something dynamic, so it's changed now
        }
    }
    @Override
    public Stream<String> getAllVarsModified() {
        return Stream.of(var);
    }
}
