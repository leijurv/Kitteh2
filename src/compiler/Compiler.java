/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler;
import compiler.command.CommandDefineFunction;
import compiler.parse.Line;
import compiler.parse.Processor;
import compiler.preprocess.Preprocessor;
import compiler.tac.TACStatement;
import compiler.tac.optimize.OptimizationSettings;
import compiler.type.TypeStruct;
import compiler.util.Kitterature;
import compiler.util.Pair;
import compiler.x86.X86Format;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 *
 * @author leijurv
 */
public class Compiler {
    static boolean PRINT_TAC = false;
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
    public static void mainImportLoop(CompilationState cs) throws IOException {
        while (cs.has()) {
            Path path = cs.pop();
            System.out.println("Loading " + path);
            Pair<List<CommandDefineFunction>, Context> funcs = load(path);
            Context context = funcs.getB();
            //System.out.println("Imports: " + context.imports);
            HashMap<String, String> fix = new HashMap<>();
            HashSet<String> rmv = new HashSet<>();
            for (Entry<String, String> imp : context.imports.entrySet()) {
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
                rmv.add(imp.getKey());
                fix.put(impPath + "", imp.getValue());
                cs.newImport(impPath);
            }
            //System.out.println("FIXING " + fix + " " + rmv);
            for (String s : rmv) {
                context.imports.remove(s);
            }
            for (Entry<String, String> entry : fix.entrySet()) {
                context.imports.put(entry.getKey(), entry.getValue());
            }
            cs.doneImporting(path, context, funcs.getA());
        }
    }
    public static void insertStructs(CompilationState cs) {
        for (Pair<Path, List<CommandDefineFunction>> pair : cs.loaded) {
            Context context = cs.ctxts.get(pair.getA());
            for (Pair<Path, List<CommandDefineFunction>> oth : cs.loaded) {
                if (oth.getA() != null && cs.autoImportedStd.contains(oth.getA())) {
                    if (oth.getA().equals(pair.getA())) {
                        continue;
                    }
                    if (PRINT_TAC) {
                        System.out.println("Assuming autoimported stdlib for " + oth.getA());
                    }
                    context.insertStructsUnderPackage(null, cs.importz.get(oth.getA()));
                }
            }
            for (Entry<String, String> imp : context.imports.entrySet()) {
                Path importing = new File(imp.getKey()).toPath();
                String underName = imp.getValue();
                context.insertStructsUnderPackage(underName, cs.importz.get(importing));
            }
        }
        cs.getStructs().stream().forEach(TypeStruct::parseContents);
        cs.getStructs().stream().forEach(TypeStruct::allocate);
    }
    public static String compile(Path main, OptimizationSettings settings) throws IOException {
        long a = System.currentTimeMillis();
        CompilationState cs = new CompilationState(main);
        mainImportLoop(cs);
        long b = System.currentTimeMillis();
        insertStructs(cs);
        long c = System.currentTimeMillis();
        List<CommandDefineFunction> allFunctions = cs.allFunctions();
        allFunctions.parallelStream().forEach(CommandDefineFunction::parseHeader);
        long d = System.currentTimeMillis();
        cs.generateFunctionsContexts();
        long e = System.currentTimeMillis();
        if (PRINT_TAC) {
            System.out.println("load: " + (b - a) + "ms, structs: " + (c - b) + "ms, parseheaders: " + (d - c) + "ms, funcContext: " + (e - d) + "ms");
            System.out.println();
            System.out.println("---- END IMPORTS, BEGIN PARSING ----");
            System.out.println();
        }
        cs.parseAllFunctionsContexts();
        cs.parseStructMethods();
        return generateASM(allFunctions, settings);
    }
    public static String compile(String program, OptimizationSettings settings) {
        try {
            File f = File.createTempFile("temp", ".k");
            f.deleteOnExit();
            try (FileOutputStream lol = new FileOutputStream(f)) {
                lol.write(program.getBytes("UTF-8"));
            }
            return compile(f.toPath(), settings);
        } catch (IOException ex) {
            Logger.getLogger(Compiler.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    private static String generateASM(List<CommandDefineFunction> commands, OptimizationSettings settings) {
        long d = System.currentTimeMillis();
        if (settings.staticValues()) {
            commands.parallelStream().forEach(CommandDefineFunction::staticValues);
        }
        //System.out.println("> DONE STATIC VALUES");
        long e = System.currentTimeMillis();
        List<Pair<String, List<TACStatement>>> wew = commands.parallelStream()
                .map(com -> new Pair<>(com.getHeader().name, com.totac(settings)))
                .collect(Collectors.toList());
        long f = System.currentTimeMillis();
        if (PRINT_TAC) {
            for (Pair<String, List<TACStatement>> pair : wew) {
                System.out.println("TAC FOR " + pair.getA());
                for (int i = 0; i < pair.getB().size(); i++) {
                    System.out.println(i + ":     " + pair.getB().get(i).toString(false));
                }
                System.out.println();
            }
        }
        long g = System.currentTimeMillis();
        String asm = X86Format.assembleFinalFile(wew);
        long h = System.currentTimeMillis();
        String loll = ("static " + (e - d) + " tacgen " + (f - e) + " debugtac " + (g - f) + " x86gen " + (h - g));
        if (PRINT_TAC) {
            System.out.println(loll);
            System.err.println(loll);
        }
        return asm;
    }
}
