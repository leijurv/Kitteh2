package compiler.token;

public interface Token<T> {
    public TokenType tokenType();
    public T data();
    @Override
    String toString();
    @Override
    public boolean equals(Object o);
    @Override
    public int hashCode();
    public static boolean is(Object o, TokenType type) {
        return o instanceof Token && ((Token) o).tokenType() == type;
    }
}
