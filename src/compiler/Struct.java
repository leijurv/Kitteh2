/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler;
import compiler.parse.Line;
import compiler.token.Token;
import compiler.token.TokenType;
import compiler.type.Type;
import compiler.util.Parse;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author leijurv
 */
public class Struct {
    final String name;
    private final HashMap<String, StructField> fields;
    private final List<Line> lines;
    private final Context context;
    private boolean parsed = false;
    public Struct(String name, List<Line> rawLines, Context context) {
        this.name = name;
        this.fields = new HashMap<>();
        this.context = context;
        this.lines = rawLines;
    }
    public void parseContents() {
        if (parsed) {
            return;
        }
        int pos = 0;
        for (int j = 0; j < lines.size(); j++) {
            Line thisLine = lines.get(j);
            List<Token> tokens = thisLine.getTokens();
            if (tokens.get(tokens.size() - 1).tokenType() != TokenType.VARIABLE) {
                throw new RuntimeException();
            }
            String fieldName = (String) tokens.get(tokens.size() - 1).data();
            Type fieldType = Parse.typeFromTokens(tokens.subList(0, tokens.size() - 1), context);
            if (fieldType == null) {
                throw new IllegalStateException("Unable to determine type of " + tokens.subList(0, tokens.size() - 1));
            }
            fields.put(fieldName, new StructField(fieldName, fieldType, pos));
            pos += fieldType.getSizeBytes();
        }
        parsed = true;
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
