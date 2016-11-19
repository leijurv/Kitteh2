/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.parse;
import java.util.ArrayList;

/**
 *
 * @author leijurv
 */
public class BlockFinder implements Transform<ArrayList<Object>> {
    public static void assertBlockBeginSane(Line line) {
        String str = line.raw();
        for (int i = 0; i < str.length() - 1; i++) {
            if (str.charAt(i) == '}' || str.charAt(i) == '{') {
                throw new IllegalStateException("lol what are you trying to do here: " + str + " line " + line.num());
            }
        }
        if (!str.endsWith("{")) {
            throw new IllegalStateException("lol what are you trying to do here: " + str + " line " + line.num());
        }
    }
    @Override
    public void apply(ArrayList<Object> lines) {
        int numBrkts = 0;//BRKTRIGGERED
        int firstBracket = -1;
        for (int i = 0; i < lines.size(); i++) {
            if (!(lines.get(i) instanceof Line)) {
                continue;
            }
            String str = ((Line) lines.get(i)).raw();
            if (str.contains("{")) {
                assertBlockBeginSane((Line) lines.get(i));
                numBrkts++;
                if (numBrkts == 1) {
                    firstBracket = i;
                }
            }
            if (str.contains("}")) {
                numBrkts--;
                if (numBrkts == 0) {
                    ArrayList<Object> before = new ArrayList<>(lines.subList(0, firstBracket + 1));
                    ArrayList<Object> during = new ArrayList<>(lines.subList(firstBracket + 1, i));//this cuts off the { I think?
                    ArrayList<Object> after = new ArrayList<>(lines.subList(i + 1, lines.size()));
                    /*System.out.println("Before " + before);
                     System.out.println("During " + during);
                     System.out.println("After " + after);*/
                    lines.clear();
                    lines.addAll(before);
                    lines.add(during);//idk about this =/
                    apply(after);
                    lines.addAll(after);
                    return;
                }
            }
        }
        if (numBrkts == 0) {
            return;
        }
        if (numBrkts > 0) {
            throw new IllegalStateException("Not enough } in " + lines);
        } else {
            throw new IllegalStateException("Too many } in " + lines);
        }
    }
}
