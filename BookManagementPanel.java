import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import com.mongodb.client.*;
import org.bson.Document;
import org.bson.types.ObjectId;

public class BookManagementPanel extends JPanel {

    private JTable table;
    private DefaultTableModel model;

    public BookManagementPanel() {

        setLayout(new BorderLayout());

        model = new DefaultTableModel(
                new String[]{"ID", "Title", "Author", "Category", "Available"}, 0);

        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel panel = new JPanel();

        JButton addBtn = new JButton("Add Book");
        JButton refreshBtn = new JButton("Refresh");
        JButton qrBtn = new JButton("Generate QR");

        panel.add(addBtn);
        panel.add(refreshBtn);
        panel.add(qrBtn);

        add(panel, BorderLayout.SOUTH);

        loadBooks();

        addBtn.addActionListener(e -> addBook());
        refreshBtn.addActionListener(e -> loadBooks());
        qrBtn.addActionListener(e -> generateQR());
    }

    // 📚 LOAD BOOKS
    private void loadBooks() {
        model.setRowCount(0);

        MongoCollection<Document> books =
                DatabaseConnection.getCollection("books");

        for (Document book : books.find()) {

            model.addRow(new Object[]{
                    book.getObjectId("_id").toString(),
                    book.getString("title"),
                    book.getString("author"),
                    book.getString("category"),
                    book.getInteger("available")
            });
        }
    }

    // ➕ ADD BOOK
    private void addBook() {

        JTextField title = new JTextField();
        JTextField author = new JTextField();
        JTextField category = new JTextField();
        JTextField available = new JTextField();

        Object[] fields = {
                "Title:", title,
                "Author:", author,
                "Category:", category,
                "Available:", available
        };

        int option = JOptionPane.showConfirmDialog(
                this, fields, "Add Book", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {

            MongoCollection<Document> books =
                    DatabaseConnection.getCollection("books");

            books.insertOne(new Document()
                    .append("title", title.getText())
                    .append("author", author.getText())
                    .append("category", category.getText())
                    .append("available", Integer.parseInt(available.getText()))
            );

            JOptionPane.showMessageDialog(this, "Book Added!");
            loadBooks();
        }
    }

    // 🔥 GENERATE QR
    private void generateQR() {

        int row = table.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a book!");
            return;
        }

        String bookId = model.getValueAt(row, 0).toString();

        java.awt.image.BufferedImage qr =
                QRService.generateQR(bookId);

        JLabel label = new JLabel(new ImageIcon(qr));

        JOptionPane.showMessageDialog(this, label, "QR Code",
                JOptionPane.PLAIN_MESSAGE);

        QRService.saveQR(qr, "QR_" + bookId + ".png");

        MongoCollection<Document> books =
                DatabaseConnection.getCollection("books");

        books.updateOne(
                new Document("_id", new ObjectId(bookId)),
                new Document("$set", new Document("qrGenerated", true))
        );

        JOptionPane.showMessageDialog(this, "QR Saved!");
    }
}