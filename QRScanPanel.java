import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.net.*;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import javax.imageio.ImageIO;

public class QRScannerPanel extends JPanel {

    private JTextField isbnField;
    private JPanel resultPanel;
    private JPanel qrDisplayPanel;
    private JLabel statusLabel;
    private String currentISBN = "";

    public QRScannerPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(245, 245, 245));
        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createMainPanel(),   BorderLayout.CENTER);
        add(createStatusBar(),   BorderLayout.SOUTH);
    }

    // ── Header ───────────────────────────────────────────────────────────────
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(44, 62, 80));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel title = new JLabel("📷  QR Code Book Scanner");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Enter ISBN → Generate QR → Scan with phone to verify availability");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(new Color(189, 195, 199));

        JPanel tp = new JPanel(new GridLayout(2, 1, 0, 4));
        tp.setOpaque(false);
        tp.add(title); tp.add(subtitle);
        panel.add(tp, BorderLayout.CENTER);
        return panel;
    }

    // ── Main split panel ─────────────────────────────────────────────────────
    private JPanel createMainPanel() {
        JPanel main = new JPanel(new GridLayout(1, 2, 15, 0));
        main.setBackground(new Color(245, 245, 245));
        main.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        main.add(createLeftPanel());
        main.add(createRightPanel());
        return main;
    }

    // ── Left: ISBN input + QR display ────────────────────────────────────────
    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(new Color(245, 245, 245));

        // Input card
        JPanel inputCard = new JPanel(new GridBagLayout());
        inputCard.setBackground(Color.WHITE);
        inputCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets    = new Insets(6, 6, 6, 6);
        gbc.fill      = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 2;

        JLabel lbl = new JLabel("Enter ISBN Number:", SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(new Color(44, 62, 80));
        gbc.gridx = 0; gbc.gridy = 0;
        inputCard.add(lbl, gbc);

        isbnField = new JTextField();
        isbnField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        isbnField.setHorizontalAlignment(JTextField.CENTER);
        isbnField.setToolTipText("e.g. 978-0743273565");
        isbnField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(52, 152, 219), 2, true),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        isbnField.addActionListener(e -> searchAndGenerate());
        gbc.gridy = 1;
        inputCard.add(isbnField, gbc);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnRow.setOpaque(false);
        JButton genBtn   = makeBtn("🔲 Generate QR",      new Color(52, 152, 219));
        JButton clearBtn = makeBtn("✖ Clear",              new Color(149, 165, 166));
        JButton openBtn  = makeBtn("🌐 Open QR in Browser", new Color(39, 174, 96));
        genBtn  .addActionListener(e -> searchAndGenerate());
        clearBtn.addActionListener(e -> clearAll());
        openBtn .addActionListener(e -> openQRInBrowser());
        btnRow.add(genBtn); btnRow.add(clearBtn);
        gbc.gridy = 2; inputCard.add(btnRow, gbc);
        gbc.gridy = 3; inputCard.add(openBtn, gbc);

        panel.add(inputCard, BorderLayout.NORTH);

        // QR display area
        qrDisplayPanel = new JPanel(new BorderLayout());
        qrDisplayPanel.setBackground(Color.WHITE);
        qrDisplayPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)));
        JLabel ph = new JLabel("QR code will appear here", SwingConstants.CENTER);
        ph.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        ph.setForeground(new Color(180, 180, 180));
        qrDisplayPanel.add(ph, BorderLayout.CENTER);
        panel.add(qrDisplayPanel, BorderLayout.CENTER);

        return panel;
    }

    // ── Right: book details ──────────────────────────────────────────────────
    private JScrollPane createRightPanel() {
        resultPanel = new JPanel();
        resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));
        resultPanel.setBackground(new Color(245, 245, 245));

        JPanel ph = new JPanel(new GridBagLayout());
        ph.setBackground(Color.WHITE);
        ph.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
            BorderFactory.createEmptyBorder(40, 20, 40, 20)));
        ph.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
        JLabel phLbl = new JLabel("Book details will appear here", SwingConstants.CENTER);
        phLbl.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        phLbl.setForeground(new Color(180, 180, 180));
        ph.add(phLbl);
        resultPanel.add(ph);

        JScrollPane scroll = new JScrollPane(resultPanel);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(new Color(245, 245, 245));
        return scroll;
    }

    // ── Status bar ───────────────────────────────────────────────────────────
    private JPanel createStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(236, 240, 241));
        bar.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        statusLabel = new JLabel("Ready — enter an ISBN and click Generate QR");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(100, 100, 100));
        bar.add(statusLabel, BorderLayout.WEST);
        JLabel hint = new JLabel("📱 Use 'Open QR in Browser' for best phone scanning");
        hint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        hint.setForeground(new Color(130, 130, 130));
        bar.add(hint, BorderLayout.EAST);
        return bar;
    }

    // ── Logic ────────────────────────────────────────────────────────────────
    private void searchAndGenerate() {
        String isbn = isbnField.getText().trim();
        if (isbn.isEmpty()) {
            showStatus("⚠  Please enter an ISBN number.", new Color(230, 126, 34)); return;
        }
        currentISBN = isbn;
        showStatus("🔄 Generating QR and searching...", new Color(52, 152, 219));

        // Run in background thread so UI doesn't freeze
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                generateAndShowQR(isbn);
                lookupBook(isbn);
                return null;
            }
        };
        worker.execute();
    }

    private void generateAndShowQR(String isbn) {
        try {
            // Fetch QR from online API
            String apiUrl = "https://api.qrserver.com/v1/create-qr-code/"
                          + "?size=220x220&data=" + URLEncoder.encode(isbn, "UTF-8")
                          + "&margin=10&format=png";
            BufferedImage qrImg = ImageIO.read(new URL(apiUrl));

            SwingUtilities.invokeLater(() -> {
                qrDisplayPanel.removeAll();

                if (qrImg != null) {
                    // Successfully got QR from API
                    JPanel inner = new JPanel(new BorderLayout(0, 8));
                    inner.setBackground(Color.WHITE);

                    JLabel titleLbl = new JLabel("Scan this QR Code", SwingConstants.CENTER);
                    titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
                    titleLbl.setForeground(new Color(44, 62, 80));

                    JLabel qrLbl = new JLabel(new ImageIcon(qrImg), SwingConstants.CENTER);

                    JLabel isbnLbl = new JLabel("ISBN: " + isbn, SwingConstants.CENTER);
                    isbnLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    isbnLbl.setForeground(new Color(52, 152, 219));

                    JLabel tipLbl = new JLabel("📱 Point your phone camera at the QR code", SwingConstants.CENTER);
                    tipLbl.setFont(new Font("Segoe UI", Font.ITALIC, 11));
                    tipLbl.setForeground(new Color(100, 100, 100));

                    JPanel bottom = new JPanel(new GridLayout(2, 1, 0, 4));
                    bottom.setBackground(Color.WHITE);
                    bottom.add(isbnLbl); bottom.add(tipLbl);

                    inner.add(titleLbl, BorderLayout.NORTH);
                    inner.add(qrLbl,    BorderLayout.CENTER);
                    inner.add(bottom,   BorderLayout.SOUTH);
                    qrDisplayPanel.add(inner, BorderLayout.CENTER);
                    showStatus("✔ QR generated! Scan with phone or click 'Open QR in Browser'", new Color(39, 174, 96));
                } else {
                    // Offline fallback
                    showOfflineQR(isbn);
                }
                qrDisplayPanel.revalidate();
                qrDisplayPanel.repaint();
            });
        } catch (Exception e) {
            SwingUtilities.invokeLater(() -> showOfflineQR(isbn));
        }
    }

    private void showOfflineQR(String isbn) {
        // Use local generator as fallback
        BufferedImage qrImg = QRCodeGenerator.generate(isbn, 220);
        qrDisplayPanel.removeAll();

        JPanel inner = new JPanel(new BorderLayout(0, 8));
        inner.setBackground(Color.WHITE);

        JLabel titleLbl = new JLabel("QR Code (Offline)", SwingConstants.CENTER);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLbl.setForeground(new Color(44, 62, 80));

        JLabel qrLbl = new JLabel(new ImageIcon(qrImg), SwingConstants.CENTER);

        JPanel tipPanel = new JPanel(new GridLayout(2, 1, 0, 3));
        tipPanel.setBackground(new Color(255, 243, 205));
        tipPanel.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));

        JLabel tip1 = new JLabel("⚠ Offline mode - for best scanning:", SwingConstants.CENTER);
        tip1.setFont(new Font("Segoe UI", Font.BOLD, 11));
        tip1.setForeground(new Color(133, 77, 14));

        JLabel tip2 = new JLabel("Click '🌐 Open QR in Browser'", SwingConstants.CENTER);
        tip2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        tip2.setForeground(new Color(133, 77, 14));

        tipPanel.add(tip1); tipPanel.add(tip2);

        JLabel isbnLbl = new JLabel("ISBN: " + isbn, SwingConstants.CENTER);
        isbnLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        isbnLbl.setForeground(new Color(52, 152, 219));

        inner.add(titleLbl, BorderLayout.NORTH);
        inner.add(qrLbl,    BorderLayout.CENTER);

        JPanel bottom = new JPanel(new GridLayout(2, 1, 0, 4));
        bottom.setBackground(Color.WHITE);
        bottom.add(isbnLbl); bottom.add(tipPanel);
        inner.add(bottom, BorderLayout.SOUTH);

        qrDisplayPanel.add(inner, BorderLayout.CENTER);
        qrDisplayPanel.revalidate();
        qrDisplayPanel.repaint();
        showStatus("⚠ Offline QR shown. Use 'Open QR in Browser' for best results.", new Color(230, 126, 34));
    }

    private void lookupBook(String isbn) {
        SwingUtilities.invokeLater(() -> {
            resultPanel.removeAll();
            showStatus("🔍 Searching for ISBN: " + isbn + "...", new Color(52, 152, 219));
        });

        try {
            MongoCollection<Document> books = DatabaseConnection.getCollection("books");
            if (books == null) {
                SwingUtilities.invokeLater(() -> showError("Cannot connect to database"));
                return;
            }
            Document book = books.find(Filters.eq("isbn", isbn)).first();

            SwingUtilities.invokeLater(() -> {
                resultPanel.removeAll();
                if (book != null) {
                    showBookResult(book, isbn);
                    showStatus("✔  Book found! Scan QR code with your phone.", new Color(39, 174, 96));
                } else {
                    showNotFound(isbn);
                    showStatus("✘  No book found for ISBN: " + isbn, new Color(231, 76, 60));
                }
                resultPanel.revalidate();
                resultPanel.repaint();
            });
        } catch (Exception e) {
            SwingUtilities.invokeLater(() -> {
                showError("Database error: " + e.getMessage());
                resultPanel.revalidate();
                resultPanel.repaint();
            });
        }
    }

    private void showBookResult(Document book, String isbn) {
        String title     = book.getString("title");
        String author    = book.getString("author");
        String category  = book.getString("category") != null ? book.getString("category") : "General";
        String shelf     = book.getString("shelf_location") != null ? book.getString("shelf_location") : "N/A";
        int    quantity  = book.getInteger("quantity", 0);
        int    available = book.getInteger("available_quantity", 0);

        boolean isAvail = available > 0;
        Color   fg = isAvail ? new Color(39,174,96)    : new Color(231,76,60);
        Color   bg = isAvail ? new Color(212,239,223)  : new Color(250,219,216);
        String  statusText = isAvail
            ? "✔  AVAILABLE  (" + available + " of " + quantity + " copies)"
            : "✘  NOT AVAILABLE  (All " + quantity + " copies issued)";

        // Availability banner
        JPanel banner = new JPanel(new FlowLayout(FlowLayout.CENTER));
        banner.setBackground(bg);
        banner.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));
        banner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        JLabel availLbl = new JLabel(statusText, SwingConstants.CENTER);
        availLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        availLbl.setForeground(fg);
        banner.add(availLbl);

        // Details card
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
            BorderFactory.createEmptyBorder(20, 25, 20, 25)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(6, 10, 6, 10);
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        addRow(card, gbc, 0, "📖  Title",          title    != null ? title    : "N/A");
        addRow(card, gbc, 1, "✍  Author",          author   != null ? author   : "N/A");
        addRow(card, gbc, 2, "🔖  ISBN",           isbn);
        addRow(card, gbc, 3, "📂  Category",       category);
        addRow(card, gbc, 4, "🗂  Shelf Location", shelf);
        addRow(card, gbc, 5, "📦  Total Copies",   String.valueOf(quantity));
        addRow(card, gbc, 6, "✅  Available",       String.valueOf(available));

        resultPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        resultPanel.add(banner);
        resultPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        resultPanel.add(card);
    }

    private void addRow(JPanel card, GridBagConstraints gbc, int row, String label, String value) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        JLabel lbl = new JLabel(label + ":");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(new Color(100, 100, 100));
        card.add(lbl, gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        val.setForeground(new Color(44, 62, 80));
        card.add(val, gbc);
    }

    private void openQRInBrowser() {
        String isbn = isbnField.getText().trim();
        if (isbn.isEmpty()) {
            showStatus("⚠  Enter an ISBN first.", new Color(230, 126, 34)); return;
        }
        try {
            String url = "https://api.qrserver.com/v1/create-qr-code/?size=400x400&data="
                       + URLEncoder.encode(isbn, "UTF-8") + "&margin=20";
            Desktop.getDesktop().browse(new URI(url));
            showStatus("✔  QR code opened in browser — scan with your phone!", new Color(39, 174, 96));
        } catch (Exception e) {
            showStatus("✘  Could not open browser: " + e.getMessage(), new Color(231, 76, 60));
        }
    }

    private void showNotFound(String isbn) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(new Color(253, 245, 230));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 176, 80), 1, true),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        JLabel msg  = new JLabel("No book found for ISBN: " + isbn, SwingConstants.CENTER);
        msg.setFont(new Font("Segoe UI", Font.BOLD, 15));
        msg.setForeground(new Color(180, 100, 0));
        JLabel hint = new JLabel("Please check the ISBN and try again.", SwingConstants.CENTER);
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        hint.setForeground(new Color(150, 100, 0));
        card.add(msg,  BorderLayout.CENTER);
        card.add(hint, BorderLayout.SOUTH);
        resultPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        resultPanel.add(card);
    }

    private void showError(String msg) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(250, 219, 216));
        card.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        JLabel l = new JLabel("Error: " + msg, SwingConstants.CENTER);
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        l.setForeground(new Color(150, 40, 27));
        card.add(l);
        resultPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        resultPanel.add(card);
    }

    private void clearAll() {
        isbnField.setText("");
        currentISBN = "";
        resultPanel.removeAll();
        qrDisplayPanel.removeAll();
        JLabel ph = new JLabel("QR code will appear here", SwingConstants.CENTER);
        ph.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        ph.setForeground(new Color(180, 180, 180));
        qrDisplayPanel.add(ph, BorderLayout.CENTER);
        qrDisplayPanel.revalidate(); qrDisplayPanel.repaint();
        resultPanel.revalidate();    resultPanel.repaint();
        showStatus("Ready — enter an ISBN and click Generate QR", new Color(100, 100, 100));
        isbnField.requestFocus();
    }

    private void showStatus(String msg, Color color) {
        statusLabel.setText(msg);
        statusLabel.setForeground(color);
    }

    private JButton makeBtn(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bg); btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false); btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(185, 38));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(bg.darker()); }
            public void mouseExited (MouseEvent e) { btn.setBackground(bg); }
        });
        return btn;
    }
}