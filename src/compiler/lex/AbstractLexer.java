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
public abstract class AbstractLexer extends StringEmitter {
    private final ArrayList<Token> temp;
    protected AbstractLexer(String line) {
        super(line);
    }
    protected void emit(Token t) {
        temp.add(t);
    }
    protected abstract void runLex();
    {
        temp = new ArrayList<>();
    }
    public ArrayList<Token> lex() {//eh this can be public
        if (!temp.isEmpty()) {
            throw new IllegalStateException("babe you are reusing a lexer what are you even doing right now XDDDD haha");
        }
        runLex();
        return temp;
    }
}
