import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StudentDashboard extends JFrame {
    private int userId;
    private JPanel contentPanel;
    private boolean isDarkMode = false;
    private Color darkBackground = new Color(33, 33, 33);
    private Color lightBackground = new Color(242, 242, 242);
    private Color darkMenuBackground = new Color(50, 50, 50);
    private Color lightMenuBackground = new Color(230, 230, 230);
    private JPanel menuPanel;

    public StudentDashboard(int userId) {
        this.userId = userId;
        setTitle("Library Management System - Student Dashboard");
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
        JLabel userLabel = new JLabel("Student Dashboard");
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        profilePanel.add(userLabel, BorderLayout.CENTER);
        panel.add(profilePanel);
        panel.add(Box.createRigidArea(new Dimension(0, 30)));

        String[] menuItems = {
            "Borrow Books",
            "Return Books",
            "View Status",
            "Request Books",
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
        
        button.addActionListener(e -> {
            switch(text) {
                case "Borrow Books":
                    showBorrowBooks();
                    break;
                case "Return Books":
                    showReturnBooks();
                    break;
                case "View Status":
                    showStatus();
                    break;
                case "Request Books":
                    showRequestBooks();
                    break;
                case "Notifications":
                    showNotifications();
                    break;
                case "Toggle Theme":
                    toggleTheme();
                    break;
                case "Logout":
                    logout();
                    break;
            }
        });
        
        return button;
    }

    private void toggleTheme() {
        isDarkMode = !isDarkMode;
        applyTheme();
    }

    private void applyTheme() {
        // Apply theme to menu panel
        menuPanel.setBackground(isDarkMode ? darkMenuBackground : lightMenuBackground);
        
        // Apply theme to content panel
        contentPanel.setBackground(isDarkMode ? darkBackground : lightBackground);
        
        // Update button colors
        for (Component c : menuPanel.getComponents()) {
            if (c instanceof JButton) {
                JButton button = (JButton) c;
                button.setBackground(isDarkMode ? new Color(70, 70, 70) : new Color(70, 130, 180));
                button.setForeground(Color.WHITE);
            } else if (c instanceof JPanel) {
                // Update profile section
                JPanel panel = (JPanel) c;
                panel.setBackground(isDarkMode ? darkMenuBackground : lightMenuBackground);
                for (Component comp : panel.getComponents()) {
                    if (comp instanceof JLabel) {
                        ((JLabel) comp).setForeground(isDarkMode ? Color.WHITE : Color.BLACK);
                    }
                }
            }
        }

        // Refresh the UI
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
        JLabel welcomeLabel = new JLabel("Welcome to Student Dashboard");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        welcomeLabel.setForeground(isDarkMode ? Color.WHITE : Color.BLACK);
        welcomePanel.add(welcomeLabel, gbc);

        // Add some stats or quick info
        try {
            Connection conn = DatabaseConnection.getConnection();
            
            // Get borrowed books count
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT COUNT(*) FROM book_borrowings WHERE user_id = ? AND status = 'BORROWED'"
            );
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                JLabel statsLabel = new JLabel("Currently Borrowed Books: " + rs.getInt(1));
                statsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
                statsLabel.setForeground(isDarkMode ? Color.WHITE : Color.BLACK);
                gbc.insets = new Insets(20, 10, 10, 10);
                welcomePanel.add(statsLabel, gbc);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        contentPanel.add(welcomePanel);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showBorrowBooks() {
        contentPanel.removeAll();
        BorrowBooksPanel borrowPanel = new BorrowBooksPanel(userId);
        contentPanel.add(borrowPanel);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

   
   
    private void showReturnBooks() {
        contentPanel.removeAll();
        try {
            ReturnBooksPanel returnPanel = new ReturnBooksPanel(userId, isDarkMode);
            contentPanel.add(returnPanel);
        } catch (Exception ex) {
            JPanel errorPanel = new JPanel(new GridBagLayout());
            errorPanel.setBackground(isDarkMode ? darkBackground : lightBackground);
            
            JLabel errorLabel = new JLabel("Error loading return books panel: " + ex.getMessage());
            errorLabel.setForeground(Color.RED);
            errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            
            errorPanel.add(errorLabel);
            contentPanel.add(errorPanel);
            ex.printStackTrace();
        }
        contentPanel.revalidate();
        contentPanel.repaint();
    }
    
    // Add this method to your StudentDashboard class if not already present
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

    private void showStatus() {
        contentPanel.removeAll();
        StatusPanel statusPanel = new StatusPanel(userId);
        contentPanel.add(statusPanel);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showRequestBooks() {
        contentPanel.removeAll();
        RequestBooksPanel requestPanel = new RequestBooksPanel(userId);
        contentPanel.add(requestPanel);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showNotifications() {
        contentPanel.removeAll();
        NotificationPanel notificationPanel = new NotificationPanel(userId, isDarkMode);    
        contentPanel.add(notificationPanel);
        contentPanel.revalidate();
        contentPanel.repaint();
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
}
