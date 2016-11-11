/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.expression;
import compiler.tac.IREmitter;
import compiler.tac.TACCast;
import compiler.tac.TempVarUsage;
import compiler.type.Type;

/**
 *
 * @author leijurv
 */
public class ExpressionCast extends Expression {
    private final Type castTo;
    private final Expression input;
    public ExpressionCast(Expression input, Type castTo) {
        this.input = input;
        this.castTo = castTo;
    }
    @Override
    protected Type calcType() {
        return castTo;
    }
    @Override
    public void generateTAC(IREmitter emit, TempVarUsage tempVars, String resultLocation) {
        String tmp = tempVars.getTempVar(input.getType());
        input.generateTAC(emit, tempVars, tmp);
        emit.emit(new TACCast(tmp, resultLocation));
    }
    @Override
    protected int calculateTACLength() {
        return 1 + input.getTACLength();
    }
}
