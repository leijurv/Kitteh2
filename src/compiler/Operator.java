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
import compiler.type.Type;
import compiler.type.TypeBoolean;
import compiler.type.TypeInt64;
import compiler.type.TypeNumerical;
import compiler.type.TypePointer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author leijurv
 */
public enum Operator {//extends Token maybe? might make things easier... idk
    PLUS("+", 50),
    MINUS("-", 50),
    MULTIPLY("*", 100),
    DIVIDE("/", 100),
    MOD("%", 1000),
    EQUAL("==", 10),
    NOT_EQUAL("!=", 10),
    GREATER(">", 10),
    LESS("<", 10),
    GREATER_OR_EQUAL(">=", 10),
    LESS_OR_EQUAL("<=", 10),
    OR("||", 4),//OR has less precedence than AND.   so a || b && c will actually be a || (b && c)
    AND("&&", 5);
    public static final ArrayList<List<Operator>> ORDER = orderOfOperations();//sorry this can't be the first line
    private final String str;
    private final int precedence;
    private Operator(String str, int precedence) {
        this.str = str;
        this.precedence = precedence;
    }
    @Override
    public String toString() {
        return str;
    }
    public static ArrayList<List<Operator>> orderOfOperations() {
        //Having it just be an array would put equal things next to each other, but not at the same place
        //For example, + might be sorted before - even though they have the same precedence
        //so, a-b+c might be parsed as a-(b+c)
        //having it be a 2d array fixes that
        Map<Integer, List<Operator>> precToOp = Stream.of(values()).collect(Collectors.groupingBy(op -> op.precedence));
        return Stream.of(values()).map(op -> op.precedence).distinct().sorted(Comparator.comparingInt(prec -> -prec)).map(prec -> precToOp.get(prec)).collect(Collectors.toCollection(ArrayList::new));
        //ArrayList<Operator> ops = new ArrayList<>(Arrays.asList(values()));
        //reverse order, so that the most important comes first (%) and least important comes last (&&, ||)
        //return ops;
    }
    public Type onApplication(Type a, Type b) {
        switch (this) {
            case PLUS:
                if (a instanceof TypePointer) {
                    if (b instanceof TypeInt64) {
                        return a;
                    } else {
                        return a;
                        //throw new IllegalStateException("Pointers are 64 bits, so for now you can only add 64 bit integers to pointers. sorry");
                    }
                }
            case MINUS:
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
                if (!(a instanceof TypeNumerical) || !(b instanceof TypeNumerical)) {
                    throw new IllegalStateException("can't do " + this + " on " + a + " and " + b);
                }
                return new TypeBoolean();
            case OR:
            case AND:
                if (!(a instanceof TypeBoolean) || !(b instanceof TypeBoolean)) {
                    throw new IllegalStateException("can't do " + this + " on " + a + " and " + b);
                }
                return new TypeBoolean();
            //dont add a default
        }
        throw new IllegalStateException("This could only happen if someone added a new operator but didn't implement calculating the type it returns. Operator in question: " + this);
    }
    public ExpressionConst apply(ExpressionConst a, ExpressionConst b) {//used in optimization
        onApplication(((Expression) a).getType(), ((Expression) b).getType());//ensure types are valid
        switch (this) {
            case PLUS:
                return new ExpressionConstNum(((ExpressionConstNum) a).getVal().intValue() + ((ExpressionConstNum) b).getVal().intValue());
            case MINUS:
                return new ExpressionConstNum(((ExpressionConstNum) a).getVal().intValue() - ((ExpressionConstNum) b).getVal().intValue());
            case MULTIPLY:
                return new ExpressionConstNum(((ExpressionConstNum) a).getVal().intValue() * ((ExpressionConstNum) b).getVal().intValue());
            case DIVIDE:
                return new ExpressionConstNum(((ExpressionConstNum) a).getVal().intValue() / ((ExpressionConstNum) b).getVal().intValue());
            case MOD:
                return new ExpressionConstNum(((ExpressionConstNum) a).getVal().intValue() % ((ExpressionConstNum) b).getVal().intValue());
            case EQUAL:
                return new ExpressionConstBool(((ExpressionConstNum) a).getVal().intValue() == ((ExpressionConstNum) b).getVal().intValue());
            case NOT_EQUAL:
                return new ExpressionConstBool(((ExpressionConstNum) a).getVal().intValue() != ((ExpressionConstNum) b).getVal().intValue());
            case LESS:
                return new ExpressionConstBool(((ExpressionConstNum) a).getVal().intValue() < ((ExpressionConstNum) b).getVal().intValue());
            case GREATER:
                return new ExpressionConstBool(((ExpressionConstNum) a).getVal().intValue() > ((ExpressionConstNum) b).getVal().intValue());
            case GREATER_OR_EQUAL:
                return new ExpressionConstBool(((ExpressionConstNum) a).getVal().intValue() >= ((ExpressionConstNum) b).getVal().intValue());
            case LESS_OR_EQUAL:
                return new ExpressionConstBool(((ExpressionConstNum) a).getVal().intValue() <= ((ExpressionConstNum) b).getVal().intValue());
            case AND:
                return new ExpressionConstBool(((ExpressionConstBool) a).getVal() && ((ExpressionConstBool) b).getVal());
            case OR:
                return new ExpressionConstBool(((ExpressionConstBool) a).getVal() || ((ExpressionConstBool) b).getVal());
            default:
                throw new IllegalStateException("DUDE IDK MAN. HOW THE HELL DO I CALCULATE " + this + " ON " + a + " AND " + b);
        }
    }
    public String tox86() {
        switch (this) {
            case LESS:
                return "jl";
            case EQUAL:
                return "je";
            case GREATER:
                return "jg";
            case NOT_EQUAL:
                return "jne";
            case LESS_OR_EQUAL:
                return "jle";
            case GREATER_OR_EQUAL:
                return "jge";
            default:
                throw new IllegalStateException();
        }
    }
    public Operator invert() {
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
                throw new IllegalStateException();
        }
    }
}
