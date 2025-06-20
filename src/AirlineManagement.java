import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class AirlineManagement extends JPanel {
    private JTable airlineTable;
    private DefaultTableModel airlineTableModel;

    public AirlineManagement() {
        setLayout(new BorderLayout());

        // Table setup
        String[] columns = {"AirlineID", "Name", "Country"};
        airlineTableModel = new DefaultTableModel(columns, 0);
        airlineTable = new JTable(airlineTableModel);
        JScrollPane scrollPane = new JScrollPane(airlineTable);

        // Load data
        loadAirlines();

        // Buttons
        JButton addAirlineBtn = new JButton("Add Airline");
        JButton deleteAirlineBtn = new JButton("Delete Selected Airline");

        addAirlineBtn.addActionListener(e -> addAirline());
        deleteAirlineBtn.addActionListener(e -> deleteAirline());

        JPanel btnPanel = new JPanel();
        btnPanel.add(addAirlineBtn);
        btnPanel.add(deleteAirlineBtn);

        add(scrollPane, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void loadAirlines() {
        airlineTableModel.setRowCount(0); // Clear existing rows

        try (Connection conn = DBConnection.getConnection();
             CallableStatement cs = conn.prepareCall("{call sp_GetAllAirlines()}");
             ResultSet rs = cs.executeQuery()) {

            while (rs.next()) {
                airlineTableModel.addRow(new Object[]{
                        rs.getInt("AirlineID"),
                        rs.getString("Name"),
                        rs.getString("Country")
                });
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading airlines: " + ex.getMessage());
        }
    }

    private void addAirline() {
        JTextField nameField = new JTextField();
        JTextField countryField = new JTextField();

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Airline Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Country:"));
        panel.add(countryField);

        while (true) {
            int result = JOptionPane.showConfirmDialog(this, panel, "Add New Airline", JOptionPane.OK_CANCEL_OPTION);
            if (result != JOptionPane.OK_OPTION) return;

            String name = nameField.getText().trim();
            String country = countryField.getText().trim();

            // Validations
            if (name.isEmpty() || name.length() < 3 || name.length() > 100 || !name.matches("[A-Za-z0-9 .&'-]+")) {
                JOptionPane.showMessageDialog(this, "Airline name must be 3–100 characters and contain valid characters.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
                continue;
            }
            if (country.isEmpty() || country.length() < 2 || country.length() > 50 || !country.matches("[A-Za-z ]+")) {
                JOptionPane.showMessageDialog(this, "Country must be 2–50 characters with letters and spaces only.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
                continue;
            }

            // Call stored procedure
            try (Connection conn = DBConnection.getConnection();
                 CallableStatement cs = conn.prepareCall("{call sp_AddAirline(?, ?)}")) {
                cs.setString(1, name);
                cs.setString(2, country);
                cs.execute();

                JOptionPane.showMessageDialog(this, "Airline added successfully.");
                loadAirlines();
                break;

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error adding airline: " + ex.getMessage());
                break;
            }
        }
    }

    private void deleteAirline() {
        int selectedRow = airlineTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an airline to delete.");
            return;
        }

        int airlineID = (int) airlineTableModel.getValueAt(selectedRow, 0);
        String airlineName = (String) airlineTableModel.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete airline: " + airlineName + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DBConnection.getConnection();
                 CallableStatement cs = conn.prepareCall("{call sp_DeleteAirline(?)}")) {
                cs.setInt(1, airlineID);
                int rows = cs.executeUpdate();

                if (rows > 0) {
                    JOptionPane.showMessageDialog(this, "Airline deleted successfully.");
                    loadAirlines();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete airline.");
                }

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error deleting airline: " + ex.getMessage());
            }
        }
    }
}