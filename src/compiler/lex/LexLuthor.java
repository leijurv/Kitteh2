/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.lex;
import compiler.parse.Line;
import compiler.parse.Transform;
import compiler.util.Parse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 *
 * @author leijurv
 */
public class LexLuthor implements Transform<ArrayList<Object>> {
    @Override
    public void apply(ArrayList<Object> lines) {
        //bad non multithreaded implementation provided for reference
        /*for (Object o : lines) {
         if (o instanceof Line) {
         ((Line) o).lex();
         }
         }*/
        //print out the composition of lines
        //SELECT class,count(*) FROM lines GROUP BY class
        //System.out.println("Lexing " + lines.stream().collect(Collectors.groupingBy(obj -> obj.getClass())).entrySet().stream().map(entry -> new Pair<>(entry.getKey(), entry.getValue().size())).collect(Collectors.toList()) + " lines");
        List<Line> toLex = Parse.filteredFlatten(Line.class, Line::unlext, lines).collect(Collectors.toList());
        if (toLex.isEmpty()) {
            return;
        }
        //this is faster than just piping parse.filteredFlatten into .parallel
        //because the super nested flatmaps, filters, streams, and maps are therefore run in series and not in parallel (which would be kinda useless as they are very very computationally light)
        //and also because we can skip hitting the parallel framework entirely if toLex is empty (so as to prevent nested parallels)
        Optional<RuntimeException> e = toLex.parallelStream().map(line -> {
            try {
                line.lex();
            } catch (Exception ex) {
                return new RuntimeException("Exception while lexing line " + line.num(), ex);
            }
            return null;
        }).filter(ex -> ex != null).findFirst();//get the first non-null exception
        if (e.isPresent()) {
            throw e.get();//and throw it
        }
        //this makes it mimic the behavior of a non parallel lexer
        //to guarantee that the first and only the first line with an error gets an error thrown
    }
}
