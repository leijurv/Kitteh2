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
import compiler.tac.TempVarUsage;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author leijurv
 */
public class CommandIf extends CommandBlock {
    private Expression condition;
    public CommandIf(Expression condition, ArrayList<Command> contents, Context context) {
        super(context, contents);
        this.condition = condition;
    }
    @Override
    public String toString() {
        return "if(" + condition + "){" + contents + "}";
    }
    @Override
    public void generateTAC0(IREmitter emit) {
        int jumpToAfter = emit.lineNumberOfNextStatement() + getTACLength();//if false, jump here
        ((ExpressionConditionalJumpable) condition).generateConditionalJump(emit, new TempVarUsage(context), jumpToAfter, true);//invert is true
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
        List<String> varsMod = getAllVarsModified();
        ArrayList<ExpressionConst> preKnown = varsMod.stream().map(a -> context.knownValue(a)).collect(Collectors.toCollection(ArrayList::new));
        for (Command com : contents) {
            com.staticValues();
        }
        if (condition instanceof ExpressionConstBool) {
            boolean isTrue = ((ExpressionConstBool) condition).getVal();
            if (!isTrue) {
                //set all known values back to what they were before
                //because this is "if(false){"
                //so whatever known values are inside should be ignored because it'll never be run
                for (int i = 0; i < varsMod.size(); i++) {
                    if (preKnown.get(i) == null) {
                        context.clearKnownValue(varsMod.get(i));
                    } else {
                        context.setKnownValue(varsMod.get(i), preKnown.get(i));
                    }
                }
            }
            return;
        }
        for (String s : getAllVarsModified()) {
            context.clearKnownValue(s);
        }
    }
}
