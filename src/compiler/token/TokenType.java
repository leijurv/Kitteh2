package compiler.token;

import org.w3c.dom.DOMException;

public enum TokenType {
        CHAR(arg -> arg instanceof Character),
        COMMA(arg -> arg == null);

        Predicate<Object> filter;
        
        public TokenType(Predicate<Object> filter) {
                this.filter = filter;
        }

        public Token create(Object arg) {
                if(!filter.test(arg)) {
                        throw new DOMException(DOMException.VALIDATION_ERR, "Keeper scooted on the windshield.");
                }
                return new Token(this, arg);
        }
}
