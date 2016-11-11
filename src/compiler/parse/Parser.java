/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.parse;
import compiler.Context;
import compiler.Keyword;
import compiler.Operator;
import compiler.command.Command;
import compiler.command.CommandBreak;
import compiler.command.CommandContinue;
import compiler.command.CommandDefineFunction;
import compiler.command.CommandExp;
import compiler.command.CommandFor;
import compiler.command.CommandIf;
import compiler.command.CommandReturn;
import compiler.command.CommandSetPtr;
import compiler.command.CommandSetVar;
import compiler.expression.Expression;
import compiler.expression.ExpressionConstNum;
import compiler.expression.ExpressionOperator;
import compiler.token.Token;
import compiler.token.TokenComma;
import compiler.token.TokenEndBrkt;
import compiler.token.TokenEndParen;
import compiler.token.TokenKeyword;
import compiler.token.TokenOperator;
import compiler.token.TokenSemicolon;
import compiler.token.TokenSetEqual;
import compiler.token.TokenStartBrkt;
import compiler.token.TokenStartParen;
import compiler.token.TokenVariable;
import compiler.type.Type;
import compiler.type.TypeBoolean;
import compiler.type.TypeInt32;
import compiler.type.TypePointer;
import compiler.type.TypeVoid;
import java.awt.image.RasterFormatException;
import java.util.ArrayList;
import java.util.IllformedLocaleException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javafx.util.Pair;

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
            Line l = (Line) o;
            if (i == lexed.size() - 1 || !(lexed.get(i + 1) instanceof ArrayList)) {//this line begins a block
                result.add(parseLine(l.getTokens(), context));
            } else {
                ArrayList<Object> rawBlock = (ArrayList<Object>) lexed.remove(i + 1);
                ArrayList<Token> lineTokens = l.getTokens();
                if (lineTokens.isEmpty()) {
                    throw new IllegalStateException("come on it's like you're TRYING to break the parser. don't have { on a line on its own");
                }
                Token startToken = lineTokens.get(0);
                if (!(startToken instanceof TokenKeyword)) {
                    throw new IllegalStateException("Line " + l + " is bad. It begins a block with {, but it doesn't begin with a TokenKeyword");
                }
                Keyword beginningKeyword = ((TokenKeyword) startToken).getKeyword();
                if (!beginningKeyword.canBeginBlock) {
                    throw new IllegalStateException("Hey guy, " + beginningKeyword + " can't be the beginning of a block");
                }
                List<Token> params = lineTokens.subList(1, lineTokens.size());
                switch (beginningKeyword) {
                    case FUNC:
                        //ok this is going to be fun
                        //func main(int i) int {
                        TokenVariable functionName = (TokenVariable) params.get(0);
                        System.out.println("FunctionName: " + functionName);
                        TokenStartParen beginArgumentList = (TokenStartParen) params.get(1);
                        int endParen = -1;
                        for (int j = 2; j < params.size(); j++) {
                            if (params.get(j) instanceof TokenEndParen) {
                                endParen = j;
                                break;
                            }
                        }
                        if (endParen == -1) {
                            throw new IllegalStateException();
                        }
                        List<Token> returnType = params.subList(endParen + 1, params.size());
                        System.out.println("Return type: " + returnType);
                        Type retType;
                        if (typeFromTokens(returnType) != null) {
                            retType = typeFromTokens(returnType);
                        } else if (returnType.isEmpty()) {
                            retType = new TypeVoid();
                        } else {
                            throw new IllegalStateException("no multiple returns yet. sorry!");
                        }
                        ArrayList<Pair<String, Type>> args = splitList(params.subList(2, endParen), TokenComma.class).stream().map(tokenList -> {
                            List<Token> typeDefinition = tokenList.subList(0, tokenList.size() - 1);
                            Type type = typeFromTokens(typeDefinition);
                            if (type == null) {
                                throw new IllegalStateException(typeDefinition + "");
                            }
                            TokenVariable tv = (TokenVariable) (tokenList.get(tokenList.size() - 1));
                            return new Pair<>(tv.val, type);
                        }).collect(Collectors.toCollection(ArrayList::new));
                        if (!context.isTopLevel()) {//make sure this is top level
                            throw new IllegalStateException();
                        }
                        Context subContext = new Context();//create new context because all funcs are top level
                        int pos = 16;//args start at *(ebp+16) in order to leave room for rip and rbp on the call stack
                        //source: http://eli.thegreenplace.net/2011/09/06/stack-frame-layout-on-x86-64/
                        for (Pair<String, Type> arg : args) {
                            subContext.registerArgumentInput(arg.getKey(), arg.getValue(), pos);
                            pos += arg.getValue().getSizeBytes();
                        }
                        CommandDefineFunction def = new CommandDefineFunction(subContext, retType, args, functionName.val, rawBlock);
                        result.add(def);
                        break;
                    case FOR:
                        System.out.println("Parsing for loop with params " + params);
                        int numSemis = (int) params.stream().filter(token -> token instanceof TokenSemicolon).count();//I really like streams lol
                        switch (numSemis) {
                            case 0: { // for{   OR  for i<5{
                                Context sub = context.subContext();
                                ArrayList<Command> blockCommands = Processor.parse(rawBlock, sub);
                                if (params.isEmpty()) {//for{
                                    System.out.println("I'm a strong independant for loop that doesn't need no conditions");
                                    result.add(new CommandFor(blockCommands, sub));
                                } else {//for i<5
                                    Expression condition = ExpressionParser.parse(params, Optional.of(new TypeBoolean()), sub);
                                    result.add(new CommandFor(condition, blockCommands, sub));
                                }
                                break;
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
                                result.add(new CommandFor(initialization, condition, afterthought, blockCommands, sub));
                                break;
                            }
                            default:
                                throw new IllegalStateException("what are you even doing");
                        }
                        break;
                    case IF://TODO else
                        Expression condition = ExpressionParser.parse(params, Optional.of(new TypeBoolean()), context);
                        System.out.println("Parsed " + params + " to " + condition);
                        Context sub = context.subContext();
                        ArrayList<Command> blockCommands = Processor.parse(rawBlock, sub);
                        result.add(new CommandIf(condition, blockCommands, sub));
                        break;
                    default:
                        throw new IllegalStateException("No parsing for block type \"" + beginningKeyword + '"');
                }
            }
        }
        return result;
    }
    public static List<List<Token>> splitList(List<Token> list, Class splitOn) {
        List<List<Token>> result = new ArrayList<>();
        List<Token> temp = new ArrayList<>();
        for (Token t : list) {
            if (t.getClass() == splitOn) {
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
            if (params.get(i) instanceof TokenSemicolon) {
                return i;
            }
        }
        return -1;
    }
    public static int lastSemicolon(List<Token> params) {
        for (int i = params.size() - 1; i >= 0; i--) {
            if (params.get(i) instanceof TokenSemicolon) {
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
        if (tokens.get(0) instanceof TokenKeyword) {
            TokenKeyword lol = (TokenKeyword) tokens.get(0);
            if (lol.getKeyword().canBeginBlock) {
                throw new IllegalStateException();
            }
            switch (lol.getKeyword()) {
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
        if (tokens.stream().anyMatch(token -> token instanceof TokenSemicolon)) {
            throw new IllegalStateException("I don't like semicolons");
        }
        int eqLoc = -1;
        for (int i = 0; i < tokens.size(); i++) {
            if (tokens.get(i) instanceof TokenSetEqual) {
                if (eqLoc != -1) {
                    throw new IllegalStateException("More than one '=' in " + tokens);
                }
                //ok wew
                eqLoc = i;
            }
        }
        if (eqLoc == -1) {
            //this isn't setting a variable, so it's an expression I think
            Expression ex = ExpressionParser.parse(tokens, Optional.empty(), context);
            System.out.println("Parsed " + tokens + " to " + ex);
            //only some expressions are okay
            //for example you couldn't just have "x+5" be a line on its own
            //some okay expressions to be lines on their own are: function calls, increments, and decrements
            if (!ex.canBeCommand()) {
                throw new IllformedLocaleException(ex + "");
            }
            return new CommandExp(ex, context);
        }
        List<Token> after = tokens.subList(eqLoc + 1, tokens.size());
        switch (eqLoc) {
            case 0:
                throw new IllegalStateException("Line cannot begin with =");
            case 1: {
                //ok we just doing something like i=5
                if (!(tokens.get(0) instanceof TokenVariable)) {
                    throw new IllegalStateException("You can't set the value of " + tokens.get(0) + " lol");
                }
                TokenVariable toSet = (TokenVariable) tokens.get(0);
                Type type = context.getType(toSet.val);
                boolean inferType = ((TokenSetEqual) tokens.get(eqLoc)).inferType;
                if (inferType ^ (type == null)) {//look at that arousing use of xor
                    throw new IllegalStateException("ur using it wrong " + inferType + " " + type);
                }
                Expression ex = ExpressionParser.parse(after, Optional.ofNullable(type), context);//if type is null, that's fine because then there's no expected type, so we infer
                if (type != null && !ex.getType().equals(type)) {//if type was already set, we passed it to the expressionparser, so the result should be the same type
                    throw new IllegalStateException(type + " " + ex.getType());
                }
                if (type == null) {
                    context.setType(toSet.val, ex.getType());
                }
                return new CommandSetVar(toSet.val, ex, context);
            }
            default: {
                //if the first token is a type, we are doing something like int i=5
                Type type = typeFromTokens(tokens.subList(0, eqLoc - 1));
                if (type == null) {
                    break;
                }
                if (!(tokens.get(eqLoc - 1) instanceof TokenVariable)) {
                    throw new IllegalStateException("You can't set the value of " + tokens.get(eqLoc - 1) + " lol");
                }
                TokenVariable toSet = (TokenVariable) tokens.get(eqLoc - 1);
                if (context.varDefined(toSet.val)) {
                    throw new IllegalStateException("Babe, " + toSet.val + " is already there");
                }
                Expression rightSide = ExpressionParser.parse(after, Optional.ofNullable(type), context);
                context.setType(toSet.val, rightSide.getType());
                //ok we doing something like long i=5
                return new CommandSetVar(toSet.val, rightSide, context);
            }
        }
        //ok so at this point we know it's a little more complicated than a simple variable definition or set
        if (tokens.get(0) instanceof TokenOperator) {
            if (((TokenOperator) tokens.get(0)).op == Operator.MULTIPLY) {
                //ok oh boy this is something like *x=y
                if (((TokenSetEqual) tokens.get(eqLoc)).inferType) {
                    throw new RasterFormatException("Can't infer type on a pointer reference");
                }
                //left side should be fine
                Expression leftSidePointer = ExpressionParser.parse(tokens.subList(1, eqLoc), Optional.empty(), context);//start at 1 because 0 would include the *
                TypePointer tp = (TypePointer) leftSidePointer.getType();
                Expression right = ExpressionParser.parse(after, Optional.of(tp.pointingTo()), context);
                return new CommandSetPtr(context, leftSidePointer, right);
            }
        }
        if (tokens.get(eqLoc - 1) instanceof TokenEndBrkt) {
            //a[b]=c
            int j = eqLoc - 2;
            int count = 1;
            while (j > 0) {
                if (tokens.get(j) instanceof TokenEndBrkt) {
                    count++;
                }
                if (tokens.get(j) instanceof TokenStartBrkt) {
                    count--;
                    break;
                }
                j--;
            }
            if (count != 0) {
                throw new RasterFormatException("");
            }
            Expression array = ExpressionParser.parse(tokens.subList(0, j), Optional.empty(), context);
            Expression index = ExpressionParser.parse(tokens.subList(j + 1, eqLoc - 1), Optional.of(new TypeInt32()), context);
            TypePointer tp = (TypePointer) array.getType();
            Type arrayContents = tp.pointingTo();
            ExpressionConstNum sizeofArrayContents = new ExpressionConstNum(arrayContents.getSizeBytes(), new TypeInt32());
            //so we want...
            //*(array + index * sizeof(arrayContents))
            Expression finalIndex = new ExpressionOperator(index, Operator.MULTIPLY, sizeofArrayContents);
            //*(array+finalIndex)
            Expression ptr = new ExpressionOperator(array, Operator.PLUS, finalIndex);
            //*(ptr)
            Expression right = ExpressionParser.parse(after, Optional.of(arrayContents), context);
            return new CommandSetPtr(context, ptr, right);
        }
        throw new IllegalStateException(tokens + "");
    }
    public static Type typeFromTokens(List<Token> tokens) {
        if (tokens.isEmpty()) {
            return null;
        }
        Token first = tokens.get(0);
        if (!(first instanceof TokenKeyword)) {
            return null;
        }
        Keyword keyword = ((TokenKeyword) first).getKeyword();
        if (!keyword.isType()) {
            return null;
        }
        Type tp = keyword.type;
        for (int i = 1; i < tokens.size(); i++) {
            if (!(tokens.get(i) instanceof TokenOperator)) {
                return null;
            }
            if (((TokenOperator) tokens.get(i)).op != Operator.MULTIPLY) {
                return null;
            }
            tp = new TypePointer(tp);//if there are N *s, it's a N - nested pointer, so for every *, wrap the type in another TypePointer
        }
        return tp;
    }
}
