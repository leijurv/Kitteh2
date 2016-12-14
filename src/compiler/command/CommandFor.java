/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.command;
import compiler.Context;
import compiler.expression.Expression;
import compiler.expression.ExpressionConditionalJumpable;
import compiler.expression.ExpressionConst;
import compiler.expression.ExpressionConstBool;
import compiler.tac.IREmitter;
import compiler.tac.TACJump;
import compiler.tac.TempVarUsage;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author leijurv
 */
public class CommandFor extends CommandBlock {
    private Command initialization;
    private Expression condition;
    private Command afterthought;
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
        this(null, new ExpressionConstBool(true), null, contents, context);
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
        int previousBreakTo = emit.canBreak() ? emit.breakTo() : -1;
        int previousContinueTo = emit.canContinue() ? emit.continueTo() : -1;
        emit.setBreak(afterItAll);//a break ends the loop, so when there's a break, jump to after it all
        emit.setContinue(continueTo);//a continue skips the rest of the loop but goes to the afterthought
        for (Command com : contents) {//TODOIFIWANTTOKILLMYSELF make this parallel
            com.generateTAC(emit);
        }
        emit.clearBreakContinue();
        if (previousBreakTo != -1) {
            emit.setBreak(previousBreakTo);
        }
        if (previousContinueTo != -1) {
            emit.setContinue(previousContinueTo);
        }
        if (afterthought != null) {
            afterthought.generateTAC(emit);
        }
        emit.updateContext(context);//same deal here as above
        emit.emit(new TACJump(loopBegin));
    }
    @Override
    protected int calculateTACLength() {
        int bodyLen = contents.stream().mapToInt(Command::getTACLength).sum();
        int init = initialization != null ? initialization.getTACLength() : 0;
        int cond = condition != null ? ((ExpressionConditionalJumpable) condition).condLength() : 0;
        int aft = afterthought != null ? afterthought.getTACLength() : 0;
        return init + cond + bodyLen + aft + 1;//+1 for the jump from after the afterthought back to the condition
    }
    @Override
    public void staticValues() {
        if (initialization != null) {
            initialization = initialization.optimize();
            //since the afterthought is now included in getAllVarsModified
            //and getAllVarsModified all get cleared
            //it's fine to run static values on the initilization
        }
        List<String> varsMod = getAllVarsModified().collect(Collectors.toList());
        List<ExpressionConst> preKnown = varsMod.stream().map(context::knownValue).collect(Collectors.toList());
        //System.out.println("CLEARING " + varsMod);
        for (String s : varsMod) {
            context.clearKnownValue(s);
        }
        if (condition != null) {
            condition = condition.insertKnownValues(context);
            condition = condition.calculateConstants();
        }
        if (afterthought != null) {
            afterthought = afterthought.optimize();
        }
        for (int i = 0; i < contents.size(); i++) {
            contents.set(i, contents.get(i).optimize());
        }
        if (condition != null && condition instanceof ExpressionConstBool) {
            boolean wew = ((ExpressionConstBool) condition).getVal();
            if (!wew) {
                //for false{
                for (int i = 0; i < varsMod.size(); i++) {
                    if (preKnown.get(i) == null) {
                        context.clearKnownValue(varsMod.get(i));
                    } else {
                        context.setKnownValue(varsMod.get(i), preKnown.get(i));
                    }
                }
                return;//return before we clear all modified vars, because this for loop won't even run once.
            }
        }
        for (String s : varsMod) {//we gotta do it after too. if you set i=5 in the loop, you don't know if it's gonna be 5 later because it might not have executed
            context.clearKnownValue(s);
        }
    }
    @Override
    public Stream<String> getAllVarsModified() {
        Stream<String> mod = super.getAllVarsModified();
        if (afterthought != null) {
            return Stream.of(mod, afterthought.getAllVarsModified()).flatMap(x -> x);
        }
        return mod;
    }
}
