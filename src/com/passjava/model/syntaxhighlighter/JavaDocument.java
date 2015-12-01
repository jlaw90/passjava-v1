package com.passjava.model.syntaxhighlighter;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.PlainDocument;
import java.util.ArrayList;
import java.util.List;

public final class JavaDocument extends PlainDocument {
    private List<DocumentRegion> editable = new ArrayList<>();
    private boolean locked;

    public JavaDocument() {
        super();
    }

    public void makeEditable(int start, int len) {
        editable.add(new DocumentRegion(start, start + len));
    }

    public void lock() {
        this.locked = true;
    }

    public void unlock() {
        this.locked = false;
    }

    public boolean isLocked() {
        return locked;
    }

    public List<DocumentRegion> getEditableRegions() {
        return editable;
    }

    @Override
    public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
        if(!this.locked) {
            super.insertString(offs, str, a);
            return;
        }
        boolean locked = true;

        // Find the Element surrounding the offset
        for (DocumentRegion d : editable) {
            if (offs >= d.start && offs <= d.end) {
                locked = false;
                break;
            }
        }

        if (!locked) {
            // adjust editable positions...
            int len = str.length();
            for (DocumentRegion d : editable) {
                if(offs < d.start) {
                    d.start += len;
                    d.end += len;
                }
                if (offs >= d.start && offs <= d.end) {
                    d.end += len;
                }
            }
            super.insertString(offs, str, a);
        }
    }

    public void remove(int offs, int len) throws BadLocationException {
        if (!this.locked) {
            super.remove(offs, len);
            return;
        }
        boolean locked = true;
        for (DocumentRegion d : editable) {
            if (offs >= d.start && offs + len <= d.end) {
                locked = false;
                break;
            }
        }
        if (!locked) {
            for (DocumentRegion d : editable) {
                if(offs < d.start) {
                    d.start -= len;
                    d.end -= len;
                }
                if (offs >= d.start && offs <= d.end) {
                    d.end -= len;
                }
            }
            super.remove(offs, len);
        }
    }

    private Element getParagraph(int globalOffset) {
        Element root = getDefaultRootElement();
        for (int i = 0; i < root.getElementCount(); i++) {
            Element e = root.getElement(i);
            if (globalOffset >= e.getStartOffset() && globalOffset < e.getEndOffset()) {
                return e;
            }
        }
        return null;
    }
}