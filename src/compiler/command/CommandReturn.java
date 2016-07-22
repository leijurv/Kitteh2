/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.command;
import compiler.Context;
import compiler.expression.Expression;
import compiler.tac.IREmitter;
import compiler.tac.TACConst;
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
        TempVarUsage lol = new TempVarUsage(context);
        String var = lol.getTempVar(toReturn.getType());
        toReturn.generateTAC(emit, lol, var);
        emit.emit(new TACConst("%eax", var));
        emit.emit(new TACReturn());
    }
    @Override
    protected int calculateTACLength() {
        return toReturn.getTACLength() + 2;
    }
    @Override
    public void staticValues() {
    }
}
