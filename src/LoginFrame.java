import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class LoginFrame extends JFrame {
    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginButton, signupButton;
    private JCheckBox showPasswordCheckBox;

    public LoginFrame() {
        setTitle("Airline Reservation - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(350, 250);
        setLocationRelativeTo(null);
        setLayout(null);

        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setBounds(30, 30, 80, 25);
        add(emailLabel);

        emailField = new JTextField();
        emailField.setBounds(120, 30, 180, 25);
        add(emailField);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(30, 70, 80, 25);
        add(passwordLabel);

        passwordField = new JPasswordField();
        passwordField.setBounds(120, 70, 180, 25);
        add(passwordField);

        showPasswordCheckBox = new JCheckBox("Show Password");
        showPasswordCheckBox.setBounds(120, 100, 150, 20);
        add(showPasswordCheckBox);
        showPasswordCheckBox.addActionListener(e -> {
            if (showPasswordCheckBox.isSelected()) {
                passwordField.setEchoChar((char) 0);
            } else {
                passwordField.setEchoChar('●');
            }
        });

        loginButton = new JButton("Login");
        loginButton.setBounds(30, 140, 90, 25);
        add(loginButton);

        signupButton = new JButton("Signup");
        signupButton.setBounds(130, 140, 90, 25);
        add(signupButton);

        loginButton.addActionListener(e -> handleLogin());
        signupButton.addActionListener(e -> {
            new SignupFrame();
            dispose();
        });

        setVisible(true);
    }

    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());

        User user = UserDAO.validateLogin(email, password);
        if (user != null) {
            JOptionPane.showMessageDialog(this, "Login successful as " + user.getRole());
            switch (user.getRole().toLowerCase()) {
                case "user":
                    new UserDashboard(user);
                    break;
                case "admin":
                    new AdminDashboard().setVisible(true);
                    break;
                case "superadmin":
                    new SuperAdminDashboard().setVisible(true);
                    break;
            }
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Invalid email or password.");
        }
    }

    public static void main(String[] args) {
        new LoginFrame();
    }
}
