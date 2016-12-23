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
class StringEmitter {
    private final String str;
    private int pos;
    public StringEmitter(String str) {
        this.str = str;
        this.pos = 0;
    }
    public final String peek2() {//TODO this is bad
        return str.charAt(pos) + "" + str.charAt(pos + 1);
    }
    public final char peek() {
        if (pos >= str.length()) {
            throw new IllegalStateException("Unexpected end of line");
        }
        return str.charAt(pos);
    }
    public final char pop() {
        char c = peek();
        pos++;
        return c;
    }
    public final boolean has2() {//TODO this is bad
        return pos + 1 < str.length();
    }
    public final boolean has() {
        return pos < str.length();
    }
    public final int pos() {
        return pos;
    }
    public final String substringSince(int start) {
        return str.substring(start, pos);//inclusive on right
    }
}
