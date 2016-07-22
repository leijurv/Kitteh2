/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler;
import compiler.expression.ExpressionConst;
import compiler.expression.ExpressionConstBool;

/**
 *
 * @author leijurv
 */
public enum Keyword {
    FOR("PURR", true, null),
    PRINT("MEOW", false, null),
    PRINTLN("MEOWLN", false, null),
    IF("BLINK", true, null),
    TRUE("YES", false, new ExpressionConstBool(true)),
    FALSE("NO", false, new ExpressionConstBool(false)),
    BREAK("TRIP", false, null),
    CONTINUE("CATINUE", false, null);
    public static boolean CAT_MODE = true;
    public final String catVersion;
    public final boolean canBeginBlock;
    private final ExpressionConst constVal;
    private Keyword(String catVersion, boolean canBeginBlock, ExpressionConst constVal) {
        this.catVersion = catVersion;
        this.canBeginBlock = canBeginBlock;
        this.constVal = constVal;
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
