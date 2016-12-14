/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.util;
import compiler.Context;
import compiler.command.CommandDefineFunction;
import compiler.parse.Line;
import compiler.parse.Processor;
import compiler.preprocess.Preprocessor;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author leijurv
 */
public class Loader {
    private static Pair<List<CommandDefineFunction>, Context> load(Path name) throws IOException {
        byte[] program;
        try {
            program = Kitterature.getResource(name.toString());
        } catch (IOException | RuntimeException e) {
            try {
                program = Files.readAllBytes(name);
            } catch (IOException | RuntimeException e2) {
                e.printStackTrace();
                e2.printStackTrace();
                throw new RuntimeException("Couldn't load " + name);
            }
        }
        List<Line> lines = Preprocessor.preprocess(new String(program, "UTF-8"));
        Context context = new Context(name + "");
        List<CommandDefineFunction> cmds = Processor.initialParse(lines, context);
        return new Pair<>(cmds, context);
    }
    public static Pair<Context, List<CommandDefineFunction>> importPath(Path path) throws IOException {
        System.out.println("Loading " + path);
        Pair<List<CommandDefineFunction>, Context> funcs = load(path);
        Context context = funcs.getB();
        //System.out.println("Imports: " + context.imports);
        List<Map.Entry<String, String>> copy = new ArrayList<>(context.imports.entrySet());
        context.imports.clear();
        for (Map.Entry<String, String> imp : copy) {
            String toImportName = imp.getKey() + ".k";
            File toImport;
            if (Kitterature.resourceExists(toImportName)) {
                toImport = new File(toImportName);
            } else {
                toImport = new File(path.toFile().getParent(), toImportName);
                if (!Kitterature.resourceExists(toImport + "") && !toImport.exists()) {
                    throw new IllegalStateException(path + " " + "Can't import " + toImportName + " because " + toImport + " doesn't exist" + imp);
                }
            }
            if (Kitterature.resourceExists(toImport + "") && toImport.exists()) {
                throw new RuntimeException("Ambigious whether to import from standard library or from locally for " + toImport);
            }
            Path impPath = new File(Kitterature.trimPath(toImport.toString())).toPath();
            //System.out.println("Replacing path " + toImport.toPath() + " with " + impPath);
            if (!toImport.getCanonicalPath().equals(impPath.toFile().getCanonicalPath())) {
                throw new RuntimeException(toImport.toPath() + " " + impPath + " " + toImport.getCanonicalPath() + " " + impPath.toFile().getCanonicalPath());
            }
            context.imports.put(impPath + "", imp.getValue());
        }
        return new Pair<>(context, funcs.getA());
    }
}
