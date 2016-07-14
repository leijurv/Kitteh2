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
public class CommandFor extends Command implements KeywordCommand {
    Command initialization;
    Expression condition;
    Command afterthought;
    ArrayList<Command> contents;
    public CommandFor(Command initialization, Expression condition, Command afterthought, ArrayList<Command> contents) {
        this.initialization = initialization;
        this.condition = condition;
        this.afterthought = afterthought;
        this.contents = contents;
    }
    public CommandFor(Expression condition, ArrayList<Command> contents) {
        this(null, condition, null, contents);
    }
    public CommandFor(ArrayList<Command> contents) {
        //this.condition=new ExpressionConstant(Boolean.TRUE)
        this(null, null, null, contents);
    }
    @Override
    public Keyword getKeyword() {
        return Keyword.FOR;
    }
}
