import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;

public class StudentRecordsPanel extends JPanel {

    private DefaultTableModel model;
    private JTable table;

    public StudentRecordsPanel() {

        setLayout(new BorderLayout());

        JLabel title = new JLabel("Student Records", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        add(title, BorderLayout.NORTH);

        createTable();
        add(new JScrollPane(table), BorderLayout.CENTER);

        loadData();
    }

    private void createTable() {

        String[] cols = {
                "ID", "Name", "Email",
                "Borrowed", "Returned", "Status"
        };

        model = new DefaultTableModel(cols, 0);
        table = new JTable(model);
    }

    // ================= LOAD =================
    private void loadData() {

        model.setRowCount(0);

        MongoCollection<Document> users =
                DatabaseConnection.getCollection("users");

        MongoCollection<Document> borrowings =
                DatabaseConnection.getCollection("book_borrowings");

        for (Document user : users.find(Filters.eq("role", "STUDENT"))) {

            String id = user.getObjectId("_id").toString();

            long totalBorrowed = borrowings.countDocuments(
                    Filters.eq("user_id", id)
            );

            long returned = borrowings.countDocuments(
                    Filters.and(
                            Filters.eq("user_id", id),
                            Filters.eq("status", "RETURNED")
                    )
            );

            model.addRow(new Object[]{
                    id,
                    user.getString("full_name"),
                    user.getString("email"),
                    totalBorrowed,
                    returned,
                    user.getBoolean("is_active", true) ? "Active" : "Inactive"
            });
        }
    }

    // ================= SEARCH =================
    private void searchStudents(String text) {

        model.setRowCount(0);

        MongoCollection<Document> users =
                DatabaseConnection.getCollection("users");

        MongoCollection<Document> borrowings =
                DatabaseConnection.getCollection("book_borrowings");

        for (Document user : users.find(Filters.eq("role", "STUDENT"))) {

            String name = user.getString("full_name");
            String email = user.getString("email");

            if (!name.toLowerCase().contains(text.toLowerCase()) &&
                !email.toLowerCase().contains(text.toLowerCase())) {
                continue;
            }

            String id = user.getObjectId("_id").toString();

            long totalBorrowed = borrowings.countDocuments(
                    Filters.eq("user_id", id)
            );

            long returned = borrowings.countDocuments(
                    Filters.and(
                            Filters.eq("user_id", id),
                            Filters.eq("status", "RETURNED")
                    )
            );

            model.addRow(new Object[]{
                    id,
                    name,
                    email,
                    totalBorrowed,
                    returned,
                    user.getBoolean("is_active", true) ? "Active" : "Inactive"
            });
        }
    }

    // ================= DETAILS =================
    private void viewStudentDetails(String id) {

        MongoCollection<Document> users =
                DatabaseConnection.getCollection("users");

        Document user = users.find(
                Filters.eq("_id", new ObjectId(id))
        ).first();

        if (user == null) return;

        JOptionPane.showMessageDialog(this,
                "Name: " + user.getString("full_name") +
                "\nEmail: " + user.getString("email") +
                "\nStatus: " + (user.getBoolean("is_active", true) ? "Active" : "Inactive")
        );
    }

    // ================= HISTORY =================
    private void viewHistory(String id) {

        MongoCollection<Document> borrowings =
                DatabaseConnection.getCollection("book_borrowings");

        MongoCollection<Document> books =
                DatabaseConnection.getCollection("books");

        StringBuilder history = new StringBuilder();

        for (Document doc : borrowings.find(Filters.eq("user_id", id))) {

            Document book = books.find(
                    Filters.eq("_id", new ObjectId(doc.getString("book_id")))
            ).first();

            history.append("Book: ")
                   .append(book != null ? book.getString("title") : "Unknown")
                   .append(" | Status: ")
                   .append(doc.getString("status"))
                   .append("\n");
        }

        JOptionPane.showMessageDialog(this, history.toString());
    }
}