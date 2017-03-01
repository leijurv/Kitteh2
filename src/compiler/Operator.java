/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler;
import compiler.expression.Expression;
import compiler.expression.ExpressionConst;
import compiler.expression.ExpressionConstBool;
import compiler.expression.ExpressionConstNum;
import compiler.token.Token;
import compiler.token.TokenType;
import compiler.type.Type;
import compiler.type.TypeBoolean;
import compiler.type.TypeInt64;
import compiler.type.TypeNumerical;
import compiler.type.TypePointer;
import java.nio.channels.UnsupportedAddressTypeException;
import java.nio.file.FileSystemAlreadyExistsException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author leijurv
 */
public enum Operator implements Token<Operator> {
    PLUS("+", 50, "add"),
    MINUS("-", 50, "sub"),
    MULTIPLY("*", 100, "mul"),
    DIVIDE("/", 100),
    MOD("%", 1000),
    EQUAL("==", 10),
    NOT_EQUAL("!=", 10),
    GREATER(">", 10),
    LESS("<", 10),
    GREATER_OR_EQUAL(">=", 10),
    LESS_OR_EQUAL("<=", 10),
    SHIFT_L("<<", 1500, "sal"),
    SHIFT_R(">>", 1500, "sar"),
    USHIFT_L("<<<", 1500, "shl"),
    USHIFT_R(">>>", 1500, "shr"),
    L_XOR("^", 2000, "xor"),
    L_OR("|", 1800, "or"),
    L_AND("&", 1900, "and"),//or has less precedence than and.   so a | b & c will actually be a | (b & c)
    OR("||", 4),
    AND("&&", 5);//OR has less precedence than AND.   so a || b && c will actually be a || (b && c)
    public static final List<List<Operator>> ORDER;//sorry this can't be the first line
    private final String str;
    private final int precedence;
    private final Optional<String> x86;
    private Operator(String str, int precedence) {
        this(str, precedence, Optional.empty());
    }
    private Operator(String str, int precedence, String x86) {
        this(str, precedence, Optional.of(x86));
    }
    private Operator(String str, int precedence, Optional<String> x86) {
        this.str = str;
        this.precedence = precedence;
        this.x86 = x86;
    }
    public String x86() {
        return x86.get();
    }
    @Override
    public String toString() {
        return str;
    }
    static {
        //Having it just be an array would put equal things next to each other, but not at the same place
        //For example, + might be sorted before - even though they have the same precedence
        //so, a-b+c might be parsed as a-(b+c)
        //having it be a 2d array fixes that
        Map<Integer, List<Operator>> precToOp = Stream.of(values()).collect(Collectors.groupingBy(op -> op.precedence));
        ORDER = Collections.unmodifiableList(Stream.of(values()).map(op -> op.precedence).distinct().sorted(Comparator.comparingInt(prec -> -prec)).map(precToOp::get).map(Collections::unmodifiableList).collect(Collectors.toList()));
        //ArrayList<Operator> ops = new ArrayList<>(Arrays.asList(values()));
        //reverse order, so that the most important comes first (%) and least important comes last (&&, ||)
        //return ops;
    }
    public boolean inputsReversible() {
        switch (this) {
            case PLUS:
            case MULTIPLY:
            case L_OR:
            case L_AND:
            case L_XOR:
            case EQUAL:
            case NOT_EQUAL:
            case OR:
            case AND:
                return true;
            default:
                return false;
        }
    }
    public Type onApplication(Type a, Type b) {
        switch (this) {
            case PLUS:
            case MINUS:
                if (a instanceof TypePointer) {
                    if (b instanceof TypePointer) {
                        throw new IllegalStateException("Can't add a pointer to a pointer " + a + " " + b);
                    }
                    if (b instanceof TypeInt64) {
                        return a;
                    } else {
                        return a;
                        //throw new IllegalStateException("Pointers are 64 bits, so for now you can only add 64 bit integers to pointers. sorry");
                    }
                }
            case MULTIPLY:
            case DIVIDE:
            case MOD:
                if (!a.equals(b)) {
                    throw new IllegalStateException("can't do " + this + " on " + a + " and " + b);
                }
                if (!(a instanceof TypeNumerical)) {
                    throw new IllegalStateException("can't do " + this + " on " + a + " and " + b);
                }
                return a;
            case EQUAL:
            case GREATER:
            case LESS:
            case GREATER_OR_EQUAL:
            case LESS_OR_EQUAL:
            case NOT_EQUAL:
                if (!(a instanceof TypeNumerical) || !(b instanceof TypeNumerical) || !a.equals(b)) {
                    throw new IllegalStateException("can't do " + this + " on " + a + " and " + b);
                }
                return new TypeBoolean();
            case OR:
            case AND:
                if (!(a instanceof TypeBoolean) || !(b instanceof TypeBoolean)) {
                    throw new IllegalStateException("can't do " + this + " on " + a + " and " + b);
                }
                return new TypeBoolean();
            case L_XOR:
            case L_AND:
            case L_OR:
                if (!a.equals(b)) {
                    throw new IllegalStateException("can't do " + this + " on " + a + " and " + b);
                }
                if (!(a instanceof TypeNumerical && b instanceof TypeNumerical)) {
                    throw new IllegalStateException("can't do " + this + " on " + a + " and " + b);
                }
                if (b instanceof TypePointer || a instanceof TypePointer) {
                    throw new IllegalStateException("can't do " + this + " on " + a + " and " + b);
                }
                return a;
            case SHIFT_L:
            case SHIFT_R:
            case USHIFT_L:
            case USHIFT_R:
                if (a instanceof TypeBoolean) {
                    throw new IllegalStateException("can't do " + this + " on " + a + " and " + b);
                }
                if (!(a instanceof TypeNumerical && b instanceof TypeNumerical)) {
                    throw new IllegalStateException("can't do " + this + " on " + a + " and " + b);
                }
                if (b instanceof TypePointer || a instanceof TypePointer) {
                    throw new IllegalStateException("can't do " + this + " on " + a + " and " + b);
                }
                return a;
            //don't add a default and maybe throw an exception
        }
        throw new IllegalStateException("This could only happen if someone added a new operator but didn't implement calculating the type it returns. Operator in question: " + this);
    }
    strictfp public <A extends E, E extends Expression & ExpressionConst<? extends V>, V, B extends E> Expression apply(A a, B b) {//used in optimization
        if (!a.getType().equals(b.getType())) {
            throw new FileSystemAlreadyExistsException(a + " " + b + " " + a.getType() + " " + b.getType());
        }
        Type resulting = onApplication(a.getType(), b.getType());//ensure types are valid
        V aVal = a.getVal();
        V bVal = b.getVal();
        if (this == OR || this == AND) {
            if (!(aVal instanceof Boolean)) {
                throw new RuntimeException("Expected " + a + " to be expression const bool");
            }
            if (!(bVal instanceof Boolean)) {
                throw new RuntimeException("Expected " + b + " to be expression const bool");
            }
            Boolean aval = (Boolean) aVal;
            Boolean bval = (Boolean) bVal;
            return new ExpressionConstBool(this == AND ? aval && bval : aval || bval);
        }
        if (!(a instanceof ExpressionConstNum)) {
            throw new RuntimeException("Expected " + a + " to be expression const num");
        }
        if (!(b instanceof ExpressionConstNum)) {
            throw new RuntimeException("Expected " + b + " to be expression const num");
        }
        Number aval = (Number) aVal;
        Number bval = (Number) bVal;
        if ((int) aval.longValue() != aval.intValue() || (int) bval.longValue() != bval.intValue() || (long) aval.intValue() != aval.longValue() || (long) bval.intValue() != bval.longValue()) {
            //if the long version, when casted to int, is different than the int version
            //some form of overflow is happening?
            throw new RuntimeException(aVal + " " + bVal);
        }
        if (resulting instanceof TypeBoolean) {
            return new ExpressionConstBool(calculateBoolean(aval.intValue(), bval.intValue()));
        }
        return new ExpressionConstNum(calculateIntegral(aval.intValue(), bval.intValue()), (TypeNumerical) resulting);
    }
    public int calculateIntegral(int a, int b) {
        switch (this) {
            case L_XOR:
                return a ^ b;
            case L_AND:
                return a & b;
            case L_OR:
                return a | b;
            case SHIFT_L:
            case USHIFT_L://TIL
                return a << b;
            case SHIFT_R:
                return a >> b;
            case USHIFT_R:
                return a >>> b;
            case PLUS:
                return a + b;
            case MINUS:
                return a - b;
            case MULTIPLY:
                return a * b;
            case DIVIDE:
                return a / b;
            case MOD:
                return a % b;
            default:
                throw new IllegalStateException("DO I CALCULATE " + this + " ON " + a + " AND " + b);
        }
    }
    public boolean calculateBoolean(int a, int b) {
        switch (this) {
            case EQUAL:
                return a == b;
            case NOT_EQUAL:
                return a != b;
            case LESS:
                return a < b;
            case GREATER:
                return a > b;
            case GREATER_OR_EQUAL:
                return a >= b;
            case LESS_OR_EQUAL:
                return a <= b;
            default:
                throw new IllegalStateException("DO I CALCULATE " + this + " ON " + a + " AND " + b);
        }
    }
    public Operator invert() {// NOT ( a (OP) b )     should be equal to   a (OP.invert) b
        switch (this) {
            case LESS:
                return GREATER_OR_EQUAL;
            case EQUAL:
                return NOT_EQUAL;
            case GREATER:
                return LESS_OR_EQUAL;
            case NOT_EQUAL:
                return EQUAL;
            case LESS_OR_EQUAL:
                return GREATER;
            case GREATER_OR_EQUAL:
                return LESS;
            default:
                throw new UnsupportedAddressTypeException();
        }
    }
    public Operator swap() {// a (OP) b   should be equal to   b (OP.swap) a
        switch (this) {
            case LESS:
                return GREATER;
            case EQUAL:
                return EQUAL;
            case GREATER:
                return LESS;
            case NOT_EQUAL:
                return NOT_EQUAL;
            case LESS_OR_EQUAL:
                return GREATER_OR_EQUAL;
            case GREATER_OR_EQUAL:
                return LESS_OR_EQUAL;
            default:
                throw new IllegalStateException();
        }
    }
    @Override
    public TokenType tokenType() {
        return TokenType.OPERATOR;
    }
}
