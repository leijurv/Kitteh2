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
public class TokenChar extends Token {
    public final char val;
    public TokenChar(char val) {
        this.val = val;
    }
    @Override
    public String toString() {
        return "'" + val + "'";
    }
}
