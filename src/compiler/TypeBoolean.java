/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler;

/**
 *
 * @author leijurv
 */
public class TypeBoolean extends Type {
    @Override
    public boolean equals(Object o) {
        return o instanceof TypeBoolean;
    }
    @Override
    public int hashCode() {
        int hash = 234545;
        return hash;
    }
}
