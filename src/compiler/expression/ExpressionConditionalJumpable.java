/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.expression;
import compiler.Context;
import compiler.tac.IREmitter;
import compiler.tac.TempVarUsage;

/**
 *
 * @author leijurv
 */
public abstract class ExpressionConditionalJumpable extends Expression {
    public abstract void generateConditionJump(Context context, IREmitter emit, TempVarUsage tempVars, int jumpTo, boolean invert);
    public abstract int condLength();
}
