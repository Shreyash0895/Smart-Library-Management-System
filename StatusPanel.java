import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;

public class StatusPanel extends JPanel {

    private String userId;
    private JTable table;
    private DefaultTableModel model;
    private JLabel fineLabel;

    public StatusPanel(String userId) {

        this.userId = userId;

        setLayout(new BorderLayout());

        add(createHeader(), BorderLayout.NORTH);

        createTable();
        add(new JScrollPane(table), BorderLayout.CENTER);

        loadData();
        updateFine();
    }

    private JPanel createHeader() {
        JPanel panel = new JPanel();
        fineLabel = new JLabel("Total Outstanding Fines: ₹0");
        fineLabel.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(fineLabel);
        return panel;
    }

    private void createTable() {
        String[] cols = {"Book", "Borrow Date", "Due Date", "Status", "Fine"};

        model = new DefaultTableModel(cols, 0);
        table = new JTable(model);
    }

    // ================= LOAD =================
    private void loadData() {

        model.setRowCount(0);

        MongoCollection<Document> borrowings =
                DatabaseConnection.getCollection("book_borrowings");

        MongoCollection<Document> books =
                DatabaseConnection.getCollection("books");

        for (Document doc : borrowings.find(
                Filters.eq("user_id", userId)
        )) {

            Document book = books.find(
                    Filters.eq("_id", new ObjectId(doc.getString("book_id")))
            ).first();

            model.addRow(new Object[]{
                    (book != null) ? book.getString("title") : "Unknown",
                    new java.util.Date(doc.getLong("borrow_date")),
                    new java.util.Date(doc.getLong("due_date")),
                    doc.getString("status"),
                    "₹" + doc.getDouble("fine_amount", 0.0)
            });
        }
    }

    // ================= FINE =================
    private void updateFine() {

        double total = 0;

        MongoCollection<Document> borrowings =
                DatabaseConnection.getCollection("book_borrowings");

        for (Document doc : borrowings.find(
                Filters.and(
                        Filters.eq("user_id", userId),
                        Filters.eq("fine_paid", false)
                )
        )) {
            total += doc.getDouble("fine_amount", 0.0);
        }

        fineLabel.setText("Total Outstanding Fines: ₹" + total);
    }
}