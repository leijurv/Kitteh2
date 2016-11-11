/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.token;
import compiler.tac.optimize.UselessTempVars;

/**
 *
 * @author leijurv
 */
public class TokenVariable extends Token {
    public final String val;
    public TokenVariable(String name) {
        this.val = name;
        if (UselessTempVars.isTempVariable(name)) {
            int x = 5 / 0;//don't try and trick the compiler by making a variable name start with tmp
        }
    }
    @Override
    public String toString() {
        return "$" + val;
    }
}
