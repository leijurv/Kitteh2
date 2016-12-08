/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler;
import compiler.expression.ExpressionConst;
import compiler.expression.ExpressionConstBool;
import compiler.token.Token;
import compiler.token.TokenType;
import compiler.type.Type;
import compiler.type.TypeBoolean;
import compiler.type.TypeFloat;
import compiler.type.TypeInt16;
import compiler.type.TypeInt32;
import compiler.type.TypeInt64;
import compiler.type.TypeInt8;
import compiler.type.TypeVoid;
import java.util.Locale;

/**
 *
 * @author leijurv
 */
public enum Keyword implements Token<Keyword> {
    FOR("PURR", true),
    PRINT("MEOW", false),
    //PRINTLN("MEOWLN", false, null, null),
    IF("BLINK", true),
    ELSE("SELL", true),
    TRUE("YES", new ExpressionConstBool(true)),
    FALSE("NO", new ExpressionConstBool(false)),
    BREAK("TRIP", false),
    CONTINUE("CATINUE", false),
    RETURN("POUNCE", false),
    FUNC("CHASE", true),
    STRUCT("IDKWHATEVER", true),
    IMPORT("SOMETHINGGG", false),
    SIZEOF("BIGNESS", false),
    BOOL("BOWL", new TypeBoolean()),
    BYTE("BITE", new TypeInt8()),
    SHORT("CHOMP", new TypeInt16()),
    INT("MEOWNT", new TypeInt32()),
    LONG("LAWNG", new TypeInt64()),
    FLOAT("FOOD", new TypeFloat()),
    VOID("MT", new TypeVoid());
    public static final boolean CAT_MODE = false;
    public final String catVersion;
    public final boolean canBeginBlock;
    private final ExpressionConst constVal;
    public final Type type;
    private Keyword(String catVersion, Type typeVal) {
        this(catVersion, false, null, typeVal);
    }
    private Keyword(String catVersion, ExpressionConst constVal) {
        this(catVersion, false, constVal, null);
    }
    private Keyword(String catVersion, boolean canBeginBlock) {
        this(catVersion, canBeginBlock, null, null);
    }
    private Keyword(String catVersion, boolean canBeginBlock, ExpressionConst constVal, Type typeVal) {
        this.catVersion = catVersion;
        this.canBeginBlock = canBeginBlock;
        this.constVal = constVal;
        this.type = typeVal;
        if (!super.toString().toUpperCase(Locale.US).equals(super.toString())) {
            throw new IllegalStateException(super.toString() + " " + super.toString().toUpperCase(Locale.US));
        }
        if (!catVersion.toUpperCase(Locale.US).equals(catVersion)) {
            throw new IllegalStateException(catVersion + " " + catVersion.toUpperCase());
        }
    }
    public boolean isType() {
        return type != null;
    }
    public ExpressionConst getConstVal() {
        return constVal;
    }
    @Override
    public String toString() {
        return toString(CAT_MODE);
    }
    public String toString(boolean catMode) {
        return (catMode ? catVersion : super.toString()).toLowerCase(Locale.US);//lowercase keywords
    }
    public static Keyword strToKeyword(String str) {
        for (Keyword k : Keyword.values()) {
            if (k.toString().equals(str)) {
                return k;
            }
            if (k.toString().equals(str.toLowerCase(Locale.US))) {
                throw new IllegalStateException("This isn't python / mathematica. Keywords in all lower case please. Keyword in question: " + str);
            }
        }
        for (Keyword k : Keyword.values()) {//detect if they are using keywords from the other mode
            if (k.toString(!CAT_MODE).equals(str.toLowerCase(Locale.US))) {
                throw new IllegalStateException("You can't use " + str + " when cat_mode is " + CAT_MODE);
            }
        }
        return null;
    }
    @Override
    public TokenType tokenType() {
        return TokenType.KEYWORD;
    }
    @Override
    public Keyword data() {
        throw new UnsupportedOperationException("");
    }
}
