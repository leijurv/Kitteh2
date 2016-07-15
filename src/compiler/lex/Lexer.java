/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.lex;
import compiler.Keyword;
import compiler.Operator;
import compiler.token.Token;
import compiler.token.TokenComma;
import compiler.token.TokenDecrement;
import compiler.token.TokenEndParen;
import compiler.token.TokenIncrement;
import compiler.token.TokenKeyword;
import compiler.token.TokenNot;
import compiler.token.TokenNum;
import compiler.token.TokenOperator;
import compiler.token.TokenSemicolon;
import compiler.token.TokenSetEqual;
import compiler.token.TokenStartParen;
import compiler.token.TokenVariable;
import java.util.ArrayList;

/**
 *
 * @author leijurv
 */
public class Lexer extends AbstractLexer {
    public Lexer(String line) {
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
                String word = readAlphanumerical();
                Keyword key = Keyword.strToKeyword(word);
                if (key != null) {
                    emit(new TokenKeyword(key));
                    continue;
                }
                emit(new TokenVariable(word));
                continue;
            }
            if (num) {
                //TODO negative numbers
                //it's nontrivial because a - and then a number can mean something else (like i-5) or really negative (like i= -5)
                //negative numbers are in the parser not the lexer I think... =/
                String word = readNumerical();
                emit(new TokenNum(word));
                continue;
            }
            pop();
            switch (ch) {
                case ' '://spaces don't do anything i think
                case '{'://lol idk man
                    break;
                case '(':
                    emit(new TokenStartParen());
                    break;
                case ')':
                    emit(new TokenEndParen());
                    break;
                case ',':
                    emit(new TokenComma());
                    break;
                case ':':
                    if (peek() == '=') {
                        pop();
                        emit(new TokenSetEqual(true));
                        break;
                    }
                    throw new IllegalStateException("Literally the only usage of : is if it has a = after it");
                case ';':
                    emit(new TokenSemicolon());
                    break;
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
                case '*':
                    emit(new TokenOperator(Operator.MULTIPLY));
                    break;
                case '/':
                    emit(new TokenOperator(Operator.DIVIDE));
                    break;
                case '%':
                    emit(new TokenOperator(Operator.MOD));
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
                    throw new IllegalStateException("Unexpected " + ch);
            }
            //lol don't put anything here
        }
    }
    public String readAlphanumerical() {
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
    public String readNumerical() {
        int start = pos();
        boolean hasHitPeriod = false;//lenny
        while (has()) {
            char ch = peek();
            if (numerical(ch) || ch == '.') {//numbers can have periods
                if (ch == '.' && hasHitPeriod) {
                    throw new IllegalStateException("Here's the thing. You said jackdaw is a crow");
                }
                hasHitPeriod = true;
                pop();
            } else if (alphabetical(ch)) {
                throw new IllegalStateException("This isn't ok. You're probably trying to make a variable name start with a number. However you did it, you have a number then a letter: " + substring(start) + ch);
            } else {
                break;
            }
        }
        return substring(start);
    }
    public static boolean allAlpha(String s) {
        for (char c : s.toCharArray()) {
            if (!alphabetical(c)) {
                return false;
            }
        }
        return true;
    }
    public static boolean allNum(String s) {
        for (char c : s.toCharArray()) {
            if (!numerical(c)) {
                return false;
            }
        }
        return true;
    }
    public static boolean alphabetical(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }
    public static boolean numerical(char c) {
        return c >= '0' && c <= '9';
    }
}
