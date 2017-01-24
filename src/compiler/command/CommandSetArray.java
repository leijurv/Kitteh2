/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.command;
import compiler.Context;
import compiler.expression.Expression;
import compiler.tac.IREmitter;
import compiler.tac.TACArrayRef;
import compiler.tac.TempVarUsage;

/**
 *
 * @author leijurv
 */
public class CommandSetArray extends Command {
    private Expression array;
    private Expression index;
    private Expression value;
    public CommandSetArray(Expression array, Expression index, Expression value, Context context) {
        super(context);
        this.value = value;
        this.array = array;
        this.index = index;
    }
    @Override
    protected void generateTAC0(IREmitter emit) {
        TempVarUsage tempVars = new TempVarUsage(context);
        String arr = tempVars.getTempVar(array.getType());
        array.generateTAC(emit, tempVars, arr);
        String ind = tempVars.getTempVar(index.getType());
        index.generateTAC(emit, tempVars, ind);
        String source = tempVars.getTempVar(value.getType());
        value.generateTAC(emit, tempVars, source);
        emit.emit(new TACArrayRef(arr, ind, source));
    }
    @Override
    protected int calculateTACLength() {
        return 1 + index.getTACLength() + array.getTACLength() + value.getTACLength();
    }
    @Override
    protected void staticValues() {
        array = array.insertKnownValues(context);
        array = array.calculateConstants();
        value = value.insertKnownValues(context);
        value = value.calculateConstants();
        index = index.insertKnownValues(context);
        index = index.calculateConstants();
    }
}
