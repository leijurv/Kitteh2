/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.command;
import compiler.Context;
import compiler.tac.IREmitter;
import compiler.tac.TACJump;

/**
 *
 * @author leijurv
 */
public class CommandBreak extends Command {
    public CommandBreak(Context context) {
        super(context);
    }
    @Override
    protected void generateTAC0(IREmitter emit) {
        emit.emit(new TACJump(emit.breakTo()));
    }
    @Override
    protected int calculateTACLength() {
        return 1;
    }
    @Override
    public void staticValues() {
    }
}
