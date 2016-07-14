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
public abstract class Expression {
    public final Type getType() {
        if (cachedType == null) {
            cachedType = calcType();
            if (cachedType == null) {
                throw new IllegalStateException();
            }
        }
        return cachedType;
    }
    private Type cachedType = null;
    protected abstract Type calcType();//the return type
}
