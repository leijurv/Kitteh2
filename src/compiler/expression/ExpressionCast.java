/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.expression;
import compiler.Context;
import compiler.Context.VarInfo;
import compiler.tac.IREmitter;
import compiler.tac.TACCast;
import compiler.tac.TempVarUsage;
import compiler.type.Type;
import compiler.type.TypeFloat;
import compiler.type.TypeNumerical;

/**
 *
 * @author leijurv
 */
public class ExpressionCast extends Expression {
    private final Type castTo;
    public Expression input;
    public ExpressionCast(Expression input, Type castTo) {
        this.input = input;
        this.castTo = castTo;
    }
    @Override
    protected Type calcType() {
        return castTo;
    }
    @Override
    public void generateTAC(IREmitter emit, TempVarUsage tempVars, VarInfo resultLocation) {
        VarInfo tmp = tempVars.getTempVar(input.getType());
        input.generateTAC(emit, tempVars, tmp);
        emit.emit(new TACCast(tmp, resultLocation));
    }
    @Override
    protected int calculateTACLength() {
        return 1 + input.getTACLength();
    }
    @Override
    public Expression calculateConstants() {
        input = input.calculateConstants();
        if (input.getType().equals(castTo)) {
            return input;
        }
        if (input instanceof ExpressionConst && !(castTo instanceof TypeFloat) && !(input.getType() instanceof TypeFloat)) {
            if (!(input instanceof ExpressionConstNum)) {
                throw new IllegalStateException("Casting a const bool??");
            }
            return new ExpressionConstNum(((ExpressionConstNum) input).getVal(), (TypeNumerical) castTo);
        }
        return this;
    }
    @Override
    public Expression insertKnownValues(Context context) {
        input = input.insertKnownValues(context);
        return this;
    }
}
