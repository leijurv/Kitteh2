/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.command;
import compiler.Context;
import compiler.Keyword;
import compiler.parse.Processor;
import compiler.tac.IREmitter;
import compiler.tac.TACStatement;
import compiler.tac.optimize.OptimizationSettings;
import compiler.tac.optimize.TACOptimizer;
import compiler.type.Type;
import compiler.type.TypeInt32;
import compiler.type.TypePointer;
import compiler.type.TypeVoid;
import compiler.util.Pair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author leijurv
 */
public class CommandDefineFunction extends Command {//dont extend commandblock because we only get the contents later because of header first parsing
    private final String name;
    private ArrayList<Command> contents;
    private final ArrayList<Object> rawContents;
    private final FunctionHeader header;
    public CommandDefineFunction(Context context, Type returnType, ArrayList<Pair<String, Type>> arguments, String functionName, ArrayList<Object> rawContents) {
        super(context);
        this.name = functionName;
        this.rawContents = rawContents;
        this.header = new FunctionHeader(name, returnType, arguments.stream().map(Pair::getValue).collect(Collectors.toCollection(ArrayList::new)));
    }
    @Override
    public String toString() {
        return header + " " + (contents == null ? "unparsed" + rawContents : "parsed" + contents);
    }
    public FunctionHeader getHeader() {
        return header;
    }
    public void parse(FunctionsContext gc) {
        context.setCurrFunc(this);
        context.gc = gc;
        contents = Processor.parse(rawContents, context);
        context.gc = null;
        boolean endWithReturn = contents.get(contents.size() - 1) instanceof CommandReturn;
        boolean returnsVoid = header.getReturnType() instanceof TypeVoid;
        if (!endWithReturn && !returnsVoid) {
            throw new RuntimeException();
        }
    }
    @Override
    protected void generateTAC0(IREmitter emit) {
        throw new UnsupportedOperationException("Not supported yet, you poo."); //To change body of generated methods, choose Tools | Templates.
    }
    @Override
    protected int calculateTACLength() {
        throw new UnsupportedOperationException("Not supported yet, you poo."); //To change body of generated methods, choose Tools | Templates.
    }
    @Override
    public void staticValues() {
        for (Command com : contents) {
            com.staticValues();
        }
    }
    public List<TACStatement> totac(OptimizationSettings settings) {
        long start = System.currentTimeMillis();
        System.out.println("> BEGIN TAC GENERATION FOR " + name);
        Context.printFull = true;
        IREmitter emit = new IREmitter();
        for (Command com : contents) {
            com.generateTAC(emit);
        }
        ArrayList<TACStatement> result = TACOptimizer.optimize(emit, settings);
        System.out.println("> END TAC GENERATION FOR " + name + " - " + (System.currentTimeMillis() - start) + "ms");
        return result;
    }

    public static class FunctionHeader {
        private FunctionHeader(String name, Type returnType, ArrayList<Type> arguments) {
            this.name = name;
            this.returnType = returnType;
            this.arguments = arguments;
        }
        public final String name;
        private final Type returnType;
        private final ArrayList<Type> arguments;
        public Type getReturnType() {
            return returnType;
        }
        public ArrayList<Type> inputs() {
            return arguments;
        }
        @Override
        public String toString() {
            return "func " + name + arguments + " " + returnType;
        }
    }
    public static final FunctionHeader PRINTINT = new FunctionHeader(Keyword.PRINT.toString(), new TypeVoid(), new ArrayList<>(Arrays.asList(new Type[]{new TypeInt32()})));
    public static final FunctionHeader MALLOC = new FunctionHeader("malloc", new <TypeVoid>TypePointer<TypeVoid>(new TypeVoid()), new ArrayList<>(Arrays.asList(new Type[]{new TypeInt32()})));
}
