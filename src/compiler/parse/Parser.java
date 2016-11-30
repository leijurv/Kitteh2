/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.parse;
import compiler.Context;
import compiler.Keyword;
import compiler.command.Command;
import compiler.token.Token;
import static compiler.token.Token.is;
import static compiler.token.TokenType.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author leijurv
 */
class Parser {
    ArrayList<Command> parse(ArrayList<Object> lexed, Context context) {
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
    Command runLine(ArrayList<Object> lexed, Context context, int i) {
        Line l = (Line) lexed.get(i);
        try {
            if (i != lexed.size() - 1 && lexed.get(i + 1) instanceof ArrayList) {//this line begins a block
                ArrayList<Object> rawBlock = (ArrayList<Object>) lexed.remove(i + 1);
                List<Token> lineTokens = l.getTokens();
                if (lineTokens.isEmpty()) {
                    throw new IllegalStateException("come on it's like you're TRYING to break the parser. don't have { on a line on its own");
                }
                Token startToken = lineTokens.get(0);
                if (!is(startToken, KEYWORD)) {
                    throw new IllegalStateException("Line " + l + " is bad. It begins a block with {, but it doesn't begin with a TokenKeyword");
                }
                Keyword beginningKeyword = (Keyword) startToken;
                if (!beginningKeyword.canBeginBlock) {
                    throw new IllegalStateException("Hey guy, " + beginningKeyword + " can't be the beginning of a block");
                }
                List<Token> params = lineTokens.subList(1, lineTokens.size());
                switch (beginningKeyword) {
                    case FUNC:
                        return BlockBeginParser.parseFunctionDefinition(params, context, rawBlock);
                    case FOR:
                        return BlockBeginParser.parseFor(params, context, rawBlock);
                    case IF:
                        return BlockBeginParser.parseIf(params, context, rawBlock);
                    case STRUCT:
                        BlockBeginParser.parseStruct(params, context, rawBlock);
                        return null;
                    default:
                        throw new RuntimeException();
                }
            } else {
                if (context.isTopLevel()) {//nothing top level that isn't a block
                    throw new IllegalStateException("No globals except for function definitions and structs");
                }
                return LineParser.parseLine(l.getTokens(), context);
            }
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("Exception while parsing line")) {
                throw e;
            }
            throw new RuntimeException("Exception while parsing line " + l.num(), e);
        }
    }
}
