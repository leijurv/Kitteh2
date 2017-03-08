/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.x86;
import compiler.type.Type;
import compiler.type.TypeInt64;
import compiler.type.TypeNumerical;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 *
 * @author leijurv
 */
public class DataFlowAnalysis {
    private HashSet<HashSet<X86Param>> equals = new HashSet<>();
    private final X86Emitter emit;
    public DataFlowAnalysis(X86Emitter emit) {
        this.emit = emit;
    }
    public DataFlowAnalysis(DataFlowAnalysis other, X86Emitter emit) {
        this(emit);
        equals = new HashSet<>();
        other.equals.stream().map(HashSet::new).forEach(equals::add);
    }
    public List<X86Param> rawAlt(X86Param a, boolean onlyReg) {
        TypeNumerical type = (TypeNumerical) a.getType();
        List<X86Param> al = new ArrayList<>();
        equals.stream().filter(eqq -> eqq.contains(a)).flatMap(HashSet::stream).filter(alt -> alt instanceof X86TypedRegister || (!onlyReg && alt instanceof X86Const)).forEach(alternative -> {
            Type alt = alternative.getType();
            if (alt.getSizeBytes() != type.getSizeBytes()) {
                if (alternative instanceof X86Const) {
                    al.add(new X86Const(((X86Const) alternative).getValue(), type));//just fix the type
                    return;
                }
                if (alt.getSizeBytes() > type.getSizeBytes()) {
                    //we're looking for equal to an int, but a long has the same value
                    //if we take the lower part of the alternative, that should be equal to what we're looking for
                    al.add(((X86TypedRegister) alternative).getRegister().getRegister(type));
                    return;
                }
                //the alternative must be smaller
                //it doesn't have enough information
                return;
                //throw new IllegalStateException(eqq + "" + alternative.getType() + " " + type);
            }
            /*if (!type.equals(alternative.getType()) && compiler.Compiler.verbose()) {
                            addComment("whoa type is different " + type + " " + eqq);
                        }*/
            al.add(alternative);
        });
        return al;
    }
    public Optional<HashSet<X86Param>> redundancy(X86Param a, X86Param b) {
        return equals.stream().filter(x -> x.contains(a)).filter(x -> x.contains(b)).findAny();
    }
    public X86Param alternative(X86Param a, boolean onlyReg) {
        if (!(a instanceof X86TypedRegister) && !(a instanceof X86Const)) {
            List<X86Param> al = rawAlt(a, onlyReg);
            if (!al.isEmpty()) {
                for (X86Param p : al) {
                    if (p instanceof X86TypedRegister) {
                        return emit.regUp((X86TypedRegister) p);
                    }
                }
                return al.get(0);
            }
        }
        if (a instanceof X86TypedRegister) {
            X86TypedRegister c = emit.regUp((X86TypedRegister) a);
            if (!c.x86().equals(a.x86())) {
                return c;
            }
        }
        return null;
    }
    public void knownEqual(X86Param a, X86Param b) {
        if (a.getType().getSizeBytes() != b.getType().getSizeBytes()) {
            throw new IllegalStateException(a + " " + b + " " + a.getType() + " " + b.getType());
        }
        //if b is a register, its not enough to just remove b. If b is %eax, we also need to clear things like 5(%rax)
        markDirty(b);//assume nothing previously equal to b is now equal to b, because it was set to a
        equals.stream().filter(cl -> cl.contains(a)).forEach(cl -> cl.add(b));//anything previously equal to a, is now equal to b (because b=a)
        if (a instanceof X86Memory && b instanceof X86TypedRegister && ((X86Memory) a).reg == ((X86TypedRegister) b).getRegister()) {
            //"movq (%rax), %rax" doesn't tell us anything. it DOESN'T mean that (%rax) and %rax are equal after this statement
            if (compiler.Compiler.verbose()) {
                emit.addComment("no information gleaned from " + a + " -> " + b);
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
        for (TypeNumerical t : TypeNumerical.INTEGER_TYPES) {
            markDirty(reg.getRegister(t).x86());
        }
    }
    public void markDirty(X86Param param) {
        if (param instanceof X86Const) {
            throw new IllegalArgumentException(param + "");
        }
        if (param instanceof X86TypedRegister) {
            markRegisterDirty(((X86TypedRegister) param).getRegister());
        } else if (param instanceof X86Memory) {
            //woohoo, this is the special case I have been dreaming of
            //if we movq into 5(%rax), that corrupts EIGHT bytes
            //this comes up often when moving structs, temp variables, etc
            List<X86Param> overlapped = equals.stream().flatMap(HashSet::stream).filter(((X86Memory) param)::overlap).collect(Collectors.toList());
            //addComment(param + " overlaps into " + overlapped);
            overlapped.forEach(bad -> {
                if (compiler.Compiler.verbose() && !bad.x86().equals(param.x86())) {
                    emit.addComment(bad + " overlaps with " + param);
                }
                markDirty(bad.x86());
            });
            markDirty(param.x86());
        } else {
            markDirty(param.x86());
        }
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
    public void clear() {
        equals = new HashSet<>();
    }
}
