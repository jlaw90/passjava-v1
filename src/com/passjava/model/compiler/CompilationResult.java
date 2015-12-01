package com.passjava.model.compiler;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.util.Collections;
import java.util.List;

public final class CompilationResult {
    public final boolean succeeded;
    public final Class creation;
    public final List<Diagnostic<? extends JavaFileObject>> diagnostics;

    public CompilationResult(boolean succeeded, List<Diagnostic<? extends JavaFileObject>> diagnostics, Class result) {
        this.succeeded = succeeded;
        this.diagnostics = Collections.unmodifiableList(diagnostics);
        this.creation = result;
    }
}