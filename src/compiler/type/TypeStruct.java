/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.type;
import compiler.Context;
import compiler.Keyword;
import compiler.command.CommandDefineFunction;
import compiler.parse.BlockFinder;
import compiler.parse.Line;
import compiler.token.Token;
import compiler.token.TokenType;
import static compiler.token.TokenType.STARTPAREN;
import compiler.util.Pair;
import compiler.util.Parse;
import java.lang.annotation.AnnotationTypeMismatchException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author leijurv
 */
public class TypeStruct extends Type {
    final String name;
    private final HashMap<String, StructField> fields;
    private final ArrayList<Object> rawBlock;
    private final Context context;
    private boolean parsed = false;
    private final ArrayList<String> fieldNames = new ArrayList<>();
    private final ArrayList<Type> fieldTypes = new ArrayList<>();
    private final List<CommandDefineFunction> structMethods = new ArrayList<>();
    public TypeStruct(String name, ArrayList<Object> rawBlock, Context context) {
        this.name = name;
        this.fields = new HashMap<>();
        this.context = context;
        this.rawBlock = rawBlock;
    }
    public static String format(String structName, String methodName) {
        return structName + "___" + methodName;
    }
    public void parseContents() {
        if (parsed) {
            throw new RuntimeException();
        }
        new BlockFinder().apply(rawBlock);
        for (int i = 0; i < rawBlock.size(); i++) {
            Line thisLine = (Line) rawBlock.get(i);
            thisLine.lex();
            if (i != rawBlock.size() - 1 && rawBlock.get(i + 1) instanceof ArrayList) {
                ArrayList<Object> functionContents = (ArrayList) rawBlock.remove(i + 1);
                rawBlock.remove(i);
                i = -1;
                List<Token> params = thisLine.getTokens();
                if (params.get(0) == Keyword.FUNC) {
                    //optional
                    params = params.subList(1, params.size());
                }
                String functionName = (String) params.get(0).data();
                System.out.println("Struct with name " + name + " has function with name " + functionName);
                functionName = format(name, functionName);
                System.out.println("Renaming to " + functionName);
                if (params.get(1) != STARTPAREN) {
                    throw new AnnotationTypeMismatchException(null, "" + params);
                }
                Context subContext = context.subContext();
                structMethods.add(new CommandDefineFunction(this, subContext, params, functionName, functionContents));
                continue;
            }
            System.out.println(i);
            rawBlock.remove(i);
            i = -1;
            List<Token> tokens = thisLine.getTokens();
            if (tokens.get(tokens.size() - 1).tokenType() != TokenType.VARIABLE) {
                throw new RuntimeException(tokens + "");
            }
            String fieldName = (String) tokens.get(tokens.size() - 1).data();
            Type fieldType = Parse.typeFromTokens(tokens.subList(0, tokens.size() - 1), context);
            if (fieldType == null) {
                throw new IllegalStateException("Unable to determine type of " + tokens.subList(0, tokens.size() - 1));
            }
            fieldNames.add(fieldName);
            fieldTypes.add(fieldType);
        }
        parsed = true;
    }
    public List<Pair<Context, CommandDefineFunction>> getStructMethods() {
        return structMethods.stream().map(cdf -> new Pair<>(context, cdf)).collect(Collectors.toList());
    }
    @Override
    public int getSizeBytes() {
        return fieldTypes.stream().mapToInt(Type::getSizeBytes).sum();
    }
    public void allocate() {
        int pos = 0;
        for (int i = 0; i < fieldNames.size(); i++) {
            String fieldName = fieldNames.get(i);
            Type fieldType = fieldTypes.get(i);
            fields.put(fieldName, new StructField(fieldName, fieldType, pos));
            pos += fieldType.getSizeBytes();
        }
    }
    public StructField getFieldByName(String name) {
        if (!parsed) {
            throw new RuntimeException("Out of order struct reference");
        }
        return fields.get(name);
    }
    public Collection<StructField> getFields() {
        if (!parsed) {
            throw new RuntimeException("Out of order struct reference");
        }
        return fields.values();
    }
    @Override
    public String toString() {
        return "Struct" + name;
    }
    public String getName() {
        return name;
    }

    public static class StructField {
        private final String name;
        private final Type type;
        private final int stackLocation;
        StructField(String name, Type type, int stackLocation) {
            this.name = name;
            this.type = type;
            this.stackLocation = stackLocation;
        }
        public Type getType() {
            return type;
        }
        public int getStackLocation() {
            return stackLocation;
        }
        public String getName() {
            return name;
        }
    }
}
