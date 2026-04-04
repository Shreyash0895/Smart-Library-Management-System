import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import java.util.Date;

public class SignUpScreen extends JFrame {
    private JTextField usernameField, emailField, fullNameField;
    private JPasswordField passwordField, confirmPasswordField;
    private JComboBox<String> roleComboBox;

    public SignUpScreen() {
        setTitle("Library Management System - Sign Up");
        setSize(200, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("Create New Account");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(titleLabel, gbc);

        gbc.gridwidth = 1;

        gbc.gridx = 0; gbc.gridy = 1; mainPanel.add(new JLabel("Full Name:"), gbc);
        gbc.gridx = 1; fullNameField = new JTextField(20); mainPanel.add(fullNameField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; mainPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; usernameField = new JTextField(20); mainPanel.add(usernameField, gbc);

        gbc.gridx = 0; gbc.gridy = 3; mainPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; emailField = new JTextField(20); mainPanel.add(emailField, gbc);

        gbc.gridx = 0; gbc.gridy = 4; mainPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; passwordField = new JPasswordField(20); mainPanel.add(passwordField, gbc);

        gbc.gridx = 0; gbc.gridy = 5; mainPanel.add(new JLabel("Confirm Password:"), gbc);
        gbc.gridx = 1; confirmPasswordField = new JPasswordField(20); mainPanel.add(confirmPasswordField, gbc);

        gbc.gridx = 0; gbc.gridy = 6; mainPanel.add(new JLabel("Role:"), gbc);
        gbc.gridx = 1;
        roleComboBox = new JComboBox<>(new String[]{"Student", "Librarian"});
        mainPanel.add(roleComboBox, gbc);

        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2;
        JButton signUpButton = new JButton("Sign Up");
        signUpButton.addActionListener(e -> handleSignUp());
        mainPanel.add(signUpButton, gbc);

        gbc.gridy = 8;
        JButton backButton = new JButton("Back to Login");
        backButton.addActionListener(e -> { new LoginScreen().setVisible(true); dispose(); });
        mainPanel.add(backButton, gbc);

        add(mainPanel);
    }

    private void handleSignUp() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirm  = new String(confirmPasswordField.getPassword());
        String email    = emailField.getText().trim();
        String fullName = fullNameField.getText().trim();
        String role     = (String) roleComboBox.getSelectedItem();

        if (username.isEmpty()||password.isEmpty()||email.isEmpty()||fullName.isEmpty()) {
            JOptionPane.showMessageDialog(this,"All fields are required!"); return;
        }
        if (!password.equals(confirm)) {
            JOptionPane.showMessageDialog(this,"Passwords do not match!"); return;
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            JOptionPane.showMessageDialog(this,"Enter a valid email!"); return;
        }

        try {
            MongoCollection<Document> users = DatabaseConnection.getCollection("users");
            if (users == null) { JOptionPane.showMessageDialog(this,"Cannot connect to MongoDB!"); return; }
            if (users.find(Filters.eq("username",username)).first()!=null) {
                JOptionPane.showMessageDialog(this,"Username already exists!"); return;
            }
            if (users.find(Filters.eq("email",email)).first()!=null) {
                JOptionPane.showMessageDialog(this,"Email already registered!"); return;
            }

            users.insertOne(new Document()
                .append("full_name",fullName).append("username",username)
                .append("password",password).append("role",role.toUpperCase())
                .append("type",role.toUpperCase()).append("email",email)
                .append("is_approved",false).append("is_active",true)
                .append("created_at",new Date()));

            DatabaseConnection.getCollection("notifications").insertOne(new Document()
                .append("message","New "+role+" registration: "+username)
                .append("type","APPROVAL").append("is_read",false)
                .append("created_at",new Date()));

            JOptionPane.showMessageDialog(this,
                "Account created successfully!\nPlease wait for admin approval to login.",
                "Message", JOptionPane.INFORMATION_MESSAGE);
            new LoginScreen().setVisible(true);
            dispose();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,"Error creating account: "+e.getMessage());
        }
    }
}
