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
import compiler.type.TypeString;

/**
 *
 * @author leijurv
 */
public class ExpressionConstStr extends Expression {
    public final String val;
    public ExpressionConstStr(String val) {
        this.val = val;
    }
    @Override
    protected Type calcType() {
        return new TypeString();
    }
    @Override
    public void generateTAC(Context context, IREmitter emit, TempVarUsage tempVars, String resultLocation) {
        emit.emit(new TACConst(resultLocation, '"' + val + '"'));
    }
    @Override
    protected int calculateTACLength() {
        return 1;
    }
}
