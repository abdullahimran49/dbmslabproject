import javax.swing.*;
import java.awt.event.*;
import java.sql.*;

public class ResetPasswordFrame extends JFrame {
    private final int userId;
    private JPasswordField oldPasswordField, newPasswordField, confirmNewPasswordField;
    private JButton resetButton, backButton;

    public ResetPasswordFrame(int userId) {
        this.userId = userId;

        setTitle("Reset Password");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(400, 280);
        setLocationRelativeTo(null);
        setLayout(null);

        JLabel oldPasswordLabel = new JLabel("Old Password:");
        oldPasswordLabel.setBounds(30, 30, 120, 25);
        add(oldPasswordLabel);

        oldPasswordField = new JPasswordField();
        oldPasswordField.setBounds(160, 30, 200, 25);
        add(oldPasswordField);

        JLabel newPasswordLabel = new JLabel("New Password:");
        newPasswordLabel.setBounds(30, 70, 120, 25);
        add(newPasswordLabel);

        newPasswordField = new JPasswordField();
        newPasswordField.setBounds(160, 70, 200, 25);
        add(newPasswordField);

        JLabel confirmNewPasswordLabel = new JLabel("Confirm Password:");
        confirmNewPasswordLabel.setBounds(30, 110, 120, 25);
        add(confirmNewPasswordLabel);

        confirmNewPasswordField = new JPasswordField();
        confirmNewPasswordField.setBounds(160, 110, 200, 25);
        add(confirmNewPasswordField);

        resetButton = new JButton("Reset Password");
        resetButton.setBounds(80, 170, 140, 30);
        add(resetButton);

        backButton = new JButton("Back");
        backButton.setBounds(230, 170, 90, 30);
        add(backButton);

        // Action listeners
        resetButton.addActionListener(e -> resetPassword());
        backButton.addActionListener(e -> dispose()); // Close the frame

        setVisible(true);
    }

    private void resetPassword() {
        String oldPass = new String(oldPasswordField.getPassword());
        String newPass = new String(newPasswordField.getPassword());
        String confirmNewPass = new String(confirmNewPasswordField.getPassword());

        if (oldPass.isEmpty() || newPass.isEmpty() || confirmNewPass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields.");
            return;
        }

        if (!newPass.equals(confirmNewPass)) {
            JOptionPane.showMessageDialog(this, "New password and confirmation do not match.");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            CallableStatement stmt = conn.prepareCall("{call ResetUserPassword(?, ?, ?, ?, ?)}");
            stmt.setInt(1, userId);
            stmt.setString(2, oldPass);
            stmt.setString(3, newPass);
            stmt.registerOutParameter(4, Types.INTEGER); // status code
            stmt.registerOutParameter(5, Types.VARCHAR); // message

            stmt.execute();

            int statusCode = stmt.getInt(4);
            String message = stmt.getString(5);

            JOptionPane.showMessageDialog(this, message);

            if (statusCode == 1) {
                dispose(); // Close frame after success
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
        }
    }
}
