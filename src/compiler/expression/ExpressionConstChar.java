/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.expression;
import compiler.tac.IREmitter;
import compiler.tac.TACConst;
import compiler.tac.TempVarUsage;
import compiler.type.Type;
import compiler.type.TypeInt8;

/**
 *
 * @author leijurv
 */
public class ExpressionConstChar extends Expression {
    private final char val;
    public ExpressionConstChar(char val) {
        this.val = val;
    }
    @Override
    protected Type calcType() {
        return new TypeInt8();
    }
    @Override
    public void generateTAC(IREmitter emit, TempVarUsage tempVars, String resultLocation) {
        emit.emit(new TACConst(resultLocation, (val + 0) + ""));//convert the char to an int, then to a string
    }
    @Override
    protected int calculateTACLength() {
        return 1;
    }
}
