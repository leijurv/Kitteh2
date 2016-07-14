/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler;

/**
 *
 * @author leijurv
 */
public class ExpressionOperator extends Expression {
    Operator op;
    Expression a;
    Expression b;
    public ExpressionOperator(Expression a, Operator op, Expression b) {
        this.a = a;
        this.b = b;
        this.op = op;
    }
    @Override
    public Type calcType() {
        Type A = a.getType();
        Type B = b.getType();
        Type result = op.onApplication(A, B);
        System.out.println("Getting type of " + A + " " + op + " " + B + ": " + result);
        return result;
    }
    @Override
    public String toString() {
        return "(" + a + ")" + op + "(" + b + ")";
    }
    public void calcNaiveTAC(Context context, IREmitter emit, TempVarUsage tempVars, String resultLocation) {
        String aName = tempVars.getTempVar();
        a.calcNaiveTAC(context, emit, tempVars, aName);
        String bName = tempVars.getTempVar();
        b.calcNaiveTAC(context, emit, tempVars, bName);
        emit.emit(new TACStandard(resultLocation, aName, bName, op));
        //TODO if we allow ++ and -- that could mess up the DAG
        //TODO || and &&
    }
    @Override
    public int calcTACLength() {
        return a.calcTACLength() + b.calcTACLength() + 1;
    }
    public int condLength() {
        if (op == Operator.AND || op == Operator.OR) {
            return ((ExpressionOperator) a).condLength() + ((ExpressionOperator) b).condLength();
        }
        return calcTACLength();
    }
    /**
     *
     * @param context
     * @param emit
     * @param tempVars
     * @param jumpTo
     * @param invert If this is true, jump if false. Otherwise, it's not
     * inverted, so jump if true
     */
    public void generateConditionJump(Context context, IREmitter emit, TempVarUsage tempVars, int jumpTo, boolean invert) {
        if (op == Operator.AND) {
            if (invert) {
                //inverted. skip down if known to be false
                ((ExpressionOperator) a).generateConditionJump(context, emit, tempVars, jumpTo, true);//invert so it skips if false, and since this is AND, if one is false the result is false
                ((ExpressionOperator) b).generateConditionJump(context, emit, tempVars, jumpTo, true);//if b is false either, skip to the same place
            } else {
                //not inverted. skip down if known to be true
                //if the first one is FALSE, we know that the result must be false
                //if we know the result is false, and we want to jump if true, we can skip b and go directly to non-jump
                //which is going to right after B
                int aLen = ((ExpressionOperator) a).condLength();
                int bLen = ((ExpressionOperator) b).condLength();
                int afterB = emit.lineNumberOfNextStatement() + aLen + bLen;
                ((ExpressionOperator) a).generateConditionJump(context, emit, tempVars, afterB, true);//if a is false, we jump to after B. invert so the jump is if A is false
                //for B, if B is true, then the result is true, so we can jump
                //if B is false, then the result is false, so we dont jump
                //B decides it now
                ((ExpressionOperator) b).generateConditionJump(context, emit, tempVars, jumpTo, false);//dont invert
            }
        } else if (op == Operator.OR) {
            if (invert) {
                //inverted. skip down if known to be false
                //if the first one is TRUE, we know the result bust be true
                //if we know the result is true, and we want to jump if false, we can skip b and go directly to non-jump
                //which is going to right after B
                int aLen = ((ExpressionOperator) a).condLength();
                int bLen = ((ExpressionOperator) b).condLength();
                int afterB = emit.lineNumberOfNextStatement() + aLen + bLen;
                ((ExpressionOperator) a).generateConditionJump(context, emit, tempVars, afterB, false);//if a is true, we jump to after B
                //for B, if B is false, then the result is false, so we can jump
                //if B is true, then the result is true, so we dont jump
                //B decides it now
                ((ExpressionOperator) b).generateConditionJump(context, emit, tempVars, jumpTo, true);//invert because if B is true then we don't jump
            } else {
                //not inverted. skip down if known to be true
                ((ExpressionOperator) a).generateConditionJump(context, emit, tempVars, jumpTo, false);//if a is true, then the result is known to be true, so we do the jump
                ((ExpressionOperator) b).generateConditionJump(context, emit, tempVars, jumpTo, false);//if b is true, same thing
            }
        } else {
            String aName = tempVars.getTempVar();
            a.calcNaiveTAC(context, emit, tempVars, aName);
            String bName = tempVars.getTempVar();
            b.calcNaiveTAC(context, emit, tempVars, bName);
            emit.emit(new TACJump(aName + op + bName, jumpTo, invert));
        }
    }
}
