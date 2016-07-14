/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.token;

/**
 *
 * @author leijurv
 */
public class TokenVariable extends Token {
    public final String val;
    public TokenVariable(String name) {
        this.val = name;
    }
    @Override
    public String toString() {
        return "$" + val;
    }
}
