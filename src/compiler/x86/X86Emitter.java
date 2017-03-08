/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.x86;
import compiler.type.Type;
import compiler.type.TypeFloat;
import compiler.type.TypeInt16;
import compiler.type.TypeInt32;
import compiler.type.TypeInt64;
import compiler.type.TypeInt8;
import compiler.type.TypeNumerical;
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
    private final ArrayList<X86Statement> statements = new ArrayList<>();
    private final String prefix;
    private final X86Function func;
    HashSet<HashSet<X86Param>> equals = new HashSet<>();
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
        for (HashSet<X86Param> e : other.equals) {//deep copy
            equals.add(new HashSet<>(e));
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
    public X86TypedRegister regUp(X86TypedRegister source) {
        List<X86Param> al = rawAlt(source, (TypeNumerical) source.getType(), true);
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
    public X86TypedRegister putInRegister(X86Param source, TypeNumerical type, X86Register desired) {
        if (source instanceof X86TypedRegister) {
            return regUp((X86TypedRegister) source);
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
            List<X86Param> al = rawAlt(a, type, onlyReg);
            if (!al.isEmpty()) {
                for (X86Param p : al) {
                    if (p instanceof X86TypedRegister) {
                        return regUp((X86TypedRegister) p);
                    }
                }
                return al.get(0);
            }
        }
        if (a instanceof X86TypedRegister) {
            X86TypedRegister c = regUp((X86TypedRegister) a);
            if (!c.x86().equals(a.x86())) {
                return c;
            }
        }
        return null;
    }
    private List<X86Param> rawAlt(X86Param a, TypeNumerical type, boolean onlyReg) {
        List<X86Param> al = new ArrayList<>();
        for (HashSet<X86Param> eqq : equals) {
            if (eqq.contains(a)) {
                for (X86Param alternative : eqq) {
                    if (alternative instanceof X86TypedRegister || (!onlyReg && alternative instanceof X86Const)) {
                        Type alt = alternative.getType();
                        if (alt.getSizeBytes() != type.getSizeBytes()) {
                            if (alternative instanceof X86Const) {
                                al.add(new X86Const(((X86Const) alternative).getValue(), type));//just fix the type
                                continue;
                            }
                            if (alt.getSizeBytes() > type.getSizeBytes()) {
                                //we're looking for equal to an int, but a long has the same value
                                //if we take the lower part of the alternative, that should be equal to what we're looking for
                                al.add(((X86TypedRegister) alternative).getRegister().getRegister(type));
                                continue;
                            }
                            //the alternative must be smaller
                            //it doesn't have enough information
                            continue;
                            //throw new IllegalStateException(eqq + "" + alternative.getType() + " " + type);
                        }
                        /*if (!type.equals(alternative.getType()) && compiler.Compiler.verbose()) {
                            addComment("whoa type is different " + type + " " + eqq);
                        }*/
                        al.add(alternative);
                    }
                }
            }
        }
        return al;
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
        X86Statement moveStmt = new Move(a, b);
        if (type instanceof TypeFloat) {
            statements.add(moveStmt);
            return;
        }
        Optional<HashSet<X86Param>> eq = equals.stream().filter(x -> x.contains(a)).filter(x -> x.contains(b)).findAny();
        if (eq.isPresent()) {
            if (compiler.Compiler.verbose()) {
                addComment("SMART redundant because of previous statement");
                addComment("lmao" + eq.get());
                addComment(moveStmt.toString());
            }
            return;//can return because this doesn't affect anything
        }
        boolean replaced = false;
        X86Param alt = alternative(a, type, false);
        if (alt != null) {
            if (compiler.Compiler.verbose()) {
                addComment("SMART Replacing move with more efficient one given previous move. Move was previously:");
                addComment(moveStmt.toString());
                addComment("Move is now");
            }
            statements.add(new Move(alt, b));
            replaced = true;
        }
        knownEqual(a, b);
        if (!replaced) {
            statements.add(moveStmt);
        }
    }
    private void knownEqual(X86Param a, X86Param b) {
        if (a.getType().getSizeBytes() != b.getType().getSizeBytes()) {
            throw new IllegalStateException(a + " " + b + " " + a.getType() + " " + b.getType());
        }
        //if b is a register, its not enough to just remove b. If b is %eax, we also need to clear things like 5(%rax)
        markDirty(b);//assume nothing previously equal to b is now equal to b, because it was set to a
        equals.stream().filter(cl -> cl.contains(a)).forEach(cl -> cl.add(b));//anything previously equal to a, is now equal to b (because b=a)
        if (a instanceof X86Memory && b instanceof X86TypedRegister && ((X86Memory) a).reg == ((X86TypedRegister) b).getRegister()) {
            //"movq (%rax), %rax" doesn't tell us anything. it DOESN'T mean that (%rax) and %rax are equal after this statement
            if (compiler.Compiler.verbose()) {
                addComment("no information gleaned from " + a + " -> " + b);
            }
        } else {
            //Note that "movq %rax, (%rax)" IS valid, and does mean that %rax is equal to (%rax)
            //that's why the condition is a.contains(b) not b.contains(a)
            equals.add(new HashSet<>(Arrays.asList(a, b)));
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
            overlapped.forEach(bad -> {
                if (compiler.Compiler.verbose() && !bad.x86().equals(param.x86())) {
                    addComment(bad + " overlaps with " + param);
                }
                markDirty(bad.x86());
            });
        }
        markDirty(param.x86());
    }
    private void markDirty(String version) {
        equals.forEach(cll -> cll.removeIf(x -> x.x86().contains(version)));
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
        statements.add(new Cast(a, b));
        markDirty(b);
        if (a.getType() instanceof TypeFloat || b.getType() instanceof TypeFloat) {
            return;
        }
        //the lower part of b is now equal to a
        if (b instanceof X86TypedRegister) {
            X86TypedRegister lowerB = ((X86TypedRegister) b).getRegister().getRegister((TypeNumerical) a.getType());
            knownEqual(a, lowerB);
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
        equals = new HashSet<>();//jump destination, anything could be anything
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
    /*public void backwardsPass(int st) {
        ArrayList<Integer> stackLocationsUsed = new ArrayList<>();
        ArrayList<X86Register> registersUsed = new ArrayList<>();
        registersUsed.add(X86Register.A);
        registersUsed.add(X86Register.C);
        for (int i = st - 1; i >= 0; i--) {
            X86Statement s = statements.get(i);
            if (s instanceof Move) {
                Move m = (Move) s;
                if (m.dest instanceof X86Memory) {
                    X86Memory d = (X86Memory) m.dest;
                    if (d.reg == X86Register.BP) {
                        if (!stackLocationsUsed.contains(d.offset)) {
                            System.out.println("Maybe not needed " + m);
                        }
                    }
                }
                if (m.dest instanceof X86TypedRegister) {
                    if (!registersUsed.contains(((X86TypedRegister) m.dest).getRegister())) {
                        System.out.println("Maybe not needed " + m);
                        continue;
                    }
                }
                if (m.source instanceof X86Memory) {
                    X86Memory d = (X86Memory) m.source;
                    if (d.reg == X86Register.BP) {
                        stackLocationsUsed.add(d.offset);
                    }
                }
                if (m.source instanceof X86TypedRegister) {
                    registersUsed.add(((X86TypedRegister) m.source).getRegister());
                }
                for (X86Register r : new X86Register[]{A, B, C, D, SI, DI, R8, R9, R10, R11, R12, R13, R14, R15}) {//forgive me father, for i have sinned
                    for (TypeNumerical type : new TypeNumerical[]{new TypeInt8(), new TypeInt16(), new TypeInt32(), new TypeInt64()}) {
                        if (s.toString().contains(r.getRegister1(type, true))) {
                            registersUsed.add(r);
                        }
                    }
                }
                continue;
            }
            String t = s.toString();
            if (t.startsWith("j")) {
                return;
            }
            if (t.startsWith("popq")) {
                continue;
            }
            if (t.startsWith("callq") && (t.contains("malloc") || t.contains("free"))) {
                registersUsed.add(DI);
            }
            if (t.equals("syscall")) {
                registersUsed.addAll(TACFunctionCall.SYSCALL_REGISTERS);
            }
            //System.out.println(t);
            if (t.contains("%rbp")) {
                //System.out.println(t);
                String a = t.split("%rbp")[0];
                String b = a.split(" ")[a.split(" ").length - 1];
                int c = Integer.parseInt(b.substring(0, b.length() - 1));
                stackLocationsUsed.add(c);
            }
            for (X86Register r : new X86Register[]{A, B, C, D, SI, DI, R8, R9, R10, R11, R12, R13, R14, R15}) {//forgive me father, for i have sinned
                for (TypeNumerical type : new TypeNumerical[]{new TypeInt8(), new TypeInt16(), new TypeInt32(), new TypeInt64()}) {
                    if (t.contains(r.getRegister1(type, true))) {
                        registersUsed.add(r);
                    }
                }
            }
        }
    }*/
    public boolean doTheThing(int pos) {
        for (int j = pos + 1; j < statements.size(); j++) {
            if (statements.get(j) instanceof Label) {
                /*System.out.println("Removing " + statements.get(pos));
                System.out.println("After " + statements.get(pos - 1));
                System.out.println("Before " + statements.get(pos + 1));*/
                return true;
            }
            if (statements.get(j) instanceof Comment) {
                continue;
            }
            if (statements.get(j).toString().contains("syscall")) {
                return false;
            }
            for (TypeNumerical type : new TypeNumerical[]{new TypeInt8(), new TypeInt16(), new TypeInt32(), new TypeInt64()}) {
                if (statements.get(j).toString().contains(X86Register.D.getRegister1(type, true))) {
                    return false;
                }
            }
        }
        return true;
    }
    public String toX86() {
        for (int i = 0; i < statements.size(); i++) {
            if (statements.get(i) instanceof Move) {
                Move m = (Move) statements.get(i);
                if (m.getDest() instanceof X86TypedRegister && ((X86TypedRegister) m.getDest()).getRegister() == X86Register.D && doTheThing(i)) {
                    if (compiler.Compiler.verbose()) {
                        statements.set(i, new Comment("REMOVED BECAUSE REDUNDANT " + statements.get(i)));
                    } else {
                        statements.remove(i);
                    }
                    i = -1;
                }
            }
        }
        return statements.stream().map(X86Statement::x86).collect(Collectors.joining("\n"));
    }
    public String withoutComments() {
        return statements.stream().filter(x -> !(x instanceof Comment)).map(X86Statement::x86).collect(Collectors.joining("\n"));
    }
}
