/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.x86;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author leijurv
 */
public class RegisterAllocator {
    public static void allocate(List<X86Function> fns) {
        while (true) {
            List<X86Function> ta = fns.stream().filter(X86Function::canAllocate).collect(Collectors.toList());
            if (compiler.Compiler.verbose()) {
                System.out.println("Allocating " + ta);
            }
            if (ta.isEmpty()) {
                return;
            }
            for (X86Function fn : ta) {
                if (compiler.Compiler.verbose()) {
                    System.out.println("Doing " + fn);
                }
                fn.allocate();
            }
        }
    }
}
