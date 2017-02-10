/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.expression;
import compiler.Context;
import compiler.Operator;
import compiler.command.Command;
import compiler.command.CommandSetArray;
import compiler.tac.IREmitter;
import compiler.tac.TACArrayDeref;
import compiler.tac.TACJumpBoolVar;
import compiler.tac.TempVarUsage;
import compiler.type.Type;
import compiler.type.TypeInt32;
import compiler.type.TypePointer;

/**
 *
 * @author leijurv
 */
public class ExpressionArrayAccess extends ExpressionConditionalJumpable implements Settable {
    private Expression array;
    private Expression index;
    public ExpressionArrayAccess(Expression array, Expression index) {
        this.array = array;
        this.index = index;
        if (!(array.getType() instanceof TypePointer)) {
            throw new IllegalStateException();
        }
    }
    @Override
    public void generateConditionalJump(IREmitter emit, TempVarUsage tempVars, int jumpTo, boolean invert) {
        String tmp = tempVars.getTempVar(((TypePointer) array.getType()).pointingTo());
        generateTAC(emit, tempVars, tmp);
        emit.emit(new TACJumpBoolVar(tmp, jumpTo, invert));
    }
    @Override
    public int condLength() {
        return 1 + getTACLength();
    }
    @Override
    protected Type calcType() {
        return ((TypePointer) array.getType()).pointingTo();
    }
    @Override
    public void generateTAC(IREmitter emit, TempVarUsage tempVars, String resultLocation) {
        String arr = tempVars.getTempVar(array.getType());
        array.generateTAC(emit, tempVars, arr);
        String ind = tempVars.getTempVar(index.getType());
        index.generateTAC(emit, tempVars, ind);
        emit.emit(new TACArrayDeref(arr, ind, resultLocation));
    }
    @Override
    protected int calculateTACLength() {
        return 1 + array.getTACLength() + index.getTACLength();
    }
    @Override
    public Command setValue(Expression rvalue, Context context) {
        return new CommandSetArray(array, index, rvalue, context);
        //alternate implementation from before i had commandsetarray:
        //Expression ptr = new ExpressionOperator(array, Operator.PLUS, new ExpressionOperator(index, Operator.MULTIPLY, new ExpressionConstNum(((TypePointer) array.getType()).pointingTo().getSizeBytes(), new TypeInt32())));
        //return new CommandSetPtr(context, ptr, rvalue);
    }
    @Override
    public Expression calculateConstants() {
        array = array.calculateConstants();
        index = index.calculateConstants();
        if (index instanceof ExpressionConst) {
            if (!(index instanceof ExpressionConstNum)) {
                throw new RuntimeException();
            }
            Expression ptr = new ExpressionOperator(array, Operator.PLUS, new ExpressionOperator(index, Operator.MULTIPLY, new ExpressionConstNum(getType().getSizeBytes(), new TypeInt32())));
            Expression res = new ExpressionPointerDeref(ptr).calculateConstants();
            return res;
        }
        return this;
    }
    @Override
    public Expression insertKnownValues(Context context) {
        array = array.insertKnownValues(context);
        index = index.insertKnownValues(context);
        return this;
    }
}
