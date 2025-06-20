import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.Calendar;

public class PaymentDialogue extends JDialog {
    private final int bookingId, userId;
    private final double amount;
    private final String seatNumber;
    private JComboBox<PaymentMethod> paymentCombo;
    private JTextField cardNumberField, cardTypeField, expiryField;
    private boolean paymentSuccessful = false;

    public PaymentDialogue(JFrame parent, int bookingId, double amount, int userId, String seatNumber) {
        super(parent, "Payment", true);
        this.bookingId = bookingId;
        this.amount = amount;
        this.userId = userId;
        this.seatNumber = seatNumber;

        setupUI();
        loadPaymentMethods();
    }

    private void setupUI() {
        setSize(400, 300);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(6, 2, 5, 5));
        form.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        form.add(new JLabel("Amount:"));
        form.add(new JLabel("PKR " + String.format("%,.2f", amount)));

        form.add(new JLabel("Payment Method:"));
        paymentCombo = new JComboBox<>();
        paymentCombo.addActionListener(e -> updateFields());
        form.add(paymentCombo);

        form.add(new JLabel("Card Number:"));
        form.add(cardNumberField = new JTextField());

        form.add(new JLabel("Card Type:"));
        form.add(cardTypeField = new JTextField());

        form.add(new JLabel("Expiry (MM/YYYY):"));
        form.add(expiryField = new JTextField());

        JPanel buttons = new JPanel();
        JButton addNew = new JButton("Add New");
        addNew.addActionListener(e -> clearFields());

        JButton pay = new JButton("Pay");
        pay.addActionListener(e -> processPayment());

        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> dispose());

        buttons.add(addNew);
        buttons.add(pay);
        buttons.add(cancel);

        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
    }

    private void loadPaymentMethods() {
        paymentCombo.removeAllItems();
        try (Connection conn = DBConnection.getConnection();
             CallableStatement stmt = conn.prepareCall("{call GetUserPaymentMethods(?)}")) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                paymentCombo.addItem(new PaymentMethod(
                        rs.getInt("PaymentMethodID"),
                        rs.getString("CardNumber"),
                        rs.getString("CardType"),
                        rs.getDate("ExpiryDate")
                ));
            }
        } catch (SQLException e) {
            showError("Error loading payment methods: " + e.getMessage());
        }
    }

    private void updateFields() {
        PaymentMethod pm = (PaymentMethod) paymentCombo.getSelectedItem();
        boolean hasSelection = pm != null;

        if (hasSelection) {
            cardNumberField.setText(pm.cardNumber);
            cardTypeField.setText(pm.cardType);
            if (pm.expiryDate != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(pm.expiryDate);
                expiryField.setText(String.format("%02d/%04d",
                        cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR)));
            }
        }

        cardNumberField.setEditable(!hasSelection);
        cardTypeField.setEditable(!hasSelection);
        expiryField.setEditable(!hasSelection);
    }

    private void clearFields() {
        paymentCombo.setSelectedIndex(-1);
        cardNumberField.setText("");
        cardTypeField.setText("");
        expiryField.setText("");
        cardNumberField.setEditable(true);
        cardTypeField.setEditable(true);
        expiryField.setEditable(true);
    }

    private void processPayment() {
        if (seatNumber == null || seatNumber.trim().isEmpty()) {
            showError("Seat number not set.");
            return;
        }

        int flightId = executeCall("{call GetBookingFlightInfo(?, ?)}", bookingId);
        if (flightId == -1) {
            showError("Could not find booking information.");
            return;
        }

        int paymentMethodId = getPaymentMethodId();
        if (paymentMethodId == -1) return;

        if (executePaymentFlow(paymentMethodId, flightId, seatNumber)) {
            paymentSuccessful = true;
            JOptionPane.showMessageDialog(this, "Payment successful! Booking confirmed.");
            dispose();
        }
    }

    private int getPaymentMethodId() {
        PaymentMethod selected = (PaymentMethod) paymentCombo.getSelectedItem();
        if (selected != null) return selected.id;

        if (cardNumberField.getText().trim().isEmpty()) {
            showError("Please select a payment method or enter card details.");
            return -1;
        }

        return saveNewPaymentMethod();
    }

    private int saveNewPaymentMethod() {
        try {
            Date sqlDate = parseExpiryDate();
            if (sqlDate == null && !expiryField.getText().trim().isEmpty()) return -1;

            try (Connection conn = DBConnection.getConnection()) {
                executeStoredProc(conn, "{call ManageUserPaymentMethod(?, ?, ?, ?, ?, ?)}",
                        "insert", null, userId, cardNumberField.getText(),
                        cardTypeField.getText(), sqlDate);

                int newId = executeCall("{call GetLatestPaymentMethodID(?, ?, ?)}",
                        userId, cardNumberField.getText());
                loadPaymentMethods();
                return newId;
            }
        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
        }
        return -1;
    }

    private Date parseExpiryDate() {
        String expiry = expiryField.getText().trim();
        if (expiry.isEmpty()) return null;

        try {
            String[] parts = expiry.split("/");
            if (parts.length != 2) throw new IllegalArgumentException("Invalid format");

            Calendar cal = Calendar.getInstance();
            cal.set(Integer.parseInt(parts[1]), Integer.parseInt(parts[0]) - 1, 1);
            return new Date(cal.getTimeInMillis());
        } catch (Exception e) {
            showError("Invalid expiry date format. Use MM/YYYY");
            return null;
        }
    }

    private boolean executePaymentFlow(int paymentMethodId, int flightId, String seatNumber) {
        if (!executeBoolCall("{call CheckSeatAvailability(?, ?, ?)}", flightId, seatNumber)) {
            showError("Seat no longer available.");
            return false;
        }

        if (!executeBoolCall("{call RecordPayment(?, ?, ?, ?)}", bookingId, paymentMethodId, amount) ||
                !executeBoolCall("{call UpdateBookingAmount(?, ?, ?)}", bookingId, amount) ||
                !executeBoolCall("{call ReserveSeat(?, ?, ?)}", flightId, seatNumber)) {
            rollback();
            return false;
        }

        int seatId = executeCall("{call GetSeatID(?, ?, ?)}", flightId, seatNumber);
        if (seatId == -1 ||
                !executeBoolCall("{call LinkSeatToBooking(?, ?, ?)}", bookingId, seatId) ||
                !executeBoolCall("{call UpdateBookingStatus(?, ?, ?)}", bookingId, "Confirmed")) {
            rollback();
            return false;
        }

        return true;
    }

    private void rollback() {
        try (Connection conn = DBConnection.getConnection()) {
            executeStoredProc(conn, "{call RemovePaymentRecord(?, ?)}", bookingId);
            executeStoredProc(conn, "{call RemoveBookedSeatLink(?, ?)}", bookingId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int executeCall(String sql, Object... params) {
        try (Connection conn = DBConnection.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            stmt.registerOutParameter(params.length + 1, Types.INTEGER);
            stmt.execute();
            return stmt.getInt(params.length + 1);
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private boolean executeBoolCall(String sql, Object... params) {
        try (Connection conn = DBConnection.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            stmt.registerOutParameter(params.length + 1, Types.BIT);
            stmt.execute();
            return stmt.getBoolean(params.length + 1);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void executeStoredProc(Connection conn, String sql, Object... params) throws SQLException {
        try (CallableStatement stmt = conn.prepareCall(sql)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            stmt.execute();
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public boolean isPaymentSuccessful() {
        return paymentSuccessful;
    }

    static class PaymentMethod {
        final int id;
        final String cardNumber, cardType;
        final Date expiryDate;

        PaymentMethod(int id, String cardNumber, String cardType, Date expiryDate) {
            this.id = id;
            this.cardNumber = cardNumber;
            this.cardType = cardType;
            this.expiryDate = expiryDate;
        }

        @Override
        public String toString() {
            String masked = "xxxx-xxxx-xxxx-" + cardNumber.substring(Math.max(0, cardNumber.length() - 4));
            return masked + " (" + cardType + ")";
        }
    }
}