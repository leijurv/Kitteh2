/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.expression;
import compiler.Context;
import compiler.command.Command;
import compiler.tac.IREmitter;
import compiler.tac.TACArrayDeref;
import compiler.tac.TACArrayRef;
import compiler.tac.TACJumpBoolVar;
import compiler.tac.TempVarUsage;
import compiler.type.Type;
import compiler.type.TypePointer;

/**
 *
 * @author leijurv
 */
public class ExpressionArrayAccess extends ExpressionConditionalJumpable implements Settable {
    Expression array;
    Expression index;
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
        class CommandSetArray extends Command {
            Expression value;
            public CommandSetArray(Expression value, Context context) {
                super(context);
                this.value = value;
            }
            @Override
            protected void generateTAC0(IREmitter emit) {
                TempVarUsage tempVars = new TempVarUsage(context);
                String arr = tempVars.getTempVar(array.getType());
                array.generateTAC(emit, tempVars, arr);
                String ind = tempVars.getTempVar(index.getType());
                index.generateTAC(emit, tempVars, ind);
                String source = tempVars.getTempVar(value.getType());
                value.generateTAC(emit, tempVars, source);
                emit.emit(new TACArrayRef(arr, ind, source));
            }
            @Override
            protected int calculateTACLength() {
                return 1 + index.getTACLength() + array.getTACLength() + value.getTACLength();
            }
            @Override
            protected void staticValues() {
                array = array.insertKnownValues(context);
                array = array.calculateConstants();
                value = value.insertKnownValues(context);
                value = value.calculateConstants();
                index = index.insertKnownValues(context);
                index = index.calculateConstants();
            }
        }
        return new CommandSetArray(rvalue, context);
        //Expression ptr = new ExpressionOperator(array, Operator.PLUS, new ExpressionOperator(index, Operator.MULTIPLY, new ExpressionConstNum(((TypePointer) array.getType()).pointingTo().getSizeBytes(), new TypeInt32())));
        //return new CommandSetPtr(context, ptr, rvalue);
    }
    @Override
    public Expression calculateConstants() {
        array = array.calculateConstants();
        index = index.calculateConstants();
        return this;
    }
    @Override
    public Expression insertKnownValues(Context context) {
        array = array.insertKnownValues(context);
        index = index.insertKnownValues(context);
        return this;
    }
}
