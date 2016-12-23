/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.parse;

/**
 *
 * @author leijurv
 * @param <E>
 */
public interface Transform<E> {
    void apply(E lines);
}
