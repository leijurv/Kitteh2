/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.preprocess;
import compiler.parse.Line;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author leijurv
 */
public class StripComments {
    int lineNumber;
    public List<Line> transform(String line) {
        lineNumber = 1;
        try {
            return actualTransform(line);
        } catch (Exception e) {
            throw new RuntimeException("Exception while stripping comments from line " + lineNumber, e);
        }
    }
    private List<Line> actualTransform(String line) {
        boolean inString = false;
        char strType = 0;
        char prevChar = 0;
        StringBuilder transformed = new StringBuilder();
        boolean inComment = false;
        boolean commentEndsWithNewLine = false;
        int lineNumberOfBegin = -1;
        ArrayList<Line> result = new ArrayList<>();
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '\n') {
                if (!inComment || commentEndsWithNewLine) {
                    result.add(new Line(transformed.toString(), lineNumber));
                    transformed = new StringBuilder();
                }
                lineNumber++;//doesn't matter if we are in a comment, a string, or whatever, a newline in the raw input means a newline.
            }
            if (!inComment && (ch == '"' || ch == '\'') && prevChar != '\\' && !(inString && ch != strType)) {
                inString = !inString;
                strType = ch;
                lineNumberOfBegin = lineNumber;
            }
            if (!inString) {
                switch (ch) {
                    case '*':
                        if (prevChar == '/') {
                            //     /*
                            if (!inComment) {
                                transformed = new StringBuilder(transformed.toString().substring(0, transformed.toString().length() - 1));//since this is a /* we need to cut off the /
                            }
                            inComment = true;
                            lineNumberOfBegin = lineNumber;
                            commentEndsWithNewLine = false;
                        }
                        break;
                    case '/':
                        if (prevChar == '*') {// */
                            if (!inComment) {
                                throw new IllegalStateException("Ending comment with */ where no comment was started");
                            }
                            inComment = false;
                            prevChar = '/';
                            continue;//you can't break within a forswitch (it breaks the switch not the for), but you CAN continue
                        }
                        if (prevChar == '/' && !inComment)/*  // */ {
                            //rest of line
                            transformed = new StringBuilder(transformed.toString().substring(0, transformed.toString().length() - 1));
                            inComment = true;
                            lineNumberOfBegin = lineNumber;
                            commentEndsWithNewLine = true;
                        }
                        break;
                }
            }
            if (inComment && commentEndsWithNewLine && ch == '\n') {
                inComment = false;
            }
            if (!inComment && ch != '\n') {
                transformed.append(ch);
            }
            prevChar = ch;
        }
        if (inComment && !commentEndsWithNewLine) {
            throw new IllegalStateException("Comment started with /* but not ended - began on line " + lineNumberOfBegin);
        }
        if (inString) {
            throw new IllegalStateException("String not ended - began on line " + lineNumberOfBegin);
        }
        //if (line.charAt(line.length() - 1) != '\n') {
        result.add(new Line(transformed.toString(), lineNumber));
        //}
        return result;
    }
}
