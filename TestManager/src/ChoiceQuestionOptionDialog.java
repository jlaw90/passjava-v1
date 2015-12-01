import javax.swing.*;
import java.awt.event.*;

public class ChoiceQuestionOptionDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField option;
    private JCheckBox correct;
    private JTextField wrongMessage;
    private ChoiceQuestionOption creation;

    public ChoiceQuestionOptionDialog() {
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

// call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

// call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        creation = new ChoiceQuestionOption(ChoiceQuestionOption.getId(), "", correct.isSelected(), wrongMessage.getText());
        creation.setOption(option.getText());
        dispose();
    }

    public ChoiceQuestionOption getCreated() {
        return creation;
    }

    private void onCancel() {
// add your code here if necessary
        dispose();
    }
}
