import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class BookingConfirmationFrame extends JFrame {
    private String seatNumber;
    private int flightID;

    public BookingConfirmationFrame(String seatNumber, int flightID) {
        this.seatNumber = seatNumber;
        this.flightID = flightID;
        setTitle("Booking Confirmation");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Show user details and selected seat
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new GridLayout(5, 1));

        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT FullName, FlightNumber FROM Users WHERE UserID = ?";  // Assuming user details are in Users table
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, 1);  // Get user ID (this should come from the logged-in user session)
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                JLabel nameLabel = new JLabel("Name: " + rs.getString("FullName"));
                JLabel flightLabel = new JLabel("Flight Number: " + flightID); // Assume Flight Number is passed
                JLabel seatLabel = new JLabel("Seat Number: " + seatNumber);

                detailsPanel.add(nameLabel);
                detailsPanel.add(flightLabel);
                detailsPanel.add(seatLabel);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        add(detailsPanel, BorderLayout.CENTER);

        // Payment Method
        JPanel paymentPanel = new JPanel();
        paymentPanel.setLayout(new FlowLayout());

        JLabel cardLabel = new JLabel("Enter Card Number:");
        JTextField cardField = new JTextField(16);  // For card number
        JButton payButton = new JButton("Pay");

        payButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Process payment (always successful)
                JOptionPane.showMessageDialog(BookingConfirmationFrame.this,
                        "Payment successful! Seat " + seatNumber + " is now reserved.");
                reserveSeat();
                dispose();  // Close the booking confirmation window
            }
        });

        paymentPanel.add(cardLabel);
        paymentPanel.add(cardField);
        paymentPanel.add(payButton);

        add(paymentPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void reserveSeat() {
        // Reserve the seat in the database
        try (Connection conn = DBConnection.getConnection()) {
            String updateQuery = "UPDATE Seats SET IsAvailable = 0 WHERE FlightID = ? AND SeatNumber = ?";
            PreparedStatement stmt = conn.prepareStatement(updateQuery);
            stmt.setInt(1, flightID);
            stmt.setString(2, seatNumber);

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Seat reserved successfully.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new BookingConfirmationFrame("A1", 101);  // Sample seat and flight ID
    }
}
