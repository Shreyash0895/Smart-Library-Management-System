import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;

public class FineManagementPanel extends JPanel {

    private JTable fineTable;
    private DefaultTableModel tableModel;
    private JTextField studentIdField;
    private JLabel totalFinesLabel;

    public FineManagementPanel() {

        setLayout(new BorderLayout());

        add(createSearchPanel(), BorderLayout.NORTH);

        createTable();
        add(new JScrollPane(fineTable), BorderLayout.CENTER);

        add(createBottomPanel(), BorderLayout.SOUTH);

        loadAllFines();
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel();

        studentIdField = new JTextField(15);
        JButton searchBtn = new JButton("Search");

        searchBtn.addActionListener(e -> searchFines());

        panel.add(new JLabel("Student ID:"));
        panel.add(studentIdField);
        panel.add(searchBtn);

        return panel;
    }

    private void createTable() {
        String[] cols = {"Fine ID", "Student", "Book", "Days Late", "Amount", "Status"};

        tableModel = new DefaultTableModel(cols, 0);
        fineTable = new JTable(tableModel);
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        totalFinesLabel = new JLabel("Total: ₹0");
        panel.add(totalFinesLabel, BorderLayout.WEST);

        JButton payBtn = new JButton("Pay");
        JButton waiveBtn = new JButton("Waive");

        payBtn.addActionListener(e -> recordPayment());
        waiveBtn.addActionListener(e -> waiveFine());

        JPanel btnPanel = new JPanel();
        btnPanel.add(payBtn);
        btnPanel.add(waiveBtn);

        panel.add(btnPanel, BorderLayout.EAST);

        return panel;
    }

    // ================= LOAD =================
    private void loadAllFines() {

        tableModel.setRowCount(0);

        MongoCollection<Document> fines =
                DatabaseConnection.getCollection("fines");

        MongoCollection<Document> users =
                DatabaseConnection.getCollection("users");

        MongoCollection<Document> books =
                DatabaseConnection.getCollection("books");

        double total = 0;

        for (Document fine : fines.find()) {

            String userId = fine.getString("user_id");
            String bookId = fine.getString("book_id");

            Document user = users.find(
                    Filters.eq("_id", new ObjectId(userId))
            ).first();

            Document book = books.find(
                    Filters.eq("_id", new ObjectId(bookId))
            ).first();

            String name = (user != null) ? user.getString("full_name") : "Unknown";
            String title = (book != null) ? book.getString("title") : "Unknown";

            double amount = fine.getDouble("amount");
            String status = fine.getString("status");

            tableModel.addRow(new Object[]{
                    fine.getObjectId("_id").toString(),
                    name,
                    title,
                    fine.getInteger("days_late", 0),
                    "₹" + amount,
                    status
            });

            if ("PENDING".equals(status)) {
                total += amount;
            }
        }

        updateTotal(total);
    }

    // ================= SEARCH =================
    private void searchFines() {

        String studentId = studentIdField.getText().trim();

        if (studentId.isEmpty()) {
            loadAllFines();
            return;
        }

        tableModel.setRowCount(0);

        MongoCollection<Document> fines =
                DatabaseConnection.getCollection("fines");

        for (Document fine : fines.find(Filters.eq("user_id", studentId))) {

            tableModel.addRow(new Object[]{
                    fine.getObjectId("_id").toString(),
                    studentId,
                    fine.getString("book_id"),
                    fine.getInteger("days_late", 0),
                    "₹" + fine.getDouble("amount"),
                    fine.getString("status")
            });
        }
    }

    // ================= PAY =================
    private void recordPayment() {

        int row = fineTable.getSelectedRow();
        if (row < 0) return;

        String fineId = (String) tableModel.getValueAt(row, 0);

        MongoCollection<Document> fines =
                DatabaseConnection.getCollection("fines");

        fines.updateOne(
                Filters.eq("_id", new ObjectId(fineId)),
                new Document("$set", new Document("status", "PAID"))
        );

        JOptionPane.showMessageDialog(this, "Payment recorded!");
        loadAllFines();
    }

    // ================= WAIVE =================
    private void waiveFine() {

        int row = fineTable.getSelectedRow();
        if (row < 0) return;

        String fineId = (String) tableModel.getValueAt(row, 0);

        MongoCollection<Document> fines =
                DatabaseConnection.getCollection("fines");

        fines.updateOne(
                Filters.eq("_id", new ObjectId(fineId)),
                new Document("$set", new Document("status", "WAIVED"))
        );

        JOptionPane.showMessageDialog(this, "Fine waived!");
        loadAllFines();
    }

    private void updateTotal(double total) {
        totalFinesLabel.setText("Total Outstanding: ₹" + total);
    }
}