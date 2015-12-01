import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        JFrame jf = new JFrame("Test Manager");
        jf.setPreferredSize(new Dimension(800, 600));
        jf.add(new MainForm().getContent());
        jf.pack();
        jf.setLocationRelativeTo(null);
        jf.setVisible(true);
    }
}