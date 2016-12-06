/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.expression;
import compiler.Context;
import compiler.Operator;
import compiler.tac.IREmitter;
import compiler.tac.TACConst;
import compiler.tac.TACJump;
import compiler.tac.TACJumpCmp;
import compiler.tac.TACStandard;
import compiler.tac.TempVarUsage;
import compiler.type.Type;
import compiler.type.TypeBoolean;

/**
 *
 * @author leijurv
 */
public class ExpressionOperator extends ExpressionConditionalJumpable {
    private final Operator op;
    private Expression a;
    private Expression b;
    public ExpressionOperator(Expression a, Operator op, Expression b) {
        this.a = a;
        this.b = b;
        this.op = op;
        if (op.onApplication(a.getType(), b.getType()) instanceof TypeBoolean) {
            if (!a.getType().equals(b.getType())) {
                throw new RuntimeException("Type mismatch " + a.getType() + " " + b.getType() + " from expressions " + a + " " + b);
            }
        }
    }
    @Override
    public Type calcType() {
        Type A = a.getType();
        Type B = b.getType();
        Type result = op.onApplication(A, B);
        //System.out.println("Getting type of " + A + " " + op + " " + B + ": " + result);
        return result;
    }
    Operator getOP() {
        return op;
    }
    Expression getA() {
        return a;
    }
    Expression getB() {
        return b;
    }
    @Override
    public String toString() {
        return "(" + a + ")" + op + "(" + b + ")";
    }
    @Override
    public void generateTAC(IREmitter emit, TempVarUsage tempVars, String resultLocation) {
        if (op == Operator.AND || op == Operator.OR) {
            int ifTrue = emit.lineNumberOfNextStatement() + condLength() + 2;
            int ifFalse = ifTrue + 1;
            generateConditionalJump(emit, tempVars, ifTrue, false);
            emit.emit(new TACConst(resultLocation, "0"));
            emit.emit(new TACJump(ifFalse));
            emit.emit(new TACConst(resultLocation, "1"));
            return;
        }
        String aName = tempVars.getTempVar(a.getType());
        a.generateTAC(emit, tempVars, aName);
        String bName = tempVars.getTempVar(b.getType());
        b.generateTAC(emit, tempVars, bName);
        emit.emit(new TACStandard(resultLocation, aName, bName, op));
    }
    @Override
    public int calculateTACLength() {
        if (op == Operator.AND || op == Operator.OR) {
            return condLength() + 3;
        }
        return condLength();
    }
    @Override
    public int condLength() {
        if (op == Operator.AND || op == Operator.OR) {
            return ((ExpressionConditionalJumpable) a).condLength() + ((ExpressionConditionalJumpable) b).condLength();
        }
        return a.getTACLength() + b.getTACLength() + 1;
    }
    /**
     * honestly i don't even know
     *
     * @param emit
     * @param tempVars
     * @param jumpTo
     * @param invert If this is true, jump if false. Otherwise, it's not
     * inverted, so jump if true
     */
    @Override
    public void generateConditionalJump(IREmitter emit, TempVarUsage tempVars, int jumpTo, boolean invert) {
        if (op == Operator.AND) {
            if (invert) {
                //inverted. skip down if known to be false
                ((ExpressionConditionalJumpable) a).generateConditionalJump(emit, tempVars, jumpTo, true);//invert so it skips if false, and since this is AND, if one is false the result is false
                ((ExpressionConditionalJumpable) b).generateConditionalJump(emit, tempVars, jumpTo, true);//if b is false either, skip to the same place
            } else {
                //not inverted. skip down if known to be true
                //if the first one is FALSE, we know that the result must be false
                //if we know the result is false, and we want to jump if true, we can skip b and go directly to non-jump
                //which is going to right after B
                int aLen = ((ExpressionConditionalJumpable) a).condLength();
                int bLen = ((ExpressionConditionalJumpable) b).condLength();
                int afterB = emit.lineNumberOfNextStatement() + aLen + bLen;
                ((ExpressionConditionalJumpable) a).generateConditionalJump(emit, tempVars, afterB, true);//if a is false, we jump to after B. invert so the jump is if A is false
                //for B, if B is true, then the result is true, so we can jump
                //if B is false, then the result is false, so we dont jump
                //B decides it now
                ((ExpressionConditionalJumpable) b).generateConditionalJump(emit, tempVars, jumpTo, false);//dont invert
            }
        } else if (op == Operator.OR) {
            if (invert) {
                //inverted. skip down if known to be false
                //if the first one is TRUE, we know the result bust be true
                //if we know the result is true, and we want to jump if false, we can skip b and go directly to non-jump
                //which is going to right after B
                int aLen = ((ExpressionConditionalJumpable) a).condLength();
                int bLen = ((ExpressionConditionalJumpable) b).condLength();
                int afterB = emit.lineNumberOfNextStatement() + aLen + bLen;
                ((ExpressionConditionalJumpable) a).generateConditionalJump(emit, tempVars, afterB, false);//if a is true, we jump to after B
                //for B, if B is false, then the result is false, so we can jump
                //if B is true, then the result is true, so we dont jump
                //B decides it now
                ((ExpressionConditionalJumpable) b).generateConditionalJump(emit, tempVars, jumpTo, true);//invert because if B is true then we don't jump
            } else {
                //not inverted. skip down if known to be true
                ((ExpressionConditionalJumpable) a).generateConditionalJump(emit, tempVars, jumpTo, false);//if a is true, then the result is known to be true, so we do the jump
                ((ExpressionConditionalJumpable) b).generateConditionalJump(emit, tempVars, jumpTo, false);//if b is true, same thing
            }
        } else {
            String aName = tempVars.getTempVar(a.getType());
            a.generateTAC(emit, tempVars, aName);
            String bName = tempVars.getTempVar(b.getType());
            b.generateTAC(emit, tempVars, bName);
            Operator operator = invert ? op.invert() : op;
            emit.emit(new TACJumpCmp(aName, bName, operator, jumpTo));
        }
    }
    @Override
    public Expression insertKnownValues(Context context) {
        a = a.insertKnownValues(context);//TODO multithread this with a and b /s
        b = b.insertKnownValues(context);
        return this;
    }
    @Override
    public Expression calculateConstants() {
        a = a.calculateConstants();
        b = b.calculateConstants();
        if (a instanceof ExpressionConst && b instanceof ExpressionConst) {
            return (Expression) op.apply((ExpressionConst) a, (ExpressionConst) b);
        }
        return this;
    }
}
