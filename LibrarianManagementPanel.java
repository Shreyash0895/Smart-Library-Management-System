import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;

public class LibrarianManagementPanel extends JPanel {

    private JTable table;
    private DefaultTableModel model;

    public LibrarianManagementPanel() {

        setLayout(new BorderLayout());

        String[] cols = {"ID", "Username", "Name", "Email", "Status"};
        model = new DefaultTableModel(cols, 0);

        table = new JTable(model);

        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel buttons = new JPanel();

        JButton add = new JButton("Add");
        JButton deactivate = new JButton("Deactivate");
        JButton activate = new JButton("Activate");
        JButton refresh = new JButton("Refresh");

        buttons.add(add);
        buttons.add(deactivate);
        buttons.add(activate);
        buttons.add(refresh);

        add(buttons, BorderLayout.SOUTH);

        loadData();

        add.addActionListener(e -> addLibrarian());
        deactivate.addActionListener(e -> updateStatus(false));
        activate.addActionListener(e -> updateStatus(true));
        refresh.addActionListener(e -> loadData());
    }

    // ================= LOAD =================
    private void loadData() {

        model.setRowCount(0);

        MongoCollection<Document> users =
                DatabaseConnection.getCollection("users");

        for (Document doc : users.find(Filters.eq("role", "LIBRARIAN"))) {

            model.addRow(new Object[]{
                    doc.getObjectId("_id").toString(),
                    doc.getString("username"),
                    doc.getString("full_name"),
                    doc.getString("email"),
                    doc.getBoolean("is_active", true) ? "Active" : "Inactive"
            });
        }
    }

    // ================= ADD =================
    private void addLibrarian() {

        JTextField username = new JTextField();
        JTextField name = new JTextField();
        JTextField email = new JTextField();
        JPasswordField password = new JPasswordField();

        Object[] fields = {
                "Username:", username,
                "Password:", password,
                "Full Name:", name,
                "Email:", email
        };

        int option = JOptionPane.showConfirmDialog(this, fields);

        if (option == JOptionPane.OK_OPTION) {

            MongoCollection<Document> users =
                    DatabaseConnection.getCollection("users");

            Document doc = new Document()
                    .append("username", username.getText())
                    .append("password", new String(password.getPassword()))
                    .append("full_name", name.getText())
                    .append("email", email.getText())
                    .append("role", "LIBRARIAN")
                    .append("is_active", true);

            users.insertOne(doc);

            JOptionPane.showMessageDialog(this, "Added!");
            loadData();
        }
    }

    // ================= UPDATE STATUS =================
    private void updateStatus(boolean active) {

        int row = table.getSelectedRow();

        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select user");
            return;
        }

        String id = (String) model.getValueAt(row, 0);

        MongoCollection<Document> users =
                DatabaseConnection.getCollection("users");

        users.updateOne(
                Filters.eq("_id", new ObjectId(id)),
                new Document("$set", new Document("is_active", active))
        );

        JOptionPane.showMessageDialog(this, "Updated!");
        loadData();
    }
}