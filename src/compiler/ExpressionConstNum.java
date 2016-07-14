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
public class ExpressionConstNum extends Expression {
    Number val;
    Type type;
    public ExpressionConstNum(Number val, TypeNumerical type) {
        this.val = val;
        this.type = type;
    }
    public ExpressionConstNum(Number val) {
        this(val, new TypeInt32());
    }
    @Override
    public Type calcType() {
        return type;
    }
    @Override
    public String toString() {
        return val.toString();
    }
    @Override
    public void calcNaiveTAC(Context context, IREmitter emit, TempVarUsage tempVars, String resultLocation) {
        emit.emit(new TACConst(resultLocation, val.toString()));//this one, too, at least, is easy
        //wew that was like a lot of commas. 3 commas for 7 words. that's 3/7, which is 42.8572% (rounding)
    }
    @Override
    public int calcTACLength() {
        return 1;
    }
}
