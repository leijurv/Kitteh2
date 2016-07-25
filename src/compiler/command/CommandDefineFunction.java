/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.command;
import compiler.Context;
import compiler.X86Emitter;
import compiler.tac.IREmitter;
import compiler.tac.TACReturn;
import compiler.type.Type;
import java.util.ArrayList;
import javafx.util.Pair;

/**
 *
 * @author leijurv
 */
public class CommandDefineFunction extends Command {
    ArrayList<Pair<String, Type>> arguments;
    Type returnType;
    String name;
    ArrayList<Command> contents;
    public CommandDefineFunction(Context context, Type returnType, ArrayList<Pair<String, Type>> arguments, String functionName) {
        super(context);
        this.arguments = arguments;
        this.name = functionName;
        this.returnType = returnType;
    }
    public void setContents(ArrayList<Command> contents) {
        this.contents = contents;
    }
    public Type getReturnType() {
        return returnType;
    }
    @Override
    protected void generateTAC0(IREmitter emit) {
        throw new UnsupportedOperationException("Not supported yet, you poo."); //To change body of generated methods, choose Tools | Templates.
    }
    @Override
    protected int calculateTACLength() {
        throw new UnsupportedOperationException("Not supported yet, you poo."); //To change body of generated methods, choose Tools | Templates.
    }
    @Override
    public void staticValues() {
        for (Command com : contents) {
            com.staticValues();
        }
    }
    public void generateX86(StringBuilder resp) {
        IREmitter emit = new IREmitter();
        for (Command com : contents) {
            com.generateTAC(emit);
        }
        System.out.println("TAC FOR " + name);
        for (int i = 0; i < emit.getResult().size(); i++) {
            System.out.println(i + ":     " + emit.getResult().get(i));
        }
        System.out.println();
        X86Emitter emitter = new X86Emitter(name);
        for (int i = 0; i < emit.getResult().size(); i++) {
            emitter.addStatement(emitter.lineToLabel(i) + ":");
            emit.getResult().get(i).printx86(emitter);
        }
        boolean endsWithReturn = emit.getResult().get(emit.getResult().size() - 1) instanceof TACReturn;
        emitter.addStatement(emitter.lineToLabel(emit.getResult().size()) + ":");
        if (!endsWithReturn) {
            new TACReturn().printx86(emitter);
        }
        resp.append("	.globl	_").append(name).append("\n	.align	4, 0x90\n");
        resp.append("_").append(name).append(":\n");
        resp.append(FUNC_HEADER).append('\n');
        resp.append(emitter.toX86()).append('\n');
        resp.append(FUNC_FOOTER).append('\n');
    }
    static final String FUNC_HEADER = "	.cfi_startproc\n"
            + "	pushq	%rbp\n"
            + "	.cfi_def_cfa_offset 16\n"
            + "	.cfi_offset %rbp, -16\n"
            + "	movq	%rsp, %rbp\n"
            + "	.cfi_def_cfa_register %rbp";
    static final String FUNC_FOOTER = "	.cfi_endproc";
}
