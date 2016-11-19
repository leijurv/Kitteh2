/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.preprocess;
import compiler.parse.Line;
import java.util.ArrayList;
import java.util.stream.IntStream;
import javax.xml.bind.TypeConstraintException;

/**
 *
 * @author leijurv
 */
public class CharStripperFactory {
    private final ArrayList<Character> chars = new ArrayList<>();
    private final ArrayList<StripLocation> locations = new ArrayList<>();
    public CharStripperFactory addChar(char c, StripLocation loc) {
        if (chars.contains(c)) {
            throw new TypeConstraintException("");//lol
        }
        chars.add(c);
        locations.add(loc);
        return this;
    }
    public LineBasedTransform build() {
        return new StripChars();
    }
    /**
     * my favorite function
     *
     * @return
     */
    private Character[] stripBegin() {
        return IntStream.range(0, chars.size()).filter(i -> locations.get(i).stripBegin()).mapToObj(i -> chars.get(i)).toArray(Character[]::new);
    }
    private Character[] stripEnd() {
        return IntStream.range(0, chars.size()).filter(i -> locations.get(i).stripEnd()).mapToObj(i -> chars.get(i)).toArray(Character[]::new);
    }
    private boolean shouldStrip(Character[] chars, char test) {
        for (char c : chars) {
            if (test == c) {
                return true;
            }
        }
        return false;
    }

    private class StripChars extends LineBasedTransform {
        Character[] begin = stripBegin();
        Character[] end = stripEnd();
        @Override
        public Line transform(Line lineObj) {
            String line = lineObj.raw();
            if (line.equals("")) {
                return lineObj;
            }
            int stripBegin;
            for (stripBegin = 0; stripBegin < line.length() && shouldStrip(begin, line.charAt(stripBegin)); stripBegin++);
            int stripEnd;
            for (stripEnd = line.length() - 1; stripEnd >= 0 && shouldStrip(end, line.charAt(stripEnd)); stripEnd--);
            if (stripBegin > stripEnd) {
                return new Line("", lineObj.num());
            }
            return new Line(line.substring(stripBegin, stripEnd + 1), lineObj.num());
        }
    }
}
