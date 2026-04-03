import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import com.mongodb.client.*;
import com.mongodb.client.model.*;
import org.bson.Document;
import org.bson.types.ObjectId;

public class RequestBooksPanel extends JPanel {
    private JTable booksTable;
    private DefaultTableModel tableModel;
    private String userId;
    private JTextField searchField;

    public RequestBooksPanel(String userId) {
        this.userId = userId;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        initializeComponents();
        loadAvailableBooks();
    }

    private void initializeComponents() {
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField(20);
        JButton searchBtn  = createBtn("Search");
        searchPanel.add(new JLabel("Search Books: "));
        searchPanel.add(searchField); searchPanel.add(searchBtn);

        String[] columns = {"Book ID", "Title", "Author", "Category", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        booksTable = new JTable(tableModel);
        booksTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton requestBtn = createBtn("Request Book");
        JButton refreshBtn = createBtn("Refresh");
        btnPanel.add(requestBtn); btnPanel.add(refreshBtn);

        add(searchPanel,               BorderLayout.NORTH);
        add(new JScrollPane(booksTable), BorderLayout.CENTER);
        add(btnPanel,                  BorderLayout.SOUTH);

        searchBtn .addActionListener(e -> searchBooks());
        requestBtn.addActionListener(e -> requestBook());
        refreshBtn.addActionListener(e -> loadAvailableBooks());
    }

    private JButton createBtn(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        b.setPreferredSize(new Dimension(120, 30));
        b.setBackground(new Color(70, 130, 180));
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false); b.setBorderPainted(false);
        return b;
    }

    private void loadAvailableBooks() {
        tableModel.setRowCount(0);
        try {
            for (Document d : DatabaseConnection.getCollection("books")
                    .find(Filters.eq("is_active", true))) {
                int avail = d.getInteger("available_quantity", 0);
                tableModel.addRow(new Object[]{
                    d.getObjectId("_id").toString(),
                    d.getString("title"),
                    d.getString("author"),
                    d.getString("category"),
                    avail > 0 ? "Available" : "Not Available"
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading books: " + ex.getMessage());
        }
    }

    private void searchBooks() {
        String term = searchField.getText().trim();
        tableModel.setRowCount(0);
        try {
            Iterable<Document> docs = term.isEmpty()
                ? DatabaseConnection.getCollection("books").find(Filters.eq("is_active", true))
                : DatabaseConnection.getCollection("books").find(Filters.and(
                    Filters.eq("is_active", true),
                    Filters.or(
                        Filters.regex("title",    term, "i"),
                        Filters.regex("author",   term, "i"),
                        Filters.regex("category", term, "i"))));
            for (Document d : docs) {
                int avail = d.getInteger("available_quantity", 0);
                tableModel.addRow(new Object[]{
                    d.getObjectId("_id").toString(),
                    d.getString("title"), d.getString("author"),
                    d.getString("category"), avail > 0 ? "Available" : "Not Available"
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error searching books: " + ex.getMessage());
        }
    }

    private void requestBook() {
        int row = booksTable.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Please select a book"); return; }

        String bookId    = (String) tableModel.getValueAt(row, 0);
        String bookTitle = (String) tableModel.getValueAt(row, 1);
        String status    = (String) tableModel.getValueAt(row, 4);

        if ("Not Available".equals(status)) {
            JOptionPane.showMessageDialog(this, "This book is not currently available."); return;
        }

        try {
            MongoCollection<Document> borrowings = DatabaseConnection.getCollection("book_borrowings");
            long already = borrowings.countDocuments(Filters.and(
                Filters.eq("user_id", userId),
                Filters.eq("book_id", bookId),
                Filters.eq("status",  "BORROWED")));
            if (already > 0) { JOptionPane.showMessageDialog(this, "You already have this book borrowed."); return; }

            // Update available quantity
            DatabaseConnection.getCollection("books").updateOne(
                Filters.and(Filters.eq("_id", new ObjectId(bookId)), Filters.gt("available_quantity", 0)),
                Updates.inc("available_quantity", -1));

            // Create borrowing record
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, 14);
            borrowings.insertOne(new Document()
                .append("book_id",    bookId)
                .append("user_id",    userId)
                .append("title",      bookTitle)
                .append("borrow_date",new Date())
                .append("due_date",   cal.getTime())
                .append("status",     "BORROWED")
                .append("fine_amount",0.0)
                .append("fine_paid",  false));

            JOptionPane.showMessageDialog(this, "Book borrowed successfully! Due in 14 days.");
            loadAvailableBooks();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error requesting book: " + ex.getMessage());
        }
    }
}