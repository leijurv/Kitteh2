/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler;
import java.util.ArrayList;

/**
 *
 * @author leijurv
 */
public class IREmitter {
    ArrayList<TACStatement> result;
    public IREmitter() {
        this.result = new ArrayList<>();
    }
    private IREmitter(IREmitter prev) {
        result = new ArrayList<>(prev.result);
    }
    public void emit(TACStatement ts) {
        result.add(ts);
    }
    public int lineNumberOfNextStatement() {
        return result.size();
    }
    public int mostRecentLineNumber() {
        return result.size() - 1;
    }
    public IREmitter genFakeEmitter() {
        return new IREmitter(this);
    }
}
