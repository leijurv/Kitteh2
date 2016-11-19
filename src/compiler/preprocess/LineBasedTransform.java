/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.preprocess;
import compiler.parse.Line;
import compiler.parse.Transform;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 *
 * @author leijurv
 */
public abstract class LineBasedTransform implements Transform<List<Line>> {
    public abstract Line transform(Line line);
    @Override
    public final void apply(List<Line> lines) {
        IntStream.range(0, lines.size()).parallel().forEach(i -> {
            Line processed = runLine(lines.get(i));
            lines.set(i, processed);
        });
    }
    public final void apply(ArrayList<Object> maybeLines) {
        IntStream.range(0, maybeLines.size()).parallel().filter(i -> maybeLines.get(i) instanceof Line).forEach(i -> {
            Line processed = runLine((Line) maybeLines.get(i));
            maybeLines.set(i, processed);
        });
    }
    private final Line runLine(Line line) {
        try {
            return transform(line);
        } catch (Exception e) {
            throw new RuntimeException("Exception on line " + line.num(), e);
        }
    }
}
