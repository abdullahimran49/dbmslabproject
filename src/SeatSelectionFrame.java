import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class SeatSelectionFrame extends JFrame {
    private int flightID;
    private Map<String, JButton> seatButtons = new HashMap<>();
    private String selectedSeatNumber = null;
    private JButton selectedButton = null;

    public SeatSelectionFrame(int flightID) {
        this.flightID = flightID;
        setTitle("Seat Selection");
        setSize(400, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new GridLayout(10, 3));  // Adjust as needed

        loadSeatMap();

        setVisible(true);
    }

    private void loadSeatMap() {
        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT SeatNumber, Class, IsAvailable FROM Seats WHERE FlightID = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, flightID);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String seatNumber = rs.getString("SeatNumber");
                boolean isAvailable = rs.getBoolean("IsAvailable");

                JButton seatButton = new JButton(seatNumber);
                seatButton.setEnabled(isAvailable);
                seatButton.setBackground(isAvailable ? Color.GREEN : Color.RED);

                if (isAvailable) {
                    seatButton.addActionListener(e -> handleSeatSelection(seatNumber, seatButton));
                }

                seatButtons.put(seatNumber, seatButton);
                add(seatButton);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleSeatSelection(String seatNumber, JButton button) {
        // Deselect previously selected
        if (selectedButton != null) {
            selectedButton.setBackground(Color.GREEN);
            selectedButton.setEnabled(true);
        }

        // Ask for confirmation
        int confirm = JOptionPane.showConfirmDialog(this,
                "Do you want to reserve seat " + seatNumber + "?",
                "Confirm Seat",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            selectedSeatNumber = seatNumber;
            selectedButton = button;
            selectedButton.setBackground(Color.BLUE);
            selectedButton.setEnabled(false);

            JOptionPane.showMessageDialog(this,
                    "Seat " + seatNumber + " temporarily reserved.\nProceed to payment to confirm booking.");

            // Proceed to booking confirmation and payment
            new BookingConfirmationFrame(selectedSeatNumber, flightID);
            dispose(); // Close the current frame
        } else {
            selectedSeatNumber = null;
            selectedButton = null;
        }
    }

    public String getSelectedSeatNumber() {
        return selectedSeatNumber;
    }

    public static void main(String[] args) {
        new SeatSelectionFrame(5);  // Replace with real flightID
    }
}
