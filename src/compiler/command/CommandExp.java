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
 * evaluate expression
 *
 * @author leijurv
 */
public class CommandExp extends Command {
    Expression ex;
    public CommandExp(Expression ex) {
        this.ex = ex;
    }
    public String toString() {
        return ex.toString();
    }
    @Override
    public void generateTAC(Context context, IREmitter emit) {
        ex.generateTAC(context, emit, new TempVarUsage(), null);
    }
    @Override
    protected int calculateTACLength() {
        return ex.calculateTACLength();
    }
}
