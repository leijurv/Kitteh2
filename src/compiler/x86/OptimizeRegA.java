/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.x86;
import compiler.type.TypeNumerical;
import java.util.HashSet;
import java.util.List;

/**
 *
 * @author leijurv
 */
public class OptimizeRegA {
    public static void optimize(List<X86Statement> statements) {
        HashSet<Integer> toA = new HashSet<>();
        HashSet<Integer> fromA = new HashSet<>();
        HashSet<Integer> involvingA = new HashSet<>();
        for (int i = 0; i < statements.size(); i++) {
            if (statements.get(i) instanceof Comment || statements.get(i) instanceof Label) {
                continue;
            }
            if (statements.get(i) instanceof Move) {
                Move m = (Move) statements.get(i);
                if (m.getDest() instanceof X86TypedRegister && ((X86TypedRegister) m.getDest()).getRegister() == X86Register.A) {
                    toA.add(i);
                }
                if (m.getSource() instanceof X86TypedRegister && ((X86TypedRegister) m.getSource()).getRegister() == X86Register.A) {
                    fromA.add(i);
                }
            }
            if (statements.get(i) instanceof Cast) {
                Cast m = (Cast) statements.get(i);
                if (m.getDest() instanceof X86TypedRegister && ((X86TypedRegister) m.getDest()).getRegister() == X86Register.A) {
                    toA.add(i);
                }
                if (m.getSource() instanceof X86TypedRegister && ((X86TypedRegister) m.getSource()).getRegister() == X86Register.A) {
                    fromA.add(i);
                }
            }
            for (TypeNumerical type : TypeNumerical.INTEGER_TYPES) {
                if (statements.get(i).toString().contains(X86Register.A.getRegister1(type, true))) {
                    involvingA.add(i);
                }
            }
        }
        involvingA.addAll(toA);
        involvingA.addAll(fromA);
        for (int i = 0; i < statements.size(); i++) {
            if (toA.contains(i)) {
                X86Param origSource = (statements.get(i) instanceof Move) ? ((Move) statements.get(i)).getSource() : ((Cast) statements.get(i)).getSource();
                for (int j = i + 1; j < statements.size(); j++) {
                    if (statements.get(j).toString().startsWith("j")) {
                        break;
                    }
                    if (statements.get(j) instanceof Label) {
                        break;
                    }
                    if (statements.get(j).toString().startsWith("idiv") || statements.get(j).toString().startsWith("call")) {
                        break;
                    }
                    if (fromA.contains(j)) {
                        X86Param dest = (statements.get(j) instanceof Move) ? ((Move) statements.get(j)).getDest() : ((Cast) statements.get(j)).getDest();
                        if (!(dest instanceof X86TypedRegister)) {
                            break;
                        }
                        // X86Register repl = ((X86TypedRegister) dest).getRegister();
                        for (int k = j + 1; k < statements.size(); k++) {
                            if (statements.get(k) instanceof Label) {
                                break;
                            }
                            if (statements.get(k) instanceof Comment) {
                                continue;
                            }
                            String str = statements.get(k).toString();
                            if (str.contains("(%rax)") || str.contains("cltq")) {
                                break;
                            }
                            if (toA.contains(k) || str.startsWith("call")) {
                                /*System.out.println("found");
                                System.out.println();
                                System.out.println(statements.subList(i, j + 1).stream().map(X86Statement::x86).collect(Collectors.joining("\n")));
                                System.out.println();
                                System.out.println(statements.subList(i, k + 1).stream().map(X86Statement::x86).collect(Collectors.joining("\n")));
                                System.out.println();
                                System.out.println("end");*/
                                X86Statement n;
                                if (origSource.getType().equals(dest.getType()) || origSource instanceof X86Const) {
                                    n = new Move(origSource, dest);
                                } else {
                                    n = new Cast(origSource, dest);
                                }
                                statements.set(j, new Comment("AReplacement " + (origSource.getType().equals(dest.getType())) + " " + statements.get(i) + " -> " + n + " allowed to remove " + statements.get(j) + " because of " + statements.get(k)));
                                statements.set(i, n);
                                break;
                            }
                            if (involvingA.contains(k)) {
                                break;
                            }
                            if (str.startsWith("j")) {
                                break;
                            }
                        }
                        break;
                    }
                    if (involvingA.contains(j)) {
                        break;
                    }
                }
            }
        }
    }
}
