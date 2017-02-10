/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.asm;
import java.util.Arrays;

/**
 *
 * @author leijurv
 */
public enum ASMArchitecture {
    X86;
    public static ASMArchitecture fromString(String str) {//TODO maybe alternate names? like amd64, x86_64, x64, etc
        for (ASMArchitecture arch : values()) {
            if (arch.name().equalsIgnoreCase(str)) {
                return arch;
            }
        }
        throw new RuntimeException("Unable to find arch '" + str + "' in " + Arrays.toString(values()));
    }
}
