/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.x86;
import compiler.type.Type;
import compiler.type.TypeNumerical;
import compiler.util.Obfuscator;
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
        if (a instanceof X86Const && a.x86().equals("$0") && b instanceof X86TypedRegister) {
            addStatement("xor" + ((TypeNumerical) a.getType()).x86typesuffix() + " " + b.x86() + ", " + b.x86());
            return;
        }
        move(a.x86(), b.x86(), (TypeNumerical) a.getType());
    }
    String prevMove1 = null;//TODO keep track more than 1 mov in the past, and actually figure out what instructions modify what registers
    String prevMove2 = null;
    Type prevType = null;
    private void move(String a, String b, TypeNumerical type) {
        if (a.equals(b)) {
            if (compiler.Compiler.verbose()) {
                addComment("redundant move omitted: " + a + " to " + b);
            }
            return;
        }
        String moveStmt = "mov" + type.x86typesuffix() + " " + a + ", " + b;
        if (prevMove1 != null) {
            if ((a.equals(prevMove2) && b.equals(prevMove1)) || (a.equals(prevMove1) && b.equals(prevMove2) && !b.contains(a) && !a.contains(b))) {
                //they can't contain each other, because of cases like "movq (%rax), %rax" repeated twice
                if (compiler.Compiler.verbose()) {
                    addComment("redundant because of previous statement");
                    addComment(moveStmt);
                }
                prevMove1 = null;
                prevMove2 = null;
                prevType = null;
                return;
            }
            if (type.equals(prevType) && !a.startsWith(X86Register.REGISTER_PREFIX) && ((a.equals(prevMove1) && prevMove2.startsWith(X86Register.REGISTER_PREFIX) && !a.contains(prevMove2)) || (a.equals(prevMove2) && prevMove1.startsWith(X86Register.REGISTER_PREFIX) && !a.contains(prevMove1)))) {
                //^^ lots of edge cases that that prevents against. e.g.
                //    movq 9(%rax), %rax
                //    movq 9(%rax), %rax
                // ^ that code fragment is generated when all optimizations are off in linkedSort.k when it does .next.next
                //the second move there is NOT redundant, while this condition used to think it was
                if (compiler.Compiler.verbose()) {
                    addComment("Replacing move with more efficient one given previous move. Move was previously:");
                    addComment(moveStmt);
                    addComment("Move is now");
                }
                addStatement("mov" + type.x86typesuffix() + " " + (a.equals(prevMove2) ? prevMove1 : prevMove2) + ", " + b);
                prevMove1 = null;
                prevMove2 = null;
                prevType = null;
                return;
            }
        }
        addStatement(moveStmt);
        prevMove1 = a;
        prevMove2 = b;
        prevType = type;
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
        addStatement(cast);
    }
    public void addStatement(String ssnek) {
        if (ssnek.contains("#") || ssnek.contains(":")) {
            throw new IllegalStateException();
        }
        statements.add("    " + ssnek);
        if (!ssnek.equals("")) {
            prevMove1 = null;
            prevMove2 = null;
            prevType = null;
        }
    }
    public void addLabel(String lbl) {
        statements.add(lbl + ":");
    }
    public void addAlignedComment(String cmt) {
        statements.add("#" + cmt);
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
