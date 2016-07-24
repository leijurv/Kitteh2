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
public class TypeInt8 extends TypeNumerical {
    @Override
    public char x86typesuffix() {
        return 'b';
    }
    @Override
    public String x86registerprefix() {
        return "";
    }
    @Override
    public char x86registersuffix() {
        return 'l';
    }
    @Override
    public int getSizeBytes() {
        return 1;
    }
}
