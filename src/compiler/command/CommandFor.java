/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.command;
import compiler.Context;
import compiler.expression.Expression;
import compiler.expression.ExpressionConditionalJumpable;
import compiler.tac.IREmitter;
import compiler.tac.TACJump;
import compiler.tac.TempVarUsage;
import java.util.ArrayList;

/**
 *
 * @author leijurv
 */
public class CommandFor extends CommandBlock {
    private final Command initialization;
    private Expression condition;
    private final Command afterthought;
    public CommandFor(Command initialization, Expression condition, Command afterthought, ArrayList<Command> contents, Context context) {
        super(context, contents);
        this.initialization = initialization;
        this.condition = condition;
        this.afterthought = afterthought;
    }
    public CommandFor(Expression condition, ArrayList<Command> contents, Context context) {
        this(null, condition, null, contents, context);
    }
    public CommandFor(ArrayList<Command> contents, Context context) {
        //this.condition=new ExpressionConstant(Boolean.TRUE)
        this(null, null, null, contents, context);
    }
    @Override
    public String toString() {
        return "for(" + initialization + ";" + condition + ";" + afterthought + "){" + contents + "}";
    }
    @Override
    public void generateTAC0(IREmitter emit) {
        int afterItAll = emit.lineNumberOfNextStatement() + getTACLength();
        if (initialization != null) {
            initialization.generateTAC(emit);
        }
        int loopBegin = emit.lineNumberOfNextStatement();
        int continueTo = afterItAll - 1 - (afterthought == null ? 0 : afterthought.getTACLength());//continue should send it to the beginning of the afterthought
        //int conditionLen = ((ExpressionOperator) condition).condLength();
        //int afterLen = afterthought.getTACLength();
        //int afterItAll = placeToJumpTo + conditionLen + bodyLen + afterLen;
        //System.out.println(placeToJumpTo + " " + conditionLen + " " + bodyLen + " " + afterLen + " " + afterItAll);
        emit.updateContext(context);//I don't remember why this needs to be here, but if you remove it then compile something with a for loop, there will be an illegal state exception about the fitnessgram pacer test
        if (condition != null) {
            ((ExpressionConditionalJumpable) condition).generateConditionalJump(emit, new TempVarUsage(context), afterItAll, true);//invert so if the condition isn't satisfied we skip the loop
        }
        //note that the condition uses temp vars from within the for context. that's so it doesn't overwrite for vars between loop iterations
        emit.setBreak(afterItAll);//a break ends the loop, so when there's a break, jump to after it all
        emit.setContinue(continueTo);//a continue skips the rest of the loop but goes to the afterthought
        for (Command com : contents) {//TODOIFIWANTTOKILLMYSELF make this parallel
            com.generateTAC(emit);
        }
        emit.clearBreakContinue();
        if (afterthought != null) {
            afterthought.generateTAC(emit);
        }
        emit.updateContext(context);//same deal here as above
        emit.emit(new TACJump(loopBegin));
    }
    @Override
    protected int calculateTACLength() {
        int bodyLen = contents.parallelStream().mapToInt(com -> com.getTACLength()).sum();//parallel because calculating tac length can be slow, and it can be multithreaded /s
        int init = initialization != null ? initialization.getTACLength() : 0;
        int cond = condition != null ? ((ExpressionConditionalJumpable) condition).condLength() : 0;
        int aft = afterthought != null ? afterthought.getTACLength() : 0;
        return init + cond + bodyLen + aft + 1;//+1 for the jump from after the afterthought back to the condition
    }
    @Override
    public void staticValues() {
        //do NOT run on the init. if you do that, it'll assume that i will always be 0, even though it changes
        System.out.println("CLEARING " + getAllVarsModified());
        for (String s : getAllVarsModified()) {
            context.clearKnownValue(s);
        }
        if (condition != null) {
            condition = condition.insertKnownValues(context);
            condition = condition.calculateConstants();
        }
        if (afterthought != null) {
            afterthought.staticValues();
        }
        for (Command com : contents) {
            com.staticValues();
        }
        for (String s : getAllVarsModified()) {//we gotta do it after too. if you set i=5 in the loop, you don't know if it's gonna be 5 later because it might not have executed
            context.clearKnownValue(s);
        }
    }
    @Override
    public ArrayList<String> getAllVarsModified() {
        ArrayList<String> mod = super.getAllVarsModified();
        if (afterthought != null) {
            mod.addAll(afterthought.getAllVarsModified());
        }
        return mod;
    }
}
