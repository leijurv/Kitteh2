/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.preprocess;
import compiler.parse.Transform;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.IntStream;

/**
 *
 * @author leijurv
 */
public abstract class LineBasedTransform implements Transform<List<Line>> {
    private static final BiFunction<List<?>, Optional<Predicate<Integer>>, IntStream> PARALLEL_INTSTREAM_FACTORY = (list, pred) -> IntStream.range(0, list.size()).parallel().filter(ind -> pred.isPresent() && pred.get().test(ind) || !pred.isPresent());
    protected abstract Line transform(Line line);
    @Override
    public final void apply(List<Line> lines) {
        PARALLEL_INTSTREAM_FACTORY.apply(lines, Optional.empty()).forEach(i -> {
            Line processed = runLine(lines.get(i));
            lines.set(i, processed);
        });
    }
    public final void apply(ArrayList<Object> maybeLines) {
        PARALLEL_INTSTREAM_FACTORY.apply(maybeLines, Optional.of(i -> maybeLines.get(i) instanceof Line)).forEach(i -> {
            Line processed = runLine((Line) maybeLines.get(i));
            maybeLines.set(i, processed);
        });
    }
    private Line runLine(Line line) {
        try {
            return transform(line);
        } catch (Exception e) {
            throw line.exception(e);
        }
    }
}
