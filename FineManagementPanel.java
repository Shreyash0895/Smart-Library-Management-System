import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Date;
import com.mongodb.client.*;
import com.mongodb.client.model.*;
import org.bson.Document;
import org.bson.types.ObjectId;

public class FineManagementPanel extends JPanel {
    private JTable fineTable;
    private DefaultTableModel tableModel;
    private JTextField studentSearchField;
    private JLabel totalFinesLabel;

    public FineManagementPanel() {
        setLayout(new BorderLayout());
        add(createSearchPanel(), BorderLayout.NORTH);
        createFineTable();
        add(new JScrollPane(fineTable), BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);
        loadAllFines();
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel("Student Username:"));
        studentSearchField = new JTextField(15);
        panel.add(studentSearchField);
        JButton searchBtn = new JButton("Search");
        searchBtn.addActionListener(e -> searchFines());
        JButton allBtn = new JButton("Show All");
        allBtn.addActionListener(e -> loadAllFines());
        panel.add(searchBtn);
        panel.add(allBtn);
        return panel;
    }

    private void createFineTable() {
        String[] columns = {"Fine ID", "Student Name", "Book Title", "Due Date", "Fine Amount", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        fineTable = new JTable(tableModel);
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        totalFinesLabel = new JLabel("Total Outstanding Fines: $0.00");
        totalFinesLabel.setFont(new Font("Arial", Font.BOLD, 14));
        totalFinesLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        panel.add(totalFinesLabel, BorderLayout.WEST);

        JPanel btns = new JPanel();
        JButton payBtn   = new JButton("Record Payment");
        JButton waiveBtn = new JButton("Waive Fine");
        payBtn  .addActionListener(e -> recordPayment());
        waiveBtn.addActionListener(e -> waiveFine());
        btns.add(payBtn); btns.add(waiveBtn);
        panel.add(btns, BorderLayout.EAST);
        return panel;
    }

    private void loadAllFines() {
        tableModel.setRowCount(0);
        double total = 0;
        try {
            MongoCollection<Document> fines = DatabaseConnection.getCollection("fines");
            MongoCollection<Document> users = DatabaseConnection.getCollection("users");
            MongoCollection<Document> books = DatabaseConnection.getCollection("books");

            for (Document f : fines.find()) {
                String uId   = f.getString("user_id");
                String bId   = f.getString("book_id");
                String uName = "", bTitle = "";
                try {
                    Document u = users.find(Filters.eq("_id", new ObjectId(uId))).first();
                    if (u != null) uName = u.getString("full_name");
                    Document b = books.find(Filters.eq("_id", new ObjectId(bId))).first();
                    if (b != null) bTitle = b.getString("title");
                } catch (Exception ignored) {}

                double amt    = f.getDouble("amount") != null ? f.getDouble("amount") : 0;
                String status = f.getString("status") != null ? f.getString("status") : "PENDING";
                tableModel.addRow(new Object[]{
                    f.getObjectId("_id").toString(),
                    uName, bTitle,
                    f.getDate("due_date"),
                    String.format("$%.2f", amt),
                    status
                });
                if ("PENDING".equals(status)) total += amt;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading fines: " + e.getMessage());
        }
        totalFinesLabel.setText(String.format("Total Outstanding Fines: $%.2f", total));
    }

    private void searchFines() {
        String username = studentSearchField.getText().trim();
        if (username.isEmpty()) { loadAllFines(); return; }
        tableModel.setRowCount(0);
        double total = 0;
        try {
            MongoCollection<Document> users = DatabaseConnection.getCollection("users");
            MongoCollection<Document> fines = DatabaseConnection.getCollection("fines");
            MongoCollection<Document> books = DatabaseConnection.getCollection("books");

            Document user = users.find(Filters.eq("username", username)).first();
            if (user == null) { JOptionPane.showMessageDialog(this, "Student not found"); return; }
            String userId = user.getObjectId("_id").toString();

            for (Document f : fines.find(Filters.eq("user_id", userId))) {
                String bId = f.getString("book_id");
                String bTitle = "";
                try {
                    Document b = books.find(Filters.eq("_id", new ObjectId(bId))).first();
                    if (b != null) bTitle = b.getString("title");
                } catch (Exception ignored) {}

                double amt    = f.getDouble("amount") != null ? f.getDouble("amount") : 0;
                String status = f.getString("status") != null ? f.getString("status") : "PENDING";
                tableModel.addRow(new Object[]{
                    f.getObjectId("_id").toString(),
                    user.getString("full_name"), bTitle,
                    f.getDate("due_date"),
                    String.format("$%.2f", amt), status
                });
                if ("PENDING".equals(status)) total += amt;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error searching fines: " + e.getMessage());
        }
        totalFinesLabel.setText(String.format("Total Outstanding Fines: $%.2f", total));
    }

    private void recordPayment() {
        int row = fineTable.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Please select a fine"); return; }
        if (!"PENDING".equals(tableModel.getValueAt(row, 5))) {
            JOptionPane.showMessageDialog(this, "Can only process pending fines"); return;
        }
        try {
            String id = (String) tableModel.getValueAt(row, 0);
            DatabaseConnection.getCollection("fines").updateOne(
                Filters.eq("_id", new ObjectId(id)),
                Updates.combine(Updates.set("status", "PAID"), Updates.set("paid_at", new Date())));
            JOptionPane.showMessageDialog(this, "Payment recorded successfully!");
            loadAllFines();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error recording payment: " + e.getMessage());
        }
    }

    private void waiveFine() {
        int row = fineTable.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Please select a fine"); return; }
        if (!"PENDING".equals(tableModel.getValueAt(row, 5))) {
            JOptionPane.showMessageDialog(this, "Can only waive pending fines"); return;
        }
        int c = JOptionPane.showConfirmDialog(this, "Waive this fine?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (c == JOptionPane.YES_OPTION) {
            try {
                String id = (String) tableModel.getValueAt(row, 0);
                DatabaseConnection.getCollection("fines").updateOne(
                    Filters.eq("_id", new ObjectId(id)),
                    Updates.combine(Updates.set("status", "WAIVED"), Updates.set("paid_at", new Date())));
                JOptionPane.showMessageDialog(this, "Fine waived successfully!");
                loadAllFines();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error waiving fine: " + e.getMessage());
            }
        }
    }
}