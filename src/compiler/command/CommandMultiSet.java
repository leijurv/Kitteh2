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
import compiler.x86.X86Param;

/**
 *
 * @author leijurv
 */
public class CommandMultiSet extends Command {
    String[] varNames;
    ExpressionFunctionCall settingTo;
    public CommandMultiSet(Context context, ExpressionFunctionCall settingTo, String... varNames) {
        super(context);
        this.varNames = varNames;
        this.settingTo = settingTo;
    }
    @Override
    protected void generateTAC0(IREmitter emit) {
        X86Param[] whew = new X86Param[varNames.length];
        for (int i = 0; i < whew.length; i++) {
            whew[i] = context.get(varNames[i]);
        }
        settingTo.multipleReturns(emit, new TempVarUsage(context), whew);
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
