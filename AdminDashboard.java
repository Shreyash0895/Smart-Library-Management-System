import javax.swing.*;
import java.awt.*;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import org.bson.Document;

public class AdminDashboard extends JFrame {

    private String userId;
    private JPanel contentPanel;
    private JLabel statusLabel;

    public AdminDashboard(String userId) {
        this.userId = userId;

        setTitle("Library Management System - Admin Dashboard");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JSplitPane splitPane = new JSplitPane();
        splitPane.setDividerLocation(250);

        splitPane.setLeftComponent(createMenuPanel());

        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);
        splitPane.setRightComponent(contentPanel);

        add(splitPane);

        statusLabel = new JLabel("Welcome, Admin!");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        add(statusLabel, BorderLayout.SOUTH);

        showDashboard();
    }

    // 🔵 MENU PANEL
    private JPanel createMenuPanel() {

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(44, 62, 80));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 10, 10, 10));

        JLabel title = new JLabel("Administrator");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        panel.add(title);

        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        String[] menuItems = {
                "Dashboard Home",
                "Manage Librarians",
                "View Reports",
                "Fine Management",
                "User Approvals",
                "System Settings",
                "Logout"
        };

        for (String item : menuItems) {
            JButton btn = createMenuButton(item);
            btn.addActionListener(e -> handleMenu(item));
            panel.add(btn);
            panel.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        return panel;
    }

    // 🔘 BUTTON STYLE
    private JButton createMenuButton(String text) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(220, 45));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setBackground(new Color(52, 152, 219));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        return btn;
    }

    // 🎯 MENU HANDLER
    private void handleMenu(String item) {

        contentPanel.removeAll();

        switch (item) {

            case "Dashboard Home":
                showDashboard();
                break;

            case "Manage Librarians":
                contentPanel.add(new LibrarianManagementPanel());
                break;

            case "View Reports":
                contentPanel.add(new ReportsPanel());
                break;

            case "Fine Management":
                contentPanel.add(new FineManagementPanel());
                break;

            case "User Approvals":
                contentPanel.add(new UserApprovalPanel()); // ✅ NEW FEATURE
                break;

            case "System Settings":
                contentPanel.add(new SettingsPanel(userId));
                break;

            case "Logout":
                dispose();
                new LoginScreen().setVisible(true);
                return;
        }

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    // 🌟 DASHBOARD UI
    private void showDashboard() {

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel title = new JLabel("Welcome to Admin Dashboard");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(title, gbc);

        gbc.gridwidth = 1;

        gbc.gridy = 1;
        panel.add(createCard("Total Users", getTotalUsers()), gbc);

        gbc.gridx = 1;
        panel.add(createCard("Total Books", getTotalBooks()), gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(createCard("Active Loans", getActiveLoans()), gbc);

        gbc.gridx = 1;
        panel.add(createCard("Pending Approvals", getPendingApprovals()), gbc);

        contentPanel.add(panel);
    }

    // 🎯 CARD DESIGN
    private JPanel createCard(String title, long value) {

        JPanel card = new JPanel(new BorderLayout());
        card.setPreferredSize(new Dimension(200, 100));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JLabel valueLabel = new JLabel(String.valueOf(value));
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLabel.setForeground(new Color(52, 152, 219));

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    // 🔥 DATABASE METHODS

    private long getTotalUsers() {
        return DatabaseConnection.getCollection("users")
                .countDocuments();
    }

    private long getTotalBooks() {
        return DatabaseConnection.getCollection("books")
                .countDocuments();
    }

    private long getActiveLoans() {
        return DatabaseConnection.getCollection("book_borrowings")
                .countDocuments(Filters.eq("status", "BORROWED"));
    }

    private long getPendingApprovals() {
        return DatabaseConnection.getCollection("users")
                .countDocuments(Filters.eq("is_approved", false));
    }
}