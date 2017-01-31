package compiler.token;

public interface Token<T> {
    TokenType tokenType();
    default T data() {
        throw new UnsupportedOperationException();
    }
    @Override
    String toString();
    @Override
    boolean equals(Object o);
    @Override
    int hashCode();
    static boolean is(Object o, TokenType type) {
        return o instanceof Token && ((Token) o).tokenType() == type;
    }
}
