package com.passjava.model.syntaxhighlighter;

import com.passjava.model.syntaxhighlighter.lexer.JavaLexer;
import com.passjava.model.syntaxhighlighter.lexer.Token;
import com.passjava.view.CodeEditor;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public final class JavaView extends PlainView implements DocumentListener, TabExpander {
    private static boolean drawLineNumbers = true;
    private List<Token> tokens = new LinkedList<>();
    private DocumentReader dr;

    public JavaView(Element elem) {
        super(elem);
        elem.getDocument().addDocumentListener(this);
        dr = new DocumentReader(getDocument());
    }

    @Override
    public int viewToModel(float fx, float fy, Shape a, Position.Bias[] bias) {
        if (drawLineNumbers) {
            Font f = getContainer().getFont();
            FontMetrics fm = super.getContainer().getFontMetrics(f);
            fx -= fm.charWidth('m') * 3 + 3;
        }
        return super.viewToModel(fx, fy, a, bias);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public Shape modelToView(int dpos, Shape a, Position.Bias b) throws BadLocationException {
        // line coordinates
        Document doc = getDocument();
        Element map = getElement();
        int lineIndex = map.getElementIndex(dpos);
        if (lineIndex < 0) {
            return lineToRect(a, 0);
        }
        Rectangle lineArea = lineToRect(a, lineIndex);

        // determine span from the start of the line
        Element line = map.getElement(lineIndex);
        int p0 = line.getStartOffset();
        Segment s = new Segment();
        java.util.List<Token> toks = getTokens(p0, dpos);
        int pos = p0;
        int x = 0;
        Font orig = super.getContainer().getFont();
        FontMetrics fm = super.getContainer().getFontMetrics(orig);
        if (drawLineNumbers)
            x += fm.charWidth('m') * 3 + 3;
        for (Token t : toks) {
            if (pos < t.start) {
                fm = super.getContainer().getFontMetrics(orig);
                doc.getText(pos, t.start - pos, s);
                x += Utilities.getTabbedTextWidth(s, fm, x, this, pos);
                pos = t.start;
            }
            FormattingContext fc = CodeEditor.Settings.get(t.type);
            Font f = orig.deriveFont(fc.fontStyle);
            fm = super.getContainer().getFontMetrics(f);
            int start = t.start;
            int len = t.end - t.start;
            if (pos + len > dpos)
                len = dpos - pos;
            if (start < p0)
                start = p0;
            doc.getText(start, len, s);

            x += Utilities.getTabbedTextWidth(s, fm, x, this, pos);
            pos += len;
        }
        // Render any remainder after the tokens...
        if (pos < dpos) {
            fm = super.getContainer().getFontMetrics(orig);
            doc.getText(pos, dpos - pos, s);
            x += Utilities.getTabbedTextWidth(s, fm, x, this, pos);
        }
        s = null;

        // fill in the results and return
        lineArea.x += x;
        lineArea.width = 1;
        lineArea.height = metrics.getHeight();
        return lineArea;
    }

    @Override
    public float nextTabStop(float x, int tabOffset) {
        FontMetrics fm = getContainer().getFontMetrics(getContainer().getFont());
        return x + (fm.charWidth('m') * 4);
    }

    private static void configureAntialiasing(Graphics g) {
        if (!(g instanceof Graphics2D))
            return;
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
    }

    public void paint(Graphics g, Shape a) {
        Rectangle alloc = (Rectangle) a;
        Font orig = super.getContainer().getFont();
        FontMetrics fm = super.getContainer().getFontMetrics(orig);
        int mar = fm.charWidth('m') * 3 + 3;
        JTextComponent host = (JTextComponent) getContainer();
        Highlighter h = host.getHighlighter();
        g.setFont(host.getFont());
        updateMetrics();
        Rectangle clip = g.getClipBounds();
        int fontHeight = metrics.getHeight();
        int heightBelow = (alloc.y + alloc.height) - (clip.y + clip.height);
        int heightAbove = clip.y - alloc.y;
        int linesBelow, linesAbove, linesTotal;

        if (fontHeight > 0) {
            linesBelow = Math.max(0, heightBelow / fontHeight);
            linesAbove = Math.max(0, heightAbove / fontHeight);
            linesTotal = alloc.height / fontHeight;
            if (alloc.height % fontHeight != 0) {
                linesTotal++;
            }
        } else {
            linesBelow = linesAbove = linesTotal = 0;
        }

        Rectangle lineArea = lineToRect(a, linesAbove);
        int y = lineArea.y + metrics.getAscent();
        int x = lineArea.x;
        Element map = getElement();
        int lineCount = map.getElementCount();
        int endLine = Math.min(lineCount, linesTotal - linesBelow);
        lineCount--;
        LayeredHighlighter dh = (h instanceof LayeredHighlighter) ?
                (LayeredHighlighter) h : null;
        for (int line = linesAbove; line < endLine; line++) {
            if (dh != null) {
                Element lineElement = map.getElement(line);
                if (line == lineCount) {
                    dh.paintLayeredHighlights(g, lineElement.getStartOffset(),
                            lineElement.getEndOffset(),
                            a, host, this);
                } else {
                    dh.paintLayeredHighlights(g, lineElement.getStartOffset(),
                            lineElement.getEndOffset() - 1,
                            a, host, this);
                }
            }

            if (drawLineNumbers)
                drawLine(line, g, x + mar, y);
            else
                drawLine(line, g, x, y);
            y += fontHeight;
        }

        if (!drawLineNumbers)
            return;

        // Draw line number margin
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, mar, alloc.height);
        g.setColor(Color.GRAY);
        g.drawLine(mar, 0, mar, alloc.height);
        y = lineArea.y + metrics.getAscent();
        for (int line = linesAbove; line < endLine; line++) {
            String s = String.valueOf(line);
            int w = fm.stringWidth(s);
            int rx = mar - w - 2;
            g.setColor(Color.GRAY);
            g.drawString(s, rx + 1, y + 1);
            g.setColor(Color.WHITE);
            g.drawString(s, rx, y);
            y += fontHeight;
        }
    }

    protected void drawLine(int lineIndex, Graphics g, int x, int y) {
        Element line = getElement().getElement(lineIndex);
        Element elem;

        try {
            if (line.isLeaf()) {
                drawElement(lineIndex, line, g, x, y);
            } else {
                // this line contains the composed text.
                int count = line.getElementCount();
                for (int i = 0; i < count; i++) {
                    elem = line.getElement(i);
                    x = drawElement(lineIndex, elem, g, x, y);
                }
            }
        } catch (BadLocationException e) {
            throw new Error("Can't render line: " + lineIndex);
        }
    }

    private int drawElement(int lineIndex, Element elem, Graphics g, int x, int y) throws BadLocationException {
        int p0 = elem.getStartOffset();
        int p1 = elem.getEndOffset();
        p1 = Math.min(getDocument().getLength(), p1);

        JTextComponent host = (JTextComponent) getContainer();
        Highlighter h = host.getHighlighter();
        g.setFont(host.getFont());
        int sel0 = host.getSelectionStart();
        int sel1 = host.getSelectionEnd();
        Color unselected = (host.isEnabled()) ?
                host.getForeground() : host.getDisabledTextColor();
        Caret c = host.getCaret();
        Color selected = c.isSelectionVisible() && h != null ?
                host.getSelectedTextColor() : unselected;

        if (sel0 == sel1 || selected == unselected) {
            // no selection, or it is invisible
            x = drawUnselectedText(g, x, y, p0, p1);
        } else if ((p0 >= sel0 && p0 <= sel1) && (p1 >= sel0 && p1 <= sel1)) {
            x = drawSelectedText(g, x, y, p0, p1);
        } else if (sel0 >= p0 && sel0 <= p1) {
            if (sel1 >= p0 && sel1 <= p1) {
                x = drawUnselectedText(g, x, y, p0, sel0);
                x = drawSelectedText(g, x, y, sel0, sel1);
                x = drawUnselectedText(g, x, y, sel1, p1);
            } else {
                x = drawUnselectedText(g, x, y, p0, sel0);
                x = drawSelectedText(g, x, y, sel0, p1);
            }
        } else if (sel1 >= p0 && sel1 <= p1) {
            x = drawSelectedText(g, x, y, p0, sel1);
            x = drawUnselectedText(g, x, y, sel1, p1);
        } else {
            x = drawUnselectedText(g, x, y, p0, p1);
        }
        return x;
    }

    protected int drawUnselectedText(Graphics g, int x, int y, int p0, int p1) throws BadLocationException {
        configureAntialiasing(g);
        Color orig = (getContainer().isEnabled()) ?
                getContainer().getForeground() : ((JTextComponent) getContainer()).getDisabledTextColor();
        Font origF = g.getFont();
        JavaDocument doc = (JavaDocument) getDocument();
        Segment s = new Segment();
        java.util.List<Token> toks = getTokens(p0, p1);
        int x1 = x;
        int pos = p0;

        // draw highlighter for locked regions...
        List<DocumentRegion> regions = doc.getEditableRegions();

        FontMetrics fm = g.getFontMetrics(origF);
        int height = fm.getHeight();
        int asc = fm.getAscent();

        g.setColor(new Color(255, 255, 0, 0x7f));
        for(DocumentRegion dr: regions) {
            if((dr.start >= p0 && dr.start < p1) || (dr.end >= p0 && dr.end < p1)) {
                int x2 = 0;
                if(dr.start >= p0)
                    x2 = getWidth(p0, dr.start, doc);
                int w = getWidth(Math.max(p0, dr.start), Math.min(p1, dr.end), doc);
                g.fillRect(x+x2, y-asc, w, height);
            }
        }

        for (Token t : toks) {
            if (pos < t.start) {
                g.setColor(orig);
                g.setFont(origF);
                // render string before the token
                doc.getText(pos, t.start - pos, s);
                x1 = Utilities.drawTabbedText(s, x1, y, g, this, pos);
                pos = t.start;
            }
            FormattingContext fc = CodeEditor.Settings.get(t.type);
            g.setColor(fc.color);
            g.setFont(origF.deriveFont(fc.fontStyle));
            int start = t.start;
            if (start < p0)
                start = p0;
            int len = t.end - start;
            if (pos + len > p1)
                len = p1 - pos;
            doc.getText(start, len, s);

            x1 = Utilities.drawTabbedText(s, x1, y, g, this, pos);
            pos += len;
        }
        // Render any remainder after the tokens...
        if (pos < p1) {
            g.setColor(orig);
            g.setFont(origF);
            doc.getText(pos, p1 - pos, s);
            x1 = Utilities.drawTabbedText(s, x1, y, g, this, pos);
        }
        s = null;
        return x1;
    }

    protected int drawSelectedText(Graphics g, int x, int y, int p0, int p1) throws BadLocationException {
        configureAntialiasing(g);
        JTextComponent host = (JTextComponent) getContainer();
        Color orig = (host.isEnabled()) ? host.getForeground() : host.getDisabledTextColor();
        Highlighter h = host.getHighlighter();
        Caret c = host.getCaret();
        Color selected = c.isSelectionVisible() && h != null ? host.getSelectedTextColor() : orig;
        Font origF = g.getFont();
        JavaDocument doc = (JavaDocument) getDocument();
        Segment s = new Segment();
        java.util.List<Token> toks = getTokens(p0, p1);
        int x1 = x;
        int pos = p0;
        g.setColor(selected);
        for (Token t : toks) {
            if (pos < t.start) {
                g.setFont(origF);
                doc.getText(pos, t.start - pos, s);
                x1 = Utilities.drawTabbedText(s, x1, y, g, this, pos);
                pos = t.start;
            }
            FormattingContext fc = CodeEditor.Settings.get(t.type);
            g.setFont(origF.deriveFont(fc.fontStyle));
            int start = t.start;
            if (start < p0)
                start = p0;
            int len = t.end - start;
            if (pos + len > p1)
                len = p1 - pos;
            doc.getText(start, len, s);

            x1 = Utilities.drawTabbedText(s, x1, y, g, this, pos);
            pos += len;
        }
        // Render any remainder after the tokens...
        if (pos < p1) {
            g.setFont(origF);
            doc.getText(pos, p1 - pos, s);
            x1 = Utilities.drawTabbedText(s, x1, y, g, this, pos);
        }
        s = null;
        return x1;
    }

    private List<Token> getTokens(int start, int end) {
        List<Token> toks = new ArrayList<>();
        for (Token t : tokens)
            if (t.end >= start || t.start >= start) {
                if (t.start >= end)
                    break;
                toks.add(t);
            }
        return toks;
    }

    private void reparse() {
        Token t;
        tokens.clear();
        try {
            dr.reset(); // Back to beginning of doc
            JavaLexer jl = new JavaLexer(dr);
            while ((t = jl.nextToken()) != null)
                tokens.add(t);
        } catch (IOException e) {
            throw new Error("Problem reading document.");
        }
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        reparse();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        reparse();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        reparse();
    }

    @Override
    protected void updateDamage(javax.swing.event.DocumentEvent changes, Shape a, ViewFactory f) {
        // Calculate biggest line
        Element elem = getElement();
        DocumentEvent.ElementChange ec = changes.getChange(elem);

        Component host = getContainer();
        Element[] added = (ec != null) ? ec.getChildrenAdded() : null;
        Element[] removed = (ec != null) ? ec.getChildrenRemoved() : null;
        if (((added != null) && (added.length > 0)) ||
                ((removed != null) && (removed.length > 0))) {
            // lines were added or removed...
            if (added != null) {
                int currWide = getLineWidth(longLine);
                for (int i = 0; i < added.length; i++) {
                    int w = getLineWidth(added[i]);
                    if (w > currWide) {
                        currWide = w;
                        longLine = added[i];
                    }
                }
            }
            if (removed != null) {
                for (int i = 0; i < removed.length; i++) {
                    if (removed[i] == longLine) {
                        calculateLongestLine();
                        break;
                    }
                }
            }
            preferenceChanged(null, true, true);
        } else {
            Element map = getElement();
            int line = map.getElementIndex(changes.getOffset());
            damageLineRange(line, line, a, host);
            if (changes.getType() == DocumentEvent.EventType.INSERT) {
                // check to see if the line is longer than current
                // longest line.
                int w = getLineWidth(longLine);
                Element e = map.getElement(line);
                if (e == longLine) {
                    preferenceChanged(null, true, false);
                } else if (getLineWidth(e) > w) {
                    longLine = e;
                    preferenceChanged(null, true, false);
                }
            } else if (changes.getType() == DocumentEvent.EventType.REMOVE) {
                if (map.getElement(line) == longLine) {
                    // removed from longest line... recalc
                    calculateLongestLine();
                    preferenceChanged(null, true, false);
                }
            }
        }
        host.repaint();
    }

    private void calculateLongestLine() {
        Component c = getContainer();
        Font font = c.getFont();
        metrics = c.getFontMetrics(font);
        Element lines = getElement();
        int n = lines.getElementCount();
        int maxWidth = -1;
        for (int i = 0; i < n; i++) {
            Element line = lines.getElement(i);
            int w = getLineWidth(line);
            if (w > maxWidth) {
                maxWidth = w;
                longLine = line;
            }
        }
    }

    @Override
    protected Rectangle lineToRect(Shape a, int line) {
        return super.lineToRect(a, line);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public float getPreferredSpan(int axis) {
        updateMetrics();
        switch (axis) {
            case View.X_AXIS:
                return getLineWidth(longLine);
            case View.Y_AXIS:
                return getElement().getElementCount() * metrics.getHeight();
            default:
                throw new IllegalArgumentException("Invalid axis: " + axis);
        }
    }

    private int getWidth(int p0, int p1, Document doc) {
        try {
            java.util.List<Token> toks = getTokens(p0, p1);
            int x1 = 0;
            int pos = p0;
            Font orig = getContainer().getFont();
            FontMetrics fm;
            Segment s = new Segment();
            for (Token t : toks) {
                if (pos < t.start) {
                    fm = getContainer().getFontMetrics(orig);
                    doc.getText(pos, t.start - pos, s);
                    x1 += Utilities.getTabbedTextWidth(s, fm, 0, this, pos);
                    pos = t.start;
                }
                FormattingContext fc = CodeEditor.Settings.get(t.type);
                Font f = orig.deriveFont(fc.fontStyle);
                fm = getContainer().getFontMetrics(f);
                int start = t.start;
                if (start < p0)
                    start = p0;
                int len = t.end - start;
                if (pos + len > p1)
                    len = p1 - pos;
                doc.getText(start, len, s);
                x1 += Utilities.getTabbedTextWidth(s, fm, 0, this, pos);
                pos += len;
            }
            // Render any remainder after the tokens...
            if (pos < p1) {
                fm = getContainer().getFontMetrics(orig);
                doc.getText(pos, p1 - pos, s);
                x1 += Utilities.getTabbedTextWidth(s, fm, 0, this, pos);
            }
            s = null;
            return x1;
        } catch (BadLocationException e) {
            return 0;
        }
    }

    private int getLineWidth(Element line) {
        int width = 0;
        if(drawLineNumbers)
            width = getContainer().getFontMetrics(getContainer().getFont()).charWidth('m')*3 + 3;
        if(line == null)
            return width;
        return width + getWidth(line.getStartOffset(), line.getEndOffset(), line.getDocument());
    }

    private Element longLine;
}