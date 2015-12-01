package com.passjava.model.test.compilable;

import com.passjava.model.test.IQuestionEvaluator;
import com.passjava.model.test.Question;
import com.passjava.model.test.Test;

public final class CompilableQuestion extends Question {
    public CompilableQuestionSegment[] segments;

    public CompilableQuestion(int id, String q, CompilableQuestionSegment... segments) {
        super(id, q);
        this.segments = segments;
    }

    @Override
    public IQuestionEvaluator getEvaluator() {
        return IQuestionEvaluator.CompilableEvaluator;
    }
}