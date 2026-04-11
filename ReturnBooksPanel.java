import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Date;
import com.mongodb.client.*;
import com.mongodb.client.model.*;
import org.bson.Document;
import org.bson.types.ObjectId;

public class ReturnBooksPanel extends JPanel {
    private String userId;
    private boolean isDarkMode;
    private boolean isLibrarian;
    private DefaultTableModel model;
    private JTable table;
    private JTextField studentSearchField;

    // Constructor 1 - with isDarkMode (used by LibrarianDashboard)
    public ReturnBooksPanel(String userId, boolean isDarkMode) {
        this.userId = userId;
        this.isDarkMode = isDarkMode;
        this.isLibrarian = true;
        init();
    }

    // Constructor 2 - without isDarkMode (used by StudentDashboard)
    public ReturnBooksPanel(String userId) {
        this.userId = userId;
        this.isDarkMode = false;
        this.isLibrarian = false;
        init();
    }

    private void init() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("Return Books", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(44, 62, 80));

        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(title, BorderLayout.NORTH);

        // Add student search for librarians
        if (isLibrarian) {
            JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            searchPanel.add(new JLabel("Search Student Username:"));
            studentSearchField = new JTextField(15);
            JButton searchBtn = makeBtn("Search", new Color(70, 130, 180));
            JButton showAllBtn = makeBtn("Show All", new Color(52, 152, 219));
            searchBtn.addActionListener(e -> searchByStudent());
            showAllBtn.addActionListener(e -> loadBorrowedBooks());
            searchPanel.add(studentSearchField);
            searchPanel.add(searchBtn);
            searchPanel.add(showAllBtn);
            northPanel.add(searchPanel, BorderLayout.CENTER);
        }

        add(northPanel, BorderLayout.NORTH);

        model = new DefaultTableModel(
            new String[]{"Borrowing ID", "Book ID", "Title", "Student", "Borrow Date", "Due Date"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.setRowHeight(28);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton returnBtn  = makeBtn("Return Selected Book", new Color(41, 128, 185));
        JButton refreshBtn = makeBtn("Refresh",              new Color(52, 152, 219));
        returnBtn .addActionListener(e -> returnBook());
        refreshBtn.addActionListener(e -> loadBorrowedBooks());
        btnPanel.add(returnBtn);
        btnPanel.add(refreshBtn);
        add(btnPanel, BorderLayout.SOUTH);

        loadBorrowedBooks();
    }

    private JButton makeBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg); b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setFocusPainted(false); b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void loadBorrowedBooks() {
        model.setRowCount(0);
        try {
            MongoCollection<Document> borrowings = DatabaseConnection.getCollection("book_borrowings");
            MongoCollection<Document> users      = DatabaseConnection.getCollection("users");

            // Librarians see ALL borrowed books; students see only their own
            Iterable<Document> docs = isLibrarian
                ? borrowings.find(Filters.eq("status", "BORROWED"))
                : borrowings.find(Filters.and(
                    Filters.eq("user_id", userId),
                    Filters.eq("status", "BORROWED")));

            for (Document b : docs) {
                String studentName = "";
                if (isLibrarian) {
                    try {
                        Document user = users.find(Filters.eq("_id", new ObjectId(b.getString("user_id")))).first();
                        if (user != null) studentName = user.getString("full_name");
                    } catch (Exception ignored) {}
                }
                model.addRow(new Object[]{
                    b.getObjectId("_id").toString(),
                    b.getString("book_id"),
                    b.getString("title"),
                    studentName,
                    b.getDate("borrow_date"),
                    b.getDate("due_date")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading borrowed books: " + e.getMessage());
        }
    }

    private void searchByStudent() {
        String username = studentSearchField.getText().trim();
        if (username.isEmpty()) { loadBorrowedBooks(); return; }
        model.setRowCount(0);
        try {
            MongoCollection<Document> users = DatabaseConnection.getCollection("users");
            Document student = users.find(Filters.eq("username", username)).first();
            if (student == null) { JOptionPane.showMessageDialog(this, "Student not found"); return; }
            String studentId = student.getObjectId("_id").toString();
            String studentName = student.getString("full_name");

            MongoCollection<Document> borrowings = DatabaseConnection.getCollection("book_borrowings");
            for (Document b : borrowings.find(Filters.and(
                    Filters.eq("user_id", studentId),
                    Filters.eq("status", "BORROWED")))) {
                model.addRow(new Object[]{
                    b.getObjectId("_id").toString(),
                    b.getString("book_id"),
                    b.getString("title"),
                    studentName,
                    b.getDate("borrow_date"),
                    b.getDate("due_date")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error searching: " + e.getMessage());
        }
    }

    private void returnBook() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a book to return");
            return;
        }
        String borrowingId = (String) model.getValueAt(row, 0);
        String bookId      = (String) model.getValueAt(row, 1);
        try {
            // Update borrowing status
            DatabaseConnection.getCollection("book_borrowings").updateOne(
                Filters.eq("_id", new ObjectId(borrowingId)),
                Updates.combine(
                    Updates.set("status", "RETURNED"),
                    Updates.set("return_date", new Date())));

            // Increase available quantity
            try {
                DatabaseConnection.getCollection("books").updateOne(
                    Filters.eq("_id", new ObjectId(bookId)),
                    Updates.inc("available_quantity", 1));
            } catch (Exception ignored) {}

            model.removeRow(row);
            JOptionPane.showMessageDialog(this,
                "Book returned successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error returning book: " + e.getMessage());
        }
    }
}