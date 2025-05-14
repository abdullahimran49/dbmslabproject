import javax.swing.*;

public class AdminDashboard extends JFrame {

    public AdminDashboard() {
        setTitle("Admin Dashboard");
        setSize(400, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Add Admin dashboard content here, such as flight management, user management, etc.
        JLabel welcomeLabel = new JLabel("Welcome to the Admin Dashboard");
        welcomeLabel.setBounds(100, 100, 200, 30);
        add(welcomeLabel);

        setLayout(null);
        setVisible(true);
    }

    public static void main(String[] args) {
        new AdminDashboard();
    }
}
