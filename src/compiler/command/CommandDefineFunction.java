/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.command;
import compiler.Context;
import compiler.parse.Processor;
import compiler.tac.IREmitter;
import compiler.tac.TACStatement;
import compiler.tac.optimize.OptimizationSettings;
import compiler.tac.optimize.TACOptimizer;
import compiler.token.Token;
import static compiler.token.TokenType.COMMA;
import static compiler.token.TokenType.ENDPAREN;
import static compiler.token.TokenType.VARIABLE;
import compiler.type.Type;
import compiler.type.TypeInt32;
import compiler.type.TypeInt64;
import compiler.type.TypePointer;
import compiler.type.TypeStruct;
import compiler.type.TypeVoid;
import compiler.util.Pair;
import compiler.util.Parse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.management.openmbean.InvalidKeyException;

/**
 *
 * @author leijurv
 */
public class CommandDefineFunction extends Command {//dont extend commandblock because we only get the contents later because of header first parsing
    private final String name;
    private ArrayList<Command> contents;
    private final ArrayList<Object> rawContents;
    private FunctionHeader header;
    private List<Token> returnType;
    private final List<Token> params;
    private final TypeStruct methodOf;
    public CommandDefineFunction(Context context, List<Token> params, String functionName, ArrayList<Object> rawContents) {
        this(null, context, params, functionName, rawContents);
    }
    public CommandDefineFunction(TypeStruct methodOf, Context context, List<Token> params, String functionName, ArrayList<Object> rawContents) {
        super(context);
        this.name = functionName;
        this.rawContents = rawContents;
        if (name.contains("dankmeme")) {
            System.out.println(name + ": " + rawContents);
        }
        this.params = params;
        int endParen = params.indexOf(ENDPAREN);
        if (endParen == -1) {
            throw new InvalidKeyException();
        }
        returnType = params.subList(endParen + 1, params.size());
        this.methodOf = methodOf;
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
        return new FunctionHeader((context.packageName != null ? context.packageName.replace(".", "DOT").replace("/", "_") + ("" + context.packageName.hashCode()).replace("-", "") : context.packageName) + "__" + name, header.returnType, header.arguments);
    }
    public String getLocalName() {
        return header.name;
    }
    public FunctionHeader getLocalHeader() {
        return header;
    }
    public void parseHeader() {
        if (header != null) {
            throw new RuntimeException();
        }
        Type retType;
        if (Parse.typeFromTokens(returnType, context) != null) {
            retType = Parse.typeFromTokens(returnType, context);
        } else if (returnType.isEmpty()) {
            retType = new TypeVoid();
        } else {
            throw new IllegalStateException(returnType + "not a valid type" + (returnType.contains(COMMA) ? ". no multiple returns yet. sorry!" : ""));
        }
        int endParen = params.indexOf(ENDPAREN);
        List<Pair<String, Type>> args = Parse.splitList(params.subList(2, endParen), COMMA).stream().map(tokenList -> {
            List<Token> typeDefinition = tokenList.subList(0, tokenList.size() - 1);
            Type type = Parse.typeFromTokens(typeDefinition, context);
            if (type == null) {
                throw new IllegalStateException(typeDefinition + " not a valid type");
            }
            if (tokenList.get(tokenList.size() - 1).tokenType() != VARIABLE) {
                throw new RuntimeException();
            }
            String name = (String) tokenList.get(tokenList.size() - 1).data();
            return new Pair<>(name, type);
        }).collect(Collectors.toList());
        if (methodOf != null) {
            args.add(0, new Pair<>("this", new TypePointer(methodOf)));
        }
        this.header = new FunctionHeader(name, retType, args.stream().map(Pair::getB).collect(Collectors.toList()));
        int pos = 16; //args start at *(rbp+16) in order to leave room for rip and rbp on the call stack
        http://eli.thegreenplace.net/2011/09/06/stack-frame-layout-on-x86-64/
        for (Pair<String, Type> arg : args) {
            context.registerArgumentInput(arg.getA(), arg.getB(), pos);
            pos += arg.getB().getSizeBytes();
        }
    }
    public void parse(FunctionsContext gc) {
        context.setCurrFunc(this);
        context.gc = gc;
        //System.out.println(name + " parsing " + rawContents);
        contents = Processor.parse(rawContents, context);
        //System.out.println("wew " + contents);
        context.gc = null;
        boolean returnsVoid = header.getReturnType() instanceof TypeVoid;
        if (contents.isEmpty()) {
            if (returnsVoid) {
                contents.add(new CommandReturn(context, null));
            } else {
                throw new RuntimeException("Empty function with non-void return type " + name);
            }
        }
        boolean endWithReturn = contents.get(contents.size() - 1) instanceof CommandReturn;
        if (!endWithReturn && !returnsVoid) {
            throw new RuntimeException("You need a return as the last command");
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
        //System.out.println("> BEGIN TAC GENERATION FOR " + name);
        Context.printFull = true;
        IREmitter emit = new IREmitter();
        for (Command com : contents) {
            com.generateTAC(emit);
        }
        long middle = System.currentTimeMillis();
        List<TACStatement> result = TACOptimizer.optimize(emit, settings);
        long end = System.currentTimeMillis();
        //System.out.println("> END TAC GENERATION FOR " + name + " - " + (end - start) + "ms overall, " + (end - middle) + "ms optim, " + (middle - start) + "ms gen");
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
    //public static final FunctionHeader PRINTINT = new FunctionHeader(Keyword.PRINT.toString(), new TypeVoid(), new ArrayList<>(Arrays.asList(new Type[]{new TypeInt32()})));
    public static final FunctionHeader MALLOC = new FunctionHeader("malloc", new <TypeVoid>TypePointer<TypeVoid>(new TypeVoid()), new ArrayList<>(Arrays.asList(new Type[]{new TypeInt32()})));
    public static final FunctionHeader FREE = new FunctionHeader("free", new TypeVoid(), new ArrayList<>(Arrays.asList(new Type[]{new <TypeVoid>TypePointer<TypeVoid>(new TypeVoid())})));
    public static final FunctionHeader SYSCALL = new FunctionHeader("syscall", new TypeVoid(), new ArrayList<>(Arrays.asList(new TypeInt64(), new TypeInt64(), new TypeInt64(), new TypeInt64(), new TypeInt64(), new TypeInt64(), new TypeInt64(), new TypeInt64(), new TypeInt64(), new TypeInt64(), new TypeInt64())));
}
