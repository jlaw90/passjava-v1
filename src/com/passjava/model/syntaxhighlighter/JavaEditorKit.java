package com.passjava.model.syntaxhighlighter;

import javax.swing.*;
import javax.swing.text.*;

public final class JavaEditorKit extends StyledEditorKit {
    private static JavaViewFactory jvf = new JavaViewFactory();

    public String getContentType() {
        return "text/x-java-srouce";
    }

    @Override
    public void install(JEditorPane c) {
        super.install(c);
    }

    @Override
    public void deinstall(JEditorPane c) {
        super.deinstall(c);
    }

    public Document createDefaultDocument() {
        return new JavaDocument();
    }

    public ViewFactory getViewFactory() {
        return jvf;
    }

    static class JavaViewFactory implements ViewFactory {
        public View create(Element elem) {
            return new JavaView(elem);
        }
    }
}