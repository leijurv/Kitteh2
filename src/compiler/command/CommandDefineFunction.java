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
    public CommandDefineFunction(Context context, Type returnType, List<Pair<String, Type>> arguments, String functionName, ArrayList<Object> rawContents) {
        super(context);
        this.name = functionName;
        this.rawContents = rawContents;
        this.header = new FunctionHeader(name, returnType, arguments.stream().map(Pair::getB).collect(Collectors.toList()));
        int pos = 16; //args start at *(rbp+16) in order to leave room for rip and rbp on the call stack
        http://eli.thegreenplace.net/2011/09/06/stack-frame-layout-on-x86-64/
        for (Pair<String, Type> arg : arguments) {
            context.registerArgumentInput(arg.getA(), arg.getB(), pos);
            pos += arg.getB().getSizeBytes();
        }
    }
    private boolean isEntryPoint = false;
    public void setEntryPoint() {
        isEntryPoint = true;
    }
    @Override
    public String toString() {
        return header + " " + (contents == null ? "unparsed" + rawContents : "parsed" + contents);
    }
    public FunctionHeader getHeader() {
        if (isEntryPoint) {
            return getLocalHeader();
        }
        return new FunctionHeader((context.packageName != null ? context.packageName.replace(".", "DOT").replace("/", "_") + Math.abs(context.packageName.hashCode()) : context.packageName) + "__" + name, header.returnType, header.arguments);
    }
    public String getLocalName() {
        return header.name;
    }
    public FunctionHeader getLocalHeader() {
        return header;
    }
    public void parse(FunctionsContext gc) {
        context.setCurrFunc(this);
        context.gc = gc;
        //System.out.println("Parsing " + rawContents);
        contents = Processor.parse(rawContents, context);
        //System.out.println("wew " + contents);
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
        List<TACStatement> result = TACOptimizer.optimize(emit, settings);
        System.out.println("> END TAC GENERATION FOR " + name + " - " + (System.currentTimeMillis() - start) + "ms");
        return result;
    }

    public static class FunctionHeader {
        private FunctionHeader(String name, Type returnType, List<Type> arguments) {
            this.name = name;
            this.returnType = returnType;
            this.arguments = arguments;
        }
        public final String name;
        private final Type returnType;
        private final List<Type> arguments;
        public Type getReturnType() {
            return returnType;
        }
        public List<Type> inputs() {
            return arguments;
        }
        @Override
        public String toString() {
            return "func " + name + arguments + " " + returnType;
        }
        @Override
        public boolean equals(Object o) {
            throw new UnsupportedOperationException();
        }
        @Override
        public int hashCode() {
            throw new UnsupportedOperationException();
        }
    }
    public static final FunctionHeader PRINTINT = new FunctionHeader(Keyword.PRINT.toString(), new TypeVoid(), new ArrayList<>(Arrays.asList(new Type[]{new TypeInt32()})));
    public static final FunctionHeader MALLOC = new FunctionHeader("malloc", new <TypeVoid>TypePointer<TypeVoid>(new TypeVoid()), new ArrayList<>(Arrays.asList(new Type[]{new TypeInt32()})));
    public static final FunctionHeader CALLOC = new FunctionHeader("calloc", new <TypeVoid>TypePointer<TypeVoid>(new TypeVoid()), new ArrayList<>(Arrays.asList(new Type[]{new TypeInt32()})));
    public static final FunctionHeader FREE = new FunctionHeader("free", new TypeVoid(), new ArrayList<>(Arrays.asList(new Type[]{new <TypeVoid>TypePointer<TypeVoid>(new TypeVoid())})));
}
