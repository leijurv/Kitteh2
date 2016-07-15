/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler;

/**
 *
 * @author leijurv
 */
public enum Keyword {
    FOR("PURR", true), PRINT("MEOW", false), PRINTLN("MEOWLN", false), IF("BLINK", true), TRUE("YES", false), FALSE("NO", false);
    public static boolean CAT_MODE = false;
    public String catVersion;
    public boolean canBeginBlock;
    private Keyword(String catVersion, boolean canBeginBlock) {
        this.catVersion = catVersion;
        this.canBeginBlock = canBeginBlock;
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
