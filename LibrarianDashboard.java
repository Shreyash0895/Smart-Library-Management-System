import javax.swing.*;
import java.awt.*;

public class LibrarianDashboard extends JFrame {

    private JPanel contentPanel;

    public LibrarianDashboard(String userId) {

        setTitle("Librarian Dashboard");
        setSize(1200, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JSplitPane split = new JSplitPane();
        split.setDividerLocation(250);

        split.setLeftComponent(menu());
        contentPanel = new JPanel(new BorderLayout());
        split.setRightComponent(contentPanel);

        add(split);

        showHome();
    }

    private JPanel menu() {

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(44, 62, 80));

        String[] items = {
                "Dashboard",
                "Manage Books",
                "User Approvals",
                "Reports",
                "Fine Management",
                "Settings",
                "ISBN Book Search",
                "Logout"
        };

        for (String item : items) {
            JButton btn = createBtn(item);
            btn.addActionListener(e -> handle(item));
            panel.add(btn);
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

        contentPanel.removeAll();

        switch (item) {
            case "Dashboard": showHome(); break;
            case "Manage Books": contentPanel.add(new BookManagementPanel()); break;
            case "User Approvals": contentPanel.add(new UserApprovalPanel()); break;
            case "Reports": contentPanel.add(new ReportsPanel()); break;
            case "Fine Management": contentPanel.add(new FineManagementPanel()); break;
            case "Settings": contentPanel.add(new SettingsPanel(null)); break;
            case "ISBN Book Search": contentPanel.add(new ISBNPanel()); break;
            case "Logout":
                dispose();
                new LoginScreen().setVisible(true);
                return;
        }

        refresh();
    }

    private void showHome() {
        contentPanel.add(new JLabel("Welcome Librarian!", SwingConstants.CENTER));
    }

    private void refresh() {
        contentPanel.revalidate();
        contentPanel.repaint();
    }
}