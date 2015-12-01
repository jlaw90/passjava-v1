import com.passjava.model.syntaxhighlighter.JavaEditorKit;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.util.List;

public class CompilableQuestionForm {
    private JPanel panel1;
    private JTextArea question;
    private JEditorPane codeArea;

    private CompilableQuestion working;

    public JPanel getContent() {
        return panel1;
    }

    public void init(CompilableQuestion cq) {
        codeArea.setText(CompilableQuestionProcessor.process(cq.getSegments()));
        question.setText(cq.getQuestion());
        working = cq;
    }

    public List<CompilableQuestionSegment> processSegments() {
        return CompilableQuestionProcessor.process(codeArea.getText());
    }

    private void createUIComponents() {
        codeArea = new JEditorPane();
        codeArea.setEditorKit(new JavaEditorKit());

        codeArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if(working == null)
                    return;
                List<CompilableQuestionSegment> segs = CompilableQuestionProcessor.process(codeArea.getText());
                working.setSegments(segs);
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

        question = new JTextArea();
        question.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if(working != null)
                    working.setQuestion(question.getText());
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
}