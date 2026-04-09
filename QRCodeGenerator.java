import com.mongodb.client.*;
import org.bson.Document;

public class QRCodeGenerator {

    public static void main(String[] args) {

        MongoCollection<Document> books =
                DatabaseConnection.getCollection("books");

        for (Document book : books.find()) {

            String id = book.getObjectId("_id").toString();

            java.awt.image.BufferedImage qr =
                    QRService.generateQR(id);

            QRService.saveQR(qr, "QR_" + id + ".png");

            System.out.println("QR Generated for: " + id);
        }

        System.out.println("All QR Codes Generated Successfully!");
    }
}