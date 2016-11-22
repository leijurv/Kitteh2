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
import compiler.tac.optimize.TACOptimizer;
import compiler.tac.optimize.TACOptimizer.OptimizationSettings;
import compiler.type.Type;
import compiler.type.TypeInt32;
import compiler.type.TypePointer;
import compiler.type.TypeVoid;
import compiler.x86.X86Emitter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javafx.util.Pair;

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
    public static String generateX86(Pair<String, List<TACStatement>> pair) {
        return generateX86(pair.getKey(), pair.getValue());
    }
    public static String generateX86(String name, List<TACStatement> result) {
        long start = System.currentTimeMillis();
        System.out.println("> BEGIN X86 GENERATION FOR " + name);
        X86Emitter emitter = new X86Emitter(name);
        for (int i = 0; i < result.size(); i++) {
            emitter.addStatement(emitter.lineToLabel(i) + ":");
            emitter.addStatement("#   " + result.get(i));
            result.get(i).printx86(emitter);
            emitter.addStatement("");//nice blank line makes it more readable =)
        }
        StringBuilder resp = new StringBuilder();
        resp.append("	.globl	_").append(name).append("\n	.align	4, 0x90\n");
        resp.append("_").append(name).append(":\n");
        resp.append(FUNC_HEADER).append('\n');
        resp.append(emitter.toX86()).append('\n');
        resp.append(FUNC_FOOTER).append('\n');
        System.out.println("> END X86 GENERATION FOR " + name + " - " + (System.currentTimeMillis() - start) + "ms");
        return resp.toString();
    }
    private static final String FUNC_HEADER = "	.cfi_startproc\n"
            + "	pushq	%rbp\n"
            + "	.cfi_def_cfa_offset 16\n"
            + "	.cfi_offset %rbp, -16\n"
            + "	movq	%rsp, %rbp\n"
            + "	.cfi_def_cfa_register %rbp";
    private static final String FUNC_FOOTER = "	.cfi_endproc";

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
    public static final FunctionHeader PRINTINT = new FunctionHeader("KEYWORD" + Keyword.PRINT.toString(), new TypeVoid(), new ArrayList<>(Arrays.asList(new Type[]{new TypeInt32()})));
    public static final FunctionHeader MALLOC = new FunctionHeader("malloc", new <TypeVoid>TypePointer<TypeVoid>(new TypeVoid()), new ArrayList<>(Arrays.asList(new Type[]{new TypeInt32()})));
}
