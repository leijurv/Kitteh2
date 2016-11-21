/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.parse;
import compiler.Context;
import compiler.Keyword;
import compiler.Operator;
import compiler.Struct;
import compiler.command.Command;
import compiler.command.CommandBreak;
import compiler.command.CommandContinue;
import compiler.command.CommandDefineFunction;
import compiler.command.CommandExp;
import compiler.command.CommandFor;
import compiler.command.CommandIf;
import compiler.command.CommandReturn;
import compiler.command.CommandSetVar;
import compiler.expression.Expression;
import compiler.expression.ExpressionConstNum;
import compiler.expression.ExpressionOperator;
import compiler.expression.ExpressionVariable;
import compiler.expression.Settable;
import compiler.token.Token;
import static compiler.token.Token.is;
import compiler.token.TokenType;
import static compiler.token.TokenType.*;
import compiler.type.Type;
import compiler.type.TypeBoolean;
import compiler.type.TypeNumerical;
import compiler.type.TypePointer;
import compiler.type.TypeStruct;
import compiler.type.TypeVoid;
import java.lang.annotation.AnnotationTypeMismatchException;
import java.nio.file.ClosedDirectoryStreamException;
import java.nio.file.ProviderMismatchException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.IllformedLocaleException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javafx.util.Pair;
import javax.management.openmbean.InvalidKeyException;
import javax.management.openmbean.KeyAlreadyExistsException;

/**
 *
 * @author leijurv
 */
public class Parser {
    public ArrayList<Command> parse(ArrayList<Object> lexed, Context context) {
        ArrayList<Command> result = new ArrayList<>();
        for (int i = 0; i < lexed.size(); i++) {
            Object o = lexed.get(i);
            if (!(o instanceof Line)) {//note that when this for loop starts, there will be things in it that aren't Lines, but as it goes through they are removed before it gets to them
                //e.g. ArrayLists representing blocks are removed when the previous line has a {
                throw new IllegalStateException(o.toString());
            }
            Command c = runLine(lexed, context, i);
            if (c != null) {
                result.add(c);
            }
        }
        return result;
    }
    @SuppressWarnings("unchecked")//ArrayList<Object> rawBlock = (ArrayList<Object>) lexed.remove(i + 1);
    public Command runLine(ArrayList<Object> lexed, Context context, int i) {
        Line l = (Line) lexed.get(i);
        try {
            if (i == lexed.size() - 1 || !(lexed.get(i + 1) instanceof ArrayList)) {//this line begins a block
                if (context.isTopLevel()) {
                    throw new IllegalStateException("No globals except for function definitions and structs");
                }
                return parseLine(l.getTokens(), context);
            } else {
                ArrayList<Object> rawBlock = (ArrayList<Object>) lexed.remove(i + 1);
                ArrayList<Token> lineTokens = l.getTokens();
                if (lineTokens.isEmpty()) {
                    throw new IllegalStateException("come on it's like you're TRYING to break the parser. don't have { on a line on its own");
                }
                Token startToken = lineTokens.get(0);
                if (!is(startToken, KEYWORD)) {
                    throw new IllegalStateException("Line " + l + " is bad. It begins a block with {, but it doesn't begin with a TokenKeyword");
                }
                Keyword beginningKeyword = (Keyword) startToken.data();
                if (!beginningKeyword.canBeginBlock) {
                    throw new IllegalStateException("Hey guy, " + beginningKeyword + " can't be the beginning of a block");
                }
                List<Token> params = lineTokens.subList(1, lineTokens.size());
                switch (beginningKeyword) {
                    case FUNC:
                        //ok this is going to be fun
                        //func main(int i) int {
                        if (params.get(0).tokenType() != VARIABLE) {
                            throw new RuntimeException();
                        }
                        String functionName = (String) ((Token) params.get(0)).data();
                        //System.out.println("FunctionName: " + functionName);
                        if (!is(params.get(1), STARTPAREN)) {
                            throw new AnnotationTypeMismatchException(null, "");
                        }
                        int endParen = -1;
                        for (int j = 2; j < params.size(); j++) {
                            if (params.get(j).tokenType() == ENDPAREN) {
                                endParen = j;
                                break;
                            }
                        }
                        if (endParen == -1) {
                            throw new InvalidKeyException();
                        }
                        List<Token> returnType = params.subList(endParen + 1, params.size());
                        //System.out.println("Return type: " + returnType);
                        Type retType;
                        if (typeFromTokens(returnType, context) != null) {
                            retType = typeFromTokens(returnType, context);
                        } else if (returnType.isEmpty()) {
                            retType = new TypeVoid();
                        } else {
                            throw new IllegalStateException("no multiple returns yet. sorry!");
                        }
                        ArrayList<Pair<String, Type>> args = splitList(params.subList(2, endParen), COMMA).stream().map(tokenList -> {
                            List<Token> typeDefinition = tokenList.subList(0, tokenList.size() - 1);
                            Type type = typeFromTokens(typeDefinition, context);
                            if (type == null) {
                                throw new IllegalStateException(typeDefinition + "");
                            }
                            if (tokenList.get(tokenList.size() - 1).tokenType() != VARIABLE) {
                                throw new RuntimeException();
                            }
                            String name = (String) tokenList.get(tokenList.size() - 1).data();
                            return new Pair<>(name, type);
                        }).collect(Collectors.toCollection(ArrayList::new));
                        if (!context.isTopLevel()) {//make sure this is top level
                            throw new InvalidParameterException();
                        }
                        Context subContext = context.subContext();
                        int pos = 16;//args start at *(ebp+16) in order to leave room for rip and rbp on the call stack
                        http://eli.thegreenplace.net/2011/09/06/stack-frame-layout-on-x86-64/
                        for (Pair<String, Type> arg : args) {
                            subContext.registerArgumentInput(arg.getKey(), arg.getValue(), pos);
                            pos += arg.getValue().getSizeBytes();
                        }
                        CommandDefineFunction def = new CommandDefineFunction(subContext, retType, args, functionName, rawBlock);
                        return def;
                    case FOR:
                        //System.out.println("Parsing for loop with params " + params);
                        int numSemis = (int) params.stream().filter(SEMICOLON::is).count();//I really like streams lol
                        switch (numSemis) {
                            case 0: { // for{   OR  for i<5{
                                Context sub = context.subContext();
                                ArrayList<Command> blockCommands = Processor.parse(rawBlock, sub);
                                if (params.isEmpty()) {//for{
                                    System.out.println("I'm a strong independant for loop that doesn't need no conditions");
                                    return new CommandFor(blockCommands, sub);
                                } else {//for i<5
                                    Expression condition = ExpressionParser.parse(params, Optional.of(new TypeBoolean()), sub);
                                    return new CommandFor(condition, blockCommands, sub);
                                }
                            }
                            case 2: {//for i:=0; i<1000; i++{
                                //I wish I could do params.split(TokenSemicolon)
                                int firstSemi = firstSemicolon(params);
                                int secondSemi = lastSemicolon(params);
                                ArrayList<Token> first = new ArrayList<>(params.subList(0, firstSemi));
                                ArrayList<Token> second = new ArrayList<>(params.subList(firstSemi + 1, secondSemi));
                                ArrayList<Token> third = new ArrayList<>(params.subList(secondSemi + 1, params.size()));
                                Context sub = context.subContext();
                                Command initialization = parseLine(first, sub);
                                ArrayList<Command> blockCommands = Processor.parse(rawBlock, sub);//this has to be run AFTER we parse the initialization. because the contents might use i, and i hasn't been set before we parse the initializer
                                Expression condition = ExpressionParser.parse(second, Optional.of(new TypeBoolean()), sub);
                                Command afterthought = parseLine(third, sub);
                                return new CommandFor(initialization, condition, afterthought, blockCommands, sub);
                            }
                            default:
                                throw new IllegalStateException("what are you even doing");
                        }
                    //i can't put a break or a return here because it's unreachable atm
                    case IF://TODO else
                        Expression condition = ExpressionParser.parse(params, Optional.of(new TypeBoolean()), context);
                        //System.out.println("Parsed " + params + " to " + condition);
                        Context sub = context.subContext();
                        ArrayList<Command> blockCommands = Processor.parse(rawBlock, sub);
                        return new CommandIf(condition, blockCommands, sub);
                    case STRUCT:
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
                            ArrayList<Token> tokens = thisLine.getTokens();
                            //System.out.println(tokens);
                            if (tokens.get(tokens.size() - 1).tokenType() != VARIABLE) {
                                throw new RuntimeException();
                            }
                            fieldNames.add((String) ((Token) tokens.get(tokens.size() - 1)).data());
                            Type tft = typeFromTokens(tokens.subList(0, tokens.size() - 1), context, structName);
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
                        break;
                    default:
                        throw new IllegalStateException("No parsing for block type \"" + beginningKeyword + '"');
                }
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Exception while parsing line " + l.num(), e);
        }
    }
    public static List<List<Token>> splitList(List<Token> list, TokenType splitOn) {
        List<List<Token>> result = new ArrayList<>();
        List<Token> temp = new ArrayList<>();
        for (Token t : list) {
            if (t.tokenType() == splitOn) {
                result.add(temp);
                temp = new ArrayList<>();
            } else {
                temp.add(t);
            }
        }
        if (!temp.isEmpty()) {
            result.add(temp);
        }
        return result;
    }
    public static int firstSemicolon(List<Token> params) {
        for (int i = 0; i < params.size(); i++) {
            if (params.get(i).tokenType() == SEMICOLON) {
                return i;
            }
        }
        return -1;
    }
    public static int lastSemicolon(List<Token> params) {
        for (int i = params.size() - 1; i >= 0; i--) {
            if (params.get(i).tokenType() == SEMICOLON) {
                return i;
            }
        }
        return -1;
    }
    public static Command parseLine(Line line, Context context) {
        return parseLine(line.getTokens(), context);
    }
    public static Command parseLine(ArrayList<Token> tokens, Context context) {
        if (tokens.isEmpty()) {
            throw new IllegalStateException("what");
        }
        if (tokens.get(0).tokenType() == KEYWORD) {
            Keyword k = (Keyword) tokens.get(0).data();
            if (k.canBeginBlock) {
                throw new ProviderMismatchException(k + "");
            }
            switch (k) {
                case BREAK:
                    if (tokens.size() != 1) {
                        throw new IllegalStateException("Break should be on a line on its own");
                    }
                    return new CommandBreak(context);
                case CONTINUE:
                    if (tokens.size() != 1) {
                        throw new IllegalStateException("Continue should be on a line on its own");
                    }
                    return new CommandContinue(context);
                case RETURN:
                    Expression ex = null;
                    Type retType = context.getCurrentFunctionReturnType();
                    if (tokens.size() == 1) {
                        //you're just doing "return" without a value, which is k
                        if (!(retType instanceof TypeVoid)) {
                            throw new IllegalStateException("you can't put a round peg in a square hole. or in this case, a void peg in a " + retType + " hole");
                        }
                    } else {
                        if (retType instanceof TypeVoid) {
                            ex = ExpressionParser.parse(tokens.subList(1, tokens.size()), Optional.empty(), context);//we parse it here so that we know the type for the humorous error message
                            throw new IllegalStateException("you can't put a square peg in a round hole. or in this case, a " + ex.getType() + " peg in a void hole");
                        }
                        ex = ExpressionParser.parse(tokens.subList(1, tokens.size()), Optional.of(retType), context);
                    }
                    return new CommandReturn(context, ex);
            }
        }
        if (tokens.stream().anyMatch(token -> token.tokenType() == SEMICOLON)) {
            throw new IllegalStateException("I don't like semicolons");
        }
        int eqLoc = -1;
        for (int i = 0; i < tokens.size(); i++) {
            if (tokens.get(i).tokenType() == SETEQUAL) {
                if (eqLoc != -1) {
                    throw new IllegalStateException("More than one '=' in " + tokens);
                }
                //ok wew
                eqLoc = i;
            }
        }
        if (eqLoc == 0) {
            throw new IllegalStateException("Line cannot begin with =");
        }
        if (eqLoc == -1) {
            Type type = typeFromTokens(tokens.subList(0, tokens.size() - 1), context);
            //System.out.println("Type: " + type + " " + tokens.subList(0, tokens.size() - 1) + " " + context);
            if (type != null) {
                if (tokens.get(tokens.size() - 1).tokenType() != VARIABLE) {
                    throw new IllegalStateException("You can't set the value of " + tokens.get(tokens.size() - 1) + " lol");
                }
                String ts = (String) tokens.get(tokens.size() - 1).data();
                if (context.varDefined(ts)) {
                    throw new IllegalStateException("Babe, " + ts + " is already there");
                }
                context.setType(ts, type);
                //ok we doing something like long i=5
                return null;
            }
            if (is(tokens.get(tokens.size() - 1), INCREMENT) || is(tokens.get(tokens.size() - 1), DECREMENT)) {
                if (tokens.size() != 2 || !is(tokens.get(0), VARIABLE)) {
                    throw new IllegalStateException("Currently you can only do single variables ++ or --");
                }
                String varName = (String) tokens.get(0).data();
                Type typ = context.get(varName).getType();
                if (typ instanceof TypePointer) {
                    throw new IllegalStateException("no?");
                }
                if (typ instanceof TypeBoolean) {
                    throw new IllegalStateException("no!");
                }
                if (typ instanceof TypeStruct) {
                    throw new IllegalStateException("NO!");
                }
                return new CommandSetVar(varName, new ExpressionOperator(new ExpressionVariable(varName, context), tokens.get(tokens.size() - 1).tokenType() == INCREMENT ? Operator.PLUS : Operator.MINUS, new ExpressionConstNum(1, (TypeNumerical) context.get(varName).getType())), context);
            }
            //this isn't setting a variable, so it's an expression I think
            Expression ex = ExpressionParser.parse(tokens, Optional.empty(), context);
            //System.out.println("Parsed " + tokens + " to " + ex);
            //only some expressions are okay
            //for example you couldn't just have "x+5" be a line on its own
            //some okay expressions to be lines on their own are: function calls, increments, and decrements
            if (!ex.canBeCommand()) {
                throw new IllformedLocaleException(ex + "");
            }
            return new CommandExp(ex, context);
        }
        List<Token> after = tokens.subList(eqLoc + 1, tokens.size());
        if (eqLoc == 1) {
            //ok we just doing something like i=5
            if (tokens.get(0).tokenType() != VARIABLE) {
                throw new IllegalStateException("You can't set the value of " + tokens.get(0) + " lol");
            }
            String ts = (String) tokens.get(0).data();
            Type type = context.getType(ts);
            boolean inferType = (Boolean) tokens.get(eqLoc).data();
            if (inferType ^ (type == null)) {//look at that arousing use of xor
                throw new IllegalStateException("ur using it wrong " + inferType + " " + type + " " + tokens.get(eqLoc));
            }
            Expression ex = ExpressionParser.parse(after, Optional.ofNullable(type), context);//if type is null, that's fine because then there's no expected type, so we infer
            if (type != null && !ex.getType().equals(type)) {//if type was already set, we passed it to the expressionparser, so the result should be the same type
                throw new IllegalStateException(type + " " + ex.getType());
            }
            if (type == null) {
                context.setType(ts, ex.getType());
            }
            return new CommandSetVar(ts, ex, context);
        }
        if ((Boolean) tokens.get(eqLoc).data()) {
            throw new ClosedDirectoryStreamException();
        }
        //if the first token is a type, we are doing something like int i=5
        Type type = typeFromTokens(tokens.subList(0, eqLoc - 1), context);
        if (type != null) {
            if (tokens.get(eqLoc - 1).tokenType() != VARIABLE) {
                throw new IllegalStateException("You can't set the value of " + tokens.get(eqLoc - 1) + " lol");
            }
            String ts = (String) tokens.get(eqLoc - 1).data();
            if (context.varDefined(ts)) {
                throw new IllegalStateException("Babe, " + ts + " is already there");
            }
            Expression rightSide = ExpressionParser.parse(after, Optional.of(type), context);
            context.setType(ts, rightSide.getType());
            //ok we doing something like long i=5
            return new CommandSetVar(ts, rightSide, context);
        }
        //---------------------------------------------------------------------------------------------------------------
        Expression exp = ExpressionParser.parse(tokens.subList(0, eqLoc), Optional.empty(), context);
        //System.out.println("GETValue of " + exp);
        Expression right = ExpressionParser.parse(after, Optional.of(exp.getType()), context);
        Command result = ((Settable) exp).setValue(right, context);
        //System.out.println(tokens + " " + result + " " + exp + " " + right);
        return result;
    }
    public static Type typeFromTokens(List<Token> tokens, Context context) {
        return typeFromTokens(tokens, context, null);
    }
    public static Type typeFromTokens(List<Token> tokens, Context context, String selfRef) {
        if (tokens.isEmpty()) {
            return null;
        }
        Token first = tokens.get(0);
        Type tp;
        if (first.tokenType() == KEYWORD) {
            Keyword keyword = (Keyword) first.data();
            if (!keyword.isType()) {
                return null;
            }
            tp = keyword.type;
        } else if (first.tokenType() == VARIABLE) {
            String name = (String) first.data();
            if (name.equals(selfRef)) {
                tp = new TypeStruct(null);
            } else {
                Struct struct = context.getStruct(name);
                if (struct == null) {
                    return null;
                }
                tp = new TypeStruct(struct);
            }
        } else {
            return null;
        }
        for (int i = 1; i < tokens.size(); i++) {
            if (tokens.get(i).tokenType() != OPERATOR) {
                return null;
            }
            if (tokens.get(i).data() != Operator.MULTIPLY) {
                return null;
            }
            tp = new <Type>TypePointer<Type>(tp);//if there are N *s, it's a N - nested pointer, so for every *, wrap the type in another TypePointer
        }
        return tp;
    }
}
