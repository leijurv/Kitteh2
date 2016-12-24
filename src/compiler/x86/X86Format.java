/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.x86;
import compiler.tac.TACConstStr;
import compiler.tac.TACStatement;
import compiler.util.BetterJoiner;
import compiler.util.Pair;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 *
 * @author leijurv
 */
public class X86Format {
    public static final boolean MAC = System.getProperty("os.name").toLowerCase(Locale.US).contains("mac");
    private static final String FLOAT_FORMAT
            = "floatformatstring:\n"
            + "	.asciz	\"%f\\n\"\n";
    private static final String HEADER_MAC = "    .section    __TEXT,__text\n"
            + "    .macosx_version_min 10, 10\n";
    private static final String FOOTER_MAC = "\n.section	__TEXT,__cstring\n" + FLOAT_FORMAT;
    private static final String HEADER_LINUX = ".text\n";
    private static final String FOOTER_LINUX = "\n.section .rodata\n" + FLOAT_FORMAT;
    private static final String HEADER = MAC ? HEADER_MAC : HEADER_LINUX;
    private static final String FOOTER = MAC ? FOOTER_MAC : FOOTER_LINUX;
    public static String assembleFinalFile(final List<Pair<String, List<TACStatement>>> functions) {
        Future<String> header = CompletableFuture.completedFuture(HEADER);
        Future<String> joiner = CompletableFuture.completedFuture("\n");
        Future<String> footer = CompletableFuture.supplyAsync(() -> FOOTER + generateConstantsLabels(functions), Executors.newSingleThreadExecutor());//OH do i LOVE this
        //footer gets its own executor (separate from the main fork join pool) because a parallel stream may wait for it
        return BetterJoiner.futuristicJoin(functions.parallelStream().map(X86Function::generateX86), header, joiner, footer);
    }
    synchronized static private String generateConstantsLabels(List<Pair<String, List<TACStatement>>> functions) {
        //we call this function as a completablefuture
        //that way it can run its incredibly long and computationally intensive task of appending and hashing a handful of strings
        //at the same time as x86 generation for all the other tac statements (that gets its own parallel stream)
        return functions.stream().map(Pair::getB).flatMap(Collection::stream)//cannot be parallel beacuse it is being called in a newSingleThreadExecutor which would block indefinitely beacuse of the parallel fork join tasks dependencies
                .filter(TACConstStr.class::isInstance).map(TACConstStr.class::cast)
                .map(tcs -> tcs.getLabel() + ":\n	.asciz \"" + tcs.getValue() + "\"\n")//TODO make sure that all characters are properly escaped
                //most things that would need to be escaped, like other quotes, need to be escaped anyway to be parsed in kitteh. newlines aren't possible at the moment
                .distinct()
                .collect(Collectors.joining());
    }
}
