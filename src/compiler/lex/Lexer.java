/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.lex;
import compiler.Keyword;
import compiler.Operator;
import compiler.token.Token;
import static compiler.token.TokenType.*;
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
                    emit(KEYWORD.create(key));
                    continue;
                }
                emit(VARIABLE.create(lexeme));
                continue;
            }
            if (num) {
                //TODO negative numbers
                //it's nontrivial because a - and then a number can mean something else (like i-5) or really negative (like i= -5)
                //negative numbers are in the parser not the lexer I think... =/
                String lexeme = readNumerical();
                emit(NUM.create(lexeme));
                continue;
            }
            pop();
            if (TokenMapping.mapsToToken(ch)) {
                emit(TokenMapping.getStaticToken(ch + ""));
                continue;
            }
            if (TokenMapping.mapsToToken(ch + "" + peek())) {
                emit(TokenMapping.getStaticToken(ch + "" + pop()));
                continue;
            }
            switch (ch) {
                case ' '://spaces don't do anything i think
                case '{'://lol idk man
                    break;
                case ':':
                    if (peek() == '=') {
                        pop();
                        emit(SETEQUAL.create(true));
                        break;
                    }
                    throw new IllegalStateException("Literally the only usage of : is if it has a = after it");
                case '+'://TODO https://en.wikipedia.org/wiki/Augmented_assignment
                    if (peek() == '+') {
                        pop();
                        emit(INCREMENT);
                        break;
                    }
                    emit(OPERATOR.create(Operator.PLUS));
                    break;
                case '-':
                    if (peek() == '-') {
                        pop();
                        emit(DECREMENT);
                        break;
                    }
                    emit(OPERATOR.create(Operator.MINUS));
                    break;
                case '|':
                    if (peek() == '|') {
                        pop();
                        emit(OPERATOR.create(Operator.OR));
                        break;
                    }
                    throw new IllegalStateException("What do you think this is, C? We don't have |");
                case '&':
                    if (peek() == '&') {
                        pop();
                        emit(OPERATOR.create(Operator.AND));
                        continue;//lol I could do eiter continue or break and it'll have the same end result. live life on the edge
                    }
                    throw new IllegalStateException("What do you think this is, C? We don't have &");
                case '=':
                    if (peek() == '=') {
                        pop();
                        emit(OPERATOR.create(Operator.EQUAL));
                        break;
                    }
                    emit(SETEQUAL.create(false));
                    break;
                case '<':
                    if (peek() == '=') {
                        pop();
                        emit(OPERATOR.create(Operator.LESS_OR_EQUAL));
                        break;
                    }
                    emit(OPERATOR.create(Operator.LESS));
                    break;
                case '>':
                    if (peek() == '=') {
                        pop();
                        emit(OPERATOR.create(Operator.GREATER_OR_EQUAL));
                        break;
                    }
                    emit(OPERATOR.create(Operator.GREATER));
                    break;
                case '!':
                    if (peek() == '=') {
                        pop();
                        emit(OPERATOR.create(Operator.NOT_EQUAL));
                        break;
                    }
                    emit(NOT);
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
