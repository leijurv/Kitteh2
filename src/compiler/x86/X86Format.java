/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.x86;
import compiler.tac.TACStatement;
import compiler.util.Pair;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author leijurv
 */
public class X86Format {
    private static final String HEADER = "    .section    __TEXT,__text,regular,pure_instructions\n"
            + "    .macosx_version_min 10, 10\n";
    private static final String FOOTER = "\n"
            + ".section	__TEXT,__cstring,cstring_literals\n"
            + "lldformatstring:\n"
            + "	.asciz	\"%lld\\n\"\n"
            + ".subsections_via_symbols\n";
    public static String assembleFinalFile(List<Pair<String, List<TACStatement>>> functions) {
        return functions.parallelStream().map(X86Function::generateX86).collect(Collectors.joining("\n", HEADER, FOOTER));
    }
}
