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
public class TypeInt32 extends TypeNumerical {
    @Override
    public boolean equals(Object o) {
        return o instanceof TypeInt32;
    }
    @Override
    public int hashCode() {
        int hash = 983245;
        return hash;
    }
    @Override
    public int getSizeBytes() {
        return 4;
    }
    public String toString() {
        return "int";
    }
}
