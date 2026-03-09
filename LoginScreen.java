import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.sql.*;

public class LoginScreen extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> roleComboBox;
    private RoundedPanel loginPanel;
    private GradientPanel mainPanel;

    public LoginScreen() {
        setTitle("Library Management System");
        setSize(500, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        mainPanel = new GradientPanel();
        mainPanel.setLayout(new GridBagLayout());

        createLoginPanel();

        mainPanel.add(loginPanel);
        add(mainPanel);
    }

    private void createLoginPanel() {
        loginPanel = new RoundedPanel(20, new Color(255, 255, 255, 220));
        loginPanel.setLayout(new GridBagLayout());
        loginPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel titleLabel = createTitleLabel();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        loginPanel.add(titleLabel, gbc);

        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JPanel usernamePanel = createInputPanel("Username", "user.png");
        usernameField = new JTextField(20);
        styleTextField(usernameField);
        usernamePanel.add(usernameField);
        loginPanel.add(usernamePanel, gbc);

        gbc.gridy++;
        JPanel passwordPanel = createInputPanel("Password", "lock.png");
        passwordField = new JPasswordField(20);
        styleTextField(passwordField);
        passwordPanel.add(passwordField);
        loginPanel.add(passwordPanel, gbc);

        gbc.gridy++;
        JPanel rolePanel = createInputPanel("Role", "role.png");
        String[] roles = {"Admin", "Librarian", "Student"};
        roleComboBox = new JComboBox<>(roles);
        styleComboBox(roleComboBox);
        rolePanel.add(roleComboBox);
        loginPanel.add(rolePanel, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(20, 0, 10, 0);
        CustomButton loginButton = new CustomButton("LOGIN", new Color(41, 128, 185), false);
        loginButton.addActionListener(e -> handleLogin());
        loginPanel.add(loginButton, gbc);

        gbc.gridy++;
        CustomButton signUpButton = new CustomButton("Create New Account", new Color(46, 204, 113), false);
        signUpButton.addActionListener(e -> openSignUpScreen());
        loginPanel.add(signUpButton, gbc);
    }

    private JLabel createTitleLabel() {
        JLabel titleLabel = new JLabel("Library Management System");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(44, 62, 80));
        return titleLabel;
    }

    private JPanel createInputPanel(String labelText, String iconName) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panel.setOpaque(false);
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("/icons/" + iconName));
            Image img = icon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
            JLabel iconLabel = new JLabel(new ImageIcon(img));
            panel.add(iconLabel);
        } catch (Exception e) {
            JLabel label = new JLabel(labelText + ":");
            label.setForeground(new Color(44, 62, 80));
            label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            panel.add(label);
        }
        return panel;
    }

    private void styleTextField(JTextField textField) {
        textField.setPreferredSize(new Dimension(250, 35));
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        textField.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(189, 195, 199), 1, true),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        textField.setBackground(new Color(255, 255, 255));
    }

    private void styleComboBox(JComboBox<String> comboBox) {
        comboBox.setPreferredSize(new Dimension(250, 35));
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        comboBox.setBorder(new LineBorder(new Color(189, 195, 199), 1, true));
        comboBox.setBackground(Color.WHITE);
    }

    private void handleLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        String role = (String) roleComboBox.getSelectedItem();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password!");
            return;
        }

        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn == null) {
                showError("Cannot connect to database.\nPlease make sure MySQL server is running.");
                return;
            }
            String query = "SELECT * FROM users WHERE username = ? AND password = ? AND role = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, role.toUpperCase());

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int userId = rs.getInt("id");  // FIXED: was "user_id", now "id"
                showSuccess("Login successful!");
                openDashboard(userId, role);
            } else {
                showError("Invalid credentials or account not activated!");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            showError("Database error occurred: " + ex.getMessage());
        }
    }

    private void openSignUpScreen() {
        this.dispose();
        new SignUpScreen().setVisible(true);
    }

    private void openDashboard(int userId, String role) {
        this.dispose();
        switch (role.toUpperCase()) {
            case "ADMIN":
                new AdminDashboard(userId).setVisible(true);
                break;
            case "LIBRARIAN":
                new LibrarianDashboard(userId).setVisible(true);
                break;
            case "STUDENT":
                new StudentDashboard(userId).setVisible(true);
                break;
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> {
            new LoginScreen().setVisible(true);
        });
    }
}