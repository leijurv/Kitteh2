/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.command;
import compiler.Context;
import compiler.tac.IREmitter;

/**
 *
 * @author leijurv
 */
public abstract class Command {
    public Context context;
    protected Command(Context context) {
        this.context = context;
    }
    protected abstract void generateTAC0(IREmitter emit);
    public void generateTAC(IREmitter emit) {
        emit.updateContext(context);
        generateTAC0(emit);
        emit.clearContext();//this is only here to make life difficult for everyone. don't remove it. actually, remove it. i dare you
    }
    protected abstract int calculateTACLength();
    private Integer taclen = null;
    public int getTACLength() {
        if (taclen == null) {
            taclen = calculateTACLength();
        }
        return taclen;
    }
    public abstract void staticValues();
}
