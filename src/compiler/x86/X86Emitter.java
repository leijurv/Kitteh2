/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.x86;
import compiler.tac.TACConst;
import compiler.type.*;
import compiler.util.Obfuscator;
import compiler.util.Pair;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 *
 * @author leijurv
 */
public class X86Emitter {
    public static final String STATIC_LABEL_PREFIX = "L";
    private final ArrayList<String> statements = new ArrayList<>();
    private final String prefix;
    public X86Emitter(String funcLabelPrefix) {
        prefix = STATIC_LABEL_PREFIX + "_" + funcLabelPrefix + "_";
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
        move(a, b.x86(), (TypeNumerical) b.getType());
    }
    public void moveStr(X86Param a, String b) {
        move(a.x86(), b, (TypeNumerical) a.getType());
    }
    private void move(X86Param a, X86Param b, boolean typesCanBeDifferent) {
        if (!typesCanBeDifferent && !a.getType().equals(b.getType())) {
            throw new IllegalStateException(a + " " + b + " " + a.getType() + " " + b.getType());
        }
        if (a.getType().getSizeBytes() != b.getType().getSizeBytes()) {//honestly, there's so much sketchy code calling this that... whatever
            throw new IllegalStateException(a + " " + b + " " + a.getType() + " " + b.getType());
        }
        move(a.x86(), b.x86(), (TypeNumerical) a.getType());
    }
    public String alternative(String a, TypeNumerical type) {
        if (!a.startsWith(X86Register.REGISTER_PREFIX) && !a.startsWith("$")) {
            for (Pair<Type, HashSet<String>> eqq : equals) {
                HashSet<String> eq = eqq.getB();
                if (eq.contains(a) && type.equals(eqq.getA())) {
                    for (String alternative : eq) {
                        if (alternative.startsWith(X86Register.REGISTER_PREFIX)) {
                            return alternative;
                        }
                    }
                }
            }
        }
        return null;
    }
    HashSet<Pair<Type, HashSet<String>>> equals = new HashSet<>();
    private void move(String a, String b, TypeNumerical type) {
        //TODO modifying sections of a struct stored on a stack
        if (a.equals(b)) {
            if (compiler.Compiler.verbose()) {
                addComment("redundant move omitted: " + a + " to " + b);
            }
            return;
        }
        String moveStmt = "mov" + type.x86typesuffix() + " " + a + ", " + b;
        if (type instanceof TypeFloat) {
            addStatement(moveStmt);
            return;
        }
        if (a.equals("$0") && b.startsWith(X86Register.REGISTER_PREFIX)) {
            moveStmt = "xor" + type.x86typesuffix() + " " + b + ", " + b;
        }
        for (Pair<Type, HashSet<String>> eqq : equals) {
            HashSet<String> eq = eqq.getB();
            if (eq.contains(a) && eq.contains(b)) {
                if (compiler.Compiler.verbose()) {
                    addComment("SMART redundant because of previous statement");
                    //addComment(equals + "");
                    addComment("lmao" + eqq);
                    addComment(moveStmt);
                }
                return;//can return because this doesn't affect anything
            }
        }
        boolean replaced = false;
        String alt = alternative(a, type);
        if (alt != null) {
            if (compiler.Compiler.verbose()) {
                addComment("SMART Replacing move with more efficient one given previous move. Move was previously:");
                addComment(moveStmt);
                addComment("Move is now");
            }
            addStatement("mov" + type.x86typesuffix() + " " + alt + ", " + b);
            replaced = true;
        }
        if (b.startsWith(X86Register.REGISTER_PREFIX)) {
            X86TypedRegister bTReg = TACConst.sin(type, b);
            markRegisterDirty(bTReg.getRegister());
        }
        for (Pair<Type, HashSet<String>> cll : equals) {
            HashSet<String> cl = cll.getB();
            cl.remove(b);//assume nothing previously equal to b is now equal to b, because it was set to a
            if (cl.contains(a)) {//anything previously equal to a, is now equal to b (because b=a)
                cl.add(b);
            }
        }
        if (!a.contains(b)) {//"movq (%rax), %rax" doesn't tell us anything. it DOESN'T mean that (%rax) and %rax are equal after this statement
            //Note that "movq %rax, (%rax)" IS valid, and does mean that %rax is equal to (%rax)
            //that's why the condition is a.contains(b) not b.contains(a)
            HashSet<String> n = new HashSet<>();
            n.add(a);
            n.add(b);
            equals.add(new Pair<>(type, n));
        }
        if (!replaced) {
            addStatement(moveStmt);
        }
    }
    public void markRegisterDirty(X86Register reg) {
        for (TypeNumerical t : new TypeNumerical[]{new TypeInt8(), new TypeInt16(), new TypeInt32(), new TypeInt64()}) {
            markDirty(reg.getRegister(t).x86());
        }
    }
    public void markDirty(String version) {
        for (Pair<Type, HashSet<String>> cll : equals) {
            HashSet<String> cl = cll.getB();
            for (String str : new HashSet<>(cl)) {
                if (str.contains(version)) {
                    cl.remove(str);
                }
            }
        }
    }
    public void clearRegisters() {
        for (X86Register reg : X86Register.values()) {
            if (reg.name().contains("XMM")) {//oh my god why did I even add floating point support it just causes so many headaches and special cases UGH
                continue;
            }
            markRegisterDirty(reg);
        }
    }
    public void clearMoves() {
        equals = new HashSet<>();
    }
    public void cast(X86Param a, X86Param b) {
        TypeNumerical inp = (TypeNumerical) a.getType();
        TypeNumerical out = (TypeNumerical) b.getType();
        String cast = "movs" + inp.x86typesuffix() + "" + out.x86typesuffix() + " " + a.x86() + ", " + b.x86();
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
        if (b instanceof X86TypedRegister) {
            markRegisterDirty(((X86TypedRegister) b).getRegister());
        }
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
        clearMoves();//jump destination, anything could be anything
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
        return statements.stream().collect(Collectors.joining("\n", "", ""));
    }
    public String withoutComments() {
        return statements.stream().filter(x -> !x.startsWith("#")).collect(Collectors.joining("\n", "", ""));
    }
}
