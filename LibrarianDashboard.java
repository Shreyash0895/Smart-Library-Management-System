import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LibrarianDashboard extends JFrame {
    private int userId;
    private JPanel contentPanel;
    private boolean isDarkMode = false;
    private Color darkBackground = new Color(33, 33, 33);
    private Color lightBackground = new Color(242, 242, 242);
    private Color darkMenuBackground = new Color(50, 50, 50);
    private Color lightMenuBackground = new Color(230, 230, 230);
    private Color primaryColor = new Color(70, 130, 180);
    private JPanel menuPanel;

    public LibrarianDashboard(int userId) {
        this.userId = userId;
        setTitle("Library Management System - Librarian Dashboard");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create split pane
        JSplitPane splitPane = new JSplitPane();
        splitPane.setBorder(null);
        
        // Create menu panel
        menuPanel = createMenuPanel();
        contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());
        
        showWelcomeMessage();

        splitPane.setLeftComponent(menuPanel);
        splitPane.setRightComponent(contentPanel);
        splitPane.setDividerLocation(250);
        splitPane.setDividerSize(1);

        add(splitPane);
        applyTheme();
    }

    private JPanel createMenuPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Add profile section
        JPanel profilePanel = new JPanel(new BorderLayout());
        profilePanel.setOpaque(false);
        JLabel userLabel = new JLabel("Librarian Dashboard");
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        profilePanel.add(userLabel, BorderLayout.CENTER);
        panel.add(profilePanel);
        panel.add(Box.createRigidArea(new Dimension(0, 30)));

        String[] menuItems = {
            "Manage Books",
            "Issue Books",
            "View Issued Books",
            "Return Books",
            "Student Records",
            "Notifications",
            "Toggle Theme",
            "Logout"
        };

        for (String item : menuItems) {
            JButton button = createMenuButton(item);
            panel.add(button);
            panel.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        return panel;
    }

    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(200, 40));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        
        button.addActionListener(e -> handleMenuClick(text));
        
        return button;
    }

    private void handleMenuClick(String menuItem) {
        switch(menuItem) {
            case "Manage Books":
                showBookManagement();
                break;
            case "Issue Books":
                showIssueBooks();
                break;
            case "View Issued Books":
                showIssuedBooks();
                break;
            case "Return Books":
                showReturnBooks();
                break;
            case "Student Records":
                showStudentRecords();
                break;
            case "Notifications":
                showNotifications();
                break;
            case "Toggle Theme":
                isDarkMode = !isDarkMode;
                applyTheme();
                break;
            case "Logout":
                handleLogout();
                break;
        }
    }

    private void applyTheme() {
        menuPanel.setBackground(isDarkMode ? darkMenuBackground : lightMenuBackground);
        contentPanel.setBackground(isDarkMode ? darkBackground : lightBackground);
        
        for (Component c : menuPanel.getComponents()) {
            if (c instanceof JButton) {
                JButton button = (JButton) c;
                button.setBackground(isDarkMode ? new Color(70, 70, 70) : primaryColor);
                button.setForeground(Color.WHITE);
            } else if (c instanceof JPanel) {
                JPanel panel = (JPanel) c;
                panel.setBackground(isDarkMode ? darkMenuBackground : lightMenuBackground);
                for (Component comp : panel.getComponents()) {
                    if (comp instanceof JLabel) {
                        ((JLabel) comp).setForeground(isDarkMode ? Color.WHITE : Color.BLACK);
                    }
                }
            }
        }

        SwingUtilities.updateComponentTreeUI(this);
    }

    private void showWelcomeMessage() {
        contentPanel.removeAll();
        
        JPanel welcomePanel = new JPanel(new GridBagLayout());
        welcomePanel.setBackground(isDarkMode ? darkBackground : lightBackground);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(10, 10, 10, 10);

        // Welcome message
        JLabel welcomeLabel = new JLabel("Welcome to Librarian Dashboard");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        welcomeLabel.setForeground(isDarkMode ? Color.WHITE : Color.BLACK);
        welcomePanel.add(welcomeLabel, gbc);

        // Add librarian name if available
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT full_name FROM users WHERE user_id = ?"
            );
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                JLabel nameLabel = new JLabel("Welcome, " + rs.getString("full_name"));
                nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
                nameLabel.setForeground(isDarkMode ? Color.WHITE : Color.BLACK);
                gbc.insets = new Insets(10, 10, 20, 10);
                welcomePanel.add(nameLabel, gbc);
            }

            // Get some statistics
            stmt = conn.prepareStatement(
                "SELECT COUNT(*) FROM book_borrowings WHERE status = 'BORROWED'"
            );
            rs = stmt.executeQuery();
            if (rs.next()) {
                JLabel statsLabel = new JLabel("Total Books Currently Issued: " + rs.getInt(1));
                statsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
                statsLabel.setForeground(isDarkMode ? Color.WHITE : Color.BLACK);
                gbc.insets = new Insets(20, 10, 10, 10);
                welcomePanel.add(statsLabel, gbc);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            showErrorMessage("Error loading statistics: " + ex.getMessage());
        }

        contentPanel.add(welcomePanel);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showBookManagement() {
        contentPanel.removeAll();
        try {
            BookManagementPanel bookPanel = new BookManagementPanel(userId , isDarkMode);
            contentPanel.add(bookPanel);
        } catch (Exception ex) {
            showErrorMessage("Error loading book management panel: " + ex.getMessage());
            ex.printStackTrace();
        }
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showIssueBooks() {
        contentPanel.removeAll();
        try {
            IssueBooksPanel issuePanel = new IssueBooksPanel(userId, isDarkMode);
            contentPanel.add(issuePanel);
        } catch (Exception ex) {
            showErrorMessage("Error loading issue books panel: " + ex.getMessage());
            ex.printStackTrace();
        }
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showIssuedBooks() {
        contentPanel.removeAll();
        try {
            IssuedBooksPanel issuedBooksPanel = new IssuedBooksPanel(userId, isDarkMode);
            contentPanel.add(issuedBooksPanel);
        } catch (Exception ex) {
            showErrorMessage("Error loading issued books panel: " + ex.getMessage());
            ex.printStackTrace();
        }
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showReturnBooks() {
        contentPanel.removeAll();
        try {
            ReturnBooksPanel returnPanel = new ReturnBooksPanel(userId, isDarkMode);
            contentPanel.add(returnPanel);
        } catch (Exception ex) {
            showErrorMessage("Error loading return books panel: " + ex.getMessage());
            ex.printStackTrace();
        }
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showStudentRecords() {
        contentPanel.removeAll();
        try {
            StudentRecordsPanel studentPanel = new StudentRecordsPanel(userId, isDarkMode);
            contentPanel.add(studentPanel);
        } catch (Exception ex) {
            showErrorMessage("Error loading student records panel: " + ex.getMessage());
            ex.printStackTrace();
        }
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showNotifications() {
        contentPanel.removeAll();
        try {
            NotificationPanel notificationPanel = new NotificationPanel(userId, isDarkMode);
            contentPanel.add(notificationPanel);
        } catch (Exception ex) {
            showErrorMessage("Error loading notifications panel: " + ex.getMessage());
            ex.printStackTrace();
        }
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showErrorMessage(String message) {
        JPanel errorPanel = new JPanel(new GridBagLayout());
        errorPanel.setBackground(isDarkMode ? darkBackground : lightBackground);
        
        JLabel errorLabel = new JLabel(message);
        errorLabel.setForeground(Color.RED);
        errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        errorPanel.add(errorLabel);
        contentPanel.add(errorPanel);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void handleLogout() {
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
}