import java.sql.*;

public class UserDAO {

    // Register User with CNIC Validation
    public static String registerUser(String fullName, String cnic, String email, String password, String phone, String role) {
        if (fullName.isEmpty() || cnic.isEmpty() || email.isEmpty() || password.isEmpty() || phone.isEmpty()) {
            return "❌ Please fill in all fields.";
        }

        // Validate CNIC length (must be 13 characters)
        if (cnic.length() != 13) {
            return "❌ CNIC must be exactly 13 characters.";
        }

        try (Connection conn = DBConnection.getConnection()) {

            // Check if CNIC or Email already exists
            String checkQuery = "SELECT * FROM Users WHERE CNIC = ? OR Email = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setString(1, cnic);
            checkStmt.setString(2, email);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                return "❌ CNIC or Email already exists.";
            }

            String insert = "INSERT INTO Users (FullName, CNIC, Email, Password, Phone, Role) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(insert);
            stmt.setString(1, fullName);
            stmt.setString(2, cnic);
            stmt.setString(3, email);
            stmt.setString(4, password); // NOTE: Consider hashing in real apps
            stmt.setString(5, phone);
            stmt.setString(6, role);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                return "✅ Signup successful. Please login.";
            } else {
                return "❌ Failed to register user.";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "❌ Error: " + e.getMessage();
        }
    }

    // Validate User Login
    public static String validateLogin(String email, String password) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT Role FROM Users WHERE Email = ? AND Password = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, email);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("Role"); // "user" or "admin"
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
