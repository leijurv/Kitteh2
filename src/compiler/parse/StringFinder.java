/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.parse;
import compiler.preprocess.LineBasedTransform;
import compiler.token.TokenType;

/**
 *
 * @author leijurv
 */
class StringFinder extends LineBasedTransform {
    @Override
    public Line transform(Line l) {
        for (int j = 0; j < l.source().size(); j++) {
            if (!(l.source().get(j) instanceof String)) {
                continue;
            }
            String line = (String) l.source().get(j);
            boolean inString = false;
            int stringBegin = -1;
            char prevChar = 1;
            char strType = 1;
            for (int i = 0; i < line.length(); i++) {
                char ch = line.charAt(i);
                if ((ch == '"' || ch == '\'')//must actually be either of the string characters
                        && prevChar != '\\' //It's not a string thingy if the prev char was a \
                        && !(inString && ch != strType)) {//if a string was started with ", and we see a ', don't end the string
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
                            if (strContents.length() != 1) {
                                System.out.println(line);
                                throw new IllegalStateException("lol your single quotes can only hold single things: " + strContents);
                            }
                            //strContents = ((String) strContents).charAt(0);//if single quotes, use a Character not a String
                        }
                        String after = line.substring(i + 1, line.length());
                        l.source().set(j, before);
                        l.source().add(j + 1, strType == '"' ? TokenType.STRING.create(strContents) : TokenType.CHAR.create(strContents.charAt(0)));
                        l.source().add(j + 2, after);
                        break;
                    }
                }
                prevChar = ch;
            }
            if (inString) {
                throw new IllegalStateException("String not ended");//this should have been caught earlier...
            }
        }
        return l;
    }
}
