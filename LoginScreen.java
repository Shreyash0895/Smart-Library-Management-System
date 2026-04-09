import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import org.bson.Document;

public class LoginScreen extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> roleBox;

    public LoginScreen() {
        setTitle("Library Management System");
        setSize(500, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Main gradient panel
        JPanel mainPanel = new JPanel(new GridBagLayout()) {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setPaint(new GradientPaint(0, 0, new Color(41, 128, 185),
                    0, getHeight(), new Color(109, 213, 250)));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        // Login card
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(new Color(255, 255, 255, 230));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
            BorderFactory.createEmptyBorder(30, 40, 30, 40)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets  = new Insets(10, 10, 10, 10);
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 2;

        // Title
        JLabel titleLabel = new JLabel("Library Management System", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(new Color(44, 62, 80));
        gbc.gridx = 0; gbc.gridy = 0;
        card.add(titleLabel, gbc);

        // Username
        gbc.gridy = 1;
        JPanel usernamePanel = new JPanel(new BorderLayout(5, 0));
        usernamePanel.setOpaque(false);
        JLabel userLbl = new JLabel("Username:");
        userLbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        userLbl.setForeground(new Color(44, 62, 80));
        usernameField = new JTextField(20);
        styleField(usernameField);
        usernamePanel.add(userLbl, BorderLayout.NORTH);
        usernamePanel.add(usernameField, BorderLayout.CENTER);
        card.add(usernamePanel, gbc);

        // Password
        gbc.gridy = 2;
        JPanel passwordPanel = new JPanel(new BorderLayout(5, 0));
        passwordPanel.setOpaque(false);
        JLabel passLbl = new JLabel("Password:");
        passLbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passLbl.setForeground(new Color(44, 62, 80));
        passwordField = new JPasswordField(20);
        styleField(passwordField);
        passwordPanel.add(passLbl, BorderLayout.NORTH);
        passwordPanel.add(passwordField, BorderLayout.CENTER);
        card.add(passwordPanel, gbc);

        // Role
        gbc.gridy = 3;
        JPanel rolePanel = new JPanel(new BorderLayout(5, 0));
        rolePanel.setOpaque(false);
        JLabel roleLbl = new JLabel("Role:");
        roleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        roleLbl.setForeground(new Color(44, 62, 80));
        roleBox = new JComboBox<>(new String[]{"ADMIN", "LIBRARIAN", "STUDENT"});
        roleBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        roleBox.setPreferredSize(new Dimension(250, 38));
        rolePanel.add(roleLbl, BorderLayout.NORTH);
        rolePanel.add(roleBox, BorderLayout.CENTER);
        card.add(rolePanel, gbc);

        // Login button
        gbc.gridy = 4;
        gbc.insets = new Insets(20, 10, 5, 10);
        JButton loginBtn = new JButton("LOGIN");
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        loginBtn.setBackground(new Color(41, 128, 185));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFocusPainted(false);
        loginBtn.setBorderPainted(false);
        loginBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginBtn.setPreferredSize(new Dimension(250, 42));
        loginBtn.addActionListener(e -> login());
        // Allow pressing Enter to login
        passwordField.addActionListener(e -> login());
        card.add(loginBtn, gbc);

        // Sign up button
        gbc.gridy = 5;
        gbc.insets = new Insets(5, 10, 10, 10);
        JButton signUpBtn = new JButton("Create New Account");
        signUpBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        signUpBtn.setBackground(new Color(46, 204, 113));
        signUpBtn.setForeground(Color.WHITE);
        signUpBtn.setFocusPainted(false);
        signUpBtn.setBorderPainted(false);
        signUpBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        signUpBtn.setPreferredSize(new Dimension(250, 42));
        signUpBtn.addActionListener(e -> { new SignUpScreen().setVisible(true); dispose(); });
        card.add(signUpBtn, gbc);

        mainPanel.add(card);
        add(mainPanel);
    }

    private void styleField(JTextField f) {
        f.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        f.setPreferredSize(new Dimension(250, 38));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1, true),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        f.setBackground(Color.WHITE);
    }

    private void login() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String role     = roleBox.getSelectedItem().toString();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter username and password!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            MongoCollection<Document> users = DatabaseConnection.getCollection("users");
            if (users == null) {
                JOptionPane.showMessageDialog(this, "Cannot connect to MongoDB!\nMake sure MongoDB is running.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Find user with matching credentials and role
            Document user = users.find(Filters.and(
                Filters.eq("username", username),
                Filters.eq("password", password),
                Filters.eq("role",     role)
            )).first();

            if (user == null) {
                JOptionPane.showMessageDialog(this, "Invalid username or password!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Check if approved
            Boolean isApproved = user.getBoolean("is_approved");
            if (isApproved == null || !isApproved) {
                JOptionPane.showMessageDialog(this,
                    "Your account is pending admin approval.\nPlease wait for approval.",
                    "Not Approved", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String userId = user.getObjectId("_id").toString();
            JOptionPane.showMessageDialog(this, "Login successful!", "Welcome", JOptionPane.INFORMATION_MESSAGE);

            dispose();
            switch (role) {
                case "ADMIN":     new AdminDashboard(userId).setVisible(true);     break;
                case "LIBRARIAN": new LibrarianDashboard(userId).setVisible(true); break;
                case "STUDENT":   new StudentDashboard(userId).setVisible(true);   break;
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
        SwingUtilities.invokeLater(() -> new LoginScreen().setVisible(true));
    }
}