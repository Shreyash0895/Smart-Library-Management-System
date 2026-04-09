import javax.swing.*;
import java.awt.*;
import com.mongodb.client.*;
import org.bson.Document;

public class ISBNPanel extends JPanel {

    private JTextField isbnField;
    private JLabel qrLabel;
    private JTextArea resultArea;

    public ISBNPanel() {

        setLayout(new BorderLayout(10, 10));

        JPanel top = new JPanel(new BorderLayout());

        isbnField = new JTextField();
        JButton btn = new JButton("Generate QR & Show Book");

        top.add(new JLabel("Enter ISBN:"), BorderLayout.WEST);
        top.add(isbnField, BorderLayout.CENTER);
        top.add(btn, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);

        qrLabel = new JLabel("", SwingConstants.CENTER);
        add(qrLabel, BorderLayout.CENTER);

        resultArea = new JTextArea();
        resultArea.setEditable(false);

        add(new JScrollPane(resultArea), BorderLayout.SOUTH);

        btn.addActionListener(e -> processISBN());
    }

    private void processISBN() {

        String isbn = isbnField.getText().trim();

        if (isbn.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter ISBN");
            return;
        }

        // QR
        java.awt.image.BufferedImage qr =
                QRService.generateQR(isbn);

        qrLabel.setIcon(new ImageIcon(qr));

        // DB
        MongoCollection<Document> books =
                DatabaseConnection.getCollection("books");

        Document book = books.find(new Document("isbn", isbn)).first();

        if (book != null) {
            resultArea.setText(
                    "Title: " + book.getString("title") + "\n" +
                    "Author: " + book.getString("author") + "\n" +
                    "Available: " + book.getInteger("available")
            );
        } else {
            resultArea.setText("Book not found");
        }
    }
}