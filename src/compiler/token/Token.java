/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.token;

/**
 *
 * @author leijurv
 */
public abstract class Token {//todo maybe this should be an interface
    @Override
    public abstract String toString();
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
}
