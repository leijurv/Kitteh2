/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.parse;
import compiler.preprocess.Line;
import java.util.ArrayList;

/**
 *
 * @author leijurv
 */
public class BlockFinder implements Transform<ArrayList<Object>> {
    private static void assertLineSane(Line line, boolean shouldEndWithBracket, boolean startBracket) {
        ArrayList<Object> strs = line.source();
        //only the last string can contain { or }, so check all but the last
        //if it shouldn't end with a bracket, also check the last
        int max = strs.size() - (shouldEndWithBracket ? 1 : 0);
        for (int j = 0; j < max; j++) {
            if (!(strs.get(j) instanceof String)) {
                continue;
            }
            String str = (String) strs.get(j);
            if (str.contains("{") || str.contains("}")) {
                throw line.new LineException();
            }
        }
        if (shouldEndWithBracket) {
            String str = (String) strs.get(strs.size() - 1);
            if (!str.endsWith(startBracket ? "{" : "}")) {
                throw line.new LineException();
            }
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
            Line line = ((Line) lines.get(i));
            boolean start = line.source().stream().filter(String.class::isInstance).anyMatch(str -> ((String) str).contains("{"));
            boolean end = line.source().stream().filter(String.class::isInstance).anyMatch(str -> ((String) str).contains("}"));
            if (start && !end) {
                assertLineSane(line, true, true);
                numBrkts++;
                if (numBrkts == 1) {
                    firstBracket = i;
                }
            } else if (end) {
                if (!start) {
                    assertLineSane(line, true, false);
                }
                numBrkts--;
                if (numBrkts == 0) {
                    ArrayList<Object> before = new ArrayList<>(lines.subList(0, firstBracket + 1));
                    ArrayList<Object> during = new ArrayList<>(lines.subList(firstBracket + 1, i));
                    if (start) {//TODO fix this steaming pile of
                        if (((String) line.source().get(0)).charAt(0) != '}') {
                            throw new RuntimeException(line + "");
                        }
                        line.source().set(0, ((String) line.source().get(0)).substring(1));
                    }
                    ArrayList<Object> after = new ArrayList<>(lines.subList(i + (start ? 0 : 1), lines.size()));
                    lines.clear();
                    lines.addAll(before);
                    lines.add(during);
                    apply(after);
                    lines.addAll(after);
                    return;
                }
                if (start) {
                    numBrkts++;
                }
            } else {
                assertLineSane(line, false, false);
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
