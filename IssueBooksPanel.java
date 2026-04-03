import javax.swing.*;
import java.awt.*;
import com.mongodb.client.*;
import org.bson.Document;

public class IssueBooksPanel extends JPanel {

    private JTextField userIdField, bookIdField;

    public IssueBooksPanel() {
        setLayout(new GridLayout(3, 2));

        userIdField = new JTextField();
        bookIdField = new JTextField();

        add(new JLabel("User ID")); add(userIdField);
        add(new JLabel("Book ID")); add(bookIdField);

        JButton issue = new JButton("Issue Book");
        add(issue);

        issue.addActionListener(e -> issueBook());
    }

    private void issueBook() {
        MongoCollection<Document> borrowings =
                DatabaseConnection.getCollection("book_borrowings");

        Document record = new Document()
                .append("user_id", userIdField.getText())
                .append("book_id", bookIdField.getText())
                .append("status", "BORROWED");

        borrowings.insertOne(record);

        JOptionPane.showMessageDialog(this, "Book issued!");
    }
}