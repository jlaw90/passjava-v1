package com.passjava.model.test.choice;

import com.passjava.model.test.IQuestionEvaluator;
import com.passjava.model.test.Question;
import com.passjava.model.test.Test;

public final class ChoiceQuestion extends Question {
    public ChoiceQuestionOption[] options;
    public String correctText;

    public ChoiceQuestion(int id, String question, String correctText, ChoiceQuestionOption... options) {
        super(id, question);
        this.options = options;
        this.correctText = correctText;
    }

    @Override
    public IQuestionEvaluator getEvaluator() {
        return IQuestionEvaluator.ChoiceEvaluator;
    }
}