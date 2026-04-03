import javax.swing.*;
import java.awt.*;
import com.mongodb.client.*;
import org.bson.Document;

public class BookManagementPanel extends JPanel {

    private JTextField title, author, isbn;

    public BookManagementPanel() {
        setLayout(new GridLayout(4, 2));

        title = new JTextField();
        author = new JTextField();
        isbn = new JTextField();

        add(new JLabel("Title")); add(title);
        add(new JLabel("Author")); add(author);
        add(new JLabel("ISBN")); add(isbn);

        JButton addBook = new JButton("Add Book");
        add(addBook);

        addBook.addActionListener(e -> addBook());
    }

    private void addBook() {
        MongoCollection<Document> books = DatabaseConnection.getCollection("books");

        Document book = new Document()
                .append("title", title.getText())
                .append("author", author.getText())
                .append("isbn", isbn.getText());

        books.insertOne(book);

        JOptionPane.showMessageDialog(this, "Book added successfully!");
    }
}