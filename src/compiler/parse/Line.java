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
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author leijurv
 */
public class Line {
    private final ArrayList<Object> source;
    private List<Token> tokens;
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
    public boolean unlext() {
        return tokens == null;
    }
    public void lex() {
        if (tokens != null) {
            throw new NonReadableChannelException();//muahahaha not even an exception message XDD
        }
        tokens = source.stream().flatMap(o -> o instanceof Token ? Stream.of((Token) o) : Lexer.lex((String) o).stream()).collect(Collectors.toList());
    }
    public List<Token> getTokens() {
        if (tokens == null) {
            throw new IllegalStateException("Play more arcade games because you don't have enough tokens");
        }
        return tokens;
    }
}
