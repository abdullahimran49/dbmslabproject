import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class UserDashboard extends JFrame {

    private JPanel flightListPanel;
    private JScrollPane scrollPane;
    private JButton viewAllFlightsButton;

    public UserDashboard() {
        setTitle("User Dashboard");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Header Panel
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel headerLabel = new JLabel("Welcome to the User Dashboard");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        headerPanel.add(headerLabel);
        add(headerPanel, BorderLayout.NORTH);

        // Button to View All Flights
        viewAllFlightsButton = new JButton("View All Flights");
        viewAllFlightsButton.addActionListener(e -> loadFlights());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(viewAllFlightsButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Flight List Panel inside a ScrollPane
        flightListPanel = new JPanel();
        flightListPanel.setLayout(new BoxLayout(flightListPanel, BoxLayout.Y_AXIS));
        scrollPane = new JScrollPane(flightListPanel);
        add(scrollPane, BorderLayout.CENTER);

        setVisible(true);
    }

    // Load available flights using stored procedure
    private void loadFlights() {
        flightListPanel.removeAll(); // Clear previous entries

        try (Connection conn = DBConnection.getConnection()) {
            CallableStatement stmt = conn.prepareCall("{CALL ShowAllFlights}");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int flightID = rs.getInt("FlightID");
                String airline = rs.getString("AirlineName");
                String flightNumber = rs.getString("FlightNumber");
                String origin = rs.getString("Origin");
                String destination = rs.getString("Destination");
                Timestamp depTime = rs.getTimestamp("DepartureTime");
                Timestamp arrTime = rs.getTimestamp("ArrivalTime");
                double price = rs.getDouble("Price");

                // Row panel
                JPanel rowPanel = new JPanel(new GridLayout(2, 1));
                rowPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
                rowPanel.setBackground(Color.WHITE);
                rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

                // Info Panel
                JPanel infoPanel = new JPanel(new GridLayout(1, 6));
                infoPanel.add(new JLabel("Airline: " + airline));
                infoPanel.add(new JLabel("Flight: " + flightNumber));
                infoPanel.add(new JLabel("From: " + origin));
                infoPanel.add(new JLabel("To: " + destination));
                infoPanel.add(new JLabel("Departure: " + depTime.toString()));
                infoPanel.add(new JLabel("Arrival: " + arrTime.toString()));

                // Action Panel
                JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                actionPanel.add(new JLabel("Price: $" + price));
                JButton selectSeatsButton = new JButton("Select Seats");
                selectSeatsButton.addActionListener(ev -> new SeatSelectionFrame(flightID));
                actionPanel.add(selectSeatsButton);

                rowPanel.add(infoPanel);
                rowPanel.add(actionPanel);

                flightListPanel.add(rowPanel);
            }

            flightListPanel.revalidate();
            flightListPanel.repaint();

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading flights: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new UserDashboard();
    }
}
