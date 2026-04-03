import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;

public class BorrowBooksPanel extends JPanel {

    private String userId;
    private JTable bookTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;

    public BorrowBooksPanel(String userId) {
        this.userId = userId;

        setLayout(new BorderLayout());

        add(createSearchPanel(), BorderLayout.NORTH);

        createBookTable();
        add(new JScrollPane(bookTable), BorderLayout.CENTER);

        JButton borrowBtn = new JButton("Borrow Selected Book");
        borrowBtn.addActionListener(e -> borrowBook());
        add(borrowBtn, BorderLayout.SOUTH);

        loadAvailableBooks();
    }

    // ================= SEARCH PANEL =================
    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new FlowLayout());

        searchField = new JTextField(25);
        JButton searchBtn = new JButton("Search");

        searchBtn.addActionListener(e -> searchBooks());

        panel.add(new JLabel("Search:"));
        panel.add(searchField);
        panel.add(searchBtn);

        return panel;
    }

    // ================= TABLE =================
    private void createBookTable() {
        String[] columns = {"ID", "ISBN", "Title", "Author", "Available"};

        tableModel = new DefaultTableModel(columns, 0);
        bookTable = new JTable(tableModel);
    }

    // ================= LOAD BOOKS =================
    private void loadAvailableBooks() {
        tableModel.setRowCount(0);

        MongoCollection<Document> books =
                DatabaseConnection.getCollection("books");

        FindIterable<Document> docs =
                books.find(Filters.gt("available_quantity", 0));

        for (Document doc : docs) {
            tableModel.addRow(new Object[]{
                    doc.getObjectId("_id").toString(),
                    doc.getString("isbn"),
                    doc.getString("title"),
                    doc.getString("author"),
                    doc.getInteger("available_quantity", 0)
            });
        }
    }

    // ================= SEARCH =================
    private void searchBooks() {
        String search = searchField.getText();

        tableModel.setRowCount(0);

        MongoCollection<Document> books =
                DatabaseConnection.getCollection("books");

        FindIterable<Document> docs = books.find(
                Filters.and(
                        Filters.gt("available_quantity", 0),
                        Filters.or(
                                Filters.regex("title", search, "i"),
                                Filters.regex("author", search, "i"),
                                Filters.regex("isbn", search, "i")
                        )
                )
        );

        for (Document doc : docs) {
            tableModel.addRow(new Object[]{
                    doc.getObjectId("_id").toString(),
                    doc.getString("isbn"),
                    doc.getString("title"),
                    doc.getString("author"),
                    doc.getInteger("available_quantity", 0)
            });
        }
    }

    // ================= BORROW =================
    private void borrowBook() {

        int row = bookTable.getSelectedRow();

        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a book!");
            return;
        }

        String bookId = (String) tableModel.getValueAt(row, 0);

        MongoCollection<Document> borrowings =
                DatabaseConnection.getCollection("book_borrowings");

        MongoCollection<Document> books =
                DatabaseConnection.getCollection("books");

        // Check overdue
        long overdue = borrowings.countDocuments(
                Filters.and(
                        Filters.eq("user_id", userId),
                        Filters.eq("status", "BORROWED"),
                        Filters.lt("due_date", System.currentTimeMillis())
                )
        );

        if (overdue > 0) {
            JOptionPane.showMessageDialog(this, "You have overdue books!");
            return;
        }

        // Update book quantity
        books.updateOne(
                Filters.eq("_id", new ObjectId(bookId)),
                new Document("$inc", new Document("available_quantity", -1))
        );

        // Insert borrowing record
        Document record = new Document()
                .append("user_id", userId)
                .append("book_id", bookId)
                .append("status", "BORROWED")
                .append("borrow_date", System.currentTimeMillis())
                .append("due_date", System.currentTimeMillis() + (14L * 24 * 60 * 60 * 1000));

        borrowings.insertOne(record);

        JOptionPane.showMessageDialog(this, "Book borrowed successfully!");

        loadAvailableBooks();
    }
}