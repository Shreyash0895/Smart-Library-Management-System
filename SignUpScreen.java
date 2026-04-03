import javax.swing.*;
import java.awt.*;
import com.mongodb.client.*;
import org.bson.Document;

public class SignUpScreen extends JFrame {

    private JTextField username, email, fullName;
    private JPasswordField password;
    private JComboBox<String> roleBox;

    public SignUpScreen() {
        setTitle("Signup");
        setSize(400, 350);
        setLayout(new GridLayout(6, 2));

        username = new JTextField();
        password = new JPasswordField();
        email = new JTextField();
        fullName = new JTextField();
        roleBox = new JComboBox<>(new String[]{"STUDENT", "LIBRARIAN"});

        add(new JLabel("Username")); add(username);
        add(new JLabel("Password")); add(password);
        add(new JLabel("Email")); add(email);
        add(new JLabel("Full Name")); add(fullName);
        add(new JLabel("Role")); add(roleBox);

        JButton signup = new JButton("Sign Up");
        add(signup);

        signup.addActionListener(e -> register());

        setLocationRelativeTo(null);
    }

    private void register() {
        MongoCollection<Document> users = DatabaseConnection.getCollection("users");

        Document user = new Document()
                .append("username", username.getText())
                .append("password", new String(password.getPassword()))
                .append("email", email.getText())
                .append("full_name", fullName.getText())
                .append("role", roleBox.getSelectedItem().toString())
                .append("is_active", true)
                .append("is_approved", false);

        users.insertOne(user);

        JOptionPane.showMessageDialog(this, "Signup successful! Wait for approval.");
        dispose();
    }
}