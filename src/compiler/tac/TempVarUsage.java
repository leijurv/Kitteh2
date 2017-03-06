/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac;
import compiler.Context;
import compiler.Context.VarInfo;
import compiler.type.Type;
import java.util.HashMap;

/**
 * I didn't realize how hard it was to deal with temp variables. Temp variables
 * are only used for one expression total. So they occupy a sort of different
 * part of the stack. Their part of the stack is added on to whatever has
 * already been allocated for real variables at the time. But this protrusion is
 * deleted/ignored by the next statement, and that space might be used again for
 * temp variables or real ones, I don't know
 *
 * @author leijurv
 */
public class TempVarUsage {
    public static final String TEMP_VARIABLE_PREFIX = "tmp";
    public static final String TEMP_STRUCT_FIELD_INFIX = "sketchymanual";
    private int sizeSoFar;
    final private Context ctx;
    private final HashMap<String, VarInfo> types = new HashMap<>();//hashmap instead of arraylist to make lookups easier
    public TempVarUsage(Context context) {
        this.ctx = context;
        this.sizeSoFar = context.getNonTempStackSize();
    }
    public VarInfo getInfo(String tempVar) {
        return types.get(tempVar);
    }
    public VarInfo getTempVar(Type type) {
        String name = TEMP_VARIABLE_PREFIX + ctx.varIndex.val++;
        // if (ind > 16) {
        //    throw new IllegalStateException("I hope you're Dora because I'm going to need a Map to get out of this one");
        //}
        sizeSoFar -= type.getSizeBytes();
        VarInfo vi = ctx.new VarInfo(name, type, sizeSoFar);
        types.put(name, vi);
        ctx.updateMinAdditionalSizeTemp(sizeSoFar);//make sure the context knows how much temp var bytes we gobblin up
        return vi;//yes
    }
    public VarInfo registerLabelManually(int stackLocation, Type type, String info) {//used for structs
        String name = TEMP_VARIABLE_PREFIX + "_" + info + "_" + TEMP_STRUCT_FIELD_INFIX + ctx.varIndex.val++;
        VarInfo vi = ctx.new VarInfo(name, type, stackLocation);
        types.put(name, vi);
        return vi;
    }
}
