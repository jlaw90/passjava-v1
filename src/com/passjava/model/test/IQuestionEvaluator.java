package com.passjava.model.test;

import com.passjava.model.test.choice.ChoiceQuestionEvaluator;
import com.passjava.model.test.compilable.CompilableQuestionEvaluator;

public interface IQuestionEvaluator {
    public static final IQuestionEvaluator ChoiceEvaluator = new ChoiceQuestionEvaluator();
    public static final IQuestionEvaluator CompilableEvaluator = new CompilableQuestionEvaluator();

    Result evaluate(Test test, Question q, Object answer);
}