/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.parse;
import compiler.Context;
import compiler.Keyword;
import compiler.command.Command;
import compiler.preprocess.Line;
import compiler.preprocess.Line.LineException;
import compiler.token.Token;
import static compiler.token.Token.is;
import static compiler.token.TokenType.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
            Optional<Command> c = runLine(lexed, context, i);
            if (c.isPresent()) {
                result.add(c.get());
            }
        }
        return result;
    }
    @SuppressWarnings("unchecked")//ArrayList<Object> rawBlock = (ArrayList<Object>) lexed.remove(i + 1);
    Optional<Command> runLine(ArrayList<Object> lexed, Context context, int i) {
        Line l = (Line) lexed.get(i);
        try {
            if (i != lexed.size() - 1 && lexed.get(i + 1) instanceof ArrayList) {//this line begins a block
                ArrayList<Object> rawBlock = (ArrayList<Object>) lexed.remove(i + 1);
                List<Token> lineTokens = l.getTokens();
                if (lineTokens.isEmpty()) {
                    throw new IllegalStateException("come on it's like you're TRYING to break the parser. don't have { on a line on its own");
                    //TODO maybe in the future allow this lol. C and Java do
                }
                Token startToken = lineTokens.get(0);
                List<Token> params = lineTokens.subList(1, lineTokens.size());
                if (!is(startToken, KEYWORD)) {//ASS U ME that it's a function definition
                    startToken = Keyword.FUNC;
                    params = lineTokens;
                    //throw new IllegalStateException("Line " + l + " is bad. It begins a block with {, but it doesn't begin with a TokenKeyword");
                }
                Keyword beginningKeyword = (Keyword) startToken;
                if (!beginningKeyword.canBeginBlock) {
                    throw new IllegalStateException("Hey guy, " + beginningKeyword + " can't be the beginning of a block");
                }
                switch (beginningKeyword) {//TODO hey cAN WE USE REFELCT HERE??????
                    case FUNC:
                        return Optional.of(BlockBeginParser.parseFunctionDefinition(params, context, rawBlock));
                    case FOR:
                        return Optional.of(BlockBeginParser.parseFor(params, context, rawBlock));
                    case IF:
                        if (i != lexed.size() - 1) {
                            List<Token> nex = ((Line) lexed.get(i + 1)).getTokens();
                            if (nex.equals(Arrays.asList(Keyword.ELSE))) {
                                lexed.remove(i + 1);
                                ArrayList<Object> elseBlock = (ArrayList<Object>) lexed.remove(i + 1);
                                return Optional.of(BlockBeginParser.parseIf(params, context, rawBlock, elseBlock));
                            }
                        }
                        return Optional.of(BlockBeginParser.parseIf(params, context, rawBlock));
                    case STRUCT:
                        BlockBeginParser.parseStruct(params, context, rawBlock);
                        return Optional.empty();
                    case ELSE:
                        throw new RuntimeException("improper placement of else");
                    default:
                        throw new RuntimeException();
                }
            } else {
                if (l.getTokens().get(0).equals(Keyword.IMPORT)) {//TODO this is actually terrible
                    if (!context.isTopLevel()) {
                        throw new IllegalStateException("Imports are top level");
                    }
                    String raw = l.raw().substring(6).trim();
                    String[] args = raw.split(" ");
                    if (args[0].length() == 0 || args.length > 2 || (args.length == 2 && args[1].length() == 0)) {
                        throw new IllegalStateException(l.getTokens() + "");
                    }
                    String data = args[0];
                    String alias = args.length == 2 ? args[1] : null;//data.substring(data.lastIndexOf('/') + 1);
                    context.addImport(data, alias);
                    return Optional.empty();
                }
                if (context.isTopLevel()) {//nothing top level that isn't a block
                    throw new IllegalStateException("No globals except for function definitions and structs");
                }
                return Optional.ofNullable(LineParser.parseLine(l.getTokens(), context));//TODO have LineParser.parseLine return an Optional<Command> proper instead of a Command that can be null
            }
        } catch (RuntimeException e) {
            throw e instanceof LineException ? e : l.new <RuntimeException>LineException(e, "parsing");
        }
    }
}
