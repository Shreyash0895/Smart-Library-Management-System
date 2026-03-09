import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class SignUpScreen extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JTextField emailField;
    private JTextField fullNameField;
    private JComboBox<String> roleComboBox;

    public SignUpScreen() {
        setTitle("Library Management System - Sign Up");
        setSize(400, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("Create New Account");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;
        mainPanel.add(new JLabel("Full Name:"), gbc);
        gbc.gridx = 1;
        fullNameField = new JTextField(20);
        mainPanel.add(fullNameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        usernameField = new JTextField(20);
        mainPanel.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        mainPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        emailField = new JTextField(20);
        mainPanel.add(emailField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        mainPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        passwordField = new JPasswordField(20);
        mainPanel.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        mainPanel.add(new JLabel("Confirm Password:"), gbc);
        gbc.gridx = 1;
        confirmPasswordField = new JPasswordField(20);
        mainPanel.add(confirmPasswordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 6;
        mainPanel.add(new JLabel("Role:"), gbc);
        gbc.gridx = 1;
        String[] roles = {"Student", "Librarian"};
        roleComboBox = new JComboBox<>(roles);
        mainPanel.add(roleComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        JButton signUpButton = new JButton("Sign Up");
        signUpButton.addActionListener(e -> handleSignUp());
        mainPanel.add(signUpButton, gbc);

        gbc.gridy = 8;
        JButton backButton = new JButton("Back to Login");
        backButton.addActionListener(e -> {
            new LoginScreen().setVisible(true);
            this.dispose();
        });
        mainPanel.add(backButton, gbc);

        add(mainPanel);
    }

    private void handleSignUp() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        String email = emailField.getText().trim();
        String fullName = fullNameField.getText().trim();
        String role = (String) roleComboBox.getSelectedItem();

        if (username.isEmpty() || password.isEmpty() || email.isEmpty() || fullName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required!");
            return;
        }

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match!");
            return;
        }

        if (!isValidEmail(email)) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address!");
            return;
        }

        try {
            Connection conn = DatabaseConnection.getConnection();

            String checkQuery = "SELECT COUNT(*) FROM users WHERE username = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();
            rs.next();
            if (rs.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this, "Username already exists!");
                return;
            }

            checkQuery = "SELECT COUNT(*) FROM users WHERE email = ?";
            checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setString(1, email);
            rs = checkStmt.executeQuery();
            rs.next();
            if (rs.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this, "Email already registered!");
                return;
            }

            // INSERT with all 7 columns matching your DB: full_name, username, password, role, type, email, is_approved
            String sql = "INSERT INTO users (full_name, username, password, role, type, email, is_approved) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, fullName);             // full_name
            pstmt.setString(2, username);              // username
            pstmt.setString(3, password);              // password
            pstmt.setString(4, role.toUpperCase());    // role
            pstmt.setString(5, role.toUpperCase());    // type (same as role)
            pstmt.setString(6, email);                 // email
            pstmt.setBoolean(7, false);                // is_approved
            pstmt.executeUpdate();

            // Get the new user's ID to create a notification
            String getIdQuery = "SELECT id FROM users WHERE username = ?";
            PreparedStatement getIdStmt = conn.prepareStatement(getIdQuery);
            getIdStmt.setString(1, username);
            ResultSet idRs = getIdStmt.executeQuery();

            if (idRs.next()) {
                int newUserId = idRs.getInt("id");
                String notifyQuery = "INSERT INTO notifications (user_id, message, type) VALUES (?, ?, ?)";
                PreparedStatement notifyStmt = conn.prepareStatement(notifyQuery);
                notifyStmt.setInt(1, newUserId);
                notifyStmt.setString(2, "New " + role + " account registration: " + username);
                notifyStmt.setString(3, "APPROVAL");
                notifyStmt.executeUpdate();
            }

            JOptionPane.showMessageDialog(this,
                "Account created successfully!\nPlease wait for admin approval to login.");

            new LoginScreen().setVisible(true);
            this.dispose();

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error creating account: " + e.getMessage());
        }
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(emailRegex);
    }
}