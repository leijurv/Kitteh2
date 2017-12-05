/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.expression;
import compiler.Context;
import compiler.Context.VarInfo;
import compiler.tac.IREmitter;
import compiler.tac.TACConst;
import compiler.tac.TACJumpBoolVar;
import compiler.tac.TempVarUsage;
import compiler.type.Type;

/**
 *
 * @author leijurv
 */
public class ExpressionVariable extends ExpressionConditionalJumpable {
    private final String name;
    private final Type type;
    private final Context context;
    public ExpressionVariable(String name, Context context) {
        this.name = name;
        this.context = context;
        if (context.get(name) == null) {
            throw new IllegalStateException("variable not found " + name + " " + context);
        }
        this.type = context.get(name).getType();
    }
    @Override
    public Type calcType() {
        return type;
    }
    @Override
    public String toString() {
        return name;
    }
    public String getName() {
        return name;
    }
    @Override
    public void generateTAC(IREmitter emit, TempVarUsage tempVars, VarInfo resultLocation) {
        emit.emit(new TACConst(resultLocation, context.get(name)));
    }
    @Override
    public int calculateTACLength() {
        return 1;
    }
    @Override
    public Expression insertKnownValues(Context context) {
        ExpressionConst known = context.knownValue(name);
        return known == null ? this : (Expression) known;
    }
    @Override
    public void generateConditionalJump(IREmitter emit, TempVarUsage tempVars, int jumpTo, boolean invert) {
        emit.emit(new TACJumpBoolVar(context.get(name), jumpTo, invert));
    }
    @Override
    public int condLength() {
        return 1;
    }
}
