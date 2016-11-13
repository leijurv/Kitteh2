/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.expression;
import compiler.Context;
import compiler.Context.VarInfo;
import compiler.Struct;
import compiler.tac.IREmitter;
import compiler.tac.TACConst;
import compiler.tac.TempVarUsage;
import compiler.type.Type;
import compiler.type.TypeStruct;

/**
 *
 * @author leijurv
 */
public class ExpressionStructFieldAccess extends Expression {
    String field;
    Expression input;
    Struct struct;
    public ExpressionStructFieldAccess(Expression input, String field) {
        this.struct = ((TypeStruct) input.getType()).struct;
        this.input = input;
        this.field = field;
        if (struct.getFieldByName(field) == null) {
            throw new RuntimeException();
        }
    }
    @Override
    protected Type calcType() {
        return struct.getFieldByName(field).getType();
    }
    @Override
    public void generateTAC(IREmitter emit, TempVarUsage tempVars, String resultLocation) {
        String temp = tempVars.getTempVar(input.getType());
        VarInfo structLocation = tempVars.getInfo(temp);
        input.generateTAC(emit, tempVars, temp);
        int structLocationOnStack = structLocation.getStackLocation();
        int offsetOfThisFieldWithinStruct = struct.getFieldByName(field).getStackLocation();
        int fieldLocationOnStack = structLocationOnStack + offsetOfThisFieldWithinStruct;
        System.out.println(structLocationOnStack + " " + offsetOfThisFieldWithinStruct + " " + fieldLocationOnStack + " " + struct.getFieldByName(field));
        String fieldLabel = tempVars.registerLabelManually(fieldLocationOnStack, struct.getFieldByName(field).getType());
        emit.emit(new TACConst(resultLocation, fieldLabel));
    }
    @Override
    protected int calculateTACLength() {
        return input.getTACLength() + 1;
    }
    @Override
    public Expression calculateConstants() {
        input = input.calculateConstants();
        return this;
    }
    @Override
    public Expression insertKnownValues(Context context) {
        input = input.insertKnownValues(context);
        return this;
    }
}
