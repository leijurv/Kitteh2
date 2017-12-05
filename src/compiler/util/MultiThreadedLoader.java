/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.util;
import compiler.Context;
import compiler.command.CommandDefineFunction;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;

/**
 *
 * @author leijurv
 */
public class MultiThreadedLoader {
    private final HashSet<Thread> inProgress = new HashSet<>();
    private final HashSet<Path> alrImp = new HashSet<>();
    private volatile transient Exception thrown;
    private final CompilationState semaphore;
    public static void importFiles(CompilationState cs) {
        new MultiThreadedLoader(cs).mainImportLoop();
    }
    private MultiThreadedLoader(CompilationState cs) {
        this.semaphore = cs;
        alrImp.addAll(cs.toLoad());
    }
    private void mainImportLoop() {
        for (int i = 0; i < semaphore.toLoad().size(); i++) {//watch me whip, now watch me iterate over a linked list using indicies which is O(n^2)
            //rolls right off the tongue doesn't it
            importFileInNewThread(semaphore.toLoad().get(i), i == 0);
        }
        while (true) {
            boolean done = false;
            synchronized (this) {
                if (thrown != null) {
                    if (thrown instanceof RuntimeException) {
                        throw (RuntimeException) thrown;
                        //if it's a runtimeexception, we can just throw it (but only if we cast it first)
                    } else {
                        throw new IllegalStateException(thrown);//its a checked exception that we can't just throw, we have to wrap it in an unchecked exception
                    }
                }
                if (inProgress.isEmpty()) {
                    done = true;
                }
            }
            if (done) {
                break;
            }
            try {
                synchronized (semaphore) {
                    if (!done) {
                        semaphore.wait(5);
                    }
                }
            } catch (InterruptedException ex) {
                throw new IllegalStateException(ex);
            }
        }
    }
    public void importFileInNewThread(Path path, boolean isFirst) {
        Thread th = new Thread() {
            @Override
            public void run() {
                try {
                    importFile(path, isFirst);
                } catch (IOException | RuntimeException ex) {
                    synchronized (MultiThreadedLoader.this) {
                        if (thrown != null) {
                            System.out.println("At least two exceptions detected in loader threads, only throwing the first. Discarding " + ex);
                            return;
                        }
                        System.out.println("Got exception while loading " + path + ": " + ex);
                        thrown = ex;
                    }
                    synchronized (semaphore) {
                        semaphore.notifyAll();
                    }
                    synchronized (MultiThreadedLoader.this) {
                        inProgress.remove(this);
                    }
                }
            }
        };
        synchronized (this) {
            inProgress.add(th);
        }
        th.start();
    }
    public void importFile(Path path, boolean isFirst) throws IOException {
        Pair<Context, List<CommandDefineFunction>> loadResult = Loader.importPath(path);//<-- actual import
        Context context = loadResult.getA();
        for (String str : context.imports.keySet()) {//netbeans thinks i can use a functional operation, but i can't: this for loop uses a continue
            Path impPath = new File(str).toPath();//every file that this file is trying to import, start a thread for it if there isn't one already
            synchronized (this) {
                if (alrImp.contains(impPath)) {
                    continue;
                } else {
                    alrImp.add(impPath);
                }
            }
            importFileInNewThread(impPath, false);
        }
        semaphore.add(path, context, isFirst, loadResult.getB());
        if (compiler.Compiler.verbose()) {
            System.out.println(path + " done, notifying");
        }
        synchronized (this) {
            inProgress.remove(Thread.currentThread());
        }
        synchronized (semaphore) {
            semaphore.notifyAll();
        }
    }
}
