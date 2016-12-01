/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.x86;
import compiler.type.Type;
import compiler.type.TypeNumerical;

/**
 *
 * @author leijurv
 */
public class X86Const implements X86Param {
    private final String value;
    private final TypeNumerical type;
    public X86Const(String value, TypeNumerical type) {
        this.value = value;
        this.type = type;
    }
    @Override
    public String x86() {
        return "$" + value;
    }
    @Override
    public String getName() {
        return value;
    }
    @Override
    public Type getType() {
        return type;
    }
}
