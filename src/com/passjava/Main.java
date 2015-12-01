package com.passjava;

import com.passjava.model.session.Session;
import com.passjava.model.test.Test;
import com.passjava.model.session.User;
import com.passjava.model.store.Storage;
import com.passjava.model.util.Downloader;
import com.passjava.view.*;
import com.passjava.model.compiler.PCompiler;

import javax.swing.*;
import javax.tools.ToolProvider;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.net.URLEncoder;
import java.util.jar.JarOutputStream;
import java.util.jar.Pack200;
import java.util.zip.GZIPInputStream;

public final class Main extends JApplet implements Runnable {
    public static final String base = "http://passjava.com/applet/";

    public final LoadingPage loadingPage;
    private final MainPage page;
    private final HJLayeredPane layerCake;

    public Main() {
        loadingPage = new LoadingPage();
        page = new MainPage();
        layerCake = new HJLayeredPane();
    }

    public void init() {
        setPreferredSize(page.getMainPanel().getPreferredSize());
        setSize(getPreferredSize());
        layerCake.setSize(getPreferredSize());
        layerCake.add(loadingPage.getContent());
        getContentPane().add(layerCake);
    }

    public void start() {
        Thread t = new Thread(this);
        t.setPriority(1);
        t.setDaemon(false);
        t.start();
    }

    private void setLoadingError(String s) {
        try {
            Downloader.download(base + "error.php?s=" + URLEncoder.encode(s, "UTF-8"), null);
        } catch (UnsupportedEncodingException e) {
            // Ignore
        }
        getContentPane().removeAll();
        JTextField jta = new JTextField();
        jta.setHorizontalAlignment(JTextField.CENTER);
        jta.setForeground(Color.RED);
        jta.setFont(jta.getFont().deriveFont(Font.BOLD, 20f));
        jta.setText(s);
        jta.setEditable(false);
        JScrollPane jsp = new JScrollPane(jta);
        getContentPane().add(jsp);
        getContentPane().revalidate();
        getContentPane().repaint();
    }

    private byte[] download(String url, final String name) {
        loadingPage.setText("Downloading " + name + "...");
        Downloader d = Downloader.download(url, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Downloader d = (Downloader) e.getSource();
                switch (e.getID()) {
                    case Downloader.ProgressEnabled:
                        loadingPage.setText("Downloading " + name + " (0%)...");
                        loadingPage.setProgress(0);
                        break;
                    case Downloader.ProgressUpdate:
                        int perc = (int) d.getProgress();
                        loadingPage.setText("Downloading " + name + " (" + perc + "%)...");
                        loadingPage.setProgress(perc);
                        break;
                    case Downloader.Complete:
                        loadingPage.reset();
                        loadingPage.setText("Downloading " + name + " complete!");
                        break;
                    case Downloader.Error:
                        loadingPage.reset();
                        loadingPage.setText("Error downloading " + name + ": " + e.getActionCommand());
                        break;
                }
            }
        });
        d.join();
        if (d.getProgress() == -1)
            return null;
        return d.getData();
    }

    private boolean download(String url, String file, final String name) {
        byte[] data = download(url, name);
        if (data == null)
            return false;
        Storage.place(file, data);
        return true;
    }

    private boolean downloadPacked(String url, String file, final String name) throws IOException {
        String packExt = ".jar.pack";
        if(url.endsWith(".gz"))
            packExt += ".gz";

        byte[] data = download(url, name);
        if (data == null)
            return false;

        loadingPage.setText("Unpacking " + name + " (0%)...");
        Storage.place(file + packExt, data);

        // Unpack
        InputStream in;
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        in = bais;
        if(packExt.endsWith(".gz"))
            in = new GZIPInputStream(bais);
        FileOutputStream fos = new FileOutputStream(Storage.getPath(file));
        JarOutputStream jos = new JarOutputStream(fos);
        final Pack200.Unpacker u = Pack200.newUnpacker();
        u.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                int perc = Integer.parseInt(u.properties().get(Pack200.Unpacker.PROGRESS));
                loadingPage.setText("Unpacking " + name + " (" + perc + "%)...");
                loadingPage.setProgress(perc);
            }
        });
        u.unpack(in, jos);

        // Clean up
        data = null;
        in.close();
        loadingPage.setText("Saving " + name + "...");
        jos.close();
        fos.close();
        bais.close();
        return true;
    }

    public void run() {
        try {
            loadingPage.setText("Locating storage location...");
            // Check java version
            String jver = System.getProperty("java.version");
            String[] parts = jver.split("_"); // remove _u24 shit
            parts = parts[0].split("\\.");
            int maj = Integer.parseInt(parts[0]);
            int min = Integer.parseInt(parts[1]);

            ToolProvider.getSystemJavaCompiler();

            if (maj < 1 && min < 7) {
                setLoadingError("You must have at least Java 7 to use this applet<br/> Download the latest version from http://java.oracle.com");
                return;
            }

            // Check for tools.jar
            String file = "tools.jar";
            if (!Storage.exists(file)) {
                String pre = base + "tools/";
                String post = ".jar.pack.gz";
                String name = "compiler";

                // Download tools.jar
                String ver = System.getProperty("os.name").toLowerCase();
                if (
                        (ver.contains("win") && !downloadPacked(pre + "windows" + post, file, name)) ||
                        ((ver.contains("nux") || ver.contains("nix")) && !downloadPacked(pre + "linux" + post, file, name)) ||
                        (ver.contains("sunos") && !downloadPacked(pre + "solaris" + post, file, name)) ||
                        (ver.contains("mac") && !downloadPacked(pre + "mac" + post, file, name))
                    )
                    setLoadingError("Failed to download tools.jar");
            }
            loadingPage.setText("Initialising compiler");
            PCompiler.init();

            loadingPage.setText("Retrieving user data...");
            String sid = getParameter("sid");
            String d = new String(download(base + "session.php?test=" + getParameter("test") + "&sid=" +
                    URLEncoder.encode(sid, "UTF-8"), "user data"));
            if(d.startsWith("\\"))
                throw new RuntimeException(d.substring(1));


            String[] sessionInf = d.split("\u0000");
            String sess = sessionInf[0];
            String name = sessionInf[1];


            // Todo: load saved session...
            Session s = new Session(sess, new User(name));
            s.question = Integer.parseInt(sessionInf[2]);
            s.tick = Integer.parseInt(sessionInf[3]);
            loadingPage.setText("Downloading test...");
            s.test = new Test(Integer.parseInt(getParameter("test")), sess);
            page.setSession(s, getParameter("readonly") != null);
            SmoothPageTransitioner.transition(layerCake, loadingPage.getContent(), page.getMainPanel(), -1, -1);
        } catch (Throwable e) {
            setLoadingError("Error starting up: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void stop() {
        System.exit(0);
    }

    static {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
            // Ignore
        }
    }
}