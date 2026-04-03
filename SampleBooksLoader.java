import com.mongodb.client.*;
import org.bson.Document;
import java.util.Arrays;
import java.util.List;

public class SampleBooksLoader {

    public static void main(String[] args) {
        loadSampleBooks();
    }

    public static void loadSampleBooks() {
        try {
            MongoCollection<Document> collection = DatabaseConnection.getCollection("books");

            // Clear existing books (optional)
            // collection.deleteMany(new Document());

            List<Document> books = Arrays.asList(
                new Document("title", "To Kill a Mockingbird")
                    .append("author", "Harper Lee")
                    .append("isbn", "978-0-06-112008-4")
                    .append("quantity", 5)
                    .append("available", 3)
                    .append("description", "A gripping tale of racial injustice and childhood innocence"),

                new Document("title", "1984")
                    .append("author", "George Orwell")
                    .append("isbn", "978-0-451-52494-2")
                    .append("quantity", 4)
                    .append("available", 2)
                    .append("description", "A dystopian classic about totalitarianism"),

                new Document("title", "The Great Gatsby")
                    .append("author", "F. Scott Fitzgerald")
                    .append("isbn", "978-0-7432-7356-5")
                    .append("quantity", 6)
                    .append("available", 4)
                    .append("description", "A tale of ambition and love in the Jazz Age"),

                new Document("title", "Pride and Prejudice")
                    .append("author", "Jane Austen")
                    .append("isbn", "978-0-14-143951-8")
                    .append("quantity", 7)
                    .append("available", 5)
                    .append("description", "A romantic novel of manners and marriage"),

                new Document("title", "The Catcher in the Rye")
                    .append("author", "J.D. Salinger")
                    .append("isbn", "978-0-316-76948-0")
                    .append("quantity", 3)
                    .append("available", 1)
                    .append("description", "The journey of a teenage truant through New York City"),

                new Document("title", "Brave New World")
                    .append("author", "Aldous Huxley")
                    .append("isbn", "978-0-06-085052-4")
                    .append("quantity", 4)
                    .append("available", 2)
                    .append("description", "A futuristic society controlled by pleasure and conformity"),

                new Document("title", "Jane Eyre")
                    .append("author", "Charlotte Brontë")
                    .append("isbn", "978-0-14-043205-9")
                    .append("quantity", 5)
                    .append("available", 3)
                    .append("description", "A gothic romance featuring a strong-willed protagonist"),

                new Document("title", "Wuthering Heights")
                    .append("author", "Emily Brontë")
                    .append("isbn", "978-0-14-143951-8")
                    .append("quantity", 4)
                    .append("available", 2)
                    .append("description", "A dark tale of love and revenge on the Yorkshire moors"),

                new Document("title", "The Hobbit")
                    .append("author", "J.R.R. Tolkien")
                    .append("isbn", "978-0-547-92822-8")
                    .append("quantity", 6)
                    .append("available", 4)
                    .append("description", "An adventure fantasy about a hobbit's unexpected journey"),

                new Document("title", "The Lord of the Rings")
                    .append("author", "J.R.R. Tolkien")
                    .append("isbn", "978-0-547-92822-9")
                    .append("quantity", 5)
                    .append("available", 3)
                    .append("description", "An epic fantasy trilogy of Middle-earth"),

                new Document("title", "Harry Potter and the Philosopher's Stone")
                    .append("author", "J.K. Rowling")
                    .append("isbn", "978-0-747-53269-9")
                    .append("quantity", 8)
                    .append("available", 5)
                    .append("description", "The first magical adventure of a young wizard"),

                new Document("title", "The Da Vinci Code")
                    .append("author", "Dan Brown")
                    .append("isbn", "978-0-307-46955-7")
                    .append("quantity", 4)
                    .append("available", 2)
                    .append("description", "A mystery thriller involving art, history, and conspiracy"),

                new Document("title", "The Alchemist")
                    .append("author", "Paulo Coelho")
                    .append("isbn", "978-0-06-231500-1")
                    .append("quantity", 5)
                    .append("available", 3)
                    .append("description", "A philosophical novel about following your dreams"),

                new Document("title", "Atomic Habits")
                    .append("author", "James Clear")
                    .append("isbn", "978-0-735-21141-8")
                    .append("quantity", 7)
                    .append("available", 5)
                    .append("description", "A practical guide to building good habits and breaking bad ones"),

                new Document("title", "Sapiens")
                    .append("author", "Yuval Noah Harari")
                    .append("isbn", "978-0-062-31657-1")
                    .append("quantity", 6)
                    .append("available", 4)
                    .append("description", "A historical account of humankind from the Stone Age to modern times")
            );

            // Insert books into collection
            collection.insertMany(books);
            System.out.println("✓ " + books.size() + " sample books have been successfully added to the database!");

        } catch (Exception e) {
            System.err.println("Error loading books: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
