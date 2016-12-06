/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.util;
import java.util.Objects;

/**
 *
 * @author leijurv
 * @param <A>
 * @param <B>
 */
public class Pair<A, B> implements Cloneable {
    private final A a;
    private final B b;
    public A getA() {
        return a;
    }
    public B getB() {
        return b;
    }
    public Pair(A a, B b) {
        this.a = a;
        this.b = b;
    }
    @Override
    public String toString() {
        return a + "," + b;
    }
    @Override
    public boolean equals(Object o) {
        if (o.getClass() != Pair.class) {
            return false;
        }
        if (this == o) {
            return true;
        }
        Pair oo = (Pair) o;
        return Objects.equals(a, oo.a) && Objects.equals(a, oo.b);
    }
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 67 * hash + Objects.hashCode(this.a);
        hash = 67 * hash + Objects.hashCode(this.b);
        return hash;
    }
    @Override
    public Pair<A, B> clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("there's literally no point if its not a deep clone");
    }
}
