package com.passjava.model.syntaxhighlighter;

import com.passjava.model.syntaxhighlighter.lexer.JavaLexer;
import com.passjava.model.syntaxhighlighter.lexer.Token;
import com.passjava.view.CodeEditor;

import java.awt.*;
import java.io.IOException;
import java.io.StringReader;

public final class HTMLHighlighter {
    public static String highlight(String source) {
        source = source.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");

        JavaLexer jl = new JavaLexer(new StringReader(source));

        StringBuilder sb = new StringBuilder();
        int pos = 0;
        Token t;
        try {
            // Firstly, reset view settings
            String color = "#000000";
            boolean bold = false;
            boolean italic = false;

            boolean inFont = false;

            while ((t = jl.nextToken()) != null) {
                FormattingContext fc = CodeEditor.Settings.get(t.type);
                // change style!
                int fs = fc.fontStyle;
                boolean nB = (fs & Font.BOLD) != 0;
                boolean nI = (fs & Font.ITALIC) != 0;

                String col = Integer.toHexString(fc.color.getRGB() & 0xffffff);
                while (col.length() < 6)
                    col = "0" + col;
                col = "#" + col;

                boolean colChange = !color.equals(col);
                boolean boldChange = nB != bold;
                boolean italicChange = nI != italic;
                boolean anyChange = colChange || boldChange || italicChange;

                // Render anything that appears BEFORE this token!
                if (t.start > pos)
                    sb.append(source.substring(pos, t.start));

                pos = t.start;

                if (anyChange) {
                    if (boldChange && bold)
                        sb.append("</b>");
                    if (italicChange && italic)
                        sb.append("</i>");
                    if (colChange) {
                        if (inFont)
                            sb.append("</font>");
                        sb.append("<font color=\"").append(col).append("\">");
                        inFont = true;
                    }
                    if(boldChange && !bold)
                        sb.append("<b>");
                    if(italicChange && !italic)
                        sb.append("<i>");
                }

                color = col;
                bold = nB;
                italic = nI;

                // Append this token
                sb.append(source.substring(t.start, t.end));
                pos += t.end - t.start;
            }


            if(bold)
                sb.append("</b>");
            if(italic)
                sb.append("</i>");
            if (inFont)
                sb.append("</font>");

            // Anything missing from end?
            if (pos != source.length())
                sb.append(source.substring(pos, source.length()));

            return sb.toString().replace("\r\n", "\n").replace("\n", "<br/>\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return source;
    }
}