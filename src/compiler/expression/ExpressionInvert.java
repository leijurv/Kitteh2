/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.expression;
import compiler.Context;
import compiler.tac.IREmitter;
import compiler.tac.TempVarUsage;
import compiler.type.Type;
import compiler.type.TypeBoolean;

/**
 *
 * @author leijurv
 */
public class ExpressionInvert extends ExpressionConditionalJumpable {
    private final ExpressionConditionalJumpable inp;
    public ExpressionInvert(ExpressionConditionalJumpable inp) {
        this.inp = inp;
        if (!(inp.getType() instanceof TypeBoolean)) {
            throw new RuntimeException("Can only invert a boolean");
        }
    }
    @Override
    protected Type calcType() {
        return new TypeBoolean();
    }
    @Override
    public void generateTAC(IREmitter emit, TempVarUsage tempVars, String resultLocation) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    @Override
    protected int calculateTACLength() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    @Override
    public void generateConditionalJump(IREmitter emit, TempVarUsage tempVars, int jumpTo, boolean invert) {
        inp.generateConditionalJump(emit, tempVars, jumpTo, !invert);
    }
    @Override
    public int condLength() {
        return inp.condLength();
    }
    @Override
    public Expression insertKnownValues(Context context) {
        return new ExpressionInvert((ExpressionConditionalJumpable) inp.insertKnownValues(context));
    }
    @Override
    public Expression calculateConstants() {
        if (inp instanceof ExpressionInvert) {//!(!(x)) gets converted to x
            return ((ExpressionInvert) inp).inp;
        }
        if (inp instanceof ExpressionOperator) {//!(a<b) gets converted to a>=b
            ExpressionOperator eo = (ExpressionOperator) inp;
            return new ExpressionOperator(eo.getA(), eo.getOP().invert(), eo.getB());
        }
        return new ExpressionInvert((ExpressionConditionalJumpable) inp.calculateConstants());
    }
}
