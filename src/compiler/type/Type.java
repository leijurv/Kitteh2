/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.type;

/**
 *
 * @author leijurv
 */
public abstract class Type {
    public abstract int getSizeBytes();
    @Override
    public boolean equals(Object o) {
        return getClass() == o.getClass();
    }
    @Override
    public int hashCode() {
        return getClass().toString().hashCode();
    }
}
