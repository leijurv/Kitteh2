/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.expression;
import compiler.Context;
import compiler.tac.IREmitter;
import compiler.tac.TACConst;
import compiler.tac.TempVarUsage;
import compiler.type.Type;

/**
 *
 * @author leijurv
 */
public class ExpressionVariable extends Expression {
    String name;
    Type type;
    public ExpressionVariable(String name, Context context) {
        this.name = name;
        this.type = context.getType(name);
        if (type == null) {
            throw new IllegalStateException("pls " + name);
        }
    }
    @Override
    public Type calcType() {
        return type;
    }
    public String toString() {
        return name;
    }
    @Override
    public void calcNaiveTAC(Context context, IREmitter emit, TempVarUsage tempVars, String resultLocation) {
        emit.emit(new TACConst(resultLocation, name));
    }
    @Override
    public int calcTACLength() {
        return 1;
    }
}
