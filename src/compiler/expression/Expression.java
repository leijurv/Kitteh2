/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.expression;
import compiler.Context;
import compiler.tac.IREmitter;
import compiler.tac.TempVarUsage;
import compiler.type.Type;
import java.nio.charset.IllegalCharsetNameException;

/**
 *
 * @author leijurv
 */
public abstract class Expression {
    public final Type getType() {
        if (cachedType == null) {
            cachedType = calcType();
            if (cachedType == null) {
                throw new IllegalCharsetNameException("");
            }
        }
        return cachedType;
    }
    private Type cachedType = null;
    protected abstract Type calcType();//the return type
    public abstract void generateTAC(IREmitter emit, TempVarUsage tempVars, String resultLocation);//TODO enforce length
    protected abstract int calculateTACLength();
    private Integer taclen = null;
    public int getTACLength() {
        if (taclen == null) {
            taclen = calculateTACLength();
        }
        return taclen;
    }
    public Expression insertKnownValues(Context context) {//insert known values. e.g. if x is known to be 5, then x+1 should become 5+1
        return this;
    }
    public Expression calculateConstants() {//calculate static calculations. e.g. the expression 5+5 should return the const 10
        return this;
    }
    public boolean canBeCommand() {
        //can this expression be a line on its own. e.g. expressionfunctioncall is true because you can do f();, but expressionoperator is false because you can't do 5+6; on its own
        return false;//override where it's true, default to false
    }
}
