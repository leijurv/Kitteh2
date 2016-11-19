/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.parse;
import compiler.lex.Lexer;
import compiler.token.Token;
import java.nio.channels.NonReadableChannelException;
import java.util.ArrayList;

/**
 *
 * @author leijurv
 */
public class Line {
    private final ArrayList<Object> source;
    private ArrayList<Token> tokens;
    private final String raw;
    private final int origLineNumber;
    public Line(String raw, int origLineNumber) {
        this.raw = raw;
        this.origLineNumber = origLineNumber;
        source = new ArrayList<>();
        source.add(raw);
    }
    /*public Line(ArrayList<Object> source) {
        this.source = source;
        this.tokens = null;
    }*/
    @Override
    public String toString() {
        if (tokens == null) {
            if (source == null) {
                return origLineNumber + ": " + raw;
            } else {
                return origLineNumber + ": " + source;
            }
        } else {
            return origLineNumber + ": " + tokens;
        }
    }
    public String raw() {
        return raw;
    }
    public int num() {
        return origLineNumber;
    }
    public ArrayList<Object> source() {
        return source;
    }
    public void lex() {//todo we could use parallel streams here in the future to make it multithreaded
        if (tokens != null) {
            throw new NonReadableChannelException();//muahahaha not even an exception message XDD
        }
        tokens = new ArrayList<>(source.size());//this makes it like 1ms faster
        for (Object o : source) {//we could do a parallel stream here in the future to make it speedier
            if (o instanceof Token) {
                tokens.add((Token) o);
            } else if (o instanceof String) {
                tokens.addAll(Lexer.lex((String) o));
            } else {
                throw new IllegalStateException(o + " is neither a String nor a Token");
            }
        }
    }
    public ArrayList<Token> getTokens() {
        if (tokens == null) {
            throw new IllegalStateException("Play more arcade games because you don't have enough tokens");
        }
        return tokens;
    }
}
