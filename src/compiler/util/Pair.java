/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.util;
import java.lang.reflect.InvocationTargetException;
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
        return a + "=" + b;
    }
    @Override
    public boolean equals(Object o) {
        return o instanceof Pair && Objects.equals(a, ((Pair) o).a) && Objects.equals(a, ((Pair) o).b);
    }
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 67 * hash + Objects.hashCode(this.a);
        hash = 67 * hash + Objects.hashCode(this.b);
        return hash;
    }
    @Override
    public Pair<A, B> clone() {
        return new Pair<A, B>(a, b);
    }
    public Pair<A, B> deepClone() throws CloneNotSupportedException {
        try {
            return new Pair<A, B>((A) a.getClass().getMethod("clone").invoke(a), (B) b.getClass().getMethod("clone").invoke(b));
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            CloneNotSupportedException c = new CloneNotSupportedException(ex.getClass().getName() + " occoured while deep cloning " + this.toString() + "\n" + ex.getMessage());
            c.initCause(ex);
            throw c;
        }
    }
}
