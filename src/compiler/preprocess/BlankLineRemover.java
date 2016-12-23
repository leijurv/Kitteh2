/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.preprocess;
import compiler.parse.Transform;
import java.util.List;

/**
 * This is a bit of a "god" class, have to work on cutting it down, refactoring
 * out functionality, etc
 *
 * @author leijurv
 */
class BlankLineRemover implements Transform<List<Line>> {
    @Override
    public void apply(List<Line> lines) {
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).raw() == null || lines.get(i).raw().equals("")) {
                lines.remove(i);
                i--;
            }
        }
    }
}
