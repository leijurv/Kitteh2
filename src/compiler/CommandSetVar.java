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
public class CommandSetVar extends Command {
    Expression val;
    String var;
    public CommandSetVar(String var, Expression val) {
        this.val = val;
        this.var = var;
    }
}
