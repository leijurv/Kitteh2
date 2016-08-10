/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac.optimize;
import compiler.tac.TACJump;
import compiler.tac.TACStatement;
import java.util.ArrayList;

/**
 *
 * @author leijurv
 */
public abstract class TACOptimization {
    private final ArrayList<TACStatement> original;
    private final ArrayList<TACStatement> statements;
    public TACOptimization(ArrayList<TACStatement> statements) {
        this.original = new ArrayList<>(statements);
        this.statements = new ArrayList<>(statements);
    }
    public int size() {
        return statements.size();
    }
    public void remove(int ind) {
        for (int i = 0; i < statements.size(); i++) {
            TACStatement stmt = statements.get(i);
            if (stmt instanceof TACJump) {
                TACJump jmp = (TACJump) stmt;
                int to = jmp.jumpTo();
                if (to > ind) {
                    jmp.bump(false);
                }
            }
        }
        statements.remove(ind);
    }
    public void update(int ind, TACStatement ne) {
        statements.set(ind, ne);
    }
    public TACStatement get(int i) {
        return statements.get(i);
    }
    public ArrayList<TACStatement> go() {
        run();
        return statements;
    }
    protected abstract void run();
}
