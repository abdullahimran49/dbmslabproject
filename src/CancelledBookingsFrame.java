import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CancelledBookingsFrame extends JFrame {

    private int userID;
    private JPanel bookingsPanel;
    private JScrollPane scrollPane;
    private JButton backButton;

    public CancelledBookingsFrame(int userID) {
        this.userID = userID;

        setTitle("Cancelled Bookings");
        setSize(800, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        bookingsPanel = new JPanel();
        bookingsPanel.setLayout(new BoxLayout(bookingsPanel, BoxLayout.Y_AXIS));
        scrollPane = new JScrollPane(bookingsPanel);

        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        backButton = new JButton("Back");
        backButton.addActionListener(e -> dispose());
        bottomPanel.add(backButton);
        add(bottomPanel, BorderLayout.SOUTH);

        loadCancelledBookings();

        setVisible(true);
    }

    private void loadCancelledBookings() {
        bookingsPanel.removeAll();

        List<Booking> bookingList = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "{call GetUserBookings(?)}";
            CallableStatement stmt = conn.prepareCall(sql);
            stmt.setInt(1, userID);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String status = rs.getString("Status");
                // Only include cancelled bookings
                if (!"Cancelled".equalsIgnoreCase(status)) continue;

                Booking booking = new Booking(
                        rs.getInt("BookingID"),
                        rs.getInt("FlightID"),
                        rs.getString("AirlineName"),
                        rs.getString("FlightNumber"),
                        rs.getString("Origin"),
                        rs.getString("Destination"),
                        rs.getTimestamp("DepartureTime"),
                        rs.getTimestamp("ArrivalTime"),
                        rs.getDouble("Price"),
                        rs.getString("SeatNumber"),
                        rs.getString("SeatClass"),
                        rs.getTimestamp("BookingDate"),
                        rs.getDouble("TotalAmount"),
                        rs.getString("Status")
                );
                bookingList.add(booking);
            }

            // Sort bookings by booking date (latest first)
            bookingList.sort((b1, b2) -> b2.bookingDate.compareTo(b1.bookingDate));

            if (bookingList.isEmpty()) {
                JLabel noBookingsLabel = new JLabel("You have no cancelled bookings.");
                noBookingsLabel.setFont(new Font("Arial", Font.PLAIN, 16));
                noBookingsLabel.setHorizontalAlignment(SwingConstants.CENTER);
                bookingsPanel.add(Box.createVerticalGlue());
                bookingsPanel.add(noBookingsLabel);
                bookingsPanel.add(Box.createVerticalGlue());
            } else {
                for (Booking b : bookingList) {
                    JPanel bookingPanel = new JPanel(new BorderLayout());
                    bookingPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
                    bookingPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

                    JPanel infoPanel = new JPanel(new GridLayout(5, 1));
                    infoPanel.add(new JLabel("Flight: " + b.airline + " - " + b.flightNumber));
                    infoPanel.add(new JLabel("From: " + b.origin + " To: " + b.destination));
                    infoPanel.add(new JLabel(
                            "Departure: " + b.departureTime +
                                    " | Arrival: " + b.arrivalTime +
                                    " | Seat: " + (b.seatClass != null ? b.seatClass : "N/A") +
                                    " (" + (b.seatNumber != null ? b.seatNumber : "N/A") + ")"
                    ));
                    infoPanel.add(new JLabel(
                            "Booking Date: " + b.bookingDate +
                                    " | Total Amount: PKR " + String.format("%,.2f", b.totalAmount) +
                                    " | Status: " + b.status
                    ));

                    bookingPanel.add(infoPanel, BorderLayout.CENTER);

                    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

                    // For cancelled bookings, show refunded amount
                    JLabel refundedLabel = new JLabel("Refunded: PKR " + String.format("%,.2f", b.totalAmount));
                    refundedLabel.setForeground(new Color(0, 128, 0));
                    refundedLabel.setFont(new Font("Arial", Font.BOLD, 12));
                    buttonPanel.add(refundedLabel);

                    bookingPanel.add(buttonPanel, BorderLayout.EAST);
                    bookingsPanel.add(bookingPanel);
                    bookingsPanel.add(Box.createRigidArea(new Dimension(0, 10))); // space between bookings
                }
            }

            bookingsPanel.revalidate();
            bookingsPanel.repaint();

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading cancelled bookings: " + e.getMessage());
        }
    }

    private static class Booking {
        int bookingID;
        int flightID;
        String airline;
        String flightNumber;
        String origin;
        String destination;
        Timestamp departureTime;
        Timestamp arrivalTime;
        double price;
        String seatNumber;
        String seatClass;
        Timestamp bookingDate;
        double totalAmount;
        String status;

        public Booking(int bookingID, int flightID, String airline, String flightNumber, String origin,
                       String destination, Timestamp departureTime, Timestamp arrivalTime, double price,
                       String seatNumber, String seatClass, Timestamp bookingDate, double totalAmount, String status) {
            this.bookingID = bookingID;
            this.flightID = flightID;
            this.airline = airline;
            this.flightNumber = flightNumber;
            this.origin = origin;
            this.destination = destination;
            this.departureTime = departureTime;
            this.arrivalTime = arrivalTime;
            this.price = price;
            this.seatNumber = seatNumber;
            this.seatClass = seatClass;
            this.bookingDate = bookingDate;
            this.totalAmount = totalAmount;
            this.status = status;
        }
    }
}