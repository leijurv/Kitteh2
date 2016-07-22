/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.command;
import compiler.Context;
import compiler.expression.Expression;
import compiler.tac.IREmitter;
import compiler.tac.TACReturn;
import compiler.tac.TempVarUsage;

/**
 *
 * @author leijurv
 */
public class CommandReturn extends Command {
    Expression toReturn;
    public CommandReturn(Context context, Expression toReturn) {
        super(context);
        this.toReturn = toReturn;
    }
    @Override
    protected void generateTAC0(IREmitter emit) {
        toReturn.generateTAC(emit, new TempVarUsage(context), "%eax");
        emit.emit(new TACReturn());
    }
    @Override
    protected int calculateTACLength() {
        return toReturn.getTACLength() + 1;
    }
    @Override
    public void staticValues() {
    }
}
