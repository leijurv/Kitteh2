/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler;
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
    public CommandFor(Command initialization, Expression condition, Command afterthought, ArrayList<Command> contents) {
        this.initialization = initialization;
        this.condition = condition;
        this.afterthought = afterthought;
        this.contents = contents;
    }
    public CommandFor(Expression condition, ArrayList<Command> contents) {
        this(null, condition, null, contents);
    }
    public CommandFor(ArrayList<Command> contents) {
        //this.condition=new ExpressionConstant(Boolean.TRUE)
        this(null, null, null, contents);
    }
    @Override
    public Keyword getKeyword() {
        return Keyword.FOR;
    }
    public String toString() {
        return "for(" + initialization + ";" + condition + ";" + afterthought + "){" + contents + "}";
    }
    @Override
    public void generateTAC(Context context, IREmitter emit) {
        int afterItAll = emit.lineNumberOfNextStatement() + getTACLength();
        initialization.generateTAC(context, emit);
        int placeToJumpTo = emit.lineNumberOfNextStatement();
        //int conditionLen = ((ExpressionOperator) condition).condLength();
        //int afterLen = afterthought.getTACLength();
        //int afterItAll = placeToJumpTo + conditionLen + bodyLen + afterLen;
        //System.out.println(placeToJumpTo + " " + conditionLen + " " + bodyLen + " " + afterLen + " " + afterItAll);
        ((ExpressionOperator) condition).generateConditionJump(context, emit, new TempVarUsage(), afterItAll, true);//invert so if the condition isn't satisfied we skip the loop
        for (Command com : contents) {
            com.generateTAC(context, emit);
        }
        afterthought.generateTAC(context, emit);
        emit.emit(new TACJump("true", placeToJumpTo, false));
    }
    @Override
    protected int calculateTACLength() {
        int bodyLen = 0;
        for (Command com : contents) {
            bodyLen += com.getTACLength();
        }
        return initialization.getTACLength() + ((ExpressionOperator) condition).condLength() + bodyLen + afterthought.getTACLength() + 1;//+1 for the jump from after the afterthought back to the condition
    }
}
