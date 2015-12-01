package com.passjava.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;

public class HJLayeredPane extends JLayeredPane implements ComponentListener, ContainerListener {
    public HJLayeredPane() {
        addComponentListener(this);
        addContainerListener(this);
    }


    @Override
    public void componentResized(ComponentEvent e) {
        for(Component c: this.getComponents()) {
            c.setPreferredSize(getSize());
            c.setSize(getSize());
        }
    }

    @Override
    public void componentMoved(ComponentEvent e) {
        componentResized(null);
    }

    @Override
    public void componentShown(ComponentEvent e) {
        componentResized(null);
    }

    @Override
    public void componentHidden(ComponentEvent e) {
        componentResized(null);
    }

    @Override
    public void componentAdded(ContainerEvent e) {
        componentResized(null);
    }

    @Override
    public void componentRemoved(ContainerEvent e) {
        componentResized(null);
    }
}