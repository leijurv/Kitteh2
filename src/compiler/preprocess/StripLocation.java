/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.preprocess;

/**
 * Where to strip
 *
 * @author leijurv
 */
enum StripLocation {
    BEGIN {
        @Override
        public boolean stripBegin() {
            return true;
        }
        @Override
        public boolean stripEnd() {
            return false;
        }
    }, END {
        @Override
        public boolean stripBegin() {
            return false;
        }
    }, BOTH {
        @Override
        public boolean stripBegin() {
            return true;
        }
    };
    public abstract boolean stripBegin();
    public boolean stripEnd() {
        return true;
    }
}
