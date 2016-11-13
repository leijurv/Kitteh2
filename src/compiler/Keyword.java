/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler;
import compiler.expression.ExpressionConst;
import compiler.expression.ExpressionConstBool;
import compiler.type.Type;
import compiler.type.TypeBoolean;
import compiler.type.TypeInt16;
import compiler.type.TypeInt32;
import compiler.type.TypeInt64;
import compiler.type.TypeInt8;

/**
 *
 * @author leijurv
 */
public enum Keyword {
    FOR("PURR", true, null, null),
    PRINT("MEOW", false, null, null),
    //PRINTLN("MEOWLN", false, null, null),
    IF("BLINK", true, null, null),
    TRUE("YES", false, new ExpressionConstBool(true), null),
    FALSE("NO", false, new ExpressionConstBool(false), null),
    BREAK("TRIP", false, null, null),
    CONTINUE("CATINUE", false, null, null),
    RETURN("POUNCE", false, null, null),
    FUNC("CHASE", true, null, null),
    STRUCT("IDKWHATEVER", true, null, null),
    BOOL("BOWL", false, null, new TypeBoolean()),
    BYTE("BITE", false, null, new TypeInt8()),
    SHORT("CHOMP", false, null, new TypeInt16()),
    INT("MEOWNT", false, null, new TypeInt32()),
    LONG("LAWNG", false, null, new TypeInt64());
    public static final boolean CAT_MODE = false;
    public final String catVersion;
    public final boolean canBeginBlock;
    private final ExpressionConst constVal;
    public final Type type;
    private Keyword(String catVersion, boolean canBeginBlock, ExpressionConst constVal, Type typeVal) {
        this.catVersion = catVersion;
        this.canBeginBlock = canBeginBlock;
        this.constVal = constVal;
        this.type = typeVal;
        if (!super.toString().toUpperCase().equals(super.toString())) {
            throw new IllegalStateException(super.toString() + " " + super.toString().toUpperCase());
        }
        if (!catVersion.toUpperCase().equals(catVersion)) {
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
        return (catMode ? catVersion : super.toString()).toLowerCase();//lowercase keywords
    }
    public static Keyword strToKeyword(String str) {
        for (Keyword k : Keyword.values()) {
            if (k.toString().equals(str)) {
                return k;
            }
            if (k.toString().equals(str.toLowerCase())) {
                throw new IllegalStateException("This isn't python / mathematica. Keywords in all lower case please. Keyword in question: " + str);
            }
        }
        for (Keyword k : Keyword.values()) {//detect if they are using keywords from the other mode
            if (k.toString(!CAT_MODE).equals(str.toLowerCase())) {
                throw new IllegalStateException("You can't use " + str + " when cat_mode is " + CAT_MODE);
            }
        }
        return null;
    }
}
