package com.passjava.model.compiler;

import java.io.*;

public final class DynamicClassLoader extends ClassLoader {
    private String baseDir;

    protected DynamicClassLoader(String baseDir, ClassLoader parent) {
        super(parent);
        this.baseDir = baseDir;
    }

    private ByteArrayOutputStream baos = new ByteArrayOutputStream();
    private byte[] buf = new byte[5000];
    private synchronized Class defineClass(String name, File f) {
        baos.reset();
        try {
            FileInputStream fis = new FileInputStream(f);
            int read;
            while((read = fis.read(buf, 0, buf.length)) != -1)
                baos.write(buf, 0, read);
            fis.close();
            byte[] data = baos.toByteArray();
            baos.close();

            return super.defineClass(name, data, 0, data.length);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        String fs = baseDir + name.replace('.', File.separatorChar) + ".class";
        File f = new File(fs);
        if(!f.exists())
            throw new ClassNotFoundException();

        return defineClass(name, f);
    }
}