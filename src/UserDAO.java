import java.sql.*;

public class UserDAO {


    public static String registerUser(String fullName, String cnic, String email, String password, String phone, String role) {
        if (fullName.isEmpty() || cnic.isEmpty() || email.isEmpty() || password.isEmpty() || phone.isEmpty()) {
            return "❌ Please fill in all fields.";
        }

        // Validate CNIC length (must be 13 characters)
        if (cnic.length() != 13) {
            return "❌ CNIC must be exactly 13 characters.";
        }

        try (Connection conn = DBConnection.getConnection()) {

            String callProcedure = "{CALL sp_RegisterUser(?, ?, ?, ?, ?, ?)}";
            CallableStatement stmt = conn.prepareCall(callProcedure);

            stmt.setString(1, fullName);
            stmt.setString(2, cnic);
            stmt.setString(3, email);
            stmt.setString(4, password);
            stmt.setString(5, phone);
            stmt.setString(6, role);

            stmt.execute();
            return "✅ Signup successful. Please login.";

        } catch (SQLException e) {
            if (e.getMessage().contains("CNIC or Email already exists")) {
                return "❌ CNIC or Email already exists.";
            }
            return "❌ Failed to register user.";
        }
    }


    // Login Validator (for reference)
    public static User validateLogin(String email, String password) {
        try (Connection conn = DBConnection.getConnection()) {
            CallableStatement stmt = conn.prepareCall("{CALL ValidateUser(?, ?)}");
            stmt.setString(1, email);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setUserID(rs.getInt("UserID"));
                user.setFullName(rs.getString("FullName"));
                user.setEmail(rs.getString("Email"));
                user.setRole(rs.getString("Role"));
                return user;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
