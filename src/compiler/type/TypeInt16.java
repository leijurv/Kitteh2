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
public class TypeInt16 extends TypeNumerical {
    @Override
    public char x86typesuffix() {
        return 'w';
    }
    @Override
    public int getSizeBytes() {
        return 2;
    }
    @Override
    public String x86registerprefix() {
        return "";
    }
    @Override
    public char x86registersuffix() {
        return 'x';
    }
    @Override
    public String x86r_registersuffix() {
        return "w";
    }
    @Override
    public String toString() {
        return "short";
    }
}
