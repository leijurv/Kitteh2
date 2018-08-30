/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.parse;
import compiler.Context;
import compiler.command.Command;
import compiler.command.CommandDefineFunction;
import compiler.command.CommandFor;
import compiler.command.CommandIf;
import compiler.expression.Expression;
import compiler.expression.ExpressionConditionalJumpable;
import compiler.parse.expression.ExpressionParser;
import compiler.token.Token;
import static compiler.token.TokenType.*;
import compiler.type.TypeBoolean;
import compiler.type.TypeStruct;
import java.lang.annotation.AnnotationTypeMismatchException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.management.openmbean.KeyAlreadyExistsException;

/**
 *
 * @author leijurv
 */
class BlockBeginParser {
    private BlockBeginParser() {
    }
    public static Command parseFunctionDefinition(List<Token> params, Context context, ArrayList<Object> rawBlock) {
        //ok this is going to be fun
        //func main(int i) int {
        if (params.get(0).tokenType() != VARIABLE) {
            throw new IllegalStateException();
        }
        String functionName = (String) params.get(0).data();
        //System.out.println("FunctionName: " + functionName);
        if (params.get(1) != STARTPAREN) {
            throw new AnnotationTypeMismatchException(null, "" + params);
        }
        //System.out.println("Return type: " + returnType);
        if (!context.isTopLevel()) {
            //make sure this is top level
            throw new InvalidParameterException();
        }
        Context subContext = context.subContext();
        return new CommandDefineFunction(subContext, params, functionName, rawBlock);
    }
    public static Command parseFor(List<Token> params, Context context, ArrayList<Object> rawBlock) {
        //System.out.println("Parsing for loop with params " + params);
        int numSemis = (int) params.stream().filter(SEMICOLON).count(); //I really like streams lol
        switch (numSemis) {
            case 0: {
                // for{   OR  for i<5{
                Context sub = context.subContext();
                ArrayList<Command> blockCommands = Processor.parseRecursive(rawBlock, sub);
                if (params.isEmpty()) {
                    //for{
                    System.out.println("I'm a strong independant for loop that doesn't need no conditions");
                    return new CommandFor(blockCommands, sub);
                } else {
                    //for i<5
                    ExpressionConditionalJumpable condition = (ExpressionConditionalJumpable) ExpressionParser.parse(params, Optional.of(new TypeBoolean()), sub);
                    return new CommandFor(condition, blockCommands, sub);
                }
            }
            case 2: {
                //for i:=0; i<1000; i++{
                //I wish I could do params.split(TokenSemicolon)
                int firstSemi = params.indexOf(SEMICOLON);
                int secondSemi = params.lastIndexOf(SEMICOLON);
                ArrayList<Token> first = new ArrayList<>(params.subList(0, firstSemi));
                ArrayList<Token> second = new ArrayList<>(params.subList(firstSemi + 1, secondSemi));
                ArrayList<Token> third = new ArrayList<>(params.subList(secondSemi + 1, params.size()));
                Context sub = context.subContext();
                Command initialization = LineParser.parseLine(first, sub).orElseThrow(() -> new IllegalStateException("Missing for loop initialization"));
                ExpressionConditionalJumpable condition = (ExpressionConditionalJumpable) ExpressionParser.parse(second, Optional.of(new TypeBoolean()), sub);
                ArrayList<Command> blockCommands = Processor.parseRecursive(rawBlock, sub); //this has to be run AFTER we parse the initialization. because the contents might use i, and i hasn't been set before we parse the initializer
                Command afterthought = LineParser.parseLine(third, sub).orElseThrow(() -> new IllegalStateException("Missing for loop afterthought"));
                return new CommandFor(initialization, condition, afterthought, blockCommands, sub);
            }
            default:
                throw new IllegalStateException("what are you even doing");
        }
        //i can't put a break or a return here because it's unreachable atm
    }
    public static Command parseIf(List<Token> params, Context context, ArrayList<Object> rawBlock) {
        return parseIf(params, context, rawBlock, null);
    }
    static public Command parseIf(List<Token> params, Context context, ArrayList<Object> rawBlock, ArrayList<Object> elseBlock) {
        Expression condition = ExpressionParser.parse(params, Optional.of(new TypeBoolean()), context);
        //System.out.println("Parsed " + params + " to " + condition);
        Context ifTrue = context.subContext();
        Context ifFalse = elseBlock == null ? null : context.subContext();
        ArrayList<Command> blockCommands = Processor.parseRecursive(rawBlock, ifTrue);
        ArrayList<Command> elsa = elseBlock == null ? null : Processor.parseRecursive(elseBlock, ifFalse);
        return new CommandIf(condition, blockCommands, ifTrue, elsa, ifFalse);
    }
    static public void parseStruct(List<Token> params, Context context, ArrayList<Object> rawBlock) {
        if (params.size() != 1) {
            throw new KeyAlreadyExistsException();
        }
        if (params.get(0).tokenType() != VARIABLE) {
            throw new NumberFormatException();
        }
        String structName = (String) params.get(0).data();
        TypeStruct struct = new TypeStruct(structName, rawBlock, context);
        context.defineStruct(struct);
    }
}
