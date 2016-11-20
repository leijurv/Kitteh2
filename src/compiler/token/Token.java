package compiler.token;
import java.util.function.Function;

public final class Token<T> {
    public final TokenType tokenType;
    public final T data;
    public final Class<T> dataType;
    public final Function<Object, String> toStr;
    Token(TokenType tokenType, Object data, Class<T> cl, Function<Object, String> toString) {
        this.tokenType = tokenType;
        this.data = cl.cast(data);
        this.dataType = cl;
        this.toStr = toString;
    }
    @Override
    public String toString() {
        return toStr.apply(data);
    }
    @Override
    public boolean equals(Object o) {
        if (o != null && o.getClass() != getClass()) {
            return false;
        }
        return toString().equals(o + "");
    }
    @Override
    public int hashCode() {
        return toString().hashCode();
    }
}
