import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class SeatSelectionFrame extends JFrame {
    private int flightID;
    private Map<String, JButton> seatButtons = new HashMap<>();
    private String selectedSeatNumber = null;
    private JButton selectedButton = null;
    private int userID;
    private Map<String, String> seatClasses = new HashMap<>();

    public SeatSelectionFrame(int flightID, int userID) {
        this.flightID = flightID;
        this.userID = userID;

        setTitle("Seat Selection");
        setSize(500, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Legend Panel
        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        legendPanel.setBorder(BorderFactory.createTitledBorder("Legend"));

        legendPanel.add(createColorLabel(Color.GREEN, "Economy (Available)"));
        legendPanel.add(createColorLabel(Color.LIGHT_GRAY, "Economy (Booked)"));
        legendPanel.add(createColorLabel(Color.ORANGE, "Business (Available)"));
        legendPanel.add(createColorLabel(new Color(153, 0, 0), "Business (Booked)")); // Dark red
        legendPanel.add(createColorLabel(Color.BLUE, "Selected Seat"));

        add(legendPanel, BorderLayout.NORTH);

        // Seat Grid Panel
        JPanel seatGridPanel = new JPanel(new GridLayout(10, 3, 5, 5));
        add(seatGridPanel, BorderLayout.CENTER);

        loadSeatMap(seatGridPanel);

        setVisible(true);
    }

    private JLabel createColorLabel(Color color, String text) {
        JLabel label = new JLabel(text);
        label.setOpaque(true);
        label.setBackground(color);
        label.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        label.setPreferredSize(new Dimension(150, 25));

        // Automatically use white text for dark backgrounds
        if (isDarkColor(color)) {
            label.setForeground(Color.WHITE);
        }

        return label;
    }

    private boolean isDarkColor(Color color) {
        double luminance = (0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue()) / 255;
        return luminance < 0.5;
    }

    private void loadSeatMap(JPanel seatGridPanel) {
        try (Connection conn = DBConnection.getConnection()) {
            CallableStatement stmt = conn.prepareCall("{CALL GetSeatsByFlightID(?)}");
            stmt.setInt(1, flightID);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String seatNumber = rs.getString("SeatNumber");
                String seatClass = rs.getString("Class");
                boolean isAvailable = rs.getBoolean("IsAvailable");

                JButton seatButton = new JButton(seatNumber);
                seatButton.setEnabled(isAvailable);

                if (seatClass.equalsIgnoreCase("Economy")) {
                    seatButton.setBackground(isAvailable ? Color.GREEN : Color.LIGHT_GRAY);
                } else if (seatClass.equalsIgnoreCase("Business")) {
                    seatButton.setBackground(isAvailable ? Color.ORANGE : new Color(153, 0, 0));
                    seatButton.setForeground(Color.WHITE);
                }

                seatClasses.put(seatNumber, seatClass);

                if (isAvailable) {
                    seatButton.addActionListener(e -> handleSeatSelection(seatNumber, seatButton));
                }

                seatButtons.put(seatNumber, seatButton);
                seatGridPanel.add(seatButton);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void handleSeatSelection(String seatNumber, JButton button) {
        if (selectedButton != null) {
            String prevClass = seatClasses.get(selectedSeatNumber);
            if (prevClass.equalsIgnoreCase("Economy")) {
                selectedButton.setBackground(Color.GREEN);
            } else {
                selectedButton.setBackground(Color.ORANGE);
                selectedButton.setForeground(Color.BLACK);
            }
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to select seat " + seatNumber + "?",
                "Confirm Seat Selection",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            selectedSeatNumber = seatNumber;
            selectedButton = button;
            selectedButton.setBackground(Color.BLUE);
            selectedButton.setForeground(Color.WHITE);

            String seatClass = seatClasses.get(selectedSeatNumber);

            JOptionPane.showMessageDialog(this,
                    seatClass + " Seat " + seatNumber + " selected. Proceed to complete your booking.");

            new BookingConfirmationFrame(selectedSeatNumber, seatClass, flightID, userID);
            dispose();
        }
    }
}
