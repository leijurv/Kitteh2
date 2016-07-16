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
import compiler.tac.TempVarUsage;
import java.util.ArrayList;

/**
 *
 * @author leijurv
 */
public class CommandIf extends Command implements KeywordCommand {
    ArrayList<Command> contents;
    Expression condition;
    public CommandIf(Expression condition, ArrayList<Command> contents, Context context) {
        super(context);
        this.contents = contents;
        this.condition = condition;
    }
    @Override
    public Keyword getKeyword() {
        return Keyword.IF;
    }
    @Override
    public String toString() {
        return "if(" + condition + "){" + contents + "}";
    }
    @Override
    public void generateTAC0(IREmitter emit) {
        int jumpToAfter = emit.lineNumberOfNextStatement() + getTACLength();//if false, jump here
        ((ExpressionConditionalJumpable) condition).generateConditionJump(emit, new TempVarUsage(context), jumpToAfter, true);//invert is true
        for (Command com : contents) {
            com.generateTAC(emit);
        }
    }
    @Override
    protected int calculateTACLength() {
        int sum = contents.parallelStream().mapToInt(com -> com.getTACLength()).sum();//parallel because calculating tac length can be slow, and it can be multithreaded /s
        return sum + ((ExpressionConditionalJumpable) condition).condLength();
    }
    @Override
    public void staticValues() {
        condition = condition.insertKnownValues(context);
        condition = condition.calculateConstants();
        for (Command com : contents) {
            com.staticValues();
        }
    }
}
