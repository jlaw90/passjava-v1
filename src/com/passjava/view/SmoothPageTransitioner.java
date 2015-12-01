package com.passjava.view;


import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class SmoothPageTransitioner extends JPanel implements Runnable {
    private JLayeredPane target;
    private JComponent old;
    private JComponent newP;
    private Thread thread;
    private boolean run = true;
    private long time;
    private int type;
    private BufferedImage back;

    private SmoothPageTransitioner(JLayeredPane target, JComponent old, JComponent newP, long time, int type) {
        setOpaque(false);
        int width = old == null ? target.getWidth() : old.getWidth();
        int height = old == null ? target.getHeight() : old.getHeight();
        setPreferredSize(new Dimension(width, height));
        setSize(width, height);
        this.target = target;
        this.old = old;
        this.newP = newP;
        this.time = time;
        this.type = type;
    }

    @Override
    public void run() {
        try {
            if (old != null)
                prepare(old);
            prepare(target);
            prepare(newP);

            int width = target == null? old == null? getWidth(): old.getWidth() : target.getWidth();
            int height = target == null ? old == null? getHeight(): old.getHeight() : target.getHeight();

            back = clear(new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB), target.getBackground());
            BufferedImage oi = clear(new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB), target.getBackground());
            if (old != null)
                old.paint(oi.getGraphics());
            Graphics2D t = back.createGraphics();
            t.drawImage(oi, 0, 0, null);
            target.add(this, JLayeredPane.PALETTE_LAYER);
            if(old != null)
                target.remove(old);
            newP.setSize(width, height);
            newP.setPreferredSize(new Dimension(width, height));
            target.add(newP);
            target.revalidate();

            BufferedImage ni = clear(new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB), target.getBackground());
            newP.paint(ni.getGraphics());


            long curTime = System.currentTimeMillis();
            long start = curTime;
            while ((curTime = System.currentTimeMillis()) - start < time && run) {
                t.setPaintMode();

                // transition
                transitions[type].transition((int) (time / 20), (int) ((curTime - start) / 20), t, oi, ni, width, height);
                repaint();

                try {
                    Thread.sleep(20);
                } catch (InterruptedException ignore) {
                    ignore.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        target.remove(this);
        target.revalidate();
        target.repaint();
    }

    private static void prepare(JComponent jc) {
        // swing sucks, if the component is double buffered then you get all sorts of shitty artifacts..
        for (Component c : jc.getComponents())
            if (c instanceof JComponent)
                prepare((JComponent) c);
        jc.setDoubleBuffered(false);
    }

    public void stop() {
        // stop this transition (just skips the rest of the animation...)
        run = false;
        try {
            thread.join();
        } catch (InterruptedException e) {
            /**/
        }
    }

    public void paint(Graphics g) {
        g.drawImage(back, 0, 0, null);
    }

    public void update(Graphics g) {
        paint(g);
    }

    public static void stopTransition(Container t) {
        // Check for transition in progress
        SmoothPageTransitioner oldTran = null;
        for (Component c : t.getComponents()) {
            if (c instanceof SmoothPageTransitioner) {
                oldTran = (SmoothPageTransitioner) c;
                break;
            }
        }
        if (oldTran != null)
            oldTran.stop(); // stop last transition
    }

    public static void transition(JLayeredPane target, JPanel old, JPanel newP, int time, int type) {
        if (type == -1)
            type = (int) (Math.random() * transitions.length);
        if (time == -1)
            time = transitions[type].defaultTime;

        SmoothPageTransitioner r = new SmoothPageTransitioner(target, old, newP, time, type);
        Thread t = new Thread(r);
        t.setPriority(1);
        t.setDaemon(true);
        t.start();
        r.thread = t;
    }

    private static BufferedImage clear(BufferedImage bi, Color c) {
        Graphics g = bi.getGraphics();
        g.setColor(c);
        g.fillRect(0, 0, bi.getWidth(), bi.getHeight());
        return bi;
    }

    private static float getFadeAlpha(int elapsed, int time) {
        return (float) elapsed * (1.0f / (float) time);
    }

    static abstract class Transition {
        protected int defaultTime = 500;

        public abstract void transition(int time, int elapsed, Graphics2D g, BufferedImage oi, BufferedImage ni, int width, int height);
    }

    public static Transition fadeOut = new Transition() {
        @Override
        public void transition(int time, int elapsed, Graphics2D g, BufferedImage oi, BufferedImage ni, int width, int height) {
            float alpha = getFadeAlpha(elapsed, time);
            g.drawImage(ni, 0, 0, null);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 1f - alpha));
            g.drawImage(oi, 0, 0, null);
        }
    };
    public static Transition fadeOutIn = new Transition() {
        {
            defaultTime = 1000;
        }

        @Override
        public void transition(int time, int elapsed, Graphics2D g, BufferedImage oi, BufferedImage ni, int width, int height) {
            // fade to 0 within half the time...
            int pic = elapsed < time / 2 ? 0 : 1;
            float alpha = getFadeAlpha(elapsed, time) * 2;
            if (pic == 1)
                alpha = 1f - (alpha - 1f);
            if(alpha > 1)
                alpha = 1f;
            if(alpha < 0)
                alpha = 0f;

            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 1f - alpha));
            if (pic == 0)
                g.drawImage(oi, 0, 0, width, height, null);
            else
                g.drawImage(ni, 0, 0, width, height, null);
        }
    };

    public static Transition slideRight = new Transition() {
        @Override
        public void transition(int time, int elapsed, Graphics2D g, BufferedImage oi, BufferedImage ni, int width, int height) {
            float off = width / (float) time * elapsed;
            int x = (int) off;
            g.drawImage(oi, x, 0, null);
            g.drawImage(ni, -width + x, 0, null);
        }
    };

    public static Transition slideLeft = new Transition() {
        @Override
        public void transition(int time, int elapsed, Graphics2D g, BufferedImage oi, BufferedImage ni, int width, int height) {
            float off = width / (float) time * elapsed;
            int x = (int) off;
            g.drawImage(oi, -x, 0, null);
            g.drawImage(ni, width - x, 0, null);
        }
    };

    public static Transition slideUp = new Transition() {
        @Override
        public void transition(int time, int elapsed, Graphics2D g, BufferedImage oi, BufferedImage ni, int width, int height) {
            float off = height / (float) time * elapsed;
            int y = (int) off;
            g.drawImage(oi, 0, -y, null);
            g.drawImage(ni, 0, height - y, null);
        }
    };
    public static Transition slideDown = new Transition() {
        @Override
        public void transition(int time, int elapsed, Graphics2D g, BufferedImage oi, BufferedImage ni, int width, int height) {
            float off = height / (float) time * elapsed;
            int y = (int) off;
            g.drawImage(oi, 0, y, null);
            g.drawImage(ni, 0, -height + y, null);
        }
    };

    static Transition[] transitions = {fadeOut, fadeOutIn, slideRight, slideLeft, slideUp, slideDown};
}
