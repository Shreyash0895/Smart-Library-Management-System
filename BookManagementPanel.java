import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import com.mongodb.client.*;
import com.mongodb.client.model.*;
import org.bson.Document;
import org.bson.types.ObjectId;

public class BookManagementPanel extends JPanel {
    private String userId;
    private boolean isDarkMode;
    private JTable booksTable;
    private DefaultTableModel tableModel;
    private JTextField titleField, authorField, categoryField, quantityField, isbnField;
    private Color darkBackground  = new Color(33, 33, 33);
    private Color lightBackground = new Color(242, 242, 242);

    // Constructor with String userId
    public BookManagementPanel(String userId, boolean isDarkMode) {
        this.userId    = userId;
        this.isDarkMode = isDarkMode;
        setLayout(new BorderLayout(10, 10));
        setBackground(isDarkMode ? darkBackground : lightBackground);
        initializeComponents();
        loadBooks();
    }

    private void initializeComponents() {
        JPanel inputPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        inputPanel.setBackground(isDarkMode ? darkBackground : lightBackground);
        titleField    = new JTextField(20);
        authorField   = new JTextField(20);
        categoryField = new JTextField(20);
        quantityField = new JTextField(20);
        isbnField     = new JTextField(20);
        addLabelAndField(inputPanel, "Title:",    titleField);
        addLabelAndField(inputPanel, "Author:",   authorField);
        addLabelAndField(inputPanel, "Category:", categoryField);
        addLabelAndField(inputPanel, "Quantity:", quantityField);
        addLabelAndField(inputPanel, "ISBN:",     isbnField);

        tableModel = new DefaultTableModel(
            new String[]{"ID", "Title", "Author", "Category", "Quantity", "Available"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        booksTable = new JTable(tableModel);
        booksTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = booksTable.getSelectedRow();
                if (row != -1) {
                    titleField   .setText((String) tableModel.getValueAt(row, 1));
                    authorField  .setText((String) tableModel.getValueAt(row, 2));
                    categoryField.setText((String) tableModel.getValueAt(row, 3));
                    quantityField.setText(String.valueOf(tableModel.getValueAt(row, 4)));
                }
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(isDarkMode ? darkBackground : lightBackground);
        JButton addBtn    = createButton("Add Book");
        JButton updateBtn = createButton("Update Book");
        JButton deleteBtn = createButton("Delete Book");
        JButton clearBtn  = createButton("Clear Fields");
        addBtn   .addActionListener(e -> addBook());
        updateBtn.addActionListener(e -> updateBook());
        deleteBtn.addActionListener(e -> deleteBook());
        clearBtn .addActionListener(e -> clearFields());
        buttonPanel.add(addBtn); buttonPanel.add(updateBtn);
        buttonPanel.add(deleteBtn); buttonPanel.add(clearBtn);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(isDarkMode ? darkBackground : lightBackground);
        topPanel.add(inputPanel,  BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(booksTable), BorderLayout.CENTER);
    }

    private void addLabelAndField(JPanel p, String text, JTextField f) {
        JLabel l = new JLabel(text);
        l.setForeground(isDarkMode ? Color.WHITE : Color.BLACK);
        p.add(l); p.add(f);
    }

    private JButton createButton(String text) {
        JButton b = new JButton(text);
        b.setBackground(new Color(70, 130, 180));
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        return b;
    }

    private void loadBooks() {
        tableModel.setRowCount(0);
        try {
            for (Document doc : DatabaseConnection.getCollection("books")
                    .find(Filters.eq("is_active", true))) {
                tableModel.addRow(new Object[]{
                    doc.getObjectId("_id").toString(),
                    doc.getString("title"),
                    doc.getString("author"),
                    doc.getString("category"),
                    doc.getInteger("quantity", 0),
                    doc.getInteger("available_quantity", 0)
                });
            }
        } catch (Exception ex) { showError("Error loading books: " + ex.getMessage()); }
    }

    private void addBook() {
        if (!validateInputs()) return;
        try {
            MongoCollection<Document> books = DatabaseConnection.getCollection("books");
            if (books.find(Filters.eq("isbn", isbnField.getText().trim())).first() != null) {
                showError("A book with this ISBN already exists"); return;
            }
            int qty = Integer.parseInt(quantityField.getText().trim());
            books.insertOne(new Document()
                .append("title",              titleField.getText().trim())
                .append("author",             authorField.getText().trim())
                .append("category",           categoryField.getText().trim())
                .append("isbn",               isbnField.getText().trim())
                .append("quantity",           qty)
                .append("available",          qty)
                .append("available_quantity", qty)
                .append("is_active",          true)
                .append("created_at",         new java.util.Date()));
            showSuccess("Book added successfully");
            clearFields(); loadBooks();
        } catch (NumberFormatException ex) { showError("Quantity must be a number"); }
        catch (Exception ex) { showError("Error adding book: " + ex.getMessage()); }
    }

    private void updateBook() {
        int row = booksTable.getSelectedRow();
        if (row == -1) { showError("Please select a book to update"); return; }
        if (!validateInputs()) return;
        try {
            String id     = (String) tableModel.getValueAt(row, 0);
            int newQty    = Integer.parseInt(quantityField.getText().trim());
            int oldQty    = (int) tableModel.getValueAt(row, 4);
            int diff      = newQty - oldQty;
            DatabaseConnection.getCollection("books").updateOne(
                Filters.eq("_id", new ObjectId(id)),
                Updates.combine(
                    Updates.set("title",    titleField.getText().trim()),
                    Updates.set("author",   authorField.getText().trim()),
                    Updates.set("category", categoryField.getText().trim()),
                    Updates.set("quantity", newQty),
                    Updates.inc("available_quantity", diff)));
            showSuccess("Book updated successfully");
            clearFields(); loadBooks();
        } catch (Exception ex) { showError("Error updating book: " + ex.getMessage()); }
    }

    private void deleteBook() {
        int row = booksTable.getSelectedRow();
        if (row == -1) { showError("Please select a book to delete"); return; }
        int c = JOptionPane.showConfirmDialog(this, "Delete this book?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (c == JOptionPane.YES_OPTION) {
            try {
                String id = (String) tableModel.getValueAt(row, 0);
                DatabaseConnection.getCollection("books").updateOne(
                    Filters.eq("_id", new ObjectId(id)), Updates.set("is_active", false));
                showSuccess("Book deleted successfully");
                clearFields(); loadBooks();
            } catch (Exception ex) { showError("Error deleting book: " + ex.getMessage()); }
        }
    }

    private boolean validateInputs() {
        if (titleField.getText().trim().isEmpty())    { showError("Title is required");    return false; }
        if (authorField.getText().trim().isEmpty())   { showError("Author is required");   return false; }
        if (isbnField.getText().trim().isEmpty())     { showError("ISBN is required");     return false; }
        if (quantityField.getText().trim().isEmpty()) { showError("Quantity is required"); return false; }
        try {
            int q = Integer.parseInt(quantityField.getText().trim());
            if (q <= 0) { showError("Quantity must be > 0"); return false; }
        } catch (NumberFormatException ex) { showError("Quantity must be a number"); return false; }
        return true;
    }

    private void clearFields() {
        titleField.setText(""); authorField.setText(""); categoryField.setText("");
        quantityField.setText(""); isbnField.setText(""); booksTable.clearSelection();
    }

    private void showError(String msg)   { JOptionPane.showMessageDialog(this, msg, "Error",   JOptionPane.ERROR_MESSAGE); }
    private void showSuccess(String msg) { JOptionPane.showMessageDialog(this, msg, "Success", JOptionPane.INFORMATION_MESSAGE); }
}