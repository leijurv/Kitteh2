/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler;

/**
 *
 * @author leijurv
 */
public class TempVarUsage {
    int ind = 0;
    public String getTempVar() {
        return "t" + ind++;//yes
    }
}
