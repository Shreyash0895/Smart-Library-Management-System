import javax.swing.*;
import java.awt.*;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;

public class LibrarianDashboard extends JFrame {
    private String userId;
    private JPanel contentPanel;
    private boolean isDarkMode = false;
    private Color darkBg = new Color(33,33,33), lightBg = new Color(242,242,242);
    private Color darkMenu = new Color(50,50,50), lightMenu = new Color(44,62,80);
    private JPanel menuPanel;

    public LibrarianDashboard(String userId) {
        this.userId = userId;
        setTitle("Library Management System - Librarian Dashboard");
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
        split.setDividerSize(1);
        add(split);
        applyTheme();
    }

    private JPanel createMenuPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(15,15,15,15));
        p.setBackground(lightMenu);

        JLabel lbl = new JLabel("Librarian Dashboard");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lbl.setForeground(Color.WHITE);
        p.add(lbl);
        p.add(Box.createRigidArea(new Dimension(0,25)));

        for (String item : new String[]{"Dashboard Home","Manage Books","Issue Books",
                "View Issued Books","Return Books","Student Records",
                "Notifications","Toggle Theme","Logout"}) {
            p.add(menuBtn(item));
            p.add(Box.createRigidArea(new Dimension(0,8)));
        }
        return p;
    }

    private JButton menuBtn(String text) {
        JButton b = new JButton(text);
        b.setMaximumSize(new Dimension(215,40));
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setFocusPainted(false); b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        Color bg = text.equals("Logout") ? new Color(231,76,60) :
                   text.equals("Toggle Theme") ? new Color(39,174,96) : new Color(52,152,219);
        b.setBackground(bg); b.setForeground(Color.WHITE);
        b.addActionListener(e -> handleMenuClick(text));
        return b;
    }

    private void handleMenuClick(String item) {
        switch(item) {
            case "Dashboard Home":    showWelcomeMessage(); break;
            case "Manage Books":      load(new BookManagementPanel(userId, isDarkMode)); break;
            case "Issue Books":       load(new IssueBooksPanel(userId, isDarkMode));     break;
            case "View Issued Books": load(new IssuedBooksPanel(userId, isDarkMode));    break;
            case "Return Books":      load(new ReturnBooksPanel(userId, isDarkMode));    break;
            case "Student Records":   load(new StudentRecordsPanel(userId, isDarkMode)); break;
            case "Notifications":     load(new NotificationPanel(userId, isDarkMode));   break;
            case "Toggle Theme":      isDarkMode = !isDarkMode; applyTheme(); break;
            case "Logout":            logout(); break;
        }
    }

    private void load(JPanel p) {
        contentPanel.removeAll(); contentPanel.add(p);
        contentPanel.revalidate(); contentPanel.repaint();
    }

    private void applyTheme() {
        menuPanel.setBackground(isDarkMode ? darkMenu : lightMenu);
        contentPanel.setBackground(isDarkMode ? darkBg : lightBg);
        SwingUtilities.updateComponentTreeUI(this);
    }

    private void showWelcomeMessage() {
        contentPanel.removeAll();
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(isDarkMode ? darkBg : lightBg);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10,10,10,10);

        JLabel wl = new JLabel("Welcome to Librarian Dashboard");
        wl.setFont(new Font("Segoe UI", Font.BOLD, 24));
        wl.setForeground(isDarkMode ? Color.WHITE : new Color(44,62,80));
        gbc.gridx = 0; gbc.gridy = 0; p.add(wl, gbc);

        try {
            Document user = DatabaseConnection.getCollection("users")
                .find(Filters.eq("_id", new ObjectId(userId))).first();
            if (user != null) {
                JLabel nl = new JLabel("Welcome, " + user.getString("full_name"));
                nl.setFont(new Font("Segoe UI", Font.PLAIN, 18));
                nl.setForeground(new Color(52,73,94));
                gbc.gridy = 1; p.add(nl, gbc);
            }
            long issued = DatabaseConnection.getCollection("book_borrowings")
                .countDocuments(Filters.eq("status","BORROWED"));
            JLabel sl = new JLabel("Total Books Currently Issued: " + issued);
            sl.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            sl.setForeground(new Color(41,128,185));
            gbc.gridy = 2; p.add(sl, gbc);
        } catch (Exception ex) { ex.printStackTrace(); }

        contentPanel.add(p); contentPanel.revalidate(); contentPanel.repaint();
    }

    private void logout() {
        int c = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?",
            "Confirm Logout", JOptionPane.YES_NO_OPTION);
        if (c == JOptionPane.YES_OPTION) { dispose(); new LoginScreen().setVisible(true); }
    }
}