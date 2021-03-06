/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.parse.expression;
import compiler.Context;
import compiler.Keyword;
import compiler.command.CommandDefineFunction;
import compiler.expression.Expression;
import compiler.expression.ExpressionConstNum;
import compiler.expression.ExpressionFunctionCall;
import compiler.token.Token;
import static compiler.token.Token.is;
import static compiler.token.TokenType.*;
import compiler.type.Type;
import compiler.type.TypeInt32;
import compiler.type.TypePointer;
import compiler.type.TypeStruct;
import compiler.util.ParseUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 *
 * @author leijurv
 */
class RecursiveParentheses extends TokenBased {
    RecursiveParentheses() {
        super(STARTPAREN);
    }
    @Override
    protected boolean apply(int i, ArrayList<Object> o, Optional<Type> desiredType, Context context) {
        ArrayList<ArrayList<Object>> inParen = new ArrayList<>();
        int numToRemoveAti = fill(i, o, inParen);
        if (i != 0 && o.get(i - 1) == Keyword.SIZEOF) {
            if (inParen.size() != 1) {
                throw new IllegalStateException();
            }
            Type type = ParseUtil.typeFromObjs(inParen.get(0), context);
            if (type == null) {
                type = ExpressionParser.parseImpl(new ArrayList<>(inParen.get(0)), Optional.empty(), context).getType();//not doing sizeof(int), we're doing sizeof(variableWithTypeInt)
                //throw new IllegalStateException();
            }
            for (int j = 0; j < numToRemoveAti; j++) {
                o.remove(i);
            }
            o.set(i - 1, new ExpressionConstNum(type.getSizeBytes(), new TypeInt32()));
            return true;
        }
        if (inParen.size() == 1 && ParseUtil.typeFromObjs(inParen.get(0), context) != null) {
            //this is a cast, skip the rest and don't modify these parentheses
            return false;
        } else {
            //not a cast
            for (int j = 0; j < numToRemoveAti; j++) {
                o.remove(i);
            }
        }
        if (func(i, o, context, inParen)) {
            return true;
        }
        //System.out.println("Doing replace " + o + " " + inParen);
        if (inParen.size() != 1) {
            throw new IllegalStateException("This has commas or is empty, but isn't a function call " + inParen);
        }
        o.add(i, ExpressionParser.parseImpl(inParen.get(0), desiredType, context));
        return true;
    }
    private static int fill(int i, ArrayList<Object> o, ArrayList<ArrayList<Object>> inParen) {
        ArrayList<Object> temp = new ArrayList<>();
        int numParens = 1;
        ArrayList<Object> copy = new ArrayList<>(o);
        int numToRemoveAti = 1;
        copy.remove(i);
        while (i < copy.size()) {
            Object b = copy.remove(i);
            numToRemoveAti++;
            if (b == ENDPAREN) {
                numParens--;
                if (numParens == 0) {
                    if (temp.isEmpty()) {
                        if (numToRemoveAti != 2) {
                            throw new IllegalStateException("Dangling comma");
                        }
                    } else {
                        inParen.add(temp);
                    }
                    break;
                }
            }
            if (b == COMMA && numParens == 1) {
                inParen.add(temp);
                temp = new ArrayList<>();
            } else {
                temp.add(b);
            }
            if (b == STARTPAREN) {
                numParens++;
            }
        }
        if (numParens != 0) {
            throw new IllegalStateException("mismatched ( and )");
        }
        return numToRemoveAti;
    }
    static private boolean func(int i, ArrayList<Object> o, Context context, ArrayList<ArrayList<Object>> inParen) {
        if (i != 0 && (is(o.get(i - 1), VARIABLE) || is(o.get(i - 1), KEYWORD))) {
            String funcName;
            if (is(o.get(i - 1), VARIABLE)) {
                funcName = (String) ((Token) o.get(i - 1)).data();
            } else {
                funcName = o.get(i - 1).toString();//some functions that you call are also keywords
            }
            String pkg = null;
            boolean removePreviousTwo = false;
            if (i != 1 && o.get(i - 2) == ACCESS) {
                String accessing = (String) ((Token) o.get(i - 3)).data();
                pkg = context.reverseAlias(accessing);
                if (compiler.Compiler.verbose()) {
                    System.out.println("Accessing " + accessing + " alias for " + pkg + " ::" + funcName);
                }
                removePreviousTwo = true;
            }
            Expression accessing = null;
            if (i != 1 && o.get(i - 2) == PERIOD) {
                accessing = (Expression) o.get(i - 3);
                removePreviousTwo = true;
                if (accessing.getType() instanceof TypeStruct) {
                    throw new IllegalStateException("Can only call struct methods on a pointer to a struct, not the struct itself");
                }
                if (!(accessing.getType() instanceof TypePointer)) {
                    throw new IllegalStateException("Struct methods must be called on a pointer to the struct");
                }
                TypeStruct ts = (TypeStruct) (((TypePointer) accessing.getType()).pointingTo());
                funcName = TypeStruct.format(ts.getName(), funcName);
            }
            final String funcNameCopy = funcName;
            List<Type> desiredTypes = context.gc.getHeader(pkg, funcName).inputs();
            //System.out.println("Expecting inputs: " + desiredTypes);
            ArrayList<ArrayList<Object>> args = new ArrayList<>(inParen);
            if (accessing != null) {
                args.add(0, new ArrayList<>(Arrays.asList(accessing)));
            }
            if (args.size() != desiredTypes.size() && !"syscall".equals(funcName)) {
                throw new IllegalStateException("mismatched arg count " + args + " " + desiredTypes);
            }
            List<Expression> arguments = IntStream.range(0, args.size())
                    .mapToObj(p -> ExpressionParser.parseImpl(args.get(p), "print".equals(funcNameCopy) ? Optional.empty() : Optional.of(desiredTypes.get(p)), context))//don't box because this is an arraylist lookup which is already int not Integer
                    .collect(Collectors.toList());
            o.set(i - 1, new ExpressionFunctionCall(context, pkg, funcName, arguments));
            if ("free".equals(funcName) && pkg == null) {//calling free without a ::
                Expression arg = arguments.get(0);
                Type freeing = arg.getType();
                if (freeing instanceof TypePointer) {//freeing a pointer
                    Type pointingTo = ((TypePointer) freeing).pointingTo();
                    if (pointingTo instanceof TypeStruct) {//freeing a pointer to a struct
                        TypeStruct struct = (TypeStruct) pointingTo;
                        String xFree = TypeStruct.format(struct.getName(), "free");
                        Optional<CommandDefineFunction> freeDefinition = struct.getMethodByLocalName(xFree);
                        if (freeDefinition.isPresent()) {//does this struct* we're freeing have a .free() struct method?
                            o.set(i - 1, new ExpressionFunctionCall(context, null, xFree, arguments));//call x.free() immediately before free(x)
                        }
                        //if the struct doesn't provide a free, just call the normal
                    }
                }
            }
            if (removePreviousTwo) {
                o.remove(i - 3);
                o.remove(i - 3);
            }
            return true;
        }
        return false;
    }
}
