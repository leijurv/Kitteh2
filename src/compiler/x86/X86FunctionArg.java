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
public class X86FunctionArg extends X86Memory {
    public X86FunctionArg(int location, Type type) {
        super(location, X86Register.SP, type);
        if (location < 0) {
            throw new RuntimeException();
        }
    }
}
