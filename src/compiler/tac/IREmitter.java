/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac;
import java.util.ArrayList;

/**
 *
 * @author leijurv
 */
public class IREmitter {//extends ArrayList XDDD
    ArrayList<TACStatement> result;
    public IREmitter() {
        this.result = new ArrayList<>();
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
    public ArrayList<TACStatement> getResult() {
        return result;
    }
}
