/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.x86;
import compiler.type.TypeNumerical;
import java.util.ArrayList;
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
    private void move(String a, String b, TypeNumerical type) {
        addStatement("mov" + type.x86typesuffix() + " " + a + ", " + b);
    }
    public void cast(X86Param a, X86Param b) {
        TypeNumerical inp = (TypeNumerical) a.getType();
        TypeNumerical out = (TypeNumerical) b.getType();
        addStatement("movs" + inp.x86typesuffix() + "" + out.x86typesuffix() + " " + a.x86() + ", " + b.x86());
    }
    public void addStatement(String ssnek) {
        if (ssnek.contains("#") || ssnek.contains(":")) {
            throw new IllegalStateException();
        }
        statements.add("    " + ssnek);
    }
    public void addLabel(String lbl) {
        statements.add(lbl + ":");
    }
    public void addComment(String cmt) {
        statements.add("#" + cmt);
    }
    public String lineToLabel(int line) {
        return prefix + line;
    }
    public String toX86() {
        return statements.stream().collect(Collectors.joining("\n", "", ""));
    }
}
