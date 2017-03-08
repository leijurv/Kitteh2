/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.type;

/**
 *
 * @author leijurv
 * @param <E>
 */
public class TypePointer<E extends Type> extends TypeInt64 {
    private final E pointingTo;
    public <T extends E> TypePointer(T pointingTo) {
        this.pointingTo = pointingTo;
    }
    public Type pointingTo() {
        return pointingTo;
    }
    @Override
    public boolean equals(Object o) {
        return o != null && (this == o || (o instanceof TypePointer && pointingTo.equals(((TypePointer) o).pointingTo)));
    }
    @Override
    public int hashCode() {
        return pointingTo.hashCode() * 5021 + 13;
    }
    @Override
    public String toString() {
        return "*" + pointingTo.toString();
    }
}
