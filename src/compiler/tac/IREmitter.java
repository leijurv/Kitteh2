/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac;
import compiler.Context;
import java.util.ArrayList;

/**
 * A growing list of TAC statements, with extra information like the
 * currentContext, and the destination of break and continue statements
 *
 * @author leijurv
 */
public class IREmitter {//extends ArrayList XDDD
    private final ArrayList<TACStatement> result;
    private Context currentContext;
    private Integer breakTo;
    private Integer continueTo;
    public IREmitter() {
        this.result = new ArrayList<>();
        this.currentContext = null;
        this.breakTo = null;
        this.continueTo = null;
    }
    public boolean canBreak() {
        return breakTo != null;
    }
    public boolean canContinue() {
        return continueTo != null;
    }
    public int breakTo() {//Don't change this to return an Integer. I want the nullpointerexception if canBreak / canContinue aren't checked first.
        return breakTo;
    }
    public int continueTo() {
        return continueTo;
    }
    public void setBreak(int i) {
        breakTo = i;
    }
    public void setContinue(int i) {
        continueTo = i;
    }
    public void clearBreakContinue() {
        breakTo = null;
        continueTo = null;
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
        if (!(result.get(result.size() - 1) instanceof TACReturn)) {
            throw new RuntimeException("return should have been added in command define function");
        }
        return result;
    }
}
