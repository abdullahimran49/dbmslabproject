import javax.swing.*;

public class SignupFrame extends JFrame {
    public SignupFrame() {
        setTitle("Signup - Airline Reservation");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 400);
        setLocationRelativeTo(null);
        setLayout(null);

        JLabel nameLabel = new JLabel("Full Name:");
        nameLabel.setBounds(30, 30, 100, 25);
        add(nameLabel);
        JTextField nameField = new JTextField();
        nameField.setBounds(150, 30, 200, 25);
        add(nameField);

        JLabel cnicLabel = new JLabel("CNIC:");
        cnicLabel.setBounds(30, 70, 100, 25);
        add(cnicLabel);
        JTextField cnicField = new JTextField();
        cnicField.setBounds(150, 70, 200, 25);
        add(cnicField);

        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setBounds(30, 110, 100, 25);
        add(emailLabel);
        JTextField emailField = new JTextField();
        emailField.setBounds(150, 110, 200, 25);
        add(emailField);

        JLabel passLabel = new JLabel("Password:");
        passLabel.setBounds(30, 150, 100, 25);
        add(passLabel);
        JPasswordField passField = new JPasswordField();
        passField.setBounds(150, 150, 200, 25);
        add(passField);

        JLabel phoneLabel = new JLabel("Phone:");
        phoneLabel.setBounds(30, 190, 100, 25);
        add(phoneLabel);
        JTextField phoneField = new JTextField();
        phoneField.setBounds(150, 190, 200, 25);
        add(phoneField);

        JLabel roleLabel = new JLabel("Role:");
        roleLabel.setBounds(30, 230, 100, 25);
        add(roleLabel);
        String[] roles = {"user", "admin"};
        JComboBox<String> roleBox = new JComboBox<>(roles);
        roleBox.setBounds(150, 230, 200, 25);
        add(roleBox);

        JButton registerBtn = new JButton("Register");
        registerBtn.setBounds(130, 280, 120, 30);
        add(registerBtn);

        registerBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String cnic = cnicField.getText().trim();
            String email = emailField.getText().trim();
            String pass = new String(passField.getPassword());
            String phone = phoneField.getText().trim();
            String role = roleBox.getSelectedItem().toString();

            // CNIC length check
            if (cnic.length() != 13 || !cnic.matches("\\d+")) {
                JOptionPane.showMessageDialog(this, "❌ CNIC must be exactly 13 digits.");
                return;
            }

            String msg = UserDAO.registerUser(name, cnic, email, pass, phone, role);
            JOptionPane.showMessageDialog(this, msg);
            if (msg.contains("success")) {
                new LoginFrame();
                dispose();
            }
        });

        registerBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String cnic = cnicField.getText().trim();
            String email = emailField.getText().trim();
            String pass = new String(passField.getPassword());
            String phone = phoneField.getText().trim();
            String role = roleBox.getSelectedItem().toString();

            String msg = UserDAO.registerUser(name, cnic, email, pass, phone, role);
            JOptionPane.showMessageDialog(this, msg);
            if (msg.contains("success")) {
                new LoginFrame();
                dispose();
            }
        });

        setVisible(true);
    }
}
