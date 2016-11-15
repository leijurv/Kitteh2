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
    private final ArrayList<TACStatement> statements;
    public TACOptimization(ArrayList<TACStatement> statements) {
        this.statements = new ArrayList<>(statements);
    }
    private static ArrayList<Integer> jumpDestinations(ArrayList<TACStatement> statements) {
        ArrayList<Integer> result = statements.stream().filter(stmt -> stmt instanceof TACJump).map(stmt -> (TACJump) stmt).map(stmt -> stmt.jumpTo()).distinct().collect(Collectors.toCollection(ArrayList::new));
        result.sort(null);
        return result;
    }
    public ArrayList<TACStatement> go() {
        ArrayList<Integer> origJumpDests = jumpDestinations(statements);
        ArrayList<Integer> newJumpDests = new ArrayList<>();
        ArrayList<List<TACStatement>> blocks = new ArrayList<>();
        for (int i = -1; i < origJumpDests.size(); i++) {
            int start = i == -1 ? 0 : origJumpDests.get(i);
            int end = i + 1 == origJumpDests.size() ? statements.size() : origJumpDests.get(i + 1);
            if (start == end) {
                continue;
            }
            //System.out.println(start + " to " + end);
            List<TACStatement> thisBlock = statements.subList(start, end);
            blocks.add(thisBlock);
        }
        int pos = 0;
        for (int i = 0; i < blocks.size(); i++) {
            List<TACStatement> block = new ArrayList<>(blocks.get(i));
            run(block, i == 0 ? 0 : origJumpDests.get(i - 1));
            blocks.set(i, block);
            pos += block.size();
            newJumpDests.add(pos);
            //newJumpDests.add(pos += current.size());
        }
        ArrayList<TACStatement> result = blocks.stream().flatMap(x -> x.stream()).collect(Collectors.toCollection(ArrayList::new));
        for (int i = 0; i < result.size(); i++) {
            if (result.get(i) instanceof TACJump) {
                TACJump jump = (TACJump) result.get(i);
                int dest = jump.jumpTo();
                int destIndex = origJumpDests.indexOf(dest);
                int newDest = newJumpDests.get(destIndex);
                jump.setJumpTo(newDest);
            }
        }
        return result;
    }
    protected abstract void run(List<TACStatement> block, int blockBegin);
}
