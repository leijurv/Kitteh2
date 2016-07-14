/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author leijurv
 */
public class Parser {
    public ArrayList<Command> parse(ArrayList<Object> lexed, Context context) {
        for (int i = 0; i < lexed.size(); i++) {
            Object o = lexed.get(i);
            if (o instanceof Line) {
                Line l = (Line) o;
                //idk what I was planning here, but in case I remember I'm gonna leave it
            }
        }
        ArrayList<Command> result = new ArrayList<>();
        for (int i = 0; i < lexed.size(); i++) {
            Object o = lexed.get(i);
            if (!(o instanceof Line)) {//note that when this for loop starts, there will be things in it that aren't Lines, but as it goes through they are removed before it gets to them
                //e.g. ArrayLists representing blocks are removed when the previous line has a {
                throw new IllegalStateException(o.toString());
            }
            Line l = (Line) o;
            if (i != lexed.size() - 1 && lexed.get(i + 1) instanceof ArrayList) {//this line begins a block
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
                ArrayList<Token> params = new ArrayList<>(lineTokens.subList(1, lineTokens.size()));//hey let's make this arraylist, for no reason
                switch (beginningKeyword) {
                    case FOR:
                        System.out.println("Parsing for loop with params " + params);
                        int numSemis = (int) params.stream().filter(token -> token instanceof TokenSemicolon).count();//I really like streams lol
                        switch (numSemis) {
                            case 0: // for{   OR  for i<5{
                                ArrayList<Command> blockCommands = Processor.parse(rawBlock, context.subContext());
                                if (params.isEmpty()) {//for{
                                    System.out.println("I'm a strong independant for loop that doesn't need no conditions");
                                    result.add(new CommandFor(blockCommands));
                                } else {//for i<5
                                    Expression condition = ExpressionParser.parse(params, Optional.of(new TypeBoolean()), context);
                                    result.add(new CommandFor(condition, blockCommands));
                                }
                                break;
                            case 2://for i:=0; i<1000; i++{
                                //I wish I could do params.split(TokenSemicolon)
                                int firstSemi = firstSemicolon(params);
                                int secondSemi = lastSemicolon(params);
                                ArrayList<Token> first = new ArrayList<>(params.subList(0, firstSemi));
                                ArrayList<Token> second = new ArrayList<>(params.subList(firstSemi + 1, secondSemi));
                                ArrayList<Token> third = new ArrayList<>(params.subList(secondSemi + 1, params.size()));
                                Context sub = context.subContext();
                                Command initialization = parseLine(first, sub);
                                blockCommands = Processor.parse(rawBlock, sub);//this has to be run AFTER we parse the initialization. because the contents might use i, and i hasn't been set before we parse the initializer
                                Expression condition = ExpressionParser.parse(second, Optional.of(new TypeBoolean()), sub);
                                Command afterthought = parseLine(third, sub);
                                result.add(new CommandFor(initialization, condition, afterthought, blockCommands));
                                break;
                            default:
                                throw new IllegalStateException("what are you even doing");
                        }
                        break;
                    case IF://TODO else
                        Expression condition = ExpressionParser.parse(params, Optional.of(new TypeBoolean()), context);
                        System.out.println("Parsed " + params + " to " + condition);
                        ArrayList<Command> blockCommands = Processor.parse(rawBlock, context.subContext());
                        result.add(new CommandIf(condition, blockCommands));
                        break;
                    default:
                        throw new IllegalStateException("Leif hasn't written parsing for block type \"" + beginningKeyword + '"');
                }
            } else {
                result.add(parseLine(l.getTokens(), context));
            }
        }
        return result;
    }
    public static int firstSemicolon(ArrayList<Token> params) {
        for (int i = 0; i < params.size(); i++) {
            if (params.get(i) instanceof TokenSemicolon) {
                return i;
            }
        }
        return -1;
    }
    public static int lastSemicolon(ArrayList<Token> params) {
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
            //TODO check if ex is one of those
            return new CommandExp(ex);
        }
        if (eqLoc == 0) {
            throw new IllegalStateException();
        }
        List<Token> after = tokens.subList(eqLoc + 1, tokens.size());
        if (eqLoc == 1) {
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
            if (type != null && ex.getType() != type) {//if type was already set, we passed it to the expressionparser, so the result should be the same type
                throw new IllegalStateException();
            }
            if (type == null) {
                context.setType(toSet.val, ex.getType());
            }
            return new CommandSetVar(toSet.val, ex);
        }
        if (eqLoc == 2) {
            if (!(tokens.get(1) instanceof TokenVariable)) {
                throw new IllegalStateException("You can't set the value of " + tokens.get(1) + " lol");
            }
            TokenVariable toSet = (TokenVariable) tokens.get(1);
            if (context.varDefined(toSet.val)) {
                throw new IllegalStateException("Babe, " + toSet.val + " is already there");
            }
            Type type = null;//TODO get type from tokens.get(0
            Expression rightSide = ExpressionParser.parse(after, Optional.ofNullable(type), context);
            context.setType(toSet.val, rightSide.getType());
            //ok we doing something like long i=5
            return new CommandSetVar(toSet.val, rightSide);
        }
        throw new IllegalStateException();
    }
}
