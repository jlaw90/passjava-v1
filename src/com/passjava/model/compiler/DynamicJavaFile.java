package com.passjava.model.compiler;

import javax.tools.SimpleJavaFileObject;
import java.io.IOException;
import java.net.URI;

public final class DynamicJavaFile extends SimpleJavaFileObject {
    private String source;

    protected DynamicJavaFile(String name, String source) {
        super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
        this.source = source;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
        return source ;
    }
}