/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler;

/**
 *
 * @author leijurv
 */
public class TACConst extends TACStatement {
    String var;
    String val;
    public TACConst(String var, String val) {
        this.var = var;
        this.val = val;
    }
    @Override
    public String toString() {
        return var + " = " + val;
    }
}
