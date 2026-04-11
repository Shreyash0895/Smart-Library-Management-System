import javax.swing.*;
import java.awt.*;
import com.mongodb.client.*;
import org.bson.Document;

public class StudentDashboard extends JFrame {

    private String userId;
    private JPanel contentPanel;
    private JLabel welcomeLabel;

    public StudentDashboard(String userId) {
        this.userId = userId;

        setTitle("Library Management System - Student Dashboard");
        setSize(1200, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JSplitPane splitPane = new JSplitPane();
        splitPane.setDividerLocation(250);

        splitPane.setLeftComponent(createMenuPanel());

        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);
        splitPane.setRightComponent(contentPanel);

        add(splitPane);

        showHome();
    }

    // 🔵 SIDEBAR MENU (LIKE LIBRARIAN)
    private JPanel createMenuPanel() {

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(44, 62, 80));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 10, 10, 10));

        JLabel title = new JLabel("Student");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(Color.WHITE);
        panel.add(title);

        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        String[] menuItems = {
                "Dashboard Home",
                "Borrow Book",
                "Return Book",
                "Notifications",
                "Status",
                "Logout"
        };

        for (String item : menuItems) {
            panel.add(createMenuButton(item));
            panel.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        return panel;
    }

    // 🔘 BUTTON STYLE (MATCH LIBRARIAN)
    private JButton createMenuButton(String text) {

        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(220, 45));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);

        btn.setBackground(new Color(52, 152, 219));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));

        btn.addActionListener(e -> handleMenu(text));

        return btn;
    }

    // 🎯 MENU HANDLER
    private void handleMenu(String item) {

        contentPanel.removeAll();

        try {
            switch (item) {

                case "Dashboard Home":
                    showHome();
                    break;

                case "Borrow Book":
                    contentPanel.add(new BorrowBooksPanel(userId));
                    break;

                case "Return Book":
                    contentPanel.add(new ReturnBooksPanel(userId));
                    break;

                case "Notifications":
                    contentPanel.add(new NotificationPanel(userId));
                    break;

                case "Status":
                    contentPanel.add(new StatusPanel(userId));
                    break;

                case "Logout":
                    dispose();
                    new LoginScreen().setVisible(true);
                    return;
            }

        } catch (Exception e) {
            e.printStackTrace();
            contentPanel.add(new JLabel("Error: " + e.getMessage()));
        }

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    // 🏠 HOME SCREEN (WITH USERNAME)
    private void showHome() {

        contentPanel.removeAll();

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();

        String username = getUsername();

        welcomeLabel = new JLabel("Welcome, " + username + "!");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));

        panel.add(welcomeLabel, gbc);

        contentPanel.add(panel);
    }

    // 🔥 FETCH USERNAME FROM MONGODB
    private String getUsername() {
        try {
            MongoCollection<Document> users =
                    DatabaseConnection.getCollection("users");

            Document user = users.find(new Document("_id",
                    new org.bson.types.ObjectId(userId))).first();

            if (user != null) {
                return user.getString("username");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "Student";
    }
}