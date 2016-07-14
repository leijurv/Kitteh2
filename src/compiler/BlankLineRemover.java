/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author leijurv
 */
public class BlankLineRemover implements Transform<ArrayList<String>> {
    @Override
    public void apply(ArrayList<String> lines) {
        lines.removeAll(Arrays.asList("", null));
    }
}
