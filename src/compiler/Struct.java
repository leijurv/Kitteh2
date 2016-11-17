/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler;
import compiler.Context.VarInfo;
import compiler.type.Type;
import compiler.type.TypePointer;
import compiler.type.TypeStruct;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 *
 * @author leijurv
 */
public class Struct {
    final String name;
    private final HashMap<String, VarInfo> fields;
    public Struct(String name, ArrayList<Type> fieldTypes, ArrayList<String> fieldNames, Context context) {
        this.name = name;
        int pos = 0;
        this.fields = new HashMap<>();
        fixFieldTypes(fieldTypes);
        for (int i = 0; i < fieldTypes.size(); i++) {
            fields.put(fieldNames.get(i), context.new VarInfo(fieldNames.get(i), fieldTypes.get(i), pos));
            pos += fieldTypes.get(i).getSizeBytes();
        }
    }
    private void fixFieldTypes(ArrayList<Type> fieldTypes) {
        for (int i = 0; i < fieldTypes.size(); i++) {
            fieldTypes.set(i, fixType(fieldTypes.get(i)));
        }
    }
    private Type fixType(Type type) {
        if (type == null) {
            return new TypeStruct(this);
        }
        if (type instanceof TypePointer) {
            Type pointingTo = ((TypePointer) type).pointingTo();
            return new TypePointer<>(fixType(pointingTo));
        }
        return type;
    }
    public VarInfo getFieldByName(String name) {
        return fields.get(name);
    }
    public Collection<VarInfo> getFields() {
        return fields.values();
    }
}
