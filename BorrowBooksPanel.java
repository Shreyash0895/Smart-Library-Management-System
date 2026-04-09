import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import com.mongodb.client.*;
import com.mongodb.client.model.*;
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

        JPanel buttonPanel = new JPanel();
        JButton borrowButton = new JButton("Borrow Selected Book");
        borrowButton.addActionListener(e -> borrowBook());
        buttonPanel.add(borrowButton);
        add(buttonPanel, BorderLayout.SOUTH);

        loadAvailableBooks();
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel("Search:"));
        searchField = new JTextField(30);
        searchField.addActionListener(e -> searchBooks());
        panel.add(searchField);
        JButton searchBtn = new JButton("Search");
        searchBtn.addActionListener(e -> searchBooks());
        panel.add(searchBtn);
        return panel;
    }

    private void createBookTable() {
        tableModel = new DefaultTableModel(
            new String[]{"ID", "ISBN", "Title", "Author", "Available Quantity"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        bookTable = new JTable(tableModel);
    }

    private void loadAvailableBooks() {
        tableModel.setRowCount(0);
        try {
            MongoCollection<Document> books = DatabaseConnection.getCollection("books");
            for (Document doc : books.find(Filters.and(
                    Filters.gt("available_quantity", 0),
                    Filters.eq("is_active", true)))) {
                tableModel.addRow(new Object[]{
                    doc.getObjectId("_id").toString(),
                    doc.getString("isbn"),
                    doc.getString("title"),
                    doc.getString("author"),
                    doc.getInteger("available_quantity", 0)
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading books: " + e.getMessage());
        }
    }

    private void searchBooks() {
        String term = searchField.getText().trim();
        tableModel.setRowCount(0);
        try {
            MongoCollection<Document> books = DatabaseConnection.getCollection("books");
            Iterable<Document> results = term.isEmpty()
                ? books.find(Filters.and(Filters.gt("available_quantity", 0), Filters.eq("is_active", true)))
                : books.find(Filters.and(
                    Filters.gt("available_quantity", 0),
                    Filters.eq("is_active", true),
                    Filters.or(
                        Filters.regex("title",  term, "i"),
                        Filters.regex("author", term, "i"),
                        Filters.regex("isbn",   term, "i"))));
            for (Document doc : results) {
                tableModel.addRow(new Object[]{
                    doc.getObjectId("_id").toString(),
                    doc.getString("isbn"),
                    doc.getString("title"),
                    doc.getString("author"),
                    doc.getInteger("available_quantity", 0)
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error searching books: " + e.getMessage());
        }
    }

    private void borrowBook() {
        int selectedRow = bookTable.getSelectedRow();
        if (selectedRow < 0) { JOptionPane.showMessageDialog(this, "Please select a book to borrow"); return; }

        try {
            MongoCollection<Document> borrowings = DatabaseConnection.getCollection("book_borrowings");

            // Check overdue books
            long overdue = borrowings.countDocuments(Filters.and(
                Filters.eq("user_id", userId),
                Filters.eq("status", "BORROWED"),
                Filters.lt("due_date", new Date())));
            if (overdue > 0) {
                JOptionPane.showMessageDialog(this, "You have overdue books. Please return them first."); return;
            }

            String bookId = (String) bookTable.getValueAt(selectedRow, 0);
            String title  = (String) bookTable.getValueAt(selectedRow, 2);

            // Check already borrowed
            long alreadyBorrowed = borrowings.countDocuments(Filters.and(
                Filters.eq("user_id", userId),
                Filters.eq("book_id", bookId),
                Filters.eq("status", "BORROWED")));
            if (alreadyBorrowed > 0) {
                JOptionPane.showMessageDialog(this, "You already have this book borrowed."); return;
            }

            // Update available quantity
            MongoCollection<Document> books = DatabaseConnection.getCollection("books");
            books.updateOne(
                Filters.and(Filters.eq("_id", new ObjectId(bookId)), Filters.gt("available_quantity", 0)),
                Updates.inc("available_quantity", -1));

            // Create borrowing record
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, 14);
            borrowings.insertOne(new Document()
                .append("book_id",    bookId)
                .append("user_id",    userId)
                .append("title",      title)
                .append("borrow_date",new Date())
                .append("due_date",   cal.getTime())
                .append("status",     "BORROWED")
                .append("fine_amount",0.0)
                .append("fine_paid",  false));

            JOptionPane.showMessageDialog(this, "Book borrowed successfully!");
            loadAvailableBooks();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error borrowing book: " + e.getMessage());
        }
    }
}