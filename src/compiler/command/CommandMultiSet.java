/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.command;
import compiler.Context;
import compiler.expression.ExpressionFunctionCall;
import compiler.tac.IREmitter;
import compiler.tac.TempVarUsage;

/**
 *
 * @author leijurv
 */
public class CommandMultiSet extends Command {//TODO: merge with CommandExp. both CommandExp and CommandSetMulti do the exact same thing: set variable(s) to the result of a function call
    String[] varNames;
    ExpressionFunctionCall settingTo;
    public CommandMultiSet(Context context, ExpressionFunctionCall settingTo, String... varNames) {
        super(context);
        this.varNames = varNames;
        this.settingTo = settingTo;
    }
    @Override
    protected void generateTAC0(IREmitter emit) {
        settingTo.multipleReturns(emit, new TempVarUsage(context), varNames);
    }
    @Override
    protected int calculateTACLength() {
        return settingTo.calculateTACLength();
    }
    @Override
    protected void staticValues() {
        settingTo = (ExpressionFunctionCall) settingTo.insertKnownValues(context);
        settingTo = (ExpressionFunctionCall) settingTo.calculateConstants();
    }
}
