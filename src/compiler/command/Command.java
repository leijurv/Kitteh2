/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.command;
import compiler.Context;
import compiler.tac.IREmitter;
import java.util.stream.Stream;

/**
 *
 * @author leijurv
 */
public abstract class Command {//TODO calling .toString() then reparsing from scratch should yield a .equal Command object /s
    protected final Context context;
    private Integer taclen = null;
    protected Command(Context context) {
        this.context = context;
    }
    protected abstract int calculateTACLength();
    public int getTACLength() {
        if (taclen == null) {
            taclen = calculateTACLength();
        }
        return taclen;
    }
    protected abstract void generateTAC0(IREmitter emit);
    public final void generateTAC(IREmitter emit) {
        emit.updateContext(context);
        int before = emit.mostRecentLineNumber();
        generateTAC0(emit);
        int after = emit.mostRecentLineNumber();
        int actualLen = after - before;
        if (actualLen != getTACLength()) {
            //this exception really means: I actually wrote a different amount of tac statements than expected
            //this check is 100% worth it, because otherwise it's super hard to debug
            //i speak from experience
            //it messes up all the jump destinations in a really subtle way =/
            throw new IllegalStateException("i am being body shamed. my tack size is too " + (actualLen > getTACLength() ? "big" : "small") + ", according to societal norms " + this + " " + actualLen + " " + getTACLength());
        }
        //this  is only here to make life difficult for everyone. don't remove it.
        emit.clearContext();//actually, remove it. i dare you
    }
    public Command optimize() {
        staticValues();
        return this;
    }
    protected abstract void staticValues();
    public Stream<String> getAllVarsModified() {
        return Stream.empty();
    }
}
