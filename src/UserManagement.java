import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.regex.Pattern;

public class UserManagement extends JPanel {
    private JTable userTable;
    private DefaultTableModel userTableModel;

    // Validation patterns
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{11}$");
    private static final Pattern CNIC_PATTERN = Pattern.compile("^\\d{13}$");

    public UserManagement() {
        setLayout(new BorderLayout());

        // Define columns
        String[] columns = {"UserID", "FullName", "CNIC", "Email", "Phone", "Role"};
        userTableModel = new DefaultTableModel(columns, 0);
        userTable = new JTable(userTableModel);
        JScrollPane scrollPane = new JScrollPane(userTable);

        loadUsers();

        // Buttons
        JButton addUserBtn = new JButton("Add User");
        JButton deleteUserBtn = new JButton("Delete Selected User");

        addUserBtn.addActionListener(e -> addUser());
        deleteUserBtn.addActionListener(e -> deleteUser());

        JPanel btnPanel = new JPanel();
        btnPanel.add(addUserBtn);
        btnPanel.add(deleteUserBtn);

        add(scrollPane, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);
    }

    // Validation methods
    private boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    private boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    private boolean isValidCNIC(String cnic) {
        return cnic != null && CNIC_PATTERN.matcher(cnic.trim()).matches();
    }

    private boolean isValidName(String name) {
        return name != null && name.trim().length() >= 2 &&
                name.trim().matches("^[a-zA-Z\\s]+$");
    }

    private boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    private String validateInput(String fullName, String cnic, String email,
                                 String password, String phone) {
        if (!isValidName(fullName)) {
            return "Full Name must be at least 2 characters and contain only letters and spaces.";
        }
        if (!isValidCNIC(cnic)) {
            return "CNIC must be exactly 13 digits.";
        }
        if (!isValidEmail(email)) {
            return "Please enter a valid email address.";
        }
        if (!isValidPassword(password)) {
            return "Password must be at least 6 characters long.";
        }
        if (!isValidPhone(phone)) {
            return "Phone number must be exactly 11 digits.";
        }
        return null; // All validations passed
    }

    // Load all users
    private void loadUsers() {
        userTableModel.setRowCount(0);
        try (Connection conn = DBConnection.getConnection();
             CallableStatement cs = conn.prepareCall("{call sp_ViewAllUsers}");
             ResultSet rs = cs.executeQuery()) {

            while (rs.next()) {
                userTableModel.addRow(new Object[]{
                        rs.getInt("UserID"),
                        rs.getString("FullName"),
                        rs.getString("CNIC"),
                        rs.getString("Email"),
                        rs.getString("Phone"),
                        rs.getString("Role")
                });
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading users: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Add user with validation
    private void addUser() {
        JTextField nameField = new JTextField();
        JTextField cnicField = new JTextField();
        JTextField emailField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JTextField phoneField = new JTextField();

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Full Name:"));
        panel.add(nameField);
        panel.add(new JLabel("CNIC (13 digits):"));
        panel.add(cnicField);
        panel.add(new JLabel("Email:"));
        panel.add(emailField);
        panel.add(new JLabel("Password (min 6 characters):"));
        panel.add(passwordField);
        panel.add(new JLabel("Phone (11 digits):"));
        panel.add(phoneField);

        while (true) {
            int result = JOptionPane.showConfirmDialog(this, panel, "Add New User",
                    JOptionPane.OK_CANCEL_OPTION);

            if (result != JOptionPane.OK_OPTION) return;

            String fullName = nameField.getText().trim();
            String cnic = cnicField.getText().trim();
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword());
            String phone = phoneField.getText().trim();

            // Validate input
            String validationError = validateInput(fullName, cnic, email, password, phone);
            if (validationError != null) {
                JOptionPane.showMessageDialog(this, validationError,
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                continue; // Show dialog again
            }

            // Check for duplicate email/CNIC
            if (isDuplicateUser(email, cnic)) {
                JOptionPane.showMessageDialog(this,
                        "User with this email or CNIC already exists.",
                        "Duplicate User", JOptionPane.WARNING_MESSAGE);
                continue;
            }

            try (Connection conn = DBConnection.getConnection();
                 CallableStatement cs = conn.prepareCall("{CALL AddUsers(?, ?, ?, ?, ?, ?)}")) {
                cs.setString(1, fullName);
                cs.setString(2, cnic);
                cs.setString(3, email);
                cs.setString(4, password);
                cs.setString(5, phone);
                cs.setString(6, "User");

                cs.execute();
                JOptionPane.showMessageDialog(this, "User added successfully.",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                loadUsers();
                break;
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error adding user: " + ex.getMessage(),
                        "Database Error", JOptionPane.ERROR_MESSAGE);
                break;
            }
        }
    }

    // Check for duplicate users
    private boolean isDuplicateUser(String email, String cnic) {
        try (Connection conn = DBConnection.getConnection();
             CallableStatement cs = conn.prepareCall("{call sp_CheckUserDuplicate(?, ?)}")) {

            cs.setString(1, email);
            cs.setString(2, cnic);

            try (ResultSet rs = cs.executeQuery()) {
                return rs.next() && rs.getInt("DuplicateCount") > 0;
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error checking duplicates: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            return true; // Assume duplicate to be safe
        }
    }

    // Delete only if the role is "User"
    private void deleteUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user to delete.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String role = (String) userTableModel.getValueAt(selectedRow, 5);
        if (!role.equalsIgnoreCase("User")) {
            JOptionPane.showMessageDialog(this, "Only Super Admins can delete other admins.",
                    "Access Denied", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int userID = (int) userTableModel.getValueAt(selectedRow, 0);
        String userName = (String) userTableModel.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete user '" + userName + "'?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DBConnection.getConnection();
                 CallableStatement cs = conn.prepareCall("{call sp_DeleteUser(?)}")) {

                cs.setInt(1, userID);
                int deleted = cs.executeUpdate();

                if (deleted > 0) {
                    JOptionPane.showMessageDialog(this, "User deleted successfully.",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadUsers();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete user.",
                            "Delete Failed", JOptionPane.ERROR_MESSAGE);
                }

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error deleting user: " + ex.getMessage(),
                        "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}