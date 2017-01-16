/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.command;
import compiler.Context;
import compiler.Operator;
import compiler.expression.Expression;
import compiler.expression.ExpressionCast;
import compiler.expression.ExpressionConstNum;
import compiler.expression.ExpressionOperator;
import compiler.tac.IREmitter;
import compiler.tac.TACPointerRef;
import compiler.tac.TempVarUsage;
import compiler.type.TypePointer;

/**
 *
 * @author leijurv
 */
public class CommandSetPtr extends Command {
    private Expression pointer;
    private Expression value;
    public CommandSetPtr(Context context, Expression pointer, Expression value) {
        super(context);
        this.pointer = pointer;
        if (!(pointer.getType() instanceof TypePointer)) {
            throw new IllegalArgumentException(pointer + " " + value);
        }
        this.value = value;
    }
    public static Expression[] tryOffsetBased(Expression deReferencing) {
        if (deReferencing instanceof ExpressionCast && ((ExpressionCast) deReferencing).input instanceof ExpressionOperator) {
            ExpressionOperator eo = (ExpressionOperator) ((ExpressionCast) deReferencing).input;
            if (eo.getOP() == Operator.PLUS) {
                if (eo.getA() instanceof ExpressionConstNum) {
                    return new Expression[]{eo.getA(), new ExpressionCast(eo.getB(), deReferencing.getType())};
                } else if (eo.getB() instanceof ExpressionConstNum) {
                    return new Expression[]{eo.getB(), new ExpressionCast(eo.getA(), deReferencing.getType())};
                }
            }
        }
        if (deReferencing instanceof ExpressionOperator) {
            ExpressionOperator eo = (ExpressionOperator) deReferencing;
            if (eo.getOP() == Operator.PLUS) {
                if (eo.getA() instanceof ExpressionConstNum) {
                    return new Expression[]{eo.getA(), eo.getB()};
                } else if (eo.getB() instanceof ExpressionConstNum) {
                    return new Expression[]{eo.getB(), eo.getA()};
                }
            }
        }
        return new Expression[0];
    }
    @Override
    protected void generateTAC0(IREmitter emit) {
        Expression[] eo = tryOffsetBased(pointer);
        Expression der;
        int offset;
        if (eo.length != 0) {
            ExpressionConstNum cons = (ExpressionConstNum) eo[0];
            offset = cons.getVal().intValue();
            der = eo[1];
        } else {
            der = pointer;
            offset = 0;
        }
        TempVarUsage tvu = new TempVarUsage(context);
        String ptr = tvu.getTempVar(pointer.getType());
        der.generateTAC(emit, tvu, ptr);
        String val = tvu.getTempVar(value.getType());
        value.generateTAC(emit, tvu, val);
        emit.emit(new TACPointerRef(val, ptr, offset));
    }
    @Override
    protected int calculateTACLength() {
        return (tryOffsetBased(pointer).length != 0 ? tryOffsetBased(pointer)[1] : pointer).getTACLength() + value.getTACLength() + 1;
    }
    @Override
    public void staticValues() {
        pointer = pointer.insertKnownValues(context);
        pointer = pointer.calculateConstants();
        value = value.insertKnownValues(context);
        value = value.calculateConstants();
    }
}
