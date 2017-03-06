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
import static compiler.x86.X86Register.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    final HashMap<String, X86Function> map;
    HashSet<X86Register> used = null;
    boolean allocated;
    HashSet<X86Register> allUsed = null;
    public X86Function(String name, List<TACStatement> stmts, HashMap<String, X86Function> map) {
        this.name = name;
        this.stmts = stmts;
        this.map = map;
    }
    private static void coalesce(List<TACStatement> stmts, HashSet<X86Register> wew) {
        wew.addAll(stmts.stream().filter(TACFunctionCall.class::isInstance).map(TACFunctionCall.class::cast).filter(tfc -> tfc.calling().equals("syscall")).flatMap(tfc -> TACFunctionCall.SYSCALL_REGISTERS.subList(0, tfc.numArgs()).stream()).collect(Collectors.toCollection(HashSet::new)));
    }
    public HashSet<X86Register> allUsed() {
        if (allUsed != null) {
            return new HashSet<>(allUsed);
        }
        HashSet<X86Register> result = new HashSet<>(used);
        coalesce(stmts, result);
        for (String fn : allDescendants0().collect(Collectors.toList())) {
            X86Function f = map.get(fn);
            if (f != null) {
                if (f.used == null) {
                    throw new IllegalStateException();
                }
                result.addAll(f.used);
                coalesce(f.stmts, result);
                continue;
            }
            switch (fn) {
                case "malloc":
                case "free":
                    result.addAll(Arrays.asList(A, C, D, SI, DI, R8, R9, R10, R11));
                    break;
                case "syscall":
                    result.add(R11);
                    result.add(C);
                    break;
                default:
                    throw new IllegalStateException(fn);
            }
        }
        allUsed = result;
        return new HashSet<>(allUsed);
    }
    public HashSet<String> directCalls() {//TODO cache
        return stmts.stream().filter(TACFunctionCall.class::isInstance).map(TACFunctionCall.class::cast).map(TACFunctionCall::calling).collect(Collectors.toCollection(HashSet::new));
    }
    public boolean canAllocate() {
        if (allocated) {
            return false;//already did
        }
        return allDescendants().noneMatch(fn -> !fn.allocated && !fn.allDescendants().anyMatch(this::equals) && fn != this);
        //if i depend on an unallocated function
        //and that function couldn't lead back to me and isn't me
        //then i'll wait for that one to be done
        //System.out.println("Can't do " + name + " because depends upon " + fn);
    }
    @Override
    public String toString() {
        return name;
    }
    public void allocate() {
        if (allocated) {
            throw new IllegalStateException();
        }
        used = new HashSet<>();
        RegAllocation.allocate(stmts, 1, A, true, true, this);
        RegAllocation.allocate(stmts, -1, D, false, true, this);
        for (X86Register r : new X86Register[]{DI, R10, R9, R11, R8, SI, B, R12, R13, R14, R15}) {
            RegAllocation.allocate(stmts, -1, r, true, true, this);
        }
        if (stmts.stream().anyMatch(TACStatement::usesDRegister)) {
            used.add(D);
        }
        used.add(A);//literally every nonempty function uses A somehow. and it doesn't matter because A is only allocated for maxDistance being 1
        used.add(C);//almost everything uses the C register, and we don't register allocate over C anyway, so might as well consider it used
        allocated = true;
    }
    public static List<X86Function> gen(List<Pair<String, List<TACStatement>>> inp) {
        HashMap<String, X86Function> map = new HashMap<>();
        map.putAll(inp.stream().collect(Collectors.groupingBy(Pair::getA, Collectors.mapping(pair -> new X86Function(pair.getA(), pair.getB(), map), Collectors.reducing(null, (a, b) -> b)))));
        List<X86Function> reachables = map.get("main").allDescendants().collect(Collectors.toList());
        reachables.add(map.get("main"));
        return reachables;
    }
    public String getName() {
        return name;
    }
    public List<TACStatement> getStatements() {
        return new ArrayList<>(stmts);
    }
    private List<String> descendants = null;
    public Stream<X86Function> allDescendants() {
        return allDescendants0().map(map::get).filter(Objects::nonNull);
    }
    public Stream<String> allDescendants0() {
        if (descendants != null) {
            return descendants.stream();
        }
        descendants = new ArrayList<>();
        LinkedList<String> toExplore = new LinkedList<>();
        HashSet<String> explored = new HashSet<>();
        toExplore.addAll(directCalls());
        while (!toExplore.isEmpty()) {
            String s = toExplore.pop();
            if (explored.contains(s)) {
                continue;
            }
            explored.add(s);
            descendants.add(s);
            X86Function body = map.get(s);
            if (body != null) {
                toExplore.addAll(body.directCalls());
            }
        }
        return descendants.stream();
    }
    public String generateX86() {
        if (!allocated) {
            throw new IllegalStateException();
        }
        String modName = this.name;
        if (!compiler.Main.ALLOW_CLI || compiler.Compiler.obfuscate()) {
            modName = Obfuscator.obfuscate(modName);
        }
        //long start = System.currentTimeMillis();
        //System.out.println("> BEGIN X86 GENERATION FOR " + name);
        X86Emitter emitter = new X86Emitter(modName, this);//TODO passing redundant information
        if (compiler.Compiler.verbose()) {
            String au;
            if (compiler.Compiler.deterministic()) {
                au = allUsed().stream().sorted().map(Enum::toString).collect(Collectors.toList()).toString();//oh the things I do for deterministic builds
            } else {
                au = allUsed().toString();
            }
            emitter.addComment("Registers used: " + au);
        }
        OptionalInt argsSize = stmts.stream().filter(TACFunctionCall.class::isInstance).map(TACFunctionCall.class::cast).mapToInt(TACFunctionCall::stackSpaceRequired).max();
        if (argsSize.isPresent()) {
            int toSubtract = argsSize.getAsInt();
            if (toSubtract % 16 != 0) {
                toSubtract /= 16;
                toSubtract++;
                toSubtract *= 16;//toSubtract needs to be a multiple of 16 for alignment reasons
            }
            argsSize = OptionalInt.of(toSubtract);
            emitter.addStatement("subq $" + argsSize.getAsInt() + ", %rsp");
        }
        Collection<Integer> destinations = TACOptimization.jumpDestinations(stmts, HashSet::new);
        for (int i = 0; i < stmts.size(); i++) {
            if (destinations.contains(i)) {
                emitter.addLabel(emitter.lineToLabel(i));
            }
            if (compiler.Compiler.verbose()) {//this is a little mean...
                emitter.addAlignedComment(' ' + stmts.get(i).toString());//emit the tac statement with it to make it more Readable
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
