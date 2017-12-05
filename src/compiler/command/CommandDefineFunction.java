/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.command;
import compiler.Compiler;
import compiler.Context;
import compiler.expression.Expression;
import compiler.expression.ExpressionFunctionCall;
import compiler.expression.ExpressionVariable;
import compiler.parse.Processor;
import compiler.tac.IREmitter;
import compiler.tac.TACStatement;
import compiler.tac.optimize.OptimizationSettings;
import compiler.tac.optimize.TACOptimizer;
import compiler.token.Token;
import static compiler.token.TokenType.*;
import compiler.type.Type;
import compiler.type.TypeInt32;
import compiler.type.TypeInt64;
import compiler.type.TypePointer;
import compiler.type.TypeStruct;
import compiler.type.TypeVoid;
import compiler.util.Pair;
import compiler.util.ParseUtil;
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
    public static String headerNameFromPkgAndName(String packageName, String name) {
        return packageName.replace(".", "DOT").replace("/", "_") + ("" + packageName.hashCode()).replace("-", "") + "__" + name;
    }
    public FunctionHeader getHeader() {
        if (isEntryPoint) {
            return getLocalHeader();
        }
        return new FunctionHeader(headerNameFromPkgAndName(context.packageName, name), header.arguments, header.returnTypes);
    }
    public String getLocalName() {
        return header.name;
    }
    public FunctionHeader getLocalHeader() {
        return header;
    }
    public void parseHeader() {
        if (header != null) {
            throw new IllegalStateException();
        }
        Type[] retType;
        if (returnType.isEmpty()) {
            retType = new Type[]{new TypeVoid()};
        } else {
            List<List<Token>> splitted = ParseUtil.splitList(returnType, COMMA);
            retType = new Type[splitted.size()];
            for (int i = 0; i < retType.length; i++) {
                Type type = ParseUtil.typeFromTokens(splitted.get(i), context);
                if (type == null) {
                    throw new IllegalStateException("Invalid return type " + splitted.get(i));
                }
                retType[i] = type;
            }
        }
        int endParen = params.indexOf(ENDPAREN);
        List<Pair<String, Type>> args = ParseUtil.splitList(params.subList(2, endParen), COMMA).stream().map(tokenList -> {
            List<Token> typeDefinition = tokenList.subList(0, tokenList.size() - 1);
            Type type = ParseUtil.typeFromTokens(typeDefinition, context);
            if (type == null) {
                throw new IllegalStateException(typeDefinition + " not a valid type");
            }
            if (tokenList.get(tokenList.size() - 1).tokenType() != VARIABLE) {
                throw new IllegalStateException();
            }
            String argNemo = (String) tokenList.get(tokenList.size() - 1).data();
            return new Pair<>(argNemo, type);
        }).collect(Collectors.toList());
        if (methodOf != null) {
            args.add(0, new Pair<>("this", new <TypeStruct>TypePointer<Type>(methodOf)));
        }
        this.header = new FunctionHeader(name, args.stream().map(Pair::getB).collect(Collectors.toList()), retType);
        int pos = 16; //args start at *(rbp+16) in order to leave room for rip and rbp on the call stack
        http://eli.thegreenplace.net/2011/09/06/stack-frame-layout-on-x86-64/
        for (Pair<String, Type> arg : args) {
            context.registerArgumentInput(arg.getA(), arg.getB(), pos);
            pos += arg.getB().getSizeBytes();
        }
    }
    public void parse(FunctionsContext gc) {
        if (Compiler.verbose()) {
            System.out.println("Starting to parse " + name);
        }
        long aoeu = System.currentTimeMillis();
        context.setCurrFunc(this);
        context.gc = gc;
        //System.out.println(name + " parsing " + rawContents);
        contents = Processor.parseRecursive(rawContents, context);
        //System.out.println("wew " + contents);
        checkFrees();
        context.gc = null;
        boolean returnsVoid = header.getReturnTypes().length == 1 && header.getReturnType() instanceof TypeVoid;
        if (contents.isEmpty()) {
            if (returnsVoid) {
                contents.add(new CommandReturn(context));
            } else {
                throw new IllegalStateException("Empty function with non-void return type " + name);
            }
        }
        boolean endWithReturn = contents.get(contents.size() - 1) instanceof CommandReturn;
        if (!endWithReturn) {
            if (returnsVoid) {
                contents.add(new CommandReturn(context));
            } else {
                throw new IllegalStateException("You need a return as the last command");
            }
        }
        if (Compiler.verbose()) {
            System.out.println("Done parsing " + name + " -- took " + (System.currentTimeMillis() - aoeu) + "ms");
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
        for (int i = 0; i < contents.size(); i++) {
            contents.set(i, contents.get(i).optimize());
        }
    }
    public List<TACStatement> totac(OptimizationSettings settings) {
        long start = System.currentTimeMillis();
        if (Compiler.verbose()) {
            System.out.println("> BEGIN TAC GENERATION FOR " + name);
        }
        IREmitter emit = new IREmitter();
        contents.forEach(emit::generateTAC);
        long middle = System.currentTimeMillis();
        if (Compiler.metrics()) {
            System.out.println(name);
        }
        List<TACStatement> result = TACOptimizer.optimize(emit, settings);
        long end = System.currentTimeMillis();
        if (Compiler.verbose()) {
            System.out.println("> END TAC GENERATION FOR " + name + " - " + (middle - start) + "ms gen, " + (end - middle) + "ms optim, " + (end - start) + "ms overall");
        }
        return result;
    }

    public static class FunctionHeader {
        private FunctionHeader(String name, List<Type> arguments, Type... returnType) {
            this.name = name;
            this.returnTypes = returnType;
            this.arguments = arguments;
            if (returnTypes.length < 1) {
                throw new IllegalArgumentException();
            }
        }
        public final String name;
        private final Type[] returnTypes;
        private final List<Type> arguments;
        public Type getReturnType() {
            if (returnTypes.length != 1) {
                throw new IllegalStateException();
            }
            return returnTypes[0];
        }
        public Type[] getReturnTypes() {
            return Arrays.copyOf(returnTypes, returnTypes.length);
        }
        public List<Type> inputs() {
            return new ArrayList<>(arguments);
        }
        @Override
        public String toString() {
            return "func " + name + arguments + " " + Arrays.asList(returnTypes);
        }
        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() == o.getClass()) {
                throw new UnsupportedOperationException();
            }
            throw new IllegalStateException();
        }
        @Override
        public int hashCode() {
            throw new UnsupportedOperationException();
        }
    }
    //public static final FunctionHeader PRINTINT = new FunctionHeader(Keyword.PRINT.toString(), new TypeVoid(), new ArrayList<>(Arrays.asList(new Type[]{new TypeInt32()})));
    public static final FunctionHeader MALLOC = new FunctionHeader("malloc", new ArrayList<>(Arrays.asList(new Type[]{new TypeInt32()})), new <TypeVoid>TypePointer<TypeVoid>(new TypeVoid()));
    public static final FunctionHeader FREE = new FunctionHeader("free", new ArrayList<>(Arrays.asList(new Type[]{new <TypeVoid>TypePointer<TypeVoid>(new TypeVoid())})), new TypeVoid());
    public static final FunctionHeader SYSCALL = new FunctionHeader("syscall", new ArrayList<>(Arrays.asList(new TypeInt64(), new TypeInt64(), new TypeInt64(), new TypeInt64(), new TypeInt64(), new TypeInt64(), new TypeInt64())), new TypeInt64());
    public void checkFrees() {
        if (name.endsWith("_free") || name.equals("free")) {
            if (methodOf == null) {
                throw new IllegalStateException("Can't define a function called free outside of a struct");
            } else {
                Expression freeThis = new ExpressionFunctionCall(context, null, "free", Arrays.asList(new ExpressionVariable("this", context)));
                contents.add(new CommandExp(freeThis, context));
                for (int i = 0; i < contents.size() - 1; i++) {
                    if (contents.get(i) instanceof CommandExp) {
                        Expression ex = ((CommandExp) contents.get(i)).getEx();
                        if (ex instanceof ExpressionFunctionCall) {
                            String calling = ((ExpressionFunctionCall) ex).callingName();
                            if (calling.endsWith(name)) {
                                //we're calling free
                                List<Expression> args = ((ExpressionFunctionCall) ex).calling();
                                if (args.size() == 1) {
                                    Expression firstArg = args.get(0);
                                    if (firstArg instanceof ExpressionVariable) {
                                        String arg = ((ExpressionVariable) firstArg).getName();
                                        if (arg.equals("this")) {
                                            throw new IllegalStateException("Warning: don't call free(this) in a destructor. The compiler adds the real free(this) for you, and doing it manually will lead to infinite recursion.");
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
