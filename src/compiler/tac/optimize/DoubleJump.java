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
public class DoubleJump extends TACOptimization {
    @Override
    public ArrayList<TACStatement> go(ArrayList<TACStatement> stmts) {//don't use the framework of keeping track of jump destinations, because that would be self destructive. also we won't add or remove statements
        for (int i = 0; i < stmts.size(); i++) {
            if (stmts.get(i) instanceof TACJump) {
                TACJump first = (TACJump) stmts.get(i);
                TACStatement dest = stmts.get(first.jumpTo());
                if (dest instanceof TACJump) {
                    TACJump second = (TACJump) dest;
                    boolean unconditionalSecond = dest.getClass() == TACJump.class;
                    if (unconditionalSecond) {
                        first.setJumpTo(second.jumpTo());
                        throw new UnsupportedOperationException(stmts.stream().map(x -> x.toString()).collect(Collectors.joining()));
                    }
                }
            }
        }
        return stmts;
    }
    @Override
    protected void run(List<TACStatement> block, int blockBegin) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
