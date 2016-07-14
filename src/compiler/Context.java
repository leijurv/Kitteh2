/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler;
import java.util.Arrays;
import java.util.HashMap;

/**
 *
 * @author leijurv
 */
public class Context {
    public class VarInfo {//this class is here because when it's actually a compiler this will store some more sketchy data like stack offset, stack size, etc
        Type type;
        Object knownValue;//TODO use this for optimization. like you could optimize "i=5; j=i+i" to "j=10"
        public VarInfo(Type type) {
            this.type = type;
        }
    }
    private final HashMap<String, VarInfo>[] values;
    public Context() {
        values = new HashMap[]{new HashMap<>()};
    }
    private Context(HashMap<String, VarInfo>[] values) {
        this.values = values;
    }
    public Context subContext() {
        HashMap<String, VarInfo>[] temp = new HashMap[values.length + 1];
        System.arraycopy(values, 0, temp, 0, values.length);
        temp[values.length] = new HashMap<>();
        return new Context(temp);
    }
    public Context superContext() {
        if (values.length <= 1) {
            throw new IllegalStateException("Already top context");
        }
        HashMap<String, VarInfo>[] temp = new HashMap[values.length - 1];
        System.arraycopy(values, 0, temp, 0, temp.length);
        return new Context(temp);
    }
    public Context topContext() {
        return new Context(new HashMap[]{values[0]});
    }
    private void defineLocal(String name, VarInfo value) {
        values[values.length - 1].put(name, value);
    }
    public boolean varDefined(String name) {
        for (int i = 0; i < values.length; i++) {
            if (values[i].containsKey(name)) {
                return true;
            }
        }
        return false;
    }
    public void setType(String name, Type type) {
        if (varDefined(name)) {
            throw new IllegalStateException(name + " is already defined -_-");
        }
        defineLocal(name, new VarInfo(type));//Otherwise define it as local
    }
    public Type getType(String name) {
        VarInfo info = get(name);
        return info == null ? null : info.type;//deviously pass off the inevitable nullpointerexception
    }
    public VarInfo get(String name) {
        for (int i = values.length - 1; i >= 0; i--) {
            VarInfo possibleValue = values[i].get(name);
            if (possibleValue != null) {
                return possibleValue;
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
