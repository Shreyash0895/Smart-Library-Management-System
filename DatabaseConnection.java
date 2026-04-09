import com.mongodb.client.*;
import com.mongodb.client.model.*;
import org.bson.Document;
import org.bson.types.ObjectId;
import javax.swing.JOptionPane;
import java.util.*;

public class DatabaseConnection {
    private static final String CONNECTION_STRING = "mongodb://localhost:27017";
    private static final String DB_NAME = "library_management";
    private static MongoClient mongoClient = null;
    private static MongoDatabase database = null;

    // ── Get Database ─────────────────────────────────────────────────────────────
    public static MongoDatabase getDatabase() {
        try {
            if (mongoClient != null && database != null) return database;

            mongoClient = MongoClients.create(CONNECTION_STRING);
            database = mongoClient.getDatabase(DB_NAME);

            // Test connection
            database.runCommand(new Document("ping", 1));
            System.out.println("✔ MongoDB connected successfully!");

            initializeCollections();
            insertDefaultAdmin();
            insert50Books();

            return database;
        } catch (Exception e) {
            showError("Cannot connect to MongoDB.\n\nPlease check:\n" +
                      "1. MongoDB is running\n" +
                      "2. Open MongoDB Compass → connect to mongodb://localhost:27017\n\n" +
                      "Error: " + e.getMessage());
            return null;
        }
    }

    // ── Get Collection ───────────────────────────────────────────────────────────
    public static MongoCollection<Document> getCollection(String name) {
        MongoDatabase db = getDatabase();
        if (db == null) return null;
        return db.getCollection(name);
    }

    // ── Initialize Collections ───────────────────────────────────────────────────
    private static void initializeCollections() {
        String[] collections = {
            "users", "books", "issued_books", "book_borrowings",
            "book_requests", "book_reviews", "fines", "notifications", "settings"
        };
        Set<String> existing = new HashSet<>();
        for (String c : database.listCollectionNames()) existing.add(c);
        for (String c : collections) {
            if (!existing.contains(c)) {
                database.createCollection(c);
                System.out.println("✔ Created collection: " + c);
            }
        }
        System.out.println("✔ All collections initialized!");
    }

    // ── Insert Default Admin ─────────────────────────────────────────────────────
    private static void insertDefaultAdmin() {
        MongoCollection<Document> users = database.getCollection("users");
        if (users.find(Filters.eq("username", "admin")).first() == null) {
            users.insertOne(new Document()
                .append("full_name",   "Administrator")
                .append("username",    "admin")
                .append("password",    "admin123")
                .append("role",        "ADMIN")
                .append("type",        "ADMIN")
                .append("email",       "admin@library.com")
                .append("is_approved", true)
                .append("is_active",   true)
                .append("created_at",  new java.util.Date()));
            System.out.println("✔ Default admin created: admin / admin123");
        }
    }

    // ── Insert 50 Books ──────────────────────────────────────────────────────────
    private static void insert50Books() {
        MongoCollection<Document> books = database.getCollection("books");
        if (books.countDocuments() >= 10) return;

        String[][] bookData = {
            {"The Great Gatsby",                   "F. Scott Fitzgerald",  "978-0743273565", "Classic Literature", "4", "C1"},
            {"To Kill a Mockingbird",              "Harper Lee",           "978-0061935466", "Classic Literature", "3", "C2"},
            {"1984",                               "George Orwell",        "978-0451524935", "Classic Literature", "5", "C3"},
            {"Animal Farm",                        "George Orwell",        "978-0451526342", "Classic Literature", "4", "C4"},
            {"Brave New World",                    "Aldous Huxley",        "978-0060850524", "Classic Literature", "3", "C5"},
            {"The Catcher in the Rye",             "J.D. Salinger",        "978-0316769174", "Classic Literature", "2", "C6"},
            {"Of Mice and Men",                    "John Steinbeck",       "978-0140177398", "Classic Literature", "3", "C7"},
            {"Lord of the Flies",                  "William Golding",      "978-0399501487", "Classic Literature", "4", "C8"},
            {"The Old Man and the Sea",            "Ernest Hemingway",     "978-0684801223", "Classic Literature", "3", "C9"},
            {"Jane Eyre",                          "Charlotte Bronte",     "978-0142437209", "Classic Literature", "2", "C10"},
            {"Clean Code",                         "Robert C. Martin",     "978-0132350884", "Technology",         "5", "T1"},
            {"The Pragmatic Programmer",           "Andrew Hunt",          "978-0201616224", "Technology",         "4", "T2"},
            {"Introduction to Algorithms",         "Thomas H. Cormen",     "978-0262033848", "Technology",         "3", "T3"},
            {"Design Patterns",                    "Gang of Four",         "978-0201633610", "Technology",         "3", "T4"},
            {"Artificial Intelligence",            "Stuart Russell",       "978-0136042594", "Technology",         "4", "T5"},
            {"Deep Learning",                      "Ian Goodfellow",       "978-0262035613", "Technology",         "2", "T6"},
            {"Python Crash Course",                "Eric Matthes",         "978-1593279288", "Technology",         "5", "T7"},
            {"Java: The Complete Reference",       "Herbert Schildt",      "978-1260440249", "Technology",         "4", "T8"},
            {"Database System Concepts",           "Abraham Silberschatz", "978-0078022159", "Technology",         "3", "T9"},
            {"Computer Networks",                  "Andrew Tanenbaum",     "978-0132126953", "Technology",         "3", "T10"},
            {"Atomic Habits",                      "James Clear",          "978-0735211292", "Self Help",          "6", "S1"},
            {"The 7 Habits",                       "Stephen R. Covey",     "978-0743269513", "Self Help",          "5", "S2"},
            {"Think and Grow Rich",                "Napoleon Hill",        "978-1585424337", "Self Help",          "4", "S3"},
            {"The Power of Now",                   "Eckhart Tolle",        "978-1577314806", "Self Help",          "3", "S4"},
            {"Rich Dad Poor Dad",                  "Robert T. Kiyosaki",   "978-1612680194", "Self Help",          "5", "S5"},
            {"How to Win Friends",                 "Dale Carnegie",        "978-0671027032", "Self Help",          "4", "S6"},
            {"The Subtle Art",                     "Mark Manson",          "978-0062457714", "Self Help",          "4", "S7"},
            {"Mindset",                            "Carol S. Dweck",       "978-0345472328", "Self Help",          "3", "S8"},
            {"Ikigai",                             "Hector Garcia",        "978-0143130727", "Self Help",          "5", "S9"},
            {"The Alchemist",                      "Paulo Coelho",         "978-0062315007", "Self Help",          "6", "S10"},
            {"A Brief History of Time",            "Stephen Hawking",      "978-0553380163", "Science",            "4", "SC1"},
            {"The Selfish Gene",                   "Richard Dawkins",      "978-0198788607", "Science",            "3", "SC2"},
            {"Sapiens",                            "Yuval Noah Harari",    "978-0062316097", "Science",            "5", "SC3"},
            {"Homo Deus",                          "Yuval Noah Harari",    "978-0062464316", "Science",            "4", "SC4"},
            {"The Gene",                           "Siddhartha Mukherjee", "978-1476733500", "Science",            "3", "SC5"},
            {"Harry Potter Sorcerer Stone",        "J.K. Rowling",         "978-0439708180", "Fiction",            "7", "F1"},
            {"The Hobbit",                         "J.R.R. Tolkien",       "978-0547928227", "Fiction",            "5", "F2"},
            {"The Da Vinci Code",                  "Dan Brown",            "978-0307474278", "Fiction",            "5", "F3"},
            {"Gone Girl",                          "Gillian Flynn",        "978-0307588371", "Fiction",            "4", "F4"},
            {"The Girl with the Dragon Tattoo",    "Stieg Larsson",        "978-0307949486", "Fiction",            "3", "F5"},
            {"Zero to One",                        "Peter Thiel",          "978-0804139021", "Business",           "4", "B1"},
            {"The Lean Startup",                   "Eric Ries",            "978-0307887894", "Business",           "4", "B2"},
            {"Good to Great",                      "Jim Collins",          "978-0066620992", "Business",           "3", "B3"},
            {"Start With Why",                     "Simon Sinek",          "978-1591846444", "Business",           "4", "B4"},
            {"The Innovators Dilemma",             "Clayton Christensen",  "978-1633691780", "Business",           "3", "B5"},
            {"The Art of War",                     "Sun Tzu",              "978-1599869773", "History",            "5", "H1"},
            {"Guns Germs and Steel",               "Jared Diamond",        "978-0393354324", "History",            "3", "H2"},
            {"The Diary of a Young Girl",          "Anne Frank",           "978-0553296983", "History",            "4", "H3"},
            {"Civilization",                       "Niall Ferguson",       "978-0143122975", "History",            "3", "H4"},
            {"The Rise and Fall of Third Reich",   "William L. Shirer",    "978-1451651683", "History",            "2", "H5"},
        };

        List<Document> bookDocs = new ArrayList<>();
        for (String[] b : bookData) {
            int qty = Integer.parseInt(b[4]);
            bookDocs.add(new Document()
                .append("title",              b[0])
                .append("author",             b[1])
                .append("isbn",               b[2])
                .append("category",           b[3])
                .append("quantity",           qty)
                .append("available",          qty)
                .append("available_quantity", qty)
                .append("shelf_location",     b[5])
                .append("is_active",          true)
                .append("created_at",         new java.util.Date()));
        }
        books.insertMany(bookDocs);
        System.out.println("✔ 50 books inserted successfully!");
    }

    // ── Close ────────────────────────────────────────────────────────────────────
    public static void closeConnection() {
        if (mongoClient != null) {
            mongoClient.close();
            mongoClient = null;
            database = null;
        }
    }

    private static void showError(String message) {
        JOptionPane.showMessageDialog(null, message, "Database Connection Error", JOptionPane.ERROR_MESSAGE);
    }
}