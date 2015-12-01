package com.passjava.model.session;

import com.passjava.Main;
import com.passjava.model.test.Question;
import com.passjava.model.test.Test;
import com.passjava.model.test.choice.ChoiceQuestion;
import com.passjava.model.util.Downloader;
import com.passjava.view.AnswerAreaProvider;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;

public final class Session {
    public final String sid;
    public final User user;
    public Date finishedDate;
    public Test test;
    public int question;
    public int tick;

    public Session(String sessionId, User u) {
        this.sid = sessionId;
        this.user = u;
    }

    public void setQuestion(int idx) {
        assert(idx >= 0 && idx < test.count);
        question = idx;
    }
}