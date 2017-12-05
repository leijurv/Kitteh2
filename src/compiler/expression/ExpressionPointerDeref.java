/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.expression;
import compiler.Context;
import compiler.Context.VarInfo;
import compiler.command.Command;
import compiler.command.CommandSetPtr;
import compiler.tac.IREmitter;
import compiler.tac.TACJumpBoolVar;
import compiler.tac.TACPointerDeref;
import compiler.tac.TempVarUsage;
import compiler.type.Type;
import compiler.type.TypeBoolean;
import compiler.type.TypePointer;

/**
 *
 * @author leijurv
 */
public class ExpressionPointerDeref extends ExpressionConditionalJumpable implements Settable {
    public final Expression deReferencing;
    public ExpressionPointerDeref(Expression deref) {
        this.deReferencing = deref;
    }
    @Override
    protected Type calcType() {
        TypePointer tp = (TypePointer) deReferencing.getType();
        return tp.pointingTo();
    }
    private Expression[] tryOffsetBased() {
        return CommandSetPtr.tryOffsetBased(deReferencing);
    }
    @Override
    public void generateTAC(IREmitter emit, TempVarUsage tempVars, VarInfo resultLocation) {
        Expression[] eo = tryOffsetBased();
        Expression der;
        int offset;
        if (eo.length != 0) {
            ExpressionConstNum cons = (ExpressionConstNum) eo[0];
            offset = cons.getVal().intValue();
            der = eo[1];
        } else {
            der = deReferencing;
            offset = 0;
        }
        VarInfo tmp = tempVars.getTempVar(deReferencing.getType());
        der.generateTAC(emit, tempVars, tmp);
        emit.emit(new TACPointerDeref(tmp, resultLocation, offset));
    }
    @Override
    protected int calculateTACLength() {
        return tryOffsetBased().length == 0 ? deReferencing.getTACLength() + 1 : tryOffsetBased()[1].getTACLength() + 1;
    }
    @Override
    public Expression calculateConstants() {
        return new ExpressionPointerDeref(deReferencing.calculateConstants());
    }
    @Override
    public Expression insertKnownValues(Context context) {
        return new ExpressionPointerDeref(deReferencing.insertKnownValues(context));
    }
    @Override
    public Command setValue(Expression rvalue, Context context) {
        return new CommandSetPtr(context, deReferencing, rvalue);
    }
    @Override
    public void generateConditionalJump(IREmitter emit, TempVarUsage tempVars, int jumpTo, boolean invert) {
        VarInfo tmp = tempVars.getTempVar(new TypeBoolean());
        generateTAC(emit, tempVars, tmp);
        emit.emit(new TACJumpBoolVar(tmp, jumpTo, invert));
    }
    @Override
    public int condLength() {
        return 1 + getTACLength();
    }
}
