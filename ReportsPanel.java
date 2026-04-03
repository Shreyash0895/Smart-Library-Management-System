import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import org.bson.Document;

public class ReportsPanel extends JPanel {
    private JTabbedPane tabbedPane;
    private DefaultTableModel booksModel, borrowingsModel, finesModel;

    public ReportsPanel() {
        setLayout(new BorderLayout());
        add(createControlPanel(), BorderLayout.NORTH);
        tabbedPane = new JTabbedPane();
        createTables();
        add(tabbedPane, BorderLayout.CENTER);
        loadReports("ALL");
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel("Report Type:"));
        String[] types = {"All", "Books", "Borrowings", "Fines"};
        JComboBox<String> combo = new JComboBox<>(types);
        panel.add(combo);
        JButton genBtn = new JButton("Generate Report");
        genBtn.addActionListener(e -> loadReports((String) combo.getSelectedItem()));
        panel.add(genBtn);
        return panel;
    }

    private void createTables() {
        booksModel = new DefaultTableModel(
            new String[]{"Book ID", "ISBN", "Title", "Author", "Total Copies", "Available"}, 0);
        borrowingsModel = new DefaultTableModel(
            new String[]{"Borrowing ID", "Book Title", "Student", "Borrow Date", "Due Date", "Status"}, 0);
        finesModel = new DefaultTableModel(
            new String[]{"Fine ID", "Student", "Book Title", "Amount", "Status", "Date"}, 0);

        tabbedPane.addTab("Books",      new JScrollPane(new JTable(booksModel)));
        tabbedPane.addTab("Borrowings", new JScrollPane(new JTable(borrowingsModel)));
        tabbedPane.addTab("Fines",      new JScrollPane(new JTable(finesModel)));
    }

    private void loadReports(String type) {
        switch (type.toUpperCase()) {
            case "ALL":       loadBooksReport(); loadBorrowingsReport(); loadFinesReport(); break;
            case "BOOKS":     loadBooksReport();     break;
            case "BORROWINGS":loadBorrowingsReport();break;
            case "FINES":     loadFinesReport();     break;
        }
    }

    private void loadBooksReport() {
        booksModel.setRowCount(0);
        try {
            for (Document d : DatabaseConnection.getCollection("books")
                    .find(Filters.eq("is_active", true))) {
                booksModel.addRow(new Object[]{
                    d.getObjectId("_id").toString(),
                    d.getString("isbn"),
                    d.getString("title"),
                    d.getString("author"),
                    d.getInteger("quantity", 0),
                    d.getInteger("available_quantity", 0)
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading books: " + e.getMessage());
        }
    }

    private void loadBorrowingsReport() {
        borrowingsModel.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            MongoCollection<Document> borrowings = DatabaseConnection.getCollection("book_borrowings");
            MongoCollection<Document> users      = DatabaseConnection.getCollection("users");
            for (Document b : borrowings.find()) {
                String uId = b.getString("user_id");
                String name = "";
                try {
                    Document u = users.find(Filters.eq("_id", new org.bson.types.ObjectId(uId))).first();
                    if (u != null) name = u.getString("full_name");
                } catch (Exception ignored) {}
                borrowingsModel.addRow(new Object[]{
                    b.getObjectId("_id").toString(),
                    b.getString("title"), name,
                    b.getDate("borrow_date") != null ? sdf.format(b.getDate("borrow_date")) : "",
                    b.getDate("due_date")    != null ? sdf.format(b.getDate("due_date"))    : "",
                    b.getString("status")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading borrowings: " + e.getMessage());
        }
    }

    private void loadFinesReport() {
        finesModel.setRowCount(0);
        try {
            MongoCollection<Document> fines = DatabaseConnection.getCollection("fines");
            MongoCollection<Document> users = DatabaseConnection.getCollection("users");
            MongoCollection<Document> books = DatabaseConnection.getCollection("books");
            for (Document f : fines.find()) {
                String uName = "", bTitle = "";
                try {
                    Document u = users.find(Filters.eq("_id", new org.bson.types.ObjectId(f.getString("user_id")))).first();
                    if (u != null) uName = u.getString("full_name");
                    Document b = books.find(Filters.eq("_id", new org.bson.types.ObjectId(f.getString("book_id")))).first();
                    if (b != null) bTitle = b.getString("title");
                } catch (Exception ignored) {}
                finesModel.addRow(new Object[]{
                    f.getObjectId("_id").toString(),
                    uName, bTitle,
                    String.format("$%.2f", f.getDouble("amount") != null ? f.getDouble("amount") : 0.0),
                    f.getString("status"),
                    f.getDate("created_at")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading fines: " + e.getMessage());
        }
    }
}