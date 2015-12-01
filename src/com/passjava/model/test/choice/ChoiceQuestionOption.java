package com.passjava.model.test.choice;

public final class ChoiceQuestionOption {
    public String option;
    public String wrongText;
    public boolean correct;
    public boolean checked;

    public ChoiceQuestionOption(String option, String wrongText, boolean correct, boolean checked) {
        this.correct = correct;
        this.option = option;
        this.wrongText = wrongText;
        this.checked = checked;
    }

    public String toString() {
        return option;
    }
}