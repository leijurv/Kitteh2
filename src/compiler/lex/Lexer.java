/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.lex;
import compiler.Keyword;
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
            char ch = peek();//don't pop yet, readAlphanumerical and readNumerical might need to do their thing
            if (alphabetical(ch)) {
                String lexeme = readAlphanumerical();
                Keyword key = Keyword.strToKeyword(lexeme);
                if (key != null) {
                    emit(KEYWORD.create(key));
                    continue;
                }
                emit(VARIABLE.create(lexeme));//if it's not a keyword, assume that it's a variable
                continue;
            }
            if (numerical(ch)) {
                //TODO negative numbers
                //it's nontrivial because a - and then a number can mean something else (like i-5) or really negative (like i= -5)
                //negative numbers are in the parser not the lexer I think... =/
                String lexeme = readNumerical();
                emit(NUM.create(lexeme));
                continue;
            }
            pop();//pop "ch" because at this point we know we're going to use it
            if (has() && TokenMapping.mapsToToken(ch + "" + peek())) {
                //if this character and the next character (if present) forms a token, pop the second character and emit the compound token
                emit(TokenMapping.getStaticToken(ch + "" + pop()));
                continue;
            }
            if (TokenMapping.mapsToToken(ch + "")) {
                //if this and the next don't form a compound token, check if this one on its own does
                emit(TokenMapping.getStaticToken(ch + ""));//if so, emit that token
                continue;
            }
            if (ch == ' ') {//spaces don't do anything i think
                continue;//TODO allow any of the blank stripped chars (like tab) in the middle of a line, not just space
            }
            if (ch == '{') {//lol idk man
                continue;
            }
            throw new FileSystemAlreadyExistsException("Unexpected " + ch);
        }
    }
    private String readAlphanumerical() {
        int start = pos();
        while (has()) {
            char ch = peek();
            if (alphabetical(ch) || numerical(ch)) {
                pop();
            } else {
                break;//don't pop the first character after this alphanumerical is over
            }
        }
        return substringSince(start);//instead of popping and appending to a stringbuilder (which would be like O(n^2) or something), we keep track of the beginning then at the end take a substring to get the whole range
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
                throw new IllegalStateException("This isn't ok. You're probably trying to make a variable name start with a number. However you did it, you have a number then a letter: " + substringSince(start) + ch);
            } else {
                break;
            }
        }
        return substringSince(start);
    }
    private static boolean alphabetical(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }
    private static boolean numerical(char c) {
        return c >= '0' && c <= '9';
    }
}
