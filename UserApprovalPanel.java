import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Date;
import com.mongodb.client.*;
import com.mongodb.client.model.*;
import org.bson.Document;
import org.bson.types.ObjectId;

public class UserApprovalPanel extends JPanel {
    private JTable table;
    private DefaultTableModel model;

    public UserApprovalPanel() {
        setLayout(new BorderLayout(10,10));
        setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        JLabel title = new JLabel("User Approvals", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        add(title, BorderLayout.NORTH);

        model = new DefaultTableModel(new String[]{"ID","Username","Full Name","Email","Role"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.setRowHeight(28);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton approveBtn = btn("✔ Approve", new Color(39,174,96));
        JButton rejectBtn  = btn("✘ Reject",  new Color(231,76,60));
        JButton refreshBtn = btn("⟳ Refresh", new Color(52,152,219));
        approveBtn.addActionListener(e -> handle(true));
        rejectBtn .addActionListener(e -> handle(false));
        refreshBtn.addActionListener(e -> load());
        btns.add(approveBtn); btns.add(rejectBtn); btns.add(refreshBtn);
        add(btns, BorderLayout.SOUTH);

        load();
    }

    private JButton btn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg); b.setForeground(Color.WHITE);
        b.setFocusPainted(false); b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        return b;
    }

    private void load() {
        model.setRowCount(0);
        try {
            for (Document d : DatabaseConnection.getCollection("users").find(Filters.eq("is_approved", false))) {
                model.addRow(new Object[]{
                    d.getObjectId("_id").toString(),
                    d.getString("username"), d.getString("full_name"),
                    d.getString("email"),    d.getString("role")
                });
            }
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Error: " + e.getMessage()); }
    }

    private void handle(boolean approve) {
        int row = table.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Please select a user"); return; }
        String id   = (String) model.getValueAt(row, 0);
        String name = (String) model.getValueAt(row, 1);
        try {
            DatabaseConnection.getCollection("users").updateOne(
                Filters.eq("_id", new ObjectId(id)),
                Updates.combine(Updates.set("is_approved", approve), Updates.set("is_active", approve)));
            DatabaseConnection.getCollection("notifications").insertOne(new Document()
                .append("user_id", id)
                .append("message", "Your account has been " + (approve ? "approved" : "rejected") + " by admin.")
                .append("is_read", false).append("created_at", new Date()));
            model.removeRow(row);
            JOptionPane.showMessageDialog(this, "User " + name + " " + (approve ? "approved" : "rejected") + "!");
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Error: " + e.getMessage()); }
    }
}