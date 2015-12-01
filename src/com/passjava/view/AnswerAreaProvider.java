package com.passjava.view;

import com.passjava.model.syntaxhighlighter.DocumentRegion;
import com.passjava.model.syntaxhighlighter.JavaDocument;
import com.passjava.model.test.Question;
import com.passjava.model.test.choice.ChoiceQuestion;
import com.passjava.model.test.choice.ChoiceQuestionOption;
import com.passjava.model.test.compilable.CompilableQuestion;
import com.passjava.model.test.compilable.CompilableQuestionSegment;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class AnswerAreaProvider {
    public static void provide(Question q, QuestionView qv, boolean readonly) {
        JPanel target = qv.getAnswerContainer();
        Class c = q.getClass();
        if (c == ChoiceQuestion.class) {
            GridBagLayout gbl = new GridBagLayout();
            target.setLayout(gbl);
            ChoiceQuestion cq = (ChoiceQuestion) q;
            int optCount = cq.options.length;
            int corCount = 0;
            for (ChoiceQuestionOption cqo : cq.options)
                if (cqo.correct)
                    corCount++;
            final List<JComponent> optionComponents = new LinkedList<>();
            if (corCount != 1) { // Provide check boxes
                for (int i = 0; i < optCount; i++) {
                    ChoiceQuestionOption cqo = ((ChoiceQuestion) q).options[i];
                    if(!readonly)
                        qv.setHintText("Select one or more of the answers below");
                    else
                        qv.setHintText("Below are the answers you selected");
                    JCheckBox jcb = new JCheckBox("<html><b>" + (i + 1) + "." + "</b> " + cqo.option);
                    jcb.setSelected(cqo.checked);
                    jcb.setHorizontalTextPosition(JCheckBox.TRAILING);
                    jcb.setName("option" + i);
                    optionComponents.add(jcb);
                    jcb.setOpaque(false);
                    jcb.setEnabled(!readonly);
                }
            } else { // Provide radio boxes
                for (int i = 0; i < optCount; i++) {
                    if(!readonly)
                        qv.setHintText("Select one or more of the answers below");
                    else
                        qv.setHintText("Below are the answers you selected");
                    ChoiceQuestionOption cqo = ((ChoiceQuestion) q).options[i];
                    JRadioButton jcb = new JRadioButton("<html><b>" + (i + 1) + "." + "</b> " + cqo.option);
                    jcb.setSelected(cqo.checked);
                    jcb.setHorizontalTextPosition(JCheckBox.TRAILING);
                    jcb.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            JToggleButton src = (JToggleButton) e.getSource();
                            if (!src.isSelected())
                                return;
                            for (JComponent jcb : optionComponents)
                                if (jcb instanceof JToggleButton && jcb != src)
                                    ((JToggleButton) jcb).setSelected(false);
                        }
                    });

                    jcb.setName("option" + i);
                    optionComponents.add(jcb);
                    jcb.setOpaque(false);
                    jcb.setEnabled(!readonly);
                }
            }

            // Layout
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(0, 32, 0, 0);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.CENTER;
            gbc.weightx = gbc.weighty = 1;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;

            int col = 0;
            for (JComponent optionComponent : optionComponents) {
                gbc.gridx = col % 2;
                gbc.gridy = col / 2;
                target.add(optionComponent, gbc);
                col++;
            }
        } else if(q instanceof CompilableQuestion) {
            if(!readonly)
                qv.setHintText("You can modify any highlighted areas in the source editor below");
            else
                qv.setHintText("The highlighted areas below are the ones you would have modified to answer the question");
            CompilableQuestion cq = (CompilableQuestion) q;

            CodeEditor ce = new CodeEditor();
            ce.setEditable(!readonly);
            for(CompilableQuestionSegment cs: cq.segments) {
                if(!cs.visible)
                    continue;
                String d = cs.editable? cs.userData: cs.data;
                ce.insertString(d);
                if(cs.editable)
                    ce.makeEditable(ce.getLength() - d.length(), d.length());
            }
            ce.lock();
            ce.getContent().revalidate();
            ce.getContent().repaint();
            target.add(ce.getContent());
        }
    }

    public static boolean completed(Question q, JPanel container) {
        Class c = q.getClass();
        if(c == ChoiceQuestion.class) {
            Component[] components = container.getComponents();
            for (Component com : components) {
                if (com.getName() != null && com.getName().startsWith("option")) {
                    if(((JToggleButton) com).isSelected()) {
                        return true;
                    }
                }
            }
            return false;
        }
        if(c == CompilableQuestion.class) {
            CompilableQuestion cq = (CompilableQuestion) q;

            // Get EditorPane
            JEditorPane jep = (JEditorPane) find("codeArea", container);
            JavaDocument doc = (JavaDocument) jep.getDocument();


            // check source
            int editablePos = 0;
            for(CompilableQuestionSegment seg: cq.segments) {
                if(seg.editable) {
                    // Retrieve data as modified in the document...
                    DocumentRegion reg = doc.getEditableRegions().get(editablePos++);
                    try {
                        if(!doc.getText(reg.start, reg.end - reg.start).equals(seg.data))
                            return true;
                    } catch (BadLocationException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            return false;
        }

        throw new UnsupportedOperationException();
    }

    public static Object provideAnswer(Question q, JPanel target) {
        Class c = q.getClass();
        if (c == ChoiceQuestion.class) {
            ChoiceQuestion cq = (ChoiceQuestion) q;
            int optCount = cq.options.length;
            boolean[] checked = new boolean[optCount];
            Component[] components = target.getComponents();
            for (Component com : components) {
                if (com.getName() != null && com.getName().startsWith("option")) {
                    checked[Integer.parseInt(com.getName().substring(6))] = ((JToggleButton) com).isSelected();
                }
            }
            return checked;
        } else if(c == CompilableQuestion.class) {
            CompilableQuestion cq = (CompilableQuestion) q;

            // Get EditorPane
            JEditorPane jep = (JEditorPane) find("codeArea", target);
            JavaDocument doc = (JavaDocument) jep.getDocument();


            // build source
            int editablePos = 0;
            //int pos = 0;
            StringBuilder source = new StringBuilder();
            for(CompilableQuestionSegment seg: cq.segments) {
                if(!seg.editable) {
                    source.append(seg.data);
                } else {
                    // Retrieve data as modified in the document...
                    DocumentRegion reg = doc.getEditableRegions().get(editablePos++);
                    try {
                        source.append(doc.getText(reg.start, reg.end - reg.start));
                    } catch (BadLocationException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            return source.toString();
        }

        throw new RuntimeException();
    }

    public static Object getProgressUpdateData(Question q, JPanel target) {
        Class c = q.getClass();
        if (c == ChoiceQuestion.class) {
            ChoiceQuestion cq = (ChoiceQuestion) q;
            int optCount = cq.options.length;
            boolean[] checked = new boolean[optCount];
            Component[] components = target.getComponents();
            for (Component com : components) {
                if (com.getName() != null && com.getName().startsWith("option")) {
                    checked[Integer.parseInt(com.getName().substring(6))] = ((JToggleButton) com).isSelected();
                }
            }
            return checked;
        } else if(c == CompilableQuestion.class) {
            CompilableQuestion cq = (CompilableQuestion) q;

            // Get EditorPane
            JEditorPane jep = (JEditorPane) find("codeArea", target);
            JavaDocument doc = (JavaDocument) jep.getDocument();


            // build source
            int editablePos = 0;
            //int pos = 0;
            List<String> segs = new ArrayList<String>();
            for(CompilableQuestionSegment seg: cq.segments) {
                if (!seg.editable) {
                    continue;
                }
                DocumentRegion reg = doc.getEditableRegions().get(editablePos++);
                try {
                    segs.add(doc.getText(reg.start, reg.end - reg.start));
                } catch (BadLocationException e) {
                    throw new RuntimeException(e);
                }
            }
            String[] segsa = new String[segs.size()];
            return segs.toArray(segsa);
        }

        throw new RuntimeException();
    }

    private static Component find(String name, Container container) {
        for(Component c: container.getComponents()) {
            if(c.getName() != null && c.getName().equals(name))
                return c;

            if(c instanceof Container) {
                Component found = find(name, (Container) c);
                if(found != null)
                    return found;
            }
        }
        return null;
    }
}