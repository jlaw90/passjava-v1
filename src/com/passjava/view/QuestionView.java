package com.passjava.view;

import com.passjava.model.test.Question;

import javax.swing.*;

public class QuestionView {
    private JPanel contentPanel;
    private JEditorPane question;
    private JPanel answerContainer;
    private JLabel answerHelp;

    public JPanel getContent() {
        return contentPanel;
    }

    public JPanel getAnswerContainer() {
        return answerContainer;
    }

    public void setHintText(String s) {
        answerHelp.setText(s);
    }

    public void setQuestion(Question q, boolean readonly) {
        answerContainer.removeAll();
        this.question.setText(q.question);
        this.question.revalidate();
        AnswerAreaProvider.provide(q,  this, readonly);
    }
}