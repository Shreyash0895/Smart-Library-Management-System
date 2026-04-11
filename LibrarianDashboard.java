import javax.swing.*;
import java.awt.*;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;

public class LibrarianDashboard extends JFrame {
    private String userId;
    private JPanel contentPanel;
    private boolean isDarkMode = false;
    private Color darkBackground  = new Color(33,33,33);
    private Color lightBackground = new Color(242,242,242);
    private Color darkMenuBackground  = new Color(30,30,30);
    private Color lightMenuBackground = new Color(44,62,80);
    private JPanel menuPanel;

    private final Color BTN_NORMAL       = new Color(52,152,219);
    private final Color BTN_HOVER        = new Color(41,128,185);
    private final Color BTN_LOGOUT       = new Color(231,76,60);
    private final Color BTN_LOGOUT_HOVER = new Color(192,57,43);
    private final Color BTN_TOGGLE       = new Color(39,174,96);
    private final Color BTN_DARK_NORMAL  = new Color(70,70,70);
    private final Color BTN_DARK_HOVER   = new Color(100,100,100);

    public LibrarianDashboard(String userId) {
        this.userId = userId;
        setTitle("Library Management System - Librarian Dashboard");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JSplitPane splitPane = new JSplitPane();
        splitPane.setDividerLocation(250);
        splitPane.setDividerSize(1);
        menuPanel = createMenuPanel();
        splitPane.setLeftComponent(menuPanel);
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(lightBackground);
        splitPane.setRightComponent(contentPanel);
        add(splitPane);

        showWelcomeMessage();
        applyTheme();
    }

    private JPanel createMenuPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 10, 10, 10));
        panel.setBackground(lightMenuBackground);

        JLabel adminLabel = new JLabel("Librarian");
        adminLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        adminLabel.setForeground(Color.WHITE);
        panel.add(adminLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(100, 120, 140));
        sep.setMaximumSize(new Dimension(230, 2));
        panel.add(sep);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        for (String item : new String[]{
                "Dashboard Home","Manage Books","Issue Books",
                "View Issued Books","Return Books","Student Records",
                "Notifications","Toggle Theme","Logout"}) {
            panel.add(createMenuButton(item));
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

        Color normalColor = text.equals("Logout")       ? BTN_LOGOUT :
                            text.equals("Toggle Theme") ? BTN_TOGGLE :
                            isDarkMode ? BTN_DARK_NORMAL : BTN_NORMAL;
        Color hoverColor  = text.equals("Logout")       ? BTN_LOGOUT_HOVER :
                            text.equals("Toggle Theme") ? new Color(30,140,75) :
                            isDarkMode ? BTN_DARK_HOVER : BTN_HOVER;
        button.setBackground(normalColor);
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { button.setBackground(hoverColor); }
            public void mouseExited (java.awt.event.MouseEvent e) { button.setBackground(normalColor); }
        });
        button.addActionListener(e -> handleMenuClick(text));
        return button;
    }

    private void handleMenuClick(String item) {
        contentPanel.removeAll();
        switch (item) {
            case "Dashboard Home":    showWelcomeMessage(); return;
            case "Manage Books":      contentPanel.add(new BookManagementPanel(userId, isDarkMode));  break;
            case "Issue Books":       contentPanel.add(new IssueBooksPanel(userId, isDarkMode));      break;
            case "View Issued Books": contentPanel.add(new IssuedBooksPanel(userId, isDarkMode));     break;
            case "Return Books":      contentPanel.add(new ReturnBooksPanel(userId, isDarkMode));     break;
            case "Student Records":   contentPanel.add(new StudentRecordsPanel(userId, isDarkMode));  break;
            case "Notifications":     contentPanel.add(new NotificationPanel(userId, isDarkMode));    break;
            case "Toggle Theme":      toggleTheme(); return;
            case "Logout":            logout();      return;
        }
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void toggleTheme() { isDarkMode = !isDarkMode; applyTheme(); }

    private void applyTheme() {
        menuPanel.setBackground(isDarkMode ? darkMenuBackground : lightMenuBackground);
        contentPanel.setBackground(isDarkMode ? darkBackground : lightBackground);
        for (Component c : menuPanel.getComponents()) {
            if (c instanceof JButton) {
                JButton b = (JButton) c;
                String t = b.getText();
                b.setBackground(t.equals("Logout")       ? BTN_LOGOUT :
                                t.equals("Toggle Theme") ? BTN_TOGGLE :
                                isDarkMode ? BTN_DARK_NORMAL : BTN_NORMAL);
                b.setForeground(Color.WHITE);
            }
            if (c instanceof JLabel) ((JLabel)c).setForeground(Color.WHITE);
        }
        SwingUtilities.updateComponentTreeUI(this);
    }

    private void showWelcomeMessage() {
        contentPanel.removeAll();
        JPanel wp = new JPanel(new GridBagLayout());
        wp.setBackground(isDarkMode ? darkBackground : lightBackground);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10,10,10,10);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel welcomeLabel = new JLabel("Welcome to Librarian Dashboard");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        welcomeLabel.setForeground(isDarkMode ? Color.WHITE : Color.BLACK);
        wp.add(welcomeLabel, gbc);

        try {
            Document user = DatabaseConnection.getCollection("users")
                .find(Filters.eq("_id", new ObjectId(userId))).first();
            if (user != null) {
                JLabel nameLbl = new JLabel("Welcome, " + user.getString("full_name"));
                nameLbl.setFont(new Font("Segoe UI", Font.PLAIN, 18));
                nameLbl.setForeground(isDarkMode ? Color.WHITE : Color.BLACK);
                wp.add(nameLbl, gbc);
            }
            long issued = DatabaseConnection.getCollection("book_borrowings")
                .countDocuments(Filters.eq("status","BORROWED"));
            JLabel statsLbl = new JLabel("Total Books Currently Issued: " + issued);
            statsLbl.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            statsLbl.setForeground(isDarkMode ? Color.WHITE : Color.BLACK);
            wp.add(statsLbl, gbc);
        } catch (Exception ex) { ex.printStackTrace(); }

        contentPanel.add(wp);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void logout() {
        int c = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?",
            "Confirm Logout", JOptionPane.YES_NO_OPTION);
        if (c == JOptionPane.YES_OPTION) { dispose(); new LoginScreen().setVisible(true); }
    }
}