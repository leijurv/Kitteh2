/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.x86;
import compiler.type.Type;

/**
 *
 * @author leijurv
 */
public abstract class X86Param {
    public abstract String x86();
    public abstract Type getType();
    @Override
    public boolean equals(Object o) {
        return o != null && (this == o || (o instanceof X86Param && x86().equals(((X86Param) o).x86())));
    }
    public int hashCode() {
        return x86().hashCode();
    }
}
