/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler;
import java.util.ArrayList;

/**
 *
 * @author leijurv
 */
public class TACGenerator {
    public static void generateTAC(Context context, IREmitter emit, ArrayList<Command> commands) {
        for (Command command : commands) {
            command.generateTAC(context, emit);
        }
    }
}
