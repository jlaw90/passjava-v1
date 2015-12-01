package com.passjava.view;

import com.passjava.model.session.Session;
import com.passjava.model.test.Result;

import javax.naming.OperationNotSupportedException;
import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.text.DateFormat;

public class ResultsPage {
    private JPanel contentArea;
    private JEditorPane resultArea;
    private JButton printButton;
    private JButton saveButton;

    public ResultsPage() {
        resultArea.setContentType("text/html");
        printButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    resultArea.print();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(contentArea, ex.toString(), "Failed to print", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File f = save();
                if (f == null)
                    return;

                if (Desktop.isDesktopSupported())
                    try {
                        Desktop.getDesktop().open(f);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
            }
        });
    }

    private File save() {
        try {
            JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());

            if (jfc.showSaveDialog(contentArea) != JFileChooser.APPROVE_OPTION)
                return null;

            File f = jfc.getSelectedFile();

            if (f.exists() && !f.isDirectory() &&
                    JOptionPane.showConfirmDialog(contentArea, "A file with the name exists, if you continue it will be overwritten.",
                            "File exists", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE)
                            != JOptionPane.OK_OPTION)
                return null;

            save(f);

            return f;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private void save(File f) throws IOException {
        FileWriter fw = null;
        fw = new FileWriter(f);
        fw.write(resultArea.getText());
        fw.close();
    }

    public void init(Session s, Result[] results) {
        String wrongColor = "#CD5C5C";
        String rightColor = "#ADFF2F";

        StringBuilder sb = new StringBuilder("<html><head><style type=\"text/css\">");

        sb.append("body { font-family: Helvetica;}");
        sb.append(".feedbackcontainer {border-width: 1px;  border-color: #cdcdcd; border-style: solid; padding: 20px; margin: 0 20px 20px 20px; }");
        sb.append(".question { font-style: italic; color: gray; }");
        sb.append(".result-correct { color: ").append(rightColor).append("; }");
        sb.append(".result-incorrect { color: ").append(wrongColor).append("; }");

        sb.append("</style></head><body align=\"center\"><br/><h1>").append(s.test.getName()).append(" Results</h1><br/>");

        sb.append("Presented to: ").append(s.user.name).append("<br/>on ").
                append(DateFormat.getDateInstance(DateFormat.LONG).format(s.finishedDate))
                .append(" at ").append(DateFormat.getTimeInstance(DateFormat.SHORT).format(s.finishedDate)).append("<br/><br/>");


        // Time elapsed...
        sb.append("Time taken: ");
        int time = s.tick / 2;
        int secs = time % 60;
        time /= 60;
        int mins = time % 60;
        time /= 60;
        int hours = time;
        if (hours != 0)
            sb.append(hours).append(" hour").append(hours == 1 ? "" : "s").append(", ");
        if (mins != 0 || hours != 0)
            sb.append(mins).append(" minute").append(mins == 1 ? "" : "s").append(" and ");
        sb.append(secs).append(" seconds.<br/>");

        // Time per question...
        sb.append("Avg. time per question: ");
        time = (s.tick / 2) / results.length;
        secs = time % 60;
        time /= 60;
        mins = time % 60;
        time /= 60;
        hours = time;
        if (hours != 0)
            sb.append(hours).append(" hour").append(hours == 1 ? "" : "s").append(", ");
        if (mins != 0 || hours != 0)
            sb.append(mins).append(" minute").append(mins == 1 ? "" : "s").append(" and ");
        sb.append(secs).append(" seconds.<br/><br/>");

        // Number of questions answered correctly...
        int correct = 0;
        for (Result result : results) {
            if (result.correct) correct++;
        }
        sb.append("Questions correct: ").append(correct).append(" / ").append(results.length).
                append(" (").append(percentage(correct, results.length)).append("%)<br/><br/><br/>");

        // Table of results...
        for (int i = 0; i < results.length; i++) {
            Result r = results[i];

            String rS = r.correct? "correct": "incorrect";
            String rC = r.correct? rightColor: wrongColor;

            sb.append("<h2>Question ").append(i + 1).append(" - <span class=\"result-").append(rS).append("\">")
                    .append(rS.toUpperCase().substring(0, 1)).append(rS.substring(1)).append("</span>").append("</h2>");
            sb.append("<div align=\"center\" class=\"feedbackcontainer\">");
            sb.append("<div class=\"question\">").append(s.test.get(i).question).append("</div>");
            sb.append("<div>").append(results[i].description).append("</div>");
            sb.append("</div>");
        }


        resultArea.setText(sb.toString());
        resultArea.setContentType("text/html");
    }

    private static float percentage(float part, float whole) {
        return ((float) ((int) ((part / whole) * 10000f))) / 100f;
    }

    public JPanel getContent() {
        return contentArea;
    }
}