package com.passjava.model.store;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public final class Storage {
    public static final String location;

    public static boolean exists(String file) {
        return new File(location + file).exists();
    }

    public static String getPath(String file) {
        return location + file;
    }

    public static void place(String file, byte[] data) {
        try {
            OutputStream os = new FileOutputStream(getPath(file));
            os.write(data, 0, data.length);
            os.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    static {
        String h = System.getProperty("user.home");
        String sep = System.getProperty("file.separator");
        if(!h.endsWith(sep))
            h += sep;
        h += ".passjava" + sep;
        location = h;
        File f = new File(location);
        if(!f.exists() && !f.mkdirs())
            throw new RuntimeException("Failed to create storage directory");
    }
}