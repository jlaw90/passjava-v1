package com.passjava.model.compiler;

import com.passjava.model.store.Storage;

import javax.tools.*;
import java.io.File;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Locale;

public final class PCompiler {
    private static JavaCompiler compiler;
    private static final String outDir = Storage.getPath("compiled") + File.separator;

    public static void init() {
        // Check our compilation directory
        File f = new File(outDir);
        if(!f.exists() && !f.mkdirs())
            throw new RuntimeException("Could not access compilation directory");

        f = new File(Storage.location + "tools.jar");

        // Locate Compiler
        try {
            // com.sun.tools.javac.api.JavacTool
            URLClassLoader uc = new URLClassLoader(new URL[] {f.toURI().toURL()}, null);

            Class c = uc.loadClass("com.sun.tools.javac.api.JavacTool");
            if(c == null)
                throw new RuntimeException("Could not locate JavacTool");
            Method m = c.getDeclaredMethod("create");
            compiler = (JavaCompiler) m.invoke(null);
        } catch(Exception e) {
            throw new RuntimeException("Problem loading compiler", e);
        }
        if(compiler == null)
            throw new RuntimeException("Could not load compiler from tools.jar");
    }

    public static CompilationResult compile(String name, String data) {
        StringWriter out = new StringWriter();
        StandardJavaFileManager fm = compiler.getStandardFileManager(null, Locale.getDefault(), null);
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        JavaCompiler.CompilationTask task =
                compiler.getTask(
                        out,
                        fm,
                        diagnostics,
                        Arrays.asList("-d", outDir),
                        null,
                        Arrays.asList(new DynamicJavaFile(name, data)));
        boolean res = task.call();

        Class c = null;

        if(res) {
            try {
                // Have to load with a different classloader each time if not we get errros when redefining
                c = new DynamicClassLoader(outDir, ClassLoader.getSystemClassLoader()).findClass(name);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        return new CompilationResult(res, diagnostics.getDiagnostics(), c);
    }
}