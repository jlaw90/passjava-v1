package com.passjava.model.syntaxhighlighter;

import java.awt.*;

public final class FormattingContext {
    public int fontStyle;
    public Color color;

    public FormattingContext(int style, Color c) {
        this.fontStyle = style;
        this.color = c;
    }
}