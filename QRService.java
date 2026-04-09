import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

import com.google.zxing.*;
import com.google.zxing.common.BitMatrix;

public class QRService {

    public static BufferedImage generateQR(String text) {

        try {
            BitMatrix matrix = new MultiFormatWriter().encode(
                    text, BarcodeFormat.QR_CODE, 300, 300);

            BufferedImage image =
                    new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);

            for (int x = 0; x < 300; x++) {
                for (int y = 0; y < 300; y++) {
                    image.setRGB(x, y,
                            matrix.get(x, y) ? 0x000000 : 0xFFFFFF);
                }
            }

            return image;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ✅ ADD THIS METHOD (FIXES YOUR ERROR)
    public static void saveQR(BufferedImage image, String fileName) {
        try {
            ImageIO.write(image, "png", new File(fileName));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}