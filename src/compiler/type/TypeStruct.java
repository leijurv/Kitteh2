/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.type;
import compiler.Struct;
import compiler.Struct.StructField;

/**
 *
 * @author leijurv
 */
public class TypeStruct extends Type {
    public final Struct struct;
    public TypeStruct(Struct struct) {
        this.struct = struct;
    }
    @Override
    public int getSizeBytes() {
        return struct.getFields().stream().map(StructField::getType).mapToInt(Type::getSizeBytes).sum();
    }
    @Override
    public String toString() {
        return "Struct" + struct;
    }
}
