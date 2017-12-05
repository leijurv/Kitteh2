/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.command;
import compiler.Context;
import compiler.Context.VarInfo;
import compiler.Operator;
import compiler.expression.Expression;
import compiler.expression.ExpressionConst;
import compiler.expression.ExpressionConstNum;
import compiler.expression.ExpressionOperator;
import compiler.tac.IREmitter;
import compiler.tac.TACArrayRef;
import compiler.tac.TempVarUsage;
import compiler.type.TypeInt32;

/**
 *
 * @author leijurv
 */
public class CommandSetArray extends Command {
    private Expression array;
    private Expression index;
    private Expression value;
    public CommandSetArray(Expression array, Expression index, Expression value, Context context) {
        super(context);
        this.value = value;
        this.array = array;
        this.index = index;
    }
    @Override
    protected void generateTAC0(IREmitter emit) {
        TempVarUsage tempVars = new TempVarUsage(context);
        VarInfo arr = tempVars.getTempVar(array.getType());
        array.generateTAC(emit, tempVars, arr);
        VarInfo ind = tempVars.getTempVar(index.getType());
        index.generateTAC(emit, tempVars, ind);
        VarInfo source = tempVars.getTempVar(value.getType());
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
    @Override
    public Command optimize() {
        staticValues();
        if (index instanceof ExpressionConst) {
            if (!(index instanceof ExpressionConstNum)) {
                throw new IllegalStateException();
            }
            int val = ((ExpressionConstNum) index).getVal().intValue();
            CommandSetPtr res;
            if (val == 0) {
                res = new CommandSetPtr(context, array, value);
            } else {
                Expression ptr = new ExpressionOperator(array, Operator.PLUS, new ExpressionOperator(index, Operator.MULTIPLY, new ExpressionConstNum(value.getType().getSizeBytes(), new TypeInt32())));
                //System.out.println(ptr + " " + Arrays.toString(CommandSetPtr.tryOffsetBased(ptr)));
                res = new CommandSetPtr(context, ptr, value);
            }
            //System.out.println(array + " " + index + " " + value + " BECOMES " + res);
            return res.optimize();
        }
        return this;
    }
}
