public final class CompilableQuestionSegment {
    private String data;
    private boolean visible;
    private boolean editable;

    public CompilableQuestionSegment(String data, boolean  visible, boolean editable) {
        this.data = data;
        this.visible = visible;
        this.editable = editable;
    }

    public String getData() {
        return data;
    }

    public boolean isVisible() {
        return visible;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setData(String data) {
        this.data = data;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public boolean equals(Object o) {
        if(!(o instanceof CompilableQuestionSegment))
            return false;
        CompilableQuestionSegment cqs = (CompilableQuestionSegment) o;
        return cqs.editable == editable && cqs.visible == visible && cqs.data.equals(data);
    }
}