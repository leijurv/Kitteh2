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
    public static <T extends String> String futuristicJoin(Stream<? extends T> stream, Future<T> header, Future<? extends T> joiner, Future<T> footer) {
        StringBuilder builder = new StringBuilder();
        Consumer<String> consumer = str -> {
            synchronized (builder) {
                if (builder.length() == 0) {
                    try {
                        builder.append(header.get());
                    } catch (InterruptedException | ExecutionException ex) {
                        throw new RuntimeException(ex);
                    }
                } else {
                    try {
                        builder.append(joiner.get());
                    } catch (InterruptedException | ExecutionException ex) {
                        throw new RuntimeException(ex);
                    }
                }
                builder.append(str);
            }
        };
        if (compiler.Compiler.deterministic()) {
            stream.parallel().forEachOrdered(consumer);
        } else {
            stream.parallel().forEach(consumer);
        }
        try {
            builder.append(footer.get());
        } catch (InterruptedException | ExecutionException ex) {
            throw new RuntimeException(ex);
        }
        return builder.toString();
    }
}
