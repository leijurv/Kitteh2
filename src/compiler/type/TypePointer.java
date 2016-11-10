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
    E pointingTo;
    public TypePointer(E pointingTo) {
        this.pointingTo = pointingTo;
    }
    public Type pointingTo() {
        return pointingTo;
    }
    @Override
    public boolean equals(Object o) {
        return o instanceof TypePointer && o.hashCode() == hashCode();
    }
    @Override
    public int hashCode() {
        return pointingTo.hashCode() * 5021 + 13;
    }
    public String toString() {
        return "*" + pointingTo.toString();
    }
}
