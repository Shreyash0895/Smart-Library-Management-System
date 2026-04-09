import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Date;
import com.mongodb.client.*;
import com.mongodb.client.model.*;
import org.bson.Document;

public class IssuedBooksPanel extends JPanel {
    private String userId;
    private boolean isDarkMode;
    private Color darkBackground  = new Color(33, 33, 33);
    private Color lightBackground = new Color(242, 242, 242);

    public IssuedBooksPanel(String userId, boolean isDarkMode) {
        this.userId    = userId;
        this.isDarkMode = isDarkMode;
        setLayout(new BorderLayout(10, 10));
        setBackground(isDarkMode ? darkBackground : lightBackground);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("Issued Books", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(isDarkMode ? Color.WHITE : Color.BLACK);

        String[] columns = {"Book ID", "Title", "Student ID", "Student Name", "Borrow Date", "Due Date", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.setRowHeight(25);

        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(isDarkMode ? darkBackground : lightBackground);
        JTextField searchField = new JTextField(20);
        JButton searchBtn = new JButton("Search");
        searchBtn.setBackground(new Color(70, 130, 180));
        searchBtn.setForeground(Color.WHITE);
        String[] filterOptions = {"All", "BORROWED", "OVERDUE", "RETURNED"};
        JComboBox<String> filterCombo = new JComboBox<>(filterOptions);
        searchPanel.add(new JLabel("Search: ")); searchPanel.add(searchField); searchPanel.add(searchBtn);
        searchPanel.add(Box.createHorizontalStrut(20));
        searchPanel.add(new JLabel("Filter: ")); searchPanel.add(filterCombo);

        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(titleLabel,   BorderLayout.NORTH);
        northPanel.add(searchPanel,  BorderLayout.CENTER);
        add(northPanel, BorderLayout.NORTH);
        add(new JScrollPane(table),  BorderLayout.CENTER);

        // Stats panel
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statsPanel.setBackground(isDarkMode ? darkBackground : lightBackground);
        add(statsPanel, BorderLayout.SOUTH);

        loadIssuedBooks(model, "All");
        updateStatistics(statsPanel);

        searchBtn.addActionListener(e -> searchIssuedBooks(model, searchField.getText().trim(), (String) filterCombo.getSelectedItem()));
        filterCombo.addActionListener(e -> loadIssuedBooks(model, (String) filterCombo.getSelectedItem()));
    }

    private void loadIssuedBooks(DefaultTableModel model, String filter) {
        model.setRowCount(0);
        try {
            MongoCollection<Document> borrowings = DatabaseConnection.getCollection("book_borrowings");
            MongoCollection<Document> books      = DatabaseConnection.getCollection("books");
            MongoCollection<Document> users      = DatabaseConnection.getCollection("users");

            Iterable<Document> docs = filter.equals("All")
                ? borrowings.find()
                : borrowings.find(Filters.eq("status", filter));

            for (Document b : docs) {
                String bookId  = b.getString("book_id");
                String uId     = b.getString("user_id");
                String title   = b.getString("title");
                String fullName = "";

                try {
                    Document user = users.find(Filters.eq("_id", new org.bson.types.ObjectId(uId))).first();
                    if (user != null) fullName = user.getString("full_name");
                } catch (Exception ignored) {}

                model.addRow(new Object[]{
                    bookId, title == null ? bookId : title,
                    uId, fullName,
                    b.getDate("borrow_date"),
                    b.getDate("due_date"),
                    b.getString("status")
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading issued books: " + ex.getMessage());
        }
    }

    private void searchIssuedBooks(DefaultTableModel model, String searchText, String filter) {
        model.setRowCount(0);
        try {
            MongoCollection<Document> borrowings = DatabaseConnection.getCollection("book_borrowings");
            MongoCollection<Document> users      = DatabaseConnection.getCollection("users");

            Iterable<Document> docs = filter.equals("All")
                ? borrowings.find(Filters.regex("title", searchText, "i"))
                : borrowings.find(Filters.and(
                    Filters.regex("title", searchText, "i"),
                    Filters.eq("status", filter)));

            for (Document b : docs) {
                String uId = b.getString("user_id");
                String fullName = "";
                try {
                    Document user = users.find(Filters.eq("_id", new org.bson.types.ObjectId(uId))).first();
                    if (user != null) fullName = user.getString("full_name");
                } catch (Exception ignored) {}

                model.addRow(new Object[]{
                    b.getString("book_id"), b.getString("title"),
                    uId, fullName,
                    b.getDate("borrow_date"), b.getDate("due_date"), b.getString("status")
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error searching: " + ex.getMessage());
        }
    }

    private void updateStatistics(JPanel statsPanel) {
        try {
            MongoCollection<Document> borrowings = DatabaseConnection.getCollection("book_borrowings");
            long total    = borrowings.countDocuments();
            long current  = borrowings.countDocuments(Filters.eq("status", "BORROWED"));
            long overdue  = borrowings.countDocuments(Filters.and(
                Filters.eq("status", "BORROWED"), Filters.lt("due_date", new Date())));

            JLabel totalLbl   = new JLabel("Total Issues: " + total);
            JLabel currentLbl = new JLabel("Currently Issued: " + current);
            JLabel overdueLbl = new JLabel("Overdue: " + overdue);
            totalLbl  .setForeground(isDarkMode ? Color.WHITE : Color.BLACK);
            currentLbl.setForeground(isDarkMode ? Color.WHITE : Color.BLACK);
            overdueLbl.setForeground(Color.RED);
            statsPanel.add(totalLbl);
            statsPanel.add(Box.createHorizontalStrut(20));
            statsPanel.add(currentLbl);
            statsPanel.add(Box.createHorizontalStrut(20));
            statsPanel.add(overdueLbl);
        } catch (Exception ex) { ex.printStackTrace(); }
    }
}