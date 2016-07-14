/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler;
import java.util.ArrayList;

/**
 *
 * @author leijurv
 */
public class StringFinder implements Transform<ArrayList<Object>> {
    @Override
    public void apply(ArrayList<Object> lines) {
        for (int j = 0; j < lines.size(); j++) {
            if (!(lines.get(j) instanceof String)) {
                continue;
            }
            String line = (String) lines.get(j);
            boolean inString = false;
            int stringBegin = -1;
            char prevChar = 1;
            char strType = 1;
            for (int i = 0; i < line.length(); i++) {
                char ch = line.charAt(i);
                if (ch == '"' || ch == '\'') {
                    if (prevChar != '\\') {//It's not a string thingy if the prev char was a \
                        if (!(inString && ch != strType)) {//if a string was started with ", and we see a ', don't end the string
                            inString = !inString;
                            if (inString) {
                                strType = ch;
                                stringBegin = i;
                            } else {
                                //ok so a string is over
                                String strContents = line.substring(stringBegin + 1, i);//cut off the quote marks
                                String before = line.substring(0, stringBegin);
                                if (strType != '"') {
                                    if (strType != '\'') {
                                        throw new IllegalStateException("lol what");
                                    }
                                    if (((String) strContents).length() != 1) {
                                        System.out.println(line);
                                        throw new IllegalStateException("lol your single quotes can only hold single things: " + strContents);
                                    }
                                    //strContents = ((String) strContents).charAt(0);//if single quotes, use a Character not a String
                                }
                                String after = line.substring(i + 1, line.length());
                                lines.set(j, before);
                                lines.add(j + 1, new TokenString(strContents));
                                lines.add(j + 2, after);
                                break;
                            }
                        }
                    }
                }
                prevChar = ch;
            }
            if (inString) {
                throw new IllegalStateException("String not ended");//this should have been caught earlier...
            }
        }
    }
}
