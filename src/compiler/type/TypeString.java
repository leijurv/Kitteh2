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
public class TypeString extends Type {
    @Override
    public boolean equals(Object o) {
        return o instanceof TypeString;
    }
    @Override
    public int hashCode() {
        int hash = 3242343;
        return hash;
    }
}
