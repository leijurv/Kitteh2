/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.parse;
import compiler.token.TokenString;
import java.util.ArrayList;

/**
 *
 * @author leijurv
 */
public class LineGrouper {
    public static ArrayList<Object> groupAdjacentIntoLines(ArrayList<Object> lines) {
        //We have a mix of Strings (code) and Constants (which represent strings in code)
        ArrayList<Object> result = new ArrayList<>();
        ArrayList<Object> temp = new ArrayList<>();
        for (Object o : lines) {
            if (o instanceof String) {
                if (temp.isEmpty()) {
                    temp.add(o);
                } else {
                    Object prev = temp.get(temp.size() - 1);
                    if (prev instanceof String) {
                        //2 strings in a row
                        //the previous string ended a line, this one is a new line
                        result.add(new Line(temp));
                        temp = new ArrayList<>();
                        temp.add(o);
                    } else {
                        if (!(prev instanceof TokenString)) {
                            throw new IllegalStateException("what");
                        }
                        temp.add(o);
                    }
                }
            } else if (o instanceof TokenString) {
                temp.add(o);
            } else {
                if (!temp.isEmpty()) {
                    result.add(new Line(temp));//When we hit something that isn't code (like a parsed block), it's not part of the line
                    temp = new ArrayList<>();//dont clear, make new because we passed it by reference not value
                }
                result.add(o);
            }
        }
        if (!temp.isEmpty()) {
            result.add(new Line(temp));
        }
        return result;
    }
}
