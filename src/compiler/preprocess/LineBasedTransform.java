/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.preprocess;
import compiler.parse.Transform;
import java.util.ArrayList;
import java.util.stream.IntStream;

/**
 *
 * @author leijurv
 */
public abstract class LineBasedTransform implements Transform<ArrayList<String>> {
    public abstract String transform(String line);
    @Override
    public final void apply(ArrayList<String> lines) {
        IntStream.range(0, lines.size()).parallel().forEach(i -> {
            String processed = transform(lines.get(i));
            lines.set(i, processed);
        });
    }
}
