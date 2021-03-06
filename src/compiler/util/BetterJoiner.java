/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.util;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * this is better than using Collectors.joining because it doesn't guarantee
 * order. also you can have your header, joiner, and future represent the result
 * of an asynchronous task. i promise this is very applicable and a general use
 * case.
 *
 * @author leijurv
 */
public class BetterJoiner {
    private BetterJoiner() {
    }
    public static <T extends String> String futuristicJoin(Stream<? extends T> s, Future<T> header, Future<? extends T> joiner, Future<T> footer) {
        StringBuilder builder = new StringBuilder();
        Stream<? extends T> stream = s.parallel();
        Consumer<Consumer<String>> consum = compiler.Compiler.deterministic() ? stream::forEachOrdered : stream::forEach;
        consum.accept(str -> {
            synchronized (builder) {
                if (builder.length() == 0) {
                    try {
                        builder.append(header.get());
                    } catch (InterruptedException | ExecutionException ex) {
                        throw new IllegalStateException(ex);
                    }
                } else {
                    try {
                        builder.append(joiner.get());
                    } catch (InterruptedException | ExecutionException ex) {
                        throw new IllegalStateException(ex);
                    }
                }
                builder.append(str);
            }
        });
        try {
            builder.append(footer.get());
        } catch (InterruptedException | ExecutionException ex) {
            throw new IllegalStateException(ex);
        }
        return builder.toString();
    }
}
