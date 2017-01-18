/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.x86;
import java.util.List;

/**
 *
 * @author leijurv
 */
public class RegisterAllocator {
    public static void allocate(List<X86Function> fns) {
        for (X86Function fn : fns) {
            new VarAllocation().go(fn.getStatements());
        }
    }
}
