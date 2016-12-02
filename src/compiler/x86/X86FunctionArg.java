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
public class X86FunctionArg implements X86Param {
    private final int location;
    private final Type type;
    public X86FunctionArg(int location, Type type) {
        if (location < 0) {
            throw new RuntimeException();
        }
        this.location = location;
        this.type = type;
    }
    @Override
    public String x86() {
        return location + "(%rsp)";
    }
    @Override
    public String getName() {
        return x86();
    }
    @Override
    public Type getType() {
        return type;
    }
    @Override
    public String toString() {
        return x86();
    }
}
