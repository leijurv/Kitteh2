/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.lex;
import compiler.Keyword;
import compiler.Operator;
import compiler.token.Token;
import compiler.token.TokenDecrement;
import compiler.token.TokenIncrement;
import compiler.token.TokenKeyword;
import compiler.token.TokenNot;
import compiler.token.TokenNum;
import compiler.token.TokenOperator;
import compiler.token.TokenSetEqual;
import compiler.token.TokenVariable;
import java.nio.file.FileSystemAlreadyExistsException;
import java.util.ArrayList;

/**
 *
 * @author leijurv
 */
public class Lexer extends AbstractLexer {
    private Lexer(String line) {
        super(line);
    }
    public static ArrayList<Token> lex(String line) {
        return new Lexer(line).lex();
    }
    @Override
    protected void runLex() {
        while (has()) {
            char ch = peek();
            boolean alpha = alphabetical(ch);
            boolean num = numerical(ch);
            if (alpha) {
                String lexeme = readAlphanumerical();
                Keyword key = Keyword.strToKeyword(lexeme);
                if (key != null) {
                    emit(new TokenKeyword(key));
                    continue;
                }
                emit(new TokenVariable(lexeme));
                continue;
            }
            if (num) {
                //TODO negative numbers
                //it's nontrivial because a - and then a number can mean something else (like i-5) or really negative (like i= -5)
                //negative numbers are in the parser not the lexer I think... =/
                String lexeme = readNumerical();
                emit(new TokenNum(lexeme));
                continue;
            }
            pop();
            if (TokenMapping.charMapsToToken(ch)) {
                emit(TokenMapping.getStaticToken(ch));
                continue;
            }
            switch (ch) {
                case ' '://spaces don't do anything i think
                case '{'://lol idk man
                    break;
                case ':':
                    if (peek() == '=') {
                        pop();
                        emit(new TokenSetEqual(true));
                        break;
                    }
                    throw new IllegalStateException("Literally the only usage of : is if it has a = after it");
                case '+'://TODO https://en.wikipedia.org/wiki/Augmented_assignment
                    if (peek() == '+') {
                        pop();
                        emit(new TokenIncrement());
                        break;
                    }
                    emit(new TokenOperator(Operator.PLUS));
                    break;
                case '-':
                    if (peek() == '-') {
                        pop();
                        emit(new TokenDecrement());
                        break;
                    }
                    emit(new TokenOperator(Operator.MINUS));
                    break;
                case '|':
                    if (peek() == '|') {
                        pop();
                        emit(new TokenOperator(Operator.OR));
                        break;
                    }
                    throw new IllegalStateException("What do you think this is, C? We don't have |");
                case '&':
                    if (peek() == '&') {
                        pop();
                        emit(new TokenOperator(Operator.AND));
                        continue;//lol I could do eiter continue or break and it'll have the same end result. live life on the edge
                    }
                    throw new IllegalStateException("What do you think this is, C? We don't have &");
                case '=':
                    if (peek() == '=') {
                        pop();
                        emit(new TokenOperator(Operator.EQUAL));
                        break;
                    }
                    emit(new TokenSetEqual(false));
                    break;
                case '<':
                    if (peek() == '=') {
                        pop();
                        emit(new TokenOperator(Operator.LESS_OR_EQUAL));
                        break;
                    }
                    emit(new TokenOperator(Operator.LESS));
                    break;
                case '>':
                    if (peek() == '=') {
                        pop();
                        emit(new TokenOperator(Operator.GREATER_OR_EQUAL));
                        break;
                    }
                    emit(new TokenOperator(Operator.GREATER));
                    break;
                case '!':
                    if (peek() == '=') {
                        pop();
                        emit(new TokenOperator(Operator.NOT_EQUAL));
                        break;
                    }
                    emit(new TokenNot());
                    break;
                default:
                    throw new FileSystemAlreadyExistsException("Unexpected " + ch);
            }
            //lol don't put anything here
        }
    }
    private String readAlphanumerical() {
        int start = pos();
        while (has()) {
            char ch = peek();
            if (alphabetical(ch) || numerical(ch)) {
                pop();
            } else {
                break;
            }
        }
        return substring(start);
    }
    private String readNumerical() {
        int start = pos();
        boolean hasHitPeriod = false;//lenny
        while (has()) {
            char ch = peek();
            if (numerical(ch) || ch == '.') {//numbers can have periods
                if (ch == '.' && hasHitPeriod) {
                    throw new IllegalStateException("Here's the thing. You said jackdaw is a crow");
                }
                if (ch == '.') {
                    hasHitPeriod = true;
                }
                pop();
            } else if (alphabetical(ch)) {
                throw new IllegalStateException("This isn't ok. You're probably trying to make a variable name start with a number. However you did it, you have a number then a letter: " + substring(start) + ch);
            } else {
                break;
            }
        }
        return substring(start);
    }
    private static boolean alphabetical(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }
    private static boolean numerical(char c) {
        return c >= '0' && c <= '9';
    }
}
