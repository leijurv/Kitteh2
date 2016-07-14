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
public class TokenNum extends Token {
    public final String val;
    public TokenNum(String name) {
        this.val = name;
    }
    @Override
    public String toString() {
        return "#" + val;
    }
}
