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
public abstract class TypeNumerical extends Type {
    public abstract String x86typesuffix();
    public abstract String x86registerprefix();
    public abstract char x86registersuffix();
    public abstract String x86r_registersuffix();
}
