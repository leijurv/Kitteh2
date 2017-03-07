/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.x86;

/**
 *
 * @author leijurv
 */
public class X86Statement {
    public String x86() {
        if (this instanceof Label || this instanceof Comment) {
            return toString();
        }
        return "    " + toString();
    }
}
