/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 *
 * @author leijurv
 */
public class X86Emitter {
    public static final String STATIC_LABEL_PREFIX = "L";
    private final ArrayList<String> statements = new ArrayList<>();
    private final String prefix;
    public X86Emitter(String funcLabelPrefix) {
        prefix = STATIC_LABEL_PREFIX + "_" + funcLabelPrefix + "_";
    }
    public void addStatement(String SSSSS) {
        String ssnek = SSSSS;
        if (!ssnek.endsWith(":") && !ssnek.startsWith("#")) {
            ssnek = "    " + ssnek;//dont ask
        }
        statements.add(ssnek);
    }
    public String lineToLabel(int line) {
        return prefix + line;
    }
    public String toX86() {
        return statements.stream().collect(Collectors.joining("\n", "", ""));
    }
}
