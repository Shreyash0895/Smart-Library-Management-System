import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;

public class ReportsPanel extends JPanel {

    private JTable booksTable, borrowingsTable, finesTable;
    private DefaultTableModel booksModel, borrowingsModel, finesModel;

    public ReportsPanel() {
        setLayout(new BorderLayout());

        JTabbedPane tabs = new JTabbedPane();

        booksModel = new DefaultTableModel(
            new String[]{"ID", "ISBN", "Title", "Author", "Total", "Available"}, 0);
        booksTable = new JTable(booksModel);
        tabs.add("Books", new JScrollPane(booksTable));

        borrowingsModel = new DefaultTableModel(
            new String[]{"ID", "Book", "Student", "Borrow Date", "Due Date", "Status"}, 0);
        borrowingsTable = new JTable(borrowingsModel);
        tabs.add("Borrowings", new JScrollPane(borrowingsTable));

        finesModel = new DefaultTableModel(
            new String[]{"ID", "Student", "Book", "Amount", "Status"}, 0);
        finesTable = new JTable(finesModel);
        tabs.add("Fines", new JScrollPane(finesTable));

        // Control panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        String[] types = {"All", "Books", "Borrowings", "Fines"};
        JComboBox<String> combo = new JComboBox<>(types);
        JButton genBtn = new JButton("Generate Report");
        genBtn.setBackground(new Color(70, 130, 180));
        genBtn.setForeground(Color.WHITE);
        genBtn.addActionListener(e -> loadReports((String) combo.getSelectedItem()));
        controlPanel.add(new JLabel("Report Type:")); controlPanel.add(combo); controlPanel.add(genBtn);

        add(controlPanel, BorderLayout.NORTH);
        add(tabs,         BorderLayout.CENTER);

        loadAll();
    }

    private void loadAll() { loadBooks(); loadBorrowings(); loadFines(); }

    private void loadReports(String type) {
        switch (type.toUpperCase()) {
            case "ALL":        loadAll();        break;
            case "BOOKS":      loadBooks();      break;
            case "BORROWINGS": loadBorrowings(); break;
            case "FINES":      loadFines();      break;
        }
    }

    private void loadBooks() {
        booksModel.setRowCount(0);
        try {
            for (Document doc : DatabaseConnection.getCollection("books").find()) {
                booksModel.addRow(new Object[]{
                    doc.getObjectId("_id").toString(),
                    doc.getString("isbn"),
                    doc.getString("title"),
                    doc.getString("author"),
                    doc.getInteger("quantity", 0),
                    doc.getInteger("available_quantity", 0)
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading books: " + e.getMessage());
        }
    }

    private void loadBorrowings() {
        borrowingsModel.setRowCount(0);
        try {
            MongoCollection<Document> borrowings = DatabaseConnection.getCollection("book_borrowings");
            MongoCollection<Document> users      = DatabaseConnection.getCollection("users");

            for (Document doc : borrowings.find()) {
                String uId  = doc.getString("user_id");
                String name = "";
                try {
                    Document u = users.find(Filters.eq("_id", new ObjectId(uId))).first();
                    if (u != null) name = u.getString("full_name");
                } catch (Exception ignored) {}

                borrowingsModel.addRow(new Object[]{
                    doc.getObjectId("_id").toString(),
                    doc.getString("title") != null ? doc.getString("title") : "",
                    name,
                    doc.getDate("borrow_date"),
                    doc.getDate("due_date"),
                    doc.getString("status")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading borrowings: " + e.getMessage());
        }
    }

    private void loadFines() {
        finesModel.setRowCount(0);
        try {
            MongoCollection<Document> fines = DatabaseConnection.getCollection("fines");
            MongoCollection<Document> users = DatabaseConnection.getCollection("users");
            MongoCollection<Document> books = DatabaseConnection.getCollection("books");

            for (Document doc : fines.find()) {
                String uName = "", bTitle = "";
                try {
                    Document u = users.find(Filters.eq("_id", new ObjectId(doc.getString("user_id")))).first();
                    if (u != null) uName = u.getString("full_name");
                    Document b = books.find(Filters.eq("_id", new ObjectId(doc.getString("book_id")))).first();
                    if (b != null) bTitle = b.getString("title");
                } catch (Exception ignored) {}

                finesModel.addRow(new Object[]{
                    doc.getObjectId("_id").toString(),
                    uName, bTitle,
                    "₹" + String.format("%.2f", doc.getDouble("amount") != null ? doc.getDouble("amount") : 0.0),
                    doc.getString("status")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading fines: " + e.getMessage());
        }
    }
}