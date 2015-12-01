package com.passjava.model.test.choice;

import com.passjava.model.test.IQuestionEvaluator;
import com.passjava.model.test.Question;
import com.passjava.model.test.Result;
import com.passjava.model.test.Test;

public final class ChoiceQuestionEvaluator implements IQuestionEvaluator {
    @Override
    public Result evaluate(Test test, Question q, Object ans) {
        ChoiceQuestion cq = (ChoiceQuestion) q;
        boolean[] answers = (boolean[]) ans;

        boolean nothingSelected = true;

        // append correct answers
        int corCount = 0;
        for(int i = 0; i < cq.options.length; i++) {
            ChoiceQuestionOption cqo = cq.options[i];
            if(cqo.correct) {
                corCount++;
            }
            if(answers[i])
                nothingSelected = false;
        }

        StringBuilder sb = new StringBuilder("The correct answer").append(corCount == 1? " was ": "s were ");

        int curCor = 0;
        for(int i = 0; i < cq.options.length; i++) {
            ChoiceQuestionOption cqo = cq.options[i];
            if(cqo.correct) {
                if(curCor > 0) {
                    sb.append(curCor == corCount - 1? " and ": ", ");
                }
                sb.append(cqo.option);
                curCor++;
            }
        }
        sb.append("<br/>").append(cq.correctText).append("<br/>");

        boolean pass = true;
        for (int i = 0; i < cq.options.length; i++) {
            ChoiceQuestionOption cqo = cq.options[i];
            boolean correct = cqo.correct;
            boolean answer = nothingSelected? !correct: answers[i];
            if (answer != correct) {
                pass = false;
                if(!correct)
                    sb.append("<br/><b>").append(i+1).append("</b>. ").append(cqo.wrongText);
            }
        }
        if (pass)
            return new Result(true, cq.correctText);
        return new Result(false, sb.toString());
    }
}