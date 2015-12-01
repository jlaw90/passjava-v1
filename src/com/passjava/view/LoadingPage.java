package com.passjava.view;

import javax.swing.*;

public class LoadingPage {
    private JPanel mainPanel;
    private JProgressBar progressBar1;
    private JLabel progressIndicator;

    public void reset() {
        progressIndicator.setText("Loading, please wait...");
        progressBar1.setIndeterminate(true);
    }

    public void setProgress(int perc) {
        progressBar1.setIndeterminate(false);
        progressBar1.setValue(perc);
    }

    public void setText(String text) {
        progressIndicator.setText(text);
    }

    public void setError(String text) {
        progressBar1.setVisible(false);
        progressIndicator.setIcon(null);
        progressIndicator.setText("<html><span style='color: #ff0000;font-weight:bold;'>" + text);
    }

    public JPanel getContent() {
        return mainPanel;
    }
}