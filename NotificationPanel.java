import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import com.mongodb.client.*;
import com.mongodb.client.model.*;
import org.bson.Document;
import org.bson.types.ObjectId;

public class NotificationPanel extends JPanel {
    private String userId;
    private boolean isDarkMode;
    private Color darkBackground  = new Color(33, 33, 33);
    private Color lightBackground = new Color(242, 242, 242);
    private DefaultTableModel tableModel;
    private JTable notificationsTable;
    private JLabel titleLabel;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public NotificationPanel(String userId, boolean isDarkMode) {
        this.userId    = userId;
        this.isDarkMode = isDarkMode;
        setLayout(new BorderLayout(10, 10));
        setBackground(isDarkMode ? darkBackground : lightBackground);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        createComponents();
        loadNotifications();
    }

    private void createComponents() {
        titleLabel = new JLabel("Notifications", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(isDarkMode ? Color.WHITE : Color.BLACK);
        add(titleLabel, BorderLayout.NORTH);

        String[] columns = {"Date", "Message", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        notificationsTable = new JTable(tableModel);
        notificationsTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        notificationsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        notificationsTable.setRowHeight(25);
        notificationsTable.getColumnModel().getColumn(0).setPreferredWidth(150);
        notificationsTable.getColumnModel().getColumn(1).setPreferredWidth(400);
        notificationsTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        add(new JScrollPane(notificationsTable), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnPanel.setBackground(isDarkMode ? darkBackground : lightBackground);
        JButton markReadBtn = new JButton("Mark as Read");
        JButton deleteBtn   = new JButton("Delete");
        JButton refreshBtn  = new JButton("Refresh");
        for (JButton b : new JButton[]{markReadBtn, deleteBtn, refreshBtn}) {
            b.setBackground(new Color(70, 130, 180));
            b.setForeground(Color.WHITE);
            b.setFocusPainted(false);
            b.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            btnPanel.add(b);
        }
        markReadBtn.addActionListener(e -> markAsRead());
        deleteBtn  .addActionListener(e -> deleteNotification());
        refreshBtn .addActionListener(e -> loadNotifications());
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void loadNotifications() {
        tableModel.setRowCount(0);
        try {
            MongoCollection<Document> notifs = DatabaseConnection.getCollection("notifications");
            for (Document n : notifs.find(Filters.eq("user_id", userId))
                    .sort(new Document("created_at", -1))) {
                java.util.Date date = n.getDate("created_at");
                tableModel.addRow(new Object[]{
                    date != null ? dateFormat.format(date) : "",
                    n.getString("message"),
                    Boolean.TRUE.equals(n.getBoolean("is_read")) ? "READ" : "UNREAD"
                });
            }
            updateUnreadCount();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading notifications: " + ex.getMessage());
        }
    }

    private void markAsRead() {
        int row = notificationsTable.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Please select a notification"); return; }
        try {
            String msg = tableModel.getValueAt(row, 1).toString();
            DatabaseConnection.getCollection("notifications").updateOne(
                Filters.and(Filters.eq("user_id", userId), Filters.eq("message", msg)),
                Updates.set("is_read", true));
            tableModel.setValueAt("READ", row, 2);
            updateUnreadCount();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void deleteNotification() {
        int row = notificationsTable.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Please select a notification"); return; }
        int c = JOptionPane.showConfirmDialog(this, "Delete this notification?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (c == JOptionPane.YES_OPTION) {
            try {
                String msg = tableModel.getValueAt(row, 1).toString();
                DatabaseConnection.getCollection("notifications").deleteOne(
                    Filters.and(Filters.eq("user_id", userId), Filters.eq("message", msg)));
                tableModel.removeRow(row);
                updateUnreadCount();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }
    }

    private void updateUnreadCount() {
        try {
            long unread = DatabaseConnection.getCollection("notifications")
                .countDocuments(Filters.and(
                    Filters.eq("user_id", userId), Filters.eq("is_read", false)));
            titleLabel.setText(unread > 0 ? "Notifications (" + unread + " unread)" : "Notifications");
        } catch (Exception ex) { ex.printStackTrace(); }
    }
}