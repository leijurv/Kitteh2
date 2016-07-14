/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.lex;
import compiler.parse.Line;
import compiler.parse.Transform;
import java.util.ArrayList;

/**
 *
 * @author leijurv
 */
public class LexLuthor implements Transform<ArrayList<Object>> {
    @Override
    public void apply(ArrayList<Object> lines) {
        for (Object o : lines) {
            if (o instanceof Line) {
                ((Line) o).lex();
            }
        }
        //I did some premature optimization that actually turns out it made it slower (at least for my small test program)
        //If we ever want to multithread the lexer, just uncomment this section =)
        //(yes, it's a parallel stream of illegal state exceptions)
        /*
         Optional<IllegalStateException> e = lines.parallelStream().filter(line -> line instanceof Line).map(line -> (Line) line).map(line -> {
         try {
         line.lex();
         } catch (IllegalStateException ex) {
         return ex;
         }
         return null;
         }).filter(ex -> ex != null).findFirst();
         if (e.isPresent()) {
         throw e.get();
         }*/
    }
}
