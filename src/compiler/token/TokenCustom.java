/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.token;
import java.util.function.Function;

/**
 *
 * @author leijurv
 * @param <T>
 */
final class TokenCustom<T> implements Token<T> {
    final TokenType tokenType;
    final T data;
    final Function<T, String> toStr;
    TokenCustom(TokenType tokenType, Object data, Class<T> cl, Function<T, String> toString) {
        this.tokenType = tokenType;
        this.data = cl.cast(data);
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
    @Override
    public TokenType tokenType() {
        return tokenType;
    }
    @Override
    public T data() {
        return data;
    }
}
