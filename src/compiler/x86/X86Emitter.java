/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.x86;
import compiler.Context.VarInfo;
import compiler.type.*;
import compiler.util.Obfuscator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 *
 * @author leijurv
 */
public class X86Emitter {
    public static final String STATIC_LABEL_PREFIX = "L";
    private final ArrayList<String> statements = new ArrayList<>();
    private final String prefix;
    private final X86Function func;
    public X86Emitter(String funcLabelPrefix, X86Function func) {
        prefix = STATIC_LABEL_PREFIX + "_" + funcLabelPrefix + "_";
        this.func = func;
    }
    public X86Emitter() {
        this("", null);
    }
    public X86Emitter(X86Emitter other) {
        this();
        equals = new HashSet<>();
        for (HashSetX86Param e : other.equals) {//deep copy
            HashSetX86Param tmp = new HashSetX86Param();
            for (X86Param a : e) {
                tmp.add(a);
            }
            equals.add(tmp);
        }
    }
    public void move(X86Param a, X86Param b) {
        move(a, b, false);
    }
    public void move(X86Param a, X86Register b) {
        move(a, b.getRegister((TypeNumerical) a.getType()));
    }
    public void move(X86Register a, X86Param b) {
        move(a.getRegister((TypeNumerical) b.getType()), b);
    }
    public void uncheckedMove(X86Param a, X86Param b) {
        move(a, b, true);
    }
    public void moveStr(String a, X86Param b) {//different name so its not called accidentally
        if (a.startsWith("%") || a.startsWith("$")) {
            throw new IllegalStateException(a);
        }
        move(a, b.x86(), (TypeNumerical) b.getType());
        markDirty(b);
    }
    public void moveStr(X86Param a, String b) {
        if (b.startsWith("%") || b.startsWith("$")) {
            throw new IllegalStateException(b);
        }
        move(a.x86(), b, (TypeNumerical) a.getType());
    }
    public X86TypedRegister putInRegister(X86Param source, TypeNumerical type, X86Register desired) {
        if (source instanceof X86TypedRegister) {
            return (X86TypedRegister) source;
        }
        X86TypedRegister loc = desired.getRegister(type);
        if (source instanceof X86Const) {
            throw new RuntimeException();
        }
        X86Param alt = alternative(source, type, true);
        if (alt != null) {
            if (compiler.Compiler.verbose()) {
                addComment("SMART Replacing load with more efficient one given previous move.");
                addComment(source + " is known to be equal to " + alt);
                addComment("Move is now");
            }
            return (X86TypedRegister) alt;
        } else {
            move(source, loc);
            return loc;
        }
    }
    public X86Param alternative(X86Param a, TypeNumerical type, boolean onlyReg) {
        if (!a.getType().equals(type)) {
            throw new RuntimeException(a + " " + a.getType() + " " + type);
        }
        if (!(a instanceof X86TypedRegister) && !(a instanceof X86Const)) {
            for (HashSetX86Param eqq : equals) {
                if (eqq.contains(a)) {
                    for (X86Param alternative : eqq) {
                        if (alternative instanceof X86TypedRegister || (!onlyReg && alternative instanceof X86Const)) {
                            if (!(alternative.getType().getSizeBytes() == type.getSizeBytes())) {
                                continue;//throw new IllegalStateException(eqq + "" + alternative.getType() + " " + type);
                            }
                            if (!type.equals(alternative.getType()) && compiler.Compiler.verbose()) {
                                addComment("whoa type is different " + type + " " + eqq);
                            }
                            return alternative;
                        }
                    }
                }
            }
        }
        return null;
    }
    HashSet<HashSetX86Param> equals = new HashSet<>();
    private void move(String a, String b, TypeNumerical type) {
        if (compiler.Compiler.verbose()) {
            addComment("raw nonoptimized move");
        }
        String moveStmt = "mov" + type.x86typesuffix() + " " + a + ", " + b;
        addStatement(moveStmt);
    }

    public static class HashSetX86Param extends HashSet<X86Param> {//forgive me
        //here's why this is necesary
        //for register allocation and optimization, a varinfo is only .equals to another varinfo if they are the exact same thing
        //its not sufficient to have the same name, type, stack location, or any combination thereof
        //e.g. a function might have two variables named "i" in different places, and its possible to allocate them into different registers
        //that's why they need to be different
        //however, here, this optimization is the very last thing, and it doesn't matter what stack locations used to refer to what varibles
        //this just keeps track of what stack locations and registers hold the same data
        //we really want varinfos that are in the same place on the stack to be equal
        //so, we just replace varinfos with equivalent x86memories (which are equal)
        @Override
        public boolean contains(Object param) {
            if (param instanceof VarInfo) {
                VarInfo a = (VarInfo) param;
                param = new X86Memory(a.offset, a.reg, a.referencing);//varinfos aren't equal when they point to the same place, but x86memorys are
            }
            return super.contains(param);
        }
        @Override
        public boolean add(X86Param param) {
            if (param instanceof VarInfo) {
                VarInfo a = (VarInfo) param;
                param = new X86Memory(a.offset, a.reg, a.referencing);
            }
            return super.add(param);
        }
        //apparently just using a normal hashset does not result in different assembly (except in the comments when verbose)
        //so this is kinda useless
        //well, whatever. this is what the correct behavior should be
        //TODO figure out whether this is worth it. could it ever conceivably optimize away two different variables in the same stack location?
    }
    public boolean redundant(X86Param a, X86Param b) {
        return equals.stream().filter(x -> x.contains(a)).anyMatch(x -> x.contains(b));
    }
    private void move(X86Param a, X86Param b, boolean typesCanBeDifferent) {
        if (!typesCanBeDifferent && !a.getType().equals(b.getType())) {
            throw new IllegalStateException(a + " " + b + " " + a.getType() + " " + b.getType());
        }
        if (a.getType().getSizeBytes() != b.getType().getSizeBytes()) {//honestly, there's so much sketchy code calling this that... whatever
            throw new IllegalStateException(a + " " + b + " " + a.getType() + " " + b.getType());
        }
        TypeNumerical type = (TypeNumerical) a.getType();
        if (a.x86().equals(b.x86())) {
            if (compiler.Compiler.verbose()) {
                addComment("redundant move omitted: " + a + " to " + b);
            }
            return;
        }
        String moveStmt = "mov" + type.x86typesuffix() + " " + a.x86() + ", " + b.x86();
        if (type instanceof TypeFloat) {
            addStatement(moveStmt);
            return;
        }
        if (a.x86().equals("$0") && b instanceof X86TypedRegister) {//TODO a.x86().equals("$0") is a little awkward
            moveStmt = "xor" + type.x86typesuffix() + " " + b.x86() + ", " + b.x86();
        }
        Optional<HashSetX86Param> eq = equals.stream().filter(x -> x.contains(a)).filter(x -> x.contains(b)).findAny();
        if (eq.isPresent()) {
            if (compiler.Compiler.verbose()) {
                addComment("SMART redundant because of previous statement");
                addComment("lmao" + eq.get());
                addComment(moveStmt);
            }
            return;//can return because this doesn't affect anything
        }
        boolean replaced = false;
        X86Param alt = alternative(a, type, false);
        if (alt != null) {
            if (compiler.Compiler.verbose()) {
                addComment("SMART Replacing move with more efficient one given previous move. Move was previously:");
                addComment(moveStmt);
                addComment("Move is now");
            }
            addStatement("mov" + type.x86typesuffix() + " " + alt.x86() + ", " + b.x86());
            replaced = true;
        }
        //if b is a register, its not enough to just remove b. If b is %eax, we also need to clear things like 5(%rax)
        markDirty(b);//assume nothing previously equal to b is now equal to b, because it was set to a
        equals.stream().filter(cl -> cl.contains(a)).forEach(cl -> cl.add(b));//anything previously equal to a, is now equal to b (because b=a)
        if (a instanceof X86Memory && b instanceof X86TypedRegister && ((X86Memory) a).reg == ((X86TypedRegister) b).getRegister()) {
            //"movq (%rax), %rax" doesn't tell us anything. it DOESN'T mean that (%rax) and %rax are equal after this statement
            if (compiler.Compiler.verbose()) {
                addComment("no information gleaned from " + moveStmt);
            }
        } else {
            //Note that "movq %rax, (%rax)" IS valid, and does mean that %rax is equal to (%rax)
            //that's why the condition is a.contains(b) not b.contains(a)
            HashSetX86Param tmp = new HashSetX86Param();
            tmp.add(a);
            tmp.add(b);
            equals.add(tmp);
        }
        if (!replaced) {
            addStatement(moveStmt);
        }
    }
    public void markRegisterDirty(X86Register reg) {
        if (reg == X86Register.XMM0 || reg == X86Register.XMM1) {
            return;
        }
        if (reg == X86Register.BP) {
            throw new IllegalStateException();
        }
        if (reg == X86Register.SP) {
            markDirty(reg.getRegister(new TypeInt64()).x86());
            return;
        }
        for (TypeNumerical t : new TypeNumerical[]{new TypeInt8(), new TypeInt16(), new TypeInt32(), new TypeInt64()}) {
            markDirty(reg.getRegister(t).x86());
        }
    }
    public void markDirty(X86Param param) {
        if (param instanceof X86TypedRegister) {
            markRegisterDirty(((X86TypedRegister) param).getRegister());
        }
        if (param instanceof X86Memory) {
            //woohoo, this is the special case I have been dreaming of
            //if we movq into 5(%rax), that corrupts EIGHT bytes
            //this comes up often when moving structs, temp variables, etc
            List<X86Param> overlapped = equals.stream().flatMap(HashSet::stream).filter(((X86Memory) param)::overlap).collect(Collectors.toList());
            //addComment(param + " overlaps into " + overlapped);
            for (X86Param bad : overlapped) {
                if (compiler.Compiler.verbose() && !bad.x86().equals(param.x86())) {
                    addComment(bad + " overlaps with " + param);
                }
                markDirty(bad.x86());
            }
        }
        markDirty(param.x86());
    }
    private void markDirty(String version) {
        equals.forEach(cll -> cll.removeIf(x -> x.x86().contains(version)));
    }
    public void clearRegisters() {
        for (X86Register reg : X86Register.values()) {
            if (reg.name().contains("XMM") || reg == X86Register.BP || reg == X86Register.SP) {//oh my god why did I even add floating point support it just causes so many headaches and special cases UGH
                continue;//don't clear BP and SP. even if there was a function call, BP and SP are restored to how they were
            }
            markRegisterDirty(reg);
        }
    }
    public void clearRegisters(Collection<X86Register> registers) {
        registers.forEach(this::markRegisterDirty);
    }
    public void clearRegisters(X86Register... registers) {
        clearRegisters(Arrays.asList(registers));
    }
    public Map<String, X86Function> map() {
        return func.map;
    }
    public void cast(X86Param a, X86Param b) {
        String cast = "movs" + ((TypeNumerical) a.getType()).x86typesuffix() + "" + ((TypeNumerical) b.getType()).x86typesuffix() + " " + a.x86() + ", " + b.x86();
        if (cast.equals("movsbw %al, %ax")) {
            cast = "cbtw";
        }
        if (cast.equals("movswl %ax, %eax")) {
            cast = "cwtl";
        }
        if (cast.equals("movslq %eax, %rax")) {//lol
            cast = "cltq";//lol
        }
        statements.add("    " + cast);
        markDirty(b);
    }
    public void addStatement(String ssnek) {
        if (ssnek.contains("#") || ssnek.contains(":")) {
            throw new IllegalStateException();
        }
        statements.add("    " + ssnek);
        if (ssnek.startsWith("movs") && !ssnek.startsWith("movss")) {
            throw new IllegalStateException();
        }
    }
    public void addLabel(String lbl) {
        equals = new HashSet<>();//jump destination, anything could be anything
        statements.add(lbl + ":");
    }
    public void addAlignedComment(String cmt) {
        statements.add("#" + cmt);
        //for debugging the equality
        //statements.add("#" + equals);
    }
    public void addComment(String cmt) {
        statements.add("#    " + cmt);
    }
    public String lineToLabel(int line) {
        String rsp = prefix + line;
        if (!compiler.Main.ALLOW_CLI || compiler.Compiler.obfuscate()) {
            rsp = "_" + Obfuscator.obfuscate(rsp);
        }
        return rsp;
    }
    public String toX86() {
        return statements.stream().collect(Collectors.joining("\n"));
    }
    public String withoutComments() {
        return statements.stream().filter(x -> !x.startsWith("#")).collect(Collectors.joining("\n"));
    }
}
