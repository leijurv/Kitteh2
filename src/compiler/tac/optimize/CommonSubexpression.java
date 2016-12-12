/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac.optimize;
import compiler.Context.VarInfo;
import compiler.tac.TACConst;
import compiler.tac.TACStandard;
import compiler.tac.TACStatement;
import java.util.List;

/**
 *
 * @author leijurv
 */
public class CommonSubexpression extends TACOptimization {
    @Override
    protected void run(final List<TACStatement> block, int blockBegin) {
        for (int i = 0; i < block.size(); i++) {
            if (block.get(i) instanceof TACStandard) {
                TACStandard ts = (TACStandard) block.get(i);
                VarInfo result = (VarInfo) ts.params[2];
                final int begin = result.getStackLocation();//inclusive
                final int end = result.getStackLocation() + result.getType().getSizeBytes() - 1;//inclusive
                for (int j = i + 1; j < block.size(); j++) {
                    List<String> mod = block.get(j).modifiedVariables();
                    if (mod.contains(ts.paramNames[0]) || mod.contains(ts.paramNames[1]) || mod.contains(ts.paramNames[2])) {
                        break;//technically this break shouldn't be required, but it feels sketchy without it
                    }
                    if (block.get(j).modifiedVariableInfos().stream().anyMatch(vi -> {
                        //does vi overwrite result?
                        int viBegin = vi.getStackLocation();//inclusive
                        int viEnd = vi.getStackLocation() + vi.getType().getSizeBytes() - 1;//inclusive
                        if (viBegin >= begin && viBegin <= end) {//if there is any overlap, at least one end of one of them needs to be within the other
                            return true;
                        }
                        if (viEnd >= begin && viEnd <= end) {
                            return true;
                        }
                        if (begin >= viBegin && begin <= viEnd) {
                            return true;
                        }
                        if (end >= viBegin && end <= viEnd) {
                            return true;
                        }
                        return false;
                    })) {
                        break;
                    }
                    if (block.get(j) instanceof TACStandard) {
                        TACStandard o = (TACStandard) block.get(j);
                        if (o.op == ts.op && o.paramNames[0].equals(ts.paramNames[0]) && o.paramNames[1].equals(ts.paramNames[1])) {
                            //System.out.println("Optimizing " + i + " " + j + " " + ts + " " + o);
                            TACConst repl = new TACConst(o.paramNames[2], ts.paramNames[2]);
                            repl.copyFrom(o);
                            repl.params[0] = ts.params[2];
                            repl.params[1] = o.params[2];
                            block.set(j, repl);
                            return;
                        }
                    }
                }
            }
        }
    }
}
