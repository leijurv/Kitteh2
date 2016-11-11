/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.lex;

/**
 *
 * @author leijurv
 */
public class StringEmitter {
    private final String str;
    private int pos;
    public StringEmitter(String str) {
        this.str = str;
        this.pos = 0;
    }
    public char peek() {
        if (pos >= str.length()) {
            throw new IllegalStateException("Unexpected end of line");
        }
        return str.charAt(pos);
    }
    public char pop() {
        char c = peek();
        pos++;
        return c;
    }
    public boolean has() {
        return pos < str.length();
    }
    public int pos() {
        return pos;
    }
    public String substring(int start) {
        return str.substring(start, pos);//inclusive on right
    }
}
