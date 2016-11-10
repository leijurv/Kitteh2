/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac;
import compiler.Context;
import compiler.X86Emitter;

/**
 *
 * @author leijurv
 */
public abstract class TACStatement {
    Context context;
    public final void setContext(Context context) {
        this.context = context;
        onContextKnown();
    }
    protected abstract void onContextKnown();
    @Override
    public final String toString() {//all calls to toString should check the context first, and this results in a single error message for every case where the context isn't already set
        if (context == null) {
            throw new IllegalStateException("Context not set " + getClass());
        }
        return toString0();
    }
    public abstract String toString0();
    public abstract void printx86(X86Emitter emit);
}
