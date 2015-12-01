package com.passjava.model.util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public final class Downloader implements Runnable {
    public static final int ProgressEnabled = 1;
    public static final int ProgressUpdate = 2;
    public static final int Complete = 3;
    public static final int Error = -1;

    private ActionListener callback;
    private String url;
    private byte[] data;
    private float progress;
    private Thread t;

    private Downloader(String url, ActionListener callback) {
        this.url = url;
        this.callback = callback;
        t = new Thread(this);
        t.setPriority(1);
        t.setDaemon(true);
        t.start();
    }

    public byte[] getData() {
        return data;
    }

    public float getProgress() {
        return progress;
    }

    public void run() {
        try {
            URLConnection uc = new URL(url).openConnection();
            InputStream is = uc.getInputStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] data = new byte[5000];
            int read;
            int len = uc.getContentLength();
            if (len != -1 && callback != null)
                callback.actionPerformed(new ActionEvent(this, ProgressEnabled, null));
            int total = 0;
            while((read = is.read(data, 0, data.length)) != -1) {
                baos.write(data, 0, read);
                total += read;
                progress = ((float) total / (float) len) * 100f;
                if(len != -1 && callback != null)
                    callback.actionPerformed(new ActionEvent(this, ProgressUpdate, null));
            }
            is.close();
            this.data = baos.toByteArray();
            baos.reset();
            baos.close();
            if(callback != null)
            callback.actionPerformed(new ActionEvent(this, Complete, null));
        } catch (Exception e) {
            progress = -1;
            if(callback != null)
                callback.actionPerformed(new ActionEvent(this, Error, e.getMessage()));
        }
    }

    public void join() {
        try {
            t.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static Downloader download(String url, ActionListener callback) {
        return new Downloader(url, callback);
    }
}