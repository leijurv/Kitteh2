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
public class CommandExp extends Command {//TODO: merge with CommandSetMulti. both CommandExp and CommandSetMulti do the exact same thing: set variable(s) to the result of a function call
    private Expression ex;
    public CommandExp(Expression ex, Context context) {
        super(context);
        this.ex = ex;
    }
    @Override
    public String toString() {
        return ex.toString();
    }
    @Override
    public void generateTAC0(IREmitter emit) {
        ex.generateTAC(emit, new TempVarUsage(context), null);
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
    public Expression getEx() {
        return ex;
    }
}
