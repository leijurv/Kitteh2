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
    public int getSizeBytes() {
        return 4;
    }
    @Override
    public String toString() {
        return "int";
    }
}
