/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.command;
import compiler.Context;
import compiler.expression.Expression;
import compiler.tac.IREmitter;
import compiler.tac.TempVarUsage;

/**
 * evaluate expression
 *
 * @author leijurv
 */
public class CommandExp extends Command {
    Expression ex;
    public CommandExp(Expression ex, Context context) {
        super(context);
        this.ex = ex;
    }
    @Override
    public String toString() {
        return ex.toString();
    }
    @Override
    public void generateTAC(Context context, IREmitter emit) {
        ex.generateTAC(context, emit, new TempVarUsage(), null);
    }
    @Override
    protected int calculateTACLength() {
        return ex.getTACLength();
    }
    @Override
    public void staticValues() {
        ex = ex.insertKnownValues(context);
        ex = ex.calculateConstants();
    }
}
