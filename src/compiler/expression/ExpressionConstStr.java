/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.expression;
import compiler.tac.IREmitter;
import compiler.tac.TACConstStr;
import compiler.tac.TempVarUsage;
import compiler.type.Type;
import compiler.type.TypeInt8;
import compiler.type.TypePointer;

/**
 *
 * @author leijurv
 */
public class ExpressionConstStr extends Expression implements ExpressionConst<String> {
    public final String val;
    public ExpressionConstStr(String val) {
        this.val = val;
    }
    @Override
    protected Type calcType() {
        return new <TypeInt8>TypePointer<TypeInt8>(new TypeInt8());
    }
    @Override
    public void generateTAC(IREmitter emit, TempVarUsage tempVars, String resultLocation) {
        emit.emit(new TACConstStr(resultLocation, val));
    }
    @Override
    protected int calculateTACLength() {
        return 1;
    }
    @Override
    public String getVal() {
        return val;
    }
}
