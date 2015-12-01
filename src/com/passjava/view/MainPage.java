package com.passjava.view;

import com.passjava.Main;
import com.passjava.model.session.Session;
import com.passjava.model.test.Question;
import com.passjava.model.test.Result;
import com.passjava.model.test.Test;
import com.passjava.model.test.choice.ChoiceQuestion;
import com.passjava.model.test.compilable.CompilableQuestion;
import com.passjava.model.util.Base64Codec;
import com.passjava.model.util.Downloader;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Hashtable;

public class MainPage {
    private static String progressUrl = Main.base + "progress.php";

    private HJLayeredPane contentArea;
    private JLabel progressText;
    private JPanel mainPanel;
    private JButton nextButton;
    private JButton pauseButton;
    private JButton previousButton;
    private JSlider progressSlider;
    private JPanel progressContainer;

    boolean started = false;
    boolean finished = false;
    boolean paused = true;
    private HtmlPage homeContent;
    private PausePage pausePage = new PausePage();
    private LoadingPage loadingPage = new LoadingPage();

    private boolean readonly;
    private Session session;
    private Test test;
    private int visiblePage = 0;
    private QuestionView[] view;

    public MainPage() {
        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                nextPage();
            }
        });
        previousButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                prevPage();
            }
        });
        pauseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pause();
            }
        });
        progressSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (!started || paused || finished)
                    return;
                updateCompletionStatus(session.question);
                int curPage = session.question;
                int n = progressSlider.getValue();
                if (n == curPage)
                    return;
                setQuestion(n);
                loadPage(session.question, n < curPage? 2: 3);
                configureUI();
            }
        });
        homeContent = new HtmlPage();
        homeContent.loadUrl(Main.base);
        contentArea.add(homeContent.getContent());
    }

    public void setSession(Session s, boolean readonly) {
        this.readonly = readonly;
        this.session = s;
        this.test = s.test;
        this.view = new QuestionView[s.test.count];
        Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
        for (int i = 0; i < s.test.count; i++) {
            // Add label to progress slider
            JLabel label = new JLabel(String.valueOf(i + 1));
            label.setForeground(new Color(240, 240, 240));
            label.setFont(label.getFont().deriveFont(8f));
            labelTable.put(i, label);

            // Load QuestionView
            view[i] = new QuestionView();
            view[i].setQuestion(test.get(i), readonly);
        }
        progressSlider.setLabelTable(labelTable);
        progressSlider.setMaximum(s.test.count - 1);
        progressSlider.setMinimum(0);
        progressContainer.setVisible(false);
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    private void prevPage() {
        if (!loadPage(session.question - 1, 2))
            return;
        updateCompletionStatus(session.question);
        setQuestion(session.question - 1);
        configureUI();
    }

    private void nextPage() {
        if (paused) {
            start();
            if (visiblePage != session.question)
                loadPage(session.question, 4);
            return;
        }

        if (!loadPage(session.question + 1, 3))
            return;
        updateCompletionStatus(session.question);
        setQuestion(session.question + 1);
        configureUI();
    }

    private void setQuestion(int qidx) {
        updateProgress(qidx);
        session.setQuestion(qidx);
    }

    private void updateProgress(int qidx) {
        if(readonly)
            return;
        // Update progress on server...
        try {
            // Generate last question data...
            Question q = test.get(session.question);
            StringBuilder reqData = new StringBuilder().append(q.id).append(':');
            if (q instanceof ChoiceQuestion) {
                ChoiceQuestion cq = (ChoiceQuestion) q;
                boolean[] checked = (boolean[]) AnswerAreaProvider.getProgressUpdateData(q, view[session.question].getAnswerContainer());
                for (int i = 0; i < cq.options.length; i++)
                    reqData.append(i == 0 ? "" : ":").append(checked[i] ? '1' : '0');
            } else if (q instanceof CompilableQuestion) {
                String[] areas = (String[]) AnswerAreaProvider.getProgressUpdateData(q, view[session.question].getAnswerContainer());
                for (int i = 0; i < areas.length; i++)
                    reqData.append(i == 0 ? "" : ":").append(Base64Codec.encode(areas[i]));
            }

            String progUrl = progressUrl + "?sid=" + URLEncoder.encode(session.sid, "UTF-8") + "&ticks=" + session.tick +
                    "&q=" + qidx + "&qd=" + URLEncoder.encode(Base64Codec.encode(reqData.toString()), "UTF-8");
            Downloader.download(progUrl, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    switch(e.getID()) {
                        case Downloader.Complete:
                            System.out.println(new String(((Downloader) e.getSource()).getData()));
                            break;
                    }
                }
            });
        } catch (UnsupportedEncodingException e) {
            // encoding won't fail...
        }
    }

    private void start() {
        paused = false;
        if (started) {
            configureUI();
            return;
        }
        started = true;
        loadPage(session.question, 4);
        for (int i = 0; i < session.question; i++)
            updateCompletionStatus(i);
        configureUI();
        Timer timeIncrementer = new Timer(500, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!readonly && !paused)
                    session.tick++;
            }
        });
        timeIncrementer.setInitialDelay(0);
        timeIncrementer.start();
    }

    private void pause() {
        paused = true;
        nextButton.setEnabled(true);
        previousButton.setVisible(false);
        pauseButton.setVisible(false);
        progressContainer.setVisible(false);
        loadPage(-2, 5);
        configureUI();
    }

    private void updateCompletionStatus(int qIdx) {
        boolean finished = AnswerAreaProvider.completed(test.get(qIdx), view[qIdx].getAnswerContainer());
        JLabel jl = (JLabel) progressSlider.getLabelTable().get(qIdx);
        if (!finished) {
            jl.setForeground(Color.RED);
        } else {
            jl.setForeground(new Color(240, 240, 240));
        }
    }

    public boolean loadPage(int index, int transition) {
        visiblePage = index;

        // Results...
        if (index >= test.count) {
            if (!finished) {
                showResults();
                return false;
            }
            int q = index - test.count;
            return true;
        }

        if (paused || finished) {
            transitionContent(index == -1 ? homeContent.getContent() : index == -2 ? pausePage.getContent() : null, transition);
            return true;
        }

        transitionContent(view[index].getContent(), transition);
        return true;
    }

    private void showResults() {
        if(!readonly) {
            if(JOptionPane.showConfirmDialog(mainPanel,
                    "You will not be able to modify your answers if you continue\n" +
                    "Are you sure you wish to proceed?",
                    "Confirmation dialog",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.NO_OPTION) {
                return;
            }
        }

        session.finishedDate = new Date();
        finished = true;
        updateProgress(session.question);
        configureUI();
        paused = true;

        transitionContent(loadingPage.getContent(), 0);
        Thread t = new Thread() {
            public void run() {
                loadingPage.setText("Aggregating questions...");
                Result[] results = new Result[test.count];
                Object[] data = new Object[results.length];
                for (int i = 0; i < results.length; i++) {
                    loadingPage.setText("Evaluating question " + (i + 1));
                    Question q = test.get(i);
                    data[i] = AnswerAreaProvider.provideAnswer(q, view[i].getAnswerContainer());
                    results[i] = q.getEvaluator().evaluate(test, q, data[i]);
                }
                loadingPage.setText("Evaluation complete");
                progressText.setText("Results");

                ResultsPage rp = new ResultsPage();
                rp.init(session, results);
                transitionContent(rp.getContent(), 0);
            }
        };
        t.setDaemon(true);
        t.start();
    }

    private void configureUI() {
        if (session.question == 0)
            previousButton.setEnabled(false);
        else
            previousButton.setEnabled(true);

        if (paused) {
            progressContainer.setVisible(false);
            pauseButton.setVisible(false);
            previousButton.setVisible(false);
            progressText.setText("Paused");
            nextButton.setText("Continue");
            nextButton.setEnabled(true);
        } else if(finished) {
            progressContainer.setVisible(false);
            nextButton.setEnabled(false);
            pauseButton.setEnabled(false);
            previousButton.setEnabled(false);
            progressSlider.setEnabled(false);
            progressText.setText("Evaluating");
        } else {
            pauseButton.setVisible(true);
            previousButton.setVisible(true);
            progressSlider.setValue(session.question);
            progressSlider.revalidate();
            progressSlider.repaint();
            progressContainer.setVisible(true);
            progressText.setText("Question " + (session.question + 1) + " of " + test.count);
            if (session.question < test.count - 1) {
                nextButton.setText("Next");
                nextButton.setEnabled(true);
            } else if (session.question == test.count - 1) {
                nextButton.setText("View Results");
                nextButton.setEnabled(true);
            }
        }
    }

    private void transitionContent(JPanel p, int transition) {
        SmoothPageTransitioner.stopTransition(contentArea);
        SmoothPageTransitioner.transition(contentArea, contentArea.getComponentCount() == 0 ? null :
                (JPanel) contentArea.getComponent(0), p, -1, transition);
    }
}