/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler;
import compiler.command.CommandDefineFunction;
import compiler.expression.ExpressionConst;
import compiler.tac.TempVarUsage;
import compiler.type.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Stream;

/**
 * Sorta a symbol table that also deals with scoping and temp variables that
 * overlap on the stack
 *
 * @author leijurv
 */
public class Context {
    public static class VarInfo {
        public static boolean printFull = true;
        private final String name;
        private final Type type;
        private ExpressionConst knownValue;
        private final int stackLocation;
        //TODO maybe a pointer to the Context / TempVarInfo that instantiated this
        public VarInfo(String name, Type type, int stackLocation) {
            this.name = name;
            this.type = type;
            this.stackLocation = stackLocation;
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
        public Type getType() {
            return type;
        }
        public int getStackLocation() {
            return stackLocation;
        }
        public String x86() {
            return (stackLocation) + ("(%rbp)");
        }
    }
    private final HashMap<String, VarInfo>[] values;
    private int stackSize;
    private Integer additionalSizeTemp = null;
    private TempVarUsage currentTempVarUsage = null;
    private CommandDefineFunction currentFunction = null;
    public FunctionsContext gc;
    @SuppressWarnings("unchecked")//you can't actually do "new HashMap<>[]{" so I can't fix this warning
    public Context() {
        this.values = new HashMap[]{new HashMap<>()};
        this.stackSize = 0;
    }
    public void setCurrFunc(CommandDefineFunction cdf) {
        this.currentFunction = cdf;
    }
    public Type getCurrentFunctionReturnType() {
        return currentFunction.getHeader().getReturnType();
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
            return true;
        }
        if (getTotalStackSize() == 0) {
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
    private Context(HashMap<String, VarInfo>[] values, int stackSize) {
        this.values = values;
        this.stackSize = stackSize;
    }
    @SuppressWarnings("unchecked")//you can't actually do "new HashMap<>[" so I can't fix this warning
    public Context subContext() {
        HashMap<String, VarInfo>[] temp = new HashMap[values.length + 1];
        System.arraycopy(values, 0, temp, 0, values.length);
        temp[values.length] = new HashMap<>();
        Context subContext = new Context(temp, stackSize);
        subContext.setCurrFunc(currentFunction);
        subContext.gc = gc;
        return subContext;
    }
    /*public Context superContext() {
     if (values.length <= 1) {
     throw new IllegalStateException("Already top context");
     }
     HashMap<String, VarInfo>[] temp = new HashMap[values.length - 1];
     System.arraycopy(values, 0, temp, 0, temp.length);
     return new Context(temp);
     }
     public Context topContext() {
     return new Context(new HashMap[]{values[0]});
     }*/
    private void defineLocal(String name, VarInfo value) {
        values[values.length - 1].put(name, value);
    }
    public ExpressionConst knownValue(String name) {
        VarInfo info = get(name);
        //System.out.println("Known for " + name + ": " + info);
        if (info == null) {
            return null;
        }
        return info.knownValue;
    }
    public boolean varDefined(String name) {
        for (HashMap<String, VarInfo> value : values) {
            if (value.containsKey(name)) {
                return true;
            }
        }
        return false;
    }
    public void registerArgumentInput(String name, Type type, int loc) {
        if (loc < 16) {
            throw new IllegalStateException();
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
    public Type getType(String name) {
        VarInfo info = get(name);
        return info == null ? null : info.type;//deviously pass off the inevitable nullpointerexception
    }
    public void setKnownValue(String name, ExpressionConst val) {
        get(name).knownValue = val;
    }
    public void clearKnownValue(String name) {
        setKnownValue(name, null);
    }
    public int getStackLocation(String name) {
        return get(name).stackLocation;
    }
    public VarInfo getRequired(String name) {
        VarInfo info = get(name);
        if (info == null) {
            throw new IllegalStateException("WEWLAD\nEWLADW\nWLADWE\nLADWEW\nADWEWL\nDWEWLA\n" + name);
        }
        return info;
    }
    public VarInfo get(String name) {
        for (int i = values.length - 1; i >= 0; i--) {
            VarInfo possibleValue = values[i].get(name);
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
        System.out.println("WARNING: Unable to find requested variable named '" + name + "'. Returning null. Context is " + toString());
        return null;
    }
    @Override
    public String toString() {
        return Arrays.asList(values).toString();
    }
}
