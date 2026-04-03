import com.mongodb.client.*;
import com.mongodb.client.model.*;
import org.bson.Document;
import javax.swing.JOptionPane;
import java.util.*;

public class DatabaseConnection {
    private static final String CONNECTION_STRING = System.getenv("MONGO_CONNECTION_STRING") != null ? System.getenv("MONGO_CONNECTION_STRING") : "mongodb://localhost:27017";
    private static final String DB_NAME = "library_management";
    private static MongoClient mongoClient = null;
    private static MongoDatabase database = null;

    public static MongoDatabase getDatabase() {
        try {
            if (mongoClient != null && database != null) return database;
            mongoClient = MongoClients.create(CONNECTION_STRING);
            database = mongoClient.getDatabase(DB_NAME);
            database.runCommand(new Document("ping", 1));
            System.out.println("✔ MongoDB connected!");
            initializeCollections();
            insertDefaultAdmin();
            insert25Books();
            return database;
        } catch (Exception e) {
            showError("Cannot connect to MongoDB.\n\nMake sure MongoDB is running!\nOpen MongoDB Compass → connect to mongodb://localhost:27017\n\nError: " + e.getMessage());
            return null;
        }
    }

    public static MongoCollection<Document> getCollection(String name) {
        MongoDatabase db = getDatabase();
        if (db == null) return null;
        return db.getCollection(name);
    }

    private static void initializeCollections() {
        String[] cols = {"users","books","issued_books","book_borrowings","book_requests","fines","notifications","settings"};
        Set<String> existing = new HashSet<>();
        for (String c : database.listCollectionNames()) existing.add(c);
        for (String c : cols) { if (!existing.contains(c)) { database.createCollection(c); } }
        System.out.println("✔ Collections ready!");
    }

    private static void insertDefaultAdmin() {
        MongoCollection<Document> users = database.getCollection("users");
        if (users.find(Filters.eq("username", "admin")).first() == null) {
            users.insertOne(new Document()
                .append("full_name","Administrator").append("username","admin")
                .append("password","admin123").append("role","ADMIN").append("type","ADMIN")
                .append("email","admin@library.com").append("is_approved",true)
                .append("is_active",true).append("created_at",new java.util.Date()));
            System.out.println("✔ Admin created: admin/admin123");
        }
    }

    private static void insert25Books() {
        MongoCollection<Document> books = database.getCollection("books");
        if (books.countDocuments() >= 10) return;
        String[][] data = {
            {"The Great Gatsby","F. Scott Fitzgerald","978-0743273565","Classic Literature","4","C1"},
            {"To Kill a Mockingbird","Harper Lee","978-0061935466","Classic Literature","3","C2"},
            {"1984","George Orwell","978-0451524935","Classic Literature","5","C3"},
            {"Animal Farm","George Orwell","978-0451526342","Classic Literature","4","C4"},
            {"Brave New World","Aldous Huxley","978-0060850524","Classic Literature","3","C5"},
            {"Clean Code","Robert C. Martin","978-0132350884","Technology","5","T1"},
            {"Python Crash Course","Eric Matthes","978-1593279288","Technology","5","T2"},
            {"Java The Complete Reference","Herbert Schildt","978-1260440249","Technology","4","T3"},
            {"Introduction to Algorithms","Thomas H. Cormen","978-0262033848","Technology","3","T4"},
            {"The Pragmatic Programmer","Andrew Hunt","978-0201616224","Technology","4","T5"},
            {"Atomic Habits","James Clear","978-0735211292","Self Help","6","S1"},
            {"Rich Dad Poor Dad","Robert T. Kiyosaki","978-1612680194","Self Help","5","S2"},
            {"The Alchemist","Paulo Coelho","978-0062315007","Self Help","6","S3"},
            {"Think and Grow Rich","Napoleon Hill","978-1585424337","Self Help","4","S4"},
            {"The 7 Habits","Stephen R. Covey","978-0743269513","Self Help","5","S5"},
            {"Sapiens","Yuval Noah Harari","978-0062316097","Science","5","SC1"},
            {"A Brief History of Time","Stephen Hawking","978-0553380163","Science","4","SC2"},
            {"Homo Deus","Yuval Noah Harari","978-0062464316","Science","4","SC3"},
            {"The Selfish Gene","Richard Dawkins","978-0198788607","Science","3","SC4"},
            {"The Gene","Siddhartha Mukherjee","978-1476733500","Science","3","SC5"},
            {"Harry Potter Sorcerer Stone","J.K. Rowling","978-0439708180","Fiction","7","F1"},
            {"The Hobbit","J.R.R. Tolkien","978-0547928227","Fiction","5","F2"},
            {"Zero to One","Peter Thiel","978-0804139021","Business","4","B1"},
            {"The Art of War","Sun Tzu","978-1599869773","History","5","H1"},
            {"Guns Germs and Steel","Jared Diamond","978-0393354324","History","3","H2"}
        };
        List<Document> list = new ArrayList<>();
        for (String[] b : data) {
            int qty = Integer.parseInt(b[4]);
            list.add(new Document().append("title",b[0]).append("author",b[1]).append("isbn",b[2])
                .append("category",b[3]).append("quantity",qty).append("available",qty)
                .append("available_quantity",qty).append("shelf_location",b[5])
                .append("is_active",true).append("created_at",new java.util.Date()));
        }
        books.insertMany(list);
        System.out.println("✔ 25 books inserted!");
    }

    public static void closeConnection() {
        if (mongoClient != null) { mongoClient.close(); mongoClient = null; database = null; }
    }

    private static void showError(String msg) {
        JOptionPane.showMessageDialog(null, msg, "Database Connection Error", JOptionPane.ERROR_MESSAGE);
    }
}