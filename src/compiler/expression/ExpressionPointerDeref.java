/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.expression;
import compiler.Context;
import compiler.tac.IREmitter;
import compiler.tac.TACPointerDeref;
import compiler.tac.TempVarUsage;
import compiler.type.Type;
import compiler.type.TypePointer;

/**
 *
 * @author leijurv
 */
public class ExpressionPointerDeref extends Expression {
    private final Expression deReferencing;
    public ExpressionPointerDeref(Expression deref) {
        this.deReferencing = deref;
    }
    @Override
    protected Type calcType() {
        TypePointer tp = (TypePointer) deReferencing.getType();
        return tp.pointingTo();
    }
    @Override
    public void generateTAC(IREmitter emit, TempVarUsage tempVars, String resultLocation) {
        String tmp = tempVars.getTempVar(deReferencing.getType());
        deReferencing.generateTAC(emit, tempVars, tmp);
        emit.emit(new TACPointerDeref(tmp, resultLocation));
    }
    @Override
    protected int calculateTACLength() {
        return 1 + deReferencing.getTACLength();
    }
    @Override
    public Expression calculateConstants() {
        return new ExpressionPointerDeref(deReferencing.calculateConstants());
    }
    @Override
    public Expression insertKnownValues(Context context) {
        return new ExpressionPointerDeref(deReferencing.insertKnownValues(context));
    }
}
