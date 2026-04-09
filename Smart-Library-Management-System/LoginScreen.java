import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import org.bson.Document;

public class LoginScreen extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> roleComboBox;

    public LoginScreen() {
        setTitle("Library Management System");
        setSize(500, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new GridBagLayout()) {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setPaint(new GradientPaint(0, 0, new Color(41, 128, 185),
                        0, getHeight(), new Color(44, 62, 80)));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        JPanel loginPanel = new JPanel(new GridBagLayout());
        loginPanel.setBackground(Color.WHITE);
        loginPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 2;

        JLabel title = new JLabel("Library Management System", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        gbc.gridy = 0;
        loginPanel.add(title, gbc);

        gbc.gridy = 1;
        loginPanel.add(new JLabel("Username:"), gbc);

        gbc.gridy = 2;
        usernameField = new JTextField();
        loginPanel.add(usernameField, gbc);

        gbc.gridy = 3;
        loginPanel.add(new JLabel("Password:"), gbc);

        gbc.gridy = 4;
        passwordField = new JPasswordField();
        loginPanel.add(passwordField, gbc);

        gbc.gridy = 5;
        loginPanel.add(new JLabel("Role:"), gbc);

        gbc.gridy = 6;
        roleComboBox = new JComboBox<>(new String[]{"ADMIN", "LIBRARIAN", "STUDENT"});
        loginPanel.add(roleComboBox, gbc);

        gbc.gridy = 7;
        JButton loginBtn = new JButton("LOGIN");
        loginBtn.addActionListener(e -> handleLogin());
        loginPanel.add(loginBtn, gbc);

        gbc.gridy = 8;
        JButton signupBtn = new JButton("Create Account");
        signupBtn.addActionListener(e -> {
            new SignUpScreen().setVisible(true);
            dispose();
        });
        loginPanel.add(signupBtn, gbc);

        mainPanel.add(loginPanel);
        add(mainPanel);
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String role = roleComboBox.getSelectedItem().toString();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter all fields!");
            return;
        }

        try {
            MongoCollection<Document> users = DatabaseConnection.getCollection("users");

            Document user = users.find(Filters.and(
                    Filters.eq("username", username),
                    Filters.eq("password", password)
            )).first();

            if (user == null) {
                JOptionPane.showMessageDialog(this, "Invalid Credentials!");
                return;
            }

            // ✅ FIXED FIELD NAME
            Boolean approved = user.getBoolean("isApproved", true);
            if (!approved) {
                JOptionPane.showMessageDialog(this, "Account not approved!");
                return;
            }

            String dbRole = user.getString("role");

            // ✅ ROLE CHECK FIX
            if (!dbRole.equalsIgnoreCase(role)) {
                JOptionPane.showMessageDialog(this, "Wrong role selected!");
                return;
            }

            // ✅ SAFE ID FETCH
            String userId = user.get("_id").toString();

            JOptionPane.showMessageDialog(this, "Login Successful!");

            dispose();

            switch (dbRole.toUpperCase()) {
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

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginScreen().setVisible(true));
    }
}