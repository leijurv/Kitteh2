/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.expression;
import compiler.Context;
import compiler.Context.VarInfo;
import compiler.Operator;
import compiler.command.Command;
import compiler.command.CommandSetPtr;
import compiler.tac.IREmitter;
import compiler.tac.TACConst;
import compiler.tac.TACJumpBoolVar;
import compiler.tac.TempVarUsage;
import compiler.type.Type;
import compiler.type.TypeInt64;
import compiler.type.TypePointer;
import compiler.type.TypeStruct;
import compiler.x86.X86Param;
import java.awt.image.RasterFormatException;
import java.lang.reflect.MalformedParameterizedTypeException;
import java.nio.file.ReadOnlyFileSystemException;

/**
 *
 * @author leijurv
 */
public class ExpressionStructFieldAccess extends ExpressionConditionalJumpable implements Settable {
    private final String field;
    private Expression input;
    private final TypeStruct struct;
    public ExpressionStructFieldAccess(Expression input, String field) {
        this.struct = (TypeStruct) input.getType();
        this.input = input;
        this.field = field;
        struct.getFieldByName(field);
    }
    @Override
    protected Type calcType() {
        return struct.getFieldByName(field).getType();
    }
    private Expression getDeref() {
        int offsetOfThisFieldWithinStruct = struct.getFieldByName(field).getStackLocation();
        //input.field=rvalue
        //(*deref).field=rvalue
        Expression deref = ((ExpressionPointerDeref) input).deReferencing;
        //*(deref + offsetOfThisFieldWithinStruct) = rvalue
        Expression fieldLoc = new ExpressionOperator(deref, Operator.PLUS, new ExpressionConstNum(offsetOfThisFieldWithinStruct, new TypeInt64()));
        fieldLoc = new ExpressionCast(fieldLoc, new TypePointer<>(struct.getFieldByName(field).getType()));
        //*(fieldLoc) = rvalue
        // System.out.println("bcdua " + struct + " " + field + " " + fieldLoc + " " + deref + " " + rvalue);
        return fieldLoc;
    }
    @Override
    public void generateTAC(IREmitter emit, TempVarUsage tempVars, VarInfo resultLocation) {
        if (input instanceof ExpressionPointerDeref) {
            new ExpressionPointerDeref(getDeref()).generateTAC(emit, tempVars, resultLocation);
        } else {
            VarInfo fieldLabel = generateFieldLabel(emit, tempVars);
            emit.emit(new TACConst(resultLocation, fieldLabel));
        }
    }
    public VarInfo generateFieldLabel(IREmitter emit, TempVarUsage tempVars) {
        VarInfo structLocation = tempVars.getTempVar(input.getType());
        input.generateTAC(emit, tempVars, structLocation);
        int structLocationOnStack = structLocation.getStackLocation();
        int offsetOfThisFieldWithinStruct = struct.getFieldByName(field).getStackLocation();
        int fieldLocationOnStack = structLocationOnStack + offsetOfThisFieldWithinStruct;
        //System.out.println(structLocationOnStack + " " + offsetOfThisFieldWithinStruct + " " + fieldLocationOnStack + " " + struct.getFieldByName(field));
        String info = struct.toString() + "-" + structLocation.getName() + "." + field;
        VarInfo fieldLabel = tempVars.registerLabelManually(fieldLocationOnStack, struct.getFieldByName(field).getType(), info);
        return fieldLabel;
    }
    @Override
    protected int calculateTACLength() {
        if (input instanceof ExpressionPointerDeref) {
            return new ExpressionPointerDeref(getDeref()).getTACLength();//lol
        } else {
            return input.getTACLength() + 1;
        }
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
    @Override
    public Command setValue(Expression rvalue, Context context) {
        int offsetOfThisFieldWithinStruct = struct.getFieldByName(field).getStackLocation();
        if (input instanceof ExpressionPointerDeref) {
            Expression fieldLoc = getDeref();
            if (!(rvalue.getType().equals(((TypePointer) fieldLoc.getType()).pointingTo()))) {
                throw new RasterFormatException(rvalue.getType() + " " + fieldLoc.getType());
            }
            return new CommandSetPtr(context, fieldLoc, rvalue);
        } else if (input instanceof ExpressionVariable) {
            //input.field=rvalue
            X86Param thisVariable = context.get(((ExpressionVariable) input).name);
            class CommandSetStructField extends Command {
                Expression insert;
                int stackLoc;
                CommandSetStructField(Context aids, int stackLoc, Expression insert) {
                    super(aids);
                    this.stackLoc = stackLoc;
                    this.insert = insert;
                    if (!insert.getType().equals(struct.getFieldByName(field).getType())) {
                        throw new ReadOnlyFileSystemException();
                    }
                }
                @Override
                protected void generateTAC0(IREmitter emit) {
                    TempVarUsage cancer = new TempVarUsage(context);
                    VarInfo tam = cancer.getTempVar(insert.getType());
                    insert.generateTAC(emit, cancer, tam);
                    String info = struct.toString() + "-" + tam + "." + field;
                    VarInfo thisField = cancer.registerLabelManually(stackLoc, struct.getFieldByName(field).getType(), info);
                    emit.emit(new TACConst(thisField, tam));
                }
                @Override
                protected int calculateTACLength() {
                    return insert.getTACLength() + 1;
                }
                @Override
                public void staticValues() {
                    insert = insert.insertKnownValues(context);
                    insert = insert.calculateConstants();
                }
            }
            return new CommandSetStructField(context, ((VarInfo) thisVariable).getStackLocation() + offsetOfThisFieldWithinStruct, rvalue);
        } else {
            throw new UnsupportedOperationException(input + " ", new MalformedParameterizedTypeException());
        }
    }
    @Override
    public void generateConditionalJump(IREmitter emit, TempVarUsage tempVars, int jumpTo, boolean invert) {
        if (input instanceof ExpressionPointerDeref) {
            new ExpressionPointerDeref(getDeref()).generateConditionalJump(emit, tempVars, jumpTo, invert);
        } else {
            emit.emit(new TACJumpBoolVar(generateFieldLabel(emit, tempVars), jumpTo, invert));
        }
    }
    @Override
    public int condLength() {
        if (input instanceof ExpressionPointerDeref) {
            return new ExpressionPointerDeref(getDeref()).condLength();
        } else {
            return input.getTACLength() + 1;
        }
    }
}
