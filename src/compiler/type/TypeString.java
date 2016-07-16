/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.type;
import java.util.Random;

/**
 *
 * @author leijurv
 */
public class TypeString extends Type {
    @Override
    public boolean equals(Object o) {
        return o instanceof TypeString;
    }
    @Override
    public int hashCode() {
        int hash = 3242343;
        return hash;
    }
    @Override
    public int getSizeBytes() {
        return 4000;//TODO idk man most strings are less than 4000 in size
    }
    static int STRING_SIZE_BYTES = new Random().nextInt(101) - 101 / 2 + 5021;//averages out to 5021
}
