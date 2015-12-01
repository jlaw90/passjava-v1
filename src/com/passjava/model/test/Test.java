package com.passjava.model.test;

import com.passjava.Main;
import com.passjava.model.syntaxhighlighter.HTMLHighlighter;
import com.passjava.model.test.choice.ChoiceQuestion;
import com.passjava.model.test.choice.ChoiceQuestionOption;
import com.passjava.model.test.compilable.CompilableQuestion;
import com.passjava.model.test.compilable.CompilableQuestionSegment;
import com.passjava.model.util.Downloader;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Test {
    private static final String url = Main.base + "test.php?sid=";

    public final int id;
    public final List<Question> questions;
    public final int count;
    private String name;

    public Test(int id, String sess) {
        this.id = id;
        this.questions = Collections.unmodifiableList(load(sess));
        this.count = questions.size();
    }

    public String getName() {
        return name;
    }

    public Question get(int index) {
        return questions.get(index);
    }

    private List<Question> load(String sess) {
        // Load the test
        Downloader d = null;
        try {
            d = Downloader.download(url + URLEncoder.encode(sess, "UTF-8"), null);
        } catch (UnsupportedEncodingException e) {
            // Shouldn't happen
        }
        d.join();
        byte[] bdata = d.getData();
        if (bdata == null)
            throw new RuntimeException("failed to download test");
        String data = new String(bdata);
        if (data.startsWith("ERROR"))
            throw new RuntimeException("failed to load test with specified ID");

        String[] parts = data.split(Character.toString((char) 0x00));

        this.name = preprocess(parts[0]);
        int numQ = Integer.parseInt(parts[1]);

        List<Question> questions = new ArrayList<>(numQ);

        int off = 2;
        for (int i = 0; i < numQ; i++) {
            int qId = Integer.parseInt(parts[off++]);
            String qtype = parts[off++];
            String qq = "<html><body align=center>" + preprocess(parts[off++]);

            if (qtype.equals("singlechoice") || qtype.equals("multichoice")) {
                String correctMessage = "<html>" + preprocess(parts[off++]);
                int opts = Integer.parseInt(parts[off++]);

                ChoiceQuestionOption[] options = new ChoiceQuestionOption[opts];

                for (int j = 0; j < opts; j++) {
                    boolean correct = Boolean.parseBoolean(parts[off + 1]);
                    options[j] = new ChoiceQuestionOption("<html>" + preprocess(parts[off]), correct?null: "<html>" +
                            preprocess(parts[off+3]), correct, Boolean.parseBoolean(parts[off+2]));
                    off+= 3;
                    if(!correct)
                        off++;
                }

                questions.add(new ChoiceQuestion(qId, qq, correctMessage, options));
            } else if (qtype.equals("compilable")) {
                int segCount = Integer.parseInt(parts[off++]);
                CompilableQuestionSegment[] segments = new CompilableQuestionSegment[segCount];

                for(int j = 0; j < segCount; j++) {
                    String segData = parts[off++];
                    boolean vis = Boolean.parseBoolean(parts[off++]);
                    boolean edi = Boolean.parseBoolean(parts[off++]);
                    segments[j] = new CompilableQuestionSegment(segData, vis, edi, edi? parts[off++]: null);
                }
                questions.add(new CompilableQuestion(qId, qq, segments));
            }
        }
        return questions;
    }

    private static String preprocess(String s) {
        // Process any <java> tags...

        int start = 0;
        while ((start = s.indexOf("<java>", start + 1)) != -1) {
            int end = s.indexOf("</java>", start);
            if(end == -1) {
                continue;
            }
            String source = s.substring(start + 6, end);
            s = s.substring(0, start) + HTMLHighlighter.highlight(source) + s.substring(end + 7);
        }
        return s;
    }
}