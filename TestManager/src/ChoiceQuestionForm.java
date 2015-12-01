import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class ChoiceQuestionForm {
    private JPanel panel1;
    private JTextArea correctMessage;
    private JList optionList;
    private JButton addButton;
    private JButton deleteButton;

    private JTextArea option;
    private JCheckBox correct;
    private JTextArea wrongMessage;
    private JTextArea question;

    private ChoiceQuestion curQ;
    private ChoiceQuestionOption curO;

    public ChoiceQuestionForm() {
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(optionList.getSelectedValue() == null)
                    return;
                DefaultListModel model = (DefaultListModel) optionList.getModel();
                curQ.removeOption((ChoiceQuestionOption) optionList.getSelectedValue());
                model.removeElement(optionList.getSelectedValue());
            }
        });
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ChoiceQuestionOptionDialog dialog = new ChoiceQuestionOptionDialog();
                dialog.pack();
                dialog.setLocationRelativeTo(panel1);
                dialog.setVisible(true);
                if(dialog.getCreated() == null)
                    return;
                DefaultListModel model = (DefaultListModel) optionList.getModel();
                model.addElement(dialog.getCreated());
                curQ.addOption(dialog.getCreated());
            }
        });
        correctMessage.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if(curQ != null)
                    curQ.setCorrectText(correctMessage.getText());
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
        optionList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                ChoiceQuestionOption opt = (ChoiceQuestionOption) optionList.getSelectedValue();
                initOption(opt);
            }
        });
        correct.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if(curO != null)
                    curO.setCorrect(correct.isSelected());
            }
        });
        option.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if(curO != null)
                    curO.setOption(option.getText());
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
        wrongMessage.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if(curO != null)
                    curO.setWrongText(wrongMessage.getText());
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
        question.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if(curQ != null)
                    curQ.setQuestion(question.getText());
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
    }

    public void init(ChoiceQuestion cq) {
        curQ = null;
        question.setText(cq.getQuestion());
        correctMessage.setText(cq.getCorrectText());
        DefaultListModel<ChoiceQuestionOption> model = new DefaultListModel<>();
        List<ChoiceQuestionOption> opts = cq.getOptions();
        for (int i = 0; i < opts.size(); i++)
            model.add(i, opts.get(i));
        optionList.setModel(model);

        initOption((ChoiceQuestionOption) optionList.getSelectedValue());

        curQ = cq;
    }

    private void initOption(ChoiceQuestionOption opt) {
        curO = null;
        if(opt == null) {
            option.setEnabled(false);
            correct.setEnabled(false);
            wrongMessage.setEnabled(false);
            return;
        } else {
            option.setEnabled(true);
            correct.setEnabled(true);
            wrongMessage.setEnabled(true);
        }

        option.setText(opt.getOption());
        correct.setSelected(opt.correct());
        wrongMessage.setText(opt.getWrongText());
        curO = opt;
    }

    public JPanel getContent() {
        return panel1;
    }
}