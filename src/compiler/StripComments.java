/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler;

/**
 *
 * @author leijurv
 */
public class StripComments extends LineBasedTransform {/*   // */

    @Override
    public String transform(String line) {
        boolean inString = false;
        char strType = 0;
        char prevChar = 0;
        String transformed = "";
        boolean inComment = false;
        for (int i = 0; i < line.length(); i++) {
            boolean breakLoop = false;
            char ch = line.charAt(i);
            if (ch == '"' || ch == '\'') {
                if (prevChar != '\\') {//It's not a string thingy if the prev char was a \
                    if (!(inString && ch != strType)) {//if a string was started with ", and we see a ', don't end the string
                        inString = !inString;
                        if (inString) {
                            strType = ch;
                        }
                    }
                }
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
                            breakLoop = true;
                        }
                        break;
                }
            }
            if (breakLoop) {
                break;
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
