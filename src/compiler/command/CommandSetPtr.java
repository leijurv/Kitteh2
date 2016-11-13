/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.command;
import compiler.Context;
import compiler.expression.Expression;
import compiler.tac.IREmitter;
import compiler.tac.TACPointerRef;
import compiler.tac.TempVarUsage;

/**
 *
 * @author leijurv
 */
public class CommandSetPtr extends Command {
    private Expression pointer;
    private Expression value;
    public CommandSetPtr(Context context, Expression pointer, Expression value) {
        super(context);
        this.pointer = pointer;
        this.value = value;
    }
    @Override
    protected void generateTAC0(IREmitter emit) {
        TempVarUsage tvu = new TempVarUsage(context);
        String ptr = tvu.getTempVar(pointer.getType());
        pointer.generateTAC(emit, tvu, ptr);
        String val = tvu.getTempVar(value.getType());
        value.generateTAC(emit, tvu, val);
        emit.emit(new TACPointerRef(val, ptr));
    }
    @Override
    protected int calculateTACLength() {
        return pointer.getTACLength() + value.getTACLength() + 1;
    }
    @Override
    public void staticValues() {
        pointer = pointer.insertKnownValues(context);
        pointer = pointer.calculateConstants();
        value = value.insertKnownValues(context);
        value = value.calculateConstants();
    }
}
