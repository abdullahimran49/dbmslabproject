import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class PaymentDialogue extends JDialog {
    private JTextField cardNumberField, expiryDateField, cardTypeField;
    private final int userId, seatId, flightId;
    private final double amount;

    public PaymentDialogue(JFrame parent, int userId, int seatId, int flightId, double amount) {
        super(parent, "Enter Card Details", true);
        this.userId = userId;
        this.seatId = seatId;
        this.flightId = flightId;
        this.amount = amount;

        setLayout(new GridLayout(5, 2, 10, 10));
        setSize(400, 250);
        setLocationRelativeTo(parent);

        add(new JLabel("Card Number:"));
        cardNumberField = new JTextField();
        add(cardNumberField);

        add(new JLabel("Card Type (Visa/Master):"));
        cardTypeField = new JTextField();
        add(cardTypeField);

        add(new JLabel("Expiry Date (YYYY-MM-DD):"));
        expiryDateField = new JTextField();
        add(expiryDateField);

        JButton payButton = new JButton("Pay & Book");
        payButton.addActionListener(e -> processPayment());
        add(payButton);

        setVisible(true);
    }

    private void processPayment() {
        String cardNum = cardNumberField.getText().trim();
        String cardType = cardTypeField.getText().trim();
        String expiry = expiryDateField.getText().trim();

        if (!cardNum.matches("\\d{16}") || !expiry.matches("\\d{4}-\\d{2}-\\d{2}")) {
            JOptionPane.showMessageDialog(this, "Invalid card details");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false); // Start transaction

            // 1. Insert/Check PaymentMethod
            PreparedStatement checkCard = conn.prepareStatement(
                    "SELECT PaymentMethodID FROM PaymentMethods WHERE CardNumber = ? AND UserID = ?");
            checkCard.setString(1, cardNum);
            checkCard.setInt(2, userId);
            ResultSet rs = checkCard.executeQuery();

            int paymentMethodId;
            if (rs.next()) {
                paymentMethodId = rs.getInt("PaymentMethodID");
            } else {
                PreparedStatement insertCard = conn.prepareStatement(
                        "INSERT INTO PaymentMethods (UserID, CardNumber, CardType, ExpiryDate) OUTPUT INSERTED.PaymentMethodID VALUES (?, ?, ?, ?)");
                insertCard.setInt(1, userId);
                insertCard.setString(2, cardNum);
                insertCard.setString(3, cardType);
                insertCard.setDate(4, Date.valueOf(expiry));
                ResultSet cardRs = insertCard.executeQuery();
                cardRs.next();
                paymentMethodId = cardRs.getInt(1);
            }

            // 2. Create Booking
            PreparedStatement insertBooking = conn.prepareStatement(
                    "INSERT INTO Bookings (UserID, FlightID, TotalAmount, Status) OUTPUT INSERTED.BookingID VALUES (?, ?, ?, ?)");
            insertBooking.setInt(1, userId);
            insertBooking.setInt(2, flightId);
            insertBooking.setDouble(3, amount);
            insertBooking.setString(4, "Confirmed");
            ResultSet bookingRs = insertBooking.executeQuery();
            bookingRs.next();
            int bookingId = bookingRs.getInt(1);

            // 3. Book Seat
            PreparedStatement bookSeat = conn.prepareStatement(
                    "INSERT INTO BookedSeats (BookingID, SeatID) VALUES (?, ?)");
            bookSeat.setInt(1, bookingId);
            bookSeat.setInt(2, seatId);
            bookSeat.executeUpdate();

            // 4. Mark Seat Unavailable
            PreparedStatement updateSeat = conn.prepareStatement(
                    "UPDATE Seats SET IsAvailable = 0 WHERE SeatID = ?");
            updateSeat.setInt(1, seatId);
            updateSeat.executeUpdate();

            // 5. Insert Payment
            PreparedStatement insertPayment = conn.prepareStatement(
                    "INSERT INTO Payments (BookingID, PaymentMethodID, Amount, Status) VALUES (?, ?, ?, 'Paid')");
            insertPayment.setInt(1, bookingId);
            insertPayment.setInt(2, paymentMethodId);
            insertPayment.setDouble(3, amount);
            insertPayment.executeUpdate();

            conn.commit();
            JOptionPane.showMessageDialog(this, "Booking Confirmed! Thank you for your payment.");
            dispose();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Transaction failed. Try again.");
        }
    }
}
