import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import org.bson.Document;

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
        loginPanel = new RoundedPanel(20, new Color(255,255,255,220));
        loginPanel.setLayout(new GridBagLayout());
        loginPanel.setBorder(BorderFactory.createEmptyBorder(20,40,20,40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10,10,10,10);
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        JLabel titleLabel = new JLabel("Library Management System");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(44,62,80));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0; gbc.gridy = 0;
        loginPanel.add(titleLabel, gbc);

        // Username
        gbc.gridy++;
        JPanel usernamePanel = createInputPanel("Username:");
        usernameField = new JTextField(20);
        styleTextField(usernameField);
        usernamePanel.add(usernameField);
        loginPanel.add(usernamePanel, gbc);

        // Password
        gbc.gridy++;
        JPanel passwordPanel = createInputPanel("Password:");
        passwordField = new JPasswordField(20);
        styleTextField(passwordField);
        passwordPanel.add(passwordField);
        loginPanel.add(passwordPanel, gbc);

        // Role
        gbc.gridy++;
        JPanel rolePanel = createInputPanel("Role:");
        String[] roles = {"Admin","Librarian","Student"};
        roleComboBox = new JComboBox<>(roles);
        styleComboBox(roleComboBox);
        rolePanel.add(roleComboBox);
        loginPanel.add(rolePanel, gbc);

        // Login Button
        gbc.gridy++;
        gbc.insets = new Insets(20,0,10,0);
        CustomButton loginButton = new CustomButton("LOGIN", new Color(41,128,185), false);
        loginButton.addActionListener(e -> handleLogin());
        passwordField.addActionListener(e -> handleLogin());
        loginPanel.add(loginButton, gbc);

        // Sign Up Button
        gbc.gridy++;
        gbc.insets = new Insets(0,0,10,0);
        CustomButton signUpButton = new CustomButton("Create New Account", new Color(46,204,113), false);
        signUpButton.addActionListener(e -> { new SignUpScreen().setVisible(true); dispose(); });
        loginPanel.add(signUpButton, gbc);
    }

    private JPanel createInputPanel(String labelText) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT,10,5));
        panel.setOpaque(false);
        JLabel label = new JLabel(labelText);
        label.setForeground(new Color(44,62,80));
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(label);
        return panel;
    }

    private void styleTextField(JTextField textField) {
        textField.setPreferredSize(new Dimension(250,35));
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        textField.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(189,195,199),1,true),
            BorderFactory.createEmptyBorder(5,10,5,10)));
        textField.setBackground(Color.WHITE);
    }

    private void styleComboBox(JComboBox<String> c) {
        c.setPreferredSize(new Dimension(250,35));
        c.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        c.setBorder(new LineBorder(new Color(189,195,199),1,true));
        c.setBackground(Color.WHITE);
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String role     = (String) roleComboBox.getSelectedItem();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password!"); return;
        }

        try {
            MongoCollection<Document> users = DatabaseConnection.getCollection("users");
            if (users == null) { showError("Cannot connect to MongoDB.\nMake sure MongoDB is running!"); return; }

            Document user = users.find(Filters.and(
                Filters.eq("username", username),
                Filters.eq("password", password),
                Filters.eq("role", role.toUpperCase())
            )).first();

            if (user == null) { showError("Invalid credentials!"); return; }

            Boolean approved = user.getBoolean("is_approved");
            if (approved == null || !approved) {
                showError("Your account is pending admin approval.\nPlease wait for approval."); return;
            }

            String userId = user.getObjectId("_id").toString();
            dispose();
            switch (role.toUpperCase()) {
                case "ADMIN":     new AdminDashboard(userId).setVisible(true);     break;
                case "LIBRARIAN": new LibrarianDashboard(userId).setVisible(true); break;
                case "STUDENT":   new StudentDashboard(userId).setVisible(true);   break;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Database error: " + ex.getMessage());
        }
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
        SwingUtilities.invokeLater(() -> new LoginScreen().setVisible(true));
    }
}