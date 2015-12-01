package com.passjava.model.syntaxhighlighter;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Segment;
import java.io.IOException;
import java.io.Reader;

public final class DocumentReader extends Reader {
    private int index = 0;
    private int pos = 0;
    private Segment segment;
    private Document doc;

    public DocumentReader(Document d) {
        this.segment = new Segment();
        this.doc = d;
        try {
            loadSegment();
        } catch (IOException ioe) {
            throw new Error("unexpected: " + ioe);
        }
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        for (int i = off; i < len; i++) {
            if (index >= segment.offset + segment.count) {
                if (pos >= doc.getLength())
                    return i == off ? -1 : i - off;
                loadSegment();
            }
            cbuf[i] = segment.array[index++];
        }
        return len;
    }

    @Override
    public void close() throws IOException {
        segment = null;
        doc = null;
    }

    private void loadSegment() throws IOException {
        try {
            int n = Math.min(1024, doc.getLength() - pos);
            doc.getText(pos, n, segment);
            pos += n;
            index = segment.offset;
        } catch (BadLocationException e) {
            throw new IOException("Bad location");
        }
    }

    @Override
    public void reset() throws IOException {
        pos = 0;
        loadSegment();
    }
}