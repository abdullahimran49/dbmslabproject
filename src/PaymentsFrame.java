import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class PaymentsFrame extends JFrame {
    private JTable paymentsTable;
    private DefaultTableModel tableModel;
    private final int userId;

    public PaymentsFrame(int userId) {
        this.userId = userId;

        setTitle("Payment History");
        setSize(800, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Create table model
        String[] columns = {"Payment ID", "Booking ID", "Flight", "Payment Date", "Amount", "Status"};
        tableModel = new DefaultTableModel(columns, 0);
        paymentsTable = new JTable(tableModel);
        paymentsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        paymentsTable.setFillsViewportHeight(true);

        // Set column widths
        paymentsTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        paymentsTable.getColumnModel().getColumn(1).setPreferredWidth(80);
        paymentsTable.getColumnModel().getColumn(2).setPreferredWidth(200);
        paymentsTable.getColumnModel().getColumn(3).setPreferredWidth(150);
        paymentsTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        paymentsTable.getColumnModel().getColumn(5).setPreferredWidth(80);

        JScrollPane scrollPane = new JScrollPane(paymentsTable);
        add(scrollPane, BorderLayout.CENTER);

        // Create bottom panel for buttons
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        bottomPanel.add(closeButton);
        add(bottomPanel, BorderLayout.SOUTH);

        // Create header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Payment History", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        // Load payment data from stored procedure
        loadPaymentData();

        setVisible(true);
    }

    private void loadPaymentData() {
        tableModel.setRowCount(0); // Clear existing data

        try (Connection conn = DBConnection.getConnection()) {
            // Call stored procedure
            String query = "{CALL GetPaymentHistoryByUserId(?)}";
            CallableStatement stmt = conn.prepareCall(query);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String flightInfo = rs.getString("FlightNumber") + " - " + rs.getString("Route");

                tableModel.addRow(new Object[]{
                        rs.getInt("PaymentID"),
                        rs.getInt("BookingID"),
                        flightInfo,
                        rs.getTimestamp("PaymentDate"),
                        String.format("PKR %,.2f", rs.getDouble("Amount")),
                        rs.getString("Status")
                });
            }

            if (tableModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this,
                        "No payment records found.",
                        "Information",
                        JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading payment data: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // For testing
        SwingUtilities.invokeLater(() -> new PaymentsFrame(1));
    }
}
