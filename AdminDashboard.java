import javax.swing.*;
import java.awt.*;
import com.mongodb.client.model.Filters;
import org.bson.Document;

public class AdminDashboard extends JFrame {
    private String userId;
    private JPanel contentPanel;
    private JLabel statusLabel;
    private boolean isDarkMode = false;
    private Color darkBg   = new Color(33,33,33),   lightBg   = new Color(242,242,242);
    private Color darkMenu = new Color(30,30,30),    lightMenu = new Color(44,62,80);
    private JPanel menuPanel;
    private final Color BTN_BLUE   = new Color(52,152,219), BTN_BLUE_H  = new Color(41,128,185);
    private final Color BTN_RED    = new Color(231,76,60),  BTN_RED_H   = new Color(192,57,43);
    private final Color BTN_GREEN  = new Color(39,174,96),  BTN_GREEN_H = new Color(30,140,75);
    private final Color BTN_DARK   = new Color(70,70,70),   BTN_DARK_H  = new Color(100,100,100);

    public AdminDashboard(String userId) {
        this.userId = userId;
        setTitle("Library Management System - Admin Dashboard");
        setSize(1200,800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JSplitPane split = new JSplitPane();
        split.setDividerLocation(250);
        menuPanel = createMenuPanel();
        split.setLeftComponent(menuPanel);
        contentPanel = new JPanel(new BorderLayout());
        split.setRightComponent(contentPanel);

        statusLabel = new JLabel("Welcome, Admin!");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        add(statusLabel, BorderLayout.SOUTH);
        add(split);

        showWelcomeMessage();
        loadPendingCount();
        applyTheme();
    }

    private JPanel createMenuPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(15,10,10,10));
        p.setBackground(lightMenu);

        JLabel lbl = new JLabel("Administrator");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lbl.setForeground(Color.WHITE);
        p.add(lbl);
        p.add(Box.createRigidArea(new Dimension(0,15)));

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(100,120,140));
        sep.setMaximumSize(new Dimension(230,2));
        p.add(sep);
        p.add(Box.createRigidArea(new Dimension(0,10)));

        for (String item : new String[]{"Dashboard Home","📷 ISBN Scanner","Manage Librarians",
                "View Reports","Fine Management","User Approvals","System Settings","Toggle Theme","Logout"}) {
            p.add(menuBtn(item));
            p.add(Box.createRigidArea(new Dimension(0,8)));
        }
        return p;
    }

    private JButton menuBtn(String text) {
        JButton b = new JButton(text);
        b.setMaximumSize(new Dimension(230,42)); b.setPreferredSize(new Dimension(230,42));
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setFocusPainted(false); b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR)); b.setForeground(Color.WHITE);

        Color nc = text.equals("Logout")       ? BTN_RED   :
                   text.equals("Toggle Theme") ? BTN_GREEN :
                   isDarkMode ? BTN_DARK : BTN_BLUE;
        Color hc = text.equals("Logout")       ? BTN_RED_H   :
                   text.equals("Toggle Theme") ? BTN_GREEN_H :
                   isDarkMode ? BTN_DARK_H : BTN_BLUE_H;
        b.setBackground(nc);
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { b.setBackground(hc); }
            public void mouseExited (java.awt.event.MouseEvent e) { b.setBackground(nc); }
        });
        b.addActionListener(e -> {
            switch(text) {
                case "Dashboard Home":    showWelcomeMessage();      break;
                case "📷 ISBN Scanner":  showPanel(new ISBNScannerPanel()); updateStatus("ISBN Scanner"); break;
                case "Manage Librarians":showPanel(new LibrarianManagementPanel()); updateStatus("Manage Librarians"); break;
                case "View Reports":     showPanel(new ReportsPanel()); updateStatus("Reports"); break;
                case "Fine Management":  showPanel(new FineManagementPanel()); updateStatus("Fine Management"); break;
                case "User Approvals":   showPanel(new UserApprovalPanel()); updateStatus("User Approvals"); break;
                case "System Settings":  showPanel(new SettingsPanel(userId)); updateStatus("Settings"); break;
                case "Toggle Theme":     toggleTheme(); break;
                case "Logout":           logout(); break;
            }
        });
        return b;
    }

    private void showPanel(JPanel p) { contentPanel.removeAll(); contentPanel.add(p); contentPanel.revalidate(); contentPanel.repaint(); }

    private void toggleTheme() { isDarkMode = !isDarkMode; applyTheme(); }

    private void applyTheme() {
        menuPanel.setBackground(isDarkMode ? darkMenu : lightMenu);
        contentPanel.setBackground(isDarkMode ? darkBg : lightBg);
        for (Component c : menuPanel.getComponents()) {
            if (c instanceof JButton) {
                JButton b = (JButton)c;
                b.setBackground(b.getText().equals("Logout") ? BTN_RED :
                    b.getText().equals("Toggle Theme") ? BTN_GREEN :
                    isDarkMode ? BTN_DARK : BTN_BLUE);
                b.setForeground(Color.WHITE);
            }
        }
        statusLabel.setForeground(isDarkMode ? Color.WHITE : Color.BLACK);
        SwingUtilities.updateComponentTreeUI(this);
    }

    private void showWelcomeMessage() {
        contentPanel.removeAll();
        JPanel wp = new JPanel(new GridBagLayout());
        wp.setBackground(isDarkMode ? darkBg : lightBg);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10,10,10,10);

        JLabel wl = new JLabel("Welcome to Admin Dashboard");
        wl.setFont(new Font("Segoe UI", Font.BOLD, 24));
        wl.setForeground(isDarkMode ? Color.WHITE : Color.BLACK);
        gbc.gridx = 0; gbc.gridy = 0; wp.add(wl, gbc);

        gbc.gridy = 1; wp.add(createStatsPanel(), gbc);
        contentPanel.add(wp); contentPanel.revalidate(); contentPanel.repaint();
        updateStatus("Welcome to Dashboard");
    }

    private JPanel createStatsPanel() {
        JPanel p = new JPanel(new GridLayout(2,2,20,20));
        p.setBackground(isDarkMode ? darkBg : lightBg);
        p.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
        p.add(statCard("Total Users",       count("users", null)));
        p.add(statCard("Total Books",       count("books", null)));
        p.add(statCard("Active Loans",      count("book_borrowings", Filters.eq("status","BORROWED"))));
        p.add(statCard("Pending Approvals", count("users", Filters.eq("is_approved", false))));
        return p;
    }

    private long count(String col, org.bson.conversions.Bson filter) {
        try {
            var c = DatabaseConnection.getCollection(col);
            return filter == null ? c.countDocuments() : c.countDocuments(filter);
        } catch(Exception e) { return 0; }
    }

    private JPanel statCard(String title, long value) {
        JPanel card = new JPanel(new BorderLayout(5,5));
        card.setBackground(isDarkMode ? new Color(45,45,45) : Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(isDarkMode ? new Color(60,60,60) : new Color(200,200,200)),
            BorderFactory.createEmptyBorder(15,15,15,15)));
        JLabel tl = new JLabel(title); tl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tl.setForeground(isDarkMode ? Color.WHITE : Color.BLACK);
        JLabel vl = new JLabel(String.valueOf(value)); vl.setFont(new Font("Segoe UI", Font.BOLD, 24));
        vl.setForeground(new Color(52,152,219));
        card.add(tl, BorderLayout.NORTH); card.add(vl, BorderLayout.CENTER);
        return card;
    }

    private void loadPendingCount() {
        long c = count("users", Filters.eq("is_approved", false));
        if (c > 0) updateStatus("You have " + c + " pending user approvals");
    }

    private void logout() {
        int c = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", "Confirm Logout", JOptionPane.YES_NO_OPTION);
        if (c == JOptionPane.YES_OPTION) { dispose(); new LoginScreen().setVisible(true); }
    }

    private void updateStatus(String msg) { statusLabel.setText("Status: " + msg); }
}