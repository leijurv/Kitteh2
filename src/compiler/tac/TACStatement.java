/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac;
import compiler.Context;

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
    public final String toString() {
        if (context == null) {
            throw new IllegalStateException("My little pony");
        }
        return toString0();
    }
    public abstract String toString0();
}
