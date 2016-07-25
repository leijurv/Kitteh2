/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.preprocess;

/**
 *
 * @author leijurv
 */
public class StripComments {
    public String transform(String line) {
        boolean inString = false;
        char strType = 0;
        char prevChar = 0;
        String transformed = "";
        boolean inComment = false;
        boolean commentEndsWithNewLine = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (!inComment && (ch == '"' || ch == '\'') && prevChar != '\\' && !(inString && ch != strType)) {
                inString = !inString;
                strType = ch;
            }
            if (!inString) {
                switch (ch) {
                    case '*':
                        if (prevChar == '/') {
                            //     /*
                            if (!inComment) {
                                transformed = transformed.substring(0, transformed.length() - 1);//since this is a /* we need to cut off the /
                            }
                            inComment = true;
                            commentEndsWithNewLine = false;
                        }
                        break;
                    case '/':
                        if (prevChar == '*') {// */
                            if (!inComment) {
                                throw new IllegalStateException("Ending comment with */ where no comment was started: " + line);
                            }
                            inComment = false;
                            prevChar = '/';
                            continue;//you can't break within a forswitch (it breaks the switch not the for), but you CAN continue
                        }
                        if (prevChar == '/' && !inComment)/*  // */ {
                            //rest of line
                            transformed = transformed.substring(0, transformed.length() - 1);
                            inComment = true;
                            commentEndsWithNewLine = true;
                        }
                        break;
                }
            }
            if (inComment && commentEndsWithNewLine && ch == '\n') {//TODO I'm not sure how \r\n and similar work. idk if I need to deal with that
                inComment = false;
            }
            if (!inComment) {
                transformed += ch;
            }
            prevChar = ch;
        }
        if (inComment) {
            throw new IllegalStateException("Comment started with /* but not ended");
        }
        if (inString) {
            throw new IllegalStateException("String not ended");
        }
        return transformed;
    }
}
