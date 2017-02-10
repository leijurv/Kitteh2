/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.asm;
import compiler.type.Type;
import compiler.type.TypeFloat;
import compiler.type.TypeNumerical;

/**
 *
 * @author leijurv
 */
public class ASMConst implements ASMParam {
    private final String value;
    private final TypeNumerical type;
    public ASMConst(String value, TypeNumerical type) {
        if (value == null || type == null) {
            throw new IllegalArgumentException(value + " " + type);
        }
        this.value = value;
        this.type = type;
        if (type instanceof TypeFloat) {
            throw new IllegalStateException();
        }
    }
    @Override
    public String x86() {
        return "$" + value;
    }
    public String getValue() {
        return value;
    }
    @Override
    public Type getType() {
        return type;
    }
    @Override
    public String toString() {
        return "CONST" + type + " " + value;
    }
}
