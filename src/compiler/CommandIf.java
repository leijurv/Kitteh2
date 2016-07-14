/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler;
import java.util.ArrayList;

/**
 *
 * @author leijurv
 */
public class CommandIf extends Command implements KeywordCommand {
    ArrayList<Command> contents;
    Expression condition;
    public CommandIf(Expression condition, ArrayList<Command> contents) {
    }
    @Override
    public Keyword getKeyword() {
        return Keyword.IF;
    }
}
