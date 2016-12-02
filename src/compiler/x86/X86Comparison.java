/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.x86;
import compiler.Operator;
import java.nio.channels.UnresolvedAddressException;

/**
 *
 * @author leijurv
 */
public class X86Comparison {
    public static String tox86jump(Operator op) {
        return "j" + tox86comp(op);
    }
    public static String tox86set(Operator op) {
        return "set" + tox86comp(op);
    }
    public static String tox86comp(Operator op) {
        switch (op) {
            case LESS:
                return "l";
            case EQUAL:
                return "e";
            case GREATER:
                return "g";
            case NOT_EQUAL:
                return "ne";
            case LESS_OR_EQUAL:
                return "le";
            case GREATER_OR_EQUAL:
                return "ge";
            default:
                throw new UnresolvedAddressException();
        }
    }
}
