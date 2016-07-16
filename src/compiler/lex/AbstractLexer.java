/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.lex;
import compiler.token.Token;
import java.util.ArrayList;

/**
 * Helper superclass for the lexer to provide peek, pop, and emit in an ez way
 *
 * @author leijurv
 */
public abstract class AbstractLexer {//extends StringEmitter? I just provide wrappers for all its funcs anyway... lol
    private final StringEmitter lineReader;
    private final ArrayList<Token> temp;
    protected AbstractLexer(String line) {
        this.lineReader = new StringEmitter(line);
        this.temp = new ArrayList<>();
    }
    protected char peek() {
        return lineReader.peek();
    }
    protected char pop() {
        return lineReader.pop();
    }
    protected boolean has() {
        return lineReader.has();
    }
    protected String substring(int start) {
        return lineReader.substring(start);
    }
    public int pos() {
        return lineReader.currentPos();
    }
    protected void emit(Token t) {
        temp.add(t);
    }
    protected abstract void runLex();
    public ArrayList<Token> lex() {//eh this can be public
        if (!temp.isEmpty()) {
            throw new IllegalStateException("babe you are reusing a lexer what are you even doing right now XDDDD haha");
        }
        runLex();
        return temp;
    }
}
