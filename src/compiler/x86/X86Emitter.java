/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.x86;
import compiler.type.TypeFloat;
import compiler.type.TypeNumerical;
import compiler.util.Obfuscator;
import java.util.ArrayList;
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
    private final ArrayList<X86Statement> statements = new ArrayList<>();
    private final String prefix;
    private final X86Function func;
    public final DataFlowAnalysis dfa;
    public X86Emitter(String funcLabelPrefix, X86Function func) {
        prefix = STATIC_LABEL_PREFIX + "_" + funcLabelPrefix + "_";
        this.func = func;
        this.dfa = new DataFlowAnalysis(this);
    }
    public X86Emitter() {
        this("", null);
    }
    public X86Emitter(X86Emitter other) {
        this.dfa = new DataFlowAnalysis(other.dfa, this);
        this.func = null;
        this.prefix = "";
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
    public X86TypedRegister regUp(X86TypedRegister source) {
        List<X86Param> al = dfa.rawAlt(source, true);
        if (al.isEmpty()) {
            return (X86TypedRegister) source;
        }
        //System.out.println(source + " could be " + al);
        for (int i = statements.size() - 1; i >= 0; i--) {
            X86Statement stmt = statements.get(i);
            if (stmt instanceof Comment) {
                continue;
            }
            if (stmt instanceof Move) {
                Move m = (Move) stmt;
                if (m.getDest().equals(source) && al.contains(m.getSource())) {//this works because all it's doing is using a janky heuristic to choose an element of al, all of which are perfectly valid
                    if (compiler.Compiler.verbose()) {
                        addComment("Using register " + m.getSource().x86() + " instead of " + source.x86() + " because of previous move");
                    }
                    return (X86TypedRegister) m.getSource();
                }
            }
        }
        return (X86TypedRegister) source;
    }
    public X86TypedRegister putInRegister(X86Param source, X86Register desired) {
        if (source instanceof X86TypedRegister) {
            return regUp((X86TypedRegister) source);
        }
        X86TypedRegister loc = desired.getRegister((TypeNumerical) source.getType());
        if (source instanceof X86Const) {
            throw new IllegalStateException();
        }
        X86Param alt = dfa.alternative(source, true);
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
        X86Statement moveStmt = new Move(a, b);
        if (type instanceof TypeFloat) {
            statements.add(moveStmt);
            return;
        }
        Optional<HashSet<X86Param>> eq = dfa.redundancy(a, b);
        if (eq.isPresent()) {
            if (compiler.Compiler.verbose()) {
                addComment("SMART redundant because of previous statement");
                addComment("equivalence " + eq.get());
                addComment(moveStmt.toString());
            }
            return;//can return because this doesn't affect anything
        }
        boolean replaced = false;
        X86Param alt = dfa.alternative(a, false);
        if (alt != null) {
            if (compiler.Compiler.verbose()) {
                addComment("SMART Replacing move with more efficient one given previous move. Move was previously:");
                addComment(moveStmt.toString());
                addComment("Move is now");
            }
            statements.add(new Move(alt, b));
            replaced = true;
        }
        dfa.knownEqual(a, b);
        if (!replaced) {
            statements.add(moveStmt);
        }
    }
    public Map<String, X86Function> map() {
        return func.getMap();
    }
    public void cast(X86Param a, X86Param b) {
        statements.add(new Cast(a, b));
        dfa.markDirty(b);
        if (a.getType() instanceof TypeFloat || b.getType() instanceof TypeFloat) {
            return;
        }
        //the lower part of b is now equal to a
        if (b instanceof X86TypedRegister) {
            X86TypedRegister lowerB = ((X86TypedRegister) b).getRegister().getRegister((TypeNumerical) a.getType());
            dfa.knownEqual(a, lowerB);
        } else {
            throw new IllegalStateException(a + " " + b);//apparently this doesn't happen
        }
    }
    public void addStatement(String ssnek) {
        if (ssnek.contains("#") || ssnek.contains(":")) {
            throw new IllegalStateException();
        }
        statements.add(new Other(ssnek));
        if (ssnek.startsWith("movs") && !ssnek.startsWith("movss")) {
            throw new IllegalStateException();
        }
    }
    public void addLabel(String lbl) {
        dfa.clear();//jump destination, anything could be anything
        //TODO merge known equalities from jump sources and previous line
        statements.add(new Label(lbl));
    }
    public void addAlignedComment(String cmt) {
        statements.add(new Comment(cmt));
        //for debugging the equality
        //statements.add("#" + equals);
    }
    public void addComment(String cmt) {
        statements.add(new Comment("    " + cmt));
    }
    public String lineToLabel(int line) {
        String rsp = prefix + line;
        if (!compiler.Main.ALLOW_CLI || compiler.Compiler.obfuscate()) {
            rsp = "_" + Obfuscator.obfuscate(rsp);
        }
        return rsp;
    }
    public String toX86() {
        OptimizeRegD.optimize(statements);
        //note: OptimizeRegA has been removed from master because of how sketchy it is, and how it didn't provide a measurable performance boost. For more info, see the optimizerega branch where it's still present.
        //OptimizeRegA.optimize(statements);
        //OptimizeRegA.optimize(statements);//two passes is enough to replace all normal patterns
        //OptimizeRegA.optimize(statements);//add in a third just in case =)
        return statements.stream().filter(x -> !(x instanceof Comment) || compiler.Compiler.verbose()).map(X86Statement::x86).collect(Collectors.joining("\n"));
    }
    public String withoutComments() {
        return statements.stream().filter(x -> !(x instanceof Comment)).map(X86Statement::x86).collect(Collectors.joining("\n"));
    }
}
