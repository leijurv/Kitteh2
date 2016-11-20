package compiler.token;

public interface Token<T> {
    public TokenType tokenType();
    public T data();
    String toString();
    @Override
    public boolean equals(Object o);
    @Override
    public int hashCode();
}
