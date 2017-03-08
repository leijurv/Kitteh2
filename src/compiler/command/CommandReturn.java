/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.command;
import compiler.Context;
import compiler.Context.VarInfo;
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
import java.util.stream.Stream;

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
                throw new IllegalStateException();
            }
        }
        for (int i = 0; i < toReturn.length; i++) {
            Type should = context.getCurrentFunction().getReturnTypes()[i];
            if (!should.equals(toReturn[i].getType())) {
                throw new IllegalStateException("Floating point division not yet supported");//lol this is even funnier now because it really is. its less funny because it can literally never get to here, but...  still.
            }
        }
    }
    @Override
    protected void generateTAC0(IREmitter emit) {
        TempVarUsage lol = new TempVarUsage(context);
        VarInfo[] tempVars = new VarInfo[toReturn.length];
        for (int i = 0; i < toReturn.length; i++) {
            VarInfo var = lol.getTempVar(toReturn[i].getType());
            toReturn[i].generateTAC(emit, lol, var);
            tempVars[i] = var;
        }
        for (int i = 0; i < toReturn.length; i++) {
            X86Register register = TACFunctionCall.RETURN_REGISTERS.get(i);
            emit.emit(new TACConst(register.getRegister((TypeNumerical) toReturn[i].getType()), tempVars[i]));
        }
        emit.emit(new TACReturn());
    }
    @Override
    protected int calculateTACLength() {
        return 1 + Stream.of(toReturn).mapToInt(Expression::getTACLength).map(x -> x + 1).sum();
    }
    @Override
    public void staticValues() {
        for (int i = 0; i < toReturn.length; i++) {
            toReturn[i] = toReturn[i].insertKnownValues(context);
            toReturn[i] = toReturn[i].calculateConstants();
        }
    }
}
