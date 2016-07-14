/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.parse;

/**
 *
 * @author leijurv
 */
public interface Transform<E> {
    public void apply(E lines);
}
