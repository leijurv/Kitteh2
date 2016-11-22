/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.command;
import compiler.Context;
import compiler.x86.X86Register;
import compiler.expression.Expression;
import compiler.tac.IREmitter;
import compiler.tac.TACConst;
import compiler.tac.TACReturn;
import compiler.tac.TempVarUsage;
import compiler.type.TypeNumerical;

/**
 *
 * @author leijurv
 */
public class CommandReturn extends Command {
    private Expression toReturn;
    public CommandReturn(Context context, Expression toReturn) {
        super(context);
        this.toReturn = toReturn;
        if (toReturn != null && !context.getCurrentFunctionReturnType().equals(toReturn.getType())) {
            throw new IllegalStateException("Floating point division not yet supported");//lol
        }
    }
    @Override
    protected void generateTAC0(IREmitter emit) {
        if (toReturn != null) {
            TempVarUsage lol = new TempVarUsage(context);
            String var = lol.getTempVar(toReturn.getType());
            toReturn.generateTAC(emit, lol, var);
            emit.emit(new TACConst(X86Register.A.getRegister((TypeNumerical) toReturn.getType()), var));
        }
        emit.emit(new TACReturn());
    }
    @Override
    protected int calculateTACLength() {
        return toReturn == null ? 1 : toReturn.getTACLength() + 2;
    }
    @Override
    public void staticValues() {
        if (toReturn != null) {
            toReturn = toReturn.insertKnownValues(context);
        }
    }
}
