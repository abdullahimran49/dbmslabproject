import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;
import java.util.List;

public class UserDashboard extends JFrame {

    private JPanel flightListPanel;
    private JScrollPane scrollPane;
    private JButton viewAllFlightsButton, viewPaymentsButton, logoutButton, viewBookingsButton, viewCancelledBookingsButton;
    private final User currentUser;

    public UserDashboard(User user) {
        this.currentUser = user;

        setTitle("User Dashboard");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10)); // spacing between regions

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel headerLabel = new JLabel("Welcome, " + currentUser.getFullName());
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        headerPanel.add(headerLabel, BorderLayout.WEST);

        logoutButton = new JButton("Logout");
        logoutButton.setFocusable(false);
        logoutButton.setMargin(new Insets(5, 15, 5, 15));
        logoutButton.addActionListener(e -> {
            dispose();
            new LoginFrame();
        });

        JPanel logoutPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        logoutPanel.add(logoutButton);
        headerPanel.add(logoutPanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Left Button Panel (vertical)
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create buttons
        JButton viewProfileButton = new JButton("View Profile");
        viewAllFlightsButton = new JButton("View All Flights");
        viewPaymentsButton = new JButton("View Payments");
        viewBookingsButton = new JButton("View Bookings");
        viewCancelledBookingsButton = new JButton("Cancelled");


        // Set max size for uniform button width and align center
        Dimension btnSize = new Dimension(140, 35);
        viewProfileButton.setMaximumSize(btnSize);
        viewAllFlightsButton.setMaximumSize(btnSize);
        viewPaymentsButton.setMaximumSize(btnSize);
        viewBookingsButton.setMaximumSize(btnSize);
        viewCancelledBookingsButton.setMaximumSize(btnSize);


        viewProfileButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        viewAllFlightsButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        viewPaymentsButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        viewBookingsButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        viewCancelledBookingsButton.setAlignmentX(Component.CENTER_ALIGNMENT);


        // Add action listeners
        viewProfileButton.addActionListener(e -> new UserProfileFrame(currentUser));
        viewAllFlightsButton.addActionListener(e -> loadFlights());
        viewPaymentsButton.addActionListener(e -> new PaymentsFrame(currentUser.getUserID()));
        viewBookingsButton.addActionListener(e -> new UserBookingsFrame(currentUser.getUserID()));
        viewCancelledBookingsButton.addActionListener(e -> new CancelledBookingsFrame(currentUser.getUserID()));


        // Add buttons with spacing
        buttonPanel.add(viewProfileButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        buttonPanel.add(viewAllFlightsButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        buttonPanel.add(viewPaymentsButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        buttonPanel.add(viewBookingsButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        buttonPanel.add(viewCancelledBookingsButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 15)));


        add(buttonPanel, BorderLayout.WEST);

        // Flight List Panel
        flightListPanel = new JPanel();
        flightListPanel.setLayout(new BoxLayout(flightListPanel, BoxLayout.Y_AXIS));
        flightListPanel.setBackground(new Color(245, 245, 245)); // light gray background

        scrollPane = new JScrollPane(flightListPanel);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Available Flights"));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);

        setVisible(true);
    }


    private void loadFlights() {
        flightListPanel.removeAll();
        List<Flight> flightList = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection()) {
            CallableStatement stmt = conn.prepareCall("{CALL ShowAllFlights}");
            ResultSet rs = stmt.executeQuery();

            Timestamp now = new Timestamp(System.currentTimeMillis());

            while (rs.next()) {
                Timestamp depTime = rs.getTimestamp("DepartureTime");
                if (depTime.before(now)) continue;

                Flight flight = new Flight(
                        rs.getInt("FlightID"),
                        rs.getString("AirlineName"),
                        rs.getString("FlightNumber"),
                        rs.getString("Origin"),
                        rs.getString("Destination"),
                        depTime,
                        rs.getTimestamp("ArrivalTime"),
                        rs.getDouble("Price")
                );
                flightList.add(flight);
            }

            flightList.sort(Comparator.comparing(f -> f.departureTime));

            for (Flight f : flightList) {
                JPanel rowPanel = new JPanel(new BorderLayout(10, 5));
                rowPanel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.GRAY),
                        BorderFactory.createEmptyBorder(8, 8, 8, 8)));
                rowPanel.setBackground(Color.WHITE);
                rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

                JPanel infoPanel = new JPanel(new GridLayout(1, 6, 10, 0));
                infoPanel.setOpaque(false);
                infoPanel.add(new JLabel("Airline: " + f.airline));
                infoPanel.add(new JLabel("Flight: " + f.flightNumber));
                infoPanel.add(new JLabel("From: " + f.origin));
                infoPanel.add(new JLabel("To: " + f.destination));
                infoPanel.add(new JLabel("Departure: " + f.departureTime));
                infoPanel.add(new JLabel("Arrival: " + f.arrivalTime));

                JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                actionPanel.setOpaque(false);
                String priceInfo = String.format("Economy: PKR %,.0f | Business: PKR %,.0f", f.price, f.price + 5000);
                actionPanel.add(new JLabel(priceInfo));

                JButton selectSeatsButton = new JButton("Select Seats");
                selectSeatsButton.addActionListener(ev -> new SeatSelectionFrame(f.flightID, currentUser.getUserID()));
                actionPanel.add(selectSeatsButton);

                rowPanel.add(infoPanel, BorderLayout.CENTER);
                rowPanel.add(actionPanel, BorderLayout.EAST);
                flightListPanel.add(rowPanel);
                flightListPanel.add(Box.createRigidArea(new Dimension(0, 8))); // space between rows
            }

            flightListPanel.revalidate();
            flightListPanel.repaint();

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading flights: " + e.getMessage());
        }
    }

    class Flight {
        int flightID;
        String airline;
        String flightNumber;
        String origin;
        String destination;
        Timestamp departureTime;
        Timestamp arrivalTime;
        double price;

        public Flight(int flightID, String airline, String flightNumber, String origin,
                      String destination, Timestamp departureTime, Timestamp arrivalTime, double price) {
            this.flightID = flightID;
            this.airline = airline;
            this.flightNumber = flightNumber;
            this.origin = origin;
            this.destination = destination;
            this.departureTime = departureTime;
            this.arrivalTime = arrivalTime;
            this.price = price;
        }
    }
}