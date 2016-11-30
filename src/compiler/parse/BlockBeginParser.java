/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.parse;
import compiler.Context;
import compiler.Struct;
import compiler.command.Command;
import compiler.command.CommandDefineFunction;
import compiler.command.CommandFor;
import compiler.command.CommandIf;
import compiler.expression.Expression;
import compiler.parse.expression.ExpressionParser;
import compiler.token.Token;
import static compiler.token.TokenType.*;
import compiler.type.Type;
import compiler.type.TypeBoolean;
import compiler.type.TypeVoid;
import compiler.util.Pair;
import java.lang.annotation.AnnotationTypeMismatchException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.management.openmbean.InvalidKeyException;
import javax.management.openmbean.KeyAlreadyExistsException;

/**
 *
 * @author leijurv
 */
class BlockBeginParser {
    static Command parseFunctionDefinition(List<Token> params, Context context, ArrayList<Object> rawBlock) {
        //ok this is going to be fun
        //func main(int i) int {
        if (params.get(0).tokenType() != VARIABLE) {
            throw new RuntimeException();
        }
        String functionName = (String) params.get(0).data();
        //System.out.println("FunctionName: " + functionName);
        if (params.get(1) != STARTPAREN) {
            throw new AnnotationTypeMismatchException(null, "");
        }
        int endParen = params.indexOf(ENDPAREN);
        if (endParen == -1) {
            throw new InvalidKeyException();
        }
        List<Token> returnType = params.subList(endParen + 1, params.size());
        //System.out.println("Return type: " + returnType);
        Type retType;
        if (Util.typeFromTokens(returnType, context) != null) {
            retType = Util.typeFromTokens(returnType, context);
        } else if (returnType.isEmpty()) {
            retType = new TypeVoid();
        } else {
            throw new IllegalStateException("no multiple returns yet. sorry!");
        }
        List<Pair<String, Type>> args = Util.splitList(params.subList(2, endParen), COMMA).stream().map((List<Token> tokenList) -> {
            List<Token> typeDefinition = tokenList.subList(0, tokenList.size() - 1);
            Type type = Util.typeFromTokens(typeDefinition, context);
            if (type == null) {
                throw new IllegalStateException(typeDefinition + "");
            }
            if (tokenList.get(tokenList.size() - 1).tokenType() != VARIABLE) {
                throw new RuntimeException();
            }
            String name = (String) tokenList.get(tokenList.size() - 1).data();
            return new Pair<>(name, type);
        }).collect(Collectors.toList());
        if (!context.isTopLevel()) {
            //make sure this is top level
            throw new InvalidParameterException();
        }
        Context subContext = context.subContext();
        int pos = 16; //args start at *(ebp+16) in order to leave room for rip and rbp on the call stack
        http://eli.thegreenplace.net/2011/09/06/stack-frame-layout-on-x86-64/
        for (Pair<String, Type> arg : args) {
            subContext.registerArgumentInput(arg.getKey(), arg.getValue(), pos);
            pos += arg.getValue().getSizeBytes();
        }
        CommandDefineFunction def = new CommandDefineFunction(subContext, retType, args, functionName, rawBlock);
        return def;
    }
    static Command parseFor(List<Token> params, Context context, ArrayList<Object> rawBlock) {
        //System.out.println("Parsing for loop with params " + params);
        int numSemis = (int) params.stream().filter(SEMICOLON).count(); //I really like streams lol
        switch (numSemis) {
            case 0: {
                // for{   OR  for i<5{
                Context sub = context.subContext();
                ArrayList<Command> blockCommands = Processor.parse(rawBlock, sub);
                if (params.isEmpty()) {
                    //for{
                    System.out.println("I'm a strong independant for loop that doesn't need no conditions");
                    return new CommandFor(blockCommands, sub);
                } else {
                    //for i<5
                    Expression condition = ExpressionParser.parse(params, Optional.of(new TypeBoolean()), sub);
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
                Command initialization = LineParser.parseLine(first, sub);
                Expression condition = ExpressionParser.parse(second, Optional.of(new TypeBoolean()), sub);
                ArrayList<Command> blockCommands = Processor.parse(rawBlock, sub); //this has to be run AFTER we parse the initialization. because the contents might use i, and i hasn't been set before we parse the initializer
                Command afterthought = LineParser.parseLine(third, sub);
                return new CommandFor(initialization, condition, afterthought, blockCommands, sub);
            }
            default:
                throw new IllegalStateException("what are you even doing");
        }
        //i can't put a break or a return here because it's unreachable atm
    }
    static Command parseIf(List<Token> params, Context context, ArrayList<Object> rawBlock) {
        //TODO else
        Expression condition = ExpressionParser.parse(params, Optional.of(new TypeBoolean()), context);
        //System.out.println("Parsed " + params + " to " + condition);
        Context sub = context.subContext();
        ArrayList<Command> blockCommands = Processor.parse(rawBlock, sub);
        return new CommandIf(condition, blockCommands, sub);
    }
    static void parseStruct(List<Token> params, Context context, ArrayList<Object> rawBlock) {
        if (params.size() != 1) {
            throw new KeyAlreadyExistsException();
        }
        if (params.get(0).tokenType() != VARIABLE) {
            throw new NumberFormatException();
        }
        String structName = (String) params.get(0).data();
        ArrayList<String> fieldNames = new ArrayList<>(rawBlock.size());
        ArrayList<Type> fieldTypes = new ArrayList<>(rawBlock.size());
        for (int j = 0; j < rawBlock.size(); j++) {
            Line thisLine = (Line) rawBlock.get(j);
            thisLine.lex();
            List<Token> tokens = thisLine.getTokens();
            //System.out.println(tokens);
            if (tokens.get(tokens.size() - 1).tokenType() != VARIABLE) {
                throw new RuntimeException();
            }
            fieldNames.add((String) tokens.get(tokens.size() - 1).data());
            Type tft = Util.typeFromTokens(tokens.subList(0, tokens.size() - 1), context, structName);
            if (tft == null) {
                throw new IllegalStateException("Unable to determine type of " + tokens.subList(0, tokens.size() - 1));
            }
            fieldTypes.add(tft);
        }
        //System.out.println(fieldNames);
        //System.out.println(fieldTypes);
        //System.out.println("Parsing struct " + params + " " + rawBlock);
        Struct struct = new Struct(structName, fieldTypes, fieldNames, context);
        context.defineStruct(struct);
    }
}
