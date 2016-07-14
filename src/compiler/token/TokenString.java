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
public class TokenString extends Token {//extends Expression, once I make an Expression class
    String val;
    public TokenString(String val) {
        this.val = val;
    }
    @Override
    public String toString() {
        return '"' + val + '"';
    }
}
