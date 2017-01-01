/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac.optimize;
import compiler.tac.TACJump;
import compiler.tac.TACStatement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 *
 * @author leijurv
 */
public abstract class TACOptimization {
    private ArrayList<TACStatement> statements;
    public static <T extends Collection<Integer>> T jumpDestinations(List<TACStatement> statements, Supplier<T> sup) {
        return statements.stream().filter(TACJump.class::isInstance).map(TACJump.class::cast).map(TACJump::jumpTo).distinct().sorted().collect(Collectors.toCollection(sup));
    }
    public void reset(List<TACStatement> newStmts) {
        statements = new ArrayList<>(newStmts);
    }
    public List<TACStatement> go(List<TACStatement> stmts) {
        reset(stmts);
        List<Integer> origJumpDests = jumpDestinations(statements, ArrayList::new);
        ArrayList<Integer> newJumpDests = new ArrayList<>();
        ArrayList<List<TACStatement>> blocks = new ArrayList<>();
        for (int i = -1; i < origJumpDests.size(); i++) {
            int start = i == -1 ? 0 : origJumpDests.get(i);
            int end = i + 1 == origJumpDests.size() ? statements.size() : origJumpDests.get(i + 1);
            if (start == end) {
                blocks.add(new ArrayList<>());
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
        List<TACStatement> result = blocks.stream().flatMap(Collection::stream).collect(Collectors.toList());
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
    public boolean isUsedAtOrAfter(int pos, String searchingFor) {
        List<TACStatement> block = statements;
        for (int j = pos; j < block.size(); j++) {
            if (block.get(j) instanceof TACJump) {
                int dest = ((TACJump) block.get(j)).jumpTo();
                if (dest < pos) {
                    //jump to before where we started our check
                    //gotta check starting from that location now
                    return isUsedAtOrAfter(dest, searchingFor);
                }
            }
            if (block.get(j).requiredVariables().contains(searchingFor)) {
                return true;
            }
        }
        return false;
    }
    public boolean accessibleFromExterior(int pos) {
        if (statements.get(pos).getClass() != TACJump.class) {
            throw new RuntimeException();
        }
        TACJump tj = (TACJump) (statements.get(pos));
        if (tj.jumpTo() <= pos) {
            throw new RuntimeException();
        }
        int rangeEnd = tj.jumpTo();
        //anything jumps to (pos+1,rangeEnd-1)
        //any jumps to pos are ok, and jumps to rangeEnd are ok because rangeEnd isn't in question
        for (int i = 0; i < statements.size(); i++) {
            if (i > pos && i < rangeEnd) {
                continue;
            }
            if (statements.get(i) instanceof TACJump) {
                int dest = ((TACJump) statements.get(i)).jumpTo();
                if (dest > pos && dest < rangeEnd) {
                    return true;
                }
            }
        }
        return false;
    }
}
