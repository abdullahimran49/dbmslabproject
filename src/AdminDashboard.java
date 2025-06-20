import javax.swing.*;
import java.awt.*;

public class AdminDashboard extends JFrame {

    private final JButton logoutButton;

    public AdminDashboard() {
        setTitle("Admin Dashboard - Manage Flights, Bookings, Users, and Airlines");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // --- Top panel with logout button aligned to right ---
        logoutButton = new JButton("Logout");
        logoutButton.setFocusable(false);
        logoutButton.setMargin(new Insets(5, 15, 5, 15));
        logoutButton.addActionListener(e -> {
            dispose();       // Close dashboard
            new LoginFrame(); // Open login window
        });

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topPanel.add(logoutButton);
        add(topPanel, BorderLayout.NORTH);

        // --- Center panel with tabbed pane ---
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Airlines", new AirlineManagement());
        tabbedPane.addTab("Flights", new FlightManagement());
        tabbedPane.addTab("Bookings", new BookingManagement());
        tabbedPane.addTab("Users", new UserManagement());


        add(tabbedPane, BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AdminDashboard adminDashboard = new AdminDashboard();
            adminDashboard.setVisible(true);
        });
    }
}