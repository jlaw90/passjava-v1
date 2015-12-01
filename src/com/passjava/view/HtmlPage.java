package com.passjava.view;

import javax.swing.*;
import java.io.IOException;

public class HtmlPage {
    private JEditorPane htmlPane;
    private JPanel contentPanel;

    public JPanel getContent() {
        return contentPanel;
    }

    public void setHtml(String html) {
        htmlPane.setText(html);
    }

    public void loadUrl(final String url) {
        setHtml("<p align=\"center\">Please wait, loading...</p>");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    htmlPane.setPage(url);
                } catch (IOException e) {
                    htmlPane.setText("<p style=\"text-align: center; color:#ff0000\">Error loading content</p>");
                }
            }
        }).start();
    }
}