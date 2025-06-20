import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class BookingManagement extends JPanel {
    private JTable bookingTable;
    private DefaultTableModel bookingTableModel;

    public BookingManagement() {
        setLayout(new BorderLayout());

        String[] columns = {"BookingID", "FullName", "Email", "FlightID", "FlightNumber", "Origin", "Destination", "DepartureTime", "ArrivalTime", "BookingDate", "TotalAmount", "Status"};
        bookingTableModel = new DefaultTableModel(columns, 0);
        bookingTable = new JTable(bookingTableModel);
        JScrollPane scrollPane = new JScrollPane(bookingTable);
        loadBookings();

        JButton cancelBtn = new JButton("Cancel Booking");
        cancelBtn.addActionListener(e -> cancelSelectedBooking());

        // Added Confirm Booking button
        JButton confirmBtn = new JButton("Confirm Booking");
        confirmBtn.addActionListener(e -> confirmSelectedBooking());

        JPanel btnPanel = new JPanel();
        btnPanel.add(cancelBtn);
        btnPanel.add(confirmBtn);  // Add Confirm button next to Cancel button

        add(scrollPane, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void loadBookings() {
        bookingTableModel.setRowCount(0);
        try (Connection conn = DBConnection.getConnection();
             CallableStatement cs = conn.prepareCall("{call ShowAllBookings}")) {
            ResultSet rs = cs.executeQuery();
            while (rs.next()) {
                bookingTableModel.addRow(new Object[]{
                        rs.getInt("BookingID"),
                        rs.getString("FullName"),
                        rs.getString("Email"),
                        rs.getInt("FlightID"),
                        rs.getString("FlightNumber"),
                        rs.getString("Origin"),
                        rs.getString("Destination"),
                        rs.getTimestamp("DepartureTime"),
                        rs.getTimestamp("ArrivalTime"),
                        rs.getDate("BookingDate"),
                        rs.getDouble("TotalAmount"),
                        rs.getString("Status")
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading bookings: " + ex.getMessage());
        }
    }

    private void cancelSelectedBooking() {
        int selectedRow = bookingTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a booking to cancel.");
            return;
        }

        int bookingID = (int) bookingTableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Cancel this booking?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DBConnection.getConnection();
                 CallableStatement cs = conn.prepareCall("{call CancelBooking(?)}")) {
                cs.setInt(1, bookingID);
                int updated = cs.executeUpdate();
                if (updated > 0) {
                    JOptionPane.showMessageDialog(this, "Booking cancelled.");
                    loadBookings();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to cancel.");
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error cancelling: " + ex.getMessage());
            }
        }
    }

    // New method for confirming booking
    private void confirmSelectedBooking() {
        int selectedRow = bookingTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a booking to confirm.");
            return;
        }

        int bookingID = (int) bookingTableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Confirm this booking?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DBConnection.getConnection();
                 CallableStatement cs = conn.prepareCall("{call ConfirmBooking(?)}")) {
                cs.setInt(1, bookingID);
                ResultSet rs = cs.executeQuery();
                if (rs.next()) {
                    int rowsAffected = rs.getInt("RowsAffected");
                    if (rowsAffected > 0) {
                        JOptionPane.showMessageDialog(this, "Booking confirmed.");
                        loadBookings();
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to confirm booking or already confirmed/cancelled.");
                    }
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error confirming booking: " + ex.getMessage());
            }
        }
    }
}