/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.command;
import compiler.Context;
import compiler.tac.IREmitter;
import compiler.tac.TempVarUsage;
import compiler.expression.Expression;

/**
 *
 * @author leijurv
 */
public class CommandSetVar extends Command {
    Expression val;
    String var;
    public CommandSetVar(String var, Expression val) {
        this.val = val;
        this.var = var;
    }
    @Override
    public void generateTAC(Context context, IREmitter emit) {
        val.calcNaiveTAC(context, emit, new TempVarUsage(), var);//this one, at least, is easy
    }
    @Override
    protected int calculateTACLength() {
        return val.calcTACLength();
    }
}
