/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler;
import compiler.Context.VarInfo;
import compiler.type.Type;
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
    public Struct(String name, ArrayList<Type> fieldTypes, ArrayList<String> fieldNames) {
        this.name = name;
        int pos = 0;
        this.fields = new HashMap<>();
        for (int i = 0; i < fieldTypes.size(); i++) {
            fields.put(fieldNames.get(i), new VarInfo(fieldNames.get(i), fieldTypes.get(i), pos));
            pos += fieldTypes.get(i).getSizeBytes();
        }
    }
    public VarInfo getFieldByName(String name) {
        return fields.get(name);
    }
    public Collection<VarInfo> getFields() {
        return fields.values();
    }
}
