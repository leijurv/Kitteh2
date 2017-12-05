/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.expression;
import compiler.Context.VarInfo;
import compiler.tac.IREmitter;
import compiler.tac.TACConst;
import compiler.tac.TACJump;
import compiler.tac.TempVarUsage;
import compiler.type.Type;
import compiler.type.TypeBoolean;
import compiler.x86.X86Const;

/**
 *
 * @author leijurv
 */
public class ExpressionConstBool extends ExpressionConditionalJumpable implements ExpressionConst<Boolean> {
    private final boolean bool;
    public ExpressionConstBool(boolean bool) {
        this.bool = bool;
    }
    @Override
    public Boolean getVal() {
        return bool;
    }
    @Override
    public void generateConditionalJump(IREmitter emit, TempVarUsage tempVars, int jumpTo, boolean invert) {
        if (!invert ^ bool) {//god damn I love xor
            emit.emit(new TACJump(emit.lineNumberOfNextStatement() + 1));//I love hacks like this =D
            //note from the future: yes i added an optimization to remove this useless instruction
            //static length means this can't generate 1 statement sometimes, and 0 statements other times
            //so there needs to be a noop here, and yeah
        } else {
            emit.emit(new TACJump(jumpTo));//ez
        }
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
    public void generateTAC(IREmitter emit, TempVarUsage tempVars, VarInfo resultLocation) {
        emit.emit(new TACConst(resultLocation, new X86Const(bool ? "1" : "0", new TypeBoolean())));
    }
    @Override
    protected int calculateTACLength() {
        return 1;
    }
}
