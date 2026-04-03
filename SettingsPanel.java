import javax.swing.*;
import java.awt.*;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import org.bson.Document;

public class SettingsPanel extends JPanel {

    private String userId;

    private JComboBox<String> themeComboBox;
    private JCheckBox notificationsCheckBox;

    private JPasswordField currentPasswordField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;

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

        // Theme
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Theme:"), gbc);

        gbc.gridx = 1;
        themeComboBox = new JComboBox<>(new String[]{"Light", "Dark"});
        panel.add(themeComboBox, gbc);

        // Notifications
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Enable Notifications:"), gbc);

        gbc.gridx = 1;
        notificationsCheckBox = new JCheckBox();
        panel.add(notificationsCheckBox, gbc);

        // Separator
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        panel.add(new JSeparator(), gbc);

        gbc.gridy = 3;
        panel.add(new JLabel("Change Password"), gbc);

        // Current Password
        gbc.gridwidth = 1;
        gbc.gridy = 4;
        panel.add(new JLabel("Current Password:"), gbc);

        gbc.gridx = 1;
        currentPasswordField = new JPasswordField(20);
        panel.add(currentPasswordField, gbc);

        // New Password
        gbc.gridx = 0; gbc.gridy = 5;
        panel.add(new JLabel("New Password:"), gbc);

        gbc.gridx = 1;
        newPasswordField = new JPasswordField(20);
        panel.add(newPasswordField, gbc);

        // Confirm Password
        gbc.gridx = 0; gbc.gridy = 6;
        panel.add(new JLabel("Confirm Password:"), gbc);

        gbc.gridx = 1;
        confirmPasswordField = new JPasswordField(20);
        panel.add(confirmPasswordField, gbc);

        // Save Button
        gbc.gridx = 0; gbc.gridy = 7;
        gbc.gridwidth = 2;

        JButton saveBtn = new JButton("Save Settings");
        saveBtn.addActionListener(e -> saveSettings());

        panel.add(saveBtn, gbc);

        return panel;
    }

    // ================= LOAD SETTINGS =================
    private void loadSettings() {
        try {
            MongoCollection<Document> settings =
                    DatabaseConnection.getCollection("settings");

            Document userSettings = settings.find(
                    Filters.eq("user_id", userId)
            ).first();

            if (userSettings != null) {
                themeComboBox.setSelectedItem(userSettings.getString("theme"));
                notificationsCheckBox.setSelected(userSettings.getBoolean("notifications", false));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= SAVE SETTINGS =================
    private void saveSettings() {

        String theme = (String) themeComboBox.getSelectedItem();
        boolean notifications = notificationsCheckBox.isSelected();

        String currentPassword = new String(currentPasswordField.getPassword());
        String newPassword = new String(newPasswordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        try {
            MongoCollection<Document> settings =
                    DatabaseConnection.getCollection("settings");

            MongoCollection<Document> users =
                    DatabaseConnection.getCollection("users");

            // Save settings
            Document settingDoc = new Document("user_id", userId)
                    .append("theme", theme)
                    .append("notifications", notifications);

            settings.replaceOne(
                    Filters.eq("user_id", userId),
                    settingDoc,
                    new com.mongodb.client.model.ReplaceOptions().upsert(true)
            );

            // Password change
            if (!currentPassword.isEmpty() && !newPassword.isEmpty()) {

                if (!newPassword.equals(confirmPassword)) {
                    JOptionPane.showMessageDialog(this, "Passwords do not match!");
                    return;
                }

                Document user = users.find(
                        Filters.and(
                                Filters.eq("_id", new org.bson.types.ObjectId(userId)),
                                Filters.eq("password", currentPassword)
                        )
                ).first();

                if (user != null) {

                    users.updateOne(
                            Filters.eq("_id", new org.bson.types.ObjectId(userId)),
                            new Document("$set", new Document("password", newPassword))
                    );

                    JOptionPane.showMessageDialog(this, "Password updated!");
                    clearFields();

                } else {
                    JOptionPane.showMessageDialog(this, "Current password incorrect!");
                }
            }

            JOptionPane.showMessageDialog(this, "Settings saved!");

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving settings");
        }
    }

    private void clearFields() {
        currentPasswordField.setText("");
        newPasswordField.setText("");
        confirmPasswordField.setText("");
    }
}