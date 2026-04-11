import javax.swing.*;
import java.awt.*;
import com.mongodb.client.*;
import org.bson.Document;

public class NotificationPanel extends JPanel {

    private String userId;
    private boolean isDarkMode;

    // ✅ MAIN CONSTRUCTOR
    public NotificationPanel(String userId, boolean isDarkMode) {
        this.userId = userId;
        this.isDarkMode = isDarkMode;

        setLayout(new BorderLayout());

        JLabel title = new JLabel("Notifications", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        add(title, BorderLayout.NORTH);

        JTextArea area = new JTextArea();
        area.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(area);
        add(scrollPane, BorderLayout.CENTER);

        loadNotifications(area);
    }

    // ✅ EXTRA CONSTRUCTOR (NO ERROR ANYWHERE)
    public NotificationPanel(String userId) {
        this(userId, false);
    }

    private void loadNotifications(JTextArea area) {
        try {
            MongoCollection<Document> collection =
                    DatabaseConnection.getCollection("notifications");

            StringBuilder sb = new StringBuilder();

            for (Document doc : collection.find()) {
                String msg = doc.getString("message");
                if (msg != null) sb.append(msg).append("\n\n");
            }

            if (sb.length() == 0) {
                sb.append("No notifications available.");
            }

            area.setText(sb.toString());

        } catch (Exception e) {
            area.setText("Error loading notifications: " + e.getMessage());
        }
    }
}