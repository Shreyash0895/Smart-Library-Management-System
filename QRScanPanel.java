import javax.swing.*;
import java.awt.*;
import com.mongodb.client.*;
import org.bson.Document;
import org.bson.types.ObjectId;

public class QRScanPanel extends JPanel {

    public QRScanPanel() {

        setLayout(new BorderLayout());

        JTextField input = new JTextField();
        JButton searchBtn = new JButton("Scan QR");

        JTextArea result = new JTextArea();
        result.setEditable(false);
        result.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JPanel top = new JPanel(new BorderLayout());
        top.add(input, BorderLayout.CENTER);
        top.add(searchBtn, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(result), BorderLayout.CENTER);

        searchBtn.addActionListener(e -> loadBook(input.getText(), result));
    }

    private void loadBook(String bookId, JTextArea result) {

        try {
            MongoCollection<Document> books =
                    DatabaseConnection.getCollection("books");

            Document book = books.find(
                    new Document("_id", new ObjectId(bookId))
            ).first();

            if (book != null) {

                result.setText(
                        "📘 Title: " + book.getString("title") + "\n\n" +
                        "✍ Author: " + book.getString("author") + "\n\n" +
                        "📂 Category: " + book.getString("category") + "\n\n" +
                        "📦 Available Copies: " + book.getInteger("available")
                );

            } else {
                result.setText("❌ Book not found");
            }

        } catch (Exception e) {
            result.setText("⚠ Invalid QR Code");
        }
    }
}