package compiler.token;
import compiler.Keyword;
import compiler.Operator;
import compiler.tac.TempVarUsage;
import compiler.tac.optimize.UselessTempVars;
import java.util.function.Function;
import java.util.function.Predicate;
import org.w3c.dom.DOMException;

public enum TokenType implements Token<Void>, Predicate<Token> {
    CHAR(Character.class, arg -> "'" + arg + "'"),
    COMMA(","),
    DECREMENT("--"),
    ENDBRKT("]"),
    ENDPAREN(")"),
    INCREMENT("++"),
    KEYWORD(Keyword.class, arg -> "KEYWORD" + arg),
    NOT("!"),
    NUM(String.class, x -> "#" + x),
    OPERATOR(Operator.class, Operator::toString),
    PERIOD("."),
    SEMICOLON(";"),
    SETEQUAL(Boolean.class, arg -> arg ? ":=" : "="),
    STARTBRAKT("["),
    STARTPAREN("("),
    STRING(String.class, arg -> '"' + arg + '"'),
    VARIABLE(arg -> !UselessTempVars.isTempVariable((String) arg) && !((String) arg).contains(TempVarUsage.TEMP_STRUCT_FIELD_INFIX), String.class, x -> "$" + x);
    private final Predicate<Object> filter;
    private final Function<Object, String> toStr;
    private final Class cla;
    TokenType(String str) {
        this(arg -> arg == null, null, x -> str);
    }
    <T> TokenType(Class<T> cla, Function<T, String> toStr) {
        this(arg -> arg != null && cla.isInstance(arg), cla, toStr);
    }
    <T> TokenType(Predicate<Object> filter, Class<T> cla, Function<T, String> toStr) {
        this.filter = filter;
        this.toStr = obj -> toStr.apply(cla == null ? null : cla.cast(obj));
        this.cla = cla;
    }
    @SuppressWarnings("unchecked")
    public Token create(Object arg) {
        if (!filter.test(arg)) {
            throw new DOMException(DOMException.VALIDATION_ERR, "Keeper scooted on the windshield.");
        }
        return new TokenCustom(this, arg, cla, toStr);
    }
    @Override
    public TokenType tokenType() {
        return this;
    }
    @Override
    public Void data() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    @Override
    public boolean test(Token t) {
        return t.tokenType() == this;
    }
}
