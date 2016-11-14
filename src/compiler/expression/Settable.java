/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.expression;
import compiler.Context;
import compiler.command.Command;

/**
 *
 * @author leijurv
 */
public interface Settable {
    public Command setValue(Expression rvalue, Context context);
}
