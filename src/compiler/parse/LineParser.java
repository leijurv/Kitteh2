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
import compiler.command.CommandDefineFunction.FunctionHeader;
import compiler.command.CommandExp;
import compiler.command.CommandMultiSet;
import compiler.command.CommandReturn;
import compiler.command.CommandSetVar;
import compiler.expression.Expression;
import compiler.expression.ExpressionConstNum;
import compiler.expression.ExpressionFunctionCall;
import compiler.expression.ExpressionOperator;
import compiler.expression.ExpressionVariable;
import compiler.expression.Settable;
import compiler.parse.expression.ExpressionParser;
import compiler.token.Token;
import static compiler.token.TokenType.*;
import compiler.type.Type;
import compiler.type.TypeBoolean;
import compiler.type.TypeNumerical;
import compiler.type.TypePointer;
import compiler.type.TypeStruct;
import compiler.type.TypeVoid;
import compiler.util.Parse;
import java.nio.file.ClosedDirectoryStreamException;
import java.nio.file.ProviderMismatchException;
import java.util.Arrays;
import java.util.IllformedLocaleException;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author leijurv
 */
class LineParser {
    static Command parseLine(List<Token> tokens, Context context) {
        if (tokens.isEmpty()) {
            throw new IllegalStateException("what");
        }
        if (tokens.get(0).tokenType() == KEYWORD) {
            Keyword k = (Keyword) tokens.get(0);
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
                    FunctionHeader header = context.getCurrentFunction();
                    if (!tokens.contains(COMMA)) {
                        Type[] retTypes = header.getReturnTypes();
                        if (retTypes.length != 1) {
                            throw new IllegalStateException("Trying to return only one type when you need to return " + Arrays.asList(retTypes));
                        }
                        Type retType = retTypes[0];
                        if (tokens.size() == 1) {
                            //you're just doing "return" without a value, which is k
                            if (!(retType instanceof TypeVoid)) {
                                throw new IllegalStateException("you can't put a round peg in a square hole. or in this case, a void peg in a " + retType + " hole");
                            }
                            return new CommandReturn(context);
                        } else {
                            Expression ex;
                            if (retType instanceof TypeVoid) {
                                ex = ExpressionParser.parse(tokens.subList(1, tokens.size()), Optional.empty(), context); //we parse it here so that we know the type for the humorous error message
                                throw new IllegalStateException("you can't put a square peg in a round hole. or in this case, a " + ex.getType() + " peg in a void hole");
                            }
                            ex = ExpressionParser.parse(tokens.subList(1, tokens.size()), Optional.of(retType), context);
                            return new CommandReturn(context, ex);
                        }
                    } else {
                        List<List<Token>> splitted = Parse.splitList(tokens.subList(1, tokens.size()), COMMA);
                        Type[] retTypes = header.getReturnTypes();
                        if (retTypes.length != splitted.size()) {
                            throw new IllegalStateException("Trying to return " + splitted.size() + " vars when you need to return " + retTypes.length + " " + splitted + " " + Arrays.asList(retTypes));
                        }
                        Expression[] expressions = new Expression[retTypes.length];
                        for (int i = 0; i < retTypes.length; i++) {
                            expressions[i] = ExpressionParser.parse(splitted.get(i), Optional.of(retTypes[i]), context);
                        }
                        return new CommandReturn(context, expressions);
                    }
            }
        }
        if (tokens.stream().anyMatch(SEMICOLON)) {
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
            Type type = Parse.typeFromTokens(tokens.subList(0, tokens.size() - 1), context);
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
            if (tokens.get(tokens.size() - 1) == INCREMENT || tokens.get(tokens.size() - 1) == DECREMENT) {
                if (tokens.size() != 2 || !Token.is(tokens.get(0), VARIABLE)) {
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
        boolean inferType = (Boolean) tokens.get(eqLoc).data();
        if (tokens.subList(0, eqLoc).contains(COMMA)) {
            List<List<Token>> splitted = Parse.splitList(tokens.subList(0, eqLoc), COMMA);
            String[] variableNames = new String[splitted.size()];
            for (int i = 0; i < splitted.size(); i++) {
                List<Token> item = splitted.get(i);
                if (item.size() != 1) {
                    throw new RuntimeException(item + "");
                }
                if (item.get(0).tokenType() != VARIABLE) {
                    throw new RuntimeException(item + "");
                }
                variableNames[i] = (String) item.get(0).data();
            }
            Expression ex = ExpressionParser.parse(after, Optional.empty(), context);
            if (!(ex instanceof ExpressionFunctionCall)) {
                throw new RuntimeException("Multiple sets only for multi return function calls " + tokens);
            }
            ExpressionFunctionCall efc = (ExpressionFunctionCall) ex;
            Type[] types = efc.header().getReturnTypes();
            if (types.length != variableNames.length) {
                throw new RuntimeException("Setting " + variableNames.length + " variables to a function that returns " + types.length);
            }
            boolean settingNew = false;
            for (int i = 0; i < types.length; i++) {
                String name = variableNames[i];
                Type curr = context.get(name) == null ? null : context.get(name).getType();
                if (curr == null) {
                    settingNew = true;
                    context.setType(name, types[i]);
                } else {
                    if (!curr.equals(types[i])) {
                        throw new IllegalStateException("Trying to set " + curr + " " + name + " to " + types[i]);
                    }
                }
            }
            if (settingNew ^ inferType) {
                throw new IllegalStateException(settingNew + " " + inferType);
            }
            return new CommandMultiSet(context, (ExpressionFunctionCall) ex, variableNames);
        }
        if (eqLoc == 1) {
            //ok we just doing something like i=5
            if (tokens.get(0).tokenType() != VARIABLE) {
                throw new IllegalStateException("You can't set the value of " + tokens.get(0) + " lol");
            }
            String ts = (String) tokens.get(0).data();
            Type type = context.get(ts) == null ? null : context.get(ts).getType();
            if (inferType ^ (type == null)) {
                //look at that arousing use of xor
                throw new IllegalStateException("ur using it wrong " + inferType + " " + type + " " + tokens.get(eqLoc));
            }
            Expression ex = ExpressionParser.parse(after, Optional.ofNullable(type), context); //if type is null, that's fine because then there's no expected type, so we infer
            if (type != null && !ex.getType().equals(type)) {
                //if type was already set, we passed it to the expressionparser, so the result should be the same type
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
        Type type = Parse.typeFromTokens(tokens.subList(0, eqLoc - 1), context);
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
        Expression exp = ExpressionParser.parse(tokens.subList(0, eqLoc), Optional.empty(), context);
        //System.out.println("GETValue of " + exp);
        Expression right = ExpressionParser.parse(after, Optional.of(exp.getType()), context);
        Command result = ((Settable) exp).setValue(right, context);
        //System.out.println(tokens + " " + result + " " + exp + " " + right);
        return result;
    }
}
