import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Date;
import com.mongodb.client.*;
import com.mongodb.client.model.*;
import org.bson.Document;
import org.bson.types.ObjectId;

public class LibrarianManagementPanel extends JPanel {
    private JTable librarianTable;
    private DefaultTableModel tableModel;

    public LibrarianManagementPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        initializeComponents();
        loadLibrarians();
    }

    private void initializeComponents() {
        JLabel titleLabel = new JLabel("Manage Librarians");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));

        String[] columns = {"ID", "Username", "Full Name", "Email", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        librarianTable = new JTable(tableModel);
        librarianTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        librarianTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        librarianTable.setRowHeight(25);
        librarianTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addBtn        = createBtn("Add Librarian");
        JButton updateBtn     = createBtn("Update Librarian");
        JButton deactivateBtn = createBtn("Deactivate");
        JButton reactivateBtn = createBtn("Reactivate");
        JButton refreshBtn    = createBtn("Refresh");
        btns.add(addBtn); btns.add(updateBtn); btns.add(deactivateBtn);
        btns.add(reactivateBtn); btns.add(refreshBtn);

        add(titleLabel,                  BorderLayout.NORTH);
        add(new JScrollPane(librarianTable), BorderLayout.CENTER);
        add(btns,                        BorderLayout.SOUTH);

        addBtn       .addActionListener(e -> addLibrarian());
        updateBtn    .addActionListener(e -> updateLibrarian());
        deactivateBtn.addActionListener(e -> toggleActive(false));
        reactivateBtn.addActionListener(e -> toggleActive(true));
        refreshBtn   .addActionListener(e -> loadLibrarians());
    }

    private JButton createBtn(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        b.setBackground(new Color(70, 130, 180));
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false); b.setBorderPainted(false);
        return b;
    }

    private void loadLibrarians() {
        tableModel.setRowCount(0);
        try {
            for (Document d : DatabaseConnection.getCollection("users")
                    .find(Filters.eq("role", "LIBRARIAN"))) {
                tableModel.addRow(new Object[]{
                    d.getObjectId("_id").toString(),
                    d.getString("username"),
                    d.getString("full_name"),
                    d.getString("email"),
                    d.getBoolean("is_active", false) ? "Active" : "Inactive"
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading librarians: " + ex.getMessage());
        }
    }

    private void toggleActive(boolean active) {
        int row = librarianTable.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Please select a librarian"); return; }
        String id       = (String) tableModel.getValueAt(row, 0);
        String username = (String) tableModel.getValueAt(row, 1);
        String status   = (String) tableModel.getValueAt(row, 4);
        if (active  && "Active".equals(status))   { JOptionPane.showMessageDialog(this, "Already active");   return; }
        if (!active && "Inactive".equals(status)) { JOptionPane.showMessageDialog(this, "Already inactive"); return; }
        int c = JOptionPane.showConfirmDialog(this,
            (active ? "Reactivate" : "Deactivate") + " librarian: " + username + "?",
            "Confirm", JOptionPane.YES_NO_OPTION);
        if (c == JOptionPane.YES_OPTION) {
            try {
                DatabaseConnection.getCollection("users").updateOne(
                    Filters.eq("_id", new ObjectId(id)), Updates.set("is_active", active));
                JOptionPane.showMessageDialog(this, "Librarian " + (active ? "reactivated" : "deactivated") + " successfully!");
                loadLibrarians();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }
    }

    private void addLibrarian() {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add Librarian", true);
        dlg.setLayout(new BorderLayout(10, 10));
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5); gbc.anchor = GridBagConstraints.WEST;

        JTextField usernameField = new JTextField(20);
        JPasswordField passwordField = new JPasswordField(20);
        JTextField fullNameField = new JTextField(20);
        JTextField emailField    = new JTextField(20);

        addFormRow(form, gbc, 0, "Username:", usernameField);
        addFormRow(form, gbc, 1, "Password:", passwordField);
        addFormRow(form, gbc, 2, "Full Name:", fullNameField);
        addFormRow(form, gbc, 3, "Email:", emailField);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addBtn = createBtn("Add"); JButton cancelBtn = createBtn("Cancel");
        btns.add(addBtn); btns.add(cancelBtn);

        addBtn.addActionListener(e -> {
            try {
                MongoCollection<Document> users = DatabaseConnection.getCollection("users");
                if (users.find(Filters.eq("username", usernameField.getText().trim())).first() != null) {
                    JOptionPane.showMessageDialog(dlg, "Username already exists!"); return;
                }
                users.insertOne(new Document()
                    .append("username",    usernameField.getText().trim())
                    .append("password",    new String(passwordField.getPassword()))
                    .append("full_name",   fullNameField.getText().trim())
                    .append("email",       emailField.getText().trim())
                    .append("role",        "LIBRARIAN")
                    .append("type",        "LIBRARIAN")
                    .append("is_active",   true)
                    .append("is_approved", true)
                    .append("created_at",  new Date()));
                JOptionPane.showMessageDialog(dlg, "Librarian added successfully!");
                loadLibrarians(); dlg.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, "Error: " + ex.getMessage());
            }
        });
        cancelBtn.addActionListener(e -> dlg.dispose());

        dlg.add(form, BorderLayout.CENTER);
        dlg.add(btns, BorderLayout.SOUTH);
        dlg.pack(); dlg.setLocationRelativeTo(this); dlg.setVisible(true);
    }

    private void updateLibrarian() {
        int row = librarianTable.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Please select a librarian"); return; }
        String id = (String) tableModel.getValueAt(row, 0);

        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Update Librarian", true);
        dlg.setLayout(new BorderLayout(10, 10));
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5); gbc.anchor = GridBagConstraints.WEST;

        JTextField fullNameField = new JTextField(20);
        JTextField emailField    = new JTextField(20);

        try {
            Document d = DatabaseConnection.getCollection("users")
                .find(Filters.eq("_id", new ObjectId(id))).first();
            if (d != null) { fullNameField.setText(d.getString("full_name")); emailField.setText(d.getString("email")); }
        } catch (Exception ex) { JOptionPane.showMessageDialog(dlg, "Error loading data"); dlg.dispose(); return; }

        addFormRow(form, gbc, 0, "Full Name:", fullNameField);
        addFormRow(form, gbc, 1, "Email:", emailField);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton updateBtn = createBtn("Update"); JButton cancelBtn = createBtn("Cancel");
        btns.add(updateBtn); btns.add(cancelBtn);

        updateBtn.addActionListener(e -> {
            try {
                DatabaseConnection.getCollection("users").updateOne(
                    Filters.eq("_id", new ObjectId(id)),
                    Updates.combine(
                        Updates.set("full_name", fullNameField.getText().trim()),
                        Updates.set("email",     emailField.getText().trim())));
                JOptionPane.showMessageDialog(dlg, "Librarian updated successfully!");
                loadLibrarians(); dlg.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, "Error: " + ex.getMessage());
            }
        });
        cancelBtn.addActionListener(e -> dlg.dispose());

        dlg.add(form, BorderLayout.CENTER);
        dlg.add(btns, BorderLayout.SOUTH);
        dlg.pack(); dlg.setLocationRelativeTo(this); dlg.setVisible(true);
    }

    private void addFormRow(JPanel p, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridx = 0; gbc.gridy = row; p.add(new JLabel(label), gbc);
        gbc.gridx = 1; p.add(field, gbc);
    }
}