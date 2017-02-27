/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.x86;
import compiler.type.*;
import compiler.util.Obfuscator;
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
    public X86TypedRegister putInRegister(X86Param source, TypeNumerical type) {
        return putInRegister(source, type, X86Register.C);
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
        if (!(a instanceof X86TypedRegister) && !(a instanceof X86Const)) {
            for (HashSet<X86Param> eqq : equals) {
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
    HashSet<HashSet<X86Param>> equals = new HashSet<>();
    private void move(String a, String b, TypeNumerical type) {
        addComment("this is dumb");
        String moveStmt = "mov" + type.x86typesuffix() + " " + a + ", " + b;
        addStatement(moveStmt);
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
        if (a.x86().equals("$0") && b instanceof X86TypedRegister) {
            moveStmt = "xor" + type.x86typesuffix() + " " + b.x86() + ", " + b.x86();
        }
        for (HashSet<X86Param> eqq : equals) {
            if (eqq.contains(a) && eqq.contains(b)) {
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
        if (b instanceof X86TypedRegister) {
            markRegisterDirty(((X86TypedRegister) b).getRegister());
        }
        for (HashSet<X86Param> cll : equals) {
            cll.remove(b);//assume nothing previously equal to b is now equal to b, because it was set to a
            if (cll.contains(a)) {//anything previously equal to a, is now equal to b (because b=a)
                cll.add(b);
            }
        }
        //TODO dont use .contains on the x86, actually parse memory
        //TODO also, modifying %ax should also mess up -5(%rax)
        if (!a.x86().contains(b.x86())) {//"movq (%rax), %rax" doesn't tell us anything. it DOESN'T mean that (%rax) and %rax are equal after this statement
            //Note that "movq %rax, (%rax)" IS valid, and does mean that %rax is equal to (%rax)
            //that's why the condition is a.contains(b) not b.contains(a)
            HashSet<X86Param> n = new HashSet<>();
            n.add(a);
            n.add(b);
            equals.add(n);
        }
        if (!replaced) {
            addStatement(moveStmt);
        }
    }
    public void markRegisterDirty(X86Register reg) {
        if (reg == X86Register.XMM0 || reg == X86Register.XMM1) {
            return;
        }
        if (reg == X86Register.SP || reg == X86Register.BP) {
            throw new IllegalStateException();
        }
        for (TypeNumerical t : new TypeNumerical[]{new TypeInt8(), new TypeInt16(), new TypeInt32(), new TypeInt64()}) {
            markDirty(reg.getRegister(t).x86());
        }
    }
    public void markDirty(X86Param param) {
        if (param instanceof X86TypedRegister) {
            throw new IllegalStateException("wait what");
        }
        if (param instanceof X86Memory) {
            //woohoo, this is the special case I have been dreaming of
            X86Memory xm = (X86Memory) param;
            for (int off = xm.offset; off < xm.offset + xm.getType().getSizeBytes(); off++) {
                //if we movq into 5(%rax), that corrupts EIGHT bytes
                //this comes up often when moving structs, temp variables, etc
                X86Memory thisByte = new X86Memory(off, xm.reg, new TypeInt8());
                markDirty(thisByte.x86());//avoid infinite recursion, do .x86   =)
            }
        }
        markDirty(param.x86());
    }
    private void markDirty(String version) {
        for (HashSet<X86Param> cll : equals) {
            for (X86Param str : new HashSet<>(cll)) {
                if (str.x86().contains(version)) {
                    cll.remove(str);
                }
            }
        }
    }
    public void clearRegisters() {
        for (X86Register reg : X86Register.values()) {
            if (reg.name().contains("XMM")) {//oh my god why did I even add floating point support it just causes so many headaches and special cases UGH
                continue;
            }
            if (reg == X86Register.BP || reg == X86Register.SP) {
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
        } else {
            markDirty(b);
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
        return statements.stream().collect(Collectors.joining("\n"));
    }
    public String withoutComments() {
        return statements.stream().filter(x -> !x.startsWith("#")).collect(Collectors.joining("\n"));
    }
}
