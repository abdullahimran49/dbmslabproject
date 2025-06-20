import javax.swing.*;

public class SuperAdminDashboard extends AdminDashboard {

    public SuperAdminDashboard() {
        setTitle("Super Admin Dashboard - Manage Admins, Users, Flights & Bookings");

        JTabbedPane tabbedPane = new JTabbedPane();


        tabbedPane.addTab("Airlines", new AirlineManagement());
        tabbedPane.addTab("Flights", new FlightManagement());
        tabbedPane.addTab("Bookings", new BookingManagement());


        tabbedPane.addTab("Users & Admins", new SuperUserManagement());

        add(tabbedPane);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SuperAdminDashboard dashboard = new SuperAdminDashboard();
            dashboard.setVisible(true);
        });
    }
}