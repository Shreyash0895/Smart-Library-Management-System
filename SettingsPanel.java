import javax.swing.*;
import java.awt.*;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;

public class SettingsPanel extends JPanel {

    private String userId;
    private JComboBox<String> themeComboBox;
    private JCheckBox notificationsCheckBox;
    private JPasswordField currentPasswordField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;

    // Constructor with String userId
    public SettingsPanel(String userId) {
        this.userId = userId;
        setLayout(new BorderLayout());
        add(createSettingsPanel(), BorderLayout.CENTER);
        loadSettings();
    }

    private JPanel createSettingsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Theme:"), gbc);
        gbc.gridx = 1;
        themeComboBox = new JComboBox<>(new String[]{"Light", "Dark"});
        panel.add(themeComboBox, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Enable Notifications:"), gbc);
        gbc.gridx = 1;
        notificationsCheckBox = new JCheckBox();
        panel.add(notificationsCheckBox, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        panel.add(new JSeparator(), gbc);

        gbc.gridy = 3;
        panel.add(new JLabel("Change Password"), gbc);

        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("Current Password:"), gbc);
        gbc.gridx = 1;
        currentPasswordField = new JPasswordField(20);
        panel.add(currentPasswordField, gbc);

        gbc.gridx = 0; gbc.gridy = 5;
        panel.add(new JLabel("New Password:"), gbc);
        gbc.gridx = 1;
        newPasswordField = new JPasswordField(20);
        panel.add(newPasswordField, gbc);

        gbc.gridx = 0; gbc.gridy = 6;
        panel.add(new JLabel("Confirm Password:"), gbc);
        gbc.gridx = 1;
        confirmPasswordField = new JPasswordField(20);
        panel.add(confirmPasswordField, gbc);

        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2;
        JButton saveBtn = new JButton("Save Settings");
        saveBtn.setBackground(new Color(70, 130, 180));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFocusPainted(false);
        saveBtn.addActionListener(e -> saveSettings());
        panel.add(saveBtn, gbc);

        return panel;
    }

    private void loadSettings() {
        try {
            Document doc = DatabaseConnection.getCollection("settings")
                .find(Filters.eq("user_id", userId)).first();
            if (doc != null) {
                themeComboBox.setSelectedItem(doc.getString("theme"));
                notificationsCheckBox.setSelected(doc.getBoolean("notifications", false));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void saveSettings() {
        String theme         = (String) themeComboBox.getSelectedItem();
        boolean notifications = notificationsCheckBox.isSelected();
        String currentPassword = new String(currentPasswordField.getPassword());
        String newPassword     = new String(newPasswordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        try {
            // Save settings
            DatabaseConnection.getCollection("settings").replaceOne(
                Filters.eq("user_id", userId),
                new Document("user_id", userId)
                    .append("theme", theme)
                    .append("notifications", notifications),
                new com.mongodb.client.model.ReplaceOptions().upsert(true));

            // Change password if fields filled
            if (!currentPassword.isEmpty() && !newPassword.isEmpty()) {
                if (!newPassword.equals(confirmPassword)) {
                    JOptionPane.showMessageDialog(this, "New passwords do not match!"); return;
                }
                Document user = DatabaseConnection.getCollection("users").find(
                    Filters.and(
                        Filters.eq("_id", new ObjectId(userId)),
                        Filters.eq("password", currentPassword))).first();
                if (user != null) {
                    DatabaseConnection.getCollection("users").updateOne(
                        Filters.eq("_id", new ObjectId(userId)),
                        new Document("$set", new Document("password", newPassword)));
                    JOptionPane.showMessageDialog(this, "Password updated successfully!");
                    clearFields();
                } else {
                    JOptionPane.showMessageDialog(this, "Current password is incorrect!"); return;
                }
            }
            JOptionPane.showMessageDialog(this, "Settings saved successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving settings: " + e.getMessage());
        }
    }

    private void clearFields() {
        currentPasswordField.setText("");
        newPasswordField    .setText("");
        confirmPasswordField.setText("");
    }
}