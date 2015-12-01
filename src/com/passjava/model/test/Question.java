package com.passjava.model.test;

public abstract class Question {
    public final String question;
    public final int id;

    protected Question(int id, String q) {
        this.id = id;
        this.question = q;
    }

    public abstract IQuestionEvaluator getEvaluator();
}