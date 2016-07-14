/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler;

/**
 * evaluate expression
 *
 * @author leijurv
 */
public class CommandExp extends Command {
    Expression ex;
    public CommandExp(Expression ex) {
        this.ex = ex;
    }
}
