public final class Token<T> {
        
        public final TokenType tokenType;
        public final T data;
        public final Class<T> dataType;

        public Token(TokenType tokenType, T data) {
                this.tokenType = tokenType;
                this.data = data;
                this.dataType = data.getClass();
        }

        @Override
        public String toString() {
                return "";
        }
        @Override
        public boolean equals(Object o) {
                if (o != null && o.getClass() != getClass()) {
                    return false;
                }
                return toString().equals(o + "");
        }
        @Override
        public int hashCode() {
                return toString().hashCode();
        }
}
