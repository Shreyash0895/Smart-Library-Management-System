import javax.swing.*;
import java.awt.*;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;

public class StudentDashboard extends JFrame {
    private String userId;
    private JPanel contentPanel;
    private boolean isDarkMode = false;
    private Color darkBg = new Color(33,33,33), lightBg = new Color(242,242,242);
    private Color darkMenu = new Color(50,50,50), lightMenu = new Color(44,62,80);
    private JPanel menuPanel;

    public StudentDashboard(String userId) {
        this.userId = userId;
        setTitle("Library Management System - Student Dashboard");
        setSize(1200,800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JSplitPane split = new JSplitPane();
        split.setBorder(null);
        menuPanel    = createMenuPanel();
        contentPanel = new JPanel(new BorderLayout());
        showWelcomeMessage();
        split.setLeftComponent(menuPanel);
        split.setRightComponent(contentPanel);
        split.setDividerLocation(250);
        add(split);
        applyTheme();
    }

    private JPanel createMenuPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(15,15,15,15));
        p.setBackground(lightMenu);

        JLabel lbl = new JLabel("Student Dashboard");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lbl.setForeground(Color.WHITE);
        p.add(lbl);
        p.add(Box.createRigidArea(new Dimension(0,25)));

        for (String item : new String[]{"Borrow Books","Return Books","Request Books",
                "View Status","Notifications","Toggle Theme","Logout"}) {
            JButton b = menuBtn(item);
            p.add(b);
            p.add(Box.createRigidArea(new Dimension(0,10)));
        }
        return p;
    }

    private JButton menuBtn(String text) {
        JButton b = new JButton(text);
        b.setMaximumSize(new Dimension(210,40));
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setFocusPainted(false); b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        Color bg = text.equals("Logout") ? new Color(231,76,60) :
                   text.equals("Toggle Theme") ? new Color(39,174,96) : new Color(52,152,219);
        b.setBackground(bg); b.setForeground(Color.WHITE);
        b.addActionListener(e -> handleMenu(text));
        return b;
    }

    private void handleMenu(String item) {
        contentPanel.removeAll();
        switch(item) {
            case "Borrow Books":  contentPanel.add(new BorrowBooksPanel(userId));            break;
            case "Return Books":  contentPanel.add(new ReturnBooksPanel(userId, isDarkMode)); break;
            case "Request Books": contentPanel.add(new RequestBooksPanel(userId));            break;
            case "View Status":   contentPanel.add(new StatusPanel(userId));                  break;
            case "Notifications": contentPanel.add(new NotificationPanel(userId, isDarkMode));break;
            case "Toggle Theme":  toggleTheme(); return;
            case "Logout":        logout();      return;
        }
        contentPanel.revalidate(); contentPanel.repaint();
    }

    private void toggleTheme() { isDarkMode = !isDarkMode; applyTheme(); }

    private void applyTheme() {
        menuPanel.setBackground(isDarkMode ? darkMenu : lightMenu);
        contentPanel.setBackground(isDarkMode ? darkBg : lightBg);
        for (Component c : menuPanel.getComponents()) {
            if (c instanceof JLabel) ((JLabel)c).setForeground(Color.WHITE);
        }
        SwingUtilities.updateComponentTreeUI(this);
    }

    private void showWelcomeMessage() {
        contentPanel.removeAll();
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(isDarkMode ? darkBg : lightBg);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10,10,10,10);
        gbc.gridwidth = 1;

        JLabel title = new JLabel("Welcome, Student!", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(new Color(44,62,80));
        gbc.gridx = 0; gbc.gridy = 0;
        p.add(title, gbc);

        try {
            Document user = DatabaseConnection.getCollection("users")
                .find(Filters.eq("_id", new ObjectId(userId))).first();
            if (user != null) {
                JLabel name = new JLabel("Hello, " + user.getString("full_name"), SwingConstants.CENTER);
                name.setFont(new Font("Segoe UI", Font.PLAIN, 18));
                name.setForeground(new Color(52,73,94));
                gbc.gridy = 1; p.add(name, gbc);
            }
            long borrowed = DatabaseConnection.getCollection("book_borrowings")
                .countDocuments(Filters.and(
                    Filters.eq("user_id", userId), Filters.eq("status","BORROWED")));
            JLabel stats = new JLabel("Books Currently Borrowed: " + borrowed, SwingConstants.CENTER);
            stats.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            stats.setForeground(new Color(41,128,185));
            gbc.gridy = 2; p.add(stats, gbc);
        } catch (Exception e) { e.printStackTrace(); }

        contentPanel.add(p); contentPanel.revalidate(); contentPanel.repaint();
    }

    private void logout() {
        int c = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", "Confirm Logout", JOptionPane.YES_NO_OPTION);
        if (c == JOptionPane.YES_OPTION) { dispose(); new LoginScreen().setVisible(true); }
    }
}