/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac.optimize;
import compiler.tac.TACJump;
import compiler.tac.TACStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author leijurv
 */
public abstract class TACOptimization {
    private final ArrayList<TACStatement> original;
    private final ArrayList<TACStatement> statements;
    private List<TACStatement> current;
    public TACOptimization(ArrayList<TACStatement> statements) {
        this.original = new ArrayList<>(statements);
        this.statements = new ArrayList<>(statements);
    }
    public int size() {
        //return statements.size();
        return current.size();
    }
    public void remove(int ind) {
        /*for (int i = 0; i < statements.size(); i++) {
            TACStatement stmt = statements.get(i);
            if (stmt instanceof TACJump) {
                TACJump jmp = (TACJump) stmt;
                int to = jmp.jumpTo();
                if (to > ind) {
                    jmp.bump(false);
                    //1: a
                    //2: b -- to remove
                    //3: c
                    //jump to c / 3 becomes jump to c / 2 -- bump
                    //jump to b / 2 becomes jump to c / 2 -- no bump
                    //jump to a / 1 becomes jump to a / 1 -- no bump
                }
            }
        }
        statements.remove(ind);*/
        current.remove(ind);
    }
    private ArrayList<Integer> jumpDestinations() {
        ArrayList<Integer> result = statements.stream().filter(stmt -> stmt instanceof TACJump).map(stmt -> (TACJump) stmt).map(stmt -> stmt.jumpTo()).distinct().collect(Collectors.toCollection(ArrayList::new));
        result.sort(null);
        return result;
    }
    public void update(int ind, TACStatement ne) {
        current.set(ind, ne);
    }
    public TACStatement get(int i) {
        return current.get(i);
    }
    public ArrayList<TACStatement> go() {
        ArrayList<Integer> jd = jumpDestinations();
        ArrayList<Integer> newJd = new ArrayList<>();
        ArrayList<List<TACStatement>> blocks = new ArrayList<>();
        for (int i = -1; i < jd.size(); i++) {
            int start = i == -1 ? 0 : jd.get(i);
            int end = i + 1 == jd.size() ? statements.size() : jd.get(i + 1);
            if (start == end) {
                continue;
            }
            System.out.println(start + " to " + end);
            List<TACStatement> thisBlock = statements.subList(start, end);
            blocks.add(thisBlock);
        }
        int pos = 0;
        for (int i = 0; i < blocks.size(); i++) {
            current = new ArrayList<>(blocks.get(i));
            run();
            blocks.set(i, current);
            newJd.add(pos += current.size());
        }
        System.out.println(blocks);
        //run();
        //return statements;
        ArrayList<TACStatement> result = blocks.stream().flatMap(x -> x.stream()).collect(Collectors.toCollection(ArrayList::new));
        for (int i = 0; i < result.size(); i++) {
            if (result.get(i) instanceof TACJump) {
                TACJump tj = (TACJump) result.get(i);
                int dest = tj.jumpTo();
                tj.setJumpTo(newJd.get(jd.indexOf(dest)));
            }
        }
        return result;
    }
    protected abstract void run();
}
