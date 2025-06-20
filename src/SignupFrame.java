import javax.swing.*;

public class SignupFrame extends JFrame {
    public SignupFrame() {
        setTitle("Signup - Airline Reservation");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 350);  // Reduced height since role dropdown removed
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

        JButton registerBtn = new JButton("Register");
        registerBtn.setBounds(130, 240, 120, 30);
        add(registerBtn);

        JButton backToLoginBtn = new JButton("← Back to Login");
        backToLoginBtn.setBounds(130, 280, 200, 25);
        add(backToLoginBtn);

        // Register user with fixed "user" role
        registerBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String cnic = cnicField.getText().trim();
            String email = emailField.getText().trim();
            String pass = new String(passField.getPassword());
            String phone = phoneField.getText().trim();
            String role = "user";  // Only "user" can signup

            // Validation:
            if (name.isEmpty() || cnic.isEmpty() || email.isEmpty() || pass.isEmpty() || phone.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all fields.");
                return;
            }

            if (cnic.length() != 13 || !cnic.matches("\\d+")) {
                JOptionPane.showMessageDialog(this, "❌ CNIC must be exactly 13 digits.");
                return;
            }

            if (!email.contains("@")) {
                JOptionPane.showMessageDialog(this, "Please enter a valid email address containing '@'.");
                return;
            }

            if (pass.length() < 8) {
                JOptionPane.showMessageDialog(this, "Password must be at least 8 characters long.");
                return;
            }

            if (phone.length() != 11 || !phone.matches("\\d{11}")) {
                JOptionPane.showMessageDialog(this, "Phone number must be exactly 11 digits.");
                return;
            }

            String msg = UserDAO.registerUser(name, cnic, email, pass, phone, role);
            JOptionPane.showMessageDialog(this, msg);
            if (msg.toLowerCase().contains("success")) {
                new LoginFrame();
                dispose();
            }
        });

        backToLoginBtn.addActionListener(e -> {
            new LoginFrame();
            dispose();
        });

        setVisible(true);
    }
}