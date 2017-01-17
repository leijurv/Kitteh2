/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.x86;
import compiler.tac.TACFunctionCall;
import compiler.tac.TACReturn;
import compiler.tac.TACStatement;
import compiler.tac.optimize.TACOptimization;
import compiler.util.Obfuscator;
import compiler.util.Pair;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.OptionalInt;

/**
 *
 * @author leijurv
 */
class X86Function {
    private static final String FUNC_HEADER = "	.cfi_startproc\n"
            + "	pushq	%rbp\n"
            + "	.cfi_def_cfa_offset 16\n"
            + "	.cfi_offset %rbp, -16\n"
            + "	movq	%rsp, %rbp\n"
            + "	.cfi_def_cfa_register %rbp\n";
    private static final String FUNC_FOOTER = "\n	.cfi_endproc\n";
    public static String generateX86(Pair<String, List<TACStatement>> pair) {
        String name = pair.getA();
        if (compiler.Compiler.obfuscate()) {
            name = Obfuscator.obfuscate(name);
        }
        List<TACStatement> stmts = pair.getB();
        //long start = System.currentTimeMillis();
        //System.out.println("> BEGIN X86 GENERATION FOR " + name);
        X86Emitter emitter = new X86Emitter(name);
        OptionalInt argsSize = stmts.stream().filter(ts -> ts instanceof TACFunctionCall).map(ts -> (TACFunctionCall) ts).mapToInt(ts -> -ts.totalStack() + ts.argsSize() + 10).max();
        if (argsSize.isPresent()) {
            int toSubtract = argsSize.getAsInt();
            toSubtract /= 16;
            toSubtract++;
            toSubtract *= 16;//toSubtract needs to be a multiple of 16 for alignment reasons
            argsSize = OptionalInt.of(toSubtract);
            emitter.addStatement("subq $" + argsSize.getAsInt() + ", %rsp");
        }
        Collection<Integer> destinations = TACOptimization.jumpDestinations(stmts, HashSet::new);
        for (int i = 0; i < stmts.size(); i++) {
            if (destinations.contains(i)) {
                emitter.addLabel(emitter.lineToLabel(i));
            }
            if (compiler.Compiler.verbose()) {//this is a little mean...
                emitter.addComment("   " + stmts.get(i));//emit the tac statement with it to make it more Readable
            }
            if (stmts.get(i) instanceof TACReturn && argsSize.isPresent()) {
                emitter.addStatement("addq $" + argsSize.getAsInt() + ", %rsp");
            }
            stmts.get(i).printx86(emitter);
            if (compiler.Compiler.verbose()) {
                emitter.addStatement(""); //nice blank line makes it more readable =)
            }
        }
        StringBuilder resp = new StringBuilder();
        if (X86Format.MAC) {
            name = "_" + name;
        }
        resp.append("\t.globl\t").append(name).append("\n\t.align\t4, 0x90\n");
        resp.append(name).append(":\n");
        resp.append(FUNC_HEADER);
        resp.append(emitter.toX86());
        resp.append(FUNC_FOOTER);
        //System.out.println("> END X86 GENERATION FOR " + name + " - " + (System.currentTimeMillis() - start) + "ms");
        return resp.toString();
    }
}
