import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Date;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;

public class StatusPanel extends JPanel {
    private String userId;
    private JTable borrowingsTable;
    private DefaultTableModel tableModel;
    private JLabel fineLabel;

    public StatusPanel(String userId) {
        this.userId = userId;
        setLayout(new BorderLayout());
        add(createHeaderPanel(), BorderLayout.NORTH);
        createBorrowingsTable();
        add(new JScrollPane(borrowingsTable), BorderLayout.CENTER);
        loadBorrowings();
        updateFineAmount();
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        fineLabel = new JLabel("Total Outstanding Fines: ₹0.0");
        fineLabel.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(fineLabel);
        return panel;
    }

    private void createBorrowingsTable() {
        String[] columns = {"Book Title", "Borrow Date", "Due Date", "Status", "Fine"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        borrowingsTable = new JTable(tableModel);
        borrowingsTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        borrowingsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        borrowingsTable.setRowHeight(25);
    }

    private void loadBorrowings() {
        tableModel.setRowCount(0);
        try {
            MongoCollection<Document> borrowings = DatabaseConnection.getCollection("book_borrowings");
            MongoCollection<Document> books      = DatabaseConnection.getCollection("books");

            for (Document doc : borrowings.find(Filters.eq("user_id", userId))) {
                String title = doc.getString("title");
                if (title == null || title.isEmpty()) {
                    try {
                        String bookId = doc.getString("book_id");
                        if (bookId != null) {
                            Document book = books.find(Filters.eq("_id", new ObjectId(bookId))).first();
                            if (book != null) title = book.getString("title");
                        }
                    } catch (Exception ignored) {}
                }

                Date borrowDate = doc.getDate("borrow_date");
                Date dueDate    = doc.getDate("due_date");

                // Safe fine amount extraction
                double fineAmt = getDoubleValue(doc, "fine_amount");

                tableModel.addRow(new Object[]{
                    title != null ? title : "Unknown",
                    borrowDate, dueDate,
                    doc.getString("status"),
                    "₹" + String.format("%.2f", fineAmt)
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading borrowing history: " + e.getMessage());
        }
    }

    private void updateFineAmount() {
        double totalFine = 0;
        try {
            MongoCollection<Document> borrowings = DatabaseConnection.getCollection("book_borrowings");
            for (Document doc : borrowings.find(Filters.and(
                    Filters.eq("user_id", userId), Filters.eq("fine_paid", false)))) {
                totalFine += getDoubleValue(doc, "fine_amount");
            }
            fineLabel.setText("Total Outstanding Fines: ₹" + String.format("%.2f", totalFine));
        } catch (Exception e) { e.printStackTrace(); }
    }

    // Safe helper to get double from Document regardless of stored type
    private double getDoubleValue(Document doc, String key) {
        try {
            Object val = doc.get(key);
            if (val == null)             return 0.0;
            if (val instanceof Double)   return (Double) val;
            if (val instanceof Integer)  return ((Integer) val).doubleValue();
            if (val instanceof Long)     return ((Long) val).doubleValue();
            if (val instanceof String)   return Double.parseDouble((String) val);
        } catch (Exception ignored) {}
        return 0.0;
    }
}