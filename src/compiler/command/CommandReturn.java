/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.command;
import compiler.Context;
import compiler.expression.Expression;
import compiler.tac.IREmitter;
import compiler.tac.TACConst;
import compiler.tac.TACFunctionCall;
import compiler.tac.TACReturn;
import compiler.tac.TempVarUsage;
import compiler.type.Type;
import compiler.type.TypeNumerical;
import compiler.type.TypeVoid;
import compiler.x86.X86Register;

/**
 *
 * @author leijurv
 */
public class CommandReturn extends Command {
    private final Expression[] toReturn;
    public CommandReturn(Context context, Expression... toReturn) {
        super(context);
        this.toReturn = toReturn;
        if (toReturn.length == 0) {
            if (!(context.getCurrentFunction().getReturnTypes()[0] instanceof TypeVoid)) {
                throw new IllegalStateException();
            }
        } else {
            if (toReturn.length != context.getCurrentFunction().getReturnTypes().length) {
                throw new RuntimeException();
            }
        }
        for (int i = 0; i < toReturn.length; i++) {
            Type should = context.getCurrentFunction().getReturnTypes()[i];
            if (!should.equals(toReturn[i].getType())) {
                throw new IllegalStateException("Floating point division not yet supported");//lol
            }
        }
    }
    @Override
    protected void generateTAC0(IREmitter emit) {
        TempVarUsage lol = new TempVarUsage(context);
        String[] tempVars = new String[toReturn.length];
        for (int i = 0; i < toReturn.length; i++) {
            String var = lol.getTempVar(toReturn[i].getType());
            toReturn[i].generateTAC(emit, lol, var);
            tempVars[i] = var;
        }
        for (int i = 0; i < toReturn.length; i++) {
            X86Register register = TACFunctionCall.returnRegisters[i];
            emit.emit(new TACConst("" + register.getRegister((TypeNumerical) toReturn[i].getType()), tempVars[i]));
        }
        emit.emit(new TACReturn());
    }
    @Override
    protected int calculateTACLength() {
        int sum = 1;
        for (int i = 0; i < toReturn.length; i++) {
            sum += 1 + toReturn[i].getTACLength();
        }
        return sum;
    }
    @Override
    public void staticValues() {
        for (int i = 0; i < toReturn.length; i++) {
            toReturn[i] = toReturn[i].insertKnownValues(context);
            toReturn[i] = toReturn[i].calculateConstants();
        }
    }
}
