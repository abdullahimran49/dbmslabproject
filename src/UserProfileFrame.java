import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class UserProfileFrame extends JFrame {
    private final User currentUser;
    private JTextField fullNameField, cnicField, emailField, phoneField;
    private JButton updateButton, backButton;

    public UserProfileFrame(User user) {
        this.currentUser = user;

        setTitle("User Profile");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        formPanel.add(new JLabel("Full Name:"));
        fullNameField = new JTextField(user.getFullName());
        fullNameField.setEditable(false);
        fullNameField.setBackground(Color.LIGHT_GRAY);
        formPanel.add(fullNameField);

        formPanel.add(new JLabel("CNIC:"));
        cnicField = new JTextField(user.getCnic());
        cnicField.setEditable(false);
        cnicField.setBackground(Color.LIGHT_GRAY);
        formPanel.add(cnicField);

        formPanel.add(new JLabel("Email:"));
        emailField = new JTextField(user.getEmail());
        formPanel.add(emailField);

        formPanel.add(new JLabel("Phone (11 digits):"));
        phoneField = new JTextField(user.getPhone());
        formPanel.add(phoneField);

        updateButton = new JButton("Update Profile");
        updateButton.addActionListener(e -> updateProfile());

        backButton = new JButton("Back");
        backButton.addActionListener(e -> dispose()); // Close frame
        JButton resetPasswordButton = new JButton("Reset Password");
        resetPasswordButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        resetPasswordButton.addActionListener(e -> new ResetPasswordFrame(currentUser.getUserID()));


        JPanel btnPanel = new JPanel();
        btnPanel.add(updateButton);
        btnPanel.add(backButton);
        btnPanel.add(resetPasswordButton);

        add(formPanel, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void updateProfile() {
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();

        if (email.isEmpty() || phone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields.");
            return;
        }

        if (!email.contains("@")) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address.");
            return;
        }

        if (!phone.matches("\\d{11}")) {
            JOptionPane.showMessageDialog(this, "Phone number must be exactly 11 digits.");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            // Check for duplicate email or phone (excluding current user's CNIC)
            CallableStatement checkStmt = conn.prepareCall("{CALL CheckUserExists(?, ?, ?)}");
            checkStmt.setInt(1, currentUser.getUserID()); // Use user ID instead of CNIC
            checkStmt.setString(2, email);
            checkStmt.setString(3, phone);


            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                boolean emailExists = rs.getBoolean("emailExists");
                boolean phoneExists = rs.getBoolean("phoneExists");

                if (emailExists && !email.equals(currentUser.getEmail())) {
                    JOptionPane.showMessageDialog(this, "❌ Email address is already in use by another account.");
                    return;
                }

                if (phoneExists && !phone.equals(currentUser.getPhone())) {
                    JOptionPane.showMessageDialog(this, "❌ Phone number is already in use by another account.");
                    return;
                }
            }

            // Update user profile
            CallableStatement stmt = conn.prepareCall("{CALL UpdateUserProfile(?,?,?,?,?)}");
            stmt.setInt(1, currentUser.getUserID());
            stmt.setString(2, currentUser.getFullName());
            stmt.setString(3, email);
            stmt.setString(4, phone);
            stmt.registerOutParameter(5, Types.INTEGER);

            stmt.execute();

            int status = stmt.getInt(5);
            if (status == 1) {
                JOptionPane.showMessageDialog(this, "✅ Profile updated successfully!");
                currentUser.setEmail(email);
                currentUser.setPhone(phone);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "⚠️ No changes were made.");
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "❌ Error updating profile: " + ex.getMessage());
        }
    }
}
