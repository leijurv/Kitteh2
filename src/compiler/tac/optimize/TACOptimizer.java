/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac.optimize;
import compiler.tac.IREmitter;
import compiler.tac.TACStatement;
import java.util.ArrayList;

/**
 *
 * @author leijurv
 */
public class TACOptimizer {
    public static ArrayList<TACStatement> optimize(IREmitter emitted) {
        ArrayList<TACStatement> input = emitted.getResult();
        input = new UselessTempVars(input).go();
        return input;
    }
}
