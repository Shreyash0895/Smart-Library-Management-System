import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;

public class IssuedBooksPanel extends JPanel {

    private String userId;
    private boolean isDarkMode;

    private JTable table;
    private DefaultTableModel model;

    public IssuedBooksPanel(String userId, boolean isDarkMode) {

        this.userId = userId;
        this.isDarkMode = isDarkMode;

        setLayout(new BorderLayout(10, 10));

        // Title
        JLabel title = new JLabel("Issued Books", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        add(title, BorderLayout.NORTH);

        // Table
        String[] cols = {"Book", "Student", "Issue Date", "Due Date", "Status"};
        model = new DefaultTableModel(cols, 0);
        table = new JTable(model);

        add(new JScrollPane(table), BorderLayout.CENTER);

        // Search Panel
        JPanel top = new JPanel();
        JTextField search = new JTextField(20);
        JButton searchBtn = new JButton("Search");

        String[] filters = {"All", "BORROWED", "RETURNED"};
        JComboBox<String> filterBox = new JComboBox<>(filters);

        top.add(new JLabel("Search:"));
        top.add(search);
        top.add(searchBtn);
        top.add(new JLabel("Filter:"));
        top.add(filterBox);

        add(top, BorderLayout.NORTH);

        // Stats
        JPanel stats = new JPanel();
        add(stats, BorderLayout.SOUTH);

        // Load
        loadData("All");

        // Actions
        searchBtn.addActionListener(e ->
                searchData(search.getText(), (String) filterBox.getSelectedItem()));

        filterBox.addActionListener(e ->
                loadData((String) filterBox.getSelectedItem()));

        updateStats(stats);
    }

    // ================= LOAD =================
    private void loadData(String filter) {

        model.setRowCount(0);

        MongoCollection<Document> borrowings =
                DatabaseConnection.getCollection("book_borrowings");

        MongoCollection<Document> users =
                DatabaseConnection.getCollection("users");

        MongoCollection<Document> books =
                DatabaseConnection.getCollection("books");

        FindIterable<Document> docs;

        if (filter.equals("All")) {
            docs = borrowings.find();
        } else {
            docs = borrowings.find(Filters.eq("status", filter));
        }

        for (Document doc : docs) {

            String bookId = doc.getString("book_id");
            String userId = doc.getString("user_id");

            Document book = books.find(
                    Filters.eq("_id", new ObjectId(bookId))
            ).first();

            Document user = users.find(
                    Filters.eq("_id", new ObjectId(userId))
            ).first();

            model.addRow(new Object[]{
                    (book != null) ? book.getString("title") : "Unknown",
                    (user != null) ? user.getString("full_name") : "Unknown",
                    new java.util.Date(doc.getLong("borrow_date")),
                    new java.util.Date(doc.getLong("due_date")),
                    doc.getString("status")
            });
        }
    }

    // ================= SEARCH =================
    private void searchData(String text, String filter) {

        model.setRowCount(0);

        MongoCollection<Document> borrowings =
                DatabaseConnection.getCollection("book_borrowings");

        MongoCollection<Document> users =
                DatabaseConnection.getCollection("users");

        MongoCollection<Document> books =
                DatabaseConnection.getCollection("books");

        for (Document doc : borrowings.find()) {

            String bookId = doc.getString("book_id");
            String userId = doc.getString("user_id");

            Document book = books.find(
                    Filters.eq("_id", new ObjectId(bookId))
            ).first();

            Document user = users.find(
                    Filters.eq("_id", new ObjectId(userId))
            ).first();

            String title = (book != null) ? book.getString("title") : "";
            String name = (user != null) ? user.getString("full_name") : "";

            boolean match = title.toLowerCase().contains(text.toLowerCase()) ||
                            name.toLowerCase().contains(text.toLowerCase());

            boolean filterMatch = filter.equals("All") ||
                                  doc.getString("status").equals(filter);

            if (match && filterMatch) {
                model.addRow(new Object[]{
                        title,
                        name,
                        new java.util.Date(doc.getLong("borrow_date")),
                        new java.util.Date(doc.getLong("due_date")),
                        doc.getString("status")
                });
            }
        }
    }

    // ================= STATS =================
    private void updateStats(JPanel panel) {

        panel.removeAll();

        MongoCollection<Document> borrowings =
                DatabaseConnection.getCollection("book_borrowings");

        long total = borrowings.countDocuments();
        long current = borrowings.countDocuments(Filters.eq("status", "BORROWED"));
        long returned = borrowings.countDocuments(Filters.eq("status", "RETURNED"));

        panel.add(new JLabel("Total: " + total));
        panel.add(new JLabel("Active: " + current));
        panel.add(new JLabel("Returned: " + returned));
    }
}