import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import com.mongodb.client.*;
import com.mongodb.client.model.*;
import org.bson.Document;
import org.bson.types.ObjectId;

public class IssueBooksPanel extends JPanel {
    private String userId;
    private boolean isDarkMode;
    private Color darkBackground  = new Color(33, 33, 33);
    private Color lightBackground = new Color(242, 242, 242);
    private JTable booksTable;
    private DefaultTableModel tableModel;
    private JTextField studentIdField, searchField;

    // Constructor with String userId
    public IssueBooksPanel(String userId, boolean isDarkMode) {
        this.userId    = userId;
        this.isDarkMode = isDarkMode;
        setLayout(new BorderLayout(10, 10));
        setBackground(isDarkMode ? darkBackground : lightBackground);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        createComponents();
        loadBooks();
    }

    private void createComponents() {
        JLabel titleLabel = new JLabel("Issue Books", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(isDarkMode ? Color.WHITE : Color.BLACK);
        add(titleLabel, BorderLayout.NORTH);

        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBackground(isDarkMode ? darkBackground : lightBackground);

        JPanel studentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        studentPanel.setBackground(isDarkMode ? darkBackground : lightBackground);
        JLabel studentLabel = new JLabel("Student Username:");
        studentLabel.setForeground(isDarkMode ? Color.WHITE : Color.BLACK);
        studentIdField = new JTextField(15);
        studentPanel.add(studentLabel); studentPanel.add(studentIdField);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.setBackground(isDarkMode ? darkBackground : lightBackground);
        searchField = new JTextField(20);
        JButton searchBtn = new JButton("Search");
        styleButton(searchBtn);
        searchBtn.addActionListener(e -> searchBooks());
        searchPanel.add(new JLabel("Search:")); searchPanel.add(searchField); searchPanel.add(searchBtn);

        topPanel.add(studentPanel, BorderLayout.WEST);
        topPanel.add(searchPanel,  BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
            new String[]{"Book ID", "Title", "Author", "Category", "Available"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        booksTable = new JTable(tableModel);
        booksTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        booksTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        booksTable.setRowHeight(25);
        add(new JScrollPane(booksTable), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(isDarkMode ? darkBackground : lightBackground);
        JButton issueBtn   = new JButton("Issue Book");
        JButton clearBtn   = new JButton("Clear");
        JButton refreshBtn = new JButton("Refresh");
        styleButton(issueBtn); styleButton(clearBtn); styleButton(refreshBtn);
        issueBtn  .addActionListener(e -> issueBook());
        clearBtn  .addActionListener(e -> clearFields());
        refreshBtn.addActionListener(e -> loadBooks());
        buttonPanel.add(issueBtn); buttonPanel.add(clearBtn); buttonPanel.add(refreshBtn);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void styleButton(JButton b) {
        b.setBackground(new Color(70, 130, 180));
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    }

    private void loadBooks() {
        tableModel.setRowCount(0);
        try {
            for (Document doc : DatabaseConnection.getCollection("books")
                    .find(Filters.and(Filters.eq("is_active", true), Filters.gt("available_quantity", 0)))) {
                tableModel.addRow(new Object[]{
                    doc.getObjectId("_id").toString(),
                    doc.getString("title"), doc.getString("author"),
                    doc.getString("category"), doc.getInteger("available_quantity", 0)
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading books: " + ex.getMessage());
        }
    }

    private void searchBooks() {
        String text = searchField.getText().trim();
        if (text.isEmpty()) { loadBooks(); return; }
        tableModel.setRowCount(0);
        try {
            for (Document doc : DatabaseConnection.getCollection("books").find(Filters.and(
                    Filters.eq("is_active", true), Filters.gt("available_quantity", 0),
                    Filters.or(
                        Filters.regex("title",    text, "i"),
                        Filters.regex("author",   text, "i"),
                        Filters.regex("category", text, "i"))))) {
                tableModel.addRow(new Object[]{
                    doc.getObjectId("_id").toString(),
                    doc.getString("title"), doc.getString("author"),
                    doc.getString("category"), doc.getInteger("available_quantity", 0)
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error searching: " + ex.getMessage());
        }
    }

    private void issueBook() {
        int selectedRow = booksTable.getSelectedRow();
        String studentUsername = studentIdField.getText().trim();
        if (selectedRow == -1) { JOptionPane.showMessageDialog(this, "Please select a book"); return; }
        if (studentUsername.isEmpty()) { JOptionPane.showMessageDialog(this, "Please enter student username"); return; }

        try {
            MongoCollection<Document> users = DatabaseConnection.getCollection("users");
            Document student = users.find(Filters.and(
                Filters.eq("username", studentUsername),
                Filters.eq("role", "STUDENT"))).first();
            if (student == null) { JOptionPane.showMessageDialog(this, "Student not found"); return; }
            if (!student.getBoolean("is_active", false)) {
                JOptionPane.showMessageDialog(this, "Student account is not active"); return;
            }

            String studentId = student.getObjectId("_id").toString();
            String bookId    = (String) tableModel.getValueAt(selectedRow, 0);
            String bookTitle = (String) tableModel.getValueAt(selectedRow, 1);

            MongoCollection<Document> borrowings = DatabaseConnection.getCollection("book_borrowings");
            long overdue = borrowings.countDocuments(Filters.and(
                Filters.eq("user_id", studentId), Filters.eq("status", "BORROWED"),
                Filters.lt("due_date", new Date())));
            if (overdue > 0) { JOptionPane.showMessageDialog(this, "Student has overdue books"); return; }

            long already = borrowings.countDocuments(Filters.and(
                Filters.eq("user_id", studentId), Filters.eq("book_id", bookId),
                Filters.eq("status", "BORROWED")));
            if (already > 0) { JOptionPane.showMessageDialog(this, "Student already has this book"); return; }

            DatabaseConnection.getCollection("books").updateOne(
                Filters.and(Filters.eq("_id", new ObjectId(bookId)), Filters.gt("available_quantity", 0)),
                Updates.inc("available_quantity", -1));

            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, 14);
            Date dueDate = cal.getTime();
            borrowings.insertOne(new Document()
                .append("book_id",    bookId)
                .append("user_id",    studentId)
                .append("title",      bookTitle)
                .append("borrow_date",new Date())
                .append("due_date",   dueDate)
                .append("status",     "BORROWED")
                .append("fine_amount",0.0)
                .append("fine_paid",  false));

            DatabaseConnection.getCollection("notifications").insertOne(new Document()
                .append("user_id",    studentId)
                .append("message",    "Book '" + bookTitle + "' issued. Due: " + dueDate)
                .append("is_read",    false)
                .append("created_at", new Date()));

            JOptionPane.showMessageDialog(this, "Book issued successfully!");
            loadBooks(); clearFields();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error issuing book: " + ex.getMessage());
        }
    }

    private void clearFields() {
        studentIdField.setText(""); searchField.setText(""); booksTable.clearSelection();
    }
}