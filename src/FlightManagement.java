import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class FlightManagement extends JPanel {
    private JTable flightTable;
    private DefaultTableModel flightTableModel;

    public FlightManagement() {
        setLayout(new BorderLayout());

        String[] columns = {"FlightID", "Airline", "FlightNumber", "Origin", "Destination", "Departure", "Arrival", "Duration", "Price", "Status"};
        flightTableModel = new DefaultTableModel(columns, 0);
        flightTable = new JTable(flightTableModel);
        JScrollPane scrollPane = new JScrollPane(flightTable);
        loadFlights();

        JButton addBtn = new JButton("Add Flight");
        addBtn.addActionListener(e -> openAddFlightDialog());

        JButton updateBtn = new JButton("Update Flight");
        updateBtn.addActionListener(e -> openUpdateFlightDialog());

        JButton deleteBtn = new JButton("Delete Flight");
        deleteBtn.addActionListener(e -> deleteSelectedFlight());

        JPanel btnPanel = new JPanel();
        btnPanel.add(addBtn);
        btnPanel.add(updateBtn);
        btnPanel.add(deleteBtn);

        add(scrollPane, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void loadFlights() {
        flightTableModel.setRowCount(0);
        try (Connection conn = DBConnection.getConnection();
             CallableStatement stmt = conn.prepareCall("{call ShowAllFlights()}");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                flightTableModel.addRow(new Object[]{
                        rs.getInt("FlightID"), rs.getString("AirlineName"), rs.getString("FlightNumber"),
                        rs.getString("Origin"), rs.getString("Destination"), rs.getString("DepartureTime"),
                        rs.getString("ArrivalTime"), rs.getString("Duration"), rs.getBigDecimal("Price"),
                        rs.getString("Status")
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading flights: " + ex.getMessage());
        }
    }

    private void deleteSelectedFlight() {
        int selectedRow = flightTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a flight to delete.");
            return;
        }

        int flightID = (int) flightTableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DBConnection.getConnection();
                 CallableStatement stmt = conn.prepareCall("{call CancelFlight(?)}")) {
                stmt.setInt(1, flightID);
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Flight cancelled.");
                loadFlights();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error cancelling: " + ex.getMessage());
            }
        }
    }

    private void openAddFlightDialog() {
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "Add Flight", true);
        dialog.setLayout(new GridLayout(12, 2, 10, 10));
        dialog.setSize(400, 500);

        // Dropdown for airline
        JComboBox<String> airlineBox = new JComboBox<>();
        Map<String, Integer> airlineMap = new HashMap<>();

        // Load airline data into dropdown
        try (Connection conn = DBConnection.getConnection();
             CallableStatement cs = conn.prepareCall("{call sp_GetAirlines}");
             ResultSet rs = cs.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("AirlineID");
                String name = rs.getString("Name");
                airlineBox.addItem(name);
                airlineMap.put(name, id);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading airlines: " + ex.getMessage());
            return;
        }

        JTextField flightNumber = new JTextField();
        JTextField origin = new JTextField();
        JTextField destination = new JTextField();
        JTextField departure = new JTextField();
        JTextField arrival = new JTextField();
        JTextField duration = new JTextField();
        JTextField price = new JTextField();
        JComboBox<String> statusBox = new JComboBox<>(new String[]{"Scheduled", "Delayed"});

        JTextField economySeats = new JTextField();
        JTextField businessSeats = new JTextField();

        dialog.add(new JLabel("Airline:")); dialog.add(airlineBox);
        dialog.add(new JLabel("Flight #:")); dialog.add(flightNumber);
        dialog.add(new JLabel("Origin:")); dialog.add(origin);
        dialog.add(new JLabel("Destination:")); dialog.add(destination);
        dialog.add(new JLabel("Departure:")); dialog.add(departure);
        dialog.add(new JLabel("Arrival:")); dialog.add(arrival);
        dialog.add(new JLabel("Duration:")); dialog.add(duration);
        dialog.add(new JLabel("Price:")); dialog.add(price);
        dialog.add(new JLabel("Status:")); dialog.add(statusBox);
        dialog.add(new JLabel("Economy Seats:")); dialog.add(economySeats);
        dialog.add(new JLabel("Business Seats:")); dialog.add(businessSeats);

        JButton submitBtn = new JButton("Add Flight");
        submitBtn.addActionListener(e -> {
            try {
                String fNum = flightNumber.getText().trim();
                String ori = origin.getText().trim();
                String dest = destination.getText().trim();
                String dep = departure.getText().trim();
                String arr = arrival.getText().trim();
                String dur = duration.getText().trim();
                String priceText = price.getText().trim();
                String ecoSeatsText = economySeats.getText().trim();
                String busSeatsText = businessSeats.getText().trim();

                // Validations
                if (fNum.isEmpty() || ori.isEmpty() || dest.isEmpty() || dep.isEmpty() ||
                        arr.isEmpty() || dur.isEmpty() || priceText.isEmpty() ||
                        ecoSeatsText.isEmpty() || busSeatsText.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please fill all fields.");
                    return;
                }

                if (!fNum.matches("^[A-Z]{2}[0-9]{3,4}$")) {
                    JOptionPane.showMessageDialog(this, "Flight number must be format: AB123 or AB1234");
                    return;
                }

                BigDecimal p;
                try {
                    p = new BigDecimal(priceText);
                    if (p.compareTo(BigDecimal.ZERO) <= 0) {
                        JOptionPane.showMessageDialog(this, "Price must be greater than 0.");
                        return;
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Please enter valid price.");
                    return;
                }

                int ecoSeats, busSeats;
                try {
                    ecoSeats = Integer.parseInt(ecoSeatsText);
                    busSeats = Integer.parseInt(busSeatsText);
                    if (ecoSeats < 0 || busSeats < 0) {
                        JOptionPane.showMessageDialog(this, "Seat counts cannot be negative.");
                        return;
                    }
                    if (ecoSeats == 0 && busSeats == 0) {
                        JOptionPane.showMessageDialog(this, "At least one seat must be added.");
                        return;
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Please enter valid seat numbers.");
                    return;
                }

                String selectedAirline = (String) airlineBox.getSelectedItem();
                int airlineID = airlineMap.getOrDefault(selectedAirline, -1);
                if (airlineID == -1) {
                    JOptionPane.showMessageDialog(this, "Airline not found.");
                    return;
                }

                // Stored proc call
                try (Connection conn = DBConnection.getConnection();
                     CallableStatement stmt = conn.prepareCall("{call AddFlightWithSeats(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}")) {
                    stmt.setInt(1, airlineID);
                    stmt.setString(2, fNum);
                    stmt.setString(3, ori);
                    stmt.setString(4, dest);
                    stmt.setString(5, dep);
                    stmt.setString(6, arr);
                    stmt.setString(7, dur);
                    stmt.setBigDecimal(8, p);
                    stmt.setString(9, (String) statusBox.getSelectedItem());
                    stmt.setInt(10, ecoSeats);
                    stmt.setInt(11, busSeats);
                    stmt.executeUpdate();

                    JOptionPane.showMessageDialog(this, "Flight and seats added!");
                    dialog.dispose();
                    loadFlights();
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        dialog.add(new JLabel()); dialog.add(submitBtn);
        dialog.setVisible(true);
    }


    private int getAirlineIDByName(String airlineName) {
        try (Connection conn = DBConnection.getConnection();
             CallableStatement cs = conn.prepareCall("{call GetAirlineIDByName(?)}")) {
            cs.setString(1, airlineName);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("AirlineID");
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error retrieving airline ID: " + ex.getMessage());
        }
        return -1;
    }


    private void openUpdateFlightDialog() {
        int selectedRow = flightTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a flight to update.");
            return;
        }

        int flightID = (int) flightTableModel.getValueAt(selectedRow, 0);

        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "Update Flight", true);
        dialog.setLayout(new GridLayout(10, 2, 10, 10));
        dialog.setSize(400, 400);

        JComboBox<String> airlineBox = new JComboBox<>();
        Map<String, Integer> airlineMap = new HashMap<>();

        try (Connection conn = DBConnection.getConnection();
             CallableStatement cs = conn.prepareCall("{call sp_GetAirlines}");
             ResultSet rs = cs.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("AirlineID");
                String name = rs.getString("Name");
                airlineBox.addItem(name);
                airlineMap.put(name, id);
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading airlines: " + ex.getMessage());
            return;
        }


        JTextField flightNumber = new JTextField(flightTableModel.getValueAt(selectedRow, 2).toString());
        JTextField origin = new JTextField(flightTableModel.getValueAt(selectedRow, 3).toString());
        JTextField destination = new JTextField(flightTableModel.getValueAt(selectedRow, 4).toString());
        JTextField departure = new JTextField(flightTableModel.getValueAt(selectedRow, 5).toString());
        JTextField arrival = new JTextField(flightTableModel.getValueAt(selectedRow, 6).toString());
        JTextField duration = new JTextField(flightTableModel.getValueAt(selectedRow, 7).toString());
        JTextField price = new JTextField(flightTableModel.getValueAt(selectedRow, 8).toString());
        JComboBox<String> statusBox = new JComboBox<>(new String[]{"Scheduled", "Cancelled", "Delayed"});
        statusBox.setSelectedItem(flightTableModel.getValueAt(selectedRow, 9).toString());

        dialog.add(new JLabel("Airline:")); dialog.add(airlineBox);
        dialog.add(new JLabel("Flight #:")); dialog.add(flightNumber);
        dialog.add(new JLabel("Origin:")); dialog.add(origin);
        dialog.add(new JLabel("Destination:")); dialog.add(destination);
        dialog.add(new JLabel("Departure:")); dialog.add(departure);
        dialog.add(new JLabel("Arrival:")); dialog.add(arrival);
        dialog.add(new JLabel("Duration:")); dialog.add(duration);
        dialog.add(new JLabel("Price:")); dialog.add(price);
        dialog.add(new JLabel("Status:")); dialog.add(statusBox);

        JButton submitBtn = new JButton("Update Flight");
        submitBtn.addActionListener(e -> {
            try {
                int airlineID = airlineMap.get((String) airlineBox.getSelectedItem());
                String fNum = flightNumber.getText().trim();
                String ori = origin.getText().trim();
                String dest = destination.getText().trim();
                String dep = departure.getText().trim();
                String arr = arrival.getText().trim();
                String dur = duration.getText().trim();
                BigDecimal p = new BigDecimal(price.getText().trim());
                String stat = (String) statusBox.getSelectedItem();

                try (Connection conn = DBConnection.getConnection();
                     CallableStatement stmt = conn.prepareCall("{call UpdateFlight(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}")) {
                    stmt.setInt(1, flightID);
                    stmt.setInt(2, airlineID);
                    stmt.setString(3, fNum);
                    stmt.setString(4, ori);
                    stmt.setString(5, dest);
                    stmt.setString(6, dep);
                    stmt.setString(7, arr);
                    stmt.setString(8, dur);
                    stmt.setBigDecimal(9, p);
                    stmt.setString(10, stat);
                    stmt.executeUpdate();

                    JOptionPane.showMessageDialog(this, "Flight updated.");
                    dialog.dispose();
                    loadFlights();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        dialog.add(new JLabel()); dialog.add(submitBtn);
        dialog.setVisible(true);
    }
}