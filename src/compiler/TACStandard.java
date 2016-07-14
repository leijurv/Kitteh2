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
public class TACStandard extends TACStatement {
    String result;
    String first;
    String second;
    Operator op;
    public TACStandard(String result, String first, String second, Operator op) {
        this.result = result;
        this.first = first;
        this.second = second;
        this.op = op;
    }
    @Override
    public String toString() {
        return result + " = " + first + " " + op + " " + second;
    }
}
