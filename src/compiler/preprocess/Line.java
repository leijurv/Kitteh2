/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.preprocess;
import compiler.lex.Lexer;
import compiler.token.Token;
import java.nio.channels.NonReadableChannelException;
import java.nio.file.Path;
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
    private final Path loadedFrom;
    private Line(Line other, String newRaw) {
        this(newRaw, other.loadedFrom, other.origLineNumber);
    }
    Line(String raw, Path loadedFrom, int origLineNumber) {
        this.raw = raw;
        this.loadedFrom = loadedFrom;
        this.origLineNumber = origLineNumber;
        source = new ArrayList<>();
        source.add(raw);
    }
    Line withModifiedRaw(String newRaw) {
        return new Line(this, newRaw);
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

    public class LineException extends RuntimeException {
        public <T extends Throwable> LineException(T toWrap, String doing) {
            super(toWrap.getClass() + " while " + doing + lineMessage(), toWrap);
            if (doing == null) {
                throw new IllegalArgumentException();
            }
        }
        public LineException() {
            super("Exception on " + lineMessage());
        }
        public LineException(Throwable toWrap) {
            super(toWrap.getClass() + " on" + lineMessage(), toWrap);
        }
    }
    private String lineMessage() {
        return " line " + origLineNumber + " of " + loadedFrom;
    }
    public ArrayList<Object> source() {
        return source;
    }
    public boolean lexd() {
        return tokens != null;
    }
    public boolean unlexd() {
        return !lexd();
    }
    public void lex() {
        if (tokens != null) {
            throw new NonReadableChannelException();//muahahaha not even an exception message XDD
        }
        tokens = source.stream().flatMap(o -> o instanceof Token ? Stream.of((Token) o) : Lexer.lex((String) o).stream()).collect(Collectors.toList());
    }
    public List<Token> getTokens() {
        if (tokens == null) {//haha this is a funny exception message i just noticed
            throw new IllegalStateException("Play more arcade games because you don't have enough tokens");
        }
        return tokens;
    }
}
