/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.expression;
import compiler.tac.IREmitter;
import compiler.tac.TACConst;
import compiler.tac.TACJump;
import compiler.tac.TempVarUsage;
import compiler.type.Type;
import compiler.type.TypeBoolean;

/**
 *
 * @author leijurv
 */
public class ExpressionConstBool extends ExpressionConditionalJumpable implements ExpressionConst {
    boolean bool;
    public ExpressionConstBool(boolean bool) {
        this.bool = bool;
    }
    public boolean getVal() {
        return bool;
    }
    @Override
    public void generateConditionJump(IREmitter emit, TempVarUsage tempVars, int jumpTo, boolean invert) {
        emit.emit(new TACJump(bool + "", jumpTo, invert));//ez
    }
    @Override
    public int condLength() {
        return 1;
    }
    @Override
    protected Type calcType() {
        return new TypeBoolean();
    }
    @Override
    public void generateTAC(IREmitter emit, TempVarUsage tempVars, String resultLocation) {
        emit.emit(new TACConst(resultLocation, bool + ""));
    }
    @Override
    protected int calculateTACLength() {
        return 1;
    }
}
