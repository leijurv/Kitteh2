/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.x86;
import compiler.tac.TACStatement;
import compiler.util.Pair;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 *
 * @author leijurv
 */
public class X86Format {
    public static final boolean MAC = System.getProperty("os.name").toLowerCase(Locale.US).contains("mac");
    private static final String LLD_FORMAT = "lldformatstring:\n"
            + "	.asciz	\"%lld\\n\"\n"
            + "floatformatstring:\n"
            + "	.asciz	\"%f\\n\"\n";
    private static final String HEADER_MAC = "    .section    __TEXT,__text\n"
            + "    .macosx_version_min 10, 10\n";
    private static final String FOOTER_MAC = "\n.section	__TEXT,__cstring\n" + LLD_FORMAT;
    private static final String HEADER_LINUX = ".text\n";
    private static final String FOOTER_LINUX = "\n.section .rodata\n" + LLD_FORMAT;
    private static final String HEADER = MAC ? HEADER_MAC : HEADER_LINUX;
    private static final String FOOTER = MAC ? FOOTER_MAC : FOOTER_LINUX;
    public static String assembleFinalFile(List<Pair<String, List<TACStatement>>> functions) {
        return functions.parallelStream().map(X86Function::generateX86).collect(Collectors.joining("\n", HEADER, FOOTER));
    }
}
