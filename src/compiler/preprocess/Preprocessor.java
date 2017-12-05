/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.preprocess;
import compiler.parse.Transform;
import static compiler.preprocess.StripLocation.*;
import java.nio.file.Path;
import java.util.List;

/**
 *
 * @author leijurv
 */
public class Preprocessor {
    private Preprocessor() {
    }
    static final LineBasedTransform CHAR_STRIPPER = new CharStripperFactory()
            .addChar(' ', BOTH)//remove spaces from both ends of each line
            .addChar(';', END)//remove semicolons from the end, they are optional lol (yes you can have something like "x=5;;;;;;  ; ; ;; " and it'll be valid
            .addChar('	', BOTH)//you can use tabs or spaces
            .addChar('\r', BOTH)//idk how returns work
            .addChar((char) 11, BOTH)//literally https://en.wikipedia.org/wiki/Tab_key#Tab_characters
            .addChar(' ', BOTH)//alt+space
            .build();//TODO: heck, lets strip any ascii character ≤32
    static final Transform<List<Line>> REMOVE_BLANK = new BlankLineRemover();
    public static List<Line> preprocess(String rawProgram, Path from) {
        List<Line> program = new StripComments(from).transform(rawProgram);
        CHAR_STRIPPER.apply(program);
        REMOVE_BLANK.apply(program);
        return program;
    }
}
