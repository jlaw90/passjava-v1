package com.passjava.model.test.compilable;

public final class CompilableQuestionSegment {
    public boolean visible;
    public boolean editable;
    public String data;
    public String userData;

    public CompilableQuestionSegment(String text, boolean visible, boolean editable, String userData) {
        this.data = text;
        this.visible = visible;
        this.editable = editable;
        this.userData = userData;
    }
}