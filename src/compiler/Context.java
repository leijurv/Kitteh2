/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler;
import compiler.command.CommandDefineFunction;
import compiler.command.CommandDefineFunction.FunctionHeader;
import compiler.command.FunctionsContext;
import compiler.expression.ExpressionConst;
import compiler.tac.TempVarUsage;
import compiler.type.Type;
import compiler.type.TypeStruct;
import compiler.util.MutInt;
import compiler.util.Pair;
import compiler.x86.X86Param;
import java.awt.dnd.InvalidDnDOperationException;
import java.nio.channels.OverlappingFileLockException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Sorta a symbol table that also deals with scoping and temp variables that
 * overlap on the stack
 *
 * @author leijurv
 */
public class Context {//TODO split off some of this massive functionality into other classes, this one is getting a little godlike =)
    public boolean printFull = true;

    public class VarInfo implements X86Param {
        private final String name;
        private final Type type;
        private volatile ExpressionConst knownValue;
        private final int stackLocation;
        private final boolean secret;
        public VarInfo(String name, Type type, int stackLocation) {
            this(name, type, stackLocation, false);
        }
        public VarInfo(String name, Type type, int stackLocation, boolean secret) {
            this.name = name;
            this.type = type;
            this.stackLocation = stackLocation;
            this.secret = secret;
        }
        @Override
        public String toString() {
            //return ("{name: " + name + ", type: " + type + ", location: " + stackLocation + ", val: " + knownValue + "}");
            if (printFull) {
                return ("{name: " + name + ", type: " + type + ", location: " + stackLocation + "}");
            } else {
                return name;
            }
            //return ("{type: " + type + ", location: " + stackLocation + "}");
        }
        @Override
        public Type getType() {
            return type;
        }
        public int getStackLocation() {
            return stackLocation;
        }
        public String getName() {
            return name;
        }
        @Override
        public String x86() {
            if (secret) {
                throw new RuntimeException(Context.super.toString());
            }
            return (stackLocation) + ("(%rbp)");
        }
        public Context getContext() {
            if (secret) {
                throw new RuntimeException();
            }
            return Context.this;
        }
    }
    public final HashMap<String, String> imports;
    public final String packageName;
    private final HashMap<String, X86Param>[] values;
    private final HashMap<String, TypeStruct> structs;
    private int stackSize;
    private Integer additionalSizeTemp = null;
    private TempVarUsage currentTempVarUsage = null;
    private CommandDefineFunction currentFunction = null;
    public FunctionsContext gc;
    public final MutInt varIndex;
    public Context(String packageName) {
        this.values = createThatGenericArray(new HashMap<>());
        this.stackSize = 0;
        this.structs = new HashMap<>();
        this.varIndex = null;
        this.imports = new HashMap<>();
        this.packageName = packageName;
        String wewlad = packageName.substring(packageName.lastIndexOf('/') + 1).split("\\.k")[0];
        imports.put(wewlad, wewlad);
    }
    @SafeVarargs
    public static <T> T createThatGenericArray (T... inp)
        []{
        return inp;
    }
    public HashMap<String, TypeStruct> structsCopy() {
        return new HashMap<>(structs);
    }
    public void insertStructsUnderPackage(String alias, HashMap<String, TypeStruct> other) {
        Map<String, TypeStruct> mapt = other.entrySet().stream().map(entry -> new Pair<>((alias == null ? "" : alias + "::") + entry.getKey(), entry.getValue())).collect(Collectors.groupingBy(Pair::getA, Collectors.mapping(Pair::getB, Collectors.reducing(null, (a, b) -> b))));
        if (mapt.keySet().stream().anyMatch(structs::containsKey)) {
            throw new RuntimeException("Overwriting struct from " + mapt.keySet() + " into " + structs.keySet());
        }
        structs.putAll(mapt);
        //System.out.println(packageName + " " + structs);
    }
    public String reverseAlias(String alias) {
        Optional<String> reversed = imports.entrySet().stream().filter(entry -> alias.equals(entry.getValue())).map(Map.Entry::getKey).findAny();
        if (!reversed.isPresent()) {
            throw new RuntimeException(imports + " " + alias);
        }
        return reversed.get();
    }
    public void addImport(String fileName, String alias) {
        if (packageName == null) {
            throw new RuntimeException("This no longer can happen");
        }
        if (packageName.equals(fileName) || packageName.equals(alias)) {
            throw new RuntimeException("no " + fileName + " " + alias + " " + packageName);
        }
        if (imports.values().contains(alias) && alias != null) {
            throw new IllegalStateException("Already imported under alias " + alias);
        }
        if (imports.containsKey(fileName)) {
            throw new IllegalStateException("Already imported " + fileName);
        }
        imports.put(fileName, alias);
    }
    public void defineStruct(TypeStruct struct) {
        if (structs.containsKey(struct.getName())) {
            throw new InvalidDnDOperationException();
        }
        if (!isTopLevel()) {
            throw new OverlappingFileLockException();
        }
        structs.put(struct.getName(), struct);
    }
    public TypeStruct getStruct(String name) {
        return structs.get(name);
    }
    public void setCurrFunc(CommandDefineFunction cdf) {
        this.currentFunction = cdf;
    }
    public FunctionHeader getCurrentFunction() {
        return currentFunction.getHeader();
    }
    public TempVarUsage getTempVarUsage() {
        if (currentTempVarUsage == null) {
            throw new IllegalStateException("Unable to add int and boolean on line 7");//lol
        }
        return currentTempVarUsage;
    }
    public boolean isTopLevel() {
        if (values.length == 1) {
            if (getTotalStackSize() != 0) {
                throw new SecurityException();
            }
            if (gc != null) {
                throw new SecurityException();
            }
            return true;
        }
        if (gc == null) {
            throw new SecurityException();
        }
        return false;
    }
    public void setTempVarUsage(TempVarUsage curr) {
        if (curr == null) {
            Stream s = Stream.of(new String[]{});
            s.count();
            s.count();//this causes an exception
        }
        this.currentTempVarUsage = curr;
    }
    public int getTotalStackSize() {
        return stackSize + (additionalSizeTemp == null ? 0 : additionalSizeTemp);
    }
    public int getNonTempStackSize() {
        return stackSize;
    }
    public void updateMinAdditionalSizeTemp(int tempSize) {
        if (additionalSizeTemp == null) {
            additionalSizeTemp = tempSize;
        } else {
            additionalSizeTemp = Math.min(additionalSizeTemp, tempSize);
        }
    }
    private Context(HashMap<String, X86Param>[] values, int stackSize, FunctionsContext gc, HashMap<String, TypeStruct> structs, CommandDefineFunction currentFunction, MutInt sub, String packageName, HashMap<String, String> imports) {
        this.values = values;
        this.stackSize = stackSize;
        this.structs = structs;
        this.gc = gc;
        this.currentFunction = currentFunction;
        this.varIndex = sub;
        this.imports = imports;
        this.packageName = packageName;
    }
    public Context subContext() {
        HashMap<String, X86Param>[] temp = Arrays.copyOf(values, values.length + 1);
        temp[values.length] = new HashMap<>();
        Context subContext = new Context(temp, stackSize, gc, structs, currentFunction, varIndex == null ? new MutInt() : varIndex, packageName, imports);
        return subContext;
    }
    private void defineLocal(String name, VarInfo value) {
        values[values.length - 1].put(name, value);
    }
    public ExpressionConst knownValue(String name) {
        X86Param info = get(name);
        //System.out.println("Known for " + name + ": " + info);
        if (info == null || !(info instanceof VarInfo)) {
            return null;
        }
        return ((VarInfo) info).knownValue;
    }
    public boolean varDefined(String name) {
        for (HashMap<String, X86Param> value : values) {
            if (value.containsKey(name)) {
                return true;
            }
        }
        return false;
    }
    public void registerArgumentInput(String name, Type type, int loc) {
        if (loc < 16) {
            throw new PatternSyntaxException(name, type + "", loc);
        }
        if (varDefined(name)) {
            throw new IllegalStateException(name + " is already defined -_-");
        }
        defineLocal(name, new VarInfo(name, type, loc));
    }
    public void setType(String name, Type type) {
        if (varDefined(name)) {
            throw new IllegalStateException(name + " is already defined -_-");
        }
        stackSize -= type.getSizeBytes();
        defineLocal(name, new VarInfo(name, type, stackSize));//Otherwise define it as local
    }
    public void setKnownValue(String name, ExpressionConst val) {
        ((VarInfo) get(name)).knownValue = val;
    }
    public void clearKnownValue(String name) {
        if (get(name) != null) {
            setKnownValue(name, null);
        }
    }
    public X86Param getRequired(String name) {
        X86Param info = get(name);
        if (info == null) {
            throw new IllegalStateException("WEWLAD\nEWLADW\nWLADWE\nLADWEW\nADWEWL\nDWEWLA\n" + name);
        }
        return info;
    }
    public X86Param get(String name) {
        for (int i = values.length - 1; i >= 0; i--) {
            X86Param possibleValue = values[i].get(name);
            if (possibleValue != null) {
                return possibleValue;
            }
        }
        if (currentTempVarUsage != null) {
            VarInfo pos = currentTempVarUsage.getInfo(name);
            if (pos != null) {
                return pos;
            }
        }
        //System.out.println("WARNING: Unable to find requested variable named '" + name + "'. Returning null. Context is " + toString());
        return null;
    }
    @Override
    public String toString() {
        return Arrays.asList(values) + " " + structs;
    }
}
