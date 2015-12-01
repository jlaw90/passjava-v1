package com.passjava.view;

import com.passjava.model.syntaxhighlighter.lexer.TokenType;
import com.passjava.model.syntaxhighlighter.*;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.util.*;

public class CodeEditor {
    public static Map<TokenType, FormattingContext> Settings = new HashMap<>();
    private JEditorPane codeArea;
    private JPanel panel1;
    private JavaDocument doc;

    public CodeEditor() {
        codeArea.setEditorKitForContentType("text/x-java-source", new JavaEditorKit());
        codeArea.setContentType("text/x-java-source");
        codeArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        doc = (JavaDocument) codeArea.getDocument();
    }

    public JPanel getContent() {
        return panel1;
    }

    public void makeEditable(int start, int len) {
        doc.makeEditable(start, len);
    }

    public int getLength() {
        return doc.getLength();
    }

    public void insertString(String s) {
        try {
            doc.insertString(doc.getLength(), s, new SimpleAttributeSet());
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    static {
        // Todo: load settings from a preference file?
        int defStyle = Font.PLAIN;
        Settings.put(TokenType.String, new FormattingContext(defStyle, new Color(0, 128, 0)));
        Settings.put(TokenType.Comment, new FormattingContext(defStyle, new Color(128, 128, 128)));
        Settings.put(TokenType.Numerical, new FormattingContext(defStyle, new Color(0, 0, 255)));
        Settings.put(TokenType.Identifier, new FormattingContext(defStyle, Color.BLACK));
        Settings.put(TokenType.Keyword, new FormattingContext(defStyle | Font.BOLD, new Color(0, 0, 128)));
        Settings.put(TokenType.Operator, new FormattingContext(defStyle, Color.BLACK));
        Settings.put(TokenType.Type, new FormattingContext(defStyle | Font.BOLD, new Color(0, 0, 128)));
    }

    public void lock() {
        doc.lock();
    }


    public void unlock() {
        doc.unlock();
    }

    public void setEditable(boolean b) {
        codeArea.setEditable(b);
    }
}