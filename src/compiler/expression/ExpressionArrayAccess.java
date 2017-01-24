/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.expression;
import compiler.Context;
import compiler.Operator;
import compiler.command.Command;
import compiler.command.CommandSetPtr;
import compiler.tac.IREmitter;
import compiler.tac.TACArrayDeref;
import compiler.tac.TACArrayRef;
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
        String ind = tempVars.getTempVar(array.getType());
        new ExpressionCast(index, array.getType()).generateTAC(emit, tempVars, ind);
        emit.emit(new TACArrayDeref(arr, ind, resultLocation));
    }
    @Override
    protected int calculateTACLength() {
        return 1 + array.getTACLength() + new ExpressionCast(index, array.getType()).getTACLength();
    }
    @Override
    public Command setValue(Expression rvalue, Context context) {
        class CommandSetArray extends Command {
            Expression array;
            Expression ind;
            Expression value;
            public CommandSetArray(Expression array, Expression ind, Expression value, Context context) {
                super(context);
                this.array = array;
                this.ind = ind;
                this.value = value;
            }
            @Override
            protected void generateTAC0(IREmitter emit) {
                TempVarUsage tempVars = new TempVarUsage(context);
                String arr = tempVars.getTempVar(array.getType());
                array.generateTAC(emit, tempVars, arr);
                String ind = tempVars.getTempVar(array.getType());
                new ExpressionCast(index, array.getType()).generateTAC(emit, tempVars, ind);
                String source = tempVars.getTempVar(value.getType());
                value.generateTAC(emit, tempVars, source);
                emit.emit(new TACArrayRef(arr, ind, source));
            }
            @Override
            protected int calculateTACLength() {
                return 1 + new ExpressionCast(index, array.getType()).getTACLength() + array.getTACLength() + value.getTACLength();
            }
            @Override
            protected void staticValues() {
            }
        }
        //return new CommandSetArray(array, index, rvalue, context);
        Expression ptr = new ExpressionOperator(array, Operator.PLUS, new ExpressionOperator(index, Operator.MULTIPLY, new ExpressionConstNum(((TypePointer) array.getType()).pointingTo().getSizeBytes(), new TypeInt32())));
        return new CommandSetPtr(context, ptr, rvalue);
    }
}
