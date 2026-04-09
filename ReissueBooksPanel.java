import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;

public class ReissueBooksPanel extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private String userId;

    public ReissueBooksPanel(String userId) {

        this.userId = userId;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        createUI();
        loadBooks();
    }

    private void createUI() {

        String[] cols = {"Borrow ID", "Book", "Borrow Date", "Due Date", "Status"};

        model = new DefaultTableModel(cols, 0);
        table = new JTable(model);

        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel panel = new JPanel();

        JButton reissueBtn = new JButton("Reissue Book");
        JButton refreshBtn = new JButton("Refresh");

        panel.add(reissueBtn);
        panel.add(refreshBtn);

        add(panel, BorderLayout.SOUTH);

        reissueBtn.addActionListener(e -> reissueBook());
        refreshBtn.addActionListener(e -> loadBooks());
    }

    // ================= LOAD =================
    private void loadBooks() {

        model.setRowCount(0);

        MongoCollection<Document> borrowings =
                DatabaseConnection.getCollection("book_borrowings");

        MongoCollection<Document> books =
                DatabaseConnection.getCollection("books");

        for (Document doc : borrowings.find(
                Filters.and(
                        Filters.eq("user_id", userId),
                        Filters.eq("status", "BORROWED")
                )
        )) {

            String bookId = doc.getString("book_id");

            Document book = books.find(
                    Filters.eq("_id", new ObjectId(bookId))
            ).first();

            model.addRow(new Object[]{
                    doc.getObjectId("_id").toString(),
                    (book != null) ? book.getString("title") : "Unknown",
                    new java.util.Date(doc.getLong("borrow_date")),
                    new java.util.Date(doc.getLong("due_date")),
                    doc.getString("status")
            });
        }
    }

    // ================= REISSUE =================
    private void reissueBook() {

        int row = table.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a book first!");
            return;
        }

        String borrowId = (String) model.getValueAt(row, 0);
        String bookTitle = (String) model.getValueAt(row, 1);

        MongoCollection<Document> borrowings =
                DatabaseConnection.getCollection("book_borrowings");

        Document record = borrowings.find(
                Filters.eq("_id", new ObjectId(borrowId))
        ).first();

        if (record == null) return;

        // Check already reissued
        if (record.getBoolean("reissued", false)) {
            JOptionPane.showMessageDialog(this,
                    "This book has already been reissued once!");
            return;
        }

        long newDue = record.getLong("due_date") + (7L * 24 * 60 * 60 * 1000);

        borrowings.updateOne(
                Filters.eq("_id", new ObjectId(borrowId)),
                new Document("$set",
                        new Document("due_date", newDue)
                                .append("reissued", true)
                )
        );

        JOptionPane.showMessageDialog(this,
                "Book '" + bookTitle + "' reissued successfully!");

        loadBooks();
    }
}