import javax.swing.*;
import java.awt.*;

public class StudentRecordsPanel extends JPanel {

    private String userId;
    private boolean isDarkMode;

    public StudentRecordsPanel(String userId, boolean isDarkMode) {
        this.userId = userId;
        this.isDarkMode = isDarkMode;

        setLayout(new BorderLayout());

        JLabel label = new JLabel("Student Records Panel", SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 18));

        add(label, BorderLayout.CENTER);
    }
}