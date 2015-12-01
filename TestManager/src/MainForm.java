import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

public class MainForm {
    private JPanel panel1;
    private JSpinner testId;
    private JButton createTestButton;
    private JTextField testName;
    private JList questionList;
    private JSpinner numQuestions;
    private JButton saveButton;
    private JButton addButton;
    private JButton deleteButton;
    private JPanel questionArea;

    private Test current;
    private List<Test> origTests;
    private List<Test> tests;

    public MainForm() {
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CreateQuestionDialog cqd = new CreateQuestionDialog();
                cqd.pack();
                cqd.setLocationRelativeTo(panel1);
                cqd.setVisible(true);
                Question q = cqd.getCreation();
                if (q == null)
                    return;
                DefaultListModel model = (DefaultListModel) questionList.getModel();
                model.addElement(q);
                current.addQuestion(q);
            }
        });
        questionList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                Question q = (Question) questionList.getSelectedValue();

                if (q == null)
                    setQuestionArea(null);

                if (q instanceof ChoiceQuestion) {
                    ChoiceQuestionForm cqf = new ChoiceQuestionForm();
                    cqf.init((ChoiceQuestion) q);
                    setQuestionArea(cqf.getContent());
                }
                if(q instanceof CompilableQuestion) {
                    CompilableQuestionForm cqf = new CompilableQuestionForm();
                    cqf.init((CompilableQuestion) q);
                    setQuestionArea(cqf.getContent());
                }
            }
        });
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                save();
            }
        });
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultListModel model = (DefaultListModel) questionList.getModel();
                if (questionList.getSelectedValue() == null)
                    return;
                current.removeQuestion((Question) questionList.getSelectedValue());
                model.removeElement(questionList.getSelectedValue());
            }
        });
    }

    private void setQuestionArea(JPanel p) {
        questionArea.removeAll();
        if (p != null)
            questionArea.add(p);
        questionArea.revalidate();
        questionArea.repaint();
    }

    private void setTest(int id) {
        current = null;
        Test test = null;
        for (Test t : tests)
            if (t.id == id) {
                test = t;
                break;
            }
        testName.setText(test.getName());
        numQuestions.setValue(test.getQuestionCount());
        List<Question> questions = test.getQuestions();
        DefaultListModel<Question> model = new DefaultListModel<>();
        for (int i = 0; i < questions.size(); i++)
            model.add(i, questions.get(i));
        questionList.setModel(model);
        questionList.setSelectedValue(null, false);
        current = test;
    }

    private void createUIComponents() {
        testName = new JTextField();
        testName.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if (current != null)
                    current.setName(testName.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                insertUpdate(e);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                insertUpdate(e);
            }
        });
        numQuestions = new JSpinner(new SpinnerNumberModel(0, 0, 5000, 1));
        numQuestions.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (current != null)
                    current.setQuestions((Integer) numQuestions.getValue());
            }
        });
        testId = new JSpinner();
        questionList = new JList();
        testId.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                setTest((Integer) testId.getValue());
            }
        });

        loadTests();
        setTest((Integer) testId.getValue());
    }

    private List<Test> getAddedTests() {
        List<Test> diff = new ArrayList<Test>();
        diff.addAll(tests);
        diff.removeAll(origTests);
        return diff;
    }

    private List<Test> getDeletedTests() {
        List<Test> diff = new ArrayList<Test>();
        diff.addAll(origTests);
        diff.removeAll(tests);
        return diff;
    }

    private void save() {
        // Any new tests, ad to db
        for (final Test t : getAddedTests()) {
            String res = WebInterface.query("modifytest.php", WebInterface.create("act", "add", "id", t.id, "name",
                    t.getName(), "questions", t.getQuestionCount()));
            if (!res.equals(""))
                throw new RuntimeException(res);
            t.save();
        }

        // Iterate through all the tests
        for (final Test t : tests) {

            // If test is modified, modify on db
            if (t.isModified()) {
                String res = WebInterface.query("modifytest.php", WebInterface.create("act", "edit", "id", t.id, "name",
                        t.getName(), "questions", t.getQuestionCount()));
                if (!res.equals(""))
                    throw new RuntimeException(res);
                t.save();
            }

            // If t has new questions, add to db
            if (t.questionsModified()) {

                // Add added questions to database
                for (final Question q : t.getAddedQuestions()) {
                    Map<String, String> r = WebInterface.create("act", "add",
                            "tid", t.id, "id", q.id, "question", q.getQuestion());

                    // multi-choice specific options
                    if (q instanceof ChoiceQuestion) {
                        ChoiceQuestion cq = (ChoiceQuestion) q;
                        r.put("type", "multichoice");
                        r.put("correct", cq.getCorrectText());
                        cq.save();

                        // Add options...
                        for (ChoiceQuestionOption cqo : cq.getOptions()) {
                            String res = WebInterface.query("modifyoption.php", WebInterface.create("act", "add",
                                    "id", cqo.id, "qid", cq.id, "opt", cqo.getOption(), "correct", cqo.correct(),
                                    "err", cqo.getWrongText()));
                            if (!res.equals(""))
                                throw new RuntimeException(res);
                            cqo.save();
                        }
                        cq.saveOptions();
                    } else if(q instanceof CompilableQuestion) {
                        CompilableQuestion cq = (CompilableQuestion) q;
                        r.put("type", "compilable");
                        int order = 0;
                        for(CompilableQuestionSegment segs : cq.getSegments()) {
                            String res = WebInterface.query("modifysegment.php", WebInterface.create("act", "add",
                                    "id", cq.id, "data", segs.getData(), "visible", segs.isVisible(), "editable",
                                    segs.isEditable(), "order", order++));
                            if (!res.equals(""))
                                throw new RuntimeException(res);
                        }
                        cq.saveSegments();
                    }

                    String res = WebInterface.query("modifyquestion.php", r);
                    if (!res.equals(""))
                        throw new RuntimeException(res);
                }
                for (final Question q : t.getRemovedQuestions()) {
                    // Delete questions!
                    String res = WebInterface.query("modifyquestion.php", WebInterface.create("act", "del", "id", q.id));
                    if (!res.equals(""))
                        throw new RuntimeException(res);
                }
            }
            for (final Question q : t.getQuestions()) {
                if (q.isModified()) {
                    Map<String, String> r = WebInterface.create("act", "edit",
                            "tid", t.id, "id", q.id, "question", q.getQuestion());

                    // multi-choice specific options
                    if (q instanceof ChoiceQuestion) {
                        ChoiceQuestion cq = (ChoiceQuestion) q;
                        r.put("type", "multichoice");
                        r.put("correct", cq.getCorrectText());
                    } else if(q instanceof CompilableQuestion)
                        r.put("type", "compilable");

                    String res = WebInterface.query("modifyquestion.php", r);
                    if (!res.equals(""))
                        throw new RuntimeException(res);
                    q.save();
                }
                if (q instanceof ChoiceQuestion) {
                    ChoiceQuestion cq = (ChoiceQuestion) q;
                    if (cq.optionsModified()) {
                        for (ChoiceQuestionOption cqo : cq.getAddedOptions()) {
                            String res = WebInterface.query("modifyoption.php", WebInterface.create("act", "add",
                                    "id", cqo.id, "qid", cq.id, "opt", cqo.getOption(), "correct", cqo.correct(),
                                    "err", cqo.getWrongText()));
                            if (!res.equals(""))
                                throw new RuntimeException(res);
                            cqo.save();
                        }
                        for (ChoiceQuestionOption cqo : cq.getRemovedOptions()) {
                            String res = WebInterface.query("modifyoption.php", WebInterface.create("act", "del",
                                    "id", cqo.id));
                            if (!res.equals(""))
                                throw new RuntimeException(res);
                            cqo.save();
                        }
                        for (ChoiceQuestionOption cqo : cq.getModifiedOptions()) {
                            String res = WebInterface.query("modifyoption.php", WebInterface.create("act", "edit",
                                    "id", cqo.id, "qid", cq.id, "opt", cqo.getOption(), "correct", cqo.correct(),
                                    "err", cqo.getWrongText()));
                            if (!res.equals(""))
                                throw new RuntimeException(res);
                            cqo.save();
                        }
                        cq.saveOptions();
                    }
                } else if(q instanceof CompilableQuestion) {
                    CompilableQuestion cq = (CompilableQuestion) q;
                    if(cq.segmentsModified()) {
                        int order = 0;
                        String res = WebInterface.query("modifysegment.php", WebInterface.create("act", "del",
                                "id", cq.id));
                        if (!res.equals(""))
                            throw new RuntimeException(res);

                        for(CompilableQuestionSegment segs : cq.getSegments()) {
                            res = WebInterface.query("modifysegment.php", WebInterface.create("act", "add",
                                    "id", cq.id, "data", segs.getData(), "visible", segs.isVisible(), "editable",
                                    segs.isEditable(), "order", order++));
                            if (!res.equals(""))
                                throw new RuntimeException(res);
                        }
                        cq.saveSegments();
                    }
                }

                q.save();
            }

            t.save();
            t.saveQuestions();
        }

        origTests.clear();
        origTests.addAll(tests);
    }

    private void loadTests() {
        origTests = new ArrayList<>();
        tests = new ArrayList<>();
        // Download tests for test selector
        List<Integer> testIds = new LinkedList<>();
        String[] tests = WebInterface.query("listtests.php", null).split("\u0000");
        for (String test : tests)
            testIds.add(Integer.parseInt(test));
        testId.setModel(new SpinnerListModel(testIds));

        for (int testId : testIds) {
            Test t = new Test(testId);
            t.loadTest();
            this.tests.add(t);
        }
        origTests.addAll(this.tests);
    }

    public JPanel getContent() {
        return panel1;
    }
}
