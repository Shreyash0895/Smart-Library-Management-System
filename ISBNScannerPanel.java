import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import com.mongodb.client.*;
import org.bson.Document;

public class ISBNScannerPanel extends JPanel {

    private JTextField isbnField;
    private JPanel resultPanel;
    private JLabel statusLabel;
    private Timer clearTimer;

    public ISBNScannerPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(245, 245, 245));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(createHeaderPanel(),  BorderLayout.NORTH);
        add(createScannerPanel(), BorderLayout.CENTER);
        add(createStatusBar(),    BorderLayout.SOUTH);
    }

    // ── Header ────────────────────────────────────────────────────────────────
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(44, 62, 80));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel title = new JLabel("📚  ISBN Book Scanner");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Enter or scan an ISBN to check book availability instantly");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(new Color(189, 195, 199));

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        textPanel.setOpaque(false);
        textPanel.add(title);
        textPanel.add(subtitle);
        panel.add(textPanel, BorderLayout.CENTER);
        return panel;
    }

    // ── Scanner input area ────────────────────────────────────────────────────
    private JPanel createScannerPanel() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 20));
        wrapper.setBackground(new Color(245, 245, 245));
        wrapper.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        // --- Input card ---
        JPanel inputCard = new JPanel(new GridBagLayout());
        inputCard.setBackground(Color.WHITE);
        inputCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
            BorderFactory.createEmptyBorder(25, 30, 25, 30)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Icon label
        JLabel scanIcon = new JLabel("🔍", SwingConstants.CENTER);
        scanIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        inputCard.add(scanIcon, gbc);

        // Instruction
        JLabel instruction = new JLabel("Enter ISBN Number", SwingConstants.CENTER);
        instruction.setFont(new Font("Segoe UI", Font.BOLD, 16));
        instruction.setForeground(new Color(44, 62, 80));
        gbc.gridy = 1;
        inputCard.add(instruction, gbc);

        // ISBN field
        isbnField = new JTextField();
        isbnField.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        isbnField.setPreferredSize(new Dimension(350, 45));
        isbnField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(52, 152, 219), 2, true),
            BorderFactory.createEmptyBorder(5, 12, 5, 12)
        ));
        isbnField.setHorizontalAlignment(JTextField.CENTER);
        isbnField.addActionListener(e -> searchByISBN()); // press Enter to search
        gbc.gridy = 2;
        inputCard.add(isbnField, gbc);

        // Buttons row
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        btnRow.setOpaque(false);

        JButton searchBtn = makeButton("🔎  Search", new Color(52, 152, 219));
        searchBtn.addActionListener(e -> searchByISBN());

        JButton clearBtn = makeButton("✖  Clear", new Color(149, 165, 166));
        clearBtn.addActionListener(e -> clearAll());

        btnRow.add(searchBtn);
        btnRow.add(clearBtn);
        gbc.gridy = 3;
        inputCard.add(btnRow, gbc);

        wrapper.add(inputCard, BorderLayout.NORTH);

        // --- Result area ---
        resultPanel = new JPanel();
        resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));
        resultPanel.setBackground(new Color(245, 245, 245));
        wrapper.add(resultPanel, BorderLayout.CENTER);

        return wrapper;
    }

    private JPanel createStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(236, 240, 241));
        bar.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));

        statusLabel = new JLabel("Ready — enter an ISBN and press Search or Enter");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(100, 100, 100));
        bar.add(statusLabel, BorderLayout.WEST);
        return bar;
    }

    // ── Search logic ──────────────────────────────────────────────────────────
    private void searchByISBN() {
        String isbn = isbnField.getText().trim();
        if (isbn.isEmpty()) {
            showStatus("⚠  Please enter an ISBN number.", new Color(230, 126, 34));
            return;
        }

        resultPanel.removeAll();
        showStatus("Searching for ISBN: " + isbn + " ...", new Color(52, 152, 219));

        try {
            MongoCollection<Document> collection = DatabaseConnection.getCollection("books");
            Document book = collection.find(new Document("isbn", isbn)).first();

            if (book != null) {
                showBookResult(book);
                showStatus("✔  Book found!", new Color(39, 174, 96));
            } else {
                showNotFound(isbn);
                showStatus("✘  No book found for ISBN: " + isbn, new Color(231, 76, 60));
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Database error: " + e.getMessage());
            showStatus("✘  Database error occurred.", new Color(231, 76, 60));
        }

        resultPanel.revalidate();
        resultPanel.repaint();
    }

    // ── Result cards ──────────────────────────────────────────────────────────
    private void showBookResult(Document book) {
        String title     = book.getString("title");
        String author    = book.getString("author");
        String isbn      = book.getString("isbn");
        int    quantity  = book.getInteger("quantity");
        int    available = book.getInteger("available");

        boolean isAvailable = available > 0;
        Color availColor = isAvailable ? new Color(39, 174, 96) : new Color(231, 76, 60);
        String availText = isAvailable
            ? "✔  AVAILABLE  (" + available + " of " + quantity + " copies)"
            : "✘  NOT AVAILABLE  (All " + quantity + " copies are issued)";

        // Availability banner
        JPanel banner = new JPanel(new FlowLayout(FlowLayout.CENTER));
        banner.setBackground(isAvailable ? new Color(212, 239, 223) : new Color(250, 219, 216));
        banner.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));
        banner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JLabel availLabel = new JLabel(availText, SwingConstants.CENTER);
        availLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        availLabel.setForeground(availColor);
        banner.add(availLabel);

        // Details card
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
            BorderFactory.createEmptyBorder(20, 30, 20, 30)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(6, 10, 6, 10);

        addDetailRow(card, gbc, 0, "📖  Title",   title);
        addDetailRow(card, gbc, 1, "✍  Author",  author);
        addDetailRow(card, gbc, 2, "🔖  ISBN",    isbn);
        addDetailRow(card, gbc, 3, "📦  Copies",  quantity + " total / " + available + " available");

        resultPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        resultPanel.add(banner);
        resultPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        resultPanel.add(card);
    }

    private void addDetailRow(JPanel card, GridBagConstraints gbc, int row,
                              String label, String value) {
        gbc.gridx = 0; gbc.gridy = row;
        JLabel lbl = new JLabel(label + ":");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(new Color(100, 100, 100));
        card.add(lbl, gbc);

        gbc.gridx = 1;
        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        val.setForeground(new Color(44, 62, 80));
        card.add(val, gbc);
    }

    private void showNotFound(String isbn) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(253, 245, 230));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 176, 80), 1, true),
            BorderFactory.createEmptyBorder(25, 30, 25, 30)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        JLabel msg = new JLabel("No book found for ISBN: " + isbn, SwingConstants.CENTER);
        msg.setFont(new Font("Segoe UI", Font.BOLD, 16));
        msg.setForeground(new Color(180, 100, 0));
        card.add(msg, BorderLayout.CENTER);

        JLabel hint = new JLabel("Please check the ISBN and try again.", SwingConstants.CENTER);
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        hint.setForeground(new Color(150, 100, 0));
        card.add(hint, BorderLayout.SOUTH);

        resultPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        resultPanel.add(card);
    }

    private void showError(String message) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(250, 219, 216));
        card.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        JLabel lbl = new JLabel("Error: " + message, SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(new Color(150, 40, 27));
        card.add(lbl, BorderLayout.CENTER);

        resultPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        resultPanel.add(card);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private void clearAll() {
        isbnField.setText("");
        resultPanel.removeAll();
        resultPanel.revalidate();
        resultPanel.repaint();
        showStatus("Ready — enter an ISBN and press Search or Enter", new Color(100, 100, 100));
        isbnField.requestFocus();
    }

    private void showStatus(String msg, Color color) {
        statusLabel.setText(msg);
        statusLabel.setForeground(color);
    }

    private JButton makeButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(140, 40));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(bg.darker());
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(bg);
            }
        });
        return btn;
    }
}