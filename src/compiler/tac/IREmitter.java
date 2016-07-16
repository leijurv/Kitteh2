/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac;
import compiler.Context;
import java.util.ArrayList;

/**
 *
 * @author leijurv
 */
public class IREmitter {//extends ArrayList XDDD
    private final ArrayList<TACStatement> result;
    private Context currentContext;
    public IREmitter() {
        this.result = new ArrayList<>();
        this.currentContext = null;
    }
    public void updateContext(Context context) {
        this.currentContext = context;
    }
    public void clearContext() {
        this.currentContext = null;
    }
    public void emit(TACStatement ts) {
        if (currentContext == null) {
            throw new IllegalStateException("The FitnessGram pacer test is a multistage aerobic capacity test");
        }
        ts.setContext(currentContext);
        result.add(ts);
    }
    public int lineNumberOfNextStatement() {
        return result.size();
    }
    public int mostRecentLineNumber() {
        return result.size() - 1;
    }
    public ArrayList<TACStatement> getResult() {
        if (currentContext != null) {
            throw new IllegalStateException("YOU CAN NEVER ESCAPE THE FITNESSGRAM");
        }
        return result;
    }
}
