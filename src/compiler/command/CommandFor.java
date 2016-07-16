/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.command;
import compiler.Context;
import compiler.Keyword;
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
public class CommandFor extends Command implements KeywordCommand {
    Command initialization;
    Expression condition;
    Command afterthought;
    ArrayList<Command> contents;
    public CommandFor(Command initialization, Expression condition, Command afterthought, ArrayList<Command> contents, Context context) {
        super(context);
        this.initialization = initialization;
        this.condition = condition;
        this.afterthought = afterthought;
        this.contents = contents;
    }
    public CommandFor(Expression condition, ArrayList<Command> contents, Context context) {
        this(null, condition, null, contents, context);
    }
    public CommandFor(ArrayList<Command> contents, Context context) {
        //this.condition=new ExpressionConstant(Boolean.TRUE)
        this(null, null, null, contents, context);
    }
    @Override
    public Keyword getKeyword() {
        return Keyword.FOR;
    }
    @Override
    public String toString() {
        return "for(" + initialization + ";" + condition + ";" + afterthought + "){" + contents + "}";
    }
    @Override
    public void generateTAC0(IREmitter emit) {//TODO account for the fact that the init, condition, and / or afterthought might be null
        int afterItAll = emit.lineNumberOfNextStatement() + getTACLength();
        initialization.generateTAC(emit);
        int placeToJumpTo = emit.lineNumberOfNextStatement();
        //int conditionLen = ((ExpressionOperator) condition).condLength();
        //int afterLen = afterthought.getTACLength();
        //int afterItAll = placeToJumpTo + conditionLen + bodyLen + afterLen;
        //System.out.println(placeToJumpTo + " " + conditionLen + " " + bodyLen + " " + afterLen + " " + afterItAll);
        emit.updateContext(context);//I don't remember why this needs to be here, but if you remove it then compile something with a for loop, there will be an illegal state exception about the fitnessgram pacer test
        ((ExpressionConditionalJumpable) condition).generateConditionJump(emit, new TempVarUsage(context), afterItAll, true);//invert so if the condition isn't satisfied we skip the loop
        //note that the condition uses temp vars from within the for context. that's so it doesn't overwrite for vars between loop iterations
        for (Command com : contents) {
            com.generateTAC(emit);
        }
        afterthought.generateTAC(emit);
        emit.updateContext(context);//same deal here as above
        emit.emit(new TACJump(placeToJumpTo));
    }
    @Override
    protected int calculateTACLength() {
        int bodyLen = 0;
        for (Command com : contents) {
            bodyLen += com.getTACLength();
        }
        return initialization.getTACLength() + ((ExpressionConditionalJumpable) condition).condLength() + bodyLen + afterthought.getTACLength() + 1;//+1 for the jump from after the afterthought back to the condition
    }
    @Override
    public void staticValues() {
        //do NOT run on the init. if you do that, it'll assume that i will always be 0, even though it changes
        condition = condition.insertKnownValues(context);
        condition = condition.calculateConstants();
        afterthought.staticValues();
        for (Command com : contents) {
            com.staticValues();
        }
    }
}
