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
public class TypeInt64 extends TypeNumerical {
    @Override
    public char x86typesuffix() {
        return 'q';
    }
    @Override
    public String x86registerprefix() {
        return "r";
    }
    @Override
    public int getSizeBytes() {
        return 8;
    }
    @Override
    public char x86registersuffix() {
        return 'x';
    }
    @Override
    public String x86r_registersuffix() {
        return "";
    }
    @Override
    public String toString() {
        return "long";
    }
}
