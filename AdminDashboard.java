import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class AdminDashboard extends JFrame {
    private int userId;
    private JPanel contentPanel;
    private JLabel statusLabel;
    private boolean isDarkMode = false;
    private Color darkBackground = new Color(33, 33, 33);
    private Color lightBackground = new Color(242, 242, 242);
    private Color darkMenuBackground = new Color(30, 30, 30);
    private Color lightMenuBackground = new Color(44, 62, 80); // Dark navy sidebar
    private JPanel menuPanel;

    // Button colors
    private final Color BTN_NORMAL      = new Color(52, 152, 219);  // Bright blue
    private final Color BTN_HOVER       = new Color(41, 128, 185);  // Darker blue on hover
    private final Color BTN_LOGOUT      = new Color(231, 76, 60);   // Red for logout
    private final Color BTN_LOGOUT_HOVER= new Color(192, 57, 43);
    private final Color BTN_TOGGLE      = new Color(39, 174, 96);   // Green for toggle theme
    private final Color BTN_DARK_NORMAL = new Color(70, 70, 70);
    private final Color BTN_DARK_HOVER  = new Color(100, 100, 100);

    public AdminDashboard(int userId) {
        this.userId = userId;
        setTitle("Library Management System - Admin Dashboard");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JSplitPane splitPane = new JSplitPane();
        splitPane.setDividerLocation(250);

        menuPanel = createMenuPanel();
        splitPane.setLeftComponent(menuPanel);

        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(isDarkMode ? darkBackground : lightBackground);
        splitPane.setRightComponent(contentPanel);

        statusLabel = new JLabel("Welcome, Admin!");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        add(statusLabel, BorderLayout.SOUTH);

        add(splitPane);

        showWelcomeMessage();
        loadPendingApprovalsCount();
        applyTheme();
    }

    private JPanel createMenuPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 10, 10, 10));
        panel.setBackground(isDarkMode ? darkMenuBackground : lightMenuBackground);

        JPanel profilePanel = new JPanel(new BorderLayout());
        profilePanel.setOpaque(false);
        JLabel adminLabel = new JLabel("Administrator");
        adminLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        adminLabel.setForeground(Color.WHITE);
        profilePanel.add(adminLabel, BorderLayout.CENTER);
        panel.add(profilePanel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Separator
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(100, 120, 140));
        sep.setMaximumSize(new Dimension(230, 2));
        panel.add(sep);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        String[] menuItems = {
            "Dashboard Home",
            "📷 ISBN Scanner",
            "Manage Librarians",
            "View Reports",
            "Fine Management",
            "User Approvals",
            "System Settings",
            "Toggle Theme",
            "Logout"
        };

        for (String item : menuItems) {
            JButton button = createMenuButton(item);
            panel.add(button);
            panel.add(Box.createRigidArea(new Dimension(0, 8)));
        }

        return panel;
    }

    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setMaximumSize(new Dimension(230, 42));
        button.setPreferredSize(new Dimension(230, 42));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setForeground(Color.WHITE);

        // Pick color based on button type
        Color normalColor;
        Color hoverColor;
        if (text.equals("Logout")) {
            normalColor = BTN_LOGOUT;
            hoverColor  = BTN_LOGOUT_HOVER;
        } else if (text.equals("Toggle Theme")) {
            normalColor = BTN_TOGGLE;
            hoverColor  = new Color(30, 140, 75);
        } else {
            normalColor = isDarkMode ? BTN_DARK_NORMAL : BTN_NORMAL;
            hoverColor  = isDarkMode ? BTN_DARK_HOVER  : BTN_HOVER;
        }

        button.setBackground(normalColor);

        // Hover effect
        Color finalNormalColor = normalColor;
        Color finalHoverColor  = hoverColor;
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(finalHoverColor);
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(finalNormalColor);
            }
        });

        button.addActionListener(e -> {
            switch (text) {
                case "Dashboard Home":      showWelcomeMessage();       break;
                case "📷 ISBN Scanner":     showISBNScanner();          break;
                case "Manage Librarians":   showLibrarianManagement();  break;
                case "View Reports":        showReports();              break;
                case "Fine Management":     showFineManagement();       break;
                case "User Approvals":      showUserApprovals();        break;
                case "System Settings":     showSettings();             break;
                case "Toggle Theme":        toggleTheme();              break;
                case "Logout":              logout();                   break;
            }
        });

        return button;
    }

    private void toggleTheme() {
        isDarkMode = !isDarkMode;
        applyTheme();
    }

    private void applyTheme() {
        menuPanel.setBackground(isDarkMode ? darkMenuBackground : lightMenuBackground);
        contentPanel.setBackground(isDarkMode ? darkBackground : lightBackground);

        for (Component c : menuPanel.getComponents()) {
            if (c instanceof JButton) {
                JButton button = (JButton) c;
                String text = button.getText();
                if (text.equals("Logout")) {
                    button.setBackground(BTN_LOGOUT);
                } else if (text.equals("Toggle Theme")) {
                    button.setBackground(BTN_TOGGLE);
                } else {
                    button.setBackground(isDarkMode ? BTN_DARK_NORMAL : BTN_NORMAL);
                }
                button.setForeground(Color.WHITE);
            }
        }

        statusLabel.setBackground(isDarkMode ? darkBackground : lightBackground);
        statusLabel.setForeground(isDarkMode ? Color.WHITE : Color.BLACK);

        SwingUtilities.updateComponentTreeUI(this);
    }

    private void showWelcomeMessage() {
        contentPanel.removeAll();

        JPanel welcomePanel = new JPanel(new GridBagLayout());
        welcomePanel.setBackground(isDarkMode ? darkBackground : lightBackground);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel welcomeLabel = new JLabel("Welcome to Admin Dashboard");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        welcomeLabel.setForeground(isDarkMode ? Color.WHITE : Color.BLACK);
        gbc.gridx = 0;
        gbc.gridy = 0;
        welcomePanel.add(welcomeLabel, gbc);

        JPanel statsPanel = createStatsPanel();
        gbc.gridy = 1;
        welcomePanel.add(statsPanel, gbc);

        contentPanel.add(welcomePanel);
        contentPanel.revalidate();
        contentPanel.repaint();
        updateStatus("Welcome to Dashboard");
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 20, 20));
        panel.setBackground(isDarkMode ? darkBackground : lightBackground);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panel.add(createStatCard("Total Users",       getTotalUsers()));
        panel.add(createStatCard("Total Books",       getTotalBooks()));
        panel.add(createStatCard("Active Loans",      getActiveLoanCount()));
        panel.add(createStatCard("Pending Approvals", getPendingApprovals()));

        return panel;
    }

    private JPanel createStatCard(String title, int value) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(isDarkMode ? new Color(45, 45, 45) : Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(isDarkMode ? new Color(60, 60, 60) : new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(isDarkMode ? Color.WHITE : Color.BLACK);

        JLabel valueLabel = new JLabel(String.valueOf(value));
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLabel.setForeground(new Color(52, 152, 219));

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    private void showISBNScanner() {
        contentPanel.removeAll();
        QRScannerPanel panel = new QRScannerPanel();
        contentPanel.add(panel);
        contentPanel.revalidate();
        contentPanel.repaint();
        updateStatus("QR Code Book Scanner");
    }

    private void showLibrarianManagement() {
        contentPanel.removeAll();
        LibrarianManagementPanel panel = new LibrarianManagementPanel();
        panel.setBackground(isDarkMode ? darkBackground : lightBackground);
        contentPanel.add(panel);
        contentPanel.revalidate();
        contentPanel.repaint();
        updateStatus("Managing Librarians");
    }

    private void showReports() {
        contentPanel.removeAll();
        ReportsPanel panel = new ReportsPanel();
        panel.setBackground(isDarkMode ? darkBackground : lightBackground);
        contentPanel.add(panel);
        contentPanel.revalidate();
        contentPanel.repaint();
        updateStatus("Viewing Reports");
    }

    private void showFineManagement() {
        contentPanel.removeAll();
        FineManagementPanel panel = new FineManagementPanel();
        panel.setBackground(isDarkMode ? darkBackground : lightBackground);
        contentPanel.add(panel);
        contentPanel.revalidate();
        contentPanel.repaint();
        updateStatus("Managing Fines");
    }

    private void showUserApprovals() {
        contentPanel.removeAll();
        UserApprovalPanel panel = new UserApprovalPanel();
        panel.setBackground(isDarkMode ? darkBackground : lightBackground);
        contentPanel.add(panel);
        contentPanel.revalidate();
        contentPanel.repaint();
        updateStatus("Managing User Approvals");
    }

    private void showSettings() {
        contentPanel.removeAll();
        SettingsPanel panel = new SettingsPanel(userId);
        panel.setBackground(isDarkMode ? darkBackground : lightBackground);
        contentPanel.add(panel);
        contentPanel.revalidate();
        contentPanel.repaint();
        updateStatus("System Settings");
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to logout?",
            "Confirm Logout",
            JOptionPane.YES_NO_OPTION
        );
        if (confirm == JOptionPane.YES_OPTION) {
            this.dispose();
            new LoginScreen().setVisible(true);
        }
    }

    private void updateStatus(String message) {
        statusLabel.setText("Status: " + message);
    }

    private void loadPendingApprovalsCount() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "SELECT COUNT(*) FROM users WHERE is_active = false";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            if (rs.next() && rs.getInt(1) > 0) {
                updateStatus("You have " + rs.getInt(1) + " pending user approvals");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int getTotalUsers() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM users WHERE is_active = true");
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    private int getTotalBooks() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM books");
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    private int getActiveLoanCount() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            ResultSet rs = conn.createStatement().executeQuery(
                "SELECT COUNT(*) FROM issued_books WHERE return_date IS NULL"
            );
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    private int getPendingApprovals() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            ResultSet rs = conn.createStatement().executeQuery(
                "SELECT COUNT(*) FROM users WHERE is_approved = 0"
            );
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }
}