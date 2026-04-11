import java.awt.image.BufferedImage;
import java.net.URL;
import javax.imageio.ImageIO;
import java.net.URLEncoder;

/**
 * QR Code Generator using api.qrserver.com
 * Generates real scannable QR codes via online API
 * Falls back to a simple pattern if offline
 */
public class QRCodeGenerator {

    /**
     * Generate a real scannable QR code image for the given text.
     * Uses api.qrserver.com for best phone compatibility.
     */
    public static BufferedImage generate(String text, int size) {
        try {
            // Use online QR API for real scannable QR codes
            String encodedText = URLEncoder.encode(text, "UTF-8");
            String apiUrl = "https://api.qrserver.com/v1/create-qr-code/"
                          + "?size=" + size + "x" + size
                          + "&data=" + encodedText
                          + "&margin=10"
                          + "&format=png";

            URL url = new URL(apiUrl);
            BufferedImage img = ImageIO.read(url);
            if (img != null) {
                System.out.println("✔ QR code generated from API for: " + text);
                return img;
            }
        } catch (Exception e) {
            System.out.println("⚠ Online QR failed, using offline fallback: " + e.getMessage());
        }

        // Offline fallback - simple barcode pattern
        return generateOffline(text, size);
    }

    /**
     * Offline fallback - generates a basic matrix pattern
     */
    private static BufferedImage generateOffline(String text, int size) {
        int modules = 25;
        int scale   = Math.max(1, size / modules);
        int actual  = modules * scale;

        BufferedImage img = new BufferedImage(actual, actual, BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D g = img.createGraphics();

        // White background
        g.setColor(java.awt.Color.WHITE);
        g.fillRect(0, 0, actual, actual);

        // Draw finder patterns (3 corners)
        g.setColor(java.awt.Color.BLACK);
        drawFinder(g, 0,            0,            scale);
        drawFinder(g, 0,            modules - 7,  scale);
        drawFinder(g, modules - 7,  0,            scale);

        // Timing patterns
        for (int i = 8; i < modules - 8; i++) {
            if (i % 2 == 0) {
                g.fillRect(6 * scale, i * scale, scale, scale);
                g.fillRect(i * scale, 6 * scale, scale, scale);
            }
        }

        // Encode text data
        byte[] data = text.getBytes();
        int bit = 0;
        for (int col = modules - 1; col >= 1; col -= 2) {
            if (col == 6) col--;
            for (int row = modules - 1; row >= 0; row--) {
                for (int dc = 0; dc <= 1; dc++) {
                    int c = col - dc;
                    if (!isReserved(row, c, modules)) {
                        if (bit < data.length * 8) {
                            int b = (data[bit / 8] >> (7 - bit % 8)) & 1;
                            if (b == 1) g.fillRect(c * scale, row * scale, scale, scale);
                        }
                        bit++;
                    }
                }
            }
        }
        g.dispose();
        return img;
    }

    private static void drawFinder(java.awt.Graphics2D g, int row, int col, int scale) {
        // Outer border (black)
        g.setColor(java.awt.Color.BLACK);
        for (int r = 0; r < 7; r++) {
            for (int c = 0; c < 7; c++) {
                if (r == 0 || r == 6 || c == 0 || c == 6) {
                    g.fillRect((col + c) * scale, (row + r) * scale, scale, scale);
                }
            }
        }
        // Inner white
        g.setColor(java.awt.Color.WHITE);
        g.fillRect((col + 1) * scale, (row + 1) * scale, 5 * scale, 5 * scale);
        // Inner black square
        g.setColor(java.awt.Color.BLACK);
        g.fillRect((col + 2) * scale, (row + 2) * scale, 3 * scale, 3 * scale);
    }

    private static boolean isReserved(int r, int c, int size) {
        if (r < 9 && c < 9)          return true;
        if (r < 9 && c >= size - 8)  return true;
        if (r >= size - 8 && c < 9)  return true;
        if (r == 6 || c == 6)        return true;
        return false;
    }
}