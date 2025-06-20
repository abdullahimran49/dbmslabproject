import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class BookingConfirmationFrame extends JFrame {
    private final String seatNumber;
    private final String seatClass;
    private final int flightID;
    private final int userID;
    private int bookingId = -1;
    private double amount = 0.0;

    public BookingConfirmationFrame(String seatNumber, String seatClass, int flightID, int userID) {
        this.seatNumber = seatNumber;
        this.seatClass = seatClass;
        this.flightID = flightID;
        this.userID = userID;

        setTitle("Booking Confirmation");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JLabel titleLabel = new JLabel("Booking Confirmation", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        add(titleLabel, BorderLayout.NORTH);

        JPanel detailsPanel = new JPanel(new GridLayout(6, 1, 5, 5));
        detailsPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        detailsPanel.add(new JLabel("Booking Details:", SwingConstants.CENTER));
        detailsPanel.add(new JSeparator());

        String fullName = getPassengerName(userID);
        detailsPanel.add(new JLabel("Passenger Name: " + (fullName != null ? fullName : "Not Found")));
        detailsPanel.add(new JLabel("Flight Number: " + flightID));
        detailsPanel.add(new JLabel("Seat Number: " + seatNumber));
        detailsPanel.add(new JLabel("Seat Class: " + seatClass));

        add(detailsPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton managePaymentButton = new JButton("Proceed to Payment");
        buttonPanel.add(managePaymentButton);
        add(buttonPanel, BorderLayout.SOUTH);

        SwingUtilities.invokeLater(() -> {
            if (isSeatAvailable()) {
                bookingId = insertPendingBooking();
                if (bookingId != -1) {
                    amount = getPriceForFlight(flightID);
                    if ("Business".equalsIgnoreCase(seatClass)) {
                        amount += 5000;
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Error creating booking record.", "Error", JOptionPane.ERROR_MESSAGE);
                    dispose();
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        "Sorry, this seat has been booked by another user. Please select a different seat.",
                        "Seat No Longer Available",
                        JOptionPane.WARNING_MESSAGE);
                new SeatSelectionFrame(flightID, userID);
                dispose();
            }
        });

        managePaymentButton.addActionListener(e -> {
            if (bookingId != -1) {
                PaymentDialogue paymentDialogue = new PaymentDialogue(this, bookingId, amount, userID, seatNumber);
                paymentDialogue.setModal(true);
                paymentDialogue.setVisible(true);
                checkAndFinalizeBooking();
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Cannot proceed to payment. Please try again.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        setVisible(true);
    }

    private boolean isSeatAvailable() {
        try (Connection conn = DBConnection.getConnection()) {
            String query = "EXEC CheckSeat ?, ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, flightID);
            stmt.setString(2, seatNumber);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getBoolean("IsAvailable");
        } catch (SQLException e) {
            showError("Error checking seat availability.", e);
        }
        return false;
    }

    private String getPassengerName(int userID) {
        try (Connection conn = DBConnection.getConnection()) {
            String query = "EXEC GetPassengerName ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, userID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("FullName");
        } catch (SQLException e) {
            showError("Error fetching passenger name.", e);
        }
        return null;
    }

    private int insertPendingBooking() {
        try (Connection conn = DBConnection.getConnection()) {
            String query = "EXEC InsertPendingBooking ?, ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, userID);
            stmt.setInt(2, flightID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("BookingID");
        } catch (SQLException e) {
            showError("Failed to insert booking.", e);
        }
        return -1;
    }

    private double getPriceForFlight(int flightID) {
        try (Connection conn = DBConnection.getConnection()) {
            String query = "EXEC GetFlightPrice ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, flightID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getDouble("Price");
        } catch (SQLException e) {
            showError("Error fetching flight price.", e);
        }
        return 0.0;
    }

    private void checkAndFinalizeBooking() {
        try (Connection conn = DBConnection.getConnection()) {
            String query = "EXEC CheckBookingStatus ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, bookingId);
        } catch (SQLException e) {
            showError("Error checking booking status.", e);
        }
    }

    private void showError(String message, Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, message, "Database Error", JOptionPane.ERROR_MESSAGE);
    }
}