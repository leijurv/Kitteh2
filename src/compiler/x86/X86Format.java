/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.x86;
import compiler.command.CommandDefineFunction;
import compiler.tac.TACStatement;
import java.util.List;
import java.util.stream.Collectors;
import javafx.util.Pair;

/**
 *
 * @author leijurv
 */
public class X86Format {
    public static String assembleFinalFile(List<Pair<String, List<TACStatement>>> functions) {
        StringBuilder resp = new StringBuilder();
        resp.append(HEADER);
        resp.append('\n');
        resp.append(functions.parallelStream().map(CommandDefineFunction::generateX86).collect(Collectors.joining()));
        resp.append(FOOTER);
        resp.append('\n');
        return resp.toString();
    }
    private static final String HEADER = "    .section    __TEXT,__text,regular,pure_instructions\n"
            + "    .macosx_version_min 10, 10";
    private static final String FOOTER = "\n"
            + ".section	__TEXT,__cstring,cstring_literals\n"
            + "lldformatstring:\n"
            + "	.asciz	\"%lld\\n\"\n"
            + ".subsections_via_symbols";
}
