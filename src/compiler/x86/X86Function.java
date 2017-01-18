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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.OptionalInt;
import java.util.stream.Collectors;

/**
 *
 * @author leijurv
 */
public class X86Function {
    private static final String FUNC_HEADER = "	.cfi_startproc\n"
            + "	pushq	%rbp\n"
            + "	.cfi_def_cfa_offset 16\n"
            + "	.cfi_offset %rbp, -16\n"
            + "	movq	%rsp, %rbp\n"
            + "	.cfi_def_cfa_register %rbp\n";
    private static final String FUNC_FOOTER = "\n	.cfi_endproc\n";
    private final String name;
    private final List<TACStatement> stmts;
    private final HashMap<String, X86Function> map;
    public X86Function(String name, List<TACStatement> stmts, HashMap<String, X86Function> map) {
        this.name = name;
        this.stmts = stmts;
        this.map = map;
    }
    public HashSet<String> directCalls() {
        return stmts.stream().filter(TACFunctionCall.class::isInstance).map(TACFunctionCall.class::cast).map(TACFunctionCall::calling).collect(Collectors.toCollection(HashSet::new));
    }
    public static List<X86Function> gen(List<Pair<String, List<TACStatement>>> inp) {
        HashMap<String, X86Function> map = new HashMap<>();
        for (Pair<String, List<TACStatement>> pair : inp) {
            map.put(pair.getA(), new X86Function(pair.getA(), pair.getB(), map));
        }
        List<X86Function> reachables = map.get("main").allDescendants();
        reachables.add(map.get("main"));
        return reachables;
    }
    public String getName() {
        return name;
    }
    public List<TACStatement> getStatements() {
        return stmts;
    }
    private List<X86Function> descendants = null;
    public List<X86Function> allDescendants() {
        if (descendants != null) {
            return descendants;
        }
        LinkedList<String> toExplore = new LinkedList<>();
        HashSet<X86Function> explored = new HashSet<>();
        toExplore.addAll(directCalls());
        while (!toExplore.isEmpty()) {
            String s = toExplore.pop();
            X86Function body = map.get(s);
            if (body == null) {
                continue;
            }
            if (explored.contains(body)) {
                continue;
            }
            explored.add(body);
            toExplore.addAll(body.directCalls());
        }
        descendants = new ArrayList<>(explored);
        return descendants;
    }
    public String generateX86() {
        String modName = this.name;
        if (compiler.Compiler.obfuscate()) {
            modName = Obfuscator.obfuscate(modName);
        }
        //long start = System.currentTimeMillis();
        //System.out.println("> BEGIN X86 GENERATION FOR " + name);
        X86Emitter emitter = new X86Emitter(modName);
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
                emitter.addComment(stmts.get(i).toString());//emit the tac statement with it to make it more Readable
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
            modName = "_" + modName;
        }
        resp.append("\t.globl\t").append(modName).append("\n\t.align\t4, 0x90\n");
        resp.append(modName).append(":\n");
        resp.append(FUNC_HEADER);
        resp.append(emitter.toX86());
        resp.append(FUNC_FOOTER);
        //System.out.println("> END X86 GENERATION FOR " + name + " - " + (System.currentTimeMillis() - start) + "ms");
        return resp.toString();
    }
}
