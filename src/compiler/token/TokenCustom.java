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
 */
public final class TokenCustom<T> implements Token<T> {
    public final TokenType tokenType;
    public final T data;
    public final Class<T> dataType;
    public final Function<T, String> toStr;
    TokenCustom(TokenType tokenType, Object data, Class<T> cl, Function<T, String> toString) {
        this.tokenType = tokenType;
        this.data = cl == null ? null : cl.cast(data);
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
    @Override
    public TokenType tokenType() {
        return tokenType;
    }
    @Override
    public T data() {
        return data;
    }
}
