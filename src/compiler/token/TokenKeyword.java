/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.token;
import compiler.Keyword;

/**
 *
 * @author leijurv
 */
public class TokenKeyword extends Token {
    private final Keyword keyword;
    public TokenKeyword(Keyword keyword) {
        this.keyword = keyword;
    }
    @Override
    public String toString() {
        return "KEYWORD" + keyword.toString();
    }
    public Keyword getKeyword() {
        return keyword;
    }
}
