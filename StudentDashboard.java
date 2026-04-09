import javax.swing.*;
import java.awt.*;

public class StudentDashboard extends JFrame {

    private JPanel contentPanel, menuPanel;
    private boolean isDarkMode = false;
    private String userId;

    public StudentDashboard(String userId) {

        this.userId = userId;

        setTitle("Student Dashboard");
        setSize(1200, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JSplitPane split = new JSplitPane();
        split.setDividerLocation(250);

        menuPanel = createMenu();
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);

        split.setLeftComponent(menuPanel);
        split.setRightComponent(contentPanel);

        add(split);

        showHome();
    }

    private JPanel createMenu() {

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(44, 62, 80));

        String[] items = {
                "Dashboard",
                "Borrow Book",
                "Return Book",
                "Notifications",
                "Status",
                "ISBN Book Search",
                "Toggle Theme",
                "Logout"
        };

        for (String item : items) {
            JButton btn = createBtn(item);
            btn.addActionListener(e -> handle(item));
            panel.add(btn);
            panel.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        return panel;
    }

    private JButton createBtn(String text) {
        JButton b = new JButton(text);
        b.setMaximumSize(new Dimension(200, 45));
        b.setBackground(new Color(52, 152, 219));
        b.setForeground(Color.WHITE);
        return b;
    }

    private void handle(String item) {

        if (item.equals("Toggle Theme")) {
            toggleTheme();
            return;
        }

        contentPanel.removeAll();

        switch (item) {
            case "Dashboard": showHome(); break;
            case "Borrow Book": contentPanel.add(new BorrowBooksPanel(userId)); break;
            case "Return Book": contentPanel.add(new ReturnBooksPanel(userId)); break;
            case "Notifications": contentPanel.add(new NotificationPanel(userId)); break;
            case "Status": contentPanel.add(new StatusPanel(userId)); break;
            case "ISBN Book Search": contentPanel.add(new ISBNPanel()); break;
            case "Logout":
                dispose();
                new LoginScreen().setVisible(true);
                return;
        }

        refresh();
    }

    private void toggleTheme() {

        isDarkMode = !isDarkMode;

        Color bg = isDarkMode ? new Color(33, 33, 33) : Color.WHITE;
        Color menuBg = isDarkMode ? new Color(20, 20, 20) : new Color(44, 62, 80);

        contentPanel.setBackground(bg);
        menuPanel.setBackground(menuBg);

        for (Component c : menuPanel.getComponents()) {
            if (c instanceof JButton) {
                JButton btn = (JButton) c;
                btn.setBackground(isDarkMode ? new Color(70, 70, 70)
                        : new Color(52, 152, 219));
            }
        }

        SwingUtilities.updateComponentTreeUI(this);
    }

    private void showHome() {
        contentPanel.add(new JLabel("Welcome Student!", SwingConstants.CENTER));
    }

    private void refresh() {
        contentPanel.revalidate();
        contentPanel.repaint();
    }
}