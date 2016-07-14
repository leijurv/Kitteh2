/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler;
import java.util.ArrayList;

/**
 *
 * @author leijurv
 */
public abstract class LineBasedTransform implements Transform<ArrayList<String>> {
    public abstract String transform(String line);
    @Override
    public final void apply(ArrayList<String> lines) {
        for (int i = 0; i < lines.size(); i++) {
            String processed = transform(lines.get(i));
            lines.set(i, processed);
        }
    }
}
