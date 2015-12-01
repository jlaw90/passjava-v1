import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;

public class CreateQuestionDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JPanel questionArea;
    private JSpinner typeField;

    private Object editor;
    private int questionId = Question.getNextId();
    private Question question;

    private Question finale;

    public CreateQuestionDialog() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        typeField.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                String selected = (String) typeField.getValue();
                if(selected.equals("Multiple Choice")) {
                    ChoiceQuestionForm cqf = new ChoiceQuestionForm();
                    editor = cqf;
                    question = new ChoiceQuestion(questionId, "Question " + questionId, "");
                    cqf.init((ChoiceQuestion) question);

                    setQuestionArea(cqf.getContent());
                } else if(selected.equals("Compilable")) {
                    CompilableQuestionForm cqf = new CompilableQuestionForm();
                    editor = cqf;
                    question = new CompilableQuestion(questionId, "Question " + questionId);
                    cqf.init((CompilableQuestion) question);
                    setQuestionArea(cqf.getContent());
                }
            }
        });

        typeField.setValue(typeField.getNextValue());
        typeField.setValue(typeField.getPreviousValue());
    }

    private void setQuestionArea(JPanel j) {
        questionArea.removeAll();
        if(j != null)
            questionArea.add(j);
        questionArea.revalidate();
        questionArea.repaint();
    }

    private void onOK() {
// add your code here

        if(editor != null) {
            if(editor instanceof ChoiceQuestionForm) {
                finale = question;
            } else if(editor instanceof  CompilableQuestionForm) {
                finale = question;
            }
        }

        dispose();
    }

    public Question getCreation() {
        return finale;
    }

    private void onCancel() {
// add your code here if necessary
        dispose();
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
        typeField = new JSpinner(new SpinnerListModel(new Object[] {"Multiple Choice", "Compilable"}));
    }
}
