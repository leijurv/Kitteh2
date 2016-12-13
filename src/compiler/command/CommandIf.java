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
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author leijurv
 */
public class CommandIf extends CommandBlock {
    private Expression condition;
    private ArrayList<Command> elseBlock;
    private Context ifFalse;
    public CommandIf(Expression condition, ArrayList<Command> contents, Context ifTrue, ArrayList<Command> elseBlock, Context ifFalse) {
        super(ifTrue, contents);
        this.condition = condition;
        this.elseBlock = elseBlock;
        this.ifFalse = ifFalse;
    }
    @Override
    public String toString() {
        if (elseBlock == null) {
            return "if(" + condition + "){" + contents + "}";
        } else {
            return "if(" + condition + "){" + contents + "}else{" + elseBlock + "}";
        }
    }
    @Override
    public void generateTAC0(IREmitter emit) {
        if (elseBlock == null) {
            int jumpToAfter = emit.lineNumberOfNextStatement() + getTACLength();//if false, jump here
            ((ExpressionConditionalJumpable) condition).generateConditionalJump(emit, new TempVarUsage(context), jumpToAfter, true);//invert is true
            for (Command com : contents) {
                com.generateTAC(emit);
            }
        } else {
            //condition, jump to 1 if false
            //iftrue
            //jump to 2
            //1:
            //iffalse
            //2:
            int jumpToIfFalse = emit.lineNumberOfNextStatement() + contents.parallelStream().mapToInt(Command::getTACLength).sum() + ((ExpressionConditionalJumpable) condition).condLength() + 1;
            ((ExpressionConditionalJumpable) condition).generateConditionalJump(emit, new TempVarUsage(context), jumpToIfFalse, true);//invert is true
            for (Command com : contents) {
                com.generateTAC(emit);
            }
            int jumpTo = emit.lineNumberOfNextStatement() + 1 + elseBlock.parallelStream().mapToInt(Command::getTACLength).sum();
            new TempVarUsage(ifFalse);
            emit.updateContext(ifFalse);
            emit.emit(new TACJump(jumpTo));
            for (Command com : elseBlock) {
                com.generateTAC(emit);
            }
        }
    }
    @Override
    protected int calculateTACLength() {
        if (elseBlock == null) {
            int sum = contents.parallelStream().mapToInt(Command::getTACLength).sum();//parallel because calculating tac length can be slow, and it can be multithreaded /s
            return sum + ((ExpressionConditionalJumpable) condition).condLength();
        } else {
            return contents.parallelStream().mapToInt(Command::getTACLength).sum() + elseBlock.parallelStream().mapToInt(Command::getTACLength).sum() + 1 + ((ExpressionConditionalJumpable) condition).condLength();
        }
    }
    @Override
    public void staticValues() {
        condition = condition.insertKnownValues(context);
        condition = condition.calculateConstants();
        List<String> trueMod = super.getAllVarsModified().collect(Collectors.toList());
        List<ExpressionConst> preKnownTrue = trueMod.stream().map(context::knownValue).collect(Collectors.toList());
        for (int i = 0; i < contents.size(); i++) {
            contents.set(i, contents.get(i).optimize());
        }
        if (condition instanceof ExpressionConstBool) {
            boolean isTrue = ((ExpressionConstBool) condition).getVal();
            if (!isTrue) {
                //set all known values back to what they were before
                //because this is "if(false){"
                //so whatever known values are inside should be ignored because it'll never be run
                for (int i = 0; i < trueMod.size(); i++) {
                    if (preKnownTrue.get(i) == null) {
                        context.clearKnownValue(trueMod.get(i));
                    } else {
                        context.setKnownValue(trueMod.get(i), preKnownTrue.get(i));
                    }
                }
            }
            //if true -> don't clear known values because they are still useful because this if statement is guaranteed to run
            //if false -> we just reset the known values to what they were before because it is guaranteed to not run
        } else {
            for (String s : trueMod) {
                context.clearKnownValue(s);
            }
        }
        if (elseBlock == null) {
            return;
        }
        List<String> elseMod = varsModElse();
        List<ExpressionConst> preKnownFalse = elseMod.stream().map(ifFalse::knownValue).collect(Collectors.toList());
        for (int i = 0; i < elseBlock.size(); i++) {
            elseBlock.set(i, elseBlock.get(i).optimize());
        }
        //System.out.println(elseMod + " " + preKnownFalse);
        if (condition instanceof ExpressionConstBool) {
            boolean isTrue = ((ExpressionConstBool) condition).getVal();
            if (isTrue) {
                //System.out.println("RESETTING");
                //set all known values back to what they were before
                //because this is "if(false){"
                //so whatever known values are inside should be ignored because it'll never be run
                for (int i = 0; i < elseMod.size(); i++) {
                    if (preKnownFalse.get(i) == null) {
                        context.clearKnownValue(elseMod.get(i));
                    } else {
                        context.setKnownValue(elseMod.get(i), preKnownFalse.get(i));
                    }
                }
            }
            return;//if false -> don't clear known values because they are still useful because this if statement is guaranteed to run
            //if true -> we just reset the known values to what they were before because it is guaranteed to not run
        }
        for (String s : elseMod) {
            context.clearKnownValue(s);
        }
    }
    @Override
    public Stream<String> getAllVarsModified() {
        if (elseBlock == null) {
            return super.getAllVarsModified();
        }
        return Stream.of(elseBlock, contents).flatMap(Collection::stream).flatMap(Command::getAllVarsModified);
    }
    public List<String> varsModElse() {
        return elseBlock.stream().flatMap(Command::getAllVarsModified).collect(Collectors.toList());
    }
}
