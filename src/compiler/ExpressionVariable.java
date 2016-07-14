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
public class ExpressionVariable extends Expression {
    String name;
    Type type;
    public ExpressionVariable(String name, Context context) {
        this.name = name;
        this.type = context.getType(name);
        if (type == null) {
            throw new IllegalStateException("pls " + name);
        }
    }
    @Override
    public Type getType() {
        return type;
    }
    public String toString() {
        return name;
    }
}
