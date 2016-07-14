/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler;

/**
 * Where to strip
 *
 * @author leijurv
 */
public enum StripLocation {
    BEGIN, END, BOTH;
    public boolean stripBegin() {
        switch (this) {
            case BEGIN:
            case BOTH:
                return true;
        }
        return false;
    }
    public boolean stripEnd() {
        switch (this) {
            case END:
            case BOTH:
                return true;
        }
        return false;
    }
}
