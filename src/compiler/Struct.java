/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler;
import compiler.Context.VarInfo;
import compiler.parse.Line;
import compiler.token.Token;
import compiler.token.TokenType;
import compiler.type.Type;
import compiler.util.Parse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author leijurv
 */
public class Struct {
    final String name;
    private final HashMap<String, VarInfo> fields;
    private final List<Line> lines;
    private final Context context;
    public Struct(String name, List<Line> rawLines, Context context) {
        this.name = name;
        this.fields = new HashMap<>();
        this.context = context;
        this.lines = rawLines;
    }
    public void parseContents() {
        if (!fields.isEmpty()) {
            return;
        }
        ArrayList<String> fieldNames = new ArrayList<>(lines.size());
        ArrayList<Type> fieldTypes = new ArrayList<>(lines.size());
        for (int j = 0; j < lines.size(); j++) {
            Line thisLine = lines.get(j);
            List<Token> tokens = thisLine.getTokens();
            if (tokens.get(tokens.size() - 1).tokenType() != TokenType.VARIABLE) {
                throw new RuntimeException();
            }
            fieldNames.add((String) tokens.get(tokens.size() - 1).data());
            Type tft = Parse.typeFromTokens(tokens.subList(0, tokens.size() - 1), context);
            if (tft == null) {
                throw new IllegalStateException("Unable to determine type of " + tokens.subList(0, tokens.size() - 1));
            }
            fieldTypes.add(tft);
        }
        int pos = 0;
        for (int i = 0; i < fieldTypes.size(); i++) {
            fields.put(fieldNames.get(i), context.new VarInfo(fieldNames.get(i), fieldTypes.get(i), pos, true));
            pos += fieldTypes.get(i).getSizeBytes();
        }
    }
    public VarInfo getFieldByName(String name) {
        return fields.get(name);
    }
    public Collection<VarInfo> getFields() {
        return fields.values();
    }
    @Override
    public String toString() {
        return name;
    }
}
