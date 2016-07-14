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
public class CommandIf extends Command implements KeywordCommand {
    ArrayList<Command> contents;
    Expression condition;
    public CommandIf(Expression condition, ArrayList<Command> contents) {
        this.contents = contents;
        this.condition = condition;
    }
    @Override
    public Keyword getKeyword() {
        return Keyword.IF;
    }
    public String toString() {
        return "if(" + condition + "){" + contents + "}";
    }
    @Override
    public void generateTAC(Context context, IREmitter emit) {
        int jumpToAfter = emit.lineNumberOfNextStatement() + getTACLength();//if false, jump here
        ((ExpressionOperator) condition).generateConditionJump(context, emit, new TempVarUsage(), jumpToAfter, true);//invert is true
        for (Command com : contents) {
            com.generateTAC(context, emit);
        }
    }
    @Override
    protected int calculateTACLength() {
        int sum = 0;
        for (Command command : contents) {
            sum += command.getTACLength();
        }
        return sum + ((ExpressionOperator) condition).condLength();
    }
}
