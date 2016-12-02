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
public interface X86Param {
    String x86();
    String getName();
    Type getType();
}
