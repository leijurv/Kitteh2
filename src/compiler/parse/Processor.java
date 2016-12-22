/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.parse;
import compiler.preprocess.Line;
import compiler.Context;
import compiler.command.Command;
import compiler.command.CommandDefineFunction;
import compiler.lex.LexLuthor;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author leijurv
 */
public class Processor {
    /**
     * Top level parsing, only parses the function headers and returns a list of
     * CommandDefineFunctions.
     *
     * @param tempO
     * @param context
     * @return
     */
    public static List<CommandDefineFunction> initialParse(List<Line> tempO, Context context) {
        //long a = System.currentTimeMillis();
        ArrayList<Object> o = new ArrayList<>(tempO.size());
        o.addAll(tempO);
        tempO.clear();//idk
        //System.out.println("Processing " + o);
        new StringFinder().apply(o);//This makes a lot of things easier. Without this we can't do things like if(line.contains("{")) because the { might be in a string and therefore wouldn't actually begin a block
        new BlockFinder().apply(o);
        //long b = System.currentTimeMillis();
        new LexLuthor().apply(o);
        //long c = System.currentTimeMillis();
        //System.out.println("Done processing, beginning parsing " + o);
        ArrayList<Command> res = new Parser().parse(o, context);
        //long d = System.currentTimeMillis();
        //System.out.println("benchmark " + (b - a) + " " + (c - b) + " " + (d - c) + " -- total " + (d - a));
        return res.stream().map(CommandDefineFunction.class::cast).collect(Collectors.toList());
    }
    /**
     * Used for recursively parsing contents of blocks. Similar to initialParse,
     * but it doesn't run stringfinder or lexluthor, because there's no point:
     * they've already been run on all lines. It still needs to run blockFinder
     * because that only does one level of block finding at a time
     *
     * @param tempO
     * @param context
     * @return
     */
    public static ArrayList<Command> parseRecursive(List<Object> tempO, Context context) {
        ArrayList<Object> o = new ArrayList<>(tempO.size());
        o.addAll(tempO);
        tempO.clear();
        new BlockFinder().apply(o);
        return new Parser().parse(o, context);
    }
}
